package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;

/**
 * Feature based on an amino acid identity.
 *
 * @author Marc Vaudel
 */
public abstract class AAIdentityFeature implements Ms2pipFeature {

    /**
     * Empty default constructor
     */
    public AAIdentityFeature() {
    }

    /**
     * The index on the sequence.
     */
    protected int aaIndex;
    /**
     * The amino acid targeted represented as single letter code.
     */
    protected char aminoAcid;

    /**
     * Returns the index on the sequence.
     * 
     * @return the index on the sequence
     */
    public int getAaIndex() {
        return aaIndex;
    }

    /**
     * Sets the index on the sequence.
     * 
     * @param aaIndex the index on the sequence
     */
    public void setAaIndex(int aaIndex) {
        this.aaIndex = aaIndex;
    }

    /**
     * Returns the amino acid targeted represented as single letter code.
     * 
     * @return the amino acid targeted represented as single letter code
     */
    public char getAminoAcid() {
        return aminoAcid;
    }

    /**
     * Sets the amino acid targeted represented as single letter code.
     * 
     * @param aminoAcid the amino acid targeted represented as single letter code
     */
    public void setAminoAcid(char aminoAcid) {
        this.aminoAcid = aminoAcid;
    }
    
}
