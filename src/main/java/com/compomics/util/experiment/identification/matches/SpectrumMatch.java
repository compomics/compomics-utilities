package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.PeptideAssumption;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class models a spectrum match.
 * <p/>
 * @author Marc Vaudel
 */
public class SpectrumMatch extends IdentificationMatch {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 3227760855215444318L;
    /**
     * The index of the matched spectrum.
     */
    private String spectrumKey;
    /**
     * The corresponding peptide assumptions indexed by search engine and
     * e-value.
     */
    private HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>> assumptions = new HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>>();
    /**
     * The best assumption.
     */
    private PeptideAssumption bestAssumption;
    /**
     * Map containing the first hits indexed by the Advocate index.
     */
    private HashMap<Integer, PeptideAssumption> firstHits = new HashMap<Integer, PeptideAssumption>();
    /**
     * All advocates used.
     */
    private ArrayList<Integer> advocates = new ArrayList<Integer>();
    /**
     * The spectrum number in the mgf file. Will be used in case the spectrum title does not match
     */
    private Integer spectrumNumber = null;

    /**
     * Constructor for the spectrum match.
     */
    public SpectrumMatch() {
    }

    /**
     * Constructor for the spectrum match.
     *
     * @param spectrumKey The matched spectrumKey
     * @param assumption The matching peptide assumption
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
     * Constructor for the spectrum match.
     *
     * @param spectrumKey The matched spectrum key
     */
    public SpectrumMatch(String spectrumKey) {
        this.spectrumKey = spectrumKey;
    }

    /**
     * Getter for the best assumption.
     *
     * @return the best assumption for the spectrum
     */
    public PeptideAssumption getBestAssumption() {
        return bestAssumption;
    }

    /**
     * Setter for the best assumption.
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
     * Return all assumptions for the specified search engine indexed by their
     * e-value.
     *
     * @param advocateId the desired advocate ID
     * @return all assumptions
     */
    public HashMap<Double, ArrayList<PeptideAssumption>> getAllAssumptions(int advocateId) {
        return assumptions.get(advocateId);
    }

    /**
     * Return all assumptions for all search engines as a list.
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
     * Add a first hit.
     *
     * @param otherAdvocateId The index of the new advocate
     * @param otherAssumption The new peptide assumption
     */
    public void addHit(int otherAdvocateId, PeptideAssumption otherAssumption) {
        // Uniformize the protein inference between search engines and ranks
        Peptide loadedPeptide, newPeptide = otherAssumption.getPeptide();
        for (PeptideAssumption loadedAssumption : getAllAssumptions()) {
            if (loadedAssumption.getPeptide().getSequence().equals(newPeptide.getSequence())) {
                loadedPeptide = loadedAssumption.getPeptide();
                for (String protein : loadedPeptide.getParentProteins()) {
                    if (!newPeptide.getParentProteins().contains(protein)) {
                        newPeptide.getParentProteins().add(protein);
                    }
                }
                for (String protein : newPeptide.getParentProteins()) {
                    if (!loadedPeptide.getParentProteins().contains(protein)) {
                        loadedPeptide.getParentProteins().add(protein);
                    }
                }
            }
        }
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
     * Returns the first hit obtained using the specified advocate.
     *
     * @param advocateId the specified advocate index
     * @return the first hit
     */
    public PeptideAssumption getFirstHit(int advocateId) {
        return firstHits.get(advocateId);
    }

    /**
     * Sets the best peptideAssumption according to the search engine.
     *
     * @param advocateId the search engine index
     * @param peptideAssumption the best assumption
     */
    public void setFirstHit(int advocateId, PeptideAssumption peptideAssumption) {
        firstHits.put(advocateId, peptideAssumption);
    }

    /**
     * Returns all advocates used referenced by their index.
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

    /**
     * Replaces the new key. The key of the PSM should always be the same as the
     * spectrum key it links to.
     *
     * @param newKey the new key
     */
    public void setKey(String newKey) {
        this.spectrumKey = newKey;
    }

    /**
     * Returns the spectrum number in the spectrum file. Returns null if not implemented (for versions older than 3.4.17 for instance)
     * @return the spectrum number in the spectrum file
     */
    public Integer getSpectrumNumber() {
        return spectrumNumber;
    }

    /**
     * Sets the spectrum number in the spectrum file
     * @param spectrumNumber the spectrum number in the spectrum file
     */
    public void setSpectrumNumber(Integer spectrumNumber) {
        this.spectrumNumber = spectrumNumber;
    }
    
    /**
     * Removes an assumption from the mapping
     * @param peptideAssumption the peptide assumption to remove
     */
    public void removeAssumption(PeptideAssumption peptideAssumption) {
        ArrayList<Integer> seToRemove = new ArrayList<Integer>();
        for (int se : assumptions.keySet()) {
            ArrayList<Double> eValueToRemove = new ArrayList<Double>();
            for (double eValue : assumptions.keySet()) {
                assumptions.get(se).get(eValue).remove(peptideAssumption);
                if (assumptions.get(se).get(eValue).isEmpty()) {
                    eValueToRemove.add(eValue);
                }
            }
            for (double eValue : eValueToRemove) {
                assumptions.get(se).remove(eValue);
            }
            if (assumptions.get(se).isEmpty()) {
                seToRemove.add(se);
            }
        }
        for (int se : seToRemove) {
            assumptions.remove(se);
        }
    }
}
