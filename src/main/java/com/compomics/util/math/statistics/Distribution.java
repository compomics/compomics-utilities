/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.math.statistics;

/**
 * This class represents a statistical distribution model like a gaussian distribution
 *
 * @author Marc
 */
public interface Distribution {
    
    /**
     * Returns the density function value at a given position
     * 
     * @param x the position of interest
     * 
     * @return the value of the density function at the give position
     */
    public Double getProbabilityAt(double x);
    
    /**
     * Returns the cumulative density function value at a given position
     * 
     * @param x the position of interest
     * 
     * @return the value of the density function at the give position
     */
    public Double getCumulativeProbabilityAt(double x);
    
    /**
     * The value after which the density function will be smaller than p
     * 
     * @param p the probability of interest
     * 
     * @return the value after which the density function will be smaller than p
     */
    public Double getMaxValueForProbability(double p);
    /**
     * The value before which the density function will be smaller than p
     * 
     * @param p the probability of interest
     * 
     * @return the value before which the density function will be smaller than p
     */
    public Double getMinValueForProbability(double p);
    /**
     * The value after which the cumulative density function will be smaller than p
     * 
     * @param p the probability of interest
     * 
     * @return the value after which the cumulative density function will be smaller than p
     */
    public Double getValueAtCumulativeProbability(double p);
}
