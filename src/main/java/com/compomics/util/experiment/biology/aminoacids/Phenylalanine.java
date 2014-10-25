package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Phenylalanine.
 *
 * @author Marc Vaudel
 */
public class Phenylalanine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -939609662176974248L;

    /**
     * Constructor.
     */
    public Phenylalanine() {
        singleLetterCode = "F";
        threeLetterCode = "Phe";
        name = "Phenylalanine";
        averageMass = 147.1739;
        monoisotopicMass = 147.068414;
        subAminoAcidsWithoutCombination = new char[]{'F'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"TTT", "TTC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
