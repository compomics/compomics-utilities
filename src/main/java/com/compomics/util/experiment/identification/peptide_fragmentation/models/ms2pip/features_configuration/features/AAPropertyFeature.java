package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_generation.Ms2pipFeature;

/**
 * Feature based on an amino acid property.
 *
 * @author Marc Vaudel
 */
public abstract class AAPropertyFeature implements Ms2pipFeature {

    /**
     * The index on the sequence.
     */
    protected int index;
    /**
     * The amino acid property.
     */
    protected AminoAcid.Property property;

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
     * Returns the amino acid property.
     * 
     * @return the amino acid property
     */
    public AminoAcid.Property getProperty() {
        return property;
    }

    /**
     * Sets the amino acid property.
     * 
     * @param property the amino acid property
     */
    public void setProperty(AminoAcid.Property property) {
        this.property = property;
    }
}
