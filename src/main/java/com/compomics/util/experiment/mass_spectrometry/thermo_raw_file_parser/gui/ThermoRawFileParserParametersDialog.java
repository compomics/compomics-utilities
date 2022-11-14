package com.compomics.util.experiment.mass_spectrometry.thermo_raw_file_parser.gui;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.mass_spectrometry.thermo_raw_file_parser.ThermoRawFileParserOutputFormat;
import com.compomics.util.experiment.mass_spectrometry.thermo_raw_file_parser.ThermoRawFileParserParameters;
import java.awt.Dialog;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * Dialog for the creation and edition of ThermoRawFileParser parameters.
 *
 * @author Harald Barsnes
 */
public class ThermoRawFileParserParametersDialog extends javax.swing.JDialog {

    /**
     * Boolean indicating whether the editing was canceled.
     */
    private boolean canceled = false;

    /**
     * Constructor.
     *
     * @param parent the parent frame
     * @param thermoRawFileParserParameters initial parameters, ignored if null
     */
    public ThermoRawFileParserParametersDialog(
            java.awt.Frame parent, 
            ThermoRawFileParserParameters thermoRawFileParserParameters
    ) {
        super(parent, true);
        initComponents();
        setUpGUI();
        populateGUI(thermoRawFileParserParameters);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Constructor.
     *
     * @param parent the parent dialog
     * @param thermoRawFileParserParameters initial parameters, ignored if null
     */
    public ThermoRawFileParserParametersDialog(
            Dialog parent, 
            ThermoRawFileParserParameters thermoRawFileParserParameters
    ) {
        super(parent, true);
        initComponents();
        setUpGUI();
        populateGUI(thermoRawFileParserParameters);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Sets up the GUI components.
     */
    private void setUpGUI() {
        outputFormatCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        peakPickingCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
    }

    /**
     * Populates the GUI using the given settings.
     *
     * @param thermoRawFileParserParameters the parameters to display
     */
    private void populateGUI(
            ThermoRawFileParserParameters thermoRawFileParserParameters
    ) {

        outputFormatCmb.setSelectedItem(thermoRawFileParserParameters.getOutputFormat());

        if (thermoRawFileParserParameters.isPeackPicking()) {
            peakPickingCmb.setSelectedIndex(0);
        } else {
            peakPickingCmb.setSelectedIndex(1);
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

        backgourdPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        installationPanel = new javax.swing.JPanel();
        outputPepXmlLabel = new javax.swing.JLabel();
        outputFormatCmb = new javax.swing.JComboBox();
        peakPickingLabel = new javax.swing.JLabel();
        peakPickingCmb = new javax.swing.JComboBox();
        openDialogHelpJButton = new javax.swing.JButton();
        advancedSettingsWarningLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("ThermoRawFileParser Settings");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgourdPanel.setBackground(new java.awt.Color(230, 230, 230));

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

        installationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Conversion Setttings"));
        installationPanel.setOpaque(false);

        outputPepXmlLabel.setText("Output Format");

        outputFormatCmb.setModel(new DefaultComboBoxModel(ThermoRawFileParserOutputFormat.values()));

        peakPickingLabel.setText("Peak Picking");

        peakPickingCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout installationPanelLayout = new javax.swing.GroupLayout(installationPanel);
        installationPanel.setLayout(installationPanelLayout);
        installationPanelLayout.setHorizontalGroup(
            installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(installationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(outputPepXmlLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                    .addComponent(peakPickingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(peakPickingCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(outputFormatCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        installationPanelLayout.setVerticalGroup(
            installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(installationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputPepXmlLabel)
                    .addComponent(outputFormatCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peakPickingLabel)
                    .addComponent(peakPickingCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

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

        advancedSettingsWarningLabel.setText("Open ThermoRawFileParser web page");

        javax.swing.GroupLayout backgourdPanelLayout = new javax.swing.GroupLayout(backgourdPanel);
        backgourdPanel.setLayout(backgourdPanelLayout);
        backgourdPanelLayout.setHorizontalGroup(
            backgourdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgourdPanelLayout.createSequentialGroup()
                .addGroup(backgourdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgourdPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(installationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(backgourdPanelLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(openDialogHelpJButton)
                        .addGap(18, 18, 18)
                        .addComponent(advancedSettingsWarningLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        backgourdPanelLayout.setVerticalGroup(
            backgourdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgourdPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(installationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgourdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(openDialogHelpJButton)
                    .addComponent(advancedSettingsWarningLabel)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgourdPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgourdPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Cancel the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        boolean formatCheck = true;
        
        ThermoRawFileParserOutputFormat selectedFormat = ((ThermoRawFileParserOutputFormat) outputFormatCmb.getSelectedItem());

        if (selectedFormat != ThermoRawFileParserOutputFormat.mgf
                && selectedFormat != ThermoRawFileParserOutputFormat.mzML
                && selectedFormat != ThermoRawFileParserOutputFormat.mzMLIndexed) {
            
            int value = JOptionPane.showConfirmDialog(this, "This format is not compatible with SearchGUI. Proceed anyway?",
                    "Output Format Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (value == JOptionPane.NO_OPTION) {
                formatCheck = false;
            }
        }

        if (formatCheck) {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

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
     * Open the ThermoRawFileParser web page.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://github.com/compomics/ThermoRawFileParser");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel advancedSettingsWarningLabel;
    private javax.swing.JPanel backgourdPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel installationPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JComboBox outputFormatCmb;
    private javax.swing.JLabel outputPepXmlLabel;
    private javax.swing.JComboBox peakPickingCmb;
    private javax.swing.JLabel peakPickingLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Indicates whether the editing was canceled by the user.
     *
     * @return a boolean indicating whether the editing was canceled by the user
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the parameters as created by the user.
     *
     * @return the parameters as created by the user
     */
    public ThermoRawFileParserParameters getThermoRawFileParserParameters() {
        ThermoRawFileParserParameters thermoRawFileParserParameters = new ThermoRawFileParserParameters();
        thermoRawFileParserParameters.setMsFormat((ThermoRawFileParserOutputFormat) outputFormatCmb.getSelectedItem());
        thermoRawFileParserParameters.setPeackPicking(peakPickingCmb.getSelectedIndex() == 0);
        return thermoRawFileParserParameters;
    }
}
