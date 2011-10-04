package com.compomics.util.experiment.quantification.reporterion.quantification;

import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.quantification.Ratio;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.quantification.reporterion.QuantificationMatch;

import java.util.HashMap;

/**
 * This class models the quantification at the MS2 spectrum level.
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

    @Override
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
}
