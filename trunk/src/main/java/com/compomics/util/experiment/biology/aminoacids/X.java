package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Unknown amino acid (Mascot).
 *
 * @author Harald Barsnes
 */
public class X extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2618109862080991929L;

    /**
     * Constructor.
     */
    public X() {
        singleLetterCode = "X";
        threeLetterCode = "Xaa";
        name = "Unknown_Mascot";
        averageMass = 110; // @TODO: is this the correct mass to use? 118 is the average...
        monoisotopicMass = 110; // @TODO: is this the correct mass to use? 118 is the average...
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'Y', 'U', 'O', 'V', 'W'};
    }

    @Override
    public char[] getCombinations() {
        return new char[0];
    }
}
