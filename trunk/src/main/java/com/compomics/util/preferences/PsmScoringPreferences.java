package com.compomics.util.preferences;

import com.compomics.util.experiment.identification.psm_scoring.PsmScores;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Generic class for peptide spectrum match scoring.
 *
 * @author Marc Vaudel
 */
public class PsmScoringPreferences implements Serializable {

    /**
     * Serial version UID for backward compatibility.
     */
    static final long serialVersionUID = 4997457408322410176L;
    /**
     * The scores used to score the spectrum matches for every advocate in a
     * map: advocate index &gt; list of score indexes.
     */
    private HashMap<Integer, HashSet<Integer>> spectrumMatchingScores = null;

    /**
     * Adds a score for a given algorithm to the scoring preferences.
     *
     * @param advocateId the index of the algorithm
     * @param scoreId the index of the score
     */
    public void addScore(Integer advocateId, Integer scoreId) {
        if (spectrumMatchingScores == null) {
            spectrumMatchingScores = new HashMap<Integer, HashSet<Integer>>();
        }
        HashSet<Integer> algorithmScores = spectrumMatchingScores.get(advocateId);
        if (algorithmScores == null) {
            algorithmScores = new HashSet<Integer>(1);
            spectrumMatchingScores.put(advocateId, algorithmScores);
        }
        algorithmScores.add(scoreId);
    }

    /**
     * Returns the scores set for a given algorithm.
     *
     * @param advocateId the index of the algorithm
     *
     * @return the index of the score
     */
    public HashSet<Integer> getScoreForAlgorithm(Integer advocateId) {
        if (spectrumMatchingScores == null) {
            return null;
        }
        return spectrumMatchingScores.get(advocateId);
    }

    /**
     * Indicates whether a score computation is needed for the given advocate.
     *
     * @param advocate the index of the advocate of interest
     *
     * @return a boolean indicating whether a score computation is needed
     */
    public boolean isScoringNeeded(int advocate) {
        if (spectrumMatchingScores != null && !spectrumMatchingScores.isEmpty()) {
            HashSet<Integer> scores = spectrumMatchingScores.get(advocate);
            if (scores != null && !scores.isEmpty()) {
                for (int scoreIndex : scores) {
                    if (scoreIndex != PsmScores.native_score.index) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Indicates whether a score computation is needed for the given advocates.
     *
     * @param advocates the advocates of interest
     *
     * @return a boolean indicating whether a score computation is needed
     */
    public boolean isScoringNeeded(ArrayList<Integer> advocates) {
        if (spectrumMatchingScores != null && !spectrumMatchingScores.isEmpty()) {
            for (Integer advocate : advocates) {
                if (isScoringNeeded(advocate)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Indicates whether target decoy databases are needed for PSM scoring.
     *
     * @param advocates the advocates of interest
     *
     * @return a boolean indicating whether a score computation is needed
     */
    public boolean isTargetDecoyNeededForPsmScoring(ArrayList<Integer> advocates) {
        if (spectrumMatchingScores != null && !spectrumMatchingScores.isEmpty()) {
            for (Integer advocate : advocates) {
                HashSet<Integer> scores = spectrumMatchingScores.get(advocate);
                if (scores != null && scores.size() > 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
