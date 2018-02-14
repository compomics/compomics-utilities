package com.compomics.util.gui.tablemodels;

import com.compomics.util.gui.TableMouseWheelListener;
import com.compomics.util.gui.TableScrollBarListener;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerDummy;
import java.awt.Component;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * These table models include a self updating function. Due to instability of
 * the JTable with this model, it comprises a simple row sorter. Use it at your
 * own risks and feel free to debug.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public abstract class SelfUpdatingTableModel extends DefaultTableModel {

    /**
     * The view start index of the rows being loaded.
     */
    private int rowStartLoading = -1;
    /**
     * The view end index of the rows being loaded.
     */
    private int rowEndLoading = -1;
    /**
     * The number of rows loaded at a time.
     */
    private static final int BATCH_SIZE = 100;
    /**
     * Executor for the loading threads.
     */
    private final ExecutorService loadingPool = Executors.newFixedThreadPool(1);
    /**
     * Executor for the updating threads.
     */
    private final ExecutorService updatingPool = Executors.newFixedThreadPool(1);
    /**
     * If false, the table will not update automatically.
     */
    private boolean selfUpdating = true;
    /**
     * Mutex for the table update.
     */
    private Semaphore updateMutex = new Semaphore(1);
    /**
     * The last loading runnable.
     */
    private LoadingRunnable lastLoadingRunnable = null;
    /**
     * List of view indexes.
     */
    private ArrayList<Integer> viewIndexes = null;
    /**
     * Indicates which column was last changed.
     */
    private int lastColumnSorted = -1;
    /**
     * If true the current sorting is ascending.
     */
    private boolean sortAscending = false;
    /**
     * If true the table has not yet been sorted.
     */
    private boolean unsorted = true;
    /**
     * A progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * When true this indicates that the user is currently scrolling in the
     * table and that the table should not update.
     */
    public boolean isScrolling = false;

    /**
     * Loads the data needed for objects at rows of the given view indexes. Use
     * this method to cache data before working on the given lines.
     *
     * @param indexes the view indexes to load as a list. Shall not be empty or
     * null.
     * @param waitingHandler the waiting handler
     * @return the last updated row
     */
    protected abstract int loadDataForRows(ArrayList<Integer> indexes, WaitingHandler waitingHandler);

    /**
     * This method is called whenever an exception is encountered in a separate
     * thread.
     *
     * @param e the exception encountered
     */
    protected abstract void catchException(Exception e);

    /**
     * Calling this method indicates that data is missing at the given row. The
     * data will be loaded in a separate thread and the table updated later on.
     *
     * @param row the row number (not the view index)
     */
    protected void dataMissingAtRow(int row) {

        int anticipatedStart = (int) (row + 0.9 * BATCH_SIZE);

        if (lastLoadingRunnable == null || lastLoadingRunnable.isFinished()
                || row < rowStartLoading || row >= anticipatedStart) {

            rowStartLoading = row;
            rowEndLoading = Math.min(row + BATCH_SIZE, getRowCount() - 1);

            if (lastLoadingRunnable != null) {
                lastLoadingRunnable.cancel();
            }

            lastLoadingRunnable = new LoadingRunnable();

            loadingPool.execute(lastLoadingRunnable);

        }

        updateContent();
    }

    /**
     * Checks whether the protein table is filled properly and updates it later
     * on if not.
     *
     */
    private void updateContent() {

        if (selfUpdating && !updateMutex.tryAcquire()) {

            updatingPool.execute(() -> {
                
                try {
                    
                    if (selfUpdating) {
                        
                        fireTableDataChanged();
                        
                    }
                } catch (Exception e) {
                    
                    catchException(e);
                    
                }
                
                updateMutex.release();
                
            });
        }
    }

    /**
     * Indicates whether the table is in self update mode.
     *
     * @return true if the table is in self update mode
     */
    public boolean isSelfUpdating() {
        return selfUpdating;
    }

    /**
     * Sets whether the table is in self update mode.
     *
     * @param selfUpdating if false the table will not automatically update
     */
    public void setSelfUpdating(boolean selfUpdating) {
        this.selfUpdating = selfUpdating;
    }

    /**
     * Indicates whether the given column needs an update, i.e. whether a cell
     * contains waitingContent.
     *
     * @param column index of the column of interest
     * @param waitingContent the waiting content of this table
     * @return indicates whether the given column needs an update
     */
    public boolean needsUpdate(int column, String waitingContent) {
        for (int row = getRowCount() - 1; row >= 0; row--) {
            Object cellContent = getValueAt(row, column);
            if (cellContent instanceof String && cellContent.equals(waitingContent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initiates the sorter to the current order of the table.
     */
    public void initiateSorter() {
        int nRows = getRowCount();
        viewIndexes = new ArrayList<>(nRows);
        for (int row = 0; row < nRows; row++) {
            viewIndexes.add(row);
        }
    }

    /**
     * Returns the view index of the given row.
     *
     * @param row the row of interest
     * @return the corresponding view index
     */
    public int getViewIndex(int row) {
        if (viewIndexes == null) {
            return row;
        }
        int nRows = getRowCount();
        if (nRows != viewIndexes.size()) {
            initiateSorter();
        }
        if (row < 0 || row >= nRows) {
            nRows--;
            throw new IllegalArgumentException("Row " + row + " must be between 0 and " + nRows);
        }
        return viewIndexes.get(row);
    }

    /**
     * Returns the row number of the given view index.
     *
     * @param viewIndex view index row of interest
     * @return the corresponding row
     */
    public int getRowNumber(int viewIndex) {
        if (viewIndexes == null) {
            return viewIndex;
        }
        int nRows = getRowCount();
        if (nRows != viewIndexes.size()) {
            initiateSorter();
        }
        if (viewIndex < 0 || viewIndex >= nRows) {
            nRows--;
            throw new IllegalArgumentException("View index " + viewIndex + " must be between 0 and " + nRows + ".");
        }
        return viewIndexes.indexOf(viewIndex);
    }

    /**
     * Sorts the table according to a given column using the built in sorter.
     *
     * @param aProgressDialog a progress dialog used to display the progress and
     * interrupt the process
     */
    public void resetSorting(ProgressDialogX aProgressDialog) {

        if (!unsorted) {
            sortColumn(lastColumnSorted, aProgressDialog);

            if (!sortAscending) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Collections.reverse(viewIndexes);
                        fireTableDataChanged();
                    }
                });
            }
        }
    }

    /**
     * Sorts the table according to a given column using the built in sorter.
     *
     * @param column the column of interest
     * @param aProgressDialog a progress dialog used to display the progress and
     * interrupt the process
     */
    public void sort(int column, ProgressDialogX aProgressDialog) {

        if (column == lastColumnSorted) {

            // initate sorter if needed
            if (viewIndexes == null || viewIndexes.size() != getRowCount()) {
                initiateSorter();
            }

            sortAscending = !sortAscending; // same column, change sorting order
            Collections.reverse(viewIndexes);
            fireTableDataChanged();
        } else {
            sortAscending = true; // new column, sort acending 
            sortColumn(column, aProgressDialog);
        }
    }

    /**
     * Sort the given columns in ascending order.
     *
     * @param column the column to sort on
     * @param aProgressDialog a progress dialog
     */
    private void sortColumn(int column, ProgressDialogX aProgressDialog) {

        final int finalColumn = column;
        this.progressDialog = aProgressDialog;
        progressDialog.resetSecondaryProgressCounter();
        progressDialog.setTitle("Sorting. Please Wait...");
        progressDialog.setPrimaryProgressCounterIndeterminate(false);
        progressDialog.setMaxPrimaryProgressCounter(getRowCount());
        progressDialog.setValue(0);

        new Thread(new Runnable() {
            public void run() {
                try {
                    if (progressDialog != null) {
                        progressDialog.setVisible(true);
                    }
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();

        new Thread("SortThread") {
            @Override
            public void run() {

                try {
                    
                    setSelfUpdating(false);

                    initiateSorter();
                    lastColumnSorted = 0;

                    HashMap<Comparable, ArrayList<Integer>> valueToRowMap = new HashMap<>();
                    boolean comparable = false, string = false;

                    for (int row = 0; row < getRowCount() && (progressDialog != null && !progressDialog.isRunCanceled()); row++) {
                        Object tableValue = getValueAt(row, finalColumn);
                        Comparable key;
                        if (tableValue instanceof Comparable) {
                            key = (Comparable) tableValue;
                            comparable = true;
                        } else {
                            key = tableValue.toString();
                            string = true;
                        }
                        ArrayList<Integer> rows = valueToRowMap.get(key);
                        if (rows == null) {
                            rows = new ArrayList<>();
                            valueToRowMap.put(key, rows);
                        }
                        rows.add(row);
                        if (progressDialog != null) {
                            progressDialog.increasePrimaryProgressCounter();
                        }
                    }

                    if (progressDialog != null && progressDialog.isRunCanceled()) {
                        progressDialog.setRunFinished();
                        return;
                    }

                    ArrayList<Comparable> keys = new ArrayList<>(valueToRowMap.keySet());
                    if (string && comparable) {
                        ArrayList<Comparable> stringValues = new ArrayList<>();
                        for (Comparable value : keys) {
                            stringValues.add(value.toString());
                        }
                        keys = stringValues;
                    }

                    if (progressDialog == null || !progressDialog.isRunCanceled()) {
                        viewIndexes = new ArrayList<>();
                        Collections.sort(keys);
                        for (Comparable key : keys) {
                            viewIndexes.addAll(valueToRowMap.get(key));
                        }
                        lastColumnSorted = finalColumn;
                    }

                } catch (Exception ex) {
                    catchException(ex);
                } finally {
                    setSelfUpdating(true);
                }
                if (progressDialog != null) {
                    progressDialog.setRunFinished();
                }
                fireTableDataChanged();
            }
        }.start();
    }

    /**
     * Convenience method adding a row sorter listener to the given JTable.
     *
     * @param jTable the table to add the resetSorting listener to
     * @param progressDialog progress dialog used to display progress or cancel
     * while sorting. Can be null.
     */
    public static void addSortListener(JTable jTable, ProgressDialogX progressDialog) {

        final JTableHeader proteinTableHeader = jTable.getTableHeader();
        final JTable finalTable = jTable;
        final ProgressDialogX progressDialogX = progressDialog;

        proteinTableHeader.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON1) {
                    int column = proteinTableHeader.getColumnModel().getColumnIndexAtX(evt.getX());
                    SelfUpdatingTableModel model = (SelfUpdatingTableModel) finalTable.getModel();
                    model.sort(column, progressDialogX);
                    model.unsorted = false;
                }
            }
        });

        // set the arrows indicating the current resetSorting order
        final TableCellRenderer r = finalTable.getTableHeader().getDefaultRenderer();
        TableCellRenderer wrapper = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    label.setIcon(getSortIcon(table, column));
                    label.setHorizontalTextPosition(SwingConstants.LEFT); // @TODO: align text to the left and the icon to the right...
                    finalTable.getTableHeader().revalidate();
                    finalTable.getTableHeader().repaint();
                }
                return comp;
            }

            /**
             * Implements the logic to choose the appropriate icon.
             */
            private Icon getSortIcon(JTable table, int column) {
                if (table.getModel() instanceof SelfUpdatingTableModel
                        && ((SelfUpdatingTableModel) table.getModel()).lastColumnSorted == column
                        && !((SelfUpdatingTableModel) table.getModel()).unsorted) {
                    if (((SelfUpdatingTableModel) table.getModel()).sortAscending == false) {
                        return UIManager.getIcon("Table.descendingSortIcon");
                    } else {
                        return UIManager.getIcon("Table.ascendingSortIcon");
                    }
                } else {
                    return null;
                }
            }
        };

        finalTable.getTableHeader().setDefaultRenderer(wrapper);
    }

    /**
     * Runnable used for the loading and its interruption.
     */
    private class LoadingRunnable implements Runnable {

        /**
         * The waiting handler.
         */
        private WaitingHandlerDummy waitingHandler = new WaitingHandlerDummy();

        @Override
        public synchronized void run() {
            
            try {
            
                ArrayList<Integer> viewIndexes = new ArrayList<>(BATCH_SIZE);
                
                for (int row = rowStartLoading; row <= rowEndLoading; row++) {

                    viewIndexes.add(getViewIndex(row));

                }

                if (!viewIndexes.isEmpty() && !waitingHandler.isRunCanceled()) {

                    rowEndLoading = getRowNumber(loadDataForRows(viewIndexes, waitingHandler));

                }

            } catch (Exception e) {

                catchException(e);

            }

            waitingHandler.setRunFinished();

        }

        /**
         * Cancels the thread.
         */
        public void cancel() {
            waitingHandler.setRunCanceled();
        }

        /**
         * Indicates whether the run is finished.
         *
         * @return true if the thread is finished.
         */
        public boolean isFinished() {
            return waitingHandler.isRunFinished();
        }
    }

    /**
     * Indicates whether the table is currently being scrolled.
     *
     * @return true if the table is currently being scrolled
     */
    public boolean isScrolling() {
        return isScrolling;
    }

    /**
     * Set if the user is currently scrolling or not.
     *
     * @param isScrolling the isScrolling to set
     */
    public void setIsScrolling(boolean isScrolling) {
        
        this.isScrolling = isScrolling;

        if (isScrolling && lastLoadingRunnable != null) {
            
            lastLoadingRunnable.cancel();
        
        }
    }

    /**
     * Add scroll bar and mouse wheel listeners.
     *
     * @param table the table
     * @param scrollBar the scroll bar
     * @param scrollPane the scroll pane
     */
    public static void addScrollListeners(JTable table, JScrollPane scrollPane, JScrollBar scrollBar) {

        // add scroll bar listener
        AdjustmentListener scrollBarListener = new TableScrollBarListener(table);
        scrollBar.addAdjustmentListener(scrollBarListener);

        // add mouse wheel listener
        TableMouseWheelListener mouseWheelListener = new TableMouseWheelListener(table);
        scrollPane.addMouseWheelListener(mouseWheelListener);
    }
}
