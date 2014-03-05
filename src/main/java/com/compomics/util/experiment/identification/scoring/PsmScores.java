package com.compomics.util.experiment.identification.scoring;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.math.BasicMathFunctions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class contains different ways of scoring a match.
 *
 * @author Marc Vaudel
 */
public class PsmScores {

    /**
     * Returns a PSM score like the peptide score calculated for the A-score.
     *
     * @param peptide The peptide of interest
     * @param spectrum The corresponding spectrum
     * @param iontypes The fragment ions to look for
     * @param neutralLosses The neutral losses to look for
     * @param charges The fragment ions charges to look for
     * @param precursorCharge The precursor charge
     * @param mzTolerance The m/z tolerance to use
     * @param accountNeutralLosses a boolean indicating whether or not the
     * calculation shall account for neutral losses.
     * @return a map containing the best or two best PTM location(s) and the
     * corresponding A-score
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     */
    public static double getAScorePeptideScore(Peptide peptide, MSnSpectrum spectrum,
            HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance, boolean accountNeutralLosses) throws IOException, IllegalArgumentException, InterruptedException {

        HashMap<Integer, MSnSpectrum> spectrumMap = getReducedSpectra(spectrum, mzTolerance, 10);
        PeptideSpectrumAnnotator spectrumAnnotator = new PeptideSpectrumAnnotator();

        int N = 0;

        for (ArrayList<Ion> fragmentIons : spectrumAnnotator.getExpectedIons(iontypes, neutralLosses, charges, precursorCharge, peptide).values()) {
            N += fragmentIons.size();
        }

        HashMap<Integer, Double> depthMap = new HashMap<Integer, Double>();

        for (int i = 0; i < spectrumMap.keySet().size(); i++) {

            double p = ((double) i + 1) / 100;

            ArrayList<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, neutralLosses, charges, 
                    precursorCharge, spectrumMap.get(i), peptide, 0, mzTolerance, false, false); // @TODO: is the last false ok here???
            int n = matches.size();
            double P = 0;
            for (int k = n; k <= N; k++) {
                P += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
            }
            if (P <= Double.MIN_NORMAL) {
                P = Double.MIN_NORMAL;
            }
            double score = -10 * Math.log10(P);
            depthMap.put(i + 1, score);
        }

        Double peptideScore = 0.0;
        if (depthMap.containsKey(1)) {
            peptideScore += 0.5 * depthMap.get(1);
        }
        if (depthMap.containsKey(2)) {
            peptideScore += 0.75 * depthMap.get(2);
        }
        if (depthMap.containsKey(3)) {
            peptideScore += 1 * depthMap.get(3);
        }
        if (depthMap.containsKey(4)) {
            peptideScore += 1 * depthMap.get(4);
        }
        if (depthMap.containsKey(5)) {
            peptideScore += 1 * depthMap.get(5);
        }
        if (depthMap.containsKey(6)) {
            peptideScore += 1 * depthMap.get(6);
        }
        if (depthMap.containsKey(7)) {
            peptideScore += 0.75 * depthMap.get(7);
        }
        if (depthMap.containsKey(8)) {
            peptideScore += 0.5 * depthMap.get(8);
        }
        if (depthMap.containsKey(9)) {
            peptideScore += 0.25 * depthMap.get(9);
        }
        if (depthMap.containsKey(10)) {
            peptideScore += 0.25 * depthMap.get(10);
        }

        return peptideScore;
    }

    /**
     * Generates a map containing the spectra filtered on intensity with a basis
     * of 20*mz tolerance indexed by the depth used. (see A-score paper for more
     * details).
     *
     * @param baseSpectrum the base spectrum
     * @param mzTolerance the m/z tolerance
     * @return a map containing the spectra filtered indexed by peak depth.
     */
    public static HashMap<Integer, MSnSpectrum> getReducedSpectra(MSnSpectrum baseSpectrum, double mzTolerance) {
        return getReducedSpectra(baseSpectrum, mzTolerance, -1);
    }

    /**
     * Generates a map containing the spectra filtered on intensity with a basis
     * of 20*mz tolerance indexed by the depth used. (see A-score paper for more
     * details).
     *
     * @param baseSpectrum the base spectrum
     * @param mzTolerance the m/z tolerance
     * @param depthMax the depth to look into (10 for A-score). If -1 the
     * maximal depth will be used
     * @return a map containing the spectra filtered indexed by peak depth.
     */
    public static HashMap<Integer, MSnSpectrum> getReducedSpectra(MSnSpectrum baseSpectrum, double mzTolerance, int depthMax) {

        HashMap<Integer, MSnSpectrum> result = new HashMap<Integer, MSnSpectrum>();
        HashMap<Double, Peak> tempMap, peakMap = baseSpectrum.getPeakMap();
        ArrayList<Double> intensities, mz = new ArrayList<Double>(peakMap.keySet());
        Collections.sort(mz);
        double mzMax = mz.get(mz.size() - 1);
        int cpt = 0;
        double currentmzMin = 0;

        while (currentmzMin < mzMax) {
            int cptTemp = 0;
            while (cpt < mz.size()
                    && mz.get(cpt) < currentmzMin + 20 * mzTolerance) {
                cptTemp++;
                cpt++;
            }
            if (depthMax == -1
                    && cptTemp > depthMax) {
                depthMax = cptTemp;
            }
            currentmzMin += 200 * mzTolerance;
        }

        for (int i = 0; i < depthMax; i++) {
            result.put(i, new MSnSpectrum(2, baseSpectrum.getPrecursor(), baseSpectrum.getSpectrumTitle() + "_" + i, new HashMap<Double, Peak>(), "a score"));
        }

        cpt = 0;
        currentmzMin = 0;

        while (currentmzMin < mzMax) {
            intensities = new ArrayList<Double>();
            tempMap = new HashMap<Double, Peak>();

            while (cpt < mz.size()
                    && mz.get(cpt) < currentmzMin + 20 * mzTolerance) {
                Peak tempPeak = peakMap.get(mz.get(cpt));
                intensities.add(-tempPeak.intensity);
                tempMap.put(-tempPeak.intensity, tempPeak);
                cpt++;
            }

            Collections.sort(intensities);

            for (int i = 0; i < intensities.size(); i++) {
                for (int j = i; j < depthMax; j++) {
                    result.get(j).addPeak(tempMap.get(intensities.get(i)));
                }
            }

            currentmzMin += 200 * mzTolerance;
        }
        return result;
    }
}