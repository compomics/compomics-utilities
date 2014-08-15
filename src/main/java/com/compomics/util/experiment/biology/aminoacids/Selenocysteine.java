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
        threeLetterCode = "SeC";
        name = "U_Mascot";
        averageMass = 150.0379;
        monoisotopicMass = 150.95363;
    }

    @Override
    public char[] getSubAminoAcids() {
        return new char[]{'U'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }

    @Override
    public String[] getStandardGeneticCode() {
        return new String[] {"TGA"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
