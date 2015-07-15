package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;

/**
 * Aspartic Acid.
 *
 * @author Marc Vaudel
 */
public class AsparticAcid extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -8410332876209882538L;

    /**
     * Constructor.
     */
    public AsparticAcid() {
        singleLetterCode = "D";
        threeLetterCode = "Asp";
        name = "Aspartic Acid";
        averageMass = 115.0874;
        monoisotopicMass = 115.026943;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 4);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 3);
        subAminoAcidsWithoutCombination = new char[]{'D'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'B', 'X'};
        standardGeneticCode = new String[] {"GAT", "GAC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
