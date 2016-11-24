package com.compomics.util.experiment.identification.protein_sequences;

import antlr.StringUtils;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.general.EncapsulatedObject;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The iterator goes through a sequence and lists possible peptides with their
 * fixed modifications.
 *
 * @author Marc Vaudel
 */
public class ProteinSequenceIterator {

    /**
     * The maximal number of Xs allowed to derive peptide sequences. When
     * allowing multiple Xs all possibe combinations will be generated.
     */
    private int maxXsInSequence = 2;
    /**
     * The fixed protein N-term modification.
     */
    private String fixedProteinNtermModification = null;
    /**
     * The fixed protein C-term modification.
     */
    private String fixedProteinCtermModification = null;
    /**
     * The fixed protein N-term modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedProteinNtermModificationsAtAa = new HashMap<Character, String>(0);
    /**
     * The fixed protein C-term modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedProteinCtermModificationsAtAa = new HashMap<Character, String>(0);
    /**
     * The fixed peptide N-term modification.
     */
    private String fixedPeptideNtermModification = null;
    /**
     * The fixed peptide C-term modification.
     */
    private String fixedPeptideCtermModification = null;
    /**
     * The fixed peptide N-term modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedPeptideNtermModificationsAtAa = new HashMap<Character, String>(0);
    /**
     * The fixed peptide C-term modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedPeptideCtermModificationsAtAa = new HashMap<Character, String>(0);
    /**
     * The fixed modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedModificationsAtAa = new HashMap<Character, String>(0);
    /**
     * Map of modifications at specific amino acids (termini or not) targeting a
     * pattern of amino acids.
     */
    private HashMap<String, AminoAcidPattern> modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
    /**
     * Convenience map of the amino acid masses.
     */
    private HashMap<String, Double> modificationsMasses;
    /**
     * The minimal mass a c-terminus modification can have. 0.0 by default for
     * no modification.
     */
    private double minCtermMass = 0.0;
    /**
     * The mass of water (H2O).
     */
    private static final double WATER_MASS = (2 * Atom.H.getMonoisotopicMass()) + Atom.O.getMonoisotopicMass();

    /**
     * Constructor.
     *
     * @param fixedModifications a list of fixed modifications to consider when
     * iterating the protein sequences.
     * @param maxX The maximal number of Xs allowed in a sequence to derive the
     * possible peptides
     */
    public ProteinSequenceIterator(ArrayList<String> fixedModifications, Integer maxX) {
        fillPtmMaps(fixedModifications);
        if (maxX != null) {
            maxXsInSequence = maxX;
        }
    }

    /**
     * Constructor with 2 Xs allowed.
     *
     * @param fixedModifications a list of fixed modifications to consider when
     * iterating the protein sequences.
     */
    public ProteinSequenceIterator(ArrayList<String> fixedModifications) {
        this(fixedModifications, null);
    }

    /**
     * Returns the peptides for a given sequence. The peptides can be filtered
     * by minimal and maximal mass. If null is provided as limit no filter will
     * be used.
     *
     * @param sequence the sequence to iterate
     * @param digestionPreferences the digestion preferences to use
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     *
     * @return the possible peptides for the sequence
     */
    public ArrayList<Peptide> getPeptides(String sequence, DigestionPreferences digestionPreferences, Double massMin, Double massMax) {
        switch (digestionPreferences.getCleavagePreference()) {
            case unSpecific:
                return getPeptides(sequence, massMin, massMax);
            case wholeProtein:
                return getPeptidesNoDigestion(sequence, massMin, massMax);
            case enzyme:
                return getPeptidesDigestion(sequence, digestionPreferences, massMin, massMax);
            default:
                throw new UnsupportedOperationException("Cleavage preference of type " + digestionPreferences.getCleavagePreference() + " not supported.");
        }
    }

    /**
     * Fills the fixed modification attributes of the class based on the given
     * list of modifications.
     *
     * @param fixedModifications the list of fixed modifications to consider.
     */
    private void fillPtmMaps(ArrayList<String> fixedModifications) {
        modificationsMasses = new HashMap<String, Double>(fixedModifications.size());
        modificationsMasses.put(null, 0.0);
        PTMFactory ptmFactory = PTMFactory.getInstance();
        for (String ptmName : fixedModifications) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            switch (ptm.getType()) {
                case PTM.MODN:
                    if (fixedProteinNtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the protein N-terminus.");
                    }
                    fixedProteinNtermModification = ptmName;
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
                case PTM.MODC:
                    if (fixedProteinCtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the protein C-terminus.");
                    }
                    fixedProteinCtermModification = ptmName;
                    Double ptmMass = ptm.getMass();
                    modificationsMasses.put(ptmName, ptmMass);
                    if (ptmMass < minCtermMass) {
                        minCtermMass = ptmMass;
                    }
                    break;
                case PTM.MODNP:
                    if (fixedPeptideNtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the peptide N-terminus.");
                    }
                    fixedPeptideNtermModification = ptmName;
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
                case PTM.MODCP:
                    if (fixedPeptideCtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the peptide C-terminus.");
                    }
                    fixedPeptideCtermModification = ptmName;
                    ptmMass = ptm.getMass();
                    modificationsMasses.put(ptmName, ptmMass);
                    if (ptmMass < minCtermMass) {
                        minCtermMass = ptmMass;
                    }
                    break;
                case PTM.MODNAA:
                    AminoAcidPattern ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedProteinNtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per protein N-term amino acid. Found two at " + aa + ".");
                        }
                        fixedProteinNtermModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
                case PTM.MODCAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedProteinCtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per protein C-term amino acid. Found two at " + aa + ".");
                        }
                        fixedProteinCtermModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    ptmMass = ptm.getMass();
                    modificationsMasses.put(ptmName, ptmMass);
                    if (ptmMass < minCtermMass) {
                        minCtermMass = ptmMass;
                    }
                    break;
                case PTM.MODNPAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedPeptideNtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per peptide N-term amino acid. Found two at " + aa + ".");
                        }
                        fixedPeptideNtermModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
                case PTM.MODCPAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedPeptideCtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per peptide N-term amino acid. Found two at " + aa + ".");
                        }
                        fixedPeptideCtermModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    ptmMass = ptm.getMass();
                    modificationsMasses.put(ptmName, ptmMass);
                    if (ptmMass < minCtermMass) {
                        minCtermMass = ptmMass;
                    }
                    break;
                case PTM.MODAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per amino acid. Found two at " + aa + ".");
                        }
                        fixedModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
            }
        }
    }

    /**
     * Returns the N-term modification for the given amino acid. Null if no
     * modification is found.
     *
     * @param proteinNTerm boolean indicating whether the amino acid is at the
     * protein N-terminus
     * @param nTermAaChar the amino acid as character
     * @param proteinSequence the protein sequence
     *
     * @return the N-term modification for the given amino acid.
     */
    private String getNtermModification(boolean proteinNTerm, char nTermAaChar, String proteinSequence) {
        if (proteinNTerm) {
            if (fixedProteinNtermModification != null) {
                return fixedProteinNtermModification;
            }
            String fixedProteinNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(nTermAaChar);
            if (fixedProteinNtermModificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinNtermModificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                    return fixedProteinNtermModificationAtAa;
                }
            }
        }
        if (fixedPeptideNtermModification != null) {
            return fixedPeptideNtermModification;
        }
        String fixedPeptideNtermModificationAtAa = fixedPeptideNtermModificationsAtAa.get(nTermAaChar);
        if (fixedPeptideNtermModificationAtAa != null) {
            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideNtermModificationAtAa);
            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                return fixedPeptideNtermModificationAtAa;
            }
        }
        return null;
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
    public ArrayList<Peptide> getPeptides(String proteinSequence, Double massMin, Double massMax) {

        if (AminoAcidSequence.hasCombination(proteinSequence)) {
            return getPeptidesAaCombinations(proteinSequence, massMin, massMax);
        }

        ArrayList<Peptide> result = new ArrayList<Peptide>();
        char[] sequenceAsCharArray = proteinSequence.toCharArray();

        for (int i = 0; i < sequenceAsCharArray.length; i++) {

            Double sequenceMass = 0.0;
            char nTermAaChar = sequenceAsCharArray[i];
            StringBuilder peptideSequence = new StringBuilder(sequenceAsCharArray.length - i);
            HashMap<Integer, String> fixedModifications = new HashMap<Integer, String>(1);
            String nTermModification = getNtermModification(i == 0, nTermAaChar, proteinSequence);
            sequenceMass += modificationsMasses.get(nTermModification);

            for (int j = i; j < sequenceAsCharArray.length; j++) {

                Integer aaIndex = j - i;
                Character aaChar = sequenceAsCharArray[j];
                AminoAcid aminoAcid = AminoAcid.getAminoAcid(aaChar);
                sequenceMass += aminoAcid.getMonoisotopicMass();
                peptideSequence.append(aaChar);
                String modificationAtAa = fixedModificationsAtAa.get(aaChar);

                if (modificationAtAa != null) {
                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, j)) {
                        fixedModifications.put(aaIndex + 1, modificationAtAa);
                        sequenceMass += modificationsMasses.get(modificationAtAa);
                    }
                }

                PeptideDraft peptideDraft = new PeptideDraft(peptideSequence, nTermModification, fixedModifications, sequenceMass);
                EncapsulatedObject<Boolean> smallMass = new EncapsulatedObject<Boolean>(Boolean.FALSE);
                setCterm(peptideDraft, proteinSequence, j);
                Peptide peptide = getPeptide(peptideDraft, massMin, massMax, smallMass);

                if (!smallMass.getObject()) {
                    break;
                }
                if (peptide != null) {
                    result.add(peptide);
                }
            }
        }

        return result;
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
    private ArrayList<Peptide> getPeptidesAaCombinations(String sequence, Double massMin, Double massMax) {

        ArrayList<Peptide> result = new ArrayList<Peptide>();
        char[] sequenceAsCharArray = sequence.toCharArray();

        for (int i = 0; i < sequenceAsCharArray.length; i++) {

            ArrayList<PeptideDraft> peptideDrafts = new ArrayList<PeptideDraft>(1);
            char sequenceNTermAaChar = sequenceAsCharArray[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(sequenceNTermAaChar);
            EncapsulatedObject<Boolean> smallMasses = new EncapsulatedObject<Boolean>(Boolean.FALSE);

            for (char nTermAaChar : aminoAcid.getSubAminoAcids()) {

                Double sequenceMass = 0.0;
                StringBuilder peptideSequence = new StringBuilder(sequenceAsCharArray.length - i);
                HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
                String nTermModification = getNtermModification(i == 0, nTermAaChar, sequence);
                sequenceMass += modificationsMasses.get(nTermModification);
                AminoAcid subAminoAcid = AminoAcid.getAminoAcid(nTermAaChar);
                sequenceMass += subAminoAcid.getMonoisotopicMass();
                peptideSequence.append(nTermAaChar);
                String modificationAtAa = fixedModificationsAtAa.get(nTermAaChar);

                if (modificationAtAa != null) {

                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);

                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                        peptideModifications.put(1, modificationAtAa);
                        sequenceMass += modificationsMasses.get(modificationAtAa);
                    }
                }

                PeptideDraft simplePeptide = new PeptideDraft(peptideSequence, nTermModification, peptideModifications, sequenceMass);
                peptideDrafts.add(simplePeptide);
            }

            ArrayList<Peptide> peptidesAtIndex = getPeptides(peptideDrafts, sequence, i, massMin, massMax, smallMasses);
            result.addAll(peptidesAtIndex);

            if (!smallMasses.getObject()) {
                return result;
            }

            for (int j = i + 1; j < sequenceAsCharArray.length; j++) {

                int aaIndex = j - i;
                ArrayList<PeptideDraft> newPeptideDrafts = new ArrayList<PeptideDraft>(peptideDrafts.size());
                char sequenceChar = sequenceAsCharArray[j];
                AminoAcid sequenceAminoAcid = AminoAcid.getAminoAcid(sequenceChar);

                for (PeptideDraft peptideDraft : peptideDrafts) {

                    if (sequenceChar != 'X' || peptideDraft.getnX() < maxXsInSequence) {
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
                            String modificationAtAa = fixedModificationsAtAa.get(aaChar);

                            if (modificationAtAa != null) {

                                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);

                                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, j)) {
                                    peptideModifications.put(aaIndex + 1, modificationAtAa);
                                    peptideMass += modificationsMasses.get(modificationAtAa);
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
                result.addAll(peptidesAtIndex);

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
    private ArrayList<Peptide> getPeptides(ArrayList<PeptideDraft> peptideDrafts, String proteinSequence, int indexOnProtein, Double massMin, Double massMax, EncapsulatedObject<Boolean> smallMasses) {

        ArrayList<Peptide> results = new ArrayList<Peptide>();

        for (PeptideDraft peptideDraft : peptideDrafts) {

            EncapsulatedObject<Boolean> smallMass = new EncapsulatedObject<Boolean>(Boolean.FALSE);
            setCterm(peptideDraft, proteinSequence, indexOnProtein);
            Peptide peptide = getPeptide(peptideDraft, massMin, massMax, smallMass);

            if (peptide != null) {
                results.add(peptide);
            }
            if (smallMass.getObject()) {
                smallMasses.setObject(Boolean.TRUE);
            }
        }

        return results;
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

        String cTermModification = getCtermModification(peptideDraft, proteinSequence, indexOnProtein);

        if (cTermModification != null) {
            Double modificationMass = modificationsMasses.get(cTermModification);
            Double peptideMass = peptideDraft.getMass() + modificationMass;
            peptideDraft.setMass(peptideMass);
            peptideDraft.setcTermModification(cTermModification);
        }
    }

    /**
     * Returns the c-terminal modification for the given peptide draft.
     *
     * @param peptideDraft the peptide draft of interest
     * @param proteinSequence the protein sequence
     * @param indexOnProtein the index of the peptide draft on the protein
     *
     * @return the c-terminal modification for the given peptide draft
     */
    private String getCtermModification(PeptideDraft peptideDraft, String proteinSequence, int indexOnProtein) {

        StringBuilder peptideSequence = peptideDraft.getSequence();
        char aaChar = peptideSequence.charAt(peptideSequence.length() - 1);

        if (indexOnProtein == proteinSequence.length() - peptideDraft.length()) {

            if (fixedProteinCtermModification != null) {
                return fixedProteinCtermModification;
            }

            String fixedProteinCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinCtermModificationAtAa);

            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, proteinSequence.length() - 1)) {
                return fixedProteinCtermModification;
            }
        }

        if (fixedPeptideCtermModification != null) {
            return fixedPeptideCtermModification;
        }

        String fixedPeptideCtermModificationAtAa = fixedPeptideCtermModificationsAtAa.get(aaChar);

        if (fixedPeptideCtermModificationAtAa != null) {

            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideCtermModificationAtAa);

            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein + peptideDraft.length())) {
                return fixedPeptideCtermModificationAtAa;
            }
        }

        return null;
    }

    /**
     * Returns a peptide from the given peptide draft.
     *
     * @param peptideDraft the peptide draft
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return the peptide built from the peptide draft
     */
    private Peptide getPeptide(PeptideDraft peptideDraft, Double massMin, Double massMax) {
        return getPeptide(peptideDraft, massMin, massMax, new EncapsulatedObject<Boolean>(Boolean.FALSE));
    }

    /**
     * Returns a peptide from the given peptide draft.
     *
     * @param peptideDraft the peptide draft
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     * @param smallMass an encapsulated boolean indicating whether the peptide
     * passed the maximal mass filter
     *
     * @return the peptide built from the peptide draft
     */
    private Peptide getPeptide(PeptideDraft peptideDraft, Double massMin, Double massMax, EncapsulatedObject<Boolean> smallMass) {

        Double peptideMass = peptideDraft.getMass();
        Double tempMass = peptideMass + WATER_MASS;

        if (massMax == null || tempMass <= massMax) {

            smallMass.setObject(Boolean.TRUE);

            if (massMin == null || tempMass >= massMin) {

                ArrayList<ModificationMatch> modificationMatches = null;
                String nTermModification = peptideDraft.getnTermModification();
                String cTermModification = peptideDraft.getcTermModification();
                HashMap<Integer, String> peptideModifications = peptideDraft.getFixedAaModifications();

                if (nTermModification != null) {
                    modificationMatches = new ArrayList<ModificationMatch>(peptideModifications.size());
                    modificationMatches.add(new ModificationMatch(nTermModification, false, 1));
                }

                if (cTermModification != null) {

                    if (modificationMatches == null) {
                        modificationMatches = new ArrayList<ModificationMatch>(peptideModifications.size());
                    }

                    modificationMatches.add(new ModificationMatch(cTermModification, false, peptideDraft.length()));
                }

                for (Integer site : peptideModifications.keySet()) {

                    if (modificationMatches == null) {
                        modificationMatches = new ArrayList<ModificationMatch>(peptideModifications.size());
                    }

                    String modificationName = peptideModifications.get(site);
                    modificationMatches.add(new ModificationMatch(modificationName, false, site));
                }

                Peptide peptide = new Peptide(peptideDraft.getSequence().toString(), modificationMatches, false);
                return peptide;
            }
        }

        return null;
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
    public ArrayList<Peptide> getPeptidesNoDigestion(String sequence, Double massMin, Double massMax) {

        if (AminoAcidSequence.containsAmbiguousAminoAcid(sequence)) {

            int nX = 0;
            for (int i = 0; i < sequence.length(); i++) {
                if (sequence.charAt(i) == 'X') {
                    nX++;
                    if (nX > maxXsInSequence) {
                        return new ArrayList<Peptide>(0);
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

            ArrayList<Peptide> result = new ArrayList<Peptide>(sequences.size());

            for (StringBuilder peptideSequence : sequences) {

                String sequenceAsString = peptideSequence.toString();
                Peptide peptide = getPeptideNoDigestion(sequenceAsString, sequenceAsString, 0, massMin, massMax);

                if (peptide != null) {
                    result.add(peptide);
                }
            }

            return result;
        } else {

            ArrayList<Peptide> result = new ArrayList<Peptide>(1);
            Peptide peptide = getPeptideNoDigestion(sequence, sequence, 0, massMin, massMax);

            if (peptide != null
                    && (massMin == null || peptide.getMass() >= massMin)
                    && (massMax == null || peptide.getMass() <= massMax)) {
                result.add(peptide);
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
     * @param indexOnProtein the index of the peptide on the protein
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return a peptide from the given sequence
     */
    public Peptide getPeptideNoDigestion(String peptideSequence, String proteinSequence, int indexOnProtein, Double massMin, Double massMax) {

        char nTermAaChar = peptideSequence.charAt(0);
        String nTermModification = getNtermModification(indexOnProtein == 0, nTermAaChar, proteinSequence);
        HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
        Double peptideMass = modificationsMasses.get(nTermModification);

        for (int i = 0; i < peptideSequence.length(); i++) {

            char aaChar = peptideSequence.charAt(i);
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aaChar);
            peptideMass += aminoAcid.getMonoisotopicMass();

            if (massMax != null && peptideMass + minCtermMass > massMax) {
                return null;
            }

            String modificationAtAa = fixedModificationsAtAa.get(aaChar);

            if (modificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein + i)) {
                    peptideModifications.put(i + 1, modificationAtAa);
                    peptideMass += modificationsMasses.get(modificationAtAa);
                }
            }
        }

        PeptideDraft peptideDraft = new PeptideDraft(new StringBuilder(peptideSequence), nTermModification, peptideModifications, peptideMass);
        setCterm(peptideDraft, proteinSequence, indexOnProtein);

        return getPeptide(peptideDraft, massMin, massMax);
    }

    /**
     * Returns the possible peptides for the given protein sequence after
     * enzymatic digestion. Peptides are filtered according to the given masses.
     * Filters are ignored if null.
     *
     * @param proteinSequence the protein sequence
     * @param digestionPreferences the digestion preferences
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return the possible peptides
     */
    public ArrayList<Peptide> getPeptidesDigestion(String proteinSequence, DigestionPreferences digestionPreferences, Double massMin, Double massMax) {

        Double massMinWater = massMin;
        if (massMinWater != null) {
            massMinWater -= WATER_MASS;
        }
        Double massMaxWater = massMax;
        if (massMaxWater != null) {
            massMaxWater -= WATER_MASS;
        }

        HashMap<Integer, ArrayList<PeptideDraft>> peptides = new HashMap<Integer, ArrayList<PeptideDraft>>();
        ArrayList<PeptideDraft> originalSequence = new ArrayList<PeptideDraft>(1);
        PeptideDraft protein = new PeptideDraft(proteinSequence);
        originalSequence.add(protein);
        peptides.put(0, originalSequence);

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

        ArrayList<Peptide> result = new ArrayList<Peptide>(peptides.size());

        for (ArrayList<PeptideDraft> peptidesAtI : peptides.values()) {
            for (PeptideDraft peptideDraft : peptidesAtI) {
                Peptide peptide = getPeptide(peptideDraft, massMin, massMax);
                result.add(peptide);
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
                Double newMass = modificationsMasses.get(nTermModidification);
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
                        newMass += modificationsMasses.get(modification);
                        newModifications.put(i, modification);
                    }

                    String cTermModidification = getCtermModification(peptideDraft, proteinSequence, indexOnProtein);
                    Double peptideMass = newMass;
                    peptideMass += modificationsMasses.get(cTermModidification);
                    Double tempMass = peptideMass + WATER_MASS;

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
                newMass = modificationsMasses.get(cTermModidification);
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
                        newMass += modificationsMasses.get(modification);
                        newModifications.put(i, modification);
                    }

                    nTermModidification = getNtermModification(indexOnProtein + i == 0, aa, proteinSequence);
                    Double peptideMass = newMass;
                    peptideMass += modificationsMasses.get(nTermModidification);
                    Double tempMass = peptideMass + WATER_MASS;

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
            String nTermModification = getNtermModification(indexOnProtein == 0, subAa, proteinSequence);
            currentMass += modificationsMasses.get(nTermModification);
            HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
            String modificationAtAa = fixedModificationsAtAa.get(subAa);

            if (modificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein)) {
                    peptideModifications.put(1, modificationAtAa);
                    currentMass += modificationsMasses.get(modificationAtAa);
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

                boolean cleavageSite = false;

                for (PeptideDraft peptideDraft : tempPeptides) {

                    if ((aa != 'X' || peptideDraft.getnX() < maxXsInSequence)
                            && massMaxWater == null || peptideDraft.getMass() < massMaxWater + minCtermMass) {
                        if (aa == 'X') {
                            peptideDraft.increaseNX();
                        }
                        char aaBefore = peptideDraft.getSequence().charAt(peptideDraft.length() - 1);

                        if (enzyme.isCleavageSite(aaBefore, aaAfter)) {

                            Double peptideMass = peptideDraft.getMass();
                            String cTermModification = getCtermModification(peptideDraft, proteinSequence, i - 1 + indexOnProtein);
                            peptideMass += modificationsMasses.get(cTermModification);

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
                            cleavageSite = true;
                        }

                        if (peptideDraft.getMissedCleavages() <= maxMissedCleavages) {

                            if (combinationCount < aminoAcid.getSubAminoAcids().length) {

                                StringBuilder newSequence = new StringBuilder(peptideDraft.getSequence());
                                newSequence.append(aaAfter);
                                Double mass = peptideDraft.getMass();
                                mass += AminoAcid.getAminoAcid(aaAfter).getMonoisotopicMass();
                                HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(peptideDraft.getFixedAaModifications());
                                String modificationAtAa = fixedModificationsAtAa.get(aaAfter);

                                if (modificationAtAa != null) {

                                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);

                                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein)) {
                                        peptideModifications.put(i + 1, modificationAtAa);
                                        mass += modificationsMasses.get(modificationAtAa);
                                    }
                                }

                                if (massMaxWater == null || mass + minCtermMass <= massMaxWater) {
                                    PeptideDraft newPeptideDraft = new PeptideDraft(newSequence, peptideDraft.getnTermModification(), peptideModifications, mass, peptideDraft.getMissedCleavages());
                                    newPeptides.add(newPeptideDraft);
                                }

                            } else {

                                peptideDraft.getSequence().append(aaAfter);
                                Double newMass = peptideDraft.getMass() + AminoAcid.getAminoAcid(aaAfter).getMonoisotopicMass();
                                String modificationAtAa = fixedModificationsAtAa.get(aaAfter);

                                if (modificationAtAa != null) {

                                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);

                                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein)) {
                                        HashMap<Integer, String> peptideModifications = peptideDraft.getFixedAaModifications();
                                        peptideModifications.put(i + 1, modificationAtAa);
                                        newMass += modificationsMasses.get(modificationAtAa);
                                    }
                                }

                                if (massMaxWater == null || newMass + minCtermMass <= massMaxWater) {
                                    peptideDraft.setMass(newMass);
                                    newPeptides.add(peptideDraft);
                                }
                            }
                        }
                    }
                }
                if (cleavageSite) {

                    StringBuilder currentPeptide = new StringBuilder(10);
                    currentPeptide.append(aaAfter);
                    Double currentMass = AminoAcid.getAminoAcid(aaAfter).getMonoisotopicMass();
                    String nTermModification = getNtermModification(indexOnProtein + i == 0, aaAfter, proteinSequence);
                    currentMass += modificationsMasses.get(nTermModification);
                    HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
                    String modificationAtAa = fixedModificationsAtAa.get(aaAfter);

                    if (modificationAtAa != null) {

                        AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);

                        if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein)) {
                            peptideModifications.put(i + 1, modificationAtAa);
                            currentMass += modificationsMasses.get(modificationAtAa);
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
            String cTermModification = getCtermModification(peptideDraft, proteinSequence, sequence.length() - 1 + indexOnProtein);
            peptideMass += modificationsMasses.get(cTermModification);

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

    /**
     * Convenience class for the building of peptides.
     */
    private class PeptideDraft {

        /**
         * The amino acid sequence.
         */
        private StringBuilder sequence;
        /**
         * The N-terminal modification.
         */
        private String nTermModification;
        /**
         * The C-terminal modification.
         */
        private String cTermModification;
        /**
         * The modifications at specific amino acids.
         */
        private HashMap<Integer, String> fixedAaModifications;
        /**
         * The current mass of the peptide draft
         */
        private Double mass;
        /**
         * The number of missed cleavages.
         */
        private int missedCleavages = 0;
        /**
         * The number of Xs already considered in this draft.
         */
        private int nX = 0;

        /**
         * Constructor.
         *
         * @param sequence the peptide sequence.
         */
        public PeptideDraft(String sequence) {
            this.sequence = new StringBuilder(sequence);
        }

        /**
         * Constructor.
         *
         * @param sequence the peptide sequence
         * @param nTermModification the N-term modification
         * @param fixedAaModifications the fixed modifications at amino acids
         * @param mass the mass
         */
        public PeptideDraft(StringBuilder sequence, String nTermModification, HashMap<Integer, String> fixedAaModifications, Double mass) {
            this.sequence = sequence;
            this.nTermModification = nTermModification;
            this.fixedAaModifications = fixedAaModifications;
            this.mass = mass;
        }

        /**
         * Constructor.
         *
         * @param sequence the peptide sequence
         * @param nTermModification the N-term modification
         * @param fixedAaModifications the fixed modifications at amino acids
         * @param mass the mass
         * @param missedCleavages the number of missed cleavages
         */
        public PeptideDraft(StringBuilder sequence, String nTermModification, HashMap<Integer, String> fixedAaModifications, Double mass, int missedCleavages) {
            this.sequence = sequence;
            this.nTermModification = nTermModification;
            this.fixedAaModifications = fixedAaModifications;
            this.mass = mass;
            this.missedCleavages = missedCleavages;
        }

        /**
         * Constructor.
         *
         * @param sequence the peptide sequence
         * @param nTermModification the N-term modification
         * @param cTermModification the C-term modification
         * @param fixedAaModifications the fixed modifications at amino acids
         * @param mass the mass
         */
        public PeptideDraft(StringBuilder sequence, String nTermModification, String cTermModification, HashMap<Integer, String> fixedAaModifications, Double mass) {
            this.sequence = sequence;
            this.nTermModification = nTermModification;
            this.cTermModification = cTermModification;
            this.fixedAaModifications = fixedAaModifications;
            this.mass = mass;
        }

        /**
         * Constructor.
         *
         * @param sequence the peptide sequence
         * @param nTermModification the N-term modification
         * @param cTermModification the C-term modification
         * @param fixedAaModifications the fixed modifications at amino acids
         * @param mass the mass
         * @param missedCleavages the number of missed cleavages
         */
        public PeptideDraft(StringBuilder sequence, String nTermModification, String cTermModification, HashMap<Integer, String> fixedAaModifications, Double mass, int missedCleavages) {
            this.sequence = sequence;
            this.nTermModification = nTermModification;
            this.cTermModification = cTermModification;
            this.fixedAaModifications = fixedAaModifications;
            this.mass = mass;
            this.missedCleavages = missedCleavages;
        }

        /**
         * Creates a new peptide draft with the same attributes as this one.
         *
         * @return a new peptide draft
         */
        public PeptideDraft clone() {
            PeptideDraft newPeptideDraft = new PeptideDraft(new StringBuilder(sequence), nTermModification, cTermModification, new HashMap<Integer, String>(fixedAaModifications), mass, missedCleavages);
            return newPeptideDraft;
        }

        /**
         * Returns the sequence.
         *
         * @return the sequence
         */
        public StringBuilder getSequence() {
            return sequence;
        }

        /**
         * Sets the sequence.
         *
         * @param sequence the sequence
         */
        public void setSequence(StringBuilder sequence) {
            this.sequence = sequence;
        }

        /**
         * Returns the length of the sequence.
         *
         * @return the length of the sequence
         */
        public int length() {
            return sequence.length();
        }

        /**
         * Returns the N-term modification.
         *
         * @return the N-term modification
         */
        public String getnTermModification() {
            return nTermModification;
        }

        /**
         * Sets the N-term modification.
         *
         * @param nTermModification the N-term modification
         */
        public void setnTermModification(String nTermModification) {
            this.nTermModification = nTermModification;
        }

        /**
         * Returns the C-term modification.
         *
         * @return the C-term modification
         */
        public String getcTermModification() {
            return cTermModification;
        }

        /**
         * Sets the C-term modification.
         *
         * @param cTermModification the C-term modification
         */
        public void setcTermModification(String cTermModification) {
            this.cTermModification = cTermModification;
        }

        /**
         * Returns the mass.
         *
         * @return the mass
         */
        public Double getMass() {
            return mass;
        }

        /**
         * Sets the mass.
         *
         * @param mass the mass
         */
        public void setMass(Double mass) {
            this.mass = mass;
        }

        /**
         * Returns the modifications at specific amino acids.
         *
         * @return the modifications at specific amino acids
         */
        public HashMap<Integer, String> getFixedAaModifications() {
            return fixedAaModifications;
        }

        /**
         * Sets the modifications at specific amino acids.
         *
         * @param fixedAaModifications the modifications at specific amino acids
         */
        public void setFixedAaModifications(HashMap<Integer, String> fixedAaModifications) {
            this.fixedAaModifications = fixedAaModifications;
        }

        /**
         * Increases the number of missed cleavages.
         */
        public void increaseMissedCleavages() {
            missedCleavages++;
        }

        /**
         * Returns the number of missed cleavages.
         *
         * @return the number of missed cleavages
         */
        public int getMissedCleavages() {
            return missedCleavages;
        }

        /**
         * Increases the number of Xs already considered in this draft.
         */
        public void increaseNX() {
            nX++;
        }

        /**
         * Returns the number of Xs already considered in this draft.
         *
         * @return the number of Xs
         */
        public int getnX() {
            return nX;
        }

    }
}
