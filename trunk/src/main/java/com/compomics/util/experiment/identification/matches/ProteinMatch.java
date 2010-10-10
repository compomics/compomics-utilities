package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.utils.ExperimentObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class models a protein match.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:59:02 AM
 */
public class ProteinMatch extends ExperimentObject {

    /**
     * The matching protein
     */
    private Protein theoreticProtein;
    /**
     * The corresponding peptide matches
     */
    private HashMap<String, PeptideMatch> peptideMatches = new HashMap<String, PeptideMatch>();
    /**
     * is the match decoy?
     */
    private Boolean isDecoy = null;


    /**
     * Constructor for the protein match
     */
    public ProteinMatch() {

    }

    /**
     * Constructor for the protein match
     *
     * @param protein   the matching protein
     */
    public ProteinMatch(Protein protein) {
        theoreticProtein = protein;
    }

    /**
     * Constructor for the protein match
     *
     * @param protein       The matching protein
     * @param peptideMatch  The corresponding peptide matches
     */
    public ProteinMatch(Protein protein, PeptideMatch peptideMatch) {
        theoreticProtein = protein;
        peptideMatches.put(peptideMatch.getTheoreticPeptide().getSequence(), peptideMatch);
    }

    /**
     * getter for the matching protein
     *
     * @return the matching protein
     */
    public Protein getTheoreticProtein() {
        return theoreticProtein;
    }

    /**
     * setter for the matching protein
     *
     * @param theoreticProtein  the matching protein
     */
    public void setTheoreticProtein(Protein theoreticProtein) {
        this.theoreticProtein = theoreticProtein;
    }

    /**
     * getter for the peptide matches
     *
     * @return subordinated peptide matches
     */
    public HashMap<String, PeptideMatch> getPeptideMatches() {
        return peptideMatches;
    }

    /**
     * add a subordinated peptide match
     *
     * @param peptideMatch  a peptide match
     * @throws Exception  exception thrown when attempting to link two identifications from the same search engine on a single spectrum
     */
    public void addPeptideMatch(PeptideMatch peptideMatch) throws Exception {
        String sequence = peptideMatch.getTheoreticPeptide().getSequence();
        if (peptideMatches.get(sequence) == null) {
            peptideMatches.put(sequence, peptideMatch);
        } else {
            peptideMatches.get(sequence).addSpectrumMatches(peptideMatch.getSpectrumMatches());
        }
    }

    /**
     * method to get the spectrum count for this protein match
     *
     * @return the spectrum count for this protein match
     */
    public int getSpectrumCount() {
        int spectrumCount = 0;
        for (PeptideMatch peptideMatch : peptideMatches.values()) {
            spectrumCount += peptideMatch.getSpectrumCount();
        }
        return spectrumCount;
    }

    /**
     * methods indicates if the protein match is a decoy one
     * 
     * @return boolean indicating if the protein match is a decoy one
     */
    public boolean isDecoy() {
        if (isDecoy == null) {
            isDecoy = peptideMatches.get(0).isDecoy();
        }
        return isDecoy;
    }
}
