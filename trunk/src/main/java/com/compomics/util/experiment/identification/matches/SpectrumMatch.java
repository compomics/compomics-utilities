package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.PeptideAssumption;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class models a spectrum match.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:58:26 AM
 */
public class SpectrumMatch extends IdentificationMatch {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 3227760855215444318L;
    /**
     * The index of the matched spectrum
     */
    private String spectrumKey;
    /**
     * The corresponding peptide assumptions indexed by search engine and e-value.
     */
    private HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>> assumptions = new HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>>();
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
        assumptions.put(advocateId, new HashMap<Double, ArrayList<PeptideAssumption>>());
        assumptions.get(advocateId).put(assumption.getEValue(), new ArrayList<PeptideAssumption>());
        assumptions.get(advocateId).get(assumption.getEValue()).add(assumption);
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

    @Override
    public String getKey() {
        return spectrumKey;
    }

    /**
     * Return all assumptions for the specified search engine indexed by their e-value
     *
     * @param  advocateId the desired advocate ID
     * @return all assumptions
     */
    public HashMap<Double, ArrayList<PeptideAssumption>> getAllAssumptions(int advocateId) {
        return assumptions.get(advocateId);
    }
 
    /**
     * Return all assumptions for all search engines as a list
     *
     * @return all assumptions
     */
    public ArrayList<PeptideAssumption> getAllAssumptions() {
        ArrayList<PeptideAssumption> result = new ArrayList<PeptideAssumption>();
        for (HashMap<Double, ArrayList<PeptideAssumption>> seMap : assumptions.values()) {
            for (double eValue : seMap.keySet()) {
            result.addAll(seMap.get(eValue));
            }
        }
        return result;
    }

    /**
     * add a first hit
     *
     * @param otherAdvocateId The index of the new advocate
     * @param otherAssumption The new peptide assumption
     */
    public void addHit(int otherAdvocateId, PeptideAssumption otherAssumption) {
        if (!firstHits.containsKey(otherAdvocateId) || firstHits.get(otherAdvocateId).getEValue() > otherAssumption.getEValue()) {
            firstHits.put(otherAdvocateId, otherAssumption);
        }
        if (!assumptions.containsKey(otherAdvocateId)) {
            assumptions.put(otherAdvocateId, new HashMap<Double, ArrayList<PeptideAssumption>>());
        }
        if (!assumptions.get(otherAdvocateId).containsKey(otherAssumption.getEValue())) {
            assumptions.get(otherAdvocateId).put(otherAssumption.getEValue(), new ArrayList<PeptideAssumption>());
        }
        assumptions.get(otherAdvocateId).get(otherAssumption.getEValue()).add(otherAssumption);
        if (!advocates.contains(otherAdvocateId)) {
            advocates.add(otherAdvocateId);
        }
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
     * Sets the best peptideAssumption according to the search engine
     * 
     * @param advocateId        the search engine index
     * @param peptideAssumption the best assumption
     */
    public void setFirstHit(int advocateId, PeptideAssumption peptideAssumption) {
        firstHits.put(advocateId, peptideAssumption);
    }

    /**
     * Returns all advocates used referenced by their index
     *
     * @return all advocates used
     */
    public ArrayList<Integer> getAdvocates() {
        return advocates;
    }

    @Override
    public MatchType getType() {
        return MatchType.Spectrum;
    }
}
