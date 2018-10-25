package com.compomics.util;

/**
 * Object that stores data about one data point in an XYZ plot.
 *
 * @author Harald Barsnes
 */
public class XYZDataPoint {

    /**
     * Empty default constructor
     */
    public XYZDataPoint() {
    }

    /**
     * The x value.
     */
    private double x;
    /**
     * The y value.
     */
    private double y;
    /**
     * The z value.
     */
    private double z;

    /**
     * Create a new XYZDataPoint.
     *
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public XYZDataPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns the x value.
     * 
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the x value.
     * 
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the y value.
     * 
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the y value.
     * 
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the z value.
     * 
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the z value.
     * 
     * @param z the z to set
     */
    public void setZ(double z) {
        this.z = z;
    }
}
