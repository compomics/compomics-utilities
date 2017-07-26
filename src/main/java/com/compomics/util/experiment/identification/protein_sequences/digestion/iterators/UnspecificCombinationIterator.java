package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.AmbiguousSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideWithPosition;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.general.BoxedObject;
import java.util.Arrays;

/**
 * Iterator for unspecific cleavage.
 *
 * @author Marc Vaudel
 */
public class UnspecificCombinationIterator implements SequenceIterator {

    /**
     * Utilities classes for the digestion.
     */
    private ProteinIteratorUtils proteinIteratorUtils;
    /**
     * The protein sequence.
     */
    private String proteinSequence;
    /**
     * The protein sequence as char array.
     */
    private char[] proteinSequenceAsCharArray;
    /**
     * The minimal mass to consider.
     */
    private Double massMin;
    /**
     * The maximal mass to consider.
     */
    private Double massMax;
    /**
     * The peptide beginning index of the iterator.
     */
    private int index1 = 0;
    /**
     * The peptide end index of the iterator.
     */
    private int index2 = 0;
    /**
     * The ambiguous sequence iterator.
     */
    AmbiguousSequenceIterator ambiguousSequenceIterator = null;

    /**
     * Constructor.
     *
     * @param proteinIteratorUtils utils for the creation of the peptides
     * @param proteinSequence the sequence to iterate
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     */
    public UnspecificCombinationIterator(ProteinIteratorUtils proteinIteratorUtils, String proteinSequence, Double massMin, Double massMax) {
        this.proteinIteratorUtils = proteinIteratorUtils;
        this.proteinSequence = proteinSequence;
        this.proteinSequenceAsCharArray = proteinSequence.toCharArray();
        this.massMin = massMin;
        this.massMax = massMax;
    }

    @Override
    public PeptideWithPosition getNextPeptide() {

        // See if an ambiguous sequence is being iterated
        if (ambiguousSequenceIterator != null) {

            // Get the next sequence
            char[] sequence = ambiguousSequenceIterator.getNextSequence();

            // If the iteration is finished, go to next amino acid
            if (sequence == null) {
                ambiguousSequenceIterator = null;
                return getNextPeptide();
            }

            // Construct the peptide
            Peptide peptide = proteinIteratorUtils.getPeptideFromProtein(sequence, proteinSequence, index1, massMin, massMax);

            // Return the peptide if it passes the filters, continue iterating otherwise
            if (peptide != null
                    && (massMin == null || peptide.getMass() >= massMin)
                    && (massMax == null || peptide.getMass() <= massMax)) {
                return new PeptideWithPosition(peptide, index1);
            } else {
                return getNextPeptide();
            }
        }

        // Increase indices
        if (!increaseIndex()) {
            return null;
        }

        // Get the next sequence
        char[] sequence = Arrays.copyOfRange(proteinSequenceAsCharArray, index1, index2);

        // See whether the peptide contains amino acids combinations
        if (AminoAcidSequence.hasCombination(sequence)) {

            // Set-up iterator for the ambiguous sequences
            ambiguousSequenceIterator = new AmbiguousSequenceIterator(sequence, proteinIteratorUtils.getMaxXsInSequence());
            return getNextPeptide();
        }

        // Construct the peptide
        BoxedObject<Boolean> smallMass = new BoxedObject<>(Boolean.TRUE);
        Peptide peptide = proteinIteratorUtils.getPeptideFromProtein(sequence, proteinSequence, index1, massMin, massMax, smallMass);

        // Skip too heavy peptides
        if (!smallMass.getObject()) {
            index1++;
            if (index1 == proteinSequenceAsCharArray.length) {
                return null;
            }
            index2 = index1;
        }

        // Return the peptide if it passes the filters, continue iterating otherwise
        if (peptide != null
                && (massMin == null || peptide.getMass() >= massMin)
                && (massMax == null || peptide.getMass() <= massMax)) {
            return new PeptideWithPosition(peptide, index1);
        } else {
            return getNextPeptide();
        }
    }

    /**
     * Increases the index.
     * 
     * @return a boolean indicating whether there is another index
     */
    private boolean increaseIndex() {

        index2++;
        if (index2 == proteinSequenceAsCharArray.length + 1) {
            index1++;
            if (index1 == proteinSequenceAsCharArray.length) {
                return false;
            }
            index2 = index1 + 1;
        }
        return true;
    }
}
