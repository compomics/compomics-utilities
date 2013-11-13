/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * Spectrum identification assumption made by an identification algorithm
 *
 * @author Marc
 */
public class SpectrumIdentificationAssumption extends ExperimentObject {
    /**
     * The rank of the peptide assumption for the concerned spectrum.
     */
    protected int rank;
    /**
     * The advocate supporting this assumption.
     */
    protected int advocate;
    /**
     * The charge used for identification.
     */
    protected Charge identificationCharge;
    /**
     * The (advocate specific) score used to rank this assumption
     */
    protected double score;
    /**
     * the identification file.
     */
    protected String identificationFile;
    /**
     * Get the identification rank.
     *
     * @return the identification rank
     */
    public int getRank() {
        return rank;
    }
    /**
     * Set the rank of the PeptideAssumption.
     *
     * @param aRank the rank of the PeptideAssumptio
     */
    public void setRank(int aRank) {
        rank = aRank;
    }
    /**
     * Get the used advocate.
     *
     * @return the advocate index
     */
    public int getAdvocate() {
        return advocate;
    }
    /**
     * Returns the score assigned by the advocate.
     *
     * @return the score
     */
    public double getScore() {
        return score;
    }
    /**
     * Returns the identification file.
     *
     * @return the identification file
     */
    public String getIdentificationFile() {
        return identificationFile;
    }

    /**
     * Returns the charge used for identification.
     *
     * @return the charge used for identification
     */
    public Charge getIdentificationCharge() {
        return identificationCharge;
    }
    
}
