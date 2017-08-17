package com.compomics.util.experiment.io.mass_spectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains the indexes of an mgf file after indexing mapped with the
 * title of the spectrum.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MgfIndex extends ExperimentObject implements Serializable {

    /**
     * The map of all indexes: spectrum title &gt; index in the file.
     */
    private HashMap<String, Long> indexMap;
    /**
     * A map of all the spectrum titles and which rank they have in the file,
     * i.e., the first spectrum has rank 0, the second rank 1, etc.
     */
    private HashMap<String, Integer> spectrumNumberIndexMap;
    /**
     * List of spectrum titles.
     */
    private ArrayList<String> spectrumTitles = null;
    /**
     * Map of duplicated spectrum titles and how often they are duplicated.
     */
    private HashMap<String, Integer> duplicatedSpectrumTitles = null;
    /**
     * Map of the precursor mz values.
     */
    private HashMap<Integer, Double> precursorMzMap = null;
    /**
     * The name of the indexed file.
     */
    private String fileName;
    /**
     * The last time the indexed file was modified.
     */
    private Long lastModified;
    /**
     * The maximum RT found in the spectra.
     */
    private Double maxRT;
    /**
     * The minimum RT found in the spectra.
     */
    private Double minRT;
    /**
     * The maximal m/z in all precursors of the file.
     */
    private Double maxMz;
    /**
     * The maximal precursor intensity of the file.
     */
    private Double maxIntensity;
    /**
     * The maximal charge.
     */
    private Integer maxCharge;
    /**
     * The maximal peak count.
     */
    private Integer maxPeakCount;
    /**
     * Indicates if the spectra seem to be peak picked or not. A null value
     * indicated that the check for peak picking was not performed.
     */
    private Boolean peakPicked = null;
    /**
     * Returns the number of spectra in the file as counted by the begin ions
     * tags. Null if not set.
     */
    private Integer spectrumCount = null;
    /**
     * Indicates if there are spectra where the precursor charge is missing. A
     * null value indicated that the check was not performed.
     */
    private Boolean precursorChargesMissing = null;

    /**
     * Constructor.
     *
     * @param spectrumTitles an ordered list of all spectrum titles
     * @param indexMap map of all indexes: spectrum title &gt; index in the file
     * @param spectrumNumberIndexMap map of all spectrum index: spectrum title
     * &gt; spectrum index in the file
     * @param precursorMzMap map of the precursor mz values: spectrum index &gt;
     * precursor mz
     * @param fileName the mgf file name
     * @param maxRT the maximum retention time
     * @param minRT the minimum retention tome
     * @param maxMz the maximum m/z value
     * @param maxIntensity the maximum precursor intensity
     * @param maxCharge the maximum peak precursor charge
     * @param maxPeakCount the maximum peak count
     * @param peakPicked indicates if the spectra seem to be peak picked or not
     * @param precursorChargesMissing indicates if at least one spectrum is
     * missing the precursor charge tag
     * @param lastModified a long indicating the last time the indexed file was
     * modified
     */
    public MgfIndex(ArrayList<String> spectrumTitles, HashMap<String, Long> indexMap, HashMap<String, Integer> spectrumNumberIndexMap, HashMap<Integer, Double> precursorMzMap,
            String fileName, double minRT, double maxRT, double maxMz, double maxIntensity, int maxCharge, int maxPeakCount, boolean peakPicked, boolean precursorChargesMissing,
            long lastModified) {
        this.spectrumTitles = spectrumTitles;
        this.duplicatedSpectrumTitles = null; //information not provided
        this.indexMap = indexMap;
        this.spectrumNumberIndexMap = spectrumNumberIndexMap;
        this.precursorMzMap = precursorMzMap;
        this.fileName = fileName;
        this.maxRT = maxRT;
        this.minRT = minRT;
        this.maxMz = maxMz;
        this.maxIntensity = maxIntensity;
        this.maxCharge = maxCharge;
        this.maxPeakCount = maxPeakCount;
        this.peakPicked = peakPicked;
        this.precursorChargesMissing = precursorChargesMissing;
        this.lastModified = lastModified;
    }

    /**
     * Constructor.
     *
     * @param spectrumTitles an ordered list of all spectrum titles
     * @param duplicatedSpectrumTitles a map of duplicated spectrum titles, and
     * how often each title is duplicated
     * @param indexMap map of all indexes: spectrum title &gt; index in the file
     * @param spectrumNumberIndexMap map of all spectrum index: spectrum title
     * &gt; spectrum index in the file
     * @param precursorMzMap map of the precursor mz values: spectrum index &gt;
     * precursor mz
     * @param fileName the mgf file name
     * @param maxRT the maximum retention time
     * @param minRT the minimum retention tome
     * @param maxMz the maximum m/z value
     * @param maxIntensity the maximum precursor intensity
     * @param maxCharge the maximum peak precursor charge
     * @param maxPeakCount the maximum peak count
     * @param peakPicked indicates if the spectra seem to be peak picked or not
     * @param precursorChargesMissing indicates if at least one spectrum is
     * missing the precursor charge tag
     * @param lastModified a long indicating the last time the indexed file was
     * modified
     * @param spectrumCount the number of spectra in the file counted by the
     * number of begin ion tags
     */
    public MgfIndex(ArrayList<String> spectrumTitles, HashMap<String, Integer> duplicatedSpectrumTitles, HashMap<String, Long> indexMap, HashMap<String, Integer> spectrumNumberIndexMap,
            HashMap<Integer, Double> precursorMzMap, String fileName, double minRT, double maxRT, double maxMz, double maxIntensity, int maxCharge, int maxPeakCount,
            boolean peakPicked, boolean precursorChargesMissing, long lastModified, int spectrumCount) {
        this.spectrumTitles = spectrumTitles;
        this.duplicatedSpectrumTitles = duplicatedSpectrumTitles;
        this.indexMap = indexMap;
        this.spectrumNumberIndexMap = spectrumNumberIndexMap;
        this.precursorMzMap = precursorMzMap;
        this.fileName = fileName;
        this.maxRT = maxRT;
        this.minRT = minRT;
        this.maxMz = maxMz;
        this.maxIntensity = maxIntensity;
        this.maxCharge = maxCharge;
        this.maxPeakCount = maxPeakCount;
        this.peakPicked = peakPicked;
        this.precursorChargesMissing = precursorChargesMissing;
        this.lastModified = lastModified;
        this.spectrumCount = spectrumCount;
    }

    /**
     * Returns the index corresponding to the desired spectrum.
     *
     * @param spectrumTitle the desired spectrum
     * @return the corresponding index
     */
    public Long getIndex(String spectrumTitle) {
        return indexMap.get(spectrumTitle);
    }

    /**
     * Returns the spectrum index corresponding to the desired spectrum, i.e.,
     * returns 0 for the first spectrum in the file, 1 for the second, etc. Null
     * map is not set, and -1 if not found.
     *
     * @param spectrumTitle the desired spectrum
     * @return the corresponding spectrum index
     */
    public Integer getSpectrumIndex(String spectrumTitle) {

        if (spectrumNumberIndexMap == null) {
            return null;
        }

        Integer index = spectrumNumberIndexMap.get(spectrumTitle);

        if (index == null) {
            return -1;
        } else {
            return index;
        }
    }

    /**
     * Returns the precursor mz for the spectrum at the given index. Returns
     * null if the map is not set, or the value cannot be found.
     *
     * @param spectrumIndex the index of the spectrum, 0 for the first spectrum
     * in the file, 1 for the second, etc
     * @return the precursor mz
     */
    public Double getPrecursorMz(int spectrumIndex) {

        if (precursorMzMap == null) {
            return null;
        }

        Double mz = precursorMzMap.get(spectrumIndex);

        if (mz == null) {
            return null;
        } else {
            return mz;
        }
    }

    /**
     * Returns the spectrum title corresponding to the given spectrum number. 0
     * is the first spectrum.
     *
     * @param number the number of the spectrum
     *
     * @return the title of the spectrum of interest
     */
    public String getSpectrumTitle(int number) {
        return spectrumTitles.get(number);
    }

    /**
     * Returns a boolean indicating whether the spectrum title is implemented in
     * this index.
     *
     * @param spectrumTitle the spectrum title
     * @return a boolean indicating whether the spectrum title is implemented in
     * this index
     */
    public boolean containsSpectrum(String spectrumTitle) {
        return indexMap.containsKey(spectrumTitle);
    }

    /**
     * Returns an ordered list of all spectrum titles.
     *
     * @return an ordered list of all spectrum titles
     */
    public ArrayList<String> getSpectrumTitles() {
        if (spectrumTitles != null) {
            return spectrumTitles;
        } else {
            return new ArrayList<>(indexMap.keySet());
        }
    }

    /**
     * Returns a map of the duplicated spectrum titles, can be null.
     *
     * @return a map of the duplicated spectrum titles, can be null
     */
    public HashMap<String, Integer> getDuplicatedSpectrumTitles() {
        return duplicatedSpectrumTitles;
    }

    /**
     * Returns the name of the indexed file.
     *
     * @return the name of the indexed file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the maximal RT in this file.
     *
     * @return the maximal RT in this file
     */
    public Double getMaxRT() {
        return maxRT;
    }

    /**
     * Sets the maximal RT in this file.
     *
     * @param maxRT the maximal RT in this file
     */
    public void setMaxRT(Double maxRT) {
        this.maxRT = maxRT;
    }

    /**
     * Returns the maximum m/z in this file.
     *
     * @return the maximum m/z in this file
     */
    public Double getMaxMz() {
        return maxMz;
    }

    /**
     * Sets the maximum charge in this file.
     *
     * @param maxCharge the maximum charge in this file
     */
    public void setMaxCharge(Integer maxCharge) {
        this.maxCharge = maxCharge;
    }

    /**
     * Returns the maximal charge found in the mgf file.
     *
     * @return the maximal charge found in the mgf file
     */
    public Integer getMaxCharge() {
        return maxCharge;
    }

    /**
     * Sets the maximum m/z in this file.
     *
     * @param maxMz the maximum m/z in this file
     */
    public void setMaxMz(Double maxMz) {
        this.maxMz = maxMz;
    }

    /**
     * Returns the maximum precursor intensity in this file.
     *
     * @return the maximum precursor intensity in this file
     */
    public Double getMaxIntensity() {
        return maxIntensity;
    }

    /**
     * Sets the maximum precursor intensity in this file.
     *
     * @param maxIntensity the maximum precursor intensity in this file
     */
    public void setMaxIntensity(Double maxIntensity) {
        this.maxIntensity = maxIntensity;
    }

    /**
     * Returns the minimum RT in this file.
     *
     * @return the minimum RT in this file
     */
    public Double getMinRT() {
        return minRT;
    }

    /**
     * Sets the minimum RT in this file.
     *
     * @param minRT the minimum RT in this file
     */
    public void setMinRT(Double minRT) {
        this.minRT = minRT;
    }

    /**
     * Returns the maximum peak count in this file.
     *
     * @return the maximum peak count in this file
     */
    public Integer getMaxPeakCount() {
        return maxPeakCount;
    }

    /**
     * Sets the maximum peak count in this file.
     *
     * @param maxPeakCount the maximum peak count in this file
     */
    public void setMaxPeakCount(Integer maxPeakCount) {
        this.maxPeakCount = maxPeakCount;
    }

    /**
     * Returns the number of imported spectra.
     *
     * @return the number of imported spectra
     */
    public int getNSpectra() {
        if (spectrumCount == null) {
            spectrumCount = spectrumTitles.size();
        }
        return spectrumCount;
    }

    /**
     * Returns when the file was last modified. Null if not set or for utilities
     * versions older than 3.11.30.
     *
     * @return a long indicating when the file was last modified
     */
    public Long getLastModified() {
        return lastModified;
    }

    /**
     * Returns true if the indexed file seems to contain only peak picked
     * spectra.
     *
     * @return true if the indexed file seems to contain only peak picked
     * spectra
     */
    public Boolean isPeakPicked() {
        if (peakPicked == null) {
            peakPicked = true;
        }
        return peakPicked;
    }

    /**
     * Set if the indexed file seems to contain only peak picked spectra or not.
     *
     * @param peakPicked the peakPicked to set
     */
    public void setPeakPicked(Boolean peakPicked) {
        this.peakPicked = peakPicked;
    }

    /**
     * Returns true if the at least one spectrum is missing the precursor
     * charge.
     *
     * @return true if the at least one spectrum is missing the precursor charge
     */
    public Boolean isPrecursorChargesMissing() {
        return precursorChargesMissing;
    }

    /**
     * Set if at least one spectrum is missing the precursor charge.
     *
     * @param precursorChargesMissing the precursorChargesMissing to set
     */
    public void setPrecursorChargesMissing(Boolean precursorChargesMissing) {
        this.precursorChargesMissing = precursorChargesMissing;
    }
}
