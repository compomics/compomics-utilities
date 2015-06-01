package com.compomics.util.gui.utils.user_choice.list_choosers;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.gui.utils.user_choice.ListChooser;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;

/**
 * Dialog for choosing an item in a list of PTMs.
 *
 * @author Marc Vaudel
 */
public class PtmChooser extends ListChooser {

    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * List of PTMs to display.
     */
    private ArrayList<String> ptmList = new ArrayList<String>();

    /**
     * Constructor. Null values will be replaced by default.
     *
     * @param parent the parent frame
     * @param ptms list of the names of the PTMs for the user to select
     * @param dialogTitle the title to give to the dialog
     * @param panelTitle the title to give to the panel containing the table
     * @param instructionsLabel the instructions label on top of the table
     * @param multipleSelection boolean indicating whether the user should be allowed to select multiple items
     */
    public PtmChooser(java.awt.Frame parent, ArrayList<String> ptms, String dialogTitle, String panelTitle, String instructionsLabel, boolean multipleSelection) {
        super(parent, ptms, dialogTitle, panelTitle, instructionsLabel, multipleSelection);
        this.ptmList = ptms;
        if (ptms == null || ptms.isEmpty()) {
            throw new IllegalArgumentException("No item to select.");
        }
        setUpTable();
        setVisible(true);
    }

    /**
     * Constructor with default values.
     *
     * @param parent the parent frame
     * @param ptms list of the names of the PTMs for the user to select
     * @param multipleSelection boolean indicating whether the user should be allowed to select multiple items
     */
    public PtmChooser(java.awt.Frame parent, ArrayList<String> ptms, boolean multipleSelection) {
        this(parent, ptms, "PTM selection", "Searched PTMs", "Please select a PTM from the list of possibilities.", multipleSelection);
    }

    @Override
    protected void formatTable() {

        JTable ptmTable = getTable();
        ptmTable.setModel(new PtmTable());

        double minMass = 0;
        double maxMass = 0;
        for (String modification : ptmList) {
            PTM ptm = ptmFactory.getPTM(modification);
            double mass = ptm.getMass();
            if (mass < minMass) {
                minMass = mass;
            }
            if (mass > maxMass) {
                maxMass = mass;
            }
        }

        ptmTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        ptmTable.getColumn(" ").setMaxWidth(35);

        ptmTable.getColumn("Mass").setMaxWidth(100);
        ptmTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) ptmTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);

        ArrayList<String> modificationTableToolTips = getTableTooltips();
        modificationTableToolTips.add(null);
        modificationTableToolTips.add("Modification Name");
        modificationTableToolTips.add("Modification Mass");
        modificationTableToolTips.add("Default Modification");
    }

    /**
     * Table model for the PTM table.
     */
    private class PtmTable extends DefaultTableModel {

        @Override
        public int getRowCount() {
            return ptmList.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Name";
                case 2:
                    return "Mass";
                default:
                    return "";
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            String ptmName = ptmList.get(row);
            switch (column) {
                case 0:
                    return ptmFactory.getColor(ptmName);
                case 1:
                    return ptmName;
                case 2:
                    PTM ptm = ptmFactory.getPTM(ptmName);
                    return ptm.getMass();
                default:
                    return "";
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, columnIndex) != null) {
                    return getValueAt(i, columnIndex).getClass();
                }
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }
}
