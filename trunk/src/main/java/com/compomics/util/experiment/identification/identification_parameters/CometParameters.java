package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationAlgorithmParameter;

/**
 * The Comet specific parameters.
 *
 * @author Harald Barsnes
 */
public class CometParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = -2996752557726296967L;
    /**
     * The maximum number of spectrum matches.
     */
    private Integer numberOfSpectrumMatches = 10; // @TODO: should be the same as num_output_lines
    /**
     * The maximum number of variable modifications.
     */
    private Integer maxVariableMods = 10;
    /**
     * Controls whether the peptides must contain at least one variable
     * modification i.e. force all reported peptides to have a variable
     * modification.
     */
    private Boolean requireVariableMods = false;
    /**
     * The minimum allowed number of peaks in a spectrum.
     */
    private Integer minPeaks = 10;
    /**
     * The minimum peak intensity.
     */
    private Double minPeakIntensity = 0.0;
    /**
     * Remove precursor peak. An input value of 0 will not perform any precursor
     * removal. An input value of 1 will remove all peaks around the precursor
     * m/z. An input value of 2 will remove all charge reduced precursor peaks
     * as expected to be present for ETD/ECD spectra.
     */
    private Integer removePrecursor = 0;
    /**
     * This parameter specifies the mass tolerance (in Da) around each precursor
     * m/z that would be removed when the remove_precursor_peak option is
     * invoked.
     */
    private Double removePrecursorTolerance = 1.5;
    /**
     * Defines the lower m/z value to clear out in each MS/MS spectra.
     */
    private Double lowerClearMzRange = 0.0;
    /**
     * Defines the upper m/z value to clear out in each MS/MS spectra.
     */
    private Double upperClearMzRange = 0.0;
    /**
     * The enzyme type: 1 for a semi-enzyme search, 2 for a full-enzyme search,
     * 8 for a semi-enzyme search, unspecific cleavage on peptide's N-terminus
     * and 9 for a semi-enzyme search, unspecific cleavage on peptide's
     * C-terminus.
     */
    private Integer enzymeType = 2;
    /**
     * Isotope correction setting. 0: performs no isotope error searches, 1:
     * searches -1, 0, +1, +2, and +3 isotope offsets and 2: searches -8, -4, 0,
     * +4, +8 isotope offsets (for +4/+8 stable isotope labeling).
     */
    private Integer isotopeCorrection = 1;
    /**
     * The minimum precursor mass.
     */
    private Double minPrecursorMass = 0.0;
    /**
     * The maximum precursor mass.
     */
    private Double maxPrecursorMass = 10000.0;
    /**
     * The maximum fragment charge.
     */
    private Integer maxFragmentCharge = 3; // allowed: 1-5
    /**
     * This parameter controls whether Comet will automatically remove the
     * N-terminal methionine from a sequence entry.
     */
    private Boolean removeMethionine = false;
    /**
     * When this parameter is set to a non-zero value, say 5000, this causes
     * Comet to load and search about 5000 spectra at a time, looping through
     * sets of 5000 spectra until all data have been analyzed. Set this
     * parameter to 0 to load and search all spectra at once.
     */
    private Integer batchSize = 0;
    /**
     * The correlation score type. This parameter specifies how theoretical
     * fragment ion peaks are represented.
     */
    private Boolean theoreticalFragmentIonsSumOnly = false;
    /**
     * The fragment bin offset.
     */
    private Double fragmentBinOffset = 0.0;
    /**
     * Controls whether or not internal sparse matrix data representation is
     * used.
     */
    private Boolean useSparseMatrix = true;

    /**
     * Constructor.
     */
    public CometParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.comet;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof CometParameters) {
            CometParameters cometParameters = (CometParameters) identificationAlgorithmParameter;

            if (!numberOfSpectrumMatches.equals(cometParameters.getNumberOfSpectrumMatches())) {
                return false;
            }
            if (!maxVariableMods.equals(cometParameters.getMaxVariableMods())) {
                return false;
            }
            if (getRequireVariableMods() != cometParameters.getRequireVariableMods()) {
                return false;
            }
            if (!minPeaks.equals(cometParameters.getMinPeaks())) {
                return false;
            }
            double diff = Math.abs(minPeakIntensity - cometParameters.getMinPeakIntensity());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!removePrecursor.equals(cometParameters.getRemovePrecursor())) {
                return false;
            }
            diff = Math.abs(removePrecursorTolerance - cometParameters.getRemovePrecursorTolerance());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(lowerClearMzRange - cometParameters.getLowerClearMzRange());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(upperClearMzRange - cometParameters.getUpperClearMzRange());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!enzymeType.equals(cometParameters.getEnzymeType())) {
                return false;
            }
            if (!isotopeCorrection.equals(cometParameters.getIsotopeCorrection())) {
                return false;
            }
            diff = Math.abs(minPrecursorMass - cometParameters.getMinPrecursorMass());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(maxPrecursorMass - cometParameters.getMaxPrecursorMass());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!maxFragmentCharge.equals(cometParameters.getMaxFragmentCharge())) {
                return false;
            }
            if (!removeMethionine.equals(cometParameters.getRemoveMethionine())) {
                return false;
            }
            if (!batchSize.equals(cometParameters.getBatchSize())) {
                return false;
            }
            if (!theoreticalFragmentIonsSumOnly.equals(cometParameters.getTheoreticalFragmentIonsSumOnly())) {
                return false;
            }
            diff = Math.abs(fragmentBinOffset - cometParameters.getFragmentBinOffset());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!useSparseMatrix.equals(cometParameters.getUseSparseMatrix())) {
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

        output.append("NUMBER_SPECTRUM_MATCHES=");
        output.append(numberOfSpectrumMatches);
        output.append(newLine);
        output.append("MAX_VARIABLE_MODS=");
        output.append(maxVariableMods);
        output.append(newLine);
        output.append("REQUIRE_VARIABLE_MODS=");
        output.append(requireVariableMods);
        output.append(newLine);
        output.append("MIN_PEAKS=");
        output.append(minPeaks);
        output.append(newLine);
        output.append("MIN_PEAKS_INTENSITY=");
        output.append(minPeakIntensity);
        output.append(newLine);
        output.append("REMOVE_PRECURSOR=");
        output.append(removePrecursor);
        output.append(newLine);
        output.append("REMOVE_PRECURSOR_TOLERANCE=");
        output.append(removePrecursorTolerance);
        output.append(newLine);
        output.append("LOWER_CLEAR_MZ_RANGE=");
        output.append(lowerClearMzRange);
        output.append(newLine);
        output.append("UPPER_CLEAR_MZ_RANGE=");
        output.append(upperClearMzRange);
        output.append(newLine);
        output.append("ENZYME_TYPE=");
        output.append(enzymeType);
        output.append(newLine);
        output.append("ISOTOPE_CORRECTION=");
        output.append(isotopeCorrection);
        output.append(newLine);
        output.append("MIN_PRECURSOR_MASS=");
        output.append(minPrecursorMass);
        output.append(newLine);
        output.append("MAX_PRECURSOR_MASS=");
        output.append(maxPrecursorMass);
        output.append(newLine);
        output.append("MAX_FRAGMENT_CHARGE=");
        output.append(maxFragmentCharge);
        output.append(newLine);
        output.append("REMOVE_METHIONINE=");
        output.append(removeMethionine);
        output.append(newLine);
        output.append("BATCH_SIZE=");
        output.append(batchSize);
        output.append(newLine);
        output.append("THEORETICAL_FRAGMENT_IONS_SUM_ONLY=");
        output.append(theoreticalFragmentIonsSumOnly);
        output.append(newLine);
        output.append("FRAGMENT_BIN_OFFSET=");
        output.append(fragmentBinOffset);
        output.append(newLine);
        output.append(newLine);
        output.append("USE_SPARSE_MATRIX=");
        output.append(useSparseMatrix);
        output.append(newLine);

        return output.toString();
    }

    /**
     * Returns the maximum number of spectrum matches.
     *
     * @return the numberOfSpectrumMarches
     */
    public Integer getNumberOfSpectrumMatches() {
        if (numberOfSpectrumMatches == null) {
            numberOfSpectrumMatches = 10;
        }
        return numberOfSpectrumMatches;
    }

    /**
     * Set the maximum number of spectrum matches.
     *
     * @param numberOfSpectrumMarches the numberOfSpectrumMarches to set
     */
    public void setNumberOfSpectrumMatches(Integer numberOfSpectrumMarches) {
        this.numberOfSpectrumMatches = numberOfSpectrumMarches;
    }

    /**
     * Returns the maximum number of variable modifications.
     *
     * @return the maxVariableMods
     */
    public Integer getMaxVariableMods() {
        return maxVariableMods;
    }

    /**
     * Set the maximum number of variable modifications.
     *
     * @param maxVariableMods the maxVariableMods to set
     */
    public void setMaxVariableMods(Integer maxVariableMods) {
        this.maxVariableMods = maxVariableMods;
    }

    /**
     * Returns the minimum number of peaks.
     *
     * @return the minPeaks
     */
    public Integer getMinPeaks() {
        return minPeaks;
    }

    /**
     * Set the minimum number of peaks.
     *
     * @param minPeaks the minPeaks to set
     */
    public void setMinPeaks(Integer minPeaks) {
        this.minPeaks = minPeaks;
    }

    /**
     * Returns the minimum peak intensity.
     *
     * @return the minPeakIntensity
     */
    public Double getMinPeakIntensity() {
        return minPeakIntensity;
    }

    /**
     * Set the minimum peak intensity.
     *
     * @param minPeakIntensity the minPeakIntensity to set
     */
    public void setMinPeakIntensity(Double minPeakIntensity) {
        this.minPeakIntensity = minPeakIntensity;
    }

    /**
     * Returns if the precursor is to be removed.
     *
     * @return the removePrecursor
     */
    public Integer getRemovePrecursor() {
        return removePrecursor;
    }

    /**
     * Set if the precursor is to be removed.
     *
     * @param removePrecursor the removePrecursor to set
     */
    public void setRemovePrecursor(Integer removePrecursor) {
        this.removePrecursor = removePrecursor;
    }

    /**
     * Returns the precursor removal tolerance.
     *
     * @return the removePrecursorTolerance
     */
    public Double getRemovePrecursorTolerance() {
        return removePrecursorTolerance;
    }

    /**
     * Set the precursor removal tolerance.
     *
     * @param removePrecursorTolerance the removePrecursorTolerance to set
     */
    public void setRemovePrecursorTolerance(Double removePrecursorTolerance) {
        this.removePrecursorTolerance = removePrecursorTolerance;
    }

    /**
     * Returns the lower mass value for the clear mz range.
     *
     * @return the lowerClearMzRange
     */
    public Double getLowerClearMzRange() {
        return lowerClearMzRange;
    }

    /**
     * Set the lower mass value for the clear mz range.
     *
     * @param lowerClearMzRange the lowerClearMzRange to set
     */
    public void setLowerClearMzRange(Double lowerClearMzRange) {
        this.lowerClearMzRange = lowerClearMzRange;
    }

    /**
     * Returns the upper mass value for the clear mz range.
     *
     * @return the upperClearMzRange
     */
    public Double getUpperClearMzRange() {
        return upperClearMzRange;
    }

    /**
     * Set the upper mass value for the clear mz range.
     *
     * @param upperClearMzRange the upperClearMzRange to set
     */
    public void setUpperClearMzRange(Double upperClearMzRange) {
        this.upperClearMzRange = upperClearMzRange;
    }

    /**
     * Returns the enzyme type.
     *
     * @return the enzymeType
     */
    public Integer getEnzymeType() {
        return enzymeType;
    }

    /**
     * Set the enzyme type.
     *
     * @param enzymeType the enzymeType to set
     */
    public void setEnzymeType(Integer enzymeType) {
        this.enzymeType = enzymeType;
    }

    /**
     * Returns the isotope correction setting.
     *
     * @return the isotopeCorrection
     */
    public Integer getIsotopeCorrection() {
        return isotopeCorrection;
    }

    /**
     * Set the isotope correction setting.
     *
     * @param isotopeCorrection the isotopeCorrection to set
     */
    public void setIsotopeCorrection(Integer isotopeCorrection) {
        this.isotopeCorrection = isotopeCorrection;
    }

    /**
     * Returns the minimum precursor mass.
     *
     * @return the minPrecursorMass
     */
    public Double getMinPrecursorMass() {
        return minPrecursorMass;
    }

    /**
     * Set the minimum precursor mass.
     *
     * @param minPrecursorMass the minPrecursorMass to set
     */
    public void setMinPrecursorMass(Double minPrecursorMass) {
        this.minPrecursorMass = minPrecursorMass;
    }

    /**
     * Returns the maximum precursor mass.
     *
     * @return the maxPrecursorMass
     */
    public Double getMaxPrecursorMass() {
        return maxPrecursorMass;
    }

    /**
     * Set the maximum precursor mass.
     *
     * @param maxPrecursorMass the maxPrecursorMass to set
     */
    public void setMaxPrecursorMass(Double maxPrecursorMass) {
        this.maxPrecursorMass = maxPrecursorMass;
    }

    /**
     * Returns the maximum fragment ion charge.
     *
     * @return the maxFragmentCharge
     */
    public Integer getMaxFragmentCharge() {
        return maxFragmentCharge;
    }

    /**
     * Set the maximum fragment ion charge.
     *
     * @param maxFragmentCharge the maxFragmentCharge to set
     */
    public void setMaxFragmentCharge(Integer maxFragmentCharge) {
        this.maxFragmentCharge = maxFragmentCharge;
    }

    /**
     * Returns true if n-term methionine is to be removed.
     *
     * @return the removeMethionine
     */
    public Boolean getRemoveMethionine() {
        return removeMethionine;
    }

    /**
     * Sets if n-term methionine is to be removed.
     *
     * @param removeMethionine the removeMethionine to set
     */
    public void setRemoveMethionine(Boolean removeMethionine) {
        this.removeMethionine = removeMethionine;
    }

    /**
     * Returns the batch size.
     *
     * @return the batchSize
     */
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * Set the batch size.
     *
     * @param batchSize the batchSize to set
     */
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Returns true if the theoretical fragment ions are calculated on the sum
     * in the bin alone. False means that flanking bins are used as well.
     *
     * @return the theoreticalFragmentIonsSumOnly
     */
    public Boolean getTheoreticalFragmentIonsSumOnly() {
        return theoreticalFragmentIonsSumOnly;
    }

    /**
     * Set if the theoretical fragment ions are calculated on the sum in the bin
     * alone. False means that flanking bins are used as well.
     *
     * @param theoreticalFragmentIonsSumOnly the theoreticalFragmentIonsSumOnly
     * to set
     */
    public void setTheoreticalFragmentIonsSumOnly(Boolean theoreticalFragmentIonsSumOnly) {
        this.theoreticalFragmentIonsSumOnly = theoreticalFragmentIonsSumOnly;
    }

    /**
     * Returns the fragment ion bin offset.
     *
     * @return the fragmentBinOffset
     */
    public Double getFragmentBinOffset() {
        return fragmentBinOffset;
    }

    /**
     * Set the fragment ion bin offset.
     *
     * @param fragmentBinOffset the fragmentBinOffset to set
     */
    public void setFragmentBinOffset(Double fragmentBinOffset) {
        this.fragmentBinOffset = fragmentBinOffset;
    }

    /**
     * Returns true if a sparse matrix is to be used.
     *
     * @return the useSparseMatrix
     */
    public Boolean getUseSparseMatrix() {
        return useSparseMatrix;
    }

    /**
     * Set if a sparse matrix is to be used.
     *
     * @param useSparseMatrix the useSparseMatrix to set
     */
    public void setUseSparseMatrix(Boolean useSparseMatrix) {
        this.useSparseMatrix = useSparseMatrix;
    }

    /**
     * Returns true if at least one variable modification is required per
     * peptide.
     *
     * @return the requireVariableMods
     */
    public boolean getRequireVariableMods() {
        if (requireVariableMods == null) {
            requireVariableMods = false; 
        }
        return requireVariableMods;
    }

    /**
     * Set if at least one variable modification is required per peptide.
     *
     * @param requireVariableMods the requireVariableMods to set
     */
    public void setRequireVariableMods(Boolean requireVariableMods) {
        this.requireVariableMods = requireVariableMods;
    }
}
