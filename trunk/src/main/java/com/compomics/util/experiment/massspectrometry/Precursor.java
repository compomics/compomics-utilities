package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class modilizes the precursor.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 2:37:50 PM
 */
public class Precursor extends ExperimentObject {

    /**
     * The retention time when the precursor was isolated
     */
    private double rt;

    /**
     * The measured m/z of the precursor
     */
    private double mz;

    /**
     * The charge of the precursor
     */
    private Charge charge;

    /**
     * Constructor for the precursor
     * @param rt
     * @param mz
     * @param charge
     */
    public Precursor(double rt, double mz, Charge charge) {
        this.rt = rt;
        this.mz = mz;
        this.charge = charge;
    }

    /**
     * getter for the retention time
     * @return precursor retention time
     */
    public double getRt() {
        return rt;
    }

    /**
     * getter for the m/z
     * @return precursor m/z
     */
    public double getMz() {
        return mz;
    }

    /**
     * Getter for the charge
     * @return precursor charge
     */
    public Charge getCharge() {
        return charge;
    }
}
