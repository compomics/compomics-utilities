/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification;

import java.io.Serializable;

/**
 * interface for the algorithm specific parameters
 *
 * @author Marc
 */
public interface IdentificationAlgorithmParameter extends Serializable {

    /**
     * Returns the identification algorithm.
     *
     * @return the identification algorithm
     */
    public Advocate getAlgorithm();

    /**
     * Indicates whether another identificationAlgorithmParameter has the same
     * parameters
     *
     * @param identificationAlgorithmParameter the other
     * identificationAlgorithmParameter
     * 
     * @return true if the algorithm and parameters are the same
     */
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter);
    /**
     * Returns the parameters as a string.
     *
     * @param html use HTML formatting
     * @return the parameters as a string
     */
    public String toString(boolean html);
}
