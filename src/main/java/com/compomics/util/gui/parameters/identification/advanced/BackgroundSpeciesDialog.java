package com.compomics.util.gui.parameters.identification.advanced;

import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.io.biology.protein.FastaSummary;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.parameters.identification.advanced.GeneParameters;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

/**
 * Dialog for editing the background species.
 *
 * @author Harald Barsnes
 */
public class BackgroundSpeciesDialog extends javax.swing.JDialog {

    /**
     * Empty default constructor
     */
    public BackgroundSpeciesDialog() {
    }

    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * True of the dialog was canceled by the user.
     */
    private boolean canceled = false;
    /**
     * The gene preferences.
     */
    private GeneParameters genePreferences;
    /**
     * The search parameters.
     */
    private FastaSummary fastaSummary;
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
     * @param fastaSummary the FASTA summary
     */
    public BackgroundSpeciesDialog(JFrame parentFrame, GeneParameters genePreferences, FastaSummary fastaSummary) {
        super(parentFrame, true);
        this.parentFrame = parentFrame;
        this.genePreferences = genePreferences;
        this.fastaSummary = fastaSummary;
        initComponents();
        setUpGui();
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new GenePreferencesDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame a parent frame
     * @param genePreferences the gene preferences
     * @param fastaSummary the FASTA summary
     */
    public BackgroundSpeciesDialog(JDialog owner, java.awt.Frame parentFrame, GeneParameters genePreferences, FastaSummary fastaSummary) {
        super(owner, true);
        this.parentFrame = parentFrame;
        this.genePreferences = genePreferences;
        this.fastaSummary = fastaSummary;
        initComponents();
        setUpGui();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGui() {

        speciesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        // set the species
        Vector availableSpecies = new Vector();
        speciesMap = new HashMap<>();

        int selectedIndex = 0;

        try {

            TreeMap<String, Integer> speciesOccurrence = fastaSummary.speciesOccurrence;

            // Select the background species based on occurrence in the factory
            for (Entry<String, Integer> entry : speciesOccurrence.entrySet()) {

                String uniprotTaxonomy = entry.getKey();

                if (!uniprotTaxonomy.equals(SpeciesFactory.UNKNOWN)) {

                    Integer occurrence = entry.getValue();

                    if (occurrence != null) {

                        try {

                            Integer taxon = speciesFactory.getUniprotTaxonomy().getId(uniprotTaxonomy, true);

                            if (taxon != null) {

                                if (genePreferences.getBackgroundSpecies() != null
                                        && genePreferences.getBackgroundSpecies().intValue() == taxon) {

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

    }

    /**
     * Returns the gene preferences.
     *
     * @return the gene preferences
     */
    public GeneParameters getGeneParameters() {
        return genePreferences;
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

        mappingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Background Species"));
        mappingPanel.setOpaque(false);

        speciesLabel.setText("Species");

        javax.swing.GroupLayout mappingPanelLayout = new javax.swing.GroupLayout(mappingPanel);
        mappingPanel.setLayout(mappingPanelLayout);
        mappingPanelLayout.setHorizontalGroup(
            mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(speciesLabel)
                .addGap(18, 18, 18)
                .addComponent(speciesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mappingPanelLayout.setVerticalGroup(
            mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(speciesLabel)
                    .addComponent(speciesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
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
        if (speciesCmb.getSelectedIndex() != -1) {
            genePreferences.setBackgroundSpecies(speciesMap.get((String) speciesCmb.getSelectedItem()));
        }    
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
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton helpJButton;
    private javax.swing.JPanel mappingPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox speciesCmb;
    private javax.swing.JLabel speciesLabel;
    // End of variables declaration//GEN-END:variables
}
