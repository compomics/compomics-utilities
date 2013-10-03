package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.SequenceFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.util.List;

/**
 * This class models a protein match.
 * <p/>
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
    private ArrayList<String> peptideMatches = new ArrayList<String>();
    /**
     * The splitter in the key between spectrumFile and spectrumTitle.
     */
    public static final String PROTEIN_KEY_SPLITTER = "_cus_";

    /**
     * Constructor for the protein match.
     */
    public ProteinMatch() {
    }

    /**
     * Constructor for the protein match.
     *
     * @param proteinAccession the matching protein
     * @throws IllegalArgumentException
     */
    public ProteinMatch(String proteinAccession) throws IllegalArgumentException {
        if (proteinAccession.contains(PROTEIN_KEY_SPLITTER)) {
            throw new IllegalArgumentException("Protein accession containing '" + PROTEIN_KEY_SPLITTER + "' are not supported. Conflicting accession: " + mainMatch);
        }
        theoreticProtein.add(proteinAccession);
        mainMatch = proteinAccession;
    }

    /**
     * Constructor for the protein match.
     *
     * @param peptide the corresponding peptide match
     */
    public ProteinMatch(Peptide peptide) {
        ArrayList<String> parentProteins = peptide.getParentProteins();
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
        peptideMatches.add(peptide.getKey());
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
    }

    /**
     * Getter for the peptide matches.
     *
     * @return subordinated peptide matches
     */
    public ArrayList<String> getPeptideMatches() {
        return peptideMatches;
    }

    /**
     * Add a subordinated peptide match.
     *
     * @param peptideMatchKey a peptide match
     */
    public void addPeptideMatch(String peptideMatchKey) {
        if (!peptideMatches.contains(peptideMatchKey)) {
            peptideMatches.add(peptideMatchKey);
        }
    }

    /**
     * Returns the number of peptides found.
     *
     * @return the number of peptides found
     */
    public int getPeptideCount() {
        return peptideMatches.size();
    }

    /**
     * Method indicates if the protein match is a decoy one.
     *
     * @return boolean indicating if the protein match is a decoy one
     */
    public boolean isDecoy() {
        for (String protein : theoreticProtein) {
            if (SequenceFactory.isDecoy(protein)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method indicating whether a match is decoy based on the match
     * key.
     *
     * @param key the match key
     * @return a boolean indicating whether a match is decoy
     */
    public static boolean isDecoy(String key) {
        for (String accession : getAccessions(key)) {
            if (SequenceFactory.isDecoy(accession)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getKey() {
        Collections.sort(theoreticProtein);
        String result = "";
        for (String accession : theoreticProtein) {
            if (!result.equals("")) {
                result += PROTEIN_KEY_SPLITTER;
            }
            result += accession;
        }
        return result;
    }

    /**
     * Convenience method which returns the protein key of a peptide.
     *
     * @param peptide the considered peptide
     * @return the protein match key
     */
    public static String getProteinMatchKey(Peptide peptide) {
        ArrayList<String> accessions = new ArrayList<String>();
        for (String protein : peptide.getParentProteins()) {
            if (!accessions.contains(protein)) {
                accessions.add(protein);
            }
        }
        Collections.sort(accessions);
        String result = "";
        for (String accession : accessions) {
            if (!result.equals("")) {
                result += PROTEIN_KEY_SPLITTER;
            }
            result += accession;
        }
        return result.trim();
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
        if (sharedKey.equals(uniqueKey)) {
            return false;
        }
        List<String> sharedAccessions = Arrays.asList(getAccessions(sharedKey));
        for (String uniqueAccession : getAccessions(uniqueKey)) {
            if (!sharedAccessions.contains(uniqueAccession)) {
                return false;
            }
        }
        return true;
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
     * @param key the given key
     * @return the corresponding list of accessions
     */
    public static String[] getAccessions(String key) {
        return key.split(PROTEIN_KEY_SPLITTER);
    }

    @Override
    public MatchType getType() {
        return MatchType.Protein;
    }
}