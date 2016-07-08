package com.compomics.util.experiment.biology.variants.amino_acids;

import com.compomics.util.experiment.biology.variants.Variant;

/**
 * Class representing an amino acid insertion.
 *
 * @author Marc Vaudel
 */
public class Insertion implements Variant {
    
    /**
     * The single character code of the inserted amino acid.
     */
    char aa;
    /**
     * Constructor.
     * 
     * @param aa the single character code of the inserted amino acid
     */
    public Insertion(char aa) {
        this.aa = aa;
    }

    @Override
    public String getDescription() {
        return "Insertion of " + aa;
    }

}
