package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;

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
}
