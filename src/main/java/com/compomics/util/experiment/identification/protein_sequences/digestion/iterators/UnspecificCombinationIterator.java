package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.AmbiguousSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideDraft;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideWithPosition;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.general.BoxedObject;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

    private String proteinSequence;
    private char[] proteinSequenceAsCharArray;

    private Double massMin;
    private Double massMax;

    AmbiguousSequenceIterator ambiguousSequenceIterator = null;

    private int index1 = 0;
    private int index2 = 1;

    /**
     * Constructor.
     *
     * @param proteinIteratorUtils utils for the creation of the peptides
     * @param proteinSequence the sequence to iterate
     * @param digestionPreferences the digestion preferences to use
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     */
    public UnspecificCombinationIterator(ProteinIteratorUtils proteinIteratorUtils, String proteinSequence, DigestionPreferences digestionPreferences, Double massMin, Double massMax) {
        this.proteinIteratorUtils = proteinIteratorUtils;
        this.proteinSequence = proteinSequence;
        this.proteinSequenceAsCharArray = proteinSequence.toCharArray();
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
            Peptide peptide = proteinIteratorUtils.getPeptideFromProtein(sequence, proteinSequence, massMin, massMax);

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
        BoxedObject<Boolean> smallMass = new BoxedObject<Boolean>(Boolean.TRUE);
        Peptide peptide = proteinIteratorUtils.getPeptideFromProtein(sequence, proteinSequence, massMin, massMax, smallMass);

        // Skip too heavy peptides
        if (!smallMass.getObject()) {
            index1++;
            index2 = index1 + 1;
            if (index1 == proteinSequenceAsCharArray.length) {
                return null;
            }
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

    private boolean increaseIndex() {

        index2++;
        if (index2 == proteinSequenceAsCharArray.length + 1) {
            index1++;
            index2 = index1 + 1;
            if (index1 == proteinSequenceAsCharArray.length) {
                return false;
            }
        }
        return true;

    }
}
