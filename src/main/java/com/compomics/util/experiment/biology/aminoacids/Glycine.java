package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;

/**
 * Glycine.
 *
 * @author Marc Vaudel
 */
public class Glycine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 2128316713069027803L;

    /**
     * Constructor.
     */
    public Glycine() {
        singleLetterCode = "G";
        threeLetterCode = "Gly";
        name = "Glycine";
        averageMass = 57.0513;
        monoisotopicMass = 57.021464;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 2);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 3);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'G'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"GGT", "GGC", "GGA", "GGG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return -3.31;
    }

    @Override
    public double getHelicity() {
        return 1.15;
    }

    @Override
    public double getBasicity() {
        return 202.7;
    }

    @Override
    public double getPI() {
        return 6.06;
    }

    @Override
    public double getPK1() {
        return 2.35;
    }

    @Override
    public double getPK2() {
        return 9.78;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 48;
    }
}
