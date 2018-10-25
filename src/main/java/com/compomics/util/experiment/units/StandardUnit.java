package com.compomics.util.experiment.units;

/**
 * The standard units implemented.
 *
 * @author Marc Vaudel
 */
public enum StandardUnit {

    mol("mole", "mol"),
    percentage("percent", "%"),
    ppm("ppm","ppm");

    /**
     * The full name of the unit.
     */
    public final String FULL_NAME;
    /**
     * The abbreviated name of the unit.
     */
    public final String ABBREVIATION;

    /**
     * Constructor.
     *
     * @param fullName the full name of the unit
     * @param abbreviation the abbreviated name of the unit
     */
    private StandardUnit(String fullName, String abbreviation) {
        this.FULL_NAME = fullName;
        this.ABBREVIATION = abbreviation;
    }
    
    /**
     * Returns the standard unit having the given full name.
     * 
     * @param fullName the full name of interest
     * 
     * @return the standard unit having the given full name
     */
    public static StandardUnit getStandardUnit(String fullName) {
        for (StandardUnit standardUnit : values()) {
            if (standardUnit.FULL_NAME.equals(fullName)) {
                return standardUnit;
            }
        }
        return null;
    }

    /**
     * Empty default constructor
     */
    private StandardUnit() {
        FULL_NAME = "";
        ABBREVIATION = "";
    }
}
