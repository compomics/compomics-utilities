package com.compomics.util.protein_sequences_manager.gui.sequences_import.taxonomy;

/**
 * Query type.
 *
 * @author Kenneth Verheggen
 */
public enum QueryType {

    FASTA("uniprot"), 
    TAXONOMY("taxonomy");
    
    /**
     * The location.
     */
    private final String location;

    /**
     * Constructor.
     * 
     * @param location the location
     */
    private QueryType(String location) {
        this.location = location;
    }

    /**
     * Returns the location.
     * 
     * @return the location
     */
    public String getLocation() {
        return this.location;
    }
}
