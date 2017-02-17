package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.AmbiguousSequenceIterator;
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
public class SingleEnzymeCombinationIterator implements SequenceIterator {

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
    private HashMap<String, Integer> ambiguousPeptidesStartMap;
    private HashMap<String, Integer> ambiguousPeptidesMC;
    private String[] ambiguousPeptides;
    private AmbiguousSequenceIterator ambiguousSequenceIterator = null;
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
    private int ambiguousPeptidesIndex = 0;

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
    public SingleEnzymeCombinationIterator(ProteinIteratorUtils proteinIteratorUtils, String proteinSequence, Enzyme enzyme, int nMissedCleavages, Double massMin, Double massMax) {
        this.proteinIteratorUtils = proteinIteratorUtils;
        this.proteinSequence = proteinSequence;
        this.enzyme = enzyme;
        this.nMissedCleavages = nMissedCleavages;
        this.massMin = massMin;
        this.massMax = massMax;
        this.peptideStartMap = new HashMap<Integer, Integer>(nMissedCleavages + 1);
        this.result = new ArrayList<PeptideWithPosition>(nMissedCleavages + 1);
        this.ambiguousPeptidesStartMap = new HashMap<String, Integer>(nMissedCleavages + 1);
        this.ambiguousPeptides = new String[0];
    }

    @Override
    public PeptideWithPosition getNextPeptide() {

        // Return the next result if any
        resultIndex++;
        if (resultIndex < result.size()) {
            return result.get(resultIndex);
        }

        // See if there are ambiguous sequences to iterate
        if (ambiguousSequenceIterator != null) {
            char[] newSequence = ambiguousSequenceIterator.getNextSequence();
            if (newSequence == null) {
                ambiguousSequenceIterator = null;
                return getNextPeptide();
            }
            int peptidesMissedCleavages = 0;
            for (int i = 1; i <= newSequence.length; i++) {
                char aaBefore = newSequence[i - 1];
                char aaAfter = newSequence[i];
                if (enzyme.isCleavageSiteNoCombination(aaBefore, aaAfter)) {
                    peptidesMissedCleavages++;
                    if (peptidesMissedCleavages > nMissedCleavages) {
                        return getNextPeptide();
                    }
                }
            }
            Peptide peptide = proteinIteratorUtils.getPeptideFromProtein(newSequence, proteinSequence, massMin, massMax);
            if (peptide != null
                    && (massMin == null || peptide.getMass() >= massMin)
                    && (massMax == null || peptide.getMass() <= massMax)) {
                String ambiguousSequence = ambiguousPeptides[ambiguousPeptidesIndex];
                int startIndex = ambiguousPeptidesStartMap.get(ambiguousSequence);
                return new PeptideWithPosition(peptide, startIndex);
            }
            return getNextPeptide();
        }
        if (ambiguousPeptidesIndex < ambiguousPeptides.length) {
            String ambiguousSequence = ambiguousPeptides[ambiguousPeptidesIndex];
            ambiguousSequenceIterator = new AmbiguousSequenceIterator(ambiguousSequence, proteinIteratorUtils.getMaxXsInSequence());
            ambiguousPeptidesIndex++;
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

        ArrayList<Character> firstAaCombination = new ArrayList<Character>(1);
        if (sequenceIndex > 0) {
            char aaAfter = proteinSequenceAsCharArray[sequenceIndex];
            AminoAcid aminoAcidAfter = AminoAcid.getAminoAcid(aaAfter);
            if (aminoAcidAfter.iscombination()) {
                char aaBefore = proteinSequenceAsCharArray[sequenceIndex - 1];
                AminoAcid aminoAcidBefore = AminoAcid.getAminoAcid(aaBefore);
                for (char aaBeforeTemp : aminoAcidBefore.getSubAminoAcids(false)) {
                    for (char aaAfterTemp : aminoAcidAfter.getSubAminoAcids(false)) {
                        if (enzyme.isCleavageSiteNoCombination(aaBeforeTemp, aaAfterTemp)) {
                            firstAaCombination.add(aaAfterTemp);
                        }
                    }
                }
            }
        }
        if (firstAaCombination.isEmpty()) {
            firstAaCombination.add(proteinSequenceAsCharArray[sequenceIndex]);
        }

        ArrayList<Character> lastAaCombination = new ArrayList<Character>(1);
        while (++sequenceIndex < proteinSequenceAsCharArray.length) {

            char aaBefore = proteinSequenceAsCharArray[sequenceIndex - 1];
            char aaAfter = proteinSequenceAsCharArray[sequenceIndex];
            if (enzyme.isCleavageSiteNoCombination(aaBefore, aaAfter)) {
                break;
            }
            AminoAcid aminoAcidBefore = AminoAcid.getAminoAcid(aaBefore);
            AminoAcid aminoAcidAfter = AminoAcid.getAminoAcid(aaAfter);
            if (aminoAcidBefore.iscombination() || aminoAcidAfter.iscombination()) {
                boolean cleavage = false;
                for (char aaBeforeTemp : aminoAcidBefore.getSubAminoAcids(false)) {
                    for (char aaAfterTemp : aminoAcidAfter.getSubAminoAcids(false)) {
                        if (enzyme.isCleavageSiteNoCombination(aaBeforeTemp, aaAfterTemp)) {
                            if (aminoAcidBefore.iscombination()) {
                                lastAaCombination.add(aaBeforeTemp);
                            }
                            cleavage = true;
                        }
                    }
                }
                if (cleavage) {
                    break;
                }
            }
        }
        if (lastAaCombination.isEmpty()) {
            lastAaCombination.add(proteinSequenceAsCharArray[sequenceIndex - 1]);
        }

        result.clear();
        HashMap<String, Integer> newAmbiguousPeptidesStartMap = new HashMap<String, Integer>(ambiguousPeptidesStartMap.size());
        HashMap<String, Integer> newambiguousPeptidesMC = new HashMap<String, Integer>(ambiguousPeptidesMC.size());
        HashMap<Integer, Integer> newPeptideStartMap = new HashMap<Integer, Integer>(peptideStartMap.size());
        char[] newSequence = Arrays.copyOfRange(proteinSequenceAsCharArray, initialIndex, sequenceIndex);
        for (char firstAa : firstAaCombination) {
            newSequence[0] = firstAa;
            for (char lastAa : lastAaCombination) {
                newSequence[newSequence.length - 1] = lastAa;
                BoxedObject<Boolean> smallMass = new BoxedObject<Boolean>(Boolean.TRUE);
                if (!AminoAcidSequence.hasCombination(newSequence)) {
                    Peptide peptide = proteinIteratorUtils.getPeptideFromProtein(newSequence, proteinSequence, massMin, massMax, smallMass);
                    if (peptide != null
                            && (massMin == null || peptide.getMass() >= massMin)
                            && (massMax == null || peptide.getMass() <= massMax)) {
                        result.add(new PeptideWithPosition(peptide, initialIndex));
                    }
                } else {
                    smallMass.setObject(massMax == null || AminoAcidSequence.getMinMass(newSequence) <= massMax);
                    String newSequenceAsString = new String(newSequence);
                    newAmbiguousPeptidesStartMap.put(newSequenceAsString, initialIndex);
                    newambiguousPeptidesMC.put(newSequenceAsString, 0);
                }

                if (nMissedCleavages > 0) {
                    if (smallMass.getObject()) {
                        if (!AminoAcidSequence.hasCombination(newSequence)) {
                            newPeptideStartMap.put(initialIndex, 0);
                            for (int peptideStart : peptideStartMap.keySet()) {
                                newSequence = Arrays.copyOfRange(proteinSequenceAsCharArray, peptideStart, sequenceIndex);
                                smallMass.setObject(Boolean.TRUE);
                                Peptide peptide = proteinIteratorUtils.getPeptideFromProtein(newSequence, proteinSequence, massMin, massMax, smallMass);
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
                        } else {
                            for (int peptideStart : peptideStartMap.keySet()) {
                                char[] previousSequence = Arrays.copyOfRange(proteinSequenceAsCharArray, peptideStart, initialIndex);
                                char[] misCleavedSequence = Util.mergeCharArrays(previousSequence, newSequence);
                                int peptideMissedCleavages = peptideStartMap.get(peptideStart);
                                String newSequenceAsString = new String(misCleavedSequence);
                                newAmbiguousPeptidesStartMap.put(newSequenceAsString, peptideStart);
                                newambiguousPeptidesMC.put(newSequenceAsString, peptideMissedCleavages + 1);
                            }
                        }
                        for (String previousSequence : ambiguousPeptidesStartMap.keySet()) {
                            int peptideMissedCleavages = ambiguousPeptidesMC.get(previousSequence);
                            if (peptideMissedCleavages < nMissedCleavages) {
                                int peptideStart = ambiguousPeptidesStartMap.get(previousSequence);
                                StringBuilder misCleavedSequenceBuilder = new StringBuilder(previousSequence.length() + newSequence.length);
                                misCleavedSequenceBuilder.append(previousSequence);
                                misCleavedSequenceBuilder.append(newSequence);
                                String misCleavedSequence = misCleavedSequenceBuilder.toString();
                                newAmbiguousPeptidesStartMap.put(misCleavedSequence, peptideStart);
                                newambiguousPeptidesMC.put(misCleavedSequence, peptideMissedCleavages+1);
                            }
                        }
                    }
                }
            }
        }
        peptideStartMap = newPeptideStartMap;
        ambiguousPeptidesStartMap = newAmbiguousPeptidesStartMap;
        ambiguousPeptidesMC = newambiguousPeptidesMC;
        ambiguousPeptides = new String[ambiguousPeptides.length];
        int count = 0;
        for (String sequence : ambiguousPeptidesStartMap.keySet()) {
            ambiguousPeptides[count] = sequence;
            count++;
        }
        
        resultIndex = 0;
        ambiguousPeptidesIndex = 0;
    }
}
