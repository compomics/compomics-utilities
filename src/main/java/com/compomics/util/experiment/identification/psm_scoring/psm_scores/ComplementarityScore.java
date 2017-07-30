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
import org.apache.commons.math.util.FastMath;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.MathException;

/**
 * The intensity sub-score as adapted from the DirecTag manuscript
 * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
 *
 * @author Marc Vaudel
 */
public class ComplementarityScore {

    /**
     * Log2.
     */
    private static final Double log2 = FastMath.log(2.0);

    /**
     * Scores the match between the given peptide and spectrum using the
     * complementarity of the matched peaks. For every residue, a list of
     * matched peaks is established and if any is found, the score per residue
     * is the log of the number of matched ions. The peptide score is the
     * average of the residue scores.
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

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences,
                spectrum, peptide);

        HashMap<Integer, Double> residueToMatchesMap = new HashMap<>(sequenceLength);
        for (int i = 1; i <= sequenceLength; i++) {
            residueToMatchesMap.put(i, 0.0);
        }
        for (IonMatch ionMatch : matches) {
            Ion ion = ionMatch.ion;
            if (ion instanceof PeptideFragmentIon) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                int number = peptideFragmentIon.getNumber();
                residueToMatchesMap.put(number, residueToMatchesMap.get(number) + 1);
            }
        }

        ArrayList<Double> scorePerResidue = new ArrayList<>(residueToMatchesMap.size());
        for (int number = 1; number <= sequenceLength; number++) {
            Double nIons = residueToMatchesMap.get(number);
            if (nIons != null) {
                scorePerResidue.add(FastMath.log(nIons) / log2);
            }
        }
        
        double mean = 0;
        
        if (!scorePerResidue.isEmpty()) {
            mean = BasicMathFunctions.mean(scorePerResidue);
        }

        return Math.pow(2, mean);
    }
}
