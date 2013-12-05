package com.compomics.util.experiment.identification.ptm.ptmscores;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.math.BasicMathFunctions;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class estimates the PhosphoRS score as described in
 * http://www.ncbi.nlm.nih.gov/pubmed/22073976.
 * Warining: still under testing
 *
 * @author Marc Vaudel
 */
public class PhosphoRS {

    /**
     * The maximal depth to use per window (8 in the original paper).
     */
    public static final int maxDepth = 8;

    /**
     * Returns the PhosphoRS sequence probabilities for the PTM possible
     * locations. 0 is the first amino acid. Note that PTMs found on peptides 
     * must be loaded in the PTM factory.
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
     * @param accountNeutralLosses a boolean indicating whether or not the
     * calculation shall account for neutral losses.
     * @param matchingType the amino acid matching type to use to map PTMs on peptides
     *
     * @return a map site -> phosphoRS site probability
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
    public static HashMap<Integer, Double> getSequenceProbabilities(Peptide peptide, ArrayList<PTM> ptms, MSnSpectrum spectrum,
            HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance, boolean accountNeutralLosses, AminoAcidPattern.MatchingType matchingType)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        if (ptms.isEmpty()) {
            throw new IllegalArgumentException("No PTM given for PhosphoRS calculation.");
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

        HashMap<ArrayList<Integer>, Double> profileToScoreMap = new HashMap<ArrayList<Integer>, Double>();
        ArrayList<Integer> possibleSites = new ArrayList<Integer>();

        for (PTM ptm : ptms) {
            for (int potentialSite : peptide.getPotentialModificationSites(ptm, matchingType, mzTolerance)) {
                if (!possibleSites.contains(potentialSite)) {
                    possibleSites.add(potentialSite);
                }
            }
        }

        if (possibleSites.size() > nPTM) {

            Collections.sort(possibleSites);
            ArrayList<ArrayList<Integer>> possibleProfiles = getPossibleModificationProfiles(possibleSites, nPTM);

            PeptideSpectrumAnnotator spectrumAnnotator = new PeptideSpectrumAnnotator();
            Peptide noModPeptide = Peptide.getNoModPeptide(peptide, ptms);
            double p = getp(spectrum, mzTolerance);

            HashMap<Double, ArrayList<ArrayList<Integer>>> siteDeterminingIonsMap = getSiteDeterminingIons(
                    noModPeptide, possibleProfiles, refPTM.getName(), spectrumAnnotator, iontypes, scoringLossesMap, charges, precursorCharge);
            ArrayList<Double> siteDeterminingIons = new ArrayList<Double>(siteDeterminingIonsMap.keySet());
            Collections.sort(siteDeterminingIons);

            double minMz = spectrum.getMinMz(), maxMz = spectrum.getMaxMz(), tempMax;
            HashMap<Double, Peak> reducedSpectrum = new HashMap<Double, Peak>();

            while (minMz < maxMz) {
                tempMax = minMz + 100;
                MSnSpectrum tempSpectrum = new MSnSpectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle()
                        + "_PhosphoRS_minMZ_" + minMz, spectrum.getSubSpectrum(minMz, tempMax), spectrum.getFileName());
                ArrayList<MSnSpectrum> spectra = getReducedSpectra(tempSpectrum);
                HashMap<ArrayList<Integer>, ArrayList<Double>> subMapGoofy = new HashMap<ArrayList<Integer>, ArrayList<Double>>();
                for (double ionMz : siteDeterminingIons) {
                    if (ionMz > minMz && ionMz <= maxMz) {
                        ArrayList<ArrayList<Integer>> profiles = siteDeterminingIonsMap.get(ionMz);
                        for (ArrayList<Integer> profile : profiles) {
                            ArrayList<Double> mzs = subMapGoofy.get(profile);
                            if (mzs == null) {
                                mzs = new ArrayList<Double>();
                                subMapGoofy.put(profile, mzs);
                            }
                            mzs.add(ionMz);
                        }
                    }
                }

                if (!subMapGoofy.isEmpty()) {

                    ArrayList<ArrayList<Double>> deltas = new ArrayList<ArrayList<Double>>();
                    int nDeltas = 0;

                    for (int i = 0; i < spectra.size(); i++) {

                        ArrayList<Double> scores = new ArrayList<Double>();
                        ArrayList<Double> currentDeltas = new ArrayList<Double>();
                        ArrayList<ArrayList<Double>> scored = new ArrayList<ArrayList<Double>>();
                        boolean noIons = false;
                        for (ArrayList<Integer> profile : possibleProfiles) {
                            if (!subMapGoofy.containsKey(profile)) {
                                if (!noIons) {
                                    noIons = true;
                                    Peptide tempPeptide = Peptide.getNoModPeptide(peptide, ptms);
                                    for (int pos : profile) {
                                        tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, pos));
                                    }
                                    double score = getPhosphoRsScore(tempPeptide, spectra.get(i), p, spectrumAnnotator, iontypes, scoringLossesMap, charges, precursorCharge, mzTolerance);
                                    scores.add(score);
                                }
                            } else {
                                ArrayList<Double> tempSiteDeterminingIons = subMapGoofy.get(profile);
                                boolean alreadyScored = false;
                                for (ArrayList<Double> scoredIons : scored) {
                                    if (Util.sameLists(tempSiteDeterminingIons, scoredIons)) {
                                        alreadyScored = true;
                                        break;
                                    }
                                }
                                if (!alreadyScored) {
                                    Peptide tempPeptide = Peptide.getNoModPeptide(peptide, ptms);
                                    for (int pos : profile) {
                                        tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, pos));
                                    }
                                    double score = getPhosphoRsScore(tempPeptide, spectra.get(i), p, spectrumAnnotator, iontypes, scoringLossesMap, charges, precursorCharge, mzTolerance);
                                    scores.add(score);
                                    scored.add(tempSiteDeterminingIons);
                                }
                            }
                        }

                        Collections.sort(scores, Collections.reverseOrder());

                        for (int j = 0; j < scores.size() - 1; j++) {
                            currentDeltas.add(scores.get(j) - scores.get(j + 1));
                        }

                        if (currentDeltas.size() > nDeltas) {
                            nDeltas = currentDeltas.size();
                        }

                        deltas.add(currentDeltas);
                    }

                    int bestI = 0;
                    double largestDelta = 0;

                    for (int j = 0; j < nDeltas; j++) {
                        for (int i = 0; i < deltas.size(); i++) {
                            ArrayList<Double> tempDeltas = deltas.get(i);
                            if (j < tempDeltas.size() && tempDeltas.get(j) > largestDelta) {
                                largestDelta = tempDeltas.get(j);
                                bestI = i;
                            }
                        }
                        if (largestDelta > 0) {
                            break;
                        }
                    }

                    if (largestDelta == 0) {
                        bestI = Math.min(maxDepth, spectra.size() - 1);
                    }

                    reducedSpectrum.putAll(spectra.get(bestI).getPeakMap());
                } else {

                    double bestScore = 0;
                    int bestI = 0;

                    for (int i = 0; i < spectra.size(); i++) {
                        double score = getPhosphoRsScore(peptide, spectra.get(i), p, spectrumAnnotator, iontypes, scoringLossesMap, charges, precursorCharge, mzTolerance);
                        if (score >= bestScore) {
                            bestScore = score;
                            bestI = i;
                        }
                    }

                    reducedSpectrum.putAll(spectra.get(bestI).getPeakMap());
                }
                minMz = tempMax;
            }

            MSnSpectrum phosphoRsSpectrum = new MSnSpectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle() + "_phosphoRS", reducedSpectrum, spectrum.getFileName());
            HashMap<ArrayList<Integer>, Double> pInvMap = new HashMap<ArrayList<Integer>, Double>(possibleProfiles.size());
            double pInvTotal = 0;

            for (ArrayList<Integer> profile : possibleProfiles) {

                Peptide tempPeptide = Peptide.getNoModPeptide(peptide, ptms);

                for (int pos : profile) {
                    tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, pos));
                }

                double score = getPhosphoRsScore(tempPeptide, phosphoRsSpectrum, p, spectrumAnnotator, iontypes, scoringLossesMap, charges, precursorCharge, mzTolerance);
                double pInv = Math.pow(10, score/10);
                pInvMap.put(profile, pInv);
                pInvTotal += pInv;
            }

            for (ArrayList<Integer> profile : possibleProfiles) {
                double phosphoRsProbability = pInvMap.get(profile) / pInvTotal * 100; //in percent
                profileToScoreMap.put(profile, phosphoRsProbability);
            }

        } else if (possibleSites.size() == nPTM) {
            profileToScoreMap.put(possibleSites, 100.0);
        } else {
            throw new IllegalArgumentException("Found less potential modification sites than PTMs during A-score calculation. Peptide key: " + peptide.getKey());
        }
        
        HashMap<Integer, Double> scores = new HashMap<Integer, Double>();
        for (ArrayList<Integer> profile : profileToScoreMap.keySet()) {
            double score = profileToScoreMap.get(profile);
            for (Integer site : profile) {
                if (!scores.containsKey(site)) {
                    scores.put(site, score);
                } else {
                    scores.put(site, scores.get(site) + score);
                }
            }
        }

        return scores;
    }

    /**
     * Returns the PhosphoRS score of the given peptide on the given spectrum.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param p the probability for a calculated fragment matching one of the
     * experimental masses by chance as estimated by PhosphoRS
     * @param spectrumAnnotator spectrum annotator
     * @param iontypes the ion types to use for spectrum annotation
     * @param scoringLossesMap the neutral losses to use
     * @param charges the fragment charges to look for
     * @param precursorCharge the charge of the precursor
     * @param mzTolerance the ms2 mz tolerance
     *
     * @return the phosphoRS score
     */
    private static double getPhosphoRsScore(Peptide peptide, MSnSpectrum spectrum, double p, PeptideSpectrumAnnotator spectrumAnnotator,
            HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap scoringLossesMap,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance) {

        int N = 0;

        for (ArrayList<Ion> fragmentIons : spectrumAnnotator.getExpectedIons(iontypes, scoringLossesMap, charges, precursorCharge, peptide).values()) {
            N += fragmentIons.size();
        }

        ArrayList<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(iontypes, scoringLossesMap, charges, precursorCharge, spectrum, peptide, 0, mzTolerance, false);
        int n = matches.size();
        double P = 0;

        for (int k = n; k <= N; k++) {
            P += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
        }

        if (P <= Double.MIN_NORMAL) {
            P = Double.MIN_NORMAL;
        }

        double score = -10 * Math.log10(P);
        return score;
    }

    /**
     * The probability p for a calculated fragment matching one of the
     * experimental masses by chance as estimated in the PhosphoRS algorithm.
     *
     * @param spectrum the spectrum studied
     * @param ms2Tolerance the ms2 Tolerance
     *
     * @return the probability p for a calculated fragment matching one of the
     * experimental masses by chance as estimated in the PhosphoRS algorithm.
     */
    private static double getp(Spectrum spectrum, double ms2Tolerance) {
        int N = spectrum.getPeakMap().size();
        double w = spectrum.getMaxMz() - spectrum.getMinMz();
        return ms2Tolerance * N / w;
    }

    /**
     * Returns the possible modification profiles given the possible sites and
     * number of modifications.
     *
     * @param possibleSites the possible modification sites
     * @param nPtms the number of modifications
     *
     * @return a list of possible modification profiles
     */
    private static ArrayList<ArrayList<Integer>> getPossibleModificationProfiles(ArrayList<Integer> possibleSites, int nPtms) {

        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

        for (int pos : possibleSites) {
            ArrayList<Integer> profile = new ArrayList<Integer>(nPtms);
            profile.add(pos);
            result.add(profile);
        }

        for (int i = 2; i <= nPtms; i++) {
            ArrayList<ArrayList<Integer>> tempresult = new ArrayList<ArrayList<Integer>>(result);
            result = new ArrayList<ArrayList<Integer>>();
            for (ArrayList<Integer> tempProfile : tempresult) {
                int lastPos = tempProfile.get(tempProfile.size() - 1);
                for (int pos : possibleSites) {
                    if (pos > lastPos) {
                        ArrayList<Integer> profile = new ArrayList<Integer>(tempProfile);
                        profile.add(pos);
                        result.add(profile);
                    }
                }
            }

        }

        return result;
    }

    /**
     * Returns a list of all potential site determining ions.
     *
     * @param noModPeptide the version of the peptide which does not contain the
     * modification of interest
     * @param possibleProfiles the possible modification profiles to inspect
     * @param referencePtmName the name of the reference ptm
     * @param spectrumAnnotator the spectrum annotator used throughout the
     * scoring
     * @param iontypes the ion types to use
     * @param scoringLossesMap the neutral losses to be used for scoring
     * @param charges the charges to be used for scoring
     * @param precursorCharge the precursor charge
     *
     * @return a list of mz where we can possibly find a site determining ion
     */
    private static HashMap<Double, ArrayList<ArrayList<Integer>>> getSiteDeterminingIons(Peptide noModPeptide, ArrayList<ArrayList<Integer>> possibleProfiles,
            String referencePtmName, PeptideSpectrumAnnotator spectrumAnnotator, HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap scoringLossesMap,
            ArrayList<Integer> charges, int precursorCharge) {

        HashMap<Double, ArrayList<ArrayList<Integer>>> siteDeterminingIons = new HashMap<Double, ArrayList<ArrayList<Integer>>>();
        HashMap<Double, ArrayList<ArrayList<Integer>>> commonIons = new HashMap<Double, ArrayList<ArrayList<Integer>>>();

        for (ArrayList<Integer> modificationProfile : possibleProfiles) {

            Peptide peptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getModificationMatches());

            for (int pos : modificationProfile) {
                peptide.addModificationMatch(new ModificationMatch(referencePtmName, true, pos));
            }

            ArrayList<Double> mzs = new ArrayList<Double>();

            for (ArrayList<Ion> ions : spectrumAnnotator.getExpectedIons(iontypes, scoringLossesMap, charges, precursorCharge, peptide).values()) {
                for (Ion ion : ions) {
                    for (int charge : charges) {
                        double mz = ion.getTheoreticMz(charge);
                        if (!mzs.contains(mz)) {
                            mzs.add(mz);
                        }
                    }
                }
            }

            for (double mz : mzs) {
                if (commonIons.isEmpty()) {
                    ArrayList<ArrayList<Integer>> profiles = new ArrayList<ArrayList<Integer>>();
                    commonIons.put(mz, profiles);
                    profiles.add(modificationProfile);
                } else {
                    if (!commonIons.containsKey(mz)) {
                        ArrayList<ArrayList<Integer>> profiles = siteDeterminingIons.get(mz);
                        if (profiles == null) {
                            profiles = new ArrayList<ArrayList<Integer>>();
                            siteDeterminingIons.put(mz, profiles);
                        }
                        profiles.add(modificationProfile);
                    }
                }
            }

            for (double mz : new HashSet<Double>(commonIons.keySet())) {
                if (!mzs.contains(mz)) {
                    siteDeterminingIons.put(mz, commonIons.get(mz));
                    commonIons.remove(mz);
                } else {
                    commonIons.get(mz).add(modificationProfile);
                }
            }
        }

        return siteDeterminingIons;
    }

    /**
     * Returns a list of spectra containing only the most intense ions. The
     * index of the spectrum in the list corresponds to the number of peaks, 1
     * is the first.
     *
     * @param spectrum the spectrum of interest
     *
     * @return a list of spectra containing only the most intense ions.
     */
    private static ArrayList<MSnSpectrum> getReducedSpectra(MSnSpectrum spectrum) {

        ArrayList<MSnSpectrum> reducedSpectra = new ArrayList<MSnSpectrum>();
        HashMap<Double, ArrayList<Peak>> intensityToPeakMap = new HashMap<Double, ArrayList<Peak>>(spectrum.getPeakMap().size());

        for (Peak peak : spectrum.getPeakList()) {
            double intensity = peak.intensity;
            ArrayList<Peak> peaks = intensityToPeakMap.get(intensity);
            if (peaks == null) {
                peaks = new ArrayList<Peak>();
                intensityToPeakMap.put(intensity, peaks);
            }
            peaks.add(peak);
        }

        ArrayList<Double> intensities = new ArrayList<Double>(intensityToPeakMap.keySet());
        Collections.sort(intensities, Collections.reverseOrder());
        int depth = 1;

        while (depth <= maxDepth) {

            HashMap<Double, Peak> mzToPeak = new HashMap<Double, Peak>();
            int nPeaks = 0;

            for (double intensity : intensities) {
                for (Peak peak : intensityToPeakMap.get(intensity)) {
                    mzToPeak.put(peak.mz, peak);
                    nPeaks++;
                    if (nPeaks == depth) {
                        break;
                    }
                }
                if (nPeaks == depth) {
                    break;
                }
            }

            reducedSpectra.add(new MSnSpectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle() + "_" + depth, mzToPeak, spectrum.getFileName()));
            depth++;
        }

        return reducedSpectra;
    }
}
