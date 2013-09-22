package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Lysine.
 *
 * @author Marc
 */
public class Lysine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 3427548228986235710L;

    /**
     * Constructor.
     */
    public Lysine() {
        singleLetterCode = "K";
        threeLetterCode = "Lys";
        name = "Lysine";
        averageMass = 128.1723;
        monoisotopicMass = 128.094963;
    }

    @Override
    public char[] getSubAminoAcids() {
        return new char[]{'K'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }
}
