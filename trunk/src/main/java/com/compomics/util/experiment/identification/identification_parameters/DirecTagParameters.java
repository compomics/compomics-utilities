package com.compomics.util.experiment.identification.identification_parameters;

/**
 * The DirecTag specific parameters.
 *
 * @author Thilo Muth
 */
public class DirecTagParameters {

    /**
     * Controls the number of charge states that DirecTag will handle during all
     * stages of the program.
     */
    private int numChargeStates = 3;
    /**
     * The output of a DirecTag job will be a TAGS file for each input file. The
     * string specified by this parameter will be appended to each TAGS
     * filename. It is useful for differentiating jobs within a single
     * directory.
     */
    private String outputSuffix = "";
    /**
     * If true, DirecTag will use the charge state from the input data if it is
     * available. If false, or if charge state is not available from a
     * particular spectrum, DirecTag will use its internal algorithm to
     * determine charge state.
     */
    private boolean useChargeStateFromMS = false;
    /*
     * If DirecTag determines a spectrum to be multiply charged and this parameter is true, the spectrum will be copied and treated as if it was all possible charge states from +2 to +<NumChargeStates>. 
     * If this parameter is false, the spectrum will simply be treated as a +2.
     */
    private boolean duplicateSpectra = true;
    /**
     * Deisotoping a spectrum (consolidating isotopic peak intensities into the
     * monoisotopic peaks intensity) during preprocessing will significantly
     * improve precursor adjustment, and it may be desirable to keep the
     * deisotoped spectrum around for candidate scoring as well. Set to 0, no
     * deisotoping will be used. Set to 1, deisotoping will be used for
     * precursor adjustment only. Set to 2, deisotoping will be used for both
     * precursor adjustment and for candidate scoring.
     */
    private int deisotopingMode = 0;
    /**
     * When deisotoping a spectrum, an isotopic peak is one that is the mass of
     * a neutron higher than another peak, tolerating variation based on the
     * value of this parameter. Deisotoping actually traverses the spectrum at
     * multiple charge states, starting from the highest (NumChargeStates) and
     * ending at the lowest.
     */
    private double isotopeMzTolerance = 0.25;
    /**
     * When adjusting the precursor mass, this parameter controls how much
     * tolerance there is on each side of the calculated m/z when looking for a
     * peaks complement.
     */
    private double complementMzTolerance = 0.5;
    /**
     * A sequence tag is generated from the gaps between a number of peaks equal
     * to this parameter plus one. Longer tag lengths are more specific, but
     * harder to find because many consecutive ion fragments are rare.
     */
    private int tagLength = 3;
    /**
     * This parameter sets the maximum number of modified residues that may be
     * in any candidate sequence.
     */
    private int maxDynamicMods = 2;
    /**
     * This parameter sets the maximum number of sequence tags to report for
     * each spectrum.
     */
    private int maxTagCount = 50;
    /**
     * This parameter controls how intensity scores are combined to
     * form a total score. DirecTag scores tags on the basis of their peak intensities. 
     * Tags that contain intense peaks are more likely to be correct than those that contain average peaks. 
     */
    private double intensityScoreWeight = 1.0;
    /**
     * This parameter controls how mzFidelity scores are combined to
     * form a total score. m/z fidelity for a tag can be characterized through SSE. DirecTag evaluates the consistency of fragment ion m/z values for each tag. 
     */
    private double mzFidelityScoreWeight = 1.0;
    /**
     * This parameter controls how complement scores are combined to
     * form a total score. Peaks that match to complementary ions within the spectrum are more trustworthy than other peaks. 
     * DirecTag assesses the number and concordance of complementary ions for each tag.
     */
    private double complementScoreWeight = 1.0;

    /**
     * Constructor.
     */
    public DirecTagParameters() {
    }
    
    /**
     * Returns the number of charge states.
     * @return numChargeStates the number of charge states
     */
    public int getNumChargeStates() {
        return numChargeStates;
    }
    
     /**
     * Sets the number of charge states.
     * @param numChargeStates the number of charge states
     */
    public void setNumChargeStates(int numChargeStates) {
        this.numChargeStates = numChargeStates;
    }
    
    /**
     * Returns the output tags suffix.
     * @return the output tags suffix.
     */
    public String getOutputSuffix() {
        return outputSuffix;
    }
    
    /**
     * Sets the output tags suffix.
     * @param outputSuffix the output tags suffix.
     */
    public void setOutputSuffix(String outputSuffix) {
        this.outputSuffix = outputSuffix;
    }
    
    /**
     * Indicates whether the charge state from the spectrum should be used.
     * @return boolean indicating whether the charge states from the spectrum should be used
     */
    public boolean isUseChargeStateFromMS() {
        return useChargeStateFromMS;
    }
    
    /**
     * Sets whether the charge state from the spectrum should be used.
     * @param useChargeStateFromMS boolean indicating whether the charge states from the spectrum should be used
     */
    public void setUseChargeStateFromMS(boolean useChargeStateFromMS) {
        this.useChargeStateFromMS = useChargeStateFromMS;
    }
    
    /**
     * Indicates whether to use duplicate spectra option.
     * @return boolean indicating whether to use duplicate spectra option
     */
    public boolean isDuplicateSpectra() {
        return duplicateSpectra;
    }
    
    /**
     * Sets whether to use duplicate spectra option.
     * @param duplicateSpectra boolean indicating whether to use duplicate spectra option
     */
    public void setDuplicateSpectra(boolean duplicateSpectra) {
        this.duplicateSpectra = duplicateSpectra;
    }
    
    /**
     * Returns the deisotoping flag value.
     * @return deisotoping flag value
     */
    public int getDeisotopingMode() {
        return deisotopingMode;
    }
    
    /**
     * Sets the deisotoping flag value.
     * @param deisotopingMode deisotoping flag value
     */
    public void setDeisotopingMode(int deisotopingMode) {
        this.deisotopingMode = deisotopingMode;
    }
    
    /**
     * Returns the deisotoping m/z tolerance.
     * @return deisotoping m/z tolerance
     */
    public double getIsotopeMzTolerance() {
        return isotopeMzTolerance;
    }
    
    /**
     * Sets the deisotoping m/z tolerance.
     * @param isotopeMzTolerance deisotoping m/z tolerance
     */
    public void setIsotopeMzTolerance(double isotopeMzTolerance) {
        this.isotopeMzTolerance = isotopeMzTolerance;
    }
    
    /**
     * Returns the complement m/z tolerance.
     * @return the complement m/z tolerance
     */
    public double getComplementMzTolerance() {
        return complementMzTolerance;
    }
    
    /**
     * Sets the complement m/z tolerance
     * @param complementMzTolerance the complement m/z tolerance
     */
    public void setComplementMzTolerance(double complementMzTolerance) {
        this.complementMzTolerance = complementMzTolerance;
    }
    
    /**
     * Returns the tag legnth.
     * @return the tag length
     */
    public int getTagLength() {
        return tagLength;
    }
    /**
     * Sets the tag length.
     * @param tagLength the tag length
     */
    public void setTagLength(int tagLength) {
        this.tagLength = tagLength;
    }
    
    /**
     * Returns the maximum number of dynamic mods.
     * @return the maximum number of dynamic mods
     */
    public int getMaxDynamicMods() {
        return maxDynamicMods;
    }
    
    /**
     * Sets the maximum number of dynamic mods.
     * @param maxDynamicMods the maximum number of dynamic mods
     */
    public void setMaxDynamicMods(int maxDynamicMods) {
        this.maxDynamicMods = maxDynamicMods;
    }
    
    /**
     * Returns the maximum number of tags per spectrum to be generated.
     * @return the maximum number of tags per spectrum to be generated
     */
    public int getMaxTagCount() {
        return maxTagCount;
    }
    
    /**
     * Sets the maximum number of tags per spectrum to be generated.
     * @param maxTagCount the maximum number of tags per spectrum to be generated
     */
    public void setMaxTagCount(int maxTagCount) {
        this.maxTagCount = maxTagCount;
    }
    
    /**
     * Returns the intensity score weight. Default is 1.0.
     * @return the intensity score weight
     */
    public double getIntensityScoreWeight() {
        return intensityScoreWeight;
    }
    
    /**
     * Set the intensity score weight. Default is 1.0.
     * @param intensityScoreWeight the intensity score weight
     */
    public void setIntensityScoreWeight(double intensityScoreWeight) {
        this.intensityScoreWeight = intensityScoreWeight;
    }
    
    /**
     * Returns the mzFidelity score weight. Default is 1.0.
     * @return the mzFidelity score weight
     */
    public double getMzFidelityScoreWeight() {
        return mzFidelityScoreWeight;
    }
    
    /**
     * Set the mzFidelity score weight. Default is 1.0.
     * @param mzFidelityScoreWeight the mzFidelity score weight
     */
    public void setMzFidelityScoreWeight(double mzFidelityScoreWeight) {
        this.mzFidelityScoreWeight = mzFidelityScoreWeight;
    }

    /**
     * Returns the complement score weight. Default is 1.0.
     * @return the complement score weight
     */
    public double getComplementScoreWeight() {
        return complementScoreWeight;
    }
    
    /**
     * Set the complement score weight. Default is 1.0.
     * @param complementScoreWeight the complement score weight
     */
    public void setComplementScoreWeight(double complementScoreWeight) {
        this.complementScoreWeight = complementScoreWeight;
    }
}