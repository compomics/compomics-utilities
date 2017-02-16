package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideDraft;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideWithPosition;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Iterator for no digestion of a sequence containing amino acid combinations.
 *
 * @author Marc Vaudel
 */
public class NoDigestionIteratorCombination implements SequenceIterator {

    /**
     * Utilities classes for the digestion.
     */
    private ProteinIteratorUtils proteinIteratorUtils;

    private String proteinSequence;
    private DigestionPreferences digestionPreferences;
    private Double massMin;
    private Double massMax;

    private ArrayList<char[]> aaCombinations;
    private int[] iterationIndices;
    private int[] indicesOnSequence;
    private int secondaryIndex = 0;

    /**
     * Constructor.
     *
     * @param proteinIteratorUtils utils for the creation of the peptides
     * @param proteinSequence the sequence to iterate
     * @param digestionPreferences the digestion preferences to use
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     */
    public NoDigestionIteratorCombination(ProteinIteratorUtils proteinIteratorUtils, String proteinSequence, DigestionPreferences digestionPreferences, Double massMin, Double massMax) {
        this.proteinIteratorUtils = proteinIteratorUtils;
        this.proteinSequence = proteinSequence;
        this.digestionPreferences = digestionPreferences;
        this.massMin = massMin;
        this.massMax = massMax;
    }

    private void initialize() {

        // Find amino acid combinations and store them in a map
        int initialSize = Math.min(proteinIteratorUtils.getMaxXsInSequence(), 16);
        aaCombinations = new ArrayList<char[]>(initialSize);
        ArrayList<Integer> indicesList = new ArrayList<Integer>(initialSize);
        double minPossibleMass = 0.0;
        double maxPossibleMass = 0.0;
        char[] sequenceAsCharArray = proteinSequence.toCharArray();
        for (int i = 0; i < sequenceAsCharArray.length; i++) {
            char aa = sequenceAsCharArray[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            if (aminoAcid.iscombination()) {
                char[] possibleAas = aminoAcid.getSubAminoAcids(false);
                aaCombinations.add(possibleAas);
                indicesList.add(i);
                if (massMin != null || massMax != null) {
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
                }
            } else if (massMin != null || massMax != null) {
                double tempMass = aminoAcid.getMonoisotopicMass();
                minPossibleMass += tempMass;
                maxPossibleMass += tempMass;
            }
        }

        // See if we have a valid mass
        if (massMin != null && maxPossibleMass < massMin
                || massMax != null && minPossibleMass > massMax) {

            // Make an end status
            char[] aasAtIndex = aaCombinations.get(0);
            indicesOnSequence = new int[1];
            indicesOnSequence[0] = aasAtIndex.length - 1;

        } else {

            // Set up indices for iteration
            indicesOnSequence = new int[indicesList.size()];
            iterationIndices = new int[indicesList.size()];
            int count = 0;
            for (Integer index : indicesList) {
                indicesOnSequence[count] = index;
                iterationIndices[count] = 0;
                count++;
            }
            iterationIndices[0] = -1;
        }
    }

    private boolean increaseIndices() {
        if (secondaryIndex == iterationIndices.length) {
            return false;
        }
        char[] aasAtIndex = aaCombinations.get(secondaryIndex);
        int aaIndex = iterationIndices[secondaryIndex];
        aaIndex++;
        if (aaIndex == aasAtIndex.length) {
            iterationIndices[secondaryIndex] = 0;
            secondaryIndex++;
            return increaseIndices();
        }
        iterationIndices[secondaryIndex] = aaIndex;
        return true;
    }

    @Override
    public PeptideWithPosition getNextPeptide() {

        // Increase the amino acid iteration indices
        if (!increaseIndices()) {
            return null;
        }

        // Create the new sequence
        char[] sequenceAsCharArray = proteinSequence.toCharArray();
        for (int i = 0 ; i < indicesOnSequence.length ; i++) {
            int index = indicesOnSequence[i];
            char[] possibleAas = aaCombinations.get(i);
            int iterationIndex = iterationIndices[i];
            char aa = possibleAas[iterationIndex];
            sequenceAsCharArray[index] = aa;
        }
        String sequence = new String(sequenceAsCharArray);
        
        // Create the new peptide
        Peptide peptide = proteinIteratorUtils.getPeptideNoDigestion(sequence, sequence, massMin, massMax);
        if (peptide != null
                && (massMin == null || peptide.getMass() >= massMin)
                && (massMax == null || peptide.getMass() <= massMax)) {
            return new PeptideWithPosition(peptide, 0);
        } else {
            return getNextPeptide();
        }
    }

    /**
     * Makes a peptide from the given sequence without digestion. If the
     * sequence presents ambiguous amino acids returns all different
     * possibilities. Peptides are filtered according to the given masses.
     * Filters are ignored if null.
     *
     * @param sequence the amino acid sequence
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return a peptide built from the given sequence
     */
    public ArrayList<PeptideWithPosition> getPeptidesNoDigestion(String sequence, Double massMin, Double massMax) {

        if (AminoAcidSequence.containsAmbiguousAminoAcid(sequence)) {

            int nX = 0;
            for (int i = 0; i < sequence.length(); i++) {
                if (sequence.charAt(i) == 'X') {
                    nX++;
                    if (nX > proteinIteratorUtils.getMaxXsInSequence()) {
                        return new ArrayList<PeptideWithPosition>(0);
                    }
                }
            }

            char[] sequenceAsCharArray = sequence.toCharArray();
            char aa = sequenceAsCharArray[0];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            char[] aaCombinations = aminoAcid.getSubAminoAcids();
            ArrayList<StringBuilder> sequences = new ArrayList<StringBuilder>(aaCombinations.length);

            for (char subAa : aaCombinations) {
                StringBuilder subSequence = new StringBuilder();
                subSequence.append(subAa);
                sequences.add(subSequence);
            }

            for (int j = 1; j < sequenceAsCharArray.length; j++) {

                aa = sequenceAsCharArray[j];
                aminoAcid = AminoAcid.getAminoAcid(aa);
                aaCombinations = aminoAcid.getSubAminoAcids();

                if (aaCombinations.length == 1) {
                    for (StringBuilder peptideSequence : sequences) {
                        peptideSequence.append(aaCombinations[0]);
                    }
                } else {

                    ArrayList<StringBuilder> newSequences = new ArrayList<StringBuilder>(aaCombinations.length * sequences.size());

                    for (StringBuilder peptideSequence : sequences) {
                        for (char subAa : aaCombinations) {
                            StringBuilder newSequence = new StringBuilder(peptideSequence);
                            newSequence.append(subAa);
                            newSequences.add(newSequence);
                        }
                    }

                    sequences = newSequences;
                }
            }

            ArrayList<PeptideWithPosition> result = new ArrayList<PeptideWithPosition>(sequences.size());

            for (StringBuilder peptideSequence : sequences) {

                String sequenceAsString = peptideSequence.toString();
                Peptide peptide = getPeptideNoDigestion(sequenceAsString, sequenceAsString, massMin, massMax);

                if (peptide != null) {
                    result.add(new PeptideWithPosition(peptide, 0));
                }
            }

            return result;
        } else {

            ArrayList<PeptideWithPosition> result = new ArrayList<PeptideWithPosition>(1);
            Peptide peptide = getPeptideNoDigestion(sequence, sequence, massMin, massMax);

            if (peptide != null
                    && (massMin == null || peptide.getMass() >= massMin)
                    && (massMax == null || peptide.getMass() <= massMax)) {
                result.add(new PeptideWithPosition(peptide, 0));
            }

            return result;
        }
    }

    /**
     * Returns a peptide from the given sequence on the given protein. The
     * sequence should not contain ambiguous amino acids. Peptides are filtered
     * according to the given masses. Filters are ignored if null.
     *
     * @param peptideSequence the peptide sequence
     * @param proteinSequence the protein sequence where this peptide was found
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return a peptide from the given sequence
     */
    public Peptide getPeptideNoDigestion(String peptideSequence, String proteinSequence, Double massMin, Double massMax) {

        char nTermAaChar = peptideSequence.charAt(0);
        String nTermModification = proteinIteratorUtils.getNtermModification(true, nTermAaChar, proteinSequence);
        HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
        double peptideMass = proteinIteratorUtils.getModificationMass(nTermModification);

        for (int i = 0; i < peptideSequence.length(); i++) {

            char aaChar = peptideSequence.charAt(i);
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aaChar);
            peptideMass += aminoAcid.getMonoisotopicMass();

            if (massMax != null && peptideMass + proteinIteratorUtils.getMinCtermMass() > massMax) {
                return null;
            }

            String modificationAtAa = proteinIteratorUtils.getFixedModificationAtAa(aaChar);

            if (modificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = proteinIteratorUtils.getModificationPattern(modificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, i)) {
                    peptideModifications.put(i + 1, modificationAtAa);
                    peptideMass += proteinIteratorUtils.getModificationMass(modificationAtAa);
                }
            }
        }

        PeptideDraft peptideDraft = new PeptideDraft(new StringBuilder(peptideSequence), nTermModification, peptideModifications, peptideMass);

        String cTermModification = proteinIteratorUtils.getCtermModification(peptideDraft, proteinSequence, 0);
        if (cTermModification != null) {
            double modificationMass = proteinIteratorUtils.getModificationMass(cTermModification);
            peptideMass = peptideDraft.getMass() + modificationMass;
            peptideDraft.setMass(peptideMass);
            peptideDraft.setcTermModification(cTermModification);
        }

        return peptideDraft.getPeptide(massMin, massMax);
    }
}
