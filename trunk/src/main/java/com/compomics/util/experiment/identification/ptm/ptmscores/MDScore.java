/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.ptm.ptmscores;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.refinementparameters.MascotScore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class estimates the MD score as described in http://www.ncbi.nlm.nih.gov/pubmed/21057138
 * Note: this implementation of the MD score is not restricted to phosphorylation
 *
 * @author Marc
 */
public class MDScore {
    
    /**
     * Returns the MD score for the best peptide in a spectrum match (the best
     * peptide has to be set before). Null if not identified by Mascot. If the
     * peptide is the not the best scoring for Mascot the score will be
     * negative.
     *
     * @param spectrumMatch the spectrum match of interest
     * @param ptms the names of the ptms to score
     * 
     * @return the MD score
     */
    public static Double getMDScore(SpectrumMatch spectrumMatch, ArrayList<String> ptms) {
        return getMDScore(spectrumMatch, spectrumMatch.getBestAssumption().getPeptide(), ptms);
    }

    /**
     * Returns the MD score for the given peptide in a spectrum match. Null if
     * not identified by Mascot. If the peptide is the not the best scoring for
     * Mascot the score will be negative.
     *
     * @param peptideCandidate the peptide of interest
     * @param spectrumMatch the spectrum match of interest
     * @param ptms the names of the ptms to score
     * 
     * @return the MD score
     */
    public static Double getMDScore(SpectrumMatch spectrumMatch, Peptide peptideCandidate, ArrayList<String> ptms) {
        HashMap<Double, ArrayList<Peptide>> mascotAssumptionsMap = new HashMap<Double, ArrayList<Peptide>>();
        Double firstScore = null,
                secondScore = null;
        if (spectrumMatch.getAllAssumptions(Advocate.MASCOT) != null) {
            for (ArrayList<PeptideAssumption> peptideAssumptionList : spectrumMatch.getAllAssumptions(Advocate.MASCOT).values()) {
                for (PeptideAssumption peptideAssumption : peptideAssumptionList) {
                    if (peptideAssumption.getPeptide().isSameSequenceAndModificationStatus(peptideCandidate)) {
                        MascotScore mascotScore = new MascotScore();
                        mascotScore = (MascotScore) peptideAssumption.getUrParam(mascotScore);
                        Double score = mascotScore.getScore();
                        if (!mascotAssumptionsMap.containsKey(score)) {
                            mascotAssumptionsMap.put(score, new ArrayList<Peptide>());
                        }
                        mascotAssumptionsMap.get(score).add(peptideAssumption.getPeptide());
                    }
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
        return firstScore - secondScore;
    }
}
