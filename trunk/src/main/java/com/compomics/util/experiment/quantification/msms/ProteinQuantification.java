package com.compomics.util.experiment.quantification.msms;

import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.utils.ExperimentObject;
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
     * The identification protein match
     */
    private ProteinMatch proteinMatch;

    /**
     * The peptide quantification corresponding
     */
    private ArrayList<PeptideQuantification> peptideQuantification = new ArrayList<PeptideQuantification>();

    /**
     * The estimated protein ratios
     */
    private HashMap<Integer, Ratio> proteinRatios = new HashMap<Integer, Ratio>();

    /**
     * Constructor for the protein quantification
     * @param proteinMatch              the identified protein match
     * @param proteinRatios             the estimated protein ratios
     * @param peptideQuantification     the corresponding peptide quantification
     */
    public ProteinQuantification(ProteinMatch proteinMatch, ArrayList<PeptideQuantification> peptideQuantification, HashMap<Integer, Ratio> proteinRatios) {
        this.proteinMatch = proteinMatch;
        this.proteinRatios = proteinRatios;
        this.peptideQuantification = peptideQuantification;
    }

    /**
     * Getter for the protein match
     * @return the protein match
     */
    public ProteinMatch getProteinMatch() {
        return proteinMatch;
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
    public ArrayList<PeptideQuantification> getPeptideQuantification() {
        return peptideQuantification;
    }
}
