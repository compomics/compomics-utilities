package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

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

    @Override
    public double getHydrophobicity() {
        return 4.88;
    }

    @Override
    public double getHelicity() {
        return 1.07;
    }

    @Override
    public double getBasicity() {
        return 216.1;
    }

    @Override
    public double getPI() {
        return 5.89;
    }

    @Override
    public double getPK1() {
        return 2.46;
    }

    @Override
    public double getPK2() {
        return 9.41;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 163;
    }
}
