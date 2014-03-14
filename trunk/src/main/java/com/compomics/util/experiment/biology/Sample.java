package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class models a proteomic sample. 
 * 
 * @author Marc Vaudel
 */
public class Sample extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 303393644914835325L;
    /**
     * Sample reference.
     */
    private String reference;

    /**
     * Constructor for a sample.
     *
     * @param reference the reference of the sample
     */
    public Sample(String reference) {
        this.reference = reference;
    }

    /**
     * Setter for the reference of a sample.
     *
     * @param newReference the new reference
     */
    public void setReference(String newReference) {
        this.reference = newReference;
    }

    /**
     * Getter for the sample reference.
     *
     * @return sample reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Comparator for samples. For now based on the reference.
     *
     * @param otherSample an other sample
     * @return a boolean indicating if both samples are the same
     */
    public boolean isSameAs(Sample otherSample) {
        return reference.equals(otherSample.getReference());
    }
}
