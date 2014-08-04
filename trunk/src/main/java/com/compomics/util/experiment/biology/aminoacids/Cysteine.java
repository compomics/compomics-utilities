package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Cysteine.
 *
 * @author Marc Vaudel
 */
public class Cysteine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 9171494537928740228L;

    /**
     * Constructor.
     */
    public Cysteine() {
        singleLetterCode = "C";
        threeLetterCode = "Cys";
        name = "Cysteine";
        averageMass = 103.1429;
        monoisotopicMass = 103.009185;
    }

    @Override
    public char[] getSubAminoAcids() {
        return new char[]{'C'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }

    @Override
    public String[] getStandardGeneticCode() {
        return new String[] {"TGT", "TGC"};
    }
}
