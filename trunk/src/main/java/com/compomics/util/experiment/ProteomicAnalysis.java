package com.compomics.util.experiment;

import com.compomics.util.experiment.utils.ExperimentObject;

/**
 * This class modelizes a proteomic analysis.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 10:11:18 AM
 */
public class ProteomicAnalysis extends ExperimentObject {

    /**
     * the analysis index
     */
    private int index;

    /**
     * constructor for a proteomic analysis
     * @param index the index of the replicate
     */
    public ProteomicAnalysis(int index) {
        this.index = index;
    }

    /**
     * get the index of the replicate
     * @return the index of the replicate
     */
    public int getIndex() {
        return index;
    }
}
