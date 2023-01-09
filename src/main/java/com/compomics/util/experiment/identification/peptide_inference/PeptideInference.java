package com.compomics.util.experiment.identification.peptide_inference;

import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationProvider;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches_iterators.SpectrumMatchesIterator;
import com.compomics.util.experiment.identification.modification.ModificationSiteMapping;
import com.compomics.util.experiment.identification.peptide_shaker.PSModificationScores;
import com.compomics.util.experiment.identification.utils.ModificationUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Functions related to peptide inference.
 *
 * @author Marc Vaudel
 */
public class PeptideInference {

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

        SequenceMatchingParameters modificationSequenceMatchingParameters = identificationParameters.getModificationLocalizationParameters().getSequenceMatchingParameters();
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        ModificationParameters modificationParameters = searchParameters.getModificationParameters();

        // PSMs with confidently localized PTMs in a map: PTM mass -> peptide sequence -> spectrum keys
        HashMap<Double, HashMap<String, HashSet<Long>>> confidentPeptideInference = new HashMap<>();
        // PSMs with ambiguously localized PTMs in a map: File -> PTM mass -> spectrum keys
        HashMap<Double, HashSet<Long>> notConfidentPeptideInference = new HashMap<>();

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

        HashSet<Long> progress = new HashSet<>();

        for (Map.Entry<Double, HashSet<Long>> entry : notConfidentPeptideInference.entrySet()) {

            double modMass = entry.getKey();

            for (long spectrumKey : entry.getValue()) {

                peptideInference(
                        modMass,
                        spectrumKey,
                        confidentPeptideInference,
                        identification,
                        searchParameters,
                        modificationSequenceMatchingParameters,
                        sequenceProvider,
                        modificationProvider
                );

                if (waitingHandler.isRunCanceled()) {

                    return;

                }

                if (!progress.contains(spectrumKey)) {

                    progress.add(spectrumKey);
                    waitingHandler.increaseSecondaryProgressCounter();

                }
            }
        }
    }

    /**
     * Infers the PTM localization and its confidence for the best match of the
     * given spectrum for PTMs of the (exact) given mass.
     *
     * @param modMass The mass of the modifications to inspect.
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
            double modMass,
            long spectrumKey,
            HashMap<Double, HashMap<String, HashSet<Long>>> confidentPeptideInference,
            Identification identification,
            SearchParameters searchParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider
    ) {

        SpectrumMatch spectrumMatch = identification.getSpectrumMatch(spectrumKey);
        
        if (spectrumMatch.getSpectrumTitle().equals("File: \"E:\\Data\\H31 std\\20220323-1321_ECLIP_1001749_H31std_mix1_15pmol_rep2.raw\"; SpectrumID: \"7615\"; PrecursorID: \"0\"; scans: \"3592\"")) {
            
            int debug = 1;
            
        }

        Peptide peptide = spectrumMatch.getBestPeptideAssumption().getPeptide();
        String sequence = peptide.getSequence();

        int nMod = getNMod(
                peptide,
                modMass,
                modificationProvider
        );

        HashSet<Integer> oldLocalization = getModificationSites(peptide, modMass, modificationProvider);
        HashSet<Integer> newLocalizationCandidates = new HashSet<>(nMod - oldLocalization.size());
        HashMap<String, HashSet<Long>> modConfidentPeptides = confidentPeptideInference.get(modMass);

        if (modConfidentPeptides != null) {

            // See if we can explain this peptide by another already identified peptide 
            // with the same number of modifications (the two peptides will be merged)
            findConfidentPeptideAndMerge(
                    oldLocalization,
                    newLocalizationCandidates,
                    sequence,
                    modMass,
                    nMod,
                    modConfidentPeptides,
                    identification,
                    modificationProvider
            );

            if (oldLocalization.size() + newLocalizationCandidates.size() < nMod) {

                // There are still unexplained sites, let's see if we find a related peptide that can help.
                findRelatedPeptides(
                        oldLocalization,
                        newLocalizationCandidates,
                        peptide,
                        modMass,
                        confidentPeptideInference,
                        identification,
                        searchParameters,
                        modificationSequenceMatchingParameters,
                        sequenceProvider,
                        modificationProvider
                );
            }

            // Map the most likely inferred sites
            if (!newLocalizationCandidates.isEmpty()) {

                mapInferredSites(
                        spectrumMatch,
                        peptide,
                        newLocalizationCandidates,
                        modMass,
                        nMod,
                        searchParameters,
                        modificationSequenceMatchingParameters,
                        sequenceProvider,
                        modificationProvider
                );
            }
        }
    }

    /**
     * Returns the modification sites for the modifications of the given mass on
     * the given peptide.
     *
     * @param peptide The peptide of interest.
     * @param modMass The mass of the modifications of interest.
     * @param modificationProvider The modification provider to use.
     *
     * @return The modification sites for the modifications of the given mass on
     * the given peptide.
     */
    private HashSet<Integer> getModificationSites(
            Peptide peptide,
            double modMass,
            ModificationProvider modificationProvider
    ) {
        return Arrays.stream(peptide.getVariableModifications())
                .filter(
                        modificationMatch -> modificationMatch.getConfident() || modificationMatch.getInferred()
                )
                .filter(
                        modificationMatch -> modificationProvider.getModification(modificationMatch.getModification()).getMass() == modMass
                )
                .map(
                        ModificationMatch::getSite
                )
                .collect(
                        Collectors.toCollection(HashSet::new)
                );
    }

    /**
     * Returns the number of modifications of the given mass on the given
     * peptide.
     *
     * @param peptide The peptide of interest.
     * @param modMass The mass of the modifications of interest.
     * @param modificationProvider The modification provider to use.
     *
     * @return The number of modifications of the given mass on the given
     * peptide.
     */
    private int getNMod(
            Peptide peptide,
            double modMass,
            ModificationProvider modificationProvider
    ) {
        return (int) Arrays.stream(peptide.getVariableModifications())
                .map(
                        modificationMatch -> modificationProvider.getModification(modificationMatch.getModification())
                )
                .filter(
                        modification -> modification.getMass() == modMass
                )
                .count();
    }

    /**
     * Find whether peptides with the same sequence and confidently localized
     * modifications can be used to infer the localization of non-confident
     * modification sites, and merge the peptides if one explains another.
     *
     * @param oldLocalization The list of previous modification sites.
     * @param newLocalizationCandidates The list of new modification sites.
     * @param sequence The peptide sequence.
     * @param modMass The mass of the modification of interest.
     * @param nMod The number of modifications of the given mass.
     * @param modConfidentPeptides Map of the keys of peptides with confidently
     * localized modifications.
     * @param identification The identification object containing the matches.
     * @param modificationProvider The modification provider to use.
     */
    private void findConfidentPeptideAndMerge(
            HashSet<Integer> oldLocalization,
            HashSet<Integer> newLocalizationCandidates,
            String sequence,
            double modMass,
            int nMod,
            HashMap<String, HashSet<Long>> modConfidentPeptides,
            Identification identification,
            ModificationProvider modificationProvider
    ) {

        HashSet<Long> keys = modConfidentPeptides.get(sequence);

        if (keys != null) {

            for (long tempKey : keys) {

                SpectrumMatch secondaryMatch = identification.getSpectrumMatch(tempKey);
                Peptide tempPeptide = secondaryMatch.getBestPeptideAssumption().getPeptide();

                long tempNMod = Arrays.stream(tempPeptide.getVariableModifications())
                        .map(
                                modificationMatch -> modificationProvider.getModification(modificationMatch.getModification())
                        )
                        .filter(
                                modification -> modification.getMass() == modMass
                        )
                        .count();

                if (tempNMod == nMod) {

                    ArrayList<Integer> tempLocalizations = Arrays.stream(tempPeptide.getVariableModifications())
                            .filter(
                                    modificationMatch -> modificationMatch.getConfident() || modificationMatch.getInferred()
                            )
                            .filter(
                                    modificationMatch -> modificationProvider.getModification(modificationMatch.getModification()).getMass() == modMass
                            )
                            .map(
                                    ModificationMatch::getSite
                            )
                            .collect(
                                    Collectors.toCollection(ArrayList::new)
                            );

                    for (int localization : tempLocalizations) {

                        if (!oldLocalization.contains(localization) && !newLocalizationCandidates.contains(localization)) {

                            newLocalizationCandidates.add(localization);

                        }
                    }
                }
            }

            if (oldLocalization.size() + newLocalizationCandidates.size() < nMod) {

                // we cannot merge this peptide, see whether we can explain 
                // the remaining modifications using peptides with the same 
                // sequence but other modification profile
                for (long tempKey : keys) {

                    SpectrumMatch secondaryMatch = (SpectrumMatch) identification.retrieveObject(tempKey);
                    Peptide tempPeptide = secondaryMatch.getBestPeptideAssumption().getPeptide();

                    ArrayList<Integer> tempLocalizations = Arrays.stream(tempPeptide.getVariableModifications())
                            .filter(
                                    modificationMatch -> modificationMatch.getConfident() || modificationMatch.getInferred()
                            )
                            .filter(
                                    modificationMatch -> modificationProvider.getModification(modificationMatch.getModification()).getMass() == modMass
                            )
                            .map(
                                    ModificationMatch::getSite
                            )
                            .collect(
                                    Collectors.toCollection(ArrayList::new)
                            );

                    for (int localization : tempLocalizations) {

                        if (!oldLocalization.contains(localization) && !newLocalizationCandidates.contains(localization)) {

                            newLocalizationCandidates.add(localization);

                        }
                    }
                }
            }
        }
    }

    /**
     * Find whether peptides with the overlapping sequence and confidently
     * localized modifications can be used to infer the localization of
     * non-confident modification sites.
     *
     * @param oldLocalization The list of previous modification sites.
     * @param newLocalizationCandidates The list of new modification sites.
     * @param peptide The peptide of interest.
     * @param modMass The mass of the modification of interest.
     * @param confidentPeptideInference Map of the keys of peptides with
     * confidently localized modifications.
     * @param identification The identification object containing the matches.
     * @param searchParameters The search parameters.
     * @param modificationSequenceMatchingParameters The modification sequence
     * matching parameters.
     * @param sequenceProvider The sequence provider to use.
     * @param modificationProvider The modification provider to use.
     */
    private void findRelatedPeptides(
            HashSet<Integer> oldLocalization,
            HashSet<Integer> newLocalizationCandidates,
            Peptide peptide,
            double modMass,
            HashMap<Double, HashMap<String, HashSet<Long>>> confidentPeptideInference,
            Identification identification,
            SearchParameters searchParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider
    ) {

        String sequence = peptide.getSequence();

        HashMap<String, HashSet<Long>> confidentAtMass = confidentPeptideInference.get(modMass);

        for (String otherSequence : confidentAtMass.keySet()) {

            if (!sequence.equals(otherSequence) && sequence.contains(otherSequence)) {

                for (long tempKey : confidentAtMass.get(otherSequence)) {

                    SpectrumMatch secondaryMatch = (SpectrumMatch) identification.retrieveObject(tempKey);
                    Peptide tempPeptide = secondaryMatch.getBestPeptideAssumption().getPeptide();

                    ArrayList<Integer> tempLocalizations = Arrays.stream(tempPeptide.getVariableModifications())
                            .filter(
                                    modificationMatch -> modificationMatch.getConfident() || modificationMatch.getInferred()
                            )
                            .filter(
                                    modificationMatch -> modificationProvider.getModification(modificationMatch.getModification()).getMass() == modMass
                            )
                            .map(
                                    ModificationMatch::getSite
                            )
                            .collect(
                                    Collectors.toCollection(ArrayList::new)
                            );

                    int tempIndex, ref = 0;
                    String tempSequence = sequence;

                    while ((tempIndex = tempSequence.indexOf(otherSequence)) >= 0) {

                        ref += tempIndex;

                        for (int localization : tempLocalizations) {

                            int shiftedLocalization = ref + localization;

                            if (!oldLocalization.contains(shiftedLocalization) && !newLocalizationCandidates.contains(shiftedLocalization)) {

                                boolean siteOccupied = false;

                                for (ModificationMatch modificationMatch : peptide.getVariableModifications()) {

                                    Modification modification = modificationProvider.getModification(modificationMatch.getModification());

                                    if (modification.getMass() != modMass && modificationMatch.getSite() == shiftedLocalization) {

                                        siteOccupied = true;

                                    }
                                }

                                boolean candidatePtm = false;

                                if (!siteOccupied) {

                                    for (String modName : searchParameters.getModificationParameters().getAllNotFixedModifications()) {

                                        Modification modification = modificationProvider.getModification(modName);

                                        if (modification.getMass() == modMass) {

                                            int[] possibleSites = ModificationUtils.getPossibleModificationSites(peptide, modification, sequenceProvider, modificationSequenceMatchingParameters);

                                            if (Arrays.stream(possibleSites).anyMatch(site -> site == shiftedLocalization)) {

                                                candidatePtm = true;
                                                break;

                                            }
                                        }
                                    }
                                }

                                if (candidatePtm && !siteOccupied) {

                                    newLocalizationCandidates.add(shiftedLocalization);

                                }
                            }
                        }

                        tempSequence = tempSequence.substring(tempIndex + 1);
                        ref++;

                    }
                }

            } else if (!sequence.equals(otherSequence) && otherSequence.contains(sequence)) {

                for (long tempKey : confidentAtMass.get(otherSequence)) {

                    SpectrumMatch secondaryMatch = identification.getSpectrumMatch(tempKey);

                    Peptide tempPeptide = secondaryMatch.getBestPeptideAssumption().getPeptide();

                    ArrayList<Integer> tempLocalizations = Arrays.stream(tempPeptide.getVariableModifications())
                            .filter(
                                    modificationMatch -> modificationMatch.getConfident() || modificationMatch.getInferred()
                            )
                            .filter(
                                    modificationMatch -> modificationProvider.getModification(modificationMatch.getModification()).getMass() == modMass
                            )
                            .map(
                                    ModificationMatch::getSite
                            )
                            .collect(
                                    Collectors.toCollection(ArrayList::new)
                            );

                    int tempIndex, ref = 0;
                    String tempSequence = otherSequence;

                    while ((tempIndex = tempSequence.indexOf(sequence)) >= 0) {

                        ref += tempIndex;

                        for (int localization : tempLocalizations) {

                            int shiftedLocalization = localization - ref;

                            if (shiftedLocalization > 0 && shiftedLocalization <= sequence.length()
                                    && !oldLocalization.contains(shiftedLocalization) && !newLocalizationCandidates.contains(shiftedLocalization)) {

                                boolean siteOccupied = false;

                                for (ModificationMatch modificationMatch : peptide.getVariableModifications()) {

                                    Modification modification = modificationProvider.getModification(modificationMatch.getModification());

                                    if (modification.getMass() != modMass && modificationMatch.getSite() == shiftedLocalization) {

                                        siteOccupied = true;

                                    }
                                }

                                boolean candidateModification = false;

                                if (!siteOccupied) {

                                    for (String modName : searchParameters.getModificationParameters().getAllNotFixedModifications()) {

                                        Modification modification = modificationProvider.getModification(modName);

                                        if (modification.getMass() == modMass) {

                                            int[] possibleSites = ModificationUtils.getPossibleModificationSites(
                                                    peptide,
                                                    modification,
                                                    sequenceProvider,
                                                    modificationSequenceMatchingParameters
                                            );

                                            if (Arrays.stream(possibleSites).anyMatch(site -> site == shiftedLocalization)) {

                                                candidateModification = true;
                                                break;

                                            }
                                        }
                                    }
                                }

                                if (candidateModification && !siteOccupied) {

                                    newLocalizationCandidates.add(shiftedLocalization);

                                }
                            }
                        }

                        tempSequence = tempSequence.substring(tempIndex + 1);
                        ref++;

                    }
                }
            }
        }
    }

    /**
     * Selects modification sites among the possible new localization.
     *
     * @param spectrumMatch The spectrum match.
     * @param peptide The peptide of interest.
     * @param newLocalizationCandidates The new localization sites.
     * @param modMass The mass of the modification to
     * @param nMod The number of modifications on the peptide with this mass.
     * @param searchParameters The search parameters.
     * @param modificationSequenceMatchingParameters The modification sequence
     * matching parameters.
     * @param sequenceProvider The sequence provider to use.
     * @param modificationProvider The modification provider to use.
     */
    private synchronized void mapInferredSites(
            SpectrumMatch spectrumMatch,
            Peptide peptide,
            HashSet<Integer> newLocalizationCandidates,
            double modMass,
            int nMod,
            SearchParameters searchParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider
    ) {

        HashMap<Integer, ModificationMatch> nonConfidentMatches = new HashMap<>();

        for (ModificationMatch modificationMatch : peptide.getVariableModifications()) {

            String modName = modificationMatch.getModification();
            Modification modification = modificationProvider.getModification(modName);

            if (!modificationMatch.getConfident() && modification.getMass() == modMass) {

                nonConfidentMatches.put(modificationMatch.getSite(), modificationMatch);

            }
        }

        HashMap<Integer, Integer> mapping = ModificationSiteMapping.align(
                nonConfidentMatches.keySet(),
                newLocalizationCandidates
        );

        for (Integer oldLocalization : mapping.keySet()) {

            ModificationMatch modificationMatch = nonConfidentMatches.get(oldLocalization);
            Integer newLocalization = mapping.get(oldLocalization);

            if (modificationMatch == null) {

                throw new IllegalArgumentException(
                        "No modification match found at site "
                        + oldLocalization
                        + " in spectrum "
                        + spectrumMatch.getKey()
                        + "."
                );

            }

            if (newLocalization != null) {

                if (!newLocalization.equals(oldLocalization)) {

                    String candidateName = null;

                    for (String modName : searchParameters.getModificationParameters().getAllNotFixedModifications()) {

                        Modification modification = modificationProvider.getModification(modName);

                        if (modification.getMass() == modMass) {

                            int[] possibleSites = ModificationUtils.getPossibleModificationSites(
                                    peptide,
                                    modification,
                                    sequenceProvider,
                                    modificationSequenceMatchingParameters
                            );

                            if (Arrays.stream(possibleSites).anyMatch(site -> site == newLocalization)) {

                                candidateName = modification.getName();
                                break;
                            }
                        }
                    }

                    if (candidateName == null) {

                        throw new IllegalArgumentException(
                                "No PTM found for site "
                                + newLocalization
                                + " on  peptide "
                                + peptide.getSequence()
                                + " in spectrum "
                                + spectrumMatch.getKey()
                                + "."
                        );

                    }

                    String previousName = modificationMatch.getModification();
                    modificationMatch.setSite(newLocalization);
                    modificationMatch.setModification(candidateName);
                    PSModificationScores psmScores = (PSModificationScores) spectrumMatch.getUrParam(PSModificationScores.dummy);

                    psmScores.changeRepresentativeSite(
                            candidateName,
                            previousName,
                            oldLocalization,
                            newLocalization,
                            nMod,
                            modificationProvider
                    );

                }

                modificationMatch.setInferred(true);

            }
        }
    }

    /**
     * Fills the maps of peptides with localized modifications.
     *
     * @param spectrumMatch The spectrum match to inspect.
     * @param confidentPeptideInference The map of peptides with confidently
     * localized sites.
     * @param notConfidentPeptideInference The map of peptides with
     * non-confidently localized sites.
     * @param modificationParameters The modification parameters.
     * @param modificationProvider The modification provider to use.
     * @param waitingHandler The waiting handler used to provide feedback on
     * progress.
     */
    private void fillConfidentMaps(
            SpectrumMatch spectrumMatch,
            HashMap<Double, HashMap<String, HashSet<Long>>> confidentPeptideInference,
            HashMap<Double, HashSet<Long>> notConfidentPeptideInference,
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

                        HashSet<Long> spectra = notConfidentPeptideInference.get(modMass);

                        if (spectra == null) {

                            spectra = new HashSet<>(2);
                            notConfidentPeptideInference.put(modMass, spectra);

                        }

                        spectra.add(spectrumMatch.getKey());
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
