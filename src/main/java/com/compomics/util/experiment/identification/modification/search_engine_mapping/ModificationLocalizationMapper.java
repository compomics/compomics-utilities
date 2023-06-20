package com.compomics.util.experiment.identification.modification.search_engine_mapping;

import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationProvider;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.modification.ModificationSiteMapping;
import com.compomics.util.experiment.identification.modification.peptide_mapping.ModificationPeptideMapping;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * Function attempting to map modification localization based on their type.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ModificationLocalizationMapper {

    /**
     * Makes an initial modification mapping based on the search engine results
     * and the compatibility to the searched modifications.
     *
     * @param peptide The peptide where the modification was found.
     * @param identificationParameters The identification parameters.
     * @param idfileReader The identification file reader.
     * @param modificationProvider The modification provider to use.
     * @param sequenceProvider The sequence provider to use.
     */
    public static void modificationLocalization(
            Peptide peptide,
            IdentificationParameters identificationParameters,
            IdfileReader idfileReader,
            ModificationProvider modificationProvider,
            SequenceProvider sequenceProvider
    ) {

        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        SequenceMatchingParameters modificationSequenceMatchingParameters = identificationParameters.getModificationLocalizationParameters().getSequenceMatchingParameters();

        // Gather the site, occurrences, possible modifications and their sites
        HashMap<Double, Integer> modificationOccurrenceMap = new HashMap<>(1);
        HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore = new HashMap<>(1);
        HashMap<Double, HashMap<Integer, TreeSet<String>>> possibleModificationToPossibleSiteToName = new HashMap<>(1);

        double maxDistance = peptide.getSequence().length();

        ModificationMatch[] originalModifications = peptide.getVariableModifications();

        for (ModificationMatch modificationMatch : originalModifications) {

            int modSite = modificationMatch.getSite();

            double searchEngineMass = ModificationMassMapper.getMass(
                    modificationMatch.getModification(),
                    idfileReader,
                    searchParameters,
                    modificationProvider
            );

            Integer occurrence = modificationOccurrenceMap.get(searchEngineMass);

            if (occurrence == null) {

                modificationOccurrenceMap.put(searchEngineMass, 1);

            } else {

                modificationOccurrenceMap.put(searchEngineMass, occurrence + 1);

            }

            HashMap<Integer, TreeSet<String>> possibleSites = possibleModificationToPossibleSiteToName.get(searchEngineMass);

            if (possibleSites == null) {

                possibleSites = new HashMap<>(1);
                possibleModificationToPossibleSiteToName.put(searchEngineMass, possibleSites);

            }

            HashMap<Integer, Double> siteToScore = modificationToSiteToScore.get(searchEngineMass);

            if (siteToScore == null) {

                siteToScore = new HashMap<>(1);
                modificationToSiteToScore.put(searchEngineMass, siteToScore);

            }

            HashMap<Integer, HashSet<String>> tempNames = ModificationNameMapper.getPossibleModificationNames(
                    peptide,
                    modificationMatch,
                    idfileReader,
                    searchParameters,
                    modificationSequenceMatchingParameters,
                    sequenceProvider,
                    modificationProvider
            );

            if (tempNames.isEmpty()) {

                throw new IllegalArgumentException("Could not map modification " + modificationMatch.getModification() + " on peptide " + peptide.getSequence() + ".");

            }

            for (Entry<Integer, HashSet<String>> entry : tempNames.entrySet()) {

                int possibleSite = entry.getKey();

                for (String possibleModificationName : entry.getValue()) {

                    TreeSet<String> possibleModificationNames = possibleSites.get(possibleSite);

                    if (possibleModificationNames == null) {

                        possibleModificationNames = new TreeSet<>();
                        possibleSites.put(possibleSite, possibleModificationNames);

                    }

                    possibleModificationNames.add(possibleModificationName);

                }

                // Give a score that decreases with the distance to the original sites
                double score = 1.0 - ((possibleSite - modSite) * (possibleSite - modSite) / (maxDistance * maxDistance));
                siteToScore.put(possibleSite, score);

            }
        }

        HashMap<Double, int[]> modificationToPossibleSiteMap = new HashMap<>(possibleModificationToPossibleSiteToName.size());

        for (Entry<Double, HashMap<Integer, TreeSet<String>>> entry : possibleModificationToPossibleSiteToName.entrySet()) {

            int[] sites = entry.getValue().keySet().stream()
                    .mapToInt(
                            a -> a
                    )
                    .toArray();

            modificationToPossibleSiteMap.put(entry.getKey(), sites);

        }

        // Find the combination of modifications that best suits the input from the search engines
        HashMap<Double, TreeSet<Integer>> matchedSiteByModification = ModificationPeptideMapping.mapModifications(
                modificationToPossibleSiteMap,
                modificationOccurrenceMap,
                modificationToSiteToScore
        );

        // Create new modification matches
        ModificationMatch[] newModifications = new ModificationMatch[originalModifications.length];

        int modificationI = 0;

        for (Entry<Double, TreeSet<Integer>> entry
                : matchedSiteByModification.entrySet()) {

            double searchEngineMass = entry.getKey();
            HashMap<Integer, TreeSet<String>> possibleSites = possibleModificationToPossibleSiteToName.get(searchEngineMass);

            for (int site : entry.getValue()) {

                TreeSet<String> possibleNames = possibleSites.get(site);

                String bestName = null;
                double massDifference = Double.NaN;

                for (String possibleName : possibleNames) {

                    Modification modification = modificationProvider.getModification(possibleName);
                    double currentMassDifference = Math.abs(modification.getMass() - searchEngineMass);

                    if (bestName == null || currentMassDifference < massDifference) {

                        bestName = possibleName;
                        massDifference = currentMassDifference;

                    }
                }

                ModificationMatch modificationMatch = new ModificationMatch(bestName, site);

                newModifications[modificationI] = modificationMatch;

                modificationI++;

            }
        }

        if (modificationI < originalModifications.length) {

            throw new IllegalArgumentException("Could map only " + modificationI + " in " + originalModifications.length + ".");

        }

        peptide.setVariableModifications(newModifications);

    }
}
