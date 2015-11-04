package com.compomics.util.gui.parameters.identification_parameters;

import com.compomics.util.preferences.FractionSettings;
import java.awt.Dialog;
import javax.swing.JOptionPane;

/**
 * FractionSettingsDialog.
 *
 * @author Marc Vaudel
 */
public class FractionSettingsDialog extends javax.swing.JDialog {

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
     * @param fractionSettings the fraction settings
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public FractionSettingsDialog(java.awt.Frame parentFrame, FractionSettings fractionSettings, boolean editable) {
        super(parentFrame, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(fractionSettings);
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new FractionSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame a parent frame
     * @param fractionSettings the fraction settings
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public FractionSettingsDialog(Dialog owner, java.awt.Frame parentFrame, FractionSettings fractionSettings, boolean editable) {
        super(owner, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(fractionSettings);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGui() {
        proteinConfidenceMwTxt.setEnabled(editable);
    }

    /**
     * Fills the GUI with the given settings.
     *
     * @param fractionSettings the fraction settings to display
     */
    private void populateGUI(FractionSettings fractionSettings) {

        proteinConfidenceMwTxt.setText(fractionSettings.getProteinConfidenceMwPlots() + "");

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
     * Validates the user input.
     *
     * @return a boolean indicating whether the user input is valid
     */
    public boolean validateInput() {
        try {
            Double temp = new Double(proteinConfidenceMwTxt.getText().trim());
            if (temp < 0 || temp > 100) {
                JOptionPane.showMessageDialog(this, "Please verify the input for the Protein Confidence MW.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                proteinConfidenceMwTxt.requestFocus();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the Protein Confidence MW.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            proteinConfidenceMwTxt.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Returns the fraction settings as set by the user.
     *
     * @return the fraction settings as set by the user
     */
    public FractionSettings getFractionSettings() {
        FractionSettings fractionSettings = new FractionSettings();
        fractionSettings.setProteinConfidenceMwPlots(new Double(proteinConfidenceMwTxt.getText().trim()));
        return fractionSettings;
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
        fractionsPanel = new javax.swing.JPanel();
        proteinMwLabel = new javax.swing.JLabel();
        proteinConfidenceMwTxt = new javax.swing.JTextField();
        percentLabel4 = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        fractionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Fractions (Beta)"));
        fractionsPanel.setOpaque(false);

        proteinMwLabel.setText("Protein Confidence MW");
        proteinMwLabel.setToolTipText("<html>\nThe minium protein confidence required to be included in the<br>\naverage molecular weight analysis in the Fractions tab.\n</html>");

        proteinConfidenceMwTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        proteinConfidenceMwTxt.setText("95");
        proteinConfidenceMwTxt.setToolTipText("<html>\nThe minium protein confidence required to be included in the<br>\naverage molecular weight analysis in the Fractions tab.\n</html>");

        percentLabel4.setText("%");

        javax.swing.GroupLayout fractionsPanelLayout = new javax.swing.GroupLayout(fractionsPanel);
        fractionsPanel.setLayout(fractionsPanelLayout);
        fractionsPanelLayout.setHorizontalGroup(
            fractionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fractionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(proteinMwLabel)
                .addGap(30, 30, 30)
                .addComponent(proteinConfidenceMwTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(percentLabel4)
                .addContainerGap())
        );
        fractionsPanelLayout.setVerticalGroup(
            fractionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fractionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fractionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proteinConfidenceMwTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentLabel4)
                    .addComponent(proteinMwLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        javax.swing.GroupLayout backgrounPanelLayout = new javax.swing.GroupLayout(backgrounPanel);
        backgrounPanel.setLayout(backgrounPanelLayout);
        backgrounPanelLayout.setHorizontalGroup(
            backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgrounPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fractionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(7, 7, 7))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgrounPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
        );
        backgrounPanelLayout.setVerticalGroup(
            backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgrounPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fractionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgrounPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel fractionsPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel percentLabel4;
    private javax.swing.JTextField proteinConfidenceMwTxt;
    private javax.swing.JLabel proteinMwLabel;
    // End of variables declaration//GEN-END:variables

}