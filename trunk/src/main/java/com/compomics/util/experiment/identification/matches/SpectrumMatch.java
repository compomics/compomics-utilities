package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class models a spectrum match.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:58:26 AM
 */
public class SpectrumMatch extends ExperimentObject {

    /**
     * The index of the matched spectrum
     */
    private String spectrumKey;
    /**
     * The corresponding peptide assumptions
     */
    private HashSet<PeptideAssumption> assumptions = new HashSet<PeptideAssumption>();
    /**
     * The best assumption
     */
    private PeptideAssumption bestAssumption;
    /**
     * Map containing the first hits indexed by the Advocate index
     */
    private HashMap<Integer, PeptideAssumption> firstHits = new HashMap<Integer, PeptideAssumption>();
    /**
     * All advocates used
     */
    private ArrayList<Integer> advocates = new ArrayList<Integer>();

    /**
     * Constructor for the spectrum match
     */
    public SpectrumMatch() {

    }

    /**
     * Constructor for the spectrum match
     *
     * @param spectrumKey   The matched spectrumKey
     * @param assumption    The matching peptide assumption
     */
    public SpectrumMatch(String spectrumKey, PeptideAssumption assumption) {
        int advocateId = assumption.getAdvocate();
        assumptions.add(assumption);
        firstHits.put(advocateId, assumption);
        advocates.add(advocateId);
        this.spectrumKey = spectrumKey;
    }

    /**
     * Constructor for the spectrum match
     *
     * @param spectrumKey The matched spectrum key
     */
    public SpectrumMatch(String spectrumKey) {
        this.spectrumKey = spectrumKey;
    }

    /**
     * Getter for the best assumption
     *
     * @return the best assumption for the spectrum
     */
    public PeptideAssumption getBestAssumption() {
        return bestAssumption;
    }

    /**
     * Setter for the best assumption
     *
     * @param bestAssumption the best assumption for the spectrum
     */
    public void setBestAssumption(PeptideAssumption bestAssumption) {
        this.bestAssumption = bestAssumption;
    }

    /**
     * Getter for the spectrum key
     *
     * @return the matched spectrum key
     */
    public String getKey() {
        return spectrumKey;
    }

    /**
     * Add a secondary hit
     *
     * @param secondaryHit a secondary hit
     */
    public void addSecondaryHit(PeptideAssumption secondaryHit) {
        assumptions.add(secondaryHit);
    }

    /**
     * Return all assumptions
     *
     * @return all assumptions
     */
    public HashSet<PeptideAssumption> getAllAssumptions() {
        return assumptions;
    }

    /**
     * add a first hit
     *
     * @param otherAdvocateId The index of the new advocate
     * @param otherAssumption The new peptide assumption
     * @throws Exception exception thrown when attempting to link two identifications from the same search engine on a single spectrum
     */
    public void addFirstHit(int otherAdvocateId, PeptideAssumption otherAssumption) throws Exception {
        if (firstHits.get(otherAdvocateId) != null) {
            if (!firstHits.get(otherAdvocateId).getPeptide().getKey().equals(otherAssumption.getPeptide().getKey())) {
                throw new Exception("Two identifications by the same search engine for a single spectrum");
            } else {
                return;
            }
        }
        firstHits.put(otherAdvocateId, otherAssumption);
        assumptions.add(otherAssumption);
        advocates.add(otherAdvocateId);
    }

    /**
     * Returns the first hit obtained using the specified advocate
     *
     * @param advocateId the specified advocate index
     * @return the first hit
     */
    public PeptideAssumption getFirstHit(int advocateId) {
        return firstHits.get(advocateId);
    }

    /**
     * Returns all advocates used referenced by their index
     *
     * @return all advocates used
     */
    public ArrayList<Integer> getAdvocates() {
        return advocates;
    }
}
