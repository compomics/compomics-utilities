package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.identification.protein_sequences.AmbiguousSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ExtendedPeptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;

/**
 * Iterator for no digestion of a sequence containing amino acid combinations.
 *
 * @author Marc Vaudel
 */
public class NoDigestionCombinationIterator implements SequenceIterator {

    /**
     * Utilities classes for the digestion.
     */
    private final ProteinIteratorUtils proteinIteratorUtils;
    /**
     * The protein sequence.
     */
    private final String proteinSequence;
    /**
     * The minimal mass to consider.
     */
    private final double massMin;
    /**
     * The maximal mass to consider.
     */
    private final double massMax;
    /**
     * The ambiguous sequence iterator.
     */
    private AmbiguousSequenceIterator ambiguousSequenceIterator = null;

    /**
     * Constructor.
     *
     * @param proteinIteratorUtils utils for the creation of the peptides
     * @param proteinSequence the sequence to iterate
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     */
    public NoDigestionCombinationIterator(ProteinIteratorUtils proteinIteratorUtils, String proteinSequence, Double massMin, Double massMax) {

        this.proteinIteratorUtils = proteinIteratorUtils;
        this.proteinSequence = proteinSequence;
        this.massMin = massMin;
        this.massMax = massMax;
        ambiguousSequenceIterator = getSequenceIterator();

    }

    /**
     * Returns the sequence iterator.
     *
     * @return the sequence iterator
     */
    private AmbiguousSequenceIterator getSequenceIterator() {

        // See if the sequence is valid
        int nCombinations = 0;
        int nX = 0;
        double minPossibleMass = 0.0;
        double maxPossibleMass = 0.0;
        char[] sequenceAsCharArray = proteinSequence.toCharArray();

        for (int i = 0; i < sequenceAsCharArray.length; i++) {

            char aa = sequenceAsCharArray[i];

            if (aa == 'X') {

                nX++;

                if (nX > proteinIteratorUtils.getMaxXsInSequence()) {

                    // Skip iteration
                    return new AmbiguousSequenceIterator("", 0);

                }
            }

            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);

            if (aminoAcid.iscombination()) {

                nCombinations++;
                char[] possibleAas = aminoAcid.getSubAminoAcids(false);
                char subAa = possibleAas[0];
                AminoAcid subAminoAcid = AminoAcid.getAminoAcid(subAa);
                double tempMass = subAminoAcid.getMonoisotopicMass();
                double tempMassMin = tempMass;
                double tempMassMax = tempMass;

                for (int j = 1; j < possibleAas.length; j++) {

                    subAa = possibleAas[j];
                    subAminoAcid = AminoAcid.getAminoAcid(subAa);
                    tempMass = subAminoAcid.getMonoisotopicMass();

                    if (tempMass < tempMassMin) {

                        tempMassMin = tempMass;

                    } else if (tempMass > tempMassMax) {

                        tempMassMax = tempMass;

                    }
                }

                minPossibleMass += tempMassMin;
                maxPossibleMass += tempMassMax;

            } else {

                double tempMass = aminoAcid.getMonoisotopicMass();
                minPossibleMass += tempMass;
                maxPossibleMass += tempMass;

            }
        }

        // See if the mass is outside min and max boundaries
        if (maxPossibleMass < massMin
                || minPossibleMass > massMax) {

            // Skip iteration
            return new AmbiguousSequenceIterator("", 0);

        }

        // Set up iterator
        return new AmbiguousSequenceIterator(proteinSequence, nCombinations);
    }

    @Override
    public ExtendedPeptide getNextPeptide() throws InterruptedException {

        // Get the next sequence
        char[] sequence = ambiguousSequenceIterator.getNextSequence();

        // Iteration finished
        if (sequence == null) {
            return null;
        }

        // Create the new peptide
        ExtendedPeptide extendedPeptide = proteinIteratorUtils.getPeptideFromProtein(sequence, 0, massMin, massMax);

        if (extendedPeptide != null
                && extendedPeptide.peptide.getMass() >= massMin
                && extendedPeptide.peptide.getMass() <= massMax) {

            return extendedPeptide;

        } else {

            return getNextPeptide();

        }
    }
}
