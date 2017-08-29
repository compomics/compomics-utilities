package com.compomics.util.gui.parameters.identification.subparameters;

import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import java.awt.Dialog;
import java.awt.Toolkit;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * The PeptideShaker import settings dialog.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MatchesImportParametersDialog extends javax.swing.JDialog {

    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates a new ImportSettingsDialog with a frame as owner.
     *
     * @param parentFrame the parent frame
     * @param idFilter the identification filter
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public MatchesImportParametersDialog(java.awt.Frame parentFrame, PeptideAssumptionFilter idFilter, boolean editable) {
        super(parentFrame, true);
        this.editable = editable;
        setUpGui();
        populateGUI(idFilter);
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new ImportSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame the parent frame
     * @param idFilter the identification filter
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public MatchesImportParametersDialog(Dialog owner, java.awt.Frame parentFrame, PeptideAssumptionFilter idFilter, boolean editable) {
        super(owner, true);
        this.editable = editable;
        setUpGui();
        populateGUI(idFilter);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {

        initComponents();

        unitCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        nAAminTxt.setEditable(editable);
        nAAminTxt.setEnabled(editable);
        nAAmaxTxt.setEditable(editable);
        nAAmaxTxt.setEnabled(editable);
        precDevTxt.setEditable(editable);
        precDevTxt.setEnabled(editable);
        unitCmb.setEnabled(editable);
        unitCmb.setEnabled(editable);
        ptmsCheck.setEnabled(editable);
        minMissedCleavagesTxt.setEditable(editable);
        minMissedCleavagesTxt.setEnabled(editable);
        maxMissedCleavagesTxt.setEditable(editable);
        maxMissedCleavagesTxt.setEnabled(editable);
        minIsotopesTxt.setEditable(editable);
        minIsotopesTxt.setEnabled(editable);
        maxIsotopesTxt.setEditable(editable);
        maxIsotopesTxt.setEnabled(editable);
    }

    /**
     * Populates the GUI with the values from the given filter.
     *
     * @param idFilter the filter to display
     */
    private void populateGUI(PeptideAssumptionFilter idFilter) {

        int intValue = idFilter.getMinPepLength();
        if (intValue > 0) {
            nAAminTxt.setText(idFilter.getMinPepLength() + "");
        }
        intValue = idFilter.getMaxPepLength();
        if (intValue > 0) {
            nAAmaxTxt.setText(idFilter.getMaxPepLength() + "");
        }
        double doubleValue = idFilter.getMaxMzDeviation();
        if (doubleValue > 0) {
            precDevTxt.setText(idFilter.getMaxMzDeviation() + "");
        }
        ptmsCheck.setSelected(idFilter.removeUnknownPTMs());

        if (idFilter.isIsPpm()) {
            unitCmb.setSelectedIndex(0);
        } else {
            unitCmb.setSelectedIndex(1);
        }

        if (idFilter.getMinMissedCleavages() != null) {
            minMissedCleavagesTxt.setText(idFilter.getMinMissedCleavages() + "");
        }
        if (idFilter.getMaxMissedCleavages() != null) {
            maxMissedCleavagesTxt.setText(idFilter.getMaxMissedCleavages() + "");
        }

        if (idFilter.getMinIsotopes() != null) {
            minIsotopesTxt.setText(idFilter.getMinIsotopes() + "");
        }
        if (idFilter.getMaxIsotopes() != null) {
            maxIsotopesTxt.setText(idFilter.getMaxIsotopes() + "");
        }
    }

    /**
     * Indicates whether the input is correct.
     *
     * @return a boolean indicating whether the input is correct
     */
    private boolean validateInput() {
        try {
            String input = nAAminTxt.getText();
            if (!input.equals("")) {
                new Integer(input);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Please verify the input for the minimal peptide length.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            String input = nAAmaxTxt.getText();
            if (!input.equals("")) {
                new Integer(input);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Please verify the input for the maximal peptide length.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            String input = precDevTxt.getText();
            if (!input.equals("")) {
                new Double(input);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Please verify the input for the precursor maximal deviation.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Integer minCleavages = null;
        try {
            String input = minMissedCleavagesTxt.getText();
            if (!input.equals("")) {
                minCleavages = new Integer(input);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Please verify the input for the minimum number of missed cleavages.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Integer maxCleavages = null;
        try {
            String input = maxMissedCleavagesTxt.getText();
            if (!input.equals("")) {
                maxCleavages = new Integer(input);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Please verify the input for the maximum number of missed cleavages.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (minCleavages != null && maxCleavages != null && maxCleavages <= minCleavages) {
            JOptionPane.showMessageDialog(null, "The maximum number of missed cleavages must be higher than the minimum.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Integer minIsotopes = null;
        try {
            String input = minIsotopesTxt.getText();
            if (!input.equals("")) {
                minIsotopes = new Integer(input);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Please verify the input for the minimum number of isotopes.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Integer maxIsotopes = null;
        try {
            String input = maxIsotopesTxt.getText();
            if (!input.equals("")) {
                maxIsotopes = new Integer(input);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Please verify the input for the maximum number of isotopes.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (minIsotopes != null && maxIsotopes != null && maxIsotopes <= minIsotopes) {
            JOptionPane.showMessageDialog(null, "The maximum number of isotopes must be higher than the minimum.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
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
     * Returns the id filter as set by the user.
     *
     * @return the id filter as set by the user
     */
    public PeptideAssumptionFilter getFilter() {

        int nAAmin = -1;
        String input = nAAminTxt.getText();
        if (!input.equals("")) {
            nAAmin = new Integer(input);
        }

        int nAAmax = -1;
        input = nAAmaxTxt.getText();
        if (!input.equals("")) {
            nAAmax = new Integer(input);
        }

        double precDev = -1;
        input = precDevTxt.getText();
        if (!input.equals("")) {
            precDev = new Double(input);
        }

        boolean ppm = unitCmb.getSelectedIndex() == 0;
        boolean removePTM = ptmsCheck.isSelected();

        Integer minMissedCleavages = null;
        input = minMissedCleavagesTxt.getText();
        if (!input.equals("")) {
            minMissedCleavages = new Integer(input);
        }

        Integer maxMissedCleavages = null;
        input = maxMissedCleavagesTxt.getText();
        if (!input.equals("")) {
            maxMissedCleavages = new Integer(input);
        }

        Integer minIsotopes = null;
        input = minIsotopesTxt.getText();
        if (!input.equals("")) {
            minIsotopes = new Integer(input);
        }

        Integer maxIsotopes = null;
        input = maxIsotopesTxt.getText();
        if (!input.equals("")) {
            maxIsotopes = new Integer(input);
        }

        PeptideAssumptionFilter idFilter = new PeptideAssumptionFilter(
                nAAmin,
                nAAmax,
                precDev,
                ppm,
                removePTM,
                minMissedCleavages,
                maxMissedCleavages,
                minIsotopes,
                maxIsotopes
        );

        return idFilter;
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
        filterPanel = new javax.swing.JPanel();
        nAAmaxTxt = new javax.swing.JTextField();
        peptideLengthRangeLabel = new javax.swing.JLabel();
        nAAminTxt = new javax.swing.JTextField();
        peptideLengthLabel = new javax.swing.JLabel();
        unitCmb = new javax.swing.JComboBox();
        precDevTxt = new javax.swing.JTextField();
        precursorAccuracyLabel = new javax.swing.JLabel();
        ptmsCheck = new javax.swing.JCheckBox();
        missedCleavagesLabel = new javax.swing.JLabel();
        minMissedCleavagesTxt = new javax.swing.JTextField();
        missedCleavagesRangeLabel = new javax.swing.JLabel();
        maxMissedCleavagesTxt = new javax.swing.JTextField();
        isotopesLbl = new javax.swing.JLabel();
        minIsotopesTxt = new javax.swing.JTextField();
        isotopesRangeLabel = new javax.swing.JLabel();
        maxIsotopesTxt = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Import Filters");
        setBackground(new java.awt.Color(230, 230, 230));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Filters"));
        filterPanel.setOpaque(false);

        nAAmaxTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        peptideLengthRangeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        peptideLengthRangeLabel.setText("-");

        nAAminTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        peptideLengthLabel.setText("Peptide Length");

        unitCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ppm", "Da" }));

        precDevTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        precursorAccuracyLabel.setText("Precursor m/z Deviation");

        ptmsCheck.setText("Exclude Unknown PTMs");
        ptmsCheck.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ptmsCheck.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        ptmsCheck.setIconTextGap(10);
        ptmsCheck.setMargin(new java.awt.Insets(2, 0, 2, 2));

        missedCleavagesLabel.setText("Missed Cleavages");

        minMissedCleavagesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        missedCleavagesRangeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        missedCleavagesRangeLabel.setText("-");

        maxMissedCleavagesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        isotopesLbl.setText("Isotopes");

        minIsotopesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        isotopesRangeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        isotopesRangeLabel.setText("-");

        maxIsotopesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterPanelLayout.createSequentialGroup()
                        .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(peptideLengthLabel)
                            .addComponent(precursorAccuracyLabel)
                            .addComponent(missedCleavagesLabel)
                            .addComponent(isotopesLbl))
                        .addGap(27, 27, 27)
                        .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(filterPanelLayout.createSequentialGroup()
                                .addComponent(minIsotopesTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(isotopesRangeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(filterPanelLayout.createSequentialGroup()
                                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(precDevTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                                    .addComponent(nAAminTxt))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(peptideLengthRangeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(filterPanelLayout.createSequentialGroup()
                                .addComponent(minMissedCleavagesTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(missedCleavagesRangeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxIsotopesTxt)
                            .addComponent(maxMissedCleavagesTxt)
                            .addComponent(nAAmaxTxt)
                            .addComponent(unitCmb, 0, 82, Short.MAX_VALUE)))
                    .addGroup(filterPanelLayout.createSequentialGroup()
                        .addComponent(ptmsCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nAAmaxTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nAAminTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideLengthLabel)
                    .addComponent(peptideLengthRangeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(unitCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(precDevTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(precursorAccuracyLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxMissedCleavagesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minMissedCleavagesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(missedCleavagesLabel)
                    .addComponent(missedCleavagesRangeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxIsotopesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minIsotopesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(isotopesLbl)
                    .addComponent(isotopesRangeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ptmsCheck))
        );

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

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.setBorder(null);
        helpJButton.setBorderPainted(false);
        helpJButton.setContentAreaFilled(false);
        helpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                helpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                helpJButtonMouseExited(evt);
            }
        });
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(filterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
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
     * Closes the dialog.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Update the settings.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (editable) {
            if (validateInput()) {
                dispose();
            }
        } else {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/FilterSettings.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                "Import Filters - Help", 500, 10); // @TODO: reduce height??
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Cancel the dialog.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        canceled = true;
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel isotopesLbl;
    private javax.swing.JLabel isotopesRangeLabel;
    private javax.swing.JTextField maxIsotopesTxt;
    private javax.swing.JTextField maxMissedCleavagesTxt;
    private javax.swing.JTextField minIsotopesTxt;
    private javax.swing.JTextField minMissedCleavagesTxt;
    private javax.swing.JLabel missedCleavagesLabel;
    private javax.swing.JLabel missedCleavagesRangeLabel;
    private javax.swing.JTextField nAAmaxTxt;
    private javax.swing.JTextField nAAminTxt;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel peptideLengthLabel;
    private javax.swing.JLabel peptideLengthRangeLabel;
    private javax.swing.JTextField precDevTxt;
    private javax.swing.JLabel precursorAccuracyLabel;
    private javax.swing.JCheckBox ptmsCheck;
    private javax.swing.JComboBox unitCmb;
    // End of variables declaration//GEN-END:variables
}
