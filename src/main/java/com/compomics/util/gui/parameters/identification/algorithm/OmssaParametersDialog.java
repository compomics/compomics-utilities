package com.compomics.util.gui.parameters.identification.algorithm;

import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.OmssaParameters;
import com.compomics.util.experiment.mass_spectrometry.Charge;
import com.compomics.util.gui.GuiUtilities;
import com.compomics.util.gui.JOptionEditorPane;
import com.compomics.util.gui.error_handlers.HelpDialog;
import java.awt.Dialog;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import com.compomics.util.gui.parameters.identification.AlgorithmParametersDialog;

/**
 * Dialog for the OMSSA specific parameters.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class OmssaParametersDialog extends javax.swing.JDialog implements AlgorithmParametersDialog {

    /**
     * Boolean indicating whether the used canceled the editing.
     */
    private boolean cancelled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates new form OmssaParametersDialog with a frame as owner.
     *
     * @param parent the parent frame
     * @param omssaParameters the OMSSA parameters
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public OmssaParametersDialog(java.awt.Frame parent, OmssaParameters omssaParameters, boolean editable) {
        super(parent, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(omssaParameters);
        validateInput(false);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates new form OmssaParametersDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parent the parent frame
     * @param omssaParameters the OMSSA parameters
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public OmssaParametersDialog(Dialog owner, java.awt.Frame parent, OmssaParameters omssaParameters, boolean editable) {
        super(owner, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(omssaParameters);
        validateInput(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGui() {
        
        eliminatePrecursorCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        chargeEstimationCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        plusOneChargeCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        precursorScalingCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        sequenceMappingCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        cleaveNterminalMethionineCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        searchPositiveIonsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        forwardIonsFirstCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        cTermIonsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        correlationCorrectionScoreCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        omssaOutputFormatComboBox.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        
        lowIntensityTxt.setEditable(editable);
        lowIntensityTxt.setEnabled(editable);
        highIntensityTxt.setEditable(editable);
        highIntensityTxt.setEnabled(editable);
        intensityIncrementTxt.setEditable(editable);
        intensityIncrementTxt.setEnabled(editable);
        nPeaksTxt.setEditable(editable);
        nPeaksTxt.setEnabled(editable);
        eliminatePrecursorCombo.setEnabled(editable);
        chargeEstimationCombo.setEnabled(editable);
        plusOneChargeCmb.setEnabled(editable);
        fractionChargeTxt.setEditable(editable);
        fractionChargeTxt.setEnabled(editable);
        minPrecPerSpectrumTxt.setEditable(editable);
        minPrecPerSpectrumTxt.setEnabled(editable);
        precursorScalingCombo.setEnabled(editable);
        sequenceMappingCmb.setEnabled(editable);
        cleaveNterminalMethionineCmb.setEnabled(editable);
        minPrecChargeMultipleChargedFragmentsTxt.setEditable(editable);
        minPrecChargeMultipleChargedFragmentsTxt.setEnabled(editable);
        neutronTxt.setEditable(editable);
        neutronTxt.setEnabled(editable);
        singlyChargedWindowWidthTxt.setEditable(editable);
        singlyChargedWindowWidthTxt.setEnabled(editable);
        doublyChargedWindowWidthTxt.setEditable(editable);
        doublyChargedWindowWidthTxt.setEnabled(editable);
        singlyChargedNpeaksTxt.setEditable(editable);
        singlyChargedNpeaksTxt.setEnabled(editable);
        doublyChargedNpeaksTxt.setEditable(editable);
        doublyChargedNpeaksTxt.setEnabled(editable);
        minAnnotatedPeaksTxt.setEditable(editable);
        minAnnotatedPeaksTxt.setEnabled(editable);
        maxLaddersTxt.setEditable(editable);
        maxLaddersTxt.setEnabled(editable);
        maxFragmentChargeTxt.setEditable(editable);
        maxFragmentChargeTxt.setEnabled(editable);
        searchPositiveIonsCmb.setEnabled(editable);
        cTermIonsCmb.setEnabled(editable);
        maxFragmentsPerSeriesTxt.setEditable(editable);
        maxFragmentsPerSeriesTxt.setEnabled(editable);
        correlationCorrectionScoreCmb.setEnabled(editable);
        consecutiveIonProbabilityTxt.setEditable(editable);
        consecutiveIonProbabilityTxt.setEnabled(editable);
        nHitsPerSpectrumPerChargeTxt.setEditable(editable);
        nHitsPerSpectrumPerChargeTxt.setEnabled(editable);
        iterativeSequenceEvalueTxt.setEditable(editable);
        iterativeSequenceEvalueTxt.setEnabled(editable);
        iterativeSpectraEvalueTxt.setEditable(editable);
        iterativeSpectraEvalueTxt.setEnabled(editable);
        iterativeReplaceEvalueTxt.setEditable(editable);
        iterativeReplaceEvalueTxt.setEnabled(editable);
        minPepLengthTxt.setEditable(editable);
        minPepLengthTxt.setEnabled(editable);
        maxPepLengthTxt.setEditable(editable);
        maxPepLengthTxt.setEnabled(editable);
        maxEvalueTxt.setEditable(editable);
        maxEvalueTxt.setEnabled(editable);
        hitlistTxt.setEditable(editable);
        hitlistTxt.setEnabled(editable);
        omssaOutputFormatComboBox.setEnabled(editable);
        
    }

    /**
     * Populates the GUI using the given settings.
     * 
     * @param omssaParameters the parameters to display
     */
    private void populateGUI(OmssaParameters omssaParameters) {

        if (omssaParameters.getLowIntensityCutOff() != null) {
            lowIntensityTxt.setText(omssaParameters.getLowIntensityCutOff() + "");
        }

        if (omssaParameters.getHighIntensityCutOff() != null) {
            highIntensityTxt.setText(omssaParameters.getHighIntensityCutOff() + "");
        }

        if (omssaParameters.getIntensityCutOffIncrement() != null) {
            intensityIncrementTxt.setText(omssaParameters.getIntensityCutOffIncrement() + "");
        }

        if (omssaParameters.getMinPeaks() != null) {
            nPeaksTxt.setText(omssaParameters.getMinPeaks() + "");
        }

        if (omssaParameters.isRemovePrecursor() != null) {
            if (omssaParameters.isRemovePrecursor()) {
                eliminatePrecursorCombo.setSelectedIndex(0);
            } else {
                eliminatePrecursorCombo.setSelectedIndex(1);
            }
        }

        if (omssaParameters.isEstimateCharge() != null) {
            if (omssaParameters.isEstimateCharge()) {
                chargeEstimationCombo.setSelectedIndex(0);
            } else {
                chargeEstimationCombo.setSelectedIndex(1);
            }
        }

        if (omssaParameters.isDetermineChargePlusOneAlgorithmically() != null) {
            if (omssaParameters.isDetermineChargePlusOneAlgorithmically()) {
                plusOneChargeCmb.setSelectedIndex(0);
            } else {
                plusOneChargeCmb.setSelectedIndex(1);
            }
        }

        if (omssaParameters.getFractionOfPeaksForChargeEstimation() != null) {
            fractionChargeTxt.setText(omssaParameters.getFractionOfPeaksForChargeEstimation() + "");
        }

        if (omssaParameters.getMinPrecPerSpectrum() != null) {
            minPrecPerSpectrumTxt.setText(omssaParameters.getMinPrecPerSpectrum() + "");
        }

        if (omssaParameters.isScalePrecursor() != null) {
            if (omssaParameters.isScalePrecursor()) {
                precursorScalingCombo.setSelectedIndex(0);
            } else {
                precursorScalingCombo.setSelectedIndex(1);
            }
        } else {
            precursorScalingCombo.setSelectedIndex(1);
        }

        if (omssaParameters.isMemoryMappedSequenceLibraries() != null) {
            if (omssaParameters.isMemoryMappedSequenceLibraries()) {
                sequenceMappingCmb.setSelectedIndex(0);
            } else {
                sequenceMappingCmb.setSelectedIndex(1);
            }
        } else {
            sequenceMappingCmb.setSelectedIndex(1);
        }

        if (omssaParameters.isCleaveNterMethionine() != null) {
            if (omssaParameters.isCleaveNterMethionine()) {
                cleaveNterminalMethionineCmb.setSelectedIndex(0);
            } else {
                cleaveNterminalMethionineCmb.setSelectedIndex(1);
            }
        } else {
            cleaveNterminalMethionineCmb.setSelectedIndex(1);
        }

        if (omssaParameters.getMinPeptideLength() != null) {
            minPepLengthTxt.setText(omssaParameters.getMinPeptideLength() + "");
        }

        if (omssaParameters.getMaxPeptideLength() != null) {
            maxPepLengthTxt.setText(omssaParameters.getMaxPeptideLength() + "");
        }

        if (omssaParameters.getMaxEValue() != null) {
            maxEvalueTxt.setText(omssaParameters.getMaxEValue() + "");
        }

        if (omssaParameters.getHitListLength() != null) {
            hitlistTxt.setText(omssaParameters.getHitListLength() + "");
        }

        omssaOutputFormatComboBox.setSelectedItem(omssaParameters.getSelectedOutput());

        if (omssaParameters.getMinimalChargeForMultipleChargedFragments() != null) {
            minPrecChargeMultipleChargedFragmentsTxt.setText(omssaParameters.getMinimalChargeForMultipleChargedFragments().value + "");
        }

        if (omssaParameters.getNeutronThreshold() != null) {
            neutronTxt.setText(omssaParameters.getNeutronThreshold() + "");
        }

        if (omssaParameters.getSingleChargeWindow() != null) {
            singlyChargedWindowWidthTxt.setText(omssaParameters.getSingleChargeWindow() + "");
        }

        if (omssaParameters.getDoubleChargeWindow() != null) {
            doublyChargedWindowWidthTxt.setText(omssaParameters.getDoubleChargeWindow() + "");
        }

        if (omssaParameters.getnPeaksInSingleChargeWindow() != null) {
            singlyChargedNpeaksTxt.setText(omssaParameters.getnPeaksInSingleChargeWindow() + "");
        }

        if (omssaParameters.getnPeaksInDoubleChargeWindow() != null) {
            doublyChargedNpeaksTxt.setText(omssaParameters.getnPeaksInDoubleChargeWindow() + "");
        }

        if (omssaParameters.getnAnnotatedMostIntensePeaks() != null) {
            minAnnotatedMostIntensePeaksTxt.setText(omssaParameters.getnAnnotatedMostIntensePeaks() + "");
        }

        if (omssaParameters.getnAnnotatedMostIntensePeaks() != null) {
            minAnnotatedPeaksTxt.setText(omssaParameters.getMinAnnotatedPeaks() + "");
        }

        if (omssaParameters.getMaxMzLadders() != null) {
            maxLaddersTxt.setText(omssaParameters.getMaxMzLadders() + "");
        }

        if (omssaParameters.getMaxFragmentCharge() != null) {
            maxFragmentChargeTxt.setText(omssaParameters.getMaxFragmentCharge() + "");
        }

        if (omssaParameters.isSearchPositiveIons() != null) {
            if (omssaParameters.isSearchPositiveIons()) {
                searchPositiveIonsCmb.setSelectedIndex(0);
            } else {
                searchPositiveIonsCmb.setSelectedIndex(1);
            }
        } else {
            searchPositiveIonsCmb.setSelectedIndex(1);
        }

        if (omssaParameters.isSearchForwardFragmentFirst() != null) {
            if (omssaParameters.isSearchForwardFragmentFirst()) {
                forwardIonsFirstCmb.setSelectedIndex(0);
            } else {
                forwardIonsFirstCmb.setSelectedIndex(1);
            }
        } else {
            forwardIonsFirstCmb.setSelectedIndex(1);
        }

        if (omssaParameters.isSearchRewindFragments() != null) {
            if (omssaParameters.isSearchRewindFragments()) {
                cTermIonsCmb.setSelectedIndex(0);
            } else {
                cTermIonsCmb.setSelectedIndex(1);
            }
        } else {
            cTermIonsCmb.setSelectedIndex(1);
        }

        if (omssaParameters.getMaxFragmentPerSeries() != null) {
            maxFragmentsPerSeriesTxt.setText(omssaParameters.getMaxFragmentPerSeries() + "");
        }

        if (omssaParameters.isUseCorrelationCorrectionScore() != null) {
            if (omssaParameters.isUseCorrelationCorrectionScore()) {
                correlationCorrectionScoreCmb.setSelectedIndex(0);
            } else {
                correlationCorrectionScoreCmb.setSelectedIndex(1);
            }
        } else {
            correlationCorrectionScoreCmb.setSelectedIndex(1);
        }

        if (omssaParameters.getConsecutiveIonProbability() != null) {
            consecutiveIonProbabilityTxt.setText(omssaParameters.getConsecutiveIonProbability() + "");
        }

        if (omssaParameters.getIterativeSequenceEvalue() != null) {
            iterativeSequenceEvalueTxt.setText(omssaParameters.getIterativeSequenceEvalue() + "");
        }

        if (omssaParameters.getIterativeSpectrumEvalue() != null) {
            iterativeSpectraEvalueTxt.setText(omssaParameters.getIterativeSpectrumEvalue() + "");
        }

        if (omssaParameters.getIterativeReplaceEvalue() != null) {
            iterativeReplaceEvalueTxt.setText(omssaParameters.getIterativeReplaceEvalue() + "");
        }

        if (omssaParameters.getMaxHitsPerSpectrumPerCharge() != null) {
            nHitsPerSpectrumPerChargeTxt.setText(omssaParameters.getMaxHitsPerSpectrumPerCharge() + "");
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
     * Returns the user selection as OMSSA parameters object.
     *
     * @return the user selection
     */
    public OmssaParameters getInput() {

        OmssaParameters tempOmssaParameters = new OmssaParameters();
        String input = lowIntensityTxt.getText().trim();
        tempOmssaParameters.setLowIntensityCutOff(new Double(input));
        input = highIntensityTxt.getText().trim();
        tempOmssaParameters.setHighIntensityCutOff(new Double(input));
        input = intensityIncrementTxt.getText().trim();
        tempOmssaParameters.setIntensityCutOffIncrement(new Double(input));
        input = nPeaksTxt.getText().trim();
        tempOmssaParameters.setIntensityCutOffIncrement(new Double(input));
        tempOmssaParameters.setRemovePrecursor(eliminatePrecursorCombo.getSelectedIndex() == 0);
        tempOmssaParameters.setEstimateCharge(chargeEstimationCombo.getSelectedIndex() == 0);
        tempOmssaParameters.setDetermineChargePlusOneAlgorithmically(plusOneChargeCmb.getSelectedIndex() == 0);
        input = fractionChargeTxt.getText().trim();
        tempOmssaParameters.setFractionOfPeaksForChargeEstimation(new Double(input));
        input = minPrecPerSpectrumTxt.getText().trim();
        tempOmssaParameters.setMinPrecPerSpectrum(new Integer(input));
        tempOmssaParameters.setScalePrecursor(precursorScalingCombo.getSelectedIndex() == 0);
        tempOmssaParameters.setMemoryMappedSequenceLibraries(sequenceMappingCmb.getSelectedIndex() == 0);
        tempOmssaParameters.setCleaveNterMethionine(cleaveNterminalMethionineCmb.getSelectedIndex() == 0);
        input = minPepLengthTxt.getText().trim();
        if (!input.equals("")) {
            tempOmssaParameters.setMinPeptideLength(new Integer(input));
        }
        input = maxPepLengthTxt.getText().trim();
        if (!input.equals("")) {
            tempOmssaParameters.setMaxPeptideLength(new Integer(input));
        }
        input = maxEvalueTxt.getText().trim();
        tempOmssaParameters.setMaxEValue(new Double(input));
        input = hitlistTxt.getText().trim();
        tempOmssaParameters.setHitListLength(new Integer(input));
        tempOmssaParameters.setSelectedOutput(omssaOutputFormatComboBox.getSelectedItem().toString());
        input = minPrecChargeMultipleChargedFragmentsTxt.getText().trim();
        int charge = new Integer(input);
        tempOmssaParameters.setMinimalChargeForMultipleChargedFragments(new Charge(Charge.PLUS, charge));
        input = neutronTxt.getText().trim();
        tempOmssaParameters.setNeutronThreshold(new Double(input));
        input = singlyChargedWindowWidthTxt.getText().trim();
        tempOmssaParameters.setSingleChargeWindow(new Integer(input));
        input = doublyChargedWindowWidthTxt.getText().trim();
        tempOmssaParameters.setDoubleChargeWindow(new Integer(input));
        input = singlyChargedNpeaksTxt.getText().trim();
        tempOmssaParameters.setnPeaksInSingleChargeWindow(new Integer(input));
        input = doublyChargedNpeaksTxt.getText().trim();
        tempOmssaParameters.setnPeaksInDoubleChargeWindow(new Integer(input));
        input = minAnnotatedMostIntensePeaksTxt.getText().trim();
        tempOmssaParameters.setnAnnotatedMostIntensePeaks(new Integer(input));
        input = minAnnotatedPeaksTxt.getText().trim();
        tempOmssaParameters.setMinAnnotatedPeaks(new Integer(input));
        input = nHitsPerSpectrumPerChargeTxt.getText().trim();
        tempOmssaParameters.setMaxHitsPerSpectrumPerCharge(new Integer(input));
        input = maxLaddersTxt.getText().trim();
        tempOmssaParameters.setMaxMzLadders(new Integer(input));
        input = maxFragmentChargeTxt.getText().trim();
        tempOmssaParameters.setMaxFragmentCharge(new Integer(input));
        tempOmssaParameters.setSearchPositiveIons(searchPositiveIonsCmb.getSelectedIndex() == 0);
        tempOmssaParameters.setSearchForwardFragmentFirst(forwardIonsFirstCmb.getSelectedIndex() == 0);
        tempOmssaParameters.setSearchRewindFragments(cTermIonsCmb.getSelectedIndex() == 0);
        input = maxFragmentsPerSeriesTxt.getText().trim();
        tempOmssaParameters.setMaxFragmentPerSeries(new Integer(input));
        tempOmssaParameters.setUseCorrelationCorrectionScore(correlationCorrectionScoreCmb.getSelectedIndex() == 0);
        input = consecutiveIonProbabilityTxt.getText().trim();
        tempOmssaParameters.setConsecutiveIonProbability(new Double(input));
        input = iterativeSequenceEvalueTxt.getText().trim();
        tempOmssaParameters.setIterativeSequenceEvalue(new Double(input));
        input = iterativeSpectraEvalueTxt.getText().trim();
        tempOmssaParameters.setIterativeSpectrumEvalue(new Double(input));
        input = iterativeReplaceEvalueTxt.getText().trim();
        tempOmssaParameters.setIterativeReplaceEvalue(new Double(input));

        return tempOmssaParameters;
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
        openDialogHelpJButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        advancedSettingsWarningLabel = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        spectrumProcessingPanel = new javax.swing.JPanel();
        lowIntensityLbl = new javax.swing.JLabel();
        highIntensityLbl = new javax.swing.JLabel();
        intensityIncrementLbl = new javax.swing.JLabel();
        lowIntensityTxt = new javax.swing.JTextField();
        nPeaksLbl = new javax.swing.JLabel();
        highIntensityTxt = new javax.swing.JTextField();
        intensityIncrementTxt = new javax.swing.JTextField();
        nPeaksTxt = new javax.swing.JTextField();
        chargeReductionLabel = new javax.swing.JLabel();
        eliminatePrecursorCombo = new javax.swing.JComboBox();
        chargeEstimationCombo = new javax.swing.JComboBox();
        precursorChargeEstimationLabel = new javax.swing.JLabel();
        plusOneChargeCmb = new javax.swing.JComboBox();
        plusOneChargeAutomaticLbl = new javax.swing.JLabel();
        fractionChargeLbl = new javax.swing.JLabel();
        fractionChargeTxt = new javax.swing.JTextField();
        minPrecPerSpectrumLbl = new javax.swing.JLabel();
        minPrecPerSpectrumTxt = new javax.swing.JTextField();
        precursorMassScalingLabel = new javax.swing.JLabel();
        precursorScalingCombo = new javax.swing.JComboBox();
        databaseProcessingPanel = new javax.swing.JPanel();
        sequenceMappingLbl = new javax.swing.JLabel();
        sequenceMappingCmb = new javax.swing.JComboBox();
        cleaveNterminalMethionineCmb = new javax.swing.JComboBox();
        cleaveNterminalMethionineLbl = new javax.swing.JLabel();
        advancedSearchSettingsPanel = new javax.swing.JPanel();
        searchSettingsScrollPane = new javax.swing.JScrollPane();
        searchSettingsPanel = new javax.swing.JPanel();
        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel = new javax.swing.JLabel();
        minPrecChargeMultipleChargedFragmentsTxt = new javax.swing.JTextField();
        neutronLbl = new javax.swing.JLabel();
        neutronTxt = new javax.swing.JTextField();
        singlyChargedWindowWidthLbl = new javax.swing.JLabel();
        singlyChargedWindowWidthTxt = new javax.swing.JTextField();
        doublyChargedWindowWidthLbl = new javax.swing.JLabel();
        doublyChargedWindowWidthTxt = new javax.swing.JTextField();
        singlyChargedNpeaksTxt = new javax.swing.JTextField();
        singlyChargedNPeaksLbl = new javax.swing.JLabel();
        doublyChargedNPeaksLbl = new javax.swing.JLabel();
        doublyChargedNpeaksTxt = new javax.swing.JTextField();
        minAnnotatedMostIntensePeaksLbl = new javax.swing.JLabel();
        minAnnotatedMostIntensePeaksTxt = new javax.swing.JTextField();
        minAnnotatedPeaksLbl = new javax.swing.JLabel();
        minAnnotatedPeaksTxt = new javax.swing.JTextField();
        maxLaddersLbl = new javax.swing.JLabel();
        maxLaddersTxt = new javax.swing.JTextField();
        maxFragmentChargeTxt = new javax.swing.JTextField();
        maxFragmentChargeLbl = new javax.swing.JLabel();
        searchPositiveIonsLbl = new javax.swing.JLabel();
        searchPositiveIonsCmb = new javax.swing.JComboBox();
        forwardIonsFirstLbl = new javax.swing.JLabel();
        forwardIonsFirstCmb = new javax.swing.JComboBox();
        cTermIonsLbl = new javax.swing.JLabel();
        cTermIonsCmb = new javax.swing.JComboBox();
        maxFragmentsPerSeriesLbl = new javax.swing.JLabel();
        maxFragmentsPerSeriesTxt = new javax.swing.JTextField();
        correlationCorrectionScoreCmb = new javax.swing.JComboBox();
        correlationCorrectionScoreLbl = new javax.swing.JLabel();
        consecutiveIonProbabilityTxt = new javax.swing.JTextField();
        consecutiveIonProbabilityLbl = new javax.swing.JLabel();
        nHitsPerSpectrumPerChargeLbl = new javax.swing.JLabel();
        nHitsPerSpectrumPerChargeTxt = new javax.swing.JTextField();
        iterativeSearchSettingsPanel = new javax.swing.JPanel();
        iterativeSequenceEvalueLbl = new javax.swing.JLabel();
        iterativeSequenceEvalueTxt = new javax.swing.JTextField();
        iterativeSpectraEvalueLbl = new javax.swing.JLabel();
        iterativeSpectraEvalueTxt = new javax.swing.JTextField();
        iterativeReplaceEvalueLbl = new javax.swing.JLabel();
        iterativeReplaceEvalueTxt = new javax.swing.JTextField();
        semiEnzymaticParametersPanel = new javax.swing.JPanel();
        maxPepLengthTxt = new javax.swing.JTextField();
        peptideLengthDividerLabel1 = new javax.swing.JLabel();
        minPepLengthTxt = new javax.swing.JTextField();
        peptideLengthJLabel = new javax.swing.JLabel();
        outputParametersPanel = new javax.swing.JPanel();
        omssaOutputFormatComboBox = new javax.swing.JComboBox();
        omssaOutputFormatLabel = new javax.swing.JLabel();
        eValueLbl = new javax.swing.JLabel();
        hitListLbl = new javax.swing.JLabel();
        maxEvalueTxt = new javax.swing.JTextField();
        hitlistTxt = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("OMSSA Advanced Settings");
        setMinimumSize(new java.awt.Dimension(600, 500));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

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

        advancedSettingsWarningLabel.setText("Note: The advanced settings are for expert use only. See the help for details.");

        tabbedPane.setBackground(new java.awt.Color(230, 230, 230));
        tabbedPane.setOpaque(true);

        spectrumProcessingPanel.setBackground(new java.awt.Color(230, 230, 230));
        spectrumProcessingPanel.setPreferredSize(new java.awt.Dimension(518, 143));

        lowIntensityLbl.setText("Low Intensity Cutoff (percent of most intense peak)");

        highIntensityLbl.setText("High Intensity Cutoff (percent of most intense peak)");

        intensityIncrementLbl.setText("Intensity Cutoff Increment");

        lowIntensityTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lowIntensityTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lowIntensityTxtKeyReleased(evt);
            }
        });

        nPeaksLbl.setText("Minimal Number of Peaks");

        highIntensityTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        highIntensityTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                highIntensityTxtKeyReleased(evt);
            }
        });

        intensityIncrementTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        intensityIncrementTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                intensityIncrementTxtKeyReleased(evt);
            }
        });

        nPeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nPeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nPeaksTxtKeyReleased(evt);
            }
        });

        chargeReductionLabel.setText("Eliminate Charge Reduced Precursors in Spectra");

        eliminatePrecursorCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        chargeEstimationCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Use Range", "Believe Input File" }));

        precursorChargeEstimationLabel.setText("Precursor Charge Estimation");

        plusOneChargeCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        plusOneChargeAutomaticLbl.setText("Plus One Charge Estimated Algorithmically");

        fractionChargeLbl.setText("Fraction of Precursor m/z for Charge One Estimation");

        fractionChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        fractionChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fractionChargeTxtKeyReleased(evt);
            }
        });

        minPrecPerSpectrumLbl.setText("Minimal Number of Precursors per Spectrum");

        minPrecPerSpectrumTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecPerSpectrumTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecPerSpectrumTxtKeyReleased(evt);
            }
        });

        precursorMassScalingLabel.setText("Precursor Mass Scaling");

        precursorScalingCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout spectrumProcessingPanelLayout = new javax.swing.GroupLayout(spectrumProcessingPanel);
        spectrumProcessingPanel.setLayout(spectrumProcessingPanelLayout);
        spectrumProcessingPanelLayout.setHorizontalGroup(
            spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spectrumProcessingPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(intensityIncrementLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(nPeaksLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                        .addComponent(chargeReductionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE))
                    .addComponent(highIntensityLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(minPrecPerSpectrumLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(fractionChargeLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(plusOneChargeAutomaticLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(precursorChargeEstimationLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE))
                    .addComponent(lowIntensityLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorMassScalingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lowIntensityTxt)
                    .addComponent(highIntensityTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(nPeaksTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(eliminatePrecursorCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 179, Short.MAX_VALUE)
                    .addComponent(chargeEstimationCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 179, Short.MAX_VALUE)
                    .addComponent(plusOneChargeCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, 179, Short.MAX_VALUE)
                    .addComponent(fractionChargeTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(minPrecPerSpectrumTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(intensityIncrementTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(precursorScalingCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 179, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        spectrumProcessingPanelLayout.setVerticalGroup(
            spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spectrumProcessingPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lowIntensityLbl)
                    .addComponent(lowIntensityTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(highIntensityLbl)
                    .addComponent(highIntensityTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intensityIncrementLbl)
                    .addComponent(intensityIncrementTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nPeaksLbl)
                    .addComponent(nPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chargeReductionLabel)
                    .addComponent(eliminatePrecursorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(precursorChargeEstimationLabel)
                    .addComponent(chargeEstimationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(plusOneChargeAutomaticLbl)
                    .addComponent(plusOneChargeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(fractionChargeLbl)
                    .addComponent(fractionChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(minPrecPerSpectrumLbl)
                    .addComponent(minPrecPerSpectrumTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(precursorScalingCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorMassScalingLabel))
                .addContainerGap(378, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Spectrum", spectrumProcessingPanel);

        databaseProcessingPanel.setBackground(new java.awt.Color(230, 230, 230));

        sequenceMappingLbl.setText("Sequences Mapping in Memory");

        sequenceMappingCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        cleaveNterminalMethionineCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        cleaveNterminalMethionineLbl.setText("Cleave N-terminal Methionine");

        javax.swing.GroupLayout databaseProcessingPanelLayout = new javax.swing.GroupLayout(databaseProcessingPanel);
        databaseProcessingPanel.setLayout(databaseProcessingPanelLayout);
        databaseProcessingPanelLayout.setHorizontalGroup(
            databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databaseProcessingPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sequenceMappingLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cleaveNterminalMethionineLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cleaveNterminalMethionineCmb, 0, 180, Short.MAX_VALUE)
                    .addComponent(sequenceMappingCmb, 0, 180, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        databaseProcessingPanelLayout.setVerticalGroup(
            databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, databaseProcessingPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sequenceMappingLbl)
                    .addComponent(sequenceMappingCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cleaveNterminalMethionineLbl)
                    .addComponent(cleaveNterminalMethionineCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Database", databaseProcessingPanel);

        advancedSearchSettingsPanel.setBackground(new java.awt.Color(230, 230, 230));
        advancedSearchSettingsPanel.setPreferredSize(new java.awt.Dimension(518, 143));

        searchSettingsScrollPane.setBorder(null);

        searchSettingsPanel.setBackground(new java.awt.Color(230, 230, 230));

        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setText("Minimum Precursor Charge for Multiply Charged Fragments");

        minPrecChargeMultipleChargedFragmentsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecChargeMultipleChargedFragmentsTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecChargeMultipleChargedFragmentsTxtKeyReleased(evt);
            }
        });

        neutronLbl.setText("Mass Threshold to Consider Exact Neutron Mass");

        neutronTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        neutronTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                neutronTxtKeyReleased(evt);
            }
        });

        singlyChargedWindowWidthLbl.setText("Singly Charged Window Width (Da)");

        singlyChargedWindowWidthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        singlyChargedWindowWidthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                singlyChargedWindowWidthTxtKeyReleased(evt);
            }
        });

        doublyChargedWindowWidthLbl.setText("Doubly Charged Window Width (Da)");

        doublyChargedWindowWidthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        doublyChargedWindowWidthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                doublyChargedWindowWidthTxtKeyReleased(evt);
            }
        });

        singlyChargedNpeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        singlyChargedNpeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                singlyChargedNpeaksTxtKeyReleased(evt);
            }
        });

        singlyChargedNPeaksLbl.setText("Number of Peaks in Singly Charged Windows");

        doublyChargedNPeaksLbl.setText("Number of Peaks in Doubly Charged Windows");

        doublyChargedNpeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        doublyChargedNpeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                doublyChargedNpeaksTxtKeyReleased(evt);
            }
        });

        minAnnotatedMostIntensePeaksLbl.setText("Minimum Annotated Peaks Among the Most Intense Ones");

        minAnnotatedMostIntensePeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minAnnotatedMostIntensePeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minAnnotatedMostIntensePeaksTxtKeyReleased(evt);
            }
        });

        minAnnotatedPeaksLbl.setText("Minimum Number of Annotated Peaks");

        minAnnotatedPeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minAnnotatedPeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minAnnotatedPeaksTxtKeyReleased(evt);
            }
        });

        maxLaddersLbl.setText("Maximum m/z Ladders");

        maxLaddersTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxLaddersTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxLaddersTxtKeyReleased(evt);
            }
        });

        maxFragmentChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxFragmentChargeTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxFragmentChargeTxtActionPerformed(evt);
            }
        });
        maxFragmentChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxFragmentChargeTxtKeyReleased(evt);
            }
        });

        maxFragmentChargeLbl.setText("Maximum Fragment Charge");

        searchPositiveIonsLbl.setText("Search Positive Ions");

        searchPositiveIonsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        forwardIonsFirstLbl.setText("Search First Forward Ion (b1)");

        forwardIonsFirstCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        cTermIonsLbl.setText("Search Rewind (C-terminal) Ions");

        cTermIonsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        maxFragmentsPerSeriesLbl.setText("Maximum Fragments per Series");

        maxFragmentsPerSeriesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxFragmentsPerSeriesTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxFragmentsPerSeriesTxtActionPerformed(evt);
            }
        });
        maxFragmentsPerSeriesTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxFragmentsPerSeriesTxtKeyReleased(evt);
            }
        });

        correlationCorrectionScoreCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        correlationCorrectionScoreLbl.setText("Use Correlation Correction Score");

        consecutiveIonProbabilityTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        consecutiveIonProbabilityTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consecutiveIonProbabilityTxtActionPerformed(evt);
            }
        });
        consecutiveIonProbabilityTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                consecutiveIonProbabilityTxtKeyReleased(evt);
            }
        });

        consecutiveIonProbabilityLbl.setText("Consecutive Ion Probability");

        nHitsPerSpectrumPerChargeLbl.setText("Number of Hits per Spectrum per Charge");

        nHitsPerSpectrumPerChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nHitsPerSpectrumPerChargeTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nHitsPerSpectrumPerChargeTxtActionPerformed(evt);
            }
        });
        nHitsPerSpectrumPerChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nHitsPerSpectrumPerChargeTxtKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout searchSettingsPanelLayout = new javax.swing.GroupLayout(searchSettingsPanel);
        searchSettingsPanel.setLayout(searchSettingsPanelLayout);
        searchSettingsPanelLayout.setHorizontalGroup(
            searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minPrecChargeMultipleChargedFragmentsTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(neutronLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(neutronTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(singlyChargedWindowWidthLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(singlyChargedWindowWidthTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(doublyChargedWindowWidthLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(doublyChargedWindowWidthTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(singlyChargedNPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(singlyChargedNpeaksTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(doublyChargedNPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(doublyChargedNpeaksTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(minAnnotatedMostIntensePeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minAnnotatedMostIntensePeaksTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(minAnnotatedPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minAnnotatedPeaksTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(maxLaddersLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxLaddersTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxFragmentChargeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchPositiveIonsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchPositiveIonsCmb, 0, 180, Short.MAX_VALUE)
                    .addComponent(maxFragmentChargeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(forwardIonsFirstLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(forwardIonsFirstCmb, 0, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(cTermIonsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cTermIonsCmb, 0, 180, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(maxFragmentsPerSeriesLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxFragmentsPerSeriesTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(correlationCorrectionScoreLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(correlationCorrectionScoreCmb, 0, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(consecutiveIonProbabilityLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(consecutiveIonProbabilityTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addComponent(nHitsPerSpectrumPerChargeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nHitsPerSpectrumPerChargeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
        );
        searchSettingsPanelLayout.setVerticalGroup(
            searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchSettingsPanelLayout.createSequentialGroup()
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel)
                    .addComponent(minPrecChargeMultipleChargedFragmentsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neutronLbl)
                    .addComponent(neutronTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(singlyChargedWindowWidthLbl)
                    .addComponent(singlyChargedWindowWidthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(doublyChargedWindowWidthLbl)
                    .addComponent(doublyChargedWindowWidthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(singlyChargedNPeaksLbl)
                    .addComponent(singlyChargedNpeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(doublyChargedNPeaksLbl)
                    .addComponent(doublyChargedNpeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minAnnotatedMostIntensePeaksLbl)
                    .addComponent(minAnnotatedMostIntensePeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minAnnotatedPeaksLbl)
                    .addComponent(minAnnotatedPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxLaddersLbl)
                    .addComponent(maxLaddersTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxFragmentChargeLbl)
                    .addComponent(maxFragmentChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPositiveIonsLbl)
                    .addComponent(searchPositiveIonsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(forwardIonsFirstLbl)
                    .addComponent(forwardIonsFirstCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cTermIonsLbl)
                    .addComponent(cTermIonsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxFragmentsPerSeriesLbl)
                    .addComponent(maxFragmentsPerSeriesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(correlationCorrectionScoreLbl)
                    .addComponent(correlationCorrectionScoreCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(consecutiveIonProbabilityLbl)
                    .addComponent(consecutiveIonProbabilityTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nHitsPerSpectrumPerChargeLbl)
                    .addComponent(nHitsPerSpectrumPerChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        searchSettingsScrollPane.setViewportView(searchSettingsPanel);

        javax.swing.GroupLayout advancedSearchSettingsPanelLayout = new javax.swing.GroupLayout(advancedSearchSettingsPanel);
        advancedSearchSettingsPanel.setLayout(advancedSearchSettingsPanelLayout);
        advancedSearchSettingsPanelLayout.setHorizontalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(searchSettingsScrollPane)
                .addGap(25, 25, 25))
        );
        advancedSearchSettingsPanelLayout.setVerticalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(searchSettingsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Search", advancedSearchSettingsPanel);

        iterativeSearchSettingsPanel.setBackground(new java.awt.Color(230, 230, 230));

        iterativeSequenceEvalueLbl.setText("E-value Cutoff for Sequences (0 means all)");

        iterativeSequenceEvalueTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        iterativeSequenceEvalueTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                iterativeSequenceEvalueTxtKeyReleased(evt);
            }
        });

        iterativeSpectraEvalueLbl.setText("E-value Cutoff for Spectra (0 means all)");

        iterativeSpectraEvalueTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        iterativeSpectraEvalueTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                iterativeSpectraEvalueTxtKeyReleased(evt);
            }
        });

        iterativeReplaceEvalueLbl.setText("E-value Cutoff to Replace a Hit (0 means keep best)");

        iterativeReplaceEvalueTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        iterativeReplaceEvalueTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                iterativeReplaceEvalueTxtKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout iterativeSearchSettingsPanelLayout = new javax.swing.GroupLayout(iterativeSearchSettingsPanel);
        iterativeSearchSettingsPanel.setLayout(iterativeSearchSettingsPanelLayout);
        iterativeSearchSettingsPanelLayout.setHorizontalGroup(
            iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(iterativeSearchSettingsPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iterativeSequenceEvalueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iterativeSpectraEvalueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iterativeReplaceEvalueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iterativeReplaceEvalueTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(iterativeSequenceEvalueTxt, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(iterativeSpectraEvalueTxt, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(25, 25, 25))
        );
        iterativeSearchSettingsPanelLayout.setVerticalGroup(
            iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(iterativeSearchSettingsPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iterativeSequenceEvalueLbl)
                    .addComponent(iterativeSequenceEvalueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iterativeSpectraEvalueLbl)
                    .addComponent(iterativeSpectraEvalueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iterativeReplaceEvalueLbl)
                    .addComponent(iterativeReplaceEvalueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Iterative Search", iterativeSearchSettingsPanel);

        semiEnzymaticParametersPanel.setBackground(new java.awt.Color(230, 230, 230));

        maxPepLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPepLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPepLengthTxtKeyReleased(evt);
            }
        });

        peptideLengthDividerLabel1.setText("-");

        minPepLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPepLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPepLengthTxtKeyReleased(evt);
            }
        });

        peptideLengthJLabel.setText("Peptide Length (min - max)");

        javax.swing.GroupLayout semiEnzymaticParametersPanelLayout = new javax.swing.GroupLayout(semiEnzymaticParametersPanel);
        semiEnzymaticParametersPanel.setLayout(semiEnzymaticParametersPanelLayout);
        semiEnzymaticParametersPanelLayout.setHorizontalGroup(
            semiEnzymaticParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, semiEnzymaticParametersPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(peptideLengthJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minPepLengthTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(peptideLengthDividerLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addGap(25, 25, 25))
        );
        semiEnzymaticParametersPanelLayout.setVerticalGroup(
            semiEnzymaticParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(semiEnzymaticParametersPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(semiEnzymaticParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptideLengthJLabel)
                    .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideLengthDividerLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Semi-Enzymatic", semiEnzymaticParametersPanel);

        outputParametersPanel.setBackground(new java.awt.Color(230, 230, 230));

        omssaOutputFormatComboBox.setModel(new DefaultComboBoxModel(OmssaParameters.getOmssaOutputTypes()));
        omssaOutputFormatComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                omssaOutputFormatComboBoxActionPerformed(evt);
            }
        });

        omssaOutputFormatLabel.setText("OMSSA Output Format");

        eValueLbl.setText("E-value Cutoff");

        hitListLbl.setText("Maximum HitList Length (0 means all)");

        maxEvalueTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxEvalueTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxEvalueTxtKeyReleased(evt);
            }
        });

        hitlistTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        hitlistTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                hitlistTxtKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout outputParametersPanelLayout = new javax.swing.GroupLayout(outputParametersPanel);
        outputParametersPanel.setLayout(outputParametersPanelLayout);
        outputParametersPanelLayout.setHorizontalGroup(
            outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputParametersPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(omssaOutputFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hitListLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eValueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hitlistTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(maxEvalueTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(omssaOutputFormatComboBox, 0, 180, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        outputParametersPanelLayout.setVerticalGroup(
            outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outputParametersPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eValueLbl)
                    .addComponent(maxEvalueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hitListLbl)
                    .addComponent(hitlistTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(omssaOutputFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(omssaOutputFormatLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Output", outputParametersPanel);

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane)
                    .addComponent(jSeparator1)
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
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
     * Validate the input.
     *
     * @param evt
     */
    private void minPrecChargeMultipleChargedFragmentsTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecChargeMultipleChargedFragmentsTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPrecChargeMultipleChargedFragmentsTxtKeyReleased

    /**
     * Save the settings and close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput(true)) {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close the without saving.
     *
     * @param evt
     */
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        cancelled = true;
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

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
     * Open the help dialog.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/OmssaSettingsDialog.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                "OMSSA - Help", 500, 50);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void omssaOutputFormatComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_omssaOutputFormatComboBoxActionPerformed

        if (!((String) omssaOutputFormatComboBox.getSelectedItem()).equalsIgnoreCase("OMX") && this.isVisible()) {
            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(OmssaParametersDialog.this, JOptionEditorPane.getJOptionEditorPane(
                            "Note that the OMSSA " + (String) omssaOutputFormatComboBox.getSelectedItem()
                            + " format is not compatible with <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">PeptideShaker</a>."),
                            "Format Warning", JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        validateInput(false);
    }//GEN-LAST:event_omssaOutputFormatComboBoxActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxEvalueTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxEvalueTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxEvalueTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void hitlistTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hitlistTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_hitlistTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxPepLengthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPepLengthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPepLengthTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minPepLengthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPepLengthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPepLengthTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void iterativeSequenceEvalueTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iterativeSequenceEvalueTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_iterativeSequenceEvalueTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void iterativeSpectraEvalueTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iterativeSpectraEvalueTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_iterativeSpectraEvalueTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void iterativeReplaceEvalueTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iterativeReplaceEvalueTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_iterativeReplaceEvalueTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void neutronTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_neutronTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_neutronTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void singlyChargedWindowWidthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_singlyChargedWindowWidthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_singlyChargedWindowWidthTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void doublyChargedWindowWidthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_doublyChargedWindowWidthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_doublyChargedWindowWidthTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void singlyChargedNpeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_singlyChargedNpeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_singlyChargedNpeaksTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void doublyChargedNpeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_doublyChargedNpeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_doublyChargedNpeaksTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minAnnotatedMostIntensePeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minAnnotatedMostIntensePeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minAnnotatedMostIntensePeaksTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minAnnotatedPeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minAnnotatedPeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minAnnotatedPeaksTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxLaddersTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxLaddersTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxLaddersTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxFragmentChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxFragmentChargeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxFragmentChargeTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxFragmentChargeTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxFragmentChargeTxtActionPerformed
        validateInput(false);
    }//GEN-LAST:event_maxFragmentChargeTxtActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxFragmentsPerSeriesTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxFragmentsPerSeriesTxtActionPerformed
        validateInput(false);
    }//GEN-LAST:event_maxFragmentsPerSeriesTxtActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxFragmentsPerSeriesTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxFragmentsPerSeriesTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxFragmentsPerSeriesTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void consecutiveIonProbabilityTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consecutiveIonProbabilityTxtActionPerformed
        validateInput(false);
    }//GEN-LAST:event_consecutiveIonProbabilityTxtActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void consecutiveIonProbabilityTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_consecutiveIonProbabilityTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_consecutiveIonProbabilityTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minPrecPerSpectrumTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecPerSpectrumTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPrecPerSpectrumTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void fractionChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fractionChargeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_fractionChargeTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void nPeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nPeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_nPeaksTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void intensityIncrementTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_intensityIncrementTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_intensityIncrementTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void highIntensityTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_highIntensityTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_highIntensityTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void lowIntensityTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lowIntensityTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_lowIntensityTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void nHitsPerSpectrumPerChargeTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nHitsPerSpectrumPerChargeTxtActionPerformed
        validateInput(false);
    }//GEN-LAST:event_nHitsPerSpectrumPerChargeTxtActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void nHitsPerSpectrumPerChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nHitsPerSpectrumPerChargeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_nHitsPerSpectrumPerChargeTxtKeyReleased

    /**
     * Close the without saving.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;

        valid = GuiUtilities.validateDoubleInput(this, lowIntensityLbl, lowIntensityTxt, "low intensity cutoff", "Low Intensity Cutoff Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, highIntensityLbl, highIntensityTxt, "high intensity cutoff", "High Intensity Cutoff Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, intensityIncrementLbl, intensityIncrementTxt, "intensity cutoff increment", "Intensity Cutoff Increment Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, nPeaksLbl, nPeaksTxt, "number of peaks per spectrum", "Number of Peaks Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, fractionChargeLbl, fractionChargeTxt, "fraction of peaks for charge estimation", "Fraction of Peaks Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, minPrecPerSpectrumLbl, minPrecPerSpectrumTxt, "number of precursor per spectrum", "Number of Precursors Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, peptideLengthJLabel, minPepLengthTxt, "minimim peptide length", "Peptide Length Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, peptideLengthJLabel, maxPepLengthTxt, "maximum peptide length", "Peptide Length Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, eValueLbl, maxEvalueTxt, "maximal e-value", "Maximum E-Value Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, hitListLbl, hitlistTxt, "hitlist length", "Hitlist Length Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, nHitsPerSpectrumPerChargeLbl, nHitsPerSpectrumPerChargeTxt, "number of hits per spectrum and per charge", "Hits per Spectrum and per Charge Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel, minPrecChargeMultipleChargedFragmentsTxt, "minimal charge to consider multiply charged fragments", "Minimal Charge for Multiple Fragments Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, neutronLbl, neutronTxt, "mass after which the exact mass of a neutron is used", "Mass for Proton Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, singlyChargedWindowWidthLbl, singlyChargedWindowWidthTxt, "size for single charge windows", "Window Size Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, doublyChargedWindowWidthLbl, doublyChargedWindowWidthTxt, "size for double charge windows", "Window Size Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, singlyChargedNPeaksLbl, singlyChargedNpeaksTxt, "number of peaks for single charge windows", "Window Number of Peaks Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, doublyChargedNPeaksLbl, doublyChargedNpeaksTxt, "number of peaks for double charge windows", "Window Number of Peaks Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, minAnnotatedMostIntensePeaksLbl, minAnnotatedMostIntensePeaksTxt, "number for the minimal number of annotated peaks among the most intense ones", "Number of Annotated Intense Peaks Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, minAnnotatedPeaksLbl, minAnnotatedPeaksTxt, "number for the minimal number of annotated peaks", "Number of Annotated Peaks Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxLaddersLbl, maxLaddersTxt, "number for the maximal m/z ladder size", "Ladder Size Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxFragmentsPerSeriesLbl, maxFragmentsPerSeriesTxt, "number for the maximal number of fragments per series", "Fragments per Series Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, consecutiveIonProbabilityLbl, consecutiveIonProbabilityTxt, "consecutive ion probability", "Consecutive Ion Probability Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, iterativeSequenceEvalueLbl, iterativeSequenceEvalueTxt, "e-value cutoff to include sequences in the iterative search", "Iterative Search E-value Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, iterativeSpectraEvalueLbl, iterativeSpectraEvalueTxt, "e-value cutoff to include spectra in the iterative search", "Iterative Search E-value Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, iterativeReplaceEvalueLbl, iterativeReplaceEvalueTxt, "e-value cutoff to replace hits in the iterative search", "Iterative Search E-value Error", true, showMessage, valid);

        okButton.setEnabled(valid);

        return valid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedSearchSettingsPanel;
    private javax.swing.JLabel advancedSettingsWarningLabel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JComboBox cTermIonsCmb;
    private javax.swing.JLabel cTermIonsLbl;
    private javax.swing.JComboBox chargeEstimationCombo;
    private javax.swing.JLabel chargeReductionLabel;
    private javax.swing.JComboBox cleaveNterminalMethionineCmb;
    private javax.swing.JLabel cleaveNterminalMethionineLbl;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel consecutiveIonProbabilityLbl;
    private javax.swing.JTextField consecutiveIonProbabilityTxt;
    private javax.swing.JComboBox correlationCorrectionScoreCmb;
    private javax.swing.JLabel correlationCorrectionScoreLbl;
    private javax.swing.JPanel databaseProcessingPanel;
    private javax.swing.JLabel doublyChargedNPeaksLbl;
    private javax.swing.JTextField doublyChargedNpeaksTxt;
    private javax.swing.JLabel doublyChargedWindowWidthLbl;
    private javax.swing.JTextField doublyChargedWindowWidthTxt;
    private javax.swing.JLabel eValueLbl;
    private javax.swing.JComboBox eliminatePrecursorCombo;
    private javax.swing.JComboBox forwardIonsFirstCmb;
    private javax.swing.JLabel forwardIonsFirstLbl;
    private javax.swing.JLabel fractionChargeLbl;
    private javax.swing.JTextField fractionChargeTxt;
    private javax.swing.JLabel highIntensityLbl;
    private javax.swing.JTextField highIntensityTxt;
    private javax.swing.JLabel hitListLbl;
    private javax.swing.JTextField hitlistTxt;
    private javax.swing.JLabel intensityIncrementLbl;
    private javax.swing.JTextField intensityIncrementTxt;
    private javax.swing.JLabel iterativeReplaceEvalueLbl;
    private javax.swing.JTextField iterativeReplaceEvalueTxt;
    private javax.swing.JPanel iterativeSearchSettingsPanel;
    private javax.swing.JLabel iterativeSequenceEvalueLbl;
    private javax.swing.JTextField iterativeSequenceEvalueTxt;
    private javax.swing.JLabel iterativeSpectraEvalueLbl;
    private javax.swing.JTextField iterativeSpectraEvalueTxt;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lowIntensityLbl;
    private javax.swing.JTextField lowIntensityTxt;
    private javax.swing.JTextField maxEvalueTxt;
    private javax.swing.JLabel maxFragmentChargeLbl;
    private javax.swing.JTextField maxFragmentChargeTxt;
    private javax.swing.JLabel maxFragmentsPerSeriesLbl;
    private javax.swing.JTextField maxFragmentsPerSeriesTxt;
    private javax.swing.JLabel maxLaddersLbl;
    private javax.swing.JTextField maxLaddersTxt;
    private javax.swing.JTextField maxPepLengthTxt;
    private javax.swing.JLabel minAnnotatedMostIntensePeaksLbl;
    private javax.swing.JTextField minAnnotatedMostIntensePeaksTxt;
    private javax.swing.JLabel minAnnotatedPeaksLbl;
    private javax.swing.JTextField minAnnotatedPeaksTxt;
    private javax.swing.JTextField minPepLengthTxt;
    private javax.swing.JTextField minPrecChargeMultipleChargedFragmentsTxt;
    private javax.swing.JLabel minPrecPerSpectrumLbl;
    private javax.swing.JTextField minPrecPerSpectrumTxt;
    private javax.swing.JLabel minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel;
    private javax.swing.JLabel nHitsPerSpectrumPerChargeLbl;
    private javax.swing.JTextField nHitsPerSpectrumPerChargeTxt;
    private javax.swing.JLabel nPeaksLbl;
    private javax.swing.JTextField nPeaksTxt;
    private javax.swing.JLabel neutronLbl;
    private javax.swing.JTextField neutronTxt;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox omssaOutputFormatComboBox;
    private javax.swing.JLabel omssaOutputFormatLabel;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JPanel outputParametersPanel;
    private javax.swing.JLabel peptideLengthDividerLabel1;
    private javax.swing.JLabel peptideLengthJLabel;
    private javax.swing.JLabel plusOneChargeAutomaticLbl;
    private javax.swing.JComboBox plusOneChargeCmb;
    private javax.swing.JLabel precursorChargeEstimationLabel;
    private javax.swing.JLabel precursorMassScalingLabel;
    private javax.swing.JComboBox precursorScalingCombo;
    private javax.swing.JComboBox searchPositiveIonsCmb;
    private javax.swing.JLabel searchPositiveIonsLbl;
    private javax.swing.JPanel searchSettingsPanel;
    private javax.swing.JScrollPane searchSettingsScrollPane;
    private javax.swing.JPanel semiEnzymaticParametersPanel;
    private javax.swing.JComboBox sequenceMappingCmb;
    private javax.swing.JLabel sequenceMappingLbl;
    private javax.swing.JLabel singlyChargedNPeaksLbl;
    private javax.swing.JTextField singlyChargedNpeaksTxt;
    private javax.swing.JLabel singlyChargedWindowWidthLbl;
    private javax.swing.JTextField singlyChargedWindowWidthTxt;
    private javax.swing.JPanel spectrumProcessingPanel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
