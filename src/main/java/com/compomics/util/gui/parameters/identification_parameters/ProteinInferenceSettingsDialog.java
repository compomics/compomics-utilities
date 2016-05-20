package com.compomics.util.gui.parameters.identification_parameters;

import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.preferences.LastSelectedFolder;
import com.compomics.util.preferences.ProteinInferencePreferences;
import com.compomics.util.protein_sequences_manager.gui.SequenceDbDetailsDialog;
import java.awt.Dialog;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * Dialog for the edition of the protein inference settings
 *
 * @author Marc Vaudel
 */
public class ProteinInferenceSettingsDialog extends javax.swing.JDialog {

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
     * The last selected folder to use.
     */
    private final LastSelectedFolder lastSelectedFolder;
    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates a new ProteinInferenceSettingsDialog with a frame as owner.
     *
     * @param parentFrame a parent frame
     * @param proteinInferencePreferences the protein inference settings to
     * display
     * @param normalIcon the normal dialog icon
     * @param waitingIcon the waiting dialog icon
     * @param lastSelectedFolder the last selected folder to use
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public ProteinInferenceSettingsDialog(java.awt.Frame parentFrame, ProteinInferencePreferences proteinInferencePreferences,
            Image normalIcon, Image waitingIcon, LastSelectedFolder lastSelectedFolder, boolean editable) {
        super(parentFrame, true);
        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.lastSelectedFolder = lastSelectedFolder;
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
     * @param lastSelectedFolder the last selected folder to use
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public ProteinInferenceSettingsDialog(Dialog owner, java.awt.Frame parentFrame, ProteinInferencePreferences proteinInferencePreferences,
            Image normalIcon, Image waitingIcon, LastSelectedFolder lastSelectedFolder, boolean editable) {
        super(owner, true);
        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.lastSelectedFolder = lastSelectedFolder;
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
        if (!editable) {
            editDatabaseDetailsButton.setText("View");
        }

        simplifyGroupsCmb.setEnabled(editable);
        simplifyScoreCmb.setEnabled(editable);
        simplifyEnzymaticityCmb.setEnabled(editable);
        simplifyEvidenceCmb.setEnabled(editable);
        simplifyCharacterizationCmb.setEnabled(editable);

        simplifyGroupsCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        simplifyScoreCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        simplifyEnzymaticityCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        simplifyEvidenceCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        simplifyCharacterizationCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
    }

    /**
     * Fills the GUI with the given settings.
     *
     * @param proteinInferencePreferences the protein inference settings to
     * display
     */
    private void populateGUI(ProteinInferencePreferences proteinInferencePreferences) {
        if (proteinInferencePreferences.getProteinSequenceDatabase() != null) {
            databaseSettingsTxt.setText(proteinInferencePreferences.getProteinSequenceDatabase().getAbsolutePath());
            okButton.setEnabled(true);
        }
        if (proteinInferencePreferences.getSimplifyGroups()) {
            simplifyGroupsCmb.setSelectedIndex(0);
        } else {
            simplifyGroupsCmb.setSelectedIndex(1);
            simplifyScoreCmb.setEnabled(false);
            simplifyEnzymaticityCmb.setEnabled(false);
            simplifyEvidenceCmb.setEnabled(false);
            simplifyCharacterizationCmb.setEnabled(false);
        }
        if (proteinInferencePreferences.getSimplifyGroupsScore()) {
            simplifyScoreCmb.setSelectedIndex(0);
        } else {
            simplifyScoreCmb.setSelectedIndex(1);
        }
        if (proteinInferencePreferences.getSimplifyGroupsEnzymaticity()) {
            simplifyEnzymaticityCmb.setSelectedIndex(0);
        } else {
            simplifyEnzymaticityCmb.setSelectedIndex(1);
        }
        if (proteinInferencePreferences.getSimplifyGroupsEvidence()) {
            simplifyEvidenceCmb.setSelectedIndex(0);
        } else {
            simplifyEvidenceCmb.setSelectedIndex(1);
        }
        if (proteinInferencePreferences.getSimplifyGroupsUncharacterized()) {
            simplifyCharacterizationCmb.setSelectedIndex(0);
        } else {
            simplifyCharacterizationCmb.setSelectedIndex(1);
        }
    }

    /**
     * Returns the protein inference preferences.
     *
     * @return the protein inference preferences
     */
    public ProteinInferencePreferences getProteinInferencePreferences() {
        
        ProteinInferencePreferences proteinInferencePreferences = new ProteinInferencePreferences();
        proteinInferencePreferences.setProteinSequenceDatabase(new File(databaseSettingsTxt.getText()));
        
        proteinInferencePreferences.setSimplifyGroups(simplifyGroupsCmb.getSelectedIndex() == 0);
        proteinInferencePreferences.setSimplifyGroupsScore(simplifyScoreCmb.getSelectedIndex() == 0);
        proteinInferencePreferences.setSimplifyGroupsEvidence(simplifyEvidenceCmb.getSelectedIndex() == 0);
        proteinInferencePreferences.setSimplifyGroupsEnzymaticity(simplifyEnzymaticityCmb.getSelectedIndex() == 0);
        proteinInferencePreferences.setSimplifyGroupsUncharacterized(simplifyCharacterizationCmb.getSelectedIndex() == 0);
        
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

        backgroundPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        dataBasePanelSettings = new javax.swing.JPanel();
        databaseSettingsLbl = new javax.swing.JLabel();
        databaseSettingsTxt = new javax.swing.JTextField();
        editDatabaseDetailsButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        simplifyGroupsLbl = new javax.swing.JLabel();
        simplifyScoreLbl = new javax.swing.JLabel();
        simplifyEnzymaticityLbl = new javax.swing.JLabel();
        simplifyEvidenceLbl = new javax.swing.JLabel();
        simplifyCharacterizationLbl = new javax.swing.JLabel();
        simplifyGroupsCmb = new javax.swing.JComboBox();
        simplifyScoreCmb = new javax.swing.JComboBox();
        simplifyEnzymaticityCmb = new javax.swing.JComboBox();
        simplifyEvidenceCmb = new javax.swing.JComboBox();
        simplifyCharacterizationCmb = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Protein Inference Settings");
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

        dataBasePanelSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Database"));
        dataBasePanelSettings.setOpaque(false);

        databaseSettingsLbl.setText("Database (FASTA)");

        databaseSettingsTxt.setEditable(false);

        editDatabaseDetailsButton.setText("Edit");
        editDatabaseDetailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDatabaseDetailsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dataBasePanelSettingsLayout = new javax.swing.GroupLayout(dataBasePanelSettings);
        dataBasePanelSettings.setLayout(dataBasePanelSettingsLayout);
        dataBasePanelSettingsLayout.setHorizontalGroup(
            dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataBasePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(databaseSettingsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(databaseSettingsTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editDatabaseDetailsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        dataBasePanelSettingsLayout.setVerticalGroup(
            dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataBasePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseSettingsLbl)
                    .addComponent(editDatabaseDetailsButton)
                    .addComponent(databaseSettingsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Protein Groups Simplification"));
        jPanel1.setOpaque(false);

        simplifyGroupsLbl.setText("Simplify protein groups");

        simplifyScoreLbl.setText("- based on score");

        simplifyEnzymaticityLbl.setText("- based on enzymaticity");

        simplifyEvidenceLbl.setText("- based on Uniprot evidence level");

        simplifyCharacterizationLbl.setText("- based on protein characterization");

        simplifyGroupsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        simplifyGroupsCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simplifyGroupsCmbActionPerformed(evt);
            }
        });

        simplifyScoreCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        simplifyEnzymaticityCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        simplifyEvidenceCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        simplifyCharacterizationCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(simplifyGroupsLbl)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(simplifyScoreLbl)
                            .addComponent(simplifyEnzymaticityLbl)
                            .addComponent(simplifyEvidenceLbl)
                            .addComponent(simplifyCharacterizationLbl))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 239, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(simplifyEvidenceCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(simplifyEnzymaticityCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(simplifyScoreCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(simplifyGroupsCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, 120, Short.MAX_VALUE)
                    .addComponent(simplifyCharacterizationCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyGroupsLbl)
                    .addComponent(simplifyGroupsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyScoreLbl)
                    .addComponent(simplifyScoreCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyEnzymaticityLbl)
                    .addComponent(simplifyEnzymaticityCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyEvidenceLbl)
                    .addComponent(simplifyEvidenceCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simplifyCharacterizationLbl)
                    .addComponent(simplifyCharacterizationCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
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
     * Edit the database.
     *
     * @param evt
     */
    private void editDatabaseDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDatabaseDetailsButtonActionPerformed

        SequenceFactory sequenceFactory = SequenceFactory.getInstance();

        // clear the factory
        if (databaseSettingsTxt.getText().trim().length() == 0) {
            try {
                sequenceFactory.clearFactory();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to clear the sequence factory.", "File Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to clear the sequence factory.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        SequenceDbDetailsDialog sequenceDbDetailsDialog = new SequenceDbDetailsDialog(parentFrame, lastSelectedFolder, editable, normalIcon, waitingIcon);

        boolean success = sequenceDbDetailsDialog.selectDB(true);
        if (success) {
            sequenceDbDetailsDialog.setVisible(true);
            okButton.setEnabled(true);
        }

        if (sequenceFactory.getCurrentFastaFile() != null) {
            databaseSettingsTxt.setText(sequenceFactory.getCurrentFastaFile().getAbsolutePath());
        }
    }//GEN-LAST:event_editDatabaseDetailsButtonActionPerformed

    /**
     * Cancel the dialog.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        canceled = true;
    }//GEN-LAST:event_formWindowClosing

    private void simplifyGroupsCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simplifyGroupsCmbActionPerformed

        simplifyScoreCmb.setEnabled(editable && simplifyGroupsCmb.getSelectedIndex() == 0);
        simplifyEnzymaticityCmb.setEnabled(editable && simplifyGroupsCmb.getSelectedIndex() == 0);
        simplifyEvidenceCmb.setEnabled(editable && simplifyGroupsCmb.getSelectedIndex() == 0);
        simplifyCharacterizationCmb.setEnabled(editable && simplifyGroupsCmb.getSelectedIndex() == 0);

    }//GEN-LAST:event_simplifyGroupsCmbActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dataBasePanelSettings;
    private javax.swing.JLabel databaseSettingsLbl;
    private javax.swing.JTextField databaseSettingsTxt;
    private javax.swing.JButton editDatabaseDetailsButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox simplifyCharacterizationCmb;
    private javax.swing.JLabel simplifyCharacterizationLbl;
    private javax.swing.JComboBox simplifyEnzymaticityCmb;
    private javax.swing.JLabel simplifyEnzymaticityLbl;
    private javax.swing.JComboBox simplifyEvidenceCmb;
    private javax.swing.JLabel simplifyEvidenceLbl;
    private javax.swing.JComboBox simplifyGroupsCmb;
    private javax.swing.JLabel simplifyGroupsLbl;
    private javax.swing.JComboBox simplifyScoreCmb;
    private javax.swing.JLabel simplifyScoreLbl;
    // End of variables declaration//GEN-END:variables

}
