package com.compomics.util.experiment.identification.peptide_inference;

import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationProvider;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches_iterators.SpectrumMatchesIterator;
import com.compomics.util.experiment.identification.modification.peptide_mapping.ModificationPeptideMapping;
import com.compomics.util.experiment.identification.peptide_shaker.ModificationScoring;
import com.compomics.util.experiment.identification.peptide_shaker.PSModificationScores;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.advanced.ModificationLocalizationParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Functions related to peptide inference.
 *
 * @author Marc Vaudel
 */
public class PeptideInference {

    public final int CONFIDENT_OWN_OFFSET = 400;
    public final int CONFIDENT_OTHER_OFFSET = 200;
    public final int CONFIDENT_RELATED_OFFSET = 100;

    /**
     * Infers the PTM localization and its confidence for the best match of
     * every spectrum.
     *
     * @param identification The identification object containing the matches.
     * @param identificationParameters The identification parameters of the
     * project.
     * @param sequenceProvider The protein sequence provider to use.
     * @param modificationProvider The modification provider to use.
     * @param waitingHandler The waiting handler displaying progress to the
     * user.
     */
    public void peptideInference(
            Identification identification,
            IdentificationParameters identificationParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider,
            WaitingHandler waitingHandler
    ) {

        waitingHandler.setWaitingText("Peptide Inference. Please Wait...");

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(identification.getSpectrumIdentificationSize());

        ModificationLocalizationParameters modificationLocalizationParameters = identificationParameters.getModificationLocalizationParameters();
        SequenceMatchingParameters modificationSequenceMatchingParameters = modificationLocalizationParameters.getSequenceMatchingParameters();
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        ModificationParameters modificationParameters = searchParameters.getModificationParameters();

        // PSMs with confidently localized PTMs in a map: PTM mass -> peptide sequence -> spectrum keys
        HashMap<Double, HashMap<String, HashSet<Long>>> confidentPeptideInference = new HashMap<>();
        // PSMs with ambiguously localized PTMs
        HashSet<Long> notConfidentPeptideInference = new HashSet<>();

        SpectrumMatchesIterator psmIterator = identification.getSpectrumMatchesIterator(waitingHandler);
        SpectrumMatch spectrumMatch;

        while ((spectrumMatch = psmIterator.next()) != null) {

            if (spectrumMatch.getBestPeptideAssumption() != null) {

                fillConfidentMaps(
                        spectrumMatch,
                        confidentPeptideInference,
                        notConfidentPeptideInference,
                        modificationParameters,
                        modificationProvider,
                        waitingHandler
                );

                if (waitingHandler.isRunCanceled()) {

                    return;

                }
            }
        }

        for (long psmKey : notConfidentPeptideInference) {

            peptideInference(
                    psmKey,
                    confidentPeptideInference,
                    identification,
                    searchParameters,
                    modificationLocalizationParameters,
                    modificationSequenceMatchingParameters,
                    sequenceProvider,
                    modificationProvider
            );

            if (waitingHandler.isRunCanceled()) {

                return;

            }

            waitingHandler.increaseSecondaryProgressCounter();

        }
    }

    /**
     * Infers the PTM localization and its confidence for the best match of the
     * given spectrum for PTMs of the (exact) given mass.
     *
     * @param spectrumKey The key of the spectrum to process.
     * @param confidentPeptideInference PSMs with confidently localized PTMs in
     * a map: PTM mass, peptide sequence, spectrum keys.
     * @param identification The identification object containing the matches.
     * @param identificationParameters The identification parameters of the
     * project.
     * @param sequenceProvider The protein sequence provider to use.
     * @param modificationProvider The modification provider to use.
     * @param waitingHandler The waiting handler displaying progress to the
     * user.
     */
    private void peptideInference(
            long spectrumKey,
            HashMap<Double, HashMap<String, HashSet<Long>>> confidentPeptideInference,
            Identification identification,
            SearchParameters searchParameters,
            ModificationLocalizationParameters modificationScoringParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider
    ) {

        SpectrumMatch spectrumMatch = identification.getSpectrumMatch(spectrumKey);
        PSModificationScores modificationScores = (PSModificationScores) spectrumMatch.getUrParam(PSModificationScores.dummy);

        Peptide peptide = spectrumMatch.getBestPeptideAssumption().getPeptide();
        String sequence = peptide.getSequence();
        ModificationMatch[] modificationMatches = peptide.getVariableModifications();

        HashSet<Double> modMasses = Arrays.stream(modificationMatches)
                .map(
                        modificationMatch -> modificationProvider.getModification(modificationMatch.getModification()).getMass()
                )
                .collect(Collectors.toCollection(HashSet::new));

        HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore = new HashMap<>(modificationMatches.length);
        HashMap<Double, HashMap<Integer, String>> modificationToSiteToName = new HashMap<>(modificationMatches.length);

        for (double modMass : modMasses) {
            
            modificationToSiteToScore.put(modMass, new HashMap<>(2));
            modificationToSiteToName.put(modMass, new HashMap<>(2));
            
        }

        // See if other peptides can provide confident sites
        boolean relatedPeptide = false;
        HashSet<Long> processed = new HashSet<>();

        for (double modMass : modMasses) {

            HashMap<String, HashSet<Long>> modMap = confidentPeptideInference.get(modMass);

            for (Entry<String, HashSet<Long>> entry : modMap.entrySet()) {

                String entrySequence = entry.getKey();

                double scoreOffset = Double.NaN;
                int[] peptideSiteOffsets = null;
                int[] entrySiteOffsets = null;
                int tempIndex;

                if (entrySequence.equals(sequence)) {

                    scoreOffset = CONFIDENT_OTHER_OFFSET;
                    peptideSiteOffsets = new int[]{0};
                    entrySiteOffsets = new int[]{0};

                } else if (entrySequence.length() > sequence.length() && (tempIndex = entrySequence.indexOf(sequence)) >= 0) {

                    scoreOffset = CONFIDENT_RELATED_OFFSET;
                    peptideSiteOffsets = new int[]{0};

                    ArrayList<Integer> offsetList = new ArrayList<>(1);
                    offsetList.add(tempIndex);

                    int ref = tempIndex + 1;
                    String tempSequence = entrySequence.substring(tempIndex + 1);

                    while ((tempIndex = tempSequence.indexOf(sequence)) >= 0) {

                        ref += tempIndex;

                        offsetList.add(ref);

                        tempSequence = tempSequence.substring(tempIndex + 1);
                        ref++;

                    }

                    entrySiteOffsets = offsetList.stream()
                            .mapToInt(a -> a)
                            .toArray();

                } else if (entrySequence.length() < sequence.length() && (tempIndex = sequence.indexOf(entrySequence)) >= 0) {

                    scoreOffset = CONFIDENT_RELATED_OFFSET;
                    entrySiteOffsets = new int[]{0};

                    ArrayList<Integer> offsetList = new ArrayList<>(1);
                    offsetList.add(tempIndex);

                    int ref = tempIndex + 1;
                    String tempSequence = sequence.substring(tempIndex + 1);

                    while ((tempIndex = tempSequence.indexOf(entrySequence)) >= 0) {

                        ref += tempIndex;

                        offsetList.add(ref);

                        tempSequence = tempSequence.substring(tempIndex + 1);
                        ref++;

                    }

                    peptideSiteOffsets = offsetList.stream()
                            .mapToInt(a -> a)
                            .toArray();

                }

                if (peptideSiteOffsets != null) {

                    for (long key : entry.getValue()) {

                        if (key != spectrumKey && !processed.contains(key)) {

                            SpectrumMatch tempMatch = identification.getSpectrumMatch(key);
                            PSModificationScores tempScores = (PSModificationScores) tempMatch.getUrParam(PSModificationScores.dummy);

                            for (ModificationMatch modMatch : tempMatch.getBestPeptideAssumption().getPeptide().getVariableModifications()) {

                                if (modMatch.getConfident()) {

                                    String modName = modMatch.getModification();
                                    Modification modification = modificationProvider.getModification(modName);
                                    double tempMass = modification.getMass();

                                    if (modMasses.contains(tempMass)) {

                                        ModificationScoring modificationScoring = tempScores.getModificationScoring(modName);
                                        int site = modMatch.getSite();
                                        double score = modificationScoringParameters.isProbabilisticScoreCalculation() ? modificationScoring.getProbabilisticScore(site) : modificationScoring.getDeltaScore(site);

                                        score += scoreOffset;

                                        HashMap<Integer, Double> tempScoreMap = modificationToSiteToScore.get(tempMass);
                                        HashMap<Integer, String> tempNameMap = modificationToSiteToName.get(tempMass);

                                        for (int siteOffset1 : peptideSiteOffsets) {

                                            for (int siteOffset2 : entrySiteOffsets) {

                                                int siteOnPeptide = site + siteOffset1 - siteOffset2;

                                                if (siteOnPeptide >= 0 && siteOnPeptide <= sequence.length() + 1) {

                                                    Double currentScore = tempScoreMap.get(site);

                                                    if (currentScore == null || currentScore < score) {

                                                        tempScoreMap.put(siteOnPeptide, score);
                                                        tempNameMap.put(siteOnPeptide, modName);
                                                        
                                                        relatedPeptide = true;

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            processed.add(key);

                        }
                    }
                }
            }
        }

        // See if a related peptide was found
        if (relatedPeptide) {

            HashMap<Double, int[]> modificationToPossibleSiteMap = new HashMap<>(modificationMatches.length);
            HashMap<Double, Integer> modificationOccurrenceMap = new HashMap<>(modificationMatches.length);

            // Get the localization scores for this peptide
            for (String modName : modificationScores.getScoredModifications()) {

                Modification modification = modificationProvider.getModification(modName);
                double modMass = modification.getMass();

                HashMap<Integer, String> siteToName = modificationToSiteToName.get(modMass);

                if (siteToName == null) {

                    siteToName = new HashMap<>(2);
                    modificationToSiteToName.put(modMass, siteToName);

                }

                HashMap<Integer, Double> siteToScore = modificationToSiteToScore.get(modMass);

                if (siteToScore == null) {

                    siteToScore = new HashMap<>(2);
                    modificationToSiteToScore.put(modMass, siteToScore);

                }

                ModificationScoring modificationScoring = modificationScores.getModificationScoring(modName);

                if (modificationScoringParameters.isProbabilisticScoreCalculation()) {

                    for (int site : modificationScoring.getProbabilisticSites()) {

                        double score = modificationScoring.getProbabilisticScore(site);
                        Double currentScore = siteToScore.get(site);

                        if (currentScore == null || currentScore < score) {

                            siteToName.put(site, modName);
                            siteToScore.put(site, score);

                        }
                    }

                } else {

                    for (int site : modificationScoring.getDSites()) {

                        double score = modificationScoring.getDeltaScore(site);
                        Double currentScore = siteToScore.get(site);

                        if (currentScore == null || currentScore < score) {

                            siteToName.put(site, modName);
                            siteToScore.put(site, score);

                        }
                    }
                }
            }

            // Get the number of modifications and increase the weight of confidently localized modifications
            for (ModificationMatch modMatch : modificationMatches) {

                String modName = modMatch.getModification();
                Modification modification = modificationProvider.getModification(modName);
                double modMass = modification.getMass();
                modMasses.add(modMass);

                if (modMatch.getConfident()) {

                    HashMap<Integer, Double> siteToScore = modificationToSiteToScore.get(modMass);

                    int site = modMatch.getSite();
                    double score = siteToScore.get(site);

                    siteToScore.put(site, score + CONFIDENT_OWN_OFFSET);

                }

                Integer occurrence = modificationOccurrenceMap.get(modMass);

                if (occurrence == null) {

                    occurrence = 1;

                    modificationOccurrenceMap.put(modMass, occurrence);

                    HashMap<Integer, Double> siteToScore = modificationToSiteToScore.get(modMass);

                    HashSet<Integer> possibleSites = new HashSet<>(siteToScore.keySet());

                    int[] possilbeSitesArray = possibleSites.stream()
                            .mapToInt(a -> a)
                            .toArray();

                    modificationToPossibleSiteMap.put(modMass, possilbeSitesArray);

                } else {

                    modificationOccurrenceMap.put(modMass, occurrence + 1);

                }
            }

            // Map modifications to sites
            HashMap<Double, TreeSet<Integer>> mapping = ModificationPeptideMapping.mapModifications(modificationToPossibleSiteMap, modificationOccurrenceMap, modificationToSiteToScore);

            // Update the modifications of the peptide accordingly
            ModificationMatch[] newModificationMatches = new ModificationMatch[modificationMatches.length];

            int modI = 0;

            for (Entry<Double, TreeSet<Integer>> mappingEntry : mapping.entrySet()) {

                double modMass = mappingEntry.getKey();

                for (int site : mappingEntry.getValue()) {

                    String modName = modificationToSiteToName.get(modMass).get(site);

                    ModificationMatch modificationMatch = new ModificationMatch(modName, site);

                    double score = modificationToSiteToScore.get(modMass).get(site);

                    if (score > CONFIDENT_OWN_OFFSET) {

                        modificationMatch.setConfident(true);

                    } else if (score > CONFIDENT_RELATED_OFFSET) {

                        modificationMatch.setInferred(true);

                    }

                    newModificationMatches[modI] = modificationMatch;
                    modI++;

                }
            }
            
            if (modI < modificationMatches.length) {
                
                throw new IllegalArgumentException(modI + " modifications found where " + modificationMatches.length + " needed.");
                
            }

            peptide.setVariableModifications(newModificationMatches);

        }
    }

    /**
     * Fills the maps of peptides with localized modifications.
     *
     * @param spectrumMatch The spectrum match to inspect.
     * @param confidentPeptideInference The map of peptides with confidently
     * localized sites.
     * @param notConfidentPeptideInference The peptides with non-confidently
     * localized sites.
     * @param modificationParameters The modification parameters.
     * @param modificationProvider The modification provider to use.
     * @param waitingHandler The waiting handler used to provide feedback on
     * progress.
     */
    private void fillConfidentMaps(
            SpectrumMatch spectrumMatch,
            HashMap<Double, HashMap<String, HashSet<Long>>> confidentPeptideInference,
            HashSet<Long> notConfidentPeptideInference,
            ModificationParameters modificationParameters,
            ModificationProvider modificationProvider,
            WaitingHandler waitingHandler
    ) {

        boolean variableAA = false;
        Peptide peptide = spectrumMatch.getBestPeptideAssumption().getPeptide();

        for (ModificationMatch modificationMatch : peptide.getVariableModifications()) {

            String modName = modificationMatch.getModification();
            Modification modification = modificationProvider.getModification(modName);

            if (modification.getModificationType() == ModificationType.modaa) {

                variableAA = true;
                break;

            } else {

                double modMass = modification.getMass();

                for (String otherModName : modificationParameters.getAllNotFixedModifications()) {

                    if (!otherModName.equals(modName)) {

                        Modification otherModification = modificationProvider.getModification(otherModName);

                        if (otherModification.getMass() == modMass
                                && modification.getModificationType()
                                != otherModification.getModificationType()) {

                            variableAA = true;
                            break;

                        }
                    }
                }
            }
        }

        if (variableAA) {

            boolean confident = true;

            for (ModificationMatch modMatch : peptide.getVariableModifications()) {

                String modName = modMatch.getModification();
                Modification modification = modificationProvider.getModification(modName);
                double modMass = modification.getMass();
                boolean maybeNotTerminal = modification.getModificationType() == ModificationType.modaa;

                if (!maybeNotTerminal) {

                    for (String otherModName : modificationParameters.getAllNotFixedModifications()) {

                        if (!otherModName.equals(modName)) {

                            Modification otherModification = modificationProvider.getModification(otherModName);

                            if (otherModification.getMass() == modMass
                                    && modification.getModificationType()
                                    != otherModification.getModificationType()) {

                                maybeNotTerminal = true;
                                break;

                            }
                        }
                    }
                }

                if (maybeNotTerminal) {

                    if (!modMatch.getConfident()) {

                        notConfidentPeptideInference.add(spectrumMatch.getKey());
                        confident = false;

                    } else {

                        HashMap<String, HashSet<Long>> modMap = confidentPeptideInference.get(modMass);

                        if (modMap == null) {

                            modMap = new HashMap<>(2);
                            confidentPeptideInference.put(modMass, modMap);

                        }

                        String sequence = spectrumMatch.getBestPeptideAssumption().getPeptide().getSequence();
                        HashSet<Long> spectra = modMap.get(sequence);

                        if (spectra == null) {

                            spectra = new HashSet<>(2);
                            modMap.put(sequence, spectra);

                        }

                        spectra.add(spectrumMatch.getKey());

                    }
                }
            }

            if (confident) {

                waitingHandler.increaseSecondaryProgressCounter();

            }

        } else {

            waitingHandler.increaseSecondaryProgressCounter();

        }
    }

}
