package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.utils.ExperimentObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class models a spectrum match.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:58:26 AM
 */
public class SpectrumMatch extends ExperimentObject {

    /**
     * The matched spectrum
     */
    private MSnSpectrum spectrum;
    /**
     * The corresponding peptide assumptions
     */
    private HashSet<PeptideAssumption> assumptions = new HashSet<PeptideAssumption>();
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
     * @param spectrum      The matched spectrum
     * @param assumption    The matching peptide assumption
     */
    public SpectrumMatch(MSnSpectrum spectrum, PeptideAssumption assumption) {
        int advocateId  = assumption.getAdvocate();
        assumptions.add(assumption);
        firstHits.put(advocateId, assumption);
        advocates.add(advocateId);
        this.spectrum = spectrum;
    }

    /**
     * Getter for the spectrum
     *
     * @return the matched spectrum
     */
   public MSnSpectrum getSpectrum() {
       return spectrum;
   }

    /**
     * Add a secondary hit
     *
     * @param secondaryHit  a secondary hit
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
     * @param otherAdvocateId   The index of the new advocate
     * @param otherAssumption   The new peptide assumption
     * @throws Exception  exception thrown when attempting to link two identifications from the same search engine on a single spectrum
     */
    public void addFirstHit(int otherAdvocateId, PeptideAssumption otherAssumption) throws Exception {
        if (firstHits.get(otherAdvocateId) !=  null) {
            throw new Exception("Two identifications by the same search engine for a single spectrum");
        }
        firstHits.put(otherAdvocateId, otherAssumption);
        assumptions.add(otherAssumption);
        advocates.add(otherAdvocateId);
    }

    /**
     * Returns the first hit obtained using the specified advocate
     *
     * @param advocateId    the specified advocate index
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
