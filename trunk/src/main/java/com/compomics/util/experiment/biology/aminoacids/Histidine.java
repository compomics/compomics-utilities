package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Histidine
 *
 * @author Marc
 */
public class Histidine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -669587024023052011L;

    /**
     * Constructor
     */
    public Histidine() {
        singleLetterCode = "H";
        threeLetterCode = "His";
        name = "Histidine";
        averageMass = 137.1393;
        monoisotopicMass = 137.058912;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'H'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }
}
