package com.compomics.util.experiment.identification.protein_sequences.digestion;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.identification.protein_sequences.digestion.iterators.SpecificSingleEnzymeIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.iterators.NoDigestionIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.iterators.NoDigestionCombinationIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.iterators.SpecificSingleEnzymeCombinationIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.iterators.UnspecificCombinationIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.iterators.UnspecificIterator;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import java.util.ArrayList;

/**
 * The iterator goes through a sequence and lists possible peptides with their
 * fixed modifications.
 *
 * @author Marc Vaudel
 */
public class IteratorFactory {

    /**
     * The utils used to generate the peptides.
     */
    private ProteinIteratorUtils proteinIteratorUtils;

    /**
     * Constructor.
     *
     * @param fixedModifications a list of fixed modifications to consider when
     * iterating the protein sequences.
     * @param maxX The maximal number of Xs allowed in a sequence to derive the
     * possible peptides
     */
    public IteratorFactory(ArrayList<String> fixedModifications, Integer maxX) {
        this.proteinIteratorUtils = new ProteinIteratorUtils(fixedModifications, maxX);
    }

    /**
     * Constructor with 2 Xs allowed.
     *
     * @param fixedModifications a list of fixed modifications to consider when
     * iterating the protein sequences.
     */
    public IteratorFactory(ArrayList<String> fixedModifications) {
        this(fixedModifications, null);
    }

    /**
     * Returns a sequence iterator for the given protein sequence and digestion
     * preferences.
     *
     * @param sequence the sequence to iterate
     * @param digestionPreferences the digestion preferences to use
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     *
     * @return a sequence iterator
     * 
     * @throws java.lang.InterruptedException exception thrown if a thread is
     * interrupted
     */
    public SequenceIterator getSequenceIterator(String sequence, DigestionParameters digestionPreferences, double massMin, double massMax) throws InterruptedException {
        switch (digestionPreferences.getCleavagePreference()) {
            case unSpecific:
                if (AminoAcidSequence.hasCombination(sequence)) {
                    return new UnspecificCombinationIterator(proteinIteratorUtils, sequence, massMin, massMax);
                }
                return new UnspecificIterator(proteinIteratorUtils, sequence, massMin, massMax);
            case wholeProtein:
                if (AminoAcidSequence.hasCombination(sequence)) {
                    return new NoDigestionCombinationIterator(proteinIteratorUtils, sequence, massMin, massMax);
                }
                return new NoDigestionIterator(proteinIteratorUtils, sequence, massMin, massMax);
            case enzyme:
                ArrayList<Enzyme> enzymes = digestionPreferences.getEnzymes();
                if (enzymes.size() == 1) {
                    Enzyme enzyme = enzymes.get(0);
                    int nMissedCleavages = digestionPreferences.getnMissedCleavages(enzyme.getName());
                    if (AminoAcidSequence.hasCombination(sequence)) {
                        return new SpecificSingleEnzymeCombinationIterator(proteinIteratorUtils, sequence, enzyme, nMissedCleavages, massMin, massMax);
                    }
                    return new SpecificSingleEnzymeIterator(proteinIteratorUtils, sequence, enzyme, nMissedCleavages, massMin, massMax);
                }
            default:
                throw new UnsupportedOperationException("Cleavage preference of type " + digestionPreferences.getCleavagePreference() + " not supported.");
        }
    }
}
