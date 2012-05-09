package com.compomics.util.experiment.io.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains the indexes of an mgf file after indexing mapped with the
 * title of the spectrum
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MgfIndex extends ExperimentObject {

    static final long serialVersionUID = -5591103342266964263L;
    /**
     * The map of all indexes: spectrum title -> index in the file.
     */
    private HashMap<String, Long> indexMap;
    /**
     * List of all spectra
     */
    private ArrayList<String> spectrumTitles = null;
    /**
     * The name of the indexed file.
     */
    private String fileName;
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
     * Constructor.
     *
     * @param spectrumTitles an ordered list of all spectrum titles
     * @param indexMap map of all indexes: spectrum title -> index in the file
     * @param fileName the mgf file name
     * @param maxRT the maximum retention time
     * @param minRT the minimum retention tome
     * @param maxMz
     */
    public MgfIndex(ArrayList<String> spectrumTitles, HashMap<String, Long> indexMap, String fileName, double minRT, double maxRT, double maxMz) {
        this.spectrumTitles = spectrumTitles;
        this.indexMap = indexMap;
        this.fileName = fileName;
        this.maxRT = maxRT;
        this.minRT = minRT;
        this.maxMz = maxMz;
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
     * Returns a boolean indicating whether the spectrum title is implemented in this index
     * @param spectrumTitle the spectrum title
     * @return a boolean indicating whether the spectrum title is implemented in this index
     */
    public boolean containsSpectrum(String spectrumTitle) {
        return indexMap.containsKey(spectrumTitle);
    }

    /**
     * Returns an ordered list of all spectrum titles
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
     * Sets the maximum m/z in this file.
     *
     * @param maxMz the maximum m/z in this file
     */
    public void setMaxMz(Double maxMz) {
        this.maxMz = maxMz;
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
     * Returns the number of imported spectra.
     *
     * @return the number of imported spectra
     */
    public int getNSpectra() {
        return indexMap.size();
    }
}
