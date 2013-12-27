package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Sample;
import com.compomics.util.experiment.quantification.Quantification;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Set;

/**
 * This class contains quantification parameters.
 *
 * @author Marc Vaudel
 */
public class ReporterIonQuantification extends Quantification {

    /**
     * The sample assignment to the various ions indexed by their index.
     */
    private HashMap<Integer, Sample> sampleAssignement = new HashMap<Integer, Sample>();
    /**
     * List of control samples. The index should be the same as for the sample
     * assignment.
     */
    private ArrayList<Integer> controlSamples;
    /**
     * List of normalisation factors. The index should be the same as for the
     * sample assignment.
     */
    private HashMap<Integer, Double> normalisationFactors = new HashMap<Integer, Double>();
    /**
     * The reporter method.
     */
    private ReporterMethod reporterMethod;

    /**
     * Constructor for the reporter ion quantification.
     *
     * @param methodUsed the method used for this quantification
     */
    public ReporterIonQuantification(QuantificationMethod methodUsed) {
        this.methodUsed = methodUsed;
    }

    /**
     * Assign a sample to an ion referenced by its static index.
     *
     * @param reporterIndex the index of the ion
     * @param sample the sample
     */
    public void assignSample(int reporterIndex, Sample sample) {
        sampleAssignement.put(reporterIndex, sample);
    }

    /**
     * This method returns the sample associated to the given ion.
     *
     * @param reporterIndex the static index of the reporter ion
     * @return the corresponding sample
     */
    public Sample getSample(int reporterIndex) {
        return sampleAssignement.get(reporterIndex);
    }

    /**
     * This method returns the ion associated to the given sample.
     *
     * @param aSample the sample
     * @return the static index of the associated ion
     */
    public Integer getReporterIndex(Sample aSample) {
        for (int index : sampleAssignement.keySet()) {
            if (sampleAssignement.get(index).isSameAs(aSample)) {
                return index;
            }
        }
        return null;
    }

    /**
     * returns the reporter method used.
     *
     * @return the method used
     */
    public ReporterMethod getReporterMethod() {
        return reporterMethod;
    }

    /**
     * Sets the reporter method used.
     *
     * @param reporterMethod the reporter method used
     */
    public void setMethod(ReporterMethod reporterMethod) {
        this.reporterMethod = reporterMethod;
    }

    /**
     * Returns the default reference for an identification.
     *
     * @param experimentReference the experiment reference
     * @param sampleReference the sample reference
     * @param replicateNumber the replicate number
     * @return the default reference
     */
    public static String getDefaultReference(String experimentReference, String sampleReference, int replicateNumber) {
        return Util.removeForbiddenCharacters(experimentReference + "_" + sampleReference + "_" + replicateNumber + "_reporterQuant");
    }

    /**
     * Returns the indexes of the samples labelled as control.
     *
     * @return the indexes of the samples labelled as control
     */
    public ArrayList<Integer> getControlSamples() {
        return controlSamples;
    }

    /**
     * Sets the indexes of the samples labelled as control.
     *
     * @param controlSamples the indexes of the samples to label as control
     */
    public void setControlSamples(ArrayList<Integer> controlSamples) {
        this.controlSamples = controlSamples;
    }

    /**
     * Indicates whether the normalisation factors are set.
     *
     * @return a boolean indicating whether the normalisation factors are set
     */
    public boolean hasNormalisationFactors() {
        return !normalisationFactors.isEmpty();
    }

    /**
     * Resets the normalisation factors.
     */
    public void resetNormalisationFactors() {
        normalisationFactors.clear();
    }

    /**
     * Adds a normalisation factor.
     *
     * @param sampleIndex the index of the sample
     * @param normalisationFactor the normalisation factor
     */
    public void addNormalisationFactor(int sampleIndex, double normalisationFactor) {
        normalisationFactors.put(sampleIndex, normalisationFactor);
    }

    /**
     * Returns the normalisation factor for the given sample.
     *
     * @param sampleIndex the index of the sample
     *
     * @return the normalisation factor, 1.0 if not set.
     */
    public double getNormalisationFactor(int sampleIndex) {
        Double normalisationFactor = normalisationFactors.get(sampleIndex);
        if (normalisationFactor == null) {
            return 1.0;
        }
        return normalisationFactor;
    }

    /**
     * Returns a set containing the indexes of every sample.
     *
     * @return a set containing the indexes of every sample
     */
    public Set<Integer> getSampleIndexes() {
        return sampleAssignement.keySet();
    }
}
