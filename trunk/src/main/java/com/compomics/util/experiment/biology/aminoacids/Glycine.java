package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Glycine.
 *
 * @author Marc Vaudel
 */
public class Glycine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 2128316713069027803L;

    /**
     * Constructor.
     */
    public Glycine() {
        singleLetterCode = "G";
        threeLetterCode = "Gly";
        name = "Glycine";
        averageMass = 57.0513;
        monoisotopicMass = 57.021464;
        subAminoAcidsWithoutCombination = new char[]{'G'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"GGT", "GGC", "GGA", "GGG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
