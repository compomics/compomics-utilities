package com.compomics.util.experiment.identification.peptide_shaker;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
     * 
     * @deprecated Not used anymore, kept for backward compatibility.
     */
    private HashMap<Integer, HashSet<String>> mainModificationSites = null;
    /**
     * A map of all confident modifications in a sequence indexed by
     * modification name.
     * 
     * @deprecated Not used anymore, kept for backward compatibility.
     */
    private HashMap<String, HashSet<Integer>> confidentModificationsByModName = null;
    /**
     * A list of all ambiguous modifications in a sequence: representative site
     * &gt; secondary site &gt; modification names.
     * 
     * @deprecated Not used anymore, kept for backward compatibility.
     */
    private HashMap<Integer, HashMap<Integer, HashSet<String>>> ambiguousModificationsByRepresentativeSite = null;
    /**
     * A map of all ambiguous modifications in a sequence indexed by
     * modification name.
     * 
     * @deprecated Not used anymore, kept for backward compatibility.
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

    @Override
    public long getParameterKey() {
        return getId();
    }
}
