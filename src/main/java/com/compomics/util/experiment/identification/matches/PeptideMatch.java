package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch;
import java.util.ArrayList;

/**
 * This class models a peptide match.
 *
 * @author Marc Vaudel
 */
public class PeptideMatch extends IdentificationMatch {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 7195830246336841081L;
    /**
     * The theoretic peptide match.
     */
    private Peptide theoreticPeptide;
    /**
     * The key of the match.
     */
    private String matchKey;
    /**
     * The key of the main match, typically of the highest score.
     */
    private String mainMatchKey;
    /**
     * All spectrum matches indexed by spectrum id: FILE_TITLE.
     */
    private ArrayList<String> spectrumMatches = new ArrayList<String>();
    /**
     * Is the peptide match a decoy hit?
     */
    private Boolean isDecoy = null;

    /**
     * Constructor for the peptide match.
     */
    public PeptideMatch() {
    }

    @Override
    public String getKey() {
        if (matchKey == null) { // needed for backward compatibility
            return theoreticPeptide.getKey();
        }
        return matchKey;
    }

    /**
     * Constructor for the peptide match.
     *
     * @param peptide the matching peptide
     * @param matchKey the key of the match as referenced in the identification.
     */
    public PeptideMatch(Peptide peptide, String matchKey) {
        theoreticPeptide = peptide;
        this.matchKey = matchKey;
    }

    /**
     * Constructor for the peptide match.
     *
     * @param peptide The matching peptide
     * @param spectrumMatchKey The key of the main spectrum match
     * @param matchKey the key of the match as referenced in the identification.
     */
    public PeptideMatch(Peptide peptide, String spectrumMatchKey, String matchKey) {
        theoreticPeptide = peptide;
        mainMatchKey = spectrumMatchKey;
        spectrumMatches.add(spectrumMatchKey);
        this.matchKey = matchKey;
    }

    /**
     * Getter for the theoretic peptide.
     *
     * @return the theoretic peptide
     */
    public Peptide getTheoreticPeptide() {
        return theoreticPeptide;
    }

    /**
     * Setter for the theoretic peptide.
     *
     * @param theoreticPeptide a theoretic peptide
     */
    public void setTheoreticPeptide(Peptide theoreticPeptide) {
        this.theoreticPeptide = theoreticPeptide;
    }

    /**
     * Returns the key of the main match.
     *
     * @return the main match key
     */
    public String getMainMatchKey() {
        return mainMatchKey;
    }

    /**
     * Sets the main match.
     *
     * @param spectrumMatchKey the key of the main match
     */
    public void setMainMatch(String spectrumMatchKey) {
        this.mainMatchKey = spectrumMatchKey;
    }

    /**
     * Returns all spectra matched.
     *
     * @return all spectrum matches
     */
    public ArrayList<String> getSpectrumMatches() {
        return spectrumMatches;
    }

    /**
     * Add a spectrum match.
     *
     * @param spectrumMatchKey a spectrum match
     */
    public void addSpectrumMatch(String spectrumMatchKey) {
        if (!spectrumMatches.contains(spectrumMatchKey)) {
            spectrumMatches.add(spectrumMatchKey);
        } else {
            throw new IllegalArgumentException("Trying to add two times the same spectrum match (" + spectrumMatchKey + ") to the same peptide match (" + getKey() + ").");
        }
    }

    /**
     * Returns the number of spectra matched.
     *
     * @return spectrum count
     */
    public int getSpectrumCount() {
        return spectrumMatches.size();
    }

    @Override
    public MatchType getType() {
        return MatchType.Peptide;
    }
}
