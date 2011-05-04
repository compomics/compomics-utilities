package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.HashMap;

/**
 * This class models a peptide match.
 * <p/>
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
     * Convenience method to index peptideMatches on the peptide id
     *
     * @return the peptideMatch id
     */
    public String getKey() {
        return theoreticPeptide.getKey();
    }

    /**
     * Constructor for the peptide match
     *
     * @param peptide the matching peptide
     */
    public PeptideMatch(Peptide peptide) {
        theoreticPeptide = peptide;
    }

    /**
     * Constructor for the peptide match
     *
     * @param peptide       The matching peptide
     * @param spectrumMatch The main spectrum match
     */
    public PeptideMatch(Peptide peptide, SpectrumMatch spectrumMatch) {
        theoreticPeptide = peptide;
        mainMatch = spectrumMatch;
        String index = spectrumMatch.getKey();
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
     * @param theoreticPeptide a theoretic peptide
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
     * methods which returns the key of the main spectrum matched
     *
     * @return key of the main spectrum matched
     */
    public String getMainSpectrumKey() {
        return mainMatch.getKey();
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
        String index = spectrumMatch.getKey();
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
     * @param newMatches matched spectra
     * @throws Exception exception thrown when attempting to link two identifications from the same search engine on a single spectrum
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
        int result = 0;
        for (SpectrumMatch spectrumMatch : spectrumMatches.values()) {
            if (spectrumMatch.getBestAssumption().getPeptide().isSameAs(theoreticPeptide)) {
                result++;
            }
        }
        return result;
    }

    /**
     * inspects whether the peptide match is a decoy hit
     *
     * @return true if the peptide match is a decoy hit
     */
    public boolean isDecoy() {
        if (isDecoy == null) {
            for (Protein protein : theoreticPeptide.getParentProteins()) {
                if (!protein.isDecoy()) {
                    isDecoy = false;
                    return isDecoy;
                }
            }
            isDecoy = true;
        }
        return isDecoy;
    }
}
