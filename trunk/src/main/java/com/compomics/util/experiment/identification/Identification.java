package com.compomics.util.experiment.identification;

import com.compomics.util.Util;
import com.compomics.util.db.ObjectsCache;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch.MatchType;
import com.compomics.util.experiment.identification.advocates.SearchEngine;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.gui.waiting.WaitingHandler;
import java.io.*;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * This class contains identification results.
 *
 * @author Marc Vaudel
 */
public abstract class Identification extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -2551700699384242554L; // @TODO: update??
    /**
     * Extention for a serialized hit. cuh for Compomics Utilities Hit.
     *
     * @deprecated use the database methods instead
     */
    public static final String EXTENTION = ".cuh";
    /**
     * List of the keys of all imported proteins.
     */
    protected ArrayList<String> proteinIdentification = new ArrayList<String>();
    /**
     * List of the keys of all imported peptides.
     */
    protected ArrayList<String> peptideIdentification = new ArrayList<String>();
    /**
     * List of all imported PSMs indexed by mgf file name.
     */
    protected HashMap<String, ArrayList<String>> spectrumIdentificationMap = new HashMap<String, ArrayList<String>>();
    /**
     * List of the keys of all imported PSMs.
     *
     * @deprecated use file specific mapping instead
     */
    protected ArrayList<String> spectrumIdentification = new ArrayList<String>();
    /**
     * A map linking protein accessions to all their protein matches keys.
     */
    protected HashMap<String, ArrayList<String>> proteinMap = new HashMap<String, ArrayList<String>>();
    /**
     * The method used.
     */
    protected int methodUsed;
    /**
     * The directory where matches will be serialized/the database stored.
     */
    protected String serializationDirectory;
    /**
     * Map of the user's parameters.
     *
     * @deprecated use the database instead
     */
    protected HashMap<String, HashMap<String, UrParameter>> urParameters = new HashMap<String, HashMap<String, UrParameter>>();
    /**
     * Map of long keys (>100 characters) which will be referenced by their
     * index for file creation/database storage.
     *
     * @deprecated use the database instead
     * @TODO implement this for db keys
     */
    protected ArrayList<String> longKeys = new ArrayList<String>();
    /**
     * Boolean indicating whether the matches should be stored in a database
     * (default) or in serialized files.
     */
    private Boolean isDB = true;
    /**
     * The identificationDB object interacting with the database.
     */
    private IdentificationDB identificationDB;
    /**
     * The reference of the identification.
     */
    protected String reference;
    /**
     * The ordered list of spectrum file names.
     */
    private ArrayList<String> orderedSpectrumFileNames;

    /**
     * Returns the ordered list of spectrum file names.
     *
     * @return the ordered list of spectrum file names
     */
    public ArrayList<String> getOrderedSpectrumFileNames() {
        if (orderedSpectrumFileNames == null) {
            orderedSpectrumFileNames = getSpectrumFiles();

            // default alphabetical ordering
            Collections.sort(orderedSpectrumFileNames);
        }

        return orderedSpectrumFileNames;
    }

    /**
     * Set the ordered list of spectrum file names. Note that the list provided
     * has to be the same size as the number of spectrum files used.
     *
     * @param orderedSpectrumFileNames the ordered list of spectrum file names
     * @throws IllegalArgumentException thrown if the length of the ordered file
     * names as to be the same as the number of spectrum files
     */
    public void setOrderedListOfSpectrumFileNames(ArrayList<String> orderedSpectrumFileNames) throws IllegalArgumentException {

        if (this.orderedSpectrumFileNames.size() != orderedSpectrumFileNames.size()) {
            throw new IllegalArgumentException("The length of the ordered file names as to be the same as the number of spectrum files. "
                    + orderedSpectrumFileNames.size() + "!=" + this.orderedSpectrumFileNames.size());
        }

        this.orderedSpectrumFileNames = orderedSpectrumFileNames;
    }

    /**
     * Returns the names of the mgf files used in the spectrum identification
     * map as a list. To get the complete file path use
     * projectDetails.getSpectrumFile(...).
     *
     * @return the mgf files used in the spectrum identification map
     */
    public ArrayList<String> getSpectrumFiles() {
        // compatibility check
        if (spectrumIdentificationMap == null) {
            updateSpectrumMapping();
        }
        return new ArrayList<String>(spectrumIdentificationMap.keySet());
    }

    /**
     * Returns the number of spectrum identifications.
     *
     * @return the number of spectrum identifications
     */
    public int getSpectrumIdentificationSize() {
        int result = 0;
        for (String spectrumFile : spectrumIdentificationMap.keySet()) {
            result += spectrumIdentificationMap.get(spectrumFile).size();
        }
        return result;
    }

    /**
     * Adds a parameter with a corresponding match key which will be loaded in
     * the memory. Use this method only for frequently used parameters,
     * otherwise attach the parameters to the matches.
     *
     * @param key the key of the parameter
     * @param urParameter the additional parameter
     * @deprecated use the database match specific methods instead
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        if (!isDB) {
            if (!urParameters.containsKey(key)) {
                urParameters.put(key, new HashMap<String, UrParameter>());
            }
            urParameters.get(key).put(ExperimentObject.getParameterKey(urParameter), urParameter);
        } else {
            identificationDB.addMatchParameter(key, urParameter);
        }
    }

    /**
     * Returns the personalization parameter of the given match.
     *
     * @param matchKey the match key
     * @param urParameter example of parameter to retrieve
     * @return the personalization parameter
     * @deprecated use the database match specific methods instead
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public UrParameter getMatchParameter(String matchKey, UrParameter urParameter) throws SQLException, IOException, ClassNotFoundException {
        if (!isDB) {
            return urParameters.get(matchKey).get(ExperimentObject.getParameterKey(urParameter));
        } else {
            return identificationDB.getMatchPArameter(matchKey, urParameter, true);
        }
    }

    /**
     * Loads all spectrum match parameters of the given type in the cache of the
     * database
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
    public void loadSpectrumMatchParameters(String fileName, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadSpectrumMatchParameters(fileName, urParameter, waitingHandler);
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
    public void loadSpectrumMatchParameters(ArrayList<String> spectrumKeys, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadSpectrumMatchParameters(spectrumKeys, urParameter, waitingHandler);
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
    public void loadPeptideMatchParameters(UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadPeptideMatchParameters(urParameter, waitingHandler);
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
    public void loadPeptideMatchParameters(ArrayList<String> peptideKeys, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadPeptideMatchParameters(peptideKeys, urParameter, waitingHandler);
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
    public void loadPeptideMatches(ArrayList<String> peptideKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadPeptideMatches(peptideKeys, waitingHandler);
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
    public void loadProteinMatchParameters(UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadProteinMatchParameters(urParameter, waitingHandler);
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
    public void loadProteinMatchParameters(ArrayList<String> proteinKeys, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadProteinMatchParameters(proteinKeys, urParameter, waitingHandler);
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
    public void loadProteinMatches(WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadProteinMatches(waitingHandler);
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
    public void loadProteinMatches(ArrayList<String> proteinKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadProteinMatches(proteinKeys, waitingHandler);
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
    public void loadPeptideMatches(WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadPeptideMatches(waitingHandler);
    }

    /**
     * Loads all spectrum matches of the file in the cache of the database
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
    public void loadSpectrumMatches(String fileName, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadSpectrumMatches(fileName, waitingHandler);
    }

    /**
     * Loads the given spectrum matches in the cache of the database.
     *
     * @param spectrumKeys the spectrum keys
     * @param waitingHandler the waiting handler
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadSpectrumMatches(ArrayList<String> spectrumKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException {
        identificationDB.loadSpectrumMatches(spectrumKeys, waitingHandler);
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
        return getSpectrumMatchParameter(key, urParameter, true);
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
        if (isDB) {
            return identificationDB.getSpectrumMatchParameter(key, urParameter, useDB);
        } else {
            return getMatchParameter(key, urParameter);
        }
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
        if (isDB) {
            identificationDB.addSpectrumMatchParameter(key, urParameter);
        } else {
            addMatchParameter(key, urParameter);
        }
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
        return getPeptideMatchParameter(key, urParameter, true);
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
        if (isDB) {
            return identificationDB.getPeptideMatchParameter(key, urParameter, useDB);
        } else {
            return getMatchParameter(key, urParameter);
        }
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
        if (isDB) {
            identificationDB.addPeptideMatchParameter(key, urParameter);
        } else {
            addMatchParameter(key, urParameter);
        }
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
    public UrParameter getProteinMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, ClassNotFoundException {
        return getProteinMatchParameter(key, urParameter, true);
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
        if (isDB) {
            return identificationDB.getProteinMatchParameter(key, urParameter, useDB);
        } else {
            return getMatchParameter(key, urParameter);
        }
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
        if (isDB) {
            identificationDB.addProteinMatchParameter(key, urParameter);
        } else {
            addMatchParameter(key, urParameter);
        }
    }

    /**
     * Updates a protein match parameter in the database.
     *
     * @param key the protein key
     * @param urParameter the match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updateProteinMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        if (isDB) {
            identificationDB.updateProteinParameter(key, urParameter);
        }
    }

    /**
     * Updates a peptide match parameter in the database.
     *
     * @param key the peptide key
     * @param urParameter the match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updatePeptideMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        if (isDB) {
            identificationDB.updatePeptideParameter(key, urParameter);
        }
    }

    /**
     * Updates a spectrum match parameter in the database.
     *
     * @param key the spectrum key
     * @param urParameter the match parameter
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updateSpectrumMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        if (isDB) {
            identificationDB.updateSpectrumParameter(key, urParameter);
        }
    }

    /**
     * Updates a spectrum match in the database.
     *
     * @param spectrumMatch the match
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updateSpectrumMatch(SpectrumMatch spectrumMatch) throws SQLException, IOException {
        if (isDB) {
            identificationDB.updateSpectrumMatch(spectrumMatch);
        }
    }

    /**
     * Updates a peptide match in the database.
     *
     * @param peptideMatch the match
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updatePeptideMatch(PeptideMatch peptideMatch) throws SQLException, IOException {
        if (isDB) {
            identificationDB.updatePeptideMatch(peptideMatch);
        }
    }

    /**
     * Updates a protein match in the database.
     *
     * @param proteinMatch the match
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updateProteinMatch(ProteinMatch proteinMatch) throws SQLException, IOException {
        if (isDB) {
            identificationDB.updateProteinMatch(proteinMatch);
        }
    }

    /**
     * Returns the serialization directory.
     *
     * @return the serialization directory
     */
    public String getSerializationDirectory() {
        return serializationDirectory;
    }

    /**
     * Sets the directory where matches will be stored in order to save memory.
     * Matches can be stored in a database (default) or serialized files. If the
     * database option is chosen (see setIsDB(Boolean isDB)) and no database
     * created, the database will be created in the folder.
     *
     * @param serializationDirectory the path of the directory
     * @param deleteOldDatabase if true, tries to delete the old database
     * @throws SQLException
     * @deprecated use establishConnection(String dbFolder) instead
     */
    public void setDirectory(String serializationDirectory, boolean deleteOldDatabase) throws SQLException {
        this.serializationDirectory = serializationDirectory;
    }

    /**
     * Removes a match from the model.
     *
     * @param matchKey the key of the match to remove
     * @deprecated it is advised to use the specific psm/peptide/protein method
     * instead
     * @throws IllegalArgumentException
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException
     */
    public void removeMatch(String matchKey) throws IllegalArgumentException, SQLException, IOException {

        if (proteinIdentification.contains(matchKey)) {
            for (String protein : ProteinMatch.getAccessions(matchKey)) {
                if (proteinMap.get(protein) == null) {
                    throw new IllegalArgumentException("Protein not found: " + protein + ".");
                } else {
                    if (proteinMap.get(protein).contains(matchKey)) {
                        proteinMap.get(protein).remove(matchKey);
                        if (proteinMap.get(protein).isEmpty()) {
                            proteinMap.remove(protein);
                        }
                    }
                }
            }
        }

        proteinIdentification.remove(matchKey);
        peptideIdentification.remove(matchKey);
        spectrumIdentification.remove(matchKey);
        String fileName = Spectrum.getSpectrumFile(matchKey);
        ArrayList<String> spectrumKeys = spectrumIdentificationMap.get(fileName);
        if (spectrumKeys != null) {
            spectrumKeys.remove(matchKey);
        }

        if (isDB) {
            identificationDB.removeMatch(matchKey);
        } else {
            File matchFile = new File(serializationDirectory, getFileName(matchKey));
            matchFile.delete();
        }
    }

    /**
     * Removes a spectrum match from the model.
     *
     * @param matchKey the key of the match to remove
     * @throws IllegalArgumentException
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException
     */
    public void removeSpectrumMatch(String matchKey) throws IllegalArgumentException, SQLException, IOException {

        spectrumIdentification.remove(matchKey);
        String fileName = Spectrum.getSpectrumFile(matchKey);
        ArrayList<String> spectrumKeys = spectrumIdentificationMap.get(fileName);
        if (spectrumKeys != null) {
            spectrumKeys.remove(matchKey);
        }
        if (isDB) {
            identificationDB.removeSpectrumMatch(matchKey);
        } else {
            File matchFile = new File(serializationDirectory, getFileName(matchKey));
            matchFile.delete();
        }
    }

    /**
     * Removes a peptide match from the model.
     *
     * @param matchKey the key of the match to remove
     * @throws IllegalArgumentException
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException
     */
    public void removePeptideMatch(String matchKey) throws IllegalArgumentException, SQLException, IOException {

        peptideIdentification.remove(matchKey);
        if (isDB) {
            identificationDB.removePeptideMatch(matchKey);
        } else {
            File matchFile = new File(serializationDirectory, getFileName(matchKey));
            matchFile.delete();
        }
    }

    /**
     * Removes a protein match from the model.
     *
     * @param matchKey the key of the match to remove
     * @throws IllegalArgumentException
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException
     */
    public void removeProteinMatch(String matchKey) throws IllegalArgumentException, SQLException, IOException {
        if (proteinIdentification.contains(matchKey)) {
            for (String protein : ProteinMatch.getAccessions(matchKey)) {
                if (proteinMap.get(protein) == null) {
                    throw new IllegalArgumentException("Protein not found: " + protein + ".");
                } else {
                    if (proteinMap.get(protein).contains(matchKey)) {
                        proteinMap.get(protein).remove(matchKey);
                        if (proteinMap.get(protein).isEmpty()) {
                            proteinMap.remove(protein);
                        }
                    }
                }
            }
        }

        proteinIdentification.remove(matchKey);

        if (isDB) {
            identificationDB.removeProteinMatch(matchKey);
        } else {
            File matchFile = new File(serializationDirectory, getFileName(matchKey));
            matchFile.delete();
        }
    }

    /**
     * Indicates whether a match indexed by the given key exists.
     *
     * @param matchKey the key of the match looked for
     * @return a boolean indicating whether a match indexed by the given key
     * exists
     */
    public boolean matchExists(String matchKey) {
        String fileName = Spectrum.getSpectrumFile(matchKey);
        ArrayList<String> spectrumKeys = spectrumIdentificationMap.get(fileName);
        if (spectrumKeys != null && spectrumKeys.contains(matchKey)) {
            return true;
        }
        return proteinIdentification.contains(matchKey) || peptideIdentification.contains(matchKey) || spectrumIdentification.contains(matchKey);
    }

    /**
     * Returns a match.
     *
     * @param matchKey the key of the match
     * @return the desired match
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while retrieving the match
     * @deprecated use the database match specific methods instead
     */
    private Object getMatch(String matchKey) throws IllegalArgumentException {
        return getMatch(matchKey, 0);
    }

    /**
     * Returns a match.
     *
     * @param matchKey the key of the match
     * @param the number of exceptions found. If less than 100, the method will
     * retry after a tempo of 50ms to avoid network related issues.
     * @return the desired match
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while retrieving the match
     * @deprecated use the database match specific methods instead
     */
    private synchronized IdentificationMatch getMatch(String matchKey, int errorCounter) throws IllegalArgumentException {

        try {
            File newMatch = new File(serializationDirectory, getFileName(matchKey));
            FileInputStream fis = new FileInputStream(newMatch);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream in = new ObjectInputStream(bis);
            IdentificationMatch match = (IdentificationMatch) in.readObject();
            fis.close();
            bis.close();
            in.close();
            return match;
        } catch (Exception e) {
            if (errorCounter <= 100) {
                try {
                    wait(50);
                } catch (InterruptedException ie) {
                }
                return getMatch(matchKey, errorCounter + 1);
            } else {
                e.printStackTrace();
                throw new IllegalArgumentException("Error while loading " + matchKey);
            }
        }
    }

    /**
     * Returns a spectrum match.
     *
     * @param spectrumKey the key of the match
     * @return the desired match
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while retrieving the match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public SpectrumMatch getSpectrumMatch(String spectrumKey) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
        return getSpectrumMatch(spectrumKey, true);
    }

    /**
     * Returns a spectrum match.
     *
     * @param spectrumKey the key of the match
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @return the desired match
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while retrieving the match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public SpectrumMatch getSpectrumMatch(String spectrumKey, boolean useDB) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
        if (isDB) {
            return identificationDB.getSpectrumMatch(spectrumKey, useDB);
        } else {
            return (SpectrumMatch) getMatch(spectrumKey);
        }
    }

    /**
     * Returns a peptide match.
     *
     * @param peptideKey the key of the match
     * @return the desired match
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while retrieving the match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public PeptideMatch getPeptideMatch(String peptideKey) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
        return getPeptideMatch(peptideKey, true);
    }

    /**
     * Returns a peptide match.
     *
     * @param peptideKey the key of the match
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @return the desired match
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while retrieving the match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public PeptideMatch getPeptideMatch(String peptideKey, boolean useDB) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
        if (isDB) {
            return identificationDB.getPeptideMatch(peptideKey, useDB);
        } else {
            return (PeptideMatch) getMatch(peptideKey);
        }
    }

    /**
     * Returns a protein match.
     *
     * @param proteinKey the key of the match
     * @return the desired match
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while retrieving the match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public ProteinMatch getProteinMatch(String proteinKey) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
        return getProteinMatch(proteinKey, true);
    }

    /**
     * Returns a protein match.
     *
     * @param proteinKey the key of the match
     * @param useDB if useDB is false, null will be returned if the object is not in the cache
     * @return the desired match
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while retrieving the match
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public ProteinMatch getProteinMatch(String proteinKey, boolean useDB) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
        if (isDB) {
            return identificationDB.getProteinMatch(proteinKey, useDB);
        } else {
            return (ProteinMatch) getMatch(proteinKey);
        }
    }
    
    /**
     * Indicates whether the protein, peptide and spectrum matches corresponding to a protein match key are loaded in the cache
     * Note, only one peptide and one spectrum matches are tested
     * 
     * @param proteinKey the key of the protein match
     * @return true if everything is loaded in memory
     * @throws IllegalArgumentException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public boolean proteinDetailsInCache(String proteinKey) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
        ProteinMatch proteinMatch = getProteinMatch(proteinKey, false);
        if (proteinMatch != null) {
            PeptideMatch peptideMatch = getPeptideMatch(proteinMatch.getPeptideMatches().get(0), false);
            if (peptideMatch != null) {
                SpectrumMatch spectrumMatch = getSpectrumMatch(peptideMatch.getSpectrumMatches().get(0), false);
                if (spectrumMatch != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Indicates whether the peptide and spectrum matches corresponding to a peptide match key are loaded in the cache
     * Note, only one one spectrum match is tested
     * 
     * @param peptideKey the peptide key
     * @return true if everything is loaded in the cache
     * @throws IllegalArgumentException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public boolean peptideDetailsInCache(String peptideKey) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
            PeptideMatch peptideMatch = getPeptideMatch(peptideKey, false);
            if (peptideMatch != null) {
                SpectrumMatch spectrumMatch = getSpectrumMatch(peptideMatch.getSpectrumMatches().get(0), false);
                if (spectrumMatch != null) {
                    return true;
                }
            }
        return false;
    }

    /**
     * Returns a list of the keys of all encountered proteins.
     *
     * @return the corresponding identification results
     */
    public ArrayList<String> getProteinIdentification() {
        return proteinIdentification;
    }

    /**
     * Returns a list of the keys of all encountered peptides.
     *
     * @return the corresponding identification results
     */
    public ArrayList<String> getPeptideIdentification() {
        return peptideIdentification;
    }

    /**
     * Returns a list of the keys of all encountered psms.
     *
     * @deprecated use file specific names instead
     * @return the corresponding identification results
     */
    public ArrayList<String> getSpectrumIdentification() {
        if (!spectrumIdentification.isEmpty()) {
            return spectrumIdentification;
        } else {
            ArrayList<String> result = new ArrayList<String>();
            for (String spectrumFile : spectrumIdentificationMap.keySet()) {
                result.addAll(spectrumIdentificationMap.get(spectrumFile));
            }
            return result;
        }
    }

    /**
     * Returns the spectrum identifications for a given spectrum file name.
     *
     * @param spectrumFile the name of the spectrum file
     * @return the corresponding list of identifications
     */
    public ArrayList<String> getSpectrumIdentification(String spectrumFile) {
        return spectrumIdentificationMap.get(spectrumFile);
    }

    /**
     * Returns the keys of all identified spectra indexed by the spectrum file.
     *
     * @return the keys of all identified spectra indexed by the spectrum file
     */
    public HashMap<String, ArrayList<String>> getSpectrumIdentificationMap() {
        return spectrumIdentificationMap;
    }

    /**
     * Adds a spectrum match to the identification.
     *
     * @param newMatch the new match
     * @throws FileNotFoundException exception thrown whenever an error occurred
     * while saving the file
     * @throws IOException exception thrown whenever an error occurred while
     * saving the file
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while saving the file
     * @throws SQLException exception thrown whenever an error occurred while
     * saving the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while saving the file
     */
    public void addSpectrumMatch(SpectrumMatch newMatch) throws FileNotFoundException, IOException, IllegalArgumentException, SQLException, ClassNotFoundException {
        String spectrumKey = newMatch.getKey();
        String spectrumFile = Spectrum.getSpectrumFile(spectrumKey);
        if (spectrumIdentificationMap.containsKey(spectrumFile) && spectrumIdentificationMap.get(spectrumFile).contains(spectrumKey)) {
            SpectrumMatch oldMatch = getSpectrumMatch(spectrumKey, true);
            if (oldMatch == null) {
                throw new IllegalArgumentException("Spectrum match " + spectrumKey + " not found.");
            }
            for (int searchEngine : newMatch.getAdvocates()) {
                oldMatch.addHit(searchEngine, newMatch.getFirstHit(searchEngine));
            }
            identificationDB.updateSpectrumMatch(oldMatch);
        } else {
            if (!spectrumIdentificationMap.containsKey(spectrumFile)) {
                spectrumIdentificationMap.put(spectrumFile, new ArrayList<String>());
            }
            spectrumIdentificationMap.get(spectrumFile).add(spectrumKey);
            try {
                identificationDB.addSpectrumMatch(newMatch);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error while writing spectrum match " + spectrumKey + " in the database.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error while writing spectrum match " + spectrumKey + " in the database.");
            }
        }
    }

    /**
     * Creates the peptides and protein instances based on the spectrum matches.
     * Note that the attribute bestAssumption should be set for every spectrum
     * match at this point. This operation will be very slow if the cache is
     * already full.
     *
     * @param waitingHandler the waiting handler displaying the progress. Can be
     * null. The progress will be displayed as secondary.
     * @throws IllegalArgumentException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void buildPeptidesAndProteins(WaitingHandler waitingHandler) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressValue(getSpectrumIdentificationSize());
            waitingHandler.setSecondaryProgressValue(0);
        }
        for (String spectrumFile : spectrumIdentificationMap.keySet()) {
            for (String spectrumMatchKey : spectrumIdentificationMap.get(spectrumFile)) {
                buildPeptidesAndProteins(spectrumMatchKey);
                if (waitingHandler != null) {
                    waitingHandler.increaseSecondaryProgressValue();
                    if (waitingHandler.isRunCanceled()) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Creates the peptides and protein instances based on the given spectrum
     * match. Note that the attribute bestAssumption should be set for every
     * spectrum match at this point. This operation will be very slow if the
     * cache is already full.
     *
     * @param spectrumMatchKey The key of the spectrum match to add
     * @throws IllegalArgumentException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public void buildPeptidesAndProteins(String spectrumMatchKey) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {

        SpectrumMatch othermatch, spectrumMatch = getSpectrumMatch(spectrumMatchKey);
        if (spectrumMatch == null) {
            throw new IllegalArgumentException("Spectrum match " + spectrumMatchKey + " not found.");
        }
        Peptide otherPeptide, peptide = spectrumMatch.getBestAssumption().getPeptide();
        String peptideKey = peptide.getKey();
        PeptideMatch peptideMatch;

        if (peptideIdentification.contains(peptideKey)) {
            peptideMatch = getPeptideMatch(peptideKey);
            if (peptideMatch == null) {
                throw new IllegalArgumentException("Peptide match " + peptideKey + " not found.");
            }
            // correct protein inference discrepancies between spectrum matches
            loadSpectrumMatches(peptideMatch.getSpectrumMatches(), null);

            for (String otherMatchKey : peptideMatch.getSpectrumMatches()) {
                othermatch = getSpectrumMatch(otherMatchKey); 
                if (othermatch == null) {
                    throw new IllegalArgumentException("Spectrum match " + otherMatchKey + " not found.");
                }
                otherPeptide = othermatch.getBestAssumption().getPeptide();
                for (String protein : otherPeptide.getParentProteins()) {
                    if (!peptide.getParentProteins().contains(protein)) {
                        peptide.getParentProteins().add(protein);
                    }
                }
                for (String protein : peptide.getParentProteins()) {
                    if (!otherPeptide.getParentProteins().contains(protein)) {
                        otherPeptide.getParentProteins().add(protein);
                    }
                }
            }
            peptideMatch.addSpectrumMatch(spectrumMatchKey);
            identificationDB.updatePeptideMatch(peptideMatch);
        } else {
            peptideMatch = new PeptideMatch(peptide, spectrumMatchKey);
            peptideIdentification.add(peptideKey);
            try {
                identificationDB.addPeptideMatch(peptideMatch);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error while writing peptide match " + peptideKey + " in the database.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error while writing peptide match " + peptideKey + " in the database.");
            }
        }

        String proteinKey = ProteinMatch.getProteinMatchKey(peptide);

        if (proteinIdentification.contains(proteinKey)) {
            ProteinMatch proteinMatch = getProteinMatch(proteinKey);
            if (proteinMatch == null) {
                throw new IllegalArgumentException("Protein match " + proteinKey + " not found.");
            }
            if (!proteinMatch.getPeptideMatches().contains(peptideKey)) {
                proteinMatch.addPeptideMatch(peptideKey);
                identificationDB.updateProteinMatch(proteinMatch);
            }
        } else {
            ProteinMatch proteinMatch = new ProteinMatch(peptideMatch.getTheoreticPeptide());
            if (!proteinMatch.getKey().equals(proteinKey)) {

                // @TODO: if one is a subset of the other, is it possible to simply use the biggest set for both??
                // @TODO: or would using peptide to protein remapping for all search engines solve the problem?? use ProteinTree?

                throw new IllegalArgumentException("Protein inference issue: the protein key " + proteinKey + " does not match the peptide proteins " + proteinMatch.getKey() + "."
                        + " Peptide: " + peptideKey + " found in spectrum " + spectrumMatchKey + " most likely a problem with "
                        + SearchEngine.getName(spectrumMatch.getBestAssumption().getAdvocate()) + ".");
            }
            proteinIdentification.add(proteinKey);
            for (String protein : peptide.getParentProteins()) {
                if (!proteinMap.containsKey(protein)) {
                    proteinMap.put(protein, new ArrayList<String>());
                }
                if (!proteinMap.get(protein).contains(proteinKey)) {
                    proteinMap.get(protein).add(proteinKey);
                }
            }
            try {
                identificationDB.addProteinMatch(proteinMatch);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error while writing protein match " + proteinKey + " in the database.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error while writing protein match " + proteinKey + " in the database.");
            }
        }
    }

    /**
     * Add a set of spectrumMatches to the model.
     *
     * @param spectrumMatches The spectrum matches
     * @throws FileNotFoundException exception thrown whenever an error occurred
     * while saving the file
     * @throws IOException exception thrown whenever an error occurred while
     * saving the file
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while saving the file
     * @throws SQLException exception thrown whenever an error occurred while
     * saving the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while saving the file
     */
    public void addSpectrumMatch(Set<SpectrumMatch> spectrumMatches) throws FileNotFoundException, IOException, IllegalArgumentException, SQLException, ClassNotFoundException {
        for (SpectrumMatch spectrumMatch : spectrumMatches) {
            addSpectrumMatch(spectrumMatch);
        }
    }

    /**
     * Getter for the identification method used.
     *
     * @return the identification method used
     */
    public int getMethodUsed() {
        return methodUsed;
    }

    /**
     * Returns a map of all the protein matches which can be ascribed to a
     * protein indexed by its accession.
     *
     * @return a map of all the protein matches which can be ascribed to a
     * protein indexed by its accession.
     */
    public HashMap<String, ArrayList<String>> getProteinMap() {
        return proteinMap;
    }

    /**
     * Returns the name of the file to use for serialization/deserialization.
     *
     * @param key the key of the match
     * @return the name of the corresponding file
     * @deprecated use the database methods instead
     */
    public String getFileName(String key) {

        for (String fc : Util.forbiddenCharacters) {
            String[] split = key.split(fc);
            key = "";
            for (String splitPart : split) {
                key += splitPart;
            }
        }
        if (key.length() < 100) {
            return key + EXTENTION;
        } else {
            int index = longKeys.indexOf(key);
            if (index == -1) {
                index = longKeys.size();
                longKeys.add(key);
            }
            return index + EXTENTION;
        }
    }

    /**
     * Indicates whether the identification matches should be stored in a
     * database (true, default value) or serialized files (false, deprecated
     * default).
     *
     * @return a boolean indicating whether the identification matches should be
     * stored in a database or serialized files
     */
    public Boolean isDB() {
        if (isDB == null) {
            isDB = false; //backward compatibility check
        }
        return isDB;
    }

    /**
     * Sets whether the identification matches should be stored in a database or
     * serialized files.
     *
     * @param isDB a boolean indicating whether the identification matches
     * should be stored in a database or serialized files
     */
    public void setIsDB(Boolean isDB) {
        this.isDB = isDB;
    }

    /**
     * Closes the database connection.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     */
    public void close() throws SQLException {
        if (isDB != null && isDB && identificationDB != null) {
            identificationDB.close();
        }
    }

    /**
     * Returns the kind of match pointed by the given key in the identification
     * mappings. Null if missing from the mapping.
     *
     * @param matchKey the match key
     * @return the kind of match
     */
    public MatchType getMatchType(String matchKey) {
        if (proteinIdentification.contains(matchKey)) {
            return MatchType.Protein;
        } else if (peptideIdentification.contains(matchKey)) {
            return MatchType.Peptide;
        } else if (spectrumIdentification.contains(matchKey)) {
            return MatchType.Spectrum;
        } else {
            String fileName = Spectrum.getSpectrumFile(matchKey);
            ArrayList<String> spectrumKeys = spectrumIdentificationMap.get(fileName);
            if (spectrumKeys != null && spectrumKeys.contains(matchKey)) {
                return MatchType.Spectrum;
            }
        }
        return null;
    }

    /**
     * Establishes a connection to the database.
     *
     * @param dbFolder the absolute path to the folder where the database is
     * located
     * @param deleteOldDatabase if true, tries to delete the old database
     * @param objectsCache
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection
     */
    public void establishConnection(String dbFolder, boolean deleteOldDatabase, ObjectsCache objectsCache) throws SQLException {
        identificationDB = new IdentificationDB(dbFolder, reference, deleteOldDatabase, objectsCache);
    }

    /**
     * Converts a serialization based structure into a database based one.
     * Replaces the space separation by the standard separator.
     *
     * @param waitingHandler the waiting handler
     * @param newDirectory the new directory where to store the data
     * @param newName
     * @param objectsCache
     * @param directory the directory where the data is currently stored
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a match
     * @throws SQLException exception thrown whenever an error occurred while
     * interacting with the database
     */
    public void convert(WaitingHandler waitingHandler, String newDirectory, String newName, ObjectsCache objectsCache, File directory)
            throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        setIsDB(true);
        reference = newName;
        establishConnection(newDirectory, true, objectsCache);

        File[] files = directory.listFiles();
        int nParameters = 0;
        for (HashMap<String, UrParameter> map : urParameters.values()) {
            nParameters += map.size();
        }
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressValue(files.length + nParameters);
        }

        for (File file : files) {
            if (file.getName().endsWith(EXTENTION)) {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream in = new ObjectInputStream(bis);
                IdentificationMatch match = (IdentificationMatch) in.readObject();
                fis.close();
                bis.close();
                in.close();
                file.delete();
                try {
                    identificationDB.addMatch(match);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IOException("Error while writing match " + match.getKey() + " in the database.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new SQLException("Error while writing match " + match.getKey() + " in the database.");
                }
            }
            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }
        for (String matchKey : urParameters.keySet()) {
            MatchType matchType = getMatchType(matchKey);
            for (UrParameter urParameter : urParameters.get(matchKey).values()) {
                if (matchType == MatchType.Protein) {
                    matchKey = matchKey.replaceAll(" ", ProteinMatch.PROTEIN_KEY_SPLITTER);
                    addProteinMatchParameter(matchKey, urParameter);
                } else if (matchType == MatchType.Peptide) {
                    addPeptideMatchParameter(matchKey, urParameter);
                } else if (matchType == MatchType.Spectrum) {
                    addSpectrumMatchParameter(matchKey, urParameter);
                }
            }
            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
            }
            if (waitingHandler.isRunCanceled()) {
                break;
            }
        }
        urParameters.clear();
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(true);
        }
        Util.deleteDir(directory);
        ArrayList<String> oldProteinKeys = new ArrayList<String>(proteinIdentification);
        proteinIdentification.clear();
        for (String proteinKey : oldProteinKeys) {
            proteinIdentification.add(proteinKey.replaceAll(" ", ProteinMatch.PROTEIN_KEY_SPLITTER));
        }
        updateSpectrumMapping();
    }

    /**
     * Converts the old spectrum keys structure into the mapped version.
     */
    public void updateSpectrumMapping() {
        if (spectrumIdentificationMap == null) {
            spectrumIdentificationMap = new HashMap<String, ArrayList<String>>();
        }
        for (String psmKey : spectrumIdentification) {
            String spectrumFile = Spectrum.getSpectrumFile(psmKey);
            if (!spectrumIdentificationMap.containsKey(spectrumFile)) {
                spectrumIdentificationMap.put(spectrumFile, new ArrayList<String>());
            }
            spectrumIdentificationMap.get(spectrumFile).add(psmKey);
        }
        spectrumIdentification.clear();
    }

    /**
     * Returns the default reference for an identification
     *
     * @param experimentReference the experiment reference
     * @param sampleReference the sample reference
     * @param replicateNumber the replicate number
     * @return the default reference
     */
    public static String getDefaultReference(String experimentReference, String sampleReference, int replicateNumber) {
        return Util.removeForbiddenCharacters(experimentReference + "_" + sampleReference + "_" + replicateNumber + "_id");
    }
}
