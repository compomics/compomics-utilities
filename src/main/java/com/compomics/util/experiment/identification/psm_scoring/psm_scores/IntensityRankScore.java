package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The intensity rank sub-score as adapted from the DirecTag paper
 * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
 *
 * @author Marc Vaudel
 */
public class IntensityRankScore {

    /**
     * Scores the match between the given peptide and spectrum using the
     * intensity rank of the matched peaks. The score goes from the most intense
     * peaks to the lowest and returns the intensity rank at which more than 1%
     * of the total number of peaks is not annotated.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param iontypes the fragment ions to annotate
     * @param neutralLosses the neutral losses to annotate
     * @param charges the fragment charges to look for
     * @param identificationCharge the precursor charge
     * @param mzTolerance the ms2 m/z tolerance
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Ion.IonType, HashSet<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int identificationCharge, double mzTolerance) {
        return getScore(peptide, spectrum, iontypes, neutralLosses, charges, identificationCharge, mzTolerance, null);
    }

    /**
     * Scores the match between the given peptide and spectrum using the
     * intensity rank of the matched peaks. The score goes from the most intense
     * peaks to the lowest and returns the intensity rank at which more than 1%
     * of the total number of peaks is not annotated.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param iontypes the fragment ions to annotate
     * @param neutralLosses the neutral losses to annotate
     * @param charges the fragment charges to look for
     * @param identificationCharge the precursor charge
     * @param mzTolerance the ms2 m/z tolerance
     * @param peptideSpectrumAnnotator an external annotator (if null an
     * internal will be used)
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Ion.IonType, HashSet<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int identificationCharge, double mzTolerance, PeptideSpectrumAnnotator peptideSpectrumAnnotator) {

        if (peptideSpectrumAnnotator == null) {
            peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();
        }

        double nMissedTolerance = 10 * ((double) spectrum.getNPeaks()) / 100;

        HashMap<Double, ArrayList<Peak>> intensityMap = spectrum.getIntensityMap();
        ArrayList<Double> intensities = new ArrayList<Double>(intensityMap.keySet());
        Collections.sort(intensities, Collections.reverseOrder());

        double rank = 0;
        int missed = 0;

        for (double intensity : intensities) {
            for (Peak peak : intensityMap.get(intensity)) {
                if (peptideSpectrumAnnotator.matchPeak(peptide, iontypes, charges, identificationCharge, neutralLosses, peak).isEmpty()) { //Warning: this is very slow
                    missed++;
                    if (missed > nMissedTolerance) {
                        return ((double) rank) / spectrum.getNPeaks();
                    }
                }
                rank++;
            }
        }

        return ((double) rank) / spectrum.getNPeaks();
    }
}
