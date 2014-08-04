package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Pyrrolysine.
 *
 * @author Harald Barsnes
 */
public class Pyrrolysine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 8680204019988094523L;

    /**
     * Constructor.
     */
    public Pyrrolysine() {
        singleLetterCode = "O";
        threeLetterCode = "Pyl";
        name = "Pyrrolysine";
        averageMass = 255.3134;
        monoisotopicMass = 255.158295;
    }

    @Override
    public char[] getSubAminoAcids() {
        return new char[]{'O'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }

    @Override
    public String[] getStandardGeneticCode() {
        return new String[] {"TAG"};
    }
}
