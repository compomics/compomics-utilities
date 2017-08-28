package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Histidine.
 *
 * @author Marc Vaudel
 */
public class Histidine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -669587024023052011L;

    /**
     * Constructor.
     */
    public Histidine() {
        singleLetterCode = "H";
        threeLetterCode = "His";
        name = "Histidine";
        averageMass = 137.1393;
        monoisotopicMass = 137.058912;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 6);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 7);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 3);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'H'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"CAT", "CAC"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return -4.63;
    }

    @Override
    public double getHelicity() {
        return 0.97;
    }

    @Override
    public double getBasicity() {
        return 223.7;
    }

    @Override
    public double getPI() {
        return 7.60;
    }

    @Override
    public double getPK1() {
        return 1.80;
    }

    @Override
    public double getPK2() {
        return 9.33;
    }

    @Override
    public double getPKa() {
        return 6.54;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 118;
    }
}
