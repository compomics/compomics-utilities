package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Sample;
import com.compomics.util.experiment.normalization.NormalizationFactors;
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
     * The sample assignment to the various reporter ions indexed by their name.
     */
    private HashMap<String, Sample> sampleAssignement = new HashMap<String, Sample>();
    /**
     * The sample index map. The key is the sample reference and the element the
     * index as a zero based integer.
     */
    private HashMap<String, Integer> sampleIndexes = new HashMap<String, Integer>();
    /**
     * List of control samples. The index should be the same as for the sample
     * assignment.
     */
    private ArrayList<String> controlSamples;
    /**
     * The reporter method.
     */
    private ReporterMethod reporterMethod;
    /**
     * The normalization factors.
     */
    private NormalizationFactors normalizationFactors;

    /**
     * Constructor for the reporter ion quantification.
     *
     * @param methodUsed the method used for this quantification
     */
    public ReporterIonQuantification(QuantificationMethod methodUsed) {
        this.methodUsed = methodUsed;
        normalizationFactors = new NormalizationFactors();
    }

    /**
     * Assign a sample to an ion referenced by its static index.
     *
     * @param reporterName the name of the reporter ion
     * @param sample the sample
     */
    public void assignSample(String reporterName, Sample sample) {
        sampleAssignement.put(reporterName, sample);
    }
    
    /**
     * Returns the sample index for the given sample.
     * 
     * @param sampleReference the sample reference
     * @return the sample index for the given sample
     */
    public Integer getSampleIndex(String sampleReference) {
        if (sampleIndexes == null) {
            sampleIndexes = new HashMap<String, Integer>();
        }
        return sampleIndexes.get(sampleReference);
    }
    
    /**
     * Set the sample index for the given sample.
     * 
     * @param sampleReference the sample reference
     * @param index the index
     */
    public void setSampleIndex(String sampleReference, Integer index) {
        if (sampleIndexes == null) {
            sampleIndexes = new HashMap<String, Integer>();
        }
        sampleIndexes.put(sampleReference, index);
    }

    /**
     * This method returns the sample associated to the given ion.
     *
     * @param reporterIonName the static index of the reporter ion
     * @return the corresponding sample
     */
    public Sample getSample(String reporterIonName) {
        return sampleAssignement.get(reporterIonName);
    }

    /**
     * This method returns the reporter ion name associated to the given sample.
     * Null if not found.
     *
     * @param aSample the sample
     *
     * @return the static index of the associated ion
     */
    public String getReporterIndex(Sample aSample) {
        for (String ionName : sampleAssignement.keySet()) {
            if (sampleAssignement.get(ionName).isSameAs(aSample)) {
                return ionName;
            }
        }
        return null;
    }

    /**
     * Returns the reporter method used.
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
     * Returns the indexes of the samples labeled as control.
     *
     * @return the indexes of the samples labeled as control
     */
    public ArrayList<String> getControlSamples() {
        return controlSamples;
    }

    /**
     * Sets the indexes of the samples labeled as control.
     *
     * @param controlSamples the indexes of the samples to label as control
     */
    public void setControlSamples(ArrayList<String> controlSamples) {
        this.controlSamples = controlSamples;
    }

    /**
     * Returns a set containing the indexes of every sample.
     *
     * @return a set containing the indexes of every sample
     */
    public Set<String> getSampleIndexes() {
        return sampleAssignement.keySet();
    }

    /**
     * Returns the normalization factors.
     *
     * @return the normalization factors
     */
    public NormalizationFactors getNormalizationFactors() {
        return normalizationFactors;
    }

    /**
     * Sets the normalization factors.
     *
     * @param normalizationFactors the normalization factors
     */
    public void setNormalizationFactors(NormalizationFactors normalizationFactors) {
        this.normalizationFactors = normalizationFactors;
    }
}
