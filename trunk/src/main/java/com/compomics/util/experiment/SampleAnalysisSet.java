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
     * Contructor for an analysis of a sample without replicates
     * @param referenceSample   the reference sample
     * @param replicate        the analysis for this sample
     */
    public SampleAnalysisSet(Sample referenceSample, ProteomicAnalysis replicate) {
        this.sample = referenceSample;
        analysis.put(replicate.getIndex(), replicate);
    }
    
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

    /**
     * Returns the analysis corresponding to the selected replicate
     * @param replicateNumber   index of the replicate analyzed
     * @return the proteomic analysis of the replicate
     */
    public ProteomicAnalysis getProteomicAnalysis(int replicateNumber) {
        return analysis.get(replicateNumber);
    }
}
