package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Glutamine.
 *
 * @author Marc Vaudel
 */
public class Glutamine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -1552736259723394861L;

    /**
     * Constructor.
     */
    public Glutamine() {
        singleLetterCode = "Q";
        threeLetterCode = "Gln";
        name = "Glutamine";
        averageMass = 128.1292;
        monoisotopicMass = 128.058578;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'Q'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'Z', 'X'};
    }
}
