package com.compomics.util.gui.gene_mapping;

import com.compomics.util.experiment.annotation.gene.GeneFactory;
import com.compomics.util.experiment.annotation.go.GOFactory;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.preferences.GenePreferences;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * A dialog for choosing the species.
 *
 * @author Harald Barsnes
 */
public class SpeciesDialog extends javax.swing.JDialog {

    /**
     * The GO factory.
     */
    private GOFactory goFactory = GOFactory.getInstance();
    /**
     * The gene factory.
     */
    private GeneFactory geneFactory = GeneFactory.getInstance();
    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * The frame parent, if any.
     */
    private JFrame frameParent = null;
    /**
     * The dialog parent, if any.
     */
    private JDialog dialogParent = null;
    /**
     * The text to use to tell the user to please select a species in the list.
     */
    public final static String SELECT_SPECIES_TAG = "-- Select Species --";
    /**
     * The text to use for no species selected.
     */
    public final static String NO_SPECIES_TAG = "-- (no selection) --";
    /**
     * The icon to display when waiting.
     */
    private Image waitingImage = null;
    /**
     * The icon to display when processing is finished.
     */
    private Image normalImage = null;
    /**
     * The gene preferences.
     */
    private GenePreferences genePreferences;

    /**
     * Creates a new SpeciesDialog.
     *
     * @param parentFrame the parent frame
     * @param genePreferences the gene preferences
     * @param modal
     * @param waitingImage
     * @param normalImage
     */
    public SpeciesDialog(JFrame parentFrame, GenePreferences genePreferences, boolean modal, Image waitingImage, Image normalImage) {
        super(parentFrame, modal);
        frameParent = parentFrame;
        this.genePreferences = genePreferences;
        initComponents();
        this.waitingImage = waitingImage;
        this.normalImage = normalImage;
        setUpGUI();
        setLocationRelativeTo(frameParent);
        setVisible(true);
    }

    /**
     * Creates a new SpeciesDialog.
     *
     * @param parentDialog the parent dialog
     * @param mainFrame the parent of the parent dialog
     * @param genePreferences the gene preferences
     * @param modal
     * @param waitingImage
     * @param normalImage
     */
    public SpeciesDialog(JDialog parentDialog, JFrame mainFrame, GenePreferences genePreferences, boolean modal, Image waitingImage, Image normalImage) {
        super(parentDialog, modal);
        dialogParent = parentDialog;
        frameParent = mainFrame;
        this.genePreferences = genePreferences;
        initComponents();
        this.waitingImage = waitingImage;
        this.normalImage = normalImage;
        setUpGUI();
        speciesJComboBox.requestFocus();
        setLocationRelativeTo(parentDialog);
        setVisible(true);
    }

    /**
     * Set up the GUI details.
     */
    private void setUpGUI() {
        ensemblCategoryJComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        speciesJComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        if (genePreferences.getCurrentSpeciesType() != null) {
            ensemblCategoryJComboBox.setSelectedItem(genePreferences.getCurrentSpeciesType());
        }
        updateSpeciesList();

        // select the current species
        String selectedSpecies = genePreferences.getCurrentSpecies();
        if (selectedSpecies != null) {
            for (int i = 0; i < speciesJComboBox.getItemCount(); i++) {
                String content = (String) speciesJComboBox.getItemAt(i);
                if (content.contains(selectedSpecies)) {
                    speciesJComboBox.setSelectedIndex(i);
                    boolean dbVersion = content.contains("N/A");
                    if (dbVersion) {
                        updateMappingsButton.setText("Download");
                    } else {
                        updateMappingsButton.setText("Update");
                    }
                    updateMappingsButton.setEnabled(true);
                    break;
                }
            }
        }
    }

    /**
     * Returns the list to display in the combo box based on the available
     * species.
     *
     * @return the list to display in the combo box
     */
    private void updateSpeciesList() {

        HashMap<String, ArrayList<String>> availableSpecies = genePreferences.getAllSpecies();
        String currentEnsemblSpeciesType = (String) ensemblCategoryJComboBox.getSelectedItem();
        speciesJComboBox.setEnabled(ensemblCategoryJComboBox.getSelectedIndex() > 0);

        if (ensemblCategoryJComboBox.getSelectedIndex() > 0) {

            ArrayList<String> currentSpeciesList = availableSpecies.get(currentEnsemblSpeciesType);
            ArrayList<String> speciesList = new ArrayList<String>();

            if (currentSpeciesList != null && !currentSpeciesList.isEmpty()) {

                speciesList.add(SELECT_SPECIES_TAG);

                for (int i = 0; i < currentSpeciesList.size(); i++) {
                    String currentSpecies = currentSpeciesList.get(i);
                    String tempEnsemblVersion = genePreferences.getEnsemblSpeciesVersion(currentEnsemblSpeciesType, currentSpecies);
                    if (tempEnsemblVersion == null) {
                        tempEnsemblVersion = "N/A";
                    }
                    speciesList.add(currentSpecies + " [" + tempEnsemblVersion + "]");
                }

                speciesList.add(NO_SPECIES_TAG);

                String[] tempTable = new String[speciesList.size()];
                speciesJComboBox.setModel(new DefaultComboBoxModel(speciesList.toArray(tempTable)));
            } else {
                String[] content = new String[1];
                content[0] = NO_SPECIES_TAG;
                speciesJComboBox.setModel(new DefaultComboBoxModel(content));
            }

        } else {
            String[] content = new String[1];
            content[0] = SELECT_SPECIES_TAG;
            speciesJComboBox.setModel(new DefaultComboBoxModel(content));
        }

        updateMappingsButton.setEnabled(ensemblCategoryJComboBox.getSelectedIndex() > 0
                && speciesJComboBox.getSelectedIndex() > 0
                && speciesJComboBox.getSelectedIndex() < speciesJComboBox.getItemCount() - 1);
    }

    /**
     * Returns the species selected in the species drop down menu.
     *
     * @return the species selected
     */
    private String getSelectedSpecies() {
        String currentEnsemblSpeciesType = (String) ensemblCategoryJComboBox.getSelectedItem();
        int selectedIndex = speciesJComboBox.getSelectedIndex();
        if (selectedIndex > 0 && selectedIndex < speciesJComboBox.getItemCount() - 1) {
            return genePreferences.getAllSpecies().get(currentEnsemblSpeciesType).get(selectedIndex - 1);
        }
        return null;
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
        jPanel1 = new javax.swing.JPanel();
        speciesJComboBox = new javax.swing.JComboBox();
        updateMappingsButton = new javax.swing.JButton();
        ensemblCategoryJComboBox = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        unknownSpeciesLabel = new javax.swing.JLabel();
        ensemblVersionLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Species");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Species"));
        jPanel1.setOpaque(false);

        speciesJComboBox.setMaximumRowCount(20);
        speciesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        speciesJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speciesJComboBoxActionPerformed(evt);
            }
        });

        updateMappingsButton.setText("Update");
        updateMappingsButton.setToolTipText("Update the GO Mappings");
        updateMappingsButton.setEnabled(false);
        updateMappingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateMappingsButtonActionPerformed(evt);
            }
        });

        ensemblCategoryJComboBox.setMaximumRowCount(20);
        ensemblCategoryJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-- Select Species Type ---", "Fungi", "Plants", "Protists", "Metazoa", "Vertebrates" }));
        ensemblCategoryJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ensemblCategoryJComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ensemblCategoryJComboBox, 0, 416, Short.MAX_VALUE)
                    .addComponent(speciesJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(updateMappingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ensemblCategoryJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(speciesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateMappingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {speciesJComboBox, updateMappingsButton});

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        unknownSpeciesLabel.setFont(unknownSpeciesLabel.getFont().deriveFont((unknownSpeciesLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        unknownSpeciesLabel.setText("<html><a href>Species not in list?</a></html>");
        unknownSpeciesLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                unknownSpeciesLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                unknownSpeciesLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                unknownSpeciesLabelMouseExited(evt);
            }
        });

        ensemblVersionLabel.setFont(ensemblVersionLabel.getFont().deriveFont((ensemblVersionLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        ensemblVersionLabel.setText("<html><a href>Ensembl version?</a></html>");
        ensemblVersionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ensemblVersionLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ensemblVersionLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ensemblVersionLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(unknownSpeciesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(ensemblVersionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(unknownSpeciesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ensemblVersionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Update the gene and GO mappings according to the selected species.
     *
     * @param evt
     */
    private void speciesJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speciesJComboBoxActionPerformed

        String currentEnsemblSpeciesType = (String) ensemblCategoryJComboBox.getSelectedItem();
        String selectedSpecies = getSelectedSpecies();

        if (selectedSpecies != null) {
            updateMappingsButton.setEnabled(true);
            if (genePreferences.getEnsemblSpeciesVersion(currentEnsemblSpeciesType, selectedSpecies) == null) {
                updateMappingsButton.setText("Download");
            } else {
                updateMappingsButton.setText("Update");
            }
        } else {
            updateMappingsButton.setText("Download");
            updateMappingsButton.setEnabled(false);
        }
    }//GEN-LAST:event_speciesJComboBoxActionPerformed

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void unknownSpeciesLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unknownSpeciesLabelMouseClicked
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/SpeciesDialog.html"), "#Species",
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                "Species - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_unknownSpeciesLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void unknownSpeciesLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unknownSpeciesLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_unknownSpeciesLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void unknownSpeciesLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unknownSpeciesLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_unknownSpeciesLabelMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void ensemblVersionLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ensemblVersionLabelMouseClicked
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/SpeciesDialog.html"), "#Ensembl_Version",
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                "Species - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_ensemblVersionLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void ensemblVersionLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ensemblVersionLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_ensemblVersionLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void ensemblVersionLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ensemblVersionLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_ensemblVersionLabelMouseExited

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        String currentEnsemblSpeciesType = (String) ensemblCategoryJComboBox.getSelectedItem();
        String selectedSpecies = getSelectedSpecies();

        if (selectedSpecies != null) {
            if (genePreferences.getEnsemblSpeciesVersion(currentEnsemblSpeciesType, selectedSpecies) == null) {
                int option = JOptionPane.showConfirmDialog(this,
                        "The gene and GO annotations are not downloaded for the selected species.\n"
                        + "Download now?", "Gene Annotation Missing", JOptionPane.YES_NO_CANCEL_OPTION);

                if (option == JOptionPane.CANCEL_OPTION) {
                    // cancel the closing of the dialog
                } else if (option == JOptionPane.YES_OPTION) {
                    downloadMappings();
                } else {
                    genePreferences.setCurrentSpecies(null);
                    genePreferences.setCurrentSpeciesType(null);
                    dispose();
                }
            } else {
                genePreferences.setCurrentSpecies(selectedSpecies);
                genePreferences.setCurrentSpeciesType(currentEnsemblSpeciesType);
                dispose();
            }
        } else {
            genePreferences.setCurrentSpecies(null);
            genePreferences.setCurrentSpeciesType(null);
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Download or update the Ensembl mappings.
     *
     * @param evt
     */
    private void updateMappingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateMappingsButtonActionPerformed
        if (geneFactory.getCurrentEnsemblVersion(getEnsemblType()) != null) {
            if (updateMappingsButton.getText().equalsIgnoreCase("Download")) {
                genePreferences.clearOldMappings((String) ensemblCategoryJComboBox.getSelectedItem(), getSelectedSpecies(), false);
                downloadMappings();
            } else { // update

                // check if newer mappings are available
                Integer latestEnsemblVersion = geneFactory.getCurrentEnsemblVersion(getEnsemblType());

                String currentEnsemblSpeciesType = (String) ensemblCategoryJComboBox.getSelectedItem();
                String selectedSpecies = getSelectedSpecies();
                String selectedDb = genePreferences.getEnsemblDatabaseName(currentEnsemblSpeciesType, selectedSpecies);
                String currentEnsemblVersionAsString = genePreferences.getEnsemblVersion(selectedDb);

                if (currentEnsemblVersionAsString != null) {

                    currentEnsemblVersionAsString = currentEnsemblVersionAsString.substring(currentEnsemblVersionAsString.indexOf(" ") + 1);
                    Integer currentEnsemblVersion;

                    try {
                        currentEnsemblVersion = new Integer(currentEnsemblVersionAsString);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        currentEnsemblVersion = latestEnsemblVersion;
                    }

                    if (currentEnsemblVersion < latestEnsemblVersion) {
                        genePreferences.clearOldMappings(currentEnsemblSpeciesType, selectedSpecies, false);
                        downloadMappings();
                    } else {
                        JOptionPane.showMessageDialog(this, "Ensembl mappings are already up to date.", "Ensembl Mappings", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Ensembl mapping not available. Try again later.", "Ensembl Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_updateMappingsButtonActionPerformed

    /**
     * Update the species list to the selected Ensembl version.
     *
     * @param evt
     */
    private void ensemblCategoryJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ensemblCategoryJComboBoxActionPerformed
        updateSpeciesList();
    }//GEN-LAST:event_ensemblCategoryJComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JComboBox ensemblCategoryJComboBox;
    private javax.swing.JLabel ensemblVersionLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox speciesJComboBox;
    private javax.swing.JLabel unknownSpeciesLabel;
    private javax.swing.JButton updateMappingsButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Clear the old results.
     */
    private void clearOldResults() {

        goFactory.clearFactory();
        geneFactory.clearFactory();

        try {
            goFactory.closeFiles();
            geneFactory.closeFiles();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occured when clearing the mappings.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Try to download the gene and GO mappings for the currently selected
     * species.
     *
     * @param evt
     */
    private void downloadMappings() {

        if (dialogParent == null) {
            progressDialog = new ProgressDialogX(frameParent,
                    normalImage,
                    waitingImage,
                    true);
        } else {
            progressDialog = new ProgressDialogX(dialogParent, frameParent,
                    normalImage,
                    waitingImage,
                    true);
        }

        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Sending Request. Please Wait...");
        final SpeciesDialog finalRef = this;

        new Thread(new Runnable() {
            public void run() {
                progressDialog.setVisible(true);
            }
        }, "ProgressDialog").start();

        new Thread("DownloadThread") {
            @Override
            public void run() {

                boolean success = false;

                try {
                    success = genePreferences.downloadMappings(progressDialog, (String) ensemblCategoryJComboBox.getSelectedItem(), getSelectedSpecies(), false);

                    if (success) {
                        int selectedIndex = speciesJComboBox.getSelectedIndex();
                        updateSpeciesList();
                        JOptionPane.showMessageDialog(finalRef, "Gene mappings downloaded.", "Gene Mappings", JOptionPane.INFORMATION_MESSAGE);
                        speciesJComboBox.setSelectedIndex(selectedIndex);
                        speciesJComboBoxActionPerformed(null);
                    }
                } catch (Exception e) {
                    progressDialog.setRunFinished();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(finalRef, "An error occured when downloading the mappings.", "Download Error", JOptionPane.ERROR_MESSAGE);
                }

                // an error occured, clear the possible half downloaded data
                if (!success) {
                    genePreferences.clearOldMappings((String) ensemblCategoryJComboBox.getSelectedItem(), getSelectedSpecies(), false);
                }
            }
        }.start();
    }

    /**
     * Returns the Ensembl type, e.g., ensembl or plants.
     *
     * @return returns the Ensembl type
     */
    private String getEnsemblType() {

        int selectedIndex = ensemblCategoryJComboBox.getSelectedIndex();

        switch (selectedIndex) {
            case 1:
                return "fungi";
            case 2:
                return "plants";
            case 3:
                return "protists";
            case 4:
                return "metazoa";
            case 5:
                return "ensembl";
        }

        return "unknown"; // shouldn't be possible
    }
}
