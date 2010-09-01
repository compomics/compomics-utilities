package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.utils.ExperimentObject;

/**
 * This class models a proteomic sample.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 10:07:52 AM
 */
public class Sample extends ExperimentObject {

    /**
     * sample reference
     */
    private String reference;

    /**
     * Constructor for a sample
     * @param reference the reference of the sample
     */
    public Sample(String reference) {
        this.reference = reference;
    }

    /**
     * Getter for the sample reference
     * @return sample reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Comparator for samples. For now based on the reference
     * @param otherSample   an other sample
     * @return a boolean indicating if both samples are the same
     */
    public boolean isSameAs(Sample otherSample) {
        return reference.equals(otherSample.getReference());
    }
}
