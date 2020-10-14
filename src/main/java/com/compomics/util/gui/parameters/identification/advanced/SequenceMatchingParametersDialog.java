package com.compomics.util.gui.parameters.identification.advanced;

import com.compomics.util.gui.GuiUtilities;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.awt.Dialog;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingConstants;

/**
 * Dialog for the edition of the sequence matching settings.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SequenceMatchingParametersDialog extends javax.swing.JDialog {

    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates a new SequenceMatchingSettingsDialog with a frame as owner.
     *
     * @param parentFrame a parent frame
     * @param sequenceMatchingPreferences the sequence matching preferences to
     * display
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SequenceMatchingParametersDialog(
            java.awt.Frame parentFrame,
            SequenceMatchingParameters sequenceMatchingPreferences,
            boolean editable
    ) {

        super(parentFrame, true);

        this.parentFrame = parentFrame;
        this.editable = editable;

        initComponents();
        setUpGui();
        populateGUI(sequenceMatchingPreferences);

        setLocationRelativeTo(parentFrame);
        setVisible(true);

    }

    /**
     * Creates a new SequenceMatchingSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame a parent frame
     * @param sequenceMatchingPreferences the sequence matching preferences to
     * display
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SequenceMatchingParametersDialog(
            Dialog owner, java.awt.Frame parentFrame,
            SequenceMatchingParameters sequenceMatchingPreferences,
            boolean editable
    ) {

        super(owner, true);

        this.parentFrame = parentFrame;
        this.editable = editable;

        initComponents();
        setUpGui();
        populateGUI(sequenceMatchingPreferences);

        setLocationRelativeTo(owner);
        setVisible(true);

    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {

        matchingCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        tagMatchingCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        matchingCmb.setEnabled(editable);
        xSpinner.setEnabled(editable);
        tagMatchingCmb.setEnabled(editable);
        maxPtmsPerTagTextField.setEnabled(editable);

    }

    /**
     * Fills the GUI with the given settings.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences to
     * display
     */
    private void populateGUI(SequenceMatchingParameters sequenceMatchingPreferences) {

        SequenceMatchingParameters.MatchingType matchingType = sequenceMatchingPreferences.getSequenceMatchingType();
        matchingCmb.setSelectedItem(matchingType);
        xSpinner.setValue(sequenceMatchingPreferences.getLimitX());

        if (sequenceMatchingPreferences.isEnzymaticTagsOnly()) {
            tagMatchingCmb.setSelectedIndex(1);
        } else {
            tagMatchingCmb.setSelectedIndex(0);
        }

        maxPtmsPerTagTextField.setText(sequenceMatchingPreferences.getMaxPtmsPerTagPeptide() + "");
        minAaScoreSpinner.setValue(sequenceMatchingPreferences.getMinAminoAcidScore());
        minTagLengthTextField.setText(sequenceMatchingPreferences.getMinTagLength() + "");

    }

    /**
     * Indicates whether the user canceled the editing.
     *
     * @return a boolean indicating whether the user canceled the editing
     */
    public boolean isCanceled() {

        return canceled;

    }

    /**
     * Returns the sequence matching settings as set by the user.
     *
     * @return the sequence matching settings as set by the user
     */
    public SequenceMatchingParameters getSequenceMatchingPreferences() {

        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType((SequenceMatchingParameters.MatchingType) matchingCmb.getSelectedItem());
        sequenceMatchingPreferences.setLimitX((Double) xSpinner.getValue());
        sequenceMatchingPreferences.setEnzymaticTagsOnly(tagMatchingCmb.getSelectedIndex() == 1);
        sequenceMatchingPreferences.setMaxPtmsPerTagPeptide(Integer.parseInt(maxPtmsPerTagTextField.getText()));
        sequenceMatchingPreferences.setMinAminoAcidScore((Integer) minAaScoreSpinner.getValue());
        sequenceMatchingPreferences.setMinTagLength(Integer.parseInt(minTagLengthTextField.getText()));
        return sequenceMatchingPreferences;

    }

    /**
     * Validates the user input.
     *
     * @param showMessage if true an error messages are shown to the users
     *
     * @return a boolean indicating whether the user input is valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;
        
        valid = GuiUtilities.validateIntegerInput(
                this, maxPtmsPerTagLbl, maxPtmsPerTagTextField,
                "max PTMs per tag", "Max PTMs per Tag Error",
                true, showMessage, valid);
        
        valid = GuiUtilities.validateIntegerInput(
                this, minTagLengthLbl, minTagLengthTextField,
                "min tag length", "Min Tag Length Error",
                true, showMessage, valid);

        okButton.setEnabled(valid);

        return valid;

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
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        sequenceMatchingPanel = new javax.swing.JPanel();
        matchingMethodLbl = new javax.swing.JLabel();
        xLbl = new javax.swing.JLabel();
        matchingCmb = new javax.swing.JComboBox();
        xSpinner = new javax.swing.JSpinner();
        tagMatchingLbl = new javax.swing.JLabel();
        tagMatchingCmb = new javax.swing.JComboBox();
        maxPtmsPerTagLbl = new javax.swing.JLabel();
        maxPtmsPerTagTextField = new javax.swing.JTextField();
        minAaScoreLbl = new javax.swing.JLabel();
        minAaScoreSpinner = new javax.swing.JSpinner();
        minTagLengthLbl = new javax.swing.JLabel();
        minTagLengthTextField = new javax.swing.JTextField();
        annotationPreferencesHelpJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sequence Matching");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        sequenceMatchingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));
        sequenceMatchingPanel.setOpaque(false);

        matchingMethodLbl.setText("Matching Method");

        xLbl.setText("Maximum Share of X's");

        matchingCmb.setModel(new DefaultComboBoxModel(SequenceMatchingParameters.MatchingType.values()));

        xSpinner.setModel(new javax.swing.SpinnerNumberModel(0.25d, 0.0d, 1.0d, 0.1d));

        tagMatchingLbl.setText("Tag Matching");

        tagMatchingCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Enzymatic" }));

        maxPtmsPerTagLbl.setText("Max PTMs per Tag");
        maxPtmsPerTagLbl.setToolTipText("Maximum number of PTMs considered when mapping tags to peptides");

        maxPtmsPerTagTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPtmsPerTagTextField.setText("3");
        maxPtmsPerTagTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPtmsPerTagTextFieldKeyReleased(evt);
            }
        });

        minAaScoreLbl.setText("Min Amino Acid Score");

        minAaScoreSpinner.setModel(new javax.swing.SpinnerNumberModel(30, 0, 100, 1));

        minTagLengthLbl.setText("Min Tag Length");
        minTagLengthLbl.setToolTipText("Maximum number of PTMs considered when mapping tags to peptides");

        minTagLengthTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minTagLengthTextField.setText("3");
        minTagLengthTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minTagLengthTextFieldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout sequenceMatchingPanelLayout = new javax.swing.GroupLayout(sequenceMatchingPanel);
        sequenceMatchingPanel.setLayout(sequenceMatchingPanelLayout);
        sequenceMatchingPanelLayout.setHorizontalGroup(
            sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sequenceMatchingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(matchingMethodLbl)
                    .addComponent(xLbl)
                    .addComponent(tagMatchingLbl)
                    .addComponent(maxPtmsPerTagLbl)
                    .addComponent(minAaScoreLbl)
                    .addComponent(minTagLengthLbl))
                .addGap(18, 18, 18)
                .addGroup(sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(minTagLengthTextField)
                    .addComponent(minAaScoreSpinner)
                    .addComponent(tagMatchingCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(matchingCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                    .addComponent(maxPtmsPerTagTextField))
                .addContainerGap())
        );
        sequenceMatchingPanelLayout.setVerticalGroup(
            sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sequenceMatchingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(matchingMethodLbl)
                    .addComponent(matchingCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xLbl)
                    .addComponent(xSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tagMatchingLbl)
                    .addComponent(tagMatchingCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxPtmsPerTagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPtmsPerTagLbl))
                .addGap(0, 0, 0)
                .addGroup(sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minAaScoreLbl)
                    .addComponent(minAaScoreSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(sequenceMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minTagLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minTagLengthLbl))
                .addContainerGap())
        );

        annotationPreferencesHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        annotationPreferencesHelpJButton.setToolTipText("Help");
        annotationPreferencesHelpJButton.setBorder(null);
        annotationPreferencesHelpJButton.setBorderPainted(false);
        annotationPreferencesHelpJButton.setContentAreaFilled(false);
        annotationPreferencesHelpJButton.setFocusable(false);
        annotationPreferencesHelpJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        annotationPreferencesHelpJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        annotationPreferencesHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                annotationPreferencesHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                annotationPreferencesHelpJButtonMouseExited(evt);
            }
        });
        annotationPreferencesHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                annotationPreferencesHelpJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(annotationPreferencesHelpJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(sequenceMatchingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sequenceMatchingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(annotationPreferencesHelpJButton)
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
     * Close the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed

        canceled = true;
        dispose();

    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Close the dialog without saving.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        canceled = true;

    }//GEN-LAST:event_formWindowClosing

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void annotationPreferencesHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_annotationPreferencesHelpJButtonMouseEntered

        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

    }//GEN-LAST:event_annotationPreferencesHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void annotationPreferencesHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_annotationPreferencesHelpJButtonMouseExited

        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

    }//GEN-LAST:event_annotationPreferencesHelpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void annotationPreferencesHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotationPreferencesHelpJButtonActionPerformed

        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(parentFrame, getClass().getResource("/helpFiles/SequenceMatchingPreferences.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
                "Sequence Matching - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

    }//GEN-LAST:event_annotationPreferencesHelpJButtonActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxPtmsPerTagTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPtmsPerTagTextFieldKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPtmsPerTagTextFieldKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minTagLengthTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minTagLengthTextFieldKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minTagLengthTextFieldKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton annotationPreferencesHelpJButton;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox matchingCmb;
    private javax.swing.JLabel matchingMethodLbl;
    private javax.swing.JLabel maxPtmsPerTagLbl;
    private javax.swing.JTextField maxPtmsPerTagTextField;
    private javax.swing.JLabel minAaScoreLbl;
    private javax.swing.JSpinner minAaScoreSpinner;
    private javax.swing.JLabel minTagLengthLbl;
    private javax.swing.JTextField minTagLengthTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel sequenceMatchingPanel;
    private javax.swing.JComboBox tagMatchingCmb;
    private javax.swing.JLabel tagMatchingLbl;
    private javax.swing.JLabel xLbl;
    private javax.swing.JSpinner xSpinner;
    // End of variables declaration//GEN-END:variables

}
