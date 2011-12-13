package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.SequenceFactory;
import java.util.ArrayList;

/**
 * This class models a peptide match.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:58:46 AM
 */
public class PeptideMatch extends IdentificationMatch {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 7195830246336841081L;
    /**
     * the theoretic peptide mathing
     */
    private Peptide theoreticPeptide;
    /**
     * The key of the main match, typically of the highest score
     */
    private String mainMatchKey;
    /**
     * All spectrum matches indexed by spectrum id: FILE_TITLE
     */
    private ArrayList<String> spectrumMatches = new ArrayList<String>();
    /**
     * is the peptide match a decoy hit
     */
    private Boolean isDecoy = null;

    /**
     * constructor for the peptide match
     */
    public PeptideMatch() {
    }

    @Override
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
     * @param peptide          The matching peptide
     * @param spectrumMatchKey The key of the main spectrum match
     */
    public PeptideMatch(Peptide peptide, String spectrumMatchKey) {
        theoreticPeptide = peptide;
        mainMatchKey = spectrumMatchKey;
        spectrumMatches.add(spectrumMatchKey);
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
     * method returns the key of the main match
     *
     * @return the main match key
     */
    public String getMainMatchKey() {
        return mainMatchKey;
    }

    /**
     * methods sets the main match
     *
     * @param spectrumMatchKey the key of the main match
     */
    public void setMainMatch(String spectrumMatchKey) {
        this.mainMatchKey = spectrumMatchKey;
    }

    /**
     * returns all spectra matched
     *
     * @return all spectrum matches
     */
    public ArrayList<String> getSpectrumMatches() {
        return spectrumMatches;
    }

    /**
     * add a spectrum match
     *
     * @param spectrumMatchKey  a spectrum match
     */
    public void addSpectrumMatch(String spectrumMatchKey) {
        if (!spectrumMatches.contains(spectrumMatchKey)) {
        spectrumMatches.add(spectrumMatchKey);
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
     * inspects whether the peptide match is a decoy hit
     *
     * @return true if the peptide match is a decoy hit
     */
    public boolean isDecoy() {
        if (isDecoy == null) {
            for (String protein : theoreticPeptide.getParentProteins()) {
                if (!SequenceFactory.isDecoy(protein)) {
                    isDecoy = false;
                    return isDecoy;
                }
            }
            isDecoy = true;
        }
        return isDecoy;
    }
}
