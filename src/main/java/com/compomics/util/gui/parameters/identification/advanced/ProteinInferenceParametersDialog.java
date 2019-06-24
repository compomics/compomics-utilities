package com.compomics.util.gui.parameters.identification.advanced;

import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.parameters.identification.advanced.ProteinInferenceParameters;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.SwingConstants;

/**
 * Dialog for the edition of the protein inference settings.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ProteinInferenceParametersDialog extends javax.swing.JDialog {

    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * The normal dialog icon.
     */
    private Image normalIcon;
    /**
     * The waiting dialog icon.
     */
    private Image waitingIcon;
    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Empty default constructor
     */
    public ProteinInferenceParametersDialog() {
    }

    /**
     * Creates a new ProteinInferenceSettingsDialog with a frame as owner.
     *
     * @param parentFrame a parent frame
     * @param proteinInferencePreferences the protein inference settings to
     * display
     * @param normalIcon the normal dialog icon
     * @param waitingIcon the waiting dialog icon
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public ProteinInferenceParametersDialog(java.awt.Frame parentFrame, ProteinInferenceParameters proteinInferencePreferences,
            Image normalIcon, Image waitingIcon, boolean editable) {

        super(parentFrame, true);

        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.editable = editable;

        initComponents();
        setUpGui();
        populateGUI(proteinInferencePreferences);
        setLocationRelativeTo(parentFrame);
        setVisible(true);

    }

    /**
     * Creates a new ProteinInferenceSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame a parent frame
     * @param proteinInferencePreferences the protein inference settings to
     * display
     * @param normalIcon the normal dialog icon
     * @param waitingIcon the waiting dialog icon
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public ProteinInferenceParametersDialog(Dialog owner, java.awt.Frame parentFrame, ProteinInferenceParameters proteinInferencePreferences,
            Image normalIcon, Image waitingIcon, boolean editable) {
        super(owner, true);
        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(proteinInferencePreferences);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {

        simplifyGroupsCmb.setEnabled(editable);
        simplifyEvidenceCmb.setEnabled(editable);
        simplifyConfidenceCmb.setEnabled(editable);
        simplifyEnzymaticityCmb.setEnabled(editable);
        simplifyVariantsCmb.setEnabled(editable);
        confidenceTxt.setEnabled(editable);
        modificatoinsCmb.setEnabled(editable);

        simplifyGroupsCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        simplifyEvidenceCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        simplifyConfidenceCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        simplifyEnzymaticityCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        simplifyVariantsCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        modificatoinsCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

    }

    /**
     * Fills the GUI with the given settings.
     *
     * @param proteinInferencePreferences the protein inference settings to
     * display
     */
    private void populateGUI(ProteinInferenceParameters proteinInferencePreferences) {
        
            okButton.setEnabled(true);

        if (proteinInferencePreferences.getSimplifyGroups()) {

            simplifyGroupsCmb.setSelectedIndex(0);

        } else {

            simplifyGroupsCmb.setSelectedIndex(1);
            simplifyEvidenceCmb.setEnabled(false);
            simplifyConfidenceCmb.setEnabled(false);
            simplifyEnzymaticityCmb.setEnabled(false);
            simplifyVariantsCmb.setEnabled(false);
            modificatoinsCmb.setEnabled(false);

        }

        if (proteinInferencePreferences.getSimplifyGroupsEvidence()) {

            simplifyEvidenceCmb.setSelectedIndex(0);

        } else {

            simplifyEvidenceCmb.setSelectedIndex(1);

        }

        if (proteinInferencePreferences.getSimplifyGroupsConfidence()) {

            simplifyConfidenceCmb.setSelectedIndex(0);

        } else {

            simplifyConfidenceCmb.setSelectedIndex(1);

        }
        
        confidenceTxt.setText(Double.toString(proteinInferencePreferences.getConfidenceThreshold()));

        if (proteinInferencePreferences.getSimplifyGroupsEnzymaticity()) {

            simplifyEnzymaticityCmb.setSelectedIndex(0);

        } else {

            simplifyEnzymaticityCmb.setSelectedIndex(1);

        }

        if (proteinInferencePreferences.getSimplifyGroupsVariants()) {

            simplifyVariantsCmb.setSelectedIndex(0);

        } else {

            simplifyVariantsCmb.setSelectedIndex(1);

        }
    }

    /**
     * Returns the protein inference preferences.
     *
     * @return the protein inference preferences
     */
    public ProteinInferenceParameters getProteinInferencePreferences() {

        ProteinInferenceParameters proteinInferencePreferences = new ProteinInferenceParameters();

        proteinInferencePreferences.setSimplifyGroups(simplifyGroupsCmb.getSelectedIndex() == 0);
        proteinInferencePreferences.setSimplifyGroupsEvidence(simplifyEvidenceCmb.getSelectedIndex() == 0);
        proteinInferencePreferences.setSimplifyGroupsConfidence(simplifyConfidenceCmb.getSelectedIndex() == 0);
        proteinInferencePreferences.setSimplifyGroupsEnzymaticity(simplifyEnzymaticityCmb.getSelectedIndex() == 0);
        proteinInferencePreferences.setSimplifyGroupsVariants(simplifyVariantsCmb.getSelectedIndex() == 0);
        proteinInferencePreferences.setModificationRefinement(modificatoinsCmb.getSelectedIndex() == 0);
        
        proteinInferencePreferences.setConfidenceThreshold(Double.parseDouble(confidenceTxt.getText()));

        return proteinInferencePreferences;
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        simplifyConfidenceLbl1 = new javax.swing.JLabel();
        backgroundPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        proteinGroupPanel = new javax.swing.JPanel();
        simplifyGroupsLbl = new javax.swing.JLabel();
        simplifyEnzymaticityLbl = new javax.swing.JLabel();
        simplifyEvidenceLbl = new javax.swing.JLabel();
        simplifyGroupsCmb = new javax.swing.JComboBox();
        simplifyEnzymaticityCmb = new javax.swing.JComboBox();
        simplifyEvidenceCmb = new javax.swing.JComboBox();
        simplifyVariantsCmb = new javax.swing.JComboBox();
        simplifyVariantsLbl = new javax.swing.JLabel();
        simplifyConfidenceCmb = new javax.swing.JComboBox();
        simplifyConfidenceLbl = new javax.swing.JLabel();
        confidenceLbl = new javax.swing.JLabel();
        confidenceTxt = new javax.swing.JTextField();
        modificatoinsCmb = new javax.swing.JComboBox();
        modificationsLbl = new javax.swing.JLabel();
        helpJButton = new javax.swing.JButton();

        simplifyConfidenceLbl1.setText("- Based on Peptide Confidence");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Protein Inference");
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
        okButton.setEnabled(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        proteinGroupPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Protein Groups Simplification"));
        proteinGroupPanel.setOpaque(false);

        simplifyGroupsLbl.setText("Simplify Protein Groups");

        simplifyEnzymaticityLbl.setText("- Based on Enzymaticity");

        simplifyEvidenceLbl.setText("- Based on UniProt Evidence Level");

        simplifyGroupsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        simplifyGroupsCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simplifyGroupsCmbActionPerformed(evt);
            }
        });

        simplifyEnzymaticityCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        simplifyEvidenceCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        simplifyVariantsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        simplifyVariantsLbl.setText("- Based on Variant Mapping");

        simplifyConfidenceCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        simplifyConfidenceLbl.setText("- Based on Peptide Confidence");

        confidenceLbl.setText("Confidence below which a peptide is ignored");

        confidenceTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        modificatoinsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        modificatoinsCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificatoinsCmbActionPerformed(evt);
            }
        });

        modificationsLbl.setText("Account for modifications in protein mapping");

        javax.swing.GroupLayout proteinGroupPanelLayout = new javax.swing.GroupLayout(proteinGroupPanel);
        proteinGroupPanel.setLayout(proteinGroupPanelLayout);
        proteinGroupPanelLayout.setHorizontalGroup(
            proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteinGroupPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proteinGroupPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(simplifyEvidenceLbl)
                            .addComponent(simplifyConfidenceLbl)
                            .addGroup(proteinGroupPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(confidenceLbl)))
                        .addGap(78, 78, 78)
                        .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(simplifyEvidenceCmb, 0, 223, Short.MAX_VALUE)
                            .addComponent(simplifyConfidenceCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(confidenceTxt)))
                    .addGroup(proteinGroupPanelLayout.createSequentialGroup()
                        .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(simplifyGroupsLbl)
                            .addGroup(proteinGroupPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(simplifyVariantsLbl)
                                    .addComponent(simplifyEnzymaticityLbl)))
                            .addComponent(modificationsLbl))
                        .addGap(98, 98, 98)
                        .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(simplifyVariantsCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(simplifyGroupsCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(simplifyEnzymaticityCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(modificatoinsCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        proteinGroupPanelLayout.setVerticalGroup(
            proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteinGroupPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyGroupsLbl)
                    .addComponent(simplifyGroupsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyEvidenceLbl)
                    .addComponent(simplifyEvidenceCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyConfidenceLbl)
                    .addComponent(simplifyConfidenceCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(confidenceLbl)
                    .addComponent(confidenceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyEnzymaticityLbl)
                    .addComponent(simplifyEnzymaticityCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyVariantsLbl)
                    .addComponent(simplifyVariantsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteinGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modificationsLbl)
                    .addComponent(modificatoinsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.setBorder(null);
        helpJButton.setBorderPainted(false);
        helpJButton.setContentAreaFilled(false);
        helpJButton.setFocusable(false);
        helpJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        helpJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        helpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                helpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                helpJButtonMouseExited(evt);
            }
        });
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(proteinGroupPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(helpJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(proteinGroupPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(helpJButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
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

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(parentFrame, getClass().getResource("/helpFiles/ProteinInferencePreferences.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
                "Protein Inference - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Enable or disable the detailed options.
     *
     * @param evt
     */
    private void simplifyGroupsCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simplifyGroupsCmbActionPerformed

        simplifyEvidenceCmb.setEnabled(editable && simplifyGroupsCmb.getSelectedIndex() == 0);
        simplifyConfidenceCmb.setEnabled(editable && simplifyGroupsCmb.getSelectedIndex() == 0);
        confidenceTxt.setEnabled(editable && simplifyGroupsCmb.getSelectedIndex() == 0);
        simplifyEnzymaticityCmb.setEnabled(editable && simplifyGroupsCmb.getSelectedIndex() == 0);
        simplifyVariantsCmb.setEnabled(editable && simplifyGroupsCmb.getSelectedIndex() == 0);
        
    }//GEN-LAST:event_simplifyGroupsCmbActionPerformed

    private void modificatoinsCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificatoinsCmbActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modificatoinsCmbActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel confidenceLbl;
    private javax.swing.JTextField confidenceTxt;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel modificationsLbl;
    private javax.swing.JComboBox modificatoinsCmb;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel proteinGroupPanel;
    private javax.swing.JComboBox simplifyConfidenceCmb;
    private javax.swing.JLabel simplifyConfidenceLbl;
    private javax.swing.JLabel simplifyConfidenceLbl1;
    private javax.swing.JComboBox simplifyEnzymaticityCmb;
    private javax.swing.JLabel simplifyEnzymaticityLbl;
    private javax.swing.JComboBox simplifyEvidenceCmb;
    private javax.swing.JLabel simplifyEvidenceLbl;
    private javax.swing.JComboBox simplifyGroupsCmb;
    private javax.swing.JLabel simplifyGroupsLbl;
    private javax.swing.JComboBox simplifyVariantsCmb;
    private javax.swing.JLabel simplifyVariantsLbl;
    // End of variables declaration//GEN-END:variables

}
