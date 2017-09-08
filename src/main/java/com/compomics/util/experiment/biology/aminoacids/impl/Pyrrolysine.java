package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;

/**
 * Pyrrolysine.
 *
 * @author Harald Barsnes
 */
public class Pyrrolysine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 8680204019988094523L;

    /**
     * Constructor.
     */
    public Pyrrolysine() {
        singleLetterCode = "O";
        threeLetterCode = "Pyl";
        name = "Pyrrolysine";
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 12);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 19);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 3);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 2);
        subAminoAcidsWithoutCombination = new char[]{'O'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"TAG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        throw new UnsupportedOperationException("Not supported for Pyrrolysine.");
    }

    @Override
    public double getHelicity() {
        throw new UnsupportedOperationException("Not supported for Pyrrolysine.");
    }

    @Override
    public double getBasicity() {
        throw new UnsupportedOperationException("Not supported for Pyrrolysine.");
    }

    @Override
    public double getPI() {
        throw new UnsupportedOperationException("Not supported for Pyrrolysine.");
    }

    @Override
    public double getPK1() {
        throw new UnsupportedOperationException("Not supported for Pyrrolysine.");
    }

    @Override
    public double getPK2() {
        throw new UnsupportedOperationException("Not supported for Pyrrolysine.");
    }

    @Override
    public double getPKa() {
        throw new UnsupportedOperationException("Not supported for Pyrrolysine.");
    }

    @Override
    public int getVanDerWaalsVolume() {
        throw new UnsupportedOperationException("Not supported for Pyrrolysine.");
    }
}
