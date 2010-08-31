package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:58:46 AM
 * This class modelizes a peptide match.
 */
public class PeptideMatch {

    /**
     * the theoretic peptide mathing
     */
    private Peptide theoreticPeptide;
    /**
     * The main match, typically highest score
     */
    private SpectrumMatch mainMatch;
    /**
     * All spectrum matches
     */
    private ArrayList<SpectrumMatch> spectrumMatches = new ArrayList<SpectrumMatch>();
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
     * @param peptide   the matching peptide
     */
    public PeptideMatch(Peptide peptide) {
        theoreticPeptide = peptide;
    }

    /**
     * Constructor for the peptide match
     * @param peptide           The matching peptide
     * @param spectrumMatch     The main spectrum match
     */
    public PeptideMatch(Peptide peptide, SpectrumMatch spectrumMatch) {
        theoreticPeptide = peptide;
        mainMatch = spectrumMatch;
        spectrumMatches.add(spectrumMatch);
    }


    /**
     * getter for the theoretic peptide
     * @return the theoretic peptide
     */
    public Peptide getTheoreticPeptide() {
        return theoreticPeptide;
    }

    /**
     * setter for the theoretic peptide
     * @param theoreticPeptide  a theoretic peptide
     */
    public void setTheoreticPeptide(Peptide theoreticPeptide) {
        this.theoreticPeptide = theoreticPeptide;
    }

    /**
     * method returns the main match
     * @return the main match
     */
    public SpectrumMatch getMainMatch() {
        return mainMatch;
    }

    /**
     * methods sets the main match
     * @param spectrumMatch the main match
     */
    public void setMainMatch(SpectrumMatch spectrumMatch) {
        this.mainMatch = spectrumMatch;
    }

    /**
     * methods which returns the main spectrum matched
     * @return main spectrum matched
     */
    public MSnSpectrum getMainSpectrum() {
        return mainMatch.getSpectrum();
    }

    /**
     * methods which returns all spectrum matched
     * @return all spectrum matches
     */
    public ArrayList<SpectrumMatch> getSpectrumMatches() {
        return spectrumMatches;
    }

    /**
     * add a spectrum match
     * @param spectrumMatch a spectrum match
     */
    public void addSpectrumMatch(SpectrumMatch spectrumMatch) {
        spectrumMatches.add(spectrumMatch);
    }

    /**
     * returns the number of spectra matched
     * @return spectrum count
     */
    public int getSpectrumCount() {
        return spectrumMatches.size();
    }

    /**
     * inspects wether the peptide match is a decoy hit
     * @return
     */
    public boolean isDecoy() {
        if (isDecoy == null) {
            for (SpectrumMatch spectrumMatch : spectrumMatches) {
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
