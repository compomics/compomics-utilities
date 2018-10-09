package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ExtendedPeptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.general.BoxedObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Iterator for enzymatic digestion.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SpecificSingleEnzymeIterator implements SequenceIterator {

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
     * The enzyme to use to digest the sequence.
     */
    private final Enzyme enzyme;
    /**
     * The maximum number of missed cleavages
     */
    private final int nMissedCleavages;
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
    private final ArrayList<ExtendedPeptide> result;
    /**
     * Index of the result iterator.
     */
    private int resultIndex = -1;

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
    public SpecificSingleEnzymeIterator(ProteinIteratorUtils proteinIteratorUtils, String proteinSequence, Enzyme enzyme, int nMissedCleavages, double massMin, double massMax) {
        this.proteinIteratorUtils = proteinIteratorUtils;
        this.proteinSequence = proteinSequence;
        this.proteinSequenceAsCharArray = proteinSequence.toCharArray();
        this.enzyme = enzyme;
        this.nMissedCleavages = nMissedCleavages;
        this.massMin = massMin;
        this.massMax = massMax;
        this.peptideStartMap = new HashMap<>(nMissedCleavages + 1);
        this.result = new ArrayList<>(nMissedCleavages + 1);
    }

    @Override
    public ExtendedPeptide getNextPeptide() throws InterruptedException {

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
     *
     * @throws java.lang.InterruptedException exception thrown if a thread is
     * interrupted
     */
    private void iterateSequence() throws InterruptedException {

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
        BoxedObject<Boolean> smallMass = new BoxedObject<>(Boolean.TRUE);
        ExtendedPeptide extendedPeptide = proteinIteratorUtils.getPeptideFromProtein(newSequence, proteinSequence, initialIndex, massMin, massMax, smallMass);
        if (extendedPeptide != null
                && extendedPeptide.peptide.getMass() >= massMin
                && extendedPeptide.peptide.getMass() <= massMax) {
            result.add(new ExtendedPeptide(extendedPeptide.peptide, initialIndex, extendedPeptide.fixedModifications));
        }

        if (nMissedCleavages > 0) {
            if (smallMass.getObject()) {
                HashMap<Integer, Integer> newPeptideStartMap = new HashMap<>(peptideStartMap.size());
                newPeptideStartMap.put(initialIndex, 0);
                for (int peptideStart : peptideStartMap.keySet()) {
                    newSequence = Arrays.copyOfRange(proteinSequenceAsCharArray, peptideStart, sequenceIndex);
                    smallMass.setObject(Boolean.TRUE);
                    extendedPeptide = proteinIteratorUtils.getPeptideFromProtein(newSequence, proteinSequence, peptideStart, massMin, massMax, smallMass);
                    if (extendedPeptide != null
                            && extendedPeptide.peptide.getMass() >= massMin
                            && extendedPeptide.peptide.getMass() <= massMax) {
                        result.add(new ExtendedPeptide(extendedPeptide.peptide, peptideStart, extendedPeptide.fixedModifications));
                    }
                    int peptideMissedCleavages = peptideStartMap.get(peptideStart);
                    if (smallMass.getObject() && peptideMissedCleavages + 1 < nMissedCleavages) {
                        newPeptideStartMap.put(peptideStart, peptideMissedCleavages + 1);
                    }
                }
                peptideStartMap = newPeptideStartMap;
            } else {
                peptideStartMap.clear();
            }
        }
        resultIndex = -1;
    }
}
