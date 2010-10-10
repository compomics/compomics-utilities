package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.utils.ExperimentObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class models a peptide match.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:58:46 AM
 */
public class PeptideMatch extends ExperimentObject {

    /**
     * the theoretic peptide mathing
     */
    private Peptide theoreticPeptide;
    /**
     * The main match, typically highest score
     */
    private SpectrumMatch mainMatch;
    /**
     * All spectrum matches indexed by spectrum id: FILE_TITLE
     */
    private HashMap<String, SpectrumMatch> spectrumMatches = new HashMap<String, SpectrumMatch>();
    /**
     * is the peptide match a decoy hit
     */
    private Boolean isDecoy = null;


    /**
     * constructor for the peptide match
     */
    public PeptideMatch() {

    }

    /**
     * Constructor for the peptide match
     *
     * @param peptide   the matching peptide
     */
    public PeptideMatch(Peptide peptide) {
        theoreticPeptide = peptide;
    }

    /**
     * Constructor for the peptide match
     *
     * @param peptide           The matching peptide
     * @param spectrumMatch     The main spectrum match
     */
    public PeptideMatch(Peptide peptide, SpectrumMatch spectrumMatch) {
        theoreticPeptide = peptide;
        mainMatch = spectrumMatch;
        String index = spectrumMatch.getSpectrum().getFileName() + "_" + spectrumMatch.getSpectrum().getSpectrumTitle();
        spectrumMatches.put(index, spectrumMatch);
    }

    /**
     * getter for the theoretic peptide
     *
     * @return the theoretic peptide
     */
    public Peptide getTheoreticPeptide() {
        return theoreticPeptide;
    }

    /**
     * setter for the theoretic peptide
     *
     * @param theoreticPeptide  a theoretic peptide
     */
    public void setTheoreticPeptide(Peptide theoreticPeptide) {
        this.theoreticPeptide = theoreticPeptide;
    }

    /**
     * method returns the main match
     *
     * @return the main match
     */
    public SpectrumMatch getMainMatch() {
        return mainMatch;
    }

    /**
     * methods sets the main match
     *
     * @param spectrumMatch the main match
     */
    public void setMainMatch(SpectrumMatch spectrumMatch) {
        this.mainMatch = spectrumMatch;
    }

    /**
     * methods which returns the main spectrum matched
     *
     * @return main spectrum matched
     */
    public MSnSpectrum getMainSpectrum() {
        return mainMatch.getSpectrum();
    }

    /**
     * returns all spectra matched
     *
     * @return all spectrum matches
     */
    public HashMap<String, SpectrumMatch> getSpectrumMatches() {
        return spectrumMatches;
    }

    /**
     * add a spectrum match
     *
     * @param spectrumMatch a spectrum match
     */
    public void addSpectrumMatch(SpectrumMatch spectrumMatch) throws Exception {
        String index = spectrumMatch.getSpectrum().getFileName() + "_" + spectrumMatch.getSpectrum().getSpectrumTitle();
        if (spectrumMatches.get(index) == null) {
            spectrumMatches.put(index, spectrumMatch);
        } else {
            for (int searchEngine : spectrumMatch.getAdvocates()) {
                spectrumMatches.get(index).addFirstHit(searchEngine, spectrumMatch.getFirstHit(searchEngine));
            }
        }
    }

    /**
     * add spectrum matches
     *
     * @param newMatches  matched spectra
     * @throws Exception  exception thrown when attempting to link two identifications from the same search engine on a single spectrum
     */
    public void addSpectrumMatches(HashMap<String, SpectrumMatch> newMatches) throws Exception {
        SpectrumMatch newMatch;
        for (String index : newMatches.keySet()) {
            newMatch = newMatches.get(index);
            if (spectrumMatches.get(index) == null) {
                spectrumMatches.put(index, newMatch);
            } else {
                 for (int searchEngine : newMatch.getAdvocates()) {
                     spectrumMatches.get(index).addFirstHit(searchEngine, newMatch.getFirstHit(searchEngine));
                 }
            }
        }
    }

    /**
     * returns the number of spectra matched
     *
     * @return spectrum count
     */
    public int getSpectrumCount() {
        return spectrumMatches.size();
    }

    /**
     * inspects wether the peptide match is a decoy hit
     * 
     * @return true if the peptide match is a decoy hit
     */
    public boolean isDecoy() {
        if (isDecoy == null) {
            for (SpectrumMatch spectrumMatch : spectrumMatches.values()) {
                ArrayList<Integer> advocates = spectrumMatch.getAdvocates();
                PeptideAssumption currentPeptideAssumption = spectrumMatch.getFirstHit(advocates.get(0));
                if (currentPeptideAssumption.getPeptide().isSameAs(theoreticPeptide)) {
                    isDecoy = currentPeptideAssumption.isDecoy();
                    return isDecoy;
                } else {
                    for (int i = 1; i < advocates.size(); i++) {
                        currentPeptideAssumption = spectrumMatch.getFirstHit(advocates.get(i));
                        if (currentPeptideAssumption.getPeptide().isSameAs(theoreticPeptide)) {
                            isDecoy = currentPeptideAssumption.isDecoy();
                            return isDecoy;
                        }
                    }
                }
            }
            isDecoy = true;
        }
        return isDecoy;
    }
}
