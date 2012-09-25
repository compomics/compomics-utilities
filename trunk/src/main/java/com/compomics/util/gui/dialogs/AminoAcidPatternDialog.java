package com.compomics.util.gui.dialogs;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * This dialog allows the design and test of amino-acid patterns. (see class
 * com.compomics.util.experiment.biology.AminoAcidPattern)
 *
 * @author Marc Vaudel
 */
public class AminoAcidPatternDialog extends javax.swing.JDialog {

    /**
     * The pattern displayed
     */
    private AminoAcidPattern pattern;
    /**
     * A boolean indicating whether the pattern can be edited
     */
    private boolean editable;
    /**
     * A boolean indicating whether the used clicked the cancel button
     */
    private boolean cancel = false;

    /**
     * Creates a new AminoAcidPatternDialog.
     *
     * @param parent
     * @param pattern
     * @param editable
     */
    public AminoAcidPatternDialog(java.awt.Frame parent, AminoAcidPattern pattern, boolean editable) {
        super(parent, true);

        this.pattern = new AminoAcidPattern(pattern);
        this.editable = editable;

        initComponents();

        if (!editable) {
            cancelButton.setEnabled(false);
        }
        testPattern();
        repaintTable();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AminoAcidPatternDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AminoAcidPatternDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AminoAcidPatternDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AminoAcidPatternDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                AminoAcidPatternDialog dialog = new AminoAcidPatternDialog(new javax.swing.JFrame(), new AminoAcidPattern(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
            }
        });
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
        patternDesignPanel = new javax.swing.JPanel();
        patternDesignScrollPane = new javax.swing.JScrollPane();
        patternDesignTable = new javax.swing.JTable();
        testPanel = new javax.swing.JPanel();
        testScrollPane = new javax.swing.JScrollPane();
        testTxt = new javax.swing.JTextArea();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        exampleButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        patternDesignPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Pattern Design"));

        patternDesignTable.setModel(new PatternTable());
        patternDesignScrollPane.setViewportView(patternDesignTable);

        javax.swing.GroupLayout patternDesignPanelLayout = new javax.swing.GroupLayout(patternDesignPanel);
        patternDesignPanel.setLayout(patternDesignPanelLayout);
        patternDesignPanelLayout.setHorizontalGroup(
            patternDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(patternDesignPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(patternDesignScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                .addContainerGap())
        );
        patternDesignPanelLayout.setVerticalGroup(
            patternDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(patternDesignPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(patternDesignScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addContainerGap())
        );

        testPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Test"));

        testTxt.setColumns(20);
        testTxt.setLineWrap(true);
        testTxt.setRows(5);
        testTxt.setText("MKFILLWALLNLTVALAFNPDYTVSSTPPYLVYLKSDYLPCAGVLIHPLWVITAAHCNLPKLRVILGVTIPADSNEKHLQVIGYEKMIHHPHFSVTSIDHDIMLIKLKTEAELNDYVKLANLPYQTISENTMCSVSTWSYNVCDIYKEPDSLQTVNISVISKPQCRDAYKTYNITENMLCVGIVPGRRQPCKEVSAAPAICNGMLQGILSFADGCVLRADVGIYAKIFYYIPWIENVIQNN");
        testTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                testTxtKeyReleased(evt);
            }
        });
        testScrollPane.setViewportView(testTxt);

        javax.swing.GroupLayout testPanelLayout = new javax.swing.GroupLayout(testPanel);
        testPanel.setLayout(testPanelLayout);
        testPanelLayout.setHorizontalGroup(
            testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(testPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(testScrollPane)
                .addContainerGap())
        );
        testPanelLayout.setVerticalGroup(
            testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(testPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(testScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addContainerGap())
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.setPreferredSize(new java.awt.Dimension(65, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        exampleButton.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        exampleButton.setText("Example");
        exampleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exampleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(testPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(patternDesignPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addComponent(exampleButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(patternDesignPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exampleButton))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cancelButton, exampleButton, okButton});

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
     * Validate the input and close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
//@TODO validate input and make sure that the targeted index has no rejected amino acid
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancel = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Open an example pattern.
     *
     * @param evt
     */
    private void exampleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exampleButtonActionPerformed
        new AminoAcidPatternDialog(null, AminoAcidPattern.getTrypsinExample(), false);
    }//GEN-LAST:event_exampleButtonActionPerformed

    /**
     * Test the current pattern.
     *
     * @param evt
     */
    private void testTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_testTxtKeyReleased
        testPattern();
    }//GEN-LAST:event_testTxtKeyReleased

    /**
     * Returns the pattern as edited by the user.
     *
     * @return the pattern as edited by the user
     */
    public AminoAcidPattern getPattern() {
        return pattern;
    }

    /**
     * Returns the given list of amino acids as a comma separated String.
     *
     * @param aminoAcids the given list of amino acids
     * @return the given list of amino acids as a comma separated String
     */
    private String getAAasString(ArrayList<AminoAcid> aminoAcids) {
        String result = "";
        if (aminoAcids != null) {
            for (AminoAcid aa : aminoAcids) {
                if (!result.contains(aa.singleLetterCode)) {
                    if (!result.equals("")) {
                        result += ", ";
                    }
                    result += aa.singleLetterCode;
                }
            }
        }
        return result;
    }

    /**
     * Returns a list of amino acids from a comma separated String.
     *
     * @param aminoAcids the comma separated String
     * @return the corresponding list of amino acids
     */
    private ArrayList<AminoAcid> getAAfromString(String aminoAcids) {
        ArrayList<AminoAcid> result = new ArrayList<AminoAcid>();
        for (String aa : aminoAcids.split(", ")) {
            String input = aa.trim();
            if (!input.equals("")) {
                AminoAcid aminoAcid = AminoAcid.getAminoAcid(input);
                if (aminoAcid == null) {
                    throw new IllegalArgumentException("Cannot parse " + input + " into an amino-acid");
                } else {
                    result.add(aminoAcid);
                }
            }
        }
        return result;
    }

    /**
     * Tests the pattern on the test text and puts a '*' every time the pattern
     * is found.
     */
    private void testPattern() {

        // @TODO: replace the stars with color

        String txt = testTxt.getText();
        ArrayList<Integer> indexes = pattern.getIndexes(txt);
        if (indexes.isEmpty()) {
            return;
        }
        indexes.add(0);
        Collections.sort(indexes);
        String result = "";
        for (int i = 0; i < indexes.size() - 1; i++) {
            String temp = txt.substring(indexes.get(i), indexes.get(i + 1));
            if (!temp.endsWith("*")) {
                temp += "*";
            }
            result += temp;
        }
        result += txt.substring(indexes.get(indexes.size() - 1));
        testTxt.setText(result);
    }

    /**
     * Table model for the pattern table.
     */
    private class PatternTable extends DefaultTableModel {

        @Override
        public int getRowCount() {
            return pattern.length();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 1:
                    return "Ref";
                case 2:
                    return "Targeted AA";
                case 3:
                    return "Excluded AA";
                default:
                    return "";
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return row == pattern.getTarget();
                case 2:
                    return getAAasString(pattern.getTargetedAA(row));
                case 3:
                    return getAAasString(pattern.getExcludedAA(row));
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            try {
                ArrayList<AminoAcid> aa = getAAfromString(getValueAt(row, column).toString()); // @TODO: this doesn' actually do anything?
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            }
            repaintTable();
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, columnIndex) != null) {
                    return getValueAt(i, columnIndex).getClass();
                }
            }
            return (new Double(0.0)).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int column) {

            if (editable && column != 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Repaints the table.
     */
    private void repaintTable() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                patternDesignTable.revalidate();
                patternDesignTable.repaint();
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton exampleButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel patternDesignPanel;
    private javax.swing.JScrollPane patternDesignScrollPane;
    private javax.swing.JTable patternDesignTable;
    private javax.swing.JPanel testPanel;
    private javax.swing.JScrollPane testScrollPane;
    private javax.swing.JTextArea testTxt;
    // End of variables declaration//GEN-END:variables
}
