package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;

/**
 * SeC (U) (Mascot)-
 *
 * @author Harald Barsnes
 */
public class Selenocysteine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2123392615229813870L;

    /**
     * Constructor.
     */
    public Selenocysteine() {
        singleLetterCode = "U";
        threeLetterCode = "Sec";
        name = "Selenocysteine";
        averageMass = 150.0379;
        monoisotopicMass = 150.95363;
        monoisotopicAtomChain = new AtomChain();
        monoisotopicAtomChain.append(new AtomImpl(Atom.C, 0), 3);
        monoisotopicAtomChain.append(new AtomImpl(Atom.H, 0), 5);
        monoisotopicAtomChain.append(new AtomImpl(Atom.N, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.O, 0), 1);
        monoisotopicAtomChain.append(new AtomImpl(Atom.Se, 0), 1);
        subAminoAcidsWithoutCombination = new char[]{'U'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = new String[] {"TGA"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }

    @Override
    public double getHydrophobicity() {
        throw new UnsupportedOperationException("Not supported for Selenocysteine.");
    }

    @Override
    public double getHelicity() {
        throw new UnsupportedOperationException("Not supported for Selenocysteine.");
    }

    @Override
    public double getBasicity() {
        throw new UnsupportedOperationException("Not supported for Selenocysteine.");
    }

    @Override
    public double getPI() {
        return 5.47;
    }

    @Override
    public double getPK1() {
        return 1.91;
    }

    @Override
    public double getPK2() {
        return 10;
    }

    @Override
    public double getPKa() {
        return 5.43;
    }

    @Override
    public int getVanDerWaalsVolume() {
        throw new UnsupportedOperationException("Not supported for Selenocysteine.");
    }
}
