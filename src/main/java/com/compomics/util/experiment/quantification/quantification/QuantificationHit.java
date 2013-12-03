/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.quantification.quantification;

import java.util.ArrayList;

/**
 *
 * @author Kenneth
 */
public class QuantificationHit {

    private final double intensity;
    private final QuantificationWeight weight;
    private final int peptideKey;
    private QuantificationModel quantificationGroup;

    public QuantificationHit(double intensity, int peptideKey, QuantificationWeight weight) {
        this.weight = weight;
        this.intensity = intensity;
        this.peptideKey = peptideKey;
    }

    public double getIntensity() {
        return intensity;
    }

    public QuantificationWeight getWeight() {
        return weight;
    }

    public int getPeptideKey() {
        return peptideKey;
    }

    public QuantificationModel getQuantificationGroup() {
        return quantificationGroup;
    }

    public void setQuantificationGroup(QuantificationModel quantificationGroup) {
        this.quantificationGroup = quantificationGroup;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.intensity) ^ (Double.doubleToLongBits(this.intensity) >>> 32));
        hash = 67 * hash + this.peptideKey;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QuantificationHit other = (QuantificationHit) obj;
        if (Double.doubleToLongBits(this.intensity) != Double.doubleToLongBits(other.intensity)) {
            return false;
        }
        if (this.weight != other.weight) {
            return false;
        }
        if (this.peptideKey != other.peptideKey) {
            return false;
        }
        return true;
    }

}
