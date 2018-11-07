package com.compomics.util.experiment.biology.variants.amino_acids;

import com.compomics.util.experiment.biology.variants.Variant;

/**
 * Class representing an amino acid substitution.
 *
 * @author Marc Vaudel
 */
public class Substitution implements Variant {

    /**
     * Empty default constructor
     */
    public Substitution() {
    }
    
    /**
     * The single character code of the original amino acid.
     */
    char originalAa;
    /**
     * The single character code of the substituted amino acid.
     */
    char substitutedAa;
    /**
     * Constructor.
     * 
     * @param originalAa the single character code of the original amino acid
     * @param substitutedAa the single character code of the substituted amino acid
     */
    public Substitution(char originalAa, char substitutedAa) {
        this.originalAa = originalAa;
        this.substitutedAa = substitutedAa;
    }

    @Override
    public String getDescription() {
        return originalAa + " substituted by " + substitutedAa;
    }

    
    public char getOriginalAminoAcid(){
        return originalAa;
    }
    
    public char getSubstitutedAminoAcid(){
        return substitutedAa;
    }
}
