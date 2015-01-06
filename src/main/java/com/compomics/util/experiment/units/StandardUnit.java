package com.compomics.util.experiment.units;

/**
 * The standard units implemented.
 *
 * @author Marc Vaudel
 */
public enum StandardUnit {

    mol("mole", "mol");

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
}
