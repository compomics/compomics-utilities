package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Aspartic Acid.
 *
 * @author Marc Vaudel
 */
public class AsparticAcid extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -8410332876209882538L;

    /**
     * Constructor.
     */
    public AsparticAcid() {
        singleLetterCode = "D";
        threeLetterCode = "Asp";
        name = "Aspartic Acid";
        averageMass = 115.0874;
        monoisotopicMass = 115.026943;
        subAminoAcidsWithoutCombination = new char[]{'D'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'B', 'X'};
        standardGeneticCode = new String[] {"GAT", "GAC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
