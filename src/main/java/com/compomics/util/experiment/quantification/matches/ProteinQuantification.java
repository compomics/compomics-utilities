package com.compomics.util.experiment.quantification.matches;

import com.compomics.util.experiment.quantification.Ratio;
import com.compomics.util.experiment.quantification.QuantificationMatch;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class models the quantification of a protein.
 * @TODO: make it quantification method independent
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 3:31:25 PM
 */
public class ProteinQuantification extends QuantificationMatch {

    /**
     * The identification protein match key
     */
    private String proteinKey;

    /**
     * The peptide quantification corresponding
     */
    private ArrayList<String> peptideQuantification = new ArrayList<String>();

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
    public ProteinQuantification(String proteinKey, ArrayList<String> peptideQuantification) {
        this.proteinKey = proteinKey;
        this.peptideQuantification.addAll(peptideQuantification);
    }

    /**
     * Constructor for the protein quantification
     * @param proteinKey              the identified protein match key
     * @param proteinRatios             the estimated protein ratios
     * @param peptideQuantification     the corresponding peptide quantification
     */
    public ProteinQuantification(String proteinKey, ArrayList<String> peptideQuantification, HashMap<Integer, Ratio> proteinRatios) {
        this.proteinKey = proteinKey;
        this.ratios = proteinRatios;
        this.peptideQuantification.addAll(peptideQuantification);
    }

    /**
     * Getter for the corresponding peptide quantification
     * @return list of peptide quantification
     */
    public ArrayList<String> getPeptideQuantification() {
        return peptideQuantification;
    }

    /**
     * Adds a new peptide quantification in the protein quantification
     * @param newPeptideQuantification the new peptide quantification
     */
    public void addPeptideQuantification(String newPeptideQuantification) {
        peptideQuantification.add(newPeptideQuantification);
    }

    @Override
    public String getKey() {
        return proteinKey;
    }

    @Override
    public MatchType getType() {
        return MatchType.Protein;
    }
}
