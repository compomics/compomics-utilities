package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Isoleucine or Leucine.
 *
 * @author Harald Barsnes
 */
public class J extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 1963175809911841522L;

    /**
     * Constructor.
     */
    public J() {
        singleLetterCode = "J";
        threeLetterCode = "I/L";
        name = "Isoleucine or Leucine";
        averageMass = 113.15980;
        monoisotopicMass = 113.08407;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'I', 'L'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }
}
