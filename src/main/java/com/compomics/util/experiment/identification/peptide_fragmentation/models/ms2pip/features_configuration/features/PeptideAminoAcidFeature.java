package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.AASequenceFeature;
import com.compomics.util.experiment.biology.aminoacids.AminoAcid;

/**
 * Feature based on the amino acid properties of a peptide. 
 *
 * @author Marc Vaudel
 */
public class PeptideAminoAcidFeature extends AASequenceFeature {

    /**
     * The index of this ms2pip feature.
     */
    public static final int index = 1;
    
    /**
     * Constructor.
     * 
     * @param aminoAcidProperty the amino acid property to consider
     * @param function the function used to compare the amino acid properties
     */
    public PeptideAminoAcidFeature(AminoAcid.Property aminoAcidProperty, Function function) {
        this.aminoAcidProperty = aminoAcidProperty;
        this.function = function;
    }
    
    @Override
    public String getCategory() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        return function.name() + " of the " + aminoAcidProperty.name + " of the amino acids of the peptide."; 
    }

    @Override
    public int getIndex() {
        return index;
    }
}
