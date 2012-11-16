package com.compomics.util.gui;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 * Interface for parents of VisibleTableColumnsDialog.
 *
 * @author Harald Barsnes
 */
public interface VisibleTableColumnsDialogParent {

    /**
     * Sets the list of visible columns.
     *
     * @param visibleColumns map of the visible columns, key: model column index
     */
    public void setVisibleColumns(HashMap<Integer, Boolean> visibleColumns);

    /**
     * Returns the list of visible columns.
     *
     * @return the list of visible columns, key: model column index
     */
    public HashMap<Integer, Boolean> getVisibleColumns();
    
    /**
     * Returns the JTable for which to hide/show the column for.
     * 
     * @return the table
     */
    public JTable getTable();
    
    /**
     * Returns all the table columns, both visible and hidden.
     * 
     * @return all the table columns
     */
    public ArrayList<TableColumn> getAllTableColumns();
}
