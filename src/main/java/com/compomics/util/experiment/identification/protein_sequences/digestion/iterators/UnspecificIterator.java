package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideDraft;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideWithPosition;
import com.compomics.util.general.BoxedObject;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Iterator for unspecific cleavage.
 *
 * @author Marc Vaudel
 */
public class UnspecificIterator {

    /**
     * Utilities classes for the digestion.
     */
    private ProteinIteratorUtils proteinIteratorUtils;

    /**
     * Constructor.
     * 
     * @param proteinIteratorUtils utils for the creation of the peptides
     */
    public UnspecificIterator(ProteinIteratorUtils proteinIteratorUtils) {
        this.proteinIteratorUtils = proteinIteratorUtils;
    }
    

    /**
     * Returns all the peptides that can be expected from a protein sequence.
     * The peptides can be filtered by minimal and maximal mass. If null is
     * provided as limit no filter will be used.
     *
     * @param proteinSequence the protein
     * @param massMin the minimal mass to consider
     * @param massMax the maximal mass to consider
     *
     * @return a list of all the peptides that can be expected from a protein
     * sequence.
     */
    public ArrayList<PeptideWithPosition> getPeptides(String proteinSequence, Double massMin, Double massMax) {

        if (AminoAcidSequence.hasCombination(proteinSequence)) {
            return getPeptidesAaCombinations(proteinSequence, massMin, massMax);
        }

        ArrayList<PeptideWithPosition> result = new ArrayList<PeptideWithPosition>();
        char[] sequenceAsCharArray = proteinSequence.toCharArray();

        for (int i = 0; i < sequenceAsCharArray.length; i++) {

            Double sequenceMass = 0.0;
            char nTermAaChar = sequenceAsCharArray[i];
            StringBuilder peptideSequence = new StringBuilder(sequenceAsCharArray.length - i);
            HashMap<Integer, String> fixedModifications = new HashMap<Integer, String>(1);
            String nTermModification = proteinIteratorUtils.getNtermModification(i == 0, nTermAaChar, proteinSequence);
            sequenceMass += proteinIteratorUtils.getModificationMass(nTermModification);

            for (int j = i; j < sequenceAsCharArray.length; j++) {

                Integer aaIndex = j - i;
                Character aaChar = sequenceAsCharArray[j];
                AminoAcid aminoAcid = AminoAcid.getAminoAcid(aaChar);
                sequenceMass += aminoAcid.getMonoisotopicMass();
                peptideSequence.append(aaChar);
                String modificationAtAa = proteinIteratorUtils.getFixedModificationAtAa(aaChar);

                if (modificationAtAa != null) {
                    AminoAcidPattern aminoAcidPattern = proteinIteratorUtils.getModificationPattern(modificationAtAa);
                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, j)) {
                        fixedModifications.put(aaIndex + 1, modificationAtAa);
                        sequenceMass += proteinIteratorUtils.getModificationMass(modificationAtAa);
                    }
                }

                PeptideDraft peptideDraft = new PeptideDraft(peptideSequence, nTermModification, fixedModifications, sequenceMass);
                BoxedObject<Boolean> smallMass = new BoxedObject<Boolean>(Boolean.FALSE);
                setCterm(peptideDraft, proteinSequence, j);
                Peptide peptide = peptideDraft.getPeptide(massMin, massMax, smallMass);

                if (!smallMass.getObject()) {
                    break;
                }
                if (peptide != null) {
                    PeptideWithPosition peptideWithPosition = new PeptideWithPosition(peptide, i);
                    result.add(peptideWithPosition);
                }
            }
        }

        return result;
    }

    /**
     * Sets the c-terminal modification if any to the given peptide draft.
     *
     * @param peptideDraft the peptide draft of interest
     * @param proteinSequence the protein sequence
     * @param indexOnProtein the index of the peptide draft on the protein
     * sequence
     */
    private void setCterm(PeptideDraft peptideDraft, String proteinSequence, int indexOnProtein) {

        String cTermModification = proteinIteratorUtils.getCtermModification(peptideDraft, proteinSequence, indexOnProtein);

        if (cTermModification != null) {
            double modificationMass = proteinIteratorUtils.getModificationMass(cTermModification);
            double peptideMass = peptideDraft.getMass() + modificationMass;
            peptideDraft.setMass(peptideMass);
            peptideDraft.setcTermModification(cTermModification);
        }
    }

    /**
     * Returns all the peptides that can be expected from a protein sequence
     * extending amino acid combinations. The peptides can be filtered by
     * minimal and maximal mass. If null is provided as limit no filter will be
     * used.
     *
     * @param proteinSequence the protein
     * @param massMin the minimal mass to consider
     * @param massMax the maximal mass to consider
     *
     * @return a list of all the peptides that can be expected from a protein
     * sequence.
     */
    private ArrayList<PeptideWithPosition> getPeptidesAaCombinations(String sequence, Double massMin, Double massMax) {

        ArrayList<PeptideWithPosition> result = new ArrayList<PeptideWithPosition>();
        char[] sequenceAsCharArray = sequence.toCharArray();

        for (int i = 0; i < sequenceAsCharArray.length; i++) {

            ArrayList<PeptideDraft> peptideDrafts = new ArrayList<PeptideDraft>(1);
            char sequenceNTermAaChar = sequenceAsCharArray[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(sequenceNTermAaChar);
            BoxedObject<Boolean> smallMasses = new BoxedObject<Boolean>(Boolean.FALSE);

            for (char nTermAaChar : aminoAcid.getSubAminoAcids()) {

                Double sequenceMass = 0.0;
                StringBuilder peptideSequence = new StringBuilder(sequenceAsCharArray.length - i);
                HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
                String nTermModification = proteinIteratorUtils.getNtermModification(i == 0, nTermAaChar, sequence);
                sequenceMass += proteinIteratorUtils.getModificationMass(nTermModification);
                AminoAcid subAminoAcid = AminoAcid.getAminoAcid(nTermAaChar);
                sequenceMass += subAminoAcid.getMonoisotopicMass();
                peptideSequence.append(nTermAaChar);
                String modificationAtAa = proteinIteratorUtils.getFixedModificationAtAa(nTermAaChar);

                if (modificationAtAa != null) {

                    AminoAcidPattern aminoAcidPattern = proteinIteratorUtils.getModificationPattern(modificationAtAa);

                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                        peptideModifications.put(1, modificationAtAa);
                        sequenceMass += proteinIteratorUtils.getModificationMass(modificationAtAa);
                    }
                }

                PeptideDraft simplePeptide = new PeptideDraft(peptideSequence, nTermModification, peptideModifications, sequenceMass);
                peptideDrafts.add(simplePeptide);
            }

            ArrayList<Peptide> peptidesAtIndex = getPeptides(peptideDrafts, sequence, i, massMin, massMax, smallMasses);
            for (Peptide peptide : peptidesAtIndex) {
                result.add(new PeptideWithPosition(peptide, i));
            }

            if (!smallMasses.getObject()) {
                return result;
            }

            for (int j = i + 1; j < sequenceAsCharArray.length; j++) {

                int aaIndex = j - i;
                ArrayList<PeptideDraft> newPeptideDrafts = new ArrayList<PeptideDraft>(peptideDrafts.size());
                char sequenceChar = sequenceAsCharArray[j];
                AminoAcid sequenceAminoAcid = AminoAcid.getAminoAcid(sequenceChar);

                for (PeptideDraft peptideDraft : peptideDrafts) {

                    if (sequenceChar != 'X' || peptideDraft.getnX() < proteinIteratorUtils.getMaxXsInSequence()) {
                        if (sequenceChar == 'X') {
                            peptideDraft.increaseNX();
                        }

                        for (char aaChar : sequenceAminoAcid.getSubAminoAcids()) {

                            StringBuilder peptideSequence;

                            if (sequenceAminoAcid.getSubAminoAcids().length > 1) {
                                peptideSequence = new StringBuilder(peptideDraft.getSequence());
                            } else {
                                peptideSequence = peptideDraft.getSequence();
                            }

                            Double peptideMass = peptideDraft.getMass();
                            String nTermModification = peptideDraft.getnTermModification();
                            HashMap<Integer, String> peptideModifications;

                            if (sequenceAminoAcid.getSubAminoAcids().length > 1) {
                                peptideModifications = new HashMap<Integer, String>(peptideDraft.getFixedAaModifications());
                            } else {
                                peptideModifications = peptideDraft.getFixedAaModifications();
                            }

                            AminoAcid subAminoAcid = AminoAcid.getAminoAcid(aaChar);
                            peptideMass += subAminoAcid.getMonoisotopicMass();
                            peptideSequence.append(aaChar);
                            String modificationAtAa = proteinIteratorUtils.getFixedModificationAtAa(aaChar);

                            if (modificationAtAa != null) {

                                AminoAcidPattern aminoAcidPattern = proteinIteratorUtils.getModificationPattern(modificationAtAa);

                                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, j)) {
                                    peptideModifications.put(aaIndex + 1, modificationAtAa);
                                    peptideMass += proteinIteratorUtils.getModificationMass(modificationAtAa);
                                }
                            }

                            PeptideDraft newPeptideDraft = new PeptideDraft(peptideSequence, nTermModification, peptideModifications, peptideMass);
                            newPeptideDrafts.add(newPeptideDraft);
                        }
                    }
                }

                peptideDrafts = newPeptideDrafts;
                smallMasses.setObject(false);
                peptidesAtIndex = getPeptides(peptideDrafts, sequence, j, massMin, massMax, smallMasses);
                for (Peptide peptide : peptidesAtIndex) {
                    result.add(new PeptideWithPosition(peptide, i));
                }

                if (!smallMasses.getObject()) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Returns the peptides that can be built from the given peptide drafts.
     *
     * @param peptideDrafts a list of peptide drafts
     * @param proteinSequence the protein sequence
     * @param indexOnProtein the index of the peptide drafts on the protein
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     * @param smallMasses an encapsulated boolean indicating whether at least
     * one of the peptides will pass the maximal mass filter
     *
     * @return the peptides that can be built from the given peptide drafts
     */
    private ArrayList<Peptide> getPeptides(ArrayList<PeptideDraft> peptideDrafts, String proteinSequence, int indexOnProtein, Double massMin, Double massMax, BoxedObject<Boolean> smallMasses) {

        ArrayList<Peptide> results = new ArrayList<Peptide>();

        for (PeptideDraft peptideDraft : peptideDrafts) {

            BoxedObject<Boolean> smallMass = new BoxedObject<Boolean>(Boolean.FALSE);
            setCterm(peptideDraft, proteinSequence, indexOnProtein);
            Peptide peptide = peptideDraft.getPeptide(massMin, massMax, smallMass);

            if (peptide != null) {
                results.add(peptide);
            }
            if (smallMass.getObject()) {
                smallMasses.setObject(Boolean.TRUE);
            }
        }

        return results;
    }
}
