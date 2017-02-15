package com.compomics.util.experiment.identification.protein_sequences.digestion.iterators;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideDraft;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideWithPosition;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Iterator for enzymatic digestion.
 *
 * @author Marc Vaudel
 */
public class EnzymaticIterator {

    /**
     * Utilities classes for the digestion.
     */
    private ProteinIteratorUtils proteinIteratorUtils;
    
    /**
     * Constructor.
     * 
     * @param proteinIteratorUtils utils for the creation of the peptides
     */
    public EnzymaticIterator(ProteinIteratorUtils proteinIteratorUtils) {
        this.proteinIteratorUtils = proteinIteratorUtils;
    }

    /**
     * Returns the possible peptides for the given protein sequence after
     * enzymatic digestion. Peptides are filtered according to the given masses.
     * Filters are ignored if null.
     *
     * @param proteinSequence the protein sequence
     * @param
     * 
     * 
     * digestionPreferences the digestion preferences
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return the possible peptides
     */
    public ArrayList<PeptideWithPosition> getPeptidesDigestion(String proteinSequence, DigestionPreferences digestionPreferences, Double massMin, Double massMax) {

        Double massMinWater = massMin;
        if (massMinWater != null) {
            massMinWater -= ProteinIteratorUtils.WATER_MASS;
        }
        Double massMaxWater = massMax;
        if (massMaxWater != null) {
            massMaxWater -= ProteinIteratorUtils.WATER_MASS;
        }

        HashMap<Integer, ArrayList<PeptideDraft>> peptides = new HashMap<Integer, ArrayList<PeptideDraft>>();
        ArrayList<PeptideDraft> originalSequence = new ArrayList<PeptideDraft>(1);
        PeptideDraft protein = new PeptideDraft(proteinSequence);
        originalSequence.add(protein);
        peptides.put(0, originalSequence);

        if (digestionPreferences.getCleavagePreference() == DigestionPreferences.CleavagePreference.enzyme) {
            for (Enzyme enzyme : digestionPreferences.getEnzymes()) {

                String enzymeName = enzyme.getName();
                Integer nMissedCleavages = digestionPreferences.getnMissedCleavages(enzymeName);
                HashMap<Integer, ArrayList<PeptideDraft>> newPeptides = new HashMap<Integer, ArrayList<PeptideDraft>>(peptides.size());

                for (Integer peptideStart : peptides.keySet()) {

                    ArrayList<PeptideDraft> peptidesAtPosition = peptides.get(peptideStart);

                    for (PeptideDraft peptide : peptidesAtPosition) {

                        HashMap<Integer, ArrayList<PeptideDraft>> subPeptides = digest(peptide.getSequence().toString(), proteinSequence, peptideStart, enzyme, nMissedCleavages, massMinWater, massMaxWater);

                        for (Integer tempPeptideStart : subPeptides.keySet()) {

                            ArrayList<PeptideDraft> tempPeptides = subPeptides.get(tempPeptideStart);
                            Integer newPeptideStart = tempPeptideStart + peptideStart;
                            ArrayList<PeptideDraft> newPeptidesAtI = newPeptides.get(newPeptideStart);

                            if (newPeptidesAtI == null) {
                                newPeptides.put(newPeptideStart, tempPeptides);
                            } else {
                                newPeptidesAtI.addAll(tempPeptides);
                            }
                        }
                    }
                }

                DigestionPreferences.Specificity specificity = digestionPreferences.getSpecificity(enzymeName);

                if (specificity != DigestionPreferences.Specificity.specific) {

                    HashMap<Integer, ArrayList<PeptideDraft>> semiSpecificPeptides = new HashMap<Integer, ArrayList<PeptideDraft>>(newPeptides.size());
                    for (Integer peptideStart : newPeptides.keySet()) {

                        ArrayList<PeptideDraft> peptidesAtPosition = newPeptides.get(peptideStart);

                        for (PeptideDraft peptideDraft : peptidesAtPosition) {

                            HashMap<Integer, ArrayList<PeptideDraft>> semiSpecificPeptidesMap = getNonSpecificPeptides(peptideDraft, proteinSequence, peptideStart, specificity, massMin, massMax);

                            for (Integer positionOnPeptide : semiSpecificPeptidesMap.keySet()) {

                                ArrayList<PeptideDraft> semiSpecificPeptidesOnPeptide = semiSpecificPeptidesMap.get(positionOnPeptide);
                                Integer positionOnProtein = peptideStart + positionOnPeptide;
                                ArrayList<PeptideDraft> semiSpecificPeptidesOnProtein = semiSpecificPeptides.get(positionOnProtein);

                                if (semiSpecificPeptidesOnProtein == null) {
                                    semiSpecificPeptides.put(positionOnProtein, semiSpecificPeptidesOnPeptide);
                                } else {
                                    semiSpecificPeptidesOnProtein.addAll(semiSpecificPeptidesOnPeptide);
                                }
                            }
                        }
                    }

                    newPeptides = semiSpecificPeptides;
                }

                peptides = newPeptides;
            }
        }

        ArrayList<PeptideWithPosition> result = new ArrayList<PeptideWithPosition>(peptides.size());

        for (Integer peptideStart : peptides.keySet()) {
            ArrayList<PeptideDraft> peptidesAtI = peptides.get(peptideStart);
            for (PeptideDraft peptideDraft : peptidesAtI) {
                Peptide peptide = peptideDraft.getPeptide(massMin, massMax);
                result.add(new PeptideWithPosition(peptide, peptideStart));
            }
        }

        return result;
    }

    /**
     * Returns a map of possible non specific peptides for the given peptide
     * draft. the possible peptides are returned in a map indexed by start index
     * on the peptide sequence.
     *
     * @param peptideDraft the peptide draft
     * @param proteinSequence the protein sequence
     * @param indexOnProtein the index on protein
     * @param specificity the specificity
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return a map of possible non specific peptides for the given peptide
     * draft
     */
    private HashMap<Integer, ArrayList<PeptideDraft>> getNonSpecificPeptides(PeptideDraft peptideDraft, String proteinSequence,
            int indexOnProtein, DigestionPreferences.Specificity specificity, Double massMin, Double massMax) {

        switch (specificity) {

            case specificNTermOnly:

                ArrayList<PeptideDraft> newPeptides = new ArrayList<PeptideDraft>(peptideDraft.length() / 2);
                String nTermModidification = peptideDraft.getnTermModification();
                HashMap<Integer, String> peptideModifications = peptideDraft.getFixedAaModifications();
                double newMass = proteinIteratorUtils.getModificationMass(nTermModidification);
                String sequence = peptideDraft.getSequence().toString();
                StringBuilder newSequence = new StringBuilder(sequence.length());
                HashMap<Integer, String> newModifications = new HashMap<Integer, String>(peptideModifications.size());

                for (int i = 0; i < sequence.length(); i++) {

                    char aa = sequence.charAt(i);
                    AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                    newSequence.append(aa);
                    newMass += aminoAcid.getMonoisotopicMass();
                    String modification = newModifications.get(i);

                    if (modification != null) {
                        newMass += proteinIteratorUtils.getModificationMass(modification);
                        newModifications.put(i, modification);
                    }

                    String cTermModidification = proteinIteratorUtils.getCtermModification(peptideDraft, proteinSequence, indexOnProtein);
                    Double peptideMass = newMass;
                    peptideMass += proteinIteratorUtils.getModificationMass(cTermModidification);
                    Double tempMass = peptideMass + ProteinIteratorUtils.WATER_MASS;

                    if ((massMin == null || tempMass >= massMin)
                            && (massMax == null || tempMass <= massMax)) {
                        PeptideDraft newPeptide = new PeptideDraft(new StringBuilder(newSequence), nTermModidification, cTermModidification, new HashMap<Integer, String>(newModifications), newMass);
                        newPeptides.add(newPeptide);
                    }
                }

                HashMap<Integer, ArrayList<PeptideDraft>> result = new HashMap<Integer, ArrayList<PeptideDraft>>(1);
                result.put(0, newPeptides);
                return result;

            case specificCTermOnly:

                String cTermModidification = peptideDraft.getcTermModification();
                peptideModifications = peptideDraft.getFixedAaModifications();
                newMass = proteinIteratorUtils.getModificationMass(cTermModidification);
                sequence = peptideDraft.getSequence().toString();
                newSequence = new StringBuilder(sequence.length());
                newModifications = new HashMap<Integer, String>(peptideModifications.size());
                result = new HashMap<Integer, ArrayList<PeptideDraft>>(sequence.length());

                for (int i = sequence.length() - 1; i >= 0; i--) {
                    char aa = sequence.charAt(i);
                    AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                    newSequence.insert(0, aa);
                    newMass += aminoAcid.getMonoisotopicMass();
                    String modification = newModifications.get(i);

                    if (modification != null) {
                        newMass += proteinIteratorUtils.getModificationMass(modification);
                        newModifications.put(i, modification);
                    }

                    nTermModidification = proteinIteratorUtils.getNtermModification(indexOnProtein + i == 0, aa, proteinSequence);
                    Double peptideMass = newMass;
                    peptideMass += proteinIteratorUtils.getModificationMass(nTermModidification);
                    Double tempMass = peptideMass + ProteinIteratorUtils.WATER_MASS;

                    if ((massMin == null || tempMass >= massMin)
                            && (massMax == null || tempMass <= massMax)) {
                        newPeptides = new ArrayList<PeptideDraft>(1);
                        PeptideDraft newPeptide = new PeptideDraft(new StringBuilder(newSequence), nTermModidification, cTermModidification, new HashMap<Integer, String>(newModifications), newMass);
                        newPeptides.add(newPeptide);
                        result.put(i, newPeptides);
                    }
                }
                return result;

            case semiSpecific:

                HashMap<Integer, ArrayList<PeptideDraft>> nTermResults = getNonSpecificPeptides(peptideDraft, proteinSequence, indexOnProtein, DigestionPreferences.Specificity.specificNTermOnly, massMin, massMax);
                HashMap<Integer, ArrayList<PeptideDraft>> results = getNonSpecificPeptides(peptideDraft, proteinSequence, indexOnProtein, DigestionPreferences.Specificity.specificCTermOnly, massMin, massMax);
                results.put(0, nTermResults.get(0));
                return results;

            default:
                throw new UnsupportedOperationException("Non specific digestion not implemented for specificity " + specificity + ".");
        }
    }

    /**
     * Digests the given sequence and returns the possible peptide drafts
     * indexed by their starting index on the sequence.
     *
     * @param sequence the sequence to digest
     * @param proteinSequence the protein sequence
     * @param indexOnProtein the index of the sequence on the protein
     * @param enzyme the enzyme to use
     * @param maxMissedCleavages the maximal number of missed cleavages
     * @param massMin the minimal mass allowed with water removed
     * @param massMax the maximal mass allowed with water removed
     *
     * @return the possible peptide drafts
     */
    private HashMap<Integer, ArrayList<PeptideDraft>> digest(String sequence, String proteinSequence, Integer indexOnProtein, Enzyme enzyme, int maxMissedCleavages, Double massMinWater, Double massMaxWater) {

        char aa = sequence.charAt(0);
        ArrayList<PeptideDraft> tempPeptides = new ArrayList<PeptideDraft>();
        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);

        for (char subAa : aminoAcid.getSubAminoAcids()) {
            StringBuilder currentPeptide = new StringBuilder(10);
            currentPeptide.append(subAa);
            Double currentMass = AminoAcid.getAminoAcid(subAa).getMonoisotopicMass();
            String nTermModification = proteinIteratorUtils.getNtermModification(indexOnProtein == 0, subAa, proteinSequence);
            currentMass += proteinIteratorUtils.getModificationMass(nTermModification);
            HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
            String modificationAtAa = proteinIteratorUtils.getFixedModificationAtAa(subAa);

            if (modificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = proteinIteratorUtils.getModificationPattern(modificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein)) {
                    peptideModifications.put(1, modificationAtAa);
                    currentMass += proteinIteratorUtils.getModificationMass(modificationAtAa);
                }
            }

            PeptideDraft peptideDraft = new PeptideDraft(currentPeptide, nTermModification, peptideModifications, currentMass);
            if (aa == 'X') {
                peptideDraft.increaseNX();
            }
            tempPeptides.add(peptideDraft);
        }

        HashMap<Integer, ArrayList<PeptideDraft>> result = new HashMap<Integer, ArrayList<PeptideDraft>>();

        for (int i = 1; i < sequence.length(); i++) {

            aa = sequence.charAt(i);
            aminoAcid = AminoAcid.getAminoAcid(aa);

            ArrayList<PeptideDraft> newPeptides = new ArrayList<PeptideDraft>(tempPeptides.size());

            int combinationCount = 1;
            for (char aaAfter : aminoAcid.getSubAminoAcids()) {

                for (PeptideDraft peptideDraft : tempPeptides) {

                    if ((aa != 'X' || peptideDraft.getnX() < proteinIteratorUtils.getMaxXsInSequence())
                            && massMaxWater == null || peptideDraft.getMass() < massMaxWater + proteinIteratorUtils.getMinCtermMass()) {
                        if (aa == 'X') {
                            peptideDraft.increaseNX();
                        }
                        char aaBefore = peptideDraft.getSequence().charAt(peptideDraft.length() - 1);

                        if (enzyme.isCleavageSiteNoCombination(aaBefore, aaAfter)) {

                            Double peptideMass = peptideDraft.getMass();
                            String cTermModification = proteinIteratorUtils.getCtermModification(peptideDraft, proteinSequence, i - 1 + indexOnProtein);
                            peptideMass += proteinIteratorUtils.getModificationMass(cTermModification);

                            if ((massMinWater == null || peptideMass >= massMinWater)
                                    && (massMaxWater == null || peptideMass <= massMaxWater)) {

                                PeptideDraft peptideDraftWithCTerm = peptideDraft.clone();
                                peptideDraftWithCTerm.setcTermModification(cTermModification);
                                peptideDraftWithCTerm.setMass(peptideMass);
                                Integer startIndex = i - peptideDraftWithCTerm.length();
                                ArrayList<PeptideDraft> peptidesAtI = result.get(startIndex);

                                if (peptidesAtI == null) {
                                    peptidesAtI = new ArrayList<PeptideDraft>(maxMissedCleavages + 1);
                                    result.put(startIndex, peptidesAtI);
                                }

                                peptidesAtI.add(peptideDraftWithCTerm);
                            }

                            peptideDraft.increaseMissedCleavages();
                        }

                        if (peptideDraft.getMissedCleavages() <= maxMissedCleavages) {

                            if (combinationCount < aminoAcid.getSubAminoAcids().length) {

                                StringBuilder newSequence = new StringBuilder(peptideDraft.getSequence());
                                newSequence.append(aaAfter);
                                Double mass = peptideDraft.getMass();
                                mass += AminoAcid.getAminoAcid(aaAfter).getMonoisotopicMass();
                                HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(peptideDraft.getFixedAaModifications());
                                String modificationAtAa = proteinIteratorUtils.getFixedModificationAtAa(aaAfter);

                                if (modificationAtAa != null) {

                                    AminoAcidPattern aminoAcidPattern = proteinIteratorUtils.getModificationPattern(modificationAtAa);

                                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein)) {
                                        peptideModifications.put(i + 1, modificationAtAa);
                                        mass += proteinIteratorUtils.getModificationMass(modificationAtAa);
                                    }
                                }

                                if (massMaxWater == null || mass + proteinIteratorUtils.getMinCtermMass() <= massMaxWater) {
                                    PeptideDraft newPeptideDraft = new PeptideDraft(newSequence, peptideDraft.getnTermModification(), peptideModifications, mass, peptideDraft.getMissedCleavages());
                                    newPeptides.add(newPeptideDraft);
                                }

                            } else {

                                peptideDraft.getSequence().append(aaAfter);
                                Double newMass = peptideDraft.getMass() + AminoAcid.getAminoAcid(aaAfter).getMonoisotopicMass();
                                String modificationAtAa = proteinIteratorUtils.getFixedModificationAtAa(aaAfter);

                                if (modificationAtAa != null) {

                                    AminoAcidPattern aminoAcidPattern = proteinIteratorUtils.getModificationPattern(modificationAtAa);

                                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein)) {
                                        HashMap<Integer, String> peptideModifications = peptideDraft.getFixedAaModifications();
                                        peptideModifications.put(i + 1, modificationAtAa);
                                        newMass += proteinIteratorUtils.getModificationMass(modificationAtAa);
                                    }
                                }

                                if (massMaxWater == null || newMass + proteinIteratorUtils.getMinCtermMass() <= massMaxWater) {
                                    peptideDraft.setMass(newMass);
                                    newPeptides.add(peptideDraft);
                                }
                            }
                        }
                    }
                }

                boolean cleavageSite = false;
                char aa0 = sequence.charAt(i - 1);
                AminoAcid aminoAcid0 = AminoAcid.getAminoAcid(aa0);
                for (char aaBefore : aminoAcid0.getSubAminoAcids()) {
                    if (enzyme.isCleavageSite(aaBefore, aaAfter)) {
                        cleavageSite = true;
                        break;
                    }
                }

                if (cleavageSite) {

                    StringBuilder currentPeptide = new StringBuilder(10);
                    currentPeptide.append(aaAfter);
                    Double currentMass = AminoAcid.getAminoAcid(aaAfter).getMonoisotopicMass();
                    String nTermModification = proteinIteratorUtils.getNtermModification(indexOnProtein + i == 0, aaAfter, proteinSequence);
                    currentMass += proteinIteratorUtils.getModificationMass(nTermModification);
                    HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
                    String modificationAtAa = proteinIteratorUtils.getFixedModificationAtAa(aaAfter);

                    if (modificationAtAa != null) {

                        AminoAcidPattern aminoAcidPattern = proteinIteratorUtils.getModificationPattern(modificationAtAa);

                        if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein)) {
                            peptideModifications.put(i + 1, modificationAtAa);
                            currentMass += proteinIteratorUtils.getModificationMass(modificationAtAa);
                        }
                    }

                    PeptideDraft newPeptideDraft = new PeptideDraft(currentPeptide, nTermModification, peptideModifications, currentMass);
                    newPeptides.add(newPeptideDraft);
                }

                combinationCount++;
            }

            tempPeptides = newPeptides;
        }

        for (PeptideDraft peptideDraft : tempPeptides) {

            Double peptideMass = peptideDraft.getMass();
            String cTermModification = proteinIteratorUtils.getCtermModification(peptideDraft, proteinSequence, sequence.length() - 1 + indexOnProtein);
            peptideMass += proteinIteratorUtils.getModificationMass(cTermModification);

            if ((massMinWater == null || peptideMass >= massMinWater)
                    && (massMaxWater == null || peptideMass <= massMaxWater)) {

                peptideDraft.setcTermModification(cTermModification);
                peptideDraft.setMass(peptideMass);
                Integer startIndex = sequence.length() - peptideDraft.length();
                ArrayList<PeptideDraft> peptidesAtI = result.get(startIndex);

                if (peptidesAtI == null) {
                    peptidesAtI = new ArrayList<PeptideDraft>(maxMissedCleavages + 1);
                    result.put(startIndex, peptidesAtI);
                }

                peptidesAtI.add(peptideDraft);
            }
        }

        return result;
    }
}
