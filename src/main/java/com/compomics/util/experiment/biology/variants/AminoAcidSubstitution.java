package com.compomics.util.experiment.biology.variants;

/**
 * Class representing an amino acid substitution.
 *
 * @author Marc Vaudel
 */
public class AminoAcidSubstitution {

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
     * @param originalAminoAcid
     * @param variantAminoAcid
     */
    public AminoAcidSubstitution(char originalAminoAcid, char variantAminoAcid) {
        this.originalAminoAcid = originalAminoAcid;
        this.variantAminoAcid = variantAminoAcid;
    }

}
