package com.compomics.util.experiment.identification.identification_parameters.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.massspectrometry.FragmentationMethod;
import java.util.HashMap;

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
     * Boolean indicating whether the higher charge option should be used.
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
    private Integer numberOfCandidates = 10;
    /**
     * Map of the Andromeda indexes used for user modifications in this search.
     */
    private HashMap<Integer, String> ptmIndexes = new HashMap<>(128);
    /**
     * The decoy mode.
     */
    private AndromedaDecoyMode decoyMode = AndromedaDecoyMode.none;

    /**
     * The available decoy modes.
     */
    public enum AndromedaDecoyMode {
        none, reverse;
    }

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
    public double getMaxPeptideMass() {
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
     * Returns the decoy mode.
     * 
     * @return the decoy mode.
     */
    public AndromedaDecoyMode getDecoyMode() {
        if (decoyMode == null) {
            decoyMode = AndromedaDecoyMode.none;
        }
        return decoyMode;
    }

    /**
     * Set the decoy mode.
     * 
     * @param decoyMode the decoy mode
     */
    public void setDecoyMode(AndromedaDecoyMode decoyMode) {
        this.decoyMode = decoyMode;
    }

    /**
     * Returns the number of candidates.
     *
     * @return the number of candidates
     */
    public int getNumberOfCandidates() {
        return numberOfCandidates;
    }

    /**
     * Sets the index for a given modification. If another modification was
     * already given with the same index the previous setting will be silently
     * overwritten.
     *
     * @param modificationName the name of the modification
     * @param ptmIndex the index of the modification
     */
    public void setPtmIndex(String modificationName, int ptmIndex) {
        ptmIndexes.put(ptmIndex, modificationName);
    }

    /**
     * Returns the name of the modification indexed by the given index. Null if
     * not found.
     *
     * @param ptmIndex the index of the modification to look for
     *
     * @return the name of the modification indexed by the given index
     */
    public String getModificationName(int ptmIndex) {
        return ptmIndexes.get(ptmIndex);
    }

    /**
     * Indicates whether the modification profile has PTM indexes.
     *
     * @return true if an PTM indexes map is set
     */
    public boolean hasPtmIndexes() {
        return ptmIndexes != null && !ptmIndexes.isEmpty();
    }

    /**
     * Returns the index of a given modification, null if not found.
     *
     * @param modificationName the name of the modification
     *
     * @return the corresponding index
     */
    public Integer getPtmIndex(String modificationName) {
        for (int index : ptmIndexes.keySet()) {
            if (modificationName.equalsIgnoreCase(ptmIndexes.get(index))) {
                return index;
            }
        }
        return null;
    }

    /**
     * Returns the PTM indexes as a map.
     *
     * @return the PTM indexes
     */
    public HashMap<Integer, String> getPtmIndexes() {
        return ptmIndexes;
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

            if (minPeptideLengthNoEnzyme != andromedaParameters.getMinPeptideLengthNoEnzyme()) {
                return false;
            }
            if (maxPeptideLengthNoEnzyme != andromedaParameters.getMaxPeptideLengthNoEnzyme()) {
                return false;
            }
            if (maxPeptideMass != andromedaParameters.getMaxPeptideMass()) {
                return false;
            }
            if (numberOfCandidates != andromedaParameters.getNumberOfCandidates()) {
                return false;
            }
            if (maxNumberOfModifications != andromedaParameters.getMaxNumberOfModifications()) {
                return false;
            }
            if (fragmentationMethod != andromedaParameters.getFragmentationMethod()) {
                return false;
            }
            if (!includeWater.equals(andromedaParameters.isIncludeWater())) {
                return false;
            }
            if (!includeAmmonia.equals(andromedaParameters.isIncludeAmmonia())) {
                return false;
            }
            if (!dependentLosses.equals(andromedaParameters.isDependentLosses())) {
                return false;
            }
            if (!equalIL.equals(andromedaParameters.isEqualIL())) {
                return false;
            }
            if (!fragmentAll.equals(andromedaParameters.isFragmentAll())) {
                return false;
            }
            if (!empiricalCorrection.equals(andromedaParameters.isEmpiricalCorrection())) {
                return false;
            }
            if (!higherCharge.equals(andromedaParameters.isHigherCharge())) {
                return false;
            }
            if (maxCombinations != andromedaParameters.getMaxCombinations()) {
                return false;
            }
            if (topPeaks != andromedaParameters.getTopPeaks()) {
                return false;
            }
            if (topPeaksWindow != andromedaParameters.getTopPeaksWindow()) {
                return false;
            }
            if (getDecoyMode() != andromedaParameters.getDecoyMode()) {
                return false;
            }

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

        output.append("MIN_PEPTIDE_LENGHT_NO_ENZYME=");
        output.append(minPeptideLengthNoEnzyme);
        output.append(newLine);
        output.append("MAX_PEPTIDE_LENGHT_NO_ENZYME=");
        output.append(maxPeptideLengthNoEnzyme);
        output.append(newLine);
        output.append("MAX_PEPTIDE_MASS=");
        output.append(maxPeptideMass);
        output.append(newLine);
        output.append("NUMBER_SPECTRUM_MATCHES=");
        output.append(numberOfCandidates);
        output.append(newLine);
        output.append("MAX_PTMS=");
        output.append(maxNumberOfModifications);
        output.append(newLine);
        output.append("FRAGMENTATION_METHOD=");
        output.append(fragmentationMethod);
        output.append(newLine);
        output.append("WATER_LOSS=");
        output.append(includeWater);
        output.append(newLine);
        output.append("AMMONIA_LOSS=");
        output.append(includeAmmonia);
        output.append(newLine);
        output.append("SEQUENCE_DEPENDENT_NEUTRAL_LOSS=");
        output.append(dependentLosses);
        output.append(newLine);
        output.append("EQUAL_IL=");
        output.append(equalIL);
        output.append(newLine);
        output.append("FRAGMENT_ALL=");
        output.append(fragmentAll);
        output.append(newLine);
        output.append("EMPERICAL_CORRECTION=");
        output.append(empiricalCorrection);
        output.append(newLine);
        output.append("HIGHER_CHARGE=");
        output.append(higherCharge);
        output.append(newLine);
        output.append("MAX_COMBINATIONS=");
        output.append(maxCombinations);
        output.append(newLine);
        output.append("TOP_PEAKS=");
        output.append(topPeaks);
        output.append(newLine);
        output.append("TOP_PEAKS_WINDOW=");
        output.append(topPeaksWindow);
        output.append(newLine);
        output.append("DECOY_MODE=");
        output.append(decoyMode);
        output.append(newLine);

        return output.toString();
    }
}
