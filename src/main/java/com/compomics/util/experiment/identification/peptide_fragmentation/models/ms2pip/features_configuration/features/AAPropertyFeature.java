package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.SingleAAPropertyFeature;

/**
 * Feature based on an amino acid property.
 *
 * @author Marc Vaudel
 */
public abstract class AAPropertyFeature implements Ms2pipFeature, SingleAAPropertyFeature {

    /**
     * The index on the sequence.
     */
    protected int index;
    /**
     * The property of the amino acids to consider.
     */
    protected AminoAcid.Property aminoAcidProperty;

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

    @Override
    public AminoAcid.Property getAminoAcidProperty() {
        return aminoAcidProperty;
    }

    @Override
    public void setAminoAcidProperty(AminoAcid.Property aminoAcidProperty) {
        this.aminoAcidProperty = aminoAcidProperty;
    }
}
