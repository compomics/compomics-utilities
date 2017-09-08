package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Tyrosine.
 *
 * @author Marc Vaudel
 */
public class Tyrosine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 7542892886050340088L;

    /**
     * Constructor.
     */
    public Tyrosine() {
        singleLetterCode = "Y";
        threeLetterCode = "Tyr";
        name = "Tyrosine";
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 9);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 9);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 2);
        subAminoAcidsWithoutCombination = new char[]{'Y'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"TAT", "TAC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return 2.00;
    }

    @Override
    public double getHelicity() {
        return 1.11;
    }

    @Override
    public double getBasicity() {
        return 213.1;
    }

    @Override
    public double getPI() {
        return 5.64;
    }

    @Override
    public double getPK1() {
        return 2.20;
    }

    @Override
    public double getPK2() {
        return 9.21;
    }

    @Override
    public double getPKa() {
        return 9.84;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 141;
    }
}
