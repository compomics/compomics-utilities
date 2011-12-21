
package com.compomics.util.gui.protein;

import java.awt.Color;

/**
 * A simple modofication profile object.
 * 
 * @author Harald Barsnes
 */
public class ModificationProfile {

    /**
     * The delta score row index.
     */
    public static final int DELTA_SCORE_ROW_INDEX = 0;
    /**
     * The a score row index.
     */
    public static final int A_SCORE_ROW_INDEX = 1;
    /**
     * The PTM name.
     */
    private String ptmName;
    /**
     * The PTM profile. Delta scores in the first row and a score in the second row, 
     * i.e., [d1][DELTA_SCORE_ROW_INDEX] and [a1][A_SCORE_ROW_INDEX].
     */
    private double[][] profile;
    /**
     * The profile color.
     */
    private Color color;

    /**
     * Modification profile constructor.
     * 
     * @param ptmName   the PTM namr
     * @param profile   the PTM profile
     * @param color     the PTM color
     */
    public ModificationProfile(String ptmName, double[][] profile, Color color) {
        this.ptmName = ptmName;
        this.profile = profile;
        this.color = color;
    }

    /**
     * Returns the PTM name.
     * 
     * @return the ptmName
     */
    public String getPtmName() {
        return ptmName;
    }

    /**
     * Sets the PTM name.
     * 
     * @param ptmName the ptmName to set
     */
    public void setPtmName(String ptmName) {
        this.ptmName = ptmName;
    }

    /**
     * Returns the profile. Delta scores in the first row and a score in the second row, 
     * i.e., [d1][DELTA_SCORE_ROW_INDEX] and [a1][A_SCORE_ROW_INDEX].
     * 
     * @return the profile
     */
    public double[][] getProfile() {
        return profile;
    }

    /**
     * Sets the profile. Delta scores in the first row and a score in the second row, 
     * i.e., [d1][DELTA_SCORE_ROW_INDEX] and [a1][A_SCORE_ROW_INDEX].
     * 
     * @param profile the profile to set
     */
    public void setProfile(double[][] profile) {
        this.profile = profile;
    }

    /**
     * Returns the PTM color.
     * 
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the PTM color.
     * 
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }
}
