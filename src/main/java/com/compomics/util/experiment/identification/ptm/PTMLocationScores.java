package com.compomics.util.experiment.identification.ptm;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.math.BasicMathFunctions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class scores PTM locations using various scores.
 *
 * @author Marc Vaudel
 */
public class PTMLocationScores {

    /**
     * Returns the A-score for the best PTM location without accounting for
     * neutral losses. In case the two best locations score the same they are
     * both given with the score of 0.
     *
     * @param peptide The peptide of interest
     * @param ptm The PTM to score
     * @param nPTM The number of occurrences where this PTM is expected on this
     * peptide
     * @param spectrum The corresponding spectrum
     * @param iontypes The fragment ions to look for
     * @param charges The fragment ions charges to look for
     * @param precursorCharge The precursor charge
     * @param mzTolerance The m/z tolerance to use
     * @return a map containing the best or two best PTM location(s) and the
     * corresponding A-score
     */
    public static HashMap<ArrayList<Integer>, Double> getAScore(Peptide peptide, PTM ptm, int nPTM, MSnSpectrum spectrum,
            HashMap<Ion.IonType, ArrayList<Integer>> iontypes,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance) {
        return getAScore(peptide, ptm, nPTM, spectrum, iontypes, null, charges, precursorCharge, mzTolerance, false);
    }

    /**
     * Returns the A-score for the best PTM location accounting for neutral
     * losses. In case the two best locations score the same they are both given
     * with the score of 0.
     *
     * @param peptide The peptide of interest
     * @param ptm The PTM to score
     * @param nPTM The number of occurrences where this PTM is expected on this
     * peptide
     * @param spectrum The corresponding spectrum
     * @param iontypes The fragment ions to look for
     * @param neutralLosses The neutral losses to look for
     * @param charges The fragment ions charges to look for
     * @param precursorCharge The precursor charge
     * @param mzTolerance The m/z tolerance to use
     * @return a map containing the best or two best PTM location(s) and the
     * corresponding A-score
     */
    public static HashMap<ArrayList<Integer>, Double> getAScore(Peptide peptide, PTM ptm, int nPTM, MSnSpectrum spectrum,
            HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance) {
        return getAScore(peptide, ptm, nPTM, spectrum, iontypes, neutralLosses, charges, precursorCharge, mzTolerance, true);
    }

    /**
     * Returns the A-score for the best PTM location. In case the two best
     * locations score the same they are both given with the score of 0.
     *
     * @param peptide The peptide of interest
     * @param ptm The PTM to score
     * @param nPTM The number of occurrences where this PTM is expected on this
     * peptide
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
     */
    public static HashMap<ArrayList<Integer>, Double> getAScore(Peptide peptide, PTM ptm, int nPTM, MSnSpectrum spectrum,
            HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance, boolean accountNeutralLosses) {

        NeutralLossesMap scoringLossesMap = new NeutralLossesMap();

        if (accountNeutralLosses) {
            // here annotation should be sequence and modification independant
            for (NeutralLoss neutralLoss : neutralLosses.getAccountedNeutralLosses()) {
                if (Math.abs(neutralLoss.mass - ptm.getMass()) > mzTolerance) {
                    scoringLossesMap.addNeutralLoss(neutralLoss, 1, 1);
                }
            }
        }

        HashMap<ArrayList<Integer>, Double> result = new HashMap<ArrayList<Integer>, Double>();
        ArrayList<Integer> possibleSites = Peptide.getPotentialModificationSites(peptide.getSequence(), ptm);

        if (possibleSites.size() > nPTM) {
            Collections.sort(possibleSites);
            ArrayList<IonMatch> matches;
            HashMap<Integer, HashMap<Integer, Double>> positionToScoreMap = new HashMap<Integer, HashMap<Integer, Double>>();
            HashMap<Integer, MSnSpectrum> spectrumMap = getReducedSpectra(spectrum, mzTolerance, 10);
            SpectrumAnnotator spectrumAnnotator = new SpectrumAnnotator();

            double p, P, score1, score2, score;
            int n, N = 0;
            Peptide tempPeptide, noModPeptide = new Peptide(peptide.getSequence(), peptide.getParentProteins(), new ArrayList<ModificationMatch>());

            for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
                if (!modificationMatch.getTheoreticPtm().equals(ptm.getName())) {
                    noModPeptide.addModificationMatch(modificationMatch);
                }
            }

            for (ArrayList<Ion> fragmentIons : spectrumAnnotator.getExpectedIons(iontypes, scoringLossesMap, charges, precursorCharge, peptide).values()) {
                N += fragmentIons.size();
            }


            for (int i = 0; i < spectrumMap.keySet().size(); i++) {

                p = ((double) i + 1) / 100;

                for (int pos : possibleSites) {
                    tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getParentProteins(), noModPeptide.getModificationMatches());
                    tempPeptide.addModificationMatch(new ModificationMatch(ptm.getName(), true, pos + 1));
                    matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, scoringLossesMap, charges, precursorCharge, spectrumMap.get(i), tempPeptide, 0, mzTolerance, false);
                    n = matches.size();
                    P = 0;
                    for (int k = n; k <= N; k++) {
                        P += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
                    }
                    if (P <= Double.MIN_NORMAL) {
                        P = Double.MIN_NORMAL;
                    }
                    score = -10 * Math.log10(P);
                    if (!positionToScoreMap.containsKey(pos)) {
                        positionToScoreMap.put(pos, new HashMap<Integer, Double>());
                    }
                    positionToScoreMap.get(pos).put(i + 1, score);
                }
            }

            Double peptideScore, bestScore = null, secondScore = null;
            Integer bestPosition = null, secondPosition = null;
            ArrayList<Integer> bestPositions = new ArrayList<Integer>();
            for (int pos : positionToScoreMap.keySet()) {
                peptideScore = 0.0;
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
                if (bestScore == null) {
                    bestScore = peptideScore;
                    bestPosition = pos;
                    bestPositions.add(pos + 1);
                } else if (peptideScore >= bestScore) {
                    if (secondScore == null || bestScore >= secondScore) {
                        secondScore = bestScore;
                        secondPosition = bestPosition;
                    }
                    if (peptideScore > bestScore) {
                        bestPositions.clear();
                    }
                    bestScore = peptideScore;
                    bestPosition = pos;
                    bestPositions.add(pos + 1);
                } else if (secondScore == null || peptideScore >= secondScore) {
                    secondScore = peptideScore;
                    secondPosition = pos;
                }
            }

            double diff, maxDiff = 0;
            int bestI = 0;

            for (int i = 1; i <= 10; i++) {
                diff = positionToScoreMap.get(bestPosition).get(i) - positionToScoreMap.get(secondPosition).get(i);
                if (diff >= maxDiff) {
                    bestI = i - 1;
                    maxDiff = diff;
                }
            }

            N = 0;
            int aa;
            int posMin = Math.min(bestPosition, secondPosition);
            int posMax = Math.max(bestPosition, secondPosition);

            for (ArrayList<Ion> ions : spectrumAnnotator.getExpectedIons(
                    iontypes, scoringLossesMap, charges, precursorCharge, peptide).values()) {
                for (Ion ion : ions) {
                    if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                        PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
                        if (ion.getSubType() == PeptideFragmentIon.A_ION
                                || ion.getSubType() == PeptideFragmentIon.B_ION
                                || ion.getSubType() == PeptideFragmentIon.C_ION) {
                            aa = fragmentIon.getNumber();
                            if (aa > posMin && aa <= posMax) {
                                N++;
                            }
                        } else if (ion.getSubType() == PeptideFragmentIon.X_ION
                                || ion.getSubType() == PeptideFragmentIon.Y_ION
                                || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                            aa = peptide.getSequence().length() - fragmentIon.getNumber();
                            if (aa > posMin && aa <= posMax) {
                                N++;
                            }
                        }
                    }
                }
            }

            p = ((double) bestI + 1) / 100;
            tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getParentProteins(), noModPeptide.getModificationMatches());
            tempPeptide.addModificationMatch(new ModificationMatch(ptm.getName(), true, posMin + 1));
            matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, scoringLossesMap, charges, precursorCharge, spectrumMap.get(bestI), tempPeptide, 0, mzTolerance, false);
            n = 0;
            for (IonMatch match : matches) {
                Ion ion = match.ion;
                if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                    PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
                    if (ion.getSubType() == PeptideFragmentIon.A_ION
                            || ion.getSubType() == PeptideFragmentIon.B_ION
                            || ion.getSubType() == PeptideFragmentIon.C_ION) {
                        aa = fragmentIon.getNumber();
                        if (aa > posMin && aa <= posMax) {
                            n++;
                        }
                    } else if (ion.getSubType() == PeptideFragmentIon.X_ION
                            || ion.getSubType() == PeptideFragmentIon.Y_ION
                            || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                        aa = peptide.getSequence().length() - fragmentIon.getNumber();
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

            tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getParentProteins(), noModPeptide.getModificationMatches());
            tempPeptide.addModificationMatch(new ModificationMatch(ptm.getName(), true, posMax + 1));
            matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, scoringLossesMap, charges, precursorCharge, spectrumMap.get(bestI), tempPeptide, 0, mzTolerance, false);
            n = 0;
            for (IonMatch match : matches) {
                Ion ion = match.ion;
                if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                    PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
                    if (ion.getSubType() == PeptideFragmentIon.A_ION
                            || ion.getSubType() == PeptideFragmentIon.B_ION
                            || ion.getSubType() == PeptideFragmentIon.C_ION) {
                        aa = fragmentIon.getNumber();
                        if (aa > posMin && aa <= posMax) {
                            n++;
                        }
                    } else if (ion.getSubType() == PeptideFragmentIon.X_ION
                            || ion.getSubType() == PeptideFragmentIon.Y_ION
                            || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                        aa = peptide.getSequence().length() - fragmentIon.getNumber();
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
                result.put(bestPositions, 0.0);
            } else if (p1 < p2) {
                ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
                modificationProfile.add(posMin + 1);
                score1 = -10 * Math.log10(p1);
                score2 = -10 * Math.log10(p2);
                score = score1 - score2;
                result.put(modificationProfile, score);
            } else {
                ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
                modificationProfile.add(posMax + 1);
                score1 = -10 * Math.log10(p1);
                score2 = -10 * Math.log10(p2);
                score = score2 - score1;
                result.put(modificationProfile, score);
            }
        } else {
            ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
            for (int pos : possibleSites) {
                modificationProfile.add(pos + 1);
            }
            result.put(modificationProfile, 100.0);
        }
        return result;
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
        int cptTemp;
        double currentmzMin = 0;

        while (currentmzMin < mzMax) {
            cptTemp = 0;
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
        Peak tempPeak;

        while (currentmzMin < mzMax) {
            intensities = new ArrayList<Double>();
            tempMap = new HashMap<Double, Peak>();

            while (cpt < mz.size()
                    && mz.get(cpt) < currentmzMin + 20 * mzTolerance) {
                tempPeak = peakMap.get(mz.get(cpt));
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

    /**
     * Returns the ptm plot series in the jfreechart format for one psm.
     *
     * @param peptide The peptide of interest
     * @param ptm The PTM to score
     * @param nPTM The amount of times the PTM is expected
     * @param spectrum The corresponding spectrum
     * @param iontypes The fragment ions to look for
     * @param neutralLosses The neutral losses to look for
     * @param charges The fragment ions charges to look for
     * @param precursorCharge The precursor charge
     * @param mzTolerance The m/z tolerance to use
     * @param intensityLimit
     * @return the ptm plot series in the jfreechert format for one psm.
     */
    public static HashMap<PeptideFragmentIon, ArrayList<IonMatch>> getPTMPlotData(Peptide peptide, PTM ptm, int nPTM, MSnSpectrum spectrum,
            HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance, double intensityLimit) {

        Peptide noModPeptide = new Peptide(peptide.getSequence(), peptide.getParentProteins(), new ArrayList<ModificationMatch>());

        for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
            if (!modificationMatch.getTheoreticPtm().equals(ptm.getName())) {
                noModPeptide.addModificationMatch(modificationMatch);
            }
        }

        SpectrumAnnotator spectrumAnnotator = new SpectrumAnnotator();
        HashMap<Integer, ArrayList<Ion>> fragmentIons =
                spectrumAnnotator.getExpectedIons(iontypes, neutralLosses, charges, precursorCharge, noModPeptide);
        HashMap<PeptideFragmentIon, ArrayList<IonMatch>> map = new HashMap<PeptideFragmentIon, ArrayList<IonMatch>>();
        PeptideFragmentIon peptideFragmentIon, noModFragmentIon;
        ArrayList<IonMatch> matches;

        for (int i = 0; i <= nPTM; i++) {

            spectrumAnnotator.setMassShift(i * ptm.getMass());
            matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, neutralLosses, charges, precursorCharge, spectrum, noModPeptide, intensityLimit, mzTolerance, false);
            for (IonMatch ionMatch : matches) {
                if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                    peptideFragmentIon = (PeptideFragmentIon) ionMatch.ion;
                    for (Ion noModIon : fragmentIons.get(ionMatch.charge.value)) {
                        if (noModIon.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION
                                && peptideFragmentIon.isSameAs(noModIon)) {
                            noModFragmentIon = (PeptideFragmentIon) noModIon;
                            if (!map.containsKey(noModFragmentIon)) {
                                map.put(noModFragmentIon, new ArrayList<IonMatch>());
                            }
                            map.get(noModFragmentIon).add(ionMatch);
                            break;
                        }
                    }
                }
            }
        }
        return map;
    }

    /**
     * Get the PTM table content.
     *
     * @param peptide The peptide of interest
     * @param ptm The PTM to score
     * @param nPTM The amount of times the PTM is expected
     * @param spectrum The corresponding spectrum
     * @param iontypes The fragment ions to look for
     * @param neutralLosses The neutral losses to look for
     * @param charges The fragment ions charges to look for
     * @param precursorCharge The precursor charge
     * @param mzTolerance The m/z tolerance to use
     * @param intensityLimit
     * @return the PtmtableContent object
     */
    public static PtmtableContent getPTMTableContent(Peptide peptide, PTM ptm, int nPTM, MSnSpectrum spectrum,
            HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance, double intensityLimit) {

        PtmtableContent ptmTableContent = new PtmtableContent();

        Peptide noModPeptide = new Peptide(peptide.getSequence(), peptide.getParentProteins(), new ArrayList<ModificationMatch>());

        for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
            if (!modificationMatch.getTheoreticPtm().equals(ptm.getName())) {
                noModPeptide.addModificationMatch(modificationMatch);
            }
        }

        NeutralLossesMap lossesMap = new NeutralLossesMap();
        for (NeutralLoss neutralLoss : neutralLosses.getAccountedNeutralLosses()) {
            if (Math.abs(neutralLoss.mass - ptm.getMass()) > mzTolerance) {
                lossesMap.addNeutralLoss(neutralLoss, 1, 1);
            }
        }


        SpectrumAnnotator spectrumAnnotator = new SpectrumAnnotator();
        spectrumAnnotator.setPeptide(noModPeptide, precursorCharge);
        PeptideFragmentIon peptideFragmentIon;
        ArrayList<IonMatch> matches;

        for (int i = 0; i <= nPTM; i++) {

            spectrumAnnotator.setMassShift(i * ptm.getMass());
            matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, lossesMap, charges, precursorCharge, spectrum, noModPeptide, intensityLimit, mzTolerance, false);

            for (IonMatch ionMatch : matches) {
                if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                    peptideFragmentIon = (PeptideFragmentIon) ionMatch.ion;
                    if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION
                            || peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION
                            || peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                        ptmTableContent.addIntensity(i, peptideFragmentIon.getSubType(), peptideFragmentIon.getNumber(), ionMatch.peak.intensity);
                    } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION
                            || peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION
                            || peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                        ptmTableContent.addIntensity(i, peptideFragmentIon.getSubType(), peptide.getSequence().length() - peptideFragmentIon.getNumber() + 1, ionMatch.peak.intensity);
                    }
                }
            }
        }
        return ptmTableContent;
    }
}
