package com.compomics.util.experiment.identification.ptm.ptmscores;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.preferences.AnnotationPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.preferences.SpecificAnnotationPreferences;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class estimates the PhosphoRS score as described in
 * http://www.ncbi.nlm.nih.gov/pubmed/22073976. Warning: the calculation in its
 * present form is very slow for multiply modified peptides, peptides with many
 * modification sites, and noisy spectra. Typically, avoid scoring deamindation
 * sites.
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
     * locations. 1 is the first amino acid. The N-terminus is indexed 0 and the
     * C-terminus with the peptide length+1. Note that PTMs found on peptides
     * must be loaded in the PTM factory
     * (com.compomics.util.experiment.biology.PTMFactory), and if the scoring
     * involves protein terminal PTMs, the protein sequences must be loaded in
     * the sequence factory
     * (com.compomics.util.experiment.identification.SequenceFactory) and
     * indexed using the protein tree (see getDefaultProteinTree in
     * SequenceFactory). PTMs of same mass should be scored together and given
     * in the ptms list. Neutral losses of mass equal to the mass of the PTM
     * will be ignored. Neutral losses to be accounted for should be given in
     * the SpecificAnnotationPreferences and will be ignored if
     * accountNeutralLosses is false.
     *
     * @param peptide The peptide of interest
     * @param ptms The PTMs to score, for instance different phosphorylations.
     * These PTMs are considered as indistinguishable, i.e. of same mass.
     * @param spectrum The corresponding spectrum
     * @param annotationPreferences the global annotation preferences
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this peptide and spectrum
     * @param accountNeutralLosses a boolean indicating whether or not the
     * calculation shall account for neutral losses.
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param spectrumAnnotator the peptide spectrum annotator to use for
     * spectrum annotation, can be null
     * @param ptmScoreScale number of decimals to use for the score calculation
     *
     * @return a map site &gt; phosphoRS site probability
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
     */
    public static HashMap<Integer, Double> getSequenceProbabilities(Peptide peptide, ArrayList<PTM> ptms, MSnSpectrum spectrum,
            AnnotationPreferences annotationPreferences, SpecificAnnotationPreferences specificAnnotationPreferences, boolean accountNeutralLosses, SequenceMatchingPreferences sequenceMatchingPreferences,
            PeptideSpectrumAnnotator spectrumAnnotator, Integer ptmScoreScale)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        if (ptms.isEmpty()) {
            throw new IllegalArgumentException("No PTM given for PhosphoRS calculation.");
        }

        if (spectrumAnnotator == null) {
            spectrumAnnotator = new PeptideSpectrumAnnotator();
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
            throw new IllegalArgumentException("Given PTMs not found in the peptide for PhosphoRS calculation.");
        }

        PTM refPTM = ptms.get(0);
        double ptmMass = refPTM.getMass();

        NeutralLossesMap annotationNeutralLosses = specificAnnotationPreferences.getNeutralLossesMap(),
                scoringLossesMap = new NeutralLossesMap();
        if (accountNeutralLosses) {
            // here annotation should be sequence and modification independant
            for (NeutralLoss neutralLoss : annotationNeutralLosses.getAccountedNeutralLosses()) {
                if (Math.abs(neutralLoss.mass - ptmMass) > specificAnnotationPreferences.getFragmentIonAccuracy()) {
                    scoringLossesMap.addNeutralLoss(neutralLoss, 1, 1);
                }
            }
        }

        HashMap<ArrayList<Integer>, Double> profileToScoreMap = new HashMap<ArrayList<Integer>, Double>();
        ArrayList<Integer> possibleSites = new ArrayList<Integer>();

        int peptideLength = peptide.getSequence().length();

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
            ArrayList<ArrayList<Integer>> possibleProfiles = getPossibleModificationProfiles(possibleSites, nPTM);

            Peptide noModPeptide = Peptide.getNoModPeptide(peptide, ptms);
            double p = getp(spectrum, specificAnnotationPreferences.getFragmentIonAccuracy());

            HashMap<Double, ArrayList<ArrayList<Integer>>> siteDeterminingIonsMap = getSiteDeterminingIons(
                    noModPeptide, possibleProfiles, refPTM.getName(), spectrumAnnotator, annotationPreferences, specificAnnotationPreferences);
            ArrayList<Double> siteDeterminingIons = new ArrayList<Double>(siteDeterminingIonsMap.keySet());
            Collections.sort(siteDeterminingIons);

            double minMz = spectrum.getMinMz(), maxMz = spectrum.getMaxMz(), tempMax;
            HashMap<Double, Peak> reducedSpectrum = new HashMap<Double, Peak>();

            while (minMz < maxMz) {

                tempMax = minMz + 100;
                MSnSpectrum tempSpectrum = new MSnSpectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle()
                        + "_PhosphoRS_minMZ_" + minMz, spectrum.getSubSpectrum(minMz, tempMax), spectrum.getFileName());
                ArrayList<MSnSpectrum> spectra = getReducedSpectra(tempSpectrum);
                HashMap<ArrayList<Integer>, HashSet<Double>> subMapGoofy = new HashMap<ArrayList<Integer>, HashSet<Double>>();
                for (double ionMz : siteDeterminingIons) {
                    if (ionMz > minMz && ionMz <= maxMz) {
                        ArrayList<ArrayList<Integer>> profiles = siteDeterminingIonsMap.get(ionMz);
                        for (ArrayList<Integer> profile : profiles) {
                            HashSet<Double> mzs = subMapGoofy.get(profile);
                            if (mzs == null) {
                                mzs = new HashSet<Double>();
                                subMapGoofy.put(profile, mzs);
                            }
                            mzs.add(ionMz);
                        }
                    }
                }

                if (!subMapGoofy.isEmpty()) {

                    ArrayList<ArrayList<Double>> deltas = new ArrayList<ArrayList<Double>>();
                    int nDeltas = 0;

                    for (MSnSpectrum currentSpectrum : spectra) {
                        ArrayList<Double> scores = new ArrayList<Double>();
                        ArrayList<Double> currentDeltas = new ArrayList<Double>();
                        ArrayList<HashSet<Double>> scored = new ArrayList<HashSet<Double>>();
                        boolean noIons = false;
                        for (ArrayList<Integer> profile : possibleProfiles) {
                            if (!subMapGoofy.containsKey(profile)) {
                                if (!noIons) {
                                    noIons = true;
                                    Peptide tempPeptide = Peptide.getNoModPeptide(peptide, ptms);
                                    for (int pos : profile) {
                                        int index = pos;
                                        if (index == 0) {
                                            index = 1;
                                        } else if (index == peptideLength + 1) {
                                            index = peptideLength;
                                        }
                                        tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, index));
                                    }
                                    double score = getPhosphoRsScore(tempPeptide, currentSpectrum, p, spectrumAnnotator, annotationPreferences, specificAnnotationPreferences);
                                    scores.add(score);
                                }
                            } else {
                                HashSet<Double> tempSiteDeterminingIons = subMapGoofy.get(profile);
                                boolean alreadyScored = false;
                                for (HashSet<Double> scoredIons : scored) {
                                    if (Util.sameSets(tempSiteDeterminingIons, scoredIons)) {
                                        alreadyScored = true;
                                        break;
                                    }
                                }
                                if (!alreadyScored) {
                                    Peptide tempPeptide = Peptide.getNoModPeptide(peptide, ptms);
                                    for (int pos : profile) {
                                        int index = pos;
                                        if (index == 0) {
                                            index = 1;
                                        } else if (index == peptideLength + 1) {
                                            index = peptideLength;
                                        }
                                        tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, index));
                                    }
                                    double score = getPhosphoRsScore(tempPeptide, currentSpectrum, p, spectrumAnnotator, annotationPreferences, specificAnnotationPreferences);
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
                        double score = getPhosphoRsScore(peptide, spectra.get(i), p, spectrumAnnotator, annotationPreferences, specificAnnotationPreferences);
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
                    int index = pos;
                    if (index == 0) {
                        index = 1;
                    } else if (index == peptideLength + 1) {
                        index = peptideLength;
                    }
                    tempPeptide.addModificationMatch(new ModificationMatch(refPTM.getName(), true, index));
                }

                double score = getPhosphoRsScore(tempPeptide, phosphoRsSpectrum, p, spectrumAnnotator, annotationPreferences, specificAnnotationPreferences);
                double pInv = Math.pow(10, score / 10);
                pInvMap.put(profile, pInv);
                pInvTotal += pInv;
            }

            for (ArrayList<Integer> profile : possibleProfiles) {
                Double phosphoRsProbability = pInvMap.get(profile) * 100 / pInvTotal; //in percent
                profileToScoreMap.put(profile, phosphoRsProbability);
            }

        } else if (possibleSites.size() == nPTM) {
            profileToScoreMap.put(possibleSites, 100.0);
        } else {
            throw new IllegalArgumentException("Found less potential modification sites than PTMs during PhosphoRS calculation. Peptide key: " + peptide.getKey());
        }

        HashMap<Integer, BigDecimal> scores = new HashMap<Integer, BigDecimal>();
        MathContext mathContext = new MathContext(ptmScoreScale, RoundingMode.HALF_DOWN);
        for (ArrayList<Integer> profile : profileToScoreMap.keySet()) {
            Double score = profileToScoreMap.get(profile);
            BigDecimal scoreBigDecimal = new BigDecimal(score, mathContext);
            for (Integer site : profile) {
                BigDecimal previousScore = scores.get(site);
                if (previousScore == null) {
                    scores.put(site, scoreBigDecimal);
                } else {
                    BigDecimal newScore = scoreBigDecimal.add(previousScore, mathContext);
                    scores.put(site, newScore);
                }
            }
        }

        for (int site : possibleSites) {
            if (!scores.keySet().contains(site)) {
                throw new IllegalArgumentException("Site " + site + " not scored for modification " + ptmMass + " in spectrum " + spectrum.getSpectrumTitle() + " of file " + spectrum.getFileName() + ".");
            }
        }

        HashMap<Integer, Double> doubleScoreMap = new HashMap<Integer, Double>(scores.size());
        for (Integer site : scores.keySet()) {
            Double score = scores.get(site).doubleValue();
            doubleScoreMap.put(site, score);
        }

        return doubleScoreMap;
    }

    /**
     * Returns the PhosphoRS score of the given peptide on the given spectrum.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param p the probability for a calculated fragment matching one of the
     * experimental masses by chance as estimated by PhosphoRS
     * @param spectrumAnnotator spectrum annotator
     * @param annotationPreferences the global annotation preferences
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this peptide and spectrum
     *
     * @return the phosphoRS score
     */
    private static double getPhosphoRsScore(Peptide peptide, MSnSpectrum spectrum, double p, PeptideSpectrumAnnotator spectrumAnnotator,
            AnnotationPreferences annotationPreferences, SpecificAnnotationPreferences specificAnnotationPreferences) {

        int N = 0;

        for (ArrayList<Ion> fragmentIons : spectrumAnnotator.getExpectedIons(specificAnnotationPreferences, peptide).values()) {
            N += fragmentIons.size();
        }

        ArrayList<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences, spectrum, peptide);
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
     * @param annotationPreferences the global annotation preferences
     * @param specificAnnotationPreferences the annotation preferences specific
     * to this peptide and spectrum
     *
     * @return a list of mz where we can possibly find a site determining ion
     */
    private static HashMap<Double, ArrayList<ArrayList<Integer>>> getSiteDeterminingIons(Peptide noModPeptide, ArrayList<ArrayList<Integer>> possibleProfiles,
            String referencePtmName, PeptideSpectrumAnnotator spectrumAnnotator, AnnotationPreferences annotationPreferences, SpecificAnnotationPreferences specificAnnotationPreferences) {

        HashMap<Double, ArrayList<ArrayList<Integer>>> siteDeterminingIons = new HashMap<Double, ArrayList<ArrayList<Integer>>>();
        HashMap<Double, ArrayList<ArrayList<Integer>>> commonIons = new HashMap<Double, ArrayList<ArrayList<Integer>>>();

        for (ArrayList<Integer> modificationProfile : possibleProfiles) {

            String sequence = noModPeptide.getSequence();
            Peptide peptide = new Peptide(sequence, noModPeptide.getModificationMatches());
            int sequenceLength = sequence.length();

            for (int pos : modificationProfile) {
                int position;
                if (pos == 0) {
                    position = 1;
                } else if (pos == sequenceLength + 1) {
                    position = sequenceLength;
                } else {
                    position = pos;
                }
                peptide.addModificationMatch(new ModificationMatch(referencePtmName, true, position));
            }

            HashSet<Double> mzs = new HashSet<Double>();

            for (ArrayList<Ion> ions : spectrumAnnotator.getExpectedIons(specificAnnotationPreferences, peptide).values()) {
                for (Ion ion : ions) {
                    for (int charge : specificAnnotationPreferences.getSelectedCharges()) {
                        double mz = ion.getTheoreticMz(charge);
                        mzs.add(mz);
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
