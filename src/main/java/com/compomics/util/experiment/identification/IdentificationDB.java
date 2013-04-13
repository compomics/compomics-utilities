package com.compomics.util.experiment.identification;

import com.compomics.util.db.ObjectsCache;
import com.compomics.util.db.ObjectsDB;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.gui.waiting.WaitingHandler;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class uses a database to manage identification matches.
 *
 * @author Marc Vaudel
 */
public class IdentificationDB implements Serializable {

    static final long serialVersionUID = 691986038787590646L; // @TODO: update??
    /**
     * The name which will be used for the database.
     */
    public String dbName;
    /**
     * The name of the protein table.
     */
    private String proteinTableName = "proteins";
    /**
     * The suffix for protein parameters tables.
     */
    private String proteinParametersTableSuffix = "_protein_parameters";
    /**
     * The name of the peptide table.
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
     * The suffix for a generic parameter table.
     *
     * @deprecated use match specific mapping instead
     */
    private String parametersSuffix = "_parameters";
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
     * List of all match parameters tables.
     *
     * @deprecated use match specific mapping instead
     */
    private ArrayList<String> matchParametersTables = new ArrayList<String>();
    /**
     * The database which will contain the objects.
     */
    private ObjectsDB objectsDB;

    /**
     * Constructor creating the database and the protein and protein parameters
     * tables.
     *
     * @param folder the folder where to put the database
     * @param name
     * @param deleteOldDatabase if true, tries to delete the old database
     * @param objectCache
     * @throws SQLException an exception thrown whenever an error occurred while
     * creating the database
     */
    public IdentificationDB(String folder, String name, boolean deleteOldDatabase, ObjectsCache objectCache) throws SQLException {
        this.dbName = name;
        objectsDB = new ObjectsDB(folder, dbName, deleteOldDatabase, objectCache);
        if (deleteOldDatabase) {
            objectsDB.addTable(proteinTableName);
            objectsDB.addTable(peptideTableName);
        }
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
    public boolean spectrumMatchLoaded(String spectrumKey) throws SQLException {
        String tableName = getSpectrumMatchTable(spectrumKey);
        return objectsDB.inDB(tableName, spectrumKey, true);
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
    public boolean peptideMatchLoaded(String peptideKey) throws SQLException {
        return objectsDB.inDB(peptideTableName, peptideKey, true);
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
    public boolean proteinMatchLoaded(String proteinKey) throws SQLException {
        return objectsDB.inDB(proteinTableName, proteinKey, true);
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
    public void updateProteinMatch(ProteinMatch proteinMatch) throws SQLException, IOException {
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
    public void updatePeptideMatch(PeptideMatch peptideMatch) throws SQLException, IOException {
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
    public void updateSpectrumMatch(SpectrumMatch spectrumMatch) throws SQLException, IOException {
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
    public void updateMatch(IdentificationMatch match) throws SQLException, IOException {
        switch (match.getType()) {
            case Spectrum:
                updateSpectrumMatch((SpectrumMatch) match);
                return;
            case Peptide:
                updatePeptideMatch((PeptideMatch) match);
                return;
            case Protein:
                updateProteinMatch((ProteinMatch) match);
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
     * @throws IOException
     */
    public void removeProteinMatch(String key) throws SQLException, IOException {
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
     * @throws IOException
     */
    public void removePeptideMatch(String key) throws SQLException, IOException {
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
     * @throws IOException
     */
    public void removeSpectrumMatch(String key) throws SQLException, IOException {
        for (String psmTable : psmTables) {
            objectsDB.deleteObject(psmTable, key);
        }
        for (String psmParameterTable : psmParametersTables) {
            objectsDB.deleteObject(psmParameterTable, key);
        }
    }

    /**
     * Deletes a match from the database.
     *
     * @param key the key of the match
     * @deprecated it is advised to use the specific psm/peptide/protein method
     * instead
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException
     */
    public void removeMatch(String key) throws SQLException, IOException {
        removeProteinMatch(key);
        removePeptideMatch(key);
        removeSpectrumMatch(key);
    }

    /**
     * Returns the desired spectrum match.
     *
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @param key the psm key
     * @return the spectrum match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public SpectrumMatch getSpectrumMatch(String key, boolean useDB) throws SQLException, IOException, ClassNotFoundException {
        String tableName = getSpectrumMatchTable(key);
        return (SpectrumMatch) objectsDB.retrieveObject(tableName, key, useDB);
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
    public void addSpectrumMatch(SpectrumMatch spectrumMatch) throws SQLException, IOException {
        String key = spectrumMatch.getKey();
        String tableName = getSpectrumMatchTable(key);
        if (!psmTables.contains(tableName)) {
            objectsDB.addTable(tableName);
            psmTables.add(tableName);
        }
        objectsDB.insertObject(tableName, key, spectrumMatch, true);
    }

    /**
     * Returns the desired peptide match.
     *
     * @param key the peptide key
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @return the peptide match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public PeptideMatch getPeptideMatch(String key, boolean useDB) throws SQLException, IOException, ClassNotFoundException {
        return (PeptideMatch) objectsDB.retrieveObject(peptideTableName, key, useDB);
    }

    /**
     * Adds a peptide match to the database
     *
     * @param peptideMatch the peptide match to be added
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addPeptideMatch(PeptideMatch peptideMatch) throws SQLException, IOException {
        objectsDB.insertObject(peptideTableName, peptideMatch.getKey(), peptideMatch, true);
    }

    /**
     * Returns the desired protein match.
     *
     * @param key the protein key
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @return the protein match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public ProteinMatch getProteinMatch(String key, boolean useDB) throws SQLException, IOException, ClassNotFoundException {
        return (ProteinMatch) objectsDB.retrieveObject(proteinTableName, key, useDB);
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
    public void addProteinMatch(ProteinMatch proteinMatch) throws SQLException, IOException {
        objectsDB.insertObject(proteinTableName, proteinMatch.getKey(), proteinMatch, true);
    }

    /**
     * Adds an identification match to the database.
     *
     * @param match the match to be added
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addMatch(IdentificationMatch match) throws SQLException, IOException {
        switch (match.getType()) {
            case Spectrum:
                addSpectrumMatch((SpectrumMatch) match);
                return;
            case Peptide:
                addPeptideMatch((PeptideMatch) match);
                return;
            case Protein:
                addProteinMatch((ProteinMatch) match);
        }
    }

    /**
     * Loads all peptide match parameters of the given type in the cache of the
     * database.
     *
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadPeptideMatchParameters(UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        String tableName = getPeptideParameterTable(urParameter);
        objectsDB.loadObjects(tableName, waitingHandler);
    }

    /**
     * Loads the desired peptide match parameters of the given type in the cache
     * of the database.
     *
     * @param peptideKeys the list of peptide keys of the parameters to load
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadPeptideMatchParameters(ArrayList<String> peptideKeys, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setSecondaryProgressValue(0);
            waitingHandler.setMaxSecondaryProgressValue(peptideKeys.size());
        }
        String tableName = getPeptideParameterTable(urParameter);
        objectsDB.loadObjects(tableName, peptideKeys, waitingHandler);
    }

    /**
     * Loads the desired peptide matches of the given type in the cache of the
     * database.
     *
     * @param peptideKeys the list of peptide keys to load
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadPeptideMatches(ArrayList<String> peptideKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setSecondaryProgressValue(0);
            waitingHandler.setMaxSecondaryProgressValue(peptideKeys.size());
        }
        objectsDB.loadObjects(peptideTableName, peptideKeys, waitingHandler);
    }

    /**
     * Loads all protein match parameters of the given type in the cache of the
     * database.
     *
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadProteinMatchParameters(UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        String tableName = getProteinParameterTable(urParameter);
        objectsDB.loadObjects(tableName, waitingHandler);
    }

    /**
     * Loads the desired protein match parameters of the given type in the cache
     * of the database.
     *
     * @param proteinKeys the list of protein keys of the parameters to load
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadProteinMatchParameters(ArrayList<String> proteinKeys, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setSecondaryProgressValue(0);
            waitingHandler.setMaxSecondaryProgressValue(proteinKeys.size());
        }
        String tableName = getProteinParameterTable(urParameter);
        objectsDB.loadObjects(tableName, proteinKeys, waitingHandler);
    }
    
    /**
     * Loads all protein matches in the cache of the database.
     *
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadProteinMatches(WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        objectsDB.loadObjects(proteinTableName, waitingHandler);
    }
    
    /**
     * Loads the desired protein matches of the given type in the cache of the
     * database.
     *
     * @param proteinKeys the list of protein keys to load
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadProteinMatches(ArrayList<String> proteinKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException, InterruptedException {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setSecondaryProgressValue(0);
            waitingHandler.setMaxSecondaryProgressValue(proteinKeys.size());
        }
        objectsDB.loadObjects(proteinTableName, proteinKeys, waitingHandler);
    }

    /**
     * Loads all peptide matches in the cache of the database.
     *
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadPeptideMatches(WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException, InterruptedException {
        objectsDB.loadObjects(peptideTableName, waitingHandler);
    }

    /**
     * Loads all spectrum matches of the given file in the cache of the
     * database.
     *
     * @param fileName the file name
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadSpectrumMatches(String fileName, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        String testKey = Spectrum.getSpectrumKey(fileName, "test");
        String tableName = getSpectrumMatchTable(testKey);
        objectsDB.loadObjects(tableName, waitingHandler);
    }

    /**
     * Loads all given spectrum matches in the cache of the database.
     *
     * @param spectrumKeys the key of the spectrum matches to be loaded
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadSpectrumMatches(ArrayList<String> spectrumKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setSecondaryProgressValue(0);
            waitingHandler.setMaxSecondaryProgressValue(2 * spectrumKeys.size());
        }
        HashMap<String, ArrayList<String>> sortedKeys = new HashMap<String, ArrayList<String>>();
        for (String spectrumKey : spectrumKeys) {
            String tableName = getSpectrumMatchTable(spectrumKey);
            if (!sortedKeys.containsKey(tableName)) {
                sortedKeys.put(tableName, new ArrayList<String>());
            }
            sortedKeys.get(tableName).add(spectrumKey);
            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }
        for (String tableName : sortedKeys.keySet()) {
            objectsDB.loadObjects(tableName, sortedKeys.get(tableName), waitingHandler);
        }
    }

    /**
     * Loads all spectrum match parameters of the given type in the cache of the
     * database.
     *
     * @param fileName the file name
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadSpectrumMatchParameters(String fileName, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        String testKey = Spectrum.getSpectrumKey(fileName, "test");
        String tableName = getSpectrumParameterTable(testKey, urParameter);
        objectsDB.loadObjects(tableName, waitingHandler);
    }

    /**
     * Loads all desired spectrum match parameters in the cache of the database.
     *
     * @param spectrumKeys the key of the spectrum match of the parameters to be
     * loaded
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadSpectrumMatchParameters(ArrayList<String> spectrumKeys, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setSecondaryProgressValue(0);
            waitingHandler.setMaxSecondaryProgressValue(2 * spectrumKeys.size());
        }
        HashMap<String, ArrayList<String>> sortedKeys = new HashMap<String, ArrayList<String>>();
        for (String spectrumKey : spectrumKeys) {
            String tableName = getSpectrumParameterTable(spectrumKey, urParameter);
            if (!sortedKeys.containsKey(tableName)) {
                sortedKeys.put(tableName, new ArrayList<String>());
            }
            sortedKeys.get(tableName).add(spectrumKey);
            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }
        for (String tableName : sortedKeys.keySet()) {
            objectsDB.loadObjects(tableName, sortedKeys.get(tableName), waitingHandler);
        }
    }

    /**
     * Returns the desired spectrum match parameter.
     *
     * @param key the psm key
     * @param urParameter the match parameter
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @return the spectrum match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public UrParameter getSpectrumMatchParameter(String key, UrParameter urParameter, boolean useDB) throws SQLException, IOException, ClassNotFoundException {
        String tableName = getSpectrumParameterTable(key, urParameter);
        return (UrParameter) objectsDB.retrieveObject(tableName, key, useDB);
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
            objectsDB.addTable(tableName);
            psmParametersTables.add(tableName);
        }
        objectsDB.insertObject(tableName, key, urParameter, true);
    }

    /**
     * Returns the desired peptide match parameter.
     *
     * @param key the peptide key
     * @param urParameter the match parameter
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @return the peptide match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public UrParameter getPeptideMatchParameter(String key, UrParameter urParameter, boolean useDB) throws SQLException, IOException, ClassNotFoundException {
        String tableName = getPeptideParameterTable(urParameter);
        return (UrParameter) objectsDB.retrieveObject(tableName, key, useDB);
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
            objectsDB.addTable(tableName);
            peptideParametersTables.add(tableName);
        }
        objectsDB.insertObject(tableName, key, urParameter, true);
    }

    /**
     * Returns the desired protein match parameter.
     *
     * @param key the protein key
     * @param urParameter the match parameter
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @return the protein match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public UrParameter getProteinMatchParameter(String key, UrParameter urParameter, boolean useDB) throws SQLException, IOException, ClassNotFoundException {
        String tableName = getProteinParameterTable(urParameter);
        return (UrParameter) objectsDB.retrieveObject(tableName, key, useDB);
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
            objectsDB.addTable(tableName);
            proteinParametersTables.add(tableName);
        }
        objectsDB.insertObject(tableName, key, urParameter, true);
    }

    /**
     * Returns the desired match parameter.
     *
     * @param key the match key
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @param urParameter the match parameter
     * @return the match match
     * @deprecated use match specific mapping instead
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public UrParameter getMatchPArameter(String key, UrParameter urParameter, boolean useDB) throws SQLException, IOException, ClassNotFoundException {
        String tableName = getParameterTable(urParameter);
        return (UrParameter) objectsDB.retrieveObject(tableName, key, useDB);
    }

    /**
     * Adds a protein match parameter to the database.
     *
     * @param key the protein key
     * @param urParameter the match parameter
     * @deprecated use match specific mapping instead
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        String tableName = getParameterTable(urParameter);
        if (!matchParametersTables.contains(tableName)) {
            objectsDB.addTable(tableName);
            matchParametersTables.add(tableName);
        }
        objectsDB.insertObject(tableName, key, urParameter, true);
    }

    /**
     * Returns the table name associated with the given spectrum key.
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
     * Returns the table name associated with the given spectrum parameter.
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
     * Returns the table name associated with the given peptide parameter.
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
     * Returns the table name associated with the given protein parameter.
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
     * Returns the table name associated with the given parameter.
     *
     * @param urParameter the parameter
     * @return the table name of the given protein parameter
     * @deprecated use match specific mapping instead
     */
    public String getParameterTable(UrParameter urParameter) {
        String tableName = ExperimentObject.getParameterKey(urParameter) + parametersSuffix;
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
}
