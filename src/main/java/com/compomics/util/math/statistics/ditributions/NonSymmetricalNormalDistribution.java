package com.compomics.util.math.statistics.ditributions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.statistics.Distribution;
import java.util.ArrayList;

/**
 * This class represents a non symmetrical normal distribution.
 *
 * @author Marc Vaudel
 */
public class NonSymmetricalNormalDistribution implements Distribution {

    /**
     * The standard deviation on the right of the distribution.
     */
    private double stdUp;
    /**
     * The standard deviation on the left of the distribution.
     */
    private double stdDown;
    /**
     * The mean
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
     * Returns the non-symmetrical distribution of the input list of double
     * calibrated on the median, 15.9% and 84.1% percentiles.
     *
     * @param input the input list
     * @return the non symmetrical distribution calibrated on the median, 15.9%
     * and 84.1% percentiles.
     */
    public static NonSymmetricalNormalDistribution getRobustNonSymmetricalNormalDistribution(ArrayList<Double> input) {
        return new NonSymmetricalNormalDistribution(BasicMathFunctions.median(input), BasicMathFunctions.percentile(input, 0.159), BasicMathFunctions.percentile(input, 0.841));
    }

    @Override
    public Double getProbabilityAt(double x) {
        if (x >= mean) {
            return distributionUp.getProbabilityAt(x);
        } else {
            return distributionDown.getProbabilityAt(x);
        }
    }

    @Override
    public Double getCumulativeProbabilityAt(double x) {
        if (x >= mean) {
            return distributionUp.getCumulativeProbabilityAt(x);
        } else {
            return distributionDown.getCumulativeProbabilityAt(x);
        }
    }

    @Override
    public Double getMaxValueForProbability(double p) {
        return distributionUp.getMaxValueForProbability(p);
    }

    @Override
    public Double getMinValueForProbability(double p) {
        return distributionDown.getMinValueForProbability(p);
    }

    @Override
    public Double getValueAtCumulativeProbability(double p) {
        if (p < 0.5) {
            return distributionDown.getValueAtCumulativeProbability(p);
        } else {
            return distributionUp.getValueAtCumulativeProbability(p);
        }
    }
}
