package com.compomics.util.experiment.identification.ptm.ptmscores;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.math.BigMathUtils;
import com.compomics.util.math.statistics.distributions.BinomialDistribution;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math.MathException;

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
     * the SpecificAnnotationSettings and will be ignored if
     * accountNeutralLosses is false.
     *
     * @param peptide the peptide of interest
     * @param ptms the PTMs to score, for instance different phosphorylations
     * (the PTMs are considered as indistinguishable, i.e. of same mass)
     * @param spectrum the corresponding spectrum
     * @param annotationSettings the global annotation settings
     * @param specificAnnotationSettings the annotation settings specific to
     * this peptide and spectrum
     * @param accountNeutralLosses a boolean indicating whether or not the
     * calculation shall account for neutral losses.
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for PTM to peptide mapping
     * @param spectrumAnnotator the peptide spectrum annotator to use for
     * spectrum annotation, can be null
     * @param mathContext the math context to use for calculation
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
     * @throws org.apache.commons.math.MathException exception thrown whenever a
     * math error occurred while computing the score.
     */
    public static HashMap<Integer, Double> getSequenceProbabilities(Peptide peptide, ArrayList<PTM> ptms, MSnSpectrum spectrum, AnnotationSettings annotationSettings,
            SpecificAnnotationSettings specificAnnotationSettings, boolean accountNeutralLosses, SequenceMatchingPreferences sequenceMatchingPreferences,
            SequenceMatchingPreferences ptmSequenceMatchingPreferences, PeptideSpectrumAnnotator spectrumAnnotator, MathContext mathContext)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException, MathException {

        if (ptms.isEmpty()) {
            throw new IllegalArgumentException("No PTM given for PhosphoRS calculation.");
        }

        if (spectrumAnnotator == null) {
            spectrumAnnotator = new PeptideSpectrumAnnotator();
        }

        int nPTM = 0;
        if (peptide.isModified()) {
            for (ModificationMatch modMatch : peptide.getModificationMatches()) {
                if (modMatch.isVariable()) {
                    for (PTM ptm : ptms) {
                        if (ptm.getName().equals(modMatch.getTheoreticPtm())) {
                            nPTM++;
                        }
                    }
                }
            }
        }
        if (nPTM == 0) {
            throw new IllegalArgumentException("Given PTMs not found in the peptide for PhosphoRS calculation.");
        }

        PTM refPTM = ptms.get(0);
        double ptmMass = refPTM.getMass();
        BigDecimal resolutionLimit = BigDecimal.ONE.divide(BigDecimal.TEN.pow(mathContext.getPrecision()), mathContext);

        NeutralLossesMap annotationNeutralLosses = specificAnnotationSettings.getNeutralLossesMap(),
                scoringLossesMap = new NeutralLossesMap();
        if (accountNeutralLosses) {
            // here annotation are sequence and modification independant
            for (NeutralLoss neutralLoss : annotationNeutralLosses.getAccountedNeutralLosses()) {
                if (Math.abs(neutralLoss.getMass() - ptmMass) > specificAnnotationSettings.getFragmentIonAccuracy()) {
                    scoringLossesMap.addNeutralLoss(neutralLoss, 1, 1);
                }
            }
        }
        SpecificAnnotationSettings scorinAnnotationSetttings = specificAnnotationSettings.clone();
        scorinAnnotationSetttings.setNeutralLossesMap(scoringLossesMap);
        HashMap<Ion.IonType, HashSet<Integer>> ions = specificAnnotationSettings.getIonTypes(),
                newIons = new HashMap<Ion.IonType, HashSet<Integer>>(1);
        for (Ion.IonType ionType : ions.keySet()) {
            if (ionType == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                newIons.put(ionType, ions.get(ionType));
            }
        }
        scorinAnnotationSetttings.setSelectedIonsMap(newIons);

        HashMap<ArrayList<Integer>, Double> profileToScoreMap = new HashMap<ArrayList<Integer>, Double>();
        ArrayList<Integer> possibleSites = new ArrayList<Integer>();

        int peptideLength = peptide.getSequence().length();

        for (PTM ptm : ptms) {
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

            spectrum = filterSpectrum(spectrum, scorinAnnotationSetttings.getFragmentIonAccuracy());

            Collections.sort(possibleSites);
            ArrayList<ArrayList<Integer>> possibleProfiles = getPossibleModificationProfiles(possibleSites, nPTM);

            Peptide noModPeptide = Peptide.getNoModPeptide(peptide, ptms);

            HashMap<Double, ArrayList<ArrayList<Integer>>> siteDeterminingIonsMap = getSiteDeterminingIons(noModPeptide, possibleProfiles, refPTM.getName(), spectrumAnnotator, scorinAnnotationSetttings);
            ArrayList<Double> siteDeterminingIons = new ArrayList<Double>(siteDeterminingIonsMap.keySet());
            Collections.sort(siteDeterminingIons);

            double minMz = spectrum.getMinMz(), maxMz = spectrum.getMaxMz(), tempMax;
            HashMap<Double, Peak> reducedSpectrum = new HashMap<Double, Peak>();

            while (minMz < maxMz) {

                tempMax = minMz + 100;
                HashMap<Double, Peak> extractedPeakList = spectrum.getSubSpectrum(minMz, tempMax);

                if (!extractedPeakList.isEmpty()) {

                    MSnSpectrum tempSpectrum = new MSnSpectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle()
                            + "_PhosphoRS_minMZ_" + minMz, extractedPeakList, spectrum.getFileName());

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

                        ArrayList<ArrayList<BigDecimal>> deltas = new ArrayList<ArrayList<BigDecimal>>(spectra.size());
                        int nDeltas = 0;

                        for (MSnSpectrum currentSpectrum : spectra) {
                            ArrayList<BigDecimal> bigPs = new ArrayList<BigDecimal>(possibleProfiles.size());
                            ArrayList<BigDecimal> currentDeltas = new ArrayList<BigDecimal>(possibleProfiles.size());
                            ArrayList<HashSet<Double>> scored = new ArrayList<HashSet<Double>>(possibleProfiles.size());
                            boolean noIons = false;
                            double currentP = getp(currentSpectrum, scorinAnnotationSetttings.getFragmentIonAccuracy());
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
                                        BigDecimal bigP = getPhosphoRsScoreP(tempPeptide, currentSpectrum, currentP, spectrumAnnotator, annotationSettings, scorinAnnotationSetttings, mathContext);
                                        if (bigP.compareTo(BigDecimal.ZERO) == -1) {
                                            throw new IllegalArgumentException("PhosphoRS probability < 0.");
                                        } else if (bigP.compareTo(BigDecimal.ONE) == 1) {
                                            throw new IllegalArgumentException("PhosphoRS probability > 1.");
                                        }
                                        bigPs.add(bigP);
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
                                        BigDecimal bigP = getPhosphoRsScoreP(tempPeptide, currentSpectrum, currentP, spectrumAnnotator, annotationSettings, scorinAnnotationSetttings, mathContext);
                                        // Check calculation error
                                        if (bigP.compareTo(BigDecimal.ZERO.subtract(resolutionLimit)) == -1) {
                                            throw new IllegalArgumentException("PhosphoRS probability < 0.");
                                        } else if (bigP.compareTo(BigDecimal.ONE.add(resolutionLimit)) == 1) {
                                            throw new IllegalArgumentException("PhosphoRS probability > 1.");
                                        }
                                        // compensate rounding effects
                                        if (bigP.compareTo(BigDecimal.ZERO) == -1) {
                                            bigP = BigDecimal.ZERO;
                                        } else if (bigP.compareTo(BigDecimal.ONE.add(resolutionLimit)) == 1) {
                                            bigP = BigDecimal.ONE;
                                        }
                                        bigPs.add(bigP);
                                        scored.add(tempSiteDeterminingIons);
                                    }
                                }
                            }
                            Collections.sort(bigPs, Collections.reverseOrder());
                            for (int j = 0; j < bigPs.size() - 1; j++) {
                                BigDecimal pJ = bigPs.get(j);
                                BigDecimal pJPlusOne = bigPs.get(j + 1);
                                BigDecimal delta = pJPlusOne.subtract(pJ);
                                currentDeltas.add(delta);
                            }
                            if (currentDeltas.size() > nDeltas) {
                                nDeltas = currentDeltas.size();
                            }
                            deltas.add(currentDeltas);
                        }

                        int bestI = 0;
                        BigDecimal largestDelta = BigDecimal.ZERO;

                        for (int j = 0; j < nDeltas && largestDelta.compareTo(BigDecimal.ZERO) == 0; j++) {
                            for (int i = 0; i < deltas.size(); i++) {
                                ArrayList<BigDecimal> tempDeltas = deltas.get(i);
                                if (j < tempDeltas.size() && tempDeltas.get(j).compareTo(largestDelta) == 1) {
                                    largestDelta = tempDeltas.get(j);
                                    bestI = i;
                                }
                            }
                        }

                        if (largestDelta.compareTo(BigDecimal.ZERO) == 0) {
                            bestI = Math.min(maxDepth, spectra.size() - 1);
                        }

                        reducedSpectrum.putAll(spectra.get(bestI).getPeakMap());
                    } else {

                        BigDecimal bestP = BigDecimal.ZERO;
                        int bestI = 0;

                        for (int i = 0; i < spectra.size(); i++) {
                            MSnSpectrum currentSpectrum = spectra.get(i);
                            double currentP = getp(currentSpectrum, scorinAnnotationSetttings.getFragmentIonAccuracy());
                            BigDecimal bigP = getPhosphoRsScoreP(peptide, currentSpectrum, currentP, spectrumAnnotator, annotationSettings, scorinAnnotationSetttings, mathContext);
                            // Check calculation error
                            if (bigP.compareTo(BigDecimal.ZERO.subtract(resolutionLimit)) == -1) {
                                throw new IllegalArgumentException("PhosphoRS probability < 0.");
                            } else if (bigP.compareTo(BigDecimal.ONE.add(resolutionLimit)) == 1) {
                                throw new IllegalArgumentException("PhosphoRS probability > 1.");
                            }
                            // compensate rounding effects
                            if (bigP.compareTo(BigDecimal.ZERO) == -1) {
                                bigP = BigDecimal.ZERO;
                            } else if (bigP.compareTo(BigDecimal.ONE.add(resolutionLimit)) == 1) {
                                bigP = BigDecimal.ONE;
                            }
                            if (bigP.compareTo(bestP) == -1) {
                                bestP = bigP;
                                bestI = i;
                            }
                        }

                        reducedSpectrum.putAll(spectra.get(bestI).getPeakMap());
                    }
                }

                minMz = tempMax;
            }

            MSnSpectrum phosphoRsSpectrum = new MSnSpectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle() + "_phosphoRS", reducedSpectrum, spectrum.getFileName());
            HashMap<ArrayList<Integer>, BigDecimal> pInvMap = new HashMap<ArrayList<Integer>, BigDecimal>(possibleProfiles.size());
            BigDecimal pInvTotal = BigDecimal.ZERO;

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

                double currentP = getp(phosphoRsSpectrum, scorinAnnotationSetttings.getFragmentIonAccuracy());
                BigDecimal bigP = getPhosphoRsScoreP(tempPeptide, phosphoRsSpectrum, currentP, spectrumAnnotator, annotationSettings, scorinAnnotationSetttings, mathContext);
                // Check calculation error
                if (bigP.compareTo(BigDecimal.ZERO.subtract(resolutionLimit)) == -1) {
                    throw new IllegalArgumentException("PhosphoRS probability < 0.");
                } else if (bigP.compareTo(BigDecimal.ONE.add(resolutionLimit)) == 1) {
                    throw new IllegalArgumentException("PhosphoRS probability > 1.");
                }
                // compensate rounding effects
                if (bigP.compareTo(resolutionLimit) == -1) {
                    bigP = resolutionLimit;
                } else if (bigP.compareTo(BigDecimal.ONE.add(resolutionLimit)) == 1) {
                    bigP = BigDecimal.ONE;
                }
                BigDecimal pInv = BigDecimal.ONE.divide(bigP, mathContext);
                pInvMap.put(profile, pInv);
                pInvTotal = pInvTotal.add(pInv, mathContext);
            }
            if (pInvTotal.compareTo(BigDecimal.ZERO.subtract(resolutionLimit)) == -1) {
                throw new IllegalArgumentException("PhosphoRS probability < 0.");
            }

            for (ArrayList<Integer> profile : possibleProfiles) {
                BigDecimal phosphoRsProbabilityBD = pInvMap.get(profile).multiply(new BigDecimal(100)).divide(pInvTotal, mathContext); //in percent
                if (phosphoRsProbabilityBD.compareTo(BigMathUtils.maxDouble) == 1) {
                    throw new IllegalArgumentException("PhosphoRS probability >100%");
                } else if (phosphoRsProbabilityBD.compareTo(BigDecimal.ZERO) == -1) {
                    throw new IllegalArgumentException("PhosphoRS probability <0%");
                }
                Double phosphoRsProbability = 0.0;
                if (phosphoRsProbabilityBD.compareTo(BigMathUtils.minNormalDouble) == 1) {
                    phosphoRsProbability = phosphoRsProbabilityBD.doubleValue();
                }
                profileToScoreMap.put(profile, phosphoRsProbability);
            }

        } else if (possibleSites.size() == nPTM) {
            profileToScoreMap.put(possibleSites, 100.0);
        } else {
            throw new IllegalArgumentException("Found less potential modification sites than PTMs during PhosphoRS calculation. Peptide key: " + peptide.getKey());
        }

        HashMap<Integer, BigDecimal> scores = new HashMap<Integer, BigDecimal>();
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
     * This method returns P in -10.log(P).
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param p the probability for a calculated fragment matching one of the
     * experimental masses by chance as estimated by PhosphoRS
     * @param spectrumAnnotator spectrum annotator
     * @param annotationSettings the global annotation settings
     * @param scoringAnnotationSettings the annotation settings specific to this
     * peptide and spectrum
     * @param mathContext the math context to use for calculation
     *
     * @return the phosphoRS score
     */
    private static BigDecimal getPhosphoRsScoreP(Peptide peptide, MSnSpectrum spectrum, double p, PeptideSpectrumAnnotator spectrumAnnotator,
            AnnotationSettings annotationSettings, SpecificAnnotationSettings scoringAnnotationSettings, MathContext mathContext) throws MathException {

        int n = 0;
        for (ArrayList<Ion> fragmentIons : spectrumAnnotator.getExpectedIons(scoringAnnotationSettings, peptide).values()) {
            for (Ion ion : fragmentIons) {
                if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                    n++;
                }
            }
            n += fragmentIons.size();
        }

        BinomialDistribution distribution = new BinomialDistribution(n, p);

        ArrayList<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(annotationSettings, scoringAnnotationSettings, spectrum, peptide);
        int k = 0;
        for (IonMatch ionMatch : matches) {
            if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                k++;
            }
        }
        if (k == 0) {
            return BigDecimal.ONE;
        }

        return distribution.getCumulativeProbabilityAt((double) k, mathContext);
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
        double p = ms2Tolerance * N / w;
        if (p > 1) {
            p = 1;
        }
        return p;
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
     * Returns a map of all potential site determining ions indexed by their
     * m/z.
     *
     * @param noModPeptide the version of the peptide which does not contain the
     * modification of interest
     * @param possibleProfiles the possible modification profiles to inspect
     * @param referencePtmName the name of the reference ptm
     * @param spectrumAnnotator the spectrum annotator used throughout the
     * scoring
     * @param scoringAnnotationSetttings the annotation settings specific to
     * this peptide and spectrum
     *
     * @return a list of mz where we can possibly find a site determining ion
     */
    private static HashMap<Double, ArrayList<ArrayList<Integer>>> getSiteDeterminingIons(Peptide noModPeptide, ArrayList<ArrayList<Integer>> possibleProfiles,
            String referencePtmName, PeptideSpectrumAnnotator spectrumAnnotator, SpecificAnnotationSettings scoringAnnotationSetttings) {

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

            for (ArrayList<Ion> ions : spectrumAnnotator.getExpectedIons(scoringAnnotationSetttings, peptide).values()) {
                for (Ion ion : ions) {
                    if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                        for (int charge : scoringAnnotationSetttings.getSelectedCharges()) {
                            double mz = ion.getTheoreticMz(charge);
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

            if (!mzToPeak.isEmpty()) {
                reducedSpectra.add(new MSnSpectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle() + "_" + depth, mzToPeak, spectrum.getFileName()));
            }
            depth++;
        }

        return reducedSpectra;
    }

    /**
     * Filters the spectrum so that p is lower or equal to 1 by retaining the
     * most intense peaks in a window of 10 times the ms2 tolerance.
     *
     * @param spectrum the original spectrum
     * @param ms2Tolerance the ms2 tolerance
     *
     * @return the filtered spectrum
     */
    private static MSnSpectrum filterSpectrum(MSnSpectrum spectrum, double ms2Tolerance) {

        Double window;
        Integer maxPeaks;

        if (ms2Tolerance <= 10) {
            window = 10 * ms2Tolerance;
            maxPeaks = 10;
        } else {
            window = 100.0;
            maxPeaks = (int) (window / ms2Tolerance);
        }

        if (maxPeaks < 1) {
            throw new IllegalArgumentException("All peaks removed by filtering");
        }

        HashMap<Double, Peak> peakMap = spectrum.getPeakMap(),
                newMap = new HashMap<Double, Peak>(peakMap.size()),
                tempMap = new HashMap<Double, Peak>();
        Double refMz = null;

        for (Double mz : spectrum.getOrderedMzValues()) {
            if (refMz == null) {
                refMz = mz;
            } else if (mz > refMz + window) {
                ArrayList<Double> intensities = new ArrayList<Double>(tempMap.keySet());
                Collections.sort(intensities, Collections.reverseOrder());
                for (int i = 0; i < Math.min(intensities.size(), maxPeaks); i++) {
                    Double intensity = intensities.get(i);
                    Peak peak = tempMap.get(intensity);
                    newMap.put(peak.mz, peak);
                }
                tempMap.clear();
                refMz = mz;
            }
            Peak peak = peakMap.get(mz);
            tempMap.put(peak.intensity, peak);
        }

        ArrayList<Double> intensities = new ArrayList<Double>(tempMap.keySet());
        Collections.sort(intensities, Collections.reverseOrder());

        for (int i = 0; i < Math.min(intensities.size(), maxPeaks); i++) {
            Double intensity = intensities.get(i);
            Peak peak = tempMap.get(intensity);
            newMap.put(peak.mz, peak);
        }

        return new MSnSpectrum(spectrum.getLevel(), spectrum.getPrecursor(), spectrum.getSpectrumTitle() + "_filtered", newMap, spectrum.getFileName());
    }
}
