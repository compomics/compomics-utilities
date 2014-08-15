package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Arginine.
 *
 * @author Marc Vaudel
 */
public class Arginine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
        static final long serialVersionUID = -5308475190007072857L;

    /**
     * Constructor.
     */
    public Arginine() {
        singleLetterCode = "R";
        threeLetterCode = "Arg";
        name = "Arginine";
        averageMass = 156.1857;
        monoisotopicMass = 156.101111;
    }

    @Override
    public char[] getSubAminoAcids() {
        return new char[]{'R'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }

    @Override
    public String[] getStandardGeneticCode() {
        return new String[] {"CGT", "CGC", "CGA", "CGG", "AGA", "AGG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
