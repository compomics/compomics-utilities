package com.compomics.util.parameters.identification;

import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.advanced.PeptideVariantsParameters;
import com.compomics.util.parameters.identification.advanced.ModificationLocalizationParameters;
import com.compomics.util.parameters.identification.advanced.FractionParameters;
import com.compomics.util.parameters.identification.advanced.PsmScoringParameters;
import com.compomics.util.parameters.identification.advanced.ProteinInferenceParameters;
import com.compomics.util.parameters.identification.advanced.GeneParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.advanced.IdMatchValidationParameters;
import com.compomics.util.experiment.io.parameters.MarshallableParameter;
import com.compomics.util.experiment.io.parameters.DummyParameters;
import com.compomics.util.Util;
import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Generic class grouping the parameters used for protein identification.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
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
     * The peak annotation parameters.
     */
    private AnnotationParameters annotationParameters;
    /**
     * The peptide to protein matching parameters.
     */
    private SequenceMatchingParameters sequenceMatchingParameters;
    /**
     * The peptide variants parameters.
     */
    private PeptideVariantsParameters peptideVariantsParameters;
    /**
     * The gene parameters.
     */
    private GeneParameters geneParameters;
    /**
     * The PSM scores to use.
     */
    private PsmScoringParameters psmScoringParameters;
    /**
     * The PSM filter.
     */
    private PeptideAssumptionFilter peptideAssumptionFilter;
    /**
     * The modification localization parameters.
     */
    private ModificationLocalizationParameters modificationLocalizationParameters;
    /**
     * The protein inference parameters.
     */
    private ProteinInferenceParameters proteinInferenceParameters;
    /**
     * The identification validation parameters.
     */
    private IdMatchValidationParameters idValidationParameters;
    /**
     * The fraction settings.
     */
    private FractionParameters fractionParameters;

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
     * @param annotationParameters the annotation parameters
     * @param sequenceMatchingParameters the sequence matching parameters
     * @param peptideVariantsParameters the peptide variant parameters
     * @param geneParameters the gene parameters
     * @param psmScoringParameters the PSM scoring parameters
     * @param peptideAssumptionFilter the peptide assumption filters
     * @param ModificationLocalizationParameters the PTM localization scoring
     * parameters
     * @param proteinInferenceParameters the protein inference parameters
     * @param idValidationParameters the matches validation parameters
     * @param fractionParameters the fraction parameters
     */
    public IdentificationParameters(String name, String description,
            SearchParameters searchParameters, AnnotationParameters annotationParameters,
            SequenceMatchingParameters sequenceMatchingParameters, PeptideVariantsParameters peptideVariantsParameters,
            GeneParameters geneParameters, PsmScoringParameters psmScoringParameters,
            PeptideAssumptionFilter peptideAssumptionFilter, ModificationLocalizationParameters ModificationLocalizationParameters,
            ProteinInferenceParameters proteinInferenceParameters, IdMatchValidationParameters idValidationParameters,
            FractionParameters fractionParameters) {

        this.name = name;
        this.description = description;
        this.searchParameters = searchParameters;
        this.annotationParameters = annotationParameters;
        this.sequenceMatchingParameters = sequenceMatchingParameters;
        this.peptideVariantsParameters = peptideVariantsParameters;
        this.geneParameters = geneParameters;
        this.psmScoringParameters = psmScoringParameters;
        this.peptideAssumptionFilter = peptideAssumptionFilter;
        this.modificationLocalizationParameters = ModificationLocalizationParameters;
        this.proteinInferenceParameters = proteinInferenceParameters;
        this.idValidationParameters = idValidationParameters;
        this.fractionParameters = fractionParameters;
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
     * tolerance in Dalton at the given reference mass.
     *
     * @param daltonTolerance the absolute tolerance in Dalton
     * @param refMass the reference mass in Dalton
     *
     * @return the relative tolerance in ppm
     */
    public static double getPpmTolerance(double daltonTolerance, double refMass) {

        return daltonTolerance / refMass * 1000000.0;

    }

    /**
     * Returns the absolute tolerance in Dalton corresponding to the relative
     * tolerance in ppm at the given reference mass.
     *
     * @param ppmTolerance the absolute tolerance in ppm
     * @param refMass the reference mass in Dalton
     *
     * @return the relative tolerance in ppm
     */
    public static double getDaTolerance(double ppmTolerance, double refMass) {

        return ppmTolerance / 1000000.0 * refMass;

    }

    /**
     * Returns the annotation parameters used for identification.
     *
     * @return the annotation parameters used for identification
     */
    public AnnotationParameters getAnnotationParameters() {

        return annotationParameters;

    }

    /**
     * Sets the annotation parameters used for identification.
     *
     * @param annotationParameters the annotation parameters used for
     * identification
     */
    public void setAnnotationParameters(AnnotationParameters annotationParameters) {

        this.annotationParameters = annotationParameters;

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
    public void setPeptideAssumptionFilter(PeptideAssumptionFilter peptideAssumptionFilter) {

        this.peptideAssumptionFilter = peptideAssumptionFilter;

    }

    /**
     * Returns the scoring parameters used when scoring PSMs.
     *
     * @return the scoring parameters used when scoring PSMs
     */
    public PsmScoringParameters getPsmScoringParameters() {

        return psmScoringParameters;

    }

    /**
     * Sets the scoring preferences used when scoring PSMs.
     *
     * @param psmScoringParameters the scoring preferences used when scoring
     * PSMs
     */
    public void setPsmScoringParameters(PsmScoringParameters psmScoringParameters) {

        this.psmScoringParameters = psmScoringParameters;

    }

    /**
     * Returns the modification localization scoring parameters.
     *
     * @return the modification localization scoring parameters
     */
    public ModificationLocalizationParameters getModificationLocalizationParameters() {

        return modificationLocalizationParameters;

    }

    /**
     * Sets the modification localization parameters.
     *
     * @param modificationLocalizationParameters the modification localization
     * parameters
     */
    public void setModificationLocalizationParameters(ModificationLocalizationParameters modificationLocalizationParameters) {

        this.modificationLocalizationParameters = modificationLocalizationParameters;

    }

    /**
     * Returns the sequence matching parameters.
     *
     * @return the sequence matching parameters
     */
    public SequenceMatchingParameters getSequenceMatchingParameters() {

        return sequenceMatchingParameters;

    }

    /**
     * Sets the sequence matching preferences.
     *
     * @param sequenceMatchingParameters the sequence matching preferences
     */
    public void setSequenceMatchingParameters(SequenceMatchingParameters sequenceMatchingParameters) {

        this.sequenceMatchingParameters = sequenceMatchingParameters;

    }

    /**
     * Returns the peptide variant parameters.
     *
     * @return the peptide variant parameters
     */
    public PeptideVariantsParameters getPeptideVariantsParameters() {

        return peptideVariantsParameters;

    }

    /**
     * Sets the peptide variant parameters.
     *
     * @param peptideVariantsParameters the peptide variant parameters
     */
    public void setPeptideVariantsParameters(PeptideVariantsParameters peptideVariantsParameters) {

        this.peptideVariantsParameters = peptideVariantsParameters;

    }

    /**
     * Returns the identification matches validation parameters.
     *
     * @return the identification matches validation parameters
     */
    public IdMatchValidationParameters getIdValidationParameters() {

        return idValidationParameters;

    }

    /**
     * Sets the identification matches validation parameters.
     *
     * @param idValidationParameters the identification matches validation
     * parameters
     */
    public void setIdValidationParameters(IdMatchValidationParameters idValidationParameters) {

        this.idValidationParameters = idValidationParameters;

    }

    /**
     * Returns the protein inference parameters.
     *
     * @return the protein inference parameters
     */
    public ProteinInferenceParameters getProteinInferenceParameters() {

        return proteinInferenceParameters;

    }

    /**
     * Sets the protein inference parameters.
     *
     * @param proteinInferenceParameters the protein inference parameters
     */
    public void setProteinInferenceParameters(ProteinInferenceParameters proteinInferenceParameters) {

        this.proteinInferenceParameters = proteinInferenceParameters;

    }

    /**
     * Returns the gene parameters.
     *
     * @return the gene parameters
     */
    public GeneParameters getGeneParameters() {

        return geneParameters;

    }

    /**
     * Sets the gene parameters.
     *
     * @param geneParameters the gene parameters
     */
    public void setGeneParameters(GeneParameters geneParameters) {

        this.geneParameters = geneParameters;

    }

    /**
     * Returns the fraction parameters.
     *
     * @return the fraction parameters
     */
    public FractionParameters getFractionParameters() {

        return fractionParameters;

    }

    /**
     * Sets the fraction parameters.
     *
     * @param fractionParameters the fraction parameters
     */
    public void setFractionParameters(FractionParameters fractionParameters) {

        this.fractionParameters = fractionParameters;

    }

    /**
     * Loads the identification parameters from a file. If the given file is a
     * search parameters file, default identification parameters are inferred.
     *
     * @param identificationParametersFile the file
     *
     * @return the parameters
     *
     * @throws IOException if an error occurs while reading the file
     * @throws ClassNotFoundException if the file could not be casted
     */
    public static IdentificationParameters getIdentificationParameters(File identificationParametersFile) throws IOException, ClassNotFoundException {

        Object savedObject;

            IdentificationParametersMarshaller jsonMarshaller = new IdentificationParametersMarshaller();
            Class expectedObjectType = DummyParameters.class;
            Object object = jsonMarshaller.fromJson(expectedObjectType, identificationParametersFile);
            DummyParameters dummyParameters = (DummyParameters) object;

            if (dummyParameters.getType() == MarshallableParameter.Type.search_parameters) {
                
                expectedObjectType = SearchParameters.class;
                savedObject = jsonMarshaller.fromJson(expectedObjectType, identificationParametersFile);
            
            } else if (dummyParameters.getType() == MarshallableParameter.Type.identification_parameters) {

                expectedObjectType = IdentificationParameters.class;
                savedObject = jsonMarshaller.fromJson(expectedObjectType, identificationParametersFile);

            } else {

                throw new IllegalArgumentException("Parameters file " + identificationParametersFile + " not recognized.");

            }

        IdentificationParameters identificationParameters;

        if (savedObject instanceof SearchParameters) {

            SearchParameters searchParameters = (SearchParameters) savedObject;
            identificationParameters = new IdentificationParameters(searchParameters);
            identificationParameters.setName(Util.removeExtension(identificationParametersFile.getName()));

        } else if (savedObject instanceof IdentificationParameters) {

            identificationParameters = (IdentificationParameters) savedObject;

        } else {

            throw new UnsupportedOperationException("Parameters of type " + savedObject.getClass() + " not supported.");

        }

        identificationParameters.getSearchParameters().getDigestionParameters();

        return identificationParameters;

    }

    /**
     * Saves the identification parameters to a file.
     *
     * @param identificationParameters the identification parameters
     * @param identificationParametersFile the file
     *
     * @throws IOException if an error occurred while writing the file
     */
    public static void saveIdentificationParameters(IdentificationParameters identificationParameters, File identificationParametersFile) throws IOException {

        // Save to json file
        IdentificationParametersMarshaller jsonMarshaller = new IdentificationParametersMarshaller();
        identificationParameters.setType();
        jsonMarshaller.saveObjectToJson(identificationParameters, identificationParametersFile);
        
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
    public boolean getDefaultDescription() {
        
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
        annotationParameters = new AnnotationParameters();
        annotationParameters.addNeutralLoss(NeutralLoss.H2O);
        annotationParameters.addNeutralLoss(NeutralLoss.NH3);
        
        if (searchParameters != null) {
            
            annotationParameters.setParametersFromSearchParameters(searchParameters);
            
        }
        
        annotationParameters.setIntensityLimit(0.75);
        annotationParameters.setAutomaticAnnotation(true);
        peptideAssumptionFilter = new PeptideAssumptionFilter();
        
        if (searchParameters != null) {
            
            peptideAssumptionFilter.setFilterFromSearchParameters(searchParameters);
            
        }
        
        if (psmScoringParameters == null) {
            
            psmScoringParameters = new PsmScoringParameters();
            
        }
        
        if (modificationLocalizationParameters == null) {
            
            modificationLocalizationParameters = new ModificationLocalizationParameters();
            
        }
        
        if (sequenceMatchingParameters == null) {
            
            sequenceMatchingParameters = SequenceMatchingParameters.getDefaultSequenceMatching();
            
        }
        
        if (peptideVariantsParameters == null) {
            
            peptideVariantsParameters = new PeptideVariantsParameters();
            
        }
        
        if (geneParameters == null) {
            
            geneParameters = new GeneParameters();
            geneParameters.setPreferencesFromSearchParameters(searchParameters);
            
        }
        
        if (proteinInferenceParameters == null) {
            
            proteinInferenceParameters = new ProteinInferenceParameters();
            
        }
        
        if (idValidationParameters == null) {
            
            idValidationParameters = new IdMatchValidationParameters();
            
        }
        
        if (fractionParameters == null) {
            
            fractionParameters = new FractionParameters();
            
        }
        
        if (searchParameters != null) {
            
            setDescription(searchParameters.getShortDescription(), true);
            
        }
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

    /**
     * Returns true if the identification parameter objects have identical
     * settings.
     *
     * @param otherIdentificationParameters the parameters to compare to
     *
     * @return true if the identification parameter objects have identical
     * settings
     */
    public boolean equals(IdentificationParameters otherIdentificationParameters) {

        if (otherIdentificationParameters == null) {
            
            return false;
            
        }

        if (!searchParameters.equals(otherIdentificationParameters.getSearchParameters())) {
            
            return false;
            
        }
        if (!annotationParameters.isSameAs(otherIdentificationParameters.getAnnotationParameters())) {
            
            return false;
            
        }
        
        if (!sequenceMatchingParameters.isSameAs(otherIdentificationParameters.getSequenceMatchingParameters())) {
            
            return false;
            
        }
        
        if (!getPeptideVariantsParameters().isSameAs(otherIdentificationParameters.getPeptideVariantsParameters())) {
            
            return false;
            
        }
        
        if (!geneParameters.equals(otherIdentificationParameters.getGeneParameters())) {
            
            return false;
            
        }
        
        if (!psmScoringParameters.equals(otherIdentificationParameters.getPsmScoringParameters())) {
            
            return false;
            
        }
        
        if (!peptideAssumptionFilter.isSameAs(otherIdentificationParameters.getPeptideAssumptionFilter())) {
            
            return false;
            
        }
        
        if (!modificationLocalizationParameters.equals(otherIdentificationParameters.getModificationLocalizationParameters())) {
            
            return false;
            
        }
        
        if (!proteinInferenceParameters.equals(otherIdentificationParameters.getProteinInferenceParameters())) {
            
            return false;
            
        }
        
        if (!fractionParameters.isSameAs(otherIdentificationParameters.getFractionParameters())) {
            
            return false;
            
        }

        return true;
        
    }
}
