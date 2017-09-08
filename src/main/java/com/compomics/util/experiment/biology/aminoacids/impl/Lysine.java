package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Lysine.
 *
 * @author Marc
 */
public class Lysine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 3427548228986235710L;

    /**
     * Constructor.
     */
    public Lysine() {
        singleLetterCode = "K";
        threeLetterCode = "Lys";
        name = "Lysine";
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 6);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 12);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 2);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'K'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"AAA", "AAG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return -5.00;
    }

    @Override
    public double getHelicity() {
        return 0.88;
    }

    @Override
    public double getBasicity() {
        return 221.8;
    }

    @Override
    public double getPI() {
        return 9.60;
    }

    @Override
    public double getPK1() {
        return 2.16;
    }

    @Override
    public double getPK2() {
        return 9.06;
    }

    @Override
    public double getPKa() {
        return 10.40;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 135;
    }
}
