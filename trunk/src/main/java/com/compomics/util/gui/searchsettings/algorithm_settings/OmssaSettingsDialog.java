package com.compomics.util.gui.searchsettings.algorithm_settings;

import com.compomics.util.experiment.identification.identification_parameters.OmssaParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.gui.JOptionEditorPane;
import com.compomics.util.gui.error_handlers.HelpDialog;
import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Dialog for the OMSSA specific parameters.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class OmssaSettingsDialog extends javax.swing.JDialog {

    /**
     * The OMSSA parameters class containing the information to display.
     */
    private OmssaParameters omssaParameters;
    /**
     * Boolean indicating whether the used canceled the editing.
     */
    private boolean cancelled = false;

    /**
     * Creates new form OmssaParametersDialog.
     *
     * @param parent the parent frame
     * @param omssaParameters the OMSSA parameters
     */
    public OmssaSettingsDialog(java.awt.Frame parent, OmssaParameters omssaParameters) {
        super(parent, true);
        this.omssaParameters = omssaParameters;
        initComponents();
        setUpGui();
        fillGUI();
        validateInput(false);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Set up the GUI.
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
    }

    /**
     * Fills the GUI with the information contained in the OMSSA parameters
     * object.
     */
    private void fillGUI() {

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

        if (omssaParameters.getNumberOfItotopicPeaks() != null) {
            nIsotopesTxt.setText(omssaParameters.getNumberOfItotopicPeaks() + "");
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

    /**
     * Indicates whether the user canceled the process.
     *
     * @return true if cancel was pressed
     */
    public boolean isCancelled() {
        return cancelled;
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
        input = nIsotopesTxt.getText().trim();
        tempOmssaParameters.setNumberOfItotopicPeaks(new Integer(input));
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
        semiEnzymaticParametersPanel = new javax.swing.JPanel();
        maxPepLengthTxt = new javax.swing.JTextField();
        peptideLengthDividerLabel1 = new javax.swing.JLabel();
        minPepLengthTxt = new javax.swing.JTextField();
        peptideLengthJLabel = new javax.swing.JLabel();
        iterativeSearchSettingsPanel = new javax.swing.JPanel();
        iterativeSequenceEvalueLbl = new javax.swing.JLabel();
        iterativeSequenceEvalueTxt = new javax.swing.JTextField();
        iterativeSpectraEvalueLbl = new javax.swing.JLabel();
        iterativeSpectraEvalueTxt = new javax.swing.JTextField();
        iterativeReplaceEvalueLbl = new javax.swing.JLabel();
        iterativeReplaceEvalueTxt = new javax.swing.JTextField();
        advancedSearchSettingsPanel = new javax.swing.JPanel();
        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel = new javax.swing.JLabel();
        minPrecChargeMultipleChargedFragmentsTxt = new javax.swing.JTextField();
        nIsotopesLbl = new javax.swing.JLabel();
        nIsotopesTxt = new javax.swing.JTextField();
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
        outputParametersPanel = new javax.swing.JPanel();
        omssaOutputFormatComboBox = new javax.swing.JComboBox();
        omssaOutputFormatLabel = new javax.swing.JLabel();
        eValueLbl = new javax.swing.JLabel();
        hitListLbl = new javax.swing.JLabel();
        maxEvalueTxt = new javax.swing.JTextField();
        hitlistTxt = new javax.swing.JTextField();
        openDialogHelpJButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Advanced OMSSA Settings");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        spectrumProcessingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Spectrum Processing Settings"));
        spectrumProcessingPanel.setOpaque(false);
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

        chargeEstimationCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

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
                .addContainerGap()
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(precursorMassScalingLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(spectrumProcessingPanelLayout.createSequentialGroup()
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
                            .addComponent(lowIntensityLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lowIntensityTxt)
                    .addComponent(highIntensityTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(nPeaksTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(eliminatePrecursorCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 109, Short.MAX_VALUE)
                    .addComponent(chargeEstimationCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 109, Short.MAX_VALUE)
                    .addComponent(plusOneChargeCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, 109, Short.MAX_VALUE)
                    .addComponent(fractionChargeTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(minPrecPerSpectrumTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(intensityIncrementTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(precursorScalingCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 109, Short.MAX_VALUE))
                .addContainerGap())
        );
        spectrumProcessingPanelLayout.setVerticalGroup(
            spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spectrumProcessingPanelLayout.createSequentialGroup()
                .addContainerGap()
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
                .addGroup(spectrumProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(precursorMassScalingLabel)
                    .addComponent(precursorScalingCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        databaseProcessingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Database Processing Settings"));
        databaseProcessingPanel.setOpaque(false);

        sequenceMappingLbl.setText("Sequences Mapping in Memory");

        sequenceMappingCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        cleaveNterminalMethionineCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        cleaveNterminalMethionineLbl.setText("Cleave N-terminal Methionine");

        javax.swing.GroupLayout databaseProcessingPanelLayout = new javax.swing.GroupLayout(databaseProcessingPanel);
        databaseProcessingPanel.setLayout(databaseProcessingPanelLayout);
        databaseProcessingPanelLayout.setHorizontalGroup(
            databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databaseProcessingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sequenceMappingLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cleaveNterminalMethionineLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cleaveNterminalMethionineCmb, 0, 109, Short.MAX_VALUE)
                    .addComponent(sequenceMappingCmb, 0, 109, Short.MAX_VALUE))
                .addContainerGap())
        );
        databaseProcessingPanelLayout.setVerticalGroup(
            databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, databaseProcessingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sequenceMappingLbl)
                    .addComponent(sequenceMappingCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cleaveNterminalMethionineLbl)
                    .addComponent(cleaveNterminalMethionineCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        semiEnzymaticParametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Semi-Enzymatic Mode Settings"));
        semiEnzymaticParametersPanel.setOpaque(false);

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
                .addContainerGap()
                .addComponent(peptideLengthJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minPepLengthTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(peptideLengthDividerLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );
        semiEnzymaticParametersPanelLayout.setVerticalGroup(
            semiEnzymaticParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(semiEnzymaticParametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(semiEnzymaticParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptideLengthJLabel)
                    .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideLengthDividerLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        iterativeSearchSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Iterative Search Settings"));
        iterativeSearchSettingsPanel.setOpaque(false);

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
                .addContainerGap()
                .addGroup(iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iterativeSequenceEvalueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iterativeSpectraEvalueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iterativeReplaceEvalueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iterativeReplaceEvalueTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(iterativeSequenceEvalueTxt, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(iterativeSpectraEvalueTxt, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        iterativeSearchSettingsPanelLayout.setVerticalGroup(
            iterativeSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(iterativeSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
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

        advancedSearchSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Advanced Search Settings"));
        advancedSearchSettingsPanel.setOpaque(false);
        advancedSearchSettingsPanel.setPreferredSize(new java.awt.Dimension(518, 143));

        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setText("Minimum Precursor Charge for Multiply Charged Fragments");

        minPrecChargeMultipleChargedFragmentsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecChargeMultipleChargedFragmentsTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecChargeMultipleChargedFragmentsTxtKeyReleased(evt);
            }
        });

        nIsotopesLbl.setText("Number of Isotopes");

        nIsotopesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nIsotopesTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nIsotopesTxtKeyReleased(evt);
            }
        });

        neutronLbl.setText("Mass Threshold to Consider Exact Neutron Mass");

        neutronTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        neutronTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                neutronTxtKeyReleased(evt);
            }
        });

        singlyChargedWindowWidthLbl.setText("Singly Charged Window Width");

        singlyChargedWindowWidthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        singlyChargedWindowWidthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                singlyChargedWindowWidthTxtKeyReleased(evt);
            }
        });

        doublyChargedWindowWidthLbl.setText("Doubly Charged Window Width");

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

        forwardIonsFirstLbl.setText("Search Forward Ions First");

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

        javax.swing.GroupLayout advancedSearchSettingsPanelLayout = new javax.swing.GroupLayout(advancedSearchSettingsPanel);
        advancedSearchSettingsPanel.setLayout(advancedSearchSettingsPanelLayout);
        advancedSearchSettingsPanelLayout.setHorizontalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minPrecChargeMultipleChargedFragmentsTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(nIsotopesLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nIsotopesTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(neutronLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(neutronTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(singlyChargedWindowWidthLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(singlyChargedWindowWidthTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(doublyChargedWindowWidthLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(doublyChargedWindowWidthTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(singlyChargedNPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(singlyChargedNpeaksTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(doublyChargedNPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(doublyChargedNpeaksTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(minAnnotatedMostIntensePeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minAnnotatedMostIntensePeaksTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(minAnnotatedPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minAnnotatedPeaksTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxLaddersLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxLaddersTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxFragmentChargeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(searchPositiveIonsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(searchPositiveIonsCmb, 0, 105, Short.MAX_VALUE)
                            .addComponent(maxFragmentChargeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(forwardIonsFirstLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(forwardIonsFirstCmb, 0, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(cTermIonsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cTermIonsCmb, 0, 105, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxFragmentsPerSeriesLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxFragmentsPerSeriesTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(correlationCorrectionScoreLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(correlationCorrectionScoreCmb, 0, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(consecutiveIonProbabilityLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(consecutiveIonProbabilityTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(nHitsPerSpectrumPerChargeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nHitsPerSpectrumPerChargeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)))
                .addContainerGap())
        );
        advancedSearchSettingsPanelLayout.setVerticalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel)
                    .addComponent(minPrecChargeMultipleChargedFragmentsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nIsotopesLbl)
                    .addComponent(nIsotopesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neutronLbl)
                    .addComponent(neutronTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(singlyChargedWindowWidthLbl)
                    .addComponent(singlyChargedWindowWidthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(doublyChargedWindowWidthLbl)
                    .addComponent(doublyChargedWindowWidthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(singlyChargedNPeaksLbl)
                    .addComponent(singlyChargedNpeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(doublyChargedNPeaksLbl)
                    .addComponent(doublyChargedNpeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minAnnotatedMostIntensePeaksLbl)
                    .addComponent(minAnnotatedMostIntensePeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minAnnotatedPeaksLbl)
                    .addComponent(minAnnotatedPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxLaddersLbl)
                    .addComponent(maxLaddersTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxFragmentChargeLbl)
                    .addComponent(maxFragmentChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPositiveIonsLbl)
                    .addComponent(searchPositiveIonsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(forwardIonsFirstLbl)
                    .addComponent(forwardIonsFirstCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cTermIonsLbl)
                    .addComponent(cTermIonsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxFragmentsPerSeriesLbl)
                    .addComponent(maxFragmentsPerSeriesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(correlationCorrectionScoreLbl)
                    .addComponent(correlationCorrectionScoreCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(consecutiveIonProbabilityLbl)
                    .addComponent(consecutiveIonProbabilityTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nHitsPerSpectrumPerChargeLbl)
                    .addComponent(nHitsPerSpectrumPerChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        outputParametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Settings"));
        outputParametersPanel.setOpaque(false);

        omssaOutputFormatComboBox.setModel(new DefaultComboBoxModel(OmssaParameters.getOmssaOutputTypes()));
        omssaOutputFormatComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                omssaOutputFormatComboBoxActionPerformed(evt);
            }
        });

        omssaOutputFormatLabel.setText("OMSSA Output Format");

        eValueLbl.setText("E-value Cutoff");

        hitListLbl.setText("Maximum HitList Length");

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
                .addContainerGap()
                .addGroup(outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(omssaOutputFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hitListLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eValueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hitlistTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                    .addComponent(maxEvalueTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                    .addComponent(omssaOutputFormatComboBox, 0, 105, Short.MAX_VALUE))
                .addContainerGap())
        );
        outputParametersPanelLayout.setVerticalGroup(
            outputParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outputParametersPanelLayout.createSequentialGroup()
                .addContainerGap()
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

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(openDialogHelpJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(iterativeSearchSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spectrumProcessingPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                            .addComponent(databaseProcessingPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(semiEnzymaticParametersPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(advancedSearchSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                            .addComponent(outputParametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(advancedSearchSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addComponent(spectrumProcessingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(databaseProcessingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(semiEnzymaticParametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iterativeSearchSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(outputParametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(openDialogHelpJButton)
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
                "SearchGUI - Help", 500, 50);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void omssaOutputFormatComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_omssaOutputFormatComboBoxActionPerformed

        if (((String) omssaOutputFormatComboBox.getSelectedItem()).equalsIgnoreCase("CSV") && this.isVisible()) {
            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(OmssaSettingsDialog.this, JOptionEditorPane.getJOptionEditorPane(
                            "Note that the OMSSA CSV format is not compatible with <a href=\"http://www.peptide-shaker.googlecode.com\">PeptideShaker</a>."),
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
    private void nIsotopesTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nIsotopesTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_nIsotopesTxtKeyReleased

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
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;
        lowIntensityLbl.setForeground(Color.BLACK);
        highIntensityLbl.setForeground(Color.BLACK);
        intensityIncrementLbl.setForeground(Color.BLACK);
        nPeaksLbl.setForeground(Color.BLACK);
        fractionChargeLbl.setForeground(Color.BLACK);
        minPrecPerSpectrumLbl.setForeground(Color.BLACK);
        peptideLengthJLabel.setForeground(Color.BLACK);
        maxPepLengthTxt.setForeground(Color.BLACK);
        eValueLbl.setForeground(Color.BLACK);
        hitListLbl.setForeground(Color.BLACK);
        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setForeground(Color.BLACK);
        nIsotopesLbl.setForeground(Color.BLACK);
        neutronLbl.setForeground(Color.BLACK);
        singlyChargedWindowWidthLbl.setForeground(Color.BLACK);
        doublyChargedWindowWidthLbl.setForeground(Color.BLACK);
        singlyChargedNPeaksLbl.setForeground(Color.BLACK);
        doublyChargedNPeaksLbl.setForeground(Color.BLACK);
        minAnnotatedMostIntensePeaksLbl.setForeground(Color.BLACK);
        minAnnotatedPeaksLbl.setForeground(Color.BLACK);
        maxLaddersLbl.setForeground(Color.BLACK);
        maxFragmentChargeLbl.setForeground(Color.BLACK);
        maxFragmentsPerSeriesLbl.setForeground(Color.BLACK);
        consecutiveIonProbabilityLbl.setForeground(Color.BLACK);
        iterativeSequenceEvalueLbl.setForeground(Color.BLACK);
        iterativeSpectraEvalueLbl.setForeground(Color.BLACK);
        iterativeReplaceEvalueLbl.setForeground(Color.BLACK);
        nHitsPerSpectrumPerChargeLbl.setForeground(Color.BLACK);

        lowIntensityLbl.setToolTipText(null);
        highIntensityLbl.setToolTipText(null);
        intensityIncrementLbl.setToolTipText(null);
        nPeaksLbl.setToolTipText(null);
        fractionChargeLbl.setToolTipText(null);
        minPrecPerSpectrumLbl.setToolTipText(null);
        peptideLengthJLabel.setToolTipText(null);
        maxPepLengthTxt.setToolTipText(null);
        eValueLbl.setToolTipText(null);
        hitListLbl.setToolTipText(null);
        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setToolTipText(null);
        nIsotopesLbl.setToolTipText(null);
        neutronLbl.setToolTipText(null);
        singlyChargedWindowWidthLbl.setToolTipText(null);
        doublyChargedWindowWidthLbl.setToolTipText(null);
        singlyChargedNPeaksLbl.setToolTipText(null);
        doublyChargedNPeaksLbl.setToolTipText(null);
        minAnnotatedMostIntensePeaksLbl.setToolTipText(null);
        minAnnotatedPeaksLbl.setToolTipText(null);
        maxLaddersLbl.setToolTipText(null);
        maxFragmentChargeLbl.setToolTipText(null);
        maxFragmentsPerSeriesLbl.setToolTipText(null);
        consecutiveIonProbabilityLbl.setToolTipText(null);
        iterativeSequenceEvalueLbl.setToolTipText(null);
        iterativeSpectraEvalueLbl.setToolTipText(null);
        iterativeReplaceEvalueLbl.setToolTipText(null);
        nHitsPerSpectrumPerChargeLbl.setToolTipText(null);

        // Low intensity cutoff
        if (lowIntensityTxt.getText() == null || lowIntensityTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a low intensity cutoff.",
                        "Low Intensity Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            lowIntensityLbl.setForeground(Color.RED);
            lowIntensityLbl.setToolTipText("Please select a low intensity cutoff limit");
        }
        double doubleInput = -1;
        try {
            doubleInput = new Double(lowIntensityTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the low intensity cutoff.",
                        "Low Intensity Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            lowIntensityLbl.setForeground(Color.RED);
            lowIntensityLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the low intensity cutoff.",
                        "Low Intensity Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            sequenceMappingLbl.setForeground(Color.RED);
            sequenceMappingLbl.setToolTipText("Please select a positive number");
        }

        // High intensity cutoff
        if (highIntensityTxt.getText() == null || highIntensityTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a high intensity cutoff.",
                        "High Intensity Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            highIntensityLbl.setForeground(Color.RED);
            highIntensityLbl.setToolTipText("Please select a high intensity cutoff limit");
        }
        doubleInput = -1;
        try {
            doubleInput = new Double(highIntensityTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the high intensity cutoff.",
                        "High Intensity Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            highIntensityLbl.setForeground(Color.RED);
            highIntensityLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the high intensity cutoff.",
                        "High Intensity Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            highIntensityLbl.setForeground(Color.RED);
            highIntensityLbl.setToolTipText("Please select a positive number");
        }

        // intensity cutoff increment
        if (intensityIncrementTxt.getText() == null || intensityIncrementTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify an intensity cutoff increment.",
                        "Intensity Cutoff Increment Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            intensityIncrementLbl.setForeground(Color.RED);
            intensityIncrementLbl.setToolTipText("Please select a high intensity cutoff limit");
        }
        doubleInput = -1;
        try {
            doubleInput = new Double(intensityIncrementTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the intensity cutoff increment.",
                        "Intensity Cutoff Increment Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            intensityIncrementLbl.setForeground(Color.RED);
            intensityIncrementLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the intensity cutoff increment.",
                        "Intensity Cutoff Increment Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            intensityIncrementLbl.setForeground(Color.RED);
            intensityIncrementLbl.setToolTipText("Please select a positive number");
        }

        // number of peaks
        if (nPeaksTxt.getText() == null || nPeaksTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number of peaks per spectrum.",
                        "Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nPeaksLbl.setForeground(Color.RED);
            nPeaksLbl.setToolTipText("Please select a high intensity cutoff limit");
        }
        int intInput = -1;
        try {
            intInput = new Integer(nPeaksTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks per spectrum.",
                        "Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nPeaksLbl.setForeground(Color.RED);
            nPeaksLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks per spectrum.",
                        "Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nPeaksLbl.setForeground(Color.RED);
            nPeaksLbl.setToolTipText("Please select a positive number");
        }

        // fraction of peaks for charge estimation
        if (fractionChargeTxt.getText() == null || fractionChargeTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify the fraction of peaks for charge estimation.",
                        "Fraction of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            fractionChargeLbl.setForeground(Color.RED);
            fractionChargeLbl.setToolTipText("Please select a high intensity cutoff limit");
        }
        doubleInput = -1;
        try {
            doubleInput = new Double(fractionChargeTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the fraction of peaks for charge estimation.",
                        "Fraction of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            fractionChargeLbl.setForeground(Color.RED);
            fractionChargeLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the fraction of peaks for charge estimation.",
                        "Fraction of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            fractionChargeLbl.setForeground(Color.RED);
            fractionChargeLbl.setToolTipText("Please select a positive number");
        }

        // minimum number of precursors per spectrum
        if (minPrecPerSpectrumTxt.getText() == null || minPrecPerSpectrumTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the number of precursor per spectrum.",
                        "Number of Precursor Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecPerSpectrumLbl.setForeground(Color.RED);
            minPrecPerSpectrumLbl.setToolTipText("Please select a high intensity cutoff limit");
        }
        intInput = -1;
        try {
            intInput = new Integer(minPrecPerSpectrumTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of precursor per spectrum.",
                        "Number of Precursor Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecPerSpectrumLbl.setForeground(Color.RED);
            minPrecPerSpectrumLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of precursor per spectrum.",
                        "Number of Precursor Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecPerSpectrumLbl.setForeground(Color.RED);
            minPrecPerSpectrumLbl.setToolTipText("Please select a positive number");
        }

        // Validate peptide sizes
        if (minPepLengthTxt.getText() != null && !minPepLengthTxt.getText().trim().equals("") || maxPepLengthTxt.getText() != null && !maxPepLengthTxt.getText().trim().equals("")) {

            // OK, see if it is an integer.
            int length = 0;
            try {
                length = Integer.parseInt(minPepLengthTxt.getText().trim());
            } catch (NumberFormatException nfe) {
                // Unparseable number!
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "You need to specify positive numbers for the peptide lengths.",
                            "Peptide Length Error", JOptionPane.WARNING_MESSAGE);
                }
                valid = false;
                peptideLengthJLabel.setForeground(Color.RED);
                peptideLengthJLabel.setToolTipText("Please select positive integers");
            }

            // And it should be greater than 0.
            if (length < 0) {
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "You need to specify positive integers for the peptide lengths.",
                            "Incorrect peptide lengths found!", JOptionPane.WARNING_MESSAGE);
                }
                valid = false;
                peptideLengthJLabel.setForeground(Color.RED);
                peptideLengthJLabel.setToolTipText("Please select positive integers");
            }

            // OK, see if it is an integer.
            length = 0;
            try {
                length = Integer.parseInt(maxPepLengthTxt.getText().trim());
            } catch (NumberFormatException nfe) {
                // Unparseable number!
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "You need to specify positive numbers for the peptide lengths.",
                            "Peptide Length Error", JOptionPane.WARNING_MESSAGE);
                }
                valid = false;
                peptideLengthJLabel.setForeground(Color.RED);
                peptideLengthJLabel.setToolTipText("Please select positive integers for the peptide length");
            }

            // And it should be greater than 0.
            if (length <= 0) {
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "You need to specify positive integers for the peptide lengths.",
                            "Incorrect peptide lengths found.", JOptionPane.WARNING_MESSAGE);
                }
                valid = false;
                peptideLengthJLabel.setForeground(Color.RED);
                peptideLengthJLabel.setToolTipText("Please select positive integers");
            }
        }

        // maximum reported e-value
        if (maxEvalueTxt.getText() == null || maxEvalueTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a value for the maximal e-value.",
                        "Maximum E-Value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a maximal e-value");
        }
        doubleInput = -1;
        try {
            doubleInput = new Double(maxEvalueTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal e-value.",
                        "Maximum E-Value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal e-value.",
                        "Maximum E-Value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a positive number");
        }

        // hitlist length
        if (hitlistTxt.getText() == null || hitlistTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the hitlist length.",
                        "Hitlist Length Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            hitListLbl.setForeground(Color.RED);
            hitListLbl.setToolTipText("Please select a hitlist length");
        }
        intInput = -1;
        try {
            intInput = new Integer(hitlistTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the hitlist length.",
                        "Hitlist Length Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            hitListLbl.setForeground(Color.RED);
            hitListLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the hitlist length.",
                        "Hitlist Length Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            hitListLbl.setForeground(Color.RED);
            hitListLbl.setToolTipText("Please select a positive number");
        }

        // hits per spectrum and per charge
        if (nHitsPerSpectrumPerChargeTxt.getText() == null || nHitsPerSpectrumPerChargeTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the number of hits per spectrum and per charge.",
                        "Hits per Spectrum and per Charge Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nHitsPerSpectrumPerChargeLbl.setForeground(Color.RED);
            nHitsPerSpectrumPerChargeLbl.setToolTipText("Please select a hitlist length");
        }
        intInput = -1;
        try {
            intInput = new Integer(nHitsPerSpectrumPerChargeTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of hits per spectrum and per charge.",
                        "Hits per Spectrum and per Charge Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nHitsPerSpectrumPerChargeLbl.setForeground(Color.RED);
            nHitsPerSpectrumPerChargeLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of hits per spectrum and per charge.",
                        "Hits per Spectrum and per Charge Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nHitsPerSpectrumPerChargeLbl.setForeground(Color.RED);
            nHitsPerSpectrumPerChargeLbl.setToolTipText("Please select a positive number");
        }

        // precursor charge for multiple charged fragment ions
        if (minPrecChargeMultipleChargedFragmentsTxt.getText() == null || minPrecChargeMultipleChargedFragmentsTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the minimal charge to consider multiply charged fragments.",
                        "Minimal Charge for Multiple Fragments Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setForeground(Color.RED);
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setToolTipText("Please select a minimal charge to consider multiply charged fragments");
        }
        intInput = -1;
        try {
            String debug = minPrecChargeMultipleChargedFragmentsTxt.getText().trim();
            intInput = new Integer(debug);
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal charge to consider multiply charged fragments.",
                        "Minimal Charge for Multiple Fragments Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setForeground(Color.RED);
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal charge to consider multiply charged fragments.",
                        "Minimal Charge for Multiple Fragments Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setForeground(Color.RED);
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setToolTipText("Please select a positive number");
        }

        // number of isotopes to consider
        if (nIsotopesTxt.getText() == null || nIsotopesTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the number of isotopes to consider.",
                        "Minimal Charge for Multiple Fragments Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nIsotopesLbl.setForeground(Color.RED);
            nIsotopesLbl.setToolTipText("Please select a number of isotopes to consider");
        }
        intInput = -1;
        try {
            intInput = new Integer(minPrecChargeMultipleChargedFragmentsTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of isotopes to consider.",
                        "Number of Isotopes Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nIsotopesLbl.setForeground(Color.RED);
            nIsotopesLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of isotopes to consider.",
                        "Number of Isotopes Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nIsotopesLbl.setForeground(Color.RED);
            nIsotopesLbl.setToolTipText("Please select a positive number");
        }

        // mass after which consider exact mass for the neutron
        if (maxEvalueTxt.getText() == null || maxEvalueTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a value for the mass after which the exact mass of a neutron is used.",
                        "Mass for Proton Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a mass after which the exact mass of a neutron is used");
        }
        doubleInput = -1;
        try {
            doubleInput = new Double(maxEvalueTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the mass after which the exact mass of a neutron is used.",
                        "Mass for Proton Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the mass after which the exact mass of a neutron is used.",
                        "Mass for Proton Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a positive number");
        }

        // single charge window
        if (singlyChargedWindowWidthTxt.getText() == null || singlyChargedWindowWidthTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a size for single charge windows.",
                        "Window Size Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            singlyChargedWindowWidthLbl.setForeground(Color.RED);
            singlyChargedWindowWidthLbl.setToolTipText("Please select a size for single charge windows");
        }
        intInput = -1;
        try {
            intInput = new Integer(singlyChargedWindowWidthTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the size for single charge windows.",
                        "Window Size Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            singlyChargedWindowWidthLbl.setForeground(Color.RED);
            singlyChargedWindowWidthLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the size for single charge windows.",
                        "Window Size Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            singlyChargedWindowWidthLbl.setForeground(Color.RED);
            singlyChargedWindowWidthLbl.setToolTipText("Please select a positive number");
        }

        // double charge window
        if (doublyChargedWindowWidthTxt.getText() == null || doublyChargedWindowWidthTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a size for double charge windows.",
                        "Window Size Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            doublyChargedWindowWidthLbl.setForeground(Color.RED);
            doublyChargedWindowWidthLbl.setToolTipText("Please select a size for double charge windows");
        }
        intInput = -1;
        try {
            intInput = new Integer(doublyChargedWindowWidthTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the size for double charge windows.",
                        "Window Size Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            doublyChargedWindowWidthLbl.setForeground(Color.RED);
            doublyChargedWindowWidthLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the size for double charge windows.",
                        "Window Size Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            doublyChargedWindowWidthLbl.setForeground(Color.RED);
            doublyChargedWindowWidthLbl.setToolTipText("Please select a positive number");
        }

        // single charge window number of peaks
        if (singlyChargedNpeaksTxt.getText() == null || singlyChargedNpeaksTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number of peaks for single charge windows.",
                        "Window Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            singlyChargedNPeaksLbl.setForeground(Color.RED);
            singlyChargedNPeaksLbl.setToolTipText("Please select a number of peaks for single charge windows");
        }
        intInput = -1;
        try {
            intInput = new Integer(singlyChargedNpeaksTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks for single charge windows.",
                        "Window Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            singlyChargedNPeaksLbl.setForeground(Color.RED);
            singlyChargedNPeaksLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks size for single charge windows.",
                        "Window Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            singlyChargedNPeaksLbl.setForeground(Color.RED);
            singlyChargedNPeaksLbl.setToolTipText("Please select a positive number");
        }

        // double charge window number of peaks
        if (doublyChargedNpeaksTxt.getText() == null || doublyChargedNpeaksTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number of peaks for double charge windows.",
                        "Window Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            doublyChargedNPeaksLbl.setForeground(Color.RED);
            doublyChargedNPeaksLbl.setToolTipText("Please select a number of peaks for double charge windows");
        }
        intInput = -1;
        try {
            intInput = new Integer(doublyChargedNpeaksTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks for double charge windows.",
                        "Window Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            doublyChargedNPeaksLbl.setForeground(Color.RED);
            doublyChargedNPeaksLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks size for double charge windows.",
                        "Window Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            doublyChargedNPeaksLbl.setForeground(Color.RED);
            doublyChargedNPeaksLbl.setToolTipText("Please select a positive number");
        }

        // minimal number of annotated peaks among the most intense ones
        if (minAnnotatedMostIntensePeaksTxt.getText() == null || minAnnotatedMostIntensePeaksTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the minimal number of annotated peaks among the most intense ones.",
                        "Number of Annotated Intense Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minAnnotatedMostIntensePeaksLbl.setForeground(Color.RED);
            minAnnotatedMostIntensePeaksLbl.setToolTipText("Please select a number of peaks for the minimal number of annotated peaks among the most intense ones");
        }
        intInput = -1;
        try {
            intInput = new Integer(minAnnotatedMostIntensePeaksTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal number of annotated peaks among the most intense ones.",
                        "Number of Annotated Intense Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minAnnotatedMostIntensePeaksLbl.setForeground(Color.RED);
            minAnnotatedMostIntensePeaksLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal number of annotated peaks among the most intense ones.",
                        "Number of Annotated Intense Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minAnnotatedMostIntensePeaksLbl.setForeground(Color.RED);
            minAnnotatedMostIntensePeaksLbl.setToolTipText("Please select a positive number");
        }

        // minimal number of annotated peaks
        if (minAnnotatedPeaksTxt.getText() == null || minAnnotatedPeaksTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the minimal number of annotated peaks.",
                        "Number of Annotated Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minAnnotatedPeaksLbl.setForeground(Color.RED);
            minAnnotatedPeaksLbl.setToolTipText("Please select a number of peaks for the minimal number of annotated peaks");
        }
        intInput = -1;
        try {
            intInput = new Integer(minAnnotatedPeaksTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal number of annotated peaks.",
                        "Number of Annotated Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minAnnotatedPeaksLbl.setForeground(Color.RED);
            minAnnotatedPeaksLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal number of annotated peaks.",
                        "Number of Annotated Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minAnnotatedPeaksLbl.setForeground(Color.RED);
            minAnnotatedPeaksLbl.setToolTipText("Please select a positive number");
        }

        // maximal m/z ladder size
        if (maxLaddersTxt.getText() == null || maxLaddersTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the maximal m/z ladder size.",
                        "Ladder Size Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxLaddersLbl.setForeground(Color.RED);
            maxLaddersLbl.setToolTipText("Please select a number of peaks for the maximal m/z ladder size");
        }
        intInput = -1;
        try {
            intInput = new Integer(maxLaddersTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal m/z ladder size.",
                        "Ladder Size Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxLaddersLbl.setForeground(Color.RED);
            maxLaddersLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal m/z ladder size.",
                        "Ladder Size Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxLaddersLbl.setForeground(Color.RED);
            maxLaddersLbl.setToolTipText("Please select a positive number");
        }

        // maximal fragment charge
        if (maxFragmentChargeTxt.getText() == null || maxFragmentChargeTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the maximal fragment charge.",
                        "Fragment Charge Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxFragmentChargeLbl.setForeground(Color.RED);
            maxFragmentChargeLbl.setToolTipText("Please select a number of peaks for the maximal fragment charge");
        }
        intInput = -1;
        try {
            intInput = new Integer(maxFragmentChargeTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal fragment charge.",
                        "Fragment Charge Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxFragmentChargeLbl.setForeground(Color.RED);
            maxFragmentChargeLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal fragment charge.",
                        "Fragment Charge Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxFragmentChargeLbl.setForeground(Color.RED);
            maxFragmentChargeLbl.setToolTipText("Please select a positive number");
        }

        // maximal fragment per series
        if (maxFragmentsPerSeriesTxt.getText() == null || maxFragmentsPerSeriesTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number for the maximal number of fragments per series.",
                        "Fragments per Series Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxFragmentsPerSeriesLbl.setForeground(Color.RED);
            maxFragmentsPerSeriesLbl.setToolTipText("Please select a number of peaks for the maximal number of fragments per series");
        }
        intInput = -1;
        try {
            intInput = new Integer(maxFragmentsPerSeriesTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal number of fragments per series.",
                        "Fragments per Series Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxFragmentsPerSeriesLbl.setForeground(Color.RED);
            maxFragmentsPerSeriesLbl.setToolTipText("Please select a positive number");
        }
        if (intInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal number of fragments per series.",
                        "Fragments per Series Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxFragmentsPerSeriesLbl.setForeground(Color.RED);
            maxFragmentsPerSeriesLbl.setToolTipText("Please select a positive number");
        }

        // mass after which consider exact mass for the neutron
        if (consecutiveIonProbabilityTxt.getText() == null || consecutiveIonProbabilityTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a value for the consecutive ion probability.",
                        "Consecutive Ion Probability Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            consecutiveIonProbabilityLbl.setForeground(Color.RED);
            consecutiveIonProbabilityLbl.setToolTipText("Please select a mass after which the consecutive ion probability");
        }
        doubleInput = -1;
        try {
            doubleInput = new Double(consecutiveIonProbabilityTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the consecutive ion probability.",
                        "Consecutive Ion Probability Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            consecutiveIonProbabilityLbl.setForeground(Color.RED);
            consecutiveIonProbabilityLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the consecutive ion probability.",
                        "Consecutive Ion Probability Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            consecutiveIonProbabilityLbl.setForeground(Color.RED);
            consecutiveIonProbabilityLbl.setToolTipText("Please select a positive number");
        }

        // e-value threshold for sequence in iterative search
        if (iterativeSequenceEvalueTxt.getText() == null || iterativeSequenceEvalueTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a value for the e-value cutoff to include sequences in the iterative search.",
                        "Iterative Search E-value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            iterativeSequenceEvalueLbl.setForeground(Color.RED);
            iterativeSequenceEvalueLbl.setToolTipText("Please select a mass after which the e-value cutoff to include sequences in the iterative search");
        }
        doubleInput = -1;
        try {
            doubleInput = new Double(iterativeSequenceEvalueTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff to include sequences in the iterative search.",
                        "Iterative Search E-value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            iterativeSequenceEvalueLbl.setForeground(Color.RED);
            iterativeSequenceEvalueLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff to include sequences in the iterative search.",
                        "Iterative Search E-value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            iterativeSequenceEvalueLbl.setForeground(Color.RED);
            iterativeSequenceEvalueLbl.setToolTipText("Please select a positive number");
        }

        // e-value threshold for spectrum in iterative search
        if (iterativeSpectraEvalueTxt.getText() == null || iterativeSpectraEvalueTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a value for the e-value cutoff to include spectra in the iterative search.",
                        "Iterative Search E-value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            iterativeSpectraEvalueLbl.setForeground(Color.RED);
            iterativeSpectraEvalueLbl.setToolTipText("Please select a mass after which the e-value cutoff to include spectra in the iterative search");
        }
        doubleInput = -1;
        try {
            doubleInput = new Double(iterativeSpectraEvalueTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff to include spectra in the iterative search.",
                        "Iterative Search E-value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            iterativeSpectraEvalueLbl.setForeground(Color.RED);
            iterativeSpectraEvalueLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff to include spectra in the iterative search.",
                        "Iterative Search E-value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            iterativeSpectraEvalueLbl.setForeground(Color.RED);
            iterativeSpectraEvalueLbl.setToolTipText("Please select a positive number");
        }

        // e-value threshold for replacing hits in iterative search
        if (iterativeReplaceEvalueTxt.getText() == null || iterativeReplaceEvalueTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a value for the e-value cutoff to replace hits in the iterative search.",
                        "Iterative Search E-value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            iterativeReplaceEvalueLbl.setForeground(Color.RED);
            iterativeReplaceEvalueLbl.setToolTipText("Please select a mass after which the e-value cutoff to replace hits in the iterative search");
        }
        doubleInput = -1;
        try {
            doubleInput = new Double(iterativeReplaceEvalueTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff to replace hits in the iterative search.",
                        "Iterative Search E-value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            iterativeReplaceEvalueLbl.setForeground(Color.RED);
            iterativeReplaceEvalueLbl.setToolTipText("Please select a positive number");
        }
        if (doubleInput < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff to replace hits in the iterative search.",
                        "Iterative Search E-value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            iterativeReplaceEvalueLbl.setForeground(Color.RED);
            iterativeReplaceEvalueLbl.setToolTipText("Please select a positive number");
        }

        return valid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedSearchSettingsPanel;
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
    private javax.swing.JLabel nIsotopesLbl;
    private javax.swing.JTextField nIsotopesTxt;
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
    private javax.swing.JPanel semiEnzymaticParametersPanel;
    private javax.swing.JComboBox sequenceMappingCmb;
    private javax.swing.JLabel sequenceMappingLbl;
    private javax.swing.JLabel singlyChargedNPeaksLbl;
    private javax.swing.JTextField singlyChargedNpeaksTxt;
    private javax.swing.JLabel singlyChargedWindowWidthLbl;
    private javax.swing.JTextField singlyChargedWindowWidthTxt;
    private javax.swing.JPanel spectrumProcessingPanel;
    // End of variables declaration//GEN-END:variables
}
