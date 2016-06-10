package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;

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
     * The amino acid substitution matrix selected.
     */
    private AaSubstitutionMatrix aaSubstitutionMatrix;
    
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

    /**
     * Returns the amino acid substitution matrix to use.
     * 
     * @return the amino acid substitution matrix to use
     */
    public AaSubstitutionMatrix getAaSubstitutionMatrix() {
        return aaSubstitutionMatrix;
    }

    /**
     * Sets the amino acid substitution matrix to use.
     * 
     * @param aaSubstitutionMatrix the amino acid substitution matrix to use
     */
    public void setAaSubstitutionMatrix(AaSubstitutionMatrix aaSubstitutionMatrix) {
        this.aaSubstitutionMatrix = aaSubstitutionMatrix;
    }
    
}
