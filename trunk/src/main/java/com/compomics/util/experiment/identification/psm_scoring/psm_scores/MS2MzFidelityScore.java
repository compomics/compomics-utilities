package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.preferences.AnnotationPreferences;
import com.compomics.util.preferences.SpecificAnnotationPreferences;
import java.util.ArrayList;

/**
 * The m/z fidelity sub-score as adapted from the DirecTag manuscript
 * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
 *
 * @author Marc Vaudel
 */
public class MS2MzFidelityScore {

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. The mass interquartile distance of the fragment ion mass
     * error is used as m/z fidelity score.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param annotationPreferences the general spectrum annotation preferences
     * @param specificAnnotationPreferences the annotation preferences specific to this psm
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationPreferences annotationPreferences, SpecificAnnotationPreferences specificAnnotationPreferences) {
        return getScore(peptide, spectrum, annotationPreferences, specificAnnotationPreferences, null);
    }

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. The mass interquartile distance of the fragment ion mass
     * error is used as m/z fidelity score.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param annotationPreferences the general spectrum annotation preferences
     * @param specificAnnotationPreferences the annotation preferences specific to this psm
     * @param peptideSpectrumAnnotator an external annotator (if null an
     * internal will be used)
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationPreferences annotationPreferences, SpecificAnnotationPreferences specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator) {

        if (peptideSpectrumAnnotator == null) {
            peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();
        }

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences,
                spectrum, peptide);
        ArrayList<Double> mzDeviations = new ArrayList<Double>(matches.size());
        for (IonMatch ionMatch : matches) {
            double mzError = ionMatch.getAbsoluteError();
            mzDeviations.add(mzError);
        }
        if (mzDeviations.size() < 2) {
            return specificAnnotationPreferences.getFragmentIonAccuracy();
        }
        double deviationUp = BasicMathFunctions.percentile(mzDeviations, 0.75);
        double deviationDown = BasicMathFunctions.percentile(mzDeviations, 0.25);
        return (deviationUp - deviationDown) / 2;
    }
}
