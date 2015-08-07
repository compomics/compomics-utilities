package com.compomics.util.experiment.identification.identification_parameters.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * The OMSSA specific parameters.
 *
 * @author Marc Vaudel
 */
public class OmssaParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = -6704164074092668637L;
    /**
     * Maximal e-value cut-off.
     */
    private Double maxEValue = 100.0;
    /**
     * The maximal hit list length.
     */
    private Integer hitListLength = 10;
    /**
     * The minimal charge to be considered for multiple fragment charges.
     */
    private Charge minimalChargeForMultipleChargedFragments = new Charge(Charge.PLUS, 3);
    /**
     * The minimum peptide length (for semi and non tryptic searches).
     */
    private Integer minPeptideLength = 6;
    /**
     * The maximal peptide length (for semi and non tryptic searches).
     */
    private Integer maxPeptideLength = 30;
    /**
     * Indicates whether the precursor removal option is used.
     */
    private Boolean removePrecursor = false;
    /**
     * Indicates whether the precursor scaling option is used.
     */
    private Boolean scalePrecursor = true;
    /**
     * Indicates whether the precursor charge estimation option.
     */
    private Boolean estimateCharge = true;
    /**
     * The selected output type (see omssaOutputTypes).
     */
    private String selectedOutput = "OMX";
    /**
     * The available types of output.
     */
    private static String[] omssaOutputTypes = {"OMX", "CSV", "pepXML"};
    /**
     * Map sequence in libraries in memory
     */
    private Boolean memoryMappedSequenceLibraries = false;
    /**
     * Number of isotopic peaks to consider.
     */
    private Integer numberOfItotopicPeaks = 0;
    /**
     * Mass after which a the exact mass of a neutron should be considered.
     */
    private Double neutronThreshold = 1446.94;
    /**
     * Low intensity cut-off as percentage of the most intense peak.
     */
    private Double lowIntensityCutOff = 0.0;
    /**
     * High intensity cut-off as percentage of the most intense peak.
     */
    private Double highIntensityCutOff = 0.2;
    /**
     * Intensity cut-off increment.
     */
    private Double intensityCutOffIncrement = 0.0005;
    /**
     * Window width for singly charged fragments.
     */
    private Integer singleChargeWindow = 27;
    /**
     * Window width for doubly charged fragments.
     */
    private Integer doubleChargeWindow = 14;
    /**
     * Number of peaks allowed in a singly charged window.
     */
    private Integer nPeaksInSingleChargeWindow = 2;
    /**
     * Number of peaks allowed in a doubly charged window.
     */
    private Integer nPeaksInDoubleChargeWindow = 2;
    /**
     * Maximum number of hits searched per spectrum and per charge.
     */
    private Integer maxHitsPerSpectrumPerCharge = 30;
    /**
     * Number of annotated most intense peaks required per spectrum.
     */
    private Integer nAnnotatedMostIntensePeaks = 6;
    /**
     * Minimal number of annotated peaks required per spectrum.
     */
    private Integer minAnnotatedPeaks = 2;
    /**
     * Minimal number of peaks per spectrum.
     */
    private Integer minPeaks = 4;
    /**
     * Cleave the N-term methionines.
     */
    private Boolean cleaveNtermMethionine = true;
    /**
     * Maximum length of m/z ladders.
     */
    private Integer maxMzLadders = 128;
    /**
     * Maximum fragment charge.
     */
    private Integer maxFragmentCharge = 2;
    /**
     * Fraction of peaks below the precursor to estimate charge &gt;1.
     */
    private Double fractionOfPeaksForChargeEstimation = 0.95;
    /**
     * Determine charge plus one algorithmically.
     */
    private Boolean determineChargePlusOneAlgorithmically = true;
    /**
     * Search positive ions (if false, negative ions).
     */
    private Boolean searchPositiveIons = true;
    /**
     * Minimal precursor per spectrum.
     */
    private Integer minPrecPerSpectrum = 1;
    /**
     * Search forward ions (b1) first.
     */
    private Boolean searchForwardFragmentFirst = false;
    /**
     * Search c-terminal ions.
     */
    private Boolean searchRewindFragments = true;
    /**
     * Maximal number of fragment per series.
     */
    private Integer maxFragmentPerSeries = 100;
    /**
     * Use correlation correction score.
     */
    private Boolean useCorrelationCorrectionScore = true;
    /**
     * Probability of consecutive ions.
     */
    private Double consecutiveIonProbability = 0.5;
    /**
     * E-value threshold to include a sequence in the iterative search (0 means
     * all).
     */
    private Double iterativeSequenceEvalue = 0.0;
    /**
     * E-value threshold to replace a hit in the iterative search (0 means
     * replace if better).
     */
    private Double iterativeReplaceEvalue = 0.0;
    /**
     * E-value threshold to include a spectrum in the iterative search (0 means
     * all).
     */
    private Double iterativeSpectrumEvalue = 0.01;
    /**
     * ID numbers of ion series to apply no product ions at proline rule at.
     * NOTE: not implemented for now.
     */
    private ArrayList<Integer> noProlineRuleSeries = new ArrayList<Integer>();
    /**
     * Map of the OMSSA indexes used for user modifications in this search.
     */
    private HashMap<Integer, String> ptmIndexes = new HashMap<Integer, String>();

    /**
     * Constructor.
     */
    public OmssaParameters() {

    }

    /**
     * Returns the maximal e-value searched for.
     *
     * @return the maximal e-value searched for
     */
    public Double getMaxEValue() {
        return maxEValue;
    }

    /**
     * Sets the maximal e-value searched for.
     *
     * @param maxEValue the maximal e-value searched for
     */
    public void setMaxEValue(Double maxEValue) {
        this.maxEValue = maxEValue;
    }

    /**
     * Returns the length of the hit list for OMSSA.
     *
     * @return the length of the hit list for OMSSA
     */
    public Integer getHitListLength() {
        return hitListLength;
    }

    /**
     * Sets the length of the hit list for OMSSA.
     *
     * @param hitListLength the length of the hit list for OMSSA
     */
    public void setHitListLength(Integer hitListLength) {
        this.hitListLength = hitListLength;
    }

    /**
     * Returns the minimal precursor charge to account for multiply charged
     * fragments in OMSSA.
     *
     * @return the minimal precursor charge to account for multiply charged
     * fragments in OMSSA
     */
    public Charge getMinimalChargeForMultipleChargedFragments() {
        return minimalChargeForMultipleChargedFragments;
    }

    /**
     * Sets the minimal precursor charge to account for multiply charged
     * fragments in OMSSA.
     *
     * @param minimalChargeForMultipleChargedFragments the minimal precursor
     * charge to account for multiply charged fragments in OMSSA
     */
    public void setMinimalChargeForMultipleChargedFragments(Charge minimalChargeForMultipleChargedFragments) {
        this.minimalChargeForMultipleChargedFragments = minimalChargeForMultipleChargedFragments;
    }

    /**
     * Returns the maximal peptide length allowed.
     *
     * @return the maximal peptide length allowed
     */
    public Integer getMaxPeptideLength() {
        return maxPeptideLength;
    }

    /**
     * Sets the maximal peptide length allowed.
     *
     * @param maxPeptideLength the maximal peptide length allowed
     */
    public void setMaxPeptideLength(Integer maxPeptideLength) {
        this.maxPeptideLength = maxPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @return the minimal peptide length allowed
     */
    public Integer getMinPeptideLength() {
        return minPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @param minPeptideLength the minimal peptide length allowed
     */
    public void setMinPeptideLength(Integer minPeptideLength) {
        this.minPeptideLength = minPeptideLength;
    }

    /**
     * Indicates whether the precursor charge shall be estimated for OMSSA.
     *
     * @return a boolean indicating whether the precursor charge shall be
     * estimated for OMSSA
     */
    public Boolean isEstimateCharge() {
        return estimateCharge;
    }

    /**
     * Sets whether the precursor charge shall be estimated for OMSSA.
     *
     * @param estimateCharge a boolean indicating whether the precursor charge
     * shall be estimated for OMSSA
     */
    public void setEstimateCharge(Boolean estimateCharge) {
        this.estimateCharge = estimateCharge;
    }

    /**
     * Indicates whether the precursor shall be removed for OMSSA.
     *
     * @return a boolean indicating whether the precursor shall be removed for
     * OMSSA
     */
    public Boolean isRemovePrecursor() {
        return removePrecursor;
    }

    /**
     * Sets whether the precursor shall be removed for OMSSA.
     *
     * @param removePrecursor a boolean indicating whether the precursor shall
     * be removed for OMSSA
     */
    public void setRemovePrecursor(Boolean removePrecursor) {
        this.removePrecursor = removePrecursor;
    }

    /**
     * Indicates whether the precursor shall be scaled for OMSSA.
     *
     * @return a boolean indicating whether the precursor shall be scaled for
     * OMSSA
     */
    public Boolean isScalePrecursor() {
        return scalePrecursor;
    }

    /**
     * Sets whether the precursor shall be scaled for OMSSA.
     *
     * @param scalePrecursor a boolean indicating whether the precursor shall be
     * scaled for OMSSA
     */
    public void setScalePrecursor(Boolean scalePrecursor) {
        this.scalePrecursor = scalePrecursor;
    }

    /**
     * Returns the selected output type, omx, csv or pepXML.
     *
     * @return the selected output type
     */
    public String getSelectedOutput() {
        return selectedOutput;
    }

    /**
     * Sets the output type, omx, csv or pepXML.
     *
     * @param selectedOutput the output type
     */
    public void setSelectedOutput(String selectedOutput) {
        this.selectedOutput = selectedOutput;
    }

    /**
     * Returns the output types available.
     *
     * @return the output types available
     */
    public static String[] getOmssaOutputTypes() {
        return omssaOutputTypes;
    }

    /**
     * Indicates whether sequence libraries should be mapped in memory.
     *
     * @return a boolean indicating whether sequence libraries should be mapped
     * in memory
     */
    public Boolean isMemoryMappedSequenceLibraries() {
        return memoryMappedSequenceLibraries;
    }

    /**
     * Sets whether sequence libraries should be mapped in memory.
     *
     * @param memoryMappedSequenceLibraries a boolean indicating whether
     * sequence libraries should be mapped in memory
     */
    public void setMemoryMappedSequenceLibraries(Boolean memoryMappedSequenceLibraries) {
        this.memoryMappedSequenceLibraries = memoryMappedSequenceLibraries;
    }

    /**
     * Returns the number of isotopic peaks to consider.
     *
     * @return the number of isotopic peaks to consider
     */
    public Integer getNumberOfItotopicPeaks() {
        return numberOfItotopicPeaks;
    }

    /**
     * Sets the number of isotopic peaks to consider.
     *
     * @param numberOfItotopicPeaks the number of isotopic peaks to consider
     */
    public void setNumberOfItotopicPeaks(Integer numberOfItotopicPeaks) {
        this.numberOfItotopicPeaks = numberOfItotopicPeaks;
    }

    /**
     * Returns the mass after which exact neutron mass should be considered in
     * the calculation.
     *
     * @return the mass after which exact neutron mass should be considered in
     * the calculation
     */
    public Double getNeutronThreshold() {
        return neutronThreshold;
    }

    /**
     * Sets the mass after which exact neutron mass should be considered in the
     * calculation.
     *
     * @param neutronThreshold the mass after which exact neutron mass should be
     * considered in the calculation
     */
    public void setNeutronThreshold(Double neutronThreshold) {
        this.neutronThreshold = neutronThreshold;
    }

    /**
     * Returns the low intensity cut-off as percentage of the most intense ion
     * peak.
     *
     * @return the low intensity cut-off as percentage of the most intense ion
     * peak
     */
    public Double getLowIntensityCutOff() {
        return lowIntensityCutOff;
    }

    /**
     * Sets the low intensity cut-off as percentage of the most intense ion
     * peak.
     *
     * @param lowIntensityCutOff the low intensity cut-off as percentage of the
     * most intense ion peak
     */
    public void setLowIntensityCutOff(Double lowIntensityCutOff) {
        this.lowIntensityCutOff = lowIntensityCutOff;
    }

    /**
     * Returns the high intensity cut-off as percentage of the most intense ion
     * peak.
     *
     * @return the high intensity cut-off as percentage of the most intense ion
     * peak
     */
    public Double getHighIntensityCutOff() {
        return highIntensityCutOff;
    }

    /**
     * Sets the high intensity cut-off as percentage of the most intense ion
     * peak.
     *
     * @param highIntensityCutOff the high intensity cut-off as percentage of
     * the most intense ion peak
     */
    public void setHighIntensityCutOff(Double highIntensityCutOff) {
        this.highIntensityCutOff = highIntensityCutOff;
    }

    /**
     * Returns the intensity cut-off increment.
     *
     * @return the intensity cut-off increment
     */
    public Double getIntensityCutOffIncrement() {
        return intensityCutOffIncrement;
    }

    /**
     * Sets the intensity cut-off increment.
     *
     * @param intensityCutOffIncrement the intensity cut-off increment
     */
    public void setIntensityCutOffIncrement(Double intensityCutOffIncrement) {
        this.intensityCutOffIncrement = intensityCutOffIncrement;
    }

    /**
     * Returns the window size for singly charged ions.
     *
     * @return the window size for singly charged ions
     */
    public Integer getSingleChargeWindow() {
        return singleChargeWindow;
    }

    /**
     * Sets the window size for singly charged ions.
     *
     * @param singleChargeWindow the window size for singly charged ions
     */
    public void setSingleChargeWindow(Integer singleChargeWindow) {
        this.singleChargeWindow = singleChargeWindow;
    }

    /**
     * Returns the window size for doubly charged ions.
     *
     * @return the window size for doubly charged ions
     */
    public Integer getDoubleChargeWindow() {
        return doubleChargeWindow;
    }

    /**
     * Sets the window size for doubly charged ions.
     *
     * @param doubleChargeWindow the window size for doubly charged ions
     */
    public void setDoubleChargeWindow(Integer doubleChargeWindow) {
        this.doubleChargeWindow = doubleChargeWindow;
    }

    /**
     * Returns the number of peaks in singly charged windows.
     *
     * @return the number of peaks in singly charged windows
     */
    public Integer getnPeaksInSingleChargeWindow() {
        return nPeaksInSingleChargeWindow;
    }

    /**
     * Sets the number of peaks in singly charged windows.
     *
     * @param nPeaksInSingleChargeWindow the number of peaks in singly charged
     * windows
     */
    public void setnPeaksInSingleChargeWindow(Integer nPeaksInSingleChargeWindow) {
        this.nPeaksInSingleChargeWindow = nPeaksInSingleChargeWindow;
    }

    /**
     * Returns the number of peaks in doubly charged windows.
     *
     * @return the number of peaks in doubly charged windows
     */
    public Integer getnPeaksInDoubleChargeWindow() {
        return nPeaksInDoubleChargeWindow;
    }

    /**
     * Sets the number of peaks in doubly charged windows.
     *
     * @param nPeaksInDoubleChargeWindow the number of peaks in doubly charged
     * windows
     */
    public void setnPeaksInDoubleChargeWindow(Integer nPeaksInDoubleChargeWindow) {
        this.nPeaksInDoubleChargeWindow = nPeaksInDoubleChargeWindow;
    }

    /**
     * Returns the maximal number of hits searched per spectrum and per charge.
     *
     * @return the maximal number of hits searched per spectrum and per charge
     */
    public Integer getMaxHitsPerSpectrumPerCharge() {
        return maxHitsPerSpectrumPerCharge;
    }

    /**
     * Set the maximal number of hits searched per spectrum and per charge.
     *
     * @param maxHitsPerSpectrumPerCharge the maximal number of hits searched
     * per spectrum and per charge
     */
    public void setMaxHitsPerSpectrumPerCharge(Integer maxHitsPerSpectrumPerCharge) {
        this.maxHitsPerSpectrumPerCharge = maxHitsPerSpectrumPerCharge;
    }

    /**
     * Returns the minimal number of annotated most intense peaks.
     *
     * @return the minimal number of annotated most intense peaks
     */
    public Integer getnAnnotatedMostIntensePeaks() {
        return nAnnotatedMostIntensePeaks;
    }

    /**
     * Sets the minimal number of annotated most intense peaks.
     *
     * @param nAnnotatedMostIntensePeaks the minimal number of annotated most
     * intense peaks
     */
    public void setnAnnotatedMostIntensePeaks(Integer nAnnotatedMostIntensePeaks) {
        this.nAnnotatedMostIntensePeaks = nAnnotatedMostIntensePeaks;
    }

    /**
     * Returns the minimal number of annotated peaks a peptide should have.
     *
     * @return the minimal number of annotated peaks a peptide should have
     */
    public Integer getMinAnnotatedPeaks() {
        return minAnnotatedPeaks;
    }

    /**
     * Sets the minimal number of annotated peaks a peptide should have.
     *
     * @param minAnnotatedPeaks the minimal number of annotated peaks a peptide
     * should have
     */
    public void setMinAnnotatedPeaks(Integer minAnnotatedPeaks) {
        this.minAnnotatedPeaks = minAnnotatedPeaks;
    }

    /**
     * Returns the minimal number of peaks a spectrum should contain.
     *
     * @return the minimal number of peaks a spectrum should contain
     */
    public Integer getMinPeaks() {
        return minPeaks;
    }

    /**
     * Sets the minimal number of peaks a spectrum should contain.
     *
     * @param minPeaks the minimal number of peaks a spectrum should contain
     */
    public void setMinPeaks(Integer minPeaks) {
        this.minPeaks = minPeaks;
    }

    /**
     * Indicates whether N-terminal methionines should be cleaved.
     *
     * @return a boolean indicating whether N-terminal methionines should be
     * cleaved
     */
    public Boolean isCleaveNterMethionine() {
        return cleaveNtermMethionine;
    }

    /**
     * Sets whether N-terminal methionines should be cleaved.
     *
     * @param cleaveNterMethionine whether N-terminal methionines should be
     * cleaved
     */
    public void setCleaveNterMethionine(Boolean cleaveNterMethionine) {
        this.cleaveNtermMethionine = cleaveNterMethionine;
    }

    /**
     * Returns the maximal m/z ladder length.
     *
     * @return the maximal m/z ladder length
     */
    public Integer getMaxMzLadders() {
        return maxMzLadders;
    }

    /**
     * Sets the maximal m/z ladder length.
     *
     * @param maxMzLadders the maximal m/z ladder length
     */
    public void setMaxMzLadders(Integer maxMzLadders) {
        this.maxMzLadders = maxMzLadders;
    }

    /**
     * Returns the maximal fragment charge.
     *
     * @return the maximal fragment charge
     */
    public Integer getMaxFragmentCharge() {
        return maxFragmentCharge;
    }

    /**
     * Sets the maximal fragment charge.
     *
     * @param maxFragmentCharge the maximal fragment charge
     */
    public void setMaxFragmentCharge(Integer maxFragmentCharge) {
        this.maxFragmentCharge = maxFragmentCharge;
    }

    /**
     * Returns the fraction of peaks to be retained for charge &gt;1 estimation.
     *
     * @return the fraction of peaks to be retained for charge &gt;1 estimation
     */
    public Double getFractionOfPeaksForChargeEstimation() {
        return fractionOfPeaksForChargeEstimation;
    }

    /**
     * Sets the fraction of peaks to be retained for charge &gt;1 estimation.
     *
     * @param fractionOfPeaksForChargeEstimation the fraction of peaks to be
     * retained for charge &gt;1 estimation
     */
    public void setFractionOfPeaksForChargeEstimation(Double fractionOfPeaksForChargeEstimation) {
        this.fractionOfPeaksForChargeEstimation = fractionOfPeaksForChargeEstimation;
    }

    /**
     * Indicates whether charge plus one should be determined algorithmically.
     *
     * @return whether charge plus one should be determined algorithmically
     */
    public Boolean isDetermineChargePlusOneAlgorithmically() {
        return determineChargePlusOneAlgorithmically;
    }

    /**
     * Sets whether charge plus one should be determined algorithmically.
     *
     * @param determineChargePlusOneAlgorithmically whether charge plus one
     * should be determined algorithmically
     */
    public void setDetermineChargePlusOneAlgorithmically(Boolean determineChargePlusOneAlgorithmically) {
        this.determineChargePlusOneAlgorithmically = determineChargePlusOneAlgorithmically;
    }

    /**
     * Indicates whether positive ions are searched. False means negative ions.
     *
     * @return a boolean indicating whether positive ions are searched
     */
    public Boolean isSearchPositiveIons() {
        return searchPositiveIons;
    }

    /**
     * Sets whether positive ions are searched. False means negative ions.
     *
     * @param searchPositiveIons a boolean indicating whether positive ions are
     * searched
     */
    public void setSearchPositiveIons(Boolean searchPositiveIons) {
        this.searchPositiveIons = searchPositiveIons;
    }

    /**
     * Returns the minimal number of precursors per spectrum.
     *
     * @return the minimal number of precursors per spectrum
     */
    public Integer getMinPrecPerSpectrum() {
        return minPrecPerSpectrum;
    }

    /**
     * Sets the minimal number of precursors per spectrum.
     *
     * @param minPrecPerSpectrum the minimal number of precursors per spectrum
     */
    public void setMinPrecPerSpectrum(Integer minPrecPerSpectrum) {
        this.minPrecPerSpectrum = minPrecPerSpectrum;
    }

    /**
     * Indicates whether forward ions (b1) should be searched first.
     *
     * @return a boolean indicating whether forward ions (b1) should be searched
     * first
     */
    public Boolean isSearchForwardFragmentFirst() {
        return searchForwardFragmentFirst;
    }

    /**
     * Sets whether forward ions (b1) should be searched first.
     *
     * @param searchForwardFragmentFirst whether forward ions (b1) should be
     * searched first
     */
    public void setSearchForwardFragmentFirst(Boolean searchForwardFragmentFirst) {
        this.searchForwardFragmentFirst = searchForwardFragmentFirst;
    }

    /**
     * Indicates whether C-terminal fragments should be searched.
     *
     * @return a boolean indicating whether C-terminal fragments should be
     * searched
     */
    public Boolean isSearchRewindFragments() {
        return searchRewindFragments;
    }

    /**
     * Sets whether C-terminal fragments should be searched.
     *
     * @param searchRewindFragments whether C-terminal fragments should be
     * searched
     */
    public void setSearchRewindFragments(Boolean searchRewindFragments) {
        this.searchRewindFragments = searchRewindFragments;
    }

    /**
     * Returns the maximal number of fragments to retain per series.
     *
     * @return the maximal number of fragments to retain per series
     */
    public Integer getMaxFragmentPerSeries() {
        return maxFragmentPerSeries;
    }

    /**
     * Sets the maximal number of fragments to retain per series.
     *
     * @param maxFragmentPerSeries the maximal number of fragments to retain per
     * series
     */
    public void setMaxFragmentPerSeries(Integer maxFragmentPerSeries) {
        this.maxFragmentPerSeries = maxFragmentPerSeries;
    }

    /**
     * Indicates whether the correlation correction score should be used.
     *
     * @return a boolean indicating whether the correlation correction score
     * should be used
     */
    public Boolean isUseCorrelationCorrectionScore() {
        return useCorrelationCorrectionScore;
    }

    /**
     * Sets whether the correlation correction score should be used.
     *
     * @param useCorrelationCorrectionScore a boolean indicating whether the
     * correlation correction score should be used
     */
    public void setUseCorrelationCorrectionScore(Boolean useCorrelationCorrectionScore) {
        this.useCorrelationCorrectionScore = useCorrelationCorrectionScore;
    }

    /**
     * Returns the consecutive ion probability.
     *
     * @return the consecutive ion probability
     */
    public Double getConsecutiveIonProbability() {
        return consecutiveIonProbability;
    }

    /**
     * Set the consecutive ion probability.
     *
     * @param consecutiveIonProbability the consecutive ion probability
     */
    public void setConsecutiveIonProbability(Double consecutiveIonProbability) {
        this.consecutiveIonProbability = consecutiveIonProbability;
    }

    /**
     * Returns the e-value threshold to use to consider a sequence for the
     * iterative search. 0.0 means all.
     *
     * @return the e-value threshold to use to consider a sequence for the
     * iterative search
     */
    public Double getIterativeSequenceEvalue() {
        return iterativeSequenceEvalue;
    }

    /**
     * Sets the e-value threshold to use to consider a sequence for the
     * iterative search. 0.0 means all.
     *
     * @param iterativeSequenceEvalue the e-value threshold to use to consider a
     * sequence for the iterative search
     */
    public void setIterativeSequenceEvalue(Double iterativeSequenceEvalue) {
        this.iterativeSequenceEvalue = iterativeSequenceEvalue;
    }

    /**
     * Returns the e-value threshold to use to replace a hit for the iterative
     * search. 0.0 means the best hit will be retained.
     *
     * @return the e-value threshold to use to replace a hit for the iterative
     * search
     */
    public Double getIterativeReplaceEvalue() {
        return iterativeReplaceEvalue;
    }

    /**
     * Sets the e-value threshold to use to replace a hit for the iterative
     * search. 0.0 means the best hit will be retained.
     *
     * @param iterativeReplaceEvalue the e-value threshold to use to replace a
     * hit for the iterative search
     */
    public void setIterativeReplaceEvalue(Double iterativeReplaceEvalue) {
        this.iterativeReplaceEvalue = iterativeReplaceEvalue;
    }

    /**
     * Returns the e-value threshold to use consider a spectrum for the
     * iterative search. 0.0 means all.
     *
     * @return the e-value threshold to use consider a spectrum for the
     * iterative search
     */
    public Double getIterativeSpectrumEvalue() {
        return iterativeSpectrumEvalue;
    }

    /**
     * Sets the e-value threshold to use consider a spectrum for the iterative
     * search. 0.0 means all.
     *
     * @param iterativeSpectrumEvalue the e-value threshold to use consider a
     * spectrum for the iterative search
     */
    public void setIterativeSpectrumEvalue(Double iterativeSpectrumEvalue) {
        this.iterativeSpectrumEvalue = iterativeSpectrumEvalue;
    }

    /**
     * Returns the id numbers of ion series to apply no product ions at proline
     * rule at.
     *
     * @return the id numbers of ion series to apply no product ions at proline
     * rule at
     */
    public ArrayList<Integer> getNoProlineRuleSeries() {
        return noProlineRuleSeries;
    }

    /**
     * Sets the id numbers of ion series to apply no product ions at proline
     * rule at.
     *
     * @param noProlineRuleSeries the id numbers of ion series to apply no
     * product ions at proline rule at
     */
    public void setNoProlineRuleSeries(ArrayList<Integer> noProlineRuleSeries) {
        this.noProlineRuleSeries = noProlineRuleSeries;
    }

    /**
     * Sets the index for a given modification. If another modification
     * was already given with the same index the previous setting will be
     * silently overwritten.
     *
     * @param modificationName the name of the modification
     * @param ptmIndex the index of the modification
     */
    public void setPtmIndex(String modificationName, int ptmIndex) {
        ptmIndexes.put(ptmIndex, modificationName);
    }

    /**
     * Returns the name of the modification indexed by the given index.
     * Null if not found.
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
     * Set the PTM indexes of the modifications searched.
     * 
     * @param modificationProfile the modification profile of this search
     */
    public void setPtmIndexes(PtmSettings modificationProfile) {
        ptmIndexes.clear();
        int rank = 1;
        for (String ptm : modificationProfile.getAllModifications()) {
            int omssaIndex = rank + 118;
            if (omssaIndex > 128) {
                omssaIndex += 13;
            }
            setPtmIndex(ptm, omssaIndex);
            rank++;
        }
    }

    /**
     * Imports the OMSSA indexes from an XML file.
     *
     * @param modificationsFile the modification file
     * 
     * @return a map of all indexes: modification name &gt; OMSSA index
     * @throws XmlPullParserException if an XmlPullParserException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public static HashMap<String, Integer> getOMSSAIndexes(File modificationsFile) throws XmlPullParserException, FileNotFoundException, IOException {

        HashMap<String, Integer> indexes = new HashMap<String, Integer>();

        // Create the pull parser.
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        // Create a reader for the input file.
        BufferedReader br = new BufferedReader(new FileReader(modificationsFile));
        // Set the XML Pull Parser to read from this reader.
        parser.setInput(br);
        // Start the parsing.
        int type = parser.next();
        Integer number = null;
        // Go through the whole document.
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG && parser.getName().equals("MSMod")) {
                parser.next();
                String numberString = parser.getText();
                try {
                    number = new Integer(numberString);
                } catch (NumberFormatException nfe) {
                    throw new XmlPullParserException("Found non-parseable text '" + numberString
                            + "' for the value of the 'MSMod' tag on line " + parser.getLineNumber() + ".");
                }
            }
            if (type == XmlPullParser.START_TAG && parser.getName().equals("MSModSpec_name")) {
                parser.next();
                String name = parser.getText();
                if (number != null) {
                    indexes.put(name, number);
                }
            }
            type = parser.next();
        }
        br.close();

        return indexes;
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.omssa;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof OmssaParameters) {
            OmssaParameters omssaParameters = (OmssaParameters) identificationAlgorithmParameter;
            if (!getMaxEValue().equals(omssaParameters.getMaxEValue())) {
                return false;
            }
            if (!getHitListLength().equals(omssaParameters.getHitListLength())) {
                return false;
            }
            if (!getMaxPeptideLength().equals(omssaParameters.getMaxPeptideLength())) {
                return false;
            }
            if (!getMinPeptideLength().equals(omssaParameters.getMinPeptideLength())) {
                return false;
            }
            if (!getMinimalChargeForMultipleChargedFragments().equals(omssaParameters.getMinimalChargeForMultipleChargedFragments())) {
                return false;
            }
            if (!isEstimateCharge().equals(omssaParameters.isEstimateCharge())) {
                return false;
            }
            if (!isRemovePrecursor().equals(omssaParameters.isRemovePrecursor())) {
                return false;
            }
            if (!isScalePrecursor().equals(omssaParameters.isScalePrecursor())) {
                return false;
            }
            if (!getSelectedOutput().equals(omssaParameters.getSelectedOutput())) {
                return false;
            }
            if (!isMemoryMappedSequenceLibraries().equals(omssaParameters.isMemoryMappedSequenceLibraries())) {
                return false;
            }
            if (!getNumberOfItotopicPeaks().equals(omssaParameters.getNumberOfItotopicPeaks())) {
                return false;
            }
            double diff = Math.abs(neutronThreshold - omssaParameters.getNeutronThreshold());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!getLowIntensityCutOff().equals(omssaParameters.getLowIntensityCutOff())) {
                return false;
            }
            if (!getHighIntensityCutOff().equals(omssaParameters.getHighIntensityCutOff())) {
                return false;
            }
            if (!getIntensityCutOffIncrement().equals(omssaParameters.getIntensityCutOffIncrement())) {
                return false;
            }
            if (!getSingleChargeWindow().equals(omssaParameters.getSingleChargeWindow())) {
                return false;
            }
            if (!getDoubleChargeWindow().equals(omssaParameters.getDoubleChargeWindow())) {
                return false;
            }
            if (!getnPeaksInSingleChargeWindow().equals(omssaParameters.getnPeaksInSingleChargeWindow())) {
                return false;
            }
            if (!getnPeaksInDoubleChargeWindow().equals(omssaParameters.getnPeaksInDoubleChargeWindow())) {
                return false;
            }
            if (!getMaxHitsPerSpectrumPerCharge().equals(omssaParameters.getMaxHitsPerSpectrumPerCharge())) {
                return false;
            }
            if (!getnAnnotatedMostIntensePeaks().equals(omssaParameters.getnAnnotatedMostIntensePeaks())) {
                return false;
            }
            if (!getMinAnnotatedPeaks().equals(omssaParameters.getMinAnnotatedPeaks())) {
                return false;
            }
            if (!getMinPeaks().equals(omssaParameters.getMinPeaks())) {
                return false;
            }
            if (!isCleaveNterMethionine().equals(omssaParameters.isCleaveNterMethionine())) {
                return false;
            }
            if (!getMaxMzLadders().equals(omssaParameters.getMaxMzLadders())) {
                return false;
            }
            if (!getMaxFragmentCharge().equals(omssaParameters.getMaxFragmentCharge())) {
                return false;
            }
            diff = Math.abs(fractionOfPeaksForChargeEstimation - omssaParameters.getFractionOfPeaksForChargeEstimation());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!isDetermineChargePlusOneAlgorithmically().equals(omssaParameters.isDetermineChargePlusOneAlgorithmically())) {
                return false;
            }
            if (!isSearchPositiveIons().equals(omssaParameters.isSearchPositiveIons())) {
                return false;
            }
            if (!isSearchRewindFragments().equals(omssaParameters.isSearchRewindFragments())) {
                return false;
            }
            if (!getMaxFragmentPerSeries().equals(omssaParameters.getMaxFragmentPerSeries())) {
                return false;
            }
            if (!isUseCorrelationCorrectionScore().equals(omssaParameters.isUseCorrelationCorrectionScore())) {
                return false;
            }
            diff = Math.abs(consecutiveIonProbability - omssaParameters.getConsecutiveIonProbability());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(iterativeSequenceEvalue - omssaParameters.getIterativeSequenceEvalue());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(iterativeSpectrumEvalue - omssaParameters.getIterativeSpectrumEvalue());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(iterativeReplaceEvalue - omssaParameters.getIterativeReplaceEvalue());
            if (diff > 0.0000000000001) {
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

        output.append("EVALUE_CUTOFF=");
        output.append(maxEValue);
        output.append(newLine);

        output.append("MAXIMUM_HITLIST_LENGTH=");
        output.append(hitListLength);
        output.append(newLine);

        output.append("PRECURSOR_ELIMINATION=");
        output.append(removePrecursor);
        output.append(newLine);

        output.append("PRECURSOR_SCALING=");
        output.append(scalePrecursor);
        output.append(newLine);

        output.append("MINIMAL_PEPTIDE_SIZE=");
        output.append(minPeptideLength);
        output.append(newLine);

        output.append("MAXIMAL_PEPTIDE_SIZE=");
        output.append(maxPeptideLength);
        output.append(newLine);

        output.append("PRECURSOR_CHARGE_TO_CONSIDER_MULTIPLY_CHARGED_FRAGMENTS=");
        output.append(minimalChargeForMultipleChargedFragments);
        output.append(newLine);

        output.append("CHARGE_ESTIMATION=");
        output.append(estimateCharge);
        output.append(newLine);

        output.append("OUTPUT_TYPE=");
        output.append(selectedOutput);
        output.append(newLine);

        output.append("MEMORY_MAPPED_SEQUENCES=");
        output.append(memoryMappedSequenceLibraries);
        output.append(newLine);

        output.append("NUMBER_OF_ISOTOPES=");
        output.append(numberOfItotopicPeaks);
        output.append(newLine);

        output.append("MASS_TO_CONSIDER_EXACT_NEUTRON_MASS=");
        output.append(neutronThreshold);
        output.append(newLine);

        output.append("LOW_INTENSITY_CUTOFF=");
        output.append(lowIntensityCutOff);
        output.append(newLine);

        output.append("HIGH_INTENSITY_CUTOFF=");
        output.append(highIntensityCutOff);
        output.append(newLine);

        output.append("INTENSITY_INCREMENT=");
        output.append(intensityCutOffIncrement);
        output.append(newLine);

        output.append("SINGLE_CHARGE_WINDOW_WIDTH=");
        output.append(singleChargeWindow);
        output.append(newLine);

        output.append("DOUBLE_CHARGE_WINDOW_WIDTH=");
        output.append(doubleChargeWindow);
        output.append(newLine);

        output.append("SINGLE_CHARGE_WINDOW_NPEAKS=");
        output.append(nPeaksInSingleChargeWindow);
        output.append(newLine);

        output.append("DOUBLE_CHARGE_WINDOW_NPEAKS=");
        output.append(nPeaksInDoubleChargeWindow);
        output.append(newLine);

        output.append("MAX_HITS_PER_SPECTRUM_PER_CHARGE=");
        output.append(maxHitsPerSpectrumPerCharge);
        output.append(newLine);

        output.append("MIN_ANNOTATED_INTENSE_PEAKS=");
        output.append(nAnnotatedMostIntensePeaks);
        output.append(newLine);

        output.append("MIN_ANNOTATED_PEAKS=");
        output.append(minAnnotatedPeaks);
        output.append(newLine);

        output.append("MIN_PEAKS=");
        output.append(minPeaks);
        output.append(newLine);

        output.append("CLEAVE_TERMINAL_METHIONINE=");
        output.append(cleaveNtermMethionine);
        output.append(newLine);

        output.append("MAX_MZ_LADDERS=");
        output.append(maxMzLadders);
        output.append(newLine);

        output.append("MAX_FRAGMENT_CHARGE=");
        output.append(maxFragmentCharge);
        output.append(newLine);

        output.append("FRACTION_PEAKS_FOR_CHARGE_1_ESTIMATION=");
        output.append(fractionOfPeaksForChargeEstimation);
        output.append(newLine);

        output.append("AUTO_DETERMINE_CHARGE_ONE=");
        output.append(determineChargePlusOneAlgorithmically);
        output.append(newLine);

        output.append("SEARCH_POSITIVE_IONS=");
        output.append(searchPositiveIons);
        output.append(newLine);

        output.append("MIN_PERCURSORS_PER_SPECTRUM=");
        output.append(minPrecPerSpectrum);
        output.append(newLine);

        output.append("FORWARD_FRAGMENTS_FIRST=");
        output.append(searchForwardFragmentFirst);
        output.append(newLine);

        output.append("REWIND_FRAGMENTS=");
        output.append(searchRewindFragments);
        output.append(newLine);

        output.append("FRAGMENTS_PER_SERIES=");
        output.append(maxFragmentPerSeries);
        output.append(newLine);

        output.append("CORRELATION_CORRECTION_SCORE=");
        output.append(useCorrelationCorrectionScore);
        output.append(newLine);

        output.append("CONSECUTIVE_ION_PROBABILITY=");
        output.append(consecutiveIonProbability);
        output.append(newLine);

        output.append("ITERATIVE_SEARCH_SEQUENCE_EVALUE=");
        output.append(iterativeSequenceEvalue);
        output.append(newLine);

        output.append("ITERATIVE_SEARCH_SPECTRUM_EVALUE=");
        output.append(iterativeSpectrumEvalue);
        output.append(newLine);

        output.append("ITERATIVE_SEARCH_REPLACEMENT_EVALUE=");
        output.append(iterativeReplaceEvalue);
        output.append(newLine);

        return output.toString();
    }
}
