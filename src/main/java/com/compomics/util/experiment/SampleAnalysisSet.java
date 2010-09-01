package com.compomics.util.experiment;

import com.compomics.util.experiment.biology.Sample;
import com.compomics.util.experiment.utils.ExperimentObject;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * A sample measurement set is a set of replicates from a common sample.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 10:05:53 AM
 */
public class SampleAnalysisSet extends ExperimentObject {

    /**
     * The reference sample
     */
    private Sample sample;

    /**
     * The various analysis indexed by replicates indexes
     */
    private HashMap<Integer, ProteomicAnalysis> analysis = new HashMap<Integer, ProteomicAnalysis>();

    /**
     * Contructor for a set of analysis of a sample
     * @param referenceSample   the reference sample
     * @param replicates        the set of replicates for this sample
     */
    public SampleAnalysisSet(Sample referenceSample, ArrayList<ProteomicAnalysis> replicates) {
        this.sample = referenceSample;
        for (ProteomicAnalysis replicate : replicates) {
            analysis.put(replicate.getIndex(), replicate);
        }
    }
}
