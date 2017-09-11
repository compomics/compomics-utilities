package com.compomics.util.experiment.identification.psm_scoring;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.peptide_fragmentation.PeptideFragmentationModel;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.AAIntensityRankScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.AAMS2MzFidelityScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.ComplementarityScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.IntensityRankScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.MS2MzFidelityScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.PrecursorAccuracy;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.preferences.IdentificationParameters;
import org.apache.commons.math.MathException;

/**
 * This class can be used to estimate PSM scores.
 *
 * @author Marc Vaudel
 */
public class PsmScoresEstimator {

    /**
     * The peptide fragmentation model to use.
     */
    private PeptideFragmentationModel peptideFragmentationModel;
    /**
     * Instance of the AAIntensityRankScore.
     */
    private AAIntensityRankScore aaIntensityRankScore;
    /**
     * Instance of the AAMS2MzFidelityScore.
     */
    private AAMS2MzFidelityScore aaMS2MzFidelityScore;
    /**
     * Instance of the ComplementarityScore.
     */
    private ComplementarityScore complementarityScore;
    /**
     * Instance of the cross correlation score.
     */
    private HyperScore crossCorrelation;
    /**
     * Instance of the IntensityRankScore.
     */
    private IntensityRankScore intensityRankScore;
    /**
     * Instance of the MS2MzFidelityScore.
     */
    private MS2MzFidelityScore ms2MzFidelityScore;

    /**
     * Instance of the PrecursorAccuracy.
     */
    private PrecursorAccuracy precursorAccuracy;

    /**
     * Constructor.
     *
     * @param peptideFragmentationModel the peptide fragmentation model to use
     */
    public PsmScoresEstimator(PeptideFragmentationModel peptideFragmentationModel) {
        this.peptideFragmentationModel = peptideFragmentationModel;
        instantiateScores();
    }

    /**
     * Instantiates the different scores.
     */
    private void instantiateScores() {
        aaIntensityRankScore = new AAIntensityRankScore();
        aaMS2MzFidelityScore = new AAMS2MzFidelityScore();
        complementarityScore = new ComplementarityScore();
        crossCorrelation = new HyperScore(peptideFragmentationModel);
        intensityRankScore = new IntensityRankScore();
        ms2MzFidelityScore = new MS2MzFidelityScore();
        precursorAccuracy = new PrecursorAccuracy();
    }

    /**
     * Constructor using a uniform fragmentation.
     */
    public PsmScoresEstimator() {
        this(PeptideFragmentationModel.uniform);
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
     * to this PSM
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     * @param scoreIndex the index of the score to use
     *
     * @return the score of the match
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     * @throws org.apache.commons.math.MathException exception thrown if a math exception occurred when estimating the noise level 
     */
    public double getDecreasingScore(Peptide peptide, Integer peptideCharge, MSnSpectrum spectrum, IdentificationParameters identificationParameters, 
            SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator, int scoreIndex) 
            throws InterruptedException, MathException {
        PsmScore psmScore = PsmScore.getScore(scoreIndex);
        double score = getScore(peptide, peptideCharge, spectrum, identificationParameters, 
                specificAnnotationPreferences, peptideSpectrumAnnotator, psmScore);
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
     * to this PSM
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     * @param scoreIndex the index of the score to use
     *
     * @return the score of the match
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     * @throws org.apache.commons.math.MathException exception thrown if a math exception occurred when estimating the noise level 
     */
    public double getScore(Peptide peptide, Integer peptideCharge, MSnSpectrum spectrum, IdentificationParameters identificationParameters, 
            SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator, int scoreIndex) 
            throws InterruptedException, MathException {
        PsmScore psmScore = PsmScore.getScore(scoreIndex);
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
     * @throws org.apache.commons.math.MathException exception thrown if a math exception occurred when estimating the noise level 
     */
    public double getScore(Peptide peptide, Integer peptideCharge, MSnSpectrum spectrum, IdentificationParameters identificationParameters, 
            SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator, PsmScore psmScore) 
            throws InterruptedException, MathException {
        switch (psmScore) {
            case native_score:
                throw new IllegalArgumentException("Impossible to compute the native score of an algorithm");
            case precursor_accuracy:
                return precursorAccuracy.getScore(peptide, peptideCharge, spectrum.getPrecursor(), 
                        identificationParameters.getSearchParameters().isPrecursorAccuracyTypePpm(), 
                        identificationParameters.getSearchParameters().getMinIsotopicCorrection(), 
                        identificationParameters.getSearchParameters().getMaxIsotopicCorrection());
            case hyperScore:
                return crossCorrelation.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), 
                        specificAnnotationPreferences, peptideSpectrumAnnotator);
            case ms2_mz_fidelity:
                return ms2MzFidelityScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), 
                        specificAnnotationPreferences, peptideSpectrumAnnotator);
            case aa_ms2_mz_fidelity:
                return aaMS2MzFidelityScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), 
                        specificAnnotationPreferences, peptideSpectrumAnnotator);
            case intensity:
                return intensityRankScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), 
                        specificAnnotationPreferences, peptideSpectrumAnnotator);
            case aa_intensity:
                return aaIntensityRankScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), 
                        specificAnnotationPreferences, peptideSpectrumAnnotator);
            case complementarity:
                return complementarityScore.getScore(peptide, spectrum, identificationParameters.getAnnotationPreferences(), 
                        specificAnnotationPreferences, peptideSpectrumAnnotator);
            default:
                throw new UnsupportedOperationException("Score not implemented.");
        }
    }
}
