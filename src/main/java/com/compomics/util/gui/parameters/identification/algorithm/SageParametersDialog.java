package com.compomics.util.gui.parameters.identification.algorithm;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;
import com.compomics.util.gui.GuiUtilities;
import java.awt.Dialog;
import javax.swing.SwingConstants;
import com.compomics.util.gui.parameters.identification.AlgorithmParametersDialog;
import com.compomics.util.parameters.identification.tool_specific.SageParameters;
import java.awt.Color;

/**
 * Dialog for the Sage specific settings.
 *
 * @author Harald Barsnes
 */
public class SageParametersDialog extends javax.swing.JDialog implements AlgorithmParametersDialog {

    /**
     * Boolean indicating whether the used canceled the editing.
     */
    private boolean cancelled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates new form SageSettingsDialog with a frame as owner.
     *
     * @param parent the parent frame
     * @param sageParameters the Sage parameters
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SageParametersDialog(java.awt.Frame parent, SageParameters sageParameters, boolean editable) {
        super(parent, true);
        this.editable = editable;
        initComponents();
        setUpGUI();
        populateGUI(sageParameters);
        validateInput(false);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates new form SageSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parent the parent frame
     * @param sageParameters the Sage parameters
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SageParametersDialog(Dialog owner, java.awt.Frame parent, SageParameters sageParameters, boolean editable) {
        super(owner, true);
        this.editable = editable;
        initComponents();
        setUpGUI();
        populateGUI(sageParameters);
        validateInput(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGUI() {

        generateDecoysCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        tmtTypeCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        tmtLevelCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        tmtSnCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        lfqCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        deisotopeCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        chimericSpectraCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        predictRtCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        wideWindowCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        lfqPeakScoringCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        lfqIntegrationCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        generateDecoysCmb.setEnabled(editable);
        tmtTypeCmb.setEnabled(editable);
        tmtLevelCmb.setEnabled(editable);
        tmtSnCmb.setEnabled(editable);
        lfqCmb.setEnabled(editable);
        deisotopeCmb.setEnabled(editable);
        chimericSpectraCmb.setEnabled(editable);
        predictRtCmb.setEnabled(editable);
        wideWindowCmb.setEnabled(editable);
        lfqPeakScoringCmb.setEnabled(editable);
        lfqIntegrationCmb.setEnabled(editable);

        bucketSizeTxt.setEditable(editable);
        bucketSizeTxt.setEnabled(editable);
        minPepLengthTxt.setEditable(editable);
        minPepLengthTxt.setEnabled(editable);
        maxPepLengthTxt.setEditable(editable);
        maxPepLengthTxt.setEnabled(editable);
        minFragmentMzTxt.setEditable(editable);
        minFragmentMzTxt.setEnabled(editable);
        maxFragmentMzTxt.setEditable(editable);
        maxFragmentMzTxt.setEnabled(editable);
        minPepLengthTxt.setEditable(editable);
        minPepLengthTxt.setEnabled(editable);
        maxPepLengthTxt.setEditable(editable);
        maxPepLengthTxt.setEnabled(editable);
        minIonIndexTxt.setEditable(editable);
        minIonIndexTxt.setEnabled(editable);
        maxVariableModsTxt.setEditable(editable);
        maxVariableModsTxt.setEnabled(editable);
        minNumberOfPeaksTxt.setEditable(editable);
        minNumberOfPeaksTxt.setEnabled(editable);
        maxNumberOfPeaksTxt.setEditable(editable);
        maxNumberOfPeaksTxt.setEnabled(editable);
        maxFragmentChargeTxt.setEditable(editable);
        maxFragmentChargeTxt.setEnabled(editable);
        numberOfPsmsPerSpectrumTxt.setEditable(editable);
        numberOfPsmsPerSpectrumTxt.setEnabled(editable);
        batchSizeTxt.setEditable(editable);
        batchSizeTxt.setEnabled(editable);
        lfqSpectralAngleTxt.setEditable(editable);
        lfqSpectralAngleTxt.setEnabled(editable);
        lfqPpmToleranceTxt.setEditable(editable);
        lfqPpmToleranceTxt.setEnabled(editable);

    }

    /**
     * Populates the GUI using the given settings.
     *
     * @param sageParameters the parameters to display
     */
    private void populateGUI(SageParameters sageParameters) {

        if (sageParameters.getBucketSize() != null) {
            bucketSizeTxt.setText(sageParameters.getBucketSize() + "");
        }

        if (sageParameters.getMinPeptideLength() != null) {
            minPepLengthTxt.setText(sageParameters.getMinPeptideLength() + "");
        }
        if (sageParameters.getMaxPeptideLength() != null) {
            maxPepLengthTxt.setText(sageParameters.getMaxPeptideLength() + "");
        }

        if (sageParameters.getMinFragmentMz() != null) {
            minFragmentMzTxt.setText(sageParameters.getMinFragmentMz() + "");
        }
        if (sageParameters.getMaxFragmentMz() != null) {
            maxFragmentMzTxt.setText(sageParameters.getMaxFragmentMz() + "");
        }

        if (sageParameters.getMinPeptideMass() != null) {
            minPeptideMassTxt.setText(sageParameters.getMinPeptideMass() + "");
        }
        if (sageParameters.getMaxPeptideMass() != null) {
            maxPeptideMassTxt.setText(sageParameters.getMaxPeptideMass() + "");
        }

        if (sageParameters.getMinIonIndex() != null) {
            minIonIndexTxt.setText(sageParameters.getMinIonIndex() + "");
        }

        if (sageParameters.getGenerateDecoys()) {
            generateDecoysCmb.setSelectedIndex(0);
        } else {
            generateDecoysCmb.setSelectedIndex(1);
        }

        if (sageParameters.getMaxVariableMods() != null) {
            maxVariableModsTxt.setText(sageParameters.getMaxVariableMods() + "");
        }

        if (sageParameters.getTmtType() != null) {
            tmtTypeCmb.setSelectedItem(sageParameters.getTmtType());
        } else {
            tmtTypeCmb.setSelectedItem("None");
        }

        if (sageParameters.getTmtLevel() != null) {
            tmtLevelCmb.setSelectedItem(sageParameters.getTmtLevel());
        } else {
            tmtLevelCmb.setSelectedItem("3");
        }

        if (sageParameters.getTmtSn()) {
            tmtSnCmb.setSelectedIndex(0);
        } else {
            tmtSnCmb.setSelectedIndex(1);
        }

        if (sageParameters.getPerformLfq()) {
            lfqCmb.setSelectedIndex(0);
        } else {
            lfqCmb.setSelectedIndex(1);
        }

        if (sageParameters.getLfqPeakScoring() != null) {
            lfqPeakScoringCmb.setSelectedItem(sageParameters.getLfqPeakScoring());
        } else {
            lfqPeakScoringCmb.setSelectedItem("Hybrid");
        }

        if (sageParameters.getLfqIntergration() != null) {
            lfqIntegrationCmb.setSelectedItem(sageParameters.getLfqIntergration());
        } else {
            lfqIntegrationCmb.setSelectedItem("Sum");
        }

        if (sageParameters.getLfqSpectralAngle() != null) {
            lfqSpectralAngleTxt.setText(sageParameters.getLfqSpectralAngle() + "");
        }

        if (sageParameters.getLfqPpmTolerance() != null) {
            lfqPpmToleranceTxt.setText(sageParameters.getLfqPpmTolerance() + "");
        }

        if (sageParameters.getDeisotope()) {
            deisotopeCmb.setSelectedIndex(0);
        } else {
            deisotopeCmb.setSelectedIndex(1);
        }

        if (sageParameters.getChimera()) {
            chimericSpectraCmb.setSelectedIndex(0);
        } else {
            chimericSpectraCmb.setSelectedIndex(1);
        }

        if (sageParameters.getWideWindow()) {
            wideWindowCmb.setSelectedIndex(0);
        } else {
            wideWindowCmb.setSelectedIndex(1);
        }

        if (sageParameters.getPredictRt()) {
            predictRtCmb.setSelectedIndex(0);
        } else {
            predictRtCmb.setSelectedIndex(1);
        }

        if (sageParameters.getMinPeaks() != null) {
            minNumberOfPeaksTxt.setText(sageParameters.getMinPeaks() + "");
        }
        if (sageParameters.getMaxPeaks() != null) {
            maxNumberOfPeaksTxt.setText(sageParameters.getMaxPeaks() + "");
        }

        if (sageParameters.getMinMatchedPeaks() != null) {
            minMatchedPeaksTxt.setText(sageParameters.getMinMatchedPeaks() + "");
        }

        if (sageParameters.getMaxFragmentCharge() != null) {
            maxFragmentChargeTxt.setText(sageParameters.getMaxFragmentCharge() + "");
        }

        if (sageParameters.getNumPsmsPerSpectrum() != null) {
            numberOfPsmsPerSpectrumTxt.setText(sageParameters.getNumPsmsPerSpectrum() + "");
        }

        if (sageParameters.getBatchSize() != null) {
            batchSizeTxt.setText(sageParameters.getBatchSize() + "");
        } else {
            batchSizeTxt.setText("");
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
     * Returns the user selection as Sage parameters object.
     *
     * @return the user selection
     */
    public SageParameters getInput() {

        SageParameters result = new SageParameters();

        String input = bucketSizeTxt.getText().trim();
        if (!input.equals("")) {
            result.setBucketSize(Integer.valueOf(input));
        }

        input = minPepLengthTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPeptideLength(Integer.parseInt(input));
        }
        input = maxPepLengthTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxPeptideLength(Integer.parseInt(input));
        }

        input = minFragmentMzTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinFragmentMz(Double.valueOf(input));
        }
        input = maxFragmentMzTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxFragmentMz(Double.valueOf(input));
        }

        input = minPeptideMassTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPeptideMass(Double.valueOf(input));
        }
        input = maxPeptideMassTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxPeptideMass(Double.valueOf(input));
        }

        input = minIonIndexTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinIonIndex(Integer.valueOf(input));
        }

        input = maxVariableModsTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxVariableMods(Integer.valueOf(input));
        }

        result.setGenerateDecoys(generateDecoysCmb.getSelectedIndex() == 0);

        if (tmtTypeCmb.getSelectedIndex() != 0) {
            result.setTmtType(((String) tmtTypeCmb.getSelectedItem()));
        } else {
            result.setTmtType(null);
        }

        result.setTmtLevel(Integer.valueOf((String) tmtLevelCmb.getSelectedItem()));
        result.setTmtSn(tmtSnCmb.getSelectedIndex() == 0);

        result.setPerformLfq(lfqCmb.getSelectedIndex() == 0);
        result.setDeisotope(deisotopeCmb.getSelectedIndex() == 0);
        result.setChimera(chimericSpectraCmb.getSelectedIndex() == 0);
        result.setPredictRt(predictRtCmb.getSelectedIndex() == 0);

        input = minNumberOfPeaksTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPeaks(Integer.valueOf(input));
        }
        input = maxNumberOfPeaksTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxPeaks(Integer.valueOf(input));
        }

        input = minMatchedPeaksTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinMatchedPeaks(Integer.valueOf(input));
        }

        input = maxFragmentChargeTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxFragmentCharge(Integer.valueOf(input));
        }

        input = numberOfPsmsPerSpectrumTxt.getText().trim();
        if (!input.equals("")) {
            result.setNumPsmsPerSpectrum(Integer.valueOf(input));
        }

        input = batchSizeTxt.getText().trim();
        if (!input.equals("")) {
            result.setBatchSize(Integer.valueOf(input));
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
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        openDialogHelpJButton = new javax.swing.JButton();
        advancedSettingsWarningLabel = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        advancedSearchSettingsPanel = new javax.swing.JPanel();
        predictRtCmb = new javax.swing.JComboBox();
        batchSizeLabel = new javax.swing.JLabel();
        predictRtLabel = new javax.swing.JLabel();
        chimericSpectraLabel = new javax.swing.JLabel();
        chimericSpectraCmb = new javax.swing.JComboBox();
        deisotopeLabel = new javax.swing.JLabel();
        deisotopeCmb = new javax.swing.JComboBox();
        minPepLengthTxt = new javax.swing.JTextField();
        peptideLengthDividerLabel = new javax.swing.JLabel();
        maxPepLengthTxt = new javax.swing.JTextField();
        peptideLengthLabel = new javax.swing.JLabel();
        minIonIndexLabel = new javax.swing.JLabel();
        minIonIndexTxt = new javax.swing.JTextField();
        generateDecoysLabel = new javax.swing.JLabel();
        generateDecoysCmb = new javax.swing.JComboBox();
        bucketSizeLabel = new javax.swing.JLabel();
        bucketSizeTxt = new javax.swing.JTextField();
        fragmentMzLabel = new javax.swing.JLabel();
        minFragmentMzTxt = new javax.swing.JTextField();
        fragmentMzDividerLabel = new javax.swing.JLabel();
        maxFragmentMzTxt = new javax.swing.JTextField();
        peptideMassLabel = new javax.swing.JLabel();
        minPeptideMassTxt = new javax.swing.JTextField();
        peptideMassDividerLabel = new javax.swing.JLabel();
        maxPeptideMassTxt = new javax.swing.JTextField();
        numberOfPeaksLabel = new javax.swing.JLabel();
        minNumberOfPeaksTxt = new javax.swing.JTextField();
        numberOfPeaksDividerLabel = new javax.swing.JLabel();
        maxNumberOfPeaksTxt = new javax.swing.JTextField();
        maxFragmentChargeLabel = new javax.swing.JLabel();
        maxFragmentChargeTxt = new javax.swing.JTextField();
        numberOfPsmsPerSpectrumLabel = new javax.swing.JLabel();
        numberOfPsmsPerSpectrumTxt = new javax.swing.JTextField();
        maxVariableModsLabel = new javax.swing.JLabel();
        maxVariableModsTxt = new javax.swing.JTextField();
        minMatchedPeaksLabel = new javax.swing.JLabel();
        minMatchedPeaksTxt = new javax.swing.JTextField();
        batchSizeTxt = new javax.swing.JTextField();
        wideWindowLabel = new javax.swing.JLabel();
        wideWindowCmb = new javax.swing.JComboBox();
        quantificationSettingsPanel = new javax.swing.JPanel();
        tmtTypeLabel = new javax.swing.JLabel();
        tmtTypeCmb = new javax.swing.JComboBox();
        tmtLevelLabel = new javax.swing.JLabel();
        tmtLevelCmb = new javax.swing.JComboBox();
        tmtSnCmb = new javax.swing.JComboBox();
        tmtSnLabel = new javax.swing.JLabel();
        lfqLabel = new javax.swing.JLabel();
        lfqCmb = new javax.swing.JComboBox();
        lfqPeakScoringLabel = new javax.swing.JLabel();
        lfqPeakScoringCmb = new javax.swing.JComboBox();
        lfqIntegrationLabel = new javax.swing.JLabel();
        lfqIntegrationCmb = new javax.swing.JComboBox();
        lfqSpectraAngleLabel = new javax.swing.JLabel();
        lfqPpmToleranceLabel = new javax.swing.JLabel();
        lfqSpectralAngleTxt = new javax.swing.JTextField();
        lfqPpmToleranceTxt = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sage Advanced Settings");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

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

        advancedSettingsWarningLabel.setText("Click to open the Sage web page");

        advancedSearchSettingsPanel.setBorder(null);
        advancedSearchSettingsPanel.setOpaque(false);

        predictRtCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        batchSizeLabel.setText("Batch Size (if not set: CPUs/2)");

        predictRtLabel.setText("Predict Retention Time");

        chimericSpectraLabel.setText("Chimeric Spectra");

        chimericSpectraCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        chimericSpectraCmb.setSelectedIndex(1);

        deisotopeLabel.setText("Deisotope");

        deisotopeCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        deisotopeCmb.setSelectedIndex(1);

        minPepLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPepLengthTxt.setText("8");
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

        minIonIndexLabel.setText("Minimum Ion Index");

        minIonIndexTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minIonIndexTxt.setText("2");
        minIonIndexTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minIonIndexTxtKeyReleased(evt);
            }
        });

        generateDecoysLabel.setText("Generate Decoys");

        generateDecoysCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        generateDecoysCmb.setSelectedIndex(1);

        bucketSizeLabel.setText("Bucket Size");

        bucketSizeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        bucketSizeTxt.setText("32768");
        bucketSizeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                bucketSizeTxtKeyReleased(evt);
            }
        });

        fragmentMzLabel.setText("Fragment m/z (min - max)");

        minFragmentMzTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minFragmentMzTxt.setText("200");
        minFragmentMzTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minFragmentMzTxtKeyReleased(evt);
            }
        });

        fragmentMzDividerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fragmentMzDividerLabel.setText("-");

        maxFragmentMzTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxFragmentMzTxt.setText("2000");
        maxFragmentMzTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxFragmentMzTxtKeyReleased(evt);
            }
        });

        peptideMassLabel.setText("Peptide Mass (min - max)");

        minPeptideMassTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPeptideMassTxt.setText("600");
        minPeptideMassTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPeptideMassTxtKeyReleased(evt);
            }
        });

        peptideMassDividerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        peptideMassDividerLabel.setText("-");

        maxPeptideMassTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPeptideMassTxt.setText("5000");
        maxPeptideMassTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPeptideMassTxtKeyReleased(evt);
            }
        });

        numberOfPeaksLabel.setText("Number of Peaks (min - max)");

        minNumberOfPeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minNumberOfPeaksTxt.setText("15");
        minNumberOfPeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minNumberOfPeaksTxtKeyReleased(evt);
            }
        });

        numberOfPeaksDividerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        numberOfPeaksDividerLabel.setText("-");

        maxNumberOfPeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxNumberOfPeaksTxt.setText("150");
        maxNumberOfPeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxNumberOfPeaksTxtKeyReleased(evt);
            }
        });

        maxFragmentChargeLabel.setText("Maximum Fragment Charge");

        maxFragmentChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxFragmentChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxFragmentChargeTxtKeyReleased(evt);
            }
        });

        numberOfPsmsPerSpectrumLabel.setText("Number of PSMs per Spectrum");

        numberOfPsmsPerSpectrumTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfPsmsPerSpectrumTxt.setText("1");
        numberOfPsmsPerSpectrumTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numberOfPsmsPerSpectrumTxtKeyReleased(evt);
            }
        });

        maxVariableModsLabel.setText("Maximum Variable Modifications");

        maxVariableModsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxVariableModsTxt.setText("2");
        maxVariableModsTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxVariableModsTxtKeyReleased(evt);
            }
        });

        minMatchedPeaksLabel.setText("Minimum Matched Peaks");

        minMatchedPeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minMatchedPeaksTxt.setText("4");
        minMatchedPeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minMatchedPeaksTxtKeyReleased(evt);
            }
        });

        batchSizeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        batchSizeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                batchSizeTxtKeyReleased(evt);
            }
        });

        wideWindowLabel.setText("Wide Window");

        wideWindowCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        wideWindowCmb.setSelectedIndex(1);

        javax.swing.GroupLayout advancedSearchSettingsPanelLayout = new javax.swing.GroupLayout(advancedSearchSettingsPanel);
        advancedSearchSettingsPanel.setLayout(advancedSearchSettingsPanelLayout);
        advancedSearchSettingsPanelLayout.setHorizontalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(predictRtLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(batchSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chimericSpectraLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chimericSpectraCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(predictRtCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(batchSizeTxt, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(deisotopeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deisotopeCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(minIonIndexLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minIonIndexTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(generateDecoysLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateDecoysCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(bucketSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bucketSizeTxt))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxFragmentChargeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxFragmentChargeTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(numberOfPsmsPerSpectrumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numberOfPsmsPerSpectrumTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, advancedSearchSettingsPanelLayout.createSequentialGroup()
                                .addComponent(peptideMassLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minPeptideMassTxt))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, advancedSearchSettingsPanelLayout.createSequentialGroup()
                                .addComponent(fragmentMzLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minFragmentMzTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fragmentMzDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(peptideMassDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(maxPeptideMassTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                            .addComponent(maxFragmentMzTxt)))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(peptideLengthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(peptideLengthDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(numberOfPeaksLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minNumberOfPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(numberOfPeaksDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(maxNumberOfPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxVariableModsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxVariableModsTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(minMatchedPeaksLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minMatchedPeaksTxt))
                    .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                        .addComponent(wideWindowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wideWindowCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        advancedSearchSettingsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {maxFragmentMzTxt, maxNumberOfPeaksTxt, maxPepLengthTxt, maxPeptideMassTxt, minFragmentMzTxt, minNumberOfPeaksTxt, minPepLengthTxt, minPeptideMassTxt});

        advancedSearchSettingsPanelLayout.setVerticalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bucketSizeLabel)
                    .addComponent(bucketSizeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideLengthDividerLabel)
                    .addComponent(peptideLengthLabel))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minFragmentMzTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxFragmentMzTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentMzDividerLabel)
                    .addComponent(fragmentMzLabel))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPeptideMassTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPeptideMassTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideMassDividerLabel)
                    .addComponent(peptideMassLabel))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minIonIndexLabel)
                    .addComponent(minIonIndexTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxVariableModsLabel)
                    .addComponent(maxVariableModsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generateDecoysLabel)
                    .addComponent(generateDecoysCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deisotopeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deisotopeLabel))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chimericSpectraCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chimericSpectraLabel))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wideWindowCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wideWindowLabel))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(predictRtCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(predictRtLabel))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minNumberOfPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxNumberOfPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numberOfPeaksDividerLabel)
                    .addComponent(numberOfPeaksLabel))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minMatchedPeaksLabel)
                    .addComponent(minMatchedPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxFragmentChargeLabel)
                    .addComponent(maxFragmentChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberOfPsmsPerSpectrumLabel)
                    .addComponent(numberOfPsmsPerSpectrumTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(batchSizeLabel)
                    .addComponent(batchSizeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tabbedPane.addTab("Search", advancedSearchSettingsPanel);

        quantificationSettingsPanel.setOpaque(false);

        tmtTypeLabel.setText("TMT Type");

        tmtTypeCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Tmt6", "Tmt10", "Tmt11", "Tmt16", "Tmt18" }));
        tmtTypeCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tmtTypeCmbActionPerformed(evt);
            }
        });

        tmtLevelLabel.setText("Level");

        tmtLevelCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3" }));
        tmtLevelCmb.setSelectedIndex(1);

        tmtSnCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        tmtSnLabel.setText("Signal/Noise");

        lfqLabel.setText("LFQ");

        lfqCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        lfqCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lfqCmbActionPerformed(evt);
            }
        });

        lfqPeakScoringLabel.setText("Peak Scoring");

        lfqPeakScoringCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Hybrid", "RetentionTime", "SpectralAngle" }));

        lfqIntegrationLabel.setText("Integration");

        lfqIntegrationCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sum", "Max" }));

        lfqSpectraAngleLabel.setText("Spectral Angle");

        lfqPpmToleranceLabel.setText("PPM Tolerance");

        lfqSpectralAngleTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lfqSpectralAngleTxt.setText("0.7");

        lfqPpmToleranceTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lfqPpmToleranceTxt.setText("5.0");

        javax.swing.GroupLayout quantificationSettingsPanelLayout = new javax.swing.GroupLayout(quantificationSettingsPanel);
        quantificationSettingsPanel.setLayout(quantificationSettingsPanelLayout);
        quantificationSettingsPanelLayout.setHorizontalGroup(
            quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lfqSpectraAngleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lfqIntegrationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lfqIntegrationCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lfqSpectralAngleTxt)))
                            .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(lfqPeakScoringLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lfqPeakScoringCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(lfqPpmToleranceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lfqPpmToleranceTxt))
                                    .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                                        .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(tmtSnLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tmtLevelLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(tmtSnCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(tmtLevelCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(tmtTypeCmb, 0, 239, Short.MAX_VALUE))))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                        .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                                .addComponent(lfqLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lfqCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tmtTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        quantificationSettingsPanelLayout.setVerticalGroup(
            quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quantificationSettingsPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tmtTypeLabel)
                    .addComponent(tmtTypeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tmtLevelLabel)
                    .addComponent(tmtLevelCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tmtSnLabel)
                    .addComponent(tmtSnCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lfqCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lfqLabel))
                .addGap(0, 0, 0)
                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lfqPeakScoringLabel)
                    .addComponent(lfqPeakScoringCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lfqIntegrationLabel)
                    .addComponent(lfqIntegrationCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lfqSpectraAngleLabel)
                    .addComponent(lfqSpectralAngleTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(quantificationSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lfqPpmToleranceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lfqPpmToleranceLabel))
                .addContainerGap(234, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Quantification", quantificationSettingsPanel);

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tabbedPane)
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
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
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
     * Open the Sage help page.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://github.com/lazear/sage");
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
    private void minIonIndexTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minIonIndexTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minIonIndexTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void bucketSizeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_bucketSizeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_bucketSizeTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minFragmentMzTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minFragmentMzTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minFragmentMzTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxFragmentMzTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxFragmentMzTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxFragmentMzTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minPeptideMassTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPeptideMassTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPeptideMassTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxPeptideMassTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPeptideMassTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPeptideMassTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minNumberOfPeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minNumberOfPeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minNumberOfPeaksTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxNumberOfPeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxNumberOfPeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxNumberOfPeaksTxtKeyReleased

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
    private void numberOfPsmsPerSpectrumTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numberOfPsmsPerSpectrumTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_numberOfPsmsPerSpectrumTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxVariableModsTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxVariableModsTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxVariableModsTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minMatchedPeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minMatchedPeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minMatchedPeaksTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void batchSizeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_batchSizeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_batchSizeTxtKeyReleased

    /**
     * Enable or disable the TMT settings.
     *
     * @param evt
     */
    private void tmtTypeCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tmtTypeCmbActionPerformed

        tmtLevelCmb.setEnabled(tmtTypeCmb.getSelectedIndex() > 0);
        tmtSnCmb.setEnabled(tmtTypeCmb.getSelectedIndex() > 0);

    }//GEN-LAST:event_tmtTypeCmbActionPerformed

    /**
     * Enable or disable the LFQ settings.
     *
     * @param evt
     */
    private void lfqCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lfqCmbActionPerformed

        lfqPeakScoringCmb.setEnabled(lfqCmb.getSelectedIndex() == 0);
        lfqIntegrationCmb.setEnabled(lfqCmb.getSelectedIndex() == 0);
        lfqSpectralAngleTxt.setEnabled(lfqCmb.getSelectedIndex() == 0);
        lfqPpmToleranceTxt.setEnabled(lfqCmb.getSelectedIndex() == 0);

    }//GEN-LAST:event_lfqCmbActionPerformed

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;

        valid = GuiUtilities.validateIntegerInput(this, bucketSizeLabel, bucketSizeTxt, "bucket size", "Bucket Size Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, peptideLengthLabel, minPepLengthTxt, "minimum peptide length", "Peptide Length Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, peptideLengthLabel, maxPepLengthTxt, "maximum peptide length", "Peptide Length Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, fragmentMzLabel, minFragmentMzTxt, "minimum fragment m/z", "Fragment M/Z Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, fragmentMzLabel, maxFragmentMzTxt, "maximum fragment m/z", "Fragment M/Z Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, peptideMassLabel, minPeptideMassTxt, "minimum peptide mass", "Peptide Mass Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, peptideMassLabel, maxFragmentMzTxt, "maximum peptide mass", "Peptide Mass Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, minIonIndexLabel, minIonIndexTxt, "minimum ion index", "Minimum Ion Index Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxVariableModsLabel, maxVariableModsTxt, "maximum variable modifications", "Maximum Variable Modifications Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, numberOfPeaksLabel, minNumberOfPeaksTxt, "minimum number of peaks", "Number of Peaks Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, numberOfPeaksLabel, maxNumberOfPeaksTxt, "maximum number of peaks", "Number of Peaks Error", true, showMessage, valid);

        if (!maxFragmentChargeTxt.getText().trim().isEmpty()) {
            valid = GuiUtilities.validateIntegerInput(this, maxFragmentChargeLabel, maxFragmentChargeTxt, "maximum fragment charge", "Fragment Charge Error", true, showMessage, valid);
        } else {
            maxFragmentChargeLabel.setForeground(Color.BLACK);
            maxFragmentChargeLabel.setToolTipText(null);
        }

        if (!minMatchedPeaksTxt.getText().trim().isEmpty()) {
            valid = GuiUtilities.validateIntegerInput(this, minMatchedPeaksLabel, minMatchedPeaksTxt, "minimum matched peaks", "Minimum Matched Peaks Error", true, showMessage, valid);
        } else {
            minMatchedPeaksLabel.setForeground(Color.BLACK);
            minMatchedPeaksLabel.setToolTipText(null);
        }

        valid = GuiUtilities.validateIntegerInput(this, numberOfPsmsPerSpectrumLabel, numberOfPsmsPerSpectrumTxt, "number of PSMs per spectrum", "PSMs per Spectrum Error", true, showMessage, valid);

        if (!batchSizeTxt.getText().trim().isEmpty()) {
            valid = GuiUtilities.validateIntegerInput(this, batchSizeLabel, batchSizeTxt, "batch size", "Batch Size Error", true, showMessage, valid);
        } else {
            batchSizeLabel.setForeground(Color.BLACK);
            batchSizeLabel.setToolTipText(null);
        }

        valid = GuiUtilities.validateDoubleInput(this, lfqSpectraAngleLabel, lfqSpectralAngleTxt, "spectral angle", "Spectral Angle Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, lfqPpmToleranceLabel, lfqPpmToleranceTxt, "ppm tolerance", "PPM Tolerance Error", true, showMessage, valid);

        okButton.setEnabled(valid);

        return valid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedSearchSettingsPanel;
    private javax.swing.JLabel advancedSettingsWarningLabel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JLabel batchSizeLabel;
    private javax.swing.JTextField batchSizeTxt;
    private javax.swing.JLabel bucketSizeLabel;
    private javax.swing.JTextField bucketSizeTxt;
    private javax.swing.JComboBox chimericSpectraCmb;
    private javax.swing.JLabel chimericSpectraLabel;
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox deisotopeCmb;
    private javax.swing.JLabel deisotopeLabel;
    private javax.swing.JLabel fragmentMzDividerLabel;
    private javax.swing.JLabel fragmentMzLabel;
    private javax.swing.JComboBox generateDecoysCmb;
    private javax.swing.JLabel generateDecoysLabel;
    private javax.swing.JComboBox lfqCmb;
    private javax.swing.JComboBox lfqIntegrationCmb;
    private javax.swing.JLabel lfqIntegrationLabel;
    private javax.swing.JLabel lfqLabel;
    private javax.swing.JComboBox lfqPeakScoringCmb;
    private javax.swing.JLabel lfqPeakScoringLabel;
    private javax.swing.JLabel lfqPpmToleranceLabel;
    private javax.swing.JTextField lfqPpmToleranceTxt;
    private javax.swing.JLabel lfqSpectraAngleLabel;
    private javax.swing.JTextField lfqSpectralAngleTxt;
    private javax.swing.JLabel maxFragmentChargeLabel;
    private javax.swing.JTextField maxFragmentChargeTxt;
    private javax.swing.JTextField maxFragmentMzTxt;
    private javax.swing.JTextField maxNumberOfPeaksTxt;
    private javax.swing.JTextField maxPepLengthTxt;
    private javax.swing.JTextField maxPeptideMassTxt;
    private javax.swing.JLabel maxVariableModsLabel;
    private javax.swing.JTextField maxVariableModsTxt;
    private javax.swing.JTextField minFragmentMzTxt;
    private javax.swing.JLabel minIonIndexLabel;
    private javax.swing.JTextField minIonIndexTxt;
    private javax.swing.JLabel minMatchedPeaksLabel;
    private javax.swing.JTextField minMatchedPeaksTxt;
    private javax.swing.JTextField minNumberOfPeaksTxt;
    private javax.swing.JTextField minPepLengthTxt;
    private javax.swing.JTextField minPeptideMassTxt;
    private javax.swing.JLabel numberOfPeaksDividerLabel;
    private javax.swing.JLabel numberOfPeaksLabel;
    private javax.swing.JLabel numberOfPsmsPerSpectrumLabel;
    private javax.swing.JTextField numberOfPsmsPerSpectrumTxt;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JLabel peptideLengthDividerLabel;
    private javax.swing.JLabel peptideLengthLabel;
    private javax.swing.JLabel peptideMassDividerLabel;
    private javax.swing.JLabel peptideMassLabel;
    private javax.swing.JComboBox predictRtCmb;
    private javax.swing.JLabel predictRtLabel;
    private javax.swing.JPanel quantificationSettingsPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JComboBox tmtLevelCmb;
    private javax.swing.JLabel tmtLevelLabel;
    private javax.swing.JComboBox tmtSnCmb;
    private javax.swing.JLabel tmtSnLabel;
    private javax.swing.JComboBox tmtTypeCmb;
    private javax.swing.JLabel tmtTypeLabel;
    private javax.swing.JComboBox wideWindowCmb;
    private javax.swing.JLabel wideWindowLabel;
    // End of variables declaration//GEN-END:variables
}
