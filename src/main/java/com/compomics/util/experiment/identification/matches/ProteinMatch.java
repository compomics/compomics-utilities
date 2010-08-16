package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Protein;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:59:02 AM
 * This class modelizes a protein match.
 */
public class ProteinMatch {

    // Attributes

    private Protein theoreticProtein;

    private ArrayList<PeptideMatch> peptideMatches = new ArrayList<PeptideMatch>();
    private Boolean isDecoy = null;


    // Constructor

    public ProteinMatch() {

    }

    public ProteinMatch(Protein protein) {
        theoreticProtein = protein;
    }

    public ProteinMatch(Protein protein, PeptideMatch peptideMatch) {
        theoreticProtein = protein;
        peptideMatches.add(peptideMatch);
    }


    // Methods

    public Protein getTheoreticProtein() {
        return theoreticProtein;
    }

    public void setTheoreticProtein(Protein theoreticProtein) {
        this.theoreticProtein = theoreticProtein;
    }

    public ArrayList<PeptideMatch> getPeptideMatches() {
        return peptideMatches;
    }

    public void addPeptideMatch(PeptideMatch peptideMatch) {
        peptideMatches.add(peptideMatch);
    }

    public int getSpectrumCount() {
        int spectrumCount = 0;
        for (PeptideMatch peptideMatch : peptideMatches) {
            spectrumCount += peptideMatch.getSpectrumCount();
        }
        return spectrumCount;
    }

    public boolean isDecoy() {
        if (isDecoy == null) {
            isDecoy = peptideMatches.get(0).isDecoy();
        }
        return isDecoy;
    }
}
