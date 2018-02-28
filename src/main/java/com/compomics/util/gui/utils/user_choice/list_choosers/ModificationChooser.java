package com.compomics.util.gui.utils.user_choice.list_choosers;

import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.gui.utils.user_choice.ListChooser;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;

/**
 * Dialog for choosing an item in a list of modifications.
 *
 * @author Marc Vaudel
 */
public class ModificationChooser extends ListChooser {

    /**
     * The modifications factory.
     */
    private ModificationFactory modificationsFactory = ModificationFactory.getInstance();
    /**
     * List of modifications to display.
     */
    private ArrayList<String> modificationsList = new ArrayList<>();

    /**
     * Constructor. Null values will be replaced by default.
     *
     * @param parent the parent frame
     * @param modifications list of the names of the modifications for the user to select
     * @param dialogTitle the title to give to the dialog
     * @param panelTitle the title to give to the panel containing the table
     * @param instructionsLabel the instructions label on top of the table
     * @param multipleSelection boolean indicating whether the user should be
     * allowed to select multiple items
     */
    public ModificationChooser(java.awt.Frame parent, ArrayList<String> modifications, String dialogTitle, String panelTitle, String instructionsLabel, boolean multipleSelection) {
        
        super(parent, modifications, dialogTitle, panelTitle, instructionsLabel, multipleSelection);
        this.modificationsList = modifications;
        
        if (modifications == null || modifications.isEmpty()) {
        
            throw new IllegalArgumentException("No item to select.");
        
        }
        
        setUpTable();
        setVisible(true);
    
    }

    /**
     * Constructor with default values.
     *
     * @param parent the parent frame
     * @param modifications list of the names of the modifications for the user to select
     * @param multipleSelection boolean indicating whether the user should be
     * allowed to select multiple items
     */
    public ModificationChooser(java.awt.Frame parent, ArrayList<String> modifications, boolean multipleSelection) {
        this(parent, modifications, "Modification Selection", "Searched Modifications", "Please select a modification from the list of possibilities.", multipleSelection);
    }

    @Override
    protected void formatTable() {

        JTable modificationsJTable = getTable();
        modificationsJTable.setModel(new ModificationsTableModel());

        double minMass = modificationsList.stream()
                .map(modName -> modificationsFactory.getModification(modName))
                .mapToDouble(Modification::getMass)
                .min()
                .orElse(0.0);
        double maxMass = modificationsList.stream()
                .map(modName -> modificationsFactory.getModification(modName))
                .mapToDouble(Modification::getMass)
                .max()
                .orElse(0.0);

        modificationsJTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        modificationsJTable.getColumn(" ").setMaxWidth(35);

        modificationsJTable.getColumn("Mass").setMaxWidth(100);
        modificationsJTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) modificationsJTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);

        ArrayList<String> modificationTableToolTips = getTableTooltips();
        modificationTableToolTips.add(null);
        modificationTableToolTips.add("Modification Name");
        modificationTableToolTips.add("Modification Mass");
        modificationTableToolTips.add("Default Modification");
    }

    /**
     * Table model for the modifications table.
     */
    private class ModificationsTableModel extends DefaultTableModel {

        @Override
        public int getRowCount() {
            return modificationsList.size();
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
            String modName = modificationsList.get(row);
            switch (column) {
                case 0:
                    return modificationsFactory.getColor(modName);
                case 1:
                    return modName;
                case 2:
                    Modification modification = modificationsFactory.getModification(modName);
                    return modification.getMass();
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
