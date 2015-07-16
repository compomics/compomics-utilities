package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.mutations.MutationMatrix;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.Serializable;

/**
 * The sequence matching options.
 *
 * @author Marc Vaudel
 */
public class SequenceMatchingPreferences implements Serializable {

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
         * Matches amino acids of indistinguishable masses.
         */
        indistiguishableAminoAcids;
    }

    /**
     * Serialization number for backward compatibility.
     */
    static final long serialVersionUID = 228961121369106450L;
    /**
     * The amino acid matching type.
     */
    private MatchingType sequenceMatchingType;
    /**
     * The ms2 m/z tolerance to use for amino acid distinction when
     * sequenceMatchingType is indistiguishableAminoAcids. Can be null
     * otherwise.
     */
    private Double ms2MzTolerance = null;
    /**
     * Limit the share of Xs a match can contain.
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
     * @param searchParameters the identification parameters used
     *
     * @return default preferences from amino acid matching
     */
    public static SequenceMatchingPreferences getDefaultSequenceMatching(SearchParameters searchParameters) {
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setMs2MzTolerance(searchParameters.getFragmentIonAccuracy());
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
     * Returns the ms2 m/z tolerance for amino acid distinction.
     *
     * @return the ms2 m/z tolerance for amino acid distinction
     */
    public Double getMs2MzTolerance() {
        return ms2MzTolerance;
    }

    /**
     * Sets the ms2 m/z tolerance for amino acid distinction.
     *
     * @param ms2MzTolerance the ms2 m/z tolerance for amino acid distinction
     */
    public void setMs2MzTolerance(Double ms2MzTolerance) {
        this.ms2MzTolerance = ms2MzTolerance;
    }

    /**
     * Returns the maximal share of Xs a match can contain. Null if not set.
     *
     * @return the maximal share of Xs a match can contain
     */
    public Double getLimitX() {
        return limitX;
    }

    /**
     * Indicates whether the share of Xs should be limited.
     *
     * @return whether the share of Xs should be limited
     */
    public boolean hasLimitX() {
        return limitX != null && limitX >= 0;
    }

    /**
     * Sets the maximal share of Xs a match can contain.
     *
     * @param limitX the maximal share of Xs a match can contain
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
        if (ms2MzTolerance != null && proteinInferencePreferences.getMs2MzTolerance() != null) {
            double diff = Math.abs(ms2MzTolerance - proteinInferencePreferences.getMs2MzTolerance());
            if (diff > 0.0000000000001) {
                return false;
            }
        }
        if (ms2MzTolerance == null && proteinInferencePreferences.getMs2MzTolerance() != null) {
            return false;
        }
        if (ms2MzTolerance != null && proteinInferencePreferences.getMs2MzTolerance() == null) {
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
}
