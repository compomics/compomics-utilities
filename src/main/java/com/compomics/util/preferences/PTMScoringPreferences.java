package com.compomics.util.preferences;

import com.compomics.util.experiment.identification.ptm.PtmScore;
import java.io.Serializable;

/**
 * This class contains the PTM localization scoring preferences.
 *
 * @author Marc Vaudel
 */
public class PTMScoringPreferences implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -6656074270981104708L;
    /**
     * The FLR threshold in percent
     */
    private double flr = 1.0;
    /**
     * Boolean indicating whether a probabilitstic score is to be calculated.
     */
    private Boolean probabilitsticScoreCalculation = true;
    /**
     * The probabilistic score selected.
     */
    private PtmScore selectedProbabilisticScore = PtmScore.PhosphoRS;
    /**
     * Boolean indicating whether the threshold should be FLR based.
     */
    private boolean estimateFlr = true;
    /**
     * The probabilistic score threshold.
     */
    private double probabilisticScoreThreshold = 99;
    /**
     * Boolean indicating whether neutral losses shall be accounted for in the
     * calculation of the probabilistic score.
     */
    private Boolean probabilisticScoreNeutralLosses = false;
    /**
     * The preferences to use when matching ptms to amino acid sequences.
     */
    private SequenceMatchingPreferences sequenceMatchingPreferences;

    /**
     * Constructor.
     */
    public PTMScoringPreferences() {
        sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.aminoAcid);
    }

    /**
     * Indicates whether a probabilistic PTM score is required.
     *
     * @return a boolean indicating whether a probabilistic PTM score is
     * required
     */
    public Boolean isProbabilitsticScoreCalculation() {
        return probabilitsticScoreCalculation;
    }

    /**
     * Sets whether a probabilistic PTM score is required.
     *
     * @param probabilitsticScoreCalculation a boolean indicating whether a
     * probabilistic PTM score is required
     */
    public void setProbabilitsticScoreCalculation(boolean probabilitsticScoreCalculation) {
        this.probabilitsticScoreCalculation = probabilitsticScoreCalculation;
    }

    /**
     * Returns the selected probabilistic score.
     *
     * @return the selected probabilistic score
     */
    public PtmScore getSelectedProbabilisticScore() {
        if (selectedProbabilisticScore == null) {
            return PtmScore.AScore; // backward compatibility
        }
        return selectedProbabilisticScore;
    }

    /**
     * Sets the selected probabilistic score.
     *
     * @param selectedProbabilisticScore the selected probabilistic score
     */
    public void setSelectedProbabilisticScore(PtmScore selectedProbabilisticScore) {
        this.selectedProbabilisticScore = selectedProbabilisticScore;
    }

    /**
     * Indicates whether the threshold is FLR based.
     *
     * @return a boolean indicating whether the threshold is FLR based
     */
    public boolean isEstimateFlr() {
        return estimateFlr;
    }

    /**
     * Sets whether the threshold is FLR based.
     *
     * @param estimateFlr indicates whether the threshold is FLR based
     */
    public void setEstimateFlr(boolean estimateFlr) {
        this.estimateFlr = estimateFlr;
    }

    /**
     * Returns the probabilistic score threshold.
     *
     * @return the probabilistic score threshold
     */
    public double getProbabilisticScoreThreshold() {
        return probabilisticScoreThreshold;
    }

    /**
     * Sets the probabilistic score threshold.
     *
     * @param probabilisticScoreThreshold the probabilistic score threshold
     */
    public void setProbabilisticScoreThreshold(double probabilisticScoreThreshold) {
        this.probabilisticScoreThreshold = probabilisticScoreThreshold;
    }

    /**
     * Indicates whether the neutral losses shall be taken into account for
     * spectrum annotation when calculating the probabilistic score.
     *
     * @return a boolean indicating whether the neutral losses shall be taken
     * into account for spectrum annotation when calculating the probabilistic
     * score
     */
    public Boolean isProbabilisticScoreNeutralLosses() {
        return probabilisticScoreNeutralLosses;
    }

    /**
     * Sets whether the neutral losses shall be taken into account for spectrum
     * annotation when calculating the probabilistic score.
     *
     * @param probabilisticScoreNeutralLosses indicates whether the neutral
     * losses shall be taken into account for spectrum annotation when
     * calculating the probabilistic score
     */
    public void setProbabilisticScoreNeutralLosses(boolean probabilisticScoreNeutralLosses) {
        this.probabilisticScoreNeutralLosses = probabilisticScoreNeutralLosses;
    }

    /**
     * Returns the sequence matching preferences to use when mapping PTMs on
     * amino acid sequences.
     *
     * @return the sequence matching preferences to use when mapping PTMs on
     * amino acid sequences
     */
    public SequenceMatchingPreferences getSequenceMatchingPreferences() {
        if (sequenceMatchingPreferences == null) {
            sequenceMatchingPreferences = new SequenceMatchingPreferences();
            sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.aminoAcid);
        }
        return sequenceMatchingPreferences;
    }

    /**
     * Sets the sequence matching preferences to use when mapping PTMs on amino
     * acid sequences.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences to
     * use when mapping PTMs on amino acid sequences
     */
    public void setSequenceMatchingPreferences(SequenceMatchingPreferences sequenceMatchingPreferences) {
        this.sequenceMatchingPreferences = sequenceMatchingPreferences;
    }
    
    /**
     * Returns the FLR threshold.
     *
     * @return the FLR threshold
     */
    public double getFlrThreshold() {
        return flr;
    }

    /**
     * Sets the FLR threshold.
     *
     * @param flr the FLR threshold
     */
    public void setFlrThreshold(double flr) {
        this.flr = flr;
    }
}
