package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon.PeptideFragmentIonType;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.ptm.PtmtableContent;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.math.BasicMathFunctions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class scores PTM locations using various A-scores.
 *
 * @author Marc Vaudel
 */
public class PTMLocationScores {

    /**
     * Returns the A-score for the best PTM location. In case the two best locations score the 
     * same they are both given with the score of 50.
     * 
     * @param peptide               The peptide of interest
     * @param ptm                   The PTM to score
     * @param nPTM                  The number of occurrences where this PTM is expected on this peptide
     * @param spectrum              The corresponding spectrum
     * @param expectedFragmentIons  The fragment ions to look for
     * @param neutralLosses         The neutral losses to look for
     * @param charges               The fragment ions charges to look for
     * @param mzTolerance           The m/z tolerance to use
     * @return a map containing the best or two best PTM location(s) and the corresponding A-score
     */
    public static HashMap<ArrayList<Integer>, Double> getAScore(Peptide peptide, PTM ptm, int nPTM, MSnSpectrum spectrum,
            ArrayList<PeptideFragmentIon.PeptideFragmentIonType> expectedFragmentIons, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance) {

        NeutralLossesMap scoringLossesMap = new NeutralLossesMap();
        
        // here annotation should be sequence and modification independant
        for (NeutralLoss neutralLoss : neutralLosses.getAccountedNeutralLosses()) {
            if (neutralLoss != NeutralLoss.CH4OS
                    && neutralLoss != NeutralLoss.H3PO4
                    && neutralLoss != NeutralLoss.HPO3) {
                scoringLossesMap.addNeutralLoss(neutralLoss, 0, 0);
            }
        }

        HashMap<ArrayList<Integer>, Double> result = new HashMap<ArrayList<Integer>, Double>();
        ArrayList<Integer> possibleSites = Peptide.getPotentialModificationSites(peptide.getSequence(), ptm);
        
        if (possibleSites.size() > nPTM) {
            Collections.sort(possibleSites);
            ArrayList<IonMatch> matches;
            HashMap<Integer, HashMap<Double, ArrayList<Integer>>> rawMap = new HashMap<Integer, HashMap<Double, ArrayList<Integer>>>(); // curve figure 2b in A-score paper
            HashMap<Integer, MSnSpectrum> spectrumMap = getReducedSpectra(spectrum, mzTolerance);
            SpectrumAnnotator spectrumAnnotator = new SpectrumAnnotator();

            double p, P, score;
            int n, N = 0;
            Peptide tempPeptide, noModPeptide = new Peptide(peptide.getSequence(), peptide.getParentProteins(), new ArrayList<ModificationMatch>());
            
            for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
                if (!modificationMatch.getTheoreticPtm().equals(ptm.getName())) {
                    noModPeptide.addModificationMatch(modificationMatch);
                }
            }
            
            for (ArrayList<PeptideFragmentIon> fragmentIons : spectrumAnnotator.getExpectedIons(expectedFragmentIons, neutralLosses, charges, precursorCharge, peptide).values()) {
                N += fragmentIons.size();
            }
            
            int bestI = 0, pos1 = 0, pos2 = 0;
            
            for (int i = 0; i < spectrumMap.keySet().size(); i++) {
                
                rawMap.put(i, new HashMap<Double, ArrayList<Integer>>());
                p = ((double) i + 1) / Math.max(spectrumMap.keySet().size(), 100);
                
                for (int pos = 0; pos < possibleSites.size(); pos++) {
                    tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getParentProteins(), noModPeptide.getModificationMatches());
                    tempPeptide.addModificationMatch(new ModificationMatch(ptm.getName(), true, possibleSites.get(pos) + 1));
                    matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, precursorCharge, spectrumMap.get(i), tempPeptide, 0, mzTolerance);
                    n = matches.size();
                    P = 0;
                    for (int k = n; k <= N; k++) {
                        P += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
                    }
                    score = 10 * Math.log10(P);
                    if (!rawMap.get(i).containsKey(score)) {
                        rawMap.get(i).put(score, new ArrayList<Integer>());
                    }
                    rawMap.get(i).get(score).add(possibleSites.get(pos) + 1);
                }
            }
            
            double maxDiff = -1;
            ArrayList<Double> scores;
            
            for (int i = 0; i < spectrumMap.keySet().size(); i++) {
                
                scores = new ArrayList<Double>(rawMap.get(i).keySet());
                Collections.sort(scores);
                
                if (rawMap.get(i).get(scores.get(0)).size() == 1) {
                    if (scores.get(1) - scores.get(0) > maxDiff) {
                        pos1 = rawMap.get(i).get(scores.get(0)).get(0);
                        pos2 = rawMap.get(i).get(scores.get(1)).get(0);
                        bestI = i;
                        maxDiff = scores.get(1) - scores.get(0);
                    }
                } else {
                    if (0 > maxDiff) {
                        pos1 = rawMap.get(i).get(scores.get(0)).get(0);
                        pos2 = rawMap.get(i).get(scores.get(0)).get(1);
                        bestI = i;
                        maxDiff = 0;
                    }
                }
            }
            
            N = 0;
            int aa;
            int posMin = Math.min(pos1, pos2);
            int posMax = Math.max(pos1, pos2);
            
            for (ArrayList<PeptideFragmentIon> fragmentIons : spectrumAnnotator.getExpectedIons(
                    expectedFragmentIons, neutralLosses, charges, precursorCharge, peptide).values()) {
                
                for (PeptideFragmentIon peptideFragmentIon : fragmentIons) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION
                            || peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION
                            || peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        aa = peptideFragmentIon.getNumber();
                        if (aa >= posMin && aa < posMax) {
                            N++;
                        }
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION
                            || peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION
                            || peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        aa = peptide.getSequence().length() - peptideFragmentIon.getNumber();
                        if (aa >= posMin && aa < posMax) {
                            N++;
                        }
                    }
                }
            }
            
            p = ((double) bestI + 1) / Math.max(spectrumMap.keySet().size(), 100);
            tempPeptide = new Peptide(noModPeptide.getSequence(), noModPeptide.getParentProteins(), noModPeptide.getModificationMatches());
            tempPeptide.addModificationMatch(new ModificationMatch(ptm.getName(), true, pos1));
            matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, precursorCharge, spectrumMap.get(bestI), tempPeptide, 0, mzTolerance);
            n = matches.size();
            double p1 = 0;
            
            for (int k = n; k <= N; k++) {
                p1 += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
            }
            
            tempPeptide.addModificationMatch(new ModificationMatch(ptm.getName(), true, pos2));
            matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, precursorCharge, spectrumMap.get(bestI), tempPeptide, 0, mzTolerance);
            n = matches.size();
            double p2 = 0;
            
            for (int k = n; k <= N; k++) {
                p2 += BasicMathFunctions.getCombination(k, N) * Math.pow(p, k) * Math.pow(1 - p, N - k);
            }
            
            if (p1 == p2) {
                ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
                modificationProfile.add(pos1);
                modificationProfile.add(pos2);
                result.put(modificationProfile, 50.0);
            } else if (p1 < p2) {
                ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
                modificationProfile.add(pos1);
                score = -10 * Math.log10(p2 - p1);
                result.put(modificationProfile, score);
            } else {
                ArrayList<Integer> modificationProfile = new ArrayList<Integer>();
                modificationProfile.add(pos2);
                score = -10 * Math.log10(p1 - p2);
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
     * Generates a map containing the spectra filtered on intensity with a basis of 20*mz tolerance 
     * indexed by the depth used. (see A-score paper for more details)
     *
     * @param baseSpectrum  the base spectrum
     * @param mzTolerance   the m/z tolerance
     * @return a map containing the spectra filtered indexed by peak depth.
     */
    public static HashMap<Integer, MSnSpectrum> getReducedSpectra(MSnSpectrum baseSpectrum, double mzTolerance) {
        
        HashMap<Integer, MSnSpectrum> result = new HashMap<Integer, MSnSpectrum>();
        HashMap<Double, Peak> tempMap, peakMap = baseSpectrum.getPeakMap();
        ArrayList<Double> intensities, mz = new ArrayList<Double>(peakMap.keySet());
        Collections.sort(mz);
        double mzMax = mz.get(mz.size() - 1);
        int cpt = 0;
        int cptTemp, nMax = 0;
        double currentmzMin = 0;
        
        while (currentmzMin < mzMax) {
            cptTemp = 0;
            while (cpt < mz.size()
                    && mz.get(cpt) < currentmzMin + 20 * mzTolerance) {
                cptTemp++;
                cpt++;
            }
            if (cptTemp > nMax) {
                nMax = cptTemp;
            }
            currentmzMin += 200 * mzTolerance;
        }
        
        for (int i = 0; i < nMax; i++) {
            result.put(i, new MSnSpectrum(2, baseSpectrum.getPrecursor(), baseSpectrum.getSpectrumTitle() + "_" + i, new HashSet<Peak>(), "a score"));
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
                for (int j = i; j < nMax; j++) {
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
     * @param peptide               The peptide of interest
     * @param ptm                   The PTM to score
     * @param nPTM                  The amount of times the PTM is expected
     * @param spectrum              The corresponding spectrum
     * @param expectedFragmentIons  The fragment ions to look for
     * @param neutralLosses         The neutral losses to look for
     * @param charges               The fragment ions charges to look for
     * @param mzTolerance           The m/z tolerance to use
     * @param intensityLimit 
     * @return the ptm plot series in the jfreechert format for one psm.
     */
    public static HashMap<PeptideFragmentIon, ArrayList<IonMatch>> getPTMPlotData(Peptide peptide, PTM ptm, int nPTM, MSnSpectrum spectrum,
            ArrayList<PeptideFragmentIon.PeptideFragmentIonType> expectedFragmentIons, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance, double intensityLimit) {

        Peptide noModPeptide = new Peptide(peptide.getSequence(), peptide.getParentProteins(), new ArrayList<ModificationMatch>());

        for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
            if (!modificationMatch.getTheoreticPtm().equals(ptm.getName())) {
                noModPeptide.addModificationMatch(modificationMatch);
            }
        }

        SpectrumAnnotator spectrumAnnotator = new SpectrumAnnotator();
        HashMap<Integer, ArrayList<PeptideFragmentIon>> fragmentIons =
                spectrumAnnotator.getExpectedIons(expectedFragmentIons, neutralLosses, charges, precursorCharge, noModPeptide);
        HashMap<PeptideFragmentIon, ArrayList<IonMatch>> map = new HashMap<PeptideFragmentIon, ArrayList<IonMatch>>();
        PeptideFragmentIon peptideFragmentIon;
        ArrayList<IonMatch> matches;

        for (int i = 0; i <= nPTM; i++) {

            spectrumAnnotator.setMassShift(i * ptm.getMass());
            matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, precursorCharge, spectrum, noModPeptide, intensityLimit, mzTolerance);
  
            for (IonMatch ionMatch : matches) {

                peptideFragmentIon = (PeptideFragmentIon) ionMatch.ion;

                if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION
                        || peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION
                        || peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION
                        || peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION
                        || peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION
                        || peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {

                    for (PeptideFragmentIon noModIon : fragmentIons.get(ionMatch.charge.value)) {
                        if (noModIon.getType() == peptideFragmentIon.getType()
                                && noModIon.getNumber() == peptideFragmentIon.getNumber()
                                && noModIon.getNeutralLoss().equals(peptideFragmentIon.getNeutralLoss())) {
                            if (!map.containsKey(noModIon)) {
                                map.put(noModIon, new ArrayList<IonMatch>());
                            }
                            map.get(noModIon).add(ionMatch);
                            break;
                        }
                    }
                }
            }
        }
        return map;
    }
    
    public static PtmtableContent getPTMTableContent(Peptide peptide, PTM ptm, int nPTM, MSnSpectrum spectrum,
            ArrayList<PeptideFragmentIon.PeptideFragmentIonType> expectedFragmentIons, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, double mzTolerance, double intensityLimit) {

        PtmtableContent ptmTableContent = new PtmtableContent();
        
        Peptide noModPeptide = new Peptide(peptide.getSequence(), peptide.getParentProteins(), new ArrayList<ModificationMatch>());

        for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
            if (!modificationMatch.getTheoreticPtm().equals(ptm.getName())) {
                noModPeptide.addModificationMatch(modificationMatch);
            }
        }

        SpectrumAnnotator spectrumAnnotator = new SpectrumAnnotator();
        spectrumAnnotator.setPeptide(noModPeptide, precursorCharge);
        PeptideFragmentIon peptideFragmentIon;
        ArrayList<IonMatch> matches;

        for (int i = 0; i <= nPTM; i++) {

            spectrumAnnotator.setMassShift(i * ptm.getMass());
            matches = spectrumAnnotator.getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, precursorCharge, spectrum, noModPeptide, intensityLimit, mzTolerance);
  
            for (IonMatch ionMatch : matches) {

                peptideFragmentIon = (PeptideFragmentIon) ionMatch.ion;

                if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION
                        || peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION
                        || peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                    ptmTableContent.addIntensity(i, peptideFragmentIon.getType(), peptideFragmentIon.getNumber(), ionMatch.peak.intensity);
                } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION
                        || peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION
                        || peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                    ptmTableContent.addIntensity(i, peptideFragmentIon.getType(), peptide.getSequence().length() - peptideFragmentIon.getNumber() + 1, ionMatch.peak.intensity);
                }
            }
        }
        return ptmTableContent;
    }
    
}
