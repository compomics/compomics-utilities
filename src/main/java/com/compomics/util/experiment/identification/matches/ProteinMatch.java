package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;

/**
 * This class models a protein match.
 *
 * @author Marc Vaudel
 */
public class ProteinMatch extends IdentificationMatch {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -6061842447053092696L;
    /**
     * The matching protein(s) accessions.
     */
    private ArrayList<String> theoreticProtein = new ArrayList<String>();
    /**
     * The accession of the retained protein after protein inference resolution.
     */
    private String mainMatch;
    /**
     * The corresponding peptide match keys.
     */
    private ArrayList<String> peptideMatchesKeys = new ArrayList<String>();
    /**
     * The splitter in the key between protein accessions.
     */
    public static final String PROTEIN_KEY_SPLITTER = "_cus_";
    /**
     * Map of the most complex groups: key | proteins.
     */
    private static HashMap<String, String[]> proteinGroupCache = new HashMap<String, String[]>(1000);
    /**
     * Size of the protein groups cache.
     */
    private static int cacheSize = 1000;
    /**
     * The minimal group size to include a protein in the cache.
     */
    private static int sizeOfProteinsInCache = 10;

    /**
     * Constructor for the protein match.
     */
    public ProteinMatch() {
    }

    /**
     * Constructor for the protein match.
     *
     * @param proteinAccession the matching protein
     * @throws IllegalArgumentException if an IllegalArgumentException occurs
     */
    public ProteinMatch(String proteinAccession) throws IllegalArgumentException {
        if (proteinAccession.contains(PROTEIN_KEY_SPLITTER)) {
            throw new IllegalArgumentException("Protein accession containing '" + PROTEIN_KEY_SPLITTER + "' are not supported. Conflicting accession: " + mainMatch);
        }
        theoreticProtein.add(proteinAccession);
        mainMatch = proteinAccession;
    }

    /**
     * Constructor for the protein match. Note: proteins must be set for the
     * peptide.
     *
     * @param peptide the corresponding peptide match
     * @param peptideMatchKey the key of the peptide match
     *
     * @throws IOException if an IOException occurs
     * @throws SQLException if an SQLException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     */
    public ProteinMatch(Peptide peptide, String peptideMatchKey) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        ArrayList<String> parentProteins = peptide.getParentProteinsNoRemapping();
        if (parentProteins == null || parentProteins.isEmpty()) {
            throw new IllegalArgumentException("Peptide " + peptide.getSequence() + " presents no parent protein.");
        }
        Collections.sort(parentProteins);
        for (String protein : parentProteins) {
            if (!theoreticProtein.contains(protein)) {
                theoreticProtein.add(protein);
            }
        }
        mainMatch = parentProteins.get(0);
        peptideMatchesKeys.add(peptideMatchKey);
    }

    /**
     * Returns the accessions of the possible theoretic proteins.
     *
     * @return the accessions of the possible theoretic proteins
     */
    public ArrayList<String> getTheoreticProteinsAccessions() {
        return theoreticProtein;
    }

    /**
     * Setter for the matching protein.
     *
     * @param proteinAccession the matching protein
     */
    public void addTheoreticProtein(String proteinAccession) {
        theoreticProtein.add(proteinAccession);
        setModified(true);
    }

    /**
     * Returns the main match accession after protein inference.
     *
     * @return the main match accession after protein inference
     */
    public String getMainMatch() {
        return mainMatch;
    }

    /**
     * Sets the main protein accession after protein inference.
     *
     * @param mainMatch the main match
     */
    public void setMainMatch(String mainMatch) {
        this.mainMatch = mainMatch;
        setModified(true);
    }

    /**
     * Getter for the peptide keys.
     *
     * @return subordinated peptide keys
     */
    public ArrayList<String> getPeptideMatchesKeys() {
        return peptideMatchesKeys;
    }

    /**
     * Add a subordinated peptide key.
     *
     * @param peptideMatchKey a peptide key
     */
    public void addPeptideMatchKey(String peptideMatchKey) {
        if (!peptideMatchesKeys.contains(peptideMatchKey)) {
            peptideMatchesKeys.add(peptideMatchKey);
            setModified(true);
        }
    }

    /**
     * Sets the peptide keys for this protein match.
     *
     * @param peptideMatchKeys the peptide match keys
     */
    public void setPeptideKeys(ArrayList<String> peptideMatchKeys) {
        peptideMatchesKeys = peptideMatchKeys;
        setModified(true);
    }

    /**
     * Returns the number of peptides found.
     *
     * @return the number of peptides found
     */
    public int getPeptideCount() {
        return peptideMatchesKeys.size();
    }

    /**
     * Method indicates if the protein match is a decoy one.
     *
     * @return boolean indicating if the protein match is a decoy one
     */
    public boolean isDecoy() {
        for (String accession : theoreticProtein) {
            if (SequenceFactory.getInstance().isDecoyAccession(accession)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method indicating whether a match is decoy based on the match
     * key. A match is considered decoy if at least one of its accessions is
     * decoy.
     *
     * Note: the sequence database should be loaded in the sequence factory
     *
     * @param key the match key
     * @return a boolean indicating whether a match is decoy
     */
    public static boolean isDecoy(String key) {
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        for (String accession : getAccessions(key)) {
            if (sequenceFactory.isDecoyAccession(accession)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getKey() {
        Collections.sort(theoreticProtein);
        StringBuilder result = new StringBuilder();
        for (String accession : theoreticProtein) {
            if (result.length() != 0) {
                result.append(PROTEIN_KEY_SPLITTER);
            }
            result.append(accession);
        }
        return result.toString();
    }

    /**
     * Convenience method which returns the protein key of a peptide. Note:
     * proteins must be set for the peptide.
     *
     * @param peptide the considered peptide
     * @return the protein match key
     *
     * @throws IOException if an IOException occurs
     * @throws SQLException if an SQLException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     */
    public static String getProteinMatchKey(Peptide peptide) throws IOException, SQLException, ClassNotFoundException, InterruptedException {

        ArrayList<String> accessions = peptide.getParentProteinsNoRemapping();
        if (accessions == null) {
            throw new IllegalArgumentException("Proteins not set for peptide " + peptide.getKey() + ".");
        }
        HashSet<String> uniqueAccessions = new HashSet<String>(accessions);
        accessions = new ArrayList<String>(uniqueAccessions);
        Collections.sort(accessions);
        StringBuilder key = new StringBuilder(accessions.size() * 6);
        for (String accession : accessions) {
            if (key.length() > 0) {
                key.append(PROTEIN_KEY_SPLITTER);
            }
            key.append(accession);
        }
        return key.toString();
    }

    /**
     * Returns the number of proteins for the match corresponding to the given.
     * key.
     *
     * @param matchKey the given key
     * @return the number of proteins for this match
     */
    public static int getNProteins(String matchKey) {
        return getAccessions(matchKey).length;
    }

    /**
     * Returns the number of proteins for this match.
     *
     * @return the number of proteins for this match
     */
    public int getNProteins() {
        return theoreticProtein.size();
    }

    /**
     * Returns a boolean indicating whether a protein match contains another set
     * of matches.
     *
     * @param sharedKey the key of the protein of interest
     * @param uniqueKey the key of the protein supposedly contained
     * @return a boolean indicating whether a protein match contains another set
     * of matches.
     */
    public static boolean contains(String sharedKey, String uniqueKey) {
        List<String> sharedAccessions = Arrays.asList(getAccessions(sharedKey));
        for (String uniqueAccession : getAccessions(uniqueKey)) {
            if (!sharedAccessions.contains(uniqueAccession)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a boolean indicating whether a protein match contains another set
     * of matches.
     *
     * @param sharedAccessions the accessions of the shared protein match
     * @param uniqueKeys the keys of the unique protein match
     *
     * @return a boolean indicating whether a protein match contains another set
     * of matches.
     */
    public static boolean contains(HashSet<String> sharedAccessions, ArrayList<String> uniqueKeys) {
        for (String uniqueAccession : uniqueKeys) {
            if (!sharedAccessions.contains(uniqueAccession)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the proteins in a group (group1) which are not in another group
     * (group2).
     *
     * @param group1 the key of the shared group
     * @param group2 the key of the unique group
     * @return list of the accessions in the search group which are not in the
     * unique group
     */
    public static ArrayList<String> getOtherProteins(String group1, String group2) {
        String[] group1Proteins = getAccessions(group1);
        List<String> group2Proteins = Arrays.asList(getAccessions(group2));
        ArrayList<String> result = new ArrayList<String>();
        for (String sharedAccession : group1Proteins) {
            if (!group2Proteins.contains(sharedAccession)) {
                result.add(sharedAccession);
            }
        }
        return result;
    }

    /**
     * Returns the common proteins between two protein groups.
     *
     * @param group1 key of the first group
     * @param group2 key of the second group
     *
     * @return a list of common keys
     */
    public static ArrayList<String> getCommonProteins(String group1, String group2) {
        String[] group1Proteins = getAccessions(group1);
        List<String> group2Proteins = Arrays.asList(getAccessions(group2));
        ArrayList<String> result = new ArrayList<String>();
        for (String sharedAccession : group1Proteins) {
            if (group2Proteins.contains(sharedAccession)) {
                result.add(sharedAccession);
            }
        }
        return result;
    }

    /**
     * Returns a boolean indicating whether the protein match contains another
     * set of theoretic proteins.
     *
     * @param proteinMatch another protein match
     * @return a boolean indicating whether the protein match contains another
     * set of theoretic proteins
     */
    public boolean contains(ProteinMatch proteinMatch) {
        if (getKey().equals(proteinMatch.getKey())) {
            return false;
        }
        for (String accession : proteinMatch.getTheoreticProteinsAccessions()) {
            if (!theoreticProtein.contains(accession)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a boolean indicating whether a protein was found in this protein
     * match.
     *
     * @param aProtein the inspected protein
     * @return a boolean indicating whether a protein was found in this protein
     * match
     */
    public boolean contains(String aProtein) {
        return theoreticProtein.contains(aProtein);
    }

    /**
     * Returns a list of accessions from the given key.
     *
     * @param groupKey the given key
     *
     * @return the corresponding list of accessions
     */
    public static String[] getAccessions(String groupKey) {
        String[] result = proteinGroupCache.get(groupKey);
        if (result == null) {
            result = groupKey.split(PROTEIN_KEY_SPLITTER);
            if (result.length > sizeOfProteinsInCache) {
                proteinGroupCache.put(groupKey, result);
                if (proteinGroupCache.size() > cacheSize) {
                    int smallestSize = sizeOfProteinsInCache;
                    String smallestGroup = null;
                    for (String key : proteinGroupCache.keySet()) {
                        String[] group = proteinGroupCache.get(key);
                        if (smallestGroup == null || group.length < smallestSize) {
                            smallestGroup = key;
                            smallestSize = group.length;
                        }
                    }
                    proteinGroupCache.remove(smallestGroup);
                    sizeOfProteinsInCache = smallestSize;
                }
            }
        }
        return result;
    }

    /**
     * Clears the cache.
     */
    public static void clearCache() {
        proteinGroupCache.clear();
        sizeOfProteinsInCache = 10;
    }

    /**
     * Indicates whether the protein group has an enzymatic peptide when
     * considering the given accession as main accession.
     *
     * @param accession the candidate main accession
     * @param enzymes the enzymes used
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     *
     * @return true if the main accession generates an enzymatic peptide
     */
    public boolean hasEnzymaticPeptide(String accession, ArrayList<Enzyme> enzymes, SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, InterruptedException, ClassNotFoundException {
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        for (String peptideKey : peptideMatchesKeys) {
            String peptideSequence = Peptide.getSequence(peptideKey);
            Protein protein = sequenceFactory.getProtein(accession);
            if (protein.isEnzymaticPeptide(peptideSequence, enzymes, sequenceMatchingPreferences)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MatchType getType() {
        return MatchType.Protein;
    }
}
