package com.compomics.util.gui.parameters;

import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.gui.gene_mapping.SpeciesDialog;
import com.compomics.util.gui.parameters.identification_parameters.AnnotationSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.MatchesImportFiltersDialog;
import com.compomics.util.gui.parameters.identification_parameters.PTMLocalizationParametersDialog;
import com.compomics.util.gui.parameters.identification_parameters.SearchSettingsDialog;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.io.ConfigurationFile;
import com.compomics.util.preferences.GenePreferences;
import com.compomics.util.preferences.IdMatchValidationPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.LastSelectedFolder;
import com.compomics.util.preferences.PTMScoringPreferences;
import com.compomics.util.preferences.ProteinInferencePreferences;
import com.compomics.util.preferences.PsmScoringPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.SwingConstants;

/**
 *
 * @author Marc Vaudel
 */
public class IdentificationParametersDialog extends javax.swing.JDialog {

    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * The normal icon.
     */
    private Image normalIcon;
    /**
     * The waiting icon.
     */
    private Image waitingIcon;
    /**
     * The last selected folder
     */
    private LastSelectedFolder lastSelectedFolder;
    /**
     * Boolean indicating whether the parameters can be edited.
     */
    private boolean editable;
    /**
     * The configuration file containing the modification use.
     */
    private ConfigurationFile configurationFile;
    /**
     * The peak annotation settings.
     */
    private AnnotationSettings annotationSettings;
    /**
     * The parameters used for the spectrum matching.
     */
    private SearchParameters searchParameters;
    /**
     * The peptide to protein matching preferences.
     */
    private SequenceMatchingPreferences sequenceMatchingPreferences;
    /**
     * The gene preferences.
     */
    private GenePreferences genePreferences;
    /**
     * The PSM scores to use.
     */
    private PsmScoringPreferences psmScoringPreferences;
    /**
     * The PSM filter.
     */
    private PeptideAssumptionFilter peptideAssumptionFilter = new PeptideAssumptionFilter();
    /**
     * The PTM localization scoring preferences.
     */
    private PTMScoringPreferences ptmScoringPreferences = new PTMScoringPreferences();
    /**
     * The protein inference preferences.
     */
    private ProteinInferencePreferences proteinInferencePreferences;
    /**
     * The identification validation preferences.
     */
    private IdMatchValidationPreferences idValidationPreferences = new IdMatchValidationPreferences();
    /**
     * The possible neutral losses in a list.
     */
    private ArrayList<NeutralLoss> possibleNeutralLosses;
    /**
     * List of possible reporter ions.
     */
    private ArrayList<Integer> reporterIons;

    /**
     * Constructor.
     * 
     * @param parentFrame the parent frame
     * @param identificationParameters the identification parameters to display
     * @param configurationFile the configuration file containing the PTM usage preferences
     * @param possibleNeutralLosses the possible neutral losses
     * @param reporterIons the possible reporter ions
     * @param normalIcon the normal icon
     * @param waitingIcon the waiting icon
     * @param lastSelectedFolder the last selected folder
     * @param editable boolean indicating whether the parameters can be edited
     */
    public IdentificationParametersDialog(java.awt.Frame parentFrame, IdentificationParameters identificationParameters, ConfigurationFile configurationFile, ArrayList<NeutralLoss> possibleNeutralLosses, ArrayList<Integer> reporterIons, Image normalIcon, Image waitingIcon, LastSelectedFolder lastSelectedFolder, boolean editable) {
        super(parentFrame, true);

        this.parentFrame = parentFrame;
        this.annotationSettings = identificationParameters.getAnnotationPreferences();
        this.searchParameters = identificationParameters.getSearchParameters();
        this.sequenceMatchingPreferences = identificationParameters.getSequenceMatchingPreferences();
        this.genePreferences = identificationParameters.getGenePreferences();
        this.psmScoringPreferences = identificationParameters.getPsmScoringPreferences();
        this.peptideAssumptionFilter = identificationParameters.getPeptideAssumptionFilter();
        this.ptmScoringPreferences = identificationParameters.getPtmScoringPreferences();
        this.proteinInferencePreferences = identificationParameters.getProteinInferencePreferences();
        this.idValidationPreferences = identificationParameters.getIdValidationPreferences();
        this.reporterIons = reporterIons;
        this.possibleNeutralLosses = possibleNeutralLosses;

        initComponents();
        setUpGui();
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {

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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        spectrumMatchingButton = new javax.swing.JButton();
        matchesFiltersButton = new javax.swing.JButton();
        sequenceMatchingButton = new javax.swing.JButton();
        validationButton = new javax.swing.JButton();
        spectrumAnnotationButton = new javax.swing.JButton();
        psmScoringButton = new javax.swing.JButton();
        ptmLocalizationButton = new javax.swing.JButton();
        geneMappingButton = new javax.swing.JButton();
        proteinInferenceButton = new javax.swing.JButton();
        qualityControlButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        jButton1.setText("Cancel");

        jButton2.setText("OK");

        spectrumMatchingButton.setText("Spectrum Matching");
        spectrumMatchingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spectrumMatchingButtonActionPerformed(evt);
            }
        });

        matchesFiltersButton.setText("Matches Filters");
        matchesFiltersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchesFiltersButtonActionPerformed(evt);
            }
        });

        sequenceMatchingButton.setText("Sequence to Protein Matching");
        sequenceMatchingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequenceMatchingButtonActionPerformed(evt);
            }
        });

        validationButton.setText("Validation");
        validationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validationButtonActionPerformed(evt);
            }
        });

        spectrumAnnotationButton.setText("Spectrum Annotation");
        spectrumAnnotationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spectrumAnnotationButtonActionPerformed(evt);
            }
        });

        psmScoringButton.setText("PSM Scoring");
        psmScoringButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                psmScoringButtonActionPerformed(evt);
            }
        });

        ptmLocalizationButton.setText("PTM Localization");
        ptmLocalizationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ptmLocalizationButtonActionPerformed(evt);
            }
        });

        geneMappingButton.setText("Protein to Gene Mapping");
        geneMappingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geneMappingButtonActionPerformed(evt);
            }
        });

        proteinInferenceButton.setText("Protein Inference");
        proteinInferenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proteinInferenceButtonActionPerformed(evt);
            }
        });

        qualityControlButton.setText("Quality Control");
        qualityControlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qualityControlButtonActionPerformed(evt);
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
                        .addGap(0, 340, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(qualityControlButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(spectrumMatchingButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(matchesFiltersButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(validationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(psmScoringButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(sequenceMatchingButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ptmLocalizationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(geneMappingButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(proteinInferenceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(spectrumAnnotationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spectrumMatchingButton)
                .addGap(18, 18, 18)
                .addComponent(spectrumAnnotationButton)
                .addGap(18, 18, 18)
                .addComponent(sequenceMatchingButton)
                .addGap(18, 18, 18)
                .addComponent(geneMappingButton)
                .addGap(18, 18, 18)
                .addComponent(matchesFiltersButton)
                .addGap(18, 18, 18)
                .addComponent(psmScoringButton)
                .addGap(18, 18, 18)
                .addComponent(ptmLocalizationButton)
                .addGap(18, 18, 18)
                .addComponent(proteinInferenceButton)
                .addGap(18, 18, 18)
                .addComponent(validationButton)
                .addGap(18, 18, 18)
                .addComponent(qualityControlButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
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

    private void spectrumAnnotationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spectrumAnnotationButtonActionPerformed
        AnnotationSettingsDialog annotationSettingsDialog = new AnnotationSettingsDialog(parentFrame, annotationSettings, possibleNeutralLosses, reporterIons);
        if (!annotationSettingsDialog.isCanceled()) {
            AnnotationSettings newAnnotationSettings = annotationSettingsDialog.getAnnotationSettings();
            if (!newAnnotationSettings.isSameAs(annotationSettings)) {
                annotationSettings = newAnnotationSettings;
            }
        }
    }//GEN-LAST:event_spectrumAnnotationButtonActionPerformed

    private void spectrumMatchingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spectrumMatchingButtonActionPerformed
        SearchSettingsDialog searchSettingsDialog = new SearchSettingsDialog(null, searchParameters, normalIcon, waitingIcon, editable, editable, configurationFile, lastSelectedFolder, editable);
        if (editable && !searchSettingsDialog.isCanceled()) {
            searchParameters = searchSettingsDialog.getSearchParameters();
        }
    }//GEN-LAST:event_spectrumMatchingButtonActionPerformed

    private void sequenceMatchingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequenceMatchingButtonActionPerformed
        // @TODO make a dialog
    }//GEN-LAST:event_sequenceMatchingButtonActionPerformed

    private void geneMappingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geneMappingButtonActionPerformed
        SpeciesDialog speciesDialog = new SpeciesDialog(null, genePreferences, true, waitingIcon, normalIcon);
        // @TODO decouple the gene factory from the preferences
    }//GEN-LAST:event_geneMappingButtonActionPerformed

    private void matchesFiltersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchesFiltersButtonActionPerformed
        MatchesImportFiltersDialog matchesImportFiltersDialog = new MatchesImportFiltersDialog(parentFrame, peptideAssumptionFilter, editable);
        if (!matchesImportFiltersDialog.isCanceled()) {
            PeptideAssumptionFilter newPeptideAssumptionFilter = matchesImportFiltersDialog.getFilter();
            if (!newPeptideAssumptionFilter.isSameAs(peptideAssumptionFilter)) {
                peptideAssumptionFilter = newPeptideAssumptionFilter;
            }
        }
    }//GEN-LAST:event_matchesFiltersButtonActionPerformed

    private void psmScoringButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_psmScoringButtonActionPerformed
        // @TODO create dialog
    }//GEN-LAST:event_psmScoringButtonActionPerformed

    private void ptmLocalizationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ptmLocalizationButtonActionPerformed
        PTMLocalizationParametersDialog ptmLocalizationParametersDialog = new PTMLocalizationParametersDialog(parentFrame, ptmScoringPreferences);
        if (!ptmLocalizationParametersDialog.isCanceled()) {
            ptmScoringPreferences = ptmLocalizationParametersDialog.getPtmScoringPreferences();
        }
    }//GEN-LAST:event_ptmLocalizationButtonActionPerformed

    private void proteinInferenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proteinInferenceButtonActionPerformed
        // @TODO create dialog
    }//GEN-LAST:event_proteinInferenceButtonActionPerformed

    private void validationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validationButtonActionPerformed
        // @TODO create dialog
    }//GEN-LAST:event_validationButtonActionPerformed

    private void qualityControlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qualityControlButtonActionPerformed
        // @TODO use dialog
    }//GEN-LAST:event_qualityControlButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton geneMappingButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton matchesFiltersButton;
    private javax.swing.JButton proteinInferenceButton;
    private javax.swing.JButton psmScoringButton;
    private javax.swing.JButton ptmLocalizationButton;
    private javax.swing.JButton qualityControlButton;
    private javax.swing.JButton sequenceMatchingButton;
    private javax.swing.JButton spectrumAnnotationButton;
    private javax.swing.JButton spectrumMatchingButton;
    private javax.swing.JButton validationButton;
    // End of variables declaration//GEN-END:variables

}
