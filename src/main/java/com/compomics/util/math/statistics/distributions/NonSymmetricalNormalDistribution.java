package com.compomics.util.math.statistics.distributions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.statistics.Distribution;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    private final double stdUp;
    /**
     * The standard deviation on the left of the distribution.
     */
    private final double stdDown;
    /**
     * The mean.
     */
    private final double mean;
    /**
     * Distribution equivalent to the right of the distribution.
     */
    private final NormalDistribution distributionUp;
    /**
     * Distribution equivalent to the left of the distribution.
     */
    private final NormalDistribution distributionDown;

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
        
        ArrayList<Double> sortedInput = input.stream().sorted().collect(Collectors.toCollection(ArrayList::new));
        
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
    public Double getDescendingCumulativeProbabilityAt(double x) {
        
        if (x > mean) {
            return distributionUp.getDescendingCumulativeProbabilityAt(x);
        } else {
            return distributionDown.getDescendingCumulativeProbabilityAt(x);
        }
    }

    @Override
    public Double getSmallestCumulativeProbabilityAt(double x) {
        
        if (x > mean) {
            return getDescendingCumulativeProbabilityAt(x);
        } else {
            getCumulativeProbabilityAt(x);
        }
        return 0.5;
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

    @Override
    public Double getValueAtDescendingCumulativeProbability(double p) {
        
        if (p < 0.5) {
            return distributionUp.getValueAtDescendingCumulativeProbability(p);
        } else {
            return distributionDown.getValueAtDescendingCumulativeProbability(p);
        }
    }
}
