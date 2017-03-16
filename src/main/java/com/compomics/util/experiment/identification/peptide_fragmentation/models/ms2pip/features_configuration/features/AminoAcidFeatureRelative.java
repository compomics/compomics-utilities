package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * An amino acid feature at a position relative to the end of an ion on the peptide sequence. In this feature, an index of 0 represents the last amino acid of the ion.
 *
 * @author Marc Vaudel
 */
public class AminoAcidFeatureRelative extends AminoAcidFeature {

    /**
     * Constructor. An index of 0 represents the last amino acid of the ion.
     * 
     * @param index the index
     * @param property the amino acid property
     */
    public AminoAcidFeatureRelative(int index, AminoAcid.Property property) {
        this.index = index;
        this.property = property;
    }
    
    @Override
    public String getCategory() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        if (index == 0) {
            return property.name + " of the last amino acid of the ion";
        } else {
            char sign = '+';
            if (index > 0) {
                sign = '-';
            }
            return property.name + " of the amino acid at the end of the ion " + sign + index;
        }
    }
}
