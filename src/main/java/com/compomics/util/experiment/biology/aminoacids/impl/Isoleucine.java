package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

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
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 6);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 11);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'I'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'J', 'X'};
        standardGeneticCode = new String[] {"ATT", "ATC", "ATA"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return 4.41;
    }

    @Override
    public double getHelicity() {
        return 1.29;
    }

    @Override
    public double getBasicity() {
        return 210.8;
    }

    @Override
    public double getPI() {
        return 6.05;
    }

    @Override
    public double getPK1() {
        return 2.32;
    }

    @Override
    public double getPK2() {
        return 9.76;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 124;
    }
}
