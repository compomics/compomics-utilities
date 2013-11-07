package com.compomics.util.preferences.gui;

import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.preferences.PTMScoringPreferences;
import com.compomics.util.preferences.ProcessingPreferences;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * A simple dialog where the user can view/edit the PeptideShaker processing
 * preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ProcessingPreferencesDialog extends javax.swing.JDialog {

    /**
     * The processing preferences.
     */
    private ProcessingPreferences processingPreferences;
    /**
     * The PTM preferences. This deserves to be modifiable after import of the
     * files as well.
     */
    private PTMScoringPreferences ptmScoringPreferences;

    /**
     * Creates a new parameters dialog.
     *
     * @param parent the parent frame
     * @param editable a boolean indicating whether the processing parameters
     * are editable
     * @param processingPreferences the processing preferences
     * @param ptmScoringPreferences the PTM scoring preferences
     */
    public ProcessingPreferencesDialog(java.awt.Frame parent, boolean editable,
            ProcessingPreferences processingPreferences, PTMScoringPreferences ptmScoringPreferences) {
        super(parent, true);
        initComponents();

        proteinFdrTxt.setText(processingPreferences.getProteinFDR() + "");
        peptideFdrTxt.setText(processingPreferences.getPeptideFDR() + "");
        psmFdrTxt.setText(processingPreferences.getPsmFDR() + "");

        proteinFdrTxt.setEditable(editable);
        peptideFdrTxt.setEditable(editable);
        psmFdrTxt.setEditable(editable);
        proteinFdrTxt.setEnabled(editable);
        peptideFdrTxt.setEnabled(editable);
        psmFdrTxt.setEnabled(editable);
        probabilisticScoreCmb.setEnabled(editable);
        proteinConfidenceMwTxt.setEnabled(editable);

        if (ptmScoringPreferences.isProbabilitsticScoreCalculation()) {
            probabilisticScoreCmb.setSelectedIndex(0);
            scoreCmb.setEnabled(editable);
            if (ptmScoringPreferences.getSelectedProbabilisticScore() != null ) {
            scoreCmb.setSelectedItem(ptmScoringPreferences.getSelectedProbabilisticScore().getName());
            }
            neutralLossesCmb.setEnabled(editable);
            if (ptmScoringPreferences.isProbabilisticScoreNeutralLosses()) {
                neutralLossesCmb.setSelectedIndex(0);
            } else {
                neutralLossesCmb.setSelectedIndex(1);
            }
            thresholdCmb.setEnabled(editable);
            if (ptmScoringPreferences.isEstimateFlr()) {
                thresholdCmb.setSelectedIndex(0);
                thresholdTxt.setEnabled(false);
                thresholdTxt.setEditable(false);
            } else {
                thresholdCmb.setSelectedIndex(1);
                thresholdTxt.setEnabled(editable);
                thresholdTxt.setEditable(editable);
                thresholdTxt.setText(ptmScoringPreferences.getProbabilisticScoreThreshold() + "");
            }
        } else {
            probabilisticScoreCmb.setSelectedIndex(1);
            scoreCmb.setEnabled(false);
            neutralLossesCmb.setEnabled(false);
            thresholdCmb.setEnabled(false);
            thresholdTxt.setEnabled(false);
            thresholdTxt.setEditable(false);
        }

        proteinConfidenceMwTxt.setText(processingPreferences.getProteinConfidenceMwPlots() + "");

        probabilisticScoreCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        thresholdCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        neutralLossesCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        scoreCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        this.processingPreferences = processingPreferences;
        this.ptmScoringPreferences = ptmScoringPreferences;

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Indicates whether the input is correct.
     *
     * @return a boolean indicating whether the input is correct
     */
    private boolean validateInput() {
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
        try {
            if (probabilisticScoreCmb.getSelectedIndex() == 0 && thresholdCmb.getSelectedIndex() == 1) {
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

        if (probabilisticScoreCmb.getSelectedIndex() == 1) {
            int outcome = JOptionPane.showConfirmDialog(this, "Disabling the probabilistic score will impair PTM localization and thus distinction between peptides. See help for more details. Continue with this setting?",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            if (outcome == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        if (probabilisticScoreCmb.getSelectedIndex() == 0 && neutralLossesCmb.getSelectedIndex() == 0) {
            int outcome = JOptionPane.showConfirmDialog(this, "In our experience probabilistic scores perform poorely when accounting for neutral losses. See help for more details. Continue with this setting?",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            if (outcome == JOptionPane.CANCEL_OPTION) {
                return false;
            }
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

        neutralLossesLabel2 = new javax.swing.JLabel();
        neutralLossesCmb2 = new javax.swing.JComboBox();
        backgroundPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        processingParamsPanel = new javax.swing.JPanel();
        proteinFdrLabel = new javax.swing.JLabel();
        peptideFdrLabel = new javax.swing.JLabel();
        psmFdrLabel = new javax.swing.JLabel();
        psmFdrTxt = new javax.swing.JTextField();
        percentLabel = new javax.swing.JLabel();
        peptideFdrTxt = new javax.swing.JTextField();
        percentLabel2 = new javax.swing.JLabel();
        proteinFdrTxt = new javax.swing.JTextField();
        percentLabel3 = new javax.swing.JLabel();
        ptmScoringPanel = new javax.swing.JPanel();
        scoreCmb = new javax.swing.JComboBox();
        thresholdTxt = new javax.swing.JTextField();
        aScoreLabel = new javax.swing.JLabel();
        probabilisticScoreCmb = new javax.swing.JComboBox();
        neutralLossesLabel = new javax.swing.JLabel();
        estimateAScoreLabel = new javax.swing.JLabel();
        neutralLossesLabel1 = new javax.swing.JLabel();
        neutralLossesCmb = new javax.swing.JComboBox();
        neutralLossesLabel3 = new javax.swing.JLabel();
        thresholdCmb = new javax.swing.JComboBox();
        fractionsPanel = new javax.swing.JPanel();
        proteinMwLabel = new javax.swing.JLabel();
        proteinConfidenceMwTxt = new javax.swing.JTextField();
        percentLabel4 = new javax.swing.JLabel();
        helpJButton = new javax.swing.JButton();

        neutralLossesLabel2.setText("Account Neutral Losses");

        neutralLossesCmb2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Processing");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        processingParamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Processing Parameters"));
        processingParamsPanel.setOpaque(false);

        proteinFdrLabel.setText("Protein FDR");

        peptideFdrLabel.setText("Peptide FDR");

        psmFdrLabel.setText("PSM FDR");

        psmFdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        psmFdrTxt.setText("1");

        percentLabel.setText("%");

        peptideFdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptideFdrTxt.setText("1");

        percentLabel2.setText("%");

        proteinFdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        proteinFdrTxt.setText("1");

        percentLabel3.setText("%");

        javax.swing.GroupLayout processingParamsPanelLayout = new javax.swing.GroupLayout(processingParamsPanel);
        processingParamsPanel.setLayout(processingParamsPanelLayout);
        processingParamsPanelLayout.setHorizontalGroup(
            processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(processingParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(proteinFdrLabel)
                    .addComponent(peptideFdrLabel)
                    .addComponent(psmFdrLabel))
                .addGap(32, 92, Short.MAX_VALUE)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, processingParamsPanelLayout.createSequentialGroup()
                        .addComponent(proteinFdrTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(percentLabel3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, processingParamsPanelLayout.createSequentialGroup()
                        .addComponent(peptideFdrTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(percentLabel2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, processingParamsPanelLayout.createSequentialGroup()
                        .addComponent(psmFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(percentLabel)))
                .addContainerGap())
        );
        processingParamsPanelLayout.setVerticalGroup(
            processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(processingParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proteinFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentLabel3)
                    .addComponent(proteinFdrLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptideFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentLabel2)
                    .addComponent(peptideFdrLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psmFdrLabel)
                    .addComponent(psmFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ptmScoringPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PTM Scoring"));
        ptmScoringPanel.setOpaque(false);

        scoreCmb.setModel(new DefaultComboBoxModel(PTMScoringPreferences.ProbabilisticScore.getPossibilitiesAsString()));

        thresholdTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        aScoreLabel.setText("Threshold");

        probabilisticScoreCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        probabilisticScoreCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                probabilisticScoreCmbActionPerformed(evt);
            }
        });

        neutralLossesLabel.setText("Score");

        estimateAScoreLabel.setText("Probabilistic Score");

        neutralLossesLabel1.setText("Account Neutral Losses");

        neutralLossesCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        neutralLossesLabel3.setText("Threshold Auto");

        thresholdCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        thresholdCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thresholdCmbActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ptmScoringPanelLayout = new javax.swing.GroupLayout(ptmScoringPanel);
        ptmScoringPanel.setLayout(ptmScoringPanelLayout);
        ptmScoringPanelLayout.setHorizontalGroup(
            ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(neutralLossesLabel1)
                    .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                        .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                                .addComponent(estimateAScoreLabel)
                                .addGap(10, 61, Short.MAX_VALUE)
                                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(probabilisticScoreCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, 185, Short.MAX_VALUE)
                                    .addComponent(scoreCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, 185, Short.MAX_VALUE)
                                    .addComponent(neutralLossesCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(neutralLossesLabel)
                            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                                .addComponent(aScoreLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                                .addComponent(neutralLossesLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(thresholdCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(thresholdTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(29, 29, 29))))
        );
        ptmScoringPanelLayout.setVerticalGroup(
            ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ptmScoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(estimateAScoreLabel)
                    .addComponent(probabilisticScoreCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neutralLossesLabel)
                    .addComponent(scoreCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neutralLossesLabel1)
                    .addComponent(neutralLossesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neutralLossesLabel3)
                    .addComponent(thresholdCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(ptmScoringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aScoreLabel)
                    .addComponent(thresholdTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

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
                .addComponent(proteinConfidenceMwTxt)
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

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.setBorder(null);
        helpJButton.setBorderPainted(false);
        helpJButton.setContentAreaFilled(false);
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
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton))
                    .addComponent(ptmScoringPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fractionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(processingParamsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(processingParamsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ptmScoringPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fractionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(helpJButton)
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

    /**
     * Update the preferences and close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput()) {
            processingPreferences.setProteinFDR(new Double(proteinFdrTxt.getText().trim()));
            processingPreferences.setPeptideFDR(new Double(peptideFdrTxt.getText().trim()));
            processingPreferences.setPsmFDR(new Double(psmFdrTxt.getText().trim()));
            ptmScoringPreferences.setProbabilitsticScoreCalculation(probabilisticScoreCmb.getSelectedIndex() == 0);
            ptmScoringPreferences.setSelectedProbabilisticScore(PTMScoringPreferences.ProbabilisticScore.getProbabilisticScoreFromName(scoreCmb.getSelectedItem().toString()));
            ptmScoringPreferences.setProbabilisticScoreNeutralLosses(neutralLossesCmb.getSelectedIndex() == 0);
            if (thresholdCmb.getSelectedIndex() == 0) {
                ptmScoringPreferences.setEstimateFlr(true);
            } else {
                ptmScoringPreferences.setEstimateFlr(false);
                ptmScoringPreferences.setProbabilisticScoreThreshold(new Double(thresholdTxt.getText().trim()));
            }
            processingPreferences.setProteinConfidenceMwPlots(new Double(proteinConfidenceMwTxt.getText().trim()));
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Change the probabilistic score settings.
     *
     * @param evt
     */
    private void probabilisticScoreCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_probabilisticScoreCmbActionPerformed
        if (probabilisticScoreCmb.getSelectedIndex() == 0) {
            scoreCmb.setEnabled(true);
            neutralLossesCmb.setEnabled(true);
            thresholdCmb.setEnabled(true);
            if (thresholdCmb.getSelectedIndex() == 1) {
                thresholdTxt.setEnabled(true);
                thresholdTxt.setEditable(true);
            } else {
                thresholdTxt.setEnabled(false);
                thresholdTxt.setEditable(false);
            }
        } else {
            scoreCmb.setEnabled(false);
            neutralLossesCmb.setEnabled(false);
            thresholdCmb.setEnabled(false);
            thresholdTxt.setEnabled(false);
            thresholdTxt.setEditable(false);
        }
    }//GEN-LAST:event_probabilisticScoreCmbActionPerformed

    /**
     * Change the threshold settings.
     *
     * @param evt
     */
    private void thresholdCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thresholdCmbActionPerformed
        if (thresholdCmb.getSelectedIndex() == 1) {
            thresholdTxt.setEnabled(true);
            thresholdTxt.setEditable(true);
        } else {
            thresholdTxt.setEnabled(false);
            thresholdTxt.setEditable(false);
        }
    }//GEN-LAST:event_thresholdCmbActionPerformed

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
        new HelpDialog(this, getClass().getResource("/helpFiles/ProcessingPreferences.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                null, "Processing Preference Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aScoreLabel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JLabel estimateAScoreLabel;
    private javax.swing.JPanel fractionsPanel;
    private javax.swing.JButton helpJButton;
    private javax.swing.JComboBox neutralLossesCmb;
    private javax.swing.JComboBox neutralLossesCmb2;
    private javax.swing.JLabel neutralLossesLabel;
    private javax.swing.JLabel neutralLossesLabel1;
    private javax.swing.JLabel neutralLossesLabel2;
    private javax.swing.JLabel neutralLossesLabel3;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel peptideFdrLabel;
    private javax.swing.JTextField peptideFdrTxt;
    private javax.swing.JLabel percentLabel;
    private javax.swing.JLabel percentLabel2;
    private javax.swing.JLabel percentLabel3;
    private javax.swing.JLabel percentLabel4;
    private javax.swing.JComboBox probabilisticScoreCmb;
    private javax.swing.JPanel processingParamsPanel;
    private javax.swing.JTextField proteinConfidenceMwTxt;
    private javax.swing.JLabel proteinFdrLabel;
    private javax.swing.JTextField proteinFdrTxt;
    private javax.swing.JLabel proteinMwLabel;
    private javax.swing.JLabel psmFdrLabel;
    private javax.swing.JTextField psmFdrTxt;
    private javax.swing.JPanel ptmScoringPanel;
    private javax.swing.JComboBox scoreCmb;
    private javax.swing.JComboBox thresholdCmb;
    private javax.swing.JTextField thresholdTxt;
    // End of variables declaration//GEN-END:variables
}
