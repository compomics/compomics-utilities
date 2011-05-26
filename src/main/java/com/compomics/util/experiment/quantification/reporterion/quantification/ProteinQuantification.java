package com.compomics.util.experiment.quantification.reporterion.quantification;

import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.quantification.Ratio;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class models the quantification results for a protein match.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 3:31:25 PM
 */
public class ProteinQuantification extends ExperimentObject {

    /**
     * The identification protein match key
     */
    private String proteinKey;

    /**
     * The peptide quantification corresponding
     */
    private HashMap<String, PeptideQuantification> peptideQuantification = new HashMap<String, PeptideQuantification>();

    /**
     * The estimated protein ratios
     */
    private HashMap<Integer, Ratio> proteinRatios = new HashMap<Integer, Ratio>();

    /**
     * Constructor for the protein quantification
     * @param proteinKey              the identified protein match key
     */
    public ProteinQuantification(String proteinKey) {
        this.proteinKey = proteinKey;
    }
    /**
     * Constructor for the protein quantification
     * @param proteinKey              the identified protein match key
     * @param peptideQuantification     the corresponding peptide quantification
     */
    public ProteinQuantification(String proteinKey, HashMap<String, PeptideQuantification> peptideQuantification) {
        this.proteinKey = proteinKey;
        this.peptideQuantification = peptideQuantification;
    }

    /**
     * Constructor for the protein quantification
     * @param proteinKey              the identified protein match key
     * @param proteinRatios             the estimated protein ratios
     * @param peptideQuantification     the corresponding peptide quantification
     */
    public ProteinQuantification(String proteinKey, HashMap<String, PeptideQuantification> peptideQuantification, HashMap<Integer, Ratio> proteinRatios) {
        this.proteinKey = proteinKey;
        this.proteinRatios = proteinRatios;
        this.peptideQuantification = peptideQuantification;
    }

    /**
     * sets new protein ratios
     * @param ratios    estimated protein ratios
     */
    public void setProteinRatios(HashMap<Integer, Ratio> ratios) {
        this.proteinRatios = ratios;
    }

    /**
     * Getter for the ratios
     * @return the estimated ratios
     */
    public HashMap<Integer, Ratio> getProteinRatios() {
        return proteinRatios;
    }

    /**
     * Getter for the corresponding peptide quantification
     * @return list of peptide quantification
     */
    public HashMap<String, PeptideQuantification> getPeptideQuantification() {
        return peptideQuantification;
    }

    /**
     * Returns a specific peptide quantification
     * @param index the index of the desired peptide quantification
     * @return      the desired peptide quantification
     */
    public PeptideQuantification getPeptideQuantification(String index) {
        return peptideQuantification.get(index);
    }

    /**
     * Adds a new peptide quantification in the protein quantification
     * @param newPeptideQuantification the new peptide quantification
     */
    public void addPeptideQuantification(PeptideQuantification newPeptideQuantification) {
        peptideQuantification.put(newPeptideQuantification.getKey(), newPeptideQuantification);
    }

    /**
     * Returns the indexing key of the protein quantification
     * @return the indexing key of the protein quantification
     */
    public String getKey() {
        return proteinKey;
    }
}
