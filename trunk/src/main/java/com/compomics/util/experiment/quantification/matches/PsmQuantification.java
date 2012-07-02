package com.compomics.util.experiment.quantification.matches;

import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.quantification.QuantificationMatch;

import java.util.HashMap;

/**
 * This class models the quantification of a spectrum.
 * @TODO: make it quantification method independent
 * 
 * @author Marc Vaudel
 */
public class PsmQuantification extends QuantificationMatch {

    /**
     * The corresponding spectrum key
     */
    private String spectrumKey;
    /**
     * The key of the spectrum match
     */
    private String spectrumMatchKey;
    /**
     * The matches of the reporter ions
     */
    private HashMap<Integer, IonMatch> reporterMatches = new HashMap<Integer, IonMatch>();
    /**
     * The deisotoped intensities
     */
    private HashMap<Integer, Double> deisotopedIntensities = new HashMap<Integer, Double>();
    /**
     * The reference intensity taken to estimate the ratios
     */
    private double referenceIntensity;

    /**
     * Constructor for a spectrumQuantification
     * @param spectrumKey the corresponding spectrum key
     * @param spectrumMatchKey  the corresponding spectrum match key
     */
    public PsmQuantification(String spectrumKey, String spectrumMatchKey) {
        this.spectrumKey = spectrumKey;
        this.spectrumMatchKey = spectrumMatchKey;
    }

    /**
     * Method to add a match between a peak and a reporter ion
     * @param reporterIndex     static index of the reporter ion
     * @param match             The corresponding ion match
     */
    public void addIonMatch(int reporterIndex, IonMatch match) {
        reporterMatches.put(reporterIndex, match);
    }

    @Override
    public String getKey() {
        return spectrumKey;
    }

    /**
     * Getter for the key of the spectrum match
     * @return the key of the spectrum match
     */
    public String getSpectrumMatchKey() {
        return spectrumMatchKey;
    }

    /**
     * Getter for the reporter matches
     * @return matches between reporters and peaks
     */
    public HashMap<Integer, IonMatch> getReporterMatches() {
        return reporterMatches;
    }

    /**
     * returns the deisotoped intensities
     * @return the deisotoped intensities 
     */
    public HashMap<Integer, Double> getDeisotopedIntensities() {
        return deisotopedIntensities;
    }

    /**
     * Sets the deisotoped intensities
     * @param deisotopedIntensities the deisotoped intensities
     */
    public void setDeisotopedIntensities(HashMap<Integer, Double> deisotopedIntensities) {
        this.deisotopedIntensities = deisotopedIntensities;
    }

    /**
     * Returns the reference intensity used to estimate the ratios
     * @return the reference intensity used to estimate the ratios
     */
    public double getReferenceIntensity() {
        return referenceIntensity;
    }

    /**
     * Sets the reference intensity used to estimate the ratios
     * @param referenceIntensity the reference intensity used to estimate the ratios
     */
    public void setReferenceIntensity(double referenceIntensity) {
        this.referenceIntensity = referenceIntensity;
    }

    @Override
    public MatchType getType() {
        return MatchType.Spectrum;
    }
}
