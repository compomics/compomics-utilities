package com.compomics.util.experiment.identification;

import com.compomics.util.Util;
import com.compomics.util.db.ObjectsCache;
import com.compomics.util.db.ObjectsDB;
import com.compomics.util.experiment.biology.Peptide;
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
import com.orientechnologies.orient.object.iterator.OObjectIteratorClass;
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
    protected HashSet<String> spectrumIdentificationMap = new HashSet<String>();
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
     * The reference of the identification.
     */
    protected String reference;
    /**
     * The ordered list of spectrum file names.
     */
    private ArrayList<String> orderedSpectrumFileNames;
    /**
     * The database which will contain the objects.
     */
    private final ObjectsDB objectsDB;
    
    
    /**
     * Constructor
     * @param objectsDB the database for storing all objects on disk when memory is too low
     */
    public Identification(ObjectsDB objectsDB) {
        this.objectsDB = objectsDB;
    }
    

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
        return orderedSpectrumFileNames;
    }

    /**
     * Returns the number of spectrum identifications.
     *
     * @return the number of spectrum identifications
     */
    public int getSpectrumIdentificationSize() {
        return spectrumIdentificationMap.size();
    }
    

    /**
     * Returns the number of objects of a given class
     *
     * @param className the class name of a given class
     * @return the number of objects
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
    public int getNumber(String className) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return objectsDB.getNumber(className);
    }
    
    
    /**
     * Returns an iterator of all objects of a given class
     *
     * @param className the class name of a given class
     * @return the iterator
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
    public OObjectIteratorClass<?> getIterator(String className) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return objectsDB.getObjectsIterator(className);
    }
    


    /**
     * Loads all objects of the class in cache.
     *
     * @param className the class name
     * @param lazyLoading indicates wheather the iterator should load data lazy from the db
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @return returns the list of hashed keys
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
    public ArrayList<Long> loadObjects(String className, boolean lazyLoading, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return objectsDB.loadObjects(className, lazyLoading, waitingHandler, displayProgress);
    }
    
    
    /**
     * Loads all objects of given keys in cache.
     *
     * @param keyList the list of keys of given objects
     * @param lazyLoading indicates wheather the iterator should load data lazy from the db
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @return returns the list of hashed keys
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
    public ArrayList<Long> loadObjects(ArrayList<String> keyList, boolean lazyLoading, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return objectsDB.loadObjects(keyList, lazyLoading, waitingHandler, displayProgress);
    }
    
    
    /**
     * Loads all spectrum matches of given keys in cache.
     *
     * @param iterator the iterator
     * @param num number of objects that have to be retrieved in a batch
     * @param lazyLoading indicates wheather the iterator should load data lazy from the db
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @return returns the list of hashed keys
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
    public ArrayList<Long> loadObjects(OObjectIteratorClass<?> iterator, int num, boolean lazyLoading, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return objectsDB.loadObjects(iterator, num, lazyLoading, waitingHandler, displayProgress);
    }
    
    
    /**
     * Returns an array of all objects of a given list of keys
     *
     * @param longKey the hash key
     * @return the objects
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
    public Object retrieveObject(long longKey) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return objectsDB.retrieveObject(longKey);
    }
    
    
    /**
     * Returns an array of all objects of a given list of keys
     *
     * @param key the key
     * @return the objects
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
    public Object retrieveObject(String key) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return objectsDB.retrieveObject(key);
    }
    
    
    /**
     * Returns an array of all objects of a given list of keys
     *
     * @param keyList the key list
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @return list of objects
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
    public ArrayList<Object> retrieveObjects(ArrayList<String> keyList, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return objectsDB.retrieveObjects(keyList, waitingHandler, displayProgress);
    }
    
    
    /**
     * Returns an array of all objects of a given class
     *
     * @param className the class name
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @return list of objects
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
    public ArrayList<Object> retrieveObjects(String className, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return objectsDB.retrieveObjects(className, waitingHandler, displayProgress);
    }
    

    /**
     * Adds an object into the database.
     * 
     * @param key the key of the object
     * @param object the object
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void addObject(String key , Object object) throws SQLException, IOException, InterruptedException {
        objectsDB.insertObject(key, object);
    }
    

    /**
     * Adds a list of objects into the database.
     * 
     * @param objects the object
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void addObjects(HashMap<String, Object> objects, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, InterruptedException {
        objectsDB.insertObjects(objects, waitingHandler, displayProgress);
    }
    
    
    

    /**
     * Removes an object from the database.
     * 
     * @param key the key of the object
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void removeObject(String key) throws SQLException, IOException, InterruptedException {
        objectsDB.removeObject(key);
    }
    

    /**
     * Remove a list of objects from the database.
     * 
     * @param keys the list of object keys
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void addObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, InterruptedException {
        objectsDB.removeObjects(keys, waitingHandler, displayProgress);
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
        spectrumIdentificationMap.remove(matchKey);
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
            if (spectrumIdentificationMap.contains(matchKey)) {
                return true;
            }
        }

        return proteinIdentification.contains(matchKey) || peptideIdentification.contains(matchKey);
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
        if (objectsDB.inCache(proteinKey)) {
            ProteinMatch proteinMatch = (ProteinMatch)retrieveObject(proteinKey);
            String peptideKey = proteinMatch.getPeptideMatchesKeys().get(0);
            if (objectsDB.inCache(peptideKey)) {
                PeptideMatch peptideMatch = (PeptideMatch)retrieveObject(peptideKey);
                return objectsDB.inCache(peptideMatch.getSpectrumMatchesKeys().get(0));
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
     * Returns the keys of the spectrum identifications
     *
     * @return the corresponding list of spectrum matches keys. See
     * Spectrum.getKey() for more details.
     */
    public HashSet<String> getSpectrumIdentification() {
        return spectrumIdentificationMap;
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
        for (String spectrumMatchKey : spectrumIdentificationMap) {
            buildPeptidesAndProteins(spectrumMatchKey, sequenceMatchingPreferences);
            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressCounter();
                if (waitingHandler.isRunCanceled()) {
                    return;
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
        objectsDB.close();
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
        return objectsDB.isConnectionActive();
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
        ArrayList<String> parentProteins = peptide.getParentProteinsNoRemapping();
        if (parentProteins == null) {
            throw new IllegalArgumentException("Proteins are not mapped for peptide " + peptide.getKey() + ".");
        }
        for (String accession : parentProteins) {
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
        return new PsmIterator(this, psmParameters, loadAssumptions, waitingHandler);
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
        return new PsmIterator(this, loadAssumptions, waitingHandler);
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
     * @return a PSM iterator
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InterruptedException
     */
    public PsmIterator getPsmIterator() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return new PsmIterator(this);
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
}
