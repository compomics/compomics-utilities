package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;

/**
 * Feature based on an amino acid identity.
 *
 * @author Marc Vaudel
 */
public abstract class AAIdentityFeature implements Ms2pipFeature {

    /**
     * The index on the sequence.
     */
    protected int index;
    /**
     * The amino acid targeted represented as single letter code.
     */
    protected char aminoAcid;

    /**
     * Returns the index on the sequence.
     * 
     * @return the index on the sequence
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index on the sequence.
     * 
     * @param index the index on the sequence
     */
    public void setIndex(int index) {
        this.index = index;
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
