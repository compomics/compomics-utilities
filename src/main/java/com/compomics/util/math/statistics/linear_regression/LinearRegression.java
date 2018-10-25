package com.compomics.util.math.statistics.linear_regression;

import com.compomics.util.math.statistics.linear_regression.filters.ProbabilityFilter;
import com.compomics.util.math.statistics.linear_regression.regressions.MedianRegression;
import com.compomics.util.math.statistics.linear_regression.regressions.SimpleLinearRegression;
import java.util.ArrayList;

/**
 * Enum of the different implementations for a regression.
 *
 * @author Marc Vaudel
 */
public class LinearRegression {

    /**
     * Empty default constructor
     */
    public LinearRegression() {
    }

    /**
     * Returns a simple linear regression.
     *
     * @param x the x series
     * @param y the y series
     *
     * @return a simple linear regression
     */
    public static RegressionStatistics getSimpleLinearRegression(ArrayList<Double> x, ArrayList<Double> y) {
        return SimpleLinearRegression.getLinearRegression(x, y);
    }

    /**
     * Returns a robust linear regression based on the median.
     *
     * @param x the x series
     * @param y the y series
     *
     * @return a simple linear regression
     */
    public static RegressionStatistics getRobustLinearRegression(ArrayList<Double> x, ArrayList<Double> y) {
        return MedianRegression.getLinearRegression(x, y);
    }

    /**
     * Returns a simple linear regression performed after outlier removal. If
     * less than 100 points are available before or after filtering, a robust
     * regression is used.
     *
     * @param x the x series
     * @param y the y series
     * @param p the probability for outlier exclusion, e.g. 0.95 for 95%
     * confidence
     *
     * @return a simple linear regression
     */
    public static RegressionStatistics getSimpleLinearRegressionOutlierRemoval(ArrayList<Double> x, ArrayList<Double> y, Double p) {

        if (x.size() < 100) {
            return MedianRegression.getLinearRegression(x, y);
        }

        ArrayList<ArrayList<Double>> filteredInput = ProbabilityFilter.getFilteredInput(x, y, p);
        ArrayList<Double> filteredX = filteredInput.get(0);
        ArrayList<Double> filteredY = filteredInput.get(1);
        if (filteredX.size() < 100) {
            return MedianRegression.getLinearRegression(x, y);
        }

        return SimpleLinearRegression.getLinearRegression(filteredX, filteredY);
    }
}
