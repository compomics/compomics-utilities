package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Asparagine.
 *
 * @author Marc Vaudel
 */
public class Asparagine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 5951975489690885808L;

    /**
     * Constructor.
     */
    public Asparagine() {
        singleLetterCode = "N";
        threeLetterCode = "Asn";
        name = "Asparagine";
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 4);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 6);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 2);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 2);
        subAminoAcidsWithoutCombination = new char[]{'N'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'B', 'X'};
        standardGeneticCode = new String[] {"AAT", "AAC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return -3.79;
    }

    @Override
    public double getHelicity() {
        return 0.94;
    }

    @Override
    public double getBasicity() {
        return 212.8;
    }

    @Override
    public double getPI() {
        return 5.41;
    }

    @Override
    public double getPK1() {
        return 2.14;
    }

    @Override
    public double getPK2() {
        return 8.72;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 96;
    }
}
