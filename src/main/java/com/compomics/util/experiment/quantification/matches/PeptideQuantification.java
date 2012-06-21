package com.compomics.util.experiment.quantification.matches;

import com.compomics.util.experiment.quantification.Ratio;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.quantification.QuantificationMatch;
import java.util.ArrayList;

import java.util.HashMap;

/**
 * This class models quantification of a peptide.
 * @TODO: make it quantification method independent
 * 
 * @author Marc Vaudel
 */
public class PeptideQuantification extends QuantificationMatch {

    /**
     * The identification peptide match
     */
    private String peptideKey;

    /**
     * The corresponding spectrum quantification
     */
    private ArrayList<String> psmQuantification = new ArrayList<String>();

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
    public PeptideQuantification(String peptideKey, ArrayList<String> psmQuantification) {
        this.peptideKey = peptideKey;
        this.psmQuantification.addAll(psmQuantification);
    }

    /**
     * Constructor for the peptide quantification
     * @param peptideKey              the identification peptide match
     * @param psmQuantification         the corresponding spectrum quantification
     * @param ratios                    the estimated ratios
     */
    public PeptideQuantification(String peptideKey, ArrayList<String> psmQuantification, HashMap<Integer, Ratio> ratios) {
        this.peptideKey = peptideKey;
        this.psmQuantification.addAll(psmQuantification);
        this.ratios = ratios;
    }

    /**
     * Getter for the corresponding spectrum quantification
     * @return List of spectrum quantification
     */
    public ArrayList<String> getPsmQuantification() {
        return psmQuantification;
    }

    /**
     * Adds a new psm quantification in the psm quantification map
     * @param newPsmQuantification the new psm quantification
     */
    public void addPsmQuantification(String newPsmQuantification) {
        psmQuantification.add(newPsmQuantification);
    }

    @Override
    public String getKey() {
        return peptideKey;
    }

    @Override
    public MatchType getType() {
        return MatchType.Peptide;
    }
}
