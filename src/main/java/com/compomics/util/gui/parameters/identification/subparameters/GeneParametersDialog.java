package com.compomics.util.gui.parameters.identification.subparameters;

import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.parameters.identification.GeneParameters;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

/**
 * Dialog for editing the Gene Mapping Preferences.
 *
 * @author Harald Barsnes
 */
public class GeneParametersDialog extends javax.swing.JDialog {

    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * True of the dialog was canceled by the user.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;
    /**
     * The gene preferences.
     */
    private GeneParameters genePreferences;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The species factory.
     */
    private SpeciesFactory speciesFactory = SpeciesFactory.getInstance();
    /**
     * A map from the species names used in the drop down menu to the taxon.
     */
    private HashMap<String, Integer> speciesMap;

    /**
     * Creates a new GenePreferencesDialog with a frame as owner.
     *
     * @param parentFrame the parent frame
     * @param genePreferences the gene preferences
     * @param searchParameters the search parameters
     * @param speciesOccurrence a map of the occurrence of species
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public GeneParametersDialog(JFrame parentFrame, GeneParameters genePreferences, SearchParameters searchParameters, HashMap<String, Integer> speciesOccurrence, boolean editable) {
        super(parentFrame, true);
        this.parentFrame = parentFrame;
        this.editable = editable;
        this.genePreferences = genePreferences;
        this.searchParameters = searchParameters;
        initComponents();
        setUpGui(speciesOccurrence);
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new GenePreferencesDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame a parent frame
     * @param genePreferences the gene preferences
     * @param searchParameters the search parameters
     * @param speciesOccurrence a map of the occurrence of species
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public GeneParametersDialog(JDialog owner, java.awt.Frame parentFrame, GeneParameters genePreferences, SearchParameters searchParameters, HashMap<String, Integer> speciesOccurrence, boolean editable) {
        super(owner, true);
        this.parentFrame = parentFrame;
        this.editable = editable;
        this.genePreferences = genePreferences;
        this.searchParameters = searchParameters;
        initComponents();
        setUpGui(speciesOccurrence);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     * 
     * @param speciesOccurrence a map of the occurrence of species
     */
    private void setUpGui(HashMap<String, Integer> speciesOccurrence) {
        
        speciesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        useMappingCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        autoUpdateCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        useMappingCmb.setEnabled(editable);
        autoUpdateCmb.setEnabled(editable);

        // set the species
        Vector availableSpecies = new Vector();
        speciesMap = new HashMap<>();

        File fastaFile = searchParameters.getFastaFile();

        if (fastaFile != null) {

            int selectedIndex = 0;

            try {

                // Select the background species based on occurrence in the factory
                for (String uniprotTaxonomy : speciesOccurrence.keySet()) {

                    if (!uniprotTaxonomy.equals(SpeciesFactory.UNKNOWN)) {
                        Integer occurrence = speciesOccurrence.get(uniprotTaxonomy);

                        if (occurrence != null) {
                            try {
                                Integer taxon = speciesFactory.getUniprotTaxonomy().getId(uniprotTaxonomy, true);
                                if (taxon != null) {
                                    if (genePreferences.getSelectedBackgroundSpecies() != null
                                            && genePreferences.getSelectedBackgroundSpecies().intValue() == taxon) {
                                        selectedIndex = availableSpecies.size();
                                    }
                                    String tempSpecies = speciesFactory.getName(taxon) + " (" + occurrence + ")";
                                    availableSpecies.add(tempSpecies);
                                    speciesMap.put(tempSpecies, taxon);
                                }
                            } catch (Exception e) {
                                // taxon not available, ignore
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Not able to read the species, ignore
                e.printStackTrace();
            }

            speciesCmb.setModel(new DefaultComboBoxModel(availableSpecies));
            if (!availableSpecies.isEmpty()) {
                speciesCmb.setSelectedIndex(selectedIndex);
            }
        } else {
            availableSpecies.add("(no species available)");
            speciesCmb.setModel(new DefaultComboBoxModel(availableSpecies));
            speciesCmb.setEnabled(false);
        }

        // set if the gene mappings are to be used
        if (genePreferences.getUseGeneMapping()) {
            useMappingCmb.setSelectedIndex(0);
        } else {
            useMappingCmb.setSelectedIndex(1);
        }

        // set if the gene mappings are to be auto updated
        if (genePreferences.getAutoUpdate()) {
            autoUpdateCmb.setSelectedIndex(0);
        } else {
            autoUpdateCmb.setSelectedIndex(1);
        }
    }

    /**
     * Returns the gene preferences.
     *
     * @return the gene preferences
     */
    public GeneParameters getGenePreferences() {

        GeneParameters tempGenePreferences = new GeneParameters();

        tempGenePreferences.setUseGeneMapping(useMappingCmb.getSelectedIndex() == 0);
        tempGenePreferences.setAutoUpdate(autoUpdateCmb.getSelectedIndex() == 0);

        if (speciesCmb.isEnabled()) {
            tempGenePreferences.setSelectedBackgroundSpecies(speciesMap.get((String) speciesCmb.getSelectedItem()));
        }

        return tempGenePreferences;
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        mappingPanel = new javax.swing.JPanel();
        speciesLabel = new javax.swing.JLabel();
        speciesCmb = new javax.swing.JComboBox();
        useMappingLabel = new javax.swing.JLabel();
        useMappingCmb = new javax.swing.JComboBox();
        autoUpdateLabel = new javax.swing.JLabel();
        autoUpdateCmb = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Gene Annotation");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        mappingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));
        mappingPanel.setOpaque(false);

        speciesLabel.setText("Species");

        useMappingLabel.setText("Use Mapping");

        useMappingCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        autoUpdateLabel.setText("Auto Update");

        autoUpdateCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout mappingPanelLayout = new javax.swing.GroupLayout(mappingPanel);
        mappingPanel.setLayout(mappingPanelLayout);
        mappingPanelLayout.setHorizontalGroup(
            mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(speciesLabel)
                    .addComponent(useMappingLabel)
                    .addComponent(autoUpdateLabel))
                .addGap(18, 18, 18)
                .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(speciesCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(useMappingCmb, 0, 364, Short.MAX_VALUE)
                    .addComponent(autoUpdateCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mappingPanelLayout.setVerticalGroup(
            mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(speciesLabel)
                    .addComponent(speciesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useMappingLabel)
                    .addComponent(useMappingCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoUpdateLabel)
                    .addComponent(autoUpdateCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mappingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(helpJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mappingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(okButton)
                    .addComponent(cancelButton)
                    .addComponent(helpJButton))
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
     * Close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        okButtonActionPerformed(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Cancel the dialog.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

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
        new HelpDialog(parentFrame, getClass().getResource("/helpFiles/GeneAnnotationPreferences.html"),
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
            "Gene Annotation - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox autoUpdateCmb;
    private javax.swing.JLabel autoUpdateLabel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton helpJButton;
    private javax.swing.JPanel mappingPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox speciesCmb;
    private javax.swing.JLabel speciesLabel;
    private javax.swing.JComboBox useMappingCmb;
    private javax.swing.JLabel useMappingLabel;
    // End of variables declaration//GEN-END:variables
}
