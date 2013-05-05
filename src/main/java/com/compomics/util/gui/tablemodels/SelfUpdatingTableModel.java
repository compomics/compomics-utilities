package com.compomics.util.gui.tablemodels;

import com.compomics.util.gui.waiting.WaitingHandler;
import java.util.ArrayList;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;

/**
 * These table models include a self updating function.
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
     * The row sorter used for the given table. The sorter is used to preload
     * the data, ignored if null.
     */
    private RowSorter sorter = null;
    /**
     * A boolean indicating that the table shall be updated only when all data is loaded
     */
    private boolean updateWhenComplete = false;

    /**
     * Loads the data needed for rows between start and end (inclusively).
     *
     * @param rows the rows model indexes to load as a list. Shall not be empty
     * or null.
     * @param interrupted a boolean indicating whether the loading shall be
     * stopped
     * @return the last updated row
     */
    protected abstract int loadDataForRows(ArrayList<Integer> rows, boolean interrupted);

    /**
     * Loads the data for a column.
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
     * @param row the row number
     * @throws InterruptedException
     */
    protected void dataMissingAtRow(int row) throws InterruptedException {

        if (sorter != null) {
            row = sorter.convertRowIndexToView(row);
        }

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
                        if (selfUpdating
                                && (!updateWhenComplete || lastLoadingRunnable.isFinished())) {
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
     * Loads the content of a column without updating the table.
     *
     * @param columns the column of interest
     * @param waitingContent the content of a cell indicating that loading is
     * not completed
     * @param waitingHandler waiting handler for display of the progress or
     * loading interruption (can be null)
     * @throws InterruptedException
     */
    public synchronized void loadColumnsContent(ArrayList<Integer> columns, String waitingContent, WaitingHandler waitingHandler) throws InterruptedException {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressValue(columns.size() * getRowCount());
            waitingHandler.setSecondaryProgressValue(0);
        }
        for (int column : columns) {
            loadDataForColumn(column, waitingHandler);
        }
    }

    /**
     * Sets the row sorter used for the table. The sorter will be used to
     * preload data.
     *
     * @param sorter the row sorter used for the table
     */
    public void setRowSorter(RowSorter sorter) {
        this.sorter = sorter;
    }
    
    /**
     * Sets whether the table content shall be updated only once the loading is complete
     * @param updateWhenComplete 
     */
    public void setUpdateWhenComplete(boolean updateWhenComplete) {
        this.updateWhenComplete = updateWhenComplete;
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
                ArrayList<Integer> indexes = new ArrayList<Integer>(batchSize);
                for (int row = rowStartLoading; row <= rowEndLoading; row++) {
                    if (sorter == null) {
                        indexes.add(row);
                    } else {
                        indexes.add(sorter.convertRowIndexToModel(row));
                    }
                }
                if (!indexes.isEmpty() && !interrupted) {
                    rowEndLoading = loadDataForRows(indexes, interrupted);
                    if (sorter != null) {
                        rowEndLoading = sorter.convertRowIndexToView(rowEndLoading);
                    }
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
