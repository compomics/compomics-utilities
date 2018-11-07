package com.compomics.util.experiment.quantification;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * An abstract quantification class.
 *
 * @author Marc Vaudel
 */
public abstract class Quantification extends ExperimentObject {

    /**
     * Empty default constructor
     */
    public Quantification() {
    }

    /**
     * The implemented quantification methods.
     */
    public enum QuantificationMethod {

        /**
         * relative quantification by comparison of peptide intensities
         * extracted from the MS1 map or XIC
         */
        MS1_LABEL_FREE,
        /**
         * Relative quantification by comparison of labeled versions of the
         * peptides in the same MS1 map (like SILAC)
         */
        MS1_LABEL,
        /**
         * Relative or absolute quantification by counting identified spectra
         * (like emPAI or NSAF)
         */
        SPECTRUM_COUNTING,
        /**
         * Relative quantification by comparison of reporter ion intensities
         */
        REPORTER_IONS
    }
    /**
     * The quantification method used
     */
    protected QuantificationMethod methodUsed;

    /**
     * getter for the method used
     *
     * @return the method used
     */
    public QuantificationMethod getMethodUsed() {
        return methodUsed;
    }

    /**
     * setter for the method used
     *
     * @param methodUsed the method used
     */
    public void setMethodUsed(QuantificationMethod methodUsed) {
        this.methodUsed = methodUsed;
    }
}
