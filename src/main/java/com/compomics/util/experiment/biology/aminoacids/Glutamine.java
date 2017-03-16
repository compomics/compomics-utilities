package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;

/**
 * Glutamine.
 *
 * @author Marc Vaudel
 */
public class Glutamine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -1552736259723394861L;

    /**
     * Constructor.
     */
    public Glutamine() {
        singleLetterCode = "Q";
        threeLetterCode = "Gln";
        name = "Glutamine";
        averageMass = 128.1292;
        monoisotopicMass = 128.058578;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 8);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 2);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 2);
        subAminoAcidsWithoutCombination = new char[]{'Q'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'Z', 'X'};
        standardGeneticCode = new String[] {"CAA", "CAG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        return -2.76;
    }

    @Override
    public double getHelicity() {
        return 0.96;
    }

    @Override
    public double getBasicity() {
        return 214.2;
    }

    @Override
    public double getPI() {
        return 5.65;
    }

    @Override
    public double getPK1() {
        return 2.17;
    }

    @Override
    public double getPK2() {
        return 9.13;
    }

    @Override
    public double getPKa() {
        return 0.0;
    }

    @Override
    public int getVanDerWaalsVolume() {
        return 114;
    }
}
