/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-dec-02
 * Time: 13:54:36
 */
package com.compomics.util.db;
import org.apache.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.util.Vector;
import java.io.Writer;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2007/10/16 10:07:37 $
 */

/**
 * This class wraps a DB resultset in an 'offline' object.
 *
 * @author Lennart Martens
 */
public class DBResultSet extends AbstractTableModel {
	// Class specific log4j logger for DBResultSet instances.
	Logger logger = Logger.getLogger(DBResultSet.class);

    /**
     * The number of columns in this resultset.
     */
    private int iColCount = 0;

    /**
     * The number of rows in this resultset.
     */
    private int iRowCount = 0;

    /**
     * The column names in this resultset.
     */
    private String[] iColNames = null;

    /**
     * The data.
     */
    private Object[][] iData = null;

    /**
     * Default constructor, just creates an empty resultset.
     */
    public DBResultSet() {
        iColNames = new String[]{};
        iData = new Object[][]{{}};
    }

    /**
     * This constructor takes a ResultSet from which the data
     * is read. This ResultSet will not be closed by this constructor,
     * so it remains available and the closing is up to the caller!
     * Nullreplace is FALSE for this constructor!
     *
     * @param   aRS ResultSet from which to read the data. Closing the ResultSet
     *              is up to the caller!!
     * @exception   SQLException    whenever reading the resultset failed.
     */
    public DBResultSet(ResultSet aRS) throws SQLException {
        this(aRS, false);
    }

    /**
     * This constructor takes a ResultSet from which the data
     * is read. This ResultSet will not be closed by this constructor,
     * so it remains available and the closing is up to the caller!
     * Notice that the null replace flag allows you to replace 'null' values
     * returned by the DB to the String "(null)".
     *
     * @param   aRS ResultSet from which to read the data. Closing the ResultSet
     *              is up to the caller!!
     * @param   aNullReplace    boolean to indicate whether 'null' values returned by
     *                          the DB should be converted into "(null)" Strings ('true')
     *                          or not ('false').
     * @exception   SQLException    whenever reading the resultset failed.
     */
    public DBResultSet(ResultSet aRS, boolean aNullReplace) throws SQLException {
        ResultSetMetaData rsmd = aRS.getMetaData();
        // ColCount.
        iColCount = rsmd.getColumnCount();
        iColNames = new String[iColCount];
        // Col names.
        for(int i=0;i<iColCount;i++) {
            iColNames[i] = rsmd.getColumnLabel(i+1);
        }
        // Cycle rows.
        Vector rows = new Vector();
        while(aRS.next()) {
            Object[] temp = new Object[iColCount];
            for(int i = 0; i < temp.length; i++) {
                temp[i] = aRS.getObject(i+1);
                if(aNullReplace) {
                    // Change 'null' values with the String (null).
                    if(temp[i] == null) {
                        temp[i] = "(null)";
                    }
                }
            }
            rows.add(temp);
        }
        // Transform Vector.
        iRowCount = rows.size();
        iData = new Object[iRowCount][iColCount];
        for(int i = 0; i < iData.length; i++) {
            iData[i] = (Object[])rows.get(i);
        }
    }

    /**
     * This method reports on all the columnn ames.
     *
     * @return  String[]    with all the column names.
     */
    public String[] getColumnNames() {
        return this.iColNames;
    }

    /**
     * This method reports on the data stored in the resultset.
     *
     * @return  Object[][]  with the data.
     */
    public Object[][] getData() {
        return this.iData;
    }

    /**
     * This method reports on the number of columns in the resultset.
     *
     * @return  int with the columncount.
     */
    public int getColumnCount() {
        return this.iColCount;
    }

    /**
     * This method reports on the number of rows in the resultset.
     *
     * @return  int with the rowcount.
     */
    public int getRowCount() {
        return this.iRowCount;
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex 	the column whose value is to be queried
     * @return	the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return iData[rowIndex][columnIndex];
    }

    /**
     *  Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
     *
     *  @param columnIndex  the column being queried
     *  @return the Object.class
     */
    public Class getColumnClass(int columnIndex) {
        // If a data column conrtains only 'null', we should
        // give a Class for it anyway, since it requires a renderer!
        // String seems to be the natural choice.
        Class result = String.class;
        // Cycle the whole column to see if there is a non-null value.
        // If there is any, take it and break the loop. If there is none,
        // we still have the default String class.
        for (int i = 0; i < iData.length; i++) {
            if(iData[i][columnIndex] != null) {
                result = iData[i][columnIndex].getClass();
                break;
            }
        }
        return result;
    }

    /**
     *  Returns a default name for the column using spreadsheet conventions:
     *  A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
     *  returns an empty string.
     *
     * @param column  the column being queried
     * @return a string containing the default name of <code>column</code>
     */
    public String getColumnName(int column) {
        return iColNames[column];
    }

    /**
     * This method allows the caller to write the current dataset to the
     * specified Writer. Note that flushing and closing the writer is up to the
     * caller.
     *
     * @param   out Writer to print to. Note that flushing and closing this
     *              writer is up to the caller.
     * @param   aSeparator  String with the separator character(s) to use.
     * @exception   IOException when something goes wrong.
     */
    public void writeToCSVFile(Writer out, String aSeparator) throws IOException {
        // Write headers first.
        for(int i=0;i<iColCount;i++) {
            out.write(aSeparator + this.getColumnName(i));
        }
        out.write("\n");
        for(int i=0;i<iRowCount;i++) {
            for(int j=0;j<iColCount;j++) {
                String text = "" + this.getValueAt(i,j);
                if(text.indexOf("<html>") >= 0) {
                    // Replace 'html' tags with 'body' tags for Excel compatibility.
                    // Remove 'html' tags.
                    int start = -1;
                    while((start = text.indexOf("<html>")) >= 0) {
                        text = text.substring(0, start) + text.substring(start + 6);
                    }
                    while((start = text.indexOf("</html>")) >= 0) {
                        text = text.substring(0, start) + text.substring(start + 7);
                    }
                }
                out.write(aSeparator + text);
            }
            out.write("\n");
        }
    }

    /**
     * This method allows the caller to write the current dataset to the
     * specified Writer. Note that flushing and closing the writer is up to the
     * caller.
     *
     * @param   out Writer to print to. Note that flushing and closing this
     *              writer is up to the caller.
     * @param   aBorderstyle    int with the HTML table border tag style for the table.
     * @exception   IOException when something goes wrong.
     */
    public void writeToHTMLTable(Writer out, int aBorderstyle) throws IOException {
        // Write headers first.
        out.write("<table border=\"" + aBorderstyle + "\">\n");
        out.write(" <caption align=\"bottom\">This table was generated by the DBResultSet Java Object.</caption>\n");
        out.write(" <tr>\n");
        for(int i=0;i<iColCount;i++) {
            out.write("  <th>" + this.getColumnName(i) + "</th>\n");
        }
        out.write(" </tr>\n");

        for(int i=0;i<iRowCount;i++) {
            out.write(" <tr>\n");
            for(int j=0;j<iColCount;j++) {
                Object o = this.getValueAt(i,j);
                String text = null;
                if(o == null) {
                    text = "&nbsp";
                } else {
                    text = o.toString();
                    if(text.trim().equals("")) {
                        text = "&nbsp";
                    } else if(text.indexOf("<html>") >= 0) {
                        // Remove 'html' tags.
                        int start = -1;
                        while((start = text.indexOf("<html>")) >= 0) {
                            text = text.substring(0, start) + text.substring(start + 6);
                        }
                        while((start = text.indexOf("</html>")) >= 0) {
                            text = text.substring(0, start) + text.substring(start + 7);
                        }
                    }
                }
                out.write("  <td>" + text + "</td>\n");
            }
            out.write(" </tr>\n");
        }
        out.write("</table>\n");
    }
}
