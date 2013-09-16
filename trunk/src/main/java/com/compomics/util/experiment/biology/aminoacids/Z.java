package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Glu or Gln: Glx (Mascot)
 *
 * @author Harald Barsnes
 */
public class Z extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -7714841171012071337L;

    /**
     * Constructor
     */
    public Z() {
        singleLetterCode = "Z";
        threeLetterCode = "Glx";
        name = "Z_Mascot";
        averageMass = 128.6216;
        monoisotopicMass = 128.5505855;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'Q', 'E'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }
}
