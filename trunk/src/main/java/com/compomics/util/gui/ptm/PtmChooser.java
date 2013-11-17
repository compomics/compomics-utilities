package com.compomics.util.gui.ptm;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;

/**
 * A dialog for selecting from a list of PTMs.
 * 
 * @author Marc Vaudel
 */
public class PtmChooser extends javax.swing.JDialog {

    /**
     * Boolean indicating whether the cancel button has been pressed.
     */
    private boolean canceled = false;
    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * List of PTMs to display.
     */
    private ArrayList<String> ptmList = new ArrayList<String>();
    /**
     * The modification table column header tooltips.
     */
    private ArrayList<String> modificationTableToolTips;

    /**
     * Creates new form PtmChooser. Note, all PTMs shall be in the PTMFactory.
     *
     * @param parent the parent frame
     * @param ptmList list of PTMs to display
     * @param selectedPTMs PTM original selection
     */
    public PtmChooser(java.awt.Frame parent, ArrayList<String> ptmList, ArrayList<String> selectedPTMs) {
        super(parent, true);
        this.ptmList = ptmList;
        initComponents();
        setUpGUI();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Returns a list of PTMs selected by the user.
     *
     * @return a list of PTMs selected by the user
     */
    public ArrayList<String> getSelection() {
        ArrayList<String> result = new ArrayList<String>();
        for (int i : ptmTable.getSelectedRows()) {
            int index = ptmTable.convertRowIndexToModel(i);
            result.add(ptmList.get(index));
        }
        return result;
    }

    /**
     * Boolean indicating whether the cancel button was pressed by the user.
     *
     * @return true if the cancel button was pressed
     */
    public boolean isCanceled() {
        return canceled;
    }

    private void setUpGUI() {

        // make sure that the scroll panes are see-through
        ptmTableScrollPane.getViewport().setOpaque(false);
        
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

        modificationTableToolTips = new ArrayList<String>();
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        ptmSelectionPanel = new javax.swing.JPanel();
        ptmTableScrollPane = new javax.swing.JScrollPane();
        ptmTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return (String) modificationTableToolTips.get(realIndex);
                    }
                };
            }
        };
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(400, 400));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        ptmSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PTM Selection"));
        ptmSelectionPanel.setOpaque(false);

        ptmTable.setModel(new PtmTable());
        ptmTableScrollPane.setViewportView(ptmTable);

        javax.swing.GroupLayout ptmSelectionPanelLayout = new javax.swing.GroupLayout(ptmSelectionPanel);
        ptmSelectionPanel.setLayout(ptmSelectionPanelLayout);
        ptmSelectionPanelLayout.setHorizontalGroup(
            ptmSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ptmSelectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ptmTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addContainerGap())
        );
        ptmSelectionPanelLayout.setVerticalGroup(
            ptmSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ptmSelectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ptmTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                .addContainerGap())
        );

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ptmSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ptmSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Close the dialog.
     * 
     * @param evt 
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Save the selection and close the selection.
     * 
     * @param evt 
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close the dialog.
     * 
     * @param evt 
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel ptmSelectionPanel;
    private javax.swing.JTable ptmTable;
    private javax.swing.JScrollPane ptmTableScrollPane;
    // End of variables declaration//GEN-END:variables
}
