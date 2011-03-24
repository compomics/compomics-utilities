package com.compomics.util;

/**
 * Object that stores data about one data point in an XYZ plot.
 *
 * @author Harald Barsnes
 */
public class XYZDataPoint {

    private double x;
    private double y;
    private double z;

    /**
     * Create a new XYZDataPoint.
     *
     * @param x
     * @param y
     * @param z
     */
    public XYZDataPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * @param z the z to set
     */
    public void setZ(double z) {
        this.z = z;
    }
}
