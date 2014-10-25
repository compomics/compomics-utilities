package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Methionine.
 *
 * @author Marc Vaudel
 */
public class Methionine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 1841628592146093511L;

    /**
     * Constructor.
     */
    public Methionine() {
        singleLetterCode = "M";
        threeLetterCode = "Met";
        name = "Methionine";
        averageMass = 131.1961;
        monoisotopicMass = 131.040485;
        subAminoAcidsWithoutCombination = new char[]{'M'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"ATG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
