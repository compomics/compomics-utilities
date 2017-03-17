package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

/**
 * An amino acid identity feature at a fixed position on the peptide sequence. In this feature, a positive index refers to counting from the N-term where the first amino acid is 0. A negative index refers to counting from the C-term where the first amino acid is -1.
 *
 * @author Marc Vaudel
 */
public class AAIdentityFeatureAbsolute extends AAIdentityFeature {

    /**
     * Constructor. A positive index refers to counting from the N-term where the first amino acid is 0. A negative index refers to counting from the C-term where the first amino acid is -1.
     * 
     * @param index the index
     * @param aminoAcid the amino acid targeted
     */
    public AAIdentityFeatureAbsolute(int index, char aminoAcid) {
        this.index = index;
        this.aminoAcid = aminoAcid;
    }
    
    @Override
    public String getCategory() {
        return this.getClass().getName();
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

}
