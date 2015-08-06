package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The m/z fidelity sub-score as adapted from the DirecTag paper where the
 * minimal value per amino-scid is retained
 * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
 *
 * @author Marc Vaudel
 */
public class AAMS2MzFidelityScore {

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. Returns the average over the peptide sequence of the
     * minimal mass error of the ions annotating an amino acid.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param annotationPreferences the general spectrum annotation preferences
     * @param specificAnnotationPreferences the annotation preferences specific to this psm
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences) {
        return getScore(peptide, spectrum, annotationPreferences, specificAnnotationPreferences, null);
    }

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. Returns the average over the peptide sequence of the
     * minimal mass error of the ions annotating an amino acid.
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
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator) {

        if (peptideSpectrumAnnotator == null) {
            peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();
        }

        int sequenceLength = peptide.getSequence().length();
        HashMap<Integer, Double> aaDeviations = new HashMap(sequenceLength);
        for (int i = 1; i <= sequenceLength; i++) {
            aaDeviations.put(i, specificAnnotationPreferences.getFragmentIonAccuracy());
        }

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences,
                spectrum, peptide);
        
        for (IonMatch ionMatch : matches) {
            Ion ion = ionMatch.ion;
            if (ion instanceof PeptideFragmentIon) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                int number = peptideFragmentIon.getNumber();
                double error = aaDeviations.get(number),
                        tempError = Math.abs(ionMatch.getAbsoluteError());
                if (tempError < error) {
                    aaDeviations.put(number, tempError);
                }
            }
        }
        
        ArrayList<Double> mzDeviations = new ArrayList<Double>(aaDeviations.values());
        if (mzDeviations.isEmpty()) {
            return specificAnnotationPreferences.getFragmentIonAccuracy();
        }

        return BasicMathFunctions.mean(mzDeviations);
    }
}
