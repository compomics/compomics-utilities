package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Glutamic Acid.
 *
 * @author Marc Vaudel
 */
public class GlutamicAcid extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 6850534412637609745L;

    /**
     * Constructor.
     */
    public GlutamicAcid() {
        singleLetterCode = "E";
        threeLetterCode = "Glu";
        name = "Glutamic Acid";
        averageMass = 129.114;
        monoisotopicMass = 129.042593;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 7);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 3);
        subAminoAcidsWithoutCombination = new char[]{'E'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'Z', 'X'};
        standardGeneticCode = new String[] {"GAA", "GAG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return -1.50;
    }

    @Override
    public double getHelicity() {
        return 0.85;
    }

    @Override
    public double getBasicity() {
        return 215.6;
    }

    @Override
    public double getPI() {
        return 3.15;
    }

    @Override
    public double getPK1() {
        return 2.10;
    }

    @Override
    public double getPK2() {
        return 9.47;
    }

    @Override
    public double getPKa() {
        return 4.25;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 109;
    }
}
