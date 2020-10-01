package com.compomics.util.gui.enzymes;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.pride.CvTerm;
import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.*;

/**
 * This dialog allows the user to create a new enzyme.
 *
 * @author Harald Barsnes
 */
public class NewEnzymeDialog extends javax.swing.JDialog {

    /**
     * The enzymes factory.
     */
    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    /**
     * The parent enzymes dialog.
     */
    private EnzymesDialog enzymesDialog;
    /**
     * Boolean indicating whether the dialog was canceled by the user.
     */
    private boolean canceled = false;

    /**
     * Creates a NewEnzymeDialog.
     *
     * @param enzymesDialog the EnzymesDialog parent
     */
    public NewEnzymeDialog(EnzymesDialog enzymesDialog) {
        super(enzymesDialog, true);
        this.enzymesDialog = enzymesDialog;
        initComponents();
        setLocationRelativeTo(enzymesDialog);
        setVisible(true);
    }

    /**
     * Indicates whether the dialog was canceled by the user.
     *
     * @return a boolean indicating whether the dialog was canceled by the user
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns a boolean indicating whether the input can be translated into an
     * enzyme.
     *
     * @return a boolean indicating whether the input can be translated into an
     * enzyme
     */
    private boolean validateInput(boolean showMessage) {

        boolean error = false;

        nameLabel.setForeground(Color.BLACK);
        cleavesAfterLabel.setForeground(Color.BLACK);
        butNotBeforeLabel.setForeground(Color.BLACK);
        cleavesBeforeLabel.setForeground(Color.BLACK);
        butNotAfterLabel.setForeground(Color.BLACK);
        psiMsAccessionLabel.setForeground(Color.BLACK);

        nameLabel.setToolTipText(null);
        nameTxt.setToolTipText(null);
        cleavesAfterLabel.setToolTipText(null);
        cleavesAfterTxt.setToolTipText(null);
        butNotBeforeLabel.setToolTipText(null);
        butNotBeforeTxt.setToolTipText(null);
        cleavesBeforeLabel.setToolTipText(null);
        cleavesBeforeTxt.setToolTipText(null);
        butNotAfterLabel.setToolTipText(null);
        butNotAfterTxt.setToolTipText(null);
        psiMsAccessionLabel.setToolTipText(null);
        psiMsAccessionJTextField.setToolTipText(null);

        String name = nameTxt.getText().trim();

        // check that a name is provided
        if (name.isEmpty()) {
            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this, "Enzyme name required.");
            }
            error = true;
            nameLabel.setForeground(Color.RED);
            nameLabel.setToolTipText("Please provide an enzyme name");
            nameTxt.setToolTipText("Please provide an enzyme name");
        }

        // check if the name contains comma
        if (name.lastIndexOf(",") != -1) {
            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this, "Enzyme names cannot contain comma.");
            }
            error = true;
            nameLabel.setForeground(Color.RED);
            nameLabel.setToolTipText("Enzyme names cannot contain comma");
            nameTxt.setToolTipText("Enzyme names cannot contain comma");
        }

        // check that the enzyme name does not already exist
        if (enzymeFactory.getEnzyme(name) != null) {
            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this,
                        "An enzyme named \'" + name + "\' already exists.\n"
                        + "Please choose a different name.",
                        "Enzyme Already Exists", JOptionPane.WARNING_MESSAGE);
            } else {
                nameLabel.setForeground(Color.RED);
                nameLabel.setToolTipText(
                        "<html>An enzyme named \'" + name + "\' already exists.<br>"
                        + "Please choose a different name.</html>");
                nameTxt.setToolTipText(
                        "<html>An enzyme named \'" + name + "\' already exists.<br>"
                        + "Please choose a different name.</html>");
            }
            error = true;
        }

        // check that an enzyme cleavage pattern is provided
        if (cleavesAfterTxt.getText().trim().isEmpty()
                && cleavesBeforeTxt.getText().trim().isEmpty()) {

            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this,
                        "Please provide at least one amino acid for the\n"
                        + "cleaves after or cleaves before options.",
                        "Cleaving Pattern Missing", JOptionPane.WARNING_MESSAGE);
            }
            error = true;
            cleavesAfterLabel.setForeground(Color.RED);
            cleavesAfterLabel.setToolTipText("Please provide a cleavage pattern");
            cleavesAfterTxt.setToolTipText("Please provide a cleavage pattern");

            cleavesBeforeLabel.setForeground(Color.RED);
            cleavesBeforeLabel.setToolTipText("Please provide a cleavage pattern");
            cleavesBeforeTxt.setToolTipText("Please provide a cleavage pattern");
        }

        // validate the comma separated input
        if (!cleavesAfterTxt.getText().trim().isEmpty()
                && !validateAminoAcidInput(cleavesAfterTxt.getText())) {

            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this,
                        "Please provide a comma separate list of amino acids.",
                        "Amino Acid List Error", JOptionPane.WARNING_MESSAGE);
            }
            error = true;
            cleavesAfterLabel.setForeground(Color.RED);
            cleavesAfterLabel.setToolTipText("Please provide a comma separate list of amino acids");
            cleavesAfterTxt.setToolTipText("Please provide a comma separate list of amino acids");
        }
        if (!butNotBeforeTxt.getText().trim().isEmpty()
                && !validateAminoAcidInput(butNotBeforeTxt.getText())) {

            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this,
                        "Please provide a comma separate list of amino acids.",
                        "Amino Acid List Error", JOptionPane.WARNING_MESSAGE);
            }
            error = true;
            butNotBeforeLabel.setForeground(Color.RED);
            butNotBeforeLabel.setToolTipText("Please provide a comma separate list of amino acids");
            butNotBeforeTxt.setToolTipText("Please provide a comma separate list of amino acids");
        }
        if (!cleavesBeforeTxt.getText().trim().isEmpty()
                && !validateAminoAcidInput(cleavesBeforeTxt.getText())) {

            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this,
                        "Please provide a comma separate list of amino acids.",
                        "Amino Acid List Error", JOptionPane.WARNING_MESSAGE);
            }
            error = true;
            cleavesBeforeLabel.setForeground(Color.RED);
            cleavesBeforeLabel.setToolTipText("Please provide a comma separate list of amino acids");
            cleavesBeforeTxt.setToolTipText("Please provide a comma separate list of amino acids");
        }
        if (!butNotAfterTxt.getText().trim().isEmpty()
                && !validateAminoAcidInput(butNotAfterTxt.getText())) {

            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this,
                        "Please provide a comma separate list of amino acids.",
                        "Amino Acid List Error", JOptionPane.WARNING_MESSAGE);
            }
            error = true;
            butNotAfterLabel.setForeground(Color.RED);
            butNotAfterLabel.setToolTipText("Please provide a comma separate list of amino acids");
            butNotAfterTxt.setToolTipText("Please provide a comma separate list of amino acids");
        }

        // check that the psi-ms cv term accesion is an integer
        if (!psiMsAccessionJTextField.getText().trim().isEmpty()) {
            try {
                Integer.valueOf(psiMsAccessionJTextField.getText().trim());
            } catch (NumberFormatException e) {
                if (showMessage && !error) {
                    JOptionPane.showMessageDialog(this,
                            "Please provide the PSI-MS accession number as an integer.",
                            "PSI-MS Accession", JOptionPane.WARNING_MESSAGE);
                }
                error = true;
                psiMsAccessionLabel.setForeground(Color.RED);
                psiMsAccessionLabel.setToolTipText("Please provide the PSI-MS accession number as an integer");
                psiMsAccessionJTextField.setToolTipText("Please provide the PSI-MS accession number as an integer");
            }
        }

        okButton.setEnabled(!error);

        return true;
    }

    /**
     * Returns true if the input is a comma separated list of amino acids, e.g.
     * "R, K", "R,K" or "K".
     *
     * @param aminoAcidList the comma separated list to check
     * @return true if the input is a comma separated list of amino acids
     */
    private boolean validateAminoAcidInput(String aminoAcidList) {

        String[] tempAminoAcids = aminoAcidList.split(",");

        for (String tempAminoAcid : tempAminoAcids) {
            
            if (!AminoAcid.getAminoAcidsList().contains(tempAminoAcid.trim())) {
                return false;
            }
        }

        return true;
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
        cleavageRulesPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTxt = new javax.swing.JTextField();
        cleavesAfterLabel = new javax.swing.JLabel();
        cleavesAfterTxt = new javax.swing.JTextField();
        butNotBeforeLabel = new javax.swing.JLabel();
        butNotBeforeTxt = new javax.swing.JTextField();
        cleavesBeforeLabel = new javax.swing.JLabel();
        cleavesBeforeTxt = new javax.swing.JTextField();
        butNotAfterLabel = new javax.swing.JLabel();
        butNotAfterTxt = new javax.swing.JTextField();
        psMsMappingPanel = new javax.swing.JPanel();
        psiMsAccessionLabel = new javax.swing.JLabel();
        psiMsAccessionJTextField = new javax.swing.JTextField();
        psiMsLinkLabel = new javax.swing.JLabel();
        helpJButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Enzyme");
        setMinimumSize(new java.awt.Dimension(400, 300));
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));
        backgroundPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        cleavageRulesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Cleavage Rules"));
        cleavageRulesPanel.setOpaque(false);

        nameLabel.setText("Name");
        nameLabel.setToolTipText("The modification name");

        nameTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nameTxt.setToolTipText("The enzyme name");
        nameTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTxtKeyReleased(evt);
            }
        });

        cleavesAfterLabel.setText("Cleaves After");

        cleavesAfterTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cleavesAfterTxt.setToolTipText("Comma separated list of amino acids");
        cleavesAfterTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cleavesAfterTxtKeyReleased(evt);
            }
        });

        butNotBeforeLabel.setText("But Not Before");
        butNotBeforeLabel.setToolTipText("Residues modified");

        butNotBeforeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        butNotBeforeTxt.setToolTipText("Comma separated list of amino acids");
        butNotBeforeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                butNotBeforeTxtKeyReleased(evt);
            }
        });

        cleavesBeforeLabel.setText("Cleaves Before");
        cleavesBeforeLabel.setToolTipText("The modification name");

        cleavesBeforeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cleavesBeforeTxt.setToolTipText("Comma separated list of amino acids");
        cleavesBeforeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cleavesBeforeTxtKeyReleased(evt);
            }
        });

        butNotAfterLabel.setText("But Not After");
        butNotAfterLabel.setToolTipText("Monoisotopic mass");

        butNotAfterTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        butNotAfterTxt.setToolTipText("Comma separated list of amino acids");
        butNotAfterTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                butNotAfterTxtKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout cleavageRulesPanelLayout = new javax.swing.GroupLayout(cleavageRulesPanel);
        cleavageRulesPanel.setLayout(cleavageRulesPanelLayout);
        cleavageRulesPanelLayout.setHorizontalGroup(
            cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cleavageRulesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cleavageRulesPanelLayout.createSequentialGroup()
                        .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nameTxt))
                    .addGroup(cleavageRulesPanelLayout.createSequentialGroup()
                        .addGroup(cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(butNotBeforeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cleavesAfterLabel)
                            .addComponent(cleavesBeforeLabel)
                            .addComponent(butNotAfterLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(butNotBeforeTxt)
                            .addComponent(cleavesAfterTxt)
                            .addComponent(cleavesBeforeTxt)
                            .addComponent(butNotAfterTxt))))
                .addContainerGap())
        );

        cleavageRulesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {butNotBeforeLabel, nameLabel});

        cleavageRulesPanelLayout.setVerticalGroup(
            cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cleavageRulesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addGap(0, 0, 0)
                .addGroup(cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cleavesAfterTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cleavesAfterLabel))
                .addGap(0, 0, 0)
                .addGroup(cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(butNotBeforeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butNotBeforeLabel))
                .addGap(0, 0, 0)
                .addGroup(cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cleavesBeforeLabel)
                    .addComponent(cleavesBeforeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(cleavageRulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(butNotAfterLabel)
                    .addComponent(butNotAfterTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        psMsMappingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PSI-MS Mapping"));
        psMsMappingPanel.setOpaque(false);

        psiMsAccessionLabel.setText("Accession");

        psiMsAccessionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        psiMsAccessionJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                psiMsAccessionJTextFieldKeyReleased(evt);
            }
        });

        psiMsLinkLabel.setText("<html><a href>See: ebi.ac.uk/ols/ontologies/ms</a></html>");
        psiMsLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                psiMsLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                psiMsLinkLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                psiMsLinkLabelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout psMsMappingPanelLayout = new javax.swing.GroupLayout(psMsMappingPanel);
        psMsMappingPanel.setLayout(psMsMappingPanelLayout);
        psMsMappingPanelLayout.setHorizontalGroup(
            psMsMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psMsMappingPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(psMsMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(psMsMappingPanelLayout.createSequentialGroup()
                        .addComponent(psiMsAccessionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(psiMsAccessionJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))
                    .addComponent(psiMsLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        psMsMappingPanelLayout.setVerticalGroup(
            psMsMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psMsMappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(psMsMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psiMsAccessionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(psiMsAccessionLabel))
                .addGap(0, 0, 0)
                .addComponent(psiMsLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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

        okButton.setText("OK");
        okButton.setEnabled(false);
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
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cleavageRulesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(psMsMappingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cleavageRulesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(psMsMappingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(helpJButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
     * Add the enzyme to the enzyme factory.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        if (validateInput(true)) {

            // create the psi-ms cv term
            CvTerm psiMsCvTerm = null;
            if (!psiMsAccessionJTextField.getText().trim().isEmpty()) {
                int psiMsAccession = Integer.valueOf(psiMsAccessionJTextField.getText().trim());
                psiMsCvTerm = new CvTerm("MS", "MS:" + psiMsAccession, nameTxt.getText().trim(), null);
            }

            Enzyme enzyme = new Enzyme(nameTxt.getText().trim());
            if (!cleavesAfterTxt.getText().trim().isEmpty()) {
                for (String tempAminoAcid : cleavesAfterTxt.getText().split(",")) {
                    enzyme.addAminoAcidBefore(tempAminoAcid.trim().charAt(0));
                }
            }
            if (!butNotBeforeTxt.getText().trim().isEmpty()) {
                for (String tempAminoAcid : butNotBeforeTxt.getText().split(",")) {
                    enzyme.addRestrictionAfter(tempAminoAcid.trim().charAt(0));
                }
            }
            if (!cleavesBeforeTxt.getText().trim().isEmpty()) {
                for (String tempAminoAcid : cleavesBeforeTxt.getText().split(",")) {
                    enzyme.addAminoAcidBefore(tempAminoAcid.trim().charAt(0));
                }
            }
            if (!butNotAfterTxt.getText().trim().isEmpty()) {
                for (String tempAminoAcid : butNotAfterTxt.getText().split(",")) {
                    enzyme.addRestrictionAfter(tempAminoAcid.trim().charAt(0));
                }
            }

            enzyme.setCvTerm(psiMsCvTerm);

            enzymeFactory.addEnzyme(enzyme);
            enzymesDialog.updateEnzymesTable();

            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Changes the cursor to a hand cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseEntered

    /**
     * Change the cursor to the default cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseExited

    /**
     * Opens the help dialog.
     *
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/NewEnzymeDialog.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                null, "New Enzyme - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void nameTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_nameTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void cleavesBeforeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cleavesBeforeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_cleavesBeforeTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void psiMsAccessionJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_psiMsAccessionJTextFieldKeyReleased
        validateInput(false);
    }//GEN-LAST:event_psiMsAccessionJTextFieldKeyReleased

    /**
     * Open the OLS PSI-MOD web page.
     *
     * @param evt
     */
    private void psiMsLinkLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_psiMsLinkLabelMouseReleased
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://www.ebi.ac.uk/ols/ontologies/mod");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_psiMsLinkLabelMouseReleased

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void psiMsLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_psiMsLinkLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_psiMsLinkLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void psiMsLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_psiMsLinkLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_psiMsLinkLabelMouseEntered

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void cleavesAfterTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cleavesAfterTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_cleavesAfterTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void butNotBeforeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_butNotBeforeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_butNotBeforeTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void butNotAfterTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_butNotAfterTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_butNotAfterTxtKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JLabel butNotAfterLabel;
    private javax.swing.JTextField butNotAfterTxt;
    private javax.swing.JLabel butNotBeforeLabel;
    private javax.swing.JTextField butNotBeforeTxt;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel cleavageRulesPanel;
    private javax.swing.JLabel cleavesAfterLabel;
    private javax.swing.JTextField cleavesAfterTxt;
    private javax.swing.JLabel cleavesBeforeLabel;
    private javax.swing.JTextField cleavesBeforeTxt;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel psMsMappingPanel;
    private javax.swing.JTextField psiMsAccessionJTextField;
    private javax.swing.JLabel psiMsAccessionLabel;
    private javax.swing.JLabel psiMsLinkLabel;
    // End of variables declaration//GEN-END:variables

}
