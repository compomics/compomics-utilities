package com.compomics.util.experiment.identification.matches;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * This class models a spectrum match.
 *
 * @author Marc Vaudel
 */
public class SpectrumMatch extends IdentificationMatch {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 3227760855215444318L;
    /**
     * The key of the spectrum.
     */
    private String spectrumKey;
    /**
     * Map of the identification algorithm assumption: advocate number &gt;
     * score &gt; assumptions.
     */
    private HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptionsMap = null;
    /**
     * The best peptide assumption.
     */
    private PeptideAssumption bestPeptideAssumption;
    /**
     * The best tag assumption.
     */
    private TagAssumption bestTagAssumption;

    /**
     * Constructor for the spectrum match.
     */
    public SpectrumMatch() {
    }

    public void setTagAssumptionMapKeySize(int tagAssumptionsMapKeySize) {
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
    }

    /**
     * Constructor for the spectrum match.
     *
     * @param spectrumKey the key of the spectrum
     * @param assumption the matching assumption
     */
    public SpectrumMatch(String spectrumKey, SpectrumIdentificationAssumption assumption) {
        int advocateId = assumption.getAdvocate();
        assumptionsMap = new HashMap<>(1);
        assumptionsMap.put(advocateId, new HashMap<>(1));
        ArrayList<SpectrumIdentificationAssumption> assumptionsList = new ArrayList<>(1);
        assumptionsList.add(assumption);
        assumptionsMap.get(advocateId).put(assumption.getScore(), assumptionsList);

        this.spectrumKey = spectrumKey;
    }

    /**
     * Sets the assumption map.
     *
     * @param assumptionsMap the assumption map
     */
    public void setAssumptionMap(HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptionsMap) {
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        this.assumptionsMap = assumptionsMap;
    }

    /**
     * Constructor for the spectrum match.
     *
     * @param spectrumKey the key of the spectrum
     */
    public SpectrumMatch(String spectrumKey) {
        this.spectrumKey = spectrumKey;
        assumptionsMap = new HashMap<>(1);
    }

    /**
     * Getter for the best peptide assumption.
     *
     * @return the best peptide assumption for the spectrum
     */
    public PeptideAssumption getBestPeptideAssumption() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return bestPeptideAssumption;
    }

    /**
     * Setter for the best peptide assumption.
     *
     * @param bestPeptideAssumption the best peptide assumption for the spectrum
     */
    public void setBestPeptideAssumption(PeptideAssumption bestPeptideAssumption) {
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        this.bestPeptideAssumption = bestPeptideAssumption;
    }

    /**
     * Getter for the best tag assumption.
     *
     * @return the best tag assumption for the spectrum
     */
    public TagAssumption getBestTagAssumption() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return bestTagAssumption;
    }

    /**
     * Setter for the best tag assumption.
     *
     * @param bestTagAssumption the best tag assumption for the spectrum
     */
    public void setBestTagAssumption(TagAssumption bestTagAssumption) {
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        this.bestTagAssumption = bestTagAssumption;
    }

    @Override
    public String getKey() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return spectrumKey;
    }

    /**
     * Returns all assumptions for the specified search engine indexed by their
     * score. Null if none found.
     *
     * @param advocateId the desired advocate ID
     *
     * @return all assumptions
     */
    public HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> getAllAssumptions(int advocateId) {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return assumptionsMap.get(advocateId);
    }

    /**
     * Returns a stream of all assumptions
     *
     * @return all assumptions
     */
    public Stream<SpectrumIdentificationAssumption> getAllAssumptions() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return assumptionsMap.values().stream().flatMap(algorithmMap -> algorithmMap.values().stream()).flatMap(assumptionsList -> assumptionsList.stream());
    }

    /**
     * Returns the assumptions map: advocate id &gt; score &gt; list of
     * assumptions.
     *
     * @return the assumptions map
     */
    public HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> getAssumptionsMap() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return assumptionsMap;
    }

    /**
     * Add a hit.
     *
     * @param otherAdvocateId the index of the new advocate
     * @param otherAssumption the new identification assumption
     */
    public void addHit(int otherAdvocateId, SpectrumIdentificationAssumption otherAssumption) {
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> advocateMap = assumptionsMap.get(otherAdvocateId);
        if (advocateMap == null) {
            advocateMap = new HashMap<>(1);
            assumptionsMap.put(otherAdvocateId, advocateMap);
        }
        Double score = otherAssumption.getScore();
        ArrayList<SpectrumIdentificationAssumption> assumptionList = advocateMap.get(score);
        if (assumptionList == null) {
            assumptionList = new ArrayList<>(1);
            advocateMap.put(score, assumptionList);
        }
        assumptionList.add(otherAssumption);
    }

    @Override
    public MatchType getType() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return MatchType.Spectrum;
    }

    /**
     * Sets the match key. The key of the PSM should always be the same as the
     * spectrum key it links to.
     *
     * @param spectrumKey the key of the spectrum match
     */
    public void setSpectrumKey(String spectrumKey) {
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        this.spectrumKey = spectrumKey;
    }

    /**
     * Removes an assumption from the mapping.
     *
     * @param assumption the peptide assumption to remove
     */
    public void removeAssumption(SpectrumIdentificationAssumption assumption) {
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        int se = assumption.getAdvocate();
        HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> algorithmMap = assumptionsMap.get(se);
        ArrayList<SpectrumIdentificationAssumption> assumptionsList = algorithmMap.get(assumption.getScore());
        assumptionsList.remove(assumption);
        if (assumptionsList.isEmpty()) {
            algorithmMap.remove(assumption.getScore());
        }
        if (algorithmMap.isEmpty()) {
            assumptionsMap.remove(se);
        }
    }

    /**
     * Indicates whether the spectrum match contains a spectrum assumption.
     *
     * @return a boolean indicating whether the spectrum match contains an
     * assumption
     */
    public boolean hasAssumption() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return assumptionsMap.values().stream().flatMap(algorithmMap -> algorithmMap.values().stream())
                .anyMatch(assumptionsList -> !assumptionsList.isEmpty());
    }

    /**
     * Indicates whether the spectrum match contains a peptide assumption for
     * the given advocate (see the Advocate class).
     *
     * @param advocateId The index of the advocate
     *
     * @return a boolean indicating whether the spectrum match contains an
     * assumption for the given advocate
     */
    public boolean hasAssumption(int advocateId) {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> algorithmIds = assumptionsMap.get(advocateId);

        return algorithmIds == null ? false : algorithmIds.values().stream().anyMatch(assumptions -> !assumptions.isEmpty());
    }
}
