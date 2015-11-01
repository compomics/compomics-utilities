package com.compomics.util.preferences;

import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.io.SerializationUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

/**
 * Generic class grouping the parameters used for protein identification.
 *
 * @author Marc Vaudel
 */
public class IdentificationParameters implements Serializable, MarshallableParameter {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -5516259326385167746L;
    /**
     * Name of the type of marshalled parameter.
     */
    private String marshallableParameterType = null;
    /**
     * The name of the parameters.
     */
    private String name;
    /**
     * The description of the parameters.
     */
    private String description;
    /**
     * Indicates whether the description is automatically generated.
     */
    private Boolean defaultDescription = true;
    /**
     * The parameters used for the spectrum matching.
     */
    private SearchParameters searchParameters;
    /**
     * The peak annotation preferences.
     */
    private AnnotationSettings annotationSettings;
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
    private PeptideAssumptionFilter peptideAssumptionFilter;
    /**
     * The PTM localization scoring preferences.
     */
    private PTMScoringPreferences ptmScoringPreferences;
    /**
     * The protein inference preferences.
     */
    private ProteinInferencePreferences proteinInferencePreferences;
    /**
     * The identification validation preferences.
     */
    private IdMatchValidationPreferences idValidationPreferences;
    /**
     * The fraction settings.
     */
    private FractionSettings fractionSettings;

    /**
     * Creates empty identification parameters.
     */
    public IdentificationParameters() {
    }

    /**
     * Creates default identification parameters from the given search
     * parameters.
     *
     * @param searchParameters the search parameters
     */
    public IdentificationParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
        setParametersFromSearch(searchParameters);
    }

    /**
     * Constructor.
     *
     * @param name the name of the parameters
     * @param description the description
     * @param searchParameters the search parameters
     * @param annotationSettings the annotation preferences
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param genePreferences the gene preferences
     * @param psmScoringPreferences the PSM scoring preferences
     * @param peptideAssumptionFilter the peptide assumption filters
     * @param ptmScoringPreferences the PTM localization scoring preferences
     * @param proteinInferencePreferences the protein inference preferences
     * @param idValidationPreferences the matches validation preferences
     * @param fractionSettings the fraction settings
     */
    public IdentificationParameters(String name, String description, SearchParameters searchParameters, AnnotationSettings annotationSettings, SequenceMatchingPreferences sequenceMatchingPreferences, GenePreferences genePreferences, PsmScoringPreferences psmScoringPreferences, PeptideAssumptionFilter peptideAssumptionFilter, PTMScoringPreferences ptmScoringPreferences, ProteinInferencePreferences proteinInferencePreferences, IdMatchValidationPreferences idValidationPreferences, FractionSettings fractionSettings) {
        this.name = name;
        this.description = description;
        this.searchParameters = searchParameters;
        this.annotationSettings = annotationSettings;
        this.sequenceMatchingPreferences = sequenceMatchingPreferences;
        this.genePreferences = genePreferences;
        this.psmScoringPreferences = psmScoringPreferences;
        this.peptideAssumptionFilter = peptideAssumptionFilter;
        this.ptmScoringPreferences = ptmScoringPreferences;
        this.proteinInferencePreferences = proteinInferencePreferences;
        this.idValidationPreferences = idValidationPreferences;
        this.fractionSettings = fractionSettings;
    }

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
        if (defaultDescription || description == null || description.length() == 0) {
            setDescription(searchParameters.getShortDescription(), true);
        }
    }

    /**
     * Returns the relative tolerance in ppm corresponding to the absolute
     * tolerance in dalton at the given reference mass.
     *
     * @param daltonTolerance the absolute tolerance in dalton
     * @param refMass the reference mass in dalton
     *
     * @return the relative tolerance in ppm
     */
    public static double getPpmTolerance(double daltonTolerance, double refMass) {
        double result = daltonTolerance / refMass * 1000000;
        return result;
    }

    /**
     * Returns the absolute tolerance in dalton corresponding to the relative
     * tolerance in ppm at the given reference mass.
     *
     * @param ppmTolerance the absolute tolerance in ppm
     * @param refMass the reference mass in dalton
     *
     * @return the relative tolerance in ppm
     */
    public static double getDaTolerance(double ppmTolerance, double refMass) {
        double result = ppmTolerance / 1000000 * refMass;
        return result;
    }

    /**
     * Returns the annotation preferences used for identification.
     *
     * @return the annotation preferences used for identification
     */
    public AnnotationSettings getAnnotationPreferences() {
        if (annotationSettings == null) { // Backward compatibility
            annotationSettings = new AnnotationSettings();
            annotationSettings.setPreferencesFromSearchParameters(searchParameters);
        }
        return annotationSettings;
    }

    /**
     * Sets the annotation preferences used for identification.
     *
     * @param annotationSettings the annotation preferences used for
     * identification
     */
    public void setAnnotationSettings(AnnotationSettings annotationSettings) {
        this.annotationSettings = annotationSettings;
    }

    /**
     * Returns the filter used when importing PSMs.
     *
     * @return the filter used when importing PSMs
     */
    public PeptideAssumptionFilter getPeptideAssumptionFilter() {
        return peptideAssumptionFilter;
    }

    /**
     * Sets the filter used when importing PSMs.
     *
     * @param peptideAssumptionFilter the filter used when importing PSMs
     */
    public void setIdFilter(PeptideAssumptionFilter peptideAssumptionFilter) {
        this.peptideAssumptionFilter = peptideAssumptionFilter;
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
     * Returns the fraction settings.
     *
     * @return the fraction settings
     */
    public FractionSettings getFractionSettings() {
        if (fractionSettings == null) { // Backward compatibility
            return new FractionSettings();
        }
        return fractionSettings;
    }

    /**
     * Sets the fraction settings.
     *
     * @param fractionSettings the fraction settings
     */
    public void setFractionSettings(FractionSettings fractionSettings) {
        this.fractionSettings = fractionSettings;
    }

    /**
     * Loads the identification parameters from a file.
     *
     * @param identificationParametersFile the file
     *
     * @return the parameters
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public static IdentificationParameters getIdentificationParameters(File identificationParametersFile) throws FileNotFoundException, IOException, ClassNotFoundException {

        IdentificationParameters identificationParameters = (IdentificationParameters) SerializationUtils.readObject(identificationParametersFile);
        return identificationParameters;
    }

    /**
     * Saves the identification parameters to a file.
     *
     * @param identificationParameters the identification parameters
     * @param searchParametersFile the file
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public static void saveIdentificationParameters(IdentificationParameters identificationParameters, File searchParametersFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        SerializationUtils.writeObject(identificationParameters, searchParametersFile);
    }

    /**
     * Returns the name of the parameters.
     *
     * @return the name of the parameters
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the parameters.
     *
     * @param name the name of the parameters
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of the parameters.
     *
     * @return the description of the parameters
     */
    public String getDescription() {
        return description;
    }

    /**
     * Indicates whether the description is automatically generated.
     *
     * @return a boolean indicating whether the description is automatically
     * generated
     */
    public Boolean getDefaultDescription() {
        if (defaultDescription == null) {
            return false;
        }
        return defaultDescription;
    }

    /**
     * Sets the description of the parameters.
     *
     * @param description the description of the parameters
     * @param automaticallyGenerated boolean indicating whether the description
     * is automatically generated
     */
    public void setDescription(String description, boolean automaticallyGenerated) {
        this.description = description;
        this.defaultDescription = automaticallyGenerated;
    }

    /**
     * Sets identification parameters based on given search parameters.
     *
     * @param searchParameters the parameters used for the search
     */
    public void setParametersFromSearch(SearchParameters searchParameters) {
        setSearchParameters(searchParameters);
        annotationSettings = new AnnotationSettings();
        annotationSettings.addNeutralLoss(NeutralLoss.H2O);
        annotationSettings.addNeutralLoss(NeutralLoss.NH3);
        if (searchParameters != null) {
            annotationSettings.setPreferencesFromSearchParameters(searchParameters);
        }
        annotationSettings.setIntensityLimit(0.75);
        annotationSettings.setAutomaticAnnotation(true);
        peptideAssumptionFilter = new PeptideAssumptionFilter();
        if (searchParameters != null) {
            peptideAssumptionFilter.setFilterFromSearchParameters(searchParameters);
        }
        if (psmScoringPreferences == null) {
            psmScoringPreferences = new PsmScoringPreferences();
        }
        if (ptmScoringPreferences == null) {
            ptmScoringPreferences = new PTMScoringPreferences();
        }
        if (sequenceMatchingPreferences == null) {
            sequenceMatchingPreferences = SequenceMatchingPreferences.getDefaultSequenceMatching();
        }
        if (genePreferences == null) {
            genePreferences = new GenePreferences();
        }
        if (proteinInferencePreferences == null) {
            proteinInferencePreferences = new ProteinInferencePreferences();
            if (searchParameters.getFastaFile() != null) {
                proteinInferencePreferences.setProteinSequenceDatabase(searchParameters.getFastaFile());
            }
        }
        if (idValidationPreferences == null) {
            idValidationPreferences = new IdMatchValidationPreferences();
        }
        if (fractionSettings == null) {
            fractionSettings = new FractionSettings();
        }
        setDescription(searchParameters.getShortDescription(), true);
    }

    @Override
    public void setType() {
        marshallableParameterType = Type.identification_parameters.name();
    }

    @Override
    public Type getType() {
        if (marshallableParameterType == null) {
            return null;
        }
        return Type.valueOf(marshallableParameterType);
    }
}
