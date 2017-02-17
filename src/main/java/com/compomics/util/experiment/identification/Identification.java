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
import java.util.HashSet;

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
     * List of the keys of all imported proteins.
     */
    protected HashSet<String> proteinIdentification = new HashSet<String>();
    /**
     * List of the keys of all imported peptides.
     */
    protected HashSet<String> peptideIdentification = new HashSet<String>();
    /**
     * List of all imported PSMs indexed by mgf file name.
     */
    protected HashMap<String, HashSet<String>> spectrumIdentificationMap = new HashMap<String, HashSet<String>>();
    /**
     * A map linking protein accessions to all their protein matches keys.
     */
    protected HashMap<String, HashSet<String>> proteinMap = new HashMap<String, HashSet<String>>();
    /**
     * The method used.
     */
    protected int methodUsed;
    /**
     * The directory where the database stored.
     */
    protected String dbDirectory;
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
     * Loads all spectrum matches of the file in the cache of the database.
     *
     * @param fileName the file name
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadAssumptions(String fileName, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadAssumptions(fileName, waitingHandler, displayProgress);
    }

    /**
     * Loads the assumptions of the spectrum matches indicated by the given keys
     * in the cache of the database.
     *
     * @param spectrumKeys the spectrum keys
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadAssumptions(ArrayList<String> spectrumKeys, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadAssumptions(spectrumKeys, waitingHandler, displayProgress);
    }

    /**
     * Loads the raw assumptions of the spectrum matches indicated by the given
     * keys in the cache of the database.
     *
     * @param fileName the file name
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadRawAssumptions(String fileName, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadRawAssumptions(fileName, waitingHandler, displayProgress);
    }

    /**
     * Loads all spectrum matches of the file in cache.
     *
     * @param fileName the file name
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadSpectrumMatches(String fileName, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadSpectrumMatches(fileName, waitingHandler, displayProgress);
    }

    /**
     * Loads the spectrum matches corresponding to the given keys in cache.
     *
     * @param spectrumKeys the spectrum keys
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadSpectrumMatches(ArrayList<String> spectrumKeys, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadSpectrumMatches(spectrumKeys, waitingHandler, displayProgress);
    }

    /**
     * Loads all spectrum match parameters of the given type in the cache of the
     * database
     *
     * @param fileName the file name
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadSpectrumMatchParameters(String fileName, UrParameter urParameter, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadSpectrumMatchParameters(fileName, urParameter, waitingHandler, displayProgress);
    }

    /**
     * Loads all desired spectrum match parameters in the cache of the database.
     *
     * @param spectrumKeys the key of the spectrum match of the parameters to be
     * loaded
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadSpectrumMatchParameters(ArrayList<String> spectrumKeys, UrParameter urParameter, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadSpectrumMatchParameters(spectrumKeys, urParameter, waitingHandler, displayProgress);
    }

    /**
     * Loads the desired peptide matches of the given type in the cache of the
     * database.
     *
     * @param peptideKeys the list of peptide keys to load
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadPeptideMatches(ArrayList<String> peptideKeys, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadPeptideMatches(peptideKeys, waitingHandler, displayProgress);
    }

    /**
     * Loads all peptide matches in the cache of the database.
     *
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadPeptideMatches(WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadPeptideMatches(waitingHandler, displayProgress);
    }

    /**
     * Loads all peptide match parameters of the given type in the cache of the
     * database.
     *
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadPeptideMatchParameters(UrParameter urParameter, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadPeptideMatchParameters(urParameter, waitingHandler, displayProgress);
    }

    /**
     * Loads the desired peptide match parameters of the given type in the cache
     * of the database.
     *
     * @param peptideKeys the list of peptide keys of the parameters to load
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadPeptideMatchParameters(ArrayList<String> peptideKeys, UrParameter urParameter, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadPeptideMatchParameters(peptideKeys, urParameter, waitingHandler, displayProgress);
    }

    /**
     * Loads all protein matches in the cache of the database.
     *
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadProteinMatches(WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadProteinMatches(waitingHandler, displayProgress);
    }

    /**
     * Loads the desired protein matches of the given type in the cache of the
     * database.
     *
     * @param proteinKeys the list of protein keys to load
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadProteinMatches(ArrayList<String> proteinKeys, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadProteinMatches(proteinKeys, waitingHandler, displayProgress);
    }

    /**
     * Loads all protein match parameters of the given type in the cache of the
     * database.
     *
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadProteinMatchParameters(UrParameter urParameter, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadProteinMatchParameters(urParameter, waitingHandler, displayProgress);
    }

    /**
     * Loads the desired protein match parameters of the given type in the cache
     * of the database.
     *
     * @param proteinKeys the list of protein keys of the parameters to load
     * @param urParameter the parameter type
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
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
    public void loadProteinMatchParameters(ArrayList<String> proteinKeys, UrParameter urParameter, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.loadProteinMatchParameters(proteinKeys, urParameter, waitingHandler, displayProgress);
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
        return identificationDB.getSpectrumMatchParameter(key, urParameter, useDB);
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
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void addSpectrumMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, InterruptedException {
        identificationDB.addSpectrumMatchParameter(key, urParameter);
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
        return identificationDB.getPeptideMatchParameter(key, urParameter, useDB);
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
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public void addPeptideMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, InterruptedException {
        identificationDB.addPeptideMatchParameter(key, urParameter);
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
        return identificationDB.getProteinMatchParameter(key, urParameter, useDB);
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
        identificationDB.addProteinMatchParameter(key, urParameter);
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updateSpectrumMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, InterruptedException {
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updatePeptideMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, InterruptedException {
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updateProteinMatchParameter(String key, UrParameter urParameter) throws SQLException, IOException, InterruptedException {
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updateAssumptions(String spectrumKey, HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptions) throws SQLException, IOException, InterruptedException {
        identificationDB.updateAssumptions(spectrumKey, assumptions);
    }

    /**
     * Updates the raw assumptions of a spectrum.
     *
     * @param spectrumKey the key of the spectrum
     * @param assumptions the assumptions
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updateRawAssumptions(String spectrumKey, HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptions) throws SQLException, IOException, InterruptedException {
        identificationDB.updateRawAssumptions(spectrumKey, assumptions);
    }

    /**
     * Updates a spectrum match in the database.
     *
     * @param spectrumMatch the match
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updateSpectrumMatch(SpectrumMatch spectrumMatch) throws SQLException, IOException, InterruptedException {
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updatePeptideMatch(PeptideMatch peptideMatch) throws SQLException, IOException, InterruptedException {
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
            HashSet<String> proteinGroups = proteinMap.get(accession);
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updateProteinMatch(ProteinMatch proteinMatch) throws SQLException, IOException, InterruptedException {
        identificationDB.updateProteinMatch(proteinMatch);
    }

    /**
     * Returns the database directory.
     *
     * @return the database directory
     */
    public String getDatabaseDirectory() {
        return dbDirectory;
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void removeAssumptions(String matchKey) throws SQLException, IOException, InterruptedException {
        identificationDB.removeAssumptions(matchKey);
    }

    /**
     * Removes the raw assumptions of a spectrum.
     *
     * @param matchKey the key of the spectrum
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     * @throws IOException exception thrown whenever an IO issue occurred while
     * interacting with the database
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void removeRawAssumptions(String matchKey) throws SQLException, IOException, InterruptedException {
        identificationDB.removeRawAssumptions(matchKey);
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void removeSpectrumMatch(String matchKey) throws SQLException, IOException, InterruptedException {

        String fileName = Spectrum.getSpectrumFile(matchKey);
        HashSet<String> spectrumKeys = spectrumIdentificationMap.get(fileName);
        if (spectrumKeys != null) {
            spectrumKeys.remove(matchKey);
        }
        identificationDB.removeSpectrumMatch(matchKey);
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void removePeptideMatch(String matchKey) throws SQLException, IOException, InterruptedException {

        peptideIdentification.remove(matchKey);
        identificationDB.removePeptideMatch(matchKey);
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
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void removeProteinMatch(String matchKey) throws SQLException, IOException, InterruptedException {
        if (proteinIdentification.contains(matchKey)) {
            for (String protein : ProteinMatch.getAccessions(matchKey)) {
                HashSet<String> proteinKeys = proteinMap.get(protein);
                if (proteinKeys != null) {
                    proteinKeys.remove(matchKey);
                    if (proteinKeys.isEmpty()) {
                        proteinMap.remove(protein);
                    }
                }
            }
        }

        proteinIdentification.remove(matchKey);
        identificationDB.removeProteinMatch(matchKey);
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
            HashSet<String> spectrumKeys = spectrumIdentificationMap.get(fileName);
            if (spectrumKeys != null && spectrumKeys.contains(matchKey)) {
                return true;
            }
        }

        return proteinIdentification.contains(matchKey) || peptideIdentification.contains(matchKey);
    }

    /**
     * Returns the assumptions of a spectrum.
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
        return identificationDB.getAssumptions(spectrumKey, useDB);
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
     * Returns the raw assumptions of a spectrum.
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
    public HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> getRawAssumptions(String spectrumKey, boolean useDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return identificationDB.getRawAssumptions(spectrumKey, useDB);
    }

    /**
     * Returns a the raw assumptions of a spectrum.
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
    public HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> getRawAssumptions(String spectrumKey) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getRawAssumptions(spectrumKey, true);
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
        return identificationDB.getSpectrumMatch(spectrumKey, useDB);
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
        return identificationDB.getPeptideMatch(peptideKey, useDB);
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
        return identificationDB.getProteinMatch(proteinKey, useDB);
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
                SpectrumMatch spectrumMatch = getSpectrumMatch(peptideMatch.getSpectrumMatchesKeys().get(0), false);
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
            SpectrumMatch spectrumMatch = getSpectrumMatch(peptideMatch.getSpectrumMatchesKeys().get(0), false);
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
    public HashSet<String> getProteinIdentification() {
        return proteinIdentification;
    }

    /**
     * Returns a list of the keys of all encountered peptides.
     *
     * @return the corresponding identification results
     */
    public HashSet<String> getPeptideIdentification() {
        return peptideIdentification;
    }

    /**
     * Returns the keys of the spectrum identifications for a given spectrum
     * file name.
     *
     * @param spectrumFile the name of the spectrum file
     * @return the corresponding list of spectrum matches keys. See
     * Spectrum.getKey() for more details.
     */
    public HashSet<String> getSpectrumIdentification(String spectrumFile) {
        return spectrumIdentificationMap.get(spectrumFile);
    }

    /**
     * Returns the keys of all identified spectra indexed by the spectrum file.
     *
     * @return the keys of all identified spectra indexed by the spectrum file
     */
    public HashMap<String, HashSet<String>> getSpectrumIdentificationMap() {
        return spectrumIdentificationMap;
    }

    /**
     * Adds the assumptions corresponding to a spectrum to the database.
     * Warning: only one thread per spectrum supported. Maps and lists are
     * reused and not cloned.
     *
     * @param spectrumKey the key of the spectrum
     * @param newAssumptions the assumptions to add to the mapping
     * @param overwriteExisting if true any existing assumption will be
     * overwritten
     * @param newSpectrum if this is the first time this spectrum is seen
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
    public void addAssumptions(String spectrumKey, HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> newAssumptions, boolean overwriteExisting, boolean newSpectrum)
            throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> currentAssumptions = null;
        if (!newSpectrum && !overwriteExisting) {
            currentAssumptions = getAssumptions(spectrumKey, true);
        }
        if (currentAssumptions == null) {
            identificationDB.addAssumptions(spectrumKey, newAssumptions);
        } else {
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
            updateAssumptions(spectrumKey, currentAssumptions);
        }
    }

    /**
     * Adds the assumptions corresponding to a spectrum. Warning: only one
     * thread per spectrum supported. Maps and lists are reused and not cloned.
     *
     * @param spectrumKey the key of the spectrum
     * @param newAssumptions the assumptions to add to the mapping
     * @param newSpectrum if this is the first time this spectrum is seen
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
    public synchronized void addAssumptions(String spectrumKey, HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> newAssumptions, boolean newSpectrum)
            throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        addAssumptions(spectrumKey, newAssumptions, false, newSpectrum);
    }

    /**
     * Adds the raw assumptions corresponding to a spectrum to the database.
     * Warning: maps and lists are reused and not duplicated. Only one thread should access the same spectrum match at a time.
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
    public void addRawAssumptions(String spectrumKey, HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> newAssumptions)
            throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> currentAssumptions = getRawAssumptions(spectrumKey, true);
        if (currentAssumptions == null) {
            identificationDB.addRawAssumptions(spectrumKey, newAssumptions);
        } else {
            for (Integer advocateId : newAssumptions.keySet()) {
                HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> newAdvocateMap = newAssumptions.get(advocateId);
                HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> currentAdvocateMap = currentAssumptions.get(advocateId);
                if (newAdvocateMap != null) {
                    if (currentAdvocateMap == null) {
                        currentAssumptions.put(advocateId, newAdvocateMap);
                    } else {
                        for (double score : newAdvocateMap.keySet()) {
                            ArrayList<SpectrumIdentificationAssumption> newAssumptionList = newAdvocateMap.get(score);
                            ArrayList<SpectrumIdentificationAssumption> currentAssumptionList = currentAdvocateMap.get(score);
                            if (currentAssumptionList == null) {
                                currentAdvocateMap.put(score, newAssumptionList);
                            } else {
                                currentAssumptionList.addAll(newAssumptionList);
                            }
                        }
                    }
                }
            }
            updateRawAssumptions(spectrumKey, currentAssumptions);
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
    public void addSpectrumMatch(SpectrumMatch newMatch)
            throws IOException, SQLException, ClassNotFoundException, InterruptedException {

        String spectrumKey = newMatch.getKey();
        String spectrumFile = Spectrum.getSpectrumFile(spectrumKey);
        HashSet<String> spectrumKeys = spectrumIdentificationMap.get(spectrumFile);

        if (spectrumKeys == null) {
            spectrumKeys = getSpectrumKeysSynchronized(spectrumFile);
        }

        // check if the spectrum has been seen before
        boolean newSpectrum = !spectrumKeys.contains(spectrumKey);

        HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptions = newMatch.getAssumptionsMap();
        if (assumptions != null) {
            addAssumptions(spectrumKey, assumptions, newSpectrum);
            newMatch.removeAssumptions();
        }

        if (newSpectrum) {
            addKeyToSetSynchronized(spectrumKeys, spectrumKey);
            identificationDB.addSpectrumMatch(newMatch);
        }
    }

    /**
     * Adds a key to a set.
     *
     * @param spectrumKeys the set
     * @param spectrumKey the key
     */
    public synchronized void addKeyToSetSynchronized(HashSet<String> spectrumKeys, String spectrumKey) {
        spectrumKeys.add(spectrumKey);
    }

    /**
     * Checks whether the spectrumIdentificationMap contains spectrum keys for
     * this file. If yes, returns the corresponding set. If no, adds a new set
     * to the map and returns it.
     *
     * @param spectrumFile the name of the file
     *
     * @return the set of spectrum keys for this file
     */
    private synchronized HashSet<String> getSpectrumKeysSynchronized(String spectrumFile) {
        HashSet<String> spectrumKeys = spectrumIdentificationMap.get(spectrumFile);
        if (spectrumKeys == null) {
            spectrumKeys = new HashSet<String>(1000);
            spectrumIdentificationMap.put(spectrumFile, spectrumKeys);
        }
        return spectrumKeys;
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
     * @param spectrumMatchKey the key of the spectrum match to add
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
                peptideMatch.addSpectrumMatchKey(spectrumMatchKey);
                identificationDB.updatePeptideMatch(peptideMatch);
            } else {
                peptideMatch = new PeptideMatch(peptide, peptideKey);
                peptideMatch.addSpectrumMatchKey(spectrumMatchKey);
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
                        proteinMap.put(protein, new HashSet<String>());
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
    public HashMap<String, HashSet<String>> getProteinMap() {
        return proteinMap;
    }

    /**
     * Closes the database connection.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    public void close() throws SQLException, InterruptedException {
        if (identificationDB != null) {
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
        } else {
            String fileName = Spectrum.getSpectrumFile(matchKey);
            HashSet<String> spectrumKeys = spectrumIdentificationMap.get(fileName);
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
     * @param objectsCache the objects cache
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection, typically when another software already has
     * a connection open
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void establishConnection(String dbFolder, boolean deleteOldDatabase, ObjectsCache objectsCache) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB = new IdentificationDB(dbFolder, reference, deleteOldDatabase, objectsCache);
    }

    /**
     * Restores the connection to the database.
     *
     * @param dbFolder the folder where the database is located
     * @param deleteOldDatabase if true, tries to delete the old database
     * @param objectsCache the objects cache
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection, typically when another software already has
     * a connection open
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void restoreConnection(String dbFolder, boolean deleteOldDatabase, ObjectsCache objectsCache) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        identificationDB.restoreConnection(dbFolder, deleteOldDatabase, objectsCache);
    }

    /**
     * Indicates whether the connection to the DB is active.
     *
     * @return true if the connection to the DB is active
     */
    public boolean isConnectionActive() {
        return identificationDB.isConnectionActive();
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
    public HashSet<String> getProteinMatches(Peptide peptide) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        HashSet<String> proteinMatches = new HashSet<String>();
        if (peptide.getParentProteinsNoRemapping() == null) {
            throw new IllegalArgumentException("Proteins are not mapped for peptide " + peptide.getKey() + ".");
        }
        for (String accession : peptide.getParentProteinsNoRemapping()) {
            HashSet<String> keys = proteinMap.get(accession);
            if (keys != null) {
                for (String key : keys) {
                    proteinMatches.add(key);
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
    public boolean isUniqueInDatabase(Peptide peptide) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        return getProteinMatches(peptide).size() == 1;
    }

    /**
     * Returns a PSM iterator.
     *
     * @param spectrumFile the file to iterate
     * @param spectrumKeys specific keys to iterate
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     *
     * @return a PSM iterator
     */
    public PsmIterator getPsmIterator(String spectrumFile, ArrayList<String> spectrumKeys, ArrayList<UrParameter> psmParameters, boolean loadAssumptions, WaitingHandler waitingHandler) {
        return new PsmIterator(spectrumFile, spectrumKeys, this, psmParameters, loadAssumptions, waitingHandler);
    }

    /**
     * Returns a PSM iterator iterating all PSMs in a file.
     *
     * @param spectrumFile the file to iterate
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     *
     * @return a PSM iterator
     */
    public PsmIterator getPsmIterator(String spectrumFile, ArrayList<UrParameter> psmParameters, boolean loadAssumptions, WaitingHandler waitingHandler) {
        return new PsmIterator(spectrumFile, this, psmParameters, loadAssumptions, waitingHandler);
    }

    /**
     * Returns a PSM iterator iterating all PSMs in a file.
     *
     * @param spectrumFile the file to iterate
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     *
     * @return a PSM iterator
     */
    public PsmIterator getPsmIterator(String spectrumFile, boolean loadAssumptions, WaitingHandler waitingHandler) {
        return new PsmIterator(spectrumFile, this, loadAssumptions, waitingHandler);
    }

    /**
     * Returns a PSM iterator.
     *
     * @param spectrumKeys specific keys to iterate
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     *
     * @return a PSM iterator
     */
    public PsmIterator getPsmIterator(ArrayList<String> spectrumKeys, ArrayList<UrParameter> psmParameters, boolean loadAssumptions, WaitingHandler waitingHandler) {
        return new PsmIterator(spectrumKeys, this, psmParameters, loadAssumptions, waitingHandler);
    }

    /**
     * Returns a PSM iterator.
     *
     * @param spectrumKeys specific keys to iterate
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     *
     * @return a PSM iterator
     */
    public PsmIterator getPsmIterator(ArrayList<String> spectrumKeys, boolean loadAssumptions, WaitingHandler waitingHandler) {
        return new PsmIterator(spectrumKeys, this, loadAssumptions, waitingHandler);
    }

    /**
     * Returns a PSM iterator iterating all PSMs in a file.
     *
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     *
     * @return a PSM iterator
     */
    public PsmIterator getPsmIterator(boolean loadAssumptions, ArrayList<UrParameter> psmParameters, WaitingHandler waitingHandler) {
        return new PsmIterator(this, psmParameters, loadAssumptions, waitingHandler);
    }

    /**
     * Returns a peptide matches iterator.
     *
     * @param peptideKeys the keys of the peptides to iterate
     * @param peptideParameters the peptide parameters to load along with the
     * matches
     * @param loadPsms if true PSMs of the peptides will be loaded as well
     * @param psmParameters the PSM parameters to load along with the PSMs
     * @param waitingHandler the waiting handler
     *
     * @return a peptide matches iterator
     */
    public PeptideMatchesIterator getPeptideMatchesIterator(ArrayList<String> peptideKeys, ArrayList<UrParameter> peptideParameters,
            boolean loadPsms, ArrayList<UrParameter> psmParameters, WaitingHandler waitingHandler) {
        return new PeptideMatchesIterator(peptideKeys, this, peptideParameters, loadPsms, psmParameters, waitingHandler);
    }

    /**
     * Returns a peptide matches iterator iterating all peptides.
     *
     * @param peptideParameters the peptide parameters to load along with the
     * matches
     * @param loadPsms if true PSMs of the peptides will be loaded as well
     * @param psmParameters the PSM parameters to load along with the PSMs
     * @param waitingHandler the waiting handler
     *
     * @return a peptide matches iterator
     */
    public PeptideMatchesIterator getPeptideMatchesIterator(ArrayList<UrParameter> peptideParameters,
            boolean loadPsms, ArrayList<UrParameter> psmParameters, WaitingHandler waitingHandler) {
        return new PeptideMatchesIterator(this, peptideParameters, loadPsms, psmParameters, waitingHandler);
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
     * @param waitingHandler the waiting handler
     *
     * @return a protein matches iterator
     */
    public ProteinMatchesIterator getProteinMatchesIterator(ArrayList<String> proteinKeys, ArrayList<UrParameter> proteinParameters, boolean loadPeptides,
            ArrayList<UrParameter> peptideParameters, boolean loadPsms, ArrayList<UrParameter> psmParameters, WaitingHandler waitingHandler) {
        return new ProteinMatchesIterator(proteinKeys, this, proteinParameters, loadPeptides, peptideParameters, loadPsms, psmParameters, waitingHandler);
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
     * @param waitingHandler the waiting handler
     *
     * @return a protein matches iterator
     */
    public ProteinMatchesIterator getProteinMatchesIterator(ArrayList<UrParameter> proteinParameters, boolean loadPeptides,
            ArrayList<UrParameter> peptideParameters, boolean loadPsms, ArrayList<UrParameter> psmParameters, WaitingHandler waitingHandler) {
        return new ProteinMatchesIterator(this, proteinParameters, loadPeptides, peptideParameters, loadPsms, psmParameters, waitingHandler);
    }

    /**
     * Returns the identification database object used to interact with the
     * back-end database.
     *
     * @return the identification database object used to interact with the
     * back-end database
     */
    public IdentificationDB getIdentificationDB() {
        return identificationDB;
    }
}
