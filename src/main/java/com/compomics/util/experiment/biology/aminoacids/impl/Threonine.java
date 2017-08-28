package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Threonine.
 *
 * @author Marc Vaudel
 */
public class Threonine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 172831874616867727L;

    /**
     * Constructor.
     */
    public Threonine() {
        singleLetterCode = "T";
        threeLetterCode = "Thr";
        name = "Threonine";
        averageMass = 101.1039;
        monoisotopicMass = 101.047679;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 4);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 7);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 2);
        subAminoAcidsWithoutCombination = new char[]{'T'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"ACT", "ACC", "ACA", "ACG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return -1.08;
    }

    @Override
    public double getHelicity() {
        return 1.09;
    }

    @Override
    public double getBasicity() {
        return 211.7;
    }

    @Override
    public double getPI() {
        return 5.60;
    }

    @Override
    public double getPK1() {
        return 2.09;
    }

    @Override
    public double getPK2() {
        return 9.10;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 93;
    }
}
