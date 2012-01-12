package com.compomics.util.experiment.io.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.HashMap;

/**
 * This class contains the indexes of an mgf file after indexing mapped with the title of the spectrum
 *
 * @author Marc Vaudel
 */
public class MgfIndex extends ExperimentObject {

    static final long serialVersionUID = -5591103342266964263L;
    /**
     * The map of all indexes: spectrum title -> index in the file
     */
    private HashMap<String, Long> indexMap;
    /**
     * The name of the indexed file
     */
    private String fileName;
    /**
     * The maximal RT found in the spectra
     */
    private Double maxRT;

    /**
     * Constructor
     * @param indexMap map of all indexes: spectrum title -> index in the file
     * @param fileName  
     */
    public MgfIndex(HashMap<String, Long> indexMap, String fileName, double maxRT) {
        this.indexMap = indexMap;
        this.fileName = fileName;
        this.maxRT = maxRT;
    }

    /**
     * Returns the index corresponding to the desired spectrum
     * @param spectrumTitle the desired spectrum
     * @return the corresponding index
     */
    public Long getIndex(String spectrumTitle) {
        return indexMap.get(spectrumTitle);
    }

    /**
     * Returns the index map
     * @return the index mapF
     */
    public HashMap<String, Long> getIndexes() {
        return indexMap;
    }

    /**
     * Returns the name of the indexed file
     * @return the name of the indexed file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the maximal RT in this file
     * @return the maximal RT in this file
     */
    public Double getMaxRT() {
        return maxRT;
    }

    /**
     * Sets the maximal RT in this file
     * @param maxRT the maximal RT in this file
     */
    public void setMaxRT(Double maxRT) {
        this.maxRT = maxRT;
    }
    
    /**
     * Returns the number of imported spectra
     * @return the number of imported spectra
     */
    public int getNSpectra() {
        return indexMap.size();
    }
}
