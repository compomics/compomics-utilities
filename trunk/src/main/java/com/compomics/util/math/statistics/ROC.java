package com.compomics.util.math.statistics;

import org.apache.commons.math.MathException;

/**
 * This class can be used to draw ROC curves.
 *
 * @author Marc Vaudel
 */
public class ROC {

    /**
     * The control distribution.
     */
    private Distribution distributionControl;
    /**
     * The patient distribution.
     */
    private Distribution distributionPatient;

    /**
     * Constructor. The patient distribution should be higher (to the right)
     * than the control distribution.
     *
     * @param distributionControl the control distribution
     * @param distributionPatient the patient distribution
     */
    public ROC(Distribution distributionControl, Distribution distributionPatient) {
        this.distributionControl = distributionControl;
        this.distributionPatient = distributionPatient;
    }

    /**
     * Returns the sensitivity at a given specificity, i.e., 1-type 2 error, the
     * number of true healthy for a given type 1 error, the number of false
     * healthy.
     *
     * @param specificity the specificity (0.1 is 10%)
     * 
     * @return the sensitivity at the given specificity (0.1 is 10%)
     * 
     * @throws org.apache.commons.math.MathException
     */
    public double getValueAt(double specificity) throws MathException {
        double x = distributionPatient.getValueAtCumulativeProbability(specificity);
        return distributionControl.getCumulativeProbabilityAt(x);
    }
}
