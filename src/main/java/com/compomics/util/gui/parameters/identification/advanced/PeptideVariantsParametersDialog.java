package com.compomics.util.gui.parameters.identification.advanced;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.gui.variants.aa_substitutions.AaSubstitutionMatrixTableModel;
import com.compomics.util.parameters.identification.advanced.PeptideVariantsParameters;
import com.compomics.util.parameters.identification.advanced.PeptideVariantsParameters.VariantType;
import java.awt.Dialog;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;

/**
 * FractionSettingsDialog.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PeptideVariantsParametersDialog extends javax.swing.JDialog {

    /**
     * Empty default constructor
     */
    public PeptideVariantsParametersDialog() {
    }

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
     * Creates a new FractionSettingsDialog with a frame as owner.
     *
     * @param parentFrame a parent frame
     * @param peptideVariantPreferences the peptide variants settings
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public PeptideVariantsParametersDialog(java.awt.Frame parentFrame, PeptideVariantsParameters peptideVariantPreferences, boolean editable) {
        super(parentFrame, true);
        this.parentFrame = parentFrame;
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(peptideVariantPreferences);
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new FractionSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame a parent frame
     * @param peptideVariantPreferences the peptide variants settings
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public PeptideVariantsParametersDialog(Dialog owner, java.awt.Frame parentFrame, PeptideVariantsParameters peptideVariantPreferences, boolean editable) {
        super(owner, true);
        this.parentFrame = parentFrame;
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(peptideVariantPreferences);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGui() {
        totalVariantsSpinner.setEnabled(editable);
        specificVariantsComboBox.setEnabled(editable);
        specificVariantsComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        deletionsSpinner.setEnabled(editable);
        insertionsSpinner.setEnabled(editable);
        subsitutionsSpinner.setEnabled(editable);
        swapSpinner.setEnabled(editable);
        substitutionMatrixComboBox.setEnabled(editable);
        substitutionMatrixComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        setTableProperties();
    }

    /**
     * Sets the table properties.
     */
    private void setTableProperties() {
        TableColumnModel tableColumnModel = substitutionMatrixTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(50);
        substitutionMatrixTableJScrollPane.getViewport().setOpaque(false);
        for (int i = 0; i < AminoAcid.getUniqueAminoAcids().length; i++) {
            tableColumnModel.getColumn(i + 1).setCellRenderer(new TrueFalseIconRenderer(
                    new ImageIcon(this.getClass().getResource("/icons/selected_green.png")),
                    null,
                    "On", "Off"));
        }
    }

    /**
     * Fills the GUI with the given settings.
     *
     * @param fractionSettings the fraction settings to display
     */
    private void populateGUI(PeptideVariantsParameters peptideVariantPreferences) {

        totalVariantsSpinner.setValue(peptideVariantPreferences.getnVariants());
        VariantType variantType = peptideVariantPreferences.getVariantType();
        specificVariantsComboBox.setSelectedIndex(variantType.ordinal());
       
        
        if (editable) {
            deletionsSpinner.setEnabled(variantType == VariantType.SPECIFIC);
            insertionsSpinner.setEnabled(variantType == VariantType.SPECIFIC);
            subsitutionsSpinner.setEnabled(variantType == VariantType.SPECIFIC);
            swapSpinner.setEnabled(variantType == VariantType.SPECIFIC);
        }

        deletionsSpinner.setValue(peptideVariantPreferences.getnAaDeletions());
        insertionsSpinner.setValue(peptideVariantPreferences.getnAaInsertions());
        subsitutionsSpinner.setValue(peptideVariantPreferences.getnAaSubstitutions());
        swapSpinner.setValue(peptideVariantPreferences.getnAaSwap());

        AaSubstitutionMatrix aaSubstitutionMatrix = peptideVariantPreferences.getAaSubstitutionMatrix();
        substitutionMatrixComboBox.setSelectedItem(aaSubstitutionMatrix);
        updateTableContent(aaSubstitutionMatrix);

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
     * Returns the peptide variants settings as set by the user.
     *
     * @return the peptide variants settings as set by the user
     */
    public PeptideVariantsParameters getPeptideVariantsPreferences() {
        PeptideVariantsParameters peptideVariantsPreferences = new PeptideVariantsParameters();
        peptideVariantsPreferences.setnVariants((Integer) totalVariantsSpinner.getValue());
        peptideVariantsPreferences.setVatiantType(VariantType.values()[specificVariantsComboBox.getSelectedIndex()]);
        peptideVariantsPreferences.setnAaDeletions((Integer) deletionsSpinner.getValue());
        peptideVariantsPreferences.setnAaInsertions((Integer) insertionsSpinner.getValue());
        peptideVariantsPreferences.setnAaSubstitutions((Integer) subsitutionsSpinner.getValue());
        peptideVariantsPreferences.setnAaSwap((Integer) swapSpinner.getValue());
        peptideVariantsPreferences.setAaSubstitutionMatrix((AaSubstitutionMatrix) substitutionMatrixComboBox.getSelectedItem());
        return peptideVariantsPreferences;
    }

    /**
     * Updates the content of the table with the given substitution matrix.
     *
     * @param aaSubstitutionMatrix the substitution matrix to display
     */
    private void updateTableContent(AaSubstitutionMatrix aaSubstitutionMatrix) {

        ((AaSubstitutionMatrixTableModel) substitutionMatrixTable.getModel()).setAaSubstitutionMatrix(aaSubstitutionMatrix);
        updateTableContent();
    }

    /**
     * Updates the content of the table.
     */
    private void updateTableContent() {
        ((DefaultTableModel) substitutionMatrixTable.getModel()).fireTableDataChanged();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgrounPanel = new javax.swing.JPanel();
        maxVariantsJPanel = new javax.swing.JPanel();
        deletionsSpinner = new javax.swing.JSpinner();
        deletionsLbl = new javax.swing.JLabel();
        deletionsLbl1 = new javax.swing.JLabel();
        insertionsSpinner = new javax.swing.JSpinner();
        deletionsLbl2 = new javax.swing.JLabel();
        subsitutionsSpinner = new javax.swing.JSpinner();
        deletionsLbl3 = new javax.swing.JLabel();
        swapSpinner = new javax.swing.JSpinner();
        totalVariantsLbl = new javax.swing.JLabel();
        totalVariantsSpinner = new javax.swing.JSpinner();
        specificVariantsLbl = new javax.swing.JLabel();
        specificVariantsComboBox = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        aaSubstitutionsTableJPanel = new javax.swing.JPanel();
        substitutionMatrixComboBox = new javax.swing.JComboBox();
        substitutionMatrixTableJScrollPane = new javax.swing.JScrollPane();
        substitutionMatrixTable = new javax.swing.JTable();
        helpJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Peptide Variants");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgrounPanel.setBackground(new java.awt.Color(230, 230, 230));

        maxVariantsJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Number of Variants Allowed per Peptide"));
        maxVariantsJPanel.setOpaque(false);

        deletionsSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        deletionsLbl.setText("Amino acid deletions");

        deletionsLbl1.setText("Amino acid insertions");

        insertionsSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        deletionsLbl2.setText("Amino acid substitutions");

        subsitutionsSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        deletionsLbl3.setText("Amino acid swap");

        swapSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        totalVariantsLbl.setText("Total");

        totalVariantsSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        specificVariantsLbl.setText("Variant Type");

        specificVariantsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Generic", "Specific", "Fixed" }));
        specificVariantsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specificVariantsComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout maxVariantsJPanelLayout = new javax.swing.GroupLayout(maxVariantsJPanel);
        maxVariantsJPanel.setLayout(maxVariantsJPanelLayout);
        maxVariantsJPanelLayout.setHorizontalGroup(
            maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxVariantsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(maxVariantsJPanelLayout.createSequentialGroup()
                        .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(totalVariantsLbl)
                            .addComponent(specificVariantsLbl))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(specificVariantsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(totalVariantsSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)))
                    .addGroup(maxVariantsJPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deletionsLbl)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, maxVariantsJPanelLayout.createSequentialGroup()
                                .addComponent(deletionsLbl1)
                                .addGap(15, 15, 15))
                            .addComponent(deletionsLbl2)
                            .addComponent(deletionsLbl3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 496, Short.MAX_VALUE)
                        .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(swapSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(subsitutionsSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deletionsSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(insertionsSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        maxVariantsJPanelLayout.setVerticalGroup(
            maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, maxVariantsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalVariantsLbl)
                    .addComponent(totalVariantsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(specificVariantsLbl)
                    .addComponent(specificVariantsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deletionsLbl)
                    .addComponent(deletionsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deletionsLbl1)
                    .addComponent(insertionsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deletionsLbl3)
                    .addComponent(swapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(maxVariantsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deletionsLbl2)
                    .addComponent(subsitutionsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        aaSubstitutionsTableJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Allowed Amino Acid Substitutions"));
        aaSubstitutionsTableJPanel.setOpaque(false);

        substitutionMatrixComboBox.setModel(new DefaultComboBoxModel(AaSubstitutionMatrix.defaultMutationMatrices));
        substitutionMatrixComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                substitutionMatrixComboBoxActionPerformed(evt);
            }
        });

        substitutionMatrixTable.setModel(new AaSubstitutionMatrixTableModel(null, false));
        substitutionMatrixTableJScrollPane.setViewportView(substitutionMatrixTable);

        javax.swing.GroupLayout aaSubstitutionsTableJPanelLayout = new javax.swing.GroupLayout(aaSubstitutionsTableJPanel);
        aaSubstitutionsTableJPanel.setLayout(aaSubstitutionsTableJPanelLayout);
        aaSubstitutionsTableJPanelLayout.setHorizontalGroup(
            aaSubstitutionsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aaSubstitutionsTableJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(aaSubstitutionsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(substitutionMatrixTableJScrollPane)
                    .addComponent(substitutionMatrixComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        aaSubstitutionsTableJPanelLayout.setVerticalGroup(
            aaSubstitutionsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aaSubstitutionsTableJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(substitutionMatrixComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(substitutionMatrixTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                .addContainerGap())
        );

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.setBorder(null);
        helpJButton.setBorderPainted(false);
        helpJButton.setContentAreaFilled(false);
        helpJButton.setFocusable(false);
        helpJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        helpJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
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

        javax.swing.GroupLayout backgrounPanelLayout = new javax.swing.GroupLayout(backgrounPanel);
        backgrounPanel.setLayout(backgrounPanelLayout);
        backgrounPanelLayout.setHorizontalGroup(
            backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgrounPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgrounPanelLayout.createSequentialGroup()
                        .addComponent(maxVariantsJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(7, 7, 7))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgrounPanelLayout.createSequentialGroup()
                        .addGroup(backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(aaSubstitutionsTableJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(backgrounPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(helpJButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton)))
                        .addContainerGap())))
        );
        backgrounPanelLayout.setVerticalGroup(
            backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgrounPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(maxVariantsJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aaSubstitutionsTableJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgrounPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(helpJButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgrounPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgrounPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Cancel the dialog.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Cancel the dialog.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        canceled = true;
    }//GEN-LAST:event_formWindowClosing

    private void substitutionMatrixComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_substitutionMatrixComboBoxActionPerformed
        AaSubstitutionMatrix aaSubstitutionMatrix = (AaSubstitutionMatrix) substitutionMatrixComboBox.getSelectedItem();
        updateTableContent(aaSubstitutionMatrix);
    }//GEN-LAST:event_substitutionMatrixComboBoxActionPerformed

    private void specificVariantsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specificVariantsComboBoxActionPerformed
        if (editable) {
            boolean useSpecific = specificVariantsComboBox.getSelectedIndex() == 0;
            deletionsSpinner.setEnabled(useSpecific);
            insertionsSpinner.setEnabled(useSpecific);
            subsitutionsSpinner.setEnabled(useSpecific);
            swapSpinner.setEnabled(useSpecific);
        }
    }//GEN-LAST:event_specificVariantsComboBoxActionPerformed

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
        new HelpDialog(parentFrame, getClass().getResource("/helpFiles/PeptideVariantsPreferences.html"),
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
            "PeptideShaker - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aaSubstitutionsTableJPanel;
    private javax.swing.JPanel backgrounPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel deletionsLbl;
    private javax.swing.JLabel deletionsLbl1;
    private javax.swing.JLabel deletionsLbl2;
    private javax.swing.JLabel deletionsLbl3;
    private javax.swing.JSpinner deletionsSpinner;
    private javax.swing.JButton helpJButton;
    private javax.swing.JSpinner insertionsSpinner;
    private javax.swing.JPanel maxVariantsJPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox specificVariantsComboBox;
    private javax.swing.JLabel specificVariantsLbl;
    private javax.swing.JSpinner subsitutionsSpinner;
    private javax.swing.JComboBox substitutionMatrixComboBox;
    private javax.swing.JTable substitutionMatrixTable;
    private javax.swing.JScrollPane substitutionMatrixTableJScrollPane;
    private javax.swing.JSpinner swapSpinner;
    private javax.swing.JLabel totalVariantsLbl;
    private javax.swing.JSpinner totalVariantsSpinner;
    // End of variables declaration//GEN-END:variables

}
