package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.mutations.MutationMatrix;
import java.io.Serializable;

/**
 * The sequence matching options.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SequenceMatchingPreferences implements Serializable {

    /**
     * Serialization number for backward compatibility.
     */
    static final long serialVersionUID = 228961121369106450L;
    /**
     * The different types of amino acid matching.
     */
    public static enum MatchingType {

        /**
         * Matches character strings only.
         */
        string,
        /**
         * Matches amino acids.
         */
        aminoAcid,
        /**
         * Matches amino acids of indistinguishable masses (I/L).
         */
        indistiguishableAminoAcids;

        @Override
        public String toString() {
            switch (this) {
                case string:
                    return "Characters Sequence";
                case aminoAcid:
                    return "Amino Acids";
                case indistiguishableAminoAcids:
                    return "Indistinguishable Amino Acids";
                default:
                    throw new UnsupportedOperationException("Not implemented for matching type " + this.name() + ".");
            }
        }
    }
    /**
     * The amino acid matching type.
     */
    private MatchingType sequenceMatchingType;
    /**
     * Limit the share of X's a match can contain, range [0.0-1.0].
     */
    private Double limitX = null;
    /**
     * Matrix of allowed mutations.
     */
    private MutationMatrix mutationMatrix = null;
    /**
     * The maximal number of mutations allowed per peptide.
     */
    private Integer maxMutationsPerPeptide = null;

    /**
     * Constructor for empty preferences.
     */
    public SequenceMatchingPreferences() {

    }

    /**
     * Default string matching.
     */
    public static final SequenceMatchingPreferences defaultStringMatching = getStringMatching();

    /**
     * Returns preferences for simple string matching.
     *
     * @return preferences for simple string matching
     */
    public static SequenceMatchingPreferences getStringMatching() {
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(MatchingType.string);
        return sequenceMatchingPreferences;
    }

    /**
     * Returns default preferences from amino acid matching.
     *
     * @return default preferences from amino acid matching
     */
    public static SequenceMatchingPreferences getDefaultSequenceMatching() {
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);
        return sequenceMatchingPreferences;
    }

    /**
     * Returns the sequence matching type.
     *
     * @return the sequence matching type
     */
    public MatchingType getSequenceMatchingType() {
        return sequenceMatchingType;
    }

    /**
     * Sets the sequence matching type.
     *
     * @param sequenceMatchingType the sequence matching type
     */
    public void setSequenceMatchingType(MatchingType sequenceMatchingType) {
        this.sequenceMatchingType = sequenceMatchingType;
    }

    /**
     * Returns the maximal share of X's a match can contain, range [0.0-1.0].
     * Null if not set.
     *
     * @return the maximal share of X's a match can contain
     */
    public Double getLimitX() {
        return limitX;
    }

    /**
     * Indicates whether the share of X's should be limited.
     *
     * @return whether the share of X's should be limited
     */
    public boolean hasLimitX() {
        return limitX != null && limitX >= 0;
    }

    /**
     * Sets the maximal share of X's a match can contain, range [0.0-1.0].
     *
     * @param limitX the maximal share of X's a match can contain
     */
    public void setLimitX(Double limitX) {
        this.limitX = limitX;
    }

    /**
     * Returns the mutation matrix to use. Null if not set.
     *
     * @return the mutation matrix to use
     */
    public MutationMatrix getMutationMatrix() {
        return mutationMatrix;
    }

    /**
     * Sets the mutation matrix to use.
     *
     * @param mutationMatrix the mutation matrix to use
     */
    public void setMutationMatrix(MutationMatrix mutationMatrix) {
        this.mutationMatrix = mutationMatrix;
    }

    /**
     * Indicates whether a mutation matrix shall be used.
     *
     * @return whether a mutation matrix shall be used
     */
    public boolean hasMutationMatrix() {
        return mutationMatrix != null;
    }

    /**
     * Returns the maximal number of mutations allowed per peptide. Null if not
     * set.
     *
     * @return the maximal number of mutations allowed per peptide
     */
    public Integer getMaxMutationsPerPeptide() {
        return maxMutationsPerPeptide;
    }

    /**
     * Sets the maximal number of mutations allowed per peptide.
     *
     * @param maxMutationsPerPeptide the maximal number of mutations allowed per
     * peptide
     */
    public void setMaxMutationsPerPeptide(Integer maxMutationsPerPeptide) {
        this.maxMutationsPerPeptide = maxMutationsPerPeptide;
    }

    /**
     * Indicates whether another protein inference preferences is the same as
     * this one.
     *
     * @param proteinInferencePreferences the other protein inference
     * preferences
     *
     * @return whether another protein inference preferences is the same as this
     * one
     */
    public boolean isSameAs(SequenceMatchingPreferences proteinInferencePreferences) {
        if (sequenceMatchingType != proteinInferencePreferences.getSequenceMatchingType()) {
            return false;
        }
        if (hasLimitX() && proteinInferencePreferences.hasLimitX()) {
            double diff = Math.abs(limitX - proteinInferencePreferences.getLimitX());
            if (diff > 0.0000000000001) {
                return false;
            }
        }
        if (hasLimitX() && !proteinInferencePreferences.hasLimitX()) {
            return false;
        }
        if (!hasLimitX() && proteinInferencePreferences.hasLimitX()) {
            return false;
        }
        if (hasMutationMatrix() && proteinInferencePreferences.hasMutationMatrix() && !mutationMatrix.isSameAs(proteinInferencePreferences.getMutationMatrix())) {
            return false;
        }
        if (!hasMutationMatrix() && proteinInferencePreferences.hasMutationMatrix()) {
            return false;
        }
        if (hasMutationMatrix() && !proteinInferencePreferences.hasMutationMatrix()) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        
        String newLine = System.getProperty("line.separator");
        
        StringBuilder output = new StringBuilder();
        
        output.append("Method: ").append(sequenceMatchingType).append(".").append(newLine);
        output.append("Max share of x's: ").append(limitX).append(".").append(newLine);

        return output.toString();
    }
}
