package com.compomics.util.gui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A class containing simple GUI helper methods.
 *
 * @author Harald Barsnes
 */
public class GuiUtilities {

    /**
     * Returns the preferred width of a given cell in a table.
     *
     * @param table the table
     * @param colIndex the colum index
     * @param rowIndex the row index
     * @return the prefereed width of the cell
     */
    public static int getPreferredWidthOfCell(JTable table, int rowIndex, int colIndex) {

        int width = 0;

        // get width of column data
        TableCellRenderer renderer = table.getCellRenderer(rowIndex, colIndex);
        Component comp = renderer.getTableCellRendererComponent(
                table, table.getValueAt(rowIndex, colIndex), false, false, rowIndex, colIndex);
        width = Math.max(width, comp.getPreferredSize().width);

        return width;
    }
}
