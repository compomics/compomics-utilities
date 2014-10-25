package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Tyrosine.
 *
 * @author Marc Vaudel
 */
public class Tyrosine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 7542892886050340088L;

    /**
     * Constructor.
     */
    public Tyrosine() {
        singleLetterCode = "Y";
        threeLetterCode = "Tyr";
        name = "Tyrosine";
        averageMass = 163.1733;
        monoisotopicMass = 163.06332;
        subAminoAcidsWithoutCombination = new char[]{'Y'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"TAT", "TAC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
