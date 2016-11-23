package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The intensity rank sub-score as adapted from the DirecTag paper where the
 * minimal value per amino-scid is retained
 * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
 *
 * @author Marc Vaudel
 */
public class AAIntensityRankScore {

    /**
     * The number of bins for the spectrum intensities.
     */
    public final int nBins = 10;

    /**
     * Scores the match between the given peptide and spectrum using the
     * intensity rank of the matched peaks. For every amino-acid, the rank of
     * the most intense peak is taken and the average value over the sequence is
     * returned.
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
     */
    public double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator peptideSpectrumAnnotator) throws InterruptedException {

        int sequenceLength = peptide.getSequence().length();
        HashMap<Integer, Double> aaIntensities = new HashMap(sequenceLength);
        for (int i = 1; i <= sequenceLength; i++) {
            aaIntensities.put(i, 0.0);
        }

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences,
                spectrum, peptide);
        for (IonMatch ionMatch : matches) {
            Ion ion = ionMatch.ion;
            if (ion instanceof PeptideFragmentIon) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                int number = peptideFragmentIon.getNumber();
                double intensity = aaIntensities.get(number),
                        tempIntensity = ionMatch.peak.intensity;
                if (tempIntensity > intensity) {
                    aaIntensities.put(number, tempIntensity);
                }
            }
        }

        int percentile = spectrum.getNPeaks() / nBins;
        HashMap<Double, ArrayList<Peak>> intensityMap = spectrum.getIntensityMap();
        ArrayList<Double> intensities = new ArrayList<Double>(intensityMap.keySet());
        Collections.sort(intensities, Collections.reverseOrder());
        ArrayList<Double> thresholds = new ArrayList<Double>(100);
        int count = 0;
        for (double intensity : intensities) {
            if (++count == percentile) {
                thresholds.add(intensity);
                count = 0;
            }
        }

        HashMap<Integer, Double> aaPercentile = new HashMap<Integer, Double>(sequenceLength);
        for (int aa : aaIntensities.keySet()) {
            double intensity = aaIntensities.get(aa);
            double rank = nBins;
            if (intensity > 0) {
                rank = 0;
                for (double threshold : thresholds) {
                    if (intensity >= threshold) {
                        break;
                    } else {
                        rank++;
                    }
                }
            }
            aaPercentile.put(aa, rank);
        }

        ArrayList<Double> ranks = new ArrayList<Double>(aaPercentile.values());
        return BasicMathFunctions.mean(ranks);
    }
}
