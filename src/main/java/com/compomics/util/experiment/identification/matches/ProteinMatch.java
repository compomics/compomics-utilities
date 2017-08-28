package com.compomics.util.experiment.identification.matches;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import java.util.stream.Collectors;

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
     * The matching protein(s) accessions sorted in natural order.
     */
    private ArrayList<String> accessions = new ArrayList<>(1);
    /**
     * The accession of the leading protein.
     */
    private String leadingAccession;
    /**
     * The corresponding peptide match keys.
     */
    private HashSet<String> peptideMatchesKeys = new HashSet<>(1);
    /**
     * The key of the match.
     */
    private String matchKey = null;
    /**
     * The splitter in the key between protein accessions.
     */
    public static final String PROTEIN_KEY_SPLITTER = "_";

    /**
     * Constructor for the protein match.
     */
    public ProteinMatch() {
    }

    /**
     * Constructor for the protein match.
     *
     * @param proteinAccession the matching protein
     */
    public ProteinMatch(String proteinAccession) {
        
        accessions.add(proteinAccession);
        leadingAccession = proteinAccession;
        
    }

    /**
     * Constructor for the protein match. Note: proteins must be set for the
     * peptide.
     *
     * @param peptide the corresponding peptide match
     * @param peptideMatchKey the key of the peptide match
     */
    public ProteinMatch(Peptide peptide, String peptideMatchKey) {
        
        accessions = new ArrayList<>(peptide.getProteinMapping().keySet());
        Collections.sort(accessions);
        leadingAccession = accessions.get(0);
        peptideMatchesKeys.add(peptideMatchKey);
        
    }

    /**
     * Sets the accessions of the proteins in this group.
     *
     * @param newAccessions the accessions of the proteins in this group
     */
    public void setAccessions(ArrayList<String> newAccessions) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.accessions = newAccessions;
        Collections.sort(accessions);
        leadingAccession = accessions.get(0);
        matchKey = null;
        
    }

    /**
     * Returns the accessions of the proteins in this match.
     *
     * @return the accessions of the proteins in this match
     */
    public ArrayList<String> getAccessions() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return accessions;
    }

    /**
     * Returns the leading accession for this match.
     *
     * @return the leading accession for this match
     */
    public String getLeadingAccession() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return leadingAccession;
    }

    /**
     * Sets the leading accession for this match.
     *
     * @param leadingAccession the leading accession for this match
     */
    public void setLeadingAccession(String leadingAccession) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.leadingAccession = leadingAccession;
    }

    /**
     * Getter for the peptide keys.
     *
     * @return subordinated peptide keys
     */
    public HashSet<String> getPeptideMatchesKeys() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return peptideMatchesKeys;
    }

    /**
     * Add a subordinated peptide key.
     *
     * @param peptideMatchKey a peptide key
     */
    public void addPeptideMatchKey(String peptideMatchKey) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        peptideMatchesKeys.add(peptideMatchKey);
    }

    /**
     * Sets the peptide keys for this protein match.
     *
     * @param peptideMatchKeys the peptide match keys
     */
    public void setPeptideMatchesKeys(HashSet<String> peptideMatchKeys) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        peptideMatchesKeys = peptideMatchKeys;
    }

    /**
     * Returns the number of peptides found.
     *
     * @return the number of peptides found
     */
    public int getPeptideCount() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return peptideMatchesKeys.size();
    }

    @Override
    public String getKey() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        if (matchKey == null) {
            
            setMatchKey();
            
        }
        
        return matchKey;
    }

    /**
     * Sets the matchKey field.
     */
    private synchronized void setMatchKey() {

        if (matchKey == null) {

            ObjectsDB.increaseRWCounter();
            zooActivateWrite();
            ObjectsDB.decreaseRWCounter();

            matchKey = accessions.stream()
                    .collect(Collectors.joining(PROTEIN_KEY_SPLITTER));

        }
    }

    /**
     * Convenience method which returns the protein key from a peptide. Note:
     * proteins must be set for the peptide.
     *
     * @param peptide the considered peptide
     *
     * @return the protein match key
     */
    public static String getProteinMatchKey(Peptide peptide) {
        
        return peptide.getProteinMapping().keySet().stream()
                .sorted()
                .collect(Collectors.joining(PROTEIN_KEY_SPLITTER));
        
    }

    /**
     * Returns the number of proteins for this match.
     *
     * @return the number of proteins for this match
     */
    public int getNProteins() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return accessions.size();
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
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return accessions.contains(aProtein);
    }

    @Override
    public MatchType getType() {
        return MatchType.Protein;
    }
}
