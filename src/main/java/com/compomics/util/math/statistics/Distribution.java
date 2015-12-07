package com.compomics.util.math.statistics;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import org.apache.commons.math.MathException;

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
     * @param mathContext the math context to use for calculation
     * 
     * @return the value of the density function at the give position
     */
    public BigDecimal getProbabilityAt(double x, MathContext mathContext);

    /**
     * Returns the cumulative density function value at a given position.
     *
     * @param x the position of interest
     * @param mathContext the math context to use for calculation
     * 
     * @return the value of the density function at the give position
     * 
     * @throws MathException if a MathException occurs
     */
    public BigDecimal getCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException;

    /**
     * Returns the cumulative density function value at a given position when
     * starting from the high values.
     *
     * @param x the position of interest
     * @param mathContext the math context to use for calculation
     * 
     * @return the value of the density function at the give position
     * 
     * @throws MathException if a MathException occurs
     */
    public BigDecimal getDescendingCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException;

    /**
     * Returns the cumulative density function value at a given position, starting from the low values if before the median, from the high otherwise.
     *
     * @param x the position of interest
     * @param mathContext the math context to use for calculation
     * 
     * @return the value of the density function at the give position
     * 
     * @throws MathException if a MathException occurs
     */
    public BigDecimal getSmallestCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException;

    /**
     * The value after which the density function will be smaller than p.
     *
     * @param p the probability of interest
     * @param mathContext the math context to use for calculation
     * 
     * @return the value after which the density function will be smaller than p
     */
    public BigDecimal getMaxValueForProbability(double p, MathContext mathContext);

    /**
     * The value before which the density function will be smaller than p.
     *
     * @param p the probability of interest
     * @param mathContext the math context to use for calculation
     * 
     * @return the value before which the density function will be smaller than
     * p
     */
    public BigDecimal getMinValueForProbability(double p, MathContext mathContext);

    /**
     * The value after which the cumulative density function will be smaller
     * than p.
     *
     * @param p the probability of interest
     * @param mathContext the math context to use for calculation
     * 
     * @return the value after which the cumulative density function will be
     * smaller than p
     * 
     * @throws MathException if a MathException occurs
     */
    public BigDecimal getValueAtCumulativeProbability(double p, MathContext mathContext) throws MathException;

    /**
     * The value after which the cumulative density function will be smaller
     * than p when starting from high values.
     *
     * @param p the probability of interest
     * @param mathContext the math context to use for calculation
     * 
     * @return the value after which the cumulative density function will be
     * smaller than p
     * 
     * @throws MathException if a MathException occurs
     */
    public BigDecimal getValueAtDescendingCumulativeProbability(double p, MathContext mathContext) throws MathException;
}
