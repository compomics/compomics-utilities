package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;

/**
 * Cysteine.
 *
 * @author Marc Vaudel
 */
public class Cysteine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 9171494537928740228L;

    /**
     * Constructor.
     */
    public Cysteine() {
        singleLetterCode = "C";
        threeLetterCode = "Cys";
        name = "Cysteine";
        averageMass = 103.1429;
        monoisotopicMass = 103.009185;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 3);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.S, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'C'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"TGT", "TGC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
