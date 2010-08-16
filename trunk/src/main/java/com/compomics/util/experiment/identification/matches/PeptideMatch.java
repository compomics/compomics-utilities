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

    // Attributes

    private Peptide theoreticPeptide;
    private SpectrumMatch mainMatch;
    private ArrayList<SpectrumMatch> spectrumMatches = new ArrayList<SpectrumMatch>();
    private Boolean isDecoy = null;


    // Constructor

    public PeptideMatch() {

    }

    public PeptideMatch(Peptide peptide) {
        theoreticPeptide = peptide;
    }

    public PeptideMatch(Peptide peptide, SpectrumMatch spectrumMatch) {
        theoreticPeptide = peptide;
        mainMatch = spectrumMatch;
        spectrumMatches.add(spectrumMatch);
    }


    // Methods

    public Peptide getTheoreticPeptide() {
        return theoreticPeptide;
    }

    public void setTheoreticPeptide(Peptide theoreticPeptide) {
        this.theoreticPeptide = theoreticPeptide;
    }

    public SpectrumMatch getMainMatch() {
        return mainMatch;
    }

    public void setMainMatch(SpectrumMatch spectrumMatch) {
        this.mainMatch = spectrumMatch;
    }

    public MSnSpectrum getMainSpectrum() {
        return mainMatch.getSpectrum();
    }

    public ArrayList<SpectrumMatch> getSpectrumMatches() {
        return spectrumMatches;
    }

    public void addSpectrumMatch(SpectrumMatch spectrumMatch) {
        spectrumMatches.add(spectrumMatch);
    }

    public int getSpectrumCount() {
        return spectrumMatches.size();
    }

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
