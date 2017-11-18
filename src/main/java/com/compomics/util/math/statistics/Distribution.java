package com.compomics.util.math.statistics;

import java.io.Serializable;

/**
 * This class represents a statistical distribution model like a Gaussian
 * distribution.
 *
 * @author Marc Vaudel
 */
public interface Distribution extends Serializable {

    /**
     * Returns the density function value at a given position.
     *
     * @param x the position of interest
     * 
     * @return the value of the density function at the give position
     */
    public double getProbabilityAt(double x);

    /**
     * Returns the cumulative density function value at a given position.
     *
     * @param x the position of interest
     * 
     * @return the value of the density function at the give position
     */
    public double getCumulativeProbabilityAt(double x);

    /**
     * Returns the cumulative density function value at a given position when
     * starting from the high values.
     *
     * @param x the position of interest
     * 
     * @return the value of the density function at the give position
     */
    public double getDescendingCumulativeProbabilityAt(double x);

    /**
     * Returns the cumulative density function value at a given position, starting from the low values if before the median, from the high otherwise.
     *
     * @param x the position of interest
     * 
     * @return the value of the density function at the give position
     */
    public double getSmallestCumulativeProbabilityAt(double x);

    /**
     * The value after which the density function will be smaller than p.
     *
     * @param p the probability of interest
     * 
     * @return the value after which the density function will be smaller than p
     */
    public double getMaxValueForProbability(double p);

    /**
     * The value before which the density function will be smaller than p.
     *
     * @param p the probability of interest
     * 
     * @return the value before which the density function will be smaller than
     * p
     */
    public double getMinValueForProbability(double p);

    /**
     * The value after which the cumulative density function will be smaller
     * than p.
     *
     * @param p the probability of interest
     * 
     * @return the value after which the cumulative density function will be
     * smaller than p
     */
    public double getValueAtCumulativeProbability(double p);

    /**
     * The value after which the cumulative density function will be smaller
     * than p when starting from high values.
     *
     * @param p the probability of interest
     * 
     * @return the value after which the cumulative density function will be
     * smaller than p
     */
    public double getValueAtDescendingCumulativeProbability(double p);
}
