package com.compomics.util.experiment.identification.modification.mapping;

import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationProvider;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.modification.ModificationSiteMapping;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Function attempting to map modification localization based on their type.
 *
 * @author Marc Vaudel
 */
public class ModificationLocalizationMapper {

    /**
     * The mass added per amino acid as part of the reference mass when
     * converting Dalton tolerances to ppm.
     */
    public static final double MASS_PER_AA = 100.0;

    /**
     * Makes an initial modification mapping based on the search engine results
     * and the compatibility to the searched modifications.
     *
     * @param peptide The peptide where the modification was found.
     * @param expectedNames The expected modifications at each site.
     * @param modNames The possible names for every modification match.
     * @param identificationParameters The identification parameters.
     * @param idfileReader The identification file reader.
     * @param modificationProvider The modification provider to use.
     */
    public static void modificationLocalization(
            Peptide peptide,
            HashMap<Integer, ArrayList<String>> expectedNames,
            HashMap<ModificationMatch, ArrayList<String>> modNames,
            IdentificationParameters identificationParameters,
            IdfileReader idfileReader,
            ModificationProvider modificationProvider
    ) {

        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        ModificationParameters modificationParameters = searchParameters.getModificationParameters();

        int peptideLength = peptide.getSequence().length();

        // If a terminal modification cannot be elsewhere lock the terminus
        ModificationMatch nTermModification = null;
        ModificationMatch[] modificationMatches = peptide.getVariableModifications();

        for (ModificationMatch modMatch : modificationMatches) {

            double refMass = ModificationMassMapper.getMass(
                    modMatch.getModification(),
                    idfileReader,
                    searchParameters,
                    modificationProvider
            );
            int modSite = modMatch.getSite();

            if (modSite == 1) {

                ArrayList<String> expectedNamesAtSite = expectedNames.get(modSite);

                if (expectedNamesAtSite != null) {

                    ArrayList<String> filteredNamesAtSite = new ArrayList<>(expectedNamesAtSite.size());

                    for (String modName : expectedNamesAtSite) {

                        Modification modification = modificationProvider.getModification(modName);

                        if (Math.abs(modification.getMass() - refMass) < searchParameters.getFragmentIonAccuracyInDaltons(MASS_PER_AA * peptideLength)) {

                            filteredNamesAtSite.add(modName);

                        }
                    }

                    for (String modName : filteredNamesAtSite) {

                        Modification modification = modificationProvider.getModification(modName);

                        if (modification.getModificationType().isNTerm()) {

                            boolean otherPossibleMod = false;

                            for (String tempName : modificationParameters.getAllNotFixedModifications()) {

                                if (!tempName.equals(modName)) {

                                    Modification tempModification = modificationProvider.getModification(tempName);

                                    if (tempModification.getMass() == modification.getMass() && !tempModification.getModificationType().isNTerm()) {

                                        otherPossibleMod = true;
                                        break;

                                    }
                                }
                            }

                            if (!otherPossibleMod) {

                                nTermModification = modMatch;
                                modMatch.setModification(modName);
                                break;

                            }
                        }
                    }

                    if (nTermModification != null) {

                        break;

                    }
                }
            }
        }

        ModificationMatch cTermModification = null;

        for (ModificationMatch modMatch : peptide.getVariableModifications()) {

            if (modMatch != nTermModification) {

                double refMass = ModificationMassMapper.getMass(
                        modMatch.getModification(),
                        idfileReader,
                        searchParameters,
                        modificationProvider
                );
                int modSite = modMatch.getSite();

                if (modSite == peptideLength) {

                    ArrayList<String> expectedNamesAtSite = expectedNames.get(modSite);

                    if (expectedNamesAtSite != null) {

                        ArrayList<String> filteredNamesAtSite = new ArrayList<>(expectedNamesAtSite.size());

                        for (String modName : expectedNamesAtSite) {

                            Modification modification = modificationProvider.getModification(modName);

                            if (Math.abs(modification.getMass() - refMass) < searchParameters.getFragmentIonAccuracyInDaltons(MASS_PER_AA * peptideLength)) {

                                filteredNamesAtSite.add(modName);

                            }
                        }

                        for (String modName : filteredNamesAtSite) {

                            Modification modification = modificationProvider.getModification(modName);

                            if (modification.getModificationType().isCTerm()) {

                                boolean otherPossibleMod = false;

                                for (String tempName : modificationParameters.getAllNotFixedModifications()) {

                                    if (!tempName.equals(modName)) {

                                        Modification tempModification = modificationProvider.getModification(tempName);

                                        if (tempModification.getMass() == modification.getMass() && !tempModification.getModificationType().isCTerm()) {

                                            otherPossibleMod = true;
                                            break;

                                        }
                                    }
                                }

                                if (!otherPossibleMod) {

                                    cTermModification = modMatch;
                                    modMatch.setModification(modName);
                                    break;

                                }
                            }
                        }

                        if (cTermModification != null) {

                            break;

                        }
                    }
                }
            }
        }

        // Map the modifications according to search engine localization
        HashMap<Integer, ArrayList<String>> siteToModMap = new HashMap<>(modificationMatches.length); // Site to ptm name including termini
        HashMap<Integer, ModificationMatch> siteToMatchMap = new HashMap<>(modificationMatches.length); // Site to Modification match excluding termini
        HashMap<ModificationMatch, Integer> matchToSiteMap = new HashMap<>(modificationMatches.length); // Modification match to site excluding termini
        boolean allMapped = true;

        for (ModificationMatch modMatch : modificationMatches) {

            boolean mapped = false;

            if (modMatch != nTermModification && modMatch != cTermModification) {

                double refMass = ModificationMassMapper.getMass(
                        modMatch.getModification(),
                        idfileReader,
                        searchParameters,
                        modificationProvider
                );
                int modSite = modMatch.getSite();
                boolean terminal = false;
                ArrayList<String> expectedNamesAtSite = expectedNames.get(modSite);

                if (expectedNamesAtSite != null) {

                    ArrayList<String> filteredNamesAtSite = new ArrayList<>(expectedNamesAtSite.size());
                    ArrayList<String> modificationAtSite = siteToModMap.get(modSite);

                    for (String modName : expectedNamesAtSite) {

                        Modification modification = modificationProvider.getModification(modName);

                        if (Math.abs(modification.getMass() - refMass) < searchParameters.getFragmentIonAccuracyInDaltons(MASS_PER_AA * peptideLength)
                                && (modificationAtSite == null || !modificationAtSite.contains(modName))) {

                            filteredNamesAtSite.add(modName);

                        }
                    }

                    if (filteredNamesAtSite.size() == 1) {

                        String modName = filteredNamesAtSite.get(0);
                        Modification modification = modificationProvider.getModification(modName);
                        ModificationType modificationType = modification.getModificationType();

                        if (modificationType.isNTerm() && nTermModification == null) {

                            nTermModification = modMatch;
                            mapped = true;

                        } else if (modificationType.isCTerm() && cTermModification == null) {

                            cTermModification = modMatch;
                            mapped = true;

                        } else if (!modificationType.isNTerm() && !modificationType.isCTerm()) {

                            matchToSiteMap.put(modMatch, modSite);
                            siteToMatchMap.put(modSite, modMatch);
                            mapped = true;

                        }

                        if (mapped) {

                            modMatch.setModification(modName);

                            if (modificationAtSite == null) {

                                modificationAtSite = new ArrayList<>(1);
                                siteToModMap.put(modSite, modificationAtSite);

                            }

                            modificationAtSite.add(modName);

                        }
                    }

                    if (!mapped) {

                        if (filteredNamesAtSite.isEmpty()) {

                            filteredNamesAtSite = expectedNamesAtSite;

                        }

                        if (modSite == 1) {

                            Double minDiff = null;
                            String bestPtmName = null;

                            for (String modName : filteredNamesAtSite) {

                                Modification modification = modificationProvider.getModification(modName);

                                if (modification.getModificationType().isNTerm() && nTermModification == null) {

                                    double massError = Math.abs(refMass - modification.getMass());

                                    if (massError <= searchParameters.getFragmentIonAccuracyInDaltons(MASS_PER_AA * peptideLength)
                                            && (minDiff == null || massError < minDiff)) {

                                        bestPtmName = modName;
                                        minDiff = massError;

                                    }
                                }
                            }

                            if (bestPtmName != null) {

                                nTermModification = modMatch;
                                modMatch.setModification(bestPtmName);
                                terminal = true;

                                if (modificationAtSite == null) {

                                    modificationAtSite = new ArrayList<>(1);
                                    siteToModMap.put(modSite, modificationAtSite);

                                }

                                modificationAtSite.add(bestPtmName);
                                mapped = true;

                            }

                        } else if (modSite == peptideLength) {

                            Double minDiff = null;
                            String bestModName = null;

                            for (String modName : filteredNamesAtSite) {

                                Modification modification = modificationProvider.getModification(modName);

                                if (modification.getModificationType().isCTerm() && cTermModification == null) {

                                    double massError = Math.abs(refMass - modification.getMass());

                                    if (massError <= searchParameters.getFragmentIonAccuracyInDaltons(MASS_PER_AA * peptideLength)
                                            && (minDiff == null || massError < minDiff)) {

                                        bestModName = modName;
                                        minDiff = massError;

                                    }
                                }
                            }

                            if (bestModName != null) {

                                cTermModification = modMatch;
                                modMatch.setModification(bestModName);
                                terminal = true;

                                if (modificationAtSite == null) {

                                    modificationAtSite = new ArrayList<>(1);
                                    siteToModMap.put(modSite, modificationAtSite);

                                }

                                modificationAtSite.add(bestModName);
                                mapped = true;

                            }
                        }

                        if (!terminal) {

                            Double minDiff = null;
                            String bestModName = null;

                            for (String modName : filteredNamesAtSite) {

                                Modification modification = modificationProvider.getModification(modName);
                                ModificationType modificationType = modification.getModificationType();

                                if (!modificationType.isCTerm() && !modificationType.isNTerm() && modNames.get(modMatch).contains(modName) && !siteToMatchMap.containsKey(modSite)) {

                                    double massError = Math.abs(refMass - modification.getMass());

                                    if (massError <= searchParameters.getFragmentIonAccuracyInDaltons(MASS_PER_AA * peptideLength)
                                            && (minDiff == null || massError < minDiff)) {

                                        bestModName = modName;
                                        minDiff = massError;

                                    }
                                }
                            }

                            if (bestModName != null) {

                                modMatch.setModification(bestModName);

                                if (modificationAtSite == null) {

                                    modificationAtSite = new ArrayList<>(1);
                                    siteToModMap.put(modSite, modificationAtSite);

                                }

                                modificationAtSite.add(bestModName);
                                matchToSiteMap.put(modMatch, modSite);
                                siteToMatchMap.put(modSite, modMatch);
                                mapped = true;

                            }
                        }
                    }
                }
            }

            if (!mapped) {

                allMapped = false;

            }
        }

        if (!allMapped) {

            // Try to correct incompatible localizations
            HashMap<Integer, ArrayList<Integer>> remap = new HashMap<>(0);

            for (ModificationMatch modMatch : peptide.getVariableModifications()) {

                if (modMatch != nTermModification && modMatch != cTermModification && !matchToSiteMap.containsKey(modMatch)) {

                    int modSite = modMatch.getSite();

                    for (int candidateSite : expectedNames.keySet()) {

                        if (!siteToMatchMap.containsKey(candidateSite)) {

                            for (String modName : expectedNames.get(candidateSite)) {

                                if (modNames.get(modMatch).contains(modName)) {

                                    Modification modification = modificationProvider.getModification(modName);
                                    ModificationType modificationType = modification.getModificationType();

                                    if ((!modificationType.isCTerm() || cTermModification == null)
                                            && (!modificationType.isNTerm() || nTermModification == null)) {

                                        ArrayList<Integer> modSites = remap.get(modSite);

                                        if (modSites == null) {

                                            modSites = new ArrayList<>(2);
                                            remap.put(modSite, modSites);

                                        }

                                        if (!modSites.contains(candidateSite)) {

                                            modSites.add(candidateSite);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HashMap<Integer, Integer> correctedIndexes = ModificationSiteMapping.alignAll(remap);

            for (ModificationMatch modMatch : peptide.getVariableModifications()) {

                if (modMatch != nTermModification && modMatch != cTermModification && !matchToSiteMap.containsKey(modMatch)) {

                    Integer modSite = correctedIndexes.get(modMatch.getSite());

                    if (modSite != null) {

                        if (expectedNames.containsKey(modSite)) {

                            for (String modName : expectedNames.get(modSite)) {

                                if (modNames.get(modMatch).contains(modName)) {

                                    ArrayList<String> taken = siteToModMap.get(modSite);

                                    if (taken == null || !taken.contains(modName)) {

                                        matchToSiteMap.put(modMatch, modSite);
                                        modMatch.setModification(modName);
                                        modMatch.setSite(modSite);

                                        if (taken == null) {

                                            taken = new ArrayList<>(1);
                                            siteToModMap.put(modSite, taken);

                                        }

                                        taken.add(modName);
                                        break;

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
