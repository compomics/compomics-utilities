package com.compomics.util.math.statistics.linear_regression.filters;

import com.compomics.util.math.BasicMathFunctions;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Filters by binning.
 *
 * @author Marc Vaudel
 */
public class BinningFilter {

    /**
     * Empty default constructor
     */
    public BinningFilter() {
    }

    /**
     * Returns a list containing first the filtered xs and then the filtered ys.
     * The filtered values correspond to the median of the points grouped in the
     * given number of bins.
     *
     * @param x x series
     * @param y y series
     * @param nBins the number of bins to create
     *
     * @return a filtered list of x and y
     */
    public static ArrayList<ArrayList<Double>> getFilteredInputFixedBins(ArrayList<Double> x, ArrayList<Double> y, int nBins) {

        if (nBins < 2) {
            throw new IllegalArgumentException("The number of bins must be greater than 1.");
        }
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
        if (n <= nBins) {
            throw new IllegalArgumentException("Vector size (" + n + ") smaller than number of bins (" + nBins + ").");
        }

        int binSize = n / nBins;
        int rest = n - (binSize * nBins);
        return getFilteredInput(x, y, binSize, rest);
    }

    /**
     * Returns a list containing first the filtered xs and then the filtered ys.
     * The filtered values correspond to the median of the points grouped in the
     * given bin size.
     *
     * @param x x series
     * @param y y series
     * @param binSize the maximal bin size
     *
     * @return a filtered list of x and y
     */
    public static ArrayList<ArrayList<Double>> getFilteredInputFixedBinsSize(ArrayList<Double> x, ArrayList<Double> y, int binSize) {

        if (binSize < 1) {
            throw new IllegalArgumentException("Bin size must be greater than 1.");
        }
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
        int nBins = n / binSize;
        if (nBins < 2) {
            throw new IllegalArgumentException("Vector size (" + n + ") does not allow having more than two bins of size (" + binSize + ").");
        }
        int rest = n - (binSize * nBins);
        return getFilteredInput(x, y, binSize, rest);
    }

    /**
     * Filters the input vectors x and y in bins of size binSize with the rest
     * distributed in the first bins.
     *
     * @param x x series
     * @param y y series
     * @param binSize the bin size
     * @param rest the rest to distribute
     *
     * @return a filtered list of x and y
     */
    private static ArrayList<ArrayList<Double>> getFilteredInput(ArrayList<Double> x, ArrayList<Double> y, int binSize, int rest) {

        int currentBin = 0;
        ArrayList<Double> sortedX = new ArrayList<>(x);
        Collections.sort(sortedX);
        ArrayList<Double> currentX = new ArrayList<>(binSize + 1);
        ArrayList<Double> currentY = new ArrayList<>(binSize + 1);
        ArrayList<Double> filteredX = new ArrayList<>(x.size());
        ArrayList<Double> filteredY = new ArrayList<>(y.size());
        Double x0 = x.get(0);
        boolean newX = false;

        for (int i = 0; i < x.size(); i++) {
            Double xi = x.get(i);
            if (!newX && !xi.equals(x0)) {
                newX = true;
            }
            Double yi = y.get(i);
            currentX.add(xi);
            currentY.add(yi);
            int limit = binSize;
            if (currentBin < rest) {
                limit += 1;
            }
            if (currentX.size() == limit) {
                Double xMedian = BasicMathFunctions.medianSorted(currentX);
                Double yMedian = BasicMathFunctions.median(currentY);
                filteredX.add(xMedian);
                filteredY.add(yMedian);
                currentX.clear();
                currentY.clear();
                currentBin++;
            }
        }

        if (!newX) {
            throw new IllegalArgumentException("Attempting to perform the linear regression of a vertical line or a point.");
        }

        if (!currentX.isEmpty()) {
            throw new IllegalArgumentException("Not all points in bins.");
        }

        ArrayList<ArrayList<Double>> result = new ArrayList<>(2);
        result.add(filteredX);
        result.add(filteredY);
        return result;
    }
}
