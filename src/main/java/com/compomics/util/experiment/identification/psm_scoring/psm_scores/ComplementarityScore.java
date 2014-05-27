package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.math.BasicMathFunctions;
import org.apache.commons.math.util.FastMath;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The intensity sub-score as adapted from the DirecTag paper
 * (http://www.ncbi.nlm.nih.gov/pubmed/18630943)
 *
 * @author Marc
 */
public class ComplementarityScore {

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
     * @param iontypes the fragment ions to annotate
     * @param neutralLosses the neutral losses to annotate
     * @param charges the fragment charges to look for
     * @param precursorCharge the precursor charge
     * @param mzTolerance the ms2 m/z tolerance
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Ion.IonType, ArrayList<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int precursorCharge, double mzTolerance) {
        return getScore(peptide, spectrum, iontypes, neutralLosses, charges, precursorCharge, mzTolerance, null);
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
     * @param iontypes the fragment ions to annotate
     * @param neutralLosses the neutral losses to annotate
     * @param charges the fragment charges to look for
     * @param precursorCharge the precursor charge
     * @param mzTolerance the ms2 m/z tolerance
     * @param peptideSpectrumAnnotator an external annotator (if null an
     * internal will be used)
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Ion.IonType, ArrayList<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int precursorCharge, double mzTolerance, PeptideSpectrumAnnotator peptideSpectrumAnnotator) {

        if (peptideSpectrumAnnotator == null) {
            peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();
        }

        int sequenceLength = peptide.getSequence().length();

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(iontypes, neutralLosses, charges, precursorCharge,
                spectrum, peptide, 0, mzTolerance, false, true);

        HashMap<Integer, Double> residueToMatchesMap = new HashMap<Integer, Double>(sequenceLength);
        for (IonMatch ionMatch : matches) {
            Ion ion = ionMatch.ion;
            if (ion instanceof PeptideFragmentIon) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                int number = peptideFragmentIon.getNumber();
                Double nIons = residueToMatchesMap.get(number);
                if (nIons == null) {
                    nIons = 1.0;
                } else {
                    nIons++;
                }
                residueToMatchesMap.put(number, nIons);
            }
        }

        ArrayList<Double> scorePerResidue = new ArrayList<Double>(residueToMatchesMap.size());
        for (int number = 1; number <= sequenceLength; number++) {
            Double nIons = residueToMatchesMap.get(number);
            if (nIons == null) {
                scorePerResidue.add(0.0);
            } else {
                scorePerResidue.add(FastMath.log(nIons) / log2);
            }
        }

        return BasicMathFunctions.mean(scorePerResidue);
    }
}
