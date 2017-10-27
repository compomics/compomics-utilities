package com.compomics.util.parameters.identification.advanced;

import java.io.Serializable;

/**
 * The sequence matching options.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SequenceMatchingParameters implements Serializable {

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
    private double limitX = 0.25;

    /**
     * Constructor for empty preferences.
     */
    public SequenceMatchingParameters() {

    }

    /**
     * Default string matching.
     */
    public static final SequenceMatchingParameters defaultStringMatching = getStringMatching();

    /**
     * Returns preferences for simple string matching.
     *
     * @return preferences for simple string matching
     */
    public static SequenceMatchingParameters getStringMatching() {
        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType(MatchingType.string);
        return sequenceMatchingPreferences;
    }

    /**
     * Returns default preferences from amino acid matching.
     *
     * @return default preferences from amino acid matching
     */
    public static SequenceMatchingParameters getDefaultSequenceMatching() {
        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
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
    public double getLimitX() {
        return limitX;
    }

    /**
     * Sets the maximal share of X's a match can contain, range [0.0-1.0].
     *
     * @param limitX the maximal share of X's a match can contain
     */
    public void setLimitX(double limitX) {
        this.limitX = limitX;
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
    public boolean isSameAs(SequenceMatchingParameters sequenceMatchingPreferences) {

        if (sequenceMatchingType != sequenceMatchingPreferences.getSequenceMatchingType()) {

            return false;

        }

        if (limitX != sequenceMatchingPreferences.getLimitX()) {

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
