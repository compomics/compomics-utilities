package com.compomics.util.parameters.identification.advanced;

import com.compomics.util.experiment.identification.modification.ModificationLocalizationScore;
import java.io.Serializable;

/**
 * This class contains the PTM localization preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ModificationLocalizationParameters implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -6656074270981104708L;
    /**
     * Boolean indicating whether a probabilistic score is to be calculated.
     */
    private boolean probabilisticScoreCalculation = true;
    /**
     * The probabilistic score selected.
     */
    private ModificationLocalizationScore selectedProbabilisticScore = ModificationLocalizationScore.PhosphoRS;
    /**
     * The probabilistic score threshold.
     */
    private double probabilisticScoreThreshold = 95;
    /**
     * Boolean indicating whether neutral losses shall be accounted for in the
     * calculation of the probabilistic score.
     */
    private boolean probabilisticScoreNeutralLosses = false;
    /**
     * The preferences to use when matching modifications to amino acid sequences.
     */
    private SequenceMatchingParameters sequenceMatchingParameters;
    /**
     * Boolean indicating whether the non confidently localized PTMs should be
     * aligned on the confident sites.
     */
    private boolean alignNonConfidentPTMs = true;

    /**
     * Constructor.
     */
    public ModificationLocalizationParameters() {
        
        sequenceMatchingParameters = new SequenceMatchingParameters();
        sequenceMatchingParameters.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.aminoAcid);
    
    }

    /**
     * Indicates whether a probabilistic PTM score is required.
     *
     * @return a boolean indicating whether a probabilistic PTM score is
     * required
     */
    public boolean isProbabilisticScoreCalculation() {
        
        return probabilisticScoreCalculation;
    
    }

    /**
     * Sets whether a probabilistic PTM score is required.
     *
     * @param probabilisticScoreCalculation a boolean indicating whether a
     * probabilistic PTM score is required
     */
    public void setProbabilisticScoreCalculation(boolean probabilisticScoreCalculation) {
        
        this.probabilisticScoreCalculation = probabilisticScoreCalculation;
    
    }

    /**
     * Returns the selected probabilistic score.
     *
     * @return the selected probabilistic score
     */
    public ModificationLocalizationScore getSelectedProbabilisticScore() {
        
        return selectedProbabilisticScore;
    
    }

    /**
     * Sets the selected probabilistic score.
     *
     * @param selectedProbabilisticScore the selected probabilistic score
     */
    public void setSelectedProbabilisticScore(ModificationLocalizationScore selectedProbabilisticScore) {
        
        this.selectedProbabilisticScore = selectedProbabilisticScore;
    
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
    public boolean isProbabilisticScoreNeutralLosses() {

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
    public SequenceMatchingParameters getSequenceMatchingParameters() {
        
        return sequenceMatchingParameters;
        
    }

    /**
     * Sets the sequence matching preferences to use when mapping PTMs on amino
     * acid sequences.
     *
     * @param sequenceMatchingParameters the sequence matching preferences to
     * use when mapping PTMs on amino acid sequences
     */
    public void setSequenceMatchingParameters(SequenceMatchingParameters sequenceMatchingParameters) {

        this.sequenceMatchingParameters = sequenceMatchingParameters;

    }

    /**
     * Indicates whether the non confidently localized PTMs should be aligned on
     * the confident sites.
     *
     * @return boolean indicating whether the non confidently localized PTMs
     * should be aligned on the confident sites
     */
    public boolean getAlignNonConfidentPTMs() {

        return alignNonConfidentPTMs;

    }

    /**
     * Sets whether the non confidently localized PTMs should be aligned on the
     * confident sites.
     *
     * @param alignNonConfidentPTMs a boolean indicating whether the non
     * confidently localized PTMs should be aligned on the confident sites
     */
    public void setAlignNonConfidentPTMs(boolean alignNonConfidentPTMs) {

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
        output.append("Threshold: ").append(probabilisticScoreThreshold).append(".").append(newLine);
        output.append("Align PTMs: ").append(getAlignNonConfidentPTMs()).append(".").append(newLine);

        return output.toString();
        
    }

    /**
     * Returns true if the objects have identical settings.
     *
     * @param otherPtmScoringParameters the PTMScoringParameters to compare to
     *
     * @return true if the objects have identical settings
     */
    public boolean equals(ModificationLocalizationParameters otherPtmScoringParameters) {

        if (otherPtmScoringParameters == null) {
            
            return false;
        
        }

        if (probabilisticScoreCalculation != otherPtmScoringParameters.isProbabilisticScoreCalculation()) {
         
            return false;
        
        }

        if (selectedProbabilisticScore != otherPtmScoringParameters.getSelectedProbabilisticScore()) {
         
            return false;
        
        }

        if (probabilisticScoreNeutralLosses != otherPtmScoringParameters.isProbabilisticScoreNeutralLosses()) {
           
            return false;
        
        }

        if (!getAlignNonConfidentPTMs() == otherPtmScoringParameters.getAlignNonConfidentPTMs()) {
        
            return false;
        
        }

        if (!sequenceMatchingParameters.isSameAs(otherPtmScoringParameters.getSequenceMatchingParameters())) {
        
            return false;
        
        }

        return true;
        
    }
}
