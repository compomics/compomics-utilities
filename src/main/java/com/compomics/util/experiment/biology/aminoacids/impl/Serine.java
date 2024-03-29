package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Serine.
 *
 * @author Marc Vaudel
 */
public class Serine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2201410868329421240L;

    /**
     * Constructor.
     */
    public Serine() {
        singleLetterCode = "S";
        threeLetterCode = "Ser";
        name = "Serine";
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 3);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 2);
        subAminoAcidsWithoutCombination = new char[]{'S'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"AGT", "AGC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return -2.85;
    }

    @Override
    public double getHelicity() {
        return 1.00;
    }

    @Override
    public double getBasicity() {
        return 207.6;
    }

    @Override
    public double getPI() {
        return 5.68;
    }

    @Override
    public double getPK1() {
        return 2.19;
    }

    @Override
    public double getPK2() {
        return 9.21;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 73;
    }
}
