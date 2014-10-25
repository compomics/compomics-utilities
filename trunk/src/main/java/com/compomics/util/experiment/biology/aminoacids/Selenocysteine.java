package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * SeC (U) (Mascot)-
 *
 * @author Harald Barsnes
 */
public class Selenocysteine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2123392615229813870L;

    /**
     * Constructor.
     */
    public Selenocysteine() {
        singleLetterCode = "U";
        threeLetterCode = "Sec";
        name = "Selenocysteine";
        averageMass = 150.0379;
        monoisotopicMass = 150.95363;
        subAminoAcidsWithoutCombination = new char[]{'U'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"TGA"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
