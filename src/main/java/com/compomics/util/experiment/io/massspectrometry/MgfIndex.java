package com.compomics.util.experiment.io.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains the indexes of an mgf file after indexing mapped with the
 * title of the spectrum.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MgfIndex extends ExperimentObject {

    /**
     * The map of all indexes: spectrum title -> index in the file.
     */
    private HashMap<String, Long> indexMap;
    /**
     * List of spectrum titles.
     */
    private ArrayList<String> spectrumTitles = null;
    /**
     * Map of duplicated spectrum titles and how often they are duplicated.
     */
    private HashMap<String, Integer> duplicatedSpectrumTitles = null;
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
     * Constructor.
     *
     * @param spectrumTitles an ordered list of all spectrum titles
     * @param indexMap map of all indexes: spectrum title -> index in the file
     * @param fileName the mgf file name
     * @param maxRT the maximum retention time
     * @param minRT the minimum retention tome
     * @param maxMz the maximum m/z value
     * @param maxIntensity the maximum precursor intensity
     * @param maxCharge the maximum peak precursor charge
     * @param maxPeakCount the maximum peak count
     * @param lastModified a long indicating the last time the indexed file was
     * modified
     */
    public MgfIndex(ArrayList<String> spectrumTitles, HashMap<String, Long> indexMap, String fileName, double minRT,
            double maxRT, double maxMz, double maxIntensity, int maxCharge, int maxPeakCount, long lastModified) {
        this.spectrumTitles = spectrumTitles;
        this.duplicatedSpectrumTitles = null; //information not provided
        this.indexMap = indexMap;
        this.fileName = fileName;
        this.maxRT = maxRT;
        this.minRT = minRT;
        this.maxMz = maxMz;
        this.maxIntensity = maxIntensity;
        this.maxCharge = maxCharge;
        this.maxPeakCount = maxPeakCount;
        this.lastModified = lastModified;
    }

    /**
     * Constructor.
     *
     * @param spectrumTitles an ordered list of all spectrum titles
     * @param duplicatedSpectrumTitles a map of duplicated spectrum titles, and
     * how often each title is duplicated
     * @param indexMap map of all indexes: spectrum title -> index in the file
     * @param fileName the mgf file name
     * @param maxRT the maximum retention time
     * @param minRT the minimum retention tome
     * @param maxMz the maximum m/z value
     * @param maxIntensity the maximum precursor intensity
     * @param maxCharge the maximum peak precursor charge
     * @param maxPeakCount the maximum peak count
     * @param lastModified a long indicating the last time the indexed file was
     * modified
     */
    public MgfIndex(ArrayList<String> spectrumTitles, HashMap<String, Integer> duplicatedSpectrumTitles, HashMap<String, Long> indexMap, String fileName, double minRT,
            double maxRT, double maxMz, double maxIntensity, int maxCharge, int maxPeakCount, long lastModified) {
        this.spectrumTitles = spectrumTitles;
        this.duplicatedSpectrumTitles = duplicatedSpectrumTitles;
        this.indexMap = indexMap;
        this.fileName = fileName;
        this.maxRT = maxRT;
        this.minRT = minRT;
        this.maxMz = maxMz;
        this.maxIntensity = maxIntensity;
        this.maxCharge = maxCharge;
        this.maxPeakCount = maxPeakCount;
        this.lastModified = lastModified;
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
            return new ArrayList<String>(indexMap.keySet());
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
        return spectrumTitles.size();
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
}