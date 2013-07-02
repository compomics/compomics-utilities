package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.protein.Header.DatabaseType;
import java.util.Calendar;
import java.util.HashMap;

/**
 * This class contains the index of a FASTA file.
 *
 * @author Marc Vaudel
 */
public class FastaIndex extends ExperimentObject {

    /**
     * The indexes of the inspected FASTA file.
     */
    private HashMap<String, Long> indexes;
    /**
     * The FASTA file name.
     */
    private String fileName;
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
     * The database type.
     */
    private DatabaseType databaseType = DatabaseType.Unknown;
    /**
     * The version of the database.
     */
    private String version;
    /**
     * Indicates whether the database is a concatenated target/decoy.
     */
    private boolean concatenatedTargetDecoy;
    /**
     * The tag used for decoy sequences.
     */
    private String decoyTag;

    /**
     * Constructor.
     *
     * @param indexes The indexes of the inspected FASTA file
     * @param fileName The FASTA file name
     * @param concatenatedTargetDecoy If the FASTA file is a concatenated
     * target/decoy database
     * @param isDefaultReversed is this a default reversed database
     * @param nTarget Number of target sequences found in the database
     * @param lastModified a long indicating the last time the indexed file was
     * modified
     * @param databaseType the database type
     * @param decoyTag the decoy tag
     * @param version the database version
     */
    public FastaIndex(HashMap<String, Long> indexes, String fileName, boolean concatenatedTargetDecoy, boolean isDefaultReversed, 
            int nTarget, long lastModified, DatabaseType databaseType, String decoyTag, String version) {
        this.indexes = indexes;
        this.fileName = fileName;
        this.concatenatedTargetDecoy = concatenatedTargetDecoy;
        this.isDefaultReversed = isDefaultReversed;
        this.nTarget = nTarget;
        this.lastModified = lastModified;
        this.databaseType = databaseType;
        this.decoyTag = decoyTag;
        this.version = version;
    }

    /**
     * Returns a map of all indexes of the FASTA file (accession -> index).
     *
     * @return a map of all indexes of the FASTA file (accession -> index)
     */
    public HashMap<String, Long> getIndexes() {
        return indexes;
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
     * Returns the file name of the inspected FASTA file.
     *
     * @return the file name of the inspected FASTA file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns a boolean indicating whether the database ius a concatenated
     * target/decoy database.
     *
     * @return a boolean indicating whether the database ius a concatenated
     * target/decoy database
     */
    public boolean isDecoy() {
        return concatenatedTargetDecoy;
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
     * Indicates the database type.
     *
     * @return the database type.
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Sets the database type.
     *
     * @param databaseType the database type
     */
    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
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
     * Returns the default version based on the time the file was last modified.
     * Default version is the date "DD.MM.YYYY".
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
}