package com.compomics.util.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Class used to perform basic mathematical functions
 *
 * @author Marc
 */
public class BasicMathFunctions {

    /**
     * returns n!
     * 
     * @param n a given integer
     * @return  the corresponding factorial
     */
    public static int factorial(int n) {
        if (n <= 1) {
            return 1;
        } else {
            return n * (n - 1);
        }
    }

    /**
     * Returns the number of k-combinations in a set of n elements
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
     * Method to estimate the median
     *
     * @param ratios    array of double
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
     * Method to estimate the median
     *
     * @param input    ArrayList of double
     * @return median of the input
     */
    public static double median(ArrayList<Double> input) {
        Collections.sort(input);
        int length = input.size();
        if (length == 0) {
            throw new IllegalArgumentException("Attempting to estimate the median of an empty list.");
        }
        if (length == 1) {
            return input.get(0);
        }
        int index = length/2;
        if (length % 2 == 1) {
            return input.get(index);
        } else {
            return (input.get(index) + input.get(index-1)) / 2;
        }
    }

    /**
     * Method estimating the median absolute deviation
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
     * Convenience method returning the standard deviation of a list of doubles. Returns 0 if the list is null or of size < 2.
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
            result += Math.pow(x-mean, 2);
        }
        result = result / (input.size()-1);
        result = Math.sqrt(result);
        return result;
    }
    
    /**
     * Convenience method returning the mean of a list of doubles
     * @param input input list
     * @return the corresponding mean
     */
    public static double mean(ArrayList<Double> input) {
        double result = 0;
        for (Double x : input) {
            result += x;
        }
        return result/input.size();
    }
}
