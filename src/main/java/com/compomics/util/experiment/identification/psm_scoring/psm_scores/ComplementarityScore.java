package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.preferences.AnnotationPreferences;
import com.compomics.util.preferences.SpecificAnnotationPreferences;
import org.apache.commons.math.util.FastMath;
import java.util.ArrayList;
import java.util.HashMap;

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
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationPreferences annotationPreferences, SpecificAnnotationPreferences specificAnnotationPreferences) {
        return getScore(peptide, spectrum, annotationPreferences, specificAnnotationPreferences, null);
    }

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
     * @param peptideSpectrumAnnotator an external annotator (if null an
     * internal will be used)
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationPreferences annotationPreferences, SpecificAnnotationPreferences specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator) {

        if (peptideSpectrumAnnotator == null) {
            peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();
        }

        int sequenceLength = peptide.getSequence().length();

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences,
                spectrum, peptide);

        HashMap<Integer, Double> residueToMatchesMap = new HashMap<Integer, Double>(sequenceLength);
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

        ArrayList<Double> scorePerResidue = new ArrayList<Double>(residueToMatchesMap.size());
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
