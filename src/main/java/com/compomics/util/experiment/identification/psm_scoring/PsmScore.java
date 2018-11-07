package com.compomics.util.experiment.identification.psm_scoring;

/**
 * Enum listing the PSM scores implemented in compomics utilities.
 *
 * @author Marc Vaudel
 */
public enum PsmScore {

    /**
     * The native score of the search engine.
     */
    native_score(-1, "Native", false, "The algorithm native scor"),
    /**
     * The precursor accuracy.
     */
    precursor_accuracy(0, "Precursor accuracy", false, "Precursor accuracy score"),
    /**
     * Hyperscore as variation of the score implemented in X!Tandem www.thegpm.org/tandem.
     * See com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore for details.
     */
    hyperScore(1, "Hyperscore", true, "Hyperscore as variation of the score implemented in X!Tande."),
    /**
     * Signal to noise ratio score.
     */
    snrScore(2, "snr", false, "Signal to noise ratio score");

    /**
     * The index of the score of interest.
     */
    public final Integer index;
    /**
     * The name of the score.
     */
    public final String name;
    /**
     * Indicates whether the score increases with the quality of the match.
     */
    public final boolean increasing;
    /**
     * Short description of the score.
     */
    public final String description;

    /**
     * Constructor.
     *
     * @param index the index of the score
     * @param name the name of the score
     * @param increasing whether the score increases with the quality of the
     * match
     * @param description short description of the score
     */
    private PsmScore(int index, String name, boolean increasing, String description) {
        this.index = index;
        this.name = name;
        this.increasing = increasing;
        this.description = description;
    }

    /**
     * Returns the PSM score of the given index. Null if not found.
     *
     * @param scoreIndex the index of the desired score
     * @return the score of given index
     */
    public static PsmScore getScore(int scoreIndex) {
        for (PsmScore psmScore : values()) {
            if (psmScore.index == scoreIndex) {
                return psmScore;
            }
        }
        return null;
    }

    /**
     * Returns the PSM score of the given name. Null if not found.
     *
     * @param scoreName the name of the desired score
     *
     * @return the score of given name
     */
    public static PsmScore getScore(String scoreName) {
        for (PsmScore psmScore : values()) {
            if (psmScore.name.equals(scoreName)) {
                return psmScore;
            }
        }
        return null;
    }

    /**
     * Empty default constructor
     */
    private PsmScore() {
        index = null;
        name = "";
        increasing = false;
        description = "";
    }
}
