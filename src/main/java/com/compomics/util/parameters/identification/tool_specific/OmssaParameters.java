package com.compomics.util.parameters.identification.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;
import com.compomics.util.parameters.identification.search.PtmSettings;
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
    private double maxEValue = 100.0;
    /**
     * The maximal hit list length.
     */
    private int hitListLength = 10;
    /**
     * The minimal charge to be considered for multiple fragment charges.
     */
    private int minimalChargeForMultipleChargedFragments = 3;
    /**
     * The minimum peptide length (for semi and non tryptic searches).
     */
    private int minPeptideLength = 8;
    /**
     * The maximal peptide length (for semi and non tryptic searches).
     */
    private int maxPeptideLength = 30;
    /**
     * Indicates whether the precursor removal option is used.
     */
    private boolean removePrecursor = false;
    /**
     * Indicates whether the precursor scaling option is used.
     */
    private boolean scalePrecursor = true;
    /**
     * Indicates whether the precursor charge estimation option.
     */
    private boolean estimateCharge = true;
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
    private boolean memoryMappedSequenceLibraries = false;
    /**
     * Mass after which a the exact mass of a neutron should be considered.
     */
    private double neutronThreshold = 1446.94;
    /**
     * Low intensity cut-off as percentage of the most intense peak.
     */
    private double lowIntensityCutOff = 0.0;
    /**
     * High intensity cut-off as percentage of the most intense peak.
     */
    private double highIntensityCutOff = 0.2;
    /**
     * Intensity cut-off increment.
     */
    private double intensityCutOffIncrement = 0.0005;
    /**
     * Window width for singly charged fragments.
     */
    private int singleChargeWindow = 27;
    /**
     * Window width for doubly charged fragments.
     */
    private int doubleChargeWindow = 14;
    /**
     * Number of peaks allowed in a singly charged window.
     */
    private int nPeaksInSingleChargeWindow = 2;
    /**
     * Number of peaks allowed in a doubly charged window.
     */
    private int nPeaksIndoubleChargeWindow = 2;
    /**
     * Maximum number of hits searched per spectrum and per charge.
     */
    private int maxHitsPerSpectrumPerCharge = 30;
    /**
     * Number of annotated most intense peaks required per spectrum.
     */
    private int nAnnotatedMostIntensePeaks = 6;
    /**
     * Minimal number of annotated peaks required per spectrum.
     */
    private int minAnnotatedPeaks = 2;
    /**
     * Minimal number of peaks per spectrum.
     */
    private int minPeaks = 4;
    /**
     * Cleave the N-term methionines.
     */
    private boolean cleaveNtermMethionine = true;
    /**
     * Maximum length of m/z ladders.
     */
    private int maxMzLadders = 128;
    /**
     * Maximum fragment charge.
     */
    private int maxFragmentCharge = 2;
    /**
     * Fraction of peaks below the precursor to estimate charge &gt;1.
     */
    private double fractionOfPeaksForChargeEstimation = 0.95;
    /**
     * Determine charge plus one algorithmically.
     */
    private boolean determineChargePlusOneAlgorithmically = true;
    /**
     * Search positive ions (if false, negative ions).
     */
    private boolean searchPositiveIons = true;
    /**
     * Minimal precursor per spectrum.
     */
    private int minPrecPerSpectrum = 1;
    /**
     * Search forward ions (b1) first.
     */
    private boolean searchForwardFragmentFirst = false;
    /**
     * Search c-terminal ions.
     */
    private boolean searchRewindFragments = true;
    /**
     * Maximal number of fragment per series.
     */
    private int maxFragmentPerSeries = 100;
    /**
     * Use correlation correction score.
     */
    private boolean useCorrelationCorrectionScore = true;
    /**
     * Probability of consecutive ions.
     */
    private double consecutiveIonProbability = 0.5;
    /**
     * E-value threshold to include a sequence in the iterative search (0 means
     * all).
     */
    private double iterativeSequenceEvalue = 0.0;
    /**
     * E-value threshold to replace a hit in the iterative search (0 means
     * replace if better).
     */
    private double iterativeReplaceEvalue = 0.0;
    /**
     * E-value threshold to include a spectrum in the iterative search (0 means
     * all).
     */
    private double iterativeSpectrumEvalue = 0.01;
    /**
     * ID numbers of ion series to apply no product ions at proline rule at.
     * NOTE: not implemented for now.
     */
    private ArrayList<Integer> noProlineRuleSeries = new ArrayList<>();
    /**
     * Map of the OMSSA indexes used for user modifications in this search.
     */
    private HashMap<Integer, String> ptmIndexes = new HashMap<>();

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
    public double getMaxEValue() {
        return maxEValue;
    }

    /**
     * Sets the maximal e-value searched for.
     *
     * @param maxEValue the maximal e-value searched for
     */
    public void setMaxEValue(double maxEValue) {
        this.maxEValue = maxEValue;
    }

    /**
     * Returns the length of the hit list for OMSSA.
     *
     * @return the length of the hit list for OMSSA
     */
    public int getHitListLength() {
        return hitListLength;
    }

    /**
     * Sets the length of the hit list for OMSSA.
     *
     * @param hitListLength the length of the hit list for OMSSA
     */
    public void setHitListLength(int hitListLength) {
        this.hitListLength = hitListLength;
    }

    /**
     * Returns the minimal precursor charge to account for multiply charged
     * fragments in OMSSA.
     *
     * @return the minimal precursor charge to account for multiply charged
     * fragments in OMSSA
     */
    public int getMinimalChargeForMultipleChargedFragments() {
        return minimalChargeForMultipleChargedFragments;
    }

    /**
     * Sets the minimal precursor charge to account for multiply charged
     * fragments in OMSSA.
     *
     * @param minimalChargeForMultipleChargedFragments the minimal precursor
     * charge to account for multiply charged fragments in OMSSA
     */
    public void setMinimalChargeForMultipleChargedFragments(int minimalChargeForMultipleChargedFragments) {
        this.minimalChargeForMultipleChargedFragments = minimalChargeForMultipleChargedFragments;
    }

    /**
     * Returns the maximal peptide length allowed.
     *
     * @return the maximal peptide length allowed
     */
    public int getMaxPeptideLength() {
        return maxPeptideLength;
    }

    /**
     * Sets the maximal peptide length allowed.
     *
     * @param maxPeptideLength the maximal peptide length allowed
     */
    public void setMaxPeptideLength(int maxPeptideLength) {
        this.maxPeptideLength = maxPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @return the minimal peptide length allowed
     */
    public int getMinPeptideLength() {
        return minPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @param minPeptideLength the minimal peptide length allowed
     */
    public void setMinPeptideLength(int minPeptideLength) {
        this.minPeptideLength = minPeptideLength;
    }

    /**
     * Indicates whether the precursor charge shall be estimated for OMSSA.
     *
     * @return a boolean indicating whether the precursor charge shall be
     * estimated for OMSSA
     */
    public boolean isEstimateCharge() {
        return estimateCharge;
    }

    /**
     * Sets whether the precursor charge shall be estimated for OMSSA.
     *
     * @param estimateCharge a boolean indicating whether the precursor charge
     * shall be estimated for OMSSA
     */
    public void setEstimateCharge(boolean estimateCharge) {
        this.estimateCharge = estimateCharge;
    }

    /**
     * Indicates whether the precursor shall be removed for OMSSA.
     *
     * @return a boolean indicating whether the precursor shall be removed for
     * OMSSA
     */
    public boolean isRemovePrecursor() {
        return removePrecursor;
    }

    /**
     * Sets whether the precursor shall be removed for OMSSA.
     *
     * @param removePrecursor a boolean indicating whether the precursor shall
     * be removed for OMSSA
     */
    public void setRemovePrecursor(boolean removePrecursor) {
        this.removePrecursor = removePrecursor;
    }

    /**
     * Indicates whether the precursor shall be scaled for OMSSA.
     *
     * @return a boolean indicating whether the precursor shall be scaled for
     * OMSSA
     */
    public boolean isScalePrecursor() {
        return scalePrecursor;
    }

    /**
     * Sets whether the precursor shall be scaled for OMSSA.
     *
     * @param scalePrecursor a boolean indicating whether the precursor shall be
     * scaled for OMSSA
     */
    public void setScalePrecursor(boolean scalePrecursor) {
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
    public boolean isMemoryMappedSequenceLibraries() {
        return memoryMappedSequenceLibraries;
    }

    /**
     * Sets whether sequence libraries should be mapped in memory.
     *
     * @param memoryMappedSequenceLibraries a boolean indicating whether
     * sequence libraries should be mapped in memory
     */
    public void setMemoryMappedSequenceLibraries(boolean memoryMappedSequenceLibraries) {
        this.memoryMappedSequenceLibraries = memoryMappedSequenceLibraries;
    }

    /**
     * Returns the mass after which exact neutron mass should be considered in
     * the calculation.
     *
     * @return the mass after which exact neutron mass should be considered in
     * the calculation
     */
    public double getNeutronThreshold() {
        return neutronThreshold;
    }

    /**
     * Sets the mass after which exact neutron mass should be considered in the
     * calculation.
     *
     * @param neutronThreshold the mass after which exact neutron mass should be
     * considered in the calculation
     */
    public void setNeutronThreshold(double neutronThreshold) {
        this.neutronThreshold = neutronThreshold;
    }

    /**
     * Returns the low intensity cut-off as percentage of the most intense ion
     * peak.
     *
     * @return the low intensity cut-off as percentage of the most intense ion
     * peak
     */
    public double getLowIntensityCutOff() {
        return lowIntensityCutOff;
    }

    /**
     * Sets the low intensity cut-off as percentage of the most intense ion
     * peak.
     *
     * @param lowIntensityCutOff the low intensity cut-off as percentage of the
     * most intense ion peak
     */
    public void setLowIntensityCutOff(double lowIntensityCutOff) {
        this.lowIntensityCutOff = lowIntensityCutOff;
    }

    /**
     * Returns the high intensity cut-off as percentage of the most intense ion
     * peak.
     *
     * @return the high intensity cut-off as percentage of the most intense ion
     * peak
     */
    public double getHighIntensityCutOff() {
        return highIntensityCutOff;
    }

    /**
     * Sets the high intensity cut-off as percentage of the most intense ion
     * peak.
     *
     * @param highIntensityCutOff the high intensity cut-off as percentage of
     * the most intense ion peak
     */
    public void setHighIntensityCutOff(double highIntensityCutOff) {
        this.highIntensityCutOff = highIntensityCutOff;
    }

    /**
     * Returns the intensity cut-off increment.
     *
     * @return the intensity cut-off increment
     */
    public double getIntensityCutOffIncrement() {
        return intensityCutOffIncrement;
    }

    /**
     * Sets the intensity cut-off increment.
     *
     * @param intensityCutOffIncrement the intensity cut-off increment
     */
    public void setIntensityCutOffIncrement(double intensityCutOffIncrement) {
        this.intensityCutOffIncrement = intensityCutOffIncrement;
    }

    /**
     * Returns the window size for singly charged ions.
     *
     * @return the window size for singly charged ions
     */
    public int getSingleChargeWindow() {
        return singleChargeWindow;
    }

    /**
     * Sets the window size for singly charged ions.
     *
     * @param singleChargeWindow the window size for singly charged ions
     */
    public void setSingleChargeWindow(int singleChargeWindow) {
        this.singleChargeWindow = singleChargeWindow;
    }

    /**
     * Returns the window size for doubly charged ions.
     *
     * @return the window size for doubly charged ions
     */
    public int getDoubleChargeWindow() {
        return doubleChargeWindow;
    }

    /**
     * Sets the window size for doubly charged ions.
     *
     * @param doubleChargeWindow the window size for doubly charged ions
     */
    public void setDoubleChargeWindow(int doubleChargeWindow) {
        this.doubleChargeWindow = doubleChargeWindow;
    }

    /**
     * Returns the number of peaks in singly charged windows.
     *
     * @return the number of peaks in singly charged windows
     */
    public int getnPeaksInSingleChargeWindow() {
        return nPeaksInSingleChargeWindow;
    }

    /**
     * Sets the number of peaks in singly charged windows.
     *
     * @param nPeaksInSingleChargeWindow the number of peaks in singly charged
     * windows
     */
    public void setnPeaksInSingleChargeWindow(int nPeaksInSingleChargeWindow) {
        this.nPeaksInSingleChargeWindow = nPeaksInSingleChargeWindow;
    }

    /**
     * Returns the number of peaks in doubly charged windows.
     *
     * @return the number of peaks in doubly charged windows
     */
    public int getnPeaksInDoubleChargeWindow() {
        return nPeaksIndoubleChargeWindow;
    }

    /**
     * Sets the number of peaks in doubly charged windows.
     *
     * @param nPeaksIndoubleChargeWindow the number of peaks in doubly charged
     * windows
     */
    public void setnPeaksInDoubleChargeWindow(int nPeaksIndoubleChargeWindow) {
        this.nPeaksIndoubleChargeWindow = nPeaksIndoubleChargeWindow;
    }

    /**
     * Returns the maximal number of hits searched per spectrum and per charge.
     *
     * @return the maximal number of hits searched per spectrum and per charge
     */
    public int getMaxHitsPerSpectrumPerCharge() {
        return maxHitsPerSpectrumPerCharge;
    }

    /**
     * Set the maximal number of hits searched per spectrum and per charge.
     *
     * @param maxHitsPerSpectrumPerCharge the maximal number of hits searched
     * per spectrum and per charge
     */
    public void setMaxHitsPerSpectrumPerCharge(int maxHitsPerSpectrumPerCharge) {
        this.maxHitsPerSpectrumPerCharge = maxHitsPerSpectrumPerCharge;
    }

    /**
     * Returns the minimal number of annotated most intense peaks.
     *
     * @return the minimal number of annotated most intense peaks
     */
    public int getnAnnotatedMostIntensePeaks() {
        return nAnnotatedMostIntensePeaks;
    }

    /**
     * Sets the minimal number of annotated most intense peaks.
     *
     * @param nAnnotatedMostIntensePeaks the minimal number of annotated most
     * intense peaks
     */
    public void setnAnnotatedMostIntensePeaks(int nAnnotatedMostIntensePeaks) {
        this.nAnnotatedMostIntensePeaks = nAnnotatedMostIntensePeaks;
    }

    /**
     * Returns the minimal number of annotated peaks a peptide should have.
     *
     * @return the minimal number of annotated peaks a peptide should have
     */
    public int getMinAnnotatedPeaks() {
        return minAnnotatedPeaks;
    }

    /**
     * Sets the minimal number of annotated peaks a peptide should have.
     *
     * @param minAnnotatedPeaks the minimal number of annotated peaks a peptide
     * should have
     */
    public void setMinAnnotatedPeaks(int minAnnotatedPeaks) {
        this.minAnnotatedPeaks = minAnnotatedPeaks;
    }

    /**
     * Returns the minimal number of peaks a spectrum should contain.
     *
     * @return the minimal number of peaks a spectrum should contain
     */
    public int getMinPeaks() {
        return minPeaks;
    }

    /**
     * Sets the minimal number of peaks a spectrum should contain.
     *
     * @param minPeaks the minimal number of peaks a spectrum should contain
     */
    public void setMinPeaks(int minPeaks) {
        this.minPeaks = minPeaks;
    }

    /**
     * Indicates whether N-terminal methionines should be cleaved.
     *
     * @return a boolean indicating whether N-terminal methionines should be
     * cleaved
     */
    public boolean isCleaveNterMethionine() {
        return cleaveNtermMethionine;
    }

    /**
     * Sets whether N-terminal methionines should be cleaved.
     *
     * @param cleaveNterMethionine whether N-terminal methionines should be
     * cleaved
     */
    public void setCleaveNterMethionine(boolean cleaveNterMethionine) {
        this.cleaveNtermMethionine = cleaveNterMethionine;
    }

    /**
     * Returns the maximal m/z ladder length.
     *
     * @return the maximal m/z ladder length
     */
    public int getMaxMzLadders() {
        return maxMzLadders;
    }

    /**
     * Sets the maximal m/z ladder length.
     *
     * @param maxMzLadders the maximal m/z ladder length
     */
    public void setMaxMzLadders(int maxMzLadders) {
        this.maxMzLadders = maxMzLadders;
    }

    /**
     * Returns the maximal fragment charge.
     *
     * @return the maximal fragment charge
     */
    public int getMaxFragmentCharge() {
        return maxFragmentCharge;
    }

    /**
     * Sets the maximal fragment charge.
     *
     * @param maxFragmentCharge the maximal fragment charge
     */
    public void setMaxFragmentCharge(int maxFragmentCharge) {
        this.maxFragmentCharge = maxFragmentCharge;
    }

    /**
     * Returns the fraction of peaks to be retained for charge &gt;1 estimation.
     *
     * @return the fraction of peaks to be retained for charge &gt;1 estimation
     */
    public double getFractionOfPeaksForChargeEstimation() {
        return fractionOfPeaksForChargeEstimation;
    }

    /**
     * Sets the fraction of peaks to be retained for charge &gt;1 estimation.
     *
     * @param fractionOfPeaksForChargeEstimation the fraction of peaks to be
     * retained for charge &gt;1 estimation
     */
    public void setFractionOfPeaksForChargeEstimation(double fractionOfPeaksForChargeEstimation) {
        this.fractionOfPeaksForChargeEstimation = fractionOfPeaksForChargeEstimation;
    }

    /**
     * Indicates whether charge plus one should be determined algorithmically.
     *
     * @return whether charge plus one should be determined algorithmically
     */
    public boolean isDetermineChargePlusOneAlgorithmically() {
        return determineChargePlusOneAlgorithmically;
    }

    /**
     * Sets whether charge plus one should be determined algorithmically.
     *
     * @param determineChargePlusOneAlgorithmically whether charge plus one
     * should be determined algorithmically
     */
    public void setDetermineChargePlusOneAlgorithmically(boolean determineChargePlusOneAlgorithmically) {
        this.determineChargePlusOneAlgorithmically = determineChargePlusOneAlgorithmically;
    }

    /**
     * Indicates whether positive ions are searched. False means negative ions.
     *
     * @return a boolean indicating whether positive ions are searched
     */
    public boolean isSearchPositiveIons() {
        return searchPositiveIons;
    }

    /**
     * Sets whether positive ions are searched. False means negative ions.
     *
     * @param searchPositiveIons a boolean indicating whether positive ions are
     * searched
     */
    public void setSearchPositiveIons(boolean searchPositiveIons) {
        this.searchPositiveIons = searchPositiveIons;
    }

    /**
     * Returns the minimal number of precursors per spectrum.
     *
     * @return the minimal number of precursors per spectrum
     */
    public int getMinPrecPerSpectrum() {
        return minPrecPerSpectrum;
    }

    /**
     * Sets the minimal number of precursors per spectrum.
     *
     * @param minPrecPerSpectrum the minimal number of precursors per spectrum
     */
    public void setMinPrecPerSpectrum(int minPrecPerSpectrum) {
        this.minPrecPerSpectrum = minPrecPerSpectrum;
    }

    /**
     * Indicates whether forward ions (b1) should be searched first.
     *
     * @return a boolean indicating whether forward ions (b1) should be searched
     * first
     */
    public boolean isSearchForwardFragmentFirst() {
        return searchForwardFragmentFirst;
    }

    /**
     * Sets whether forward ions (b1) should be searched first.
     *
     * @param searchForwardFragmentFirst whether forward ions (b1) should be
     * searched first
     */
    public void setSearchForwardFragmentFirst(boolean searchForwardFragmentFirst) {
        this.searchForwardFragmentFirst = searchForwardFragmentFirst;
    }

    /**
     * Indicates whether C-terminal fragments should be searched.
     *
     * @return a boolean indicating whether C-terminal fragments should be
     * searched
     */
    public boolean isSearchRewindFragments() {
        return searchRewindFragments;
    }

    /**
     * Sets whether C-terminal fragments should be searched.
     *
     * @param searchRewindFragments whether C-terminal fragments should be
     * searched
     */
    public void setSearchRewindFragments(boolean searchRewindFragments) {
        this.searchRewindFragments = searchRewindFragments;
    }

    /**
     * Returns the maximal number of fragments to retain per series.
     *
     * @return the maximal number of fragments to retain per series
     */
    public int getMaxFragmentPerSeries() {
        return maxFragmentPerSeries;
    }

    /**
     * Sets the maximal number of fragments to retain per series.
     *
     * @param maxFragmentPerSeries the maximal number of fragments to retain per
     * series
     */
    public void setMaxFragmentPerSeries(int maxFragmentPerSeries) {
        this.maxFragmentPerSeries = maxFragmentPerSeries;
    }

    /**
     * Indicates whether the correlation correction score should be used.
     *
     * @return a boolean indicating whether the correlation correction score
     * should be used
     */
    public boolean isUseCorrelationCorrectionScore() {
        return useCorrelationCorrectionScore;
    }

    /**
     * Sets whether the correlation correction score should be used.
     *
     * @param useCorrelationCorrectionScore a boolean indicating whether the
     * correlation correction score should be used
     */
    public void setUseCorrelationCorrectionScore(boolean useCorrelationCorrectionScore) {
        this.useCorrelationCorrectionScore = useCorrelationCorrectionScore;
    }

    /**
     * Returns the consecutive ion probability.
     *
     * @return the consecutive ion probability
     */
    public double getConsecutiveIonProbability() {
        return consecutiveIonProbability;
    }

    /**
     * Set the consecutive ion probability.
     *
     * @param consecutiveIonProbability the consecutive ion probability
     */
    public void setConsecutiveIonProbability(double consecutiveIonProbability) {
        this.consecutiveIonProbability = consecutiveIonProbability;
    }

    /**
     * Returns the e-value threshold to use to consider a sequence for the
     * iterative search. 0.0 means all.
     *
     * @return the e-value threshold to use to consider a sequence for the
     * iterative search
     */
    public double getIterativeSequenceEvalue() {
        return iterativeSequenceEvalue;
    }

    /**
     * Sets the e-value threshold to use to consider a sequence for the
     * iterative search. 0.0 means all.
     *
     * @param iterativeSequenceEvalue the e-value threshold to use to consider a
     * sequence for the iterative search
     */
    public void setIterativeSequenceEvalue(double iterativeSequenceEvalue) {
        this.iterativeSequenceEvalue = iterativeSequenceEvalue;
    }

    /**
     * Returns the e-value threshold to use to replace a hit for the iterative
     * search. 0.0 means the best hit will be retained.
     *
     * @return the e-value threshold to use to replace a hit for the iterative
     * search
     */
    public double getIterativeReplaceEvalue() {
        return iterativeReplaceEvalue;
    }

    /**
     * Sets the e-value threshold to use to replace a hit for the iterative
     * search. 0.0 means the best hit will be retained.
     *
     * @param iterativeReplaceEvalue the e-value threshold to use to replace a
     * hit for the iterative search
     */
    public void setIterativeReplaceEvalue(double iterativeReplaceEvalue) {
        this.iterativeReplaceEvalue = iterativeReplaceEvalue;
    }

    /**
     * Returns the e-value threshold to use consider a spectrum for the
     * iterative search. 0.0 means all.
     *
     * @return the e-value threshold to use consider a spectrum for the
     * iterative search
     */
    public double getIterativeSpectrumEvalue() {
        return iterativeSpectrumEvalue;
    }

    /**
     * Sets the e-value threshold to use consider a spectrum for the iterative
     * search. 0.0 means all.
     *
     * @param iterativeSpectrumEvalue the e-value threshold to use consider a
     * spectrum for the iterative search
     */
    public void setIterativeSpectrumEvalue(double iterativeSpectrumEvalue) {
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
     * Returns the index of a given modification
     *
     * @param modificationName the name of the modification
     * 
     * @return the corresponding index
     */
    public int getPtmIndex(String modificationName) {
        for (int index : ptmIndexes.keySet()) {
            if (modificationName.equalsIgnoreCase(ptmIndexes.get(index))) {
                return index;
            }
        }
        throw new IllegalArgumentException("No OMSSA index set for " + modificationName + "."); 
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

        HashMap<String, Integer> indexes = new HashMap<>();

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
        int number = -1;
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
                if (number != -1) {
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
            if (getMaxEValue() != omssaParameters.getMaxEValue()) {
                return false;
            }
            if (getHitListLength() != omssaParameters.getHitListLength()) {
                return false;
            }
            if (getMaxPeptideLength() != omssaParameters.getMaxPeptideLength()) {
                return false;
            }
            if (getMinPeptideLength() != omssaParameters.getMinPeptideLength()) {
                return false;
            }
            if (getMinimalChargeForMultipleChargedFragments() != omssaParameters.getMinimalChargeForMultipleChargedFragments()) {
                return false;
            }
            if (isEstimateCharge() != omssaParameters.isEstimateCharge()) {
                return false;
            }
            if (isRemovePrecursor() != omssaParameters.isRemovePrecursor()) {
                return false;
            }
            if (isScalePrecursor() != omssaParameters.isScalePrecursor()) {
                return false;
            }
            if (!getSelectedOutput().equals(omssaParameters.getSelectedOutput())) {
                return false;
            }
            if (isMemoryMappedSequenceLibraries() != omssaParameters.isMemoryMappedSequenceLibraries()) {
                return false;
            }
            double diff = Math.abs(neutronThreshold - omssaParameters.getNeutronThreshold());
            if (diff > Double.MIN_VALUE) {
                return false;
            }
            if (getLowIntensityCutOff() != omssaParameters.getLowIntensityCutOff()) {
                return false;
            }
            if (getHighIntensityCutOff() != omssaParameters.getHighIntensityCutOff()) {
                return false;
            }
            if (getIntensityCutOffIncrement() != omssaParameters.getIntensityCutOffIncrement()) {
                return false;
            }
            if (getSingleChargeWindow() != omssaParameters.getSingleChargeWindow()) {
                return false;
            }
            if (getDoubleChargeWindow() != omssaParameters.getDoubleChargeWindow()) {
                return false;
            }
            if (getnPeaksInSingleChargeWindow() != omssaParameters.getnPeaksInSingleChargeWindow()) {
                return false;
            }
            if (getnPeaksInDoubleChargeWindow() != omssaParameters.getnPeaksInDoubleChargeWindow()) {
                return false;
            }
            if (getMaxHitsPerSpectrumPerCharge() != omssaParameters.getMaxHitsPerSpectrumPerCharge()) {
                return false;
            }
            if (getnAnnotatedMostIntensePeaks() != omssaParameters.getnAnnotatedMostIntensePeaks()) {
                return false;
            }
            if (getMinAnnotatedPeaks() != omssaParameters.getMinAnnotatedPeaks()) {
                return false;
            }
            if (getMinPeaks() != omssaParameters.getMinPeaks()) {
                return false;
            }
            if (isCleaveNterMethionine() != omssaParameters.isCleaveNterMethionine()) {
                return false;
            }
            if (getMaxMzLadders() != omssaParameters.getMaxMzLadders()) {
                return false;
            }
            if (getMaxFragmentCharge() != omssaParameters.getMaxFragmentCharge()) {
                return false;
            }
            diff = Math.abs(fractionOfPeaksForChargeEstimation - omssaParameters.getFractionOfPeaksForChargeEstimation());
            if (diff > Double.MIN_VALUE) {
                return false;
            }
            if (isDetermineChargePlusOneAlgorithmically() != omssaParameters.isDetermineChargePlusOneAlgorithmically()) {
                return false;
            }
            if (isSearchPositiveIons() != omssaParameters.isSearchPositiveIons()) {
                return false;
            }
            if (isSearchRewindFragments() != omssaParameters.isSearchRewindFragments()) {
                return false;
            }
            if (getMaxFragmentPerSeries() != omssaParameters.getMaxFragmentPerSeries()) {
                return false;
            }
            if (isUseCorrelationCorrectionScore() != omssaParameters.isUseCorrelationCorrectionScore()) {
                return false;
            }
            diff = Math.abs(consecutiveIonProbability - omssaParameters.getConsecutiveIonProbability());
            if (diff > Double.MIN_VALUE) {
                return false;
            }
            diff = Math.abs(iterativeSequenceEvalue - omssaParameters.getIterativeSequenceEvalue());
            if (diff > Double.MIN_VALUE) {
                return false;
            }
            diff = Math.abs(iterativeSpectrumEvalue - omssaParameters.getIterativeSpectrumEvalue());
            if (diff > Double.MIN_VALUE) {
                return false;
            }
            diff = Math.abs(iterativeReplaceEvalue - omssaParameters.getIterativeReplaceEvalue());
            if (diff > Double.MIN_VALUE) {
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
        output.append(nPeaksIndoubleChargeWindow);
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
