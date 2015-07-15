package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;

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
}
