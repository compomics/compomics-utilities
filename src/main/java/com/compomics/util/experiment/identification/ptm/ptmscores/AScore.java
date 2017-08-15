package com.compomics.util.experiment.identification.ptm.ptmscores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.spectra.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.spectra.Peak;
import com.compomics.util.math.statistics.distributions.BinomialDistribution;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.util.MathUtils;

/**
 * This class estimates the A-score as described in
 * http://www.ncbi.nlm.nih.gov/pubmed/16964243 Note: Here the window size is
 * adapted to mz tolerance and the score is not restricted to phosphorylation.
 *
 * @author Marc Vaudel
 */
public class AScore {

    /**
     * Returns the A-score for the best PTM location. In case the two best
     * locations score the same they are both given with the score of 0. 1 is
     * the first amino acid. The N-terminus is indexed 0 and the C-terminus with
     * the peptide length+1. Note that PTMs found on peptides must be loaded in
     * the PTM factory (com.compomics.util.experiment.biology.PTMFactory), and
     * if the scoring involves protein terminal PTMs, the protein sequences must
     * be loaded in the sequence factory
     * (com.compomics.util.experiment.identification.SequenceFactory) and
     * indexed using the protein tree (see getDefaultProteinTree in
     * SequenceFactory). PTMs of same mass should be scored together and given
     * in the PTMs list. Neutral losses of mass equal to the mass of the PTM
     * will be ignored. Neutral losses to be accounted for should be given in
     * the SpecificAnnotationPreferences and will be ignored if
     * accountNeutralLosses is false.
     *
     * @param peptide the peptide of interest
     * @param ptms the PTMs to score, for instance different phosphorylations
     * (the PTMs are considered as indistinguishable, i.e. of same mass).
     * @param spectrum the corresponding spectrum
     * @param annotationPreferences the global annotation preferences
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this peptide and spectrum
     * @param accountNeutralLosses if false, neutral losses available in the
     * specific annotation preferences will be ignored
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for PTM to peptide mapping
     * @param spectrumAnnotator a spectrum annotator to annotate the spectra
     *
     * @return a map containing the best or two best PTM location(s) and the
     * corresponding A-score
     *
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while reading or writing a file
     * @throws java.lang.InterruptedException exception thrown whenever a
     * threading issue occurred while scoring the PTM
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while deserializing an object from the protein tree (the
     * protein sequence index)
     * @throws java.sql.SQLException exception thrown whenever an error occurred
     * while interacting with the protein tree
     * @throws org.apache.commons.math.MathException exception thrown whenever a
     * math error occurred while computing the score.
     */
    public static HashMap<Integer, Double> getAScore(Peptide peptide, ArrayList<Modification> ptms, MSnSpectrum spectrum, AnnotationSettings annotationPreferences,
            SpecificAnnotationSettings specificAnnotationPreferences, boolean accountNeutralLosses, SequenceMatchingPreferences sequenceMatchingPreferences,
            SequenceMatchingPreferences ptmSequenceMatchingPreferences, PeptideSpectrumAnnotator spectrumAnnotator)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException, MathException {

        if (ptms.isEmpty()) {
            throw new IllegalArgumentException("No PTM given for A-score calculation.");
        }

        int nPTM = 0;
        if (peptide.isModified()) {
            for (ModificationMatch modMatch : peptide.getModificationMatches()) {
                if (modMatch.getVariable()) {
                    for (Modification ptm : ptms) {
                        if (ptm.getName().equals(modMatch.getModification())) {
                            nPTM++;
                        }
                    }
                }
            }
        }
        if (nPTM == 0) {
            throw new IllegalArgumentException("Given PTMs not found in the peptide for A-score calculation.");
        }

        Modification refPTM = ptms.get(0);
        double ptmMass = refPTM.getMass();

        NeutralLossesMap annotationNeutralLosses = specificAnnotationPreferences.getNeutralLossesMap(),
                scoringLossesMap = new NeutralLossesMap();
        if (accountNeutralLosses) {
            // here annotation should be sequence and modification independant
            for (String neutralLossName : annotationNeutralLosses.getAccountedNeutralLosses()) {
                NeutralLoss neutralLoss = NeutralLoss.getNeutralLoss(neutralLossName);
                if (Math.abs(neutralLoss.getMass() - ptmMass) > specificAnnotationPreferences.getFragmentIonAccuracyInDa(spectrum.getMaxMz())) {
                    scoringLossesMap.addNeutralLoss(neutralLoss, 1, 1);
                }
            }
        }

        int peptideLength = peptide.getSequence().length();

        ArrayList<Integer> possibleSites = new ArrayList<>();
        for (Modification ptm : ptms) {
            if (ptm.isNTerm()) {
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(1)) {
                    possibleSites.add(0);
                }
            } else if (ptm.isCTerm()) {
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(peptideLength)) {
                    possibleSites.add(peptideLength + 1);
                }
            } else {
                for (int potentialSite : peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences)) {
                    if (!possibleSites.contains(potentialSite)) {
                        possibleSites.add(potentialSite);
                    }
                }
            }
        }

        if (possibleSites.size() > nPTM) {
            Collections.sort(possibleSites);
            Peptide noModPeptide = Peptide.getNoModPeptide(peptide, ptms);
            HashMap<Integer, MSnSpectrum> spectrumMap = getReducedSpectra(spectrum, specificAnnotationPreferences.getFragmentIonAccuracyInDa(spectrum.getMaxMz()), 10);

            HashMap<Integer, HashMap<Integer, Double>> positionToScoreMap = getPositionToScoreMap(peptide, noModPeptide, possibleSites,
                    spectrum, spectrumMap, annotationPreferences, specificAnnotationPreferences, spectrumAnnotator, refPTM);

            HashMap<Double, ArrayList<Integer>> peptideScoreToPostitionMap = getPeptideScoreToPositionMap(positionToScoreMap);
            ArrayList<Double> scores = new ArrayList<>(peptideScoreToPostitionMap.keySet());
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
                    tempMap = getScoreForPositions(peptide, noModPeptide, refPTM, bestPosition, secondPosition, annotationPreferences, specificAnnotationPreferences, spectrumAnnotator, bestDepth, spectrumMap.get(bestDepth));
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
                    } else if (tempMapLowestScore.equals(lowestScore)) {
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
                            tempMap = getScoreForPositions(peptide, noModPeptide, refPTM, bestPosition, secondPosition, annotationPreferences, specificAnnotationPreferences, spectrumAnnotator, bestDepth, spectrumMap.get(bestDepth));
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
                            } else if (tempMapLowestScore.equals(lowestScore)) {
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
            HashMap<Integer, Double> result = new HashMap<>();
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

        Double maxDiff = 0.0;
        int bestI = 0;

        for (int i = 1; i <= 10; i++) {
            double diff = positionToScoreMap.get(bestPosition).get(i) - positionToScoreMap.get(secondPosition).get(i);
            if (diff > maxDiff) {
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
     * @param annotationPreferences the global annotation preferences
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this peptide and spectrum
     * @param spectrumAnnotator the spectrum annotator which should be used to
     * annotate the spectrum
     * @param bestDepth the depth maximizing the score difference between the
     * best and second best scoring sites (see getBestDepth)
     * @param spectrumAtBestDepth the spectrum extracted from the original
     * spectrum filtered at bestDepth intensities
     *
     * @return the candidate A-score in a map
     *
     * @throws org.apache.commons.math.MathException exception thrown whenever a
     * math error occurred while computing the score or estimating the noise level
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    private static HashMap<Integer, Double> getScoreForPositions(Peptide peptide, Peptide noModPeptide, Modification refPTM, int bestPosition, int secondPosition, AnnotationSettings annotationPreferences,
            SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator spectrumAnnotator, int bestDepth, MSnSpectrum spectrumAtBestDepth) throws MathException, InterruptedException {

        HashMap<Integer, Double> result = new HashMap<>(2);

        int N = 0;
        int posMin = Math.min(bestPosition, secondPosition);
        int posMax = Math.max(bestPosition, secondPosition);

        for (ArrayList<Ion> ions : spectrumAnnotator.getExpectedIons(specificAnnotationPreferences, peptide).values()) {
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
        ArrayList<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences,
                spectrumAtBestDepth, tempPeptide, false);
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

        BinomialDistribution distribution = new BinomialDistribution(N, p);

        Double p1 = distribution.getDescendingCumulativeProbabilityAt((double) n);

        tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getModificationMatches());
        tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, posMax));
        matches = spectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences,
                spectrumAtBestDepth, tempPeptide, false);
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
        Double p2 = distribution.getDescendingCumulativeProbabilityAt((double) n);

        if (p1.equals(p2)) {
            result.put(posMin, 0.0);
            result.put(posMax, 0.0);
        } else if (p1 < p2) {
            Double score1;
            if (p1 == 0.0) {
                score1 = Double.MAX_VALUE;
            } else {
                score1 = -10 * MathUtils.log(10, p1);
            }
            Double score2;
            if (p2 == 0.0) {
                score2 = Double.MAX_VALUE;
            } else {
                score2 = -10 * MathUtils.log(10, p2);
            }
            Double score = score1 - score2;
            result.put(posMin, score);
        } else {
            Double score1;
            if (p1 == 0.0) {
                score1 = Double.MAX_VALUE;
            } else {
                score1 = -10 * MathUtils.log(10, p1);
            }
            Double score2;
            if (p2 == 0.0) {
                score2 = Double.MAX_VALUE;
            } else {
                score2 = -10 * MathUtils.log(10, p2);
            }
            Double score = score2 - score1;
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

        HashMap<Double, ArrayList<Integer>> result = new HashMap<>();

        for (int pos : positionToScoreMap.keySet()) {
            Double peptideScore = 0.0;
            Double depthScore = positionToScoreMap.get(pos).get(1);
            if (depthScore != null) {
                peptideScore += 0.5 * depthScore;
            }
            depthScore = positionToScoreMap.get(pos).get(2);
            if (depthScore != null) {
                peptideScore += 0.75 * depthScore;
            }
            depthScore = positionToScoreMap.get(pos).get(3);
            if (depthScore != null) {
                peptideScore += depthScore;
            }
            depthScore = positionToScoreMap.get(pos).get(4);
            if (depthScore != null) {
                peptideScore += depthScore;
            }
            depthScore = positionToScoreMap.get(pos).get(5);
            if (depthScore != null) {
                peptideScore += depthScore;
            }
            depthScore = positionToScoreMap.get(pos).get(6);
            if (depthScore != null) {
                peptideScore += depthScore;
            }
            depthScore = positionToScoreMap.get(pos).get(7);
            if (depthScore != null) {
                peptideScore += 0.75 * depthScore;
            }
            depthScore = positionToScoreMap.get(pos).get(8);
            if (depthScore != null) {
                peptideScore += 0.5 * depthScore;
            }
            depthScore = positionToScoreMap.get(pos).get(9);
            if (depthScore != null) {
                peptideScore += 0.25 * depthScore;
            }
            depthScore = positionToScoreMap.get(pos).get(10);
            if (depthScore != null) {
                peptideScore += 0.25 * depthScore;
            }
            ArrayList<Integer> sites = result.get(peptideScore);
            if (sites == null) {
                sites = new ArrayList<>(2);
                result.put(peptideScore, sites);
            }
            sites.add(pos);
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
     * @param annotationPreferences the global annotation preferences
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this peptide and spectrum
     * @param spectrumAnnotator the spectrum annotator which should be used to
     * annotate the spectrum
     * @param spectrum the spectrum of interest
     * @param spectrumMap the map of the extracted spectra: depth &gt; extracted
     * spectrum
     * @param possibleSites the possible modification sites
     *
     * @return a map PTM localization &gt; score
     *
     * @throws org.apache.commons.math.MathException exception thrown whenever a
     * math error occurred while computing the score or estimating the noise level
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public static HashMap<Integer, HashMap<Integer, Double>> getPositionToScoreMap(Peptide peptide, Peptide noModPeptide, ArrayList<Integer> possibleSites,
            MSnSpectrum spectrum, HashMap<Integer, MSnSpectrum> spectrumMap, AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences, PeptideSpectrumAnnotator spectrumAnnotator, Modification refPTM) throws MathException, InterruptedException {

        HashMap<Integer, HashMap<Integer, Double>> positionToScoreMap = new HashMap<>();

        int N = 0;

        for (ArrayList<Ion> fragmentIons : spectrumAnnotator.getExpectedIons(specificAnnotationPreferences, peptide).values()) {
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

                ArrayList<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences,
                        spectrumMap.get(i), tempPeptide, false);
                int n = matches.size();

                BinomialDistribution distribution = new BinomialDistribution(N, p);
                Double bigP = distribution.getDescendingCumulativeProbabilityAt((double) n);
                Double score = -10 * MathUtils.log(10, bigP);
                HashMap<Integer, Double> scoresAtPosition = positionToScoreMap.get(pos);
                if (scoresAtPosition == null) {
                    scoresAtPosition = new HashMap<>(2);
                    positionToScoreMap.put(pos, scoresAtPosition);
                }
                scoresAtPosition.put(i + 1, score);
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
     *
     * @return a map containing the spectra filtered indexed by peak depth.
     */
    public static HashMap<Integer, MSnSpectrum> getReducedSpectra(MSnSpectrum baseSpectrum, double mzTolerance) {
        return getReducedSpectra(baseSpectrum, mzTolerance, -1);
    }

    /**
     * Generates a map containing the spectra filtered on intensity with a basis
     * of 20*m/z tolerance indexed by the depth used.
     *
     * @param baseSpectrum the base spectrum
     * @param mzTolerance the m/z tolerance
     * @param depthMax the depth to look into (10 for A-score). If -1 the
     * maximal depth will be used
     *
     * @return a map containing the spectra filtered indexed by peak depth.
     */
    public static HashMap<Integer, MSnSpectrum> getReducedSpectra(MSnSpectrum baseSpectrum, double mzTolerance, int depthMax) {

        HashMap<Integer, MSnSpectrum> result = new HashMap<>();
        HashMap<Double, Peak> tempMap, peakMap = baseSpectrum.getPeakMap();
        ArrayList<Double> intensities, mz = new ArrayList<>(peakMap.keySet());
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
            result.put(i, new MSnSpectrum(2, baseSpectrum.getPrecursor(), baseSpectrum.getSpectrumTitle() + "_" + i, new HashMap<>(), "a score"));
        }

        cpt = 0;
        currentmzMin = 0;

        while (currentmzMin < mzMax) {
            intensities = new ArrayList<>();
            tempMap = new HashMap<>();

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
