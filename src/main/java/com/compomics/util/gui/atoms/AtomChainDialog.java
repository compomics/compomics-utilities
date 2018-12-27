package com.compomics.util.gui.atoms;

import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;
import com.compomics.util.gui.error_handlers.HelpDialog;
import java.awt.Component;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import no.uib.jsparklines.renderers.util.Util;

/**
 * AtomChainDialog.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class AtomChainDialog extends javax.swing.JDialog {

    /**
     * The added atom chain edited by the user.
     */
    private AtomChain atomChainAdded;
    /**
     * The removed atom chain edited by the user.
     */
    private AtomChain atomChainRemoved;
    /**
     * Boolean indicating whether the user canceled the input.
     */
    private boolean canceled = false;
    /**
     * The atom panel index.
     */
    private int atomPanelIndex = 0;
    /**
     * True of the GUI is currently being set up.
     */
    private boolean settingUpGUI = false;
    /**
     * If true, then only adding atoms is allowed.
     */
    private boolean addOnly;

    /**
     * Creates a new dialog.
     *
     * @param parent the parent dialog
     * @param atomChainAdded the added atom chain to edit
     * @param atomChainRemoved the removed atom chain to edit
     * @param onlyAddition if true, atoms can only be added and not removed
     */
    public AtomChainDialog(JDialog parent, AtomChain atomChainAdded, AtomChain atomChainRemoved, boolean onlyAddition) {
        super(parent, true);
        initComponents();
        if (atomChainAdded != null) {
            this.atomChainAdded = atomChainAdded.clone();
        } else {
            this.atomChainAdded = new AtomChain();
        }
        if (atomChainRemoved != null) {
            this.atomChainRemoved = atomChainRemoved.clone();
        } else {
            this.atomChainRemoved = new AtomChain();
        }
        this.addOnly = onlyAddition;
        setupGUI();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates a new dialog.
     *
     * @param parent the parent frame
     * @param atomChainAdded the added atom chain to edit
     * @param atomChainRemoved the removed atom chain to edit
     * @param onlyAddition if true, atoms can only be added and not removed
     */
    public AtomChainDialog(java.awt.Frame parent, AtomChain atomChainAdded, AtomChain atomChainRemoved, boolean onlyAddition) {
        super(parent, true);
        initComponents();
        if (atomChainAdded != null) {
            this.atomChainAdded = atomChainAdded.clone();
        } else {
            this.atomChainAdded = new AtomChain();
        }
        if (atomChainRemoved != null) {
            this.atomChainRemoved = atomChainRemoved.clone();
        } else {
            this.atomChainRemoved = new AtomChain();
        }
        this.addOnly = onlyAddition;
        setupGUI();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Sets up the GUI components.
     */
    private void setupGUI() {
        elementsScrollPane.getViewport().setOpaque(false);
        settingUpGUI = true;

        if (atomChainAdded.getAtomChain().isEmpty() && atomChainRemoved.getAtomChain().isEmpty()) {
            elementsPanel.add(new AtomPanel(this, null, 0, 0, atomPanelIndex++, addOnly));
        }

        if (!atomChainAdded.getAtomChain().isEmpty()) {

            HashMap<String, HashSet<Integer>> atomsAdded = new HashMap<>(4);

            for (AtomImpl tempAtomImpl : atomChainAdded.getAtomChain()) {

                String atomSymbol = tempAtomImpl.getAtomSymbol();
                Atom atom = Atom.getAtom(atomSymbol);
                HashSet<Integer> isotopeList = atomsAdded.get(atomSymbol);

                if (isotopeList == null) {
                    elementsPanel.add(new AtomPanel(this, atom, tempAtomImpl.getIsotope(),
                            atomChainAdded.getOccurrence(atom, tempAtomImpl.getIsotope()), atomPanelIndex++, addOnly));
                    isotopeList = new HashSet<>(2);
                    isotopeList.add(tempAtomImpl.getIsotope());
                    atomsAdded.put(atomSymbol, isotopeList);
                } else if (!isotopeList.contains(tempAtomImpl.getIsotope())) {
                    elementsPanel.add(new AtomPanel(this, atom, tempAtomImpl.getIsotope(),
                            atomChainAdded.getOccurrence(atom, tempAtomImpl.getIsotope()), atomPanelIndex++, addOnly));
                    isotopeList.add(tempAtomImpl.getIsotope());
                }
            }
        }

        if (!atomChainRemoved.getAtomChain().isEmpty()) {
            HashMap<String, HashSet<Integer>> atomsRemoved = new HashMap<>(4);

            for (AtomImpl tempAtomImpl : atomChainRemoved.getAtomChain()) {

                String atomSymbol = tempAtomImpl.getAtomSymbol();
                Atom atom = Atom.getAtom(atomSymbol);
                HashSet<Integer> isotopeList = atomsRemoved.get(atomSymbol);

                if (isotopeList == null) {
                    elementsPanel.add(new AtomPanel(this, atom, tempAtomImpl.getIsotope(),
                            -atomChainRemoved.getOccurrence(atom, tempAtomImpl.getIsotope()), atomPanelIndex++, addOnly));
                    isotopeList = new HashSet<>(2);
                    isotopeList.add(tempAtomImpl.getIsotope());
                    atomsRemoved.put(atomSymbol, isotopeList);
                } else if (!isotopeList.contains(tempAtomImpl.getIsotope())) {
                    elementsPanel.add(new AtomPanel(this, atom, tempAtomImpl.getIsotope(),
                            -atomChainRemoved.getOccurrence(atom, tempAtomImpl.getIsotope()), atomPanelIndex++, addOnly));
                    isotopeList.add(tempAtomImpl.getIsotope());
                }
            }
        }

        settingUpGUI = false;
        updateAtomComposition();

        elementsPanel.revalidate();
        elementsPanel.repaint();
    }

    /**
     * Add a new atom panel.
     */
    public void addElementsPanel() {
        elementsPanel.add(new AtomPanel(this, null, 0, 0, atomPanelIndex++, addOnly));
    }

    /**
     * Remove the given elements panel.
     *
     * @param panelIndex the index of the panel to remove
     */
    public void removeElementsPanel(int panelIndex) {

        for (int componentIndex = 0; componentIndex < elementsPanel.getComponentCount(); componentIndex++) {
            Component tempComponent = elementsPanel.getComponent(componentIndex);
            if (tempComponent instanceof AtomPanel) {
                AtomPanel tempAtomPanel = (AtomPanel) tempComponent;
                if (tempAtomPanel.getPanelIndex() == panelIndex) {
                    elementsPanel.remove(componentIndex);
                    break;
                }
            }
        }

        updateAtomComposition();

        if (elementsPanel.getComponents().length == 0) {
            addElementsPanel();
        }

        elementsPanel.revalidate();
        elementsPanel.repaint();
    }

    /**
     * Updates the atom composition panel.
     */
    public void updateAtomComposition() {

        if (!settingUpGUI) {
            atomChainAdded = new AtomChain();
            atomChainRemoved = new AtomChain();

            for (int componentIndex = 0; componentIndex < elementsPanel.getComponentCount(); componentIndex++) {
                Component tempComponent = elementsPanel.getComponent(componentIndex);
                if (tempComponent instanceof AtomPanel) {
                    AtomPanel tempAtomPanel = (AtomPanel) tempComponent;
                    if (tempAtomPanel.getAtom() != null && tempAtomPanel.getIsotope() != null && tempAtomPanel.getOccurrence() != 0) {
                        if (tempAtomPanel.getOccurrence() > 0) {
                            int previousOccurence = atomChainAdded.getOccurrence(tempAtomPanel.getAtom(), tempAtomPanel.getIsotope());
                            atomChainAdded.setOccurrence(tempAtomPanel.getAtom(), tempAtomPanel.getIsotope(), previousOccurence + tempAtomPanel.getOccurrence());
                        } else {
                            int previousOccurence = atomChainRemoved.getOccurrence(tempAtomPanel.getAtom(), tempAtomPanel.getIsotope());
                            atomChainRemoved.setOccurrence(tempAtomPanel.getAtom(), tempAtomPanel.getIsotope(), previousOccurence + Math.abs(tempAtomPanel.getOccurrence()));
                        }
                    }
                }
            }

            String addition = "";
            if (atomChainAdded.size() > 0) {
                addition = atomChainAdded.toString();
            }
            String deletion = "";
            if (atomChainRemoved.size() > 0) {
                deletion = "-" + atomChainRemoved.toString();
            }
            String temp = addition + " " + deletion;
            compositionTxt.setText(temp.trim());

            massTxt.setText(Util.roundDouble(atomChainAdded.getMass() - atomChainRemoved.getMass(), 6) + " Da");
        }
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
     * Returns the added atom chain as edited by the user.
     *
     * @return the atom chain as edited by the user
     */
    public AtomChain getAtomChainAdded() {
        return atomChainAdded;
    }

    /**
     * Returns the removed atom chain as edited by the user.
     *
     * @return the atom chain as edited by the user
     */
    public AtomChain getAtomChainRemoved() {
        return atomChainRemoved;
    }

    /**
     * Main method for testing purposes.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // ignore
        }

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AtomChainDialog dialog = new AtomChainDialog(new JFrame(), new AtomChain(), new AtomChain(), false);
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
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        editPanel = new javax.swing.JPanel();
        elementsScrollPane = new javax.swing.JScrollPane();
        elementsPanel = new javax.swing.JPanel();
        compositionPanel = new javax.swing.JPanel();
        compositionLbl = new javax.swing.JLabel();
        compositionTxt = new javax.swing.JTextField();
        MassLbl = new javax.swing.JLabel();
        massTxt = new javax.swing.JTextField();
        helpJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Atomic Composition");

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

        editPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Composition Editor"));
        editPanel.setOpaque(false);

        elementsScrollPane.setBorder(null);
        elementsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        elementsScrollPane.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        elementsScrollPane.setOpaque(false);

        elementsPanel.setOpaque(false);
        elementsPanel.setLayout(new javax.swing.BoxLayout(elementsPanel, javax.swing.BoxLayout.Y_AXIS));
        elementsScrollPane.setViewportView(elementsPanel);

        javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(editPanel);
        editPanel.setLayout(editPanelLayout);
        editPanelLayout.setHorizontalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(elementsScrollPane)
                .addContainerGap())
        );
        editPanelLayout.setVerticalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(elementsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                .addContainerGap())
        );

        compositionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Atomic Composition"));
        compositionPanel.setOpaque(false);

        compositionLbl.setText("Composition");

        compositionTxt.setEditable(false);
        compositionTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        MassLbl.setText("Mass");

        massTxt.setEditable(false);
        massTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout compositionPanelLayout = new javax.swing.GroupLayout(compositionPanel);
        compositionPanel.setLayout(compositionPanelLayout);
        compositionPanelLayout.setHorizontalGroup(
            compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compositionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(compositionLbl)
                    .addComponent(MassLbl))
                .addGap(18, 18, 18)
                .addGroup(compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(compositionTxt)
                    .addComponent(massTxt))
                .addContainerGap())
        );
        compositionPanelLayout.setVerticalGroup(
            compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compositionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compositionLbl)
                    .addComponent(compositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(compositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MassLbl)
                    .addComponent(massTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(compositionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 398, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(compositionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelButton)
                        .addComponent(okButton))
                    .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    /**
     * Save the input and close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
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
     * Change the icon to a hand cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseEntered

    /**
     * Change the icon back to the default cursor.
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
        new HelpDialog(this, getClass().getResource("/helpFiles/AtomChainDialog.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                "Atom Composition - Help", 500, 10);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel MassLbl;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel compositionLbl;
    private javax.swing.JPanel compositionPanel;
    private javax.swing.JTextField compositionTxt;
    private javax.swing.JPanel editPanel;
    private javax.swing.JPanel elementsPanel;
    private javax.swing.JScrollPane elementsScrollPane;
    private javax.swing.JButton helpJButton;
    private javax.swing.JTextField massTxt;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
