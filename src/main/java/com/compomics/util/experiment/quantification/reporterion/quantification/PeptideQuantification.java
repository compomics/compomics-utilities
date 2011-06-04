package com.compomics.util.experiment.quantification.reporterion.quantification;

import com.compomics.util.experiment.quantification.Ratio;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.HashMap;

/**
 * This class models quantification at the peptide level.
 * 
 * @author Marc Vaudel
 */
public class PeptideQuantification extends ExperimentObject {

    /**
     * The identification peptide match
     */
    private String peptideKey;

    /**
     * The corresponding spectrum quantification
     */
    private HashMap<String, PsmQuantification> psmQuantification = new HashMap<String, PsmQuantification>();

    /**
     * The estimated ratios
     */
    private HashMap<Integer, Ratio> ratios = new HashMap<Integer, Ratio>();

    /**
     * Constructor for the peptide quantification
     * @param peptideKey              the key of the identification peptide match
     */
    public PeptideQuantification(String peptideKey) {
        this.peptideKey = peptideKey;
    }
    /**
     * Constructor for the peptide quantification
     * @param peptideKey              the key of the identification peptide match
     * @param psmQuantification         the corresponding spectrum quantification 
     */
    public PeptideQuantification(String peptideKey, HashMap<String, PsmQuantification> psmQuantification) {
        this.peptideKey = peptideKey;
        this.psmQuantification = psmQuantification;
    }

    /**
     * Constructor for the peptide quantification
     * @param peptideKey              the identification peptide match
     * @param psmQuantification         the corresponding spectrum quantification
     * @param ratios                    the estimated ratios
     */
    public PeptideQuantification(String peptideKey, HashMap<String, PsmQuantification> psmQuantification, HashMap<Integer, Ratio> ratios) {
        this.peptideKey = peptideKey;
        this.psmQuantification = psmQuantification;
        this.ratios = ratios;
    }

    /**
     * sets new peptide ratios
     * @param ratios    the new peptide ratios
     */
    public void setPeptideRatios(HashMap<Integer, Ratio> ratios) {
        this.ratios = ratios;
    }

    /**
     * Getter for the peptide ratios
     * @return the peptide ratios
     */
    public HashMap<Integer, Ratio> getRatios() {
        return ratios;
    }

    /**
     * Getter for the corresponding spectrum quantification
     * @return List of spectrum quantification
     */
    public HashMap<String, PsmQuantification> getPsmQuantification() {
        return psmQuantification;
    }
    /**
     * returns a specific psm quantification
     * @param index the index of the quantification 
     * @return the desired psm
     */
    public PsmQuantification getPsm(String index) {
        return psmQuantification.get(index);
    }

    /**
     * Adds a new psm quantification in the psm quantification map
     * @param newPsmQuantification the new psm quantification
     */
    public void addPsmQuantification(PsmQuantification newPsmQuantification) {
        psmQuantification.put(newPsmQuantification.getKey(), newPsmQuantification);
    }

    /**
     * Returns the key of the peptide quantification
     * @return the key of the peptide quantification
     */
    public String getKey() {
        return peptideKey;
    }
}
