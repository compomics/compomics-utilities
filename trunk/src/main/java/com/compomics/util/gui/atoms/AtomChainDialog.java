package com.compomics.util.gui.atoms;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 * AtomChainDialog.
 * 
 * @author Marc Vaudel
 */
public class AtomChainDialog extends javax.swing.JDialog {

    /**
     * The atom chain edited by the user.
     */
    private AtomChain atomChain;
    /**
     * Boolean indicating whether the user canceled the input.
     */
    private boolean canceled = false;
    /**
     * The selected atom, the initial value will be selected by default.
     */
    private String selectedAtom = "C";
    /**
     * Map of the isotopes as displayed: displayed string | isotope number.
     */
    private HashMap<String, Integer> isotopesMap;
    /**
     * The selected isotope, the initial value will be selected by default.
     */
    private Integer selectedIsotope = 0;
    /**
     * Boolean indicating whether the atom chain can be edited.
     */
    private boolean editable;

    /**
     * Creates a new dialog.
     *
     * @param parent the parent frame
     * @param atomChain the atom chain to edit
     * @param editable if the dialog is editable or not
     */
    public AtomChainDialog(java.awt.Frame parent, AtomChain atomChain, boolean editable) {
        super(parent, true);
        initComponents();
        if (atomChain != null) {
            this.atomChain = atomChain.clone();
        } else {
            this.atomChain = new AtomChain();
        }
        this.editable = editable;
        setupGUI();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Sets up the GUI components.
     */
    private void setupGUI() {
        atomCmb.setModel(new DefaultComboBoxModel(Atom.getImplementedAtoms()));
        if (selectedAtom != null) {
            atomCmb.setSelectedItem(selectedAtom);
        }
        clearButton.setEnabled(editable);
        atomCmb.setEnabled(editable);
        isotopeCmb.setEnabled(editable);
        occurrenceTxt.setEditable(editable);
        if (!editable) {
            compositionSplitPane.setDividerLocation(1.0);
        }
        updateIsotopes();
    }

    /**
     * Updates the atom composition panel.
     */
    private void updateAtomComposition() {
        compositionTxt.setText(atomChain.toString());
        massTxt.setText(atomChain.getMass() + " Da");
    }

    /**
     * Updates the isotope list.
     */
    private void updateIsotopes() {
        String atomShortName = (String) atomCmb.getSelectedItem();
        Atom atom = Atom.getAtom(atomShortName);
        ArrayList<Integer> isotopesList = atom.getImplementedIsotopes();
        Collections.sort(isotopesList);
        isotopesMap = new HashMap<String, Integer>(isotopesList.size());
        String[] itemsArray = new String[isotopesList.size()];
        int zeroIndex = 0;
        for (int i = 0; i < isotopesList.size(); i++) {
            Integer isotope = isotopesList.get(i);
            String isotopeName;
            if (isotope == 0) {
                zeroIndex = i;
                isotopeName = "Monoisotopic";
            } else if (isotope > 0) {
                isotopeName = "+" + isotope;
            } else {
                isotopeName = isotope + "";
            }
            Double mass = atom.getIsotopeMass(isotope);
            mass = Util.roundDouble(mass, 2);
            String display = isotopeName + " (" + mass + " Da)";
            isotopesMap.put(display, isotope);
            itemsArray[i] = display;
        }
        isotopeCmb.setModel(new DefaultComboBoxModel(itemsArray));
        isotopeCmb.setSelectedIndex(zeroIndex);
        updateOccurrence();
    }

    /**
     * Updates the occurrence of the selected isotope.
     */
    private void updateOccurrence() {
        Atom atom = Atom.getAtom(selectedAtom);
        Integer occurrence = atomChain.getOccurrence(atom, selectedIsotope);
        occurrenceTxt.setText(occurrence + "");
    }

    /**
     * Indicates whether the edition has been canceled by the user.
     *
     * @return a boolean indicating whether the edition has been canceled by the
     * user
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the atom chain as edited by the user.
     *
     * @return the atom chain as edited by the user
     */
    public AtomChain getAtomChain() {
        return atomChain;
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
        compositionSplitPane = new javax.swing.JSplitPane();
        compositionPanel = new javax.swing.JPanel();
        compositionLbl = new javax.swing.JLabel();
        compositionTxt = new javax.swing.JTextField();
        MassLbl = new javax.swing.JLabel();
        clearButton = new javax.swing.JButton();
        massTxt = new javax.swing.JTextField();
        editPanel = new javax.swing.JPanel();
        atomLbl = new javax.swing.JLabel();
        isotopeLbl = new javax.swing.JLabel();
        occurrenceTxt = new javax.swing.JTextField();
        updateButton = new javax.swing.JButton();
        occurrenceLbl = new javax.swing.JLabel();
        atomCmb = new javax.swing.JComboBox();
        isotopeCmb = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Atomic Composition");

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

        compositionSplitPane.setBorder(null);
        compositionSplitPane.setDividerLocation(400);
        compositionSplitPane.setDividerSize(0);
        compositionSplitPane.setResizeWeight(0.5);
        compositionSplitPane.setPreferredSize(new java.awt.Dimension(800, 229));

        compositionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Atomic Composition"));

        compositionLbl.setText("Composition");

        compositionTxt.setEditable(false);

        MassLbl.setText("Mass");

        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        massTxt.setEditable(false);

        javax.swing.GroupLayout compositionPanelLayout = new javax.swing.GroupLayout(compositionPanel);
        compositionPanel.setLayout(compositionPanelLayout);
        compositionPanelLayout.setHorizontalGroup(
            compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compositionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(compositionLbl)
                    .addComponent(MassLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(compositionPanelLayout.createSequentialGroup()
                        .addComponent(compositionTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearButton))
                    .addComponent(massTxt))
                .addContainerGap())
        );
        compositionPanelLayout.setVerticalGroup(
            compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compositionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compositionLbl)
                    .addComponent(compositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MassLbl)
                    .addComponent(massTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(146, Short.MAX_VALUE))
        );

        compositionSplitPane.setLeftComponent(compositionPanel);

        editPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Edit"));

        atomLbl.setText("Atom");

        isotopeLbl.setText("Isotope");

        occurrenceTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                occurrenceTxtKeyReleased(evt);
            }
        });

        updateButton.setText("Update");

        occurrenceLbl.setText("Occurrence");

        atomCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        atomCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                atomCmbActionPerformed(evt);
            }
        });

        isotopeCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        isotopeCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isotopeCmbActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(editPanel);
        editPanel.setLayout(editPanelLayout);
        editPanelLayout.setHorizontalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(occurrenceLbl)
                        .addGap(18, 18, 18)
                        .addComponent(occurrenceTxt))
                    .addComponent(updateButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(editPanelLayout.createSequentialGroup()
                        .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(atomLbl)
                            .addComponent(isotopeLbl))
                        .addGap(38, 38, 38)
                        .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(isotopeCmb, 0, 279, Short.MAX_VALUE)
                            .addComponent(atomCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        editPanelLayout.setVerticalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(atomLbl)
                    .addComponent(atomCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isotopeLbl)
                    .addComponent(isotopeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(occurrenceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(occurrenceLbl))
                .addGap(18, 18, 18)
                .addComponent(updateButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        compositionSplitPane.setRightComponent(editPanel);

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(compositionSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(compositionSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
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
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void occurrenceTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_occurrenceTxtKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                Atom atom = Atom.getAtom(selectedAtom);
                Integer occurrence = new Integer(occurrenceTxt.getText());
                atomChain.setOccurrence(atom, selectedIsotope, occurrence);
                updateAtomComposition();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Please verify the number of atoms.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_occurrenceTxtKeyReleased

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        atomChain = new AtomChain();
        updateAtomComposition();
        updateOccurrence();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void atomCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_atomCmbActionPerformed
        selectedAtom = (String) atomCmb.getSelectedItem();
        updateIsotopes();
    }//GEN-LAST:event_atomCmbActionPerformed

    private void isotopeCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isotopeCmbActionPerformed
        selectedIsotope = isotopesMap.get((String) isotopeCmb.getSelectedItem());
        updateOccurrence();
    }//GEN-LAST:event_isotopeCmbActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel MassLbl;
    private javax.swing.JComboBox atomCmb;
    private javax.swing.JLabel atomLbl;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JLabel compositionLbl;
    private javax.swing.JPanel compositionPanel;
    private javax.swing.JSplitPane compositionSplitPane;
    private javax.swing.JTextField compositionTxt;
    private javax.swing.JPanel editPanel;
    private javax.swing.JComboBox isotopeCmb;
    private javax.swing.JLabel isotopeLbl;
    private javax.swing.JTextField massTxt;
    private javax.swing.JLabel occurrenceLbl;
    private javax.swing.JTextField occurrenceTxt;
    private javax.swing.JButton okButton;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables

}
