package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Serine.
 *
 * @author Marc Vaudel
 */
public class Serine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2201410868329421240L;

    /**
     * Constructor.
     */
    public Serine() {
        singleLetterCode = "S";
        threeLetterCode = "Ser";
        name = "Serine";
        averageMass = 87.0773;
        monoisotopicMass = 87.032028;
        subAminoAcidsWithoutCombination = new char[]{'S'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"AGT", "AGC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
