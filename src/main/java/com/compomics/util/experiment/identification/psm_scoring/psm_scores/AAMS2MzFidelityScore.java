package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.spectra.MSnSpectrum;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.MathException;

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
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     *
     * @return the score of the match
     * 
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     * @throws org.apache.commons.math.MathException exception thrown if a math exception occurred when estimating the noise level 
     */
    public double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator) throws InterruptedException, MathException {

        int sequenceLength = peptide.getSequence().length();
        HashMap<Integer, Double> aaDeviations = new HashMap(sequenceLength);
        for (int i = 1; i <= sequenceLength; i++) {
            aaDeviations.put(i, specificAnnotationPreferences.getFragmentIonAccuracyInDa(spectrum.getMaxMz()));
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
        
        ArrayList<Double> mzDeviations = new ArrayList<>(aaDeviations.values());
        if (mzDeviations.isEmpty()) {
            return specificAnnotationPreferences.getFragmentIonAccuracyInDa(spectrum.getMaxMz());
        }

        return BasicMathFunctions.mean(mzDeviations);
    }
}
