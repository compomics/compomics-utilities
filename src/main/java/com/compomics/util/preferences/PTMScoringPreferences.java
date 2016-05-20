package com.compomics.util.preferences;

import com.compomics.util.experiment.identification.ptm.PtmScore;
import java.io.Serializable;

/**
 * This class contains the PTM localization preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PTMScoringPreferences implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -6656074270981104708L;
    /**
     * The FLR threshold in percent.
     */
    private double flr = 1.0;
    /**
     * Boolean indicating whether a probabilistic score is to be calculated.
     */
    private Boolean probabilitsticScoreCalculation = true; // @TODO: typo: probabilistic
    /**
     * The probabilistic score selected.
     */
    private PtmScore selectedProbabilisticScore = PtmScore.PhosphoRS;
    /**
     * Boolean indicating whether the threshold should be FLR based.
     */
    private boolean estimateFlr = false;
    /**
     * The probabilistic score threshold.
     */
    private double probabilisticScoreThreshold = 95;
    /**
     * Boolean indicating whether neutral losses shall be accounted for in the
     * calculation of the probabilistic score.
     */
    private Boolean probabilisticScoreNeutralLosses = false;
    /**
     * The preferences to use when matching PTMs to amino acid sequences.
     */
    private SequenceMatchingPreferences sequenceMatchingPreferences;
    /**
     * Boolean indicating whether the non confidently localized PTMs should be aligned on the confident sites.
     */
    private Boolean alignNonConfidentPTMs = true;

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
    public Boolean isProbabilitsticScoreCalculation() {  // @TODO: typo: probabilistic
        return probabilitsticScoreCalculation;
    }

    /**
     * Sets whether a probabilistic PTM score is required.
     *
     * @param probabilitsticScoreCalculation a boolean indicating whether a
     * probabilistic PTM score is required
     */
    public void setProbabilitsticScoreCalculation(boolean probabilitsticScoreCalculation) { // @TODO: typo: probabilistic
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

    /**
     * Indicates whether the non confidently localized PTMs should be aligned on the confident sites.
     * 
     * @return boolean indicating whether the non confidently localized PTMs should be aligned on the confident sites
     */
    public Boolean getAlignNonConfidentPTMs() {
        if (alignNonConfidentPTMs == null) { // Backward compatibility
            alignNonConfidentPTMs = true;
        }
        return alignNonConfidentPTMs;
    }

    /**
     * Sets whether the non confidently localized PTMs should be aligned on the confident sites.
     * 
     * @param alignNonConfidentPTMs a boolean indicating whether the non confidently localized PTMs should be aligned on the confident sites
     */
    public void setAlignNonConfidentPTMs(Boolean alignNonConfidentPTMs) {
        this.alignNonConfidentPTMs = alignNonConfidentPTMs;
    }
    
    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {

        String newLine = System.getProperty("line.separator");

        StringBuilder output = new StringBuilder();

        output.append("Score: ").append(selectedProbabilisticScore).append(".").append(newLine);
        output.append("Include Neutral Losses: ").append(probabilisticScoreNeutralLosses).append(".").append(newLine);
        output.append("Threshold Auto: ").append(estimateFlr).append(".").append(newLine);
        output.append("Threshold: ").append(probabilisticScoreThreshold).append(".").append(newLine);
        output.append("Align PTMs: ").append(getAlignNonConfidentPTMs()).append(".").append(newLine);

        return output.toString();
    }

    /**
     * Returns true if the objects have identical settings.
     *
     * @param otherPtmScoringPreferences the PTMScoringPreferences to compare to
     *
     * @return true if the objects have identical settings
     */
    public boolean equals(PTMScoringPreferences otherPtmScoringPreferences) {

        if (otherPtmScoringPreferences == null) {
            return false;
        }

        double diff = Math.abs(flr - otherPtmScoringPreferences.getFlrThreshold());
        if (diff > Double.MIN_VALUE) {
            return false;
        }

        if (probabilitsticScoreCalculation.booleanValue() != otherPtmScoringPreferences.isProbabilitsticScoreCalculation()) {
            return false;
        }

        if (selectedProbabilisticScore != otherPtmScoringPreferences.getSelectedProbabilisticScore()) {
            return false;
        }

        if (estimateFlr != otherPtmScoringPreferences.isEstimateFlr()) {
            return false;
        }

        diff = Math.abs(probabilisticScoreThreshold - otherPtmScoringPreferences.getProbabilisticScoreThreshold());
        if (diff > Double.MIN_VALUE) {
            return false;
        }

        if (probabilisticScoreNeutralLosses.booleanValue() != otherPtmScoringPreferences.isProbabilisticScoreNeutralLosses()) {
            return false;
        }
        
        if (!getAlignNonConfidentPTMs() == otherPtmScoringPreferences.getAlignNonConfidentPTMs()) {
            return false;
        }

        if (!sequenceMatchingPreferences.isSameAs(otherPtmScoringPreferences.getSequenceMatchingPreferences())) {
            return false;
        }

        return true;
    }
}
