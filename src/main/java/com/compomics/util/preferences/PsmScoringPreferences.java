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
     * The minimal number of decoys to include in a bin to set the bin size of
     * the score histogram.
     */
    private Integer minDecoysInBin = 10;

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
            spectrumMatchingScores = new HashMap<>();
        }
        HashSet<Integer> algorithmScores = spectrumMatchingScores.get(advocateId);
        if (algorithmScores == null) {
            algorithmScores = new HashSet<>(1);
            spectrumMatchingScores.put(advocateId, algorithmScores);
        }
        algorithmScores.add(scoreId);
    }

    /**
     * Clears the score for the given algorithm.
     *
     * @param advocateId the score for the given algorithm
     */
    public void clearScores(Integer advocateId) {
        if (spectrumMatchingScores != null) {
            spectrumMatchingScores.remove(advocateId);
        }
    }

    /**
     * Clears all scores.
     */
    public void clearAllScores() {
        spectrumMatchingScores.clear();
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
            return new HashSet<>(0);
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

        HashSet<Integer> thisAdvocates = new HashSet<>(getAdvocates());
        HashSet<Integer> otherAdvocates = new HashSet<>(otherPsmScoringPreferences.getAdvocates());
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
        defaultScores = new HashSet<>(1);
        defaultScores.add(PsmScore.native_score.index);

        // De novo scores
        if (spectrumMatchingScores == null) { // Backward compatibility
            spectrumMatchingScores = new HashMap<>(3);
            HashSet<Integer> scores = new HashSet<>(3);
            scores.add(PsmScore.hyperScore.index);
            spectrumMatchingScores.put(Advocate.direcTag.getIndex(), scores);
            spectrumMatchingScores.put(Advocate.pepnovo.getIndex(), new HashSet<>(scores));
            spectrumMatchingScores.put(Advocate.pNovo.getIndex(), new HashSet<>(scores));
        }
    }

    /**
     * Returns the minimal number of decoys to include in a bin to set the bin
     * size of the score histogram.
     *
     * @return the minimal number of decoys to include in a bin to set the bin
     * size of the score histogram
     */
    public Integer getDecoysInFirstBin() {
        if (minDecoysInBin == null) {
            minDecoysInBin = 10;
        }
        return minDecoysInBin;
    }

    /**
     * Sets the minimal number of decoys to include in a bin to set the bin size
     * of the score histogram.
     *
     * @param decoysInFirstBin the minimal number of decoys to include in a bin
     * to set the bin size of the score histogram
     */
    public void setDecoysInFirstBin(Integer decoysInFirstBin) {
        this.minDecoysInBin = decoysInFirstBin;
    }
}
