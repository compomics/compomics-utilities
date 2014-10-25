package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Alanine.
 *
 * @author Marc Vaudel
 */
public class Alanine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 2553535668713619525L;

    /**
     * Constructor.
     */
    public Alanine() {
        singleLetterCode = "A";
        threeLetterCode = "Ala";
        name = "Alanine";
        averageMass = 71.0779;
        monoisotopicMass = 71.037114;
        subAminoAcidsWithoutCombination = new char[]{'A'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"GCT", "GCC", "GCA", "GCG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
