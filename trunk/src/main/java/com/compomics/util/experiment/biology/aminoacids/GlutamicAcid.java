package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Glutamic Acid.
 *
 * @author Marc Vaudel
 */
public class GlutamicAcid extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 6850534412637609745L;

    /**
     * Constructor.
     */
    public GlutamicAcid() {
        singleLetterCode = "E";
        threeLetterCode = "Glu";
        name = "Glutamic Acid";
        averageMass = 129.114;
        monoisotopicMass = 129.042593;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'E'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'Z', 'X'};
    }
}
