package com.compomics.util.experiment.identification;

import com.compomics.util.Util;
import com.compomics.util.db.ObjectsCache;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch.MatchType;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches_iterators.PeptideMatchesIterator;
import com.compomics.util.experiment.identification.matches_iterators.ProteinMatchesIterator;
import com.compomics.util.experiment.identification.matches_iterators.PsmIterator;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.*;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class contains identification results.
 *
 * @author Marc Vaudel
 */
public abstract class Identification extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -2551700699384242554L;
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
     * Map of long keys (&gt;100 characters) which will be referenced by their
     * index for file creation/database storage.
     *
     * @deprecated use the database instead
     */
    protected ArrayList<String> longKeys = new ArrayList<String>();
    /**
     * Boolean indicating whether the identifications are stored in a a database
     * (default) or in serialized files (deprecated).
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
     * @throws java.lang.InterruptedException
     * @deprecated use the database match specific methods instead
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void addMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, InterruptedException {
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
     * 
     * @return the personalization parameter
     * 
     * @throws java.lang.InterruptedException
     * @deprecated use the database match specific methods instead
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     */
    public UrParameter getMatchParameter(String matchKey, UrParameter urParameter) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (!isDB) {
            return urParameters.get(matchKey).get(ExperimentObject.getParameterKey(urParameter));
        } else {
            return identificationDB.getMatchPArameter(matchKey, urParameter, true);
        }
    }

    /**
     * Loads all spectrum matches of the file in the cache of the database.
     *
     * @param fileName the file name
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadAssumptions(String fileName, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadAssumptions(fileName, waitingHandler);
    }

    /**
     * Loads the given spectrum matches in the cache of the database.
     *
     * @param spectrumKeys the spectrum keys
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadAssumptions(ArrayList<String> spectrumKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadAssumptions(spectrumKeys, waitingHandler);
    }

    /**
     * Loads all assumptions of all spectrum matches of the file in the cache of the database.
     *
     * @param fileName the file name
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadSpectrumMatches(String fileName, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadSpectrumMatches(fileName, waitingHandler);
    }

    /**
     * Loads the given spectrum matches in the cache of the database.
     *
     * @param spectrumKeys the spectrum keys
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadSpectrumMatches(ArrayList<String> spectrumKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadSpectrumMatches(spectrumKeys, waitingHandler);
    }

    /**
     * Loads all spectrum match parameters of the given type in the cache of the
     * database
     *
     * @param fileName the file name
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadSpectrumMatchParameters(String fileName, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadSpectrumMatchParameters(fileName, urParameter, waitingHandler);
    }

    /**
     * Loads all desired spectrum match parameters in the cache of the database.
     *
     * @param spectrumKeys the key of the spectrum match of the parameters to be
     * loaded
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadSpectrumMatchParameters(ArrayList<String> spectrumKeys, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadSpectrumMatchParameters(spectrumKeys, urParameter, waitingHandler);
    }

    /**
     * Loads the desired peptide matches of the given type in the cache of the
     * database.
     *
     * @param peptideKeys the list of peptide keys to load
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadPeptideMatches(ArrayList<String> peptideKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadPeptideMatches(peptideKeys, waitingHandler);
    }

    /**
     * Loads all peptide matches in the cache of the database.
     *
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadPeptideMatches(WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadPeptideMatches(waitingHandler);
    }

    /**
     * Loads all peptide match parameters of the given type in the cache of the
     * database.
     *
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadPeptideMatchParameters(UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadPeptideMatchParameters(urParameter, waitingHandler);
    }

    /**
     * Loads the desired peptide match parameters of the given type in the cache
     * of the database.
     *
     * @param peptideKeys the list of peptide keys of the parameters to load
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadPeptideMatchParameters(ArrayList<String> peptideKeys, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadPeptideMatchParameters(peptideKeys, urParameter, waitingHandler);
    }

    /**
     * Loads all protein matches in the cache of the database.
     *
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadProteinMatches(WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadProteinMatches(waitingHandler);
    }

    /**
     * Loads the desired protein matches of the given type in the cache of the
     * database.
     *
     * @param proteinKeys the list of protein keys to load
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadProteinMatches(ArrayList<String> proteinKeys, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadProteinMatches(proteinKeys, waitingHandler);
    }

    /**
     * Loads all protein match parameters of the given type in the cache of the
     * database.
     *
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadProteinMatchParameters(UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadProteinMatchParameters(urParameter, waitingHandler);
    }

    /**
     * Loads the desired protein match parameters of the given type in the cache
     * of the database.
     *
     * @param proteinKeys the list of protein keys of the parameters to load
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void loadProteinMatchParameters(ArrayList<String> proteinKeys, UrParameter urParameter, WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadProteinMatchParameters(proteinKeys, urParameter, waitingHandler);
    }

    /**
     * Returns the desired spectrum match parameter.
     *
     * @param key the PSM key
     * @param urParameter the match parameter
     * 
     * @return the spectrum match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public UrParameter getSpectrumMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getSpectrumMatchParameter(key, urParameter, true);
    }

    /**
     * Returns the desired spectrum match parameter.
     *
     * @param key the PSM key
     * @param urParameter the match parameter
     * @param useDB if useDB is false, null will be returned if the object is
     * not in the cache
     * 
     * @return the spectrum match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public UrParameter getSpectrumMatchParameter(String key, UrParameter urParameter, boolean useDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (isDB) {
            return identificationDB.getSpectrumMatchParameter(key, urParameter, useDB);
        } else {
            return getMatchParameter(key, urParameter);
        }
    }

    /**
     * Adds a spectrum match parameter to the database.
     *
     * @param key the PSM key
     * @param urParameter the match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void addSpectrumMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, InterruptedException {
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
     * 
     * @return the peptide match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public UrParameter getPeptideMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getPeptideMatchParameter(key, urParameter, true);
    }

    /**
     * Returns the desired peptide match parameter.
     *
     * @param key the peptide key
     * @param urParameter the match parameter
     * @param useDB if useDB is false, null will be returned if the object is
     * not in the cache
     * 
     * @return the peptide match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public UrParameter getPeptideMatchParameter(String key, UrParameter urParameter, boolean useDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
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
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void addPeptideMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, InterruptedException {
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
     * 
     * @return the protein match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public UrParameter getProteinMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getProteinMatchParameter(key, urParameter, true);
    }

    /**
     * Returns the desired protein match parameter.
     *
     * @param key the protein key
     * @param urParameter the match parameter
     * @param useDB if useDB is false, null will be returned if the object is
     * not in the cache
     * 
     * @return the protein match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public UrParameter getProteinMatchParameter(String key, UrParameter urParameter, boolean useDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
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
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void addProteinMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, InterruptedException {
        if (isDB) {
            identificationDB.addProteinMatchParameter(key, urParameter);
        } else {
            addMatchParameter(key, urParameter);
        }
    }

    /**
     * Updates a spectrum match parameter in the database.
     *
     * @param key the spectrum key
     * @param urParameter the match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updateSpectrumMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        identificationDB.updateSpectrumParameter(key, urParameter);
    }

    /**
     * Updates a peptide match parameter in the database.
     *
     * @param key the peptide key
     * @param urParameter the match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updatePeptideMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        identificationDB.updatePeptideParameter(key, urParameter);
    }

    /**
     * Updates a protein match parameter in the database.
     *
     * @param key the protein key
     * @param urParameter the match parameter
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updateProteinMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException {
        identificationDB.updateProteinParameter(key, urParameter);
    }

    /**
     * Updates the assumptions of a spectrum.
     *
     * @param spectrumKey the key of the spectrum
     * @param assumptions the assumptions
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updateAssumptions(String spectrumKey, HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptions) throws SQLException, IOException {
        identificationDB.updateAssumptions(spectrumKey, assumptions);
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
        identificationDB.updateSpectrumMatch(spectrumMatch);
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
        identificationDB.updatePeptideMatch(peptideMatch);
    }

    /**
     * Updates a peptide match where the key was changed.
     *
     * @param oldKey the old peptide key
     * @param newKey the new peptide key
     * @param peptideMatch the new peptide match
     *
     * @throws SQLException exception thrown whenever an SQL error occurred
     * while interacting with the database
     * @throws IOException exception thrown whenever an IO issue occurred while
     * interacting with the database
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     * @throws java.lang.ClassNotFoundException exception thrown whenever a
     * casting issue occurred while interacting with the database
     */
    public void updatePeptideMatch(String oldKey, String newKey, PeptideMatch peptideMatch) throws SQLException, IOException, InterruptedException, ClassNotFoundException {
        for (String paramterTable : identificationDB.getPeptideParametersTables()) {
            UrParameter parameter = (UrParameter) identificationDB.getObject(paramterTable, oldKey, true);
            if (parameter != null) {
                addPeptideMatchParameter(newKey, parameter);
            }
        }
        removePeptideMatch(oldKey);
        peptideMatch.setKey(newKey);
        peptideIdentification.remove(oldKey);
        peptideIdentification.add(newKey);
        identificationDB.addPeptideMatch(peptideMatch);
        for (String accession : peptideMatch.getTheoreticPeptide().getParentProteinsNoRemapping()) {
            ArrayList<String> proteinGroups = proteinMap.get(accession);
            if (proteinGroups != null) {
                for (String proteinKey : proteinGroups) {
                    ProteinMatch proteinMatch = getProteinMatch(proteinKey);
                    ArrayList<String> oldPeptideMatches = proteinMatch.getPeptideMatchesKeys();
                    ArrayList<String> newPeptideMatches = new ArrayList<String>(oldPeptideMatches.size());
                    boolean found = false;
                    for (String peptideMatchKey : oldPeptideMatches) {
                        if (peptideMatchKey.equals(oldKey)) {
                            found = true;
                        } else {
                            newPeptideMatches.add(peptideMatchKey);
                        }
                    }
                    if (found) {
                        newPeptideMatches.add(newKey);
                        proteinMatch.setPeptideKeys(newPeptideMatches);
                    }
                    updateProteinMatch(proteinMatch);
                }
            }
        }
    }

    /**
     * Updates a protein match in the database.
     *
     * @param proteinMatch the match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updateProteinMatch(ProteinMatch proteinMatch) throws SQLException, IOException {
        identificationDB.updateProteinMatch(proteinMatch);
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
     *
     * @throws SQLException exception thrown whenever an SQL error occurred
     * while interacting with the database
     *
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
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException exception thrown whenever an IO issue occurred while
     * interacting with the database
     */
    public void removeMatch(String matchKey) throws SQLException, IOException {

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
     * Removes the assumptions of a spectrum.
     *
     * @param matchKey the key of the spectrum
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException exception thrown whenever an IO issue occurred while
     * interacting with the database
     */
    public void removeAssumptions(String matchKey) throws SQLException, IOException {
        identificationDB.removeAssumptions(matchKey);
    }

    /**
     * Removes a spectrum match from the model.
     *
     * @param matchKey the key of the match to remove
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException exception thrown whenever an IO issue occurred while
     * interacting with the database
     */
    public void removeSpectrumMatch(String matchKey) throws SQLException, IOException {

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
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException exception thrown whenever an IO issue occurred while
     * interacting with the database
     */
    public void removePeptideMatch(String matchKey) throws SQLException, IOException {

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
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException exception thrown whenever an IO issue occurred while
     * interacting with the database
     */
    public void removeProteinMatch(String matchKey) throws SQLException, IOException {
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
     *
     * @return a boolean indicating whether a match indexed by the given key
     * exists
     */
    public boolean matchExists(String matchKey) {

        if (matchKey == null || matchKey.length() == 0) {
            return false;
        }

        if (matchKey.lastIndexOf(Spectrum.SPECTRUM_KEY_SPLITTER) != -1) {
            String fileName = Spectrum.getSpectrumFile(matchKey);
            ArrayList<String> spectrumKeys = spectrumIdentificationMap.get(fileName);
            if (spectrumKeys != null && spectrumKeys.contains(matchKey)) {
                return true;
            }
        }

        return proteinIdentification.contains(matchKey) || peptideIdentification.contains(matchKey) || spectrumIdentification.contains(matchKey);
    }

    /**
     * Returns a match.
     *
     * @param matchKey the key of the match
     *
     * @return the desired match
     *
     * @deprecated use the database match specific methods instead
     */
    private Object getMatch(String matchKey) {
        return getMatch(matchKey, 0);
    }

    /**
     * Returns a match.
     *
     * @param matchKey the key of the match
     * @param the number of exceptions found. If less than 100, the method will
     * retry after a tempo of 50ms to avoid network related issues.
     *
     * @return the desired match
     *
     * @deprecated use the database match specific methods instead
     */
    private synchronized IdentificationMatch getMatch(String matchKey, int errorCounter) {

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
     * Returns a the assumptions of a spectrum.
     *
     * @param spectrumKey the key of the spectrum
     *
     * @return the assumptions
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> getAssumptions(String spectrumKey) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getAssumptions(spectrumKey, true);
    }

    /**
     * Returns a the assumptions of a spectrum.
     *
     * @param spectrumKey the key of the spectrum
     * @param useDB if useDB is false, null will be returned if the object is
     * not in the cache
     *
     * @return the assumptions
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> getAssumptions(String spectrumKey, boolean useDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptions = identificationDB.getAssumptions(spectrumKey, useDB);
        if (assumptions == null) { // backward compatibility check
            SpectrumMatch spectrumMatch = getSpectrumMatch(spectrumKey, true);
            if (spectrumMatch != null) {
                assumptions = spectrumMatch.getAssumptionsMap();
                if (assumptions != null) {
                    addAssumptions(spectrumKey, assumptions);
                    spectrumMatch.removeAssumptions();
                    updateSpectrumMatch(spectrumMatch);
                }
            }
        }
        return assumptions;
    }

    /**
     * Returns a spectrum match.
     *
     * @param spectrumKey the key of the match
     *
     * @return the desired match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public SpectrumMatch getSpectrumMatch(String spectrumKey) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getSpectrumMatch(spectrumKey, true);
    }

    /**
     * Returns a spectrum match.
     *
     * @param spectrumKey the key of the match
     * @param useDB if useDB is false, null will be returned if the object is
     * not in the cache
     *
     * @return the desired match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public SpectrumMatch getSpectrumMatch(String spectrumKey, boolean useDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
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
     *
     * @return the desired match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public PeptideMatch getPeptideMatch(String peptideKey) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getPeptideMatch(peptideKey, true);
    }

    /**
     * Returns a peptide match.
     *
     * @param peptideKey the key of the match
     * @param useDB if useDB is false, null will be returned if the object is
     * not in the cache
     *
     * @return the desired match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public PeptideMatch getPeptideMatch(String peptideKey, boolean useDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
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
     *
     * @return the desired match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public ProteinMatch getProteinMatch(String proteinKey) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getProteinMatch(proteinKey, true);
    }

    /**
     * Returns a protein match.
     *
     * @param proteinKey the key of the match
     * @param useDB if useDB is false, null will be returned if the object is
     * not in the cache
     *
     * @return the desired match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public ProteinMatch getProteinMatch(String proteinKey, boolean useDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (isDB) {
            return identificationDB.getProteinMatch(proteinKey, useDB);
        } else {
            return (ProteinMatch) getMatch(proteinKey);
        }
    }

    /**
     * Indicates whether the protein, peptide and spectrum matches corresponding
     * to a protein match key are loaded in the cache. Note, only one peptide
     * and one spectrum match is tested.
     *
     * @param proteinKey the key of the protein match
     *
     * @return true if everything is loaded in memory
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public boolean proteinDetailsInCache(String proteinKey) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        ProteinMatch proteinMatch = getProteinMatch(proteinKey, false);
        if (proteinMatch != null) {
            PeptideMatch peptideMatch = getPeptideMatch(proteinMatch.getPeptideMatchesKeys().get(0), false);
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
     * Indicates whether the peptide and spectrum matches corresponding to a
     * peptide match key are loaded in the cache. Note, only one one spectrum
     * match is tested.
     *
     * @param peptideKey the peptide key
     * @return true if everything is loaded in the cache
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public boolean peptideDetailsInCache(String peptideKey) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
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
     *
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
     * Returns the keys of the spectrum identifications for a given spectrum
     * file name.
     *
     * @param spectrumFile the name of the spectrum file
     * @return the corresponding list of spectrum matches keys. See
     * Spectrum.getKey() for more details.
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
     * Adds the assumptions corresponding to a spectrum.
     *
     * @param spectrumKey the key of the spectrum
     * @param newAssumptions the assumptions to add to the mapping
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public synchronized void addAssumptions(String spectrumKey, HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> newAssumptions)
            throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        boolean createAssumptions = false;
        HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> currentAssumptions = getAssumptions(spectrumKey, true);
        if (currentAssumptions == null) {
            currentAssumptions = new HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>>(newAssumptions.size());
            createAssumptions = true;
        }
        for (Integer advocateId : newAssumptions.keySet()) {
            HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> newAdvocateMap = newAssumptions.get(advocateId);
            HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> currentAdvocateMap = currentAssumptions.get(advocateId);
            if (newAdvocateMap != null) {
                if (currentAdvocateMap == null) {
                    currentAdvocateMap = new HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>(newAdvocateMap.size());
                    currentAssumptions.put(advocateId, currentAdvocateMap);
                }
                for (double score : newAdvocateMap.keySet()) {
                    ArrayList<SpectrumIdentificationAssumption> newAssumptionList = newAdvocateMap.get(score);
                    ArrayList<SpectrumIdentificationAssumption> currentAssumptionList = currentAdvocateMap.get(score);
                    if (currentAssumptionList == null) {
                        currentAssumptionList = new ArrayList<SpectrumIdentificationAssumption>(newAssumptionList);
                        currentAdvocateMap.put(score, currentAssumptionList);
                    } else {
                        currentAssumptionList.addAll(newAssumptionList);
                    }
                }
            }
        }
        if (createAssumptions) {
            identificationDB.addAssumptions(spectrumKey, newAssumptions);
        } else {
            updateAssumptions(spectrumKey, newAssumptions);
        }
    }

    /**
     * Adds a spectrum match to the identification.
     *
     * @param newMatch the new match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public synchronized void addSpectrumMatch(SpectrumMatch newMatch)
            throws IOException, SQLException, ClassNotFoundException, InterruptedException {

        String spectrumKey = newMatch.getKey();

        HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptions = newMatch.getAssumptionsMap();
        if (assumptions != null) {
            addAssumptions(spectrumKey, assumptions);
            newMatch.removeAssumptions();
        }

        String spectrumFile = Spectrum.getSpectrumFile(spectrumKey);
        ArrayList<String> spectrumKeys = spectrumIdentificationMap.get(spectrumFile);
        if (spectrumKeys == null || !spectrumKeys.contains(spectrumKey)) {
            if (spectrumKeys == null) {
                spectrumKeys = new ArrayList<String>(1000);
                spectrumIdentificationMap.put(spectrumFile, spectrumKeys);
            }
            spectrumKeys.add(spectrumKey);
            identificationDB.addSpectrumMatch(newMatch);
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
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void buildPeptidesAndProteins(WaitingHandler waitingHandler, SequenceMatchingPreferences sequenceMatchingPreferences) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(getSpectrumIdentificationSize());
            waitingHandler.setSecondaryProgressCounter(0);
        }
        for (String spectrumFile : spectrumIdentificationMap.keySet()) {
            for (String spectrumMatchKey : spectrumIdentificationMap.get(spectrumFile)) {
                buildPeptidesAndProteins(spectrumMatchKey, sequenceMatchingPreferences);
                if (waitingHandler != null) {
                    waitingHandler.increaseSecondaryProgressCounter();
                    if (waitingHandler.isRunCanceled()) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Creates the peptides and protein instances based on the given spectrum
     * match. Note that only the best peptide assumption is used, the method has
     * no effect if it is null. This operation will be very slow if the cache is
     * already full. Note: if proteins are not set for a peptide they will be
     * assigned using the default protein tree and the given matching
     * parameters.
     *
     * @param spectrumMatchKey The key of the spectrum match to add
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void buildPeptidesAndProteins(String spectrumMatchKey, SequenceMatchingPreferences sequenceMatchingPreferences) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        SpectrumMatch spectrumMatch = getSpectrumMatch(spectrumMatchKey);
        if (spectrumMatch == null) {
            throw new IllegalArgumentException("Spectrum match " + spectrumMatchKey + " not found.");
        }
        if (spectrumMatch.getBestPeptideAssumption() != null) {
            Peptide peptide = spectrumMatch.getBestPeptideAssumption().getPeptide();
            if (peptide.getParentProteinsNoRemapping() == null) {
                peptide.getParentProteins(sequenceMatchingPreferences);
            }
            String peptideKey = peptide.getMatchingKey(sequenceMatchingPreferences);
            PeptideMatch peptideMatch;

            if (peptideIdentification.contains(peptideKey)) {
                peptideMatch = getPeptideMatch(peptideKey);
                if (peptideMatch == null) {
                    throw new IllegalArgumentException("Peptide match " + peptideKey + " not found.");
                }
                peptideMatch.addSpectrumMatch(spectrumMatchKey);
                identificationDB.updatePeptideMatch(peptideMatch);
            } else {
                peptideMatch = new PeptideMatch(peptide, spectrumMatchKey, peptideKey);
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
                if (!proteinMatch.getPeptideMatchesKeys().contains(peptideKey)) {
                    proteinMatch.addPeptideMatchKey(peptideKey);
                    identificationDB.updateProteinMatch(proteinMatch);
                }
            } else {
                ProteinMatch proteinMatch = new ProteinMatch(peptideMatch.getTheoreticPeptide(), peptideKey);
                if (!proteinMatch.getKey().equals(proteinKey)) {
                    throw new IllegalArgumentException("Protein inference issue: the protein key " + proteinKey + " does not match the peptide proteins " + proteinMatch.getKey() + "."
                            + " Peptide: " + peptideKey + " found in spectrum " + spectrumMatchKey + ".");
                }
                proteinIdentification.add(proteinKey);
                for (String protein : peptide.getParentProteinsNoRemapping()) {
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
    }

    /**
     * Add a set of spectrumMatches to the model.
     *
     * @param spectrumMatches the spectrum matches
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void addSpectrumMatches(Iterable<SpectrumMatch> spectrumMatches)
            throws IOException, SQLException, ClassNotFoundException, InterruptedException {
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
     * 
     * @return the name of the corresponding file
     * 
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
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection, typically when another software already has a connection open
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
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void convert(WaitingHandler waitingHandler, String newDirectory, String newName, ObjectsCache objectsCache, File directory)
            throws IOException, ClassNotFoundException, SQLException, InterruptedException {
        setIsDB(true);
        reference = newName;
        establishConnection(newDirectory, true, objectsCache);

        File[] files = directory.listFiles();
        int nParameters = 0;
        for (HashMap<String, UrParameter> map : urParameters.values()) {
            nParameters += map.size();
        }
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(files.length + nParameters);
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
                waitingHandler.increaseSecondaryProgressCounter();
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
                waitingHandler.increaseSecondaryProgressCounter();
            }
            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                break;
            }
        }
        urParameters.clear();
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
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
     * Returns the default reference for an identification.
     *
     * @param experimentReference the experiment reference
     * @param sampleReference the sample reference
     * @param replicateNumber the replicate number
     * 
     * @return the default reference
     */
    public static String getDefaultReference(String experimentReference, String sampleReference, int replicateNumber) {
        return Util.removeForbiddenCharacters(experimentReference + "_" + sampleReference + "_" + replicateNumber + "_id");
    }

    /**
     * Returns the keys of the protein matches where a peptide can be found.
     * Note: proteins have to be set for the peptide.
     *
     * @param peptide the peptide of interest
     * 
     * @return the keys of the protein matches
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public ArrayList<String> getProteinMatches(Peptide peptide) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        ArrayList<String> proteinMatches = new ArrayList<String>();
        if (peptide.getParentProteinsNoRemapping() == null) {
            throw new IllegalArgumentException("Proteins are not mapped for peptide " + peptide.getKey() + ".");
        }
        for (String accession : peptide.getParentProteinsNoRemapping()) {
            ArrayList<String> keys = proteinMap.get(accession);
            if (keys != null) {
                for (String key : keys) {
                    if (!proteinMatches.contains(key)) {
                        proteinMatches.add(key);
                    }
                }
            }
        }
        return proteinMatches;
    }

    /**
     * Indicates whether a peptide is found in a single protein match.
     *
     * @param peptide the peptide of interest
     * 
     * @return true if peptide is found in a single protein match
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public boolean isUnique(Peptide peptide) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        return getProteinMatches(peptide).size() == 1;
    }

    /**
     * Returns a PSM iterator.
     *
     * @param spectrumFile the file to iterate
     * @param spectrumKeys specific keys to iterate
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     *
     * @return a PSM iterator
     */
    public PsmIterator getPsmIterator(String spectrumFile, ArrayList<String> spectrumKeys, ArrayList<UrParameter> psmParameters, boolean loadAssumptions) {
        return new PsmIterator(spectrumFile, spectrumKeys, this, psmParameters, loadAssumptions);
    }

    /**
     * Returns a PSM iterator iterating all PSMs in a file.
     *
     * @param spectrumFile the file to iterate
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     *
     * @return a PSM iterator
     */
    public PsmIterator getPsmIterator(String spectrumFile, ArrayList<UrParameter> psmParameters, boolean loadAssumptions) {
        return new PsmIterator(spectrumFile, this, psmParameters, loadAssumptions);
    }

    /**
     * Returns a peptide matches iterator.
     *
     * @param peptideKeys the keys of the peptides to iterate
     * @param peptideParameters the peptide parameters to load along with the
     * matches
     * @param loadPsms if true PSMs of the peptides will be loaded as well
     * @param psmParameters the PSM parameters to load along with the PSMs
     *
     * @return a peptide matches iterator
     */
    public PeptideMatchesIterator getPeptideMatchesIterator(ArrayList<String> peptideKeys, ArrayList<UrParameter> peptideParameters, boolean loadPsms, ArrayList<UrParameter> psmParameters) {
        return new PeptideMatchesIterator(peptideKeys, this, peptideParameters, loadPsms, psmParameters);
    }

    /**
     * Returns a peptide matches iterator iterating all peptides.
     *
     * @param peptideParameters the peptide parameters to load along with the
     * matches
     * @param loadPsms if true PSMs of the peptides will be loaded as well
     * @param psmParameters the PSM parameters to load along with the PSMs
     *
     * @return a peptide matches iterator
     */
    public PeptideMatchesIterator getPeptideMatchesIterator(ArrayList<UrParameter> peptideParameters, boolean loadPsms, ArrayList<UrParameter> psmParameters) {
        return new PeptideMatchesIterator(this, peptideParameters, loadPsms, psmParameters);
    }

    /**
     * Returns a protein matches iterator.
     *
     * @param proteinKeys the keys of the proteins to iterate
     * @param proteinParameters the protein parameters to load along with the
     * matches
     * @param loadPeptides if true the peptides corresponding to these proteins
     * will be batch loaded along with the proteins
     * @param peptideParameters the parameters to load along with the peptide
     * matches
     * @param loadPsms if true the PSMs of the peptides will be batch loaded
     * along with the matches
     * @param psmParameters the parameters to load along with the matches
     *
     * @return a protein matches iterator
     */
    public ProteinMatchesIterator getProteinMatchesIterator(ArrayList<String> proteinKeys, ArrayList<UrParameter> proteinParameters, boolean loadPeptides, ArrayList<UrParameter> peptideParameters, boolean loadPsms, ArrayList<UrParameter> psmParameters) {
        return new ProteinMatchesIterator(proteinKeys, this, proteinParameters, loadPeptides, peptideParameters, loadPsms, psmParameters);
    }

    /**
     * Returns a protein matches iterator iterating all protein matches.
     *
     * @param proteinParameters the protein parameters to load along with the
     * matches
     * @param loadPeptides if true the peptides corresponding to these proteins
     * will be batch loaded along with the proteins
     * @param peptideParameters the parameters to load along with the peptide
     * matches
     * @param loadPsms if true the PSMs of the peptides will be batch loaded
     * along with the matches
     * @param psmParameters the parameters to load along with the matches
     *
     * @return a protein matches iterator
     */
    public ProteinMatchesIterator getProteinMatchesIterator(ArrayList<UrParameter> proteinParameters, boolean loadPeptides, ArrayList<UrParameter> peptideParameters, boolean loadPsms, ArrayList<UrParameter> psmParameters) {
        return new ProteinMatchesIterator(this, proteinParameters, loadPeptides, peptideParameters, loadPsms, psmParameters);
    }
}
