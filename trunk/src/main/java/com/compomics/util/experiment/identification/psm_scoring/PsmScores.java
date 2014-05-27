package com.compomics.util.experiment.identification.psm_scoring;

/**
 * Enum listing the PSM scores implemented in compomics utilities.
 *
 * @author Marc Vaudel
 */
public enum PsmScores {

    /**
     * The intensity sub-score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
     */
    intensity(0, "intensity"),
    /**
     * The m/z fidelity score as described in the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
     */
    ms2_mz_fidelity(1, "fragment ion m/z fildelity"),
    /**
     * The complementarity score as described in the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
     */
    complementarity(2, "complementarity");

    /**
     * The name of the score.
     */
    public final String name;
    /**
     * The index of the score of interest.
     */
    public final int index;

    /**
     * Constructor.
     *
     * @param index the index of the score
     * @param name the name of the score
     */
    private PsmScores(int index, String name) {
        this.index = index;
        this.name = name;
    }
}
