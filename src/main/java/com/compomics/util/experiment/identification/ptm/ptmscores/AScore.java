package com.compomics.util.experiment.identification.ptm.ptmscores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class estimates the A-score as described in
 * http://www.ncbi.nlm.nih.gov/pubmed/16964243 Note: Here the window size is
 * adapted to mz tolerance and the score is not restricted to phosphorylation.
 *
 * @author Marc Vaudel
 */
public class AScore {

    /**
     * Returns the A-score for the best PTM location without accounting for
     * neutral losses. In case the two best locations score the same they are
     * both given with the score of 0. Note that PTMs found on peptides must be
     * loaded in the PTM factory.
     *
     * @param peptide The peptide of interest
     * @param ptms The PTMs to score, for instance different phosphorylations.
     * These PTMs are considered as indistinguishable, i.e. of same mass.
     * @param spectrum The corresponding spectrum
     * @param iontypes The fragment ions to look for
     * @param charges The fragment ions charges to look for
     * @param precursorCharge The precursor charge
     * @param mzTolerance The m/z tolerance to use
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a map containing the best or two best PTM location(s) and the
     * corresponding A-score
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static HashMap<Integer, Double> getAScore(Peptide peptide, ArrayList<PTM> ptms, MSnSpectrum spectrum,
            HashMap<Ion.IonType, HashSet<Integer>> iontypes, ArrayList<Integer> charges, int precursorCharge, double mzTolerance, SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {
        return getAScore(peptide, ptms, spectrum, iontypes, null, charges, precursorCharge, mzTolerance, false, sequenceMatchingPreferences);
    }

    /**
     * Returns the A-score for the best PTM location accounting for neutral
     * losses. In case the two best locations score the same they are both given
     * with the score of 0. Note that PTMs found on peptides must be loaded in
     * the PTM factory.
     *
     * @param peptide The peptide of interest
     * @param ptms The PTMs to score, for instance different phosphorylations.
     * These PTMs are considered as indistinguishable, i.e. of same mass.
     * @param spectrum The corresponding spectrum
     * @param iontypes The fragment ions to look for
     * @param neutralLosses The neutral losses to look for
     * @param charges The fragment ions charges to look for
     * @param precursorCharge The precursor charge
     * @param mzTolerance The m/z tolerance to use
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a map containing the best or two best PTM location(s) and the
     * corresponding A-score
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static HashMap<Integer, Double> getAScore(Peptide peptide, ArrayList<PTM> ptms, MSnSpectrum spectrum, HashMap<Ion.IonType, HashSet<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int precursorCharge, double mzTolerance, SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {
        return getAScore(peptide, ptms, spectrum, iontypes, neutralLosses, charges, precursorCharge, mzTolerance, true, sequenceMatchingPreferences);
    }

    /**
     * Returns the A-score for the best PTM location. In case the two best
     * locations score the same they are both given with the score of 0. 1 is
     * the first amino acid. The N-terminus is indexed 0 and the C-terminus with
     * the peptide length+1. Note that PTMs found on peptides must be loaded in
     * the PTM factory.
     *
     * @param peptide The peptide of interest
     * @param ptms The PTMs to score, for instance different phosphorylations.
     * These PTMs are considered as indistinguishable, i.e. of same mass.
     * @param spectrum The corresponding spectrum
     * @param iontypes The fragment ions to look for
     * @param neutralLosses The neutral losses to look for
     * @param charges The fragment ions charges to look for
     * @param precursorCharge The precursor charge
     * @param mzTolerance The MS2 m/z tolerance to use
     * @param accountNeutralLosses a boolean indicating whether or not the
     * calculation shall account for neutral losses.
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a map containing the best or two best PTM location(s) and the
     * corresponding A-score
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static HashMap<Integer, Double> getAScore(Peptide peptide, ArrayList<PTM> ptms, MSnSpectrum spectrum, HashMap<Ion.IonType, HashSet<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int precursorCharge, double mzTolerance, boolean accountNeutralLosses, SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {
        return getAScore(peptide, ptms, spectrum, iontypes, neutralLosses, charges, precursorCharge, mzTolerance, accountNeutralLosses, sequenceMatchingPreferences, null);
    }

    /**
     * Returns the A-score for the best PTM location. In case the two best
     * locations score the same they are both given with the score of 0. 1 is
     * the first amino acid. The N-terminus is indexed 0 and the C-terminus with
     * the peptide length+1. Note that PTMs found on peptides must be loaded in
     * the PTM factory.
     *
     * @param peptide The peptide of interest
     * @param ptms The PTMs to score, for instance different phosphorylations.
     * These PTMs are considered as indistinguishable, i.e. of same mass.
     * @param spectrum The corresponding spectrum
     * @param iontypes The fragment ions to look for
     * @param neutralLosses The neutral losses to look for
     * @param charges The fragment ions charges to look for
     * @param precursorCharge The precursor charge
     * @param mzTolerance The MS2 m/z tolerance to use
     * @param accountNeutralLosses a boolean indicating whether or not the
     * calculation shall account for neutral losses.
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param spectrumAnnotator a spectrum annotator to annotate the spectra
     *
     * @return a map containing the best or two best PTM location(s) and the
     * corresponding A-score
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static HashMap<Integer, Double> getAScore(Peptide peptide, ArrayList<PTM> ptms, MSnSpectrum spectrum, HashMap<Ion.IonType, HashSet<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int precursorCharge, double mzTolerance, boolean accountNeutralLosses,
            SequenceMatchingPreferences sequenceMatchingPreferences, PeptideSpectrumAnnotator spectrumAnnotator)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        if (ptms.isEmpty()) {
            throw new IllegalArgumentException("No PTM given for A-score calculation.");
        }

        int nPTM = 0;
        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            if (modMatch.isVariable()) {
                for (PTM ptm : ptms) {
                    if (ptm.getName().equals(modMatch.getTheoreticPtm())) {
                        nPTM++;
                    }
                }
            }
        }
        if (nPTM == 0) {
            throw new IllegalArgumentException("Given PTMs not found in the peptide for A-score calculation.");
        }

        PTM refPTM = ptms.get(0);
        double ptmMass = refPTM.getMass();

        NeutralLossesMap scoringLossesMap = new NeutralLossesMap();
        if (accountNeutralLosses) {
            // here annotation should be sequence and modification independant
            for (NeutralLoss neutralLoss : neutralLosses.getAccountedNeutralLosses()) {
                if (Math.abs(neutralLoss.mass - ptmMass) > mzTolerance) {
                    scoringLossesMap.addNeutralLoss(neutralLoss, 1, 1);
                }
            }
        }

        int peptideLength = peptide.getSequence().length();

        ArrayList<Integer> possibleSites = new ArrayList<Integer>();
        for (PTM ptm : ptms) {
            if (ptm.isNTerm()) {
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(1)) {
                    possibleSites.add(0);
                }
            } else if (ptm.isCTerm()) {
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(peptideLength)) {
                    possibleSites.add(peptideLength + 1);
                }
            } else {
                for (int potentialSite : peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences)) {
                    if (!possibleSites.contains(potentialSite)) {
                        possibleSites.add(potentialSite);
                    }
                }
            }
        }

        if (possibleSites.size() > nPTM) {
            Collections.sort(possibleSites);
            Peptide noModPeptide = Peptide.getNoModPeptide(peptide, ptms);
            HashMap<Integer, MSnSpectrum> spectrumMap = getReducedSpectra(spectrum, mzTolerance, 10);

            HashMap<Integer, HashMap<Integer, Double>> positionToScoreMap = getPositionToScoreMap(peptide, noModPeptide, possibleSites,
                    spectrum, spectrumMap, iontypes, scoringLossesMap, charges, precursorCharge, mzTolerance, spectrumAnnotator, refPTM);

            HashMap<Double, ArrayList<Integer>> peptideScoreToPostitionMap = getPeptideScoreToPositionMap(positionToScoreMap);
            ArrayList<Double> scores = new ArrayList<Double>(peptideScoreToPostitionMap.keySet());
            Collections.sort(scores, Collections.reverseOrder());
            ArrayList<Integer> bestScoringSites = peptideScoreToPostitionMap.get(scores.get(0));
            if (bestScoringSites.size() == 1) {
                int bestPosition = bestScoringSites.get(0);
                ArrayList<Integer> secondScoringSites = null;
                for (int i = 1; i < scores.size() && (secondScoringSites == null || secondScoringSites.isEmpty()); i++) {
                    secondScoringSites = peptideScoreToPostitionMap.get(scores.get(i));
                }
                if (secondScoringSites == null || secondScoringSites.isEmpty()) {
                    throw new IllegalArgumentException("Only one site found in peptide score to position map when estimating the A-score for spectrum "
                            + spectrum.getSpectrumTitle() + " in file " + spectrum.getFileName() + " for modification " + refPTM.getName() + " on peptide " + peptide.getSequence() + ".");
                }
                HashMap<Integer, Double> lowestScoreMap = null, tempMap;
                Double lowestScore = null;
                for (int secondPosition : secondScoringSites) {
                    int bestDepth = getBestDepth(positionToScoreMap, bestPosition, secondPosition);
                    tempMap = getScoreForPositions(peptide, noModPeptide, refPTM, bestPosition, secondPosition, iontypes, scoringLossesMap,
                            charges, precursorCharge, mzTolerance, spectrumAnnotator, bestDepth, spectrumMap.get(bestDepth));
                    Double tempMapLowestScore = null;
                    for (int tempPos : tempMap.keySet()) {
                        double tempScore = tempMap.get(tempPos);
                        if (tempMapLowestScore == null || tempScore < tempMapLowestScore) {
                            tempMapLowestScore = tempScore;
                        }
                        if (tempMapLowestScore == 0.0) {
                            break;
                        }
                    }
                    if (tempMapLowestScore == null) {
                        throw new IllegalArgumentException("No secondary position score found for " + spectrum.getSpectrumTitle() + " in file "
                                + spectrum.getFileName() + " for modification " + refPTM.getName() + " on peptide " + peptide.getSequence() + ".");
                    }
                    if (lowestScore == null || tempMapLowestScore < lowestScore) {
                        lowestScore = tempMapLowestScore;
                        lowestScoreMap = tempMap;
                    } else if (tempMapLowestScore == lowestScore) {
                        lowestScoreMap.putAll(tempMap);
                    }
                }
                if (lowestScoreMap == null) {
                    throw new IllegalArgumentException("No A-score found for " + spectrum.getSpectrumTitle() + " in file " + spectrum.getFileName()
                            + " for modification " + refPTM.getName() + " on peptide " + peptide.getSequence() + ".");
                }
                return lowestScoreMap;
            } else {
                HashMap<Integer, Double> lowestScoreMap = null, tempMap;
                Double lowestScore = null;
                for (int bestPosition : bestScoringSites) {
                    for (int secondPosition : bestScoringSites) {
                        if (bestPosition != secondPosition) {
                            int bestDepth = getBestDepth(positionToScoreMap, bestPosition, secondPosition);
                            tempMap = getScoreForPositions(peptide, noModPeptide, refPTM, bestPosition, secondPosition, iontypes, scoringLossesMap,
                                    charges, precursorCharge, mzTolerance, spectrumAnnotator, bestDepth, spectrumMap.get(bestDepth));
                            Double tempMapLowestScore = null;
                            for (int tempPos : tempMap.keySet()) {
                                double tempScore = tempMap.get(tempPos);
                                if (tempMapLowestScore == null || tempScore < tempMapLowestScore) {
                                    tempMapLowestScore = tempScore;
                                }
                            }
                            if (tempMapLowestScore == null) {
                                throw new IllegalArgumentException("No secondary position score found for " + spectrum.getSpectrumTitle() + " in file "
                                        + spectrum.getFileName() + " for modification " + refPTM.getName() + " on peptide " + peptide.getSequence() + ".");
                            }
                            if (lowestScore == null || tempMapLowestScore < lowestScore) {
                                lowestScore = tempMapLowestScore;
                                lowestScoreMap = tempMap;
                            } else if (tempMapLowestScore == lowestScore) {
                                lowestScoreMap.putAll(tempMap);
                            }
                        }
                    }
                    if (lowestScore == 0.0) {
                        break;
                    }
                }
                if (lowestScoreMap == null) {
                    throw new IllegalArgumentException("No A-score found for " + spectrum.getSpectrumTitle() + " in file " + spectrum.getFileName()
                            + " for modification " + refPTM.getName() + " on peptide " + peptide.getSequence() + ".");
                }
                return lowestScoreMap;
            }
        } else if (possibleSites.size() == nPTM) {
            HashMap<Integer, Double> result = new HashMap<Integer, Double>();
            for (int pos : possibleSites) {
                result.put(pos, 100.0);
            }
            return result;
        } else {
            throw new IllegalArgumentException("Found less potential modification sites than PTMs during A-score calculation. Peptide key: " + peptide.getKey());
        }
    }

    /**
     * Returns the spectrum depth for two PTM sites which maximizes the score
     * difference.
     *
     * @param positionToScoreMap the position to score map
     * @param bestPosition the best position
     * @param secondPosition the second best position
     *
     * @return the depth at which the score difference between the best position
     * and the second position is maximized
     */
    private static int getBestDepth(HashMap<Integer, HashMap<Integer, Double>> positionToScoreMap, int bestPosition, int secondPosition) {

        double diff, maxDiff = 0;
        int bestI = 0;

        for (int i = 1; i <= 10; i++) {
            diff = positionToScoreMap.get(bestPosition).get(i) - positionToScoreMap.get(secondPosition).get(i);
            if (diff >= maxDiff) {
                bestI = i - 1;
                maxDiff = diff;
            }
        }
        return bestI;
    }

    /**
     * Returns the A-score for two candidate PTM sites in a map: bestSite &gt;
     * score. If the sites score equally both will be returned in the map.
     *
     * @param peptide the peptide of interest
     * @param noModPeptide the peptide without the variable modification of
     * interest
     * @param refPTM the PTM of interest
     * @param bestPosition the best scoring position
     * @param secondPosition the second best scoring position
     * @param iontypes the ion types to look for in the spectrum
     * @param scoringLossesMap the neutral losses to look for in the spectrum
     * @param charges the fragment ion charges to look for in the spectrum
     * @param precursorCharge the precursor charge to look for in the spectrum
     * @param mzTolerance the fragment ion m/z tolerance for spectrum annotation
     * @param spectrumAnnotator the spectrum annotator which should be used to
     * annotate the spectrum
     * @param bestDepth the depth maximizing the score difference between the
     * best and second best scoring sites (see getBestDepth)
     * @param spectrumAtBestDepth the spectrum extracted from the original
     * spectrum filtered at bestDepth intensities
     *
     * @return the candidate A-score in a map
     */
    private static HashMap<Integer, Double> getScoreForPositions(Peptide peptide, Peptide noModPeptide, PTM refPTM, int bestPosition, int secondPosition,
            HashMap<Ion.IonType, HashSet<Integer>> iontypes, NeutralLossesMap scoringLossesMap, ArrayList<Integer> charges, int precursorCharge,
            double mzTolerance, PeptideSpectrumAnnotator spectrumAnnotator, int bestDepth, MSnSpectrum spectrumAtBestDepth) {

        HashMap<Integer, Double> result = new HashMap<Integer, Double>(2);

        int N = 0;
        int posMin = Math.min(bestPosition, secondPosition);
        int posMax = Math.max(bestPosition, secondPosition);

        for (ArrayList<Ion> ions : spectrumAnnotator.getExpectedIons(iontypes, scoringLossesMap, charges, precursorCharge, peptide).values()) {
            for (Ion ion : ions) {
                if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                    PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
                    if (ion.getSubType() == PeptideFragmentIon.A_ION
                            || ion.getSubType() == PeptideFragmentIon.B_ION
                            || ion.getSubType() == PeptideFragmentIon.C_ION) {
                        int aa = fragmentIon.getNumber();
                        if (aa > posMin && aa <= posMax) {
                            N++;
                        }
                    } else if (ion.getSubType() == PeptideFragmentIon.X_ION
                            || ion.getSubType() == PeptideFragmentIon.Y_ION
                            || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                        int aa = peptide.getSequence().length() - fragmentIon.getNumber();
                        if (aa > posMin && aa <= posMax) {
                            N++;
                        }
                    }
                }
            }
        }

        double p = ((double) bestDepth + 1) / 100;
        Peptide tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getModificationMatches());
        tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, posMin));
        ArrayList<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, scoringLossesMap, charges, precursorCharge,
                spectrumAtBestDepth, tempPeptide, 0, mzTolerance, false, false);
        int n = 0;

        for (IonMatch match : matches) {
            Ion ion = match.ion;
            if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
                if (ion.getSubType() == PeptideFragmentIon.A_ION
                        || ion.getSubType() == PeptideFragmentIon.B_ION
                        || ion.getSubType() == PeptideFragmentIon.C_ION) {
                    int aa = fragmentIon.getNumber();
                    if (aa > posMin && aa <= posMax) {
                        n++;
                    }
                } else if (ion.getSubType() == PeptideFragmentIon.X_ION
                        || ion.getSubType() == PeptideFragmentIon.Y_ION
                        || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                    int aa = peptide.getSequence().length() - fragmentIon.getNumber();
                    if (aa > posMin && aa <= posMax) {
                        n++;
                    }
                }
            }
        }

        double p1 = 0;

        for (int k = n; k <= N; k++) {
            p1 += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
        }

        tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getModificationMatches());
        tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, posMax));
        matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, scoringLossesMap, charges, precursorCharge,
                spectrumAtBestDepth, tempPeptide, 0, mzTolerance, false, false);
        n = 0;

        for (IonMatch match : matches) {
            Ion ion = match.ion;
            if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
                if (ion.getSubType() == PeptideFragmentIon.A_ION
                        || ion.getSubType() == PeptideFragmentIon.B_ION
                        || ion.getSubType() == PeptideFragmentIon.C_ION) {
                    int aa = fragmentIon.getNumber();
                    if (aa > posMin && aa <= posMax) {
                        n++;
                    }
                } else if (ion.getSubType() == PeptideFragmentIon.X_ION
                        || ion.getSubType() == PeptideFragmentIon.Y_ION
                        || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                    int aa = peptide.getSequence().length() - fragmentIon.getNumber();
                    if (aa > posMin && aa <= posMax) {
                        n++;
                    }
                }
            }
        }
        double p2 = 0;

        for (int k = n; k <= N; k++) {
            p2 += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
        }

        if (p1 == p2) {
            result.put(posMin, 0.0);
            result.put(posMax, 0.0);
        } else if (p1 < p2) {
            ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
            modificationProfile.add(posMin);
            double score1 = -10 * Math.log10(p1);
            double score2 = -10 * Math.log10(p2);
            double score = score1 - score2;
            result.put(posMin, score);
        } else {
            ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
            modificationProfile.add(posMax);
            double score1 = -10 * Math.log10(p1);
            double score2 = -10 * Math.log10(p2);
            double score = score2 - score1;
            result.put(posMax, score);
        }
        return result;
    }

    /**
     * Estimates the peptide score for every modification localization and
     * returns a map score &gt; localization.
     *
     * @param positionToScoreMap the position to score map
     *
     * @return a score to position map
     */
    public static HashMap<Double, ArrayList<Integer>> getPeptideScoreToPositionMap(HashMap<Integer, HashMap<Integer, Double>> positionToScoreMap) {

        HashMap<Double, ArrayList<Integer>> result = new HashMap<Double, ArrayList<Integer>>();

        for (int pos : positionToScoreMap.keySet()) {
            Double peptideScore = 0.0;
            if (positionToScoreMap.get(pos).containsKey(1)) {
                peptideScore += 0.5 * positionToScoreMap.get(pos).get(1);
            }
            if (positionToScoreMap.get(pos).containsKey(2)) {
                peptideScore += 0.75 * positionToScoreMap.get(pos).get(2);
            }
            if (positionToScoreMap.get(pos).containsKey(3)) {
                peptideScore += 1 * positionToScoreMap.get(pos).get(3);
            }
            if (positionToScoreMap.get(pos).containsKey(4)) {
                peptideScore += 1 * positionToScoreMap.get(pos).get(4);
            }
            if (positionToScoreMap.get(pos).containsKey(5)) {
                peptideScore += 1 * positionToScoreMap.get(pos).get(5);
            }
            if (positionToScoreMap.get(pos).containsKey(6)) {
                peptideScore += 1 * positionToScoreMap.get(pos).get(6);
            }
            if (positionToScoreMap.get(pos).containsKey(7)) {
                peptideScore += 0.75 * positionToScoreMap.get(pos).get(7);
            }
            if (positionToScoreMap.get(pos).containsKey(8)) {
                peptideScore += 0.5 * positionToScoreMap.get(pos).get(8);
            }
            if (positionToScoreMap.get(pos).containsKey(9)) {
                peptideScore += 0.25 * positionToScoreMap.get(pos).get(9);
            }
            if (positionToScoreMap.get(pos).containsKey(10)) {
                peptideScore += 0.25 * positionToScoreMap.get(pos).get(10);
            }
            if (!result.containsKey(peptideScore)) {
                result.put(peptideScore, new ArrayList<Integer>());
            }
            result.get(peptideScore).add(pos);
        }
        return result;
    }

    /**
     * Returns a map PTM localization &gt; score.
     *
     * @param peptide the peptide of interest
     * @param noModPeptide the peptide without the variable modification of
     * interest
     * @param refPTM the PTM of interest
     * @param iontypes the ion types to look for in the spectrum
     * @param scoringLossesMap the neutral losses to look for in the spectrum
     * @param charges the fragment ion charges to look for in the spectrum
     * @param precursorCharge the precursor charge to look for in the spectrum
     * @param mzTolerance the fragment ion m/z tolerance for spectrum annotation
     * @param spectrumAnnotator the spectrum annotator which should be used to
     * annotate the spectrum
     * @param spectrum the spectrum of interest
     * @param spectrumMap the map of the extracted spectra: depth &gt; extracted
     * spectrum
     * @param possibleSites the possible modification sites
     *
     * @return a map PTM localization &gt; score
     */
    public static HashMap<Integer, HashMap<Integer, Double>> getPositionToScoreMap(Peptide peptide, Peptide noModPeptide, ArrayList<Integer> possibleSites,
            MSnSpectrum spectrum, HashMap<Integer, MSnSpectrum> spectrumMap, HashMap<Ion.IonType, HashSet<Integer>> iontypes, NeutralLossesMap scoringLossesMap,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance, PeptideSpectrumAnnotator spectrumAnnotator, PTM refPTM) {

        HashMap<Integer, HashMap<Integer, Double>> positionToScoreMap = new HashMap<Integer, HashMap<Integer, Double>>();

        int N = 0;

        for (ArrayList<Ion> fragmentIons : spectrumAnnotator.getExpectedIons(iontypes, scoringLossesMap, charges, precursorCharge, peptide).values()) {
            N += fragmentIons.size();
        }

        String sequence = noModPeptide.getSequence();
        int sequenceLength = sequence.length();

        for (int i = 0; i < spectrumMap.size(); i++) {

            double p = ((double) i + 1) / 100;

            for (int pos : possibleSites) {
                Peptide tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getModificationMatches());
                int position;
                if (pos == 0) {
                    position = 1;
                } else if (pos == sequenceLength + 1) {
                    position = sequenceLength;
                } else {
                    position = pos;
                }
                tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, position));

                ArrayList<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, scoringLossesMap, charges, precursorCharge,
                        spectrumMap.get(i), tempPeptide, 0, mzTolerance, false, false);
                int n = matches.size();
                double P = 0;
                for (int k = n; k <= N; k++) {
                    P += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
                }
                if (P <= Double.MIN_NORMAL) {
                    P = Double.MIN_NORMAL;
                }
                double score = -10 * Math.log10(P);
                if (!positionToScoreMap.containsKey(pos)) {
                    positionToScoreMap.put(pos, new HashMap<Integer, Double>());
                }
                positionToScoreMap.get(pos).put(i + 1, score);
            }
        }
        return positionToScoreMap;
    }

    /**
     * Generates a map containing the spectra filtered on intensity with a basis
     * of 20*m/z tolerance indexed by the depth used. (see A-score paper for
     * more details).
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
     * of 20*m/z tolerance indexed by the depth used. (see A-score paper for
     * more details).
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
