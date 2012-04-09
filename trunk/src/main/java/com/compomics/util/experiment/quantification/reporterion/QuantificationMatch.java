package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.quantification.Ratio;
import java.util.HashMap;

/**
 * Abstract class for a quantification match.
 *
 * @author Marc Vaudel
 */
public abstract class QuantificationMatch extends ExperimentObject {

    /**
     * Returns the key of a match.
     *
     * @return the key of a match
     */
    public abstract String getKey();
    /**
     * The estimated ratios.
     */
    protected HashMap<Integer, Ratio> ratios = new HashMap();

    /**
     * Sets new peptide ratios.
     *
     * @param ratios the new peptide ratios
     */
    public void setRatios(HashMap<Integer, Ratio> ratios) {
        this.ratios = ratios;
    }

    /**
     * Adds a new ratio to the ratio map.
     *
     * @param reporterIon the index of the reporter ion
     * @param ratio the ratio
     */
    public void addRatio(Integer reporterIon, Ratio ratio) {
        ratios.put(reporterIon, ratio);
    }

    /**
     * Getter for the peptide ratios.
     *
     * @return the peptide ratios
     */
    public HashMap<Integer, Ratio> getRatios() {
        return ratios;
    }
}
