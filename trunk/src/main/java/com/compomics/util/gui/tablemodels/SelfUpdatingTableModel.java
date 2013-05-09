package com.compomics.util.gui.tablemodels;

import com.compomics.util.gui.waiting.WaitingHandler;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * These table models include a self updating function. Due to instability of
 * the JTable with this model, it comprises a simple row sorter. Use it at your
 * own risks and feel free to debug.
 *
 * @author Marc Vaudel
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
    private static int batchSize = 100;
    /**
     * If false, the table will not update automatically.
     */
    private boolean selfUpdating = true;
    /**
     * boolean indicating whether an update was scheduled.
     */
    private boolean updateScheduled = false;
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
    private int lastColumnSorted = 0;

    /**
     * Loads the data needed for objects at rows of the given view indexes. Use
     * this method to cache data before working on the given lines.
     *
     * @param indexes the view indexes to load as a list. Shall not be empty or
     * null.
     * @param interrupted a boolean indicating whether the loading shall be
     * stopped
     * @return the last updated row
     */
    protected abstract int loadDataForRows(ArrayList<Integer> indexes, boolean interrupted);

    /**
     * Loads the data for a column. Use this method to cache data before working
     * on the given column.
     *
     * @param column the column number
     * @param waitingHandler a waiting handler used to display progress to the
     * user or interrupt the process
     */
    protected abstract void loadDataForColumn(int column, WaitingHandler waitingHandler);

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
     * @throws InterruptedException
     */
    protected void dataMissingAtRow(int row) throws InterruptedException {

        int anticipatedStart = (int) (row + 0.9 * batchSize);

        if (lastLoadingRunnable == null || lastLoadingRunnable.isFinished()
                || row < rowStartLoading || row >= anticipatedStart) {

            rowStartLoading = row;
            rowEndLoading = Math.min(row + batchSize, getRowCount() - 1);

            if (lastLoadingRunnable != null) {
                lastLoadingRunnable.cancel();
            }

            lastLoadingRunnable = new LoadingRunnable();

            new Thread(lastLoadingRunnable, "identificationFeatures").start();
        }

        updateContent();
    }

    /**
     * Checks whether the protein table is filled properly and updates it later
     * on if not.
     *
     * @throws InterruptedException
     */
    private synchronized void updateContent() throws InterruptedException {
        if (selfUpdating && !updateScheduled) {
            updateScheduled = true;
            wait(100);
            new Thread(new Runnable() {
                public synchronized void run() {
                    try {
                        if (selfUpdating) {
                            fireTableDataChanged();
                        }
                    } catch (Exception e) {
                        catchException(e);
                    }
                    updateScheduled = false;
                }
            }, "tableUpdate").start();
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
        viewIndexes = new ArrayList<Integer>(nRows);
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
            throw new IllegalArgumentException("View index " + viewIndex + " must be between 0 and " + nRows);
        }
        return viewIndexes.indexOf(viewIndex);
    }

    /**
     * Sorts the table according to a given column using the built in sorter.
     *
     * @param column the column of interest
     * @param progressDialog a progress dialog used to display the progress and
     * interrupt the process
     */
    public void sort(int column, ProgressDialogX progressDialog) {

        if (column == lastColumnSorted) {
            if (viewIndexes == null || viewIndexes.size() != getRowCount()) {
                initiateSorter();
            }
            Collections.reverse(viewIndexes);
            fireTableDataChanged();
        } else {

            final int finalColumn = column;
            final ProgressDialogX finalProgressDialog = progressDialog;
            finalProgressDialog.resetSecondaryProgressBar();
            finalProgressDialog.setTitle("Sorting. Please Wait...");
            finalProgressDialog.setIndeterminate(false);
            finalProgressDialog.setMaxProgressValue(getRowCount());
            finalProgressDialog.setValue(0);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (finalProgressDialog != null) {
                            finalProgressDialog.setVisible(true);
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
                        loadDataForColumn(finalColumn, finalProgressDialog);

                        initiateSorter();
                        lastColumnSorted = 0;

                        HashMap<Comparable, ArrayList<Integer>> valueToRowMap = new HashMap<Comparable, ArrayList<Integer>>();
                        boolean comparable = false, string = false;

                        for (int row = 0; row < getRowCount() && (finalProgressDialog != null && !finalProgressDialog.isRunCanceled()); row++) {
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
                                rows = new ArrayList<Integer>();
                                valueToRowMap.put(key, rows);
                            }
                            rows.add(row);
                            if (finalProgressDialog != null) {
                                finalProgressDialog.increaseProgressValue();
                            }
                        }

                        if (finalProgressDialog != null && finalProgressDialog.isRunCanceled()) {
                            finalProgressDialog.setRunFinished();
                            return;
                        }

                        ArrayList<Comparable> keys = new ArrayList<Comparable>(valueToRowMap.keySet());
                        if (string && comparable) {
                            ArrayList<Comparable> stringValues = new ArrayList<Comparable>();
                            for (Comparable value : keys) {
                                stringValues.add(value.toString());
                            }
                            keys = stringValues;
                        }

                        if (finalProgressDialog == null || !finalProgressDialog.isRunCanceled()) {
                            viewIndexes = new ArrayList<Integer>();
                            Collections.sort(keys, Collections.reverseOrder());
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
                    if (finalProgressDialog != null) {
                        finalProgressDialog.setRunFinished();
                    }
                    fireTableDataChanged();
                }
            }.start();
        }
    }

    /**
     * Convenience method adding a row sorter listener to the given JTable.
     *
     * @param jTable
     * @param progressDialog progress dialog used to display progress or cancel
     * while sorting. Can be null.
     */
    public static void addSortListener(JTable jTable, ProgressDialogX progressDialog) {
        final JTableHeader proteinTableHeader = jTable.getTableHeader();
        final JTable finalTable = jTable;
        final ProgressDialogX progressDialogX = progressDialog;
        proteinTableHeader.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                int column = proteinTableHeader.getColumnModel().getColumnIndexAtX(evt.getX());
                SelfUpdatingTableModel model = (SelfUpdatingTableModel) finalTable.getModel();
                model.sort(column, progressDialogX);
            }
        });
    }

    /**
     * Runnable used for the loading and its interruption.
     */
    private class LoadingRunnable implements Runnable {

        /**
         * Boolean indicating whether the thread shall be interrupted.
         */
        private boolean interrupted = false;
        /**
         * Boolean indicating whether the thread shall be interrupted.
         */
        private boolean finished = false;

        @Override
        public synchronized void run() {
            try {
                ArrayList<Integer> viewIndexes = new ArrayList<Integer>(batchSize);
                for (int row = rowStartLoading; row <= rowEndLoading; row++) {
                    viewIndexes.add(getViewIndex(row));
                }
                if (!viewIndexes.isEmpty() && !interrupted) {
                    rowEndLoading = getRowNumber(loadDataForRows(viewIndexes, interrupted));
                }
            } catch (Exception e) {
                catchException(e);
            }
            finished = true;
        }

        /**
         * Cancels the thread.
         */
        public void cancel() {
            interrupted = true;
        }

        /**
         * Indicates whether the run is finished.
         *
         * @return true if the thread is finished.
         */
        public boolean isFinished() {
            return finished;
        }
    }
}
