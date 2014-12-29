/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-feb-03
 * Time: 13:26:33
 */
package com.compomics.util.gui;
import org.apache.log4j.Logger;

import com.compomics.util.sun.TableSorter;
import com.compomics.util.gui.renderers.ByteArrayRenderer;
import com.compomics.util.gui.renderers.TimestampRenderer;
import com.compomics.util.AlternateRowColoursJTable;

import javax.swing.*;
import javax.swing.table.*;
import java.util.Vector;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/*
 * CVS information:
 *
 * $Revision: 1.6 $
 * $Date: 2009/08/02 13:23:46 $
 */

/**
 * This class extends a JTable with specific cellrenderers for fields,
 * retrieved from DB.
 *
 * @author Lennart Martens
 */
public class JTableForDB extends AlternateRowColoursJTable {

    // Class specific log4j logger for JTableForDB instances.
    Logger logger = Logger.getLogger(JTableForDB.class);

    /**
     *  This String contains the date and time format.
     */
    private String iDateTimeFormat = "dd-MM-yyyy HH:mm:ss";

    /**
     * This Vector holds the references to all the tables that have been created
     * during the life cycle.
     */
    private Vector iFrames = new Vector(5, 2);

    /**
     * Constructs a default <code>JTable</code> that is initialized with a default
     * data model, a default column model, and a default selection
     * model.
     *
     * @see #createDefaultDataModel
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public JTableForDB() {
        super();
        this.setRenderers();
    }

    /**
     * Constructs a <code>JTable</code> with <code>numRows</code>
     * and <code>numColumns</code> of empty cells using
     * <code>DefaultTableModel</code>.  The columns will have
     * names of the form "A", "B", "C", etc.
     *
     * @param numRows           the number of rows the table holds
     * @param numColumns        the number of columns the table holds
     * @see javax.swing.table.DefaultTableModel
     */
    public JTableForDB(int numRows, int numColumns) {
        super(numRows, numColumns);
        this.setRenderers();
    }

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, <code>cm</code>
     * as the column model, and a default selection model.
     *
     * @param dm        the data model for the table
     * @param cm        the column model for the table
     * @see #createDefaultSelectionModel
     */
    public JTableForDB(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
        this.setRenderers();
    }

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, a default column model,
     * and a default selection model.
     *
     * @param dm        the data model for the table
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public JTableForDB(TableModel dm) {
        super(dm);
        this.setRenderers();
    }

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, <code>cm</code> as the
     * column model, and <code>sm</code> as the selection model.
     * If any of the parameters are <code>null</code> this method
     * will initialize the table with the corresponding default model.
     * The <code>autoCreateColumnsFromModel</code> flag is set to false
     * if <code>cm</code> is non-null, otherwise it is set to true
     * and the column model is populated with suitable
     * <code>TableColumns</code> for the columns in <code>dm</code>.
     *
     * @param dm        the data model for the table
     * @param cm        the column model for the table
     * @param sm        the row selection model for the table
     * @see #createDefaultDataModel
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public JTableForDB(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
        this.setRenderers();
    }

    /**
     * Constructs a <code>JTable</code> to display the values in the
     * <code>Vector</code> of <code>Vectors</code>, <code>rowData</code>,
     * with column names, <code>columnNames</code>.  The
     * <code>Vectors</code> contained in <code>rowData</code>
     * should contain the values for that row. In other words,
     * the value of the cell at row 1, column 5 can be obtained
     * with the following code:
     * <br><br>
     * <pre>((Vector)rowData.elementAt(1)).elementAt(5);</pre>
     * <br><br>
     * Each row must contain a value for each column or an exception
     * will be raised.
     * 
     * @param rowData           the data for the new table
     * @param columnNames       names of each column
     */
    public JTableForDB(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
        this.setRenderers();
    }

    /**
     * Constructs a <code>JTable</code> to display the values in the two dimensional array,
     * <code>rowData</code>, with column names, <code>columnNames</code>.
     * <code>rowData</code> is an array of rows, so the value of the cell at row 1,
     * column 5 can be obtained with the following code:
     * <br><br>
     * <pre> rowData[1][5]; </pre>
     * <br><br>
     * All rows must be of the same length as <code>columnNames</code>.
     * 
     * @param rowData           the data for the new table
     * @param columnNames       names of each column
     */
    public JTableForDB(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
        this.setRenderers();
    }

    /**
     * This method allows the setting of the date /time format.
     *
     * @param   aFormat String with the datetimeformat.
     */
    public void setDateTimeFormat(String aFormat) {
        this.iDateTimeFormat = aFormat;
    }

    /**
     * This method allows you to specify a tablemodel and a boolean
     * which indicates whether or not the table should be sortable by
     * clicking the column headers.
     *
     * @param   aModel  TableModel with the data.
     * @param   aSortable   boolean that indicates whether the table should be sortable.
     */
    public void setModel(TableModel aModel, boolean aSortable) {
        TableModel tempModel = aModel;
        if(aSortable) {
            TableModel oldModel = super.getModel();

            TableSorter sorter = new TableSorter(aModel);
            if(sorter != null) {
                if( (oldModel != null) && (oldModel instanceof TableSorter)) {
                    ((TableSorter)oldModel).removeMouseListenerToHeaderInTable(this);
                }
                sorter.addMouseListenerToHeaderInTable(this);
            }
            tempModel = sorter;
        }
        super.setModel(tempModel);
    }

    /**
     * Sets a TableModel for the table and defaults it to being sortable.
     *
     * @param   aModel  TableModel with the data for this table.
     */
    public void setModel(TableModel aModel) {
        if(aModel != null) {
            this.setModel(aModel, true);
        } else {
            this.setModel(aModel, false);
        }
    }

    /**
     * This method will set some specific cell renderers for
     * a table that is mostly used for displaying data from RDBMS systems.
     */
    private void setRenderers() {
        // Renderer for timestamps (displays date and time; default: "dd-MM-yyyy HH:mm:ss");
        // The date/time format can be set using the setDateTimeFormat method.
        this.setDefaultRenderer(java.sql.Timestamp.class, new TimestampRenderer());

        // Renderer for byte[] (simple displays the sipmle fact that it contians a byte[]).
        this.setDefaultRenderer(byte[].class, new ByteArrayRenderer());
        // Now to detect doubleclicks on a cell.
        // If the doubleclick is on a byte[], show it.
        this.addMouseListener(new MouseAdapter() {
            /**
             * Invoked when the mouse has been clicked on a component.
             */
            public void mouseClicked(MouseEvent e) {
                // Transform clickpoint to row and column indices +
                // retrieve the renderer at that location.
                Point compLoc = e.getPoint();
                int col = columnAtPoint(compLoc);
                int row = rowAtPoint(compLoc);
                TableCellRenderer comp = getCellRenderer(row, col);

                // Right-click means: goto formatted results for datfile or server.
                if(e.getModifiers() == MouseEvent.BUTTON3_MASK || e.getModifiers() == MouseEvent.BUTTON2_MASK) {
                    if(JTableForDB.this.rowSelectionAllowed) {
                        JTableForDB.this.setRowSelectionInterval(row, row);
                    } else {
                        JTableForDB.this.setColumnSelectionInterval(row, row);
                    }
                } else if(e.getClickCount() >= 2) {
                    if(comp instanceof ByteArrayRenderer) {
                        // Creating the frame with the data from the model.
                        int modelCol = convertColumnIndexToModel(col);
                        JFrame frame = new JFrameForBytes("Display for byte[]", (byte[])getModel().getValueAt(row, modelCol));
                        // Add the frame to the cache.
                        iFrames.add(frame);
                        // Display and location stuff.
                        frame.setLocation(JTableForDB.this.getLocation().x + 50, JTableForDB.this.getLocation().x + 50);
                        frame.setVisible(true);
                    } else if((getColumnName(col) != null) && (getColumnName(col).toLowerCase().trim().equalsIgnoreCase("accession"))) {
                        // Get the data.
                        String accession = ((String)getValueAt(row, col)).trim();
                        // The URL will be stored here.
                        String url = null;
                        // Find out if it is SP or NCBI.
                        String upper = accession.toUpperCase();
                        if(upper.startsWith("Q") || upper.startsWith("O") || upper.startsWith("P")) {
                            // SwissProt.
                            url = "http://us.expasy.org/cgi-bin/niceprot.pl?" + accession;
                        } else if(Character.isDigit(accession.charAt(0))) {
                            url = "\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=protein&cmd=search&term=" + accession + "\"";
                        } else if(accession.toLowerCase().startsWith("ipi")) {
                            // Isoform '.x' annotation needs to be removed.
                            String tempAccession = accession;
                            url = "http://srs.ebi.ac.uk/srsbin/cgi-bin/wgetz?-e+[IPI:'" + tempAccession + "']";
                        }
                        // The process.
                        try {
                            Runtime.getRuntime().exec("startIexplore.cmd " + url);
                        } catch(Exception exc) {
                            logger.error(exc.getMessage(), exc);
                            JOptionPane.showMessageDialog((Component) comp, "Unable to open internet view of selected entry: " + exc.getMessage()
                                    + ".", "Unable to open browser window", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                super.mouseClicked(e);
            }
        });

        // Now to detect <ctrl>+<c> combinations.
        this.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been pressed.
             */
            public void keyPressed(KeyEvent e) {
                if((e.isShiftDown()) && (e.getKeyCode() == KeyEvent.VK_C)) {
                    int col = JTableForDB.this.getSelectedColumn();
                    int row = JTableForDB.this.getSelectedRow();
                    if((col >= 0) && (row >= 0)) {
                        String value = JTableForDB.this.getValueAt(row, col).toString();
                        Object temp = new StringSelection(value);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents((Transferable)temp, (ClipboardOwner)temp);
                    }
                } else {
                    super.keyPressed(e);
                }
            }
        });
    }

    /**
     * Called by the garbage collector on an object when garbage collection
     * determines that there are no more references to the object.
     * A subclass overrides the <code>finalize</code> method to dispose of
     * system resources or to perform other cleanup.
     * <p>
     * The general contract of <tt>finalize</tt> is that it is invoked
     * if and when the Java virtual
     * machine has determined that there is no longer any
     * means by which this object can be accessed by any thread that has
     * not yet died, except as a result of an action taken by the
     * finalization of some other object or class which is ready to be
     * finalized. The <tt>finalize</tt> method may take any action, including
     * making this object available again to other threads; the usual purpose
     * of <tt>finalize</tt>, however, is to perform cleanup actions before
     * the object is irrevocably discarded. For example, the finalize method
     * for an object that represents an input/output connection might perform
     * explicit I/O transactions to break the connection before the object is
     * permanently discarded.
     * <p>
     * The <tt>finalize</tt> method of class <tt>Object</tt> performs no
     * special action; it simply returns normally. Subclasses of
     * <tt>Object</tt> may override this definition.
     * <p>
     * The Java programming language does not guarantee which thread will
     * invoke the <tt>finalize</tt> method for any given object. It is
     * guaranteed, however, that the thread that invokes finalize will not
     * be holding any user-visible synchronization locks when finalize is
     * invoked. If an uncaught exception is thrown by the finalize method,
     * the exception is ignored and finalization of that object terminates.
     * <p>
     * After the <tt>finalize</tt> method has been invoked for an object, no
     * further action is taken until the Java virtual machine has again
     * determined that there is no longer any means by which this object can
     * be accessed by any thread that has not yet died, including possible
     * actions by other objects or classes which are ready to be finalized,
     * at which point the object may be discarded.
     * <p>
     * The <tt>finalize</tt> method is never invoked more than once by a Java
     * virtual machine for any given object.
     * <p>
     * Any exception thrown by the <code>finalize</code> method causes
     * the finalization of this object to be halted, but is otherwise
     * ignored.
     *
     * @throws Throwable the <code>Exception</code> raised by this method
     */
    protected void finalize() throws Throwable {
        int liSize = iFrames.size();
        for(int i = 0; i < liSize; i++) {
            JFrame lFrame = (JFrame)iFrames.elementAt(i);
            if(lFrame != null) {
                lFrame.setVisible(false);
                lFrame.dispose();
            }
        }
        super.finalize();
    }
}
