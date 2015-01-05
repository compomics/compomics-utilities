package com.compomics.util.experiment.units;

/**
 * The standard units implemented
 *
 * @author Marc
 */
public enum StandardUnit {

    mol("mole", "mol");
    
    /**
     * The full name of the unit.
     */
    public final String fullName;
    /**
     * The abbreviated name of the unit.
     */
    public final String abbreviation;
    /**
     * Constructor.
     * 
     * @param fullName the full name of the unit
     * @param abbreviation the abbreviated name of the unit
     */
    private StandardUnit(String fullName, String abbreviation) {
        this.fullName = fullName;
        this.abbreviation = abbreviation;
    }
    
}
