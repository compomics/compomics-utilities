package com.compomics.util.experiment.identification.modification.scores;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.IonFactory;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.math.statistics.distributions.BinomialDistribution;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import com.compomics.util.experiment.identification.utils.ModificationUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math.util.FastMath;

/**
 * This class estimates the PhosphoRS score as described in
 * http://www.ncbi.nlm.nih.gov/pubmed/22073976. Warning: the calculation in its
 * present form is very slow for multiply modified peptides, peptides with many
 * modification sites, and noisy spectra. Typically, avoid scoring deamidation
 * sites.
 *
 * @author Marc Vaudel
 */
public class PhosphoRS {

    /**
     * The window size in m/z.
     */
    public static final double WINDOW_SIZE = 100.0;
    /**
     * The maximal depth to use per window. 8 in the original paper, Must be
     * greater than 1.
     */
    public static final int MAX_DEPTH = 8;
    /**
     * The minimal depth to use per window.
     */
    public static final int MIN_DEPTH = 2;
    /**
     * The number of binomial distributions kept in cache.
     */
    private static final int distributionCacheSize = 1000;
    /**
     * The binomial distributions cache.
     */
    private static final HashMap<Double, HashMap<Integer, BinomialDistribution>> distributionCache = new HashMap<Double, HashMap<Integer, BinomialDistribution>>();

    /**
     * Returns the PhosphoRS sequence probabilities for the modification
     * possible locations. 1 is the first amino acid. The N-terminus is indexed
     * 0 and the C-terminus with the peptide length+1. Modifications of same
     * mass should be scored together and given in the modifications list.
     * Neutral losses of mass equal to the mass of the modification will be
     * ignored. Neutral losses to be accounted for should be given in the
     * SpecificAnnotationSettings and will be ignored if accountNeutralLosses is
     * false.
     *
     * @param peptide the peptide of interest
     * @param modifications the modifications to score, for instance different
     * phosphorylations (the modifications are considered as indistinguishable,
     * i.e. of same mass)
     * @param modificationParameters the modification parameters
     * @param spectrum the corresponding spectrum
     * @param sequenceProvider a provider for the protein sequences
     * @param annotationSettings the global annotation settings
     * @param specificAnnotationSettings the annotation settings specific to
     * this peptide and spectrum
     * @param accountNeutralLosses a boolean indicating whether or not the
     * calculation shall account for neutral losses.
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param modificationSequenceMatchingParameters the sequence matching
     * preferences for modification to peptide mapping
     * @param spectrumAnnotator the peptide spectrum annotator to use for
     * spectrum annotation, can be null
     *
     * @return a map site &gt; phosphoRS site probability
     */
    public static HashMap<Integer, Double> getSequenceProbabilities(Peptide peptide, ArrayList<Modification> modifications, ModificationParameters modificationParameters, Spectrum spectrum, SequenceProvider sequenceProvider, AnnotationParameters annotationSettings,
            SpecificAnnotationParameters specificAnnotationSettings, boolean accountNeutralLosses, SequenceMatchingParameters sequenceMatchingPreferences,
            SequenceMatchingParameters modificationSequenceMatchingParameters, PeptideSpectrumAnnotator spectrumAnnotator) {

        if (modifications.isEmpty()) {
            throw new IllegalArgumentException("No modification given for PhosphoRS calculation.");
        }

        if (spectrumAnnotator == null) {
            spectrumAnnotator = new PeptideSpectrumAnnotator();
        }

        int nModification = 0;

        ModificationMatch[] modificationMatches = peptide.getVariableModifications();

        nModification = (int) Arrays.stream(modificationMatches)
                .filter(modificationMatch -> modifications.stream()
                .anyMatch(modification -> modification.getName().equals(modificationMatch.getModification())))
                .count();

        if (nModification == 0) {
            throw new IllegalArgumentException("Given modifications not found in the peptide for PhosphoRS calculation.");
        }

        double modificationMass = modifications.get(0).getMass();

        NeutralLossesMap annotationNeutralLosses = specificAnnotationSettings.getNeutralLossesMap(),
                scoringLossesMap = new NeutralLossesMap();

        if (accountNeutralLosses) {

            // here annotation are sequence and modification independant
            for (String neutralLossName : annotationNeutralLosses.getAccountedNeutralLosses()) {

                NeutralLoss neutralLoss = NeutralLoss.getNeutralLoss(neutralLossName);

                if (Math.abs(neutralLoss.getMass() - modificationMass) > specificAnnotationSettings.getFragmentIonAccuracyInDa(spectrum.getMaxMz())) {
                    scoringLossesMap.addNeutralLoss(neutralLoss, 1, 1);
                }
            }
        }

        SpecificAnnotationParameters scoringAnnotationParameters = specificAnnotationSettings.clone();
        scoringAnnotationParameters.setNeutralLossesMap(scoringLossesMap);
        HashMap<Ion.IonType, HashSet<Integer>> ions = specificAnnotationSettings.getIonTypes(),
                newIons = new HashMap<>(1);

        for (Ion.IonType ionType : ions.keySet()) {

            if (ionType == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                newIons.put(ionType, ions.get(ionType));
            }
        }

        scoringAnnotationParameters.setSelectedIonsMap(newIons);
        int[] possibleSites = modifications.stream()
                .flatMapToInt(modification -> Arrays.stream(ModificationUtils.getPossibleModificationSites(peptide, modification, sequenceProvider, modificationSequenceMatchingParameters)))
                .distinct()
                .sorted()
                .toArray();

        HashMap<Long, Double> profileToScoreMap = new HashMap<>(possibleSites.length);
        HashMap<Long, int[]> profileToSitesMap = new HashMap<>(possibleSites.length);

        if (possibleSites.length > nModification) {

            spectrum = filterSpectrum(spectrum, scoringAnnotationParameters);

            HashSet<String> modNames = modifications.stream()
                    .map(modification -> modification.getName())
                    .collect(Collectors.toCollection(HashSet::new));
            Peptide noModPeptide = peptide.getNoModPeptide(modNames);
            ArrayList<int[]> possibleProfiles = getPossibleModificationProfiles(possibleSites, nModification);
            long[] possibleProfileKeys = new long[possibleProfiles.size()];

            for (int i = 0 ; i < possibleProfiles.size() ; i++) {

                int[] profile = possibleProfiles.get(i);
                long profileKey = getModificationProfileKey(profile);
                possibleProfileKeys[i] = profileKey;
                profileToSitesMap.put(profileKey, profile);

            }

            HashMap<Long, Peptide> profileToPeptide = getPossiblePeptidesMap(peptide, modNames, possibleProfileKeys, possibleProfiles);
            HashMap<Long, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>> profileToPossibleFragments = getPossiblePeptideFragments(profileToPeptide, scoringAnnotationParameters, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
            HashMap<Long, Integer> profileToN = getPossiblePeptideToN(profileToPeptide, profileToPossibleFragments, spectrumAnnotator, scoringAnnotationParameters, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);

            HashMap<Double, ArrayList<Long>> siteDeterminingIonsMap = getSiteDeterminingIons(noModPeptide, possibleProfileKeys, possibleProfiles, modifications, spectrumAnnotator, scoringAnnotationParameters, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
            ArrayList<Double> siteDeterminingIons = new ArrayList<>(siteDeterminingIonsMap.keySet());

            double minMz = spectrum.getMinMz(), maxMz = spectrum.getMaxMz(), tempMax;

            HashMap<Double, Peak> reducedSpectrum = new HashMap<>();

            double d = specificAnnotationSettings.getFragmentIonAccuracy();
            double dOverW = d / WINDOW_SIZE;
            dOverW = -FastMath.log10(dOverW);
            int nDecimals = ((int) dOverW) + 1;
            double halfWindow = WINDOW_SIZE / 2;

            while (minMz < maxMz) {

                tempMax = minMz + WINDOW_SIZE;

                if (specificAnnotationSettings.isFragmentIonPpm()) {
                    double refMz = minMz + halfWindow;
                    d = specificAnnotationSettings.getFragmentIonAccuracyInDa(refMz);
                    dOverW = d / WINDOW_SIZE;
                    dOverW = -FastMath.log10(dOverW);
                    nDecimals = ((int) dOverW) + 1;
                }

                HashMap<Double, Peak> extractedPeakList = spectrum.getSubSpectrum(minMz, tempMax);

                if (!extractedPeakList.isEmpty()) {

                    Spectrum tempSpectrum = new Spectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle()
                            + "_PhosphoRS_minMZ_" + minMz, extractedPeakList, spectrum.getFileName());

                    ArrayList<Spectrum> spectra = getReducedSpectra(tempSpectrum);

                    HashMap<Long, HashSet<Double>> profileToSiteDeterminingIonsMz = new HashMap<>(siteDeterminingIons.size());
                    for (double ionMz : siteDeterminingIons) {
                        if (ionMz > minMz && ionMz <= tempMax) {
                            ArrayList<Long> profiles = siteDeterminingIonsMap.get(ionMz);
                            for (long profileKey : profiles) {
                                HashSet<Double> mzs = profileToSiteDeterminingIonsMz.get(profileKey);
                                if (mzs == null) {
                                    mzs = new HashSet<>(1);
                                    profileToSiteDeterminingIonsMz.put(profileKey, mzs);
                                }
                                mzs.add(ionMz);
                            }
                        }
                    }

                    if (!profileToSiteDeterminingIonsMz.isEmpty()) {

                        ArrayList<ArrayList<Double>> deltas = new ArrayList<>(spectra.size());
                        int nDeltas = 0;

                        for (Spectrum currentSpectrum : spectra) {
                            TreeSet<Double> bigPs = new TreeSet<>();
                            ArrayList<Double> currentDeltas = new ArrayList<>(possibleProfileKeys.length);
                            ArrayList<HashSet<Double>> scored = new ArrayList<>(possibleProfileKeys.length);
                            boolean profileWithNoSiteDeterminingIonsScored = false;
                            double currentP = getp(currentSpectrum, WINDOW_SIZE, d, nDecimals);
                            for (long profileKey : possibleProfileKeys) {
                                HashSet<Double> tempSiteDeterminingIons = profileToSiteDeterminingIonsMz.get(profileKey);
                                if (tempSiteDeterminingIons == null) {
                                    if (!profileWithNoSiteDeterminingIonsScored) {
                                        profileWithNoSiteDeterminingIonsScored = true;
                                        Peptide tempPeptide = profileToPeptide.get(profileKey);
                                        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons = profileToPossibleFragments.get(profileKey);
                                        int n = profileToN.get(profileKey);
                                        double bigP = getPhosphoRsScoreP(tempPeptide, possibleFragmentIons, currentSpectrum, currentP, n, spectrumAnnotator, annotationSettings, scoringAnnotationParameters, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
                                        BasicMathFunctions.checkProbabilityRange(bigP);
                                        bigPs.add(bigP);
                                    }
                                } else {
                                    boolean alreadyScored = false;
                                    for (HashSet<Double> scoredIons : scored) {
                                        if (Util.sameSets(tempSiteDeterminingIons, scoredIons)) {
                                            alreadyScored = true;
                                            break;
                                        }
                                    }
                                    if (!alreadyScored) {
                                        Peptide tempPeptide = profileToPeptide.get(profileKey);
                                        int n = profileToN.get(profileKey);
                                        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons = profileToPossibleFragments.get(profileKey);
                                        double bigP = getPhosphoRsScoreP(tempPeptide, possibleFragmentIons, currentSpectrum, currentP, n, spectrumAnnotator, annotationSettings, scoringAnnotationParameters, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
                                        BasicMathFunctions.checkProbabilityRange(bigP);
                                        bigPs.add(bigP);
                                        scored.add(tempSiteDeterminingIons);
                                    }
                                }
                            }
                            double[] bigPArray = bigPs.stream().mapToDouble(a -> a).toArray();
                            for (int j = 0; j < bigPArray.length - 1; j++) {
                                double pJ = bigPArray[j];
                                double pJPlusOne = bigPArray[j + 1];
                                double delta = pJ / pJPlusOne;
                                currentDeltas.add(delta);
                            }
                            if (currentDeltas.size() > nDeltas) {
                                nDeltas = currentDeltas.size();
                            }
                            deltas.add(currentDeltas);
                        }

                        int bestI = 0;
                        double largestDelta = 0.0;

                        for (int j = 0; j < nDeltas && largestDelta == 0.0; j++) {
                            for (int i = 0; i < deltas.size(); i++) {
                                ArrayList<Double> tempDeltas = deltas.get(i);
                                if (j < tempDeltas.size() && tempDeltas.get(j) > largestDelta) {
                                    largestDelta = tempDeltas.get(j);
                                    bestI = i;
                                }
                            }
                        }

                        if (bestI < MIN_DEPTH - 1 && MIN_DEPTH - 1 < spectra.size()) {
                            bestI = MIN_DEPTH - 1;
                        }
                        if (bestI > MAX_DEPTH - 1) {
                            bestI = MAX_DEPTH - 1;
                        }

                        reducedSpectrum.putAll(spectra.get(bestI).getPeakMap());

                    } else {

                        double bestP = 0.0;
                        int bestI = 0;

                        HashMap<Integer, ArrayList<Ion>> expectedFragmentIons = spectrumAnnotator.getExpectedIons(scoringAnnotationParameters, peptide, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
                        int nExpectedFragmentIons = 0;
                        
                        for (ArrayList<Ion> expectedIons : expectedFragmentIons.values()) {
                            nExpectedFragmentIons += expectedIons.size();
                        }
                        
                        IonFactory fragmentFactory = IonFactory.getInstance();
                        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons = fragmentFactory.getFragmentIons(peptide, scoringAnnotationParameters, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
                        
                        for (int i = 0; i < spectra.size(); i++) {
                            
                            Spectrum currentSpectrum = spectra.get(i);
                            double currentP = getp(currentSpectrum, WINDOW_SIZE, d, nDecimals);
                            double bigP = getPhosphoRsScoreP(peptide, possibleFragmentIons, currentSpectrum, currentP, nExpectedFragmentIons, spectrumAnnotator, annotationSettings, scoringAnnotationParameters, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
                            BasicMathFunctions.checkProbabilityRange(bigP);
                            
                            if (bigP < bestP) {
                                
                                bestP = bigP;
                                bestI = i;
                            }
                        }

                        reducedSpectrum.putAll(spectra.get(bestI).getPeakMap());
                    }
                }

                minMz = tempMax;
            }

            Spectrum phosphoRsSpectrum = new Spectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle() + "_phosphoRS", reducedSpectrum, spectrum.getFileName());

            double w = spectrum.getMaxMz() - spectrum.getMinMz();
            if (specificAnnotationSettings.isFragmentIonPpm()) {
                double refMz = spectrum.getMinMz() + (w / 2);
                d = specificAnnotationSettings.getFragmentIonAccuracyInDa(refMz);
            }
            dOverW = d / w;
            dOverW = -FastMath.log10(dOverW);
            nDecimals = ((int) dOverW) + 1;
            double currentP = getp(phosphoRsSpectrum, w, d, nDecimals);
            double[] pInvs = new double[possibleProfileKeys.length];
            double pInvTotal = 0.0;

            for (int i = 0 ; i < possibleProfileKeys.length ; i++) {
                long profileKey = possibleProfileKeys[i];
                Peptide tempPeptide = profileToPeptide.get(profileKey);
                Integer n = profileToN.get(profileKey);
                HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons = profileToPossibleFragments.get(profileKey);
                double bigP = getPhosphoRsScoreP(tempPeptide, possibleFragmentIons, phosphoRsSpectrum, currentP, n, spectrumAnnotator, annotationSettings, scoringAnnotationParameters, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
                BasicMathFunctions.checkProbabilityRange(bigP);
                double pInv = 1.0 / bigP;
                pInvs[i] = pInv;
                pInvTotal += pInv;
            }
            if (pInvTotal <= 0) {
                throw new IllegalArgumentException("PhosphoRS probability <= 0.");
            }

            for (int i = 0 ; i < possibleProfileKeys.length ; i++) {
                long profileKey = possibleProfileKeys[i];
                double pInv = pInvs[i];
                double phosphoRsProbability = pInv / pInvTotal;
                BasicMathFunctions.checkProbabilityRange(phosphoRsProbability);
                phosphoRsProbability *= 100.0;
                profileToScoreMap.put(profileKey, phosphoRsProbability);
            }

        } else if (possibleSites.length == nModification) {
            
            long profileKey = getModificationProfileKey(possibleSites);
            profileToScoreMap.put(profileKey, 100.0);
            profileToSitesMap.put(profileKey, possibleSites);
        
        } else {
            throw new IllegalArgumentException("Found less potential modification sites than modifications during PhosphoRS calculation. Peptide key: " + peptide.getKey());
        }

        HashMap<Integer, Double> scores = new HashMap<>();
        for (long profileKey : profileToScoreMap.keySet()) {
            double score = profileToScoreMap.get(profileKey);
            int[] sites = profileToSitesMap.get(profileKey);
            for (int site : sites) {
                Double previousScore = scores.get(site);
                if (previousScore == null) {
                    scores.put(site, score);
                } else {
                    double newScore = score + previousScore;
                    scores.put(site, newScore);
                }
            }
        }

        for (int site : possibleSites) {
            if (!scores.keySet().contains(site)) {
                throw new IllegalArgumentException("Site " + site + " not scored for modification " + modificationMass + " in spectrum " + spectrum.getSpectrumTitle() + " of file " + spectrum.getFileName() + ".");
            }
        }

        return scores;
    }

    /**
     * Returns the PhosphoRS score of the given peptide on the given spectrum.
     * This method returns P and not -10.log(P).
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param p the probability for a calculated fragment matching one of the
     * experimental masses by chance as estimated by PhosphoRS
     * @param n the number of expected ions
     * @param spectrumAnnotator spectrum annotator
     * @param annotationSettings the global annotation settings
     * @param scoringAnnotationSettings the annotation settings specific to this
     * peptide and spectrum
     * @param modificationParameters the modification parameters
     * @param sequenceProvider a provider for the protein sequences
     * @param modificationSequenceMatchingParameters the sequence matching
     * preferences for modification to peptide mapping
     *
     * @return the phosphoRS score
     */
    private static double getPhosphoRsScoreP(Peptide peptide, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possiblePeptideFragments, 
            Spectrum spectrum, double p, int n, PeptideSpectrumAnnotator spectrumAnnotator,
            AnnotationParameters annotationSettings, SpecificAnnotationParameters scoringAnnotationSettings, 
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingParameters) {

        BinomialDistribution distribution = null;
        HashMap<Integer, BinomialDistribution> distributionsAtP = distributionCache.get(p);
        boolean inCache = true;
        if (distributionsAtP != null) {
            distribution = distributionsAtP.get(n);
        }
        if (distribution == null) {
            distribution = new BinomialDistribution(n, p);
            inCache = false;
        }

        Stream<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(annotationSettings, scoringAnnotationSettings, spectrum, peptide, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters, possiblePeptideFragments, false);
        int k = (int) matches.filter(ionMatch -> ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION).count();
        if (k == 0) {
            return 1.0;
        }

        double result = distribution.getDescendingCumulativeProbabilityAt((double) k);
        if (!inCache && !distribution.isCacheEmpty()) {
            addDistributionToCache(p, n, distribution);
        }
        return result;
    }

    /**
     * Adds a distribution to the cache and manages the cache size.
     *
     * @param p the distribution p
     * @param n the distribution n
     */
    private static synchronized void addDistributionToCache(double p, int n, BinomialDistribution binomialDistribution) {
        if (distributionCache.size() >= distributionCacheSize) {
            HashSet<Double> keys = new HashSet<>(distributionCache.keySet());
            for (Double key : keys) {
                distributionCache.remove(key);
                if (distributionCache.size() < distributionCacheSize) {
                    break;
                }
            }
        }
        HashMap<Integer, BinomialDistribution> distributionsAtP = distributionCache.get(p);
        if (distributionsAtP == null) {
            distributionsAtP = new HashMap<>(2);
            distributionCache.put(p, distributionsAtP);
        }
        distributionsAtP.put(n, binomialDistribution);
    }

    /**
     * The probability p for a calculated fragment matching one of the
     * experimental masses by chance as estimated in the PhosphoRS algorithm.
     *
     * @param spectrum the spectrum studied
     * @param w the m/z range considered
     * @param d the m/z tolerance in daltons
     * @param nDecimals the number of decimals to use
     *
     * @return the probability p for a calculated fragment matching one of the
     * experimental masses by chance as estimated in the PhosphoRS algorithm.
     */
    private static double getp(Spectrum spectrum, double w, double d, int nDecimals) {
        if (w == 0.0) {
            return 1.0;
        }
        int N = spectrum.getPeakMap().size();
        if (N <= 1) {
            return 1.0;
        }
        double p = d * N / w;
        if (p > 1) {
            p = 1;
        }
        double roundedP = Util.floorDouble(p, nDecimals);
        return roundedP;
    }

    /**
     * Returns a map of the different possible peptides for the different
     * profiles.
     *
     * @param peptide the peptide of interest
     * @param modifications the modifications to score
     * @param possibleProfiles the different profiles
     *
     * @return a map of the different peptides for the different profiles
     */
    private static HashMap<Long, Peptide> getPossiblePeptidesMap(Peptide peptide, HashSet<String> modNames, long[] profileKeys, ArrayList<int[]> possibleProfiles) {

        String representativeModification = modNames.stream().findAny().get();
        HashMap<Long, Peptide> result = new HashMap<>(possibleProfiles.size());
        int peptideLength = peptide.getSequence().length();

        for (int i = 0 ; i < profileKeys.length ; i++) {

            long profileKey = profileKeys[i];
            int[] profile = possibleProfiles.get(i);
            Peptide tempPeptide = peptide.getNoModPeptide(modNames);

            for (int pos : profile) {

                int index = pos;

                if (index > 0 || index < peptideLength + 1) {
                } else if (index == 0) {

                    index = 1;

                } else {

                    index = peptideLength;

                }

                tempPeptide.addVariableModification(new ModificationMatch(representativeModification, index));

            }

            result.put(profileKey, tempPeptide);

        }

        return result;
    }

    /**
     * Returns a map of the number of possible fragment ions for every peptide
     * indexed by the corresponding profile.
     *
     * @param possiblePeptides map of the possible peptides for every profile
     * @param possiblePeptideFragments the possible peptide fragments for every
     * peptide
     * @param spectrumAnnotator the spectrum annotator
     * @param scoringAnnotationSetttings the spectrum scoring annotation
     * settings
     *
     * @return a map of the number of possible fragment ions for every peptide
     */
    private static HashMap<Long, Integer> getPossiblePeptideToN(HashMap<Long, Peptide> possiblePeptides, HashMap<Long, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>> possiblePeptideFragments,
            PeptideSpectrumAnnotator spectrumAnnotator, SpecificAnnotationParameters scoringAnnotationSetttings, 
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingParameters) {
        
        HashMap<Long, Integer> result = new HashMap<>(possiblePeptides.size());
        
        for (Entry<Long, Peptide> entry : possiblePeptides.entrySet()) {
        
            Peptide peptide = entry.getValue();
            HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> fragmentsForProfile = possiblePeptideFragments.get(entry.getKey());
            HashMap<Integer, ArrayList<Ion>> expectedFragmentIons = spectrumAnnotator.getExpectedIons(scoringAnnotationSetttings, peptide, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters, fragmentsForProfile);
            int n = expectedFragmentIons.values().stream()
                    .mapToInt(ArrayList::size)
                    .sum();
            result.put(entry.getKey(), n);
            
        }
        
        return result;
    }

    /**
     * Returns a map of the possible ions for every peptide of every profile.
     *
     * @param possiblePeptides map of the possible peptides for every profile
     * @param scoringAnnotationSetttings the spectrum scoring annotation
     * settings
     * @param modificationParameters the modification parameters
     * @param sequenceProvider the sequence provider
     * @param modificationSequenceMatchingPreferences the sequence matching preferences to use for modifications
     *
     * @return a map of the possible ions for every peptide of every profile
     */
    private static HashMap<Long, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>> getPossiblePeptideFragments(HashMap<Long, Peptide> possiblePeptides, SpecificAnnotationParameters scoringAnnotationSetttings,
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingPreferences) {
        
        HashMap<Long, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>> result = new HashMap<>(possiblePeptides.size());
        IonFactory fragmentFactory = IonFactory.getInstance();
        
        for (Entry<Long, Peptide> entry : possiblePeptides.entrySet()) {
        
            HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons = fragmentFactory.getFragmentIons(entry.getValue(), scoringAnnotationSetttings, modificationParameters, sequenceProvider, modificationSequenceMatchingPreferences);
            result.put(entry.getKey(), possibleFragmentIons);
        }
        
        return result;
    }

    /**
     * Returns the possible modification profiles given the possible sites and
     * number of modifications. Sites are sorted in increasing order.
     *
     * @param possibleSites the possible modification sites in increasing order
     * @param nPtms the number of modifications
     *
     * @return a list of possible modification profiles
     */
    private static ArrayList<int[]> getPossibleModificationProfiles(int[] possibleSites, int nPtms) {

        ArrayList<int[]> result = new ArrayList<>();

        for (int pos : possibleSites) {
            
            int[] profile = new int[nPtms];
            profile[0] = pos;
            result.add(profile);
        }

        for (int i = 2; i <= nPtms; i++) {

            ArrayList<int[]> resultAtI = new ArrayList<>(result.size());

            for (int[] previousProfile : result) {
                
                int lastPos = previousProfile[previousProfile.length - 1];
                
                for (int pos : possibleSites) {
                
                    if (pos > lastPos) {
                    
                        int[] profile = Arrays.copyOf(previousProfile, previousProfile.length + 1);
                        profile[previousProfile.length] = pos;
                        resultAtI.add(profile);
                    }
                }
            }
            
            result = resultAtI;
        }

        return result;
    }

    /**
     * Returns a map of all potential site determining ions indexed by their
     * m/z.
     *
     * @param noModPeptide the version of the peptide which does not contain the
     * modification of interest
     * @param profileKeys the keys of the different profiles
     * @param possibleProfiles the possible modification profiles to inspect
     * @param modifications the modifications scored
     * @param spectrumAnnotator the spectrum annotator used throughout the
     * scoring
     * @param scoringAnnotationParameters the annotation settings specific to
     * this peptide and spectrum
     *
     * @return a map of all potential site determining ions indexed by their m/z
     */
    private static HashMap<Double, ArrayList<Long>> getSiteDeterminingIons(Peptide noModPeptide, long[] profileKeys, ArrayList<int[]> possibleProfiles, ArrayList<Modification> modifications,
            PeptideSpectrumAnnotator spectrumAnnotator, SpecificAnnotationParameters scoringAnnotationParameters, 
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingParameters) {

        String sequence = noModPeptide.getSequence();
        Peptide peptide = new Peptide(sequence, noModPeptide.getVariableModifications());
        String representativeModification = modifications.get(0).getName();

        HashMap<Double, ArrayList<Long>> siteDeterminingIons = new HashMap<>();
        HashMap<Double, ArrayList<Long>> commonIons = new HashMap<>();

        for (int i = 0 ; i < profileKeys.length ; i++) {
            
            long profileKey = profileKeys[i];
            int[] modificationProfile = possibleProfiles.get(i);

            for (int pos : modificationProfile) {
                peptide.addVariableModification(new ModificationMatch(representativeModification, pos));
            }

            HashSet<Double> mzs = spectrumAnnotator.getExpectedIons(scoringAnnotationParameters, peptide, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters).values().stream()
                    .flatMap(ArrayList::stream)
                    .filter(ion -> ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION)
                    .flatMap(ion -> scoringAnnotationParameters.getSelectedCharges().stream()
                    .map(charge -> ion.getTheoreticMz(charge)))
                    .collect(Collectors.toCollection(HashSet::new));

            for (double mz : mzs) {
                if (commonIons.isEmpty()) {
                    ArrayList<Long> profiles = new ArrayList<>(2);
                    commonIons.put(mz, profiles);
                    profiles.add(profileKey);
                } else if (!commonIons.containsKey(mz)) {
                    ArrayList<Long> profiles = siteDeterminingIons.get(mz);
                    if (profiles == null) {
                        profiles = new ArrayList<>(2);
                        siteDeterminingIons.put(mz, profiles);
                    }
                    profiles.add(profileKey);
                }
            }

            for (double mz : new HashSet<>(commonIons.keySet())) {
                if (!mzs.contains(mz)) {
                    siteDeterminingIons.put(mz, commonIons.get(mz));
                    commonIons.remove(mz);
                } else {
                    commonIons.get(mz).add(profileKey);
                }
            }
        }

        return siteDeterminingIons;
    }

    /**
     * Returns a list of spectra containing only the most intense ions. The
     * index of the spectrum in the list corresponds to the increasing number of
     * peaks, ie the depth, starting with depth 1.
     *
     * @param spectrum the spectrum of interest
     *
     * @return a list of spectra containing only the most intense ions.
     */
    private static ArrayList<Spectrum> getReducedSpectra(Spectrum spectrum) {

        if (spectrum.isEmpty()) {
            throw new IllegalArgumentException("Attempting to extract peaks from an empty spectrum.");
        }

        ArrayList<Spectrum> reducedSpectra = new ArrayList<>(MAX_DEPTH);
        HashMap<Double, ArrayList<Peak>> intensityToPeakMap = new HashMap<>(spectrum.getPeakMap().size());

        for (Peak peak : spectrum.getPeakList()) {
            double intensity = peak.intensity;
            ArrayList<Peak> peaks = intensityToPeakMap.get(intensity);
            if (peaks == null) {
                peaks = new ArrayList<>();
                intensityToPeakMap.put(intensity, peaks);
            }
            peaks.add(peak);
        }

        ArrayList<Double> intensities = new ArrayList<>(intensityToPeakMap.keySet());
        Collections.sort(intensities, Collections.reverseOrder());
        int depth = 0;

        HashMap<Double, Peak> mzToPeak = new HashMap<>(1);

        for (double intensity : intensities) {
            for (Peak peak : intensityToPeakMap.get(intensity)) {
                mzToPeak.put(peak.mz, peak);
                depth++;
                reducedSpectra.add(new Spectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle() + "_" + depth, mzToPeak, spectrum.getFileName()));
                if (depth > MAX_DEPTH) {
                    break;
                }
                HashMap<Double, Peak> newMzToPeak = new HashMap<>(depth + 1);
                newMzToPeak.putAll(mzToPeak);
                mzToPeak = newMzToPeak;
            }
            if (depth > MAX_DEPTH) {
                break;
            }
        }

        return reducedSpectra;
    }

    /**
     * Filters the spectrum so that p is lower or equal to 1 by retaining the
     * most intense peaks in a window of 10 times the ms2 tolerance.
     *
     * @param spectrum the original spectrum
     * @param scoringAnnotationSetttings the annotation settings
     *
     * @return the filtered spectrum
     */
    private static Spectrum filterSpectrum(Spectrum spectrum, SpecificAnnotationParameters scoringAnnotationSetttings) {

        Double window;
        Integer maxPeaks;

        double ms2Tolerance = scoringAnnotationSetttings.getFragmentIonAccuracyInDa(spectrum.getMaxMz());

        if (ms2Tolerance <= 10) {
            window = 10 * ms2Tolerance;
            maxPeaks = 10;
        } else {
            window = WINDOW_SIZE;
            maxPeaks = (int) (window / ms2Tolerance);
        }

        if (maxPeaks < 1) {
            throw new IllegalArgumentException("All peaks removed by filtering.");
        }

        HashMap<Double, Peak> peakMap = spectrum.getPeakMap(),
                newMap = new HashMap<>(peakMap.size()),
                tempMap = new HashMap<>(maxPeaks);
        Double refMz = null;

        for (Double mz : spectrum.getOrderedMzValues()) {
            if (refMz == null) {
                refMz = mz;
            } else if (mz > refMz + window) {
                if (tempMap.size() <= maxPeaks) {
                    newMap.putAll(tempMap);
                    tempMap.clear();
                } else {
                    ArrayList<Double> intensities = new ArrayList<>(tempMap.keySet());
                    Collections.sort(intensities, Collections.reverseOrder());
                    for (int i = 0; i < Math.min(intensities.size(), maxPeaks); i++) {
                        Double intensity = intensities.get(i);
                        Peak peak = tempMap.get(intensity);
                        newMap.put(peak.mz, peak);
                    }
                    tempMap.clear();
                }
                refMz += window;
            }
            Peak peak = peakMap.get(mz);
            tempMap.put(peak.intensity, peak);
        }

        ArrayList<Double> intensities = new ArrayList<>(tempMap.keySet());
        Collections.sort(intensities, Collections.reverseOrder());

        for (int i = 0; i < Math.min(intensities.size(), maxPeaks); i++) {
            Double intensity = intensities.get(i);
            Peak peak = tempMap.get(intensity);
            newMap.put(peak.mz, peak);
        }

        return new Spectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle() + "_filtered", newMap, spectrum.getFileName());
    }

    /**
     * Returns a key as concatenated string for the given sites.
     *
     * @param sites the sites
     *
     * @return the corresponding key
     */
    public static long getModificationProfileKey(int[] sites) {
        return ExperimentObject.asLong(Arrays.stream(sites)
                .sorted()
                .mapToObj(site -> Integer.toString(site))
                .collect(Collectors.joining("_")));
    }
}
