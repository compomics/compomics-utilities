package com.compomics.util.math.statistics.linear_regression;

/**
 * Results of a linear regression of equation y = a.x + b and pearson correlation r.
 *
 * @author Marc Vaudel
 */
public class RegressionStatistics {

    /**
     * a in y = a.x + b
     */
    public final Double a;
    
    /**
     * b in y = a.x + b
     */
    public final Double b;
    
    /**
     * The coefficient of determination.
     */
    public final Double rSquared;
    /**
     * The mean of the squared distance of the points to the regression line.
     */
    public final Double meanDistance;
    /**
     * The median of the squared distance of the points to the regression line.
     */
    public final Double medianDistance;
    
    /**
     * Constructor.
     * 
     * @param a a
     * @param b b
     * @param rSquared r squared
     * @param meanDistance the mean of the squared distance of the points to the regression line
     * @param medianDistance the median of the squared distance of the points to the regression line
     */
    public RegressionStatistics(Double a, Double b, Double rSquared, Double meanDistance, Double medianDistance) {
        this.a = a;
        this.b = b;
        this.rSquared = rSquared;
        this.meanDistance = meanDistance;
        this.medianDistance = medianDistance;
    }
    
}
