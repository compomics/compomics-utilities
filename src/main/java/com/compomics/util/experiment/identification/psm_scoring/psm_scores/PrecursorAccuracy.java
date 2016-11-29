package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PrecursorIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;

/**
 * The precursor accuracy as a score.
 *
 * @author Marc Vaudel
 */
public class PrecursorAccuracy {

    /**
     * Scores the match between the given peptide and spectrum using the
     * precursor m/z accuracy.
     *
     * @param peptide the peptide of interest
     * @param identificationCharge the charge of the identification
     * @param precursor the precursor of this peptide
     * @param ppm indicates whether the ms1 error is in ppm
     * @param minIsotope the minimal isotope
     * @param maxIsotope the maximal isotope
     *
     * @return the score of the match
     */
    public double getScore(Peptide peptide, int identificationCharge, Precursor precursor, boolean ppm, int minIsotope, int maxIsotope) {
        IonMatch ionMatch = new IonMatch(new Peak(precursor.getMz(), 0), new PrecursorIon(peptide.getMass()), identificationCharge);
        return Math.abs(ionMatch.getError(ppm, minIsotope, maxIsotope));
    }
}
