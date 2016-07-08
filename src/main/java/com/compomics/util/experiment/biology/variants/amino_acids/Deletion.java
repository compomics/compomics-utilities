package com.compomics.util.experiment.biology.variants.amino_acids;

import com.compomics.util.experiment.biology.variants.Variant;

/**
 * Class representing an amino acid deletion.
 *
 * @author Marc Vaudel
 */
public class Deletion implements Variant {
    
    /**
     * The single character code of the deleted amino acid.
     */
    char aa;
    /**
     * Constructor.
     * 
     * @param aa the single character code of the deleted amino acid
     */
    public Deletion(char aa) {
        this.aa = aa;
    }

    @Override
    public String getDescription() {
        return "Deletion of " + aa;
    }
    
}
