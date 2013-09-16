package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Tyrosine
 *
 * @author Marc
 */
public class Tyrosine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 7542892886050340088L;

    /**
     * Constructor
     */
    public Tyrosine() {
        singleLetterCode = "Y";
        threeLetterCode = "Tyr";
        name = "Tyrosine";
        averageMass = 163.1733;
        monoisotopicMass = 163.06332;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'Y'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }
}
