package com.compomics.util.protein_sequences_manager.gui.sequences_import;

import java.io.File;

/**
 * Dialog allowing the import of sequences from uniprot.
 *
 * @author Marc Vaudel
 */
public class ImportSequencesFromUniprotDialog extends javax.swing.JDialog {

    /**
     * Boolean indicating whether the import has been canceled by the user.
     */
    private boolean canceled = false;
    /**
     * The file containing the downloaded protein sequences.
     */
    private File selectedFile = null;
    
    /**
     * Constructor.
     * 
     * @param parent the parent frame
     */
    public ImportSequencesFromUniprotDialog(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        importSequencesFromUniprotPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        importSequencesFromUniprotPanel.setBackground(new java.awt.Color(230, 230, 230));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout importSequencesFromUniprotPanelLayout = new javax.swing.GroupLayout(importSequencesFromUniprotPanel);
        importSequencesFromUniprotPanel.setLayout(importSequencesFromUniprotPanelLayout);
        importSequencesFromUniprotPanelLayout.setHorizontalGroup(
            importSequencesFromUniprotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, importSequencesFromUniprotPanelLayout.createSequentialGroup()
                .addContainerGap(254, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
        );
        importSequencesFromUniprotPanelLayout.setVerticalGroup(
            importSequencesFromUniprotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, importSequencesFromUniprotPanelLayout.createSequentialGroup()
                .addContainerGap(266, Short.MAX_VALUE)
                .addGroup(importSequencesFromUniprotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(importSequencesFromUniprotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(importSequencesFromUniprotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel importSequencesFromUniprotPanel;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables


    /**
     * Indicates whether the user has canceled the import.
     *
     * @return a boolean indicating whether the user has canceled the import
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the file selected by the user.
     *
     * @return the file selected by the user
     */
    public File getSelectedFile() {
        return selectedFile;
    }
}
