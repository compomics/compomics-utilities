package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.quantification.Quantification;
import com.compomics.util.experiment.biology.Sample;

import java.util.HashMap;

/**
 * This class will contain quantification results.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 1:42:21 PM
 */
public class ReporterIonQuantification extends Quantification {

    /**
     * The sample assignement to the various ions indexed by their static indexes
     */
    private HashMap<Integer, Sample> sampleAssignement = new HashMap<Integer, Sample>();
    /**
     * The reference label indexed by the reporter ion index
     */
    private int referenceLabel;
    /**
     * The reporter method
     */
    private ReporterMethod reporterMethod;

    /**
     * Constructor for the reporter ion quantification
     * @param methodUsed    the method used for this quantification
     */
    public ReporterIonQuantification(QuantificationMethod methodUsed) {
        this.methodUsed = methodUsed;
    }

    /**
     * assign a sample to an ion referenced by its static index
     * @param reporterIndex     the index of the ion
     * @param sample            the sample
     */
    public void assignSample(int reporterIndex, Sample sample) {
        sampleAssignement.put(reporterIndex, sample);
    }

    /**
     * This method returns the sample associated to the given ion
     * @param reporterIndex     the static index of the reporter ion
     * @return the corresponding sample
     */
    public Sample getSample(int reporterIndex) {
        return sampleAssignement.get(reporterIndex);
    }

    /**
     * This method returns the ion associated to the given sample
     * @param aSample   the sample
     * @return the static index of the associated ion
     */
    public Integer getReporterIndex(Sample aSample) {
        for(int index : sampleAssignement.keySet()) {
            if (sampleAssignement.get(index).isSameAs(aSample)) {
                return index;
            }
        }
        return null;
    }

    /**
     * returns the reporter method used
     * @return the method used
     */
    public ReporterMethod getReporterMethod() {
        return reporterMethod;
    }

    /**
     * Sets the reporter method used
     * @param reporterMethod    the reporter method used
     */
    public void setMethod(ReporterMethod reporterMethod) {
        this.reporterMethod = reporterMethod;
    }

    /**
     * Returns the reference label indexed by the corresponding reporter ion
     * @return the index of the reporter ion corresponding to the reference label
     */
    public int getReferenceLabel() {
        return referenceLabel;
    }

    /**
     * Sets the reference label indexed by the corresponding reporter ion index
     * @param referenceLabel    the index of the reporter ion corresponding to the reference label
     */
    public void setReferenceLabel(int referenceLabel) {
        this.referenceLabel = referenceLabel;
    }
}
