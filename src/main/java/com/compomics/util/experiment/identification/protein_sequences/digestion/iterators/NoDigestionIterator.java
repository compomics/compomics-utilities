package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ExtendedPeptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;

/**
 * Iterator for no digestion.
 *
 * @author Marc Vaudel
 */
public class NoDigestionIterator implements SequenceIterator {

    /**
     * Utilities classes for the digestion.
     */
    private final ProteinIteratorUtils proteinIteratorUtils;
    /**
     * The peptide with position inferred from the sequence.
     */
    private ExtendedPeptide peptideWithPosition = null;

    /**
     * Constructor.
     *
     * @param proteinIteratorUtils utils for the creation of the peptides
     * @param sequence the sequence to iterate
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     *
     * @throws java.lang.InterruptedException exception thrown if a thread is
     * interrupted
     */
    public NoDigestionIterator(ProteinIteratorUtils proteinIteratorUtils, String sequence, double massMin, double massMax) throws InterruptedException {

        this.proteinIteratorUtils = proteinIteratorUtils;
        setPeptide(sequence, massMin, massMax);

    }

    @Override
    public ExtendedPeptide getNextPeptide() {
        ExtendedPeptide result = peptideWithPosition;
        peptideWithPosition = null;
        return result;
    }

    /**
     * Makes a peptide from the given sequence without digestion and saves it as
     * attribute. The sequence should not contain ambiguous amino acids.
     * Peptides are filtered according to the given masses. Filters are ignored
     * if null.
     *
     * @param sequence the amino acid sequence
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @throws java.lang.InterruptedException exception thrown if a thread is
     * interrupted
     */
    private void setPeptide(String sequence, double massMin, double massMax) throws InterruptedException {

        ExtendedPeptide extendedPeptide = proteinIteratorUtils.getPeptideFromProtein(sequence.toCharArray(), sequence, 0, massMin, massMax);

        if (extendedPeptide != null
                && extendedPeptide.peptide.getMass() >= massMin
                && extendedPeptide.peptide.getMass() <= massMax) {
            peptideWithPosition = extendedPeptide;
        }
    }
}
