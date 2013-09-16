package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Alanine
 *
 * @author Marc
 */
public class Alanine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 2553535668713619525L;

    /**
     * Constructor
     */
    public Alanine() {
        singleLetterCode = "A";
        threeLetterCode = "Ala";
        name = "Alanine";
        averageMass = 71.0779;
        monoisotopicMass = 71.037114;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'A'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }
}
