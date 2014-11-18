package com.compomics.util.preferences;

import com.compomics.util.experiment.identification.SearchParameters;
import java.io.Serializable;

/**
 * Generic class grouping the parameters used for protein identification
 *
 * @author Marc
 */
public class IdentificationParameters implements Serializable {

    /**
     * Serial number for backward compatibility
     */
    static final long serialVersionUID = -5516259326385167746L;
    /**
     * The parameters used for the spectrum matching
     */
    private SearchParameters searchParameters;
    /**
     * The peak annotation preferences
     */
    private AnnotationPreferences annotationPreferences;
    /**
     * The Psm filter
     */
    private IdFilter idFilter;
    /**
     * The Psm scores to use
     */
    private PsmScoringPreferences psmScoringPreferences;
    /**
     * The ptm localisation scoring preferences
     */
    private PTMScoringPreferences ptmScoringPreferences;
    /**
     * The peptide to protein matching preferences
     */
    private SequenceMatchingPreferences sequenceMatchingPreferences;
    /**
     * The gene preferences
     */
    private GenePreferences genePreferences;
    /**
     * The protein inference preferences
     */
    private ProteinInferencePreferences proteinInferencePreferences;
    /**
     * The identification validation preferences
     */
    private IdMatchValidationPreferences idValidationPreferences;

    /**
     * Returns the parameters used for the spectrum matching.
     *
     * @return the parameters used for the spectrum matching
     */
    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    /**
     * Sets the parameters used for the spectrum matching.
     *
     * @param searchParameters the parameters used for the spectrum matching
     */
    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    /**
     * Returns the annotation preferences used for identification.
     *
     * @return the annotation preferences used for identification
     */
    public AnnotationPreferences getAnnotationPreferences() {
        return annotationPreferences;
    }

    /**
     * Sets the annotation preferences used for identification.
     *
     * @param annotationPreferences the annotation preferences used for
     * identification
     */
    public void setAnnotationPreferences(AnnotationPreferences annotationPreferences) {
        this.annotationPreferences = annotationPreferences;
    }

    /**
     * Returns the filter used when importing PSMs.
     *
     * @return the filter used when importing PSMs
     */
    public IdFilter getIdFilter() {
        return idFilter;
    }

    /**
     * Sets the filter used when importing PSMs.
     *
     * @param idFilter the filter used when importing PSMs
     */
    public void setIdFilter(IdFilter idFilter) {
        this.idFilter = idFilter;
    }

    /**
     * Returns the scoring preferences used when scoring PSMs.
     *
     * @return the scoring preferences used when scoring PSMs
     */
    public PsmScoringPreferences getPsmScoringPreferences() {
        return psmScoringPreferences;
    }

    /**
     * Sets the scoring preferences used when scoring PSMs.
     *
     * @param psmScoringPreferences the scoring preferences used when scoring
     * PSMs
     */
    public void setPsmScoringPreferences(PsmScoringPreferences psmScoringPreferences) {
        this.psmScoringPreferences = psmScoringPreferences;
    }

    /**
     * Returns the PTM localization scoring preferences.
     *
     * @return the PTM localization scoring preferences
     */
    public PTMScoringPreferences getPtmScoringPreferences() {
        return ptmScoringPreferences;
    }

    /**
     * Sets the PTM localization scoring preferences.
     *
     * @param ptmScoringPreferences the PTM localization scoring preferences
     */
    public void setPtmScoringPreferences(PTMScoringPreferences ptmScoringPreferences) {
        this.ptmScoringPreferences = ptmScoringPreferences;
    }

    /**
     * Returns the sequence matching preferences.
     *
     * @return the sequence matching preferences
     */
    public SequenceMatchingPreferences getSequenceMatchingPreferences() {
        return sequenceMatchingPreferences;
    }

    /**
     * Sets the sequence matching preferences.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
     */
    public void setSequenceMatchingPreferences(SequenceMatchingPreferences sequenceMatchingPreferences) {
        this.sequenceMatchingPreferences = sequenceMatchingPreferences;
    }

    /**
     * Returns the identification matches validation preferences.
     *
     * @return the identification matches validation preferences
     */
    public IdMatchValidationPreferences getIdValidationPreferences() {
        return idValidationPreferences;
    }

    /**
     * Sets the identification matches validation preferences.
     *
     * @param idValidationPreferences the identification matches validation
     * preferences
     */
    public void setIdValidationPreferences(IdMatchValidationPreferences idValidationPreferences) {
        this.idValidationPreferences = idValidationPreferences;
    }

    /**
     * Returns the protein inference preferences.
     *
     * @return the protein inference preferences
     */
    public ProteinInferencePreferences getProteinInferencePreferences() {
        return proteinInferencePreferences;
    }

    /**
     * Sets the protein inference preferences.
     *
     * @param proteinInferencePreferences the protein inference preferences
     */
    public void setProteinInferencePreferences(ProteinInferencePreferences proteinInferencePreferences) {
        this.proteinInferencePreferences = proteinInferencePreferences;
    }

    /**
     * Returns the gene preferences.
     *
     * @return the gene preferences
     */
    public GenePreferences getGenePreferences() {
        return genePreferences;
    }

    /**
     * Sets the gene preferences.
     *
     * @param genePreferences the gene preferences
     */
    public void setGenePreferences(GenePreferences genePreferences) {
        this.genePreferences = genePreferences;
    }

    /**
     * Creates blank parameters.
     *
     * @return default identification parameters
     */
    public static IdentificationParameters getDefaultIdentificationParameters() {
        return getDefaultIdentificationParameters(null);
    }

    /**
     * Returns default identification parameters based on given search
     * parameters.
     *
     * @param searchParameters the parameters used for the search
     *
     * @return default identification parameters
     */
    public static IdentificationParameters getDefaultIdentificationParameters(SearchParameters searchParameters) {
        IdentificationParameters identificationParameters = new IdentificationParameters();
        identificationParameters.setSearchParameters(searchParameters);
        AnnotationPreferences annotationPreferences = new AnnotationPreferences();
        if (searchParameters != null) {
            annotationPreferences.setPreferencesFromSearchParameters(searchParameters);
        }
        annotationPreferences.setAnnotationLevel(0.75);
        annotationPreferences.useAutomaticAnnotation(true);
        identificationParameters.setAnnotationPreferences(annotationPreferences);
        IdFilter idFilter = new IdFilter();
        if (searchParameters != null) {
            idFilter.setFilterFromSearchParameters(searchParameters);
        }
        identificationParameters.setIdFilter(idFilter);
        PsmScoringPreferences psmScoringPreferences = new PsmScoringPreferences();
        identificationParameters.setPsmScoringPreferences(psmScoringPreferences);
        PTMScoringPreferences ptmScoringPreferences = new PTMScoringPreferences();
        identificationParameters.setPtmScoringPreferences(ptmScoringPreferences);
        if (searchParameters != null) {
            SequenceMatchingPreferences sequenceMatchingPreferences = SequenceMatchingPreferences.getDefaultSequenceMatching(searchParameters);
            identificationParameters.setSequenceMatchingPreferences(sequenceMatchingPreferences);
        }
        GenePreferences genePreferences = new GenePreferences();
        identificationParameters.setGenePreferences(genePreferences);
        ProteinInferencePreferences proteinInferencePreferences = new ProteinInferencePreferences();
        if (searchParameters != null) {
            proteinInferencePreferences.setProteinSequenceDatabase(searchParameters.getFastaFile());
        }
        identificationParameters.setProteinInferencePreferences(proteinInferencePreferences);
        IdMatchValidationPreferences idValidationPreferences = new IdMatchValidationPreferences();
        identificationParameters.setIdValidationPreferences(idValidationPreferences);
        return identificationParameters;
    }

    /**
     * Sets identification parameters based on given search parameters.
     *
     * @param searchParameters the parameters used for the search
     */
    public void setParametersFromSearch(SearchParameters searchParameters) {
        setSearchParameters(searchParameters);
        annotationPreferences = new AnnotationPreferences();
        if (searchParameters != null) {
            annotationPreferences.setPreferencesFromSearchParameters(searchParameters);
        }
        annotationPreferences.setAnnotationLevel(0.75);
        annotationPreferences.useAutomaticAnnotation(true);
        idFilter = new IdFilter();
        if (searchParameters != null) {
            idFilter.setFilterFromSearchParameters(searchParameters);
        }
        if (psmScoringPreferences == null) {
            psmScoringPreferences = new PsmScoringPreferences();
        }
        if (ptmScoringPreferences == null) {
            ptmScoringPreferences = new PTMScoringPreferences();
        }
        if (searchParameters != null) {
            sequenceMatchingPreferences = SequenceMatchingPreferences.getDefaultSequenceMatching(searchParameters);
        }
        if (genePreferences == null) {
            genePreferences = new GenePreferences();
        }
        if (proteinInferencePreferences == null) {
            proteinInferencePreferences = new ProteinInferencePreferences();
        }
        if (searchParameters != null) {
            proteinInferencePreferences.setProteinSequenceDatabase(searchParameters.getFastaFile());
        }
        if (idValidationPreferences == null) {
            idValidationPreferences = new IdMatchValidationPreferences();
        }
    }

}
