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
    private Double fixedProteinNtermModificationMass = null;
    private String fixedProteinCtermModification = null;
    private Double fixedProteinCtermModificationMass = null;
    private HashMap<Character, String> fixedProteinNtermModificationsAtAa = new HashMap<Character, String>(0);
    private HashMap<Character, Double> fixedProteinNtermModificationsAtAaMass = new HashMap<Character, Double>(0);
    private HashMap<Character, String> fixedProteinCtermModificationsAtAa = new HashMap<Character, String>(0);
    private HashMap<Character, Double> fixedProteinCtermModificationsAtAaMass = new HashMap<Character, Double>(0);
    private String fixedPeptideNtermModification = null;
    private Double fixedPeptideNtermModificationMass = null;
    private String fixedPeptideCtermModification = null;
    private Double fixedPeptideCtermModificationMass = null;
    private HashMap<Character, String> fixedPeptideNtermModificationsAtAa = new HashMap<Character, String>(0);
    private HashMap<Character, Double> fixedPeptideNtermModificationsAtAaMass = new HashMap<Character, Double>(0);
    private HashMap<Character, String> fixedPeptideCtermModificationsAtAa = new HashMap<Character, String>(0);
    private HashMap<Character, Double> fixedPeptideCtermModificationsAtAaMass = new HashMap<Character, Double>(0);
    private HashMap<Character, String> fixedModificationsAtAa = new HashMap<Character, String>(0);
    private HashMap<Character, Double> fixedModificationsAtAaMass = new HashMap<Character, Double>(0);
    private HashMap<String, AminoAcidPattern> modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
    private HashMap<Character, Double> aaMasses = new HashMap<Character, Double>(26);

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
        PTMFactory ptmFactory = PTMFactory.getInstance();
        for (String ptmName : fixedModifications) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            switch (ptm.getType()) {
                case PTM.MODN:
                    if (fixedProteinNtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the protein N-terminus.");
                    }
                    fixedProteinNtermModification = ptmName;
                    fixedProteinNtermModificationMass = ptm.getMass();
                    break;
                case PTM.MODC:
                    if (fixedProteinCtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the protein C-terminus.");
                    }
                    fixedProteinCtermModification = ptmName;
                    fixedProteinCtermModificationMass = ptm.getMass();
                    break;
                case PTM.MODNP:
                    if (fixedPeptideNtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the peptide N-terminus.");
                    }
                    fixedPeptideNtermModification = ptmName;
                    fixedPeptideNtermModificationMass = ptm.getMass();
                    break;
                case PTM.MODCP:
                    if (fixedPeptideCtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the peptide C-terminus.");
                    }
                    fixedPeptideCtermModification = ptmName;
                    fixedPeptideCtermModificationMass = ptm.getMass();
                    break;
                case PTM.MODNAA:
                    AminoAcidPattern ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedProteinNtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per protein N-term amino acid. Found two at " + aa + ".");
                        }
                        fixedProteinNtermModificationsAtAa.put(aa, ptm.getName());
                        fixedProteinNtermModificationsAtAaMass.put(aa, ptm.getMass());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    break;
                case PTM.MODCAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedProteinCtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per protein C-term amino acid. Found two at " + aa + ".");
                        }
                        fixedProteinCtermModificationsAtAa.put(aa, ptm.getName());
                        fixedProteinCtermModificationsAtAaMass.put(aa, ptm.getMass());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
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
                        fixedPeptideNtermModificationsAtAaMass.put(aa, ptm.getMass());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    break;
                case PTM.MODCPAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedPeptideCtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per peptide N-term amino acid. Found two at " + aa + ".");
                        }
                        fixedPeptideCtermModificationsAtAa.put(aa, ptm.getName());
                        fixedPeptideCtermModificationsAtAaMass.put(aa, ptm.getMass());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
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
                        fixedModificationsAtAaMass.put(aa, ptm.getMass());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<String, AminoAcidPattern>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    break;
            }
        }
    }

    public ArrayList<Peptide> getPeptides(String sequence, Double massMin, Double massMax) {
        ArrayList<Peptide> result = new ArrayList<Peptide>();
        char[] sequenceAsCharArray = sequence.toCharArray();
        for (int i = 0; i < sequenceAsCharArray.length - 1; i++) {
            Double sequenceMass = 0.0;
            char nTermAaChar = sequenceAsCharArray[i];
            StringBuilder peptideSequence = new StringBuilder(sequenceAsCharArray.length - i);
            HashMap<Integer, String> fixedModifications = new HashMap<Integer, String>(1);
            String nTermModification = null;
            if (i == 0) {
                if (fixedProteinNtermModification != null) {
                    nTermModification = fixedProteinNtermModification;
                    sequenceMass += fixedProteinNtermModificationMass;
                } else {
                    String fixedProteinNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(nTermAaChar);
                    if (fixedProteinNtermModificationAtAa != null) {
                        AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinNtermModificationAtAa);
                        if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                            nTermModification = fixedProteinNtermModificationAtAa;
                            sequenceMass += fixedProteinNtermModificationsAtAaMass.get(nTermAaChar);
                        }
                    }
                }
            }
            if (nTermModification == null && fixedPeptideNtermModification != null) {
                nTermModification = fixedPeptideNtermModification;
                sequenceMass += fixedPeptideNtermModificationMass;
            }
            if (nTermModification == null) {
                String fixedPeptideNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(nTermAaChar);
                if (nTermModification == null && fixedPeptideNtermModificationAtAa != null) {
                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideNtermModificationAtAa);
                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                        nTermModification = fixedPeptideNtermModificationAtAa;
                        sequenceMass += fixedProteinNtermModificationsAtAaMass.get(nTermAaChar);
                    }
                }
            }
            for (int j = i; j < sequenceAsCharArray.length; j++) {
                Integer aaIndex = j - i;
                Character aaChar = sequenceAsCharArray[j];
                sequenceMass += aaMasses.get(aaChar);
                peptideSequence.append(aaChar);
                String modificationAtAa = fixedModificationsAtAa.get(aaChar);
                if (modificationAtAa != null) {
                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, j)) {
                        fixedModifications.put(aaIndex + 1, modificationAtAa);
                        sequenceMass += fixedModificationsAtAaMass.get(aaChar);
                    }
                }
                EncapsulatedObject<Boolean> smallMass = new EncapsulatedObject<Boolean>();
                smallMass.setObject(false);
                Peptide peptide = getPeptide(peptideSequence, sequenceMass, fixedModifications, nTermModification, sequence, j == sequence.length() - 1, massMin, massMax, smallMass);
                if (!smallMass.getObject()) {
                    break;
                }
                result.add(peptide);
            }
        }

        return result;
    }

    public ArrayList<Peptide> getPeptidesAaCombinations(String sequence, Double massMin, Double massMax) {
        ArrayList<Peptide> result = new ArrayList<Peptide>();
        char[] sequenceAsCharArray = sequence.toCharArray();
        for (int i = 0; i < sequenceAsCharArray.length - 1; i++) {
            ArrayList<StringBuilder> peptideSequences = new ArrayList<StringBuilder>(1);
            ArrayList<Double> sequenceMasses = new ArrayList<Double>(1);
            ArrayList<HashMap<Integer, String>> fixedModifications = new ArrayList<HashMap<Integer, String>>(1);
            ArrayList<String> nTermModifications = new ArrayList<String>(1);
            char sequenceNTermAaChar = sequenceAsCharArray[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(sequenceNTermAaChar);
            for (char nTermAaChar : aminoAcid.getCombinations()) {
                String nTermModification = "";
                Double sequenceMass = 0.0;
                StringBuilder peptideSequence = new StringBuilder(sequenceAsCharArray.length - i);
                HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
                if (i == 0) {
                    if (fixedProteinNtermModification != null) {
                        nTermModification = fixedProteinNtermModification;
                        sequenceMass += fixedProteinNtermModificationMass;
                    } else {
                        String fixedProteinNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(nTermAaChar);
                        if (fixedProteinNtermModificationAtAa != null) {
                            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinNtermModificationAtAa);
                            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                                nTermModification = fixedProteinNtermModificationAtAa;
                                sequenceMass += fixedProteinNtermModificationsAtAaMass.get(nTermAaChar);
                            }
                        }
                    }
                }
                if (nTermModification.equals("") && fixedPeptideNtermModification != null) {
                    nTermModification = fixedPeptideNtermModification;
                    sequenceMass += fixedPeptideNtermModificationMass;
                }
                if (nTermModification.equals("")) {
                    String fixedPeptideNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(nTermAaChar);
                    if (fixedPeptideNtermModificationAtAa != null) {
                        AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideNtermModificationAtAa);
                        if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                            nTermModification = fixedPeptideNtermModificationAtAa;
                            sequenceMass += fixedProteinNtermModificationsAtAaMass.get(nTermAaChar);
                        }
                    }
                }
                AminoAcid subAminoAcid = AminoAcid.getAminoAcid(nTermAaChar);
                sequenceMass += subAminoAcid.getMonoisotopicMass();
                peptideSequence.append(nTermAaChar);
                String modificationAtAa = fixedModificationsAtAa.get(nTermAaChar);
                if (modificationAtAa != null) {
                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                        peptideModifications.put(1, modificationAtAa);
                        sequenceMass += fixedModificationsAtAaMass.get(nTermAaChar);
                    }
                }
                peptideSequences.add(peptideSequence);
                sequenceMasses.add(sequenceMass);
                fixedModifications.add(peptideModifications);
                nTermModifications.add(nTermModification);
                EncapsulatedObject<Boolean> smallMasses = new EncapsulatedObject<Boolean>();
                smallMasses.setObject(false);
                ArrayList<Peptide> peptidesAtIndex = getPeptides(peptideSequences, sequenceMasses, fixedModifications, nTermModifications, sequence, i == sequenceAsCharArray.length - 1, massMin, massMax, smallMasses);
                result.addAll(peptidesAtIndex);
                if (!smallMasses.getObject()) {
                    return result;
                }
            }
            for (int j = i + 1; j < sequenceAsCharArray.length; j++) {
                int aaIndex = j - i;
                ArrayList<StringBuilder> newPeptideSequences = new ArrayList<StringBuilder>(peptideSequences.size());
                ArrayList<Double> newSequenceMasses = new ArrayList<Double>(sequenceMasses.size());
                ArrayList<HashMap<Integer, String>> newFixedModifications = new ArrayList<HashMap<Integer, String>>(fixedModifications.size());
                ArrayList<String> newNTermModifications = new ArrayList<String>(nTermModifications.size());
                char sequenceChar = sequenceAsCharArray[j];
                AminoAcid sequenceAminoAcid = AminoAcid.getAminoAcid(sequenceChar);
                for (int k = 0; k < peptideSequences.size(); k++) {
                    for (char aaChar : sequenceAminoAcid.getSubAminoAcids()) {
                        StringBuilder peptideSequence = new StringBuilder(peptideSequences.get(k));
                        Double peptideMass = sequenceMasses.get(k);
                        String nTermModification = nTermModifications.get(k);
                        HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(fixedModifications.get(k));
                        AminoAcid subAminoAcid = AminoAcid.getAminoAcid(aaChar);
                        peptideMass += subAminoAcid.getMonoisotopicMass();
                        peptideSequence.append(aaChar);
                        String modificationAtAa = fixedModificationsAtAa.get(aaChar);
                        if (modificationAtAa != null) {
                            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, j)) {
                                peptideModifications.put(aaIndex + 1, modificationAtAa);
                                peptideMass += fixedModificationsAtAaMass.get(aaChar);
                            }
                        }
                        newPeptideSequences.add(peptideSequence);
                        newSequenceMasses.add(peptideMass);
                        newFixedModifications.add(peptideModifications);
                        newNTermModifications.add(nTermModification);
                    }
                }
                peptideSequences = newPeptideSequences;
                sequenceMasses = newSequenceMasses;
                fixedModifications = newFixedModifications;
                nTermModifications = newNTermModifications;
                EncapsulatedObject<Boolean> smallMasses = new EncapsulatedObject<Boolean>();
                smallMasses.setObject(false);
                ArrayList<Peptide> peptidesAtIndex = getPeptides(peptideSequences, sequenceMasses, fixedModifications, nTermModifications, sequence, j == sequenceAsCharArray.length - 1, massMin, massMax, smallMasses);
                result.addAll(peptidesAtIndex);
                if (!smallMasses.getObject()) {
                    break;
                }
            }
        }
        return result;
    }

    private ArrayList<Peptide> getPeptides(ArrayList<StringBuilder> peptideSequences, ArrayList<Double> sequenceMasses, ArrayList<HashMap<Integer, String>> fixedModifications, ArrayList<String> nTermModifications, String proteinSequence, boolean proteinCTerm, Double massMin, Double massMax, EncapsulatedObject<Boolean> smallMasses) {
        ArrayList<Peptide> results = new ArrayList<Peptide>();
        for (int k = 0; k < peptideSequences.size(); k++) {
            StringBuilder peptideSequence = peptideSequences.get(k);
            Double peptideMass = sequenceMasses.get(k);
            String nTermModification = nTermModifications.get(k);
            HashMap<Integer, String> peptideModifications = fixedModifications.get(k);
            EncapsulatedObject<Boolean> smallMass = new EncapsulatedObject<Boolean>();
            smallMass.setObject(false);
            Peptide peptide = getPeptide(peptideSequence, peptideMass, peptideModifications, nTermModification, proteinSequence, proteinCTerm, massMin, massMax, smallMass);
            if (peptide != null) {
                results.add(peptide);
            }
            if (smallMass.getObject()) {
                smallMasses.setObject(true);
            }
        }
        return results;
    }

    private Peptide getPeptide(StringBuilder peptideSequence, Double sequenceMass, HashMap<Integer, String> peptideModifications, String nTermModification, String proteinSequence, boolean proteinCTerm, Double massMin, Double massMax, EncapsulatedObject<Boolean> smallMass) {
        Double peptideMass = sequenceMass;
        char aaChar = peptideSequence.charAt(peptideSequence.length() - 1);
        String cTermModification = null;
        if (proteinCTerm) {
            if (fixedProteinCtermModification != null) {
                cTermModification = fixedProteinCtermModification;
                peptideMass += fixedProteinCtermModificationMass;
            } else {
                String fixedProteinCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinCtermModificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, proteinSequence.length() - 1)) {
                    cTermModification = fixedProteinCtermModification;
                    peptideMass += fixedProteinCtermModificationsAtAaMass.get(aaChar);
                }
            }
        }
        if (cTermModification == null && fixedPeptideCtermModification != null) {
            cTermModification = fixedPeptideCtermModification;
            peptideMass += fixedPeptideCtermModificationMass;
        }
        String fixedPeptideCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
        if (cTermModification == null && fixedPeptideCtermModificationAtAa != null) {
            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideCtermModificationAtAa);
            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, proteinSequence.length() - 1)) {
                cTermModification = fixedPeptideCtermModificationAtAa;
                peptideMass += fixedPeptideCtermModificationsAtAaMass.get(aaChar);
            }
        }
        if (massMax == null || peptideMass <= massMax) {
            if (smallMass != null) {
                smallMass.setObject(true);
            }
            if (massMin == null || peptideMass >= massMin) {
                ArrayList<ModificationMatch> modificationMatches = null;
                if (nTermModification != null && !nTermModification.equals("")) {
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
            ArrayList<StringBuilder> sequences = new ArrayList<StringBuilder>();
            char[] sequenceAsCharArray = sequence.toCharArray();
            char aa = sequenceAsCharArray[0];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            for (char subAa : aminoAcid.getCombinations()) {
                StringBuilder subSequence = new StringBuilder();
                subSequence.append(subAa);
                sequences.add(subSequence);
            }
            for (int j = 1; j < sequenceAsCharArray.length; j++) {
                aa = sequenceAsCharArray[j];
                aminoAcid = AminoAcid.getAminoAcid(aa);
                ArrayList<StringBuilder> newSequences = new ArrayList<StringBuilder>(sequences.size());
                for (StringBuilder peptideSequence : sequences) {
                    for (char subAa : aminoAcid.getCombinations()) {
                        peptideSequence.append(subAa);
                        newSequences.add(peptideSequence);
                    }
                }
                sequences = newSequences;
            }
            ArrayList<Peptide> result = new ArrayList<Peptide>(sequences.size());
            for (StringBuilder peptideSequence : sequences) {
                Peptide peptide = getPeptideNoDigestion(peptideSequence.toString());
                if ((massMin == null || peptide.getMass() <= massMax)
                        && (massMax == null || peptide.getMass() <= massMax)) {
                    result.add(peptide);
                }
            }
            return result;
        } else {
            ArrayList<Peptide> result = new ArrayList<Peptide>(1);
            Peptide peptide = getPeptideNoDigestion(sequence);
            if ((massMin == null || peptide.getMass() <= massMax)
                    && (massMax == null || peptide.getMass() <= massMax)) {
                result.add(peptide);
            }
            return result;
        }
    }

    public Peptide getPeptideNoDigestion(String proteinSequence) {
        return getPeptideNoDigestion(proteinSequence, true, true);
    }

    public Peptide getPeptideNoDigestion(String sequence, boolean proteinNTerminus, boolean proteinCTerminus) {
        String nTermModification = null;
        HashMap<Integer, String> peptideModifications = new HashMap<Integer, String>(1);
        char aa = sequence.charAt(0);
        if (proteinNTerminus) {
            if (fixedProteinNtermModification != null) {
                nTermModification = fixedProteinNtermModification;
            } else {
                String fixedProteinNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(aa);
                if (fixedProteinNtermModificationAtAa != null) {
                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinNtermModificationAtAa);
                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                        nTermModification = fixedProteinNtermModificationAtAa;
                    }
                }
            }
        }
        if (nTermModification == null && fixedPeptideNtermModification != null) {
            nTermModification = fixedPeptideNtermModification;
        }
        if (nTermModification == null) {
            String fixedPeptideNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(aa);
            if (fixedPeptideNtermModificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideNtermModificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                    nTermModification = fixedPeptideNtermModificationAtAa;
                }
            }
        }
        for (int i = 0; i < sequence.length(); i++) {
            aa = sequence.charAt(i);
            String modificationAtAa = fixedModificationsAtAa.get(aa);
            if (modificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, i)) {
                    peptideModifications.put(i + 1, modificationAtAa);
                }
            }
        }
        char aaChar = sequence.charAt(sequence.length() - 1);
        String cTermModification = null;
        if (proteinCTerminus) {
            if (fixedProteinCtermModification != null) {
                cTermModification = fixedProteinCtermModification;
            } else {
                String fixedProteinCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinCtermModificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, sequence.length() - 1)) {
                    cTermModification = fixedProteinCtermModification;
                }
            }
        }
        if (cTermModification == null && fixedPeptideCtermModification != null) {
            cTermModification = fixedPeptideCtermModification;
        }
        String fixedPeptideCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
        if (cTermModification == null && fixedPeptideCtermModificationAtAa != null) {
            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideCtermModificationAtAa);
            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, sequence.length() - 1)) {
                cTermModification = fixedPeptideCtermModificationAtAa;
            }
        }
        ArrayList<ModificationMatch> modificationMatches = null;
        if (nTermModification != null && !nTermModification.equals("")) {
            modificationMatches = new ArrayList<ModificationMatch>(peptideModifications.size() + 1);
            modificationMatches.add(new ModificationMatch(nTermModification, false, 1));
        }
        if (cTermModification != null) {
            if (modificationMatches == null) {
                modificationMatches = new ArrayList<ModificationMatch>(peptideModifications.size() + 1);
            }
            modificationMatches.add(new ModificationMatch(cTermModification, false, sequence.length()));
        }
        for (Integer site : peptideModifications.keySet()) {
            if (modificationMatches == null) {
                modificationMatches = new ArrayList<ModificationMatch>(peptideModifications.size());
            }
            String modificationName = peptideModifications.get(site);
            modificationMatches.add(new ModificationMatch(modificationName, false, site));
        }
        Peptide peptide = new Peptide(sequence, modificationMatches);
        return peptide;
    }
    
    public ArrayList<Peptide> getPeptidesDigestion(String proteinSequence, DigestionPreferences digestionPreferences, Double massMin, Double massMax) {
        HashMap<Integer, ArrayList<String>> peptideSequences = new HashMap<Integer, ArrayList<String>>();
        ArrayList<String> originalSequence = new ArrayList<String>(1);
        originalSequence.add(proteinSequence);
        peptideSequences.put(0, originalSequence);
        for (Enzyme enzyme : digestionPreferences.getEnzymes()) {
            String enzymeName = enzyme.getName();
            Integer nMissedCleavages = digestionPreferences.getnMissedCleavages(enzymeName);
            DigestionPreferences.Specificity specificity = digestionPreferences.getSpecificity(enzymeName);
            HashMap<Integer, ArrayList<String>> newPeptideSequences = new HashMap<Integer, ArrayList<String>>();
            for (Integer peptideStart : peptideSequences.keySet()) {
                ArrayList<String> peptidesAtPosition = peptideSequences.get(peptideStart);
                for (String peptideSequence : peptidesAtPosition) {
                HashMap<Integer, ArrayList<String>> digestedSequence = enzyme.digest(peptideSequence, specificity, nMissedCleavages, massMin, massMax);
                for (Integer tempPeptideStart : digestedSequence.keySet()) {
                ArrayList<String> tempPeptidesAtPosition = digestedSequence.get(tempPeptideStart);
                    Integer newPeptideStart = peptideStart + tempPeptideStart;
                ArrayList<String> newPeptidesAtPosition = newPeptideSequences.get(newPeptideStart);
                if (newPeptidesAtPosition == null) {
                    newPeptideSequences.put(newPeptideStart, tempPeptidesAtPosition);
                } else {
                    newPeptidesAtPosition.addAll(tempPeptidesAtPosition);
                }
                }
                }
            }
            peptideSequences = newPeptideSequences;
        }
        ArrayList<Peptide> peptides = new ArrayList<Peptide>(peptideSequences.size());
        for (Integer peptideStart : peptideSequences.keySet()) {
            for (String peptideSequence : peptideSequences.get(peptideStart)) {
                Integer peptideEnd = peptideStart + peptideSequence.length();
                Peptide peptide = getPeptideNoDigestion(peptideSequence, peptideStart == 0, peptideEnd == proteinSequence.length());
                peptides.add(peptide);
            }
        }
        return peptides;
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

}
