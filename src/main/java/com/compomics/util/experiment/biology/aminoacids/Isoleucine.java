package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Isoleucine.
 *
 * @author Marc Vaudel
 */
public class Isoleucine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -514676021245212886L;

    /**
     * Constructor.
     */
    public Isoleucine() {
        singleLetterCode = "I";
        threeLetterCode = "Ile";
        name = "Isoleucine";
        averageMass = 113.1576;
        monoisotopicMass = 113.084064;
        subAminoAcidsWithoutCombination = new char[]{'I'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'J', 'X'};
        standardGeneticCode = new String[] {"ATT", "ATC", "ATA"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
