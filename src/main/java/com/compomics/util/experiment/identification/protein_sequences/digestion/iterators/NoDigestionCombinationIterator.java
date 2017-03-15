package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.AmbiguousSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideWithPosition;
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
    private int index2 = 1;
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

        // See if we have a valid mass
        if (massMin != null && maxPossibleMass < massMin
                || massMax != null && minPossibleMass > massMax) {

            // Skip iteration
            return new AmbiguousSequenceIterator("", 0);

        }

        // Set up iterator
        return new AmbiguousSequenceIterator(proteinSequence, nCombinations);
    }

    @Override
    public PeptideWithPosition getNextPeptide() {
        
        // Get the next sequence
        char[] sequence = ambiguousSequenceIterator.getNextSequence();
        
        // Iteration finished
        if (sequence == null) {
            return null;
        }

        // Create the new peptide
        Peptide peptide = proteinIteratorUtils.getPeptideFromProtein(sequence, 0, massMin, massMax);
        if (peptide != null
                && (massMin == null || peptide.getMass() >= massMin)
                && (massMax == null || peptide.getMass() <= massMax)) {
            return new PeptideWithPosition(peptide, 0);
        } else {
            return getNextPeptide();
        }
    }
}
