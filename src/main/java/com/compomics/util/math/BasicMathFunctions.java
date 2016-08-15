package com.compomics.util.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.math.util.FastMath;

/**
 * Class used to perform basic mathematical functions.
 *
 * @author Marc Vaudel
 */
public class BasicMathFunctions {

    /**
     * Cache for the base used for the log.
     */
    private static double logBase = 0;
    /**
     * Cache for the logarithm value of the base used for the log.
     */
    private static double logBaseValue;
    /**
     * Cache for factorials.
     */
    private static final HashMap<Integer, Long> factorialsCache = new HashMap<Integer, Long>();

    /**
     * Returns n! as a long. Returns null if the capacity of a long is not
     * sufficient (ie n higher than 20).
     *
     * @param n a given integer
     *
     * @return the corresponding factorial
     */
    public static Long factorial(Integer n) {
        if (n < 1) {
            throw new ArithmeticException("Attempting to calculate the factorial of a negative number.");
        } else if (n > 20) {
            return null;
        } else if (n <= 1) {
            return (long) 1;
        } else {
            Long result = factorialsCache.get(n);
            if (result == null) {
                result = estimateFactorial(n);
            }
            return result;
        }
    }

    /**
     * Estimates factorial in a synchronous method as part of the factorial
     * method.
     *
     * @param n a given integer
     *
     * @return the corresponding factorial
     */
    private static synchronized Long estimateFactorial(Integer n) {
        Long result = factorialsCache.get(n);
        if (result == null) {
            synchronized (BasicMathFunctions.class) {
                result = factorialsCache.get(n);
                if (result == null) {
                    result = factorial(n - 1) * n;
                    factorialsCache.put(n, result);
                }
            }
        }
        return result;
    }

    /**
     * Returns n!/k!, null if it cannot fit in a long.
     *
     * @param n a given integer
     * @param k a given integer
     *
     * @return the corresponding factorial
     */
    public static Long factorial(Integer n, Integer k) {
        if (n < k) {
            throw new ArithmeticException("n < k in n!/k!.");
        }
        if (n.equals(k)) {
            return (long) 1;
        } else {
            if (n < 20) {
                return factorial(n) / factorial(k);
            }
            Long nMinusOne = factorial(n - 1, k);
            if (nMinusOne == null || nMinusOne > Long.MAX_VALUE / n) {
                return null;
            } else {
                return nMinusOne * n;
            }
        }
    }

    /**
     * Returns the number of k-combinations in a set of n elements. If n!/k!
     * cannot fit in a long, null is returned, use BigDecimal instead (see
     * BigFunctions).
     *
     * @param k the number of k-combinations
     * @param n the number of elements
     *
     * @return the number of k-combinations in a set of n elements
     */
    public static Long getCombination(int k, int n) {
        if (k == 0) {
            return (long) 1;
        } else if (k < n) {
            Long kInN = factorial(n, k);
            Long nMinK = factorial(n - k);
            if (kInN == null || nMinK == null) {
                return null;
            } else {
                return kInN / kInN;
            }
        } else if (k == n) {
            return (long) 1;
        } else {
            throw new IllegalArgumentException("n>k in combination.");
        }
    }

    /**
     * Method to estimate the median.
     *
     * @param ratios array of double
     * @return median of the input
     */
    public static double median(double[] ratios) {
        Arrays.sort(ratios);
        int length = ratios.length;
        if (ratios.length == 1) {
            return ratios[0];
        }
        if (length % 2 == 1) {
            return ratios[(length - 1) / 2];
        } else {
            return (ratios[length / 2] + ratios[(length) / 2 - 1]) / 2;
        }
    }

    /**
     * Method to estimate the median.
     *
     * @param input ArrayList of double
     * @return median of the input
     */
    public static double median(ArrayList<Double> input) {
        return percentile(input, 0.5);
    }

    /**
     * Method to estimate the median of a sorted list.
     *
     * @param input ArrayList of double
     * @return median of the input
     */
    public static double medianSorted(ArrayList<Double> input) {
        return percentileSorted(input, 0.5);
    }

    /**
     * Returns the desired percentile in a given array of double. If the
     * percentile is between two values a linear interpolation is done.
     *
     * @param input the input array
     * @param percentile the desired percentile. 0.01 returns the first
     * percentile. 0.5 returns the median.
     *
     * @return the desired percentile
     */
    public static double percentile(double[] input, double percentile) {
        if (percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException("Incorrect input for percentile: " + percentile + ". Input must be between 0 and 1.");
        }
        Arrays.sort(input);
        int length = input.length;
        if (length == 0) {
            throw new IllegalArgumentException("Attempting to estimate the percentile of an empty list.");
        }
        if (length == 1) {
            return input[0];
        }
        double indexDouble = percentile * (length - 1);
        int index = (int) (indexDouble);
        double valueAtIndex = input[index];
        double rest = indexDouble - index;
        if (index == input.length - 1 || rest == 0) {
            return valueAtIndex;
        }
        return valueAtIndex + rest * (input[index + 1] - valueAtIndex);
    }

    /**
     * Returns the desired percentile in a given list of double. If the
     * percentile is between two values a linear interpolation is done. Note:
     * When calculating multiple percentiles on the same list, it is advised to
     * sort it and use percentileSorted.
     *
     * @param input the input list
     * @param percentile the desired percentile. 0.01 returns the first
     * percentile. 0.5 returns the median.
     *
     * @return the desired percentile
     */
    public static double percentile(ArrayList<Double> input, double percentile) {
        if (input == null) {
            throw new IllegalArgumentException("Attempting to estimate the percentile of a null object.");
        }
        int length = input.size();
        if (length == 0) {
            throw new IllegalArgumentException("Attempting to estimate the percentile of an empty list.");
        }
        ArrayList<Double> sortedInput = new ArrayList<Double>(input);
        Collections.sort(sortedInput);
        return percentileSorted(sortedInput, percentile);
    }

    /**
     * Returns the desired percentile in a given list of double. If the
     * percentile is between two values a linear interpolation is done. The list
     * must be sorted prior to submission.
     *
     * @param input the input list
     * @param percentile the desired percentile. 0.01 returns the first
     * percentile. 0.5 returns the median.
     *
     * @return the desired percentile
     */
    public static double percentileSorted(ArrayList<Double> input, double percentile) {
        if (percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException("Incorrect input for percentile: " + percentile + ". Input must be between 0 and 1.");
        }
        if (input == null) {
            throw new IllegalArgumentException("Attempting to estimate the percentile of a null object.");
        }
        int length = input.size();
        if (length == 0) {
            throw new IllegalArgumentException("Attempting to estimate the percentile of an empty list.");
        }
        if (length == 1) {
            return input.get(0);
        }
        double indexDouble = percentile * (length - 1);
        int index = (int) (indexDouble);
        double valueAtIndex = input.get(index);
        double rest = indexDouble - index;
        if (index == input.size() - 1 || rest == 0) {
            return valueAtIndex;
        }
        return valueAtIndex + rest * (input.get(index + 1) - valueAtIndex);
    }

    /**
     * Method estimating the median absolute deviation.
     *
     * @param ratios array of doubles
     * @return the mad of the input
     */
    public static double mad(double[] ratios) {
        double[] deviations = new double[ratios.length];
        double med = median(ratios);
        for (int i = 0; i < ratios.length; i++) {
            deviations[i] = Math.abs(ratios[i] - med);
        }
        return median(deviations);
    }

    /**
     * Method estimating the median absolute deviation.
     *
     * @param ratios array of doubles
     * @return the mad of the input
     */
    public static double mad(ArrayList<Double> ratios) {
        double[] deviations = new double[ratios.size()];
        double med = median(ratios);
        for (int i = 0; i < ratios.size(); i++) {
            deviations[i] = Math.abs(ratios.get(i) - med);
        }
        return median(deviations);
    }

    /**
     * Returns the log of the input in the desired base.
     *
     * @param input the input
     * @param base the log base
     *
     * @return the log value of the input in the desired base.
     */
    public static double log(double input, double base) {
        if (base <= 0) {
            throw new IllegalArgumentException("Attempting to comupute logarithm of base " + base + ".");
        } else if (base != logBase) {
            logBase = base;
            logBaseValue = FastMath.log(base);
        }
        return FastMath.log(input) / logBaseValue;
    }

    /**
     * Convenience method returning the standard deviation of a list of doubles.
     * Returns 0 if the list is null or of size &lt; 2.
     *
     * @param input input list
     * @return the corresponding standard deviation
     */
    public static double std(ArrayList<Double> input) {
        if (input == null || input.size() < 2) {
            return 0;
        }
        double result = 0;
        double mean = mean(input);
        for (Double x : input) {
            result += Math.pow(x - mean, 2);
        }
        result = result / (input.size() - 1);
        result = Math.sqrt(result);
        return result;
    }

    /**
     * Convenience method returning the mean of a list of doubles.
     *
     * @param input input list
     * @return the corresponding mean
     */
    public static double mean(ArrayList<Double> input) {
        return sum(input) / input.size();
    }

    /**
     * Convenience method returning the sum of a list of doubles.
     *
     * @param input input list
     * @return the corresponding mean
     */
    public static double sum(ArrayList<Double> input) {
        double result = 0;
        for (Double x : input) {
            result += x;
        }
        return result;
    }

    /**
     * Returns the population Pearson correlation r between series1 and series2.
     *
     * @param series1 first series to compare
     * @param series2 second series to compare
     *
     * @return the Pearson correlation factor
     */
    public static double getCorrelation(ArrayList<Double> series1, ArrayList<Double> series2) {
        if (series1.size() != series2.size()) {
            throw new IllegalArgumentException("Series must be of same size for correlation analysis (series 1: " + series1.size() + " elements, series 1: " + series2.size() + " elements).");
        }
        int n = series1.size();
        if (n <= 1) {
            throw new IllegalArgumentException("At least two values are required for the estimation of correlation factors (" + n + " elements).");
        }
        double std1 = std(series1);
        double std2 = std(series2);
        if (std1 == 0 && std2 == 0) {
            return 1;
        }
        if (std1 == 0) {
            std1 = std2;
        }
        if (std2 == 0) {
            std2 = std1;
        }
        double mean1 = mean(series1);
        double mean2 = mean(series2);
        double corr = 0;
        for (int i = 0; i < n; i++) {
            corr += (series1.get(i) - mean1) * (series2.get(i) - mean2);
        }
        corr = corr / (std1 * std2);
        corr = corr / (n - 1);
        return corr;
    }

    /**
     * Returns the population Pearson correlation r between series1 and series2.
     * Here the correlation factor is estimated using median and percentile
     * distance instead of mean and standard deviation.
     *
     * @param series1 the first series to inspect
     * @param series2 the second series to inspect
     *
     * @return a robust version of the Pearson correlation factor
     */
    public static double getRobustCorrelation(ArrayList<Double> series1, ArrayList<Double> series2) {
        if (series1.size() != series2.size()) {
            throw new IllegalArgumentException("Series must be of same size for correlation analysis (series 1: " + series1.size() + " elements, series 1: " + series2.size() + " elements).");
        }
        int n = series1.size();
        if (n <= 1) {
            throw new IllegalArgumentException("At least two values are required for the estimation of correlation factors (" + n + " elements).");
        }
        double std1 = (percentile(series1, 0.841) - percentile(series1, 0.159)) / 2;
        double std2 = (percentile(series2, 0.841) - percentile(series2, 0.159)) / 2;
        if (std1 == 0 && std2 == 0) {
            return 1;
        }
        if (std1 == 0) {
            std1 = std2;
        }
        if (std2 == 0) {
            std2 = std1;
        }
        double mean1 = median(series1);
        double mean2 = median(series2);
        double corr = 0;
        for (int i = 0; i < n; i++) {
            corr += (series1.get(i) - mean1) * (series2.get(i) - mean2);
        }
        corr = corr / (std1 * std2);
        corr = corr / (n - 1);
        return corr;
    }

    /**
     * Checks that a probability is between 0 and 1 and throws an
     * IllegalArgumentException otherwise. 0.5 represents a probability of 50%.
     *
     * @param p the probability
     */
    public static void checkProbabilityRange(double p) {
        if (p < 0.0) {
            throw new IllegalArgumentException("Probability <0%.");
        } else if (p > 1.0) {
            throw new IllegalArgumentException("Probability >100%.");
        }
    }

    /**
     * Checks that a probability is between 0 % and 100 % and throws an
     * IllegalArgumentException otherwise. 50 represents a probability of 50%.
     *
     * @param p the probability
     */
    public static void checkProbabilityRangeInPercent(double p) {
        if (p < 0.0) {
            throw new IllegalArgumentException("Probability <0%.");
        } else if (p > 100.0) {
            throw new IllegalArgumentException("Probability >100%.");
        }
    }

    /**
     * Returns an integer randomly chosen between min and max included.
     *
     * @param min the lower limit
     * @param max the higher limit
     *
     * @return a random integer
     */
    public static int getRandomInteger(int min, int max) {
        double randomDouble = min + (Math.random() * (max - min));
        if (randomDouble > max) {
            return max;
        }
        if (randomDouble < min) {
            return min;
        }
        return (int) Math.round(randomDouble);
    }

    /**
     * Returns a list of n random indexes between min and max included. The list
     * is not sorted.
     *
     * @param n the number of indexes to return
     * @param min the lower limit
     * @param max the higher limit
     *
     * @return a list of n random indexes between min and max included
     */
    public static ArrayList<Integer> getRandomIndexes(int n, int min, int max) {
        ArrayList<Integer> result = new ArrayList<Integer>(n);
        for (int i = 0; i < n; i++) {
            result.add(getRandomInteger(min, max));
        }
        return result;
    }
}
