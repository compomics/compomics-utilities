package com.compomics.util.experiment.identification.protein_sequences.digestion;

/**
 * Interface for an iterator returning peptides along a protein sequence.
 *
 * @author Marc Vaudel
 */
public interface SequenceIterator {

    /**
     * Returns the next peptide that can be generated from the iterator. Null if none left.
     * 
     * @return the next peptide that can be generated from the iterator
     */
    public PeptideWithPosition getNextPeptide();
}
