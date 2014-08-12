package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.TagAssumption;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.IOException;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
     *
     * @deprecated use the assumptionsMap instead
     */
    private HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>> assumptions = new HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>>();
    /**
     * Map of the identification algorithm assumption: advocate number -> score
     * -> assumptions
     */
    private HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptionsMap = new HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>>();
    /**
     * A tag assumptions map. advocate number -> assumptions
     */
    private HashMap<Integer, HashMap<String, ArrayList<TagAssumption>>> tagAssumptionsMap = null;
    /**
     * The size of the keys used for the tag assumptions map
     */
    private int tagAssumptionsMapKeySize = -1;
    /**
     * Map containing the first hits indexed by the Advocate index.
     *
     * @deprecated use the firstHitsMap instead
     */
    private HashMap<Integer, PeptideAssumption> firstHits = new HashMap<Integer, PeptideAssumption>();
    /**
     * Map containing the first hits indexed by the Advocate index.
     */
    private HashMap<Integer, SpectrumIdentificationAssumption> firstHitsMap = new HashMap<Integer, SpectrumIdentificationAssumption>();
    /**
     * The best peptide assumption. Note: cannot be renamed for backward
     * compatibility.
     */
    private PeptideAssumption bestAssumption;
    /**
     * The best tag assumption.
     */
    private TagAssumption bestTagAsssumption;
    /**
     * All advocates used.
     */
    private ArrayList<Integer> advocates = new ArrayList<Integer>();
    /**
     * The spectrum number in the mgf file. Will be used in case the spectrum
     * title does not match
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
     * @param assumption The matching assumption
     */
    public SpectrumMatch(String spectrumKey, SpectrumIdentificationAssumption assumption) {
        int advocateId = assumption.getAdvocate();
        assumptionsMap.put(advocateId, new HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>());
        assumptionsMap.get(advocateId).put(assumption.getScore(), new ArrayList<SpectrumIdentificationAssumption>());
        assumptionsMap.get(advocateId).get(assumption.getScore()).add(assumption);
        firstHitsMap.put(advocateId, assumption);
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
     * Getter for the best peptide assumption.
     *
     * @return the best peptide assumption for the spectrum
     */
    public PeptideAssumption getBestPeptideAssumption() {
        return bestAssumption;
    }

    /**
     * Setter for the best peptide assumption.
     *
     * @param bestAssumption the best peptide assumption for the spectrum
     */
    public void setBestPeptideAssumption(PeptideAssumption bestAssumption) {
        this.bestAssumption = bestAssumption;
    }

    /**
     * Getter for the best tag assumption.
     *
     * @return the best tag assumption for the spectrum
     */
    public TagAssumption getBestTagAssumption() {
        return bestTagAsssumption;
    }

    /**
     * Setter for the best tag assumption.
     *
     * @param bestTagAsssumption the best tag assumption for the spectrum
     */
    public void setBestTagAssumption(TagAssumption bestTagAsssumption) {
        this.bestTagAsssumption = bestTagAsssumption;
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
    public HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> getAllAssumptions(int advocateId) {
        if (assumptionsMap == null) { // backward compatibility check
            update();
        }
        return assumptionsMap.get(advocateId);
    }

    /**
     * Updates the assumption maps based on the old structure where only peptide
     * assumptions were supported.
     */
    private void update() {
        assumptionsMap = new HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>>(assumptions.size());
        for (int advocate : assumptions.keySet()) {
            HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> advocateMapping = new HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>(assumptions.get(advocate).size());
            for (double score : assumptions.get(advocate).keySet()) {
                advocateMapping.put(score, new ArrayList<SpectrumIdentificationAssumption>(assumptions.get(advocate).get(score)));
            }
            assumptionsMap.put(advocate, advocateMapping);
        }
        firstHitsMap = new HashMap<Integer, SpectrumIdentificationAssumption>(firstHits);
    }

    /**
     * Return all assumptions for all identification algorithms as a list.
     *
     * @return all assumptions
     */
    public ArrayList<SpectrumIdentificationAssumption> getAllAssumptions() {
        if (assumptionsMap == null) { // backward compatibility check
            update();
        }
        ArrayList<SpectrumIdentificationAssumption> result = new ArrayList<SpectrumIdentificationAssumption>();
        for (HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> seMap : assumptionsMap.values()) {
            for (double eValue : seMap.keySet()) {
                result.addAll(seMap.get(eValue));
            }
        }
        return result;
    }

    /**
     * Add a first hit.
     *
     * @param otherAdvocateId the index of the new advocate
     * @param otherAssumption the new identification assumption
     * @param ascendingScore indicates whether the score is ascending when hits
     * get better
     */
    public void addHit(int otherAdvocateId, SpectrumIdentificationAssumption otherAssumption, boolean ascendingScore) {
        if (!firstHitsMap.containsKey(otherAdvocateId)
                || !ascendingScore && firstHitsMap.get(otherAdvocateId).getScore() > otherAssumption.getScore()
                || ascendingScore && firstHitsMap.get(otherAdvocateId).getScore() < otherAssumption.getScore()) {
            firstHitsMap.put(otherAdvocateId, otherAssumption);
        }
        if (!assumptionsMap.containsKey(otherAdvocateId)) {
            assumptionsMap.put(otherAdvocateId, new HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>());
        }
        if (!assumptionsMap.get(otherAdvocateId).containsKey(otherAssumption.getScore())) {
            assumptionsMap.get(otherAdvocateId).put(otherAssumption.getScore(), new ArrayList<SpectrumIdentificationAssumption>());
        }
        assumptionsMap.get(otherAdvocateId).get(otherAssumption.getScore()).add(otherAssumption);
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
    public SpectrumIdentificationAssumption getFirstHit(int advocateId) {
        if (firstHitsMap == null) { // backward compatibility check
            update();
        }
        return firstHitsMap.get(advocateId);
    }

    /**
     * Returns a list of the top scoring assumptions for the given advocate.
     * 
     * @param advocateId the index of the advocate of interest
     * @return a list of the top scoring assumptions for the given advocate
     */
    public ArrayList<SpectrumIdentificationAssumption> getFirstHits(int advocateId) {
        HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> advocateHitMap = assumptionsMap.get(advocateId);
        if (advocateHitMap != null) {
            ArrayList<Double> eValues = new ArrayList<Double>(advocateHitMap.keySet());
            double bestScore = Collections.min(eValues);
            return advocateHitMap.get(bestScore);
        }
        return new ArrayList<SpectrumIdentificationAssumption>();
    }

    /**
     * Sets the best assumption according to the search engine.
     *
     * @param advocateId the search engine index
     * @param bestAssumption the best assumption
     */
    public void setFirstHit(int advocateId, SpectrumIdentificationAssumption bestAssumption) {
        firstHitsMap.put(advocateId, bestAssumption);
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
     * Returns the spectrum number in the spectrum file. Returns null if not
     * implemented (for versions older than 3.4.17 for instance).
     *
     * @return the spectrum number in the spectrum file
     */
    public Integer getSpectrumNumber() {
        return spectrumNumber;
    }

    /**
     * Sets the spectrum number in the spectrum file.
     *
     * @param spectrumNumber the spectrum number in the spectrum file
     */
    public void setSpectrumNumber(Integer spectrumNumber) {
        this.spectrumNumber = spectrumNumber;
    }

    /**
     * Removes an assumption from the mapping. Note: this does not affect the
     * first hit map.
     *
     * @param assumption the peptide assumption to remove
     */
    public void removeAssumption(SpectrumIdentificationAssumption assumption) {
        ArrayList<Integer> seToRemove = new ArrayList<Integer>();
        for (int se : assumptionsMap.keySet()) {
            ArrayList<Double> eValueToRemove = new ArrayList<Double>();
            for (double eValue : assumptionsMap.get(se).keySet()) {
                assumptionsMap.get(se).get(eValue).remove(assumption);
                if (assumptionsMap.get(se).get(eValue).isEmpty()) {
                    eValueToRemove.add(eValue);
                }
            }
            for (double eValue : eValueToRemove) {
                assumptionsMap.get(se).remove(eValue);
            }
            if (assumptionsMap.get(se).isEmpty()) {
                seToRemove.add(se);
            }
        }
        for (int se : seToRemove) {
            assumptionsMap.remove(se);
        }
    }

    /**
     * Indicates whether the spectrum match contains a peptide assumption from a
     * search engine.
     *
     * @return a boolean indicating whether the spectrum match contains an
     * assumption
     */
    public boolean hasAssumption() {
        if (assumptionsMap == null) { // backward compatibility check
            update();
        }
        for (int se : assumptionsMap.keySet()) {
            for (ArrayList<SpectrumIdentificationAssumption> assumptionsAtScore : assumptionsMap.get(se).values()) {
                if (!assumptionsAtScore.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Indicates whether the spectrum match contains a peptide assumption for
     * the given advocate (for example a search engine, see the Advocate class)
     *
     * @param advocateId The index of the advocate
     * @return a boolean indicating whether the spectrum match contains a
     * peptide assumption for the given advocate
     */
    public boolean hasAssumption(int advocateId) {
        if (assumptionsMap == null) { // backward compatibility check
            update();
        }
        if (assumptionsMap.containsKey(advocateId)) {
            for (ArrayList<SpectrumIdentificationAssumption> assumptionsAtEvalue : assumptionsMap.get(advocateId).values()) {
                if (!assumptionsAtEvalue.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a peptide based spectrum match where peptide assumptions are
     * deduced from tag assumptions. The original tag assumption is added to the
     * peptide match as refinement parameter
     *
     * @param proteinTree the protein tree to use to map tags to peptides
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param massTolerance the ms2 mass tolerance to use
     * @param scoreInAscendingOrder boolean indicating whether the tag score is
     * in the ascending order; ie the higher the score, the better the match.
     * @param fixedModifications the fixed modifications to account for
     * @param variableModifications the variable modifications to account for
     * @param ascendingScore indicates whether the score is ascending when hits
     * get better
     * @param reportFixedPtms a boolean indicating whether fixed PTMs should be
     * reported in the Peptide object
     *
     * @return a new spectrum match containing the peptide assumptions made from
     * the tag assumptions.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public SpectrumMatch getPeptidesFromTags(ProteinTree proteinTree, SequenceMatchingPreferences sequenceMatchingPreferences, Double massTolerance,
            boolean scoreInAscendingOrder, ArrayList<String> fixedModifications, ArrayList<String> variableModifications, boolean ascendingScore, boolean reportFixedPtms)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        SpectrumMatch spectrumMatch = new SpectrumMatch(spectrumKey);

        for (int advocateId : assumptionsMap.keySet()) {

            int rank = 1;
            ArrayList<Double> scores = new ArrayList<Double>(assumptionsMap.get(advocateId).keySet());

            if (scoreInAscendingOrder) {
                Collections.sort(scores);
            } else {
                Collections.sort(scores, Collections.reverseOrder());
            }

            for (double score : scores) {
                ArrayList<SpectrumIdentificationAssumption> originalAssumptions = assumptionsMap.get(advocateId).get(score);
                for (SpectrumIdentificationAssumption assumption : originalAssumptions) {
                    if (assumption instanceof TagAssumption) {
                        TagAssumption tagAssumption = (TagAssumption) assumption;
                        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping
                                = proteinTree.getProteinMapping(tagAssumption.getTag(), sequenceMatchingPreferences, massTolerance, fixedModifications, variableModifications, reportFixedPtms);
                        for (Peptide peptide : proteinMapping.keySet()) {
                            PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, advocateId,
                                    assumption.getIdentificationCharge(), score, assumption.getIdentificationFile()); //@TODO: change the score based on tag to peptide matching?
                            peptideAssumption.addUrParam(tagAssumption);
                            spectrumMatch.addHit(advocateId, peptideAssumption, ascendingScore);
                        }
                    }
                }
            }
        }

        return spectrumMatch;
    }
    
    /**
     * Returns a map containing the tag assumptions of this spectrum assumptions indexed by the beginning of the longest amino acid sequence.
     * @param keySize
     * @return 
     */
    public HashMap<Integer, HashMap<String, ArrayList<TagAssumption>>> getTagAssumptionsMap(int keySize) {
        if (tagAssumptionsMap == null || keySize != tagAssumptionsMapKeySize) {
            tagAssumptionsMap = new HashMap<Integer, HashMap<String, ArrayList<TagAssumption>>>(assumptionsMap.size());
            for (int advocate : assumptionsMap.keySet()) {
                HashMap<String, ArrayList<TagAssumption>> advocateMap = tagAssumptionsMap.get(advocate);
                if (advocateMap == null) {
                    advocateMap = new HashMap<String, ArrayList<TagAssumption>>();
                    tagAssumptionsMap.put(advocate, advocateMap);
                }
                for (Collection<SpectrumIdentificationAssumption> spectrumIdentificationAssumptions : assumptionsMap.get(advocate).values()) {
                    for (SpectrumIdentificationAssumption spectrumIdentificationAssumption : spectrumIdentificationAssumptions) {
                        if (spectrumIdentificationAssumption instanceof TagAssumption) {
                            TagAssumption tagAssumption = (TagAssumption) spectrumIdentificationAssumption;
                            String longestSequence = tagAssumption.getTag().getLongestAminoAcidSequence();
                            if (longestSequence.length() < keySize) {
                                throw new IllegalArgumentException("Tag " + tagAssumption.getTag() + " cannot be indexed. Longest amino acid sequence " + longestSequence + " should be of length >= " + keySize + ".");
                            }
                            String subSequence = longestSequence.substring(0, keySize);
                            ArrayList<TagAssumption> tagAssumptions = advocateMap.get(subSequence);
                            if (tagAssumptions == null) {
                                tagAssumptions = new ArrayList<TagAssumption>();
                                advocateMap.put(subSequence, tagAssumptions);
                            }
                            tagAssumptions.add(tagAssumption);
                        }
                    }
                }
            }
            tagAssumptionsMapKeySize = keySize;
        }
        return tagAssumptionsMap;
    }
    
    /**
     * Removes the tags assumptions Map to free memory.
     */
    public void removeTagAssumptionsMap() {
        tagAssumptionsMap = null;
    }
    
}
