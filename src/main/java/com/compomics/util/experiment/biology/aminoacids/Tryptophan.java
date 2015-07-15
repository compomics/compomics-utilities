package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;

/**
 * Tryptophan.
 *
 * @author Marc Vaudel
 */
public class Tryptophan extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -6773437038176247799L;

    /**
     * Constructor.
     */
    public Tryptophan() {
        singleLetterCode = "W";
        threeLetterCode = "Trp";
        name = "Tryptophan";
        averageMass = 186.2099;
        monoisotopicMass = 186.079313;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 11);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 10);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 2);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'W'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"TGG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
