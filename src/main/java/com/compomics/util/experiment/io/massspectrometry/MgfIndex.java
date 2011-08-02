/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.io.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.HashMap;

/**
 * This class contains the indexes of an mgf file after indexing mapped with the title of the spectrum
 *
 * @author marc
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
     * Constructor
     * @param indexMap map of all indexes: spectrum title -> index in the file
     */
    public MgfIndex(HashMap<String, Long> indexMap, String fileName) {
        this.indexMap = indexMap;
        this.fileName = fileName;
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
}
