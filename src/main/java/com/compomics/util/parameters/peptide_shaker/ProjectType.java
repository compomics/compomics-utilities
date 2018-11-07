package com.compomics.util.parameters.peptide_shaker;

/**
 * This enum lists the different types of projects that can be created using PeptideShaker.
 *
 * @author Marc Vaudel
 */
public enum ProjectType {

    psm(0,"psm", "PSM level project."), 
    peptide(1,"peptide", "Peptide level project"),
    protein(2,"protein", "Protein level project");

    /**
     * The index of the option.
     */
    public final int index;
    /**
     * The name of the option.
     */
    public final String name;
    /**
     * The description of the option.
     */
    public final String description;
    
    /**
     * Constructor.
     * 
     * @param index the index of the option
     * @param name the name of the option
     * @param description the description of the option
     */
    private ProjectType(int index, String name, String description) {
        this.index = index;
        this.name = name;
        this.description = description;
    }


    /**
     * Empty default constructor
     */
    private ProjectType() {
        index = 0;
        name = "";
        description = "";
    }
}
