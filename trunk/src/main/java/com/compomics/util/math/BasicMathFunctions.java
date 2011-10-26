package com.compomics.util.math;

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
}
