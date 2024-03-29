package com.compomics.util.experiment.identification.peptide_fragmentation.models;

/**
 * Dummy intensity predictor that returns 1.0 for every ion.
 *
 * @author Marc Vaudel
 */
public class UniformFragmentation {

    /**
     * Empty default constructor
     */
    public UniformFragmentation() {
    }

    /**
     * Returns a default intensity of 1.0.
     *
     * @return a default intensity of 1.0
     */
    public static Double getIntensity() {
        return 1.0;
    }
}
