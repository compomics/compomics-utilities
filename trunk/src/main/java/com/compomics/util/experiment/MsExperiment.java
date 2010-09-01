package com.compomics.util.experiment;

import com.compomics.util.experiment.biology.Sample;
import com.compomics.util.experiment.utils.ExperimentObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents the experiment.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:55:09 AM
 */
public class MsExperiment extends ExperimentObject {

    /**
     * reference of the experiment
     */
    private String reference;

    /**
     * the set of sample analyzed
     */
    private ArrayList<Sample> samples;

    /**
     * the analysis
     */
    private HashMap<Sample, SampleAnalysisSet> analysis = new HashMap<Sample, SampleAnalysisSet>();

    /**
     * Constructor for an experiment
     * @param reference the reference of the experiment
     * @param samples   the samples analyzed
     */
    public MsExperiment(String reference, ArrayList<Sample> samples) {
        this.reference = reference;
        this.samples = samples;
    }

    /**
     * returns the reference of the experiment
     * @return reference of the experiment
     */
    public String getReference() {
        return reference;
    }

    /**
     * Method to link an analysis set to an experiment
     * @param sample        the reference sample
     * @param analysisSet   the analysis set corresponding to this sample
     */
    public void addAnalysisSet(Sample sample, SampleAnalysisSet analysisSet) {
        analysis.put(sample, analysisSet);
    }

    /**
     * Returns the analysis set corresponding to a sample
     * @param sample    the reference sample
     * @return the analysis set corresponding to the reference sample
     */
    public SampleAnalysisSet getAnalysisSet(Sample sample) {
        return analysis.get(sample);
    }
}
