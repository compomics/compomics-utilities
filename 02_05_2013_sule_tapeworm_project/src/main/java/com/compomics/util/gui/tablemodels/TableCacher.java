package com.compomics.util.gui.tablemodels;

import com.compomics.util.general.ExceptionHandler;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JTable;
import javax.swing.RowSorter;

/**
 * A Table cacher caches data before operation on tables.
 *
 * @author Marc Vaudel
 */
public class TableCacher {

    /**
     * Back-up of the ordering keys for self updating tables.
     */
    private HashMap<String, List<? extends RowSorter.SortKey>> orderingKeys = new HashMap<String, List<? extends RowSorter.SortKey>>();
    /**
     * An exception handler used to catch the exceptions.
     */
    private ExceptionHandler exceptionHandler;
    /**
     * Boolean indicating that the cacher is caching data.
     */
    private boolean caching = false;

    /**
     * Constructor.
     *
     * @param exceptionHandler the exception handler
     */
    public TableCacher(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Updates the ordering in a self updating table. If data is missing a
     * progress bar will appear during the loading.
     *
     * @param table the table to reorder
     * @param tableName a string designing this table
     * @param loadingMessage the message displayed in a cell when data is
     * missing
     * @param progressDialog a dialog allowing display of progress and
     * interruption of the process
     */
    public void cacheForSorting(JTable table, String tableName, String loadingMessage, ProgressDialogX progressDialog) {

        final SelfUpdatingTableModel tableModel = (SelfUpdatingTableModel) table.getModel();
        tableModel.setSelfUpdating(false);
        final JTable finalTable = table;
        final RowSorter rowSorter = table.getRowSorter();
        final List<? extends RowSorter.SortKey> newKeys = rowSorter.getSortKeys();
        final String finalTableName = tableName;
        final String finalLoadingMessage = loadingMessage;
        final ProgressDialogX finalProgressDialog = progressDialog;

        ArrayList<Integer> columnsToUpdate = new ArrayList<Integer>();
        for (RowSorter.SortKey key : newKeys) {
            int column = key.getColumn();
            if (tableModel.needsUpdate(column, loadingMessage)) {
                columnsToUpdate.add(column);
            }
        }

        if (!columnsToUpdate.isEmpty()) {

            caching = true;

            final ArrayList<Integer> finalColumnsToUpdate = columnsToUpdate;
            rowSorter.setSortKeys(orderingKeys.get(tableName));

            new Thread(new Runnable() {
                public void run() {
                    try {
                        finalProgressDialog.setVisible(true);
                    } catch (IndexOutOfBoundsException e) {
                        // ignore
                    }
                }
            }, "ProgressDialog").start();

            new Thread("SortThread") {
                @Override
                public void run() {
                    try {
                        tableModel.loadColumnsContent(finalColumnsToUpdate, finalLoadingMessage, finalProgressDialog);

                        if (!finalProgressDialog.isRunCanceled()) {
                            orderingKeys.put(finalTableName, newKeys);
                        } else {
                            finalTable.getTableHeader().setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                            finalProgressDialog.setRunFinished();
                            tableModel.setSelfUpdating(true);
                            caching = false;
                            return;
                        }

                    } catch (Exception ex) {
                        finalTable.getTableHeader().setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        finalProgressDialog.setRunFinished();
                        tableModel.setSelfUpdating(true);
                        caching = false;
                        exceptionHandler.catchException(ex);
                        return;
                    }

                    finalTable.getTableHeader().setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    finalProgressDialog.setRunFinished();
                    tableModel.setSelfUpdating(true);
                    caching = false;
                    rowSorter.setSortKeys(newKeys);
                }
            }.start();
        } else {
            tableModel.setSelfUpdating(true);
        }
    }

    /**
     * Indicates whether the cacher is caching data.
     *
     * @return true if the cache is being used
     */
    public boolean isCaching() {
        return caching;
    }
}
