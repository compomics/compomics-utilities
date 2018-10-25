package com.compomics.util.math.statistics.linear_regression.filters;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.statistics.distributions.NonSymmetricalNormalDistribution;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Filter removing points with outlying slope.
 *
 * @author Marc Vaudel
 */
public class ProbabilityFilter {

    /**
     * Empty default constructor
     */
    public ProbabilityFilter() {
    }

    /**
     * Returns a list containing first the filtered xs and then the filtered ys.
     * Doublets are filtered to the given (non cumulative) probability p of
     * belonging to the distribution of points based on the distance to the
     * median regression.
     *
     * @param x x series
     * @param y y series
     * @param p the (non cumulative) probability to use for filtering, e.g. 0.95
     * for 95%
     *
     * @return a filtered list of x and y
     */
    public static ArrayList<ArrayList<Double>> getFilteredInput(ArrayList<Double> x, ArrayList<Double> y, double p) {

        if (x == null) {
            throw new IllegalArgumentException("null given as x for filtering.");
        }
        if (y == null) {
            throw new IllegalArgumentException("null given as y for filtering.");
        }
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Attempting to perform filtering of lists of different sizes.");
        }
        int n = x.size();
        if (n <= 10) {
            throw new IllegalArgumentException("Attempting to perform filtering of a vectore of size " + n + ". 10 minimum, >100 advised.");
        }
        Double medianX = BasicMathFunctions.median(x);
        Double quantileX1 = BasicMathFunctions.percentile(x, 0.25);
        Double quantileX2 = BasicMathFunctions.percentile(x, 0.75);
        Double medianY = BasicMathFunctions.median(y);
        Double quantileY1 = BasicMathFunctions.percentile(y, 0.25);
        Double quantileY2 = BasicMathFunctions.percentile(y, 0.75);
        ArrayList<Double> slopes = new ArrayList<>(n);
        HashMap<Integer, Double> slopesMap = new HashMap<>(n);
        Double x0 = x.get(0);
        boolean newX = false;

        for (int i = 0; i < x.size(); i++) {
            Double xi = x.get(i);
            if (!newX && !xi.equals(x0)) {
                newX = true;
            }
            Double yi = y.get(i);
            if (xi >= medianX) {
                Double slope = (yi - quantileY1) / (xi - quantileX1);
                slopes.add(slope);
                slopesMap.put(i, slope);
            } else {
                Double slope = (quantileY2 - yi) / (quantileX2 - xi);
                slopes.add(slope);
                slopesMap.put(i, slope);
            }
        }

        if (!newX) {
            throw new IllegalArgumentException("Attempting to perform the linear regression of a vertical line or a point.");
        }

        Double medianSlope = BasicMathFunctions.median(slopes);
        ArrayList<Double> deviationsSquare = new ArrayList<>(n);
        HashMap<Integer, Double> deviationsSquareMap = new HashMap<>(n);

        for (int i = 0; i < x.size(); i++) {
            Double xi = x.get(i);
            Double slope = slopesMap.get(i);
            if (slope != null) {
                Double deltaX;
                if (xi >= medianX) {
                    deltaX = xi - quantileX1;
                } else {
                    deltaX = quantileX2 - xi;
                }
                double yMedian = medianSlope * deltaX;
                double yi = slope * deltaX;
                Double deviationSquare = (yi * yi) - (yMedian * yMedian);
                deviationsSquare.add(deviationSquare);
                deviationsSquareMap.put(i, deviationSquare);
            }
        }

        NonSymmetricalNormalDistribution slopeDistribution = NonSymmetricalNormalDistribution.getRobustNonSymmetricalNormalDistribution(deviationsSquare);
        double threshold = 1 - p;
        Double deviationMax = slopeDistribution.getMaxValueForProbability(threshold);
        Double deviationMin = slopeDistribution.getMinValueForProbability(threshold);
        ArrayList<Double> filteredX = new ArrayList<>(x.size());
        ArrayList<Double> filteredY = new ArrayList<>(y.size());

        for (int i = 0; i < slopes.size(); i++) {
            Double deviation = slopes.get(i);
            if (deviation != null && deviation >= deviationMin && deviation <= deviationMax) {
                filteredX.add(x.get(i));
                filteredY.add(y.get(i));
            }
        }

        ArrayList<ArrayList<Double>> result = new ArrayList<>(2);
        result.add(filteredX);
        result.add(filteredY);

        return result;
    }
}
