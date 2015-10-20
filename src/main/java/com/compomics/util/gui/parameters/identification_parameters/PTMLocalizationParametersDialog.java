package com.compomics.util.gui.parameters.identification_parameters;

import com.compomics.util.experiment.identification.ptm.PtmScore;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.preferences.PTMScoringPreferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 *
 * @author Marc Vaudel
 */
public class PTMLocalizationParametersDialog extends javax.swing.JDialog {

    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the processing and identification parameters
     * should be edited upon clicking on OK.
     */
    private boolean editable;

    /**
     * Constructor.
     *
     * @param parentFrame a parent frame
     * @param ptmScoringPreferences the PTM scoring preferences to display
     * @param editable boolean indicating whether the settings can be edited
     */
    public PTMLocalizationParametersDialog(java.awt.Frame parentFrame, PTMScoringPreferences ptmScoringPreferences, boolean editable) {
        super(parentFrame, true);
        initComponents();
        setUpGui();
        populateGUI(ptmScoringPreferences);
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {

        thresholdAutpoCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        neutralLossesCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        scoreCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

    }

    /**
     * Fills the GUI with the given settings.
     *
     * @param spectrumCountingPreferences the PTM scoring preferences to display
     */
    private void populateGUI(PTMScoringPreferences ptmScoringPreferences) {

        scoreCmb.setSelectedItem(ptmScoringPreferences.getSelectedProbabilisticScore());

        if (ptmScoringPreferences.isProbabilitsticScoreCalculation()) {
            scoreCmb.setEnabled(editable);
            neutralLossesCmb.setEnabled(editable);
            if (ptmScoringPreferences.isProbabilisticScoreNeutralLosses()) {
                neutralLossesCmb.setSelectedIndex(0);
            } else {
                neutralLossesCmb.setSelectedIndex(1);
            }
            thresholdAutpoCmb.setEnabled(editable);
            if (ptmScoringPreferences.isEstimateFlr()) {
                thresholdAutpoCmb.setSelectedIndex(0);
                thresholdTxt.setEnabled(false);
                thresholdTxt.setEditable(false);
            } else {
                thresholdAutpoCmb.setSelectedIndex(1);
                thresholdTxt.setEnabled(editable);
                thresholdTxt.setEditable(editable);
                thresholdTxt.setText(ptmScoringPreferences.getProbabilisticScoreThreshold() + "");
            }
        } else {
            neutralLossesCmb.setEnabled(false);
            thresholdAutpoCmb.setEnabled(false);
            thresholdTxt.setEnabled(false);
            thresholdTxt.setEditable(false);
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
     * Validates the user input.
     *
     * @return a boolean indicating whether the user input is valid
     */
    public boolean validateInput() {
        try {
            if (scoreCmb.getSelectedItem() != PtmScore.None && thresholdAutpoCmb.getSelectedIndex() == 1) {
                Double temp = new Double(thresholdTxt.getText().trim());
                if (temp < 0 || temp > 100) {
                    JOptionPane.showMessageDialog(this, "Please verify the input for the score threshold.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    thresholdTxt.requestFocus();
                    return false;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the score threshold.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            thresholdTxt.requestFocus();
            return false;
        }

        if (scoreCmb.getSelectedItem() == PtmScore.None) {
            int outcome = JOptionPane.showConfirmDialog(this,
                    "Disabling the probabilistic score will impair PTM localization and thus distinction\n"
                    + "between peptides. See help for more details. Continue with this setting?",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            if (outcome == JOptionPane.CANCEL_OPTION || outcome == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        }
        if (scoreCmb.getSelectedItem() != PtmScore.None && neutralLossesCmb.getSelectedIndex() == 0) {
            int outcome = JOptionPane.showConfirmDialog(this,
                    "In our experience probabilistic scores perform poorly when accounting for\n"
                    + "neutral losses. See help for more details. Continue with this setting?",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            if (outcome == JOptionPane.CANCEL_OPTION || outcome == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the PTM scoring preferences as set by the user.
     *
     * @return the PTM scoring preferences as set by the user
     */
    public PTMScoringPreferences getPtmScoringPreferences() {
        
        PTMScoringPreferences ptmScoringPreferences = new PTMScoringPreferences();
        
        ptmScoringPreferences.setProbabilitsticScoreCalculation(scoreCmb.getSelectedItem() != PtmScore.None);
        ptmScoringPreferences.setSelectedProbabilisticScore((PtmScore) scoreCmb.getSelectedItem());
        ptmScoringPreferences.setProbabilisticScoreNeutralLosses(neutralLossesCmb.getSelectedIndex() == 0);
        
        if (thresholdAutpoCmb.getSelectedIndex() == 0) {
            ptmScoringPreferences.setEstimateFlr(true);
        } else {
            ptmScoringPreferences.setEstimateFlr(false);
            ptmScoringPreferences.setProbabilisticScoreThreshold(new Double(thresholdTxt.getText().trim()));
        }
        
        return ptmScoringPreferences;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        backgroundPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        ptmScoringPanel = new javax.swing.JPanel();
        scoreCmb = new javax.swing.JComboBox();
        thresholdTxt = new javax.swing.JTextField();
        thresholdLabel = new javax.swing.JLabel();
        scoreTypeLabel = new javax.swing.JLabel();
        neutralLossesLabel = new javax.swing.JLabel();
        neutralLossesCmb = new javax.swing.JComboBox();
        thresholdAutpoLabel = new javax.swing.JLabel();
        thresholdAutpoCmb = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

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

        ptmScoringPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PTM Scoring"));
        ptmScoringPanel.setOpaque(false);

        scoreCmb.setModel(new DefaultComboBoxModel(PtmScore.getScoresAsList()));
        scoreCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scoreCmbActionPerformed(evt);
            }
        });

        thresholdTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        thresholdLabel.setText("Threshold");

        scoreTypeLabel.setText("Probabilistic Score");

        neutralLossesLabel.setText("Account Neutral Losses");

        neutralLossesCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        thresholdAutpoLabel.setText("Threshold Auto");

        thresholdAutpoCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        thresholdAutpoCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thresholdAutpoCmbActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ptmScoringPanelLayout = new javax.swing.GroupLayout(ptmScoringPanel);
        ptmScoringPanel.setLayout(ptmScoringPanelLayout);
        ptmScoringPanelLayout.setHorizontalGroup(
            ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(neutralLossesLabel)
                    .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                        .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                                .addComponent(thresholdAutpoLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                                .addComponent(thresholdAutpoCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(scoreTypeLabel)
                                    .addComponent(thresholdLabel))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ptmScoringPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(scoreCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, 185, Short.MAX_VALUE)
                                        .addComponent(neutralLossesCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(thresholdTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(29, 29, 29))))
        );
        ptmScoringPanelLayout.setVerticalGroup(
            ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scoreTypeLabel)
                    .addComponent(scoreCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neutralLossesLabel)
                    .addComponent(neutralLossesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(thresholdAutpoLabel)
                    .addComponent(thresholdAutpoCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(thresholdLabel)
                    .addComponent(thresholdTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 265, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(ptmScoringPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ptmScoringPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput()) {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void scoreCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scoreCmbActionPerformed
        if (scoreCmb.getSelectedItem() != PtmScore.None) {
            neutralLossesCmb.setEnabled(true);
            thresholdAutpoCmb.setEnabled(true);
            if (thresholdAutpoCmb.getSelectedIndex() == 1) {
                thresholdTxt.setEnabled(true);
                thresholdTxt.setEditable(true);
            } else {
                thresholdTxt.setEnabled(false);
                thresholdTxt.setEditable(false);
            }
        } else {
            neutralLossesCmb.setEnabled(false);
            thresholdAutpoCmb.setEnabled(false);
            thresholdTxt.setEnabled(false);
            thresholdTxt.setEditable(false);
        }
    }//GEN-LAST:event_scoreCmbActionPerformed

    private void thresholdAutpoCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thresholdAutpoCmbActionPerformed
        if (thresholdAutpoCmb.getSelectedIndex() == 1) {
            thresholdTxt.setEnabled(true);
            thresholdTxt.setEditable(true);
        } else {
            thresholdTxt.setEnabled(false);
            thresholdTxt.setEditable(false);
        }
    }//GEN-LAST:event_thresholdAutpoCmbActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox neutralLossesCmb;
    private javax.swing.JLabel neutralLossesLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel ptmScoringPanel;
    private javax.swing.JComboBox scoreCmb;
    private javax.swing.JLabel scoreTypeLabel;
    private javax.swing.JComboBox thresholdAutpoCmb;
    private javax.swing.JLabel thresholdAutpoLabel;
    private javax.swing.JLabel thresholdLabel;
    private javax.swing.JTextField thresholdTxt;
    // End of variables declaration//GEN-END:variables

}
