package com.compomics.util.preferences;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.psm_scoring.PsmScore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
     * The scores to use by default.
     */
    private HashSet<Integer> defaultScores;

    /**
     * Constructor.
     */
    public PsmScoringPreferences() {
        setDefaultScores();
    }

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
        boolean scoreSet = false;
        if (spectrumMatchingScores != null && !spectrumMatchingScores.isEmpty()) {
            HashSet<Integer> scores = spectrumMatchingScores.get(advocate);
            if (scores != null && !scores.isEmpty()) {
                scoreSet = true;
                if (scores.size() > 1) {
                    return true;
                }
                for (int scoreIndex : scores) {
                    if (scoreIndex != PsmScore.native_score.index) {
                        return true;
                    }
                }
            }
        }
        if (!scoreSet && defaultScores != null && !defaultScores.isEmpty()) {
            if (defaultScores.size() > 1) {
                return true;
            }
            for (int scoreIndex : defaultScores) {
                if (scoreIndex != PsmScore.native_score.index) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the advocates with a specific scoring.
     *
     * @return the advocates with a specific scoring
     */
    public Set<Integer> getAdvocates() {
        if (spectrumMatchingScores == null) {
            return new HashSet<Integer>(0);
        }
        return spectrumMatchingScores.keySet();
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
                if (PsmScoringPreferences.this.isScoringNeeded(advocate)) {
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

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {

        String newLine = System.getProperty("line.separator");

        StringBuilder output = new StringBuilder();

        for (Integer advocateIndex : getAdvocates()) {
            Advocate advocate = Advocate.getAdvocate(advocateIndex);
            output.append(advocate.getName()).append(": ");
            boolean first = true;
            for (Integer scoreIndex : getScoreForAlgorithm(advocateIndex)) {
                if (first) {
                    first = false;
                } else {
                    output.append(", ");
                }
                PsmScore score = PsmScore.getScore(scoreIndex);
                output.append(score.name);
            }
            output.append(".").append(newLine);
        }
        output.append("Default: ");
        boolean first = true;
        for (Integer scoreIndex : getDefaultScores()) {
            if (first) {
                first = false;
            } else {
                output.append(", ");
            }
            PsmScore score = PsmScore.getScore(scoreIndex);
            output.append(score.name);
        }
        output.append(".").append(newLine);

        return output.toString();
    }

    /**
     * Returns true if the objects have identical settings.
     *
     * @param otherPsmScoringPreferences the PsmScoringPreferences to compare to
     *
     * @return true if the objects have identical settings
     */
    public boolean equals(PsmScoringPreferences otherPsmScoringPreferences) {

        if (otherPsmScoringPreferences == null) {
            return false;
        }

        if (!Util.sameSets(defaultScores, otherPsmScoringPreferences.getDefaultScores())) {
            return false;
        }

        HashSet<Integer> thisAdvocates = new HashSet<Integer>(getAdvocates());
        HashSet<Integer> otherAdvocates = new HashSet<Integer>(otherPsmScoringPreferences.getAdvocates());
        if (!Util.sameSets(thisAdvocates, otherAdvocates)) {
            return false;
        }

        for (Integer advocate : thisAdvocates) {
            HashSet<Integer> thisScores = getScoreForAlgorithm(advocate);
            HashSet<Integer> otherScores = otherPsmScoringPreferences.getScoreForAlgorithm(advocate);
            if (!Util.sameSets(thisScores, otherScores)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the default scores.
     *
     * @return the default scores
     */
    public HashSet<Integer> getDefaultScores() {
        if (defaultScores == null) { // Backward compatibility
            setDefaultScores();
        }
        return defaultScores;
    }

    /**
     * Sets the scores to use by default.
     */
    private void setDefaultScores() {

        // Use only the native score by default
        defaultScores = new HashSet<Integer>(1);
        defaultScores.add(PsmScore.native_score.index);

        // De novo scores
        HashSet<Integer> scores = new HashSet<Integer>(3);
        scores.add(PsmScore.precursor_accuracy.index);
        scores.add(PsmScore.aa_ms2_mz_fidelity.index);
        scores.add(PsmScore.aa_intensity.index);
        spectrumMatchingScores = new HashMap<Integer, HashSet<Integer>>(3);
        spectrumMatchingScores.put(Advocate.direcTag.getIndex(), scores);
        spectrumMatchingScores.put(Advocate.pepnovo.getIndex(), new HashSet<Integer>(scores));
        spectrumMatchingScores.put(Advocate.pNovo.getIndex(), new HashSet<Integer>(scores));

    }
}
