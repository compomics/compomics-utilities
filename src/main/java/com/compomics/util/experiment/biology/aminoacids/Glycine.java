package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Glycine.
 *
 * @author Marc Vaudel
 */
public class Glycine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 2128316713069027803L;

    /**
     * Constructor.
     */
    public Glycine() {
        singleLetterCode = "G";
        threeLetterCode = "Gly";
        name = "Glycine";
        averageMass = 57.0513;
        monoisotopicMass = 57.021464;
    }

    @Override
    public char[] getSubAminoAcids() {
        return new char[]{'G'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }

    @Override
    public String[] getStandardGeneticCode() {
        return new String[] {"GGT", "GGC", "GGA", "GGG"};
    }
}
