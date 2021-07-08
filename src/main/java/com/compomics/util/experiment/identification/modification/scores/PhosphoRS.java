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
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.math.statistics.distributions.BinomialDistribution;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import com.compomics.util.experiment.identification.utils.ModificationUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.SpectrumUtil;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math.util.FastMath;

/**
 * This class estimates the PhosphoRS score as described in
 * https://www.ncbi.nlm.nih.gov/pubmed/22073976. Warning: the calculation in its
 * present form is very slow for multiply modified peptides, peptides with many
 * modification sites, and noisy spectra. Typically, avoid scoring deamidation
 * sites.
 *
 * @author Marc Vaudel
 */
public class PhosphoRS {

    /**
     * Empty default constructor
     */
    public PhosphoRS() {
    }

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
    private static final int DISTRIBUTION_CACHE_SIZE = 1000;
    /**
     * The binomial distributions cache.
     */
    private static final HashMap<Double, HashMap<Integer, BinomialDistribution>> DISTRIBUTION_CACHE = new HashMap<Double, HashMap<Integer, BinomialDistribution>>();

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
     * @param annotationParameters the global annotation parameters
     * @param specificAnnotationSettings the annotation settings specific to
     * this peptide and spectrum
     * @param accountNeutralLosses a boolean indicating whether or not the
     * calculation shall account for neutral losses.
     * @param sequenceMatchingParameters the sequence matching preferences for
     * peptide to protein mapping
     * @param modificationSequenceMatchingParameters the sequence matching
     * preferences for modification to peptide mapping
     * @param spectrumAnnotator the peptide spectrum annotator to use for
     * spectrum annotation, can be null
     *
     * @return a map site &gt; phosphoRS site probability
     */
    public static HashMap<Integer, Double> getSequenceProbabilities(
            Peptide peptide,
            ArrayList<Modification> modifications,
            ModificationParameters modificationParameters,
            Spectrum spectrum,
            SequenceProvider sequenceProvider,
            AnnotationParameters annotationParameters,
            SpecificAnnotationParameters specificAnnotationSettings,
            boolean accountNeutralLosses,
            SequenceMatchingParameters sequenceMatchingParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            PeptideSpectrumAnnotator spectrumAnnotator
    ) {

        if (modifications.isEmpty()) {
            throw new IllegalArgumentException("No modification given for PhosphoRS calculation.");
        }

        if (spectrumAnnotator == null) {
            spectrumAnnotator = new PeptideSpectrumAnnotator();
        }

        ModificationMatch[] modificationMatches = peptide.getVariableModifications();

        int nModification = (int) Arrays.stream(modificationMatches)
                .filter(
                        modificationMatch -> modifications.stream()
                                .anyMatch(
                                        modification -> modification.getName().equals(modificationMatch.getModification())
                                )
                )
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
                .flatMapToInt(
                        modification -> Arrays.stream(
                                ModificationUtils.getPossibleModificationSites(
                                        peptide,
                                        modification,
                                        sequenceProvider,
                                        modificationSequenceMatchingParameters
                                )
                        )
                )
                .distinct()
                .sorted()
                .toArray();

        HashMap<Integer, Double> profileToScoreMap = new HashMap<>(possibleSites.length);
        HashMap<Integer, int[]> profileToSitesMap = new HashMap<>(possibleSites.length);

        if (possibleSites.length > nModification) {

            Spectrum filteredSpectrum = filterSpectrum(
                    spectrum,
                    scoringAnnotationParameters
            );

            HashSet<String> modNames = modifications.stream()
                    .map(
                            modification -> modification.getName()
                    )
                    .collect(
                            Collectors.toCollection(HashSet::new)
                    );

            IonFactory fragmentFactory = IonFactory.getInstance();
            HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons = fragmentFactory.getFragmentIons(
                    peptide,
                    scoringAnnotationParameters,
                    modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters
            );

            IonMatch[] ionMatches = spectrumAnnotator.getSpectrumAnnotation(
                    annotationParameters,
                    scoringAnnotationParameters,
                    "PhosphoRS",
                    "Peptide",
                    spectrum,
                    peptide,
                    modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters,
                    possibleFragmentIons,
                    false
            );

            ArrayList<int[]> possibleProfiles = getPossibleModificationProfiles(possibleSites, nModification);
            int[] possibleProfileKeys = new int[possibleProfiles.size()];

            for (int i = 0; i < possibleProfiles.size(); i++) {

                int[] profile = possibleProfiles.get(i);
                int profileKey = Arrays.hashCode(profile);
                possibleProfileKeys[i] = profileKey;
                profileToSitesMap.put(profileKey, profile);

            }

            HashMap<Integer, Peptide> profileToPeptide = getPossiblePeptidesMap(
                    peptide,
                    modNames,
                    possibleProfileKeys,
                    possibleProfiles
            );

            HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>> profileToPossibleFragments = getPossiblePeptideFragments(
                    profileToPeptide,
                    scoringAnnotationParameters,
                    modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters
            );
            HashMap<Integer, IonMatch[]> profileMatchedPeptideFragments = getMatchedFragments(
                    spectrumAnnotator,
                    profileToPeptide,
                    filteredSpectrum,
                    profileToPossibleFragments,
                    annotationParameters,
                    scoringAnnotationParameters,
                    modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters
            );

            HashMap<Integer, Integer> profileToN = getPossiblePeptideToN(
                    profileToPeptide,
                    profileToPossibleFragments,
                    spectrumAnnotator,
                    scoringAnnotationParameters,
                    modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters
            );

            HashMap<Double, ArrayList<Integer>> siteDeterminingIonsMap = getSiteDeterminingIons(
                    profileToPeptide,
                    spectrumAnnotator,
                    scoringAnnotationParameters,
                    modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters
            );
            ArrayList<Double> siteDeterminingIons = new ArrayList<>(siteDeterminingIonsMap.keySet());

            double minMz = filteredSpectrum.getMinMz(), maxMz = filteredSpectrum.getMaxMz(), tempMax;

            ArrayList<int[]> reducedSpectrumIndexes = new ArrayList<>();
            int reducedSpectrumLength = 0;

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

                int[] windowIndexes = SpectrumUtil.getWindowIndexes(
                        filteredSpectrum,
                        minMz,
                        tempMax
                );
                int windowStartIndex = windowIndexes[0];
                int windowEndIndex = windowIndexes[1];

                if (windowEndIndex - windowStartIndex > 0) {

                    ArrayList<Double> intensityThresholds = getIntensityThresholds(
                            filteredSpectrum,
                            windowStartIndex,
                            windowEndIndex
                    );

                    HashMap<Integer, HashSet<Double>> profileToSiteDeterminingIonsMz = new HashMap<>(siteDeterminingIons.size());

                    for (Entry<Double, ArrayList<Integer>> entry : siteDeterminingIonsMap.entrySet()) {

                        double ionMz = entry.getKey();

                        if (ionMz > minMz && ionMz <= tempMax) {

                            ArrayList<Integer> profiles = entry.getValue();

                            for (int profileKey : profiles) {

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

                        ArrayList<ArrayList<Double>> deltas = new ArrayList<>(intensityThresholds.size());
                        int nDeltas = 0;

                        for (int depth = 1; depth <= intensityThresholds.size(); depth++) {

                            double intensityThreshold = intensityThresholds.get(depth - 1);
                            int nPeaks = SpectrumUtil.getNPeaksAboveThreshold(
                                    filteredSpectrum,
                                    windowStartIndex,
                                    windowEndIndex,
                                    intensityThreshold
                            );

                            TreeSet<Double> bigPs = new TreeSet<>();
                            ArrayList<Double> currentDeltas = new ArrayList<>(possibleProfileKeys.length);
                            ArrayList<HashSet<Double>> scored = new ArrayList<>(possibleProfileKeys.length);
                            boolean profileWithNoSiteDeterminingIonsScored = false;
                            double currentP = getp(
                                    nPeaks,
                                    WINDOW_SIZE,
                                    d,
                                    nDecimals
                            );

                            for (int profileKey : possibleProfileKeys) {

                                HashSet<Double> tempSiteDeterminingIons = profileToSiteDeterminingIonsMz.get(profileKey);

                                if (tempSiteDeterminingIons == null) {

                                    if (!profileWithNoSiteDeterminingIonsScored) {

                                        profileWithNoSiteDeterminingIonsScored = true;
                                        IonMatch[] profileFragments = profileMatchedPeptideFragments.get(profileKey);
                                        int k = 0;

                                        for (IonMatch ionMatch : profileFragments) {

                                            if (ionMatch.peakMz >= minMz && ionMatch.peakMz < maxMz && ionMatch.peakIntensity >= intensityThreshold) {

                                                k++;

                                            }
                                        }

                                        double bigP = getPhosphoRsScoreP(
                                                k,
                                                currentP,
                                                nPeaks
                                        );
                                        BasicMathFunctions.checkProbabilityRange(bigP);
                                        bigPs.add(bigP);

                                    }
                                } else {

                                    boolean alreadyScored = false;

                                    for (HashSet<Double> scoredIons : scored) {

                                        if (tempSiteDeterminingIons.equals(scoredIons)) {

                                            alreadyScored = true;
                                            break;

                                        }
                                    }

                                    if (!alreadyScored) {

                                        IonMatch[] profileFragments = profileMatchedPeptideFragments.get(profileKey);
                                        int k = 0;

                                        for (IonMatch ionMatch : profileFragments) {

                                            if (ionMatch.peakMz >= minMz && ionMatch.peakMz < maxMz && ionMatch.peakIntensity >= intensityThreshold) {

                                                k++;

                                            }
                                        }

                                        double bigP = getPhosphoRsScoreP(
                                                k,
                                                currentP,
                                                nPeaks
                                        );

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

                        if (bestI < MIN_DEPTH - 1 && MIN_DEPTH - 1 < intensityThresholds.size()) {

                            bestI = MIN_DEPTH - 1;

                        }

                        if (bestI > MAX_DEPTH - 1) {

                            bestI = MAX_DEPTH - 1;

                        }

                        double bestIntensityThreshold = intensityThresholds.get(bestI);
                        int[] windowBestPeaks = IntStream.range(windowStartIndex, windowEndIndex)
                                .filter(
                                        i -> filteredSpectrum.intensity[i] >= bestIntensityThreshold
                                )
                                .toArray();

                        reducedSpectrumIndexes.add(windowBestPeaks);
                        reducedSpectrumLength += windowBestPeaks.length;

                    } else {

                        double bestP = 0.0;
                        int bestI = 0;

                        for (int depth = 1; depth <= intensityThresholds.size(); depth++) {

                            double intensityThreshold = intensityThresholds.get(depth - 1);
                            int nPeaks = SpectrumUtil.getNPeaksAboveThreshold(
                                    filteredSpectrum,
                                    windowStartIndex,
                                    windowEndIndex,
                                    intensityThreshold
                            );

                            double currentP = getp(
                                    nPeaks,
                                    WINDOW_SIZE,
                                    d,
                                    nDecimals
                            );

                            int k = 0;

                            for (IonMatch ionMatch : ionMatches) {

                                if (ionMatch.peakMz >= minMz && ionMatch.peakMz < maxMz && ionMatch.peakIntensity >= intensityThreshold) {

                                    k++;

                                }
                            }

                            double bigP = getPhosphoRsScoreP(
                                    k,
                                    currentP,
                                    nPeaks
                            );
                            BasicMathFunctions.checkProbabilityRange(bigP);

                            if (bigP < bestP) {

                                bestP = bigP;
                                bestI = depth - 1;
                            }
                        }

                        double bestIntensityThreshold = intensityThresholds.get(bestI);
                        int[] windowBestPeaks = IntStream.range(windowStartIndex, windowEndIndex)
                                .filter(
                                        i -> filteredSpectrum.intensity[i] >= bestIntensityThreshold
                                )
                                .toArray();

                        reducedSpectrumIndexes.add(windowBestPeaks);
                        reducedSpectrumLength += windowBestPeaks.length;

                    }
                }

                minMz = tempMax;

            }

            double[] reducedSpectrumMz = new double[reducedSpectrumLength];
            double[] reducedSpectrumIntensity = new double[reducedSpectrumLength];
            int count = 0;

            for (int[] indexes : reducedSpectrumIndexes) {

                for (int index : indexes) {

                    reducedSpectrumMz[count] = filteredSpectrum.mz[index];
                    reducedSpectrumIntensity[count] = filteredSpectrum.intensity[index];
                    count++;

                }
            }

            Spectrum phosphoRsSpectrum = new Spectrum(
                    spectrum.getPrecursor(),
                    reducedSpectrumMz,
                    reducedSpectrumIntensity
            );

            double w = filteredSpectrum.getMaxMz() - filteredSpectrum.getMinMz();

            if (specificAnnotationSettings.isFragmentIonPpm()) {

                double refMz = filteredSpectrum.getMinMz() + (w / 2);
                d = specificAnnotationSettings.getFragmentIonAccuracyInDa(refMz);

            }

            dOverW = d / w;
            dOverW = -FastMath.log10(dOverW);
            nDecimals = ((int) dOverW) + 1;
            double currentP = getp(
                    phosphoRsSpectrum.getNPeaks(),
                    w,
                    d,
                    nDecimals
            );
            double[] pInvs = new double[possibleProfileKeys.length];
            double pInvTotal = 0.0;

            for (int i = 0; i < possibleProfileKeys.length; i++) {

                int profileKey = possibleProfileKeys[i];
                Peptide tempPeptide = profileToPeptide.get(profileKey);
                Integer n = profileToN.get(profileKey);

                HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> profilePossibleFragmentIons = profileToPossibleFragments.get(profileKey);
                IonMatch[] profilePhosphoRsMatches = spectrumAnnotator.getSpectrumAnnotation(
                        annotationParameters,
                        scoringAnnotationParameters,
                        "phosphoRsSpectrum",
                        "profile",
                        phosphoRsSpectrum,
                        tempPeptide,
                        modificationParameters,
                        sequenceProvider,
                        modificationSequenceMatchingParameters,
                        possibleFragmentIons, // @TODO: should be profilePossibleFragmentIons, but this results in errors downstream
                        false
                );

                double bigP = getPhosphoRsScoreP(
                        profilePhosphoRsMatches.length,
                        currentP,
                        n
                );
                BasicMathFunctions.checkProbabilityRange(bigP);
                double pInv = 1.0 / bigP;
                pInvs[i] = pInv;
                pInvTotal += pInv;

            }

            if (pInvTotal <= 0) {
                throw new IllegalArgumentException("PhosphoRS probability <= 0.");
            }

            for (int i = 0; i < possibleProfileKeys.length; i++) {

                int profileKey = possibleProfileKeys[i];
                double pInv = pInvs[i];
                double phosphoRsProbability = pInv / pInvTotal;
                BasicMathFunctions.checkProbabilityRange(phosphoRsProbability);
                phosphoRsProbability *= 100.0;
                profileToScoreMap.put(profileKey, phosphoRsProbability);
                
                }

        } else if (possibleSites.length == nModification) {

            int profileKey = Arrays.hashCode(possibleSites);
            profileToScoreMap.put(profileKey, 100.0);
            profileToSitesMap.put(profileKey, possibleSites);

        } else {
            throw new IllegalArgumentException("Found less potential modification sites than modifications during PhosphoRS calculation. Peptide key: " + peptide.getKey());
        }

        HashMap<Integer, Double> scores = new HashMap<>(possibleSites.length);

        for (int profileKey : profileToScoreMap.keySet()) {

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

                throw new IllegalArgumentException("Site " + site + " not scored for modification " + modificationMass + " in peptide " + peptide.toString() + ".");

            }
        }

        return scores;
    }

    /**
     * Returns the PhosphoRS score of the given peptide on the given spectrum.
     * This method returns P and not -10.log(P).
     *
     * @param k The number of fragment ions matched.
     * @param p The probability for a calculated fragment matching one of the
     * experimental masses by chance as estimated by PhosphoRS.
     * @param n The number of expected ions.
     *
     * @return the phosphoRS score
     */
    private static double getPhosphoRsScoreP(
            int k,
            double p,
            int n
    ) {

        if (k == 0) {

            return 1.0;

        }

        BinomialDistribution distribution = null;
        HashMap<Integer, BinomialDistribution> distributionsAtP = DISTRIBUTION_CACHE.get(p);
        boolean inCache = true;

        if (distributionsAtP != null) {

            distribution = distributionsAtP.get(n);

        }

        if (distribution == null) {

            distribution = new BinomialDistribution(n, p);
            inCache = false;

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
    private static synchronized void addDistributionToCache(
            double p,
            int n,
            BinomialDistribution binomialDistribution
    ) {

        if (DISTRIBUTION_CACHE.size() >= DISTRIBUTION_CACHE_SIZE) {

            HashSet<Double> keys = new HashSet<>(DISTRIBUTION_CACHE.keySet());

            for (Double key : keys) {

                DISTRIBUTION_CACHE.remove(key);

                if (DISTRIBUTION_CACHE.size() < DISTRIBUTION_CACHE_SIZE) {

                    break;

                }
            }
        }

        HashMap<Integer, BinomialDistribution> distributionsAtP = DISTRIBUTION_CACHE.get(p);

        if (distributionsAtP == null) {

            distributionsAtP = new HashMap<>(2);
            DISTRIBUTION_CACHE.put(p, distributionsAtP);

        }

        distributionsAtP.put(n, binomialDistribution);

    }

    /**
     * The probability p for a calculated fragment matching one of the
     * experimental masses by chance as estimated in the PhosphoRS algorithm.
     *
     * @param n the number of peaks
     * @param w the m/z range considered
     * @param d the m/z tolerance in daltons
     * @param nDecimals the number of decimals to use
     *
     * @return the probability p for a calculated fragment matching one of the
     * experimental masses by chance as estimated in the PhosphoRS algorithm.
     */
    private static double getp(
            int n,
            double w,
            double d,
            int nDecimals
    ) {

        if (w == 0.0) {

            return 1.0;

        }

        if (n <= 1) {

            return 1.0;

        }

        double p = d * n / w;

        if (p > 1.0) {

            p = 1.0;

        }

        return Util.floorDouble(p, nDecimals);

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
    private static HashMap<Integer, Peptide> getPossiblePeptidesMap(
            Peptide peptide,
            HashSet<String> modNames,
            int[] profileKeys,
            ArrayList<int[]> possibleProfiles
    ) {

        String representativeModification = modNames.stream().findAny().get();
        HashMap<Integer, Peptide> result = new HashMap<>(possibleProfiles.size());

        for (int i = 0; i < profileKeys.length; i++) {

            int profileKey = profileKeys[i];
            int[] profile = possibleProfiles.get(i);
            Peptide tempPeptide = peptide.getNoModPeptide(modNames);

            for (int pos : profile) {

                tempPeptide.addVariableModification(new ModificationMatch(representativeModification, pos));

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
     * @param scoringAnnotationParameters the spectrum scoring annotation
     * settings
     * @param modificationParameters the modification parameters
     * @param sequenceProvider a provider for the protein sequences
     * @param modificationSequenceMatchingParameters the sequence matching
     * preferences for modification to peptide mapping
     *
     * @return a map of the number of possible fragment ions for every peptide
     */
    private static HashMap<Integer, Integer> getPossiblePeptideToN(
            HashMap<Integer, Peptide> possiblePeptides,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>> possiblePeptideFragments,
            PeptideSpectrumAnnotator spectrumAnnotator,
            SpecificAnnotationParameters scoringAnnotationParameters,
            ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationSequenceMatchingParameters
    ) {

        HashMap<Integer, Integer> result = new HashMap<>(possiblePeptides.size());

        for (Entry<Integer, Peptide> entry : possiblePeptides.entrySet()) {

            Peptide peptide = entry.getValue();
            HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> fragmentsForProfile = possiblePeptideFragments.get(entry.getKey());
            HashMap<Integer, ArrayList<Ion>> expectedFragmentIons = spectrumAnnotator.getExpectedIons(
                    scoringAnnotationParameters,
                    peptide,
                    modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters,
                    fragmentsForProfile
            );
            int n = expectedFragmentIons.values().stream()
                    .mapToInt(
                            ArrayList::size
                    )
                    .sum();
            result.put(entry.getKey(), n);

        }

        return result;
    }

    /**
     * Returns a map of the ions matched for every peptide of every profile.
     *
     * @param spectrumAnnotator The spectrum annotator.
     * @param possiblePeptides The peptide for each profile.
     * @param spectrum The spectrum to annotate.
     * @param possiblePeptideFragments The possible peptide fragments for each
     * profile.
     * @param annotationParameters The annotation parameters.
     * @param scoringAnnotationParameters The scoring specific annotation
     * parameters.
     * @param modificationParameters The modifications parameters.
     * @param sequenceProvider The sequence provider.
     * @param modificationSequenceMatchingParameters The modifications sequence
     * matching parameters.
     *
     * @return A map of the ions matched for every peptide of every profile.
     */
    private static HashMap<Integer, IonMatch[]> getMatchedFragments(
            PeptideSpectrumAnnotator spectrumAnnotator,
            HashMap<Integer, Peptide> possiblePeptides,
            Spectrum spectrum,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>> possiblePeptideFragments,
            AnnotationParameters annotationParameters,
            SpecificAnnotationParameters scoringAnnotationParameters,
            ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationSequenceMatchingParameters
    ) {

        HashMap<Integer, IonMatch[]> result = new HashMap<>(possiblePeptides.size());

        for (Entry<Integer, Peptide> peptideEntry : possiblePeptides.entrySet()) {

            int key = peptideEntry.getKey();
            Peptide peptide = peptideEntry.getValue();

            IonMatch[] ionMatches = spectrumAnnotator.getSpectrumAnnotation(
                    annotationParameters,
                    scoringAnnotationParameters,
                    "PhosphoRS",
                    Integer.toString(key),
                    spectrum,
                    peptide,
                    modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters,
                    possiblePeptideFragments.get(key),
                    false
            );

            result.put(key, ionMatches);

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
     * @param modificationSequenceMatchingParameters the sequence matching
     * preferences to use for modifications
     *
     * @return a map of the possible ions for every peptide of every profile
     */
    private static HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>> getPossiblePeptideFragments(
            HashMap<Integer, Peptide> possiblePeptides,
            SpecificAnnotationParameters scoringAnnotationSetttings,
            ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationSequenceMatchingParameters
    ) {

        HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>> result = new HashMap<>(possiblePeptides.size());
        IonFactory fragmentFactory = IonFactory.getInstance();

        for (Entry<Integer, Peptide> entry : possiblePeptides.entrySet()) {

            HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons = fragmentFactory.getFragmentIons(
                    entry.getValue(),
                    scoringAnnotationSetttings,
                    modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters
            );

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
    private static ArrayList<int[]> getPossibleModificationProfiles(
            int[] possibleSites,
            int nPtms
    ) {

        ArrayList<int[]> result = new ArrayList<>();

        for (int pos : possibleSites) {

            int[] profile = new int[1];
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
     * @param profileToPeptide the profile to peptide map
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
    private static HashMap<Double, ArrayList<Integer>> getSiteDeterminingIons(
            HashMap<Integer, Peptide> profileToPeptide,
            PeptideSpectrumAnnotator spectrumAnnotator,
            SpecificAnnotationParameters scoringAnnotationParameters,
            ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationSequenceMatchingParameters
    ) {

        HashMap<Double, ArrayList<Integer>> siteDeterminingIons = new HashMap<>();
        HashMap<Double, ArrayList<Integer>> commonIons = new HashMap<>();

        for (Entry<Integer, Peptide> entry : profileToPeptide.entrySet()) {

            int profileKey = entry.getKey();
            Peptide peptide = entry.getValue();

            HashSet<Double> mzs = spectrumAnnotator.getExpectedIons(
                    scoringAnnotationParameters,
                    peptide, modificationParameters,
                    sequenceProvider,
                    modificationSequenceMatchingParameters
            ).values().stream()
                    .flatMap(ArrayList::stream)
                    .filter(
                            ion -> ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION
                    )
                    .flatMap(
                            ion -> scoringAnnotationParameters.getSelectedCharges().stream()
                                    .map(
                                            charge -> ion.getTheoreticMz(charge)
                                    )
                    )
                    .collect(
                            Collectors.toCollection(HashSet::new)
                    );

            for (double mz : mzs) {

                if (commonIons.isEmpty()) {

                    ArrayList<Integer> profiles = new ArrayList<>(2);
                    commonIons.put(mz, profiles);
                    profiles.add(profileKey);

                } else if (!commonIons.containsKey(mz)) {

                    ArrayList<Integer> profiles = siteDeterminingIons.get(mz);

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
     * Returns the intensity threshold (inclusive) to use at each depth.
     *
     * @param spectrum the spectrum of interest
     * @param iMin the start (inclusive) index to look at
     * @param iMax the end (exclusive) index to look at
     *
     * @return a list of spectra containing only the most intense ions.
     */
    private static ArrayList<Double> getIntensityThresholds(
            Spectrum spectrum,
            int iMin,
            int iMax
    ) {

        TreeSet<Double> intensities = new TreeSet<>();

        for (int i = iMin; i < iMax; i++) {

            double intensity = spectrum.intensity[i];
            intensities.add(intensity);

        }

        ArrayList<Double> thresholds = new ArrayList<>(MAX_DEPTH);

        int depth = 1;

        for (double intensity : intensities.descendingSet()) {

            thresholds.add(intensity);

            if (depth == MAX_DEPTH) {

                break;

            }

            depth++;

        }

        return thresholds;

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
    private static Spectrum filterSpectrum(
            Spectrum spectrum,
            SpecificAnnotationParameters scoringAnnotationSetttings
    ) {

        double window;
        int maxPeaks;

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

        HashSet<Integer> toRemove = new HashSet<>(4);

        int refIndex = 0;
        double refMz = spectrum.mz[0];

        for (int i = 0; i < spectrum.getNPeaks(); i++) {

            double mz = spectrum.mz[i];

            if (mz > refMz + window) {

                if (i - refIndex > maxPeaks) {

                    TreeMap<Double, Integer> intensityMap = new TreeMap<>();

                    for (int j = refIndex; j < i; j++) {

                        intensityMap.put(
                                spectrum.intensity[j],
                                j
                        );
                    }

                    int count = 0;
                    for (int index : intensityMap.descendingMap().values()) {

                        count++;

                        if (count > maxPeaks) {

                            toRemove.add(index);

                        }
                    }
                }

                refIndex = i;
                refMz += window;

            }
        }

        if (spectrum.getNPeaks() - refIndex > maxPeaks) {

            TreeMap<Double, Integer> intensityMap = new TreeMap<>();

            for (int j = refIndex; j < spectrum.getNPeaks(); j++) {

                intensityMap.put(
                        spectrum.intensity[j],
                        j
                );
            }

            int count = 0;
            for (int index : intensityMap.descendingMap().values()) {

                count++;

                if (count > maxPeaks) {

                    toRemove.add(index);

                }
            }
        }

        if (toRemove.isEmpty()) {

            return spectrum;

        } else {

            double[] filteredMz = new double[spectrum.getNPeaks() - toRemove.size()];
            double[] filteredIntensities = new double[spectrum.getNPeaks() - toRemove.size()];
            int count = 0;

            for (int i = 0; i < spectrum.getNPeaks(); i++) {

                if (!toRemove.contains(i)) {

                    filteredMz[count] = spectrum.mz[i];
                    filteredIntensities[count] = spectrum.intensity[i];
                    count++;

                }
            }

            return new Spectrum(
                    spectrum.precursor,
                    filteredMz,
                    filteredIntensities
            );
        }
    }

}
