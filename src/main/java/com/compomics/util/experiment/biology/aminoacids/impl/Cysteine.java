package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

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

    @Override
    public double getHydrophobicity() {
        return 2.50;
    }

    @Override
    public double getHelicity() {
        return 0.79;
    }

    @Override
    public double getBasicity() {
        return 206.2;
    }

    @Override
    public double getPI() {
        return 5.50;
    }

    @Override
    public double getPK1() {
        return 1.92;
    }

    @Override
    public double getPK2() {
        return 10.70;
    }

    @Override
    public double getPKa() {
        return 8.55;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 86;
    }
}
