package com.compomics.util.parameters.identification.tool_specific;

import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The MetaMorpheus specific parameters.
 *
 * @author Harald Barsnes
 */
public class MetaMorpheusParameters extends ExperimentObject implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = 3692530126026333412L;

    /**
     * The available decoy types.
     */
    public enum MetaMorpheusDecoyType {
        None, Reverse, Slide;
    }

    /**
     * The available search types.
     */
    public enum MetaMorpheusSearchType {
        Classic, Modern, NonSpecific;
    }

    /**
     * The available dissociation types. Note that more types are supported by
     * MetaMorpeus, including custom, but these are not yet supported by
     * SearchGUI.
     */
    public enum MetaMorpheusDissociationType {
        HCD, CID, ECD, ETD;
    }

    /**
     * The available initiator methionine behavior types.
     */
    public enum MetaMorpheusInitiatorMethionineBehaviorType {
        Undefined, Retain, Cleave, Variable;
    }

    /**
     * The available fragmentation terminus types.
     */
    public enum MetaMorpheusFragmentationTerminusType {
        Both, N, C;
    }

    /**
     * The available mass difference acceptor types.
     */
    public enum MetaMorpheusMassDiffAcceptorType {
        Exact, OneMM, TwoMM, ThreeMM, PlusOrMinusThreeMM, ModOpen, Open;
    }

    /**
     * The available decoy types.
     */
    public enum MetaMorpheusToleranceType {
        PPM, Absolute;
    }

    /**
     * Minimum peptide length.
     */
    private Integer minPeptideLength = 8;
    /**
     * Maximum peptide length.
     */
    private Integer maxPeptideLength = 30;
    /**
     * Search type.
     */
    private MetaMorpheusSearchType searchType = MetaMorpheusSearchType.Classic;
    /**
     * Number of partitions when doing a modern or non-specific search.
     */
    private Integer totalPartitions = 1;
    /**
     * Dissociation type.
     */
    private MetaMorpheusDissociationType dissociationType = MetaMorpheusDissociationType.HCD;
    /**
     * Maximum number of modifications per peptide.
     */
    private Integer maxModsForPeptide = 2;
    /**
     * Initiator methionine behavior.
     */
    private MetaMorpheusInitiatorMethionineBehaviorType initiatorMethionineBehavior = MetaMorpheusInitiatorMethionineBehaviorType.Variable;
    /**
     * Score cut-off.
     */
    private Double scoreCutoff = 5.0;
    /**
     * Use delta score.
     */
    private boolean useDeltaScore = false;
    /**
     * Fragmentation terminus type.
     */
    private MetaMorpheusFragmentationTerminusType fragmentationTerminus = MetaMorpheusFragmentationTerminusType.Both;
    /**
     * Max fragment size.
     */
    private Double maxFragmentSize = 30000.0;
    /**
     * The minimum allowed internal fragment length. 0 means "no internal
     * fragments".
     */
    private Integer minAllowedInternalFragmentLength = 0;
    /**
     * Mass difference acceptor type.
     */
    private MetaMorpheusMassDiffAcceptorType massDiffAcceptorType = MetaMorpheusMassDiffAcceptorType.OneMM;
    /**
     * Write mzId output.
     */
    private Boolean writeMzId = true;
    /**
     * Write pepXml output.
     */
    private Boolean writePepXml = false;
    /**
     * Use provided precursor info.
     */
    private Boolean useProvidedPrecursorInfo = true;
    /**
     * Do precursor deconvolution.
     */
    private Boolean doPrecursorDeconvolution = true;
    /**
     * Deconvolution intensity ratio.
     */
    private Double deconvolutionIntensityRatio = 3.0;
    /**
     * Deconvolution mass tolerance.
     */
    private Double deconvolutionMassTolerance = 4.0;
    /**
     * Deconvolution mass tolerance type: PPM or Absolute.
     */
    private MetaMorpheusToleranceType deconvolutionMassToleranceType = MetaMorpheusToleranceType.PPM;
    /**
     * Trim Ms1 peaks.
     */
    private Boolean trimMs1Peaks = false;
    /**
     * Trim MsMs peaks.
     */
    private Boolean trimMsMsPeaks = true;
    /**
     * Number of peaks to keep per window.
     */
    private Integer numberOfPeaksToKeepPerWindow = 200;
    /**
     * Minimum allowed intensity ratio to base peak.
     */
    private Double minAllowedIntensityRatioToBasePeak = 0.01;
    /**
     * Window width in Thomson.
     */
    private Double windowWidthThomson = null;
    /**
     * Number of windows.
     */
    private Integer numberOfWindows = null;
    /**
     * Normalize peaks across all windows.
     */
    private Boolean normalizePeaksAcrossAllWindows = false;
    /**
     * Modified peptides are different.
     */
    private Boolean modPeptidesAreDifferent = false;
    /**
     * No one hit wonders.
     */
    private Boolean noOneHitWonders = false;
    /**
     * Search target.
     */
    private Boolean searchTarget = true;
    /**
     * Decoy type: None, Reverse or Slide.
     */
    private MetaMorpheusDecoyType decoyType = MetaMorpheusDecoyType.None;
    /**
     * Max modification isoforms.
     */
    private Integer maxModificationIsoforms = 1024;
    /**
     * Min variant depth.
     */
    private Integer minVariantDepth = 1;
    /**
     * Max heterozygous variants.
     */
    private Integer maxHeterozygousVariants = 4;
    /**
     * If true, the G-PTM search is performed.
     */
    private boolean runGptm = false;
    /**
     * The modification categories to include in the G-PTM search.
     */
    private ArrayList<ModificationCategory> gPtmCategories = new ArrayList<>(
            Arrays.asList(ModificationCategory.Common_Biological,
                    ModificationCategory.Common_Artifact,
                    ModificationCategory.Metal)
    );

    /**
     * Constructor.
     */
    public MetaMorpheusParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.metaMorpheus;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof MetaMorpheusParameters) {

            MetaMorpheusParameters metaMorpheusParameters = (MetaMorpheusParameters) identificationAlgorithmParameter;

            if (!minPeptideLength.equals(metaMorpheusParameters.getMinPeptideLength())) {
                return false;
            }
            if (!maxPeptideLength.equals(metaMorpheusParameters.getMaxPeptideLength())) {
                return false;
            }
            if (searchType != metaMorpheusParameters.getSearchType()) {
                return false;
            }
            if (!totalPartitions.equals(metaMorpheusParameters.getTotalPartitions())) {
                return false;
            }
            if (dissociationType != metaMorpheusParameters.getDissociationType()) {
                return false;
            }
            if (!maxModsForPeptide.equals(metaMorpheusParameters.getMaxModsForPeptide())) {
                return false;
            }
            if (initiatorMethionineBehavior != metaMorpheusParameters.getInitiatorMethionineBehavior()) {
                return false;
            }
            double diff = Math.abs(scoreCutoff - metaMorpheusParameters.getScoreCutoff());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (getUseDeltaScore() != metaMorpheusParameters.getUseDeltaScore()) {
                return false;
            }
            if (fragmentationTerminus != metaMorpheusParameters.getFragmentationTerminus()) {
                return false;
            }
            diff = Math.abs(maxFragmentSize - metaMorpheusParameters.getMaxFragmentSize());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!getMinAllowedInternalFragmentLength().equals(metaMorpheusParameters.getMinAllowedInternalFragmentLength())) {
                return false;
            }
            if (massDiffAcceptorType != metaMorpheusParameters.getMassDiffAcceptorType()) {
                return false;
            }
            if (getWriteMzId() != metaMorpheusParameters.getWriteMzId()) {
                return false;
            }
            if (getWritePepXml() != metaMorpheusParameters.getWritePepXml()) {
                return false;
            }
            if (getUseProvidedPrecursorInfo() != metaMorpheusParameters.getUseProvidedPrecursorInfo()) {
                return false;
            }
            if (getDoPrecursorDeconvolution() != metaMorpheusParameters.getDoPrecursorDeconvolution()) {
                return false;
            }
            diff = Math.abs(deconvolutionIntensityRatio - metaMorpheusParameters.getDeconvolutionIntensityRatio());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(deconvolutionMassTolerance - metaMorpheusParameters.getDeconvolutionMassTolerance());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (deconvolutionMassToleranceType != metaMorpheusParameters.getDeconvolutionMassToleranceType()) {
                return false;
            }
            if (getTrimMs1Peaks() != metaMorpheusParameters.getTrimMs1Peaks()) {
                return false;
            }
            if (getTrimMsMsPeaks() != metaMorpheusParameters.getTrimMsMsPeaks()) {
                return false;
            }
            if (!numberOfPeaksToKeepPerWindow.equals(metaMorpheusParameters.getNumberOfPeaksToKeepPerWindow())) {
                return false;
            }
            diff = Math.abs(minAllowedIntensityRatioToBasePeak - metaMorpheusParameters.getMinAllowedIntensityRatioToBasePeak());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (windowWidthThomson != null && metaMorpheusParameters.getWindowWidthThomsons() == null
                    || windowWidthThomson == null && metaMorpheusParameters.getWindowWidthThomsons() != null
                    || (windowWidthThomson != null && metaMorpheusParameters.getWindowWidthThomsons() != null && !windowWidthThomson.equals(metaMorpheusParameters.getWindowWidthThomsons()))) {
                return false;
            }
            if (numberOfWindows != null && metaMorpheusParameters.getNumberOfWindows() == null
                    || numberOfWindows == null && metaMorpheusParameters.getNumberOfWindows() != null
                    || (numberOfWindows != null && metaMorpheusParameters.getNumberOfWindows() != null && !numberOfWindows.equals(metaMorpheusParameters.getNumberOfWindows()))) {
                return false;
            }
            if (getNormalizePeaksAcrossAllWindows() != metaMorpheusParameters.getNormalizePeaksAcrossAllWindows()) {
                return false;
            }
            if (getModPeptidesAreDifferent() != metaMorpheusParameters.getModPeptidesAreDifferent()) {
                return false;
            }
            if (getNoOneHitWonders() != metaMorpheusParameters.getNoOneHitWonders()) {
                return false;
            }
            if (getSearchTarget() != metaMorpheusParameters.getSearchTarget()) {
                return false;
            }
            if (decoyType != metaMorpheusParameters.getDecoyType()) {
                return false;
            }
            if (!maxModificationIsoforms.equals(metaMorpheusParameters.getMaxModificationIsoforms())) {
                return false;
            }
            if (!minVariantDepth.equals(metaMorpheusParameters.getMinVariantDepth())) {
                return false;
            }
            if (!maxHeterozygousVariants.equals(metaMorpheusParameters.getMaxHeterozygousVariants())) {
                return false;
            }
            if (runGptm != metaMorpheusParameters.runGptm()) {
                return false;
            }
            if (!gPtmCategories.equals(metaMorpheusParameters.getGPtmCategories())) {
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
        output.append("MIN_PEPTIDE_LENGTH=");
        output.append(getMinPeptideLength());
        output.append(newLine);
        output.append("MAX_PEPTIDE_LENGTH=");
        output.append(getMaxPeptideLength());
        output.append(newLine);
        output.append("SEARCH_TYPE=");
        output.append(getSearchType());
        output.append(newLine);
        output.append("NUM_PARTITIONS=");
        output.append(getTotalPartitions());
        output.append(newLine);
        output.append("DISSOCIATION_TYPE=");
        output.append(getDissociationType());
        output.append(newLine);
        output.append("MAX_MODS_FOR_PEPTIDE=");
        output.append(getMaxModsForPeptide());
        output.append(newLine);
        output.append("INITIATOR_METHIONINE_BEHAVIOR=");
        output.append(getInitiatorMethionineBehavior());
        output.append(newLine);
        output.append("SCORE_CUTOFF=");
        output.append(getScoreCutoff());
        output.append(newLine);
        output.append("USE_DELTA_SCORE=");
        output.append(getUseDeltaScore());
        output.append(newLine);
        output.append("FRAGMENTATION_TERMINUS=");
        output.append(getFragmentationTerminus());
        output.append(newLine);
        output.append("MAX_FRAGMENTATION_SIZE=");
        output.append(getMaxFragmentSize());
        output.append(newLine);
        output.append("MIN_ALLOWED_INTERNAL_FRAGMENT_LENGTH=");
        output.append(getMinAllowedInternalFragmentLength());
        output.append(newLine);
        output.append("MASS_DIFF_ACCEPTOR_TYPE=");
        output.append(getMassDiffAcceptorType());
        output.append(newLine);
        output.append("WRITE_MZID=");
        output.append(getWriteMzId());
        output.append(newLine);
        output.append("WRITE_PEPXML=");
        output.append(getWritePepXml());
        output.append(newLine);
        output.append("USE_PROVIDED_PRECURSOR=");
        output.append(getUseProvidedPrecursorInfo());
        output.append(newLine);
        output.append("DO_PREC_DECONVOLUTION=");
        output.append(getDoPrecursorDeconvolution());
        output.append(newLine);
        output.append("DECONVOLUTION_INT_RATIO=");
        output.append(getDeconvolutionIntensityRatio());
        output.append(newLine);
        output.append("DECONVOLUTION_MASS_TOL=");
        output.append(getDeconvolutionMassTolerance());
        output.append(newLine);
        output.append("DECONVOLUTION_MASS_TOL_TYPE=");
        output.append(getDeconvolutionMassToleranceType());
        output.append(newLine);
        output.append("TRIM_MS1_PEAKS=");
        output.append(getTrimMs1Peaks());
        output.append(newLine);
        output.append("TRIM_MSMS_PEAKS=");
        output.append(getTrimMsMsPeaks());
        output.append(newLine);
        output.append("NUM_PEAKS_PER_WINDOWS=");
        output.append(getNumberOfPeaksToKeepPerWindow());
        output.append(newLine);
        output.append("MIN_ALLOWED_INT_RATIO_TO_BASE_PEAK=");
        output.append(getMinAllowedIntensityRatioToBasePeak());
        output.append(newLine);
        output.append("WINDOW_WITH_THOMPSON=");
        output.append(getWindowWidthThomsons());
        output.append(newLine);
        output.append("NUM_WINDOWS=");
        output.append(getNumberOfWindows());
        output.append(newLine);
        output.append("NORMALIZE_PEAKS_ACROSS_ALL_WINDOWS=");
        output.append(getNormalizePeaksAcrossAllWindows());
        output.append(newLine);
        output.append("MOD_PEPTIDES_ARE_DIFFERENT=");
        output.append(getModPeptidesAreDifferent());
        output.append(newLine);
        output.append("NO_ONE_HIT_WONDERS=");
        output.append(getNoOneHitWonders());
        output.append(newLine);
        output.append("SEARCH_TARGET=");
        output.append(getSearchTarget());
        output.append(newLine);
        output.append("DECOY_TYPE=");
        output.append(getDecoyType());
        output.append(newLine);
        output.append("MAX_MOD_ISOFORMS=");
        output.append(getMaxModificationIsoforms());
        output.append(newLine);
        output.append("MIN_VARIANT_DEPTH=");
        output.append(getMinVariantDepth());
        output.append(newLine);
        output.append("MAX_HETROZYGOUS_VARIANTS=");
        output.append(getMaxHeterozygousVariants());
        output.append(newLine);
        output.append("RUN_GPTM=");
        output.append(runGptm());
        output.append(newLine);
        output.append("GPTMS=");

        String tempGPtmCategories = "";

        for (ModificationCategory tempCategory : gPtmCategories) {
            if (!tempGPtmCategories.isEmpty()) {
                tempGPtmCategories += ", ";
            }
            tempGPtmCategories += tempCategory;
        }

        output.append(tempGPtmCategories);
        output.append(newLine);

        return output.toString();
    }

    /**
     * Returns the minimum peptide length.
     *
     * @return the the minimum peptide length
     */
    public Integer getMinPeptideLength() {
        return minPeptideLength;
    }

    /**
     * Set the minimum peptide length.
     *
     * @param minPeptideLength the minimum peptide length
     */
    public void setMinPeptideLength(Integer minPeptideLength) {
        this.minPeptideLength = minPeptideLength;
    }

    /**
     * Returns the maximum peptide length.
     *
     * @return the the maximum peptide length
     */
    public Integer getMaxPeptideLength() {
        return maxPeptideLength;
    }

    /**
     * Set the maximum peptide length.
     *
     * @param maxPeptideLength the maximum peptide length
     */
    public void setMaxPeptideLength(Integer maxPeptideLength) {
        this.maxPeptideLength = maxPeptideLength;
    }

    /**
     * Returns the search type.
     *
     * @return the searchType
     */
    public MetaMorpheusSearchType getSearchType() {
        return searchType;
    }

    /**
     * Set the search type.
     *
     * @param searchType the searchType to set
     */
    public void setSearchType(MetaMorpheusSearchType searchType) {
        this.searchType = searchType;
    }

    /**
     * Returns total partitions.
     *
     * @return the totalPartitions
     */
    public Integer getTotalPartitions() {
        return totalPartitions;
    }

    /**
     * Set the total partitions.
     *
     * @param totalPartitions the totalPartitions to set
     */
    public void setTotalPartitions(Integer totalPartitions) {
        this.totalPartitions = totalPartitions;
    }

    /**
     * Returns the dissociation type.
     *
     * @return the dissociationType
     */
    public MetaMorpheusDissociationType getDissociationType() {
        return dissociationType;
    }

    /**
     * Set the dissociation type.
     *
     * @param dissociationType the dissociationType to set
     */
    public void setDissociationType(MetaMorpheusDissociationType dissociationType) {
        this.dissociationType = dissociationType;
    }

    /**
     * Returns the max mods for peptide.
     *
     * @return the maxModsForPeptide
     */
    public Integer getMaxModsForPeptide() {
        return maxModsForPeptide;
    }

    /**
     * Set the max mods for peptide.
     *
     * @param maxModsForPeptide the maxModsForPeptide to set
     */
    public void setMaxModsForPeptide(Integer maxModsForPeptide) {
        this.maxModsForPeptide = maxModsForPeptide;
    }

    /**
     * Returns the initiator methionine behavior.
     *
     * @return the initiatorMethionineBehavior
     */
    public MetaMorpheusInitiatorMethionineBehaviorType getInitiatorMethionineBehavior() {
        return initiatorMethionineBehavior;
    }

    /**
     * Set the initiator methionine behavior.
     *
     * @param initiatorMethionineBehavior the initiatorMethionineBehavior to set
     */
    public void setInitiatorMethionineBehavior(MetaMorpheusInitiatorMethionineBehaviorType initiatorMethionineBehavior) {
        this.initiatorMethionineBehavior = initiatorMethionineBehavior;
    }

    /**
     * Returns the score cut-off.
     *
     * @return the scoreCutoff
     */
    public Double getScoreCutoff() {
        return scoreCutoff;
    }

    /**
     * Set the score cut-off.
     *
     * @param scoreCutoff the scoreCutoff to set
     */
    public void setScoreCutoff(Double scoreCutoff) {
        this.scoreCutoff = scoreCutoff;
    }

    /**
     * Returns true of delta score is to be used.
     *
     * @return the useDeltaScore
     */
    public boolean getUseDeltaScore() {
        return useDeltaScore;
    }

    /**
     * Set whether delta score is to be used.
     *
     * @param useDeltaScore the useDeltaScore to set
     */
    public void setUseDeltaScore(boolean useDeltaScore) {
        this.useDeltaScore = useDeltaScore;
    }

    /**
     * Returns the fragmentation terminus.
     *
     * @return the fragmentationTerminus
     */
    public MetaMorpheusFragmentationTerminusType getFragmentationTerminus() {
        return fragmentationTerminus;
    }

    /**
     * Set the fragmentation terminus.
     *
     * @param fragmentationTerminus the fragmentationTerminus to set
     */
    public void setFragmentationTerminus(MetaMorpheusFragmentationTerminusType fragmentationTerminus) {
        this.fragmentationTerminus = fragmentationTerminus;
    }

    /**
     * Returns the max fragmentation size.
     *
     * @return the maxFragmentSize
     */
    public Double getMaxFragmentSize() {
        return maxFragmentSize;
    }

    /**
     * Set the max fragmentation size.
     *
     * @param maxFragmentSize the maxFragmentSize to set
     */
    public void setMaxFragmentSize(Double maxFragmentSize) {
        this.maxFragmentSize = maxFragmentSize;
    }

    /**
     * Returns the minimum allowed internal fragment length. 0 means "no
     * internal fragments".
     *
     * @return the minAllowedInternalFragmentLength
     */
    public Integer getMinAllowedInternalFragmentLength() {
        if (minAllowedInternalFragmentLength == null) {
            minAllowedInternalFragmentLength = 0;
        }
        return minAllowedInternalFragmentLength;
    }

    /**
     * Set the minimum allowed internal fragment length. 0 means "no internal
     * fragments".
     *
     * @param minAllowedInternalFragmentLength the
     * minAllowedInternalFragmentLength to set
     */
    public void setMinAllowedInternalFragmentLength(Integer minAllowedInternalFragmentLength) {
        this.minAllowedInternalFragmentLength = minAllowedInternalFragmentLength;
    }

    /**
     * Returns the mass difference acceptor type.
     *
     * @return the massDiffAcceptorType
     */
    public MetaMorpheusMassDiffAcceptorType getMassDiffAcceptorType() {
        return massDiffAcceptorType;
    }

    /**
     * Set the mass difference acceptor type.
     *
     * @param massDiffAcceptorType the massDiffAcceptorType to set
     */
    public void setMassDiffAcceptorType(MetaMorpheusMassDiffAcceptorType massDiffAcceptorType) {
        this.massDiffAcceptorType = massDiffAcceptorType;
    }

    /**
     * Returns true if mzId output is to be created.
     *
     * @return the writeMzId
     */
    public Boolean getWriteMzId() {
        return writeMzId;
    }

    /**
     * Set whether mzId output is to be created.
     *
     * @param writeMzId the writeMzId to set
     */
    public void setWriteMzId(Boolean writeMzId) {
        this.writeMzId = writeMzId;
    }

    /**
     * Returns true if pepXML output is to be created.
     *
     * @return the writePepXml
     */
    public Boolean getWritePepXml() {
        return writePepXml;
    }

    /**
     * Set whether pepXML output is to be created.
     *
     * @param writePepXml the writePepXml to set
     */
    public void setWritePepXml(Boolean writePepXml) {
        this.writePepXml = writePepXml;
    }

    /**
     * Returns true of the provided precursor info is to be used.
     *
     * @return the useProvidedPrecursorInfo
     */
    public Boolean getUseProvidedPrecursorInfo() {
        return useProvidedPrecursorInfo;
    }

    /**
     * Set whether the provided precursor info is to be used.
     *
     * @param useProvidedPrecursorInfo the useProvidedPrecursorInfo to set
     */
    public void setUseProvidedPrecursorInfo(Boolean useProvidedPrecursorInfo) {
        this.useProvidedPrecursorInfo = useProvidedPrecursorInfo;
    }

    /**
     * Returns true if precursor deconvolution is to be carried out.
     *
     * @return the doPrecursorDeconvolution
     */
    public Boolean getDoPrecursorDeconvolution() {
        return doPrecursorDeconvolution;
    }

    /**
     * Set whether precursor deconvolution is to be carried out.
     *
     * @param doPrecursorDeconvolution the doPrecursorDeconvolution to set
     */
    public void setDoPrecursorDeconvolution(Boolean doPrecursorDeconvolution) {
        this.doPrecursorDeconvolution = doPrecursorDeconvolution;
    }

    /**
     * Returns the deconvolution intensity ratio.
     *
     * @return the deconvolutionIntensityRatio
     */
    public Double getDeconvolutionIntensityRatio() {
        return deconvolutionIntensityRatio;
    }

    /**
     * Set the deconvolution intensity ratio.
     *
     * @param deconvolutionIntensityRatio the deconvolutionIntensityRatio to set
     */
    public void setDeconvolutionIntensityRatio(Double deconvolutionIntensityRatio) {
        this.deconvolutionIntensityRatio = deconvolutionIntensityRatio;
    }

    /**
     * Returns the deconvolution mass tolerance.
     *
     * @return the deconvolutionMassTolerance
     */
    public Double getDeconvolutionMassTolerance() {
        return deconvolutionMassTolerance;
    }

    /**
     * Set the deconvolution mass tolerance.
     *
     * @param deconvolutionMassTolerance the deconvolutionMassTolerance to set
     */
    public void setDeconvolutionMassTolerance(Double deconvolutionMassTolerance) {
        this.deconvolutionMassTolerance = deconvolutionMassTolerance;
    }

    /**
     * Returns the deconvolution mass tolerance type.
     *
     * @return the deconvolutionMassToleranceType
     */
    public MetaMorpheusToleranceType getDeconvolutionMassToleranceType() {
        return deconvolutionMassToleranceType;
    }

    /**
     * Set the deconvolution mass tolerance type.
     *
     * @param deconvolutionMassToleranceType the deconvolutionMassToleranceType
     * to set
     */
    public void setDeconvolutionMassToleranceType(MetaMorpheusToleranceType deconvolutionMassToleranceType) {
        this.deconvolutionMassToleranceType = deconvolutionMassToleranceType;
    }

    /**
     * Returns true if Ms1 peaks are to be trimmed.
     *
     * @return the trimMs1Peaks
     */
    public Boolean getTrimMs1Peaks() {
        return trimMs1Peaks;
    }

    /**
     * Set whether Ms1 peaks are to be trimmed.
     *
     * @param trimMs1Peaks the trimMs1Peaks to set
     */
    public void setTrimMs1Peaks(Boolean trimMs1Peaks) {
        this.trimMs1Peaks = trimMs1Peaks;
    }

    /**
     * Returns true if MsMs peaks are to be trimmed.
     *
     * @return the trimMsMsPeaks
     */
    public Boolean getTrimMsMsPeaks() {
        return trimMsMsPeaks;
    }

    /**
     * Set whether MsMs peaks are to be trimmed.
     *
     * @param trimMsMsPeaks the trimMsMsPeaks to set
     */
    public void setTrimMsMsPeaks(Boolean trimMsMsPeaks) {
        this.trimMsMsPeaks = trimMsMsPeaks;
    }

    /**
     * Return the number of peaks to keep per window.
     *
     * @return the numberOfPeaksToKeepPerWindow
     */
    public Integer getNumberOfPeaksToKeepPerWindow() {
        return numberOfPeaksToKeepPerWindow;
    }

    /**
     * Set the number of peaks to keep per window.
     *
     * @param numberOfPeaksToKeepPerWindow the numberOfPeaksToKeepPerWindow to
     * set
     */
    public void setNumberOfPeaksToKeepPerWindow(Integer numberOfPeaksToKeepPerWindow) {
        this.numberOfPeaksToKeepPerWindow = numberOfPeaksToKeepPerWindow;
    }

    /**
     * Returns the minimum allowed intensity ratio to base peak.
     *
     * @return the minAllowedIntensityRatioToBasePeak
     */
    public Double getMinAllowedIntensityRatioToBasePeak() {
        return minAllowedIntensityRatioToBasePeak;
    }

    /**
     * Set the minimum allowed intensity ratio to base peak.
     *
     * @param minAllowedIntensityRatioToBasePeak the
     * minAllowedIntensityRatioToBasePeak to set
     */
    public void setMinAllowedIntensityRatioToBasePeak(Double minAllowedIntensityRatioToBasePeak) {
        this.minAllowedIntensityRatioToBasePeak = minAllowedIntensityRatioToBasePeak;
    }

    /**
     * Returns the window width in Thomson. Null if not set.
     *
     * @return the windowWidthThomson
     */
    public Double getWindowWidthThomsons() {
        return windowWidthThomson;
    }

    /**
     * Set the window width in Thomson.
     *
     * @param windowWidthThomsons the windowWidthThomson to set
     */
    public void setWindowWidthThomsons(Double windowWidthThomsons) {
        this.windowWidthThomson = windowWidthThomsons;
    }

    /**
     * Returns the number of windows. Null if not set.
     *
     * @return the numberOfWindows
     */
    public Integer getNumberOfWindows() {
        return numberOfWindows;
    }

    /**
     * Set the number of windows.
     *
     * @param numberOfWindows the numberOfWindows to set
     */
    public void setNumberOfWindows(Integer numberOfWindows) {
        this.numberOfWindows = numberOfWindows;
    }

    /**
     * Returns the normalize peaks across all windows.
     *
     * @return the normalizePeaksAcrossAllWindows
     */
    public Boolean getNormalizePeaksAcrossAllWindows() {
        return normalizePeaksAcrossAllWindows;
    }

    /**
     * Set the normalize peaks across all windows.
     *
     * @param normalizePeaksAcrossAllWindows the normalizePeaksAcrossAllWindows
     * to set
     */
    public void setNormalizePeaksAcrossAllWindows(Boolean normalizePeaksAcrossAllWindows) {
        this.normalizePeaksAcrossAllWindows = normalizePeaksAcrossAllWindows;
    }

    /**
     * Returns true if modified peptides are considered different.
     *
     * @return the modPeptidesAreDifferent
     */
    public Boolean getModPeptidesAreDifferent() {
        return modPeptidesAreDifferent;
    }

    /**
     * Set whether modified peptides are considered different.
     *
     * @param modPeptidesAreDifferent the modPeptidesAreDifferent to set
     */
    public void setModPeptidesAreDifferent(Boolean modPeptidesAreDifferent) {
        this.modPeptidesAreDifferent = modPeptidesAreDifferent;
    }

    /**
     * Returns true if one hit wonders are excluded.
     *
     * @return the noOneHitWonders
     */
    public Boolean getNoOneHitWonders() {
        return noOneHitWonders;
    }

    /**
     * Set whether one hit wonders are to be excluded.
     *
     * @param noOneHitWonders the noOneHitWonders to set
     */
    public void setNoOneHitWonders(Boolean noOneHitWonders) {
        this.noOneHitWonders = noOneHitWonders;
    }

    /**
     * Returns true if target sequences are to be searched.
     *
     * @return the searchTarget
     */
    public Boolean getSearchTarget() {
        return searchTarget;
    }

    /**
     * Set whether target sequences are to be searched.
     *
     * @param searchTarget the searchTarget to set
     */
    public void setSearchTarget(Boolean searchTarget) {
        this.searchTarget = searchTarget;
    }

    /**
     * Returns the decoy type.
     *
     * @return the decoyType
     */
    public MetaMorpheusDecoyType getDecoyType() {
        return decoyType;
    }

    /**
     * Set the decoy type.
     *
     * @param decoyType the decoyType to set
     */
    public void setDecoyType(MetaMorpheusDecoyType decoyType) {
        this.decoyType = decoyType;
    }

    /**
     * Returns the max modification isoforms.
     *
     * @return the maxModificationIsoforms
     */
    public Integer getMaxModificationIsoforms() {
        return maxModificationIsoforms;
    }

    /**
     * Set the max modification isoforms.
     *
     * @param maxModificationIsoforms the maxModificationIsoforms to set
     */
    public void setMaxModificationIsoforms(Integer maxModificationIsoforms) {
        this.maxModificationIsoforms = maxModificationIsoforms;
    }

    /**
     * Returns the min variant depth.
     *
     * @return the minVariantDepth
     */
    public Integer getMinVariantDepth() {
        return minVariantDepth;
    }

    /**
     * Set the min variant depth.
     *
     * @param minVariantDepth the minVariantDepth to set
     */
    public void setMinVariantDepth(Integer minVariantDepth) {
        this.minVariantDepth = minVariantDepth;
    }

    /**
     * Returns the max heterozygous variants.
     *
     * @return the maxHeterozygousVariants
     */
    public Integer getMaxHeterozygousVariants() {
        return maxHeterozygousVariants;
    }

    /**
     * Set the max heterozygous variants.
     *
     * @param maxHeterozygousVariants the maxHeterozygousVariants to set
     */
    public void setMaxHeterozygousVariants(Integer maxHeterozygousVariants) {
        this.maxHeterozygousVariants = maxHeterozygousVariants;
    }

    /**
     * Returns true if the G-PTM search is to be performed.
     *
     * @return true if the G-PTM search is to be performed
     */
    public boolean runGptm() {
        return runGptm;
    }

    /**
     * Set whether the G-PTM search is to be performed.
     *
     * @param runGptm set to true if the G-PTM search is to be performed
     */
    public void setRunGptm(boolean runGptm) {
        this.runGptm = runGptm;
    }

    /**
     * Returns the modification categories to include in the G-PTM search.
     *
     * @return the modification categories to include in the G-PTM search
     */
    public ArrayList<ModificationCategory> getGPtmCategories() {
        return gPtmCategories;
    }

    /**
     * Set the modification categories to include in the G-PTM search.
     *
     * @param gPtmCategories the gPtmCategories to set
     */
    public void setGPtmCategories(ArrayList<ModificationCategory> gPtmCategories) {
        this.gPtmCategories = gPtmCategories;
    }
}