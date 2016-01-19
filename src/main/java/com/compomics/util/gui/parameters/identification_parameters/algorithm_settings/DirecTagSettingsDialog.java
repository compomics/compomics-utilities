package com.compomics.util.gui.parameters.identification_parameters.algorithm_settings;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.DirecTagParameters;
import com.compomics.util.gui.GuiUtilities;
import com.compomics.util.gui.parameters.identification_parameters.AlgorithmSettingsDialog;
import java.awt.Dialog;
import javax.swing.SwingConstants;

/**
 * Dialog for editing the DirecTag advanced settings.
 *
 * @author Harald Barsnes
 */
public class DirecTagSettingsDialog extends javax.swing.JDialog implements AlgorithmSettingsDialog {

    /**
     * The search parameters
     */
    private SearchParameters searchParameters;
    /**
     * True if the dialog was canceled by the user.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates a new DirecTagSettingsDialog with a frame as owner.
     *
     * @param parent the parent frame
     * @param searchParameters the search parameters
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public DirecTagSettingsDialog(java.awt.Frame parent, SearchParameters searchParameters, boolean editable) {
        super(parent, true);
        this.searchParameters = searchParameters;
        this.editable = editable;
        initComponents();
        setUpGUI();
        populateGUI(searchParameters);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates a new DirecTagSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parent the parent frame
     * @param searchParameters the search parameters
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public DirecTagSettingsDialog(Dialog owner, java.awt.Frame parent, SearchParameters searchParameters, boolean editable) {
        super(owner, true);
        this.searchParameters = searchParameters;
        this.editable = editable;
        initComponents();
        setUpGUI();
        populateGUI(searchParameters);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGUI() {
        
        duplicateSpectraPerChargeCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        
        tagLengthTextField.setEditable(editable);
        tagLengthTextField.setEnabled(editable);
        numVariableModsTextField.setEditable(editable);
        numVariableModsTextField.setEnabled(editable);
        numberOfChargeStatesTextField.setEditable(editable);
        numberOfChargeStatesTextField.setEnabled(editable);
        duplicateSpectraPerChargeCmb.setEnabled(editable);
        isotopeToleranceTextField.setEditable(editable);
        isotopeToleranceTextField.setEnabled(editable);
        deisptopingModeTextField.setEditable(editable);
        deisptopingModeTextField.setEnabled(editable);
        numberOfIntensityClassesTextField.setEditable(editable);
        numberOfIntensityClassesTextField.setEnabled(editable);
        outputSuffixTextField.setEditable(editable);
        outputSuffixTextField.setEnabled(editable);
        ticCutoffTextField.setEditable(editable);
        ticCutoffTextField.setEnabled(editable);
        complementToleranceTextField.setEditable(editable);
        complementToleranceTextField.setEnabled(editable);
        precursorAdjustmentStepTextField.setEditable(editable);
        precursorAdjustmentStepTextField.setEnabled(editable);
        minPrecursorAdjustmentTextField.setEditable(editable);
        minPrecursorAdjustmentTextField.setEnabled(editable);
        maxPrecursorAdjustmentTextField.setEditable(editable);
        maxPrecursorAdjustmentTextField.setEnabled(editable);
        intensityScoreWeightTextField.setEditable(editable);
        intensityScoreWeightTextField.setEnabled(editable);
        mzFidelityScoreWeightTextField.setEditable(editable);
        mzFidelityScoreWeightTextField.setEnabled(editable);
        complementScoreWeightTextField.setEditable(editable);
        complementScoreWeightTextField.setEnabled(editable);
        
    }

    /**
     * Populates the GUI using the given settings.
     * 
     * @param searchParameters the search parameters
     */
    private void populateGUI(SearchParameters searchParameters) {

        DirecTagParameters direcTagParameters = (DirecTagParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex());

        // DirecTag specific parameters
        tagLengthTextField.setText(String.valueOf(direcTagParameters.getTagLength()));
        numVariableModsTextField.setText(String.valueOf(direcTagParameters.getMaxDynamicMods()));
        numberOfChargeStatesTextField.setText(String.valueOf(direcTagParameters.getNumChargeStates()));
        if (direcTagParameters.isDuplicateSpectra()) {
            duplicateSpectraPerChargeCmb.setSelectedIndex(0);
        } else {
            duplicateSpectraPerChargeCmb.setSelectedIndex(1);
        }
        deisptopingModeTextField.setText(String.valueOf(direcTagParameters.getDeisotopingMode()));
        isotopeToleranceTextField.setText(String.valueOf(direcTagParameters.getIsotopeMzTolerance()));
        numberOfIntensityClassesTextField.setText(String.valueOf(direcTagParameters.getNumIntensityClasses()));
        outputSuffixTextField.setText(String.valueOf(direcTagParameters.getOutputSuffix()));
        maxPeakCountTextField.setText(String.valueOf(direcTagParameters.getMaxPeakCount()));
        ticCutoffTextField.setText(String.valueOf(direcTagParameters.getTicCutoffPercentage()));
        complementToleranceTextField.setText(String.valueOf(direcTagParameters.getComplementMzTolerance()));
        precursorAdjustmentStepTextField.setText(String.valueOf(direcTagParameters.getPrecursorAdjustmentStep()));
        minPrecursorAdjustmentTextField.setText(String.valueOf(direcTagParameters.getMinPrecursorAdjustment()));
        maxPrecursorAdjustmentTextField.setText(String.valueOf(direcTagParameters.getMaxPrecursorAdjustment()));
        intensityScoreWeightTextField.setText(String.valueOf(direcTagParameters.getIntensityScoreWeight()));
        mzFidelityScoreWeightTextField.setText(String.valueOf(direcTagParameters.getMzFidelityScoreWeight()));
        complementScoreWeightTextField.setText(String.valueOf(direcTagParameters.getComplementScoreWeight()));
        
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
        okButton = new javax.swing.JButton();
        directTagPanel = new javax.swing.JPanel();
        numberOfChargeStatesLabel = new javax.swing.JLabel();
        duplicateSpectraLabel = new javax.swing.JLabel();
        deisotopingModeLabel = new javax.swing.JLabel();
        isotopeToleranceLabel = new javax.swing.JLabel();
        complementToleranceLabel = new javax.swing.JLabel();
        tagLengthLabel = new javax.swing.JLabel();
        numVariableModsLabel = new javax.swing.JLabel();
        intensityScoreWeightLabel = new javax.swing.JLabel();
        mzFidelityScoreWeightLabel = new javax.swing.JLabel();
        complementScoreWeightLabel = new javax.swing.JLabel();
        numberOfChargeStatesTextField = new javax.swing.JTextField();
        ticCutoffLabel = new javax.swing.JLabel();
        maxPeakCountTextField = new javax.swing.JTextField();
        maxPeakCountLabel = new javax.swing.JLabel();
        numberOfIntensityClassesTextField = new javax.swing.JTextField();
        numberOfIntensityClassesLabel = new javax.swing.JLabel();
        minPrecursorAdjustmentLabel = new javax.swing.JLabel();
        minPrecursorAdjustmentTextField = new javax.swing.JTextField();
        maxPrecursorAdjustmentLabel = new javax.swing.JLabel();
        maxPrecursorAdjustmentTextField = new javax.swing.JTextField();
        precursorAdjustmentStepLabel = new javax.swing.JLabel();
        precursorAdjustmentStepTextField = new javax.swing.JTextField();
        outputSuffixLabel = new javax.swing.JLabel();
        outputSuffixTextField = new javax.swing.JTextField();
        duplicateSpectraPerChargeCmb = new javax.swing.JComboBox();
        deisptopingModeTextField = new javax.swing.JTextField();
        isotopeToleranceTextField = new javax.swing.JTextField();
        complementToleranceTextField = new javax.swing.JTextField();
        tagLengthTextField = new javax.swing.JTextField();
        numVariableModsTextField = new javax.swing.JTextField();
        intensityScoreWeightTextField = new javax.swing.JTextField();
        mzFidelityScoreWeightTextField = new javax.swing.JTextField();
        complementScoreWeightTextField = new javax.swing.JTextField();
        ticCutoffTextField = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        openDialogHelpJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DirecTag Advanced Settings");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        directTagPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("DirecTag Settings"));
        directTagPanel.setOpaque(false);

        numberOfChargeStatesLabel.setText("Number of Charge States");

        duplicateSpectraLabel.setText("Duplicate Spectra per Charge");

        deisotopingModeLabel.setText("Deisptoping Mode");

        isotopeToleranceLabel.setText("Isotope MZ Tolerance (Da)");

        complementToleranceLabel.setText("Complement MZ Tolerance (Da)");

        tagLengthLabel.setText("Tag Length");

        numVariableModsLabel.setText("Max Number of Variable PTMs");

        intensityScoreWeightLabel.setText("Intensity Score Weight");

        mzFidelityScoreWeightLabel.setText("MZ Fidelity Score Weight");

        complementScoreWeightLabel.setText("Complement Score Weight");

        numberOfChargeStatesTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfChargeStatesTextField.setText("3");
        numberOfChargeStatesTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                numberOfChargeStatesTextFieldKeyPressed(evt);
            }
        });

        ticCutoffLabel.setText("TIC Cutoff Percentage");

        maxPeakCountTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPeakCountTextField.setText("400");
        maxPeakCountTextField.setEnabled(false);
        maxPeakCountTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPeakCountTextFieldKeyReleased(evt);
            }
        });

        maxPeakCountLabel.setText("Max Peak Count");

        numberOfIntensityClassesTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfIntensityClassesTextField.setText("3");
        numberOfIntensityClassesTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numberOfIntensityClassesTextFieldKeyReleased(evt);
            }
        });

        numberOfIntensityClassesLabel.setText("Number of Intensity Classes");

        minPrecursorAdjustmentLabel.setText("Min Precursor Adjustment");

        minPrecursorAdjustmentTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecursorAdjustmentTextField.setText("-2.5");
        minPrecursorAdjustmentTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecursorAdjustmentTextFieldKeyReleased(evt);
            }
        });

        maxPrecursorAdjustmentLabel.setText("Max Precursor Adjustment");

        maxPrecursorAdjustmentTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPrecursorAdjustmentTextField.setText("2.5");
        maxPrecursorAdjustmentTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPrecursorAdjustmentTextFieldKeyReleased(evt);
            }
        });

        precursorAdjustmentStepLabel.setText("Precursor Adjustment Step");

        precursorAdjustmentStepTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        precursorAdjustmentStepTextField.setText("0.1");
        precursorAdjustmentStepTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                precursorAdjustmentStepTextFieldKeyReleased(evt);
            }
        });

        outputSuffixLabel.setText("Output Suffix");

        outputSuffixTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        outputSuffixTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                outputSuffixTextFieldKeyReleased(evt);
            }
        });

        duplicateSpectraPerChargeCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        deisptopingModeTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        deisptopingModeTextField.setText("0");
        deisptopingModeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                deisptopingModeTextFieldKeyReleased(evt);
            }
        });

        isotopeToleranceTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        isotopeToleranceTextField.setText("0.25");
        isotopeToleranceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                isotopeToleranceTextFieldKeyReleased(evt);
            }
        });

        complementToleranceTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        complementToleranceTextField.setText("0.5");
        complementToleranceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                complementToleranceTextFieldKeyReleased(evt);
            }
        });

        tagLengthTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tagLengthTextField.setText("3");
        tagLengthTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tagLengthTextFieldKeyReleased(evt);
            }
        });

        numVariableModsTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numVariableModsTextField.setText("2");
        numVariableModsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numVariableModsTextFieldKeyReleased(evt);
            }
        });

        intensityScoreWeightTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        intensityScoreWeightTextField.setText("1");
        intensityScoreWeightTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                intensityScoreWeightTextFieldKeyReleased(evt);
            }
        });

        mzFidelityScoreWeightTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        mzFidelityScoreWeightTextField.setText("1");
        mzFidelityScoreWeightTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mzFidelityScoreWeightTextFieldKeyReleased(evt);
            }
        });

        complementScoreWeightTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        complementScoreWeightTextField.setText("1");
        complementScoreWeightTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                complementScoreWeightTextFieldKeyReleased(evt);
            }
        });

        ticCutoffTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        ticCutoffTextField.setText("85");
        ticCutoffTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ticCutoffTextFieldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout directTagPanelLayout = new javax.swing.GroupLayout(directTagPanel);
        directTagPanel.setLayout(directTagPanelLayout);
        directTagPanelLayout.setHorizontalGroup(
            directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(directTagPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(directTagPanelLayout.createSequentialGroup()
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, directTagPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(duplicateSpectraLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(duplicateSpectraPerChargeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(52, 52, 52))
                            .addGroup(directTagPanelLayout.createSequentialGroup()
                                .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(directTagPanelLayout.createSequentialGroup()
                                        .addComponent(isotopeToleranceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(isotopeToleranceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, directTagPanelLayout.createSequentialGroup()
                                            .addComponent(numberOfIntensityClassesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(numberOfIntensityClassesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, directTagPanelLayout.createSequentialGroup()
                                            .addComponent(outputSuffixLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(outputSuffixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, directTagPanelLayout.createSequentialGroup()
                                            .addComponent(deisotopingModeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(deisptopingModeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(directTagPanelLayout.createSequentialGroup()
                                        .addComponent(maxPeakCountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(maxPeakCountTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(directTagPanelLayout.createSequentialGroup()
                                .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(minPrecursorAdjustmentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(maxPrecursorAdjustmentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(minPrecursorAdjustmentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(maxPrecursorAdjustmentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, directTagPanelLayout.createSequentialGroup()
                                .addComponent(complementScoreWeightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(complementScoreWeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, directTagPanelLayout.createSequentialGroup()
                                .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(mzFidelityScoreWeightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(intensityScoreWeightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(intensityScoreWeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mzFidelityScoreWeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(directTagPanelLayout.createSequentialGroup()
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(directTagPanelLayout.createSequentialGroup()
                                .addGap(198, 198, 198)
                                .addComponent(tagLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tagLengthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(directTagPanelLayout.createSequentialGroup()
                                .addComponent(numVariableModsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(numVariableModsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(directTagPanelLayout.createSequentialGroup()
                                .addComponent(numberOfChargeStatesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(numberOfChargeStatesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(directTagPanelLayout.createSequentialGroup()
                                .addComponent(ticCutoffLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(ticCutoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, directTagPanelLayout.createSequentialGroup()
                                .addComponent(precursorAdjustmentStepLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(precursorAdjustmentStepTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(directTagPanelLayout.createSequentialGroup()
                                .addComponent(complementToleranceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(complementToleranceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        directTagPanelLayout.setVerticalGroup(
            directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(directTagPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(directTagPanelLayout.createSequentialGroup()
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ticCutoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ticCutoffLabel))
                        .addGap(6, 6, 6)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(complementToleranceLabel)
                            .addComponent(complementToleranceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(precursorAdjustmentStepLabel)
                            .addComponent(precursorAdjustmentStepTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(minPrecursorAdjustmentLabel)
                            .addComponent(minPrecursorAdjustmentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(maxPrecursorAdjustmentLabel)
                            .addComponent(maxPrecursorAdjustmentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(intensityScoreWeightLabel)
                            .addComponent(intensityScoreWeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(directTagPanelLayout.createSequentialGroup()
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tagLengthLabel)
                            .addComponent(tagLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(numVariableModsLabel)
                            .addComponent(numVariableModsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(numberOfChargeStatesLabel)
                            .addComponent(numberOfChargeStatesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(duplicateSpectraLabel)
                            .addComponent(duplicateSpectraPerChargeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(isotopeToleranceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(isotopeToleranceLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(deisptopingModeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deisotopingModeLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(directTagPanelLayout.createSequentialGroup()
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(numberOfIntensityClassesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(numberOfIntensityClassesLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(outputSuffixLabel)
                            .addComponent(outputSuffixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(maxPeakCountLabel)
                            .addComponent(maxPeakCountTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(directTagPanelLayout.createSequentialGroup()
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mzFidelityScoreWeightLabel)
                            .addComponent(mzFidelityScoreWeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(directTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(complementScoreWeightLabel)
                            .addComponent(complementScoreWeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        openDialogHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        openDialogHelpJButton.setToolTipText("Open the DirecTag web page");
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

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(directTagPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(openDialogHelpJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(directTagPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(openDialogHelpJButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void numberOfChargeStatesTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numberOfChargeStatesTextFieldKeyPressed
        validateParametersInput(false);
    }//GEN-LAST:event_numberOfChargeStatesTextFieldKeyPressed

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void maxPeakCountTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPeakCountTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_maxPeakCountTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void numberOfIntensityClassesTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numberOfIntensityClassesTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_numberOfIntensityClassesTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void minPrecursorAdjustmentTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecursorAdjustmentTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_minPrecursorAdjustmentTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void maxPrecursorAdjustmentTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPrecursorAdjustmentTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_maxPrecursorAdjustmentTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void precursorAdjustmentStepTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_precursorAdjustmentStepTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_precursorAdjustmentStepTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void outputSuffixTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_outputSuffixTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_outputSuffixTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void deisptopingModeTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_deisptopingModeTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_deisptopingModeTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void isotopeToleranceTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_isotopeToleranceTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_isotopeToleranceTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void complementToleranceTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_complementToleranceTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_complementToleranceTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void tagLengthTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tagLengthTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_tagLengthTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void numVariableModsTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numVariableModsTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_numVariableModsTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void intensityScoreWeightTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_intensityScoreWeightTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_intensityScoreWeightTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void mzFidelityScoreWeightTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mzFidelityScoreWeightTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_mzFidelityScoreWeightTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void complementScoreWeightTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_complementScoreWeightTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_complementScoreWeightTextFieldKeyReleased

    /**
     * Validate the settings.
     *
     * @param evt
     */
    private void ticCutoffTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ticCutoffTextFieldKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_ticCutoffTextFieldKeyReleased

    /**
     * Save the settings and close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        boolean valid = validateParametersInput(true);

        if (valid) {
            setVisible(false);
        }
    }//GEN-LAST:event_okButtonActionPerformed

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
     * Change the cursor into a hand cursor.
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
     * Open the DirecTag web page.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://fenchurch.mc.vanderbilt.edu/bumbershoot/directag/");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

    /**
     * Close the dialog without saving the settings.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel complementScoreWeightLabel;
    private javax.swing.JTextField complementScoreWeightTextField;
    private javax.swing.JLabel complementToleranceLabel;
    private javax.swing.JTextField complementToleranceTextField;
    private javax.swing.JLabel deisotopingModeLabel;
    private javax.swing.JTextField deisptopingModeTextField;
    private javax.swing.JPanel directTagPanel;
    private javax.swing.JLabel duplicateSpectraLabel;
    private javax.swing.JComboBox duplicateSpectraPerChargeCmb;
    private javax.swing.JLabel intensityScoreWeightLabel;
    private javax.swing.JTextField intensityScoreWeightTextField;
    private javax.swing.JLabel isotopeToleranceLabel;
    private javax.swing.JTextField isotopeToleranceTextField;
    private javax.swing.JLabel maxPeakCountLabel;
    private javax.swing.JTextField maxPeakCountTextField;
    private javax.swing.JLabel maxPrecursorAdjustmentLabel;
    private javax.swing.JTextField maxPrecursorAdjustmentTextField;
    private javax.swing.JLabel minPrecursorAdjustmentLabel;
    private javax.swing.JTextField minPrecursorAdjustmentTextField;
    private javax.swing.JLabel mzFidelityScoreWeightLabel;
    private javax.swing.JTextField mzFidelityScoreWeightTextField;
    private javax.swing.JLabel numVariableModsLabel;
    private javax.swing.JTextField numVariableModsTextField;
    private javax.swing.JLabel numberOfChargeStatesLabel;
    private javax.swing.JTextField numberOfChargeStatesTextField;
    private javax.swing.JLabel numberOfIntensityClassesLabel;
    private javax.swing.JTextField numberOfIntensityClassesTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JLabel outputSuffixLabel;
    private javax.swing.JTextField outputSuffixTextField;
    private javax.swing.JLabel precursorAdjustmentStepLabel;
    private javax.swing.JTextField precursorAdjustmentStepTextField;
    private javax.swing.JLabel tagLengthLabel;
    private javax.swing.JTextField tagLengthTextField;
    private javax.swing.JLabel ticCutoffLabel;
    private javax.swing.JTextField ticCutoffTextField;
    // End of variables declaration//GEN-END:variables

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateParametersInput(boolean showMessage) {

        boolean valid = true;

        valid = GuiUtilities.validatePositiveIntegerInput(this, tagLengthLabel, tagLengthTextField, "tag length", "Tag Length Error", true, showMessage, valid);
        valid = GuiUtilities.validatePositiveIntegerInput(this, numVariableModsLabel, numVariableModsTextField, "number of variable modifications", "Variable Modifications Error", true, showMessage, valid);
        valid = GuiUtilities.validatePositiveIntegerInput(this, numberOfChargeStatesLabel, numberOfChargeStatesTextField, "number of charge states", "Charge States Error", true, showMessage, valid);
        valid = GuiUtilities.validatePositiveIntegerInput(this, deisotopingModeLabel, deisptopingModeTextField, "deisotoping mode", "Deisotoping Mode Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, isotopeToleranceLabel, isotopeToleranceTextField, "isotope tolerance", "Isotope Tolerance Error", true, showMessage, valid);
        valid = GuiUtilities.validatePositiveIntegerInput(this, numberOfIntensityClassesLabel, numberOfIntensityClassesTextField, "number of intensity classes", "Intensity Classes Error", true, showMessage, valid);
        valid = GuiUtilities.validatePositiveIntegerInput(this, maxPeakCountLabel, maxPeakCountTextField, "maximum peak count", "Max Peak Count Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, ticCutoffLabel, ticCutoffTextField, "TIC cutoff", "TIC Cutoff Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, complementToleranceLabel, complementToleranceTextField, "complement tolerance", "Complement Tolerance Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, precursorAdjustmentStepLabel, precursorAdjustmentStepTextField, "precursor adjustment step", "Precursor Adjustment Step Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, minPrecursorAdjustmentLabel, minPrecursorAdjustmentTextField, "minimum precursor adjustment", "Minimum Precursor Adjustment Error", false, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, maxPrecursorAdjustmentLabel, maxPrecursorAdjustmentTextField, "maximum precursor adjustment", "Maximum Precursor Adjustment Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, intensityScoreWeightLabel, intensityScoreWeightTextField, "intensity score weight", "Intensity Score Waight Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, mzFidelityScoreWeightLabel, mzFidelityScoreWeightTextField, "mz fidelity score weight", "MZ Fidelity Score Weight Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, complementScoreWeightLabel, complementScoreWeightTextField, "complement score weight", "Complement Score Weight Error", true, showMessage, valid);

        okButton.setEnabled(valid);
        return valid;
    }

    /**
     * Returns the search parameters as set in the GUI.
     *
     * @return the search parameters as set in the GUI
     */
    public SearchParameters getSearchParametersFromGUI() {

        SearchParameters tempSearchParameters = new SearchParameters(searchParameters);
        tempSearchParameters.setEnzyme(searchParameters.getEnzyme());
        tempSearchParameters.setFragmentIonAccuracy(searchParameters.getFragmentIonAccuracy());
        tempSearchParameters.setFragmentAccuracyType(searchParameters.getFragmentAccuracyType());
        tempSearchParameters.setPrecursorAccuracy(searchParameters.getPrecursorAccuracy());
        tempSearchParameters.setPrecursorAccuracyType(searchParameters.getPrecursorAccuracyType());
        tempSearchParameters.setPtmSettings(searchParameters.getPtmSettings());

        DirecTagParameters direcTagParameters = getDirecTagParameters();
        
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.direcTag.getIndex(), direcTagParameters);

        return tempSearchParameters;
    }
    
    /**
     * Returns the DirecTag parameters as set by the user.
     * 
     * @return the DirecTag parameters
     */
    public DirecTagParameters getDirecTagParameters() {
        
        DirecTagParameters direcTagParameters = new DirecTagParameters();
        direcTagParameters.setTagLength(Integer.parseInt(tagLengthTextField.getText()));
        direcTagParameters.setMaxDynamicMods(Integer.parseInt(numVariableModsTextField.getText()));
        direcTagParameters.setNumChargeStates(Integer.parseInt(numberOfChargeStatesTextField.getText()));
        direcTagParameters.setDuplicateSpectra(duplicateSpectraPerChargeCmb.getSelectedIndex() == 0);
        direcTagParameters.setDeisotopingMode(Integer.parseInt(deisptopingModeTextField.getText()));
        direcTagParameters.setIsotopeMzTolerance(Double.parseDouble(isotopeToleranceTextField.getText()));
        direcTagParameters.setNumIntensityClasses(Integer.parseInt(numberOfIntensityClassesTextField.getText()));
        direcTagParameters.setOutputSuffix(outputSuffixTextField.getText());
        direcTagParameters.setMaxPeakCount(Integer.parseInt(maxPeakCountTextField.getText()));
        direcTagParameters.setTicCutoffPercentage(Double.parseDouble(ticCutoffTextField.getText()));
        direcTagParameters.setComplementMzTolerance(Double.parseDouble(complementToleranceTextField.getText()));
        direcTagParameters.setPrecursorAdjustmentStep(Double.parseDouble(precursorAdjustmentStepTextField.getText()));
        direcTagParameters.setMinPrecursorAdjustment(Double.parseDouble(minPrecursorAdjustmentTextField.getText()));
        direcTagParameters.setMaxPrecursorAdjustment(Double.parseDouble(maxPrecursorAdjustmentTextField.getText()));
        direcTagParameters.setIntensityScoreWeight(Double.parseDouble(intensityScoreWeightTextField.getText()));
        direcTagParameters.setMzFidelityScoreWeight(Double.parseDouble(mzFidelityScoreWeightTextField.getText()));
        direcTagParameters.setComplementScoreWeight(Double.parseDouble(complementScoreWeightTextField.getText()));

        direcTagParameters.setMaxTagCount(((DirecTagParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex())).getMaxTagCount());
        direcTagParameters.setAdjustPrecursorMass(((DirecTagParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex())).isAdjustPrecursorMass());
        direcTagParameters.setUseChargeStateFromMS(((DirecTagParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex())).isUseChargeStateFromMS());
        
        return direcTagParameters;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }
    
    @Override
    public IdentificationAlgorithmParameter getParameters() {
        return getDirecTagParameters();
    }
}
