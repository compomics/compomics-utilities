package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Isoleucine.
 *
 * @author Marc Vaudel
 */
public class Isoleucine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -514676021245212886L;

    /**
     * Constructor.
     */
    public Isoleucine() {
        singleLetterCode = "I";
        threeLetterCode = "Ile";
        name = "Isoleucine";
        averageMass = 113.1576;
        monoisotopicMass = 113.084064;
    }

    @Override
    public char[] getSubAminoAcids() {
        return new char[]{'I'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'J', 'X'};
    }
}
