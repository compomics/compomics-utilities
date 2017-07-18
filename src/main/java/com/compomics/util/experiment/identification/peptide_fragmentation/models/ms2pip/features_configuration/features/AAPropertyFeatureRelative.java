package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.AAPropertyFeature;
import com.compomics.util.experiment.biology.AminoAcid;

/**
 * An amino acid property feature at a position relative to the end of an ion on the peptide sequence. In this feature, an index of 0 represents the last amino acid of the ion.
 *
 * @author Marc Vaudel
 */
public class AAPropertyFeatureRelative extends AAPropertyFeature {

    /**
     * The index of this ms2pip feature.
     */
    public static final int index = 7;
    /**
     * Constructor. An index of 0 represents the last amino acid of the ion.
     * 
     * @param aaIndex the index on the sequence
     * @param property the amino acid property
     */
    public AAPropertyFeatureRelative(int aaIndex, AminoAcid.Property property) {
        this.aaIndex = aaIndex;
        this.aminoAcidProperty = property;
    }
    
    @Override
    public String getCategory() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        if (index == 0) {
            return aminoAcidProperty.name + " of the last amino acid of the ion";
        } else {
            String sign = "";
            if (index >= 0) {
                sign = "+";
            }
            return aminoAcidProperty.name + " of the amino acid at the end of the ion " + sign + index;
        }
    }

    @Override
    public int getIndex() {
        return index;
    }
}
