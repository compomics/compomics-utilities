/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.gui.searchsettings.algorithm_parameters;

import com.compomics.util.experiment.identification.identification_parameters.OmssaParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.gui.JOptionEditorPane;
import java.awt.Color;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * dialog for the OMSSA specific parameters
 *
 * @author Marc
 */
public class OmssaParametersDialog extends javax.swing.JDialog {

    /**
     * The omssa parameters class containing the information to display
     */
    private OmssaParameters omssaParameters;
    /**
     * boolean indicating whether the used cancelled the editing
     */
    private boolean cancelled = false;

    /**
     * Creates new form OmssaParametersDialog
     *
     * @param parent the parent frame
     * @param omssaParameters the omssa parameters
     */
    public OmssaParametersDialog(java.awt.Frame parent, OmssaParameters omssaParameters) {
        super(parent, true);
        this.omssaParameters = omssaParameters;
        initComponents();
        fillGUI();
        setTitle("OMSSA Advanced Parameters");
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Fills the GUI with the information contained in the omssa parameters
     * object
     */
    private void fillGUI() {
        if (omssaParameters.getMaxEValue() != null) {
            maxEvalueTxt.setText(omssaParameters.getMaxEValue() + "");
        }

        if (omssaParameters.getHitListLength() != null) {
            hitlistTxt.setText(omssaParameters.getHitListLength() + "");
        }

        if (omssaParameters.getMinimalChargeForMultipleChargedFragments() != null) {
            minPrecChargeMultipleChargedFragmentsTxt.setText(omssaParameters.getMinimalChargeForMultipleChargedFragments().value + "");
        }

        if (omssaParameters.getMinPeptideLength() != null) {
            minPepLengthTxt.setText(omssaParameters.getMinPeptideLength() + "");
        }

        if (omssaParameters.getMaxPeptideLength() != null) {
            maxPepLengthTxt.setText(omssaParameters.getMaxPeptideLength() + "");
        }

        if (omssaParameters.isRemovePrecursor() != null) {
            if (omssaParameters.isRemovePrecursor()) {
                eliminatePrecursorCombo.setSelectedIndex(0);
            } else {
                eliminatePrecursorCombo.setSelectedIndex(1);
            }
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

        if (omssaParameters.isEstimateCharge() != null) {
            if (omssaParameters.isEstimateCharge()) {
                chargeEstimationCombo.setSelectedIndex(0);
            } else {
                chargeEstimationCombo.setSelectedIndex(1);
            }
        }
        
        omssaOutputFormatComboBox.setSelectedItem(omssaParameters.getSelectedOutput());
        
        eliminatePrecursorCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        precursorScalingCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        chargeEstimationCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        omssaOutputFormatComboBox.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
    }

    /**
     * Indicates whether the user cancelled the process
     *
     * @return true if cancel was pressed
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Returns the user selection as omssa parameters object.
     * 
     * @return the user selection
     */
    public OmssaParameters getInput() {
        OmssaParameters omssaParameters = new OmssaParameters();
        String input = maxEvalueTxt.getText().trim();
        if (!input.equals("")) {
            omssaParameters.setMaxEValue(new Double(input));
        }
        input = hitlistTxt.getText().trim();
        if (!input.equals("")) {
            omssaParameters.setHitListLength(new Integer(input));
        }
        input = minPepLengthTxt.getText().trim();
        if (!input.equals("")) {
            omssaParameters.setMinPeptideLength(new Integer(input));
        }
        input = maxPepLengthTxt.getText().trim();
        if (!input.equals("")) {
            omssaParameters.setMaxPeptideLength(new Integer(input));
        }
        input = minPrecChargeMultipleChargedFragmentsTxt.getText().trim();
        if (!input.equals("")) {
            int charge = new Integer(minPrecChargeMultipleChargedFragmentsTxt.getText().trim());
            omssaParameters.setMinimalChargeForMultipleChargedFragments(new Charge(Charge.PLUS, charge));
        }
        omssaParameters.setRemovePrecursor(eliminatePrecursorCombo.getSelectedIndex() == 0);
        omssaParameters.setScalePrecursor(precursorScalingCombo.getSelectedIndex() == 0);
        omssaParameters.setEstimateCharge(chargeEstimationCombo.getSelectedIndex() == 0);
        omssaParameters.setSelectedOutput(omssaOutputFormatComboBox.getSelectedItem().toString());
        return omssaParameters;
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
        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel = new javax.swing.JLabel();
        chargeReductionLabel = new javax.swing.JLabel();
        precursorMassScalingLabel = new javax.swing.JLabel();
        minPrecChargeMultipleChargedFragmentsTxt = new javax.swing.JTextField();
        eliminatePrecursorCombo = new javax.swing.JComboBox();
        precursorScalingCombo = new javax.swing.JComboBox();
        precursorChargeEstimationLabel = new javax.swing.JLabel();
        chargeEstimationCombo = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        maxPepLengthTxt = new javax.swing.JTextField();
        peptideLengthDividerLabel = new javax.swing.JLabel();
        minPepLengthTxt = new javax.swing.JTextField();
        peptideLengthJLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        omssaOutputFormatComboBox = new javax.swing.JComboBox();
        omssaOutputFormatLabel = new javax.swing.JLabel();
        eValueLbl = new javax.swing.JLabel();
        hitListLbl = new javax.swing.JLabel();
        maxEvalueTxt = new javax.swing.JTextField();
        hitlistTxt = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        omssaParametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Advanced Search Parameters"));
        omssaParametersPanel.setOpaque(false);

        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setText("Minimum Precursor Charge for Multiply Charged Fragments");

        chargeReductionLabel.setText("Eliminate Charge Reduced Precursors in Spectra");

        precursorMassScalingLabel.setText("Precursor Mass Scaling");

        minPrecChargeMultipleChargedFragmentsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecChargeMultipleChargedFragmentsTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecChargeMultipleChargedFragmentsTxtKeyReleased(evt);
            }
        });

        eliminatePrecursorCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        precursorScalingCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        precursorChargeEstimationLabel.setText("Precursor Charge Estimation");

        chargeEstimationCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout omssaParametersPanelLayout = new javax.swing.GroupLayout(omssaParametersPanel);
        omssaParametersPanel.setLayout(omssaParametersPanelLayout);
        omssaParametersPanelLayout.setHorizontalGroup(
            omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(omssaParametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(precursorChargeEstimationLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .addComponent(chargeReductionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .addComponent(precursorMassScalingLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .addComponent(minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(minPrecChargeMultipleChargedFragmentsTxt)
                    .addComponent(eliminatePrecursorCombo, 0, 149, Short.MAX_VALUE)
                    .addComponent(precursorScalingCombo, 0, 1, Short.MAX_VALUE)
                    .addComponent(chargeEstimationCombo, 0, 1, Short.MAX_VALUE))
                .addContainerGap())
        );
        omssaParametersPanelLayout.setVerticalGroup(
            omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(omssaParametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel)
                    .addComponent(minPrecChargeMultipleChargedFragmentsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chargeReductionLabel)
                    .addComponent(eliminatePrecursorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(precursorMassScalingLabel)
                    .addComponent(precursorScalingCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(omssaParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(precursorChargeEstimationLabel)
                    .addComponent(chargeEstimationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters in Semi-Enzymatic Mode"));

        maxPepLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPepLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPepLengthTxtKeyReleased(evt);
            }
        });

        peptideLengthDividerLabel.setText("-");

        minPepLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPepLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPepLengthTxtKeyReleased(evt);
            }
        });

        peptideLengthJLabel.setText("Peptide Length (min - max)");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(peptideLengthJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(peptideLengthDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptideLengthJLabel)
                    .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideLengthDividerLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Parameters"));

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(omssaOutputFormatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                        .addGap(215, 215, 215)
                        .addComponent(omssaOutputFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(eValueLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(hitListLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(hitlistTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                            .addComponent(maxEvalueTxt))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eValueLbl)
                    .addComponent(maxEvalueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hitListLbl)
                    .addComponent(hitlistTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(omssaOutputFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(omssaOutputFormatLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(omssaParametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(omssaParametersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(okButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void minPrecChargeMultipleChargedFragmentsTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecChargeMultipleChargedFragmentsTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPrecChargeMultipleChargedFragmentsTxtKeyReleased

    private void minPepLengthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPepLengthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPepLengthTxtKeyReleased

    private void maxPepLengthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPepLengthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPepLengthTxtKeyReleased

    private void omssaOutputFormatComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_omssaOutputFormatComboBoxActionPerformed
        if (((String) omssaOutputFormatComboBox.getSelectedItem()).equalsIgnoreCase("OMSSA CSV") && this.isVisible()) {

            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(OmssaParametersDialog.this, JOptionEditorPane.getJOptionEditorPane(
                            "Note that the OMSSA CSV format is not compatible with <a href=\"http://www.peptide-shaker.googlecode.com\">PeptideShaker</a>."),
                            "Format Warning", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }//GEN-LAST:event_omssaOutputFormatComboBoxActionPerformed

    private void maxEvalueTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxEvalueTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxEvalueTxtKeyReleased

    private void hitlistTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hitlistTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_hitlistTxtKeyReleased

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput(true)) {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        cancelled = true;
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;
        eValueLbl.setForeground(Color.BLACK);
        hitListLbl.setForeground(Color.BLACK);
        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setForeground(Color.BLACK);
        peptideLengthJLabel.setForeground(Color.BLACK);

        eValueLbl.setToolTipText(null);
        hitListLbl.setToolTipText(null);
        minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setToolTipText(null);
        peptideLengthJLabel.setToolTipText(null);

        // Validate e-value cutoff
        if (maxEvalueTxt.getText() == null || maxEvalueTxt.getText().trim().equals("")) {
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
            eValue = Float.parseFloat(maxEvalueTxt.getText().trim());
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

        // Validate maximum hitlist
        if (hitlistTxt.getText() == null || hitlistTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a hit-list length.",
                        "Hit-List Length Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            hitListLbl.setForeground(Color.RED);
            hitListLbl.setToolTipText("Please select a hit-list length");
        }

        // OK, see if it is an integer.
        int hitList = -1;

        try {
            hitList = Integer.parseInt(hitlistTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the hit-list length.",
                        "Hit-List Length Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            hitListLbl.setForeground(Color.RED);
            hitListLbl.setToolTipText("Please select a positive integer");
        }

        // And it should be greater than 0.
        if (hitList < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the hit-list length.",
                        "Incorrect hit-list length found!", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            hitListLbl.setForeground(Color.RED);
            hitListLbl.setToolTipText("Please select a positive integer");
        }

        // Validate minimum charge to consider multiply charged fragments
        if (minPrecChargeMultipleChargedFragmentsTxt.getText() == null || minPrecChargeMultipleChargedFragmentsTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a minimum charge to start considering multiply charged fragments.",
                        "Multiply Charged Fragments Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setForeground(Color.RED);
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setToolTipText("Please select a minimum charge to start considering multiply charged fragments");
        }

        // OK, see if it is an integer.
        int minChargeForMultiplyCharged = -1;

        try {
            minChargeForMultiplyCharged = Integer.parseInt(minPrecChargeMultipleChargedFragmentsTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the minimum charge to start considering multiply charged fragments.",
                        "Multiply Charged Fragments Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setForeground(Color.RED);
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setToolTipText("Please select a positive integer");
        }

        // and it should be zero or more
        if (minChargeForMultiplyCharged < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the minimum charge to start considering multiply charged fragments.",
                        "Multiply Charged Fragments Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setForeground(Color.RED);
            minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel.setToolTipText("Please select a positive integer");
        }

        // Validate peptide minimal size
        if (minPepLengthTxt.getText() != null && !minPepLengthTxt.getText().trim().equals("")) {

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
        }

        // Validate peptide maximum size
        if (maxPepLengthTxt.getText() != null && !maxPepLengthTxt.getText().trim().equals("")) {

            // OK, see if it is an integer.
            int length = 0;
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
                peptideLengthJLabel.setToolTipText("Please select a positive integers");
            }
        }

        return valid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox chargeEstimationCombo;
    private javax.swing.JLabel chargeReductionLabel;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel eValueLbl;
    private javax.swing.JComboBox eliminatePrecursorCombo;
    private javax.swing.JLabel hitListLbl;
    private javax.swing.JTextField hitlistTxt;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField maxEvalueTxt;
    private javax.swing.JTextField maxPepLengthTxt;
    private javax.swing.JTextField minPepLengthTxt;
    private javax.swing.JTextField minPrecChargeMultipleChargedFragmentsTxt;
    private javax.swing.JLabel minPrecursorChargeConsideredMultiplyChargedFragmentsJLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox omssaOutputFormatComboBox;
    private javax.swing.JLabel omssaOutputFormatLabel;
    private javax.swing.JPanel omssaParametersPanel;
    private javax.swing.JLabel peptideLengthDividerLabel;
    private javax.swing.JLabel peptideLengthJLabel;
    private javax.swing.JLabel precursorChargeEstimationLabel;
    private javax.swing.JLabel precursorMassScalingLabel;
    private javax.swing.JComboBox precursorScalingCombo;
    // End of variables declaration//GEN-END:variables
}
