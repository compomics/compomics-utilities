package com.compomics.util.experiment.quantification.reporterion.quantification;

import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.quantification.Ratio;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.HashMap;

/**
 * This class models the quantification at the MS2 spectrum level.
 * 
 * @author Marc Vaudel
 */
public class PsmQuantification extends ExperimentObject {

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
     * The estimated ratios
     */
    private HashMap<Integer, Ratio> ratios = new HashMap();

    /**
     * Constructor for a spectrumQuantification
     * @param spectrumKey  the corresponding spectrum
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

    /**
     * Method to add an estimated ratio
     * @param reporterIndex     The static index of the reporter ion which is divided by the reference
     * @param ratio             The estimated ratio
     */
    public void addRatio(int reporterIndex, Ratio ratio) {
        ratios.put(reporterIndex, ratio);
    }

    /**
     * Getter for the spectrum key
     * @return the corresponding spectrum key
     */
    public String getKey() {
        return spectrumKey;
    }

    /**
     * Getter for the key of the spectrum match
     * @return
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
     * Getter for the estimated ratio
     * @return map containing the reporter static index and the estimated ratio
     */
    public HashMap<Integer, Ratio> getRatios() {
        return ratios;
    }
}
