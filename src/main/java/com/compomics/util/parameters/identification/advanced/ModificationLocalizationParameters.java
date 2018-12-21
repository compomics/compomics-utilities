package com.compomics.util.parameters.identification.advanced;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.experiment.identification.modification.ModificationLocalizationScore;

/**
 * This class contains the modification localization preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ModificationLocalizationParameters extends DbObject {

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
     * Boolean indicating whether the non confidently localized modificatoin should be
     * aligned on the confident sites.
     */
    private boolean alignNonConfidentModifications = true;

    /**
     * Constructor.
     */
    public ModificationLocalizationParameters() {
        
        sequenceMatchingParameters = new SequenceMatchingParameters();
        sequenceMatchingParameters.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.aminoAcid);
    
    }

    /**
     * Indicates whether a probabilistic modification score is required.
     *
     * @return a boolean indicating whether a probabilistic modification score is
     * required
     */
    public boolean isProbabilisticScoreCalculation() {
        readDBMode();
        
        return probabilisticScoreCalculation;
    
    }

    /**
     * Sets whether a probabilistic modification score is required.
     *
     * @param probabilisticScoreCalculation a boolean indicating whether a
     * probabilistic modification score is required
     */
    public void setProbabilisticScoreCalculation(boolean probabilisticScoreCalculation) {
        writeDBMode();
        this.probabilisticScoreCalculation = probabilisticScoreCalculation;
    
    }

    /**
     * Returns the selected probabilistic score.
     *
     * @return the selected probabilistic score
     */
    public ModificationLocalizationScore getSelectedProbabilisticScore() {
        readDBMode();
        
        return selectedProbabilisticScore;
    
    }

    /**
     * Sets the selected probabilistic score.
     *
     * @param selectedProbabilisticScore the selected probabilistic score
     */
    public void setSelectedProbabilisticScore(ModificationLocalizationScore selectedProbabilisticScore) {
        
        writeDBMode();
        this.selectedProbabilisticScore = selectedProbabilisticScore;
    
    }

    /**
     * Returns the probabilistic score threshold.
     *
     * @return the probabilistic score threshold
     */
    public double getProbabilisticScoreThreshold() {
        readDBMode();
        
        return probabilisticScoreThreshold;
    
    }

    /**
     * Sets the probabilistic score threshold.
     *
     * @param probabilisticScoreThreshold the probabilistic score threshold
     */
    public void setProbabilisticScoreThreshold(double probabilisticScoreThreshold) {
    
        writeDBMode();
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
        readDBMode();

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

        writeDBMode();
        this.probabilisticScoreNeutralLosses = probabilisticScoreNeutralLosses;

    }

    /**
     * Returns the sequence matching preferences to use when mapping modifications on
     * amino acid sequences.
     *
     * @return the sequence matching preferences to use when mapping modifications on
     * amino acid sequences
     */
    public SequenceMatchingParameters getSequenceMatchingParameters() {
        readDBMode();
        
        return sequenceMatchingParameters;
        
    }

    /**
     * Sets the sequence matching preferences to use when mapping modifications on amino
     * acid sequences.
     *
     * @param sequenceMatchingParameters the sequence matching preferences to
     * use when mapping modifications on amino acid sequences
     */
    public void setSequenceMatchingParameters(SequenceMatchingParameters sequenceMatchingParameters) {

        writeDBMode();
        this.sequenceMatchingParameters = sequenceMatchingParameters;

    }

    /**
     * Indicates whether the non confidently localized modifications should be aligned on
     * the confident sites.
     *
     * @return boolean indicating whether the non confidently localized modifications
     * should be aligned on the confident sites
     */
    public boolean getAlignNonConfidentModifications() {
        readDBMode();

        return alignNonConfidentModifications;

    }

    /**
     * Sets whether the non confidently localized modifications should be aligned on the
     * confident sites.
     *
     * @param alignNonConfidentModifications a boolean indicating whether the non
     * confidently localized modifications should be aligned on the confident sites
     */
    public void setAlignNonConfidentModifications(boolean alignNonConfidentModifications) {

        writeDBMode();
        this.alignNonConfidentModifications = alignNonConfidentModifications;

    }

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        readDBMode();

        String newLine = System.getProperty("line.separator");

        StringBuilder output = new StringBuilder();

        output.append("Score: ").append(selectedProbabilisticScore).append(".").append(newLine);
        output.append("Include Neutral Losses: ").append(probabilisticScoreNeutralLosses).append(".").append(newLine);
        output.append("Threshold: ").append(probabilisticScoreThreshold).append(".").append(newLine);
        output.append("Align modifications: ").append(getAlignNonConfidentModifications()).append(".").append(newLine);

        return output.toString();
        
    }

    /**
     * Returns true if the objects have identical settings.
     *
     * @param otherParameters the parameters to compare to
     *
     * @return true if the objects have identical settings
     */
    public boolean equals(ModificationLocalizationParameters otherParameters) {
        readDBMode();

        if (otherParameters == null) {
            
            return false;
        
        }

        if (probabilisticScoreCalculation != otherParameters.isProbabilisticScoreCalculation()) {
         
            return false;
        
        }

        if (selectedProbabilisticScore != otherParameters.getSelectedProbabilisticScore()) {
         
            return false;
        
        }

        if (probabilisticScoreNeutralLosses != otherParameters.isProbabilisticScoreNeutralLosses()) {
           
            return false;
        
        }

        if (!getAlignNonConfidentModifications() == otherParameters.getAlignNonConfidentModifications()) {
        
            return false;
        
        }

        if (!sequenceMatchingParameters.isSameAs(otherParameters.getSequenceMatchingParameters())) {
        
            return false;
        
        }

        return true;
        
    }
}
