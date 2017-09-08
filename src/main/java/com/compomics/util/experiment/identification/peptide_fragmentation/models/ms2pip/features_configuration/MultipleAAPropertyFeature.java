package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;

/**
 * Interface for the ms2pip features having multiple amino acid properties.
 *
 * @author Marc Vaudel
 */
public interface MultipleAAPropertyFeature {

    /**
     * Returns the amino acid properties to consider.
     * 
     * @return the amino acid properties to consider
     */
    public AminoAcid.Property[] getAminoAcidProperties();

}
