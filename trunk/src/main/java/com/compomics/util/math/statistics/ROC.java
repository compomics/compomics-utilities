/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.math.statistics;

/**
 * This class can be used to draw roc curves
 *
 * @author Marc
 */
public class ROC {
    
    /**
     * the control distribution
     */
    private Distribution distributionControl;
    /**
     * The patient distribution
     */
    private Distribution distributionPatient;
    /**
     * Constructor. The patient distribution should be higher (to the right) than the control distribution.
     * 
     * @param distributionControl the control distribution
     * @param distributionPatient the patient distribution
     */
    public ROC(Distribution distributionControl, Distribution distributionPatient) {
        this.distributionControl = distributionControl;
        this.distributionPatient = distributionPatient;
    }
    /**
     * Returns the sensitivity at a given specificity, ie 1-type 2 error, the number of true healthy for a given type 1 error, the number of false healthy.
     * 
     * @param specificity the specificity (0.1 is 10%);
     * @return the sensitivity at the given specificity (0.1 is 10%);
     */
    public double getValueAt(double specificity) {
        double x = distributionPatient.getMinValueForProbability(specificity);
        return distributionControl.getCumulativeProbabilityAt(x);
    }
}
