package com.compomics.util.math.statistics.distributions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.statistics.Distribution;
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
    private final double mean;
    /**
     * The standard deviation of the distribution.
     */
    private final double std;

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
    public double getProbabilityAt(double x) {

        if (std > 0) {

            double xNorm = (x - mean) / std;
            return Math.pow(Math.E, -Math.pow(xNorm, 2) / 2) / (Math.pow(2 * Math.PI, 0.5)); //@TODO: verify that xNorm is not too big/small

        } else if (std == 0) {

            if (x == mean) {

                return 1.0;

            } else {

                return 0.0;

            }
        }

        throw new IllegalArgumentException("std < 0");

    }

    @Override
    public double getMaxValueForProbability(double p) {

        if (std > 0) {

            if (p >= 1) {
                throw new IllegalArgumentException("Probability >= 1");
            }

            if (p <= 0) {
                throw new IllegalArgumentException("Probability <= 0");
            }

            double x = Math.pow(-2 * Math.log(p * Math.pow(2 * Math.PI, 0.5)), 0.5); //@TODO: verify that p is not too small

            return mean + std * x;

        } else if (std == 0) {

            return mean;

        }

        throw new IllegalArgumentException("std < 0");

    }

    @Override
    public double getMinValueForProbability(double p) {

        if (std > 0) {

            if (p >= 1) {
                throw new IllegalArgumentException("Probability >= 1");
            }

            if (p <= 0) {
                throw new IllegalArgumentException("Probability <= 0");
            }

            double x = Math.pow(-2 * Math.log(p * Math.pow(2 * Math.PI, 0.5)), 0.5); //@TODO: verify that p is not too small

            return mean - std * x;

        } else if (std == 0) {

            return mean;

        }

        throw new IllegalArgumentException("std < 0");
    }

    @Override
    public double getCumulativeProbabilityAt(double x) {

        if (std > 0) {

            try {

                return normalDistributionImpl.cumulativeProbability(x);

            } catch (MathException e) {

                throw new IllegalArgumentException(e);

            }

        } else if (std == 0) {
            // Note: this is my personal interpretation of the cumulative distribution in this case
            if (x < mean) {
                return 0.0;
            } else if (x == mean) {
                return 0.5;
            } else {
                return 1.0;
            }
        }

        throw new IllegalArgumentException("std < 0");
    }

    @Override
    public double getValueAtCumulativeProbability(double p) {

        if (std > 0) {

            try {

                return normalDistributionImpl.inverseCumulativeProbability(p);

            } catch (MathException e) {

                throw new IllegalArgumentException(e);

            }

        } else if (std == 0) {

            if (p < 0.5) {

                return Double.NEGATIVE_INFINITY;

            } else if (p == 0.5) {

                return mean;

            } else {

                return Double.POSITIVE_INFINITY;

            }
        }

        throw new IllegalArgumentException("std < 0");
    }

    @Override
    public double getDescendingCumulativeProbabilityAt(double x) {

        if (std > 0) {

            return 1.0 - getCumulativeProbabilityAt(x);

        } else if (std == 0) {

            if (x > mean) {

                return 0.0;

            } else if (x == mean) {

                return 0.5;

            } else {

                return 1.0;

            }
        }

        throw new IllegalArgumentException("std < 0");
    }

    @Override
    public double getSmallestCumulativeProbabilityAt(double x) {

        return x > mean
                ? getDescendingCumulativeProbabilityAt(x)
                : getCumulativeProbabilityAt(x);

    }

    @Override
    public double getValueAtDescendingCumulativeProbability(double p) {

        if (std > 0) {

            return getValueAtCumulativeProbability(1 - p);

        } else if (std == 0) {

            if (p < 0.5) {

                return Double.POSITIVE_INFINITY;

            } else if (p == 0.5) {

                return mean;

            } else {

                return Double.NEGATIVE_INFINITY;

            }
        }

        throw new IllegalArgumentException("std < 0");

    }
}
