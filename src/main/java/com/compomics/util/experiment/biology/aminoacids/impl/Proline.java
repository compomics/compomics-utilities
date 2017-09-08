package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Proline.
 *
 * @author Marc Vaudel
 */
public class Proline extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 3754407258673679661L;

    /**
     * Constructor.
     */
    public Proline() {
        singleLetterCode = "P";
        threeLetterCode = "Pro";
        name = "Proline";
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 7);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'P'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"CCT", "CCC", "CCA", "CCG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return -4.92;
    }

    @Override
    public double getHelicity() {
        return 0.57;
    }

    @Override
    public double getBasicity() {
        return 214.4;
    }

    @Override
    public double getPI() {
        return 6.30;
    }

    @Override
    public double getPK1() {
        return 1.95;
    }

    @Override
    public double getPK2() {
        return 10.64;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 90;
    }
}
