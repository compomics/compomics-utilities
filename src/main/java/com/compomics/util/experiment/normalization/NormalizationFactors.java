package com.compomics.util.experiment.normalization;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class grouping the normalization factors used to normalize quantification
 * results.
 *
 * @author Marc Vaudel
 */
public class NormalizationFactors implements Serializable {

    /**
     * List of protein level normalization factors. The key should be the same
     * as for the sample assignment.
     */
    private HashMap<String, Double> proteinNormalizationFactors = new HashMap<>();
    /**
     * List of peptide level normalization factors. The key should be the same
     * as for the sample assignment.
     */
    private HashMap<String, Double> peptideNormalizationFactors = new HashMap<>();
    /**
     * List of PSM level normalization factors. The key should be the same as
     * for the sample assignment.
     */
    private HashMap<String, Double> psmNormalizationFactors = new HashMap<>();

    /**
     * Constructor.
     */
    public NormalizationFactors() {

    }

    /**
     * Indicates whether normalization factors are set.
     *
     * @return a boolean indicating whether the protein normalization factors
     * are set
     */
    public boolean hasNormalizationFactors() {
        return hasProteinNormalisationFactors() || hasPeptideNormalisationFactors() || hasPsmNormalisationFactors();
    }

    /**
     * Indicates whether the protein normalization factors are set.
     *
     * @return a boolean indicating whether the protein normalization factors
     * are set
     */
    public boolean hasProteinNormalisationFactors() {
        return !proteinNormalizationFactors.isEmpty();
    }

    /**
     * Indicates whether the peptide normalization factors are set.
     *
     * @return a boolean indicating whether the peptide normalization factors
     * are set
     */
    public boolean hasPeptideNormalisationFactors() {
        return !peptideNormalizationFactors.isEmpty();
    }

    /**
     * Indicates whether the PSM normalization factors are set.
     *
     * @return a boolean indicating whether the PSM normalization factors are
     * set
     */
    public boolean hasPsmNormalisationFactors() {
        return !psmNormalizationFactors.isEmpty();
    }

    /**
     * Resets the protein normalization factors.
     */
    public void resetProteinNormalisationFactors() {
        proteinNormalizationFactors.clear();
    }

    /**
     * Resets the peptide normalization factors.
     */
    public void resetPeptideNormalisationFactors() {
        peptideNormalizationFactors.clear();
    }

    /**
     * Resets the PSM normalization factors.
     */
    public void resetPsmNormalisationFactors() {
        psmNormalizationFactors.clear();
    }

    /**
     * Adds a protein normalization factor.
     *
     * @param sampleName the index of the sample
     * @param normalisationFactor the normalization factor
     */
    public void addProteinNormalisationFactor(String sampleName, double normalisationFactor) {
        proteinNormalizationFactors.put(sampleName, normalisationFactor);
    }

    /**
     * Adds a peptide normalization factor.
     *
     * @param sampleName the index of the sample
     * @param normalisationFactor the normalization factor
     */
    public void addPeptideNormalisationFactor(String sampleName, double normalisationFactor) {
        peptideNormalizationFactors.put(sampleName, normalisationFactor);
    }

    /**
     * Adds a PSM normalization factor.
     *
     * @param sampleName the index of the sample
     * @param normalisationFactor the normalization factor
     */
    public void addPsmNormalisationFactor(String sampleName, double normalisationFactor) {
        psmNormalizationFactors.put(sampleName, normalisationFactor);
    }

    /**
     * Returns the protein normalization factor for the given sample, 1.0 if not
     * set.
     *
     * @param sampleName the index of the sample
     *
     * @return the normalization factor
     */
    public double getProteinNormalisationFactor(String sampleName) {
        Double normalisationFactor = proteinNormalizationFactors.get(sampleName);
        if (normalisationFactor == null) {
            return 1.0;
        }
        return normalisationFactor;
    }

    /**
     * Returns the peptide normalization factor for the given sample, 1.0 if not
     * set.
     *
     * @param sampleName the index of the sample
     *
     * @return the normalization factor
     */
    public double getPeptideNormalisationFactor(String sampleName) {
        Double normalisationFactor = peptideNormalizationFactors.get(sampleName);
        if (normalisationFactor == null) {
            return 1.0;
        }
        return normalisationFactor;
    }

    /**
     * Returns the PSM normalization factor for the given sample, 1.0 if not
     * set.
     *
     * @param sampleName the index of the sample
     *
     * @return the normalization factor
     */
    public double getPsmNormalisationFactor(String sampleName) {
        Double normalisationFactor = psmNormalizationFactors.get(sampleName);
        if (normalisationFactor == null) {
            return 1.0;
        }
        return normalisationFactor;
    }
}
