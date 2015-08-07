package com.compomics.util.preferences.gui;

import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * The PeptideShaker import settings dialog.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ImportSettingsDialog extends javax.swing.JDialog {

    /**
     * If true the user can edit the settings.
     */
    private boolean editable;
    /**
     * The identification filter set by the user. Null if cancel was pressed.
     */
    private PeptideAssumptionFilter userFilter = null;
    /**
     * The original filter.
     */
    private PeptideAssumptionFilter originalFilter;

    /**
     * Creates a new ImportSettingsDialog.
     *
     * @param parent the parent frame
     * @param idFilter the identification filter
     * @param editable boolean indicating whether the parameters can be editable
     */
    public ImportSettingsDialog(JFrame parent, PeptideAssumptionFilter idFilter, boolean editable) {
        super(parent, true);
        this.editable = editable;
        this.originalFilter = idFilter;
        setUpGui();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates a new ImportSettingsDialog.
     *
     * @param parent the parent dialog
     * @param idFilter the identification filter
     * @param editable boolean indicating whether the parameters can be editable
     */
    public ImportSettingsDialog(JDialog parent, PeptideAssumptionFilter idFilter, boolean editable) {
        super(parent, true);
        this.editable = editable;
        this.originalFilter = idFilter;
        setUpGui();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {

        initComponents();

        unitCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        int intValue = originalFilter.getMinPepLength();
        if (intValue > 0) {
            nAAminTxt.setText(originalFilter.getMinPepLength() + "");
        }
        intValue = originalFilter.getMaxPepLength();
        if (intValue > 0) {
            nAAmaxTxt.setText(originalFilter.getMaxPepLength() + "");
        }
        double doubleValue = originalFilter.getMaxMzDeviation();
        if (doubleValue > 0) {
            precDevTxt.setText(originalFilter.getMaxMzDeviation() + "");
        }
        ptmsCheck.setSelected(originalFilter.removeUnknownPTMs());

        if (originalFilter.isIsPpm()) {
            unitCmb.setSelectedIndex(0);
        } else {
            unitCmb.setSelectedIndex(1);
        }

        nAAminTxt.setEditable(editable);
        nAAmaxTxt.setEditable(editable);
        precDevTxt.setEditable(editable);
        unitCmb.setEnabled(editable);
        cancelButton.setEnabled(editable);
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
        return true;
    }

    /**
     * Returns the id filter as set by the user. Null if the user canceled the
     * editing or did not make any change.
     *
     * @return the id filter as set by the user
     */
    public PeptideAssumptionFilter getFilter() {
        return userFilter;
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
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Import Filters");
        setBackground(new java.awt.Color(230, 230, 230));

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

        precursorAccuracyLabel.setText("Precursor Accuracy");

        ptmsCheck.setText("Exclude Unknown PTMs");
        ptmsCheck.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ptmsCheck.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        ptmsCheck.setIconTextGap(10);
        ptmsCheck.setMargin(new java.awt.Insets(2, 0, 2, 2));
        ptmsCheck.setOpaque(false);

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(unitCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(filterPanelLayout.createSequentialGroup()
                            .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(peptideLengthLabel)
                                .addComponent(precursorAccuracyLabel))
                            .addGap(27, 27, 27)
                            .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(precDevTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(nAAminTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(peptideLengthRangeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(nAAmaxTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(ptmsCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
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
                .addComponent(ptmsCheck)
                .addContainerGap())
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
                        .addGap(138, 138, 138)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(helpJButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(okButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
     * Closes the dialog.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
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

                PeptideAssumptionFilter tempFilter = new PeptideAssumptionFilter(
                        nAAmin,
                        nAAmax,
                        precDev,
                        ppm,
                        removePTM);

                if (!tempFilter.equals(originalFilter)) {
                    userFilter = tempFilter;
                }

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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JButton helpJButton;
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
