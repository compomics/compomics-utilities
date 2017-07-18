package com.compomics.util.experiment.identification.protein_sequences;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.protein.Header;
import com.compomics.util.protein.Header.DatabaseType;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class contains the index of a FASTA file.
 *
 * @author Marc Vaudel
 */
public class FastaIndex extends ExperimentObject implements Serializable {

    /**
     * The indexes of the inspected FASTA file.
     */
    private HashMap<String, Long> indexes;
    /**
     * The decoy accessions.
     */
    private HashSet<String> decoyAccessions;
    /**
     * The FASTA file name.
     */
    private String fileName = null;
    /**
     * The name of the database.
     */
    private String name;
    /**
     * The last time the indexed file was modified.
     */
    private Long lastModified;
    /**
     * In case decoy hits are found, check whether these are reversed versions
     * of the target with default accession suffix.
     */
    private boolean isDefaultReversed;
    /**
     * Number of target sequences found in the database.
     */
    private int nTarget;
    /**
     * The main database type.
     */
    private DatabaseType mainDatabaseType = DatabaseType.Unknown;
    /**
     * A map of all the database types and how often they occur.
     */
    private HashMap<Header.DatabaseType, Integer> databaseTypes = new HashMap<Header.DatabaseType, Integer>();
    /**
     * The version of the database.
     */
    private String version;
    /**
     * Description of the database.
     */
    private String description;
    /**
     * The accession parsing rule.
     */
    private String accessionParsingRule;
    /**
     * Indicates whether the database is a concatenated target/decoy.
     */
    private boolean concatenatedTargetDecoy;
    /**
     * The tag used for decoy sequences.
     */
    private String decoyTag;
    /**
     * The species occurrence in the database.
     */
    private HashMap<String, Integer> speciesOccurrence;

    /**
     * Constructor.
     *
     * @param indexes The indexes of the inspected FASTA file
     * @param decoyAccessions the decoy accession numbers
     * @param fileName The FASTA file name
     * @param name the name of the database
     * @param concatenatedTargetDecoy If the FASTA file is a concatenated
     * target/decoy database
     * @param isDefaultReversed is this a default reversed database
     * @param nTarget Number of target sequences found in the database
     * @param lastModified a long indicating the last time the indexed file was
     * modified
     * @param mainDatabaseType the main database type
     * @param databaseTypes map of all the database types and how often they
     * occur
     * @param decoyTag the decoy tag
     * @param version the database version
     * @param speciesOccurrence the species occurrence in the database
     */
    public FastaIndex(HashMap<String, Long> indexes, HashSet<String> decoyAccessions, String fileName, String name,
            boolean concatenatedTargetDecoy, boolean isDefaultReversed, int nTarget, long lastModified,
            DatabaseType mainDatabaseType, HashMap<Header.DatabaseType, Integer> databaseTypes, String decoyTag, String version, HashMap<String, Integer> speciesOccurrence) {
        this.indexes = indexes;
        this.decoyAccessions = decoyAccessions;
        this.fileName = fileName;
        this.name = name;
        this.concatenatedTargetDecoy = concatenatedTargetDecoy;
        this.isDefaultReversed = isDefaultReversed;
        this.nTarget = nTarget;
        this.lastModified = lastModified;
        this.mainDatabaseType = mainDatabaseType;
        this.databaseTypes = databaseTypes;
        this.decoyTag = decoyTag;
        this.version = version;
        this.speciesOccurrence = speciesOccurrence;
    }

    /**
     * Returns a map of all indexes of the FASTA file (accession &gt; index).
     *
     * @return a map of all indexes of the FASTA file (accession &gt; index)
     */
    public HashMap<String, Long> getIndexes() {
        return indexes;
    }

    /**
     * Returns true if the given accession number is a decoy.
     *
     * @param accession the accession number to check
     * 
     * @return true if the given accession number is a decoy
     */
    public boolean isDecoy(String accession) {
        return decoyAccessions.contains(accession);
    }

    /**
     * Returns the list of decoy accessions.
     *
     * @return the list of decoy accessions
     */
    public HashSet<String> getDecoyAccesions() {
        return decoyAccessions;
    }

    /**
     * Returns the index of the accession of interest.
     *
     * @param accession the accession of interest
     * @return the index of the accession of interest
     */
    public Long getIndex(String accession) {
        return indexes.get(accession);
    }

    /**
     * Returns the file name of the indexed FASTA file.
     *
     * @return the file name of the indexed FASTA file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Indicates whether the decoy sequences are reversed versions of the target
     * and the decoy accessions built based on the sequence factory methods. See
     * getDefaultDecoyAccession(String targetAccession) in SequenceFactory.
     *
     * @return true if the decoy sequences are reversed versions of the target
     * and the decoy accessions built based on the sequence factory methods
     */
    public boolean isDefaultReversed() {
        return isDefaultReversed;
    }

    /**
     * Returns the number of target sequences in the database.
     *
     * @return the number of target sequences in the database
     */
    public int getNTarget() {
        return nTarget;
    }

    /**
     * Returns the number of sequences in the database.
     *
     * @return the number of sequences in the databases.
     */
    public int getNSequences() {
        return indexes.size();
    }

    /**
     * Returns when the file was last modified. Null if not set or for utilities
     * versions older than 3.11.30.
     *
     * @return a long indicating when the file was last modified
     */
    public Long getLastModified() {
        return lastModified;
    }

    /**
     * Returns the main database type.
     *
     * @return the main database type.
     */
    public DatabaseType getMainDatabaseType() {
        return mainDatabaseType;
    }

    /**
     * Sets the main database type.
     *
     * @param mainDatabaseType the main database type
     */
    public void setMainDatabaseType(DatabaseType mainDatabaseType) {
        this.mainDatabaseType = mainDatabaseType;
    }

    /**
     * Returns the map of the database types and how often they occur.
     *
     * @return the map of the database types
     */
    public HashMap<Header.DatabaseType, Integer> getDatabaseTypes() {
        return databaseTypes;
    }

    /**
     * Set the database types map.
     *
     * @param databaseTypes the database types
     */
    public void setDatabaseTypes(HashMap<Header.DatabaseType, Integer> databaseTypes) {
        this.databaseTypes = databaseTypes;
    }

    /**
     * Returns the database version.
     *
     * @return the database version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the database version.
     *
     * @param version the database version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the description for this database.
     * 
     * @return the description for this database
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this database.
     * 
     * @param description the description for this database
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the accession parsing rule to use for this file.
     * 
     * @return the accession parsing rule to use for this file
     */
    public String getAccessionParsingRule() {
        return accessionParsingRule;
    }

    /**
     * Sets the accession parsing rule to use for this file.
     * 
     * @param accessionParsingRule the accession parsing rule to use for this file
     */
    public void setAccessionParsingRule(String accessionParsingRule) {
        this.accessionParsingRule = accessionParsingRule;
    }

    /**
     * Indicates whether the database is a concatenated target/decoy database.
     *
     * @return whether the database is a concatenated target/decoy database
     */
    public boolean isConcatenatedTargetDecoy() {
        return concatenatedTargetDecoy;
    }

    /**
     * Sets whether the database is a concatenated target/decoy database.
     *
     * @param concatenatedTargetDecoy whether the database is a concatenated
     * target/decoy database
     */
    public void setConcatenatedTargetDecoy(boolean concatenatedTargetDecoy) {
        this.concatenatedTargetDecoy = concatenatedTargetDecoy;
    }

    /**
     * Returns the decoy tag.
     *
     * @return sets the decoy tag
     */
    public String getDecoyTag() {
        return decoyTag;
    }

    /**
     * Sets the decoy tag.
     *
     * @param decoyTag the decoy tag
     */
    public void setDecoyTag(String decoyTag) {
        this.decoyTag = decoyTag;
    }

    /**
     * Returns the name of the database.
     *
     * @return the name for the database
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new name for the database.
     *
     * @param name a new name for the database
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the default version based on the time the file was last modified.
     * Default version is the date "dd.MM.yyyy".
     *
     * @param lastModified long indicating when the database was last modified
     * @return the default version
     */
    public static String getDefaultVersion(long lastModified) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastModified);
        int day = calendar.get(Calendar.DAY_OF_MONTH),
                month = calendar.get(Calendar.MONTH) + 1,
                year = calendar.get(Calendar.YEAR);
        return day + "." + month + "." + year;
    }

    /**
     * Returns the species targeted by this database. Note: this is not
     * necessarily an exhaustive list of the species in the database.
     *
     * @return the species targeted by this database
     */
    public HashMap<String, Integer> getSpecies() {
        return speciesOccurrence;
    }

    /**
     * Sets the species targeted by this database.
     *
     * @param species the species targeted by this database
     */
    public void setSpecies(HashMap<String, Integer> species) {
        this.speciesOccurrence = species;
    }
}
