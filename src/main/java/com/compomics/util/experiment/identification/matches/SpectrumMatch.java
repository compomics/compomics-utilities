package com.compomics.util.experiment.identification.matches;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
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
     * The number of the spectrum.
     */
    private int spectrumNumber;
    /**
     * Map of the identification algorithm peptide assumptions: advocate number &gt;
     * score &gt; assumptions.
     */
    private HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>> peptideAssumptionsMap = new HashMap<>(0);
    /**
     * The best peptide assumption.
     */
    private PeptideAssumption bestPeptideAssumption;
    /**
     * Map of the identification algorithm tag assumptions: advocate number &gt;
     * score &gt; assumptions.
     */
    private HashMap<Integer, HashMap<Double, ArrayList<TagAssumption>>> tagAssumptionsMap = new HashMap<>(0);
    /**
     * The best tag assumption.
     */
    private TagAssumption bestTagAssumption;

    /**
     * Constructor for the spectrum match.
     */
    public SpectrumMatch() {
    }

    /**
     * Sets the peptide assumption map.
     *
     * @param peptideAssumptionsMap the peptide assumption map
     */
    public void setPeptideAssumptionMap(HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>> peptideAssumptionsMap) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.peptideAssumptionsMap = peptideAssumptionsMap;
    }

    /**
     * Sets the tag assumption map.
     *
     * @param tagAssumptionsMap the tag assumption map
     */
    public void setTagAssumptionMap(HashMap<Integer, HashMap<Double, ArrayList<TagAssumption>>> tagAssumptionsMap) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.tagAssumptionsMap = tagAssumptionsMap;
    }

    /**
     * Constructor for the spectrum match.
     *
     * @param spectrumKey the key of the spectrum
     */
    public SpectrumMatch(String spectrumKey) {
        this.spectrumKey = spectrumKey;
        peptideAssumptionsMap = new HashMap<>(1);
    }

    /**
     * Returns the spectrum number.
     * 
     * @return the spectrum number
     */
    public int getSpectrumNumber() {
        return spectrumNumber;
    }

    /**
     * Sets the spectrum number.
     * 
     * @param spectrumNumber the spectrum number
     */
    public void setSpectrumNumber(int spectrumNumber) {
        this.spectrumNumber = spectrumNumber;
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
     * Returns all peptide assumptions for the specified search engine indexed by their
     * score. Null if none found.
     *
     * @param advocateId the desired advocate ID
     *
     * @return all assumptions
     */
    public HashMap<Double, ArrayList<PeptideAssumption>> getAllPeptideAssumptions(int advocateId) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return peptideAssumptionsMap.get(advocateId);
    }

    /**
     * Returns all tag assumptions for the specified search engine indexed by their
     * score. Null if none found.
     *
     * @param advocateId the desired advocate ID
     *
     * @return all assumptions
     */
    public HashMap<Double, ArrayList<TagAssumption>> getAllTagAssumptions(int advocateId) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return tagAssumptionsMap.get(advocateId);
    }

    /**
     * Returns a stream of all peptide assumptions
     *
     * @return a stream of all peptide assumptions
     */
    public Stream<PeptideAssumption> getAllPeptideAssumptions() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return peptideAssumptionsMap.values().stream()
                .flatMap(algorithmMap -> algorithmMap.values().stream())
                .flatMap(assumptionsList -> assumptionsList.stream());
    }

    /**
     * Returns a stream of all tag assumptions
     *
     * @return a stream of all tag assumptions
     */
    public Stream<TagAssumption> getAllTagAssumptions() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return tagAssumptionsMap.values().stream()
                .flatMap(algorithmMap -> algorithmMap.values().stream())
                .flatMap(assumptionsList -> assumptionsList.stream());
    }

    /**
     * Returns the peptide assumptions map: advocate id &gt; score &gt; list of
     * assumptions.
     *
     * @return the assumptions map
     */
    public HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>> getPeptideAssumptionsMap() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return peptideAssumptionsMap;
    }

    /**
     * Returns the tag assumptions map: advocate id &gt; score &gt; list of
     * assumptions.
     *
     * @return the assumptions map
     */
    public HashMap<Integer, HashMap<Double, ArrayList<TagAssumption>>> getTagAssumptionsMap() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return tagAssumptionsMap;
    }

    /**
     * Add a hit.
     *
     * @param advocateId the index of the advocate of the new hit
     * @param peptideAssumption the new identification assumption
     */
    public void addPeptideAssumption(int advocateId, PeptideAssumption peptideAssumption) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        HashMap<Double, ArrayList<PeptideAssumption>> advocateMap = peptideAssumptionsMap.get(advocateId);
        
        if (advocateMap == null) {
            
            advocateMap = new HashMap<>(1);
            peptideAssumptionsMap.put(advocateId, advocateMap);
            
        }
        
        Double score = peptideAssumption.getScore();
        ArrayList<PeptideAssumption> assumptionList = advocateMap.get(score);
        
        if (assumptionList == null) {
            
            assumptionList = new ArrayList<>(1);
            advocateMap.put(score, assumptionList);
            
        }
        
        assumptionList.add(peptideAssumption);
    }

    /**
     * Add a hit.
     *
     * @param advocateId the index of the advocate of the new hit
     * @param tagAssumption the new identification assumption
     */
    public void addTagAssumption(int advocateId, TagAssumption tagAssumption) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        HashMap<Double, ArrayList<TagAssumption>> advocateMap = tagAssumptionsMap.get(advocateId);
        
        if (advocateMap == null) {
            
            advocateMap = new HashMap<>(1);
            tagAssumptionsMap.put(advocateId, advocateMap);
            
        }
        
        double score = tagAssumption.getScore();
        ArrayList<TagAssumption> assumptionList = advocateMap.get(score);
        
        if (assumptionList == null) {
            
            assumptionList = new ArrayList<>(1);
            advocateMap.put(score, assumptionList);
            
        }
        
        assumptionList.add(tagAssumption);
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
     * @param peptideAssumption the peptide assumption to remove
     */
    public void removePeptideAssumption(PeptideAssumption peptideAssumption) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        int se = peptideAssumption.getAdvocate();
        HashMap<Double, ArrayList<PeptideAssumption>> algorithmMap = peptideAssumptionsMap.get(se);
        ArrayList<PeptideAssumption> assumptionsList = algorithmMap.get(peptideAssumption.getScore());
        assumptionsList.remove(peptideAssumption);
        
        if (assumptionsList.isEmpty()) {
            
            algorithmMap.remove(peptideAssumption.getScore());
            
        }
        if (algorithmMap.isEmpty()) {
            
            peptideAssumptionsMap.remove(se);
            
        }
    }

    /**
     * Removes an assumption from the mapping.
     *
     * @param tagAssumption the tag assumption to remove
     */
    public void removeTagAssumption(TagAssumption tagAssumption) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        int se = tagAssumption.getAdvocate();
        HashMap<Double, ArrayList<TagAssumption>> algorithmMap = tagAssumptionsMap.get(se);
        ArrayList<TagAssumption> assumptionsList = algorithmMap.get(tagAssumption.getScore());
        assumptionsList.remove(tagAssumption);
        
        if (assumptionsList.isEmpty()) {
            
            algorithmMap.remove(tagAssumption.getScore());
            
        }
        
        if (algorithmMap.isEmpty()) {
            
            tagAssumptionsMap.remove(se);
            
        }
    }

    /**
     * Indicates whether the spectrum match contains a peptide assumption.
     *
     * @return a boolean indicating whether the spectrum match contains a peptide
     * assumption
     */
    public boolean hasPeptideAssumption() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return peptideAssumptionsMap.values().stream().flatMap(algorithmMap -> algorithmMap.values().stream())
                .anyMatch(assumptionsList -> !assumptionsList.isEmpty());
    }

    /**
     * Indicates whether the spectrum match contains a tag assumption.
     *
     * @return a boolean indicating whether the spectrum match contains a tag
     * assumption
     */
    public boolean hasTagAssumption() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return tagAssumptionsMap.values().stream().flatMap(algorithmMap -> algorithmMap.values().stream())
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
    public boolean hasPeptideAssumption(int advocateId) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        HashMap<Double, ArrayList<PeptideAssumption>> algorithmIds = peptideAssumptionsMap.get(advocateId);

        return algorithmIds == null ? false : algorithmIds.values().stream().anyMatch(assumptions -> !assumptions.isEmpty());
    }

    /**
     * Indicates whether the spectrum match contains a tag assumption for
     * the given advocate (see the Advocate class).
     *
     * @param advocateId The index of the advocate
     *
     * @return a boolean indicating whether the spectrum match contains an
     * assumption for the given advocate
     */
    public boolean hasTagAssumption(int advocateId) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        HashMap<Double, ArrayList<TagAssumption>> algorithmIds = tagAssumptionsMap.get(advocateId);

        return algorithmIds == null ? false : algorithmIds.values().stream().anyMatch(assumptions -> !assumptions.isEmpty());
    }
}
