package com.compomics.util.gui.gene_mapping;

import com.compomics.util.experiment.annotation.gene.GeneFactory;
import com.compomics.util.experiment.annotation.go.GOFactory;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.preferences.GenePreferences;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
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
     * The species separator used in the species comboboxes.
     */
    public final static String SPECIES_SEPARATOR = "------------------------------------------------------------";
    /**
     * The text to use to tell the user to please select a species in the list.
     */
    public final static String SELECT_SPECIES_TAG = "-- Select Species --";
    /**
     * The text to use for no species selected.
     */
    public final static String NO_SPECIES_TAG = "-- (no selection) --";
    /**
     * Position of the species separator (first species is 0)
     */
    private int separatorPosition = 3;
    /**
     * The icon to display when waiting
     */
    private Image waitingImage = null;
    /**
     * The icon to display when processing is finished
     */
    private Image normalImage = null;
    /**
     * The gene preferences
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
        setLocationRelativeTo(parentDialog);
        setVisible(true);
    }

    /**
     * Set up the GUI details.
     */
    private void setUpGUI() {
        speciesJComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        String[] speciesCmbContent = getComboBoxContent();
        speciesJComboBox.setModel(new DefaultComboBoxModel(speciesCmbContent));

        // select the current species
        String selectedSpecies = genePreferences.getCurrentSpecies();
        if (selectedSpecies != null) {
            for (int i = 0; i < speciesCmbContent.length; i++) {
                String content = speciesCmbContent[i];
                if (content.contains(selectedSpecies)) {
                    speciesJComboBox.setSelectedIndex(i);
                    boolean dbVersion = content.contains("N/A");
                    updateButton.setEnabled(dbVersion);
                    downloadButton.setEnabled(!dbVersion);
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
    private String[] getComboBoxContent() {

        ArrayList<String> availableSpecies = genePreferences.getSpecies();

        if (availableSpecies != null && !availableSpecies.isEmpty()) {

            String[] content = new String[availableSpecies.size() + 5];
            content[0] = SELECT_SPECIES_TAG;
            content[1] = SPECIES_SEPARATOR;

            for (int i = 0; i < availableSpecies.size(); i++) {
                int cpt = i + 2;
                if (i == separatorPosition) {
                    content[cpt] = SPECIES_SEPARATOR;
                } else {
                    if (i > separatorPosition) {
                        cpt++;
                    }
                    String currentSpecies = availableSpecies.get(i);
                    String ensemblVersion = genePreferences.getEnsemblSpeciesVersion(currentSpecies);
                    if (ensemblVersion == null) {
                        ensemblVersion = "N/A";
                    }
                    content[cpt] = currentSpecies + " [" + ensemblVersion + "]";
                }
            }
            content[availableSpecies.size() + 3] = SPECIES_SEPARATOR;
            content[availableSpecies.size() + 4] = NO_SPECIES_TAG;
            return content;
        } else {
            String[] content = new String[1];
            content[0] = NO_SPECIES_TAG;
            return content;
        }
    }

    /**
     * Returns the species selected in the species JComboBox.
     *
     * @return the species selected
     */
    private String getSelectedSpecies() {
        int selection = speciesJComboBox.getSelectedIndex();
        if (selection > 1
                && selection != separatorPosition + 2
                && selection < genePreferences.getSpecies().size() + 3) {
            int index = selection - 2;
            if (index > separatorPosition) {
                index++;
            }
            return genePreferences.getSpecies().get(index);
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
        updateButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
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

        updateButton.setText("Update");
        updateButton.setToolTipText("Update the GO Mappings");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        downloadButton.setText("Download");
        downloadButton.setToolTipText("Download GO Mappings");
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(speciesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 409, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(downloadButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {downloadButton, updateButton});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(speciesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {downloadButton, speciesJComboBox, updateButton});

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
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(unknownSpeciesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(ensemblVersionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(unknownSpeciesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ensemblVersionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(22, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addContainerGap())))
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
        String selectedSpecies = (String) speciesJComboBox.getSelectedItem();
        boolean separatorSelection = selectedSpecies.equals(SPECIES_SEPARATOR) || selectedSpecies.equals(SELECT_SPECIES_TAG);
        okButton.setEnabled(!separatorSelection);
        boolean speciesSelected = !separatorSelection && !selectedSpecies.equals(NO_SPECIES_TAG);
        downloadButton.setEnabled(speciesSelected);
        updateButton.setEnabled(speciesSelected);
    }//GEN-LAST:event_speciesJComboBoxActionPerformed

    /**
     * Try to download the GO mappings for the currently selected species.
     *
     * @param evt
     */
    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed

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

        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Sending Request. Please Wait...");
        final SpeciesDialog finalRef = this;

        new Thread(new Runnable() {
            public void run() {
                progressDialog.setVisible(true);
            }
        }, "ProgressDialog").start();

        new Thread("GoThread") {
            @Override
            public void run() {

                try {
                    // clear old data
                    clearOldResults();

                    // get the current Ensembl version
                    URL url = new URL("http://www.biomart.org/biomart/martservice?type=registry");

                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                    String inputLine;
                    boolean ensemblVersionFound = false;
                    String ensemblVersion = "?";

                    while ((inputLine = in.readLine()) != null && !ensemblVersionFound && !progressDialog.isRunCanceled()) {
                        if (inputLine.indexOf("database=\"ensembl_mart_") != -1) {
                            ensemblVersion = inputLine.substring(inputLine.indexOf("database=\"ensembl_mart_") + "database=\"ensembl_mart_".length());
                            ensemblVersion = ensemblVersion.substring(0, ensemblVersion.indexOf("\""));
                            ensemblVersionFound = true;
                        }
                    }

                    in.close();

                    String selectedSpecies = getSelectedSpecies();
                    String selectedDb = genePreferences.getEnsemblDatabaseName(selectedSpecies);

                    boolean geneMappingsDownloaded = false;
                    boolean goMappingsDownloadeded = false;

                    if (!progressDialog.isRunCanceled()) {
                        genePreferences.downloadGoMappings(selectedDb, ensemblVersion, progressDialog);
                    }
                    if (goMappingsDownloadeded && !progressDialog.isRunCanceled()) {
                        genePreferences.downloadGeneMappings(selectedDb, progressDialog);
                    }

                    progressDialog.setRunFinished();

                    if (geneMappingsDownloaded && goMappingsDownloadeded) {
                        JOptionPane.showMessageDialog(finalRef, "Gene mappings downloaded.\nRe-select species to use.", "Gene Mappings", JOptionPane.INFORMATION_MESSAGE);
                        genePreferences.loadSpeciesAndGoDomains();
                        speciesJComboBox.setModel(new DefaultComboBoxModel(getComboBoxContent()));
                        speciesJComboBox.setSelectedIndex(0);
                    }

                    // @TODO: the code below ought to work, but results in bugs...
                    //        therefore the user now has to reselect in the drop down menu
                    //                    int index = speciesJComboBox.getSelectedIndex();
                    //                    loadSpeciesAndGoDomains();
                    //                    speciesJComboBox.setSelectedIndex(index);
                    //                    speciesJComboBoxActionPerformed(null);
                } catch (Exception e) {
                    progressDialog.setRunFinished();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(finalRef, "An error occured when downloading the mappings.", "Download Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.start();
    }//GEN-LAST:event_downloadButtonActionPerformed

    /**
     * Tries to update the GO mappings for the currently selected species.
     *
     * @param evt
     */
    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed

        // delete the old mappings file
        String selectedSpecies = getSelectedSpecies();
        String selectedDb = genePreferences.getEnsemblDatabaseName(selectedSpecies);

        goFactory.clearFactory();
        geneFactory.clearFactory();

        try {
            goFactory.closeFiles();
            geneFactory.closeFiles();

            File tempSpeciesGoFile = new File(genePreferences.getGeneMappingFolder(), selectedDb + GenePreferences.GO_MAPPING_FILE_SUFFIX);
            File tempSpecieGenesFile = new File(genePreferences.getGeneMappingFolder(), selectedDb + GenePreferences.GENE_MAPPING_FILE_SUFFIX);

            boolean goFileDeleted = true;
            boolean geneFileDeleted = true;

            if (tempSpeciesGoFile.exists()) {
                goFileDeleted = tempSpeciesGoFile.delete();

                if (!goFileDeleted) {
                    JOptionPane.showMessageDialog(this, "Failed to delete \'" + tempSpeciesGoFile.getAbsolutePath() + "\'.\n"
                            + "Please delete the file manually, reselect the species in the list and click the Download button instead.", "Delete Failed",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            }

            if (tempSpecieGenesFile.exists()) {
                geneFileDeleted = tempSpecieGenesFile.delete();

                if (!geneFileDeleted) {
                    JOptionPane.showMessageDialog(this, "Failed to delete \'" + tempSpecieGenesFile.getAbsolutePath() + "\'.\n"
                            + "Please delete the file manually, reselect the species in the list and click the Download button instead.", "Delete Failed",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }

            if (goFileDeleted && geneFileDeleted) {
                downloadButtonActionPerformed(null);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occured when trying to update the mappings.", "File Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_updateButtonActionPerformed

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

        String selectedSpecies = getSelectedSpecies();

        if (selectedSpecies != null) {

            if (genePreferences.getEnsemblSpeciesVersion(selectedSpecies) == null) {
                int option = JOptionPane.showConfirmDialog(this,
                        "The gene and GO annotations are not downloaded for the selected species.\n"
                        + "Download now?", "Gene Annotation Missing", JOptionPane.YES_NO_CANCEL_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    downloadButtonActionPerformed(null);
                }
            } else {
                genePreferences.setCurrentSpecies(selectedSpecies);
                dispose();
            }

        } else {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton downloadButton;
    private javax.swing.JLabel ensemblVersionLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox speciesJComboBox;
    private javax.swing.JLabel unknownSpeciesLabel;
    private javax.swing.JButton updateButton;
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
}
