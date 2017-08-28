package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Methionine.
 *
 * @author Marc Vaudel
 */
public class Methionine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 1841628592146093511L;

    /**
     * Constructor.
     */
    public Methionine() {
        singleLetterCode = "M";
        threeLetterCode = "Met";
        name = "Methionine";
        averageMass = 131.1961;
        monoisotopicMass = 131.040485;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 9);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.S, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'M'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"ATG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return 3.23;
    }

    @Override
    public double getHelicity() {
        return 1.22;
    }

    @Override
    public double getBasicity() {
        return 213.3;
    }

    @Override
    public double getPI() {
        return 5.74;
    }

    @Override
    public double getPK1() {
        return 2.13;
    }

    @Override
    public double getPK2() {
        return 9.28;
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
