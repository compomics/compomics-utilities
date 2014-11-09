package com.compomics.util.preferences;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.psm_scoring.PsmScores;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class groups the user preferences for the initial PeptideShaker
 * processing.
 *
 * @author Marc Vaudel
 */
public class ProcessingPreferences implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -5883143685674607162L;
    /**
     * Boolean indicating whether the A-score should be estimated.
     *
     * @deprecated use the PTM scoring preferences instead
     */
    private boolean aScore = false;
    /**
     * The default protein FDR.
     */
    private double proteinFDR = 1.0;
    /**
     * The default peptide FDR.
     */
    private double peptideFDR = 1.0;
    /**
     * The default PSM FDR.
     */
    private double psmFDR = 1.0;
    /**
     * The minimum confidence required for a protein to be included in the
     * average molecular weight analysis in the Fractions tab.
     */
    private Double proteinConfidenceMwPlots = 95.0;
    /**
     * The scores used to score the spectrum matches for every advocate in a
     * map: advocate index &gt; list of score indexes.
     */
    private HashMap<Integer, ArrayList<Integer>> spectrumMatchingScores = null;
    /**
     * The number of threads to use.
     */
    private int nThreads;

    /**
     * Constructor with default settings.
     */
    public ProcessingPreferences() {
        initializeAlgorithmScores();
        nThreads = Math.max(Runtime.getRuntime().availableProcessors(), 1); // @TODO: make it possible for the user to control the number of threads?
//        nThreads = 1;
    }

    /**
     * Indicates whether the A-score should be calculated.
     *
     * @deprecated use the PTM scoring preferences instead
     * @return a boolean indicating whether the A-score should be calculated
     */
    public boolean isAScoreCalculated() {
        return aScore;
    }

    /**
     * Sets whether the A-score should be calculated.
     *
     * @deprecated use the PTM scoring preferences instead
     * @param shouldEstimateAScore whether the A-score should be calculated
     */
    public void estimateAScore(boolean shouldEstimateAScore) {
        this.aScore = shouldEstimateAScore;
    }

    /**
     * Returns the initial peptide FDR.
     *
     * @return the initial peptide FDR
     */
    public double getPeptideFDR() {
        return peptideFDR;
    }

    /**
     * Sets the initial peptide FDR.
     *
     * @param peptideFDR the initial peptide FDR
     */
    public void setPeptideFDR(double peptideFDR) {
        this.peptideFDR = peptideFDR;
    }

    /**
     * Returns the initial protein FDR.
     *
     * @return the initial protein FDR
     */
    public double getProteinFDR() {
        return proteinFDR;
    }

    /**
     * Sets the initial protein FDR.
     *
     * @param proteinFDR the initial protein FDR
     */
    public void setProteinFDR(double proteinFDR) {
        this.proteinFDR = proteinFDR;
    }

    /**
     * Returns the initial PSM FDR.
     *
     * @return the initial PSM FDR
     */
    public double getPsmFDR() {
        return psmFDR;
    }

    /**
     * Sets the initial PSM FDR.
     *
     * @param psmFDR the initial PSM FDR
     */
    public void setPsmFDR(double psmFDR) {
        this.psmFDR = psmFDR;
    }

    /**
     * Returns the minimum confidence required for a protein to be included in
     * the average molecular weight analysis in the Fractions tab.
     *
     * @return the minimum confidence
     */
    public Double getProteinConfidenceMwPlots() {
        if (proteinConfidenceMwPlots == null) {
            return 95.0;
        }
        return proteinConfidenceMwPlots;
    }

    /**
     * Sets the minimum confidence required for a protein to be included in the
     * average molecular weight analysis in the Fractions tab.
     *
     * @param proteinConfidenceMwPlots minimum confidence
     */
    public void setProteinConfidenceMwPlots(Double proteinConfidenceMwPlots) {
        this.proteinConfidenceMwPlots = proteinConfidenceMwPlots;
    }

    /**
     * Sets the default score selection for the implemented advocates. Note:
     * this silently erases any previous selection.
     */
    public void initializeAlgorithmScores() {
        spectrumMatchingScores = new HashMap<Integer, ArrayList<Integer>>(Advocate.values().length);
        for (Advocate advocate : Advocate.values()) {
            ArrayList<Integer> scores = new ArrayList<Integer>();
            scores.add(PsmScores.native_score.index);
            if (advocate.getType() == Advocate.AdvocateType.sequencing_algorithm || advocate.getType() == Advocate.AdvocateType.spectral_library || advocate.getType() == Advocate.AdvocateType.unknown) {
                scores.add(PsmScores.precursor_accuracy.index);
//                           scores.add(PsmScores.ms2_mz_fidelity.index);
            scores.add(PsmScores.aa_ms2_mz_fidelity.index);
//                           scores.add(PsmScores.intensity.index);
                scores.add(PsmScores.aa_intensity.index);
                scores.add(PsmScores.complementarity.index);
            }
            spectrumMatchingScores.put(advocate.getIndex(), scores);
        }
    }

    /**
     * Sets the scores to use for a given advocate.
     *
     * @param advocateIndex the index of the advocate
     * @param scores the scores
     */
    public void setScoresForAlgorithm(int advocateIndex, ArrayList<Integer> scores) {
        if (spectrumMatchingScores == null) {
            spectrumMatchingScores = new HashMap<Integer, ArrayList<Integer>>();
        }
        spectrumMatchingScores.put(advocateIndex, scores);
    }

    /**
     * Returns the scores used for a given advocate.
     *
     * @param advocateIndex the index of the advocate
     *
     * @return the scores used for a given advocate
     */
    public ArrayList<Integer> getScores(int advocateIndex) {
        if (spectrumMatchingScores == null) {
            return null;
        }
        return spectrumMatchingScores.get(advocateIndex);
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
            ArrayList<Integer> scores = spectrumMatchingScores.get(advocate);
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
                ArrayList<Integer> scores = spectrumMatchingScores.get(advocate);
                if (scores != null && scores.size() > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the number of threads to use.
     * 
     * @return the number of threads to use
     */
    public int getnThreads() {
        return nThreads;
    }

    /**
     * Sets the number of threads to use.
     * 
     * @param nThreads the number of threads to use
     */
    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }
}
