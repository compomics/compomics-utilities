/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon.PeptideFragmentIonType;
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
 * This class scores PTM locations using the A-score.
 *
 * @author Marc
 */
public class AScore {

    public static HashMap<ArrayList<Integer>, Double> getAScore(Peptide peptide, PTM ptm, int nPTM, MSnSpectrum spectrum,
            ArrayList<PeptideFragmentIon.PeptideFragmentIonType> expectedFragmentIons, HashMap<NeutralLoss, Integer> neutralLosses,
            ArrayList<Integer> charges, double intensityLimit, double mzTolerance) {

        HashMap<ArrayList<Integer>, Double> result = new HashMap<ArrayList<Integer>, Double>();
        ArrayList<Integer> possibleSites = new ArrayList<Integer>();
        String tempSequence;
        int tempIndex, ref = 0;
        for (String aa : ptm.getResiduesArray()) {
            tempSequence = peptide.getSequence();
            while ((tempIndex = tempSequence.indexOf(aa)) >= 0) {
                possibleSites.add(ref + tempIndex);
                tempSequence = tempSequence.substring(tempIndex + 1);
                ref += tempIndex + 1;
            }
        }
        if (possibleSites.size() > nPTM) {
            Collections.sort(possibleSites);
            ArrayList<IonMatch> matches;
            HashMap<Integer, HashMap<Double, ArrayList<Integer>>> rawMap = new HashMap<Integer, HashMap<Double, ArrayList<Integer>>>(); // curve figure 2b in A-score paper
            HashMap<Integer, MSnSpectrum> spectrumMap = getReducedSpectra(spectrum, mzTolerance);
            SpectrumAnnotator spectrumAnnotator = new SpectrumAnnotator();

            double p, P;
            int n, N = 0;
            Peptide tempPeptide, noModPeptide = new Peptide(peptide.getSequence(), peptide.getMass(), peptide.getParentProteins(), new ArrayList<ModificationMatch>());
            for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
                if (!modificationMatch.getTheoreticPtm().getName().equals(ptm.getName())) {
                    noModPeptide.addModificationMatch(modificationMatch);
                }
            }
            for (ArrayList<PeptideFragmentIon> fragmentIons : spectrumAnnotator.getExpectedIons(expectedFragmentIons, neutralLosses, charges, peptide, spectrum.getPrecursor().getCharge().value).values()) {
                N += fragmentIons.size();
            }
            for (int i = 1; i <= 10; i++) {
                rawMap.put(i, new HashMap<Double, ArrayList<Integer>>());
                p = ((double) i) / 100;
                for (int pos = 0; pos < possibleSites.size(); pos++) {
                    tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getMass(), noModPeptide.getParentProteins(), noModPeptide.getModificationMatches());
                    tempPeptide.addModificationMatch(new ModificationMatch(ptm, true, possibleSites.get(pos)+1));
                    matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, spectrumMap.get(i), tempPeptide, intensityLimit, mzTolerance);
                    n = matches.size();
                    P = 0;
                    for (int k = n; k <= N; k++) {
                        P += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
                    }
                    if (!rawMap.get(i).containsKey(P)) {
                    rawMap.get(i).put(P, new ArrayList<Integer>());
                    }
                    rawMap.get(i).get(P).add(possibleSites.get(pos));
                }
            }
            int bestI = 0, pos1 = 0, pos2 = 0;
            double maxDiff = -1;
            ArrayList<Double> scores;
            for (int i = 1; i <= 10; i++) {
                scores = new ArrayList<Double>(rawMap.get(i).keySet());
                Collections.sort(scores);
                if (rawMap.get(i).get(scores.get(scores.size()-1)).size()==1) {
                    if (scores.get(scores.size()-1)-scores.get(scores.size()-2) > maxDiff) {
                        pos1 = rawMap.get(i).get(scores.get(scores.size()-1)).get(0);
                        pos2 = rawMap.get(i).get(scores.get(scores.size()-2)).get(0);
                        bestI = i;
                        maxDiff = scores.get(scores.size()-1)-scores.get(scores.size()-2);
                    }
                } else {
                    if (0 > maxDiff) {
                        pos1 = rawMap.get(i).get(scores.get(scores.size()-1)).get(0);
                        pos2 = rawMap.get(i).get(scores.get(scores.size()-1)).get(1);
                        bestI = i;
                        maxDiff = 0;
                    }
                }
            }
            N = 0;
            int aa;
            for (ArrayList<PeptideFragmentIon> fragmentIons : spectrumAnnotator.getExpectedIons(expectedFragmentIons, neutralLosses, charges, peptide, spectrum.getPrecursor().getCharge().value).values()) {
                for (PeptideFragmentIon peptideFragmentIon : fragmentIons) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION
                            || peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION
                            || peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        aa = peptideFragmentIon.getNumber();
                        if (aa >= pos1 && aa < pos2) {
                            N++;
                        }
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION
                            || peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION
                            || peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        aa = peptide.getSequence().length() - peptideFragmentIon.getNumber();
                        if (aa >= pos1 && aa < pos2) {
                            N++;
                        }
                    }
                }
            }
            p = ((double) bestI) / 100;
            tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getMass(), noModPeptide.getParentProteins(), noModPeptide.getModificationMatches());
            tempPeptide.addModificationMatch(new ModificationMatch(ptm, true, pos1));
            matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, spectrumMap.get(bestI), tempPeptide, intensityLimit, mzTolerance);
            n = matches.size();
            double p1 = 0;
            for (int k = n; k <= N; k++) {
                p1 += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
            }
            tempPeptide.addModificationMatch(new ModificationMatch(ptm, true, pos2));
            matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, spectrumMap.get(bestI), tempPeptide, intensityLimit, mzTolerance);
            n = matches.size();
            double p2 = 0;
            for (int k = n; k <= N; k++) {
                p2 += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
            }
            double score;
            if (p1 == p2) {
                ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
                modificationProfile.add(pos1);
                modificationProfile.add(pos2);
                result.put(modificationProfile, 100.0);
            } else if (p1 < p2) {
                ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
                modificationProfile.add(pos1);
                score = -10* Math.log10(p2-p1);
                result.put(modificationProfile, score);
            } else {
                ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
                modificationProfile.add(pos2);
                score = -10* Math.log10(p1-p2);
                result.put(modificationProfile, score);
            }
        } else {
            ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
            for (int pos : possibleSites) {
                modificationProfile.add(pos);
            }
            result.put(modificationProfile, 100.0);
        }
        return result;
    }

    public static HashMap<Integer, MSnSpectrum> getReducedSpectra(MSnSpectrum baseSpectrum, double mzTolerance) {
        HashMap<Integer, MSnSpectrum> result = new HashMap<Integer, MSnSpectrum>();
        for (int i = 1; i <= 10; i++) {
            result.put(i, new MSnSpectrum(2, baseSpectrum.getPrecursor(), baseSpectrum.getSpectrumTitle() + " " + i, new HashSet<Peak>(), "a score"));
        }
        HashMap<Double, Peak> tempMap, peakMap = baseSpectrum.getPeakMap();
        ArrayList<Double> intensities, mz = new ArrayList<Double>(peakMap.keySet());
        Collections.sort(mz);
        double mzMax = mz.get(mz.size() - 1);
        int cpt = 0;
        double currentmzMin = 0;
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
            for (int i = 1; i <= 10; i++) {
                if (i <= intensities.size()) {
                    for (int j = i; j <= 10; j++) {
                        result.get(j).addPeak(tempMap.get(intensities.get(i - 1)));
                    }
                }
            }
            currentmzMin += 200 * mzTolerance;
        }
        return result;
    }
}
