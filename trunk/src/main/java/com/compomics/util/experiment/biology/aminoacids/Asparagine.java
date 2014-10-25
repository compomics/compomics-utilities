package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Asparagine.
 *
 * @author Marc Vaudel
 */
public class Asparagine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 5951975489690885808L;

    /**
     * Constructor.
     */
    public Asparagine() {
        singleLetterCode = "N";
        threeLetterCode = "Asn";
        name = "Asparagine";
        averageMass = 114.1026;
        monoisotopicMass = 114.042927;
        subAminoAcidsWithoutCombination = new char[]{'N'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'B', 'X'};
        standardGeneticCode = new String[] {"AAT", "AAC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
