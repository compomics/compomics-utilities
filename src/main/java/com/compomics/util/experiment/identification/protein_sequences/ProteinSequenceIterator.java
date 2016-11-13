package com.compomics.util.experiment.identification.protein_sequences;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
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
 * The iterator goes through a sequence and lists possible peptides.
 *
 * @author Marc Vaudel
 */
public class ProteinSequenceIterator {

    private String fixedProteinNtermModification = null;
    private String fixedProteinCtermModification = null;
    private HashMap<Character, String> fixedProteinNtermModificationsAtAa = new HashMap<Character, String>(0);
    private HashMap<Character, String> fixedProteinCtermModificationsAtAa = new HashMap<Character, String>(0);
    private String fixedPeptideNtermModification = null;
    private String fixedPeptideCtermModification = null;
    private HashMap<Character, String> fixedPeptideNtermModificationsAtAa = new HashMap<Character, String>(0);
    private HashMap<Character, String> fixedPeptideCtermModificationsAtAa = new HashMap<Character, String>(0);
    private HashMap<Character, String> fixedModificationsAtAa = new HashMap<Character, String>(0);
    private HashMap<String, AminoAcidPattern> modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
    private HashMap<Character, Double> aaMasses = new HashMap<Character, Double>(26);
    private HashMap<String, Double> modificationsMasses;
    private double minCtermMass = 0.0;

    public ProteinSequenceIterator(ArrayList<String> fixedModifications) {
        fillPtmMaps(fixedModifications);
        fillMassesMaps();
    }

    private void fillMassesMaps() {
        for (char aminoAcidName : AminoAcid.getUniqueAminoAcids()) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aminoAcidName);
            Double aaMass = aminoAcid.getMonoisotopicMass();
            aaMasses.put(aminoAcidName, aaMass);
        }
    }

    private void fillPtmMaps(ArrayList<String> fixedModifications) {
        modificationsMasses = new HashMap<String, Double>(fixedModifications.size());
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
        String fixedPeptideNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(nTermAaChar);
        if (fixedPeptideNtermModificationAtAa != null) {
            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideNtermModificationAtAa);
            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                return fixedPeptideNtermModificationAtAa;
            }
        }
        return null;
    }

    public ArrayList<Peptide> getPeptides(String proteinSequence, Double massMin, Double massMax) {
        ArrayList<Peptide> result = new ArrayList<Peptide>();
        char[] sequenceAsCharArray = proteinSequence.toCharArray();
        for (int i = 0; i < sequenceAsCharArray.length - 1; i++) {
            Double sequenceMass = 0.0;
            char nTermAaChar = sequenceAsCharArray[i];
            StringBuilder peptideSequence = new StringBuilder(sequenceAsCharArray.length - i);
            HashMap<Integer, String> fixedModifications = new HashMap<Integer, String>(1);
            String nTermModification = getNtermModification(i == 0, nTermAaChar, proteinSequence);
            sequenceMass += modificationsMasses.get(nTermModification);
            for (int j = i; j < sequenceAsCharArray.length; j++) {
                Integer aaIndex = j - i;
                Character aaChar = sequenceAsCharArray[j];
                sequenceMass += aaMasses.get(aaChar);
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
                Peptide peptide = getPeptide(peptideDraft, proteinSequence, j, massMin, massMax, smallMass);
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

    public ArrayList<Peptide> getPeptidesAaCombinations(String sequence, Double massMin, Double massMax) {
        ArrayList<Peptide> result = new ArrayList<Peptide>();
        char[] sequenceAsCharArray = sequence.toCharArray();
        for (int i = 0; i < sequenceAsCharArray.length - 1; i++) {
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

    private ArrayList<Peptide> getPeptides(ArrayList<PeptideDraft> peptideDrafts, String proteinSequence, int indexOnProtein, Double massMin, Double massMax, EncapsulatedObject<Boolean> smallMasses) {
        ArrayList<Peptide> results = new ArrayList<Peptide>();
        for (PeptideDraft simplePeptide : peptideDrafts) {
            EncapsulatedObject<Boolean> smallMass = new EncapsulatedObject<Boolean>(Boolean.FALSE);
            Peptide peptide = getPeptide(simplePeptide, proteinSequence, indexOnProtein, massMin, massMax, smallMass);
            if (peptide != null) {
                results.add(peptide);
            }
            if (smallMass.getObject()) {
                smallMasses.setObject(Boolean.TRUE);
            }
        }
        return results;
    }

    private Peptide getPeptide(PeptideDraft simplePeptide, String proteinSequence, int indexOnProtein, Double massMin, Double massMax, EncapsulatedObject<Boolean> smallMass) {
        Double peptideMass = simplePeptide.getMass();
        StringBuilder peptideSequence = simplePeptide.getSequence();
        char aaChar = peptideSequence.charAt(peptideSequence.length() - 1);
        String cTermModification = null;
        if (indexOnProtein == proteinSequence.length() - simplePeptide.length()) {
            if (fixedProteinCtermModification != null) {
                cTermModification = fixedProteinCtermModification;
                peptideMass += modificationsMasses.get(cTermModification);
            } else {
                String fixedProteinCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinCtermModificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, proteinSequence.length() - 1)) {
                    cTermModification = fixedProteinCtermModification;
                    peptideMass += modificationsMasses.get(fixedProteinCtermModificationAtAa);
                }
            }
        }
        if (cTermModification == null && fixedPeptideCtermModification != null) {
            cTermModification = fixedPeptideCtermModification;
            peptideMass += modificationsMasses.get(fixedPeptideCtermModification);
        }
        String fixedPeptideCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
        if (cTermModification == null && fixedPeptideCtermModificationAtAa != null) {
            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideCtermModificationAtAa);
            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein + simplePeptide.length())) {
                cTermModification = fixedPeptideCtermModificationAtAa;
                peptideMass += modificationsMasses.get(fixedPeptideCtermModificationAtAa);
            }
        }
        if (massMax == null || peptideMass <= massMax) {
            smallMass.setObject(Boolean.TRUE);
            if (massMin == null || peptideMass >= massMin) {
                ArrayList<ModificationMatch> modificationMatches = null;
                String nTermModification = simplePeptide.getnTermModification();
                HashMap<Integer, String> peptideModifications = simplePeptide.getFixedAaModifications();
                if (nTermModification != null) {
                    modificationMatches = new ArrayList<ModificationMatch>(peptideModifications.size());
                    modificationMatches.add(new ModificationMatch(nTermModification, false, 1));
                }
                if (cTermModification != null) {
                    if (modificationMatches == null) {
                        modificationMatches = new ArrayList<ModificationMatch>(peptideModifications.size());
                    }
                    modificationMatches.add(new ModificationMatch(cTermModification, false, peptideSequence.length()));
                }
                for (Integer site : peptideModifications.keySet()) {
                    if (modificationMatches == null) {
                        modificationMatches = new ArrayList<ModificationMatch>(peptideModifications.size());
                    }
                    String modificationName = peptideModifications.get(site);
                    modificationMatches.add(new ModificationMatch(modificationName, false, site));
                }
                Peptide peptide = new Peptide(peptideSequence.toString(), modificationMatches);
                return peptide;
            }
        }
        return null;
    }

    public ArrayList<Peptide> getPeptidesNoDigestion(String sequence, Double massMin, Double massMax) {
        if (AminoAcidSequence.containsAmbiguousAminoAcid(sequence)) {
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
            if ((massMin == null || peptide.getMass() <= massMax)
                    && (massMax == null || peptide.getMass() <= massMax)) {
                result.add(peptide);
            }
            return result;
        }
    }

    public Peptide getPeptideNoDigestion(String peptideSequence, String proteinSequence, int indexOnProtein, Double massMin, Double massMax) {
        char nTermAaChar = peptideSequence.charAt(0);
        String nTermModification = getNtermModification(indexOnProtein == 0, nTermAaChar, proteinSequence);
        HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
        Double peptideMass = modificationsMasses.get(nTermModification);
        for (int i = 0; i < peptideSequence.length(); i++) {
            char aaChar = peptideSequence.charAt(i);
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aaChar);
            peptideMass += aminoAcid.getMonoisotopicMass();
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
        return getPeptide(peptideDraft, proteinSequence, indexOnProtein, massMin, massMax, new EncapsulatedObject<Boolean>(Boolean.FALSE));
    }

    public ArrayList<Peptide> getPeptidesDigestion(String proteinSequence, DigestionPreferences digestionPreferences, Double massMin, Double massMax) {
        HashMap<Integer, ArrayList<Peptide>> peptides = new HashMap<Integer, ArrayList<Peptide>>();
        ArrayList<Peptide> originalSequence = new ArrayList<Peptide>(1);
        Peptide protein = new Peptide(proteinSequence, null);
        originalSequence.add(protein);
        peptides.put(0, originalSequence);
        for (Enzyme enzyme : digestionPreferences.getEnzymes()) {
            String enzymeName = enzyme.getName();
            Integer nMissedCleavages = digestionPreferences.getnMissedCleavages(enzymeName);
            DigestionPreferences.Specificity specificity = digestionPreferences.getSpecificity(enzymeName);
            HashMap<Integer, ArrayList<Peptide>> newPeptides = new HashMap<Integer, ArrayList<Peptide>>(peptides.size());
            for (Integer peptideStart : peptides.keySet()) {
                ArrayList<Peptide> peptidesAtPosition = peptides.get(peptideStart);
                for (Peptide peptide : peptidesAtPosition) {
                    HashMap<Integer, ArrayList<Peptide>> subPeptides = digest(peptide.getSequence(), proteinSequence, peptideStart, enzyme, nMissedCleavages, massMin, massMax);
                    for (Integer tempPeptideStart : subPeptides.keySet()) {
                        ArrayList<Peptide> tempPeptides = subPeptides.get(tempPeptideStart);
                        Integer newPeptideStart = tempPeptideStart + peptideStart;
                        ArrayList<Peptide> newPeptidesAtI = newPeptides.get(newPeptideStart);
                        if (newPeptidesAtI == null) {
                            newPeptides.put(newPeptideStart, tempPeptides);
                        } else {
                            newPeptidesAtI.addAll(tempPeptides);
                        }
                    }
                }
            }
            peptides = newPeptides;
        }
        ArrayList<Peptide> result = new ArrayList<Peptide>(peptides.size());
        for (ArrayList<Peptide> peptidesAtI : peptides.values()) {
            result.addAll(peptidesAtI);
        }
        return result;
    }

    public HashMap<Integer, ArrayList<Peptide>> digest(String sequence, String proteinSequence, Integer indexOnProtein, Enzyme enzyme, int maxMissedCleavages, Double massMin, Double massMax) {

        char aa = sequence.charAt(0);
        ArrayList<PeptideDraft> tempPeptides = new ArrayList<PeptideDraft>();
        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
        for (char subAa : aminoAcid.getSubAminoAcids()) {
            StringBuilder currentPeptide = new StringBuilder();
            currentPeptide.append(subAa);
            Double currentMass = AminoAcid.getAminoAcid(subAa).getMonoisotopicMass();
            String nTermModification = getNtermModification(indexOnProtein == 0, subAa, proteinSequence);
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
            tempPeptides.add(peptideDraft);
        }

        HashMap<Integer, ArrayList<Peptide>> result = new HashMap<Integer, ArrayList<Peptide>>();

        for (int i = 1; i < sequence.length(); i++) {

            aa = sequence.charAt(i);
            aminoAcid = AminoAcid.getAminoAcid(aa);
            for (char aaAfter : aminoAcid.getSubAminoAcids()) {

                ArrayList<PeptideDraft> newPeptides = new ArrayList<PeptideDraft>(tempPeptides.size());

                for (PeptideDraft peptideDraft : tempPeptides) {

                    char aaBefore = peptideDraft.getSequence().charAt(peptideDraft.length() - 1);

                    if (enzyme.isCleavageSite(aaBefore, aaAfter)) {
                        Peptide peptide = getPeptide(peptideDraft, proteinSequence, i - 1 + indexOnProtein, massMin, massMax, new EncapsulatedObject<Boolean>(Boolean.FALSE));
                        if (peptide != null) {
                            Integer startIndex = i - peptideDraft.length();
                            ArrayList<Peptide> peptidesAtI = result.get(startIndex);
                            if (peptidesAtI == null) {
                                peptidesAtI = new ArrayList<Peptide>(maxMissedCleavages + 1);
                                result.put(startIndex, peptidesAtI);
                            }
                            peptidesAtI.add(peptide);
                        }
                        peptideDraft.increaseMissedCleavages();
                    }
                    if (peptideDraft.getMissedCleavages() <= maxMissedCleavages) {
                        if (aminoAcid.getSubAminoAcids().length > 1) {
                            StringBuilder newSequence = new StringBuilder(peptideDraft.getSequence());
                            Double mass = peptideDraft.getMass();
                            mass += AminoAcid.getAminoAcid(aaAfter).getMonoisotopicMass();
                            HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(peptideDraft.getFixedAaModifications());
                            String modificationAtAa = fixedModificationsAtAa.get(aaAfter);
                            if (modificationAtAa != null) {
                                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein)) {
                                    peptideModifications.put(1, modificationAtAa);
                                    mass += modificationsMasses.get(modificationAtAa);
                                }
                            }
                            if (massMax == null || mass + minCtermMass < massMax) {
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
                                    peptideModifications.put(1, modificationAtAa);
                                    newMass += modificationsMasses.get(modificationAtAa);
                                }
                            }
                            if (massMax == null || newMass + minCtermMass < massMax) {
                                peptideDraft.setMass(newMass);
                                newPeptides.add(peptideDraft);
                            }
                        }
                    }
                }

                tempPeptides = newPeptides;

            }
        }
        for (PeptideDraft peptideDraft : tempPeptides) {

            Peptide peptide = getPeptide(peptideDraft, proteinSequence, sequence.length() - 1 + indexOnProtein, massMin, massMax, new EncapsulatedObject<Boolean>(Boolean.FALSE));
            if (peptide != null) {
                Integer startIndex = sequence.length() - peptideDraft.length();
                ArrayList<Peptide> peptidesAtI = result.get(startIndex);
                if (peptidesAtI == null) {
                    peptidesAtI = new ArrayList<Peptide>(1);
                    result.put(startIndex, peptidesAtI);
                }
                peptidesAtI.add(peptide);
            }
        }
        return result;
    }

    public ArrayList<Peptide> getPeptides(String sequence, DigestionPreferences digestionPreferences, Double massMin, Double massMax) {
        switch (digestionPreferences.getCleavagePreference()) {
            case unSpecific:
                return getPeptides(sequence, massMin, massMax);
            case wholeProtein:
                return getPeptidesNoDigestion(sequence, massMin, massMax);
            case enzyme:
            default:
                throw new UnsupportedOperationException("Cleavage preference of type " + digestionPreferences.getCleavagePreference() + " not supported.");

        }
    }

    private class PeptideDraft {

        private StringBuilder sequence;
        private String nTermModification;
        private String cTermModification;
        private HashMap<Integer, String> fixedAaModifications;
        private Double mass;
        private int missedCleavages = 0;

        public PeptideDraft(StringBuilder sequence, String nTermModification, HashMap<Integer, String> fixedAaModifications, Double mass) {
            this.sequence = sequence;
            this.nTermModification = nTermModification;
            this.fixedAaModifications = fixedAaModifications;
            this.mass = mass;
        }

        public PeptideDraft(StringBuilder sequence, String nTermModification, HashMap<Integer, String> fixedAaModifications, Double mass, int missedCleavages) {
            this.sequence = sequence;
            this.nTermModification = nTermModification;
            this.fixedAaModifications = fixedAaModifications;
            this.mass = mass;
            this.missedCleavages = missedCleavages;
        }

        public StringBuilder getSequence() {
            return sequence;
        }

        public void setSequence(StringBuilder sequence) {
            this.sequence = sequence;
        }

        public int length() {
            return sequence.length();
        }

        public String getnTermModification() {
            return nTermModification;
        }

        public void setnTermModification(String nTermModification) {
            this.nTermModification = nTermModification;
        }

        public String getcTermModification() {
            return cTermModification;
        }

        public void setcTermModification(String cTermModification) {
            this.cTermModification = cTermModification;
        }

        public Double getMass() {
            return mass;
        }

        public void setMass(Double mass) {
            this.mass = mass;
        }

        public HashMap<Integer, String> getFixedAaModifications() {
            return fixedAaModifications;
        }

        public void setFixedAaModifications(HashMap<Integer, String> fixedAaModifications) {
            this.fixedAaModifications = fixedAaModifications;
        }

        public void increaseMissedCleavages() {
            missedCleavages++;
        }

        public int getMissedCleavages() {
            return missedCleavages;
        }
    }

}
