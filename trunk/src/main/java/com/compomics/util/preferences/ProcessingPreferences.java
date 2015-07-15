package com.compomics.util.preferences;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.psm_scoring.PsmScores;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class groups the user preferences for the initial PeptideShaker
 * processing.
 *
 * @author Marc Vaudel
 */
public class ProcessingPreferences implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -5883143685674607162L;
    /**
     * The minimum confidence required for a protein to be included in the
     * average molecular weight analysis in the Fractions tab.
     */
    private Double proteinConfidenceMwPlots = 95.0; //@TODO: move to another class?
    /**
     * The number of threads to use.
     */
    private int nThreads;

    /**
     * Constructor with default settings.
     */
    public ProcessingPreferences() {
        nThreads = Math.max(Runtime.getRuntime().availableProcessors(), 1); // @TODO: make it possible for the user to control the number of threads?
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
        if (proteinConfidenceMwPlots == null) {
            return 95.0;
        }
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
