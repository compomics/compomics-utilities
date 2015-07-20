package com.compomics.util.experiment.identification.ptm.ptmscores;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class estimates the MD score as described in
 * http://www.ncbi.nlm.nih.gov/pubmed/21057138 Note: this implementation of the
 * MD score is not restricted to phosphorylation.
 *
 * @author Marc Vaudel
 */
public class MDScore {

    /**
     * Returns the MD score for the given peptide in a spectrum match. Null if
     * not identified by Mascot. If the peptide is the not the best scoring for
     * Mascot the score will be negative.
     *
     * @param mascotAssumptions the assumptions by Mascot
     * @param peptideCandidate the peptide of interest
     * @param ptms the names of the PTMs to score
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param rounding decimal to which the score should be rounded, ignored if
     * null
     *
     * @return the MD score
     */
    public static Double getMDScore(ArrayList<SpectrumIdentificationAssumption> mascotAssumptions, Peptide peptideCandidate, ArrayList<String> ptms, SequenceMatchingPreferences sequenceMatchingPreferences, Integer rounding) {

        HashMap<Double, ArrayList<Peptide>> mascotAssumptionsMap = new HashMap<Double, ArrayList<Peptide>>();
        Double firstScore = null, secondScore = null;

        if (mascotAssumptions != null) {
            for (SpectrumIdentificationAssumption assumption : mascotAssumptions) {
                if (assumption instanceof PeptideAssumption) {
                    PeptideAssumption peptideAssumption = (PeptideAssumption) assumption;
                    if (peptideAssumption.getPeptide().isSameSequenceAndModificationStatus(peptideCandidate, sequenceMatchingPreferences)) {
                        Double score = peptideAssumption.getRawScore();
                        if (!mascotAssumptionsMap.containsKey(score)) {
                            mascotAssumptionsMap.put(score, new ArrayList<Peptide>());
                        }
                        mascotAssumptionsMap.get(score).add(peptideAssumption.getPeptide());
                    }
                } else {
                    throw new UnsupportedOperationException("MD score not implemented for assumption of type " + assumption.getClass() + ".");
                }
            }

            ArrayList<Double> scores = new ArrayList<Double>(mascotAssumptionsMap.keySet());
            Collections.sort(scores, Collections.reverseOrder());

            for (double score : scores) {
                for (Peptide peptide : mascotAssumptionsMap.get(score)) {
                    if (peptide.sameModificationsAs(peptideCandidate)) {
                        firstScore = score;
                        if (secondScore != null) {
                            break;
                        }
                    } else if (secondScore == null && !peptide.sameModificationsAs(peptideCandidate, ptms)) {
                        secondScore = score;
                        if (firstScore != null) {
                            break;
                        }
                    }
                }
                if (firstScore != null && secondScore != null) {
                    break;
                }
            }
        }

        if (firstScore == null && secondScore == null) {
            return null;
        }
        if (firstScore == null) {
            return -secondScore;
        }
        if (secondScore == null) {
            return firstScore;
        }

        double score = firstScore - secondScore;

        if (rounding != null) {
            score = Util.roundDouble(score, rounding);
        }

        return score;
    }
}
