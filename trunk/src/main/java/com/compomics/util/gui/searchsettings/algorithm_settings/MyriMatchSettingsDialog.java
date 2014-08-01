package com.compomics.util.gui.searchsettings.algorithm_settings;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.identification.identification_parameters.MyriMatchParameters;
import com.compomics.util.gui.GuiUtilities;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * Dialog for the MyriMatch specific settings.
 *
 * @author Harald Barsnes
 */
public class MyriMatchSettingsDialog extends javax.swing.JDialog {

    /**
     * The MyriMatch parameters class containing the information to display.
     */
    private MyriMatchParameters myriMatchParameters;
    /**
     * Boolean indicating whether the used canceled the editing.
     */
    private boolean cancelled = false;

    /**
     * Creates new form MyriMatchSettingsDialog.
     *
     * @param parent the parent frame
     * @param myriMatchParameters the MyriMatch parameters
     */
    public MyriMatchSettingsDialog(java.awt.Frame parent, MyriMatchParameters myriMatchParameters) {
        super(parent, true);
        this.myriMatchParameters = myriMatchParameters;
        initComponents();
        setUpGUI();
        fillGUI();
        validateInput(false);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {
        useSmartPlusThreeModelCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        computeXCorrCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        terminiCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentationMethodCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
    }

    /**
     * Fills the GUI with the information contained in the MyriMatch settings
     * object.
     */
    private void fillGUI() {

        if (myriMatchParameters.getMinPeptideLength() != null) {
            minPepLengthTxt.setText(myriMatchParameters.getMinPeptideLength() + "");
        }
        if (myriMatchParameters.getMaxPeptideLength() != null) {
            maxPepLengthTxt.setText(myriMatchParameters.getMaxPeptideLength() + "");
        }

        if (myriMatchParameters.getMinPrecursorMass() != null) {
            minPrecursorMassTxt.setText(myriMatchParameters.getMinPrecursorMass() + "");
        }
        if (myriMatchParameters.getMaxPrecursorMass() != null) {
            maxPrecursorMassTxt.setText(myriMatchParameters.getMaxPrecursorMass() + "");
        }

        if (myriMatchParameters.getLowerIsotopeCorrectionRange() != null) {
            lowIsotopeErrorRangeTxt.setText(myriMatchParameters.getLowerIsotopeCorrectionRange() + "");
        }
        if (myriMatchParameters.getUpperIsotopeCorrectionRange() != null) {
            highIsotopeErrorRangeTxt.setText(myriMatchParameters.getUpperIsotopeCorrectionRange() + "");
        }

        if (myriMatchParameters.getNumberOfSpectrumMatches() != null) {
            numberMatchesTxt.setText(myriMatchParameters.getNumberOfSpectrumMatches() + "");
        }

        if (myriMatchParameters.getMaxDynamicMods() != null) {
            maxPtmsTxt.setText(myriMatchParameters.getMaxDynamicMods() + "");
        }

        fragmentationMethodCmb.setSelectedItem(myriMatchParameters.getFragmentationRule());

        if (myriMatchParameters.getMinTerminiCleavages() != null) {
            terminiCmb.setSelectedIndex(myriMatchParameters.getMinTerminiCleavages());
        }

        if (myriMatchParameters.getUseSmartPlusThreeModel()) {
            useSmartPlusThreeModelCmb.setSelectedIndex(0);
        } else {
            useSmartPlusThreeModelCmb.setSelectedIndex(1);
        }

        if (myriMatchParameters.getComputeXCorr()) {
            computeXCorrCmb.setSelectedIndex(0);
        } else {
            computeXCorrCmb.setSelectedIndex(1);
        }

        if (myriMatchParameters.getTicCutoffPercentage() != null) {
            ticCutoffPercentageTxt.setText(myriMatchParameters.getTicCutoffPercentage() + "");
        }

        if (myriMatchParameters.getNumIntensityClasses() != null) {
            numIntensityClassesTxt.setText(myriMatchParameters.getNumIntensityClasses() + "");
        }

        if (myriMatchParameters.getClassSizeMultiplier() != null) {
            classSizeMultiplierTxt.setText(myriMatchParameters.getClassSizeMultiplier() + "");
        }

        if (myriMatchParameters.getNumberOfBatches() != null) {
            numbBatchesTxt.setText(myriMatchParameters.getNumberOfBatches() + "");
        }

        if (myriMatchParameters.getMaxPeakCount() != null) {
            maxPeakCountTxt.setText(myriMatchParameters.getMaxPeakCount() + "");
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
     * Returns the user selection as MyriMatch parameters object.
     *
     * @return the user selection
     */
    public MyriMatchParameters getInput() {

        MyriMatchParameters result = new MyriMatchParameters();

        String input = minPepLengthTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPeptideLength(new Integer(input));
        }
        input = maxPepLengthTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxPeptideLength(new Integer(input));
        }

        input = minPrecursorMassTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPrecursorMass(new Double(input));
        }
        input = maxPrecursorMassTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxPrecursorMass(new Double(input));
        }

        input = lowIsotopeErrorRangeTxt.getText().trim();
        if (!input.equals("")) {
            result.setLowerIsotopeCorrectionRange(new Integer(input));
        }
        input = highIsotopeErrorRangeTxt.getText().trim();
        if (!input.equals("")) {
            result.setUpperIsotopeCorrectionRange(new Integer(input));
        }

        input = numberMatchesTxt.getText().trim();
        if (!input.equals("")) {
            result.setNumberOfSpectrumMatches(new Integer(input));
        }

        input = maxPtmsTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxDynamicMods(new Integer(input));
        }

        result.setFragmentationRule((String) fragmentationMethodCmb.getSelectedItem());
        result.setMinTerminiCleavages(terminiCmb.getSelectedIndex());
        result.setUseSmartPlusThreeModel(useSmartPlusThreeModelCmb.getSelectedIndex() == 0);
        result.setComputeXCorr(computeXCorrCmb.getSelectedIndex() == 0);

        input = ticCutoffPercentageTxt.getText().trim();
        if (!input.equals("")) {
            result.setTicCutoffPercentage(new Double(input));
        }

        input = numIntensityClassesTxt.getText().trim();
        if (!input.equals("")) {
            result.setNumIntensityClasses(new Integer(input));
        }

        input = classSizeMultiplierTxt.getText().trim();
        if (!input.equals("")) {
            result.setClassSizeMultiplier(new Integer(input));
        }

        input = numbBatchesTxt.getText().trim();
        if (!input.equals("")) {
            result.setNumberOfBatches(new Integer(input));
        }

        input = maxPeakCountTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxPeakCount(new Integer(input));
        }

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
        useSmartPlusThreeModelCmb = new javax.swing.JComboBox();
        useSmartPlusThreeModelLabel = new javax.swing.JLabel();
        minPepLengthTxt = new javax.swing.JTextField();
        peptideLengthDividerLabel = new javax.swing.JLabel();
        maxPepLengthTxt = new javax.swing.JTextField();
        peptideLengthLabel = new javax.swing.JLabel();
        numberMatchesLabel = new javax.swing.JLabel();
        numberMatchesTxt = new javax.swing.JTextField();
        computeXCorrlLabel = new javax.swing.JLabel();
        computeXCorrCmb = new javax.swing.JComboBox();
        isotopeErrorRangeLabel = new javax.swing.JLabel();
        lowIsotopeErrorRangeTxt = new javax.swing.JTextField();
        highIsotopeErrorRangeTxt = new javax.swing.JTextField();
        isotopeErrorRangeDividerLabel = new javax.swing.JLabel();
        numberTerminiLabel = new javax.swing.JLabel();
        maxPtmsLabel = new javax.swing.JLabel();
        maxPtmsTxt = new javax.swing.JTextField();
        terminiCmb = new javax.swing.JComboBox();
        precursorMassLabel = new javax.swing.JLabel();
        minPrecursorMassTxt = new javax.swing.JTextField();
        precursorMassDividerLabel = new javax.swing.JLabel();
        maxPrecursorMassTxt = new javax.swing.JTextField();
        ticCutoffPercentageLabel = new javax.swing.JLabel();
        ticCutoffPercentageTxt = new javax.swing.JTextField();
        numIntensityClassesLabel = new javax.swing.JLabel();
        numIntensityClassesTxt = new javax.swing.JTextField();
        classSizeMultiplierLabel = new javax.swing.JLabel();
        classSizeMultiplierTxt = new javax.swing.JTextField();
        numbBatchesLabel = new javax.swing.JLabel();
        numbBatchesTxt = new javax.swing.JTextField();
        fragmentationMethodLabel = new javax.swing.JLabel();
        fragmentationMethodCmb = new javax.swing.JComboBox();
        maxPeakCountLabel = new javax.swing.JLabel();
        maxPeakCountTxt = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        openDialogHelpJButton = new javax.swing.JButton();
        advancedSettingsWarningLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Advanced MyriMatch Settings");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        advancedSearchSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Settings"));
        advancedSearchSettingsPanel.setOpaque(false);

        useSmartPlusThreeModelCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        useSmartPlusThreeModelLabel.setText("Use Smart Plus Thee Model");

        minPepLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPepLengthTxt.setText("6");
        minPepLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPepLengthTxtKeyReleased(evt);
            }
        });

        peptideLengthDividerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        peptideLengthDividerLabel.setText("-");

        maxPepLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPepLengthTxt.setText("30");
        maxPepLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPepLengthTxtKeyReleased(evt);
            }
        });

        peptideLengthLabel.setText("Peptide Length (min - max)");

        numberMatchesLabel.setText("Number of Spectrum Matches");

        numberMatchesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberMatchesTxt.setText("1");
        numberMatchesTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numberMatchesTxtKeyReleased(evt);
            }
        });

        computeXCorrlLabel.setText("Compute XCorr");

        computeXCorrCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        computeXCorrCmb.setSelectedIndex(1);

        isotopeErrorRangeLabel.setText("Isotope Error Range");

        lowIsotopeErrorRangeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lowIsotopeErrorRangeTxt.setText("0");
        lowIsotopeErrorRangeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lowIsotopeErrorRangeTxtKeyReleased(evt);
            }
        });

        highIsotopeErrorRangeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        highIsotopeErrorRangeTxt.setText("1");
        highIsotopeErrorRangeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                highIsotopeErrorRangeTxtKeyReleased(evt);
            }
        });

        isotopeErrorRangeDividerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        isotopeErrorRangeDividerLabel.setText("-");

        numberTerminiLabel.setText("Enzymatic Terminals");

        maxPtmsLabel.setText("Max Variable PTMs per Peptide");

        maxPtmsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPtmsTxt.setText("2");
        maxPtmsTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPtmsTxtKeyReleased(evt);
            }
        });

        terminiCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None Required", "At Least One", "Both" }));
        terminiCmb.setSelectedIndex(2);

        precursorMassLabel.setText("Precursor Mass (min - max)");

        minPrecursorMassTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecursorMassTxt.setText("0");
        minPrecursorMassTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecursorMassTxtKeyReleased(evt);
            }
        });

        precursorMassDividerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        precursorMassDividerLabel.setText("-");

        maxPrecursorMassTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPrecursorMassTxt.setText("10000");
        maxPrecursorMassTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPrecursorMassTxtKeyReleased(evt);
            }
        });

        ticCutoffPercentageLabel.setText("TIC Cutoff Percentage (0.0-1.0)");

        ticCutoffPercentageTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        ticCutoffPercentageTxt.setText("0.98");
        ticCutoffPercentageTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ticCutoffPercentageTxtKeyReleased(evt);
            }
        });

        numIntensityClassesLabel.setText("Number of Intensity Classes");

        numIntensityClassesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numIntensityClassesTxt.setText("3");
        numIntensityClassesTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numIntensityClassesTxtKeyReleased(evt);
            }
        });

        classSizeMultiplierLabel.setText("Class Size Multiplier");

        classSizeMultiplierTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        classSizeMultiplierTxt.setText("2");
        classSizeMultiplierTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                classSizeMultiplierTxtKeyReleased(evt);
            }
        });

        numbBatchesLabel.setText("Number of Batches");

        numbBatchesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numbBatchesTxt.setText("50");
        numbBatchesTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numbBatchesTxtKeyReleased(evt);
            }
        });

        fragmentationMethodLabel.setText("Fragmentation Method");

        fragmentationMethodCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "CID", "HCD", "ETD" }));

        maxPeakCountLabel.setText("Max Peak Count");

        maxPeakCountTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPeakCountTxt.setText("100");
        maxPeakCountTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPeakCountTxtKeyReleased(evt);
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
                        .addComponent(useSmartPlusThreeModelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(useSmartPlusThreeModelCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(computeXCorrlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(computeXCorrCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(ticCutoffPercentageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ticCutoffPercentageTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(numIntensityClassesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numIntensityClassesTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(classSizeMultiplierLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(classSizeMultiplierTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(numbBatchesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numbBatchesTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(numberMatchesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numberMatchesTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxPtmsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPtmsTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(numberTerminiLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(terminiCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(isotopeErrorRangeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lowIsotopeErrorRangeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(isotopeErrorRangeDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(highIsotopeErrorRangeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                                .addComponent(peptideLengthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(peptideLengthDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                                .addComponent(precursorMassLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minPrecursorMassTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(precursorMassDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxPrecursorMassTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(fragmentationMethodLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fragmentationMethodCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxPeakCountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPeakCountTxt)))
                .addContainerGap())
        );

        advancedSearchSettingsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {highIsotopeErrorRangeTxt, lowIsotopeErrorRangeTxt, maxPepLengthTxt, maxPrecursorMassTxt, minPepLengthTxt, minPrecursorMassTxt});

        advancedSearchSettingsPanelLayout.setVerticalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideLengthDividerLabel)
                    .addComponent(peptideLengthLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPrecursorMassTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPrecursorMassTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorMassDividerLabel)
                    .addComponent(precursorMassLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lowIsotopeErrorRangeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(highIsotopeErrorRangeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(isotopeErrorRangeDividerLabel)
                    .addComponent(isotopeErrorRangeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberMatchesLabel)
                    .addComponent(numberMatchesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxPtmsLabel)
                    .addComponent(maxPtmsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fragmentationMethodCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentationMethodLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberTerminiLabel)
                    .addComponent(terminiCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useSmartPlusThreeModelLabel)
                    .addComponent(useSmartPlusThreeModelCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(computeXCorrlLabel)
                    .addComponent(computeXCorrCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ticCutoffPercentageLabel)
                    .addComponent(ticCutoffPercentageTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numIntensityClassesLabel)
                    .addComponent(numIntensityClassesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classSizeMultiplierLabel)
                    .addComponent(classSizeMultiplierTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numbBatchesLabel)
                    .addComponent(numbBatchesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxPeakCountLabel)
                    .addComponent(maxPeakCountTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        advancedSettingsWarningLabel.setText("Click to open the MyriMatch help page.");

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
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
     * Open the MyriMatch help page.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://fenchurch.mc.vanderbilt.edu/bumbershoot/myrimatch/");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

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
    private void maxPepLengthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPepLengthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPepLengthTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void numberMatchesTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numberMatchesTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_numberMatchesTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void lowIsotopeErrorRangeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lowIsotopeErrorRangeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_lowIsotopeErrorRangeTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void highIsotopeErrorRangeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_highIsotopeErrorRangeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_highIsotopeErrorRangeTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxPtmsTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPtmsTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPtmsTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minPrecursorMassTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecursorMassTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPrecursorMassTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxPrecursorMassTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPrecursorMassTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPrecursorMassTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void ticCutoffPercentageTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ticCutoffPercentageTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_ticCutoffPercentageTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void numIntensityClassesTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numIntensityClassesTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_numIntensityClassesTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void classSizeMultiplierTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_classSizeMultiplierTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_classSizeMultiplierTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void numbBatchesTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numbBatchesTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_numbBatchesTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxPeakCountTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPeakCountTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPeakCountTxtKeyReleased

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;

        valid = GuiUtilities.validateIntegerInput(this, peptideLengthLabel, minPepLengthTxt, "minimum peptide length", "Peptide Length Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, peptideLengthLabel, maxPepLengthTxt, "maximum peptide length", "Peptide Length Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, precursorMassLabel, minPrecursorMassTxt, "minimum precursor mass", "Precursor Mass Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, precursorMassLabel, maxPrecursorMassTxt, "maximum precursor mass", "Precursor Mass Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, isotopeErrorRangeLabel, lowIsotopeErrorRangeTxt, "lower isotope range", "Isotope Range Error", false, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, isotopeErrorRangeLabel, highIsotopeErrorRangeTxt, "upper isotope range", "Isotope Range Error", false, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, numberMatchesLabel, numberMatchesTxt, "number of spectrum matches", "Number Spectrum Matches Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxPtmsLabel, maxPtmsTxt, "max number of PTMs per peptide", "Peptide PTM Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, ticCutoffPercentageLabel, ticCutoffPercentageTxt, "TIC cutoff precentage", "TIC Cutoff Percentage Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, numIntensityClassesLabel, numIntensityClassesTxt, "number of intensity classes", "Intensity Classes Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, classSizeMultiplierLabel, classSizeMultiplierTxt, "class size multiplier", "Class Size Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, numbBatchesLabel, numbBatchesTxt, "number of batches", "Number of Batches Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxPeakCountLabel, maxPeakCountTxt, "maximum peak count", "Max Peak Count Error", true, showMessage, valid);

        // isotope range: the low value should be lower than the high value
        try {
            int lowValue = Integer.parseInt(lowIsotopeErrorRangeTxt.getText().trim());
            int highValue = Integer.parseInt(highIsotopeErrorRangeTxt.getText().trim());

            if (lowValue > highValue) {
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "The lower range value has to be smaller than the upper range value.",
                            "Isotope Range Error", JOptionPane.WARNING_MESSAGE);
                }
                valid = false;
                isotopeErrorRangeLabel.setForeground(Color.RED);
                isotopeErrorRangeLabel.setToolTipText("Please select a valid range (upper <= higher)");
            }
        } catch (NumberFormatException e) {
            // ignore, handled above
        }

        // check that the tic cuttoff is between 0 and 1
        try {
            double temp = Double.parseDouble(ticCutoffPercentageTxt.getText().trim());

            if (temp < 0 || temp > 1) {
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "The TIC cutoff percentage has to be between 0.0 and 1.0.",
                            "TIC Cutoff Percentage Error", JOptionPane.WARNING_MESSAGE);
                }
                valid = false;
                ticCutoffPercentageLabel.setForeground(Color.RED);
                ticCutoffPercentageLabel.setToolTipText("Please select a valid TIC cutoff percentage [0.0-1.0]");
            }

        } catch (NumberFormatException e) {
            // ignore, handled above
        }

        okButton.setEnabled(valid);

        return valid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedSearchSettingsPanel;
    private javax.swing.JLabel advancedSettingsWarningLabel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JLabel classSizeMultiplierLabel;
    private javax.swing.JTextField classSizeMultiplierTxt;
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox computeXCorrCmb;
    private javax.swing.JLabel computeXCorrlLabel;
    private javax.swing.JComboBox fragmentationMethodCmb;
    private javax.swing.JLabel fragmentationMethodLabel;
    private javax.swing.JTextField highIsotopeErrorRangeTxt;
    private javax.swing.JLabel isotopeErrorRangeDividerLabel;
    private javax.swing.JLabel isotopeErrorRangeLabel;
    private javax.swing.JTextField lowIsotopeErrorRangeTxt;
    private javax.swing.JLabel maxPeakCountLabel;
    private javax.swing.JTextField maxPeakCountTxt;
    private javax.swing.JTextField maxPepLengthTxt;
    private javax.swing.JTextField maxPrecursorMassTxt;
    private javax.swing.JLabel maxPtmsLabel;
    private javax.swing.JTextField maxPtmsTxt;
    private javax.swing.JTextField minPepLengthTxt;
    private javax.swing.JTextField minPrecursorMassTxt;
    private javax.swing.JLabel numIntensityClassesLabel;
    private javax.swing.JTextField numIntensityClassesTxt;
    private javax.swing.JLabel numbBatchesLabel;
    private javax.swing.JTextField numbBatchesTxt;
    private javax.swing.JLabel numberMatchesLabel;
    private javax.swing.JTextField numberMatchesTxt;
    private javax.swing.JLabel numberTerminiLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JLabel peptideLengthDividerLabel;
    private javax.swing.JLabel peptideLengthLabel;
    private javax.swing.JLabel precursorMassDividerLabel;
    private javax.swing.JLabel precursorMassLabel;
    private javax.swing.JComboBox terminiCmb;
    private javax.swing.JLabel ticCutoffPercentageLabel;
    private javax.swing.JTextField ticCutoffPercentageTxt;
    private javax.swing.JComboBox useSmartPlusThreeModelCmb;
    private javax.swing.JLabel useSmartPlusThreeModelLabel;
    // End of variables declaration//GEN-END:variables
}
