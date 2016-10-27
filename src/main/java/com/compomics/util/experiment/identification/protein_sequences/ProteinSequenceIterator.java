package com.compomics.util.experiment.identification.protein_sequences;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
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
    private HashMap<Character, Double> massesMin = new HashMap<Character, Double>(26);
    private HashMap<Character, Double> massesMax = new HashMap<Character, Double>(26);

    public ProteinSequenceIterator(ArrayList<String> fixedModifications) {
        fillPtmMaps(fixedModifications);
        fillMassesMaps();
    }

    private void fillMassesMaps() {
        for (char aminoAcidName : AminoAcid.getAminoAcids()) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aminoAcidName);
            if (aminoAcid.iscombination()) {
                Double massMin = null;
                Double massMax = null;
                for (char subAminoAcidName : aminoAcid.getSubAminoAcids()) {
                    AminoAcid subAminoAcid = AminoAcid.getAminoAcid(subAminoAcidName);
                    Double aaMass = subAminoAcid.getMonoisotopicMass();
                    if (massMin == null || aaMass < massMin) {
                        massMin = aaMass;
                    }
                    if (massMax == null || aaMass > massMax) {
                        massMax = aaMass;
                    }
                }
                massesMin.put(aminoAcidName, massMin);
                massesMax.put(aminoAcidName, massMax);
            } else {
                Double aaMass = aminoAcid.getMonoisotopicMass();
                massesMin.put(aminoAcidName, aaMass);
                massesMax.put(aminoAcidName, aaMass);
            }
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
            Double sequenceMassMin = 0.0;
            Double sequenceMassMax = 0.0;
            char nTermAaChar = sequenceAsCharArray[i];
            StringBuilder peptideSequence = new StringBuilder(sequenceAsCharArray.length - i);
            HashMap<Integer, String> fixedModifications = new HashMap<Integer, String>(1);
            String nTermModification = null;
            if (i == 0) {
                if (fixedProteinNtermModification != null) {
                    nTermModification = fixedProteinNtermModification;
                    sequenceMassMin += fixedProteinNtermModificationMass;
                    sequenceMassMax += fixedProteinNtermModificationMass;
                }
                String fixedProteinNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(nTermAaChar);
                if (fixedProteinNtermModificationAtAa != null) {
                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinNtermModificationAtAa);
                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                        nTermModification = fixedProteinNtermModificationAtAa;
                        sequenceMassMin += fixedProteinNtermModificationsAtAaMass.get(nTermAaChar);
                        sequenceMassMax += fixedProteinNtermModificationsAtAaMass.get(nTermAaChar);
                    }
                }
            }
            if (nTermModification == null && fixedPeptideNtermModification != null) {
                nTermModification = fixedPeptideNtermModification;
                sequenceMassMin += fixedPeptideNtermModificationMass;
                sequenceMassMax += fixedPeptideNtermModificationMass;
            }
            String fixedPeptideNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(nTermAaChar);
            if (nTermModification == null && fixedPeptideNtermModificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideNtermModificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                    nTermModification = fixedPeptideNtermModificationAtAa;
                    sequenceMassMin += fixedProteinNtermModificationsAtAaMass.get(nTermAaChar);
                    sequenceMassMax += fixedProteinNtermModificationsAtAaMass.get(nTermAaChar);
                }
            }
            for (int j = i; j < sequenceAsCharArray.length; j++) {
                Integer charIndex = j - i;
                Character aaChar = sequenceAsCharArray[j];
                sequenceMassMin += massesMin.get(aaChar);
                sequenceMassMax += massesMax.get(aaChar);
                peptideSequence.append(aaChar);
                String modificationAtAa = fixedModificationsAtAa.get(aaChar);
                if (modificationAtAa != null) {
                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                        fixedModifications.put(charIndex, modificationAtAa);
                        sequenceMassMin += fixedModificationsAtAaMass.get(aaChar);
                        sequenceMassMax += fixedModificationsAtAaMass.get(aaChar);
                    }
                }
                Double peptideMassMin = sequenceMassMin;
                Double peptideMassMax = sequenceMassMax;
                String cTermModification = null;
                if (i == 0) {
                    if (fixedProteinCtermModification != null) {
                        cTermModification = fixedProteinCtermModification;
                        peptideMassMin += fixedProteinCtermModificationMass;
                        peptideMassMax += fixedProteinCtermModificationMass;
                    }
                    String fixedProteinCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
                    if (fixedProteinCtermModificationAtAa != null) {
                        AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinCtermModificationAtAa);
                        if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                            cTermModification = fixedProteinCtermModification;
                            peptideMassMin += fixedProteinCtermModificationsAtAaMass.get(aaChar);
                            peptideMassMax += fixedProteinCtermModificationsAtAaMass.get(aaChar);
                        }
                    }
                }
                if (cTermModification == null && fixedPeptideCtermModification != null) {
                    cTermModification = fixedPeptideCtermModification;
                    peptideMassMin += fixedPeptideCtermModificationMass;
                    peptideMassMax += fixedPeptideCtermModificationMass;
                }
                String fixedPeptideCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
                if (cTermModification == null && fixedPeptideCtermModificationAtAa != null) {
                    AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideCtermModificationAtAa);
                    if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(sequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                        cTermModification = fixedPeptideCtermModificationAtAa;
                        peptideMassMin += fixedPeptideCtermModificationsAtAaMass.get(aaChar);
                        peptideMassMax += fixedPeptideCtermModificationsAtAaMass.get(aaChar);
                    }
                }
                if (massMax != null && peptideMassMin > massMax) {
                    break;
                }
                if (massMin != null && peptideMassMax >= massMin) {
                    ArrayList<ModificationMatch> modificationMatches = null;
                    if (nTermModification != null) {
                        modificationMatches = new ArrayList<ModificationMatch>(fixedModifications.size());
                        modificationMatches.add(new ModificationMatch(nTermModification, false, 1));
                    }
                    if (cTermModification != null) {
                        if (modificationMatches == null) {
                            modificationMatches = new ArrayList<ModificationMatch>(fixedModifications.size());
                        }
                        modificationMatches.add(new ModificationMatch(cTermModification, false, 1));
                    }
                    for (Integer site : fixedModifications.keySet()) {
                        if (modificationMatches == null) {
                            modificationMatches = new ArrayList<ModificationMatch>(fixedModifications.size());
                        }
                        String modificationName = fixedModifications.get(site);
                        modificationMatches.add(new ModificationMatch(modificationName, false, site));
                    }
                    Peptide peptide = new Peptide(sequence, modificationMatches);
                    result.add(peptide);
                }
            }
        }

        return result;
    }

}
