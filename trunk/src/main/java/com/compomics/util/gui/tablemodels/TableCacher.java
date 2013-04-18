/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.gui.tablemodels;

import com.compomics.util.general.ExceptionHandler;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JTable;
import javax.swing.RowSorter;

/**
 * A Table cacher caches data before operation on tables
 *
 * @author Marc
 */
public class TableCacher {
    
    /**
     * Back-up of the ordering keys for self updating tables
     */
    private HashMap<String, List<? extends RowSorter.SortKey>> orderingKeys = new HashMap<String, List<? extends RowSorter.SortKey>>();
    /**
     * An exception handler used to catch the exceptions
     */
    private ExceptionHandler exceptionHandler;
    /**
     * Constructor
     */
    public TableCacher(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    /**
     * updates the ordering in a self updating table. If data is missing a progress bar will appear during the loading
     * @param table the table to reorder
     * @param tableName a string designing this table
     * @param loadingMessage the message displayed in a cell when data is missing
     * @param progressDialog a dialog allowing display of progress and interruption of the process
     */
    public void cacheForSorting(JTable table, String tableName, String loadingMessage, ProgressDialogX progressDialog) {

        final SelfUpdatingTableModel tableModel = (SelfUpdatingTableModel) table.getModel();
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
                        orderingKeys.put(finalTableName, newKeys);
                        rowSorter.setSortKeys(newKeys);
                    } catch (Exception ex) {
                        exceptionHandler.catchException(ex);
                    } finally {
                        finalProgressDialog.dispose();
                    }
                }
            }.start();
        }
    }
}
