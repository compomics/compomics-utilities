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
     * The version UID for serialization/deserialization compatibility.
     */
    static final long serialVersionUID = 7195830246336841081L;
    /**
     * The theoretic peptide match.
     */
    private Peptide theoreticPeptide;
    /**
     * The key of the match.
     */
    private String key;
    /**
     * All spectrum matches indexed by spectrum id. See Spectrum class.
     */
    private ArrayList<String> spectrumMatchesKeys = new ArrayList<String>();
    /**
     * Is the peptide match a decoy hit?
     */
    private boolean isDecoy = false;
    

    /**
     * Constructor for the peptide match.
     */
    public PeptideMatch() {
        System.out.println("naked");
    }

    @Override
    public String getKey() {
        return key;
    }
    
    /**
     * Sets a new key for the match.
     * 
     * @param newKey a new key for the match
     */
    public void setKey(String newKey) {
        this.key = newKey;
        setModified(true);
    }

    /**
     * Constructor for the peptide match.
     *
     * @param peptide the matching peptide
     * @param matchKey the key of the match as referenced in the identification
     */
    public PeptideMatch(Peptide peptide, String matchKey) {
        theoreticPeptide = peptide;
        this.key = matchKey;
        System.out.println("closed");
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
        setModified(true);
    }

    /**
     * Returns the keys of all spectra matched.
     *
     * @return the keys of all spectrum matches
     */
    public ArrayList<String> getSpectrumMatchesKeys() {
        return spectrumMatchesKeys;
    }

    /**
     * Add a spectrum match key.
     *
     * @param spectrumMatchKey the key of a spectrum match
     */
    public void addSpectrumMatchKey(String spectrumMatchKey) {
        if (!spectrumMatchesKeys.contains(spectrumMatchKey)) {
            spectrumMatchesKeys.add(spectrumMatchKey);
            setModified(true);
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
        return spectrumMatchesKeys.size();
    }

    @Override
    public MatchType getType() {
        return MatchType.Peptide;
    }
}
