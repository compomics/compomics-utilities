package com.compomics.util.math.statistics.distributions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.statistics.Distribution;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

/**
 * This class represents a normal distribution. A dirac distribution if the
 * standard deviation is null.
 *
 * @author Marc Vaudel
 */
public class NormalDistribution implements Distribution {

    /**
     * Serial version number for backward compatibility.
     */
    static final long serialVersionUID = -4944773548279233917L;
    /**
     * The apache normal distribution implementation.
     */
    private NormalDistributionImpl normalDistributionImpl;
    /**
     * The mean of the distribution.
     */
    private double mean;
    /**
     * The standard deviation of the distribution.
     */
    private double std;

    /**
     * Constructor.
     *
     * @param mean the mean
     * @param std the standard deviation
     */
    public NormalDistribution(double mean, double std) {
        this.mean = mean;
        this.std = std;
        if (std > 0) {
            this.normalDistributionImpl = new NormalDistributionImpl(mean, std);
        }
    }

    /**
     * Returns the normal distribution corresponding to a given list of double
     * calibrated on mean and standard deviation.
     *
     * @param input the input as a list of double
     *
     * @return the normal distribution calibrated on the mean and standard
     * deviation
     */
    public static NormalDistribution getNormalDistribution(ArrayList<Double> input) {
        return new NormalDistribution(BasicMathFunctions.mean(input), BasicMathFunctions.std(input));
    }

    /**
     * Returns the normal distribution corresponding to a given list of double
     * calibrated on median and 34.1% percentile to median distance
     *
     * @param input the input as list of double
     *
     * @return a normal distribution calibrated on median and 34.1% percentile
     * to median distance
     */
    public static NormalDistribution getRobustNormalDistribution(ArrayList<Double> input) {
        double std = (BasicMathFunctions.percentile(input, 0.841) - BasicMathFunctions.percentile(input, 0.159)) / 2;
        return new NormalDistribution(BasicMathFunctions.median(input), std);
    }

    @Override
    public BigDecimal getProbabilityAt(double x, MathContext mathContext) {
        if (std == 0) {
            if (x == mean) {
                return BigDecimal.ONE;
            } else {
                return BigDecimal.ZERO;
            }
        }
        if (mathContext.getPrecision() > 300) {
            throw new UnsupportedOperationException("Not implemented for resolution " + mathContext.getPrecision() + ".");
        }
        double xNorm = (x - mean) / std;
        double result = Math.pow(Math.E, -Math.pow(xNorm, 2) / 2) / (Math.pow(2 * Math.PI, 0.5)); //@TODO: verify that xNorm is not too big/small for the desired precision
        return new BigDecimal(result, mathContext);
    }

    @Override
    public BigDecimal getMaxValueForProbability(double p, MathContext mathContext) {
        if (std == 0) {
            return new BigDecimal(mean, mathContext);
        }
        if (p >= 1) {
            throw new IllegalArgumentException("Probability >= 1");
        }
        if (p <= 0) {
            throw new IllegalArgumentException("Probability <= 0");
        }
        if (mathContext.getPrecision() > 300) {
            throw new UnsupportedOperationException("Not implemented for resolution " + mathContext.getPrecision() + ".");
        }
        double x = Math.pow(-2 * Math.log(p * Math.pow(2 * Math.PI, 0.5)), 0.5); //@TODO: verify that p is not too small
        return new BigDecimal(mean + std * x, mathContext);
    }

    @Override
    public BigDecimal getMinValueForProbability(double p, MathContext mathContext) {
        if (std == 0) {
            return new BigDecimal(mean, mathContext);
        }
        if (p >= 1) {
            throw new IllegalArgumentException("Probability >= 1");
        }
        if (p <= 0) {
            throw new IllegalArgumentException("Probability <= 0");
        }
        if (mathContext.getPrecision() > 300) {
            throw new UnsupportedOperationException("Not implemented for resolution " + mathContext.getPrecision() + ".");
        }
        double x = Math.pow(-2 * Math.log(p * Math.pow(2 * Math.PI, 0.5)), 0.5); //@TODO: verify that p is not too small
        return new BigDecimal(mean - std * x, mathContext);
    }

    @Override
    public BigDecimal getCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException {
        if (std == 0) {
            // Note: this is my personal interpretation of the cumulative distribution in this case
            if (x < mean) {
                return BigDecimal.ZERO;
            } else if (x == mean) {
                return new BigDecimal(0.5, mathContext);
            } else {
                return BigDecimal.ONE;
            }
        }
        if (mathContext.getPrecision() > 300) {
            throw new UnsupportedOperationException("Not implemented for resolution " + mathContext.getPrecision() + ".");
        }
        return new BigDecimal(normalDistributionImpl.cumulativeProbability(x), mathContext);
    }

    @Override
    public BigDecimal getValueAtCumulativeProbability(double p, MathContext mathContext) throws MathException {
        if (std == 0) {
            // Note: this is my personal interpretation of the cumulative distribution in this case
            if (p < 0.5) {
                return new BigDecimal(Double.NEGATIVE_INFINITY, mathContext);
            } else if (p == 0.5) {
                return new BigDecimal(mean, mathContext);
            } else {
                return new BigDecimal(Double.POSITIVE_INFINITY, mathContext);
            }
        }
        if (mathContext.getPrecision() > 300) {
            throw new UnsupportedOperationException("Not implemented for resolution " + mathContext.getPrecision() + ".");
        }
        return new BigDecimal(normalDistributionImpl.inverseCumulativeProbability(p), mathContext);
    }

    @Override
    public BigDecimal getDescendingCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException {
        if (std == 0) {
            // Note: this is my personal interpretation of the cumulative distribution in this case
            if (x > mean) {
                return BigDecimal.ZERO;
            } else if (x == mean) {
                return new BigDecimal(0.5, mathContext);
            } else {
                return BigDecimal.ONE;
            }
        }
        return BigDecimal.ONE.subtract(getCumulativeProbabilityAt(x, mathContext));
    }

    @Override
    public BigDecimal getValueAtDescendingCumulativeProbability(double p, MathContext mathContext) throws MathException {
        if (std == 0) {
            // Note: this is my personal interpretation of the cumulative distribution in this case
            if (p < 0.5) {
                return new BigDecimal(Double.POSITIVE_INFINITY, mathContext);
            } else if (p == 0.5) {
                return new BigDecimal(mean, mathContext);
            } else {
                return new BigDecimal(Double.NEGATIVE_INFINITY, mathContext);
            }
        }
        return getValueAtCumulativeProbability(1 - p, mathContext);
    }
}
