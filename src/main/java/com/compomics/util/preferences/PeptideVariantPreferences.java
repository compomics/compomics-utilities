package com.compomics.util.preferences;

/**
 * Preferences for the allowed variants in peptide sequences.
 *
 * @author Marc Vaudel
 */
public class PeptideVariantPreferences {

    /**
     * The number of sequence edits allowed.
     */
    private Integer nEdits = 1;
    
    /**
     * Constructor.
     */
    public PeptideVariantPreferences() {
        
    }

    /**
     * Returns the number of sequence edits allowed.
     * 
     * @return the number of sequence edits allowed
     */
    public Integer getnEdits() {
        return nEdits;
    }

    /**
     * Sets the number of sequence edits allowed.
     * 
     * @param nEdits the number of sequence edits allowed
     */
    public void setnEdits(Integer nEdits) {
        this.nEdits = nEdits;
    }
    
    
    
}
