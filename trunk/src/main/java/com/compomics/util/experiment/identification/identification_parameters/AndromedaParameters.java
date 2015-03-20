package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.massspectrometry.FragmentationMethod;

/**
 * The Andromeda specific parameters.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class AndromedaParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = 9056661756332085205L;
    /**
     * The maximal peptide mass.
     */
    private Double maxPeptideMass = 4600.0;
    /**
     * The maximum number of combinations.
     */
    private Integer maxCombinations = 250;
    /**
     * The top peaks number.
     */
    private Integer topPeaks = 8;
    /**
     * The top peaks window size.
     */
    private Integer topPeaksWindow = 100;
    /**
     * Boolean indicating whether water losses should be accounted for.
     */
    private Boolean includeWater = true;
    /**
     * Boolean indicating whether ammonia losses should be accounted for.
     */
    private Boolean includeAmmonia = true;
    /**
     * Boolean indicating whether neutral losses should be sequence dependent.
     */
    private Boolean dependentLosses = true;
    /**
     * Boolean indicating whether the fragment all option should be used.
     */
    private Boolean fragmentAll = false;
    /**
     * Boolean indicating whether the empirical correction should be used.
     */
    private Boolean empiricalCorrection = true;
    /**
     * Boolean indicating whether the empirical correction should be used.
     */
    private Boolean higherCharge = true;
    /**
     * The fragmentation method used.
     */
    private FragmentationMethod fragmentationMethod = FragmentationMethod.CID;
    /**
     * The maximal number of modifications.
     */
    private Integer maxNumberOfModifications = 5;
    /**
     * The minimal peptide length when no enzyme is used.
     */
    private Integer minPeptideLengthNoEnzyme = 8;
    /**
     * The maximal peptide length when no enzyme is used.
     */
    private Integer maxPeptideLengthNoEnzyme = 25;
    /**
     * Boolean indicating whether I and L should be considered
     * indistinguishable.
     */
    private Boolean equalIL = false;
    /**
     * The number of candidates to report.
     */
    private Integer numberOfCandidates = 15;

    /**
     * Constructor.
     */
    public AndromedaParameters() {
    }

    /**
     * Returns the maximal peptide mass.
     *
     * @return the maximal peptide mass
     */
    public Double getMaxPeptideMass() {
        if (maxPeptideMass == null) { // backward compatibility
            maxPeptideMass = 4600.0;
        }
        return maxPeptideMass;
    }

    /**
     * Sets the maximal peptide mass.
     *
     * @param maxPeptideMass the maximal peptide mass
     */
    public void setMaxPeptideMass(Double maxPeptideMass) {
        this.maxPeptideMass = maxPeptideMass;
    }

    /**
     * Returns the maximal number of combinations.
     *
     * @return the maximal number of combinations
     */
    public int getMaxCombinations() {
        if (maxCombinations == null) { // backward compatibility
            maxCombinations = 250;
        }
        return maxCombinations;
    }

    /**
     * Sets the maximal number of combinations.
     *
     * @param maxCombinations the maximal number of combinations
     */
    public void setMaxCombinations(int maxCombinations) {
        this.maxCombinations = maxCombinations;
    }

    /**
     * Returns the top peaks number.
     *
     * @return the top peaks number
     */
    public int getTopPeaks() {
        if (topPeaks == null) { // backward compatibility
            topPeaks = 8;
        }
        return topPeaks;
    }

    /**
     * Sets the top peaks number.
     *
     * @param topPeaks the top peaks number
     */
    public void setTopPeaks(int topPeaks) {
        this.topPeaks = topPeaks;
    }

    /**
     * Returns the top peaks window size.
     *
     * @return the top peaks window size
     */
    public int getTopPeaksWindow() {
        if (topPeaksWindow == null) { // backward compatibility
            topPeaksWindow = 100;
        }
        return topPeaksWindow;
    }

    /**
     * Sets the top peaks window size.
     *
     * @param topPeaksWindow the top peaks window size
     */
    public void setTopPeaksWindow(int topPeaksWindow) {
        this.topPeaksWindow = topPeaksWindow;
    }

    /**
     * Returns a boolean indicating whether water losses should be accounted
     * for.
     *
     * @return a boolean indicating whether water losses should be accounted for
     */
    public boolean isIncludeWater() {
        if (includeWater == null) { // backward compatibility
            includeWater = true;
        }
        return includeWater;
    }

    /**
     * Sets whether water losses should be accounted for.
     *
     * @param includeWater a boolean indicating whether water losses should be
     * accounted for
     */
    public void setIncludeWater(boolean includeWater) {
        this.includeWater = includeWater;
    }

    /**
     * Returns a boolean indicating whether ammonia losses should be accounted
     * for.
     *
     * @return a boolean indicating whether ammonia losses should be accounted
     * for
     */
    public boolean isIncludeAmmonia() {
        if (includeAmmonia == null) { // backward compatibility
            includeAmmonia = true;
        }
        return includeAmmonia;
    }

    /**
     * Sets whether ammonia losses should be accounted for.
     *
     * @param includeAmmonia a boolean indicating whether ammonia losses should
     * be accounted for
     */
    public void setIncludeAmmonia(boolean includeAmmonia) {
        this.includeAmmonia = includeAmmonia;
    }

    /**
     * Returns whether neutral losses should be sequence dependent.
     *
     * @return a boolean indicating whether neutral losses should be sequence
     * dependent
     */
    public boolean isDependentLosses() {
        if (dependentLosses == null) { // backward compatibility
            dependentLosses = true;
        }
        return dependentLosses;
    }

    /**
     * Sets whether neutral losses should be sequence dependent.
     *
     * @param dependentLosses a boolean indicating whether neutral losses should
     * be sequence dependent
     */
    public void setDependentLosses(boolean dependentLosses) {
        this.dependentLosses = dependentLosses;
    }

    /**
     * Indicates whether the fragment all option should be used.
     *
     * @return a boolean indicating whether the fragment all option should be
     * used
     */
    public boolean isFragmentAll() {
        if (fragmentAll == null) { // backward compatibility
            fragmentAll = false;
        }
        return fragmentAll;
    }

    /**
     * Sets whether the fragment all option should be used.
     *
     * @param fragmentAll a boolean indicating whether the fragment all option
     * should be used
     */
    public void setFragmentAll(boolean fragmentAll) {
        this.fragmentAll = fragmentAll;
    }

    /**
     * Indicates whether empirical correction should be used.
     *
     * @return a boolean indicating whether empirical correction should be used
     */
    public boolean isEmpiricalCorrection() {
        if (empiricalCorrection == null) { // backward compatibility
            empiricalCorrection = true;
        }
        return empiricalCorrection;
    }

    /**
     * Sets whether empirical correction should be used.
     *
     * @param empiricalCorrection a boolean indicating whether empirical
     * correction should be used
     */
    public void setEmpiricalCorrection(boolean empiricalCorrection) {
        this.empiricalCorrection = empiricalCorrection;
    }

    /**
     * Indicates whether higher charge should be considered.
     *
     * @return a boolean indicating whether higher charge should be considered
     */
    public boolean isHigherCharge() {
        if (higherCharge == null) { // backward compatibility
            higherCharge = true;
        }
        return higherCharge;
    }

    /**
     * Sets whether higher charge should be considered.
     *
     * @param higherCharge a boolean indicating whether higher charge should be
     * considered
     */
    public void setHigherCharge(boolean higherCharge) {
        this.higherCharge = higherCharge;
    }

    /**
     * Returns the fragmentation method used.
     *
     * @return the fragmentation method used
     */
    public FragmentationMethod getFragmentationMethod() {
        return fragmentationMethod;
    }

    /**
     * Sets the fragmentation method used.
     *
     * @param fragmentationMethod the fragmentation method used
     */
    public void setFragmentationMethod(FragmentationMethod fragmentationMethod) {
        this.fragmentationMethod = fragmentationMethod;
    }

    /**
     * Returns the maximal number of modifications.
     *
     * @return the maximal number of modifications
     */
    public int getMaxNumberOfModifications() {
        if (maxNumberOfModifications == null) { // backward compatibility
            maxNumberOfModifications = 5;
        }
        return maxNumberOfModifications;
    }

    /**
     * Sets the maximal number of modifications.
     *
     * @param maxNumberOfModifications the maximal number of modifications
     */
    public void setMaxNumberOfModifications(int maxNumberOfModifications) {
        this.maxNumberOfModifications = maxNumberOfModifications;
    }

    /**
     * Returns the minimal peptide length to use when searching with no enzyme.
     *
     * @return the minimal peptide length to use when searching with no enzyme
     */
    public int getMinPeptideLengthNoEnzyme() {
        if (minPeptideLengthNoEnzyme == null) { // backward compatibility
            minPeptideLengthNoEnzyme = 8;
        }
        return minPeptideLengthNoEnzyme;
    }

    /**
     * Sets the minimal peptide length to use when searching with no enzyme.
     *
     * @param minPeptideLengthNoEnzyme the minimal peptide length to use when
     * searching with no enzyme
     */
    public void setMinPeptideLengthNoEnzyme(int minPeptideLengthNoEnzyme) {
        this.minPeptideLengthNoEnzyme = minPeptideLengthNoEnzyme;
    }

    /**
     * Returns the maximal peptide length to use when searching with no enzyme.
     *
     * @return the maximal peptide length to use when searching with no enzyme
     */
    public int getMaxPeptideLengthNoEnzyme() {
        if (maxPeptideLengthNoEnzyme == null) { // backward compatibility
            maxPeptideLengthNoEnzyme = 25;
        }
        return maxPeptideLengthNoEnzyme;
    }

    /**
     * Sets the maximal peptide length to use when searching with no enzyme.
     *
     * @param maxPeptideLengthNoEnzyme the maximal peptide length to use when
     * searching with no enzyme
     */
    public void setMaxPeptideLengthNoEnzyme(int maxPeptideLengthNoEnzyme) {
        this.maxPeptideLengthNoEnzyme = maxPeptideLengthNoEnzyme;
    }

    /**
     * Indicates whether I and L should be considered indistinguishable.
     *
     * @return a boolean indicating whether I and L should be considered
     * indistinguishable
     */
    public boolean isEqualIL() {
        if (equalIL == null) { // backward compatibility
            equalIL = false;
        }
        return equalIL;
    }

    /**
     * Sets whether I and L should be considered indistinguishable.
     *
     * @param equalIL a boolean indicating whether I and L should be considered
     * indistinguishable
     */
    public void setEqualIL(boolean equalIL) {
        this.equalIL = equalIL;
    }

    /**
     * Returns the number of candidates.
     *
     * @return the number of candidates
     */
    public int getNumberOfCandidates() {
        if (numberOfCandidates == null) { // backward compatibility
            numberOfCandidates = 15;
        }
        return numberOfCandidates;
    }

    /**
     * Sets the number of candidates.
     *
     * @param numberOfCandidates the number of candidates
     */
    public void setNumberOfCandidates(int numberOfCandidates) {
        this.numberOfCandidates = numberOfCandidates;
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.andromeda;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof AndromedaParameters) {
            AndromedaParameters andromedaParameters = (AndromedaParameters) identificationAlgorithmParameter;

            // @TODO: implement me!
            return true;
        }

        return false;
    }

    @Override
    public String toString(boolean html) {
        String newLine = System.getProperty("line.separator");

        if (html) {
            newLine = "<br>";
        }

        StringBuilder output = new StringBuilder();
        Advocate advocate = getAlgorithm();
        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append("# ").append(advocate.getName()).append(" Specific Parameters");
        output.append(newLine);
        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append(newLine);

//        output.append("NUMBER_SPECTRUM_MATCHES="); // @TODO: implement me!
//        output.append(numberOfSpectrumMatches);
//        output.append(newLine);
        return output.toString();
    }
}
