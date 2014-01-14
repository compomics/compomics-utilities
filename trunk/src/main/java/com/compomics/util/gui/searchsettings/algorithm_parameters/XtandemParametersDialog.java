package com.compomics.util.gui.searchsettings.algorithm_parameters;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.XtandemParameters;
import com.compomics.util.preferences.ModificationProfile;
import java.awt.Color;
import javax.swing.JOptionPane;

/**
 * dialog for the X!Tandem specific parameters.
 *
 * @author Marc Vaudel
 */
public class XtandemParametersDialog extends javax.swing.JDialog {

    /**
     * The X!Tandem parameters class containing the information to display.
     */
    private XtandemParameters xtandemParameters;
    /**
     * The modification profile used for the search.
     */
    private ModificationProfile modificationProfile;
    /**
     * The fragment ion mass accuracy.
     */
    private double fragmentIonMassAccuracy;
    /**
     * boolean indicating whether the used canceled the editing.
     */
    private boolean cancelled = false;

    /**
     * Creates new form XtandemParametersDialog.
     *
     * @param parent the parent frame
     * @param xtandemParameters the X!Tandem parameters
     * @param modificationProfile the modification profile of the search
     * @param fragmentIonMassAccuracy the fragment ion mass accuracy of the mass
     * spectrometer
     */
    public XtandemParametersDialog(java.awt.Frame parent, XtandemParameters xtandemParameters, ModificationProfile modificationProfile, double fragmentIonMassAccuracy) {
        super(parent, true);
        this.xtandemParameters = xtandemParameters;
        this.modificationProfile = modificationProfile;
        this.fragmentIonMassAccuracy = fragmentIonMassAccuracy;
        initComponents();
        fillGUI();
        setTitle("X!Tandem Advanced Parameters");
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Fills the GUI with the information contained in the omssa parameters
     * object.
     */
    private void fillGUI() {
        if (xtandemParameters.getDynamicRange() != null) {
            dynamicRangeTxt.setText(xtandemParameters.getDynamicRange() + "");
        }
        if (xtandemParameters.getnPeaks() != null) {
            nPeaksTxt.setText(xtandemParameters.getnPeaks() + "");
        }
        if (xtandemParameters.getMinFragmentMz() != null) {
            minFragmentMzTxt.setText(xtandemParameters.getMinFragmentMz() + "");
        }
        if (xtandemParameters.getMinPeaksPerSpectrum() != null) {
            minPeaksTxt.setText(xtandemParameters.getMinPeaksPerSpectrum() + "");
        }
        if (xtandemParameters.isUseNoiseSuppression()) {
            noiseSuppressionCmb.setSelectedIndex(0);
            minPrecMassTxt.setEnabled(true);
        } else {
            noiseSuppressionCmb.setSelectedIndex(1);
            minPrecMassTxt.setEnabled(false);
        }
        if (xtandemParameters.getMinPrecursorMass() != null) {
            minPrecMassTxt.setText(xtandemParameters.getMinPrecursorMass() + "");
        }
        if (xtandemParameters.isProteinQuickAcetyl()) {
            quickAcetylCmb.setSelectedIndex(0);
        } else {
            quickAcetylCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isQuickPyrolidone()) {
            quickPyroCmb.setSelectedIndex(0);
        } else {
            quickPyroCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isStpBias()) {
            stpBiasCmb.setSelectedIndex(0);
        } else {
            stpBiasCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefine()) {
            quickPyroCmb.setSelectedIndex(0);
            maxEValueRefineTxt.setEnabled(true);
            unanticipatedCleavageCmb.setEnabled(true);
            semiEnzymaticCmb.setEnabled(true);
            potentialModificationsCmb.setEnabled(true);
            pointMutationsCmb.setEnabled(true);
            snapsCmb.setEnabled(true);
            spectrumSynthesisCmb.setEnabled(true);
        } else {
            quickPyroCmb.setSelectedIndex(1);
            maxEValueRefineTxt.setEnabled(false);
            unanticipatedCleavageCmb.setEnabled(false);
            semiEnzymaticCmb.setEnabled(false);
            potentialModificationsCmb.setEnabled(false);
            pointMutationsCmb.setEnabled(false);
            snapsCmb.setEnabled(false);
            spectrumSynthesisCmb.setEnabled(false);
        }
        if (xtandemParameters.getMaximumExpectationValueRefinement() != null) {
            maxEValueRefineTxt.setText(xtandemParameters.getMaximumExpectationValueRefinement() + "");
        }
        if (xtandemParameters.isRefineUnanticipatedCleavages()) {
            unanticipatedCleavageCmb.setSelectedIndex(0);
        } else {
            unanticipatedCleavageCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefineSemi()) {
            semiEnzymaticCmb.setSelectedIndex(0);
        } else {
            semiEnzymaticCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isPotentialModificationsForFullRefinment()) {
            potentialModificationsCmb.setSelectedIndex(0);
        } else {
            potentialModificationsCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefinePointMutations()) {
            pointMutationsCmb.setSelectedIndex(0);
        } else {
            pointMutationsCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefineSnaps()) {
            snapsCmb.setSelectedIndex(0);
        } else {
            snapsCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefineSpectrumSynthesis()) {
            spectrumSynthesisCmb.setSelectedIndex(0);
        } else {
            spectrumSynthesisCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.getMaxEValue() != null) {
            eValueTxt.setText(xtandemParameters.getMaxEValue() + "");
        }
        if (xtandemParameters.isOutputProteins()) {
            outputProteinsCmb.setSelectedIndex(0);
            outputSequencesCmb.setEnabled(true);
            if (xtandemParameters.isOutputSequences()) {
                outputSequencesCmb.setSelectedIndex(0);
            } else {
                outputSequencesCmb.setSelectedIndex(1);
            }
        } else {
            outputProteinsCmb.setSelectedIndex(1);
            outputSequencesCmb.setEnabled(false);
            outputSequencesCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isOutputSpectra()) {
            outputSpectraCmb.setSelectedIndex(0);
        } else {
            outputSpectraCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isOutputHistograms()) {
            outputHistogramsCmb.setSelectedIndex(0);
        } else {
            outputHistogramsCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.getSkylinePath() != null) {
            skylineTxt.setText(xtandemParameters.getSkylinePath() + "");
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
     * Returns the user selection as X!tandem parameters object.
     *
     * @return the user selection
     */
    public XtandemParameters getInput() {
        XtandemParameters result = new XtandemParameters();
        String input = dynamicRangeTxt.getText().trim();
        if (!input.equals("")) {
            result.setDynamicRange(new Double(input));
        }
        input = nPeaksTxt.getText().trim();
        if (!input.equals("")) {
            result.setnPeaks(new Integer(input));
        }
        input = minFragmentMzTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinFragmentMz(new Double(input));
        }
        input = minPeaksTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPeaksPerSpectrum(new Integer(input));
        }
        result.setUseNoiseSuppression(noiseSuppressionCmb.getSelectedIndex() == 0);
        input = minPrecMassTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPrecursorMass(new Double(input));
        }
        result.setProteinQuickAcetyl(quickAcetylCmb.getSelectedIndex() == 0);
        result.setQuickPyrolidone(quickPyroCmb.getSelectedIndex() == 0);
        result.setStpBias(stpBiasCmb.getSelectedIndex() == 0);
        result.setRefine(refinementCmb.getSelectedIndex() == 0);
        input = maxEValueRefineTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaximumExpectationValueRefinement(new Double(input));
        }
        result.setRefineUnanticipatedCleavages(unanticipatedCleavageCmb.getSelectedIndex() == 0);
        result.setRefineSemi(semiEnzymaticCmb.getSelectedIndex() == 0);
        result.setPotentialModificationsForFullRefinment(potentialModificationsCmb.getSelectedIndex() == 0);
        result.setRefinePointMutations(pointMutationsCmb.getSelectedIndex() == 0);
        result.setRefineSnaps(snapsCmb.getSelectedIndex() == 0);
        result.setRefineSpectrumSynthesis(spectrumSynthesisCmb.getSelectedIndex() == 0);
        input = eValueTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxEValue(new Double(input));
        }
        result.setOutputProteins(outputProteinsCmb.getSelectedIndex() == 0);
        result.setOutputSequences(outputSequencesCmb.getSelectedIndex() == 0);
        result.setOutputSpectra(outputSpectraCmb.getSelectedIndex() == 0);
        result.setOutputHistograms(outputHistogramsCmb.getSelectedIndex() == 0);
        input = skylineTxt.getText().trim();
        if (!input.equals("")) {
            result.setSkylinePath(input);
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

        omssaParametersPanel = new javax.swing.JPanel();
        eValueLbl = new javax.swing.JLabel();
        eValueTxt = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        outputSequencesCmb = new javax.swing.JComboBox();
        outputProteinsCmb = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        outputSpectraCmb = new javax.swing.JComboBox();
        skylineTxt = new javax.swing.JTextField();
        eValueLbl3 = new javax.swing.JLabel();
        outputHistogramsCmb = new javax.swing.JComboBox();
        jLabel17 = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        dynamicRangeLbl = new javax.swing.JLabel();
        dynamicRangeTxt = new javax.swing.JTextField();
        nPeaksTxt = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        nPeaksLbl = new javax.swing.JLabel();
        minFragMzLbl = new javax.swing.JLabel();
        minFragmentMzTxt = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        minPeaksLbl = new javax.swing.JLabel();
        minPeaksTxt = new javax.swing.JTextField();
        minPrecMassLbl = new javax.swing.JLabel();
        minPrecMassTxt = new javax.swing.JTextField();
        noiseSuppressionCmb = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        quickPyroCmb = new javax.swing.JComboBox();
        quickAcetylCmb = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        stpBiasCmb = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        refinementCmb = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        semiEnzymaticCmb = new javax.swing.JComboBox();
        maxEValueRefineTxt = new javax.swing.JTextField();
        maxEValueRefinmentLbl = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        pointMutationsCmb = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        snapsCmb = new javax.swing.JComboBox();
        spectrumSynthesisCmb = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        unanticipatedCleavageCmb = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        potentialModificationsCmb = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        omssaParametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Parameters"));
        omssaParametersPanel.setOpaque(false);

        eValueLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/omvev.html\">E-value Cutoff</a></html>");

        eValueTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        eValueTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                eValueTxtKeyReleased(evt);
            }
        });

        jLabel1.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/osequ.html\">Output Sequences</a></html>");

        outputSequencesCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        outputProteinsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        outputProteinsCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputProteinsCmbActionPerformed(evt);
            }
        });

        jLabel2.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/oprot.html\">Output Proteins</a></html>");

        jLabel3.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/ospec.html\">Output Spectra</a></html>");

        outputSpectraCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        skylineTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        skylineTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                skylineTxtKeyReleased(evt);
            }
        });

        eValueLbl3.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/ssp.html\">Skyline Path</a></html>");

        outputHistogramsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        jLabel17.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/ohist.html\">Output Histograms</a></html>");

        javax.swing.GroupLayout omssaParametersPanelLayout = new javax.swing.GroupLayout(omssaParametersPanel);
        omssaParametersPanel.setLayout(omssaParametersPanelLayout);
        omssaParametersPanelLayout.setHorizontalGroup(
            omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(omssaParametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(omssaParametersPanelLayout.createSequentialGroup()
                        .addComponent(eValueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eValueTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, omssaParametersPanelLayout.createSequentialGroup()
                        .addComponent(eValueLbl3, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(skylineTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                    .addGroup(omssaParametersPanelLayout.createSequentialGroup()
                        .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(265, 265, 265)
                        .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(outputHistogramsCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(outputSpectraCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(outputProteinsCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(outputSequencesCmb, 0, 148, Short.MAX_VALUE))))
                .addContainerGap())
        );
        omssaParametersPanelLayout.setVerticalGroup(
            omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(omssaParametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eValueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eValueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputProteinsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputSequencesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputSpectraCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputHistogramsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eValueLbl3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(skylineTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Spectrum Import Parameters"));

        dynamicRangeLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/sdr.html\">Dynamic Range</a></html>");

        dynamicRangeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        dynamicRangeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dynamicRangeTxtKeyReleased(evt);
            }
        });

        nPeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nPeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nPeaksTxtKeyReleased(evt);
            }
        });

        jLabel4.setText("Da");

        nPeaksLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/stp.html\">Number of Peaks</a></html>");

        minFragMzLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/smfmz.html\">Minimum Fragment m/z</a></html>");

        minFragmentMzTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minFragmentMzTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minFragmentMzTxtKeyReleased(evt);
            }
        });

        jLabel5.setText("m/z");

        minPeaksLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/smp.html\">Minimum Peaks</a></html>");

        minPeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPeaksTxtKeyReleased(evt);
            }
        });

        minPrecMassLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/smpmh.html\">Minimum Precursor Mass</a></html>");

        minPrecMassTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecMassTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecMassTxtKeyReleased(evt);
            }
        });

        noiseSuppressionCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        noiseSuppressionCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noiseSuppressionCmbActionPerformed(evt);
            }
        });

        jLabel6.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/suns.html\">Noise suppression</a></html>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dynamicRangeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nPeaksTxt)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(dynamicRangeTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(minFragMzLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minFragmentMzTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(minPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minPeaksTxt))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minPrecMassLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minPrecMassTxt)
                            .addComponent(noiseSuppressionCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dynamicRangeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dynamicRangeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minFragmentMzTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minFragMzLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(noiseSuppressionCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPrecMassTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minPrecMassLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Advanced Search Parameters"));

        quickPyroCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        quickPyroCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickPyroCmbActionPerformed(evt);
            }
        });

        quickAcetylCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        quickAcetylCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickAcetylCmbActionPerformed(evt);
            }
        });

        jLabel7.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/pqa.html\">Quick Acetyl</a></html>");

        jLabel8.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/pqp.html\">Quick Pyrolidone</a></html>");

        jLabel16.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/pstpb.html\">stP bias</a></html>");

        stpBiasCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(stpBiasCmb, 0, 149, Short.MAX_VALUE)
                    .addComponent(quickAcetylCmb, 0, 149, Short.MAX_VALUE)
                    .addComponent(quickPyroCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quickAcetylCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(quickPyroCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stpBiasCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Refinement Parameters"));

        refinementCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        refinementCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refinementCmbActionPerformed(evt);
            }
        });

        jLabel9.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/refine.html\">Refinement</a></html>");

        jLabel10.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rcsemi.html\">Semi-Enzymatic Cleavage</a></html>");

        semiEnzymaticCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        maxEValueRefineTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxEValueRefineTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxEValueRefineTxtKeyReleased(evt);
            }
        });

        maxEValueRefinmentLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/refmvev.html\">Maximum Valid Expectation Value</a></html>");

        jLabel11.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rpm.html\">Point Mutations</a></html>");

        pointMutationsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        jLabel12.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rsaps.html\">snAPs</a></html>");

        snapsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        spectrumSynthesisCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        jLabel13.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rss.html\">Spectrum Synthesis</a></html>");

        unanticipatedCleavageCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        jLabel14.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/ruc.html\">Unanticipated Cleavage</a></html>");

        jLabel15.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rupmffr.html\">Use Potential Modifications for Full Refinement</a></html>");

        potentialModificationsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refinementCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(semiEnzymaticCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(maxEValueRefinmentLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxEValueRefineTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pointMutationsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(snapsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(spectrumSynthesisCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(unanticipatedCleavageCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(potentialModificationsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refinementCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxEValueRefineTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxEValueRefinmentLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unanticipatedCleavageCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(semiEnzymaticCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(potentialModificationsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pointMutationsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(snapsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spectrumSynthesisCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(omssaParametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(omssaParametersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(okButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void eValueTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eValueTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_eValueTxtKeyReleased

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput(true)) {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        cancelled = true;
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void dynamicRangeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dynamicRangeTxtKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_dynamicRangeTxtKeyReleased

    private void nPeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nPeaksTxtKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_nPeaksTxtKeyReleased

    private void skylineTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_skylineTxtKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_skylineTxtKeyReleased

    private void minFragmentMzTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minFragmentMzTxtKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_minFragmentMzTxtKeyReleased

    private void minPeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPeaksTxtKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_minPeaksTxtKeyReleased

    private void minPrecMassTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecMassTxtKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_minPrecMassTxtKeyReleased

    private void maxEValueRefineTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxEValueRefineTxtKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_maxEValueRefineTxtKeyReleased

    private void noiseSuppressionCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noiseSuppressionCmbActionPerformed
        if (noiseSuppressionCmb.getSelectedIndex() == 0) {
            minPrecMassTxt.setEnabled(true);
        } else {
            minPrecMassTxt.setEnabled(false);
        }
    }//GEN-LAST:event_noiseSuppressionCmbActionPerformed

    private void refinementCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refinementCmbActionPerformed
        if (refinementCmb.getSelectedIndex() == 0) {
            maxEValueRefineTxt.setEnabled(true);
            unanticipatedCleavageCmb.setEnabled(true);
            semiEnzymaticCmb.setEnabled(true);
            potentialModificationsCmb.setEnabled(true);
            pointMutationsCmb.setEnabled(true);
            snapsCmb.setEnabled(true);
            spectrumSynthesisCmb.setEnabled(true);
        } else {
            maxEValueRefineTxt.setEnabled(false);
            unanticipatedCleavageCmb.setEnabled(false);
            semiEnzymaticCmb.setEnabled(false);
            potentialModificationsCmb.setEnabled(false);
            pointMutationsCmb.setEnabled(false);
            snapsCmb.setEnabled(false);
            spectrumSynthesisCmb.setEnabled(false);
        }
    }//GEN-LAST:event_refinementCmbActionPerformed

    private void quickAcetylCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickAcetylCmbActionPerformed
        if (quickAcetylCmb.getSelectedIndex() == 0) {
            PTMFactory ptmFactory = PTMFactory.getInstance();
            for (String modName : modificationProfile.getFixedModifications()) {
                PTM ptm = ptmFactory.getPTM(modName);
                if ((ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODN || ptm.getType() == PTM.MODNAA) && Math.abs(ptm.getMass() - 42.010565) < fragmentIonMassAccuracy) {
                    JOptionPane.showMessageDialog(this, "The quick acetyl option might conflict with " + modName + ".",
                            "Modification Conflict", JOptionPane.ERROR_MESSAGE);
                    quickAcetylCmb.setSelectedIndex(1);
                    break;
                }
            }
        }
    }//GEN-LAST:event_quickAcetylCmbActionPerformed

    private void quickPyroCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickPyroCmbActionPerformed
        if (quickPyroCmb.getSelectedIndex() == 0) {
            PTMFactory ptmFactory = PTMFactory.getInstance();
            for (String modName : modificationProfile.getFixedModifications()) {
                PTM ptm = ptmFactory.getPTM(modName);
                if ((ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODN || ptm.getType() == PTM.MODNAA) && Math.abs(ptm.getMass() + 17.026549) < fragmentIonMassAccuracy) {
                    JOptionPane.showMessageDialog(this, "The quick pyrolidone option might conflict with " + modName + ".",
                            "Modification Conflict", JOptionPane.ERROR_MESSAGE);
                    quickAcetylCmb.setSelectedIndex(1);
                    break;
                }
            }
        }
    }//GEN-LAST:event_quickPyroCmbActionPerformed

    private void outputProteinsCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputProteinsCmbActionPerformed
        if (outputProteinsCmb.getSelectedIndex() == 0) {
            outputSequencesCmb.setEnabled(true);
        } else {
            outputSequencesCmb.setSelectedIndex(1);
            outputSequencesCmb.setEnabled(false);
        }
    }//GEN-LAST:event_outputProteinsCmbActionPerformed

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;

        eValueLbl.setForeground(Color.BLACK);
        eValueLbl.setToolTipText(null);
        // Validate e-value cutoff
        if (eValueTxt.getText() == null || eValueTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify an e-value cutoff.",
                        "E-value Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select an e-value cuttoff limit");
        }

        // OK, see if it is a number.
        float eValue = -1;

        try {
            eValue = Float.parseFloat(eValueTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff.",
                        "E-value Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (eValue < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff.",
                        "E-value Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a positive number");
        }

        dynamicRangeLbl.setForeground(Color.BLACK);
        dynamicRangeLbl.setToolTipText(null);
        if (dynamicRangeTxt.getText() == null || dynamicRangeTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a dynamic range cutoff.",
                        "Dynamic Range Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            dynamicRangeLbl.setForeground(Color.RED);
            dynamicRangeLbl.setToolTipText("Please select a dynamic range cuttoff limit");
        }

        // OK, see if it is a number.
        double test = -1;

        try {
            test = new Double(dynamicRangeTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the dynamic range cutoff.",
                        "Dynamic Range Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            dynamicRangeLbl.setForeground(Color.RED);
            dynamicRangeLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (test < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the dynamic range cutoff.",
                        "Dynamic Range Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            dynamicRangeLbl.setForeground(Color.RED);
            dynamicRangeLbl.setToolTipText("Please select a positive number");
        }

        nPeaksLbl.setForeground(Color.BLACK);
        nPeaksLbl.setToolTipText(null);
        if (nPeaksTxt.getText() == null || nPeaksTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number of peaks.",
                        "Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nPeaksLbl.setForeground(Color.RED);
            nPeaksLbl.setToolTipText("Please select a number of peaks limit");
        }

        // OK, see if it is a number.
        Integer nPeaks = -1;

        try {
            nPeaks = new Integer(nPeaksTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks.",
                        "Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nPeaksLbl.setForeground(Color.RED);
            nPeaksLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (nPeaks < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks.",
                        "Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nPeaksLbl.setForeground(Color.RED);
            nPeaksLbl.setToolTipText("Please select a positive number");
        }

        minFragMzLbl.setForeground(Color.BLACK);
        minFragMzLbl.setToolTipText(null);
        if (minFragmentMzTxt.getText() == null || minFragmentMzTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a minimal fragment m/z.",
                        "Minimal Fragment m/z Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minFragMzLbl.setForeground(Color.RED);
            minFragMzLbl.setToolTipText("Please select a minimal fragment m/z limit");
        }

        // OK, see if it is a number.
        test = -1;

        try {
            test = new Double(minFragmentMzTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal fragment m/z.",
                        "Minimal Fragment m/z Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minFragMzLbl.setForeground(Color.RED);
            minFragMzLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (test < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal fragment m/z.",
                        "Minimal Fragment m/z Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minFragMzLbl.setForeground(Color.RED);
            minFragMzLbl.setToolTipText("Please select a positive number");
        }

        minPeaksLbl.setForeground(Color.BLACK);
        minPeaksLbl.setToolTipText(null);
        if (minPeaksTxt.getText() == null || minPeaksTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a minimal number of peaks.",
                        "Minimal Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPeaksLbl.setForeground(Color.RED);
            minPeaksLbl.setToolTipText("Please select a minimal number of peaks");
        }

        // OK, see if it is a number.
        nPeaks = -1;

        try {
            nPeaks = new Integer(minPeaksTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal number of peaks.",
                        "Minimal Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPeaksLbl.setForeground(Color.RED);
            minPeaksLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (nPeaks < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal number of peaks.",
                        "Minimal Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPeaksLbl.setForeground(Color.RED);
            minPeaksLbl.setToolTipText("Please select a positive number");
        }

        minPrecMassLbl.setForeground(Color.BLACK);
        minPrecMassLbl.setToolTipText(null);
        if (minPrecMassTxt.getText() == null || minPrecMassTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a minimal precursor mass.",
                        "Minimal Precursor Mass Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecMassLbl.setForeground(Color.RED);
            minPrecMassLbl.setToolTipText("Please select a minimal precursor mass");
        }

        // OK, see if it is a number.
        test = -1;

        try {
            test = new Double(minPrecMassTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal precursor mass.",
                        "Minimal Precursor Mass Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecMassLbl.setForeground(Color.RED);
            minPrecMassLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (test < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal precursor mass.",
                        "Minimal Precursor Mass Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecMassLbl.setForeground(Color.RED);
            minPrecMassLbl.setToolTipText("Please select a positive number");
        }

        maxEValueRefinmentLbl.setForeground(Color.BLACK);
        maxEValueRefinmentLbl.setToolTipText(null);
        if (maxEValueRefineTxt.getText() == null || maxEValueRefineTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a maximal e-value for the refinement.",
                        "Maximal Refinement E-Value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxEValueRefinmentLbl.setForeground(Color.RED);
            maxEValueRefinmentLbl.setToolTipText("Please select a maximal e-value for the refinement");
        }

        // OK, see if it is a number.
        test = -1;

        try {
            test = new Double(maxEValueRefineTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal e-value for the refinement.",
                        "Maximal Refinement E-Value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxEValueRefinmentLbl.setForeground(Color.RED);
            maxEValueRefinmentLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (test < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal e-value for the refinement.",
                        "Maximal Refinement E-Value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxEValueRefinmentLbl.setForeground(Color.RED);
            maxEValueRefinmentLbl.setToolTipText("Please select a positive number");
        }

        return valid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel dynamicRangeLbl;
    private javax.swing.JTextField dynamicRangeTxt;
    private javax.swing.JLabel eValueLbl;
    private javax.swing.JLabel eValueLbl3;
    private javax.swing.JTextField eValueTxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField maxEValueRefineTxt;
    private javax.swing.JLabel maxEValueRefinmentLbl;
    private javax.swing.JLabel minFragMzLbl;
    private javax.swing.JTextField minFragmentMzTxt;
    private javax.swing.JLabel minPeaksLbl;
    private javax.swing.JTextField minPeaksTxt;
    private javax.swing.JLabel minPrecMassLbl;
    private javax.swing.JTextField minPrecMassTxt;
    private javax.swing.JLabel nPeaksLbl;
    private javax.swing.JTextField nPeaksTxt;
    private javax.swing.JComboBox noiseSuppressionCmb;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel omssaParametersPanel;
    private javax.swing.JComboBox outputHistogramsCmb;
    private javax.swing.JComboBox outputProteinsCmb;
    private javax.swing.JComboBox outputSequencesCmb;
    private javax.swing.JComboBox outputSpectraCmb;
    private javax.swing.JComboBox pointMutationsCmb;
    private javax.swing.JComboBox potentialModificationsCmb;
    private javax.swing.JComboBox quickAcetylCmb;
    private javax.swing.JComboBox quickPyroCmb;
    private javax.swing.JComboBox refinementCmb;
    private javax.swing.JComboBox semiEnzymaticCmb;
    private javax.swing.JTextField skylineTxt;
    private javax.swing.JComboBox snapsCmb;
    private javax.swing.JComboBox spectrumSynthesisCmb;
    private javax.swing.JComboBox stpBiasCmb;
    private javax.swing.JComboBox unanticipatedCleavageCmb;
    // End of variables declaration//GEN-END:variables
}
