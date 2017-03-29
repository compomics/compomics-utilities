package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;

/**
 * Phenylalanine.
 *
 * @author Marc Vaudel
 */
public class Phenylalanine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -939609662176974248L;

    /**
     * Constructor.
     */
    public Phenylalanine() {
        singleLetterCode = "F";
        threeLetterCode = "Phe";
        name = "Phenylalanine";
        averageMass = 147.1739;
        monoisotopicMass = 147.068414;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 9);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 9);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'F'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"TTT", "TTC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return 5.00;
    }

    @Override
    public double getHelicity() {
        return 1.26;
    }

    @Override
    public double getBasicity() {
        return 212.1;
    }

    @Override
    public double getPI() {
        return 5.49;
    }

    @Override
    public double getPK1() {
        return 2.20;
    }

    @Override
    public double getPK2() {
        return 9.31;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 135;
    }
}
