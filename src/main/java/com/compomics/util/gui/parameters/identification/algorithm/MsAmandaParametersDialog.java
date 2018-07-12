package com.compomics.util.gui.parameters.identification.algorithm;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;
import com.compomics.util.parameters.identification.tool_specific.MsAmandaParameters;
import com.compomics.util.gui.GuiUtilities;
import com.compomics.util.gui.JOptionEditorPane;
import java.awt.Dialog;
import javax.swing.SwingConstants;
import com.compomics.util.gui.parameters.identification.AlgorithmParametersDialog;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Dialog for the MS Amanda specific settings.
 *
 * @author Harald Barsnes
 */
public class MsAmandaParametersDialog extends javax.swing.JDialog implements AlgorithmParametersDialog {

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
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public MsAmandaParametersDialog(java.awt.Frame parent, MsAmandaParameters msAmandaParameters, boolean editable) {
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
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public MsAmandaParametersDialog(Dialog owner, java.awt.Frame parent, MsAmandaParameters msAmandaParameters, boolean editable) {
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
        performDeisotopingCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxModPerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxVariableModPerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxPotentialModSitePerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxNeutralLossesPerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxPtmNeutalLossesPerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        outputFormatCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        decoyDatabaseCmb.setEnabled(editable);
        instrumentCmb.setEnabled(editable);
        monoIsotopicCmb.setEnabled(editable);
        maxRankTxt.setEditable(editable);
        maxRankTxt.setEnabled(editable);
        performDeisotopingCmb.setEnabled(editable);
        maxModPerPeptideCmb.setEnabled(editable);
        maxVariableModPerPeptideCmb.setEnabled(editable);
        maxPotentialModSitePerPeptideCmb.setEnabled(editable);
        maxNeutralLossesPerPeptideCmb.setEnabled(editable);
        maxPtmNeutalLossesPerPeptideCmb.setEnabled(editable);
        minPeptideLengthTxt.setEnabled(editable);
        maxProteinsLoadedTxt.setEnabled(editable);
        maxSpectraLoadedTxt.setEnabled(editable);
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
        maxRankTxt.setText(msAmandaParameters.getMaxRank().toString());

        if (msAmandaParameters.isMonoIsotopic()) {
            monoIsotopicCmb.setSelectedIndex(0);
        } else {
            monoIsotopicCmb.setSelectedIndex(1);
        }

        if (msAmandaParameters.isPerformDeisotoping()) {
            performDeisotopingCmb.setSelectedIndex(0);
        } else {
            performDeisotopingCmb.setSelectedIndex(1);
        }

        maxModPerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxModifications());
        maxVariableModPerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxVariableModifications());
        maxPotentialModSitePerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxModificationSites());
        maxNeutralLossesPerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxNeutralLosses());
        maxPtmNeutalLossesPerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxNeutralLossesPerModification());

        minPeptideLengthTxt.setText(msAmandaParameters.getMinPeptideLength().toString());
        maxProteinsLoadedTxt.setText(msAmandaParameters.getMaxLoadedProteins().toString());
        maxSpectraLoadedTxt.setText(msAmandaParameters.getMaxLoadedSpectra().toString());
        
        if (msAmandaParameters.getOutputFormat().equalsIgnoreCase("csv")) {
            outputFormatCmb.setSelectedIndex(0);
        } else {
            outputFormatCmb.setSelectedIndex(1);
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
        result.setPerformDeisotoping(performDeisotopingCmb.getSelectedIndex() == 0);

        result.setMaxModifications(maxModPerPeptideCmb.getSelectedIndex());
        result.setMaxVariableModifications(maxVariableModPerPeptideCmb.getSelectedIndex());
        result.setMaxModificationSites(maxPotentialModSitePerPeptideCmb.getSelectedIndex());
        result.setMaxNeutralLosses(maxNeutralLossesPerPeptideCmb.getSelectedIndex());
        result.setMaxNeutralLossesPerModification(maxPtmNeutalLossesPerPeptideCmb.getSelectedIndex());

        input = minPeptideLengthTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPeptideLength(new Integer(input));
        }

        input = maxProteinsLoadedTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxLoadedProteins(new Integer(input));
        }

        input = maxSpectraLoadedTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxLoadedSpectra(new Integer(input));
        }
        
        result.setOutputFormat((String) outputFormatCmb.getSelectedItem());

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
        performDeisotopingLabel = new javax.swing.JLabel();
        performDeisotopingCmb = new javax.swing.JComboBox();
        maxModPerPeptideLabel = new javax.swing.JLabel();
        maxModPerPeptideCmb = new javax.swing.JComboBox();
        maxVariableModPerPeptideLabel = new javax.swing.JLabel();
        maxVariableModPerPeptideCmb = new javax.swing.JComboBox();
        maxPotentialModSitePerPeptideLabel = new javax.swing.JLabel();
        maxPotentialModSitePerPeptideCmb = new javax.swing.JComboBox();
        maxNeutralLossesPerPeptideLabel = new javax.swing.JLabel();
        maxNeutralLossesPerPeptideCmb = new javax.swing.JComboBox();
        maxPtmNeutalLossesPerPeptideLabel = new javax.swing.JLabel();
        maxPtmNeutalLossesPerPeptideCmb = new javax.swing.JComboBox();
        minPeptideLengthLabel = new javax.swing.JLabel();
        minPeptideLengthTxt = new javax.swing.JTextField();
        maxProteinsLoadedLabel = new javax.swing.JLabel();
        maxProteinsLoadedTxt = new javax.swing.JTextField();
        maxSpectraLoadedLabel = new javax.swing.JLabel();
        maxSpectraLoadedTxt = new javax.swing.JTextField();
        outputFormatLabel = new javax.swing.JLabel();
        outputFormatCmb = new javax.swing.JComboBox();
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

        performDeisotopingLabel.setText("Perform Deisotoping");

        performDeisotopingCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        maxModPerPeptideLabel.setText("Max PTM Duplicates per Peptide");
        maxModPerPeptideLabel.setToolTipText("Max number of occurrences of a specific modification on a peptide");

        maxModPerPeptideCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        maxModPerPeptideCmb.setSelectedIndex(3);
        maxModPerPeptideCmb.setToolTipText("Max number of occurrences of a specific modification on a peptide");

        maxVariableModPerPeptideLabel.setText("Max Variable PTMs per Peptide");
        maxVariableModPerPeptideLabel.setToolTipText("Max number of variable modifications per peptide");

        maxVariableModPerPeptideCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        maxVariableModPerPeptideCmb.setSelectedIndex(4);
        maxVariableModPerPeptideCmb.setToolTipText("Max number of variable modifications per peptide");

        maxPotentialModSitePerPeptideLabel.setText("Max Potential PTM sites per PTM");
        maxPotentialModSitePerPeptideLabel.setToolTipText("Max number of potential modification sites per modification per peptide");

        maxPotentialModSitePerPeptideCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" }));
        maxPotentialModSitePerPeptideCmb.setSelectedIndex(6);
        maxPotentialModSitePerPeptideCmb.setToolTipText("Max number of potential modification sites per modification per peptide");

        maxNeutralLossesPerPeptideLabel.setText("Max Neutral Losses per Peptide");
        maxNeutralLossesPerPeptideLabel.setToolTipText("Max number of water and ammonia losses per peptide");

        maxNeutralLossesPerPeptideCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5" }));
        maxNeutralLossesPerPeptideCmb.setSelectedIndex(1);
        maxNeutralLossesPerPeptideCmb.setToolTipText("Max number of water and ammonia losses per peptide");

        maxPtmNeutalLossesPerPeptideLabel.setText("Max PTM Neutral Losses per Peptide");
        maxPtmNeutalLossesPerPeptideLabel.setToolTipText("Max number identical modification specific losses per peptide");

        maxPtmNeutalLossesPerPeptideCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5" }));
        maxPtmNeutalLossesPerPeptideCmb.setSelectedIndex(1);
        maxPtmNeutalLossesPerPeptideCmb.setToolTipText("Max number identical modification specific losses per peptide");

        minPeptideLengthLabel.setText("Min Peptide Length");
        minPeptideLengthLabel.setToolTipText("Minimum peptide length");

        minPeptideLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPeptideLengthTxt.setText("6");
        minPeptideLengthTxt.setToolTipText("Minimum peptide length");
        minPeptideLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPeptideLengthTxtKeyReleased(evt);
            }
        });

        maxProteinsLoadedLabel.setText("Max Proteins Loaded into Memory");
        maxProteinsLoadedLabel.setToolTipText("Max number of proteins loaded into memory (1000-500000)");

        maxProteinsLoadedTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxProteinsLoadedTxt.setText("100000");
        maxProteinsLoadedTxt.setToolTipText("Max number of proteins loaded into memory (1000-500000)");
        maxProteinsLoadedTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxProteinsLoadedTxtKeyReleased(evt);
            }
        });

        maxSpectraLoadedLabel.setText("Max Spectra Loaded into Memory");
        maxSpectraLoadedLabel.setToolTipText("Max number of spectra loaded into memory (1000-500000)");

        maxSpectraLoadedTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxSpectraLoadedTxt.setText("2000");
        maxSpectraLoadedTxt.setToolTipText("Max number of spectra loaded into memory (1000-500000)");
        maxSpectraLoadedTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxSpectraLoadedTxtKeyReleased(evt);
            }
        });

        outputFormatLabel.setText("Output Format");

        outputFormatCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "csv", "mzIdentML" }));

        javax.swing.GroupLayout advancedSearchSettingsPanelLayout = new javax.swing.GroupLayout(advancedSearchSettingsPanel);
        advancedSearchSettingsPanel.setLayout(advancedSearchSettingsPanelLayout);
        advancedSearchSettingsPanelLayout.setHorizontalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(instrumentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(decoyDatabaseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(instrumentCmb, 0, 189, Short.MAX_VALUE)
                            .addComponent(decoyDatabaseCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxRankLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxRankTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(monoIsotopicLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(monoIsotopicCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(performDeisotopingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(performDeisotopingCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxModPerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxModPerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxVariableModPerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxVariableModPerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxPotentialModSitePerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPotentialModSitePerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxNeutralLossesPerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxNeutralLossesPerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxPtmNeutalLossesPerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPtmNeutalLossesPerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(minPeptideLengthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minPeptideLengthTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxProteinsLoadedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxProteinsLoadedTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxSpectraLoadedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outputFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(outputFormatCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(maxSpectraLoadedTxt))))
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
                    .addComponent(performDeisotopingLabel)
                    .addComponent(performDeisotopingCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxModPerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxModPerPeptideLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxVariableModPerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxVariableModPerPeptideLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxPotentialModSitePerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPotentialModSitePerPeptideLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxNeutralLossesPerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxNeutralLossesPerPeptideLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxPtmNeutalLossesPerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPtmNeutalLossesPerPeptideLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPeptideLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minPeptideLengthLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxProteinsLoadedTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxProteinsLoadedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxSpectraLoadedTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxSpectraLoadedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputFormatLabel)
                    .addComponent(outputFormatCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
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
                .addComponent(advancedSearchSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        BareBonesBrowserLaunch.openURL("https://ms.imp.ac.at/?goto=msamanda");
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
     * Validate the input.
     *
     * @param evt
     */
    private void minPeptideLengthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPeptideLengthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPeptideLengthTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxProteinsLoadedTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxProteinsLoadedTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxProteinsLoadedTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxSpectraLoadedTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxSpectraLoadedTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxSpectraLoadedTxtKeyReleased

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {
        boolean valid = true;
        valid = GuiUtilities.validateIntegerInput(this, maxRankLabel, maxRankTxt, "number of spectrum matches", "Number Spectrum Matches Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, minPeptideLengthLabel, minPeptideLengthTxt, "minimum peptide length", "Minimum Peptide Length Error", false, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxProteinsLoadedLabel, maxProteinsLoadedTxt, "maximum number of proteins loaded into memory", "Maximum Proteins Loaded into Memory Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxSpectraLoadedLabel, maxSpectraLoadedTxt, "maximum number of spectra loaded into memory", "Maximum Spectra Loaded into Memory Error", true, showMessage, valid);

        // check if the max proteins in memory value is in the range (1000 - 500 000)
        if (valid) {
            try {
                Integer value = new Integer(maxProteinsLoadedTxt.getText());
                if (value < 1000 || value > 500000) {
                    if (showMessage && valid) {
                        JOptionPane.showMessageDialog(this, "Please select an integer in the range (1000 - 500 000) for Max Proteins Loaded into Memory.",
                                "Max Proteins Loaded into Memory Error", JOptionPane.WARNING_MESSAGE);
                    }
                    valid = false;
                    maxProteinsLoadedLabel.setForeground(Color.RED);
                    maxProteinsLoadedLabel.setToolTipText("Please select an integer in the range (1000 - 500 000)");
                }
            } catch (NumberFormatException e) {
                // ignore, already caught above
            }
        }

        // check if the max spectra in memory value is in the range (1000 - 500 000)
        if (valid) {
            try {
                Integer value = new Integer(maxSpectraLoadedTxt.getText());
                if (value < 1000 || value > 500000) {
                    if (showMessage && valid) {
                        JOptionPane.showMessageDialog(this, "Please select an integer in the range (1000 - 500 000) for Max Spectra Loaded into Memory.",
                                "Max Spectra Loaded into Memory Error", JOptionPane.WARNING_MESSAGE);
                    }
                    valid = false;
                    maxProteinsLoadedLabel.setForeground(Color.RED);
                    maxProteinsLoadedLabel.setToolTipText("Please select an integer in the range (1000 - 500 000)");
                }
            } catch (NumberFormatException e) {
                // ignore, already caught above
            }
        }

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
    private javax.swing.JComboBox maxModPerPeptideCmb;
    private javax.swing.JLabel maxModPerPeptideLabel;
    private javax.swing.JComboBox maxNeutralLossesPerPeptideCmb;
    private javax.swing.JLabel maxNeutralLossesPerPeptideLabel;
    private javax.swing.JComboBox maxPotentialModSitePerPeptideCmb;
    private javax.swing.JLabel maxPotentialModSitePerPeptideLabel;
    private javax.swing.JLabel maxProteinsLoadedLabel;
    private javax.swing.JTextField maxProteinsLoadedTxt;
    private javax.swing.JComboBox maxPtmNeutalLossesPerPeptideCmb;
    private javax.swing.JLabel maxPtmNeutalLossesPerPeptideLabel;
    private javax.swing.JLabel maxRankLabel;
    private javax.swing.JTextField maxRankTxt;
    private javax.swing.JLabel maxSpectraLoadedLabel;
    private javax.swing.JTextField maxSpectraLoadedTxt;
    private javax.swing.JComboBox maxVariableModPerPeptideCmb;
    private javax.swing.JLabel maxVariableModPerPeptideLabel;
    private javax.swing.JLabel minPeptideLengthLabel;
    private javax.swing.JTextField minPeptideLengthTxt;
    private javax.swing.JComboBox monoIsotopicCmb;
    private javax.swing.JLabel monoIsotopicLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JComboBox outputFormatCmb;
    private javax.swing.JLabel outputFormatLabel;
    private javax.swing.JComboBox performDeisotopingCmb;
    private javax.swing.JLabel performDeisotopingLabel;
    // End of variables declaration//GEN-END:variables
}
