package com.compomics.util.gui.parameters.identification.algorithm;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;
import com.compomics.util.parameters.identification.tool_specific.MsAmandaParameters;
import com.compomics.util.gui.GuiUtilities;
import java.awt.Dialog;
import javax.swing.SwingConstants;
import com.compomics.util.gui.parameters.identification.AlgorithmParametersDialog;
import java.awt.Color;
import javax.swing.JOptionPane;

/**
 * Dialog for the MS Amanda specific settings.
 *
 * @author Harald Barsnes
 */
public class MsAmandaParametersDialog extends javax.swing.JDialog implements AlgorithmParametersDialog {

    /**
     * Empty default constructor
     */
    public MsAmandaParametersDialog() {
    }

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
        rankTargetAndDecoySeparatelyCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        instrumentCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        monoIsotopicCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        performDeisotopingCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxModPerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxVariableModPerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxPotentialModSitePerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxNeutralLossesPerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxPtmNeutalLossesPerPeptideCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        outputFormatCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxAllowedChargeStatesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        performSecondSearchCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        keepY1Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        removeWaterLossesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        removeAmmoniaLossesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        excludeFirstPrecursorCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        consideredChargesForPrecursorsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        combineConsideredChargesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        runPercolatorCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        generatePInFileCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        minPeakDepthComboBox.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxPeakDepthComboBox.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        maxMultiplePrecursorsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        decoyDatabaseCmb.setEnabled(editable);
        rankTargetAndDecoySeparatelyCmb.setEnabled(editable);
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
        maxAllowedChargeStatesCmb.setEnabled(editable);
        performSecondSearchCmb.setEnabled(editable);
        keepY1Cmb.setEnabled(editable);
        removeWaterLossesCmb.setEnabled(editable);
        removeAmmoniaLossesCmb.setEnabled(editable);
        excludeFirstPrecursorCmb.setEnabled(editable);
        consideredChargesForPrecursorsCmb.setEnabled(editable);
        combineConsideredChargesCmb.setEnabled(editable);
        runPercolatorCmb.setEnabled(editable);
        generatePInFileCmb.setEnabled(editable);
        minPeakDepthComboBox.setEnabled(editable);
        maxPeakDepthComboBox.setEnabled(editable);

        minPeptideLengthTxt.setEnabled(editable);
        maxPeptideLengthTxt.setEnabled(editable);
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

        if (msAmandaParameters.reportBothBestHitsForTD()) {
            rankTargetAndDecoySeparatelyCmb.setSelectedIndex(0);
        } else {
            rankTargetAndDecoySeparatelyCmb.setSelectedIndex(1);
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

        if (msAmandaParameters.getPerformSecondSearch()) {
            performSecondSearchCmb.setSelectedIndex(0);
        } else {
            performSecondSearchCmb.setSelectedIndex(1);
        }

        if (msAmandaParameters.getKeepY1Ion()) {
            keepY1Cmb.setSelectedIndex(0);
        } else {
            keepY1Cmb.setSelectedIndex(1);
        }

        if (msAmandaParameters.getRemoveWaterLosses()) {
            removeWaterLossesCmb.setSelectedIndex(0);
        } else {
            removeWaterLossesCmb.setSelectedIndex(1);
        }

        if (msAmandaParameters.getRemoveAmmoniaLosses()) {
            removeAmmoniaLossesCmb.setSelectedIndex(0);
        } else {
            removeAmmoniaLossesCmb.setSelectedIndex(1);
        }

        if (msAmandaParameters.getExcludeFirstPrecursor()) {
            excludeFirstPrecursorCmb.setSelectedIndex(0);
        } else {
            excludeFirstPrecursorCmb.setSelectedIndex(1);
        }

        if (msAmandaParameters.getCombineConsideredCharges()) {
            combineConsideredChargesCmb.setSelectedIndex(0);
        } else {
            combineConsideredChargesCmb.setSelectedIndex(1);
        }

        if (msAmandaParameters.getRunPercolator()) {
            runPercolatorCmb.setSelectedIndex(0);
        } else {
            runPercolatorCmb.setSelectedIndex(1);
        }

        if (msAmandaParameters.getGeneratePInFile()) {
            generatePInFileCmb.setSelectedIndex(0);
        } else {
            generatePInFileCmb.setSelectedIndex(1);
        }

        maxModPerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxModifications());
        maxVariableModPerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxVariableModifications());
        maxPotentialModSitePerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxModificationSites());
        maxNeutralLossesPerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxNeutralLosses());
        maxPtmNeutalLossesPerPeptideCmb.setSelectedIndex(msAmandaParameters.getMaxNeutralLossesPerModification());
        maxAllowedChargeStatesCmb.setSelectedItem(msAmandaParameters.getMaxAllowedChargeState());
        minPeakDepthComboBox.setSelectedItem(msAmandaParameters.getMinPeakDepth());
        maxPeakDepthComboBox.setSelectedItem(msAmandaParameters.getMaxPeakDepth());
        maxMultiplePrecursorsCmb.setSelectedItem(msAmandaParameters.getMaxMultiplePrecursors());
        consideredChargesForPrecursorsCmb.setSelectedItem(msAmandaParameters.getConsideredChargesForPrecursors());

        minPeptideLengthTxt.setText(msAmandaParameters.getMinPeptideLength().toString());
        maxPeptideLengthTxt.setText(msAmandaParameters.getMaxPeptideLength().toString());
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
        result.setReportBothBestHitsForTD(rankTargetAndDecoySeparatelyCmb.getSelectedIndex() == 0);
        result.setInstrumentID((String) instrumentCmb.getSelectedItem());

        String input = maxRankTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxRank(Integer.valueOf(input));
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
            result.setMinPeptideLength(Integer.valueOf(input));
        }
        input = maxPeptideLengthTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxPeptideLength(Integer.valueOf(input));
        }

        input = maxProteinsLoadedTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxLoadedProteins(Integer.valueOf(input));
        }

        input = maxSpectraLoadedTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxLoadedSpectra(Integer.valueOf(input));
        }

        result.setOutputFormat((String) outputFormatCmb.getSelectedItem());

        result.setMaxAllowedChargeState((String) maxAllowedChargeStatesCmb.getSelectedItem());
        result.setMinPeakDepth(Integer.valueOf((String) minPeakDepthComboBox.getSelectedItem()));
        result.setMaxPeakDepth(Integer.valueOf((String) maxPeakDepthComboBox.getSelectedItem()));

        result.setPerformSecondSearch(performSecondSearchCmb.getSelectedIndex() == 0);
        result.setKeepY1Ion(keepY1Cmb.getSelectedIndex() == 0);
        result.setRemoveWaterLosses(removeWaterLossesCmb.getSelectedIndex() == 0);
        result.setRemoveAmmoniaLosses(removeAmmoniaLossesCmb.getSelectedIndex() == 0);
        result.setExcludeFirstPrecursor(excludeFirstPrecursorCmb.getSelectedIndex() == 0);
        result.setMaxMultiplePrecursors(Integer.valueOf((String) maxMultiplePrecursorsCmb.getSelectedItem()));
        result.setConsideredChargesForPrecursors((String) consideredChargesForPrecursorsCmb.getSelectedItem());
        result.setCombineConsideredCharges(combineConsideredChargesCmb.getSelectedIndex() == 0);
        result.setRunPercolator(runPercolatorCmb.getSelectedIndex() == 0);
        result.setGeneratePInFile(generatePInFileCmb.getSelectedIndex() == 0);

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
        tabbedPane = new javax.swing.JTabbedPane();
        searchPanel = new javax.swing.JPanel();
        instrumentLabel = new javax.swing.JLabel();
        instrumentCmb = new javax.swing.JComboBox();
        maxRankLabel = new javax.swing.JLabel();
        maxRankTxt = new javax.swing.JTextField();
        decoyDatabaseLabel = new javax.swing.JLabel();
        decoyDatabaseCmb = new javax.swing.JComboBox();
        performDeisotopingCmb = new javax.swing.JComboBox();
        performDeisotopingLabel = new javax.swing.JLabel();
        maxModPerPeptideLabel = new javax.swing.JLabel();
        maxVariableModPerPeptideLabel = new javax.swing.JLabel();
        maxPotentialModSitePerPeptideLabel = new javax.swing.JLabel();
        maxPotentialModSitePerPeptideCmb = new javax.swing.JComboBox();
        maxVariableModPerPeptideCmb = new javax.swing.JComboBox();
        maxModPerPeptideCmb = new javax.swing.JComboBox();
        maxNeutralLossesPerPeptideLabel = new javax.swing.JLabel();
        maxNeutralLossesPerPeptideCmb = new javax.swing.JComboBox();
        maxPtmNeutalLossesPerPeptideLabel = new javax.swing.JLabel();
        maxPtmNeutalLossesPerPeptideCmb = new javax.swing.JComboBox();
        rankTargetAndDecoySeparatelyLabel = new javax.swing.JLabel();
        rankTargetAndDecoySeparatelyCmb = new javax.swing.JComboBox();
        maxAllowedChargeStatesLabel = new javax.swing.JLabel();
        maxAllowedChargeStatesCmb = new javax.swing.JComboBox();
        peakDepthLabel = new javax.swing.JLabel();
        peakDepthDividerLabel = new javax.swing.JLabel();
        peptideLengthLabel = new javax.swing.JLabel();
        minPeptideLengthTxt = new javax.swing.JTextField();
        peptideLengthDividerLabel = new javax.swing.JLabel();
        maxPeptideLengthTxt = new javax.swing.JTextField();
        maxPeakDepthComboBox = new javax.swing.JComboBox<>();
        minPeakDepthComboBox = new javax.swing.JComboBox<>();
        secondSearchPanel = new javax.swing.JPanel();
        performSecondSearchLabel = new javax.swing.JLabel();
        performSecondSearchCmb = new javax.swing.JComboBox();
        keepY1Label = new javax.swing.JLabel();
        keepY1Cmb = new javax.swing.JComboBox();
        removeWaterLossesLabel = new javax.swing.JLabel();
        removeWaterLossesCmb = new javax.swing.JComboBox();
        removeAmmoniaLossesLabel = new javax.swing.JLabel();
        removeAmmoniaLossesCmb = new javax.swing.JComboBox();
        excludeFirstPrecursorLabel = new javax.swing.JLabel();
        excludeFirstPrecursorCmb = new javax.swing.JComboBox();
        maxMultiplePrecursorsLabel = new javax.swing.JLabel();
        maxMultiplePrecursorsCmb = new javax.swing.JComboBox();
        consideredChargesForPrecursorsLabel = new javax.swing.JLabel();
        consideredChargesForPrecursorsCmb = new javax.swing.JComboBox();
        basicPanel = new javax.swing.JPanel();
        monoIsotopicLabel = new javax.swing.JLabel();
        monoIsotopicCmb = new javax.swing.JComboBox();
        combineConsideredChargesLabel = new javax.swing.JLabel();
        combineConsideredChargesCmb = new javax.swing.JComboBox();
        maxProteinsLoadedLabel = new javax.swing.JLabel();
        maxProteinsLoadedTxt = new javax.swing.JTextField();
        maxSpectraLoadedLabel = new javax.swing.JLabel();
        maxSpectraLoadedTxt = new javax.swing.JTextField();
        outputFormatLabel = new javax.swing.JLabel();
        outputFormatCmb = new javax.swing.JComboBox();
        perolatorPanel = new javax.swing.JPanel();
        generatePInFileLabel = new javax.swing.JLabel();
        generatePInFileCmb = new javax.swing.JComboBox();
        runPercolatorLabel = new javax.swing.JLabel();
        runPercolatorCmb = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        openDialogHelpJButton = new javax.swing.JButton();
        msAmandaHelpPageLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("MS Amanda Advanced Settings");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        advancedSearchSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Settings"));
        advancedSearchSettingsPanel.setOpaque(false);

        searchPanel.setOpaque(false);

        instrumentLabel.setText("Fragment Ion Types");

        instrumentCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "b, y", "b, y, -H2O, -NH3", "a, b, y, -H2O, -NH3, Imm", "a, b, y, -H2O, -NH3", "a, b, y", "a, b, y, Imm", "a, b, y, z, -H2O, -NH3, Imm", "c, y, z+1, z+2", "c, z, z+1", "c, z, z+1, z+2", "b, c, y, z+1, z+2", "b, y, INT", "b, y, INT, Imm", "a, b, y, INT", "a, b, y, INT, IMM", "a, b, y, INT, IMM, -H2O", "a, b, y, INT, IMM, -H2O, -NH3", "a, b, y, INT, IMM, -NH3", "a, b, c, x, y, z", "a+1, x, y-1, z", "a+1, b, x, y-1, z", "a+1, b, x, y, y-1, z", "a, a+1, b, c, x, y, y-1, z", "a+1, x, y, y-1, z", "a, a+1, a-1, b, b+1, b-1, c, c+1, c-1, x, x+1, x-1, y, y+1, y-1, z, z+1, z-1" }));

        maxRankLabel.setText("Max Rank");

        maxRankTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxRankTxt.setText("1");
        maxRankTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxRankTxtKeyReleased(evt);
            }
        });

        decoyDatabaseLabel.setText("Generate Decoy Database");

        decoyDatabaseCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        decoyDatabaseCmb.setSelectedIndex(1);

        performDeisotopingCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        performDeisotopingLabel.setText("Perform Deisotoping");

        maxModPerPeptideLabel.setText("Max PTM Duplicates per Peptide");
        maxModPerPeptideLabel.setToolTipText("Max number of occurrences of a specific modification on a peptide");

        maxVariableModPerPeptideLabel.setText("Max Variable PTMs per Peptide");
        maxVariableModPerPeptideLabel.setToolTipText("Max number of variable modifications per peptide");

        maxPotentialModSitePerPeptideLabel.setText("Max Potential PTM sites per PTM");
        maxPotentialModSitePerPeptideLabel.setToolTipText("Max number of potential modification sites per modification per peptide");

        maxPotentialModSitePerPeptideCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" }));
        maxPotentialModSitePerPeptideCmb.setSelectedIndex(6);
        maxPotentialModSitePerPeptideCmb.setToolTipText("Max number of potential modification sites per modification per peptide");

        maxVariableModPerPeptideCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        maxVariableModPerPeptideCmb.setSelectedIndex(4);
        maxVariableModPerPeptideCmb.setToolTipText("Max number of variable modifications per peptide");

        maxModPerPeptideCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        maxModPerPeptideCmb.setSelectedIndex(3);
        maxModPerPeptideCmb.setToolTipText("Max number of occurrences of a specific modification on a peptide");

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

        rankTargetAndDecoySeparatelyLabel.setText("Rank Target and Decoy Separately");

        rankTargetAndDecoySeparatelyCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        maxAllowedChargeStatesLabel.setText("Maximum Allowed Charge States");

        maxAllowedChargeStatesCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+2", "+3", "+4", "Precursor - 1" }));

        peakDepthLabel.setText("Peak Depth");

        peakDepthDividerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        peakDepthDividerLabel.setText("-");

        peptideLengthLabel.setText("Peptide Length (min - max)");

        minPeptideLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPeptideLengthTxt.setText("8");
        minPeptideLengthTxt.setToolTipText("Minimum peptide length");
        minPeptideLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPeptideLengthTxtKeyReleased(evt);
            }
        });

        peptideLengthDividerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        peptideLengthDividerLabel.setText("-");

        maxPeptideLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPeptideLengthTxt.setText("30");
        maxPeptideLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPeptideLengthTxtKeyReleased(evt);
            }
        });

        maxPeakDepthComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30" }));
        maxPeakDepthComboBox.setSelectedIndex(9);

        minPeakDepthComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30" }));

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(instrumentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(instrumentCmb, 0, 1, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(maxRankLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxRankTxt))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(decoyDatabaseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decoyDatabaseCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(performDeisotopingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(performDeisotopingCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(maxModPerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxModPerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(maxVariableModPerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxVariableModPerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(maxPotentialModSitePerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPotentialModSitePerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(maxNeutralLossesPerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxNeutralLossesPerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(maxPtmNeutalLossesPerPeptideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPtmNeutalLossesPerPeptideCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(rankTargetAndDecoySeparatelyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rankTargetAndDecoySeparatelyCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(maxAllowedChargeStatesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxAllowedChargeStatesCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(peakDepthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minPeakDepthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(peakDepthDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPeakDepthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(peptideLengthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minPeptideLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(peptideLengthDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPeptideLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        searchPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {maxPeakDepthComboBox, maxPeptideLengthTxt, minPeakDepthComboBox, minPeptideLengthTxt});

        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(instrumentCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(instrumentLabel))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxRankLabel)
                    .addComponent(maxRankTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(decoyDatabaseLabel)
                    .addComponent(decoyDatabaseCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(performDeisotopingLabel)
                    .addComponent(performDeisotopingCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxModPerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxModPerPeptideLabel))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxVariableModPerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxVariableModPerPeptideLabel))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxPotentialModSitePerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPotentialModSitePerPeptideLabel))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxNeutralLossesPerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxNeutralLossesPerPeptideLabel))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxPtmNeutalLossesPerPeptideCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPtmNeutalLossesPerPeptideLabel))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPeptideLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideLengthLabel)
                    .addComponent(maxPeptideLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideLengthDividerLabel))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rankTargetAndDecoySeparatelyLabel)
                    .addComponent(rankTargetAndDecoySeparatelyCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxAllowedChargeStatesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxAllowedChargeStatesLabel))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(peakDepthLabel)
                    .addComponent(minPeakDepthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peakDepthDividerLabel)
                    .addComponent(maxPeakDepthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Search", searchPanel);

        secondSearchPanel.setOpaque(false);

        performSecondSearchLabel.setText("Perform Second Search");

        performSecondSearchCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        performSecondSearchCmb.setSelectedIndex(1);
        performSecondSearchCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performSecondSearchCmbActionPerformed(evt);
            }
        });

        keepY1Label.setText("Keep Y1 Ion");

        keepY1Cmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        removeWaterLossesLabel.setText("Remove Water Losses");

        removeWaterLossesCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        removeAmmoniaLossesLabel.setText("Remove Ammonia Losses");

        removeAmmoniaLossesCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        excludeFirstPrecursorLabel.setText("Exclude First Precursor");

        excludeFirstPrecursorCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        maxMultiplePrecursorsLabel.setText("Max Multiple Precursors");

        maxMultiplePrecursorsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "3", "7", "8", "9", "10" }));
        maxMultiplePrecursorsCmb.setSelectedIndex(4);

        consideredChargesForPrecursorsLabel.setText("Considered Charges For Precursors");

        consideredChargesForPrecursorsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+2", "+3", "+2, +3", "+2, +3, +4", "+3, +4", "+2, +3, +4, +5" }));
        consideredChargesForPrecursorsCmb.setSelectedIndex(2);

        javax.swing.GroupLayout secondSearchPanelLayout = new javax.swing.GroupLayout(secondSearchPanel);
        secondSearchPanel.setLayout(secondSearchPanelLayout);
        secondSearchPanelLayout.setHorizontalGroup(
            secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(secondSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(secondSearchPanelLayout.createSequentialGroup()
                        .addComponent(performSecondSearchLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(performSecondSearchCmb, 0, 171, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, secondSearchPanelLayout.createSequentialGroup()
                        .addComponent(keepY1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keepY1Cmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(secondSearchPanelLayout.createSequentialGroup()
                        .addComponent(removeWaterLossesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeWaterLossesCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, secondSearchPanelLayout.createSequentialGroup()
                        .addComponent(removeAmmoniaLossesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeAmmoniaLossesCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, secondSearchPanelLayout.createSequentialGroup()
                        .addComponent(excludeFirstPrecursorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(excludeFirstPrecursorCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, secondSearchPanelLayout.createSequentialGroup()
                        .addComponent(maxMultiplePrecursorsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxMultiplePrecursorsCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, secondSearchPanelLayout.createSequentialGroup()
                        .addComponent(consideredChargesForPrecursorsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(consideredChargesForPrecursorsCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        secondSearchPanelLayout.setVerticalGroup(
            secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(secondSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(performSecondSearchLabel)
                    .addComponent(performSecondSearchCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keepY1Label)
                    .addComponent(keepY1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removeWaterLossesLabel)
                    .addComponent(removeWaterLossesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removeAmmoniaLossesLabel)
                    .addComponent(removeAmmoniaLossesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(excludeFirstPrecursorLabel)
                    .addComponent(excludeFirstPrecursorCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxMultiplePrecursorsLabel)
                    .addComponent(maxMultiplePrecursorsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(secondSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(consideredChargesForPrecursorsLabel)
                    .addComponent(consideredChargesForPrecursorsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(165, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Second Search", secondSearchPanel);

        basicPanel.setOpaque(false);

        monoIsotopicLabel.setText("Monoisotopic");

        monoIsotopicCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        combineConsideredChargesLabel.setText("Combine Considered Charges");

        combineConsideredChargesCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

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

        javax.swing.GroupLayout basicPanelLayout = new javax.swing.GroupLayout(basicPanel);
        basicPanel.setLayout(basicPanelLayout);
        basicPanelLayout.setHorizontalGroup(
            basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addComponent(monoIsotopicLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(monoIsotopicCmb, 0, 171, Short.MAX_VALUE))
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addComponent(combineConsideredChargesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(combineConsideredChargesCmb, 0, 171, Short.MAX_VALUE))
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addComponent(maxProteinsLoadedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxProteinsLoadedTxt))
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addComponent(maxSpectraLoadedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxSpectraLoadedTxt))
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addComponent(outputFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(outputFormatCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        basicPanelLayout.setVerticalGroup(
            basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(monoIsotopicLabel)
                    .addComponent(monoIsotopicCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(combineConsideredChargesLabel)
                    .addComponent(combineConsideredChargesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxProteinsLoadedTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxProteinsLoadedLabel))
                .addGap(0, 0, 0)
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxSpectraLoadedTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxSpectraLoadedLabel))
                .addGap(0, 0, 0)
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputFormatLabel)
                    .addComponent(outputFormatCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(214, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Basic", basicPanel);

        perolatorPanel.setOpaque(false);

        generatePInFileLabel.setText("Generate PIn File");

        generatePInFileCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        generatePInFileCmb.setSelectedIndex(1);

        runPercolatorLabel.setText("Run Percolator");

        runPercolatorCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        runPercolatorCmb.setSelectedIndex(1);
        runPercolatorCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runPercolatorCmbActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout perolatorPanelLayout = new javax.swing.GroupLayout(perolatorPanel);
        perolatorPanel.setLayout(perolatorPanelLayout);
        perolatorPanelLayout.setHorizontalGroup(
            perolatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(perolatorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(perolatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(perolatorPanelLayout.createSequentialGroup()
                        .addComponent(generatePInFileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generatePInFileCmb, 0, 171, Short.MAX_VALUE))
                    .addGroup(perolatorPanelLayout.createSequentialGroup()
                        .addComponent(runPercolatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runPercolatorCmb, 0, 171, Short.MAX_VALUE)))
                .addContainerGap())
        );
        perolatorPanelLayout.setVerticalGroup(
            perolatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(perolatorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(perolatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runPercolatorLabel)
                    .addComponent(runPercolatorCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(perolatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generatePInFileLabel)
                    .addComponent(generatePInFileCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(296, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Percolator", perolatorPanel);

        javax.swing.GroupLayout advancedSearchSettingsPanelLayout = new javax.swing.GroupLayout(advancedSearchSettingsPanel);
        advancedSearchSettingsPanel.setLayout(advancedSearchSettingsPanelLayout);
        advancedSearchSettingsPanelLayout.setHorizontalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );
        advancedSearchSettingsPanelLayout.setVerticalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
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

        msAmandaHelpPageLabel.setText("Click to open the MS Amanda help page.");

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
                        .addComponent(msAmandaHelpPageLabel)
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
                    .addComponent(msAmandaHelpPageLabel)
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
    private void maxSpectraLoadedTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxSpectraLoadedTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxSpectraLoadedTxtKeyReleased

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
    private void maxPeptideLengthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPeptideLengthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPeptideLengthTxtKeyReleased

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
    private void maxRankTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxRankTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxRankTxtKeyReleased

    /**
     * Enable or disable the other second search settings.
     *
     * @param evt
     */
    private void performSecondSearchCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performSecondSearchCmbActionPerformed

        keepY1Cmb.setEnabled(performSecondSearchCmb.getSelectedIndex() == 0);
        removeWaterLossesCmb.setEnabled(performSecondSearchCmb.getSelectedIndex() == 0);
        removeAmmoniaLossesCmb.setEnabled(performSecondSearchCmb.getSelectedIndex() == 0);
        excludeFirstPrecursorCmb.setEnabled(performSecondSearchCmb.getSelectedIndex() == 0);
        maxMultiplePrecursorsCmb.setEnabled(performSecondSearchCmb.getSelectedIndex() == 0);
        consideredChargesForPrecursorsCmb.setEnabled(performSecondSearchCmb.getSelectedIndex() == 0);

    }//GEN-LAST:event_performSecondSearchCmbActionPerformed

    /**
     * Enable or disable the other Percolator settings.
     *
     * @param evt
     */
    private void runPercolatorCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runPercolatorCmbActionPerformed

        generatePInFileCmb.setEnabled(runPercolatorCmb.getSelectedIndex() == 0);

    }//GEN-LAST:event_runPercolatorCmbActionPerformed

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;

        valid = GuiUtilities.validateIntegerInput(
                this,
                maxRankLabel,
                maxRankTxt,
                "number of spectrum matches",
                "Number Spectrum Matches Error",
                true,
                showMessage,
                valid
        );

        valid = GuiUtilities.validateIntegerInput(
                this,
                peptideLengthLabel,
                minPeptideLengthTxt,
                "minimum peptide length",
                "Minimum Peptide Length Error",
                false,
                showMessage,
                valid
        );

        valid = GuiUtilities.validateIntegerInput(
                this,
                peptideLengthLabel,
                maxPeptideLengthTxt,
                "maximum peptide length",
                "Maximum Peptide Length Error",
                false,
                showMessage,
                valid
        );

        valid = GuiUtilities.validateIntegerInput(
                this,
                maxProteinsLoadedLabel,
                maxProteinsLoadedTxt,
                "maximum number of proteins loaded into memory",
                "Maximum Proteins Loaded into Memory Error",
                true,
                showMessage,
                valid
        );

        valid = GuiUtilities.validateIntegerInput(
                this,
                maxSpectraLoadedLabel,
                maxSpectraLoadedTxt,
                "maximum number of spectra loaded into memory",
                "Maximum Spectra Loaded into Memory Error",
                true,
                showMessage,
                valid
        );

        // check if the max proteins in memory value is in the range (1000 - 500 000)
        if (valid) {

            try {

                Integer value = Integer.valueOf(maxProteinsLoadedTxt.getText());

                if (value < 1000 || value > 500000) {

                    if (showMessage && valid) {

                        JOptionPane.showMessageDialog(
                                this,
                                "Please select an integer in the range (1000 - 500 000) for Max Proteins Loaded into Memory.",
                                "Max Proteins Loaded into Memory Error",
                                JOptionPane.WARNING_MESSAGE
                        );

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

                Integer value = Integer.valueOf(maxSpectraLoadedTxt.getText());

                if (value < 1000 || value > 500000) {

                    if (showMessage && valid) {

                        JOptionPane.showMessageDialog(
                                this,
                                "Please select an integer in the range (1000 - 500 000) for Max Spectra Loaded into Memory.",
                                "Max Spectra Loaded into Memory Error",
                                JOptionPane.WARNING_MESSAGE
                        );

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
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JPanel basicPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox combineConsideredChargesCmb;
    private javax.swing.JLabel combineConsideredChargesLabel;
    private javax.swing.JComboBox consideredChargesForPrecursorsCmb;
    private javax.swing.JLabel consideredChargesForPrecursorsLabel;
    private javax.swing.JComboBox decoyDatabaseCmb;
    private javax.swing.JLabel decoyDatabaseLabel;
    private javax.swing.JComboBox excludeFirstPrecursorCmb;
    private javax.swing.JLabel excludeFirstPrecursorLabel;
    private javax.swing.JComboBox generatePInFileCmb;
    private javax.swing.JLabel generatePInFileLabel;
    private javax.swing.JComboBox instrumentCmb;
    private javax.swing.JLabel instrumentLabel;
    private javax.swing.JComboBox keepY1Cmb;
    private javax.swing.JLabel keepY1Label;
    private javax.swing.JComboBox maxAllowedChargeStatesCmb;
    private javax.swing.JLabel maxAllowedChargeStatesLabel;
    private javax.swing.JComboBox maxModPerPeptideCmb;
    private javax.swing.JLabel maxModPerPeptideLabel;
    private javax.swing.JComboBox maxMultiplePrecursorsCmb;
    private javax.swing.JLabel maxMultiplePrecursorsLabel;
    private javax.swing.JComboBox maxNeutralLossesPerPeptideCmb;
    private javax.swing.JLabel maxNeutralLossesPerPeptideLabel;
    private javax.swing.JComboBox<String> maxPeakDepthComboBox;
    private javax.swing.JTextField maxPeptideLengthTxt;
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
    private javax.swing.JComboBox<String> minPeakDepthComboBox;
    private javax.swing.JTextField minPeptideLengthTxt;
    private javax.swing.JComboBox monoIsotopicCmb;
    private javax.swing.JLabel monoIsotopicLabel;
    private javax.swing.JLabel msAmandaHelpPageLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JComboBox outputFormatCmb;
    private javax.swing.JLabel outputFormatLabel;
    private javax.swing.JLabel peakDepthDividerLabel;
    private javax.swing.JLabel peakDepthLabel;
    private javax.swing.JLabel peptideLengthDividerLabel;
    private javax.swing.JLabel peptideLengthLabel;
    private javax.swing.JComboBox performDeisotopingCmb;
    private javax.swing.JLabel performDeisotopingLabel;
    private javax.swing.JComboBox performSecondSearchCmb;
    private javax.swing.JLabel performSecondSearchLabel;
    private javax.swing.JPanel perolatorPanel;
    private javax.swing.JComboBox rankTargetAndDecoySeparatelyCmb;
    private javax.swing.JLabel rankTargetAndDecoySeparatelyLabel;
    private javax.swing.JComboBox removeAmmoniaLossesCmb;
    private javax.swing.JLabel removeAmmoniaLossesLabel;
    private javax.swing.JComboBox removeWaterLossesCmb;
    private javax.swing.JLabel removeWaterLossesLabel;
    private javax.swing.JComboBox runPercolatorCmb;
    private javax.swing.JLabel runPercolatorLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel secondSearchPanel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
