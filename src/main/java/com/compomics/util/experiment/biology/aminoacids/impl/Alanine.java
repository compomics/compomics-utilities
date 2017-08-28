package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Alanine.
 *
 * @author Marc Vaudel
 */
public class Alanine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 2553535668713619525L;

    /**
     * Constructor.
     */
    public Alanine() {
        singleLetterCode = "A";
        threeLetterCode = "Ala";
        name = "Alanine";
        averageMass = 71.0779;
        monoisotopicMass = 71.037114;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 3);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'A'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"GCT", "GCC", "GCA", "GCG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return 0.16;
    }

    @Override
    public double getHelicity() {
        return 1.24;
    }

    @Override
    public double getBasicity() {
        return 206.4;
    }

    @Override
    public double getPI() {
        return 6.01;
    }

    @Override
    public double getPK1() {
        return 2.35;
    }

    @Override
    public double getPK2() {
        return 9.87;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 67;
    }
}
