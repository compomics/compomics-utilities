package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideWithPosition;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.general.BoxedObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Iterator for enzymatic digestion.
 *
 * @author Marc Vaudel
 */
public class SingleEnzymeIterator implements SequenceIterator {

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
     * The enzyme to use to digest the sequence.
     */
    private Enzyme enzyme;
    /**
     * The maximum number of missed cleavages
     */
    private int nMissedCleavages;
    /**
     * Map of the previous peptide starts to number of missed cleavages.
     */
    private HashMap<Integer, Integer> peptideStartMap;
    /**
     * Index of the sequence iterator.
     */
    private int sequenceIndex = 0;
    /**
     * The peptides found.
     */
    private ArrayList<PeptideWithPosition> result;
    /**
     * Index of the result iterator.
     */
    private int resultIndex = 0;

    /**
     * Constructor.
     *
     * @param proteinIteratorUtils utils for the creation of the peptides
     * @param proteinSequence the sequence to iterate
     * @param enzyme the enzyme to use for digestion
     * @param nMissedCleavages the maximal number of missed cleavages allowed
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     */
    public SingleEnzymeIterator(ProteinIteratorUtils proteinIteratorUtils, String proteinSequence, Enzyme enzyme, int nMissedCleavages, Double massMin, Double massMax) {
        this.proteinIteratorUtils = proteinIteratorUtils;
        this.proteinSequence = proteinSequence;
        this.enzyme = enzyme;
        this.nMissedCleavages = nMissedCleavages;
        this.massMin = massMin;
        this.massMax = massMax;
        this.peptideStartMap = new HashMap<Integer, Integer>(nMissedCleavages + 1);
        this.result = new ArrayList<PeptideWithPosition>(nMissedCleavages + 1);
    }

    @Override
    public PeptideWithPosition getNextPeptide() {

        // Return the next result if any
        resultIndex++;
        if (resultIndex < result.size()) {
            return result.get(resultIndex);
        }

        if (sequenceIndex == proteinSequenceAsCharArray.length) {
            return null;
        }

        // Get the next peptides from the sequence and return the first result
        iterateSequence();
        return getNextPeptide();
    }

    /**
     * Iterates the sequence to the next missed cleavage and stores the peptides
     * found in the result list.
     */
    private void iterateSequence() {

        int initialIndex = sequenceIndex;

        while (++sequenceIndex < proteinSequenceAsCharArray.length) {

            char aaBefore = proteinSequenceAsCharArray[sequenceIndex - 1];
            char aaAfter = proteinSequenceAsCharArray[sequenceIndex];
            if (enzyme.isCleavageSiteNoCombination(aaBefore, aaAfter)) {
                break;
            }
        }

        result.clear();
        char[] newSequence = Arrays.copyOfRange(proteinSequenceAsCharArray, initialIndex, sequenceIndex);
        BoxedObject<Boolean> smallMass = new BoxedObject<Boolean>(Boolean.TRUE);
        Peptide peptide = proteinIteratorUtils.getPeptideFromProtein(newSequence, proteinSequence, massMin, massMax, smallMass);
        if (peptide != null
                && (massMin == null || peptide.getMass() >= massMin)
                && (massMax == null || peptide.getMass() <= massMax)) {
            result.add(new PeptideWithPosition(peptide, initialIndex));
        }

        if (nMissedCleavages > 0) {
            if (smallMass.getObject()) {
                HashMap<Integer, Integer> newPeptideStartMap = new HashMap<Integer, Integer>(peptideStartMap.size());
                newPeptideStartMap.put(initialIndex, 0);
                for (int peptideStart : peptideStartMap.keySet()) {
                    newSequence = Arrays.copyOfRange(proteinSequenceAsCharArray, peptideStart, sequenceIndex);
                    smallMass.setObject(Boolean.TRUE);
                    peptide = proteinIteratorUtils.getPeptideFromProtein(newSequence, proteinSequence, massMin, massMax, smallMass);
                    if (peptide != null
                            && (massMin == null || peptide.getMass() >= massMin)
                            && (massMax == null || peptide.getMass() <= massMax)) {
                        result.add(new PeptideWithPosition(peptide, initialIndex));
                    }
                    int peptideMissedCleavages = peptideStartMap.get(peptideStart);
                    if (smallMass.getObject() && peptideMissedCleavages < nMissedCleavages) {
                        newPeptideStartMap.put(peptideStart, peptideMissedCleavages + 1);
                    }
                }
                peptideStartMap = newPeptideStartMap;
            } else {
                peptideStartMap.clear();
            }
        }
    }
}
