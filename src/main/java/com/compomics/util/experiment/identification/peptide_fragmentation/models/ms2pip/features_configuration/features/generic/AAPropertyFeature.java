package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.SingleAAPropertyFeature;

/**
 * Feature based on an amino acid property.
 *
 * @author Marc Vaudel
 */
public abstract class AAPropertyFeature implements Ms2pipFeature, SingleAAPropertyFeature {

    /**
     * Empty default constructor
     */
    public AAPropertyFeature() {
    }

    /**
     * The index on the sequence.
     */
    protected int aaIndex;
    /**
     * The property of the amino acids to consider.
     */
    protected AminoAcid.Property aminoAcidProperty;

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

    @Override
    public AminoAcid.Property getAminoAcidProperty() {
        return aminoAcidProperty;
    }

    @Override
    public void setAminoAcidProperty(AminoAcid.Property aminoAcidProperty) {
        this.aminoAcidProperty = aminoAcidProperty;
    }
}
