package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Methionine.
 *
 * @author Marc Vaudel
 */
public class Methionine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 1841628592146093511L;

    /**
     * Constructor.
     */
    public Methionine() {
        singleLetterCode = "M";
        threeLetterCode = "Met";
        name = "Methionine";
        averageMass = 131.1961;
        monoisotopicMass = 131.040485;
    }

    @Override
    public char[] getSubAminoAcids(boolean includeCombinations) {
        return new char[]{'M'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
    }

    @Override
    public String[] getStandardGeneticCode() {
        return new String[] {"ATG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
