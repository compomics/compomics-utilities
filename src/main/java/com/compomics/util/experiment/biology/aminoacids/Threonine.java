package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Threonine.
 *
 * @author Marc Vaudel
 */
public class Threonine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 172831874616867727L;

    /**
     * Constructor.
     */
    public Threonine() {
        singleLetterCode = "T";
        threeLetterCode = "Thr";
        name = "Threonine";
        averageMass = 101.1039;
        monoisotopicMass = 101.047679;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'T'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }
}
