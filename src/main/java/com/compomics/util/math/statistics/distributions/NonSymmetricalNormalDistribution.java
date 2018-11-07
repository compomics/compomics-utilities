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
     * 
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
     * 
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
    public double getProbabilityAt(double x) {
        
        return x >= mean ?
            distributionUp.getProbabilityAt(x)
                : distributionDown.getProbabilityAt(x);
    }

    @Override
    public double getCumulativeProbabilityAt(double x) {
        
        return x >= mean ?
                distributionUp.getCumulativeProbabilityAt(x)
        : distributionDown.getCumulativeProbabilityAt(x);
    }

    @Override
    public double getDescendingCumulativeProbabilityAt(double x) {
        
        return x > mean ? 
                distributionUp.getDescendingCumulativeProbabilityAt(x)
                : distributionDown.getDescendingCumulativeProbabilityAt(x);
        
    }

    @Override
    public double getSmallestCumulativeProbabilityAt(double x) {
        
        return x > mean ? 
                getDescendingCumulativeProbabilityAt(x)
                : getCumulativeProbabilityAt(x);
    }

    @Override
    public double getMaxValueForProbability(double p) {
        
        return distributionUp.getMaxValueForProbability(p);
        
    }

    @Override
    public double getMinValueForProbability(double p) {
        
        return distributionDown.getMinValueForProbability(p);
        
    }

    @Override
    public double getValueAtCumulativeProbability(double p) {
        
        return p < 0.5 ?
                distributionDown.getValueAtCumulativeProbability(p)
                : distributionUp.getValueAtCumulativeProbability(p);
        
    }

    @Override
    public double getValueAtDescendingCumulativeProbability(double p) {
        
        return p < 0.5 ? 
                distributionUp.getValueAtDescendingCumulativeProbability(p)
                : distributionDown.getValueAtDescendingCumulativeProbability(p);
        
    }
}
