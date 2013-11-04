package com.compomics.util.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Class used to perform basic mathematical functions.
 *
 * @author Marc Vaudel
 */
public class BasicMathFunctions {

    /**
     * Returns n!
     *
     * @param n a given integer
     * @return the corresponding factorial
     */
    public static int factorial(int n) {
        if (n <= 1) {
            return 1;
        } else {
            return n * (n - 1);
        }
    }

    /**
     * Returns the number of k-combinations in a set of n elements.
     *
     * @param k the number of k-combinations
     * @param n the number of elements
     * @return the number of k-combinations in a set of n elements
     */
    public static double getCombination(int k, int n) {
        if (k <= n) {
            return ((double) factorial(n)) / (factorial(k) * factorial(n - k));
        } else {
            return 0;
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
     * Returns the desired percentile in a given list of double. If the
     * percentile is between two values a linear interpolation is done.
     *
     * @param input the input list
     * @param percentile the desired percentile. 0.01 returns the first
     * percentile. 0.5 returns the median.
     *
     * @return the desired percentile
     */
    public static double percentile(ArrayList<Double> input, double percentile) {
        if (percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException("Incorrect input for percentile: " + percentile + ". Input must be between 0 and 1.");
        }
        Collections.sort(input);
        int length = input.size();
        if (length == 0) {
            throw new IllegalArgumentException("Attempting to estimate the percentile of an empty list.");
        }
        if (length == 1) {
            return input.get(0);
        }
        double indexDouble = percentile * length;
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
     * Convenience method returning the standard deviation of a list of doubles.
     * Returns 0 if the list is null or of size < 2.
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
        double result = 0;
        for (Double x : input) {
            result += x;
        }
        return result / input.size();
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
            throw new IllegalArgumentException("Series must be of same size for correlation analysis (series 1: " + series1.size() + " elements, series 1: " + series2.size() + " elements)." );
        }
        int n = series1.size();
        if (n<=1) {
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
        for (int i = 0 ; i < n ; i++) {
            corr += (series1.get(i) - mean1)*(series2.get(i) - mean2);
        }
        corr = corr / (std1*std2);
        corr = corr / (n-1);
        return corr;
    }
    
    /**
     * Returns the population Pearson correlation r between series1 and series2. Here the correlation factor is estimated using median and percentile distance instead of mean and standard deviation
     * 
     * @param series1 the first series to inspect
     * @param series2 the second series to inspect
     * 
     * @return a robust version of the Pearson correlation factor
     */
    public static double getRobustCorrelation(ArrayList<Double> series1, ArrayList<Double> series2) {
        if (series1.size() != series2.size()) {
            throw new IllegalArgumentException("Series must be of same size for correlation analysis (series 1: " + series1.size() + " elements, series 1: " + series2.size() + " elements)." );
        }
        int n = series1.size();
        if (n<=1) {
            throw new IllegalArgumentException("At least two values are required for the estimation of correlation factors (" + n + " elements).");
        }
        double std1 = (percentile(series1, 0.841)-percentile(series1, 0.159))/2;
        double std2 = (percentile(series2, 0.841)-percentile(series2, 0.159))/2;
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
        for (int i = 0 ; i < n ; i++) {
            corr += (series1.get(i) - mean1)*(series2.get(i) - mean2);
        }
        corr = corr / (std1*std2);
        corr = corr / (n-1);
        return corr;
    }
}
