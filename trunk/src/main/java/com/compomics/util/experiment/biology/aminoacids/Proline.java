package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Proline.
 *
 * @author Marc Vaudel
 */
public class Proline extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 3754407258673679661L;

    /**
     * Constructor.
     */
    public Proline() {
        singleLetterCode = "P";
        threeLetterCode = "Pro";
        name = "Proline";
        averageMass = 97.1152;
        monoisotopicMass = 97.052764;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'P'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }
}
