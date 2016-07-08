package com.compomics.util.experiment.biology.variants.amino_acids;

import com.compomics.util.experiment.biology.variants.Variant;

/**
 * Class representing an amino acid swap.
 *
 * @author Marc Vaudel
 */
public class Swap implements Variant {
    
    /**
     * The single character code of the amino acid originally to the left.
     */
    char leftAa;
    /**
     * The single character code of the amino acid originally to the right.
     */
    char rightAa;
    /**
     * Constructor.
     * 
     * @param leftAa the single character code of the amino acid originally to the left
     * @param rightAa the single character code of the amino acid originally to the right
     */
    public Swap(char leftAa, char rightAa) {
        this.leftAa = leftAa;
        this.rightAa = rightAa;
    }

    @Override
    public String getDescription() {
        return "Swap of " + leftAa + " and " + rightAa;
    }
    
}
