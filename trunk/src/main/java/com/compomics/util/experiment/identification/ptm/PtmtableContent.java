/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.ptm;

import com.compomics.util.experiment.biology.ions.PeptideFragmentIon.PeptideFragmentIonType;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Convenience class for the content of a ptm table
 *
 * @author Marc
 */
public class PtmtableContent {

    /**
     * The content of the table: modification status -> fragment ion type -> aa number -> list of intensities
     */
    private HashMap<Integer, HashMap<PeptideFragmentIonType, HashMap<Integer, ArrayList<Double>>>> map;
    private double totalIntensity = 0;
    private double maxIntensity = 0;

    public PtmtableContent() {
        map = new HashMap<Integer, HashMap<PeptideFragmentIonType, HashMap<Integer, ArrayList<Double>>>>();
    }

    public void addIntensity(int nMod, PeptideFragmentIonType peptideFragmentIonType, int aa, double intensity) {
        if (!map.containsKey(nMod)) {
            map.put(nMod, new HashMap<PeptideFragmentIonType, HashMap<Integer, ArrayList<Double>>>());
        }
        if (!map.get(nMod).containsKey(peptideFragmentIonType)) {
            map.get(nMod).put(peptideFragmentIonType, new HashMap<Integer, ArrayList<Double>>());
        }
        if (!map.get(nMod).get(peptideFragmentIonType).containsKey(aa)) {
            map.get(nMod).get(peptideFragmentIonType).put(aa, new ArrayList<Double>());
        }
        map.get(nMod).get(peptideFragmentIonType).get(aa).add(intensity);
        totalIntensity += intensity;
        if (intensity > maxIntensity) {
            maxIntensity = intensity;
        }
    }

    public ArrayList<Double> getIntensities(int nMod, PeptideFragmentIonType peptideFragmentIonType, int aa) {
        if (map.containsKey(nMod)
                && map.get(nMod).containsKey(peptideFragmentIonType)
                && map.get(nMod).get(peptideFragmentIonType).containsKey(aa)) {
            return map.get(nMod).get(peptideFragmentIonType).get(aa);
        } else {
            return new ArrayList<Double>();
        }
    }

    public Double getQuantile(int nMod, PeptideFragmentIonType peptideFragmentIonType, int aa, double quantile) {
        ArrayList<Double> intensities = getIntensities(nMod, peptideFragmentIonType, aa);
        if (intensities.size() > 0) {
            int index = (int) (quantile * intensities.size());
            return intensities.get(index);
        } else {
            return 0.0;
        }
    }

    public HashMap<Integer, HashMap<PeptideFragmentIonType, HashMap<Integer, ArrayList<Double>>>> getMap() {
        return map;
    }

    public void addAll(PtmtableContent anotherContent) {
        for (int nPTM : anotherContent.getMap().keySet()) {
            for (PeptideFragmentIonType peptideFragmentIonType : anotherContent.getMap().get(nPTM).keySet()) {
                for (int nAA : anotherContent.getMap().get(nPTM).get(peptideFragmentIonType).keySet()) {
                    for (double intensity : anotherContent.getIntensities(nPTM, peptideFragmentIonType, nAA)) {
                        addIntensity(nPTM, peptideFragmentIonType, nAA, intensity);
                    }
                }
            }
        }
    }

    public void normalize() {
        if (totalIntensity > 0) {
            double normalization = totalIntensity;
            totalIntensity = 0;
            maxIntensity = 0;
            ArrayList<Double> tempIntensities;
            for (int nPTM : map.keySet()) {
                for (PeptideFragmentIonType peptideFragmentIonType : map.get(nPTM).keySet()) {
                    for (int nAA : map.get(nPTM).get(peptideFragmentIonType).keySet()) {
                        tempIntensities = new ArrayList<Double>();
                        for (double intensity : getIntensities(nPTM, peptideFragmentIonType, nAA)) {
                            tempIntensities.add(intensity / normalization);
                        }
                        map.get(nPTM).get(peptideFragmentIonType).put(nAA, tempIntensities);
                    }
                }
            }
        }
    }

    public double getMaxIntensity() {
        return maxIntensity;
    }
}
