package com.compomics.util.gui.atoms;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * Panel for selecting atoms.
 *
 * @author Harald Barsnes
 */
public class AtomPanel extends javax.swing.JPanel {

    /**
     * Empty default constructor
     */
    public AtomPanel() {
        atomChainDialog = null;
        panelIndex = 0;
    }

    /**
     * Map of the isotopes as displayed: displayed string | isotope number.
     */
    private HashMap<String, Integer> isotopesMap;
    /**
     * The parent dialog.
     */
    private final AtomChainDialog atomChainDialog;
    /**
     * The panel index.
     */
    private final int panelIndex;

    /**
     * Creates a new AtomPanel.
     *
     * @param atomChainDialog the parent dialog
     * @param atom the atom
     * @param isotope the isotope
     * @param occurrence the occurrence
     * @param panelIndex the unique index of the panel
     * @param addOnly if true, then only adding of atoms is allowed
     */
    public AtomPanel(AtomChainDialog atomChainDialog, Atom atom, int isotope, int occurrence, int panelIndex, boolean addOnly) {
        initComponents();
        this.atomChainDialog = atomChainDialog;
        this.panelIndex = panelIndex;
        atomCmb.setModel(new DefaultComboBoxModel(Atom.getImplementedAtoms(true)));
        updateIsotopes();

        if (addOnly) {
            ((SpinnerNumberModel) occurenceSpinner.getModel()).setMinimum(0);
        }

        updateSelection(atom, isotope, occurrence);

        atomCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        isotopeCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
    }

    /**
     * Update the selection to the given atom.
     *
     * @param atom the atom
     * @param isotope the isotope
     * @param occurrence the occurrence
     */
    private void updateSelection(Atom atom, int isotope, int occurrence) {
        if (atom != null) {
            atomCmb.setSelectedItem(atom.getLetter());
            updateIsotopes();
            for (String istopeString : isotopesMap.keySet()) {
                if (isotopesMap.get(istopeString) == isotope) {
                    isotopeCmb.setSelectedItem(istopeString);
                }
            }
            occurenceSpinner.setValue(occurrence);
            atomChainDialog.updateAtomComposition();
        }
    }

    /**
     * Updates the isotope list.
     */
    private void updateIsotopes() {
        if (atomCmb.getSelectedIndex() != 0) {
            String atomShortName = (String) atomCmb.getSelectedItem();
            Atom atom = Atom.getAtom(atomShortName);
            ArrayList<Integer> isotopesList = atom.getImplementedIsotopes();
            Collections.sort(isotopesList);
            isotopesMap = new HashMap<>(isotopesList.size());
            String[] itemsArray = new String[isotopesList.size()];
            int zeroIndex = 0;
            for (int i = 0; i < isotopesList.size(); i++) {
                Integer isotope = isotopesList.get(i);
                String isotopeName;
                if (isotope == 0) {
                    zeroIndex = i;
                    isotopeName = "0";
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
        } else {
            String[] itemsArray = new String[1];
            itemsArray[0] = "- Select -";
            isotopeCmb.setModel(new DefaultComboBoxModel(itemsArray));
        }

        isotopeCmb.setEnabled(atomCmb.getSelectedIndex() != 0);
        occurenceSpinner.setEnabled(atomCmb.getSelectedIndex() != 0);
    }

    /**
     * Returns the currently selected atom.
     *
     * @return the currently selected atom, null if no atom is selected
     */
    public Atom getAtom() {
        if (atomCmb.getSelectedIndex() > 0) {
            return Atom.getAtom((String) atomCmb.getSelectedItem());
        } else {
            return null;
        }
    }

    /**
     * Returns the currently selected isotope.
     *
     * @return the currently selected isotope, null if no isotope is selected
     */
    public Integer getIsotope() {
        if (!((String) isotopeCmb.getSelectedItem()).equals("- Select -")) {
            return isotopesMap.get((String) isotopeCmb.getSelectedItem());
        } else {
            return null;
        }
    }

    /**
     * Returns the occurrence of the selected atom.
     *
     * @return the occurrence of the selected atom
     */
    public int getOccurrence() {
        return (Integer) occurenceSpinner.getValue();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        atomCmb = new javax.swing.JComboBox();
        isotopeCmb = new javax.swing.JComboBox();
        occurenceSpinner = new javax.swing.JSpinner();
        removeLabel = new javax.swing.JLabel();
        addLabel = new javax.swing.JLabel();

        setOpaque(false);

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

        occurenceSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), null, null, Integer.valueOf(1)));
        occurenceSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                occurenceSpinnerStateChanged(evt);
            }
        });

        removeLabel.setText("<html><a href>Remove</a></html>");
        removeLabel.setToolTipText("Remove the row");
        removeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                removeLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                removeLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                removeLabelMouseReleased(evt);
            }
        });

        addLabel.setText("<html><a href>Add Row</a></html>");
        addLabel.setToolTipText("Add a new row");
        addLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addLabelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(atomCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(isotopeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(occurenceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(addLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {atomCmb, isotopeCmb, occurenceSpinner});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(atomCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(isotopeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(occurenceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(removeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(addLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Update the isotopes and updates the atomic composition.
     *
     * @param evt
     */
    private void atomCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_atomCmbActionPerformed
        updateIsotopes();
        atomChainDialog.updateAtomComposition();
    }//GEN-LAST:event_atomCmbActionPerformed

    /**
     * Update the atomic composition.
     *
     * @param evt
     */
    private void isotopeCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isotopeCmbActionPerformed
        atomChainDialog.updateAtomComposition();
    }//GEN-LAST:event_isotopeCmbActionPerformed

    /**
     * Adds a new atom panel.
     *
     * @param evt
     */
    private void addLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addLabelMouseReleased
        atomChainDialog.addElementsPanel();
    }//GEN-LAST:event_addLabelMouseReleased

    /**
     * Removes this atom panel from the parent dialog.
     *
     * @param evt
     */
    private void removeLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeLabelMouseReleased
        atomChainDialog.removeElementsPanel(panelIndex);
    }//GEN-LAST:event_removeLabelMouseReleased

    /**
     * Update the atomic composition.
     *
     * @param evt
     */
    private void occurenceSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_occurenceSpinnerStateChanged
        atomChainDialog.updateAtomComposition();
    }//GEN-LAST:event_occurenceSpinnerStateChanged

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void addLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_addLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void addLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_addLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void removeLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_removeLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void removeLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_removeLabelMouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addLabel;
    private javax.swing.JComboBox atomCmb;
    private javax.swing.JComboBox isotopeCmb;
    private javax.swing.JSpinner occurenceSpinner;
    private javax.swing.JLabel removeLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Returns the panel index.
     *
     * @return the panelIndex
     */
    public int getPanelIndex() {
        return panelIndex;
    }
}
