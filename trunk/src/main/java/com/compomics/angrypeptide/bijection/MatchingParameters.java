/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.angrypeptide.bijection;

/**
 * This class groups the matching parameters. Should be merged with the search parameters in fine.
 *
 * @author Marc
 */
public class MatchingParameters {
    
    private double minFragmentMass = 50;
    
    private int maxPrecCharge = 4;
    
    private double ms2Tolerance = 0.5;

    public double getMinFragmentMass() {
        return minFragmentMass;
    }

    public void setMinFragmentMass(double minFragmentMass) {
        this.minFragmentMass = minFragmentMass;
    }

    public int getMaxPrecCharge() {
        return maxPrecCharge;
    }

    public void setMaxPrecCharge(int maxPrecCharge) {
        this.maxPrecCharge = maxPrecCharge;
    }

    public double getMs2Tolerance() {
        return ms2Tolerance;
    }

    public void setMs2Tolerance(double ms2Tolerance) {
        this.ms2Tolerance = ms2Tolerance;
    }
    
    
    
}
