package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.AAIdentityFeature;

/**
 * An amino acid identity feature at a fixed position on the peptide sequence. In this feature, a positive index refers to counting from the N-term where the first amino acid is 0. A negative index refers to counting from the C-term where the first amino acid is -1.
 *
 * @author Marc Vaudel
 */
public class AAIdentityFeatureAbsolute extends AAIdentityFeature {

    /**
     * The index of this ms2pip feature.
     */
    public static final int index = 9;
    /**
     * Constructor. A positive index refers to counting from the N-term where the first amino acid is 0. A negative index refers to counting from the C-term where the first amino acid is -1.
     * 
     * @param aaIndex the index of the amino acid
     * @param aminoAcid the amino acid targeted
     */
    public AAIdentityFeatureAbsolute(int aaIndex, char aminoAcid) {
        this.aaIndex = aaIndex;
        this.aminoAcid = aminoAcid;
    }
    
    @Override
    public String getCategory() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        if (index == 0) {
            return aminoAcid + " at the N-term";
        } else if (index == -1) {
            return aminoAcid + " at the C-term";
        } else if (index > 0) {
            return aminoAcid + " at the N-term +" + index;
        } else {
            int tempIndex = index + 1;
            return aminoAcid + " at the C-term " + tempIndex;
        }
    }

    @Override
    public int getIndex() {
        return index;
    }

}
