package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.mutations.MutationMatrix;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapperType;
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
        string(0, "Character Sequence"),
        /**
         * Matches amino acids.
         */
        aminoAcid(1, "Amino Acids"),
        /**
         * Matches amino acids of indistinguishable masses (I/L).
         */
        indistiguishableAminoAcids(2, "Indistinguishable Amino Acids");

        /**
         * The index of the type as integer.
         */
        public final int index;

        /**
         * The description.
         */
        public final String description;

        /**
         * Constructor.
         *
         * @param index the index
         * @param description the description
         */
        private MatchingType(int index, String description) {
            this.index = index;
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        /**
         * Returns the different matching types as command line options
         * description.
         *
         * @return the different matching types as command line options
         * description
         */
        public static String getCommandLineOptions() {
            StringBuilder optionsStringBuilder = new StringBuilder();
            for (MatchingType matchingType : values()) {
                if (optionsStringBuilder.length() != 0) {
                    optionsStringBuilder.append(", ");
                }
                optionsStringBuilder.append(matchingType.index).append(": ").append(matchingType.description);
            }
            return optionsStringBuilder.toString();
        }

        /**
         * Returns the matching type corresponding to the given index.
         *
         * @param index the index of the matching type
         *
         * @return the matching type
         */
        public static MatchingType getMatchingType(int index) {
            for (MatchingType matchingType : values()) {
                if (matchingType.index == index) {
                    return matchingType;
                }
            }
            throw new IllegalArgumentException("No matching type found for index " + index + ".");
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
     * The peptide mapper to use, FMI by default.
     */
    private PeptideMapperType peptideMapperType = PeptideMapperType.tree;

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
     * Returns the type of peptide mapper to use.
     *
     * @return the type of peptide mapper to use
     */
    public PeptideMapperType getPeptideMapperType() {
        if (peptideMapperType == null) { // Backward compatibility.
            peptideMapperType = PeptideMapperType.tree;
        }
        return peptideMapperType;
    }

    /**
     * Sets the type of peptide mapper to use.
     *
     * @param peptideMapperEnum the type of peptide mapper to use
     */
    public void setPeptideMapperType(PeptideMapperType peptideMapperEnum) {
        this.peptideMapperType = peptideMapperEnum;
    }

    /**
     * Indicates whether another sequence matching preferences is the same as
     * this one.
     *
     * @param sequenceMatchingPreferences the other sequence matching
     * preferences
     *
     * @return whether another sequence matching preferences is the same as this
     * one
     */
    public boolean isSameAs(SequenceMatchingPreferences sequenceMatchingPreferences) {
        if (peptideMapperType != sequenceMatchingPreferences.getPeptideMapperType()) {
            return false;
        }
        if (sequenceMatchingType != sequenceMatchingPreferences.getSequenceMatchingType()) {
            return false;
        }
        if (hasLimitX() && sequenceMatchingPreferences.hasLimitX()) {
            double diff = Math.abs(limitX - sequenceMatchingPreferences.getLimitX());
            if (diff > 0.0000000000001) {
                return false;
            }
        }
        if (hasLimitX() && !sequenceMatchingPreferences.hasLimitX()) {
            return false;
        }
        if (!hasLimitX() && sequenceMatchingPreferences.hasLimitX()) {
            return false;
        }
        if (hasMutationMatrix() && sequenceMatchingPreferences.hasMutationMatrix() && !mutationMatrix.isSameAs(sequenceMatchingPreferences.getMutationMatrix())) {
            return false;
        }
        if (!hasMutationMatrix() && sequenceMatchingPreferences.hasMutationMatrix()) {
            return false;
        }
        if (hasMutationMatrix() && !sequenceMatchingPreferences.hasMutationMatrix()) {
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

        output.append("Index: ").append(peptideMapperType).append(".").append(newLine);
        output.append("Method: ").append(sequenceMatchingType).append(".").append(newLine);
        output.append("Max share of x's: ").append(limitX).append(".").append(newLine);

        return output.toString();
    }

}
