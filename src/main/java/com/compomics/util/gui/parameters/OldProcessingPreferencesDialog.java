package com.compomics.util.gui.parameters;

import com.compomics.util.experiment.identification.ptm.PtmScore;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.preferences.FractionSettings;
import com.compomics.util.preferences.IdMatchValidationPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.PTMScoringPreferences;
import com.compomics.util.preferences.PSProcessingPreferences;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * A simple dialog where the user can view/edit the PeptideShaker processing
 * preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class OldProcessingPreferencesDialog extends javax.swing.JDialog {

    /**
     * The processing preferences.
     */
    private PSProcessingPreferences processingPreferences;
    /**
     * The identification parameters
     */
    private IdentificationParameters identificationParameters;
    /**
     * Boolean indicating whether the processing and identification parameters
     * should be edited upon clicking on OK.
     */
    private boolean editable;
    /**
     * Boolean indicating whether the cancel button was pressed.
     */
    private boolean canceled = false;

    /**
     * Creates a new parameters dialog.
     *
     * @param parent the parent frame
     * @param editable a boolean indicating whether the processing parameters
     * are editable
     * @param identificationParameters the identification parameters
     * @param processingPreferences the processing preferences
     */
    public OldProcessingPreferencesDialog(java.awt.Frame parent, boolean editable, IdentificationParameters identificationParameters, PSProcessingPreferences processingPreferences) {
        super(parent, true);
        initComponents();
        this.processingPreferences = processingPreferences;
        this.identificationParameters = identificationParameters;
        this.editable = editable;
        setUpGui();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates a new parameters dialog.
     *
     * @param parent the parent dialog
     * @param editable a boolean indicating whether the processing parameters
     * are editable
     * @param identificationParameters the identification parameters
     * @param processingPreferences the processing preferences
     */
    public OldProcessingPreferencesDialog(JDialog parent, boolean editable, IdentificationParameters identificationParameters, PSProcessingPreferences processingPreferences) {
        super(parent, true);
        initComponents();
        this.processingPreferences = processingPreferences;
        this.identificationParameters = identificationParameters;
        this.editable = editable;
        setUpGui();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Sets up the GUI according the given data.
     */
    private void setUpGui() {

        IdMatchValidationPreferences idMatchValidationPreferences = identificationParameters.getIdValidationPreferences();
        proteinFdrTxt.setText(idMatchValidationPreferences.getDefaultProteinFDR() + "");
        peptideFdrTxt.setText(idMatchValidationPreferences.getDefaultPeptideFDR() + "");
        psmFdrTxt.setText(idMatchValidationPreferences.getDefaultPsmFDR() + "");

        proteinFdrTxt.setEditable(editable);
        peptideFdrTxt.setEditable(editable);
        psmFdrTxt.setEditable(editable);
        proteinFdrTxt.setEnabled(editable);
        peptideFdrTxt.setEnabled(editable);
        psmFdrTxt.setEnabled(editable);
        proteinConfidenceMwTxt.setEnabled(editable);

        PTMScoringPreferences ptmScoringPreferences = identificationParameters.getPtmScoringPreferences();
        
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

        FractionSettings fractionSettings = identificationParameters.getFractionSettings();
        proteinConfidenceMwTxt.setText(fractionSettings.getProteinConfidenceMwPlots() + "");

        thresholdAutpoCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        neutralLossesCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        scoreCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
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
        thresholdLabel = new javax.swing.JLabel();
        scoreTypeLabel = new javax.swing.JLabel();
        neutralLossesLabel = new javax.swing.JLabel();
        neutralLossesCmb = new javax.swing.JComboBox();
        thresholdAutpoLabel = new javax.swing.JLabel();
        thresholdAutpoCmb = new javax.swing.JComboBox();
        fractionsPanel = new javax.swing.JPanel();
        proteinMwLabel = new javax.swing.JLabel();
        proteinConfidenceMwTxt = new javax.swing.JTextField();
        percentLabel4 = new javax.swing.JLabel();
        helpJButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        neutralLossesLabel2.setText("Account Neutral Losses");

        neutralLossesCmb2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Processing");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptideFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentLabel2)
                    .addComponent(peptideFdrLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psmFdrLabel)
                    .addComponent(psmFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
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
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(ptmScoringPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fractionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(processingParamsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

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
                    .addComponent(okButton)
                    .addComponent(cancelButton))
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
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Update the preferences and close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (editable && validateInput()) {

            IdMatchValidationPreferences idMatchValidationPreferences = identificationParameters.getIdValidationPreferences();
            idMatchValidationPreferences.setDefaultProteinFDR(new Double(proteinFdrTxt.getText().trim()));
            idMatchValidationPreferences.setDefaultPeptideFDR(new Double(peptideFdrTxt.getText().trim()));
            idMatchValidationPreferences.setDefaultPsmFDR(new Double(psmFdrTxt.getText().trim()));

            PTMScoringPreferences ptmScoringPreferences = identificationParameters.getPtmScoringPreferences();
            ptmScoringPreferences.setProbabilitsticScoreCalculation(scoreCmb.getSelectedItem() != PtmScore.None);
            ptmScoringPreferences.setSelectedProbabilisticScore((PtmScore) scoreCmb.getSelectedItem());
            ptmScoringPreferences.setProbabilisticScoreNeutralLosses(neutralLossesCmb.getSelectedIndex() == 0);

            if (thresholdAutpoCmb.getSelectedIndex() == 0) {
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
     * Change the threshold settings.
     *
     * @param evt
     */
    private void thresholdAutpoCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thresholdAutpoCmbActionPerformed
        if (thresholdAutpoCmb.getSelectedIndex() == 1) {
            thresholdTxt.setEnabled(true);
            thresholdTxt.setEditable(true);
        } else {
            thresholdTxt.setEnabled(false);
            thresholdTxt.setEditable(false);
        }
    }//GEN-LAST:event_thresholdAutpoCmbActionPerformed

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
                null, "Processing - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Close the dialog without saving the settings.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Close the dialog without saving the settings.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Change the probabilistic score settings.
     *
     * @param evt
     */
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel fractionsPanel;
    private javax.swing.JButton helpJButton;
    private javax.swing.JComboBox neutralLossesCmb;
    private javax.swing.JComboBox neutralLossesCmb2;
    private javax.swing.JLabel neutralLossesLabel;
    private javax.swing.JLabel neutralLossesLabel2;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel peptideFdrLabel;
    private javax.swing.JTextField peptideFdrTxt;
    private javax.swing.JLabel percentLabel;
    private javax.swing.JLabel percentLabel2;
    private javax.swing.JLabel percentLabel3;
    private javax.swing.JLabel percentLabel4;
    private javax.swing.JPanel processingParamsPanel;
    private javax.swing.JTextField proteinConfidenceMwTxt;
    private javax.swing.JLabel proteinFdrLabel;
    private javax.swing.JTextField proteinFdrTxt;
    private javax.swing.JLabel proteinMwLabel;
    private javax.swing.JLabel psmFdrLabel;
    private javax.swing.JTextField psmFdrTxt;
    private javax.swing.JPanel ptmScoringPanel;
    private javax.swing.JComboBox scoreCmb;
    private javax.swing.JLabel scoreTypeLabel;
    private javax.swing.JComboBox thresholdAutpoCmb;
    private javax.swing.JLabel thresholdAutpoLabel;
    private javax.swing.JLabel thresholdLabel;
    private javax.swing.JTextField thresholdTxt;
    // End of variables declaration//GEN-END:variables

    /**
     * Indicates whether the cancel button was pressed by the user.
     *
     * @return a boolean indicating whether the cancel button was pressed by the
     * user
     */
    public boolean isCanceled() {
        return canceled;
    }
}
