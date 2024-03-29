package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ExtendedPeptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.general.BoxedObject;
import java.util.Arrays;

/**
 * Iterator for unspecific cleavage.
 *
 * @author Marc Vaudel
 */
public class UnspecificIterator implements SequenceIterator {

    /**
     * Empty default constructor
     */
    public UnspecificIterator() {
        proteinIteratorUtils = null;
        proteinSequence = "";
        proteinSequenceAsCharArray = null;
        massMin = 0;
        massMax = 0;
    }

    /**
     * Utilities classes for the digestion.
     */
    private final ProteinIteratorUtils proteinIteratorUtils;
    /**
     * The protein sequence.
     */
    private final String proteinSequence;
    /**
     * The protein sequence as char array.
     */
    private final char[] proteinSequenceAsCharArray;
    /**
     * The minimal mass to consider.
     */
    private final double massMin;
    /**
     * The maximal mass to consider.
     */
    private final double massMax;
    /**
     * The peptide beginning index of the iterator.
     */
    private int index1 = 0;
    /**
     * The peptide end index of the iterator.
     */
    private int index2 = 0;

    /**
     * Constructor.
     *
     * @param proteinIteratorUtils utils for the creation of the peptides
     * @param proteinSequence the sequence to iterate
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     */
    public UnspecificIterator(ProteinIteratorUtils proteinIteratorUtils, String proteinSequence, double massMin, double massMax) {
        this.proteinIteratorUtils = proteinIteratorUtils;
        this.proteinSequence = proteinSequence;
        this.proteinSequenceAsCharArray = proteinSequence.toCharArray();
        this.massMin = massMin;
        this.massMax = massMax;
    }

    @Override
    public ExtendedPeptide getNextPeptide() throws InterruptedException {

        // Increase indices
        if (!increaseIndex()) {
            return null;
        }

        // Get the next sequence
        char[] sequence = Arrays.copyOfRange(proteinSequenceAsCharArray, index1, index2);

        // Construct the peptide
        BoxedObject<Boolean> smallMass = new BoxedObject<>(Boolean.TRUE);
        ExtendedPeptide extendedPeptide = proteinIteratorUtils.getPeptideFromProtein(sequence, proteinSequence, index1, massMin, massMax, smallMass);

        // Skip too heavy peptides
        if (!smallMass.getObject()) {
            index1++;
            if (index1 == proteinSequenceAsCharArray.length) {
                return null;
            }
            index2 = index1;
        }

        // Return the peptide if it passes the filters, continue iterating otherwise
        if (extendedPeptide != null
                && extendedPeptide.peptide.getMass() >= massMin
                && extendedPeptide.peptide.getMass() <= massMax) {
            return new ExtendedPeptide(extendedPeptide.peptide, index1, extendedPeptide.fixedModifications);
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
