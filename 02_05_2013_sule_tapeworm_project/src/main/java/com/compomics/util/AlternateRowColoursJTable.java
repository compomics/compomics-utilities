/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 02-Aug-2009
 * Time: 13:47:48
 */
package com.compomics.util;
import org.apache.log4j.Logger;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.*;
import java.awt.*;
import java.util.Vector;
/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2009/08/02 13:23:46 $
 */

/**
 * This class provides a simple extension on a JTable, allowing even and uneven rows to
 * have different colours. By default, the uneven row numbers get the default background
 * (corresponding to getUnevenRowColor == null), while the even rows get the colour LIGHT_AQUA,
 * defined as a constant on this class. Both even and uneven row colours can be defined by the
 * caller, where setting the uneven or even rows colours to 'null' results in default behaviour
 * for those columns.
 *
 * @author Lennart Martens
 * @version $Id: AlternateRowColoursJTable.java,v 1.1 2009/08/02 13:23:46 lennart Exp $
 */
public class AlternateRowColoursJTable extends JTable {

    // Class specific log4j logger for AlternateRowColoursJTable instances.
    Logger logger = Logger.getLogger(AlternateRowColoursJTable.class);

    /**
     * The background colour for the even rows.
     * A value of 'null' means use the default background colour for JTable.
     */
    private Color iEvenRowColor = LIGHT_AQUA;

    /**
     * The background colour for the uneven rows.
     * A value of 'null' (default here) means use the default background colour for JTable.
     */
    private Color iUnevenRowColor = null;

    /**
     * A rather pronounced AQUA colour (r=183, g=222, b=232).
     */
    public static final Color AQUA = new Color(183, 222, 232);

    /**
     * A soft-tone AQUA colour (r=219, g=238, b=244). this is the default
     * background colour for even numbered rows.
     */
    public static final Color LIGHT_AQUA = new Color(219, 238, 244);

    /**
     * Default empty constructor.
     */
    public AlternateRowColoursJTable() {}

    /**
     * Creates a new AlternateRowColoursJTable.
     *
     * @param aRows the number of rows
     * @param aCols the number of columns
     */
    public AlternateRowColoursJTable(int aRows, int aCols){
        super(aRows, aCols);
    }

    /**
     * Creates a new AlternateRowColoursJTable.
     *
     * @param dm the table model
     * @param cm the table column model
     */
    public AlternateRowColoursJTable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
    }

    /**
     * Creates a new AlternateRowColoursJTable.
     *
     * @param dm the table model
     */
    public AlternateRowColoursJTable(TableModel dm) {
        super(dm);
    }

    /**
     * Creates a new AlternateRowColoursJTable.
     *
     * @param dm the table model
     * @param cm the table column model
     * @param sm the list selection mocel
     */
    public AlternateRowColoursJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
    }

    /**
     * Creates a new AlternateRowColoursJTable.
     *
     * @param rowData the row data vector
     * @param columnNames the column name vector
     */
    public AlternateRowColoursJTable(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
    }

    /**
     * Creates a new AlternateRowColoursJTable.
     *
     * @param rowData the row data vector
     * @param columnNames the column name vector
     */
    public AlternateRowColoursJTable(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
    }

    /**
     * Prepares the renderer by querying the data model for the
     * value and selection state
     * of the cell at <code>row</code>, <code>column</code>.
     * Returns the component (may be a <code>Component</code>
     * or a <code>JComponent</code>) under the event location.
     *
     * @param renderer  the <code>TableCellRenderer</code> to prepare
     * @param rowIndex  the row of the cell to render, where 0 is the first row
     * @param vColIndex the column of the cell to render,
     *			where 0 is the first column
     * @return          the <code>Component</code> under the event location
     */
    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
        if(!isCellSelected(rowIndex, vColIndex)) {
            if ((rowIndex+1) % 2 == 0) {
                if(iEvenRowColor == null) {
                    c.setBackground(getBackground());
                } else {
                    c.setBackground(iEvenRowColor);
                }
            } else {
                if(iUnevenRowColor == null) {
                    c.setBackground(getBackground());
                } else {
                    c.setBackground(iUnevenRowColor);
                }
            }
        }
        return c;
    }

    /**
     * Sets the colour of the even numbered rows; by default, the LIGHT_AQUA background color will be used..
     * The default JTable Color can be set by providing a 'null' value here.
     *
     * @param aEvenRowColor   Color to use for the even numbered rows. Is LIGHT_AQUA by default,
     *                        and the JTable default Color can be set by specifying 'null' here.
     */
    public void setEvenRowColor(Color aEvenRowColor) {
        iEvenRowColor = aEvenRowColor;
    }

    /**
     * Sets the colour of the uneven numbered rows; by default, the default JTable background color will be used..
     * This default JTable background Color can always be set by providing a 'null' value here.
     *
     * @param aUnevenRowColor Color to use for the uneven numbered rows. Is the default JTable background by default,
     *                        and this value can be set by specifying 'null' here.
     */
    public void setUnevenRowColor(Color aUnevenRowColor) {
        iUnevenRowColor = aUnevenRowColor;
    }

    /**
     * This method returns the Color used for the even numbered rows, or 'null' if the
     * default JTable background color is maintained for these rows.
     *
     * @return Color    with the Color used for the even numbered rows, or 'null' if the default
     *                  JTable background Color is used.
     */
    public Color getEvenRowColor() {
        return iEvenRowColor;
    }

    /**
     * This method returns the Color used for the uneven numbered rows, or 'null' if the
     * default JTable background color is maintained for these rows.
     *
     * @return Color    with the Color used for the uneven numbered rows, or 'null' if the default
     *                  JTable background Color is used.
     */
    public Color getUnevenRowColor() {
        return iUnevenRowColor;
    }
}
