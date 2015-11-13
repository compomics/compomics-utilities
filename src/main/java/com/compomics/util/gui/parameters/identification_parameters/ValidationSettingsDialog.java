package com.compomics.util.gui.parameters.identification_parameters;

import com.compomics.util.preferences.IdMatchValidationPreferences;
import java.awt.Dialog;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * Dialog for the edition of the sequence matching settings
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ValidationSettingsDialog extends javax.swing.JDialog {

    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates a new ValidationSettingsDialog with a frame as owner.
     *
     * @param parentFrame a parent frame
     * @param idMatchValidationPreferences the validation preferences to display
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public ValidationSettingsDialog(java.awt.Frame parentFrame, IdMatchValidationPreferences idMatchValidationPreferences, boolean editable) {
        super(parentFrame, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(idMatchValidationPreferences);
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new ValidationSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame a parent frame
     * @param idMatchValidationPreferences the validation preferences to display
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public ValidationSettingsDialog(Dialog owner, java.awt.Frame parentFrame, IdMatchValidationPreferences idMatchValidationPreferences, boolean editable) {
        super(owner, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(idMatchValidationPreferences);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {
        proteinFdrTxt.setEditable(editable);
        proteinFdrTxt.setEnabled(editable);
        peptideFdrTxt.setEditable(editable);
        peptideFdrTxt.setEnabled(editable);
        psmFdrTxt.setEditable(editable);
        psmFdrTxt.setEnabled(editable);
        groupsComboBox.setEnabled(editable);
        groupsComboBox.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
    }

    /**
     * Fills the GUI with the given settings.
     *
     * @param idMatchValidationPreferences the validation preferences to display
     */
    private void populateGUI(IdMatchValidationPreferences idMatchValidationPreferences) {
        proteinFdrTxt.setText(idMatchValidationPreferences.getDefaultProteinFDR() + "");
        peptideFdrTxt.setText(idMatchValidationPreferences.getDefaultPeptideFDR() + "");
        psmFdrTxt.setText(idMatchValidationPreferences.getDefaultPsmFDR() + "");
        if (idMatchValidationPreferences.getMergeSmallSubgroups()) {
            groupsComboBox.setSelectedIndex(0);
        } else {
            groupsComboBox.setSelectedIndex(1);
        }
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
     * Returns the validation settings as set by the user.
     *
     * @return the validation settings as set by the user
     */
    public IdMatchValidationPreferences getIdMatchValidationPreferences() {
        IdMatchValidationPreferences idMatchValidationPreferences = new IdMatchValidationPreferences();
        idMatchValidationPreferences.setDefaultProteinFDR(new Double(proteinFdrTxt.getText().trim()));
        idMatchValidationPreferences.setDefaultPeptideFDR(new Double(peptideFdrTxt.getText().trim()));
        idMatchValidationPreferences.setDefaultPsmFDR(new Double(psmFdrTxt.getText().trim()));
        idMatchValidationPreferences.setMergeSmallSubgroups(groupsComboBox.getSelectedIndex() == 0);
        return idMatchValidationPreferences;
    }

    /**
     * Validates the user input.
     *
     * @return a boolean indicating whether the user input is valid
     */
    public boolean validateInput() {
        try {
            Double temp = new Double(proteinFdrTxt.getText().trim());
            if (temp < 0 || temp > 100) {
                JOptionPane.showMessageDialog(this, "Please verify the input for the protein FDR.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                proteinFdrTxt.requestFocus();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the protein FDR.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            proteinFdrTxt.requestFocus();
            return false;
        }
        try {
            Double temp = new Double(peptideFdrTxt.getText().trim());
            if (temp < 0 || temp > 100) {
                JOptionPane.showMessageDialog(this, "Please verify the input for the peptide FDR.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                peptideFdrTxt.requestFocus();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the peptide FDR.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            peptideFdrTxt.requestFocus();
            return false;
        }
        try {
            Double temp = new Double(psmFdrTxt.getText().trim());
            if (temp < 0 || temp > 100) {
                JOptionPane.showMessageDialog(this, "Please verify the input for the PSM FDR.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                psmFdrTxt.requestFocus();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the PSM FDR.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            psmFdrTxt.requestFocus();
            return false;
        }
        return true;
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
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        processingParamsPanel = new javax.swing.JPanel();
        proteinFdrLabel = new javax.swing.JLabel();
        peptideFdrLabel = new javax.swing.JLabel();
        psmFdrLabel = new javax.swing.JLabel();
        psmFdrTxt = new javax.swing.JTextField();
        peptideFdrTxt = new javax.swing.JTextField();
        proteinFdrTxt = new javax.swing.JTextField();
        groupsPanel = new javax.swing.JPanel();
        groupsLabel = new javax.swing.JLabel();
        groupsComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Validation Settings");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

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

        processingParamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Default FDR Levels"));
        processingParamsPanel.setOpaque(false);

        proteinFdrLabel.setText("Protein FDR (%)");

        peptideFdrLabel.setText("Peptide FDR (%)");

        psmFdrLabel.setText("PSM FDR (%)");

        psmFdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        psmFdrTxt.setText("1");

        peptideFdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptideFdrTxt.setText("1");

        proteinFdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        proteinFdrTxt.setText("1");

        javax.swing.GroupLayout processingParamsPanelLayout = new javax.swing.GroupLayout(processingParamsPanel);
        processingParamsPanel.setLayout(processingParamsPanelLayout);
        processingParamsPanelLayout.setHorizontalGroup(
            processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(processingParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(proteinFdrLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideFdrLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(psmFdrLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(psmFdrTxt, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(peptideFdrTxt)
                    .addComponent(proteinFdrTxt, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        processingParamsPanelLayout.setVerticalGroup(
            processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(processingParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proteinFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(proteinFdrLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptideFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideFdrLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psmFdrLabel)
                    .addComponent(psmFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        groupsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Groups"));
        groupsPanel.setOpaque(false);

        groupsLabel.setText("Merge Small Subgroups");

        groupsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout groupsPanelLayout = new javax.swing.GroupLayout(groupsPanel);
        groupsPanel.setLayout(groupsPanelLayout);
        groupsPanelLayout.setHorizontalGroup(
            groupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(groupsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groupsComboBox, 0, 201, Short.MAX_VALUE)
                .addContainerGap())
        );
        groupsPanelLayout.setVerticalGroup(
            groupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(groupsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(groupsLabel)
                    .addComponent(groupsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(groupsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(processingParamsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(processingParamsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groupsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput()) {
            dispose();
        }
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox groupsComboBox;
    private javax.swing.JLabel groupsLabel;
    private javax.swing.JPanel groupsPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel peptideFdrLabel;
    private javax.swing.JTextField peptideFdrTxt;
    private javax.swing.JPanel processingParamsPanel;
    private javax.swing.JLabel proteinFdrLabel;
    private javax.swing.JTextField proteinFdrTxt;
    private javax.swing.JLabel psmFdrLabel;
    private javax.swing.JTextField psmFdrTxt;
    // End of variables declaration//GEN-END:variables

}
