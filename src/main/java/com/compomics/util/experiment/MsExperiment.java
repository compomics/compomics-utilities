package com.compomics.util.experiment;

import com.compomics.util.experiment.biology.Sample;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.HashMap;

/**
 * This class represents the experiment.
 *
 * @author Marc Vaudel
 */
public class MsExperiment extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 4330394623824512352L;
    /**
     * Reference of the experiment.
     */
    private String reference;
    /**
     * The samples analyzed.
     */
    private HashMap<Integer, Sample> samples = new HashMap<Integer, Sample>();
    /**
     * The analysis.
     */
    private HashMap<Sample, SampleAnalysisSet> analysis = new HashMap<Sample, SampleAnalysisSet>();
    /**
     * The name of the serialized experiment.
     */
    public final static String experimentObjectName = "experiment";

    /**
     * Constructor for an experiment.
     *
     * @param reference the reference of the experiment
     * @param samples the samples analyzed
     */
    public MsExperiment(String reference, HashMap<Integer, Sample> samples) {
        this.reference = reference;
        this.samples = samples;
    }

    /**
     * Constructor for an experiment.
     *
     * @param reference the reference of the experiment
     */
    public MsExperiment(String reference) {
        this.reference = reference;
    }

    /**
     * Returns the reference of the experiment.
     *
     * @return reference of the experiment
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the reference of the experiment.
     *
     * @param reference the experiment reference
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Method to link an analysis set to an experiment.
     *
     * @param sample the reference sample
     * @param analysisSet the analysis set corresponding to this sample
     */
    public void addAnalysisSet(Sample sample, SampleAnalysisSet analysisSet) {
        analysis.put(sample, analysisSet);
        int index = 0;
        while (samples.containsKey(index)) {
            index++;
        }
        setSample(index, sample);
    }

    /**
     * Returns the analysis set corresponding to a sample.
     *
     * @param sample the reference sample
     * @return the analysis set corresponding to the reference sample
     */
    public SampleAnalysisSet getAnalysisSet(Sample sample) {
        return analysis.get(sample);
    }

    /**
     * Returns the implemented samples.
     *
     * @return map containing all samples
     */
    public HashMap<Integer, Sample> getSamples() {
        return samples;
    }

    /**
     * Returns a single sample accessed by its index.
     *
     * @param id the index of the desired sample
     * @return the desired sample
     */
    public Sample getSample(int id) {
        return samples.get(id);
    }

    /**
     * Set a new sample.
     *
     * @param index the index of the sample
     * @param sample the new sample
     */
    public void setSample(int index, Sample sample) {
        samples.put(index, sample);
    }
}
