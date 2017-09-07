package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Valine.
 *
 * @author Marc Vaudel
 */
public class Valine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -5155418025636472676L;

    /**
     * Constructor.
     */
    public Valine() {
        singleLetterCode = "V";
        threeLetterCode = "Val";
        name = "Valine";
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 9);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'V'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"GTT", "GTC", "GTA", "GTG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return 3.02;
    }

    @Override
    public double getHelicity() {
        return 1.27;
    }

    @Override
    public double getBasicity() {
        return 208.7;
    }

    @Override
    public double getPI() {
        return 6.00;
    }

    @Override
    public double getPK1() {
        return 2.39;
    }

    @Override
    public double getPK2() {
        return 9.74;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 105;
    }
}
