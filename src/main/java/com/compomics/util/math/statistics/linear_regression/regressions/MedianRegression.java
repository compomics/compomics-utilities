package com.compomics.util.math.statistics.linear_regression.regressions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.statistics.linear_regression.RegressionStatistics;
import java.util.ArrayList;

/**
 * Performs a robust linear regression.
 *
 * @author Marc Vaudel
 */
public class MedianRegression {

    /**
     * Returns a robust linear regression based on the median of statistics.
     *
     * @param x the x series
     * @param y the y series
     *
     * @return a simple linear regression
     */
    public static RegressionStatistics getLinearRegression(ArrayList<Double> x, ArrayList<Double> y) {

        if (x == null) {
            throw new IllegalArgumentException("null given as x for linear regression.");
        }
        if (y == null) {
            throw new IllegalArgumentException("null given as y for linear regression.");
        }
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Attempting to perform linear regression of lists of different sizes.");
        }
        int n = x.size();
        if (n <= 1) {
            throw new IllegalArgumentException("Attempting to perform linear regression of a vectore of size " + n + ".");
        }

        Double medianX = BasicMathFunctions.median(x);
        Double quantileX1 = BasicMathFunctions.percentile(x, 0.25);
        Double quantileX2 = BasicMathFunctions.percentile(x, 0.75);
        Double medianY = BasicMathFunctions.median(y);
        Double quantileY1 = BasicMathFunctions.percentile(y, 0.25);
        Double quantileY2 = BasicMathFunctions.percentile(y, 0.75);
        ArrayList<Double> slopes = new ArrayList<>(n);
        Double x0 = x.get(0);
        Double meanY = 0.0;
        boolean newX = false;

        for (int i = 0; i < x.size(); i++) {
            Double xi = x.get(i);
            if (!newX && !xi.equals(x0)) {
                newX = true;
            }
            Double yi = y.get(i);
            meanY += yi;
            if (xi >= medianX) {
                Double slope = (yi - quantileY1) / (xi - quantileX1);
                slopes.add(slope);
            } else if (xi < medianX) {
                Double slope = (quantileY2 - yi) / (quantileX2 - xi);
                slopes.add(slope);
            }
        }

        if (!newX) {
            throw new IllegalArgumentException("Attempting to perform the linear regression of a vertical line or a point.");
        }

        meanY /= n;
        Double a = BasicMathFunctions.median(slopes);
        Double b = medianY - (a * medianX);
        Double ssTot = 0.0;
        Double ssRes = 0.0;
        ArrayList<Double> deltasSquare = new ArrayList<>(x.size());

        for (int i = 0; i < x.size(); i++) {
            Double xi = x.get(i);
            Double yi = y.get(i);
            Double fi = (a * xi) + b;
            Double diffY = yi - meanY;
            ssTot += (diffY * diffY);
            Double diffF = yi - fi;
            ssRes += (diffF * diffF);
            Double deltaY = yi - b;
            Double ai = deltaY / xi;
            Double deltaA = ai - a;
            Double deltaSquare = (xi * xi) + (deltaY * deltaY);
            deltaSquare *= deltaA * deltaA;
            deltaSquare /= (1 + (ai * ai));
            deltaSquare /= (1 + (a * a));
            deltasSquare.add(deltaSquare);
        }

        Double rSquared = 1.0;
        if (ssTot > 0) {
            rSquared = 1 - (ssRes / ssTot);
        }

        Double meanDelta = BasicMathFunctions.mean(deltasSquare);
        Double medianDelta = BasicMathFunctions.median(deltasSquare);

        return new RegressionStatistics(a, b, rSquared, meanDelta, medianDelta);
    }
}
