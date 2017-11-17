package com.compomics.util.experiment.identification.psm_scoring;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.peptide_fragmentation.PeptideFragmentationModel;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.PrecursorAccuracy;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.SnrScore;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.parameters.identification.IdentificationParameters;

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
     * Instance of the cross correlation score.
     */
    private HyperScore crossCorrelation;
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
        crossCorrelation = new HyperScore(peptideFragmentationModel);
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
     */
    public double getDecreasingScore(Peptide peptide, Integer peptideCharge, Spectrum spectrum, IdentificationParameters identificationParameters, 
            SpecificAnnotationParameters specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator, int scoreIndex) {
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
     */
    public double getScore(Peptide peptide, Integer peptideCharge, Spectrum spectrum, IdentificationParameters identificationParameters, 
            SpecificAnnotationParameters specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator, int scoreIndex) {
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
     */
    public double getScore(Peptide peptide, Integer peptideCharge, Spectrum spectrum, IdentificationParameters identificationParameters, 
            SpecificAnnotationParameters specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator, PsmScore psmScore) {
        switch (psmScore) {
            case native_score:
                throw new IllegalArgumentException("Impossible to compute the native score of an algorithm");
            case precursor_accuracy:
                return precursorAccuracy.getScore(peptide, peptideCharge, spectrum.getPrecursor(), 
                        identificationParameters.getSearchParameters().isPrecursorAccuracyTypePpm(), 
                        identificationParameters.getSearchParameters().getMinIsotopicCorrection(), 
                        identificationParameters.getSearchParameters().getMaxIsotopicCorrection());
            case hyperScore:
                return crossCorrelation.getScore(peptide, spectrum, identificationParameters.getAnnotationParameters(), 
                        specificAnnotationPreferences, peptideSpectrumAnnotator);
            default:
                throw new UnsupportedOperationException("Score not implemented.");
        }
    }
}
