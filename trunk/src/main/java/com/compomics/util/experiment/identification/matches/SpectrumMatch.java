package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:58:26 AM
 * This class modelizes a spectrum match.
 */
public class SpectrumMatch {

    // Attributes
    // Map indexes are the corresponding advocate index
    private MSnSpectrum spectrum;
    private HashSet<PeptideAssumption> assumptions = new HashSet<PeptideAssumption>();
    private HashMap<Integer, PeptideAssumption> firstHits = new HashMap<Integer, PeptideAssumption>();
    private ArrayList<Integer> advocates = new ArrayList<Integer>();


    // Constructor

    public SpectrumMatch() {

    }

    public SpectrumMatch(MSnSpectrum spectrum, PeptideAssumption assumption) {
        int advocateId  = assumption.getAdvocate();
        assumptions.add(assumption);
        firstHits.put(advocateId, assumption);
        advocates.add(advocateId);
        this.spectrum = spectrum;
    }


    // Methods

   public MSnSpectrum getSpectrum() {
       return spectrum;
   }

    public void addSecondaryHit(PeptideAssumption secondaryHit) {
        assumptions.add(secondaryHit);
    }

    public HashSet<PeptideAssumption> getAllAssumptions() {
        return assumptions;
    }

    public void addFirstHit(int otherAdvocateId, PeptideAssumption otherAssumption) {
        firstHits.put(otherAdvocateId, otherAssumption);
        assumptions.add(otherAssumption);
        advocates.add(otherAdvocateId);
    }

    public PeptideAssumption getFirstHit(int advocateId) {
        return firstHits.get(advocateId);
    }

    public ArrayList<Integer> getAdvocates() {
        return advocates;
    }
}
