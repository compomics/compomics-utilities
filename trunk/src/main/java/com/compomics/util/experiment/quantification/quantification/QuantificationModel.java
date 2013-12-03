/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.quantification.quantification;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Kenneth
 */
public class QuantificationModel {

    private final HashMap<QuantificationWeight, QuantificationHit> quantifications = new HashMap<QuantificationWeight, QuantificationHit>();
    private final ArrayList<String> spectra = new ArrayList<String>();
    private String bestAssumptionSpectrum;
    private final String peptideKey;
    private final QuantificationTechnique technique;

    public QuantificationModel(String peptideKey, QuantificationTechnique technique) {
        this.peptideKey = peptideKey;
        this.technique = technique;
    }

    public String getBestAssumptionSpectrum() {
        return bestAssumptionSpectrum;
    }

    public void setBestAssumptionSpectrum(String bestAssumptionSpectrum) {
        this.bestAssumptionSpectrum = bestAssumptionSpectrum;
    }

    public HashMap<QuantificationWeight, QuantificationHit> getQuantifications() {
        return quantifications;
    }

    public ArrayList<String> getSpectra() {
        return spectra;
    }

    public String getPeptideKey() {
        return peptideKey;
    }

    public QuantificationTechnique getTechnique() {
        return technique;
    }

    public void addQuantification(QuantificationHit quantification) {
        quantifications.put(quantification.getWeight(), quantification);
    }

    public double getHighOverLightRatio() {
        if (quantifications.containsKey(QuantificationWeight.HEAVY) && quantifications.containsKey(QuantificationWeight.LIGHT)) {
            return quantifications.get(QuantificationWeight.HEAVY).getIntensity() / quantifications.get(QuantificationWeight.LIGHT).getIntensity();
        } else {
            return 0.0;
        }
    }

    public double getHeavyOverMediumRatio() {
        if (quantifications.containsKey(QuantificationWeight.HEAVY) && quantifications.containsKey(QuantificationWeight.MEDIUM)) {
            return quantifications.get(QuantificationWeight.HEAVY).getIntensity() / quantifications.get(QuantificationWeight.MEDIUM).getIntensity();
        } else {
            return 0.0;
        }
    }

    public double getMediumOverLightRatio() {
        if (quantifications.containsKey(QuantificationWeight.MEDIUM) && quantifications.containsKey(QuantificationWeight.LIGHT)) {
            return quantifications.get(QuantificationWeight.MEDIUM).getIntensity() / quantifications.get(QuantificationWeight.LIGHT).getIntensity();
        } else {
            return 0.0;
        }
    }
}
