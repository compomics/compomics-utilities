package com.compomics.util.pride.prideobjects.webservice.peptide;

/**
 * The PRIDE ModificationLocation object
 *
 * @author Kenneth Verheggen
 */
public class ModificationLocation {

    /**
     * Location of the modification in the identification
     */
    private int location;
    /**
     * The name of the modification
     */
    private String modification;

    /**
     * Creates a new ModificationLocation object
     *
     */
    public ModificationLocation() {
    }

    /**
     * Returns the modification location
     *
     * @return the modification location
     */
    public int getLocation() {
        return location;
    }

    /**
     * Set the location of the modification
     *
     * @param location the location
     */
    public void setLocation(int location) {
        this.location = location;
    }

    /**
     * Returns the modification name
     *
     * @return the modification name
     */
    public String getModification() {
        return modification;
    }

    /**
     * Set the name of the modification
     *
     * @param modification the name of the modification
     */
    public void setModification(String modification) {
        this.modification = modification;
    }

}
