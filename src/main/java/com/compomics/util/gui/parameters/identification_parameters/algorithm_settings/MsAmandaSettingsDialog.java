package com.compomics.util.gui.parameters.identification_parameters.algorithm_settings;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsAmandaParameters;
import com.compomics.util.gui.GuiUtilities;
import com.compomics.util.gui.parameters.identification_parameters.AlgorithmSettingsDialog;
import java.awt.Dialog;
import javax.swing.SwingConstants;

/**
 * Dialog for the MS Amanda specific settings.
 *
 * @author Harald Barsnes
 */
public class MsAmandaSettingsDialog extends javax.swing.JDialog implements AlgorithmSettingsDialog {

    /**
     * Boolean indicating whether the used canceled the editing.
     */
    private boolean cancelled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates new form MsAmandaSettingsDialog with a frame as owner.
     *
     * @param parent the parent frame
     * @param msAmandaParameters the MS Amanda parameters
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public MsAmandaSettingsDialog(java.awt.Frame parent, MsAmandaParameters msAmandaParameters, boolean editable) {
        super(parent, true);
        this.editable = editable;
        initComponents();
        setUpGUI();
        populateGUI(msAmandaParameters);
        validateInput(false);
        setLocationRelativeTo(parent);
        setVisible(true);
    }


    /**
     * Creates new form MsAmandaSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parent the parent frame
     * @param msAmandaParameters the MS Amanda parameters
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public MsAmandaSettingsDialog(Dialog owner, java.awt.Frame parent, MsAmandaParameters msAmandaParameters, boolean editable) {
        super(owner, true);
        this.editable = editable;
        initComponents();
        setUpGUI();
        populateGUI(msAmandaParameters);
        validateInput(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGUI() {
        
        decoyDatabaseCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        instrumentCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        monoIsotopicCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        lowMemoryModeCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        
        decoyDatabaseCmb.setEnabled(editable);
        instrumentCmb.setEnabled(editable);
        monoIsotopicCmb.setEnabled(editable);
        maxRankTxt.setEditable(editable);
        maxRankTxt.setEnabled(editable);
        lowMemoryModeCmb.setEnabled(editable);
        
    }

    /**
     * Populates the GUI using the given settings.
     * 
     * @param msAmandaParameters the parameters to display
     */
    private void populateGUI(MsAmandaParameters msAmandaParameters) {

        if (msAmandaParameters.generateDecoy()) {
            decoyDatabaseCmb.setSelectedIndex(0);
        } else {
            decoyDatabaseCmb.setSelectedIndex(1);
        }

        instrumentCmb.setSelectedItem(msAmandaParameters.getInstrumentID());
        maxRankTxt.setText(msAmandaParameters.getMaxRank() + "");

        if (msAmandaParameters.isMonoIsotopic()) {
            monoIsotopicCmb.setSelectedIndex(0);
        } else {
            monoIsotopicCmb.setSelectedIndex(1);
        }
        
        if (msAmandaParameters.isLowMemoryMode()) {
            lowMemoryModeCmb.setSelectedIndex(0);
        } else {
            lowMemoryModeCmb.setSelectedIndex(1);
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public IdentificationAlgorithmParameter getParameters() {
        return getInput();
    }

    /**
     * Returns the user selection as MS Amanda parameters object.
     *
     * @return the user selection
     */
    public MsAmandaParameters getInput() {

        MsAmandaParameters result = new MsAmandaParameters();

        result.setGenerateDecoyDatabase(decoyDatabaseCmb.getSelectedIndex() == 0);
        result.setInstrumentID((String) instrumentCmb.getSelectedItem());

        String input = maxRankTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxRank(new Integer(input));
        }

        result.setMonoIsotopic(monoIsotopicCmb.getSelectedIndex() == 0);
        result.setLowMemoryMode(lowMemoryModeCmb.getSelectedIndex() == 0);

        return result;
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
        advancedSearchSettingsPanel = new javax.swing.JPanel();
        instrumentCmb = new javax.swing.JComboBox();
        decoyDatabaseCmb = new javax.swing.JComboBox();
        decoyDatabaseLabel = new javax.swing.JLabel();
        instrumentLabel = new javax.swing.JLabel();
        maxRankLabel = new javax.swing.JLabel();
        maxRankTxt = new javax.swing.JTextField();
        monoIsotopicLabel = new javax.swing.JLabel();
        monoIsotopicCmb = new javax.swing.JComboBox();
        lowMemoryModeLabel = new javax.swing.JLabel();
        lowMemoryModeCmb = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        openDialogHelpJButton = new javax.swing.JButton();
        advancedSettingsWarningLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("MS Amanda Advanced Settings");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        advancedSearchSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Settings"));
        advancedSearchSettingsPanel.setOpaque(false);

        instrumentCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "b, y", "b, y, -H2O, -NH3", "a, b, y, -H2O, -NH3, Imm", "a, b, y, -H2O, -NH3", "a, b, y", "a, b, y, Imm", "a, b, y, z, -H2O, -NH3, Imm", "c, y, z+1, z+2", "b, c, y, z+1, z+2", "b, y, INT", "b, y, INT, Imm", "a, b, y, INT", "a, b, y, INT, IMM", "a, b, y, INT, IMM, -H2O", "a, b, y, INT, IMM, -H2O, -NH3", "a, b, y, INT, IMM, -NH3" }));

        decoyDatabaseCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        decoyDatabaseCmb.setSelectedIndex(1);

        decoyDatabaseLabel.setText("Generate Decoy Database");

        instrumentLabel.setText("Fragment Ion Types");

        maxRankLabel.setText("Max Rank");

        maxRankTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxRankTxt.setText("1");
        maxRankTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxRankTxtKeyReleased(evt);
            }
        });

        monoIsotopicLabel.setText("Monoisotopic");

        monoIsotopicCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        lowMemoryModeLabel.setText("Low Memory Mode");

        lowMemoryModeCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout advancedSearchSettingsPanelLayout = new javax.swing.GroupLayout(advancedSearchSettingsPanel);
        advancedSearchSettingsPanel.setLayout(advancedSearchSettingsPanelLayout);
        advancedSearchSettingsPanelLayout.setHorizontalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(instrumentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(decoyDatabaseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(instrumentCmb, 0, 189, Short.MAX_VALUE)
                            .addComponent(decoyDatabaseCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxRankLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxRankTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(monoIsotopicLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(monoIsotopicCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(lowMemoryModeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lowMemoryModeCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        advancedSearchSettingsPanelLayout.setVerticalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(decoyDatabaseLabel)
                    .addComponent(decoyDatabaseCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(instrumentCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(instrumentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(monoIsotopicLabel)
                    .addComponent(monoIsotopicCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxRankLabel)
                    .addComponent(maxRankTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lowMemoryModeLabel)
                    .addComponent(lowMemoryModeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        openDialogHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        openDialogHelpJButton.setToolTipText("Help");
        openDialogHelpJButton.setBorder(null);
        openDialogHelpJButton.setBorderPainted(false);
        openDialogHelpJButton.setContentAreaFilled(false);
        openDialogHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                openDialogHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                openDialogHelpJButtonMouseExited(evt);
            }
        });
        openDialogHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDialogHelpJButtonActionPerformed(evt);
            }
        });

        advancedSettingsWarningLabel.setText("Click to open the MS Amanda help page.");

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(advancedSearchSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(openDialogHelpJButton)
                        .addGap(18, 18, 18)
                        .addComponent(advancedSettingsWarningLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(advancedSearchSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(openDialogHelpJButton)
                    .addComponent(advancedSettingsWarningLabel)
                    .addComponent(okButton)
                    .addComponent(closeButton))
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
     * Close the dialog without saving the settings.
     *
     * @param evt
     */
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        cancelled = true;
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    /**
     * Save the settings and then close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput(true)) {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void openDialogHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void openDialogHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonMouseExited

    /**
     * Open the MS Amanda help page.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://ms.imp.ac.at/?goto=msamanda");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxRankTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxRankTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxRankTxtKeyReleased

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {
        boolean valid = true;
        valid = GuiUtilities.validateIntegerInput(this, maxRankLabel, maxRankTxt, "number of spectrum matches", "Number Spectrum Matches Error", true, showMessage, valid);
        okButton.setEnabled(valid);
        return valid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedSearchSettingsPanel;
    private javax.swing.JLabel advancedSettingsWarningLabel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox decoyDatabaseCmb;
    private javax.swing.JLabel decoyDatabaseLabel;
    private javax.swing.JComboBox instrumentCmb;
    private javax.swing.JLabel instrumentLabel;
    private javax.swing.JComboBox lowMemoryModeCmb;
    private javax.swing.JLabel lowMemoryModeLabel;
    private javax.swing.JLabel maxRankLabel;
    private javax.swing.JTextField maxRankTxt;
    private javax.swing.JComboBox monoIsotopicCmb;
    private javax.swing.JLabel monoIsotopicLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    // End of variables declaration//GEN-END:variables
}
