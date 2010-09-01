package com.compomics.util.experiment.quantification.msms;

import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.quantification.Ratio;
import com.compomics.util.experiment.quantification.msms.SpectrumQuantification;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * This class models quantification at the peptide level.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 3:49:22 PM
 */
public class PeptideQuantification {

    /**
     * the identification peptide match
     */
    private PeptideMatch peptideMatch;

    /**
     * The corresponding spectrum quantification
     */
    private ArrayList<SpectrumQuantification> spectrumQuantification = new ArrayList<SpectrumQuantification>();

    /**
     * The estimated ratios
     */
    private HashMap<Integer, Ratio> ratios = new HashMap<Integer, Ratio>();

    /**
     * Constructor for the peptide quantification
     * @param peptideMatch              the identification peptide match
     * @param spectrumQuantification    the corresponding spectrum quantification
     * @param ratios                    the estimated ratios
     */
    public PeptideQuantification(PeptideMatch peptideMatch, ArrayList<SpectrumQuantification> spectrumQuantification, HashMap<Integer, Ratio> ratios) {
        this.peptideMatch = peptideMatch;
        this.spectrumQuantification = spectrumQuantification;
        this.ratios = ratios;
    }

    /**
     * Getter for the peptide match
     * @return the peptide match
     */
    public PeptideMatch getPeptideMatch() {
        return peptideMatch;
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
    public ArrayList<SpectrumQuantification> getSpectrumQuantification() {
        return spectrumQuantification;
    }
}
