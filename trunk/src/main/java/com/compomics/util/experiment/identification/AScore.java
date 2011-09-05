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

    public static HashMap<ArrayList<Integer>, Double> getAScore(Peptide peptide, PTM ptm, MSnSpectrum spectrum,
            ArrayList<PeptideFragmentIon.PeptideFragmentIonType> expectedFragmentIons, HashMap<NeutralLoss, Integer> neutralLosses, 
            ArrayList<Integer> charges, double intensityLimit, double mzTolerance) {
        
        HashMap<ArrayList<Integer>, Double> result = new HashMap<ArrayList<Integer>, Double>();
        ArrayList<Integer> possibleSites = new ArrayList<Integer>();
        String tempSequence;
        int index;
        for (String aa : ptm.getResiduesArray()) {
            tempSequence = peptide.getSequence();
            while ((index = tempSequence.indexOf(aa)) > 0) {
                possibleSites.add(index);
                tempSequence = tempSequence.substring(index + 1);
            }
        }
        Collections.sort(possibleSites);
        ArrayList<IonMatch> matches;
        HashMap<Integer, HashMap<Double, Integer>> rawMap = new HashMap<Integer, HashMap<Double, Integer>>(); // curve figure 2b in A-score paper
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
            rawMap.put(i, new HashMap<Double, Integer>());
            p = ((double) i) / 100;
            for (int pos = 0; pos < possibleSites.size() - 1; pos++) {
                tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getMass(), noModPeptide.getParentProteins(), noModPeptide.getModificationMatches());
                tempPeptide.addModificationMatch(new ModificationMatch(ptm, true, possibleSites.get(pos)));
                matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, spectrumMap.get(i), tempPeptide, intensityLimit, mzTolerance);
                n = matches.size();
                P = 0;
                for (int k = n; k <= N; n++) {
                    P += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
                }
                rawMap.get(i).put(P, possibleSites.get(pos));
            }
        }
        int bestI = 0, pos1 = 0, pos2 = 0;
        double maxDiff = 0;
        for (int i = 1; i <= 10; i++) {
            for (double score1 : rawMap.get(i).keySet()) {
                for (double score2 : rawMap.get(i).keySet()) {
                    if (score1 - score2 > maxDiff) {
                        bestI = i;
                        pos1 = Math.min(rawMap.get(i).get(score1), rawMap.get(i).get(score2));
                        pos2 = Math.max(rawMap.get(i).get(score1), rawMap.get(i).get(score2));;
                    }
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
        for (int k = n; k <= N; n++) {
            p1 += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
        }
        tempPeptide.addModificationMatch(new ModificationMatch(ptm, true, pos2));
        matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, spectrumMap.get(bestI), tempPeptide, intensityLimit, mzTolerance);
        n = matches.size();
        double p2 = 0;
        for (int k = n; k <= N; n++) {
            p2 += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
        }
        if (p1 < p2) {
            ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
            modificationProfile.add(pos1);
            result.put(modificationProfile, p2 - p1);
        } else {
            ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
            modificationProfile.add(pos2);
            result.put(modificationProfile, p1 - p2);
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
        double currentmzMin = 0;
        int cpt = 0;
        Peak tempPeak;
        while (currentmzMin < mzMax) {
            intensities = new ArrayList<Double>();
            tempMap = new HashMap<Double, Peak>();
            while (mz.get(cpt) < currentmzMin + 20*mzTolerance) {
                tempPeak = peakMap.get(mz.get(cpt));
                intensities.add(tempPeak.intensity);
                tempMap.put(tempPeak.intensity, tempPeak);
                cpt++;
            }
            Collections.sort(intensities);
            for (int i = 1; i <= 10; i++) {
                if (i < intensities.size()) {
                    result.get(i).addPeak(tempMap.get(intensities.get(i - 1)));
                }
            }
            currentmzMin += 20*mzTolerance;
        }
        return result;
    }
}
