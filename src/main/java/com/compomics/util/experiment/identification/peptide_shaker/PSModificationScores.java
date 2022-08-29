package com.compomics.util.experiment.identification.peptide_shaker;

import com.compomics.util.experiment.biology.modifications.ModificationProvider;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.gui.utils.EmptyCollections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class contains the scores for the locations of the possible
 * modifications.
 *
 * @author Marc Vaudel
 */
public class PSModificationScores extends ExperimentObject implements UrParameter {

    /**
     * Dummy scores.
     */
    public static final PSModificationScores dummy = new PSModificationScores();
    /**
     * A map containing all scores indexed by the modification of interest for a
     * peptide or a PSM.
     */
    private final HashMap<String, ModificationScoring> modificationMap = new HashMap<>(0);
    /**
     * A list of all modification sites confidently localized on a sequence in a
     * map: site &gt; modification names.
     */
    private HashMap<Integer, HashSet<String>> mainModificationSites = null;
    /**
     * A map of all confident modifications in a sequence indexed by
     * modification name.
     */
    private HashMap<String, HashSet<Integer>> confidentModificationsByModName = null;
    /**
     * A list of all ambiguous modifications in a sequence: representative site
     * &gt; secondary site &gt; modification names.
     */
    private HashMap<Integer, HashMap<Integer, HashSet<String>>> ambiguousModificationsByRepresentativeSite = null;
    /**
     * A map of all ambiguous modifications in a sequence indexed by
     * modification name.
     */
    private HashMap<String, HashMap<Integer, HashSet<Integer>>> ambiguousModificationsByModName = null;

    /**
     * Constructor.
     */
    public PSModificationScores() {
    }

    /**
     * Adds a scoring result for the modification of interest.
     *
     * @param modName the modification of interest
     * @param modificationScoring the corresponding scoring
     */
    public void addModificationScoring(
            String modName,
            ModificationScoring modificationScoring
    ) {

        modificationMap.put(modName, modificationScoring);

    }

    /**
     * Returns the modification scoring for the desired modification (null if
     * none found).
     *
     * @param modName the modification of interest
     *
     * @return the scoring
     */
    public ModificationScoring getModificationScoring(
            String modName
    ) {

        return modificationMap.get(modName);

    }

    /**
     * Indicates whether a modification has been already scored.
     *
     * @param modName the modification of interest
     *
     * @return a boolean indicating whether the modification is in the map
     */
    public boolean containsModification(
            String modName
    ) {

        return modificationMap.containsKey(modName);

    }

    /**
     * Returns a list of scored modifications.
     *
     * @return a list of scored modifications
     */
    public Set<String> getScoredModifications() {

        return modificationMap.keySet();

    }

    /**
     * Adds a confident modification site.
     *
     * @param modName the modification name
     * @param modificationSite the modification site
     */
    public void addConfidentModificationSite(
            String modName,
            int modificationSite
    ) {

        // add the modification to the site map
        if (mainModificationSites == null) {

            mainModificationSites = new HashMap<>(1);

        }

        HashSet<String> modifications = mainModificationSites.get(modificationSite);

        if (modifications == null) {

            modifications = new HashSet<>(1);
            mainModificationSites.put(modificationSite, modifications);

        }

        modifications.add(modName);

        // add the site to the modification map
        if (confidentModificationsByModName == null) {

            confidentModificationsByModName = new HashMap<>(1);

        }

        HashSet<Integer> modificationSites = confidentModificationsByModName.get(modName);

        if (modificationSites == null) {

            modificationSites = new HashSet<>(1);
            confidentModificationsByModName.put(modName, modificationSites);

        }

        modificationSites.add(modificationSite);

    }

    /**
     * Adds a group of modifications to the mapping of ambiguous sites.
     *
     * @param representativeSite the representative site of this modification
     * group
     * @param possibleModifications the possible modifications in a map: site
     * &gt; modification name
     */
    public void addAmbiguousModificationSites(
            int representativeSite,
            HashMap<Integer, HashSet<String>> possibleModifications
    ) {

        if (ambiguousModificationsByRepresentativeSite == null) {

            ambiguousModificationsByRepresentativeSite = new HashMap<>(possibleModifications.size());
            ambiguousModificationsByModName = new HashMap<>(possibleModifications.size());

        }

        HashMap<Integer, HashSet<String>> modificationGroupsAtSite = ambiguousModificationsByRepresentativeSite.get(representativeSite);

        if (modificationGroupsAtSite == null) {

            modificationGroupsAtSite = new HashMap<>(1);
            ambiguousModificationsByRepresentativeSite.put(representativeSite, modificationGroupsAtSite);

        }

        for (int site : possibleModifications.keySet()) {

            for (String modName : possibleModifications.get(site)) {

                HashSet<String> modifications = modificationGroupsAtSite.get(site);

                if (modifications == null) {

                    modifications = new HashSet<>(1);
                    modificationGroupsAtSite.put(site, modifications);

                }

                if (!modifications.contains(modName)) {

                    modifications.add(modName);

                }
            }
        }

        HashSet<String> modifications = possibleModifications.get(representativeSite);

        for (String modification : modifications) {

            HashMap<Integer, HashSet<Integer>> modSites = ambiguousModificationsByModName.get(modification);

            if (modSites == null) {

                modSites = new HashMap<>(1);
                ambiguousModificationsByModName.put(modification, modSites);

            }

            HashSet<Integer> secondarySites = modSites.get(representativeSite);

            if (secondarySites == null) {

                secondarySites = new HashSet<>(1);
                modSites.put(representativeSite, secondarySites);

            }

            for (int site : possibleModifications.keySet()) {

                secondarySites.add(site);

            }
        }
    }

    /**
     * Changes the representative site for a given ambiguously localized
     * modification in all maps.
     *
     * @param modName The name of the modification at the new site.
     * @param originalModName The name of the modification at the original site.
     * @param originalRepresentativeSite The original representative site.
     * @param newRepresentativeSite The new representative site.
     * @param nMod The occurrence of modifications of the exact same mass in
     * this peptide.
     * @param modificationProvider The modification provider to use.
     */
    public void changeRepresentativeSite(
            String modName,
            String originalModName,
            int originalRepresentativeSite,
            int newRepresentativeSite,
            int nMod,
            ModificationProvider modificationProvider
    ) {

        double modMass = modificationProvider.getModification(modName).getMass();

        // Remove this modification from the mapping
        TreeSet<Integer> representativeSites = new TreeSet<>();
        representativeSites.add(newRepresentativeSite);

        TreeMap<Integer, HashSet<String>> modificationSites = new TreeMap<>();

        HashMap<Integer, HashMap<Integer, HashSet<String>>> newAmbiguousModificationsByRepresentativeSite = new HashMap<>(ambiguousModificationsByRepresentativeSite.size());

        for (Entry<Integer, HashMap<Integer, HashSet<String>>> entry1 : ambiguousModificationsByRepresentativeSite.entrySet()) {

            int representativeSite = entry1.getKey();
            HashMap<Integer, HashSet<String>> sites = entry1.getValue();
            HashMap<Integer, HashSet<String>> differentModSites = new HashMap<>(sites.size());

            for (Entry<Integer, HashSet<String>> entry2 : sites.entrySet()) {

                int secondarySite = entry2.getKey();
                HashSet<String> modifications = entry2.getValue();

                for (String modification : modifications) {

                    double tempModMass = modificationProvider.getModification(modification).getMass();

                    if (modMass == tempModMass) {

                        if (representativeSite != originalRepresentativeSite) {

                            representativeSites.add(representativeSite);

                        }
                        if (confidentModificationsByModName != null) {
                            
                            if (representativeSite == originalRepresentativeSite) {

                                HashSet<Integer> tempSites = confidentModificationsByModName.get(modification);

                                if (tempSites != null) {

                                    tempSites.remove(originalRepresentativeSite);

                                    if (tempSites.isEmpty()) {

                                        confidentModificationsByModName.remove(modification);

                                    }
                                }
                            }
                            if (secondarySite == newRepresentativeSite) {

                                HashSet<Integer> tempSites = confidentModificationsByModName.get(modification);

                                if (tempSites == null) {

                                    tempSites = new HashSet<>(1);
                                    confidentModificationsByModName.put(modification, tempSites);

                                }

                                tempSites.add(newRepresentativeSite);

                            }
                        }
                        
                        ambiguousModificationsByModName.remove(modification);

                        HashSet<String> tempMods = modificationSites.get(secondarySite);

                        if (tempMods == null) {

                            tempMods = new HashSet<>(1);
                            modificationSites.put(secondarySite, tempMods);

                        }

                        tempMods.add(modification);

                    } else {

                        HashSet<String> tempMods = differentModSites.get(secondarySite);

                        if (tempMods == null) {

                            tempMods = new HashSet<>(1);
                            differentModSites.put(secondarySite, tempMods);

                        }

                        tempMods.add(modification);

                    }
                }
            }

            if (!differentModSites.isEmpty()) {

                newAmbiguousModificationsByRepresentativeSite.put(representativeSite, differentModSites);
            }
        }

                        HashSet<String> tempMods = modificationSites.get(newRepresentativeSite);

                        if (tempMods == null) {

                            tempMods = new HashSet<>(1);
                            modificationSites.put(newRepresentativeSite, tempMods);

                        }

                        tempMods.add(modName);

        ambiguousModificationsByRepresentativeSite = newAmbiguousModificationsByRepresentativeSite;

        // Add the new sites
        int[] sortedSelectedSites = representativeSites.stream().mapToInt(a -> a).toArray();
        int[] sortedPossibleSites = modificationSites.keySet().stream().mapToInt(a -> a).toArray();

        for (int siteI = 0; siteI < sortedSelectedSites.length; siteI++) {

            int site = sortedSelectedSites[siteI];

            // Assign the closest secondary sites to this one
            HashMap<Integer, HashSet<String>> secondarySites = new HashMap<>(1);

            for (int secondarySite : sortedPossibleSites) {

                if (secondarySite < site) {

                    if (siteI > 0 && site - secondarySite < secondarySite - sortedSelectedSites[siteI - 1] || siteI == 0) {

                        secondarySites.put(secondarySite, modificationSites.get(secondarySite));

                    }

                } else if (secondarySite > site) {

                    if (siteI < sortedSelectedSites.length - 1 && secondarySite - site <= sortedSelectedSites[siteI + 1] - secondarySite || siteI == sortedSelectedSites.length - 1) {

                        secondarySites.put(secondarySite, modificationSites.get(secondarySite));

                    }

                } else {

                    secondarySites.put(site, modificationSites.get(site));

                }
            }

            addAmbiguousModificationSites(site, secondarySites);

        }

        // Sanity check: are there enough representative sites for the given modification mass?
        
        HashSet<Integer> modificationSitesTest = new HashSet<>(nMod);

        if (confidentModificationsByModName != null) {

            for (Entry<String, HashSet<Integer>> entry : confidentModificationsByModName.entrySet()) {

                String tempModName = entry.getKey();

                double tempModMass = modificationProvider.getModification(tempModName).getMass();

                if (modMass == tempModMass) {

                    modificationSitesTest.addAll(entry.getValue());

                }
            }
        }

        for (Entry<String, HashMap<Integer, HashSet<Integer>>> entry : ambiguousModificationsByModName.entrySet()) {

            String tempModName = entry.getKey();

            double tempModMass = modificationProvider.getModification(tempModName).getMass();

            if (modMass == tempModMass) {

                    modificationSitesTest.addAll(entry.getValue().keySet());

            }
        }

        if (modificationSitesTest.size() != nMod) {

            throw new IllegalArgumentException(
                    "Incorrect number of representative sites for modification of mass "
                    + modMass
                    + ". Expected: "
                    + nMod
                    + ", found: "
                    + modificationSitesTest.size()
                    + "."
            );

        }
    }

    /**
     * Indicates whether a site is already registered as confident modification
     * site.
     *
     * @param site the site of interest
     * @param modificationName the name of the modification
     *
     * @return a boolean indicating whether a site is already registered as
     * confident modification site
     */
    public boolean isConfidentModificationSite(
            int site,
            String modificationName
    ) {

        return mainModificationSites != null
                && mainModificationSites.containsKey(site)
                && mainModificationSites.get(site).contains(modificationName);

    }

    /**
     * Returns the main potential modifications at the given amino acid index.
     *
     * @param site the index in the sequence (1 is first amino acid)
     *
     * @return a list containing all potential modifications as main match, an
     * empty list if none found
     */
    public HashSet<String> getConfidentModificationsAt(
            int site
    ) {

        return mainModificationSites == null
                || !mainModificationSites.containsKey(site) ? EmptyCollections.emptyStringSet
                : mainModificationSites.get(site);

    }

    /**
     * Returns the modifications which have a representative ambiguous site at
     * the given site.
     *
     * @param site the index in the sequence (0 is first amino acid)
     *
     * @return a list of modifications which have a representative ambiguous
     * site at the given site
     */
    public HashSet<String> getModificationsAtRepresentativeSite(
            int site
    ) {

        HashMap<Integer, HashSet<String>> modificationsAtSite = getAmbiguousModificationsAtRepresentativeSite(site);
        HashSet<String> modifications = modificationsAtSite.get(site);

        return modifications == null ? EmptyCollections.emptyStringSet
                : modificationsAtSite.get(site);

    }

    /**
     * Returns the confident sites for the given modification. An empty list if
     * none found.
     *
     * @param modName the name of the modification of interest
     *
     * @return the confident sites for the given modification
     */
    public HashSet<Integer> getConfidentSitesForModification(
            String modName
    ) {

        return confidentModificationsByModName == null
                || !confidentModificationsByModName.containsKey(modName) ? EmptyCollections.emptyIntSet
                : confidentModificationsByModName.get(modName);

    }

    /**
     * Returns the ambiguous modification assignments registered at the given
     * representative site in a map: secondary site &gt; modifications.
     *
     * @param representativeSite the representative site of interest
     *
     * @return the ambiguous modification assignments registered at the given
     * representative site
     */
    public HashMap<Integer, HashSet<String>> getAmbiguousModificationsAtRepresentativeSite(
            int representativeSite
    ) {

        return ambiguousModificationsByRepresentativeSite == null
                || !ambiguousModificationsByRepresentativeSite.containsKey(representativeSite)
                ? new HashMap<>(0)
                : ambiguousModificationsByRepresentativeSite.get(representativeSite);

    }

    /**
     * Returns the ambiguous modification sites registered for the given
     * modification.
     *
     * @param modName the name of the modification of interest
     *
     * @return the ambiguous modification sites registered for the given
     * modification
     */
    public HashMap<Integer, HashSet<Integer>> getAmbiguousModificationsSites(
            String modName
    ) {

        return ambiguousModificationsByModName == null ? new HashMap<>(0)
                : ambiguousModificationsByModName.get(modName);

    }

    /**
     * Returns a list of all confident modification sites.
     *
     * @return a list of all confident modification sites
     */
    public TreeSet<Integer> getConfidentSites() {

        return mainModificationSites == null ? new TreeSet<>()
                : new TreeSet<>(mainModificationSites.keySet());

    }

    /**
     * Returns a list of all representative sites of ambiguously localized
     * modifications.
     *
     * @return a list of all representative sites of ambiguously localized
     * modifications
     */
    public TreeSet<Integer> getRepresentativeSites() {

        return ambiguousModificationsByRepresentativeSite == null ? EmptyCollections.emptyIntTreeSet
                : new TreeSet<>(ambiguousModificationsByRepresentativeSite.keySet());

    }

    /**
     * Returns a list of modifications presenting at least a confident site.
     *
     * @return a list of modifications presenting at least a confident site
     */
    public TreeSet<String> getConfidentlyLocalizedModifications() {

        return confidentModificationsByModName == null ? EmptyCollections.emptyStringTreeSet
                : new TreeSet<>(confidentModificationsByModName.keySet());

    }

    /**
     * Returns a list of modifications presenting at least an ambiguous site.
     *
     * @return a list of modifications presenting at least an ambiguous site
     */
    public TreeSet<String> getAmbiguouslyLocalizedModifications() {

        return ambiguousModificationsByModName == null ? EmptyCollections.emptyStringTreeSet
                : new TreeSet<>(ambiguousModificationsByModName.keySet());

    }

    @Override
    public long getParameterKey() {
        return getId();
    }
}
