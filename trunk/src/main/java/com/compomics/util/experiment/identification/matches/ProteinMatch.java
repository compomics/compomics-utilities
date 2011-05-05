package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.Collections;

import java.util.HashMap;
import java.util.Set;

/**
 * This class models a protein match.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:59:02 AM
 */
public class ProteinMatch extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -6061842447053092696L;
    /**
     * The matching protein(s)
     */
    private HashMap<String, Protein> theoreticProtein = new HashMap<String, Protein>();
    /**
     * The corresponding peptide matches
     */
    private HashMap<String, PeptideMatch> peptideMatches = new HashMap<String, PeptideMatch>();

    /**
     * Constructor for the protein match
     */
    public ProteinMatch() {
    }

    /**
     * Constructor for the protein match
     *
     * @param protein the matching protein
     */
    public ProteinMatch(Protein protein) {
        theoreticProtein.put(protein.getAccession(), protein);
    }

    /**
     * Constructor for the protein match
     *
     * @param peptideMatch The corresponding peptide matches
     */
    public ProteinMatch(PeptideMatch peptideMatch) {
        for (Protein protein : peptideMatch.getTheoreticPeptide().getParentProteins()) {
            theoreticProtein.put(protein.getAccession(), protein);
        }
        peptideMatches.put(peptideMatch.getTheoreticPeptide().getKey(), peptideMatch);
    }

    /**
     * getter for the matching protein
     *
     * @return the matching protein
     */
    public Protein getTheoreticProtein(String accession) {
        return theoreticProtein.get(accession);
    }

    public Set<String> getTheoreticProteinsAccessions() {
        return theoreticProtein.keySet();
    }

    /**
     * setter for the matching protein
     *
     * @param protein the matching protein
     */
    public void addTheoreticProtein(Protein protein) {
        theoreticProtein.put(protein.getAccession(), protein);
    }

    /**
     * Removes a protein from the collection
     * @param accession the accession of the protein to remove
     */
    public void removeProtein(String accession) {
        theoreticProtein.remove(accession);
    }

    /**
     * getter for the peptide matches
     *
     * @return subordinated peptide matches
     */
    public HashMap<String, PeptideMatch> getPeptideMatches() {
        return peptideMatches;
    }

    /**
     * add a subordinated peptide match
     *
     * @param peptideMatch a peptide match
     * @throws Exception exception thrown when attempting to link two identifications from the same search engine on a single spectrum
     */
    public void addPeptideMatch(PeptideMatch peptideMatch) throws Exception {
        String index = peptideMatch.getTheoreticPeptide().getKey();
        if (peptideMatches.get(index) == null) {
            peptideMatches.put(index, peptideMatch);
        } else {
            peptideMatches.get(index).addSpectrumMatches(peptideMatch.getSpectrumMatches());
        }
    }

    /**
     * method to get the spectrum count for this protein match
     *
     * @return the spectrum count for this protein match
     */
    public int getSpectrumCount() {
        int spectrumCount = 0;
        for (PeptideMatch peptideMatch : peptideMatches.values()) {
            spectrumCount += peptideMatch.getSpectrumCount();
        }
        return spectrumCount;
    }

    /**
     * Returns the number of peptides found
     * @return the number of peptides found
     */
    public int getPeptideCount() {
        int result = 0;
        for (PeptideMatch peptideMatch : peptideMatches.values()) {
            if (peptideMatch.getSpectrumCount() > 0) {
                result++;
            }
        }
        return result;
    }

    /**
     * methods indicates if the protein match is a decoy one
     *
     * @return boolean indicating if the protein match is a decoy one
     */
    public boolean isDecoy() {
        for (Protein protein : theoreticProtein.values()) {
            if (protein.isDecoy()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of the protein match
     *
     * @return the index of the protein match
     */
    public String getKey() {
        ArrayList<String> accessions = new ArrayList<String>();
        for (Protein protein : theoreticProtein.values()) {
            if (!accessions.contains(protein.getAccession())) {
                accessions.add(protein.getAccession());
            }
        }
        Collections.sort(accessions);
        String result = "";
        for (String accession : accessions) {
            result += accession + " ";
        }
        return result.trim();
    }

    /**
     * Convenience method which returns the protein key of a peptide
     * @param peptide   the considered peptide
     * @return          the protein match key
     */
    public static String getProteinMatchKey(Peptide peptide) {
        ArrayList<String> accessions = new ArrayList<String>();
        for (Protein protein : peptide.getParentProteins()) {
            if (!accessions.contains(protein.getAccession())) {
                accessions.add(protein.getAccession());
            }
        }
        Collections.sort(accessions);
        String result = "";
        for (String accession : accessions) {
            result += accession + " ";
        }
        return result.trim();
    }

    /**
     * Returns the number of proteins for this match
     *
     * @return the number of proteins for this match
     */
    public int getNProteins() {
        return theoreticProtein.size();
    }

    /**
     * Returns a boolean indicating whether the protein match contains another set of theoretic proteins.
     * @param proteinMatch  another protein match
     * @return  a boolean indicating whether the protein match contains another set of theoretic proteins
     */
    public boolean contains(ProteinMatch proteinMatch) {
        if (getKey().equals(proteinMatch.getKey())) {
            return false;
        }
        for (String accession : proteinMatch.getTheoreticProteinsAccessions()) {
            if (!theoreticProtein.containsKey(accession)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a boolean indicating whether a protein was found in this protein match
     * @param aProtein  the inspected protein
     * @return a boolean indicating whether a protein was found in this protein match
     */
    public boolean contains(Protein aProtein) {
        return theoreticProtein.containsKey(aProtein.getAccession());
    }

    /**
     * returns a proteinMatch complementary to the protein match given
     * Example:
     * if the protein match was identified on proteins A, B and C; if the given match was identified on A; the method returns a protein match with B and C.
     * @param proteinMatch  the given protein match
     * @return  a proteinMatch complementary to the protein match given
     * @throws Exception    Exception thrown when two spectrum matches from the same search engine are attached to the same spectrum
     */
    public ProteinMatch getComplementMatch(ProteinMatch proteinMatch) throws Exception {
        ProteinMatch result = new ProteinMatch();
        for (PeptideMatch peptideMatch : peptideMatches.values()) {
            result.addPeptideMatch(peptideMatch);
        }
        for (Protein protein : theoreticProtein.values()) {
            if (!proteinMatch.contains(protein)) {
                result.addTheoreticProtein(protein);
            }
        }
        return result;
    }
}
