package com.compomics.util.experiment.biology.variants;

/**
 * Class representing an amino acid substitution.
 *
 * @author Marc Vaudel
 */
public class AminoAcidSubstitution {

    /**
     * Empty default constructor
     */
    public AminoAcidSubstitution() {
        originalAminoAcid = '0';
        variantAminoAcid = '0';
    }

    /**
     * The original amino acid represented by its single letter code.
     */
    public final char originalAminoAcid;
    /**
     * The variant amino acid represented by its single letter code.
     */
    public final char variantAminoAcid;

    /**
     * Constructor.
     *
     * @param originalAminoAcid the original amino acid 
     * @param variantAminoAcid the variant amino acid
     */
    public AminoAcidSubstitution(char originalAminoAcid, char variantAminoAcid) {
        this.originalAminoAcid = originalAminoAcid;
        this.variantAminoAcid = variantAminoAcid;
    }
}
