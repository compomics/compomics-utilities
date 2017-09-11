package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Interface for the ms2pip features having a single amino acid property.
 *
 * @author Marc Vaudel
 */
public interface SingleAAPropertyFeature {

    /**
     * Returns the amino acid property to consider.
     * 
     * @return the amino acid property to consider
     */
    public AminoAcid.Property getAminoAcidProperty();

    /**
     * Sets the amino acid property to consider.
     * 
     * @param aminoAcidProperty the amino acid property to consider
     */
    public void setAminoAcidProperty(AminoAcid.Property aminoAcidProperty);
}
