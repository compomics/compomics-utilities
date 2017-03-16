package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * An amino acid feature at a fixed position on the peptide sequence. In this feature, a positive index refers to counting from the N-term where the first amino acid is 0. A negative index refers to counting from the C-term where the first amino acid is -1.
 *
 * @author Marc Vaudel
 */
public class AminoAcidFeatureAbsolute extends AminoAcidFeature {

    /**
     * Constructor. A positive index refers to counting from the N-term where the first amino acid is 0. A negative index refers to counting from the C-term where the first amino acid is -1.
     * 
     * @param index the index
     * @param property the amino acid property
     */
    public AminoAcidFeatureAbsolute(int index, AminoAcid.Property property) {
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
            return property.name + " of the N-term amino acid";
        } else if (index == -1) {
            return property.name + " of the C-term amino acid";
        } else if (index > 0) {
            return property.name + " of the amino acid at N-term +" + index;
        } else {
            int tempIndex = index - 1;
            return property.name + " of the amino acid at C-term -" + tempIndex;
        }
    }

}
