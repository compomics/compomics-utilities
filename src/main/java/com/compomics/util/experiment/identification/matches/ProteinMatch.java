package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.Arrays;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * This class models a protein match.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 */
public class ProteinMatch extends IdentificationMatch {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -6061842447053092696L;
    /**
     * The matching protein(s) accessions sorted in natural order.
     */
    private String[] accessions;
    /**
     * The accession of the leading protein.
     */
    private String leadingAccession;
    /**
     * The keys of the peptide matches associated to this protein match.
     */
    private long[] peptideMatchesKeys = new long[0];
    /**
     * The key of the match.
     */
    private long matchKey;
    /**
     * Boolean indicating whether the protein match is decoy.
     */
    private boolean decoy;

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

        accessions = new String[1];
        accessions[0] = proteinAccession;

        setMatchKey();

        leadingAccession = proteinAccession;

    }

    /**
     * Constructor for the protein match. Note: proteins must be set for the
     * peptide.
     *
     * @param peptide the corresponding peptide match
     * @param peptideMatchKey the key of the peptide match
     */
    public ProteinMatch(
            Peptide peptide,
            long peptideMatchKey
    ) {

        accessions = peptide.getProteinMapping()
                .navigableKeySet()
                .toArray(
                        new String[peptide.getProteinMapping().size()]
                );
        leadingAccession = accessions[0];

        peptideMatchesKeys = new long[1];
        peptideMatchesKeys[0] = peptideMatchKey;

        setMatchKey();

    }

    /**
     * Sets the accessions of the proteins in this group. Note, accessions must
     * be sorted.
     *
     * @param newAccessions the accessions of the proteins in this group
     */
    public void setAccessions(
            String[] newAccessions
    ) {

        writeDBMode();

        this.accessions = newAccessions;
        leadingAccession = accessions[0];
        setMatchKey();

    }

    /**
     * Returns the accessions of the proteins in this match.
     *
     * @return the accessions of the proteins in this match
     */
    public String[] getAccessions() {

        readDBMode();

        return accessions;
    }

    /**
     * Returns the leading accession for this match.
     *
     * @return the leading accession for this match
     */
    public String getLeadingAccession() {

        readDBMode();

        return leadingAccession;

    }

    /**
     * Sets the leading accession for this match.
     *
     * @param leadingAccession the leading accession for this match
     */
    public void setLeadingAccession(String leadingAccession) {

        writeDBMode();

        this.leadingAccession = leadingAccession;
    }

    /**
     * Returns a boolean indicating whether the given match is decoy.
     *
     * @return a boolean indicating whether the given match is decoy
     */
    public boolean isDecoy() {

        readDBMode();

        return decoy;

    }

    /**
     * Sets whether the given match is decoy
     *
     * @param decoy a boolean indicating whether the given match is decoy
     */
    public void setDecoy(boolean decoy) {

        writeDBMode();

        this.decoy = decoy;

    }

    /**
     * Getter for the peptide keys.
     *
     * @return subordinated peptide keys
     */
    public long[] getPeptideMatchesKeys() {

        readDBMode();

        return peptideMatchesKeys;
    }

    /**
     * Add a subordinated peptide key.
     *
     * @param peptideMatchKey a peptide key
     */
    public void addPeptideMatchKey(long peptideMatchKey) {

        writeDBMode();

        peptideMatchesKeys = Arrays.copyOf(peptideMatchesKeys, peptideMatchesKeys.length + 1);
        peptideMatchesKeys[peptideMatchesKeys.length - 1] = peptideMatchKey;

    }

    /**
     * Add a subordinated peptide key.
     *
     * @param newKeys peptide keys
     */
    public void addPeptideMatchKeys(long[] newKeys) {

        writeDBMode();

        peptideMatchesKeys = LongStream.concat(Arrays.stream(peptideMatchesKeys),
                Arrays.stream(newKeys))
                .distinct()
                .toArray();

    }

    /**
     * Sets the peptide keys for this protein match.
     *
     * @param peptideMatchKeys the peptide match keys
     */
    public void setPeptideMatchesKeys(long[] peptideMatchKeys) {

        writeDBMode();

        peptideMatchesKeys = peptideMatchKeys;
    }

    /**
     * Returns the number of peptides found.
     *
     * @return the number of peptides found
     */
    public int getPeptideCount() {

        readDBMode();

        return peptideMatchesKeys.length;
    }

    @Override
    public long getKey() {

        readDBMode();

        return matchKey;
    }

    /**
     * Sets the matchKey field.
     */
    private void setMatchKey() {

        writeDBMode();

        matchKey = ExperimentObject.asLong(
                Arrays.stream(accessions)
                        .collect(Collectors.joining()));

    }

    /**
     * Convenience method which returns the protein key from a peptide. Note:
     * proteins must be set for the peptide.
     *
     * @param peptide the considered peptide
     *
     * @return the protein match key
     */
    public static long getProteinMatchKey(Peptide peptide) {

        return ExperimentObject.asLong(
                peptide.getProteinMapping().navigableKeySet().stream()
                        .collect(Collectors.joining()));

    }

    /**
     * Returns the number of proteins for this match.
     *
     * @return the number of proteins for this match
     */
    public int getNProteins() {

        readDBMode();

        return accessions.length;
    }

    /**
     * Returns a boolean indicating whether a protein was found in this protein
     * match.
     *
     * @param aProtein the protein accession
     * 
     * @return a boolean indicating whether a protein was found in this protein
     * match
     */
    public boolean contains(String aProtein) {

        readDBMode();

        return Arrays.stream(accessions)
                .anyMatch(accession -> accession.equals(aProtein));
    }

    /**
     * Returns a boolean indicating whether a peptide was found in this protein
     * match.
     *
     * @param peptideKey the peptide key
     * 
     * @return a boolean indicating whether a peptide was found in this protein
     * match
     */
    public boolean containsPeptide(long peptideKey) {
        
        return Arrays.stream(peptideMatchesKeys)
                .anyMatch(key -> key == peptideKey);
        
    }

    @Override
    public MatchType getType() {
        return MatchType.Protein;
    }
}
