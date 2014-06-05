package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.math.BasicMathFunctions;
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
     * @param iontypes the fragment ions to annotate
     * @param neutralLosses the neutral losses to annotate
     * @param charges the fragment charges to look for
     * @param identificationCharge the precursor charge
     * @param mzTolerance the ms2 m/z tolerance
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Ion.IonType, ArrayList<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int identificationCharge, double mzTolerance) {
        return getScore(peptide, spectrum, iontypes, neutralLosses, charges, identificationCharge, mzTolerance, null);
    }

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. Returns the average over the peptide sequence of the
     * minimal mass error of the ions annotating an amino acid.
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
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Ion.IonType, ArrayList<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int identificationCharge, double mzTolerance, PeptideSpectrumAnnotator peptideSpectrumAnnotator) {

        if (peptideSpectrumAnnotator == null) {
            peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();
        }

        int sequenceLength = peptide.getSequence().length();
        HashMap<Integer, Double> aaDeviations = new HashMap(sequenceLength);
        for (int i = 1; i <= sequenceLength; i++) {
            aaDeviations.put(i, mzTolerance);
        }

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(
                iontypes, neutralLosses, charges, identificationCharge,
                spectrum, peptide, 0, mzTolerance, false, true);
        
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
            return mzTolerance;
        }

        return BasicMathFunctions.mean(mzDeviations);
    }
}
