package com.compomics.util.gui.parameters.identification_parameters;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.gui.variants.aa_substitutions.AaSubstitutionMatrixTableModel;
import com.compomics.util.preferences.PeptideVariantsPreferences;
import java.awt.Dialog;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;

/**
 * FractionSettingsDialog.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PeptideVariantsSettingsDialog extends javax.swing.JDialog {

    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates a new FractionSettingsDialog with a frame as owner.
     *
     * @param parentFrame a parent frame
     * @param peptideVariantPreferences the peptide variants settings
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public PeptideVariantsSettingsDialog(java.awt.Frame parentFrame, PeptideVariantsPreferences peptideVariantPreferences, boolean editable) {
        super(parentFrame, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(peptideVariantPreferences);
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new FractionSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame a parent frame
     * @param peptideVariantPreferences the peptide variants settings
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public PeptideVariantsSettingsDialog(Dialog owner, java.awt.Frame parentFrame, PeptideVariantsPreferences peptideVariantPreferences, boolean editable) {
        super(owner, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(peptideVariantPreferences);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGui() {
        maxVariantsSpinner.setEnabled(editable);
        substitutionMatrixComboBox.setEnabled(editable);
        substitutionMatrixComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        setTableProperties();
    }

    /**
     * Sets the table properties.
     */
    private void setTableProperties() {
        TableColumnModel tableColumnModel = substitutionMatrixTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(50);
        substitutionMatrixTableJScrollPane.getViewport().setOpaque(false);
        for (int i = 0; i < AminoAcid.getAminoAcids().length; i++) {
            tableColumnModel.getColumn(i + 1).setCellRenderer(new TrueFalseIconRenderer(
                    new ImageIcon(this.getClass().getResource("/icons/selected_green.png")),
                    null,
                    "On", "Off"));
        }
    }

    /**
     * Fills the GUI with the given settings.
     *
     * @param fractionSettings the fraction settings to display
     */
    private void populateGUI(PeptideVariantsPreferences peptideVariantPreferences) {

        maxVariantsSpinner.setValue(peptideVariantPreferences.getnEdits());

        AaSubstitutionMatrix aaSubstitutionMatrix = peptideVariantPreferences.getAaSubstitutionMatrix();
        substitutionMatrixComboBox.setSelectedItem(aaSubstitutionMatrix);
        updateTableContent(aaSubstitutionMatrix);

    }

    /**
     * Indicates whether the user canceled the editing.
     *
     * @return a boolean indicating whether the user canceled the editing
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the peptide variants settings as set by the user.
     *
     * @return the peptide variants settings as set by the user
     */
    public PeptideVariantsPreferences getPeptideVariantsPreferences() {
        PeptideVariantsPreferences peptideVariantsPreferences = new PeptideVariantsPreferences();
        peptideVariantsPreferences.setnEdits((Integer) maxVariantsSpinner.getValue());
        peptideVariantsPreferences.setAaSubstitutionMatrix((AaSubstitutionMatrix) substitutionMatrixComboBox.getSelectedItem());
        return peptideVariantsPreferences;
    }

    /**
     * Updates the content of the table with the given substitution matrix.
     *
     * @param aaSubstitutionMatrix the substitution matrix to display
     */
    private void updateTableContent(AaSubstitutionMatrix aaSubstitutionMatrix) {

        ((AaSubstitutionMatrixTableModel) substitutionMatrixTable.getModel()).setAaSubstitutionMatrix(aaSubstitutionMatrix);
        updateTableContent();
    }

    /**
     * Updates the content of the table.
     */
    private void updateTableContent() {
        ((DefaultTableModel) substitutionMatrixTable.getModel()).fireTableDataChanged();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgrounPanel = new javax.swing.JPanel();
        maxVariantsJPanel = new javax.swing.JPanel();
        maxVariantsLbl = new javax.swing.JLabel();
        maxVariantsSpinner = new javax.swing.JSpinner();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        aaSubstitutionsTableJPanel = new javax.swing.JPanel();
        substitutionMatrixComboBox = new javax.swing.JComboBox();
        substitutionMatrixTableJScrollPane = new javax.swing.JScrollPane();
        substitutionMatrixTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Fraction Settings");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgrounPanel.setBackground(new java.awt.Color(230, 230, 230));

        maxVariantsJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Max Variants"));
        maxVariantsJPanel.setOpaque(false);

        maxVariantsLbl.setText("Maximal number of edits per peptide");
        maxVariantsLbl.setToolTipText("<html>\nThe minium protein confidence required to be included in the<br>\naverage molecular weight analysis in the Fractions tab.\n</html>");

        maxVariantsSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));

        javax.swing.GroupLayout maxVariantsJPanelLayout = new javax.swing.GroupLayout(maxVariantsJPanel);
        maxVariantsJPanel.setLayout(maxVariantsJPanelLayout);
        maxVariantsJPanelLayout.setHorizontalGroup(
            maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxVariantsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(maxVariantsLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(maxVariantsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        maxVariantsJPanelLayout.setVerticalGroup(
            maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxVariantsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxVariantsLbl)
                    .addComponent(maxVariantsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        aaSubstitutionsTableJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Amino Acid Substitutions"));
        aaSubstitutionsTableJPanel.setOpaque(false);

        substitutionMatrixComboBox.setModel(new DefaultComboBoxModel(AaSubstitutionMatrix.defaultMutationMatrices));
        substitutionMatrixComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                substitutionMatrixComboBoxActionPerformed(evt);
            }
        });

        substitutionMatrixTable.setModel(new AaSubstitutionMatrixTableModel(null, false));
        substitutionMatrixTableJScrollPane.setViewportView(substitutionMatrixTable);

        javax.swing.GroupLayout aaSubstitutionsTableJPanelLayout = new javax.swing.GroupLayout(aaSubstitutionsTableJPanel);
        aaSubstitutionsTableJPanel.setLayout(aaSubstitutionsTableJPanelLayout);
        aaSubstitutionsTableJPanelLayout.setHorizontalGroup(
            aaSubstitutionsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aaSubstitutionsTableJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(aaSubstitutionsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(substitutionMatrixTableJScrollPane)
                    .addComponent(substitutionMatrixComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        aaSubstitutionsTableJPanelLayout.setVerticalGroup(
            aaSubstitutionsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aaSubstitutionsTableJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(substitutionMatrixComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(substitutionMatrixTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout backgrounPanelLayout = new javax.swing.GroupLayout(backgrounPanel);
        backgrounPanel.setLayout(backgrounPanelLayout);
        backgrounPanelLayout.setHorizontalGroup(
            backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgrounPanelLayout.createSequentialGroup()
                .addContainerGap(700, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
            .addGroup(backgrounPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgrounPanelLayout.createSequentialGroup()
                        .addComponent(aaSubstitutionsTableJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(backgrounPanelLayout.createSequentialGroup()
                        .addComponent(maxVariantsJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(7, 7, 7))))
        );
        backgrounPanelLayout.setVerticalGroup(
            backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgrounPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(maxVariantsJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aaSubstitutionsTableJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgrounPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgrounPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Cancel the dialog.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Cancel the dialog.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        canceled = true;
    }//GEN-LAST:event_formWindowClosing

    private void substitutionMatrixComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_substitutionMatrixComboBoxActionPerformed
        AaSubstitutionMatrix aaSubstitutionMatrix = (AaSubstitutionMatrix) substitutionMatrixComboBox.getSelectedItem();
        updateTableContent(aaSubstitutionMatrix);
    }//GEN-LAST:event_substitutionMatrixComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aaSubstitutionsTableJPanel;
    private javax.swing.JPanel backgrounPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel maxVariantsJPanel;
    private javax.swing.JLabel maxVariantsLbl;
    private javax.swing.JSpinner maxVariantsSpinner;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox substitutionMatrixComboBox;
    private javax.swing.JTable substitutionMatrixTable;
    private javax.swing.JScrollPane substitutionMatrixTableJScrollPane;
    // End of variables declaration//GEN-END:variables

}
