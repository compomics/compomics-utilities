package com.compomics.util.experiment.quantification;

import com.compomics.util.Util;
import com.compomics.util.db.ObjectsDB;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.experiment.quantification.matches.PeptideQuantification;
import com.compomics.util.experiment.quantification.matches.ProteinQuantification;
import com.compomics.util.experiment.quantification.matches.PsmQuantification;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;

/**
 * This class uses a database to manage quantification matches.
 *
 * @author Marc Vaudel
 */
public class QuantificationDB implements Serializable {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 4600091320219653637L;
    /**
     * The name which will be used for the database.
     */
    public static final String dbName = "utilitiesQuantDB";
    /**
     * The name of the protein table.
     */
    private String proteinTableName = "proteins";
    /**
     * The suffix for protein parameters tables.
     */
    private String proteinParametersTableSuffix = "_protein_parameters";
    /**
     * The suffix of a peptide table.
     */
    private String peptideTableName = "peptides";
    /**
     * The suffix for a peptide parameters table.
     */
    private String peptideParametersTableSuffix = "_peptide_parameters";
    /**
     * The suffix for a PSM table.
     */
    private String psmTableSuffix = "_psms";
    /**
     * The suffix for a PSM parameters table.
     */
    private String psmParametersTableSuffix = "_psm_parameters";
    /**
     * List of all psms tables.
     */
    private ArrayList<String> psmTables = new ArrayList<String>();
    /**
     * List of all psm parameters tables.
     */
    private ArrayList<String> psmParametersTables = new ArrayList<String>();
    /**
     * List of all peptide parameters tables.
     */
    private ArrayList<String> peptideParametersTables = new ArrayList<String>();
    /**
     * List of all proteins parameters tables.
     */
    private ArrayList<String> proteinParametersTables = new ArrayList<String>();
    /**
     * The maximal size for a BLOB match in the database.
     */
    public static final String matchSize = "128k";
    /**
     * The maximal size for a BLOB parameter match in the database.
     */
    public static final String parametersSize = "8k";
    /**
     * The database which will contain the objects.
     */
    private ObjectsDB objectsDB;

    /**
     * Constructor creating the database and the protein and protein parameters
     * tables.
     *
     * @param folder the folder where to put the database
     * @throws SQLException an exception thrown whenever an error occurred while
     * creating the database
     */
    public QuantificationDB(String folder) throws SQLException {

        File dbFolder = new File(folder, dbName);
        if (dbFolder.exists()) {
            Util.deleteDir(dbFolder);
        }
        objectsDB = new ObjectsDB(folder, dbName);
        objectsDB.addTable(proteinTableName, matchSize);
        objectsDB.addTable(peptideTableName, matchSize);
    }

    /**
     * Indicates whether a spectrum match is loaded.
     *
     * @param spectrumKey the spectrumMatch key
     * @return a boolean indicating whether a spectrum match is loaded in the
     * given table
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     */
    public boolean spectrumMatchInDB(String spectrumKey) throws SQLException {
        String tableName = getSpectrumMatchTable(spectrumKey);
        return objectsDB.inDB(tableName, spectrumKey);
    }

    /**
     * Indicates whether a peptide match is loaded.
     *
     * @param peptideKey the peptide key
     * @return a boolean indicating whether a peptide match is loaded in the
     * given table
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     */
    public boolean peptideMatchInDB(String peptideKey) throws SQLException {
        return objectsDB.inDB(peptideTableName, peptideKey);
    }

    /**
     * Indicates whether a protein match is loaded.
     *
     * @param proteinKey the protein key
     * @return a boolean indicating whether a protein match is loaded in the
     * given table
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     */
    public boolean proteinMatchInDB(String proteinKey) throws SQLException {
        return objectsDB.inDB(proteinTableName, proteinKey);
    }

    /**
     * Updates a protein match.
     *
     * @param proteinMatch the protein match
     * @throws SQLException exception thrown whenever an error occurred while
     * updating a match in the table
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void updateProteinMatch(ProteinQuantification proteinMatch) throws SQLException, IOException {
        objectsDB.updateObject(proteinTableName, proteinMatch.getKey(), proteinMatch);
    }

    /**
     * Updates a peptide match.
     *
     * @param peptideMatch the peptide match
     * @throws SQLException exception thrown whenever an error occurred while
     * updating a match in the table
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void updatePeptideMatch(PeptideQuantification peptideMatch) throws SQLException, IOException {
        objectsDB.updateObject(peptideTableName, peptideMatch.getKey(), peptideMatch);
    }

    /**
     * Updates a spectrum match.
     *
     * @param spectrumMatch the spectrum match
     * @throws SQLException exception thrown whenever an error occurred while
     * updating a match in the table
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void updateSpectrumMatch(PsmQuantification spectrumMatch) throws SQLException, IOException {
        String key = spectrumMatch.getKey();
        String tableName = getSpectrumMatchTable(key);
        objectsDB.updateObject(tableName, key, spectrumMatch);
    }

    /**
     * Updates a match.
     *
     * @param match the match to update
     * @throws SQLException exception thrown whenever an error occurred while
     * updating a match in the table
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void updateMatch(QuantificationMatch match) throws SQLException, IOException {
        switch (match.getType()) {
            case Spectrum:
                updateSpectrumMatch((PsmQuantification) match);
                return;
            case Peptide:
                updatePeptideMatch((PeptideQuantification) match);
                return;
            case Protein:
                updateProteinMatch((ProteinQuantification) match);
        }
    }

    /**
     * Updates a protein match parameter.
     *
     * @param key the key of the protein match
     * @param urParameter the parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * updating the parameter in the table
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void updateProteinParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        String tableName = getProteinParameterTable(urParameter);
        objectsDB.updateObject(tableName, key, urParameter);
    }

    /**
     * Updates a peptide match parameter.
     *
     * @param key the key of the peptide match
     * @param urParameter the parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * updating the parameter in the table
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void updatePeptideParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        String tableName = getPeptideParameterTable(urParameter);
        objectsDB.updateObject(tableName, key, urParameter);
    }

    /**
     * Updates a spectrum match parameter.
     *
     * @param key the key of the spectrum match
     * @param urParameter the parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * updating the parameter in the table
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void updateSpectrumParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        String tableName = getSpectrumParameterTable(key, urParameter);
        objectsDB.updateObject(tableName, key, urParameter);
    }

    /**
     * Deletes a protein match from the database.
     *
     * @param key the key of the match
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     */
    public void removeProteinMatch(String key) throws SQLException {
        objectsDB.deleteObject(proteinTableName, key);
        for (String proteinParameterTable : proteinParametersTables) {
            objectsDB.deleteObject(proteinParameterTable, key);
        }
    }

    /**
     * Deletes a peptide match from the database.
     *
     * @param key the key of the match
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     */
    public void removePeptideMatch(String key) throws SQLException {
        objectsDB.deleteObject(peptideTableName, key);
        for (String peptideParameterTable : peptideParametersTables) {
            objectsDB.deleteObject(peptideParameterTable, key);
        }
    }

    /**
     * Deletes a spectrum match from the database.
     *
     * @param key the key of the match
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     */
    public void removeSpectrumMatch(String key) throws SQLException {
        for (String psmTable : psmTables) {
            objectsDB.deleteObject(psmTable, key);
        }
        for (String psmParameterTable : psmParametersTables) {
            objectsDB.deleteObject(psmParameterTable, key);
        }
    }

    /**
     * Returns the desired spectrum match.
     *
     * @param key the psm key
     * @return the spectrum match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public PsmQuantification getSpectrumMatch(String key) throws SQLException, IOException, ClassNotFoundException {
        String tableName = getSpectrumMatchTable(key);
        return (PsmQuantification) objectsDB.retrieveObject(tableName, key);
    }

    /**
     * Adds a spectrum match to the database.
     *
     * @param spectrumMatch the spectrum match to be added
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addSpectrumMatch(PsmQuantification spectrumMatch) throws SQLException, IOException {
        String key = spectrumMatch.getKey();
        String tableName = getSpectrumMatchTable(key);
        if (!psmTables.contains(tableName)) {
            objectsDB.addTable(tableName, matchSize);
            psmTables.add(tableName);
        }
        if (spectrumMatchInDB(key)) {
            updateMatch(spectrumMatch);
        } else {
            objectsDB.insertObject(tableName, key, spectrumMatch);
        }
    }

    /**
     * Returns the desired peptide match.
     *
     * @param key the peptide key
     * @return the peptide match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public PeptideQuantification getPeptideMatch(String key) throws SQLException, IOException, ClassNotFoundException {
        return (PeptideQuantification) objectsDB.retrieveObject(peptideTableName, key);
    }

    /**
     * Adds a peptide match to the database.
     *
     * @param peptideMatch the peptide match to be added
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addPeptideMatch(PeptideQuantification peptideMatch) throws SQLException, IOException {
        if (peptideMatchInDB(peptideMatch.getKey())) {
            updatePeptideMatch(peptideMatch);
        } else {
            objectsDB.insertObject(peptideTableName, peptideMatch.getKey(), peptideMatch);
        }
    }

    /**
     * Returns the desired protein match.
     *
     * @param key the protein key
     * @return the protein match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public ProteinQuantification getProteinMatch(String key) throws SQLException, IOException, ClassNotFoundException {
        return (ProteinQuantification) objectsDB.retrieveObject(proteinTableName, key);
    }

    /**
     * Adds a protein match to the database.
     *
     * @param proteinMatch the protein match to be added
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addProteinMatch(ProteinQuantification proteinMatch) throws SQLException, IOException {
        if (proteinMatchInDB(proteinMatch.getKey())) {
            updateProteinMatch(proteinMatch);
        } else {
            objectsDB.insertObject(proteinTableName, proteinMatch.getKey(), proteinMatch);
        }
    }

    /**
     * Returns the desired spectrum match parameter.
     *
     * @param key the psm key
     * @param urParameter the match parameter
     * @return the spectrum match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public UrParameter getSpectrumMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, ClassNotFoundException {
        String tableName = getSpectrumParameterTable(key, urParameter);
        return (UrParameter) objectsDB.retrieveObject(tableName, key);
    }

    /**
     * Adds a spectrum match parameter to the database.
     *
     * @param key the psm key
     * @param urParameter the match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addSpectrumMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        String tableName = getSpectrumParameterTable(key, urParameter);
        if (!psmParametersTables.contains(tableName)) {
            objectsDB.addTable(tableName, parametersSize);
            psmParametersTables.add(tableName);
        }
        objectsDB.insertObject(tableName, key, urParameter);
    }

    /**
     * Returns the desired peptide match parameter.
     *
     * @param key the peptide key
     * @param urParameter the match parameter
     * @return the peptide match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public UrParameter getPeptideMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, ClassNotFoundException {
        String tableName = getPeptideParameterTable(urParameter);
        return (UrParameter) objectsDB.retrieveObject(tableName, key);
    }

    /**
     * Adds a peptide match parameter to the database.
     *
     * @param key the peptide key
     * @param urParameter the match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addPeptideMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        String tableName = getPeptideParameterTable(urParameter);
        if (!peptideParametersTables.contains(tableName)) {
            objectsDB.addTable(tableName, parametersSize);
            peptideParametersTables.add(tableName);
        }
        objectsDB.insertObject(tableName, key, urParameter);
    }

    /**
     * Returns the desired protein match parameter.
     *
     * @param key the protein key
     * @param urParameter the match parameter
     * @return the protein match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public UrParameter getProteinMatchPArameter(String key, UrParameter urParameter) throws SQLException, IOException, ClassNotFoundException {
        String tableName = getProteinParameterTable(urParameter);
        return (UrParameter) objectsDB.retrieveObject(tableName, key);
    }

    /**
     * Adds a protein match parameter to the database.
     *
     * @param key the protein key
     * @param urParameter the match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addProteinMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        String tableName = getProteinParameterTable(urParameter);
        if (!proteinParametersTables.contains(tableName)) {
            objectsDB.addTable(tableName, parametersSize);
            proteinParametersTables.add(tableName);
        }
        objectsDB.insertObject(tableName, key, urParameter);
    }

    /**
     * Returns the table name associated to the given spectrum key.
     *
     * @param spectrumKey the given spectrum key
     * @return the table name of the given spectrum
     */
    public String getSpectrumMatchTable(String spectrumKey) {
        String tableName = Spectrum.getSpectrumFile(spectrumKey) + psmTableSuffix;
        tableName = objectsDB.correctTableName(tableName);
        return tableName;
    }

    /**
     * Returns the table name associated to the given spectrum parameter.
     *
     * @param spectrumKey the given spectrum key
     * @param urParameter the parameter
     * @return the table name of the given spectrum parameter
     */
    public String getSpectrumParameterTable(String spectrumKey, UrParameter urParameter) {
        String fileName = Spectrum.getSpectrumFile(spectrumKey);
        String tableName = ExperimentObject.getParameterKey(urParameter) + "_" + fileName + psmParametersTableSuffix;
        tableName = objectsDB.correctTableName(tableName);
        return tableName;
    }

    /**
     * Returns the table name associated to the given peptide parameter.
     *
     * @param urParameter the parameter
     * @return the table name of the given peptide parameter
     */
    public String getPeptideParameterTable(UrParameter urParameter) {
        String tableName = ExperimentObject.getParameterKey(urParameter) + peptideParametersTableSuffix;
        tableName = objectsDB.correctTableName(tableName);
        return tableName;
    }

    /**
     * Returns the table name associated to the given protein parameter.
     *
     * @param urParameter the parameter
     * @return the table name of the given protein parameter
     */
    public String getProteinParameterTable(UrParameter urParameter) {
        String tableName = ExperimentObject.getParameterKey(urParameter) + proteinParametersTableSuffix;
        tableName = objectsDB.correctTableName(tableName);
        return tableName;
    }

    /**
     * Closes the db connection.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     */
    public void close() throws SQLException {
        objectsDB.close();
    }

    /**
     * Establishes connection to the database.
     *
     * @param dbFolder the absolute path to the folder where the database is
     * located
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection
     */
    public void establishConnection(String dbFolder) throws SQLException {
        objectsDB.establishConnection(dbFolder);
    }

    /**
     * Adds an quantification match to the database.
     *
     * @param match the match to be added
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addMatch(QuantificationMatch match) throws SQLException, IOException {
        switch (match.getType()) {
            case Spectrum:
                addSpectrumMatch((PsmQuantification) match);
                return;
            case Peptide:
                addPeptideMatch((PeptideQuantification) match);
                return;
            case Protein:
                addProteinMatch((ProteinQuantification) match);
        }
    }
}
