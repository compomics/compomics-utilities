package com.compomics.util.experiment.identification.peptide_shaker;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.experiment.identification.validation.MatchValidationLevel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.math.util.FastMath;

/**
 * PeptideShaker compomics utilities experiment customizable parameter. This
 * parameter will be added to spectrum, peptide and protein matches to score
 * them, indicate the estimated posterior error probability associated and flag
 * whether they have been validated or not.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PSParameter extends ExperimentObject implements UrParameter {

    /**
     * Serial version UID for post-serialization compatibility.
     */
    static final long serialVersionUID = 2846587135366515967L;
    /**
     * The difference in identification algorithm level PEP with the next best
     * peptide assumption.
     */
    private double algorithmDeltaPEP;
    /**
     * The difference in identification algorithm level PEP with the next best
     * peptide assumption with sequence difference across all search engines.
     */
    private double deltaPEP;
    /**
     * The score of the match.
     */
    private double score;
    /**
     * The probability of the match.
     */
    private double probability;
    /**
     * The validation level of a given match.
     */
    private MatchValidationLevel matchValidationLevel;
    /**
     * Boolean indicating whether the validation confidence was manually
     * updated.
     */
    private boolean manualValidation = false;
    /**
     * Boolean indicating whether this is a hidden match.
     */
    private boolean hidden = false;
    /**
     * Boolean indicating whether this is a starred match.
     */
    private boolean starred = false;
    /**
     * Protein groups can belong to the following groups according to the static
     * field indexing.
     */
    private int proteinInferenceGroupClass = NOT_GROUP;
    /**
     * Static index for a protein inference group: 0 - not a protein group or
     * unique peptide of single protein group.
     */
    public static final int NOT_GROUP = 0;
    /**
     * Static index for a protein group: 1 - related proteins or peptide from
     * related protein groups (not necessarily unique to the group).
     */
    public static final int RELATED = 1;
    /**
     * Static index for a protein group: 2 - related and a unrelated proteins or
     * peptide shared by related and unrelated proteins (not necessarily unique
     * to the group).
     */
    public static final int RELATED_AND_UNRELATED = 2;
    /**
     * Static index for a protein group: 3 - unrelated proteins proteins or
     * peptide shared by unrelated proteins.
     */
    public static final int UNRELATED = 3;
    /**
     * The fraction confidence map.
     */
    private HashMap<String, Double> fractionPEP = null;
    /**
     * The fraction confidence map.
     */
    private HashMap<String, Double> fractionScore = null;
    /**
     * The number of validated peptides per fraction.
     */
    private HashMap<String, Integer> validatedPeptidesPerFraction = null;
    /**
     * The number of validated spectra per fraction.
     */
    private HashMap<String, Integer> validatedSpectraPerFraction = null;
    /**
     * The precursor intensity per fraction.
     */
    private HashMap<String, ArrayList<Double>> precursorIntensityPerFraction = null;
    /**
     * The average precursor intensity per fraction.
     */
    private HashMap<String, Double> precursorIntensityAveragePerFraction = null;
    /**
     * The summed precursor intensity per fraction.
     */
    private HashMap<String, Double> precursorIntensitySummedPerFraction = null;
    /**
     * The results of the validation quality filters.
     */
    private HashMap<String, Boolean> qcFilters = null;
    /**
     * Map of the intermediate scores. Score index &gt; value
     */
    private HashMap<Integer, Double> intermediateScores;
    /**
     * An empty parameter used for instantiation.
     */
    public static final PSParameter dummy = new PSParameter();

    /**
     * Constructor.
     */
    public PSParameter() {
    }

    /**
     * Returns the match probability.
     *
     * @return the match probability
     */
    public double getProbability() {

        return probability;

    }

    /**
     * Set the probability.
     *
     * @param probability the new peptide posterior error probability
     */
    public void setProbability(double probability) {

        this.probability = probability;

    }

    public void setGroupClass(int groupClass) {

        this.proteinInferenceGroupClass = groupClass;

    }

    /**
     * Returns the score.
     *
     * @return the score
     */
    public double getScore() {

        return score;

    }

    /**
     * Returns the log transformed score.
     *
     * @return the log transformed score
     */
    public double getTransformedScore() {

        return transformScore(getScore());

    }

    /**
     * Set the peptide score.
     *
     * @param score the score
     */
    public void setScore(double score) {

        this.score = score;

    }

    /**
     * Returns the confidence.
     *
     * @return the confidence
     */
    public double getConfidence() {

        double confidence = 100.0 * (1 - probability);

        return confidence < 0.0 ? 0.0 : confidence;

    }

    /**
     * Returns the difference in identification algorithm level PEP with the
     * next best peptide assumption with sequence difference for the given
     * search engine.
     *
     * @return the difference in identification algorithm level PEP with the
     * next best peptide assumption with sequence difference for the given
     * search engine
     */
    public Double getAlgorithmDeltaPEP() {

        return algorithmDeltaPEP;

    }

    /**
     * Sets the difference in identification algorithm level PEP with the next
     * best peptide assumption with sequence difference for the given search
     * engine.
     *
     * @param deltaPEP the difference in identification algorithm level PEP with
     * the next best peptide assumption with sequence difference for the given
     * search engine
     */
    public void setAlgorithmDeltaPEP(double deltaPEP) {

        this.algorithmDeltaPEP = deltaPEP;

    }

    /**
     * Returns the difference in identification algorithm level PEP with the
     * next best peptide assumption with sequence difference across all search
     * engines.
     *
     * @return the difference in identification algorithm level PEP with the
     * next best peptide assumption with sequence difference across all search
     * engines
     */
    public double getDeltaPEP() {

        return deltaPEP;

    }

    /**
     * Sets the difference in identification algorithm level PEP with the next
     * best peptide assumption with sequence difference across all search
     * engines.
     *
     * @param deltaPEP the difference in identification algorithm level PEP with
     * the next best peptide assumption with sequence difference across all
     * search engines
     */
    public void setDeltaPEP(double deltaPEP) {

        this.deltaPEP = deltaPEP;

    }

    /**
     * Sets the qc filters.
     *
     * @param qcFilters the qc filters
     */
    public void setQcFilters(HashMap<String, Boolean> qcFilters) {

        this.qcFilters = qcFilters;

    }

    /**
     * Returns the validation level of the match.
     *
     * @return the validation level of the match
     */
    public MatchValidationLevel getMatchValidationLevel() {

        return matchValidationLevel;

    }

    /**
     * Sets the validation level of the match.
     *
     * @param matchValidationLevel the validation level of the match
     */
    public void setMatchValidationLevel(MatchValidationLevel matchValidationLevel) {

        this.matchValidationLevel = matchValidationLevel;

    }

    /**
     * Hide/Unhide a match.
     *
     * @param hidden boolean indicating whether the match should be hidden
     */
    public void setHidden(boolean hidden) {

        this.hidden = hidden;

    }

    /**
     * Returns whether a match is hidden or not.
     *
     * @return boolean indicating whether a match is hidden or not
     */
    public boolean getHidden() {

        return hidden;

    }

    /**
     * Star/Unstar a match.
     *
     * @param starred boolean indicating whether the match should be starred
     */
    public void setStarred(boolean starred) {

        this.starred = starred;

    }

    /**
     * Returns whether a match is starred or not.
     *
     * @return boolean indicating whether a match is starred or not
     */
    public boolean getStarred() {

        return starred;

    }

    /**
     * Returns the protein inference class of the protein match.
     *
     * @return the protein inference class of the protein match.
     */
    public int getProteinInferenceGroupClass() {

        return proteinInferenceGroupClass;

    }

    /**
     * Returns the protein inference class as a string for the given
     * integer-based class
     *
     * @return the group class description
     */
    public String getProteinInferenceClassAsString() {

        return getProteinInferenceClassAsString(proteinInferenceGroupClass);

    }

    /**
     * Returns the protein inference class as a string for the given
     * integer-based class.
     *
     * @param matchClass the protein inference class as integer (see static
     * fields)
     *
     * @return the group class description
     */
    public static String getProteinInferenceClassAsString(int matchClass) {

        switch (matchClass) {

            case NOT_GROUP:

                return "Single Protein";

            case RELATED:

                return "Related Proteins";

            case RELATED_AND_UNRELATED:

                return "Related and Unrelated Proteins";

            case UNRELATED:

                return "Unrelated Proteins";

            default:

                return "";

        }
    }

    /**
     * Sets the protein group class.
     *
     * @param groupClass the protein group class
     */
    public void setProteinInferenceClass(int groupClass) {

        this.proteinInferenceGroupClass = groupClass;

    }

    /**
     * Returns the number of validated peptides per fraction.
     *
     * @return the number of validated peptides per fraction
     */
    public HashMap<String, Integer> getValidatedPeptidesPerFraction() {

        return validatedPeptidesPerFraction;

    }

    /**
     * Returns the number of validated spectra per fraction.
     *
     * @return the number of validated spectra per fraction
     */
    public HashMap<String, Integer> getValidatedSpectraPerFraction() {

        return validatedSpectraPerFraction;
    }

    /**
     * Sets the fraction confidence.
     *
     * @param fraction the fraction
     * @param confidence the confidence
     */
    public void setFractionScore(String fraction, double confidence) {

        if (fractionScore == null) {

            fractionScore = new HashMap<>(2);

        }

        fractionScore.put(fraction, confidence);

    }

    /**
     * Sets the fraction score map.
     *
     * @param fractionScore the fraction score map
     */
    public void setFractionScore(HashMap<String, Double> fractionScore) {

        this.fractionScore = fractionScore;

    }

    /**
     * Returns the fraction score. Null if not found.
     *
     * @param fraction the fraction
     *
     * @return the fraction score
     */
    public Double getFractionScore(String fraction) {

        if (fractionScore == null) {

            return null;

        }

        return fractionScore.get(fraction);

    }

    /**
     * Return the fractions where this match was found. Null if not found.
     *
     * @return the fractions where this match was found
     */
    public Set<String> getFractions() {

        return fractionScore == null ? null
                : fractionScore.keySet();

    }

    /**
     * Return the fractions where this match was found. Null if not found.
     *
     * @return the fractions where this match was found
     */
    public HashMap<String, Double> getFractionScore() {

        return fractionScore;

    }

    /**
     * Sets the fraction confidence.
     *
     * @param fraction the fraction
     * @param confidence the confidence
     */
    public void setFractionPEP(String fraction, Double confidence) {

        if (fractionPEP == null) {

            fractionPEP = new HashMap<>(2);

        }

        fractionPEP.put(fraction, confidence);

    }

    public void setFractionPEP(HashMap<String, Double> fractionPEP) {

        this.fractionPEP = fractionPEP;

    }

    /**
     * Returns the fraction pep. Null if not found.
     *
     * @param fraction the fraction
     * @return the fraction pep
     */
    public Double getFractionPEP(String fraction) {

        return fractionPEP == null ? null : fractionPEP.get(fraction);

    }

    /**
     * Returns the fraction pep map.
     *
     * @return the fraction pep map
     */
    public HashMap<String, Double> getFractionPEP() {

        return fractionPEP;

    }

    /**
     * Returns the fraction confidence. Null if not found.
     *
     * @param fraction the fraction
     *
     * @return the fraction confidence
     */
    public Double getFractionConfidence(String fraction) {

        return fractionPEP == null || fractionPEP.get(fraction) == null ? null
                : 100 * (1 - fractionPEP.get(fraction));

    }

    /**
     * Get the number of validated peptides in the given fraction. Zero if not
     * found.
     *
     * @param fraction the fraction
     *
     * @return the number of validated peptides in the given fraction
     */
    public int getFractionValidatedPeptides(String fraction) {

        return validatedPeptidesPerFraction == null || validatedPeptidesPerFraction.get(fraction) == null ? 0
                : validatedPeptidesPerFraction.get(fraction);

    }

    /**
     * Get the number of validated peptides in the given fraction.
     *
     * @param validatedPeptidesPerFraction the validated peptides per fraction
     * map
     */
    public void setValidatedPeptidesPerFraction(HashMap<String, Integer> validatedPeptidesPerFraction) {

        this.validatedPeptidesPerFraction = validatedPeptidesPerFraction;

    }

    /**
     * Get the number of validated spectra in the given fraction. Zero if not
     * found.
     *
     * @param fraction the fraction
     * @return the number of validated spectra in the given fraction
     */
    public Integer getFractionValidatedSpectra(String fraction) {

        return validatedSpectraPerFraction == null || validatedSpectraPerFraction.get(fraction) == null ? 0
                : validatedSpectraPerFraction.get(fraction);

    }

    /**
     * Get the number of validated spectra in the given fraction.
     *
     * @param validatedSpectraPerFraction the validated spectra per fraction map
     */
    public void setValidatedSpectraPepFraction(HashMap<String, Integer> validatedSpectraPerFraction) {

        this.validatedSpectraPerFraction = validatedSpectraPerFraction;

    }

    /**
     * Get the precursor intensity in the given fraction. Null if not found.
     *
     * @param fraction the fraction
     * @return the precursor intensity in the given fraction
     */
    public ArrayList<Double> getPrecursorIntensityPerFraction(String fraction) {

        return precursorIntensityPerFraction == null ? new ArrayList<>(0)
                : precursorIntensityPerFraction.get(fraction);

    }

    /**
     * Returns the precursor intensity per fraction map.
     *
     * @return the precursor intensity per fraction map
     */
    public HashMap<String, ArrayList<Double>> getPrecursorIntensityPerFraction() {

        return precursorIntensityPerFraction;

    }

    /**
     * Sets the precursor intensity per fraction map.
     *
     * @param precursorIntensityAveragePerFraction the precursor intensity per
     * fraction map
     */
    public void setPrecursorIntensityAveragePerFraction(HashMap<String, Double> precursorIntensityAveragePerFraction) {

        this.precursorIntensityAveragePerFraction = precursorIntensityAveragePerFraction;

    }

    /**
     * Sets the summed precursor intensity per fraction map.
     *
     * @param precursorIntensitySummedPerFraction the summed precursor intensity
     * per fraction map
     */
    public void setPrecursorIntensitySummedPerFraction(HashMap<String, Double> precursorIntensitySummedPerFraction) {

        this.precursorIntensitySummedPerFraction = precursorIntensitySummedPerFraction;

    }

    /**
     * Get the precursor intensity in the given fraction.
     *
     * @param precursorIntensityPerFraction the precursor intensities per
     * fraction map
     */
    public void setPrecursorIntensityPerFraction(HashMap<String, ArrayList<Double>> precursorIntensityPerFraction) {

        this.precursorIntensityPerFraction = precursorIntensityPerFraction;

        for (Entry<String, ArrayList<Double>> entry : precursorIntensityPerFraction.entrySet()) {

            String fraction = entry.getKey();
            double sum = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            if (precursorIntensitySummedPerFraction == null) {

                precursorIntensitySummedPerFraction = new HashMap<>(2);

            }

            if (precursorIntensityAveragePerFraction == null) {

                precursorIntensityAveragePerFraction = new HashMap<>(2);

            }

            if (sum > 0) {

                precursorIntensitySummedPerFraction.put(fraction, sum);
                precursorIntensityAveragePerFraction.put(fraction, sum / precursorIntensityPerFraction.get(fraction).size());

            } else {

                precursorIntensitySummedPerFraction.put(fraction, null);
                precursorIntensityAveragePerFraction.put(fraction, null);

            }
        }
    }

    /**
     * Get the average precursor intensity in the given fraction. Null if not
     * found.
     *
     * @param fraction the fraction
     * @return the average precursor intensity in the given fraction
     */
    public Double getPrecursorIntensityAveragePerFraction(String fraction) {

        return precursorIntensityAveragePerFraction == null ? null
                : precursorIntensityAveragePerFraction.get(fraction);

    }

    /**
     * Returns the fraction precursor intensity average map.
     *
     * @return the fraction precursor intensity average map
     */
    public HashMap<String, Double> getPrecursorIntensityAveragePerFraction() {

        return precursorIntensityAveragePerFraction;

    }

    /**
     * Get the summed precursor intensity in the given fraction. Null if not
     * found.
     *
     * @param fraction the fraction
     * @return the summed precursor intensity in the given fraction
     */
    public Double getPrecursorIntensitySummedPerFraction(String fraction) {

        return precursorIntensitySummedPerFraction == null ? null
                : precursorIntensitySummedPerFraction.get(fraction);

    }

    /**
     * Returns the fraction summed intensity map.
     *
     * @return the fraction summed intensity map
     */
    public HashMap<String, Double> getPrecursorIntensitySummedPerFraction() {

        return precursorIntensitySummedPerFraction;

    }

    /**
     * Indicates whether the match validation was manually inspected.
     *
     * @return a boolean indicating whether the match validation was manually
     * inspected
     */
    public boolean getManualValidation() {

        return manualValidation;

    }

    /**
     * Sets whether the match validation was manually inspected.
     *
     * @param manualValidation a boolean indicating whether the match validation
     * was manually inspected
     */
    public void setManualValidation(boolean manualValidation) {

        this.manualValidation = manualValidation;

    }

    /**
     * Sets whether the match passed a quality control check.
     *
     * @param criterion the QC criterion
     * @param validated boolean indicating whether the test was passed
     */
    public void setQcResult(String criterion, boolean validated) {

        if (qcFilters == null) {

            qcFilters = new HashMap<>(1);

        }

        qcFilters.put(criterion, validated);

    }

    /**
     * Indicates whether the given QC check was passed. Null if not found.
     *
     * @param criterion the QC criterion
     *
     * @return a boolean indicating whether the test was passed
     */
    public Boolean isQcPassed(String criterion) {

        return qcFilters == null ? null
                : qcFilters.get(criterion);

    }

    /**
     * Returns the list of QC checks made for this match. Null if not found.
     *
     * @return the list of QC checks made for this match in a set
     */
    public Set<String> getQcCriteria() {

        return qcFilters == null ? new HashSet<>(0)
                : qcFilters.keySet();
    }

    /**
     * Returns the qc filters map.
     *
     * @return the qc filters map
     */
    public HashMap<String, Boolean> getQcFilters() {

        return qcFilters;

    }

    /**
     * Resets the results of the QC filters.
     */
    public void resetQcResults() {

        if (qcFilters == null) {

            qcFilters = new HashMap<>(1);

        } else {

            qcFilters.clear();

        }
    }

    /**
     * Indicates whether QC filters were implemented for this match.
     *
     * @return a boolean indicating whether QC filters were implemented for this
     * match
     */
    public boolean hasQcFilters() {

        return qcFilters != null && !qcFilters.isEmpty();

    }

    /**
     * Adds an intermediate score.
     *
     * @param scoreId the index of the score
     * @param score the value of the score
     */
    public void setIntermediateScore(Integer scoreId, Double score) {

        if (intermediateScores == null) {

            createIntermediateScoreMap();

        }

        intermediateScores.put(scoreId, score);

    }

    public void setIntermediateScores(HashMap<Integer, Double> intermediateScores) {

        this.intermediateScores = intermediateScores;

    }

    /**
     * Instantiates the intermediate scores map if null.
     */
    public synchronized void createIntermediateScoreMap() {

        if (intermediateScores == null) {

            intermediateScores = new HashMap<>(1);

        }
    }

    /**
     * Returns the desired intermediate score. Null if not found.
     *
     * @param scoreId the index of the score
     *
     * @return the intermediate score
     */
    public Double getIntermediateScore(int scoreId) {

        return intermediateScores == null ? null
                : intermediateScores.get(scoreId);

    }

    /**
     * Returns the intermediate scores map.
     *
     * @return the intermediate scores map
     */
    public HashMap<Integer, Double> getIntermediateScores() {

        return intermediateScores;

    }

    /**
     * Returns a score from a raw score where the score = -10*log(rawScore). The
     * maximum score is 100 and raw scores smaller or equal to zero have a score
     * of 100.
     *
     * @param rawScore the raw score
     *
     * @return the score
     */
    public static double transformScore(double rawScore) {

        double score;

        if (rawScore <= 0) {

            score = 100;

        } else {

            score = -10 * FastMath.log10(rawScore);

            if (score > 100) {

                score = 100;

            }
        }

        return score;

    }

    @Override
    public long getParameterKey() {

        return serialVersionUID;

    }
}
