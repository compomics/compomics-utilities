package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.util.ArrayList;
import org.apache.commons.math.MathException;

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
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this psm
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     *
     * @return the score of the match
     * 
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     * @throws org.apache.commons.math.MathException exception thrown if a math exception occurred when estimating the noise level 
     */
    public double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator) throws InterruptedException, MathException {

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences,
                spectrum, peptide);
        ArrayList<Double> mzDeviations = new ArrayList<>(matches.size());
        for (IonMatch ionMatch : matches) {
            double mzError = ionMatch.getAbsoluteError();
            mzDeviations.add(mzError);
        }
        if (mzDeviations.size() < 2) {
            return specificAnnotationPreferences.getFragmentIonAccuracyInDa(spectrum.getMaxMz());
        }
        double deviationUp = BasicMathFunctions.percentile(mzDeviations, 0.75);
        double deviationDown = BasicMathFunctions.percentile(mzDeviations, 0.25);
        return (deviationUp - deviationDown) / 2;
    }
}
