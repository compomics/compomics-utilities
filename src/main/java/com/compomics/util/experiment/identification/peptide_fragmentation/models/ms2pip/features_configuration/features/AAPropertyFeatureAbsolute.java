package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.AAPropertyFeature;
import com.compomics.util.experiment.biology.AminoAcid;

/**
 * An amino acid property feature at a fixed position on the peptide sequence. In this feature, a positive index refers to counting from the N-term where the first amino acid is 0. A negative index refers to counting from the C-term where the first amino acid is -1.
 *
 * @author Marc Vaudel
 */
public class AAPropertyFeatureAbsolute extends AAPropertyFeature {

    /**
     * The index of this ms2pip feature.
     */
    public static final int index = 6;
    /**
     * Constructor. A positive index refers to counting from the N-term where the first amino acid is 0. A negative index refers to counting from the C-term where the first amino acid is -1.
     * 
     * @param aaIndex the index on the sequence
     * @param property the amino acid property
     */
    public AAPropertyFeatureAbsolute(int aaIndex, AminoAcid.Property property) {
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
            return aminoAcidProperty.name + " of the N-term amino acid";
        } else if (index == -1) {
            return aminoAcidProperty.name + " of the C-term amino acid";
        } else if (index > 0) {
            return aminoAcidProperty.name + " of the amino acid at N-term +" + index;
        } else {
            int tempIndex = index + 1;
            return aminoAcidProperty.name + " of the amino acid at C-term " + tempIndex;
        }
    }

    @Override
    public int getIndex() {
        return index;
    }

}
