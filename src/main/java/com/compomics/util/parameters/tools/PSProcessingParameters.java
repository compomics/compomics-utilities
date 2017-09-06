package com.compomics.util.parameters.tools;

import java.io.Serializable;

/**
 * This class groups the user preferences for the initial PeptideShaker
 * processing.
 * 
 * @deprecated replaced by utilities processing preferences and fraction preferences
 *
 * @author Marc Vaudel
 */
public class PSProcessingParameters implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -5883143685674607162L;
    /**
     * The number of threads to use.
     */
    private int nThreads;
    /**
     * The minimum confidence required for a protein to be included in the
     * calculation of the average molecular weight plot in the Fractions tab.
     */
    private Double proteinConfidenceMwPlots = 95.0; //@TODO: move to another class

    /**
     * Constructor with default settings.
     */
    public PSProcessingParameters() {
        nThreads = Math.max(Runtime.getRuntime().availableProcessors(), 1);
//        nThreads = Math.max(Runtime.getRuntime().availableProcessors()-1, 1);
//        nThreads = 1;
    }

    /**
     * Returns the number of threads to use.
     *
     * @return the number of threads to use
     */
    public int getnThreads() {
        return nThreads;
    }

    /**
     * Sets the number of threads to use.
     *
     * @param nThreads the number of threads to use
     */
    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    /**
     * Returns the minimum confidence required for a protein to be included in
     * the average molecular weight analysis in the Fractions tab.
     *
     * @return the minimum confidence
     */
    public Double getProteinConfidenceMwPlots() {
        return proteinConfidenceMwPlots;
    }

    /**
     * Sets the minimum confidence required for a protein to be included in the
     * average molecular weight analysis in the Fractions tab.
     *
     * @param proteinConfidenceMwPlots minimum confidence
     */
    public void setProteinConfidenceMwPlots(Double proteinConfidenceMwPlots) {
        this.proteinConfidenceMwPlots = proteinConfidenceMwPlots;
    }
}
