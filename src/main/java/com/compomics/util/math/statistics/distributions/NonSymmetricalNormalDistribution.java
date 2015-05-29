package com.compomics.util.math.statistics.distributions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.statistics.Distribution;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.math.MathException;

/**
 * This class represents a non symmetrical normal distribution.
 *
 * @author Marc Vaudel
 */
public class NonSymmetricalNormalDistribution implements Distribution {

    /**
     * Serial version number for backward compatibility.
     */
    static final long serialVersionUID = -5258258835569357886L;
    /**
     * The standard deviation on the right of the distribution.
     */
    private double stdUp;
    /**
     * The standard deviation on the left of the distribution.
     */
    private double stdDown;
    /**
     * The mean.
     */
    private double mean;
    /**
     * Distribution equivalent to the right of the distribution.
     */
    private NormalDistribution distributionUp;
    /**
     * Distribution equivalent to the left of the distribution.
     */
    private NormalDistribution distributionDown;

    /**
     * Constructor.
     *
     * @param mean the mean
     * @param stdDown the standard deviation on the left of the mean
     * @param stdUp the standard deviation on the right of the mean
     */
    public NonSymmetricalNormalDistribution(double mean, double stdDown, double stdUp) {
        this.mean = mean;
        this.stdDown = stdDown;
        this.stdUp = stdUp;
        this.distributionDown = new NormalDistribution(mean, stdDown);
        this.distributionUp = new NormalDistribution(mean, stdUp);
    }

    /**
     * Returns the standard deviation to the right of the distribution.
     * 
     * @return the standard deviation to the right of the distribution
     */
    public double getStdUp() {
        return stdUp;
    }

    /**
     * Returns the standard deviation to the left of the distribution.
     * 
     * @return the standard deviation to the left of the distribution
     */
    public double getStdDown() {
        return stdDown;
    }

    /**
     * Returns the mean of the distribution.
     * 
     * @return the mean of the distribution
     */
    public double getMean() {
        return mean;
    }

    /**
     * Returns the non-symmetrical distribution of the input list of double
     * calibrated on the median, 15.9% and 84.1% percentiles.
     *
     * @param input the input list
     * @return the non symmetrical distribution calibrated on the median, 15.9%
     * and 84.1% percentiles.
     */
    public static NonSymmetricalNormalDistribution getRobustNonSymmetricalNormalDistribution(ArrayList<Double> input) {
        ArrayList<Double> sortedInput = new ArrayList<Double>(input);
        Collections.sort(sortedInput);
        return getRobustNonSymmetricalNormalDistributionFromSortedList(sortedInput);
    }

    /**
     * Returns the non-symmetrical distribution of the input list of double
     * calibrated on the median, 15.9% and 84.1% percentiles.
     *
     * @param input the input list
     * @return the non symmetrical distribution calibrated on the median, 15.9%
     * and 84.1% percentiles.
     */
    public static NonSymmetricalNormalDistribution getRobustNonSymmetricalNormalDistributionFromSortedList(ArrayList<Double> input) {
        double median = BasicMathFunctions.medianSorted(input);
        double percentileDown = BasicMathFunctions.percentileSorted(input, 0.159);
        double percentileUp = BasicMathFunctions.percentileSorted(input, 0.841);
        return new NonSymmetricalNormalDistribution(median, median - percentileDown, percentileUp - median);
    }

    @Override
    public BigDecimal getProbabilityAt(double x, MathContext mathContext) {
        if (x >= mean) {
            return distributionUp.getProbabilityAt(x, mathContext);
        } else {
            return distributionDown.getProbabilityAt(x, mathContext);
        }
    }

    @Override
    public BigDecimal getCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException {
        if (x >= mean) {
            return distributionUp.getCumulativeProbabilityAt(x, mathContext);
        } else {
            return distributionDown.getCumulativeProbabilityAt(x, mathContext);
        }
    }

    @Override
    public BigDecimal getMaxValueForProbability(double p, MathContext mathContext) {
        return distributionUp.getMaxValueForProbability(p, mathContext);
    }

    @Override
    public BigDecimal getMinValueForProbability(double p, MathContext mathContext) {
        return distributionDown.getMinValueForProbability(p, mathContext);
    }

    @Override
    public BigDecimal getValueAtCumulativeProbability(double p, MathContext mathContext) throws MathException {
        if (p < 0.5) {
            return distributionDown.getValueAtCumulativeProbability(p, mathContext);
        } else {
            return distributionUp.getValueAtCumulativeProbability(p, mathContext);
        }
    }

    @Override
    public BigDecimal getDescendingCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException {
        if (x > mean) {
            return distributionUp.getDescendingCumulativeProbabilityAt(x, mathContext);
        } else {
            return distributionDown.getDescendingCumulativeProbabilityAt(x, mathContext);
        }
    }

    @Override
    public BigDecimal getValueAtDescendingCumulativeProbability(double p, MathContext mathContext) throws MathException {
        if (p < 0.5) {
            return distributionUp.getValueAtDescendingCumulativeProbability(p, mathContext);
        } else {
            return distributionDown.getValueAtDescendingCumulativeProbability(p, mathContext);
        }
    }
}
