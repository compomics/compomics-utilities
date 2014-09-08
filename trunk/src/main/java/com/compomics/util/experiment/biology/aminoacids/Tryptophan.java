package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Tryptophan.
 *
 * @author Marc Vaudel
 */
public class Tryptophan extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -6773437038176247799L;

    /**
     * Constructor.
     */
    public Tryptophan() {
        singleLetterCode = "W";
        threeLetterCode = "Trp";
        name = "Tryptophan";
        averageMass = 186.2099;
        monoisotopicMass = 186.079313;
    }

    @Override
    public char[] getSubAminoAcids(boolean includeCombinations) {
        return new char[]{'W'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }

    @Override
    public String[] getStandardGeneticCode() {
        return new String[] {"TGG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
