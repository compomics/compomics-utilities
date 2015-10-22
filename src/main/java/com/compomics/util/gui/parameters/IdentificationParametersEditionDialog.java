package com.compomics.util.gui.parameters;

import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.gui.gene_mapping.SpeciesDialog;
import com.compomics.util.gui.parameters.identification_parameters.AnnotationSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.FractionSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.MatchesImportFiltersDialog;
import com.compomics.util.gui.parameters.identification_parameters.PTMLocalizationParametersDialog;
import com.compomics.util.gui.parameters.identification_parameters.ProteinInferenceSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.PsmScoringSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.SearchSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.SequenceMatchingSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.ValidationQCPreferencesDialog;
import com.compomics.util.gui.parameters.identification_parameters.ValidationQCPreferencesDialogParent;
import com.compomics.util.gui.parameters.identification_parameters.ValidationSettingsDialog;
import com.compomics.util.io.ConfigurationFile;
import com.compomics.util.preferences.FractionSettings;
import com.compomics.util.preferences.GenePreferences;
import com.compomics.util.preferences.IdMatchValidationPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.LastSelectedFolder;
import com.compomics.util.preferences.PTMScoringPreferences;
import com.compomics.util.preferences.ProteinInferencePreferences;
import com.compomics.util.preferences.PsmScoringPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.preferences.ValidationQCPreferences;
import java.awt.Image;
import java.util.ArrayList;

/**
 * IdentificationParametersEditionDialog.
 *
 * @author Marc Vaudel
 */
public class IdentificationParametersEditionDialog extends javax.swing.JDialog {

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
     * The possible neutral losses in a list.
     */
    private ArrayList<NeutralLoss> possibleNeutralLosses;
    /**
     * List of possible reporter ions.
     */
    private ArrayList<Integer> reporterIons;
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
     * The fraction settings.
     */
    private FractionSettings fractionSettings;
    /**
     * A parent handling the edition of QC filters.
     */
    private ValidationQCPreferencesDialogParent validationQCPreferencesDialogParent;

    /**
     * Constructor.
     *
     * @param parentFrame the parent frame
     * @param identificationParameters the identification parameters to display
     * @param configurationFile the configuration file containing the PTM usage
     * preferences
     * @param possibleNeutralLosses the possible neutral losses
     * @param reporterIons the possible reporter ions
     * @param normalIcon the normal icon
     * @param waitingIcon the waiting icon
     * @param lastSelectedFolder the last selected folder
     * @param validationQCPreferencesDialogParent a parent handling the edition
     * of QC filters
     * @param editable boolean indicating whether the parameters can be edited
     */
    public IdentificationParametersEditionDialog(java.awt.Frame parentFrame, IdentificationParameters identificationParameters, ConfigurationFile configurationFile, ArrayList<NeutralLoss> possibleNeutralLosses, ArrayList<Integer> reporterIons, Image normalIcon, Image waitingIcon, LastSelectedFolder lastSelectedFolder, ValidationQCPreferencesDialogParent validationQCPreferencesDialogParent, boolean editable) {
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
        this.fractionSettings = identificationParameters.getFractionSettings();
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.configurationFile = configurationFile;
        this.lastSelectedFolder = lastSelectedFolder;
        this.validationQCPreferencesDialogParent = validationQCPreferencesDialogParent;
        this.reporterIons = reporterIons;
        this.reporterIons = reporterIons;
        this.possibleNeutralLosses = possibleNeutralLosses;

        initComponents();
        setUpGui();
        populateGUI(identificationParameters);
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {
        nameTxt.setEditable(editable);
        descriptionTxt.setEditable(editable);
    }

    /**
     * Populates the GUI using the given identification parameters.
     *
     * @param identificationParameters the identification parameters to display
     */
    public void populateGUI(IdentificationParameters identificationParameters) {
        nameTxt.setText(identificationParameters.getName());
        descriptionTxt.setText(identificationParameters.getDescription());
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
     * Returns the identification parameters as set by the user.
     *
     * @return the identification parameters as set by the user
     */
    public IdentificationParameters getIdentificationParameters() {
        IdentificationParameters identificationParameters = new IdentificationParameters();
        identificationParameters.setAnnotationSettings(annotationSettings);
        identificationParameters.setSearchParameters(searchParameters);
        identificationParameters.setSequenceMatchingPreferences(sequenceMatchingPreferences);
        identificationParameters.setGenePreferences(genePreferences);
        identificationParameters.setIdFilter(peptideAssumptionFilter);
        identificationParameters.setPsmScoringPreferences(psmScoringPreferences);
        identificationParameters.setPtmScoringPreferences(ptmScoringPreferences);
        identificationParameters.setProteinInferencePreferences(proteinInferencePreferences);
        identificationParameters.setIdValidationPreferences(idValidationPreferences);
        identificationParameters.setFractionSettings(fractionSettings);
        return identificationParameters;
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
        attributesPanel = new javax.swing.JPanel();
        decriptionLbl = new javax.swing.JLabel();
        nameTxt = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionTxt = new javax.swing.JTextArea();
        nameLbl = new javax.swing.JLabel();
        settingsPanel = new javax.swing.JPanel();
        spectrumAnnotationButton = new javax.swing.JButton();
        fractionsButton = new javax.swing.JButton();
        psmScoringButton = new javax.swing.JButton();
        matchesFiltersButton = new javax.swing.JButton();
        qualityControlButton = new javax.swing.JButton();
        sequenceMatchingButton = new javax.swing.JButton();
        spectrumMatchingButton = new javax.swing.JButton();
        proteinInferenceButton = new javax.swing.JButton();
        geneMappingButton = new javax.swing.JButton();
        validationButton = new javax.swing.JButton();
        ptmLocalizationButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        cancelButton.setText("Cancel");

        okButton.setText("OK");

        attributesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters Attributes"));
        attributesPanel.setOpaque(false);

        decriptionLbl.setText("Description");

        descriptionTxt.setColumns(20);
        descriptionTxt.setRows(5);
        jScrollPane1.setViewportView(descriptionTxt);

        nameLbl.setText("Name");

        javax.swing.GroupLayout attributesPanelLayout = new javax.swing.GroupLayout(attributesPanel);
        attributesPanel.setLayout(attributesPanelLayout);
        attributesPanelLayout.setHorizontalGroup(
            attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(attributesPanelLayout.createSequentialGroup()
                        .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(attributesPanelLayout.createSequentialGroup()
                                .addComponent(nameLbl)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(nameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(decriptionLbl))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        attributesPanelLayout.setVerticalGroup(
            attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLbl)
                    .addComponent(nameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(decriptionLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                .addContainerGap())
        );

        settingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Identification Settings"));
        settingsPanel.setOpaque(false);

        spectrumAnnotationButton.setText("Spectrum Annotation");
        spectrumAnnotationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spectrumAnnotationButtonActionPerformed(evt);
            }
        });

        fractionsButton.setText("Fractions");
        fractionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fractionsButtonActionPerformed(evt);
            }
        });

        psmScoringButton.setText("PSM Scoring");
        psmScoringButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                psmScoringButtonActionPerformed(evt);
            }
        });

        matchesFiltersButton.setText("Matches Filters");
        matchesFiltersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchesFiltersButtonActionPerformed(evt);
            }
        });

        qualityControlButton.setText("Quality Control");
        qualityControlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qualityControlButtonActionPerformed(evt);
            }
        });

        sequenceMatchingButton.setText("Sequence to Protein Matching");
        sequenceMatchingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequenceMatchingButtonActionPerformed(evt);
            }
        });

        spectrumMatchingButton.setText("Spectrum Matching");
        spectrumMatchingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spectrumMatchingButtonActionPerformed(evt);
            }
        });

        proteinInferenceButton.setText("Protein Inference");
        proteinInferenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proteinInferenceButtonActionPerformed(evt);
            }
        });

        geneMappingButton.setText("Protein to Gene Mapping");
        geneMappingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geneMappingButtonActionPerformed(evt);
            }
        });

        validationButton.setText("Validation");
        validationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validationButtonActionPerformed(evt);
            }
        });

        ptmLocalizationButton.setText("PTM Localization");
        ptmLocalizationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ptmLocalizationButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(fractionsButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(validationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(proteinInferenceButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ptmLocalizationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(psmScoringButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(matchesFiltersButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(geneMappingButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sequenceMatchingButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(spectrumAnnotationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spectrumMatchingButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(qualityControlButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(213, Short.MAX_VALUE))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
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
                .addComponent(fractionsButton)
                .addGap(18, 18, 18)
                .addComponent(qualityControlButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(attributesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attributesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void spectrumAnnotationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spectrumAnnotationButtonActionPerformed
        AnnotationSettingsDialog annotationSettingsDialog = new AnnotationSettingsDialog(parentFrame, annotationSettings, possibleNeutralLosses, reporterIons);
        if (!annotationSettingsDialog.isCanceled()) {
            annotationSettings = annotationSettingsDialog.getAnnotationSettings();
        }
    }//GEN-LAST:event_spectrumAnnotationButtonActionPerformed

    private void spectrumMatchingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spectrumMatchingButtonActionPerformed
        SearchSettingsDialog searchSettingsDialog = new SearchSettingsDialog(parentFrame, searchParameters, normalIcon, waitingIcon, editable, editable, configurationFile, lastSelectedFolder, editable);
        if (!searchSettingsDialog.isCanceled()) {
            searchParameters = searchSettingsDialog.getSearchParameters();
        }
    }//GEN-LAST:event_spectrumMatchingButtonActionPerformed

    private void sequenceMatchingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequenceMatchingButtonActionPerformed
        SequenceMatchingSettingsDialog sequenceMatchingSettingsDialog = new SequenceMatchingSettingsDialog(parentFrame, sequenceMatchingPreferences);
        if (!sequenceMatchingSettingsDialog.isCanceled()) {
            sequenceMatchingPreferences = sequenceMatchingSettingsDialog.getSequenceMatchingPreferences();
        }
    }//GEN-LAST:event_sequenceMatchingButtonActionPerformed

    private void geneMappingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geneMappingButtonActionPerformed
        SpeciesDialog speciesDialog = new SpeciesDialog(null, genePreferences, true, waitingIcon, normalIcon);
        // @TODO decouple the gene factory from the preferences
    }//GEN-LAST:event_geneMappingButtonActionPerformed

    private void matchesFiltersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchesFiltersButtonActionPerformed
        MatchesImportFiltersDialog matchesImportFiltersDialog = new MatchesImportFiltersDialog(parentFrame, peptideAssumptionFilter, editable);
        if (!matchesImportFiltersDialog.isCanceled()) {
            peptideAssumptionFilter = matchesImportFiltersDialog.getFilter();
        }
    }//GEN-LAST:event_matchesFiltersButtonActionPerformed

    private void psmScoringButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_psmScoringButtonActionPerformed
        PsmScoringSettingsDialog psmScoringSettingsDialog = new PsmScoringSettingsDialog(parentFrame, psmScoringPreferences);
        if (!psmScoringSettingsDialog.isCanceled()) {
            psmScoringPreferences = psmScoringSettingsDialog.getPsmScoringPreferences();
        }
    }//GEN-LAST:event_psmScoringButtonActionPerformed

    private void ptmLocalizationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ptmLocalizationButtonActionPerformed
        PTMLocalizationParametersDialog ptmLocalizationParametersDialog = new PTMLocalizationParametersDialog(parentFrame, ptmScoringPreferences, editable);
        if (!ptmLocalizationParametersDialog.isCanceled()) {
            ptmScoringPreferences = ptmLocalizationParametersDialog.getPtmScoringPreferences();
        }
    }//GEN-LAST:event_ptmLocalizationButtonActionPerformed

    private void proteinInferenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proteinInferenceButtonActionPerformed
        ProteinInferenceSettingsDialog proteinInferenceSettingsDialog = new ProteinInferenceSettingsDialog(parentFrame, proteinInferencePreferences, normalIcon, waitingIcon, lastSelectedFolder, editable);
        if (!proteinInferenceSettingsDialog.isCanceled()) {
            proteinInferencePreferences = proteinInferenceSettingsDialog.getProteinInferencePreferences();
        }
    }//GEN-LAST:event_proteinInferenceButtonActionPerformed

    private void validationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validationButtonActionPerformed
        ValidationSettingsDialog validationSettingsDialog = new ValidationSettingsDialog(parentFrame, idValidationPreferences, editable);
        if (!validationSettingsDialog.isCanceled()) {
            ValidationQCPreferences validationQCPreferences = idValidationPreferences.getValidationQCPreferences();
            idValidationPreferences = validationSettingsDialog.getIdMatchValidationPreferences();
            idValidationPreferences.setValidationQCPreferences(validationQCPreferences);
        }
    }//GEN-LAST:event_validationButtonActionPerformed

    private void qualityControlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qualityControlButtonActionPerformed
        ValidationQCPreferences validationQCPreferences = idValidationPreferences.getValidationQCPreferences();
        ValidationQCPreferencesDialog validationQCPreferencesDialog = new ValidationQCPreferencesDialog(parentFrame, validationQCPreferencesDialogParent, validationQCPreferences);
        if (!validationQCPreferencesDialog.isCanceled()) {
            idValidationPreferences.setValidationQCPreferences(validationQCPreferencesDialog.getValidationQCPreferences());
        }
    }//GEN-LAST:event_qualityControlButtonActionPerformed

    private void fractionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fractionsButtonActionPerformed
        FractionSettingsDialog fractionSettingsDialog = new FractionSettingsDialog(parentFrame, fractionSettings, editable);
        if (!fractionSettingsDialog.isCanceled()) {
            fractionSettings = fractionSettingsDialog.getFractionSettings();
        }
    }//GEN-LAST:event_fractionsButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel attributesPanel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel decriptionLbl;
    private javax.swing.JTextArea descriptionTxt;
    private javax.swing.JButton fractionsButton;
    private javax.swing.JButton geneMappingButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton matchesFiltersButton;
    private javax.swing.JLabel nameLbl;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JButton okButton;
    private javax.swing.JButton proteinInferenceButton;
    private javax.swing.JButton psmScoringButton;
    private javax.swing.JButton ptmLocalizationButton;
    private javax.swing.JButton qualityControlButton;
    private javax.swing.JButton sequenceMatchingButton;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JButton spectrumAnnotationButton;
    private javax.swing.JButton spectrumMatchingButton;
    private javax.swing.JButton validationButton;
    // End of variables declaration//GEN-END:variables

}
