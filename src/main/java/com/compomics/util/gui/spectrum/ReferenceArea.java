
package com.compomics.util.gui.spectrum;

import java.awt.Color;

/**
 * A reference area to be added to a GraphicsPanel.
 *
 * @author Harald Barsnes.
 */
public class ReferenceArea {

    /** the reference label */
    private String label;
    /** the start of the reference area */
    private double start;
    /** the end of the reference area */
    private double end;
    /** the color of the reference area */
    private Color areaColor;
    /** the alpha level of the reference area */
    private float alpha;
    /** if the area is to be drawn on top of or behind the data */
    private boolean drawOnTop;
    /** if true the label is drawn*/
    private boolean drawLabel;

    /**
     * Creates a new ReferenceArea
     *
     * @param label     the reference label
     * @param start     the start of the reference area
     * @param end       the end of the reference area
     * @param areaColor the color of the reference area
     * @param alpha     the alpha level of the reference area
     * @param drawOnTop if the area is to be drawn on top of or behind the data
     * @param drawLabel if the label is to be drawn or not
     * @throws IllegalArgumentException alpha must be in the range 0.0f to 1.0f
     */
    public ReferenceArea(String label, double start, double end, Color areaColor, float alpha, boolean drawOnTop, boolean drawLabel) throws IllegalArgumentException {
        this.label = label;
        this.start = start;
        this.end = end;
        this.areaColor = areaColor;
        this.drawOnTop = drawOnTop;
        this.drawLabel = drawLabel;

        // check the validity of alpha
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("The alpha transparency must be in the range 0.0f to 1.0f!");
        } else {
            this.alpha = alpha;
        }
    }

    /**
     * Returns the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label,
     *
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the start value.
     *
     * @return the start
     */
    public double getStart() {
        return start;
    }

    /**
     * Set the start value.
     *
     * @param start the start to set
     */
    public void setStart(double start) {
        this.start = start;
    }

    /**
     * Get the end value.
     *
     * @return the end
     */
    public double getEnd() {
        return end;
    }

    /**
     * Set the end value.
     *
     * @param end the end to set
     */
    public void setEnd(double end) {
        this.end = end;
    }

    /**
     * Get the area color.
     *
     * @return the areaColor
     */
    public Color getAreaColor() {
        return areaColor;
    }

    /**
     * Set the area color.
     *
     * @param areaColor the areaColor to set
     */
    public void setAreaColor(Color areaColor) {
        this.areaColor = areaColor;
    }

    /**
     * Get the alpha level.
     *
     * @return the alpha level
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Set the alpha level.
     *
     * @param alpha the alpha level to set
     */
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    /**
     * Returns true if the area is to be drawn in front of the data, false
     * otherwise.
     *
     * @return true if the area is to be drawn in front of the data, false otherwise
     */
    public boolean drawOnTop() {
        return drawOnTop;
    }

    /**
     * Set to true if the area is to be drawn in front of the data, false
     * otherwise.
     *
     * @param drawOnTop if the area is to be drawn in front of the data
     */
    public void setDrawOnTop(boolean drawOnTop) {
        this.drawOnTop = drawOnTop;
    }

    /**
     * Returns true if the label is to be drawn, false otherwise.
     *
     * @return true if the label is to be drawn, false otherwise
     */
    public boolean drawLabel() {
        return drawLabel;
    }

    /**
     * Set to true if the label is to be drawn, false otherwise.
     *
     * @param drawLabel if the label is to be drawn
     */
    public void setDrawLabel(boolean drawLabel) {
        this.drawLabel = drawLabel;
    }
}
