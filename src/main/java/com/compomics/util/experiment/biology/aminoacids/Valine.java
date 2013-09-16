package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Valine.
 *
 * @author Marc Vaudel
 */
public class Valine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -5155418025636472676L;

    /**
     * Constructor.
     */
    public Valine() {
        singleLetterCode = "V";
        threeLetterCode = "Val";
        name = "Valine";
        averageMass = 99.1311;
        monoisotopicMass = 99.068414;
    }

    @Override
    public char[] getActualAminoAcids() {
        return new char[]{'V'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }
}
