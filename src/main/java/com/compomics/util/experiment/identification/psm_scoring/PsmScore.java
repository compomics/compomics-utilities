package com.compomics.util.experiment.identification.psm_scoring;

import com.compomics.util.experiment.ShotgunProtocol;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.AAIntensityRankScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.AAMS2MzFidelityScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.ComplementarityScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.IntensityRankScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.MS2MzFidelityScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.PrecursorAccuracy;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;

/**
 * Enum listing the PSM scores implemented in compomics utilities.
 *
 * @author Marc Vaudel
 */
public enum PsmScore {

    /**
     * The native score of the search engine.
     */
    native_score(-1, "native", false, "The algorithm native score"),
    /**
     * The precursor accuracy.
     */
    precursor_accuracy(0, "precursor accuracy", false, "Precursor accuracy score"),
    /**
     * The m/z fidelity score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
     */
    ms2_mz_fidelity(1, "fragment ion mz fildelity", false, "Fragment ion m/z fidelity score"),
    /**
     * The m/z fidelity score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943) per amino acid.
     */
    aa_ms2_mz_fidelity(2, "AA fragment ion mz fildelity", false, "Fragment ion m/z fidelity score per amino acid"),
    /**
     * The intensity sub-score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
     */
    intensity(3, "intensity", true, "Intensity score"),
    /**
     * The intensity sub-score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943) per amino acid.
     */
    aa_intensity(4, "AA intensity", false, "Intensity score per amino acid"),
    /**
     * The complementarity score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
     */
    complementarity(5, "complementarity", true, "Ion complementarity score");

    /**
     * The index of the score of interest.
     */
    public final int index;
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
     * Scores the match between the given peptide and spectrum using the given
     * score. The score is forced to decrease with the quality of the match by
     * taking the opposite value when relevant.
     *
     * @param peptide the peptide of interest
     * @param peptideCharge the charge of the peptide
     * @param spectrum the spectrum of interest
     * @param identificationParameters the identification parameters
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this psm
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     * @param scoreIndex the index of the score to use
     *
     * @return the score of the match
     * 
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public static double getDecreasingScore(Peptide peptide, Integer peptideCharge, MSnSpectrum spectrum, IdentificationParameters identificationParameters, SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator, int scoreIndex) throws InterruptedException {
        PsmScore psmScore = getScore(scoreIndex);
        double score = getScore(peptide, peptideCharge, spectrum, identificationParameters, specificAnnotationPreferences, peptideSpectrumAnnotator, psmScore);
        if (psmScore.increasing) {
            return -score;
        }
        return score;
    }

    /**
     * Scores the match between the given peptide and spectrum using the given
     * score.
     *
     * @param peptide the peptide of interest
     * @param peptideCharge the charge of the peptide
     * @param spectrum the spectrum of interest
     * @param identificationParameters the identification parameters
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this psm
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     * @param scoreIndex the index of the score to use
     *
     * @return the score of the match
     * 
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public static double getScore(Peptide peptide, Integer peptideCharge, MSnSpectrum spectrum, IdentificationParameters identificationParameters, SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator, int scoreIndex) throws InterruptedException {
        PsmScore psmScore = getScore(scoreIndex);
        return getScore(peptide, peptideCharge, spectrum, identificationParameters, specificAnnotationPreferences, peptideSpectrumAnnotator, psmScore);
    }

    /**
     * Scores the match between the given peptide and spectrum using the given
     * score.
     *
     * @param peptide the peptide of interest
     * @param peptideCharge the charge of the peptide
     * @param spectrum the spectrum of interest
     * @param identificationParameters the identification parameters
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this psm
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     * @param psmScore the score to use
     *
     * @return the score of the match
     * 
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public static double getScore(Peptide peptide, Integer peptideCharge, MSnSpectrum spectrum, IdentificationParameters identificationParameters, SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator, PsmScore psmScore) throws InterruptedException {
        switch (psmScore) {
            case native_score:
                throw new IllegalArgumentException("Impossible to compute the native score of an algorithm");
            case precursor_accuracy:
                return PrecursorAccuracy.getScore(peptide, peptideCharge, spectrum.getPrecursor(), identificationParameters.getSearchParameters().isPrecursorAccuracyTypePpm(), identificationParameters.getSearchParameters().getMinIsotopicCorrection(), identificationParameters.getSearchParameters().getMaxIsotopicCorrection());
            case ms2_mz_fidelity:
                return MS2MzFidelityScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), specificAnnotationPreferences, peptideSpectrumAnnotator);
            case aa_ms2_mz_fidelity:
                return AAMS2MzFidelityScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), specificAnnotationPreferences, peptideSpectrumAnnotator);
            case intensity:
                return IntensityRankScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), specificAnnotationPreferences, peptideSpectrumAnnotator);
            case aa_intensity:
                return AAIntensityRankScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), specificAnnotationPreferences, peptideSpectrumAnnotator);
            case complementarity:
                return ComplementarityScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), specificAnnotationPreferences, peptideSpectrumAnnotator);
            default:
                throw new UnsupportedOperationException("Score not implemented.");
        }
    }

}
