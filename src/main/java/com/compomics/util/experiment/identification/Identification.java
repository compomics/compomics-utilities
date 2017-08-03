package com.compomics.util.experiment.identification;

import com.compomics.util.Util;
import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches_iterators.PeptideMatchesIterator;
import com.compomics.util.experiment.identification.matches_iterators.ProteinMatchesIterator;
import com.compomics.util.experiment.identification.matches_iterators.PsmIterator;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

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
    protected HashSet<String> proteinIdentification = new HashSet<>();
    /**
     * List of the keys of all imported peptides.
     */
    protected HashSet<String> peptideIdentification = new HashSet<>();
    /**
     * A map linking protein accessions to all their protein matches keys.
     */
    protected HashMap<String, HashSet<String>> proteinMap = new HashMap<>();
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
     * The database which will contain the objects.
     */
    private final ObjectsDB objectsDB;
    /**
     * Map mapping spectra per file.
     */
    private HashMap<String, ArrayList<String>> spectraPerFile = null;
    /**
     * Ordered list of spectrum file names
     */
    private ArrayList<String> orderedSpectrumFileNames = null;

    /**
     * Constructor
     *
     * @param objectsDB the database for storing all objects on disk when memory
     * is too low
     */
    public Identification(ObjectsDB objectsDB) {
        this.objectsDB = objectsDB;
    }

    /**
     * Returns the objects database used in this class.
     * 
     * @return the objects database used in this class
     */
    public ObjectsDB getObjectsDB() {
        return objectsDB;
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
     */
    public void setOrderedListOfSpectrumFileNames(ArrayList<String> orderedSpectrumFileNames) {
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
        if (spectraPerFile == null) {
            fillSpectraPerFile();
        }
        return new ArrayList<>(spectraPerFile.keySet());
    }

    /**
     * Fills the spectra per file map
     */
    public void fillSpectraPerFile() {
        spectraPerFile = new HashMap<>(getNumber(SpectrumMatch.class));
        PsmIterator psmIterator = getPsmIterator(null);
        SpectrumMatch spectrumMatch;
        while ((spectrumMatch = psmIterator.next()) != null) {
            String key = spectrumMatch.getSpectrumFile();
            String title = spectrumMatch.getSpectrumTitle();
            if (!spectraPerFile.containsKey(key)) {
                spectraPerFile.put(key, new ArrayList<>());
            }
            spectraPerFile.get(key).add(title);
        }
    }

    /**
     * Returns the number of spectrum identifications.
     *
     * @return the number of spectrum identifications
     */
    public int getSpectrumIdentificationSize() {
        return objectsDB.getNumber(SpectrumMatch.class);
    }

    /**
     * Returns the number of objects of a given class
     *
     * @param className the class name of a given class
     * @return the number of objects
     */
    public int getNumber(Class className) {
        return objectsDB.getNumber(className);
    }

    /**
     * Returns an iterator of all objects of a given class
     *
     * @param className the class name of a given class
     * @param filters filters for the class
     * 
     * @return the iterator
     */
    public Iterator<?> getIterator(Class className, String filters) {
        return objectsDB.getObjectsIterator(className, filters);
    }

    /**
     * Returns the keys of the objects of the given class,
     * 
     * @param className the class
     * 
     * @return the keys of the objects
     */
    public HashSet<Long> getClassObjects(Class className) {
        return objectsDB.getClassObjects(className);
    }

    /**
     * Loads all objects of the class in cache.
     *
     * @param className the class name
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * 
     * @return returns the list of hashed keys
     */
    public ArrayList<Long> loadObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) {
        return objectsDB.loadObjects(className, waitingHandler, displayProgress);
    }

    /**
     * Loads all objects of given keys in cache.
     *
     * @param keyList the list of keys of given objects
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * 
     * @return returns the list of hashed keys
     */
    public ArrayList<Long> loadObjects(ArrayList<String> keyList, WaitingHandler waitingHandler, boolean displayProgress) {
        return objectsDB.loadObjects(keyList, waitingHandler, displayProgress);
    }
    
    /**
     * Returns an array of all objects of a given list of keys
     *
     * @param longKey the hash key
     * 
     * @return the objects
     */
    public Object retrieveObject(long longKey) {
        return objectsDB.retrieveObject(longKey);
    }

    /**
     * Returns an array of all objects of a given list of keys
     *
     * @param key the key
     * 
     * @return the objects
     */
    public Object retrieveObject(String key) {
        return objectsDB.retrieveObject(key);
    }

    /**
     * Returns an array of all objects of a key. If an exception is encountered
     * it is wrapped as runtime exception.
     *
     * @param key the key
     *
     * @return the objects
     */
    public Object retrieveObjectWrappedExceptions(String key) {
        try {
            return objectsDB.retrieveObject(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an array of all objects of a given list of keys
     *
     * @param keyList the key list
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * 
     * @return list of objects
     */
    public ArrayList<Object> retrieveObjects(ArrayList<String> keyList, WaitingHandler waitingHandler, boolean displayProgress) {
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
     * 
     * @return list of objects
     */
    public ArrayList<Object> retrieveObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) {
        return objectsDB.retrieveObjects(className, waitingHandler, displayProgress);
    }

    /**
     * Adds an object into the database.
     *
     * @param key the key of the object
     * @param object the object
     */
    public void addObject(String key, Object object) {
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
     */
    public void addObjects(HashMap<String, Object> objects, WaitingHandler waitingHandler, boolean displayProgress) {
        objectsDB.insertObjects(objects, waitingHandler, displayProgress);
    }

    /**
     * Removes an object from the database.
     *
     * @param key the key of the object
     */
    public void removeObject(String key) {
        if (objectsDB.retrieveObject(key) instanceof ProteinMatch) {
            if (proteinIdentification.contains(key)) {
                for (String protein : ProteinMatch.getAccessions(key)) {
                    HashSet<String> proteinKeys = proteinMap.get(protein);
                    if (proteinKeys != null) {
                        proteinKeys.remove(key);
                        if (proteinKeys.isEmpty()) {
                            proteinMap.remove(protein);
                        }
                    }
                }
            }

            proteinIdentification.remove(key);
        }

        objectsDB.removeObject(key);
    }

    /**
     * Checks if database contains a certain object.
     *
     * @param key the key of the object
     * 
     * @return true if database contains a certain object otherwise false
     */
    public boolean contains(String key) {
        return objectsDB.inDB(key);
    }

    /**
     * Remove a list of objects from the database.
     *
     * @param keys the list of object keys
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     */
    public void removeObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) {
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
        return objectsDB.inDB(matchKey);
    }

    /**
     * Indicates whether the protein, peptide and spectrum matches corresponding
     * to a protein match key are loaded in the cache. Note, only one peptide
     * and one spectrum match is tested.
     *
     * @param proteinKey the key of the protein match
     *
     * @return true if everything is loaded in memory
     */
    public boolean proteinDetailsInCache(String proteinKey) {
        if (objectsDB.inCache(proteinKey)) {
            ProteinMatch proteinMatch = (ProteinMatch) retrieveObject(proteinKey);
            String peptideKey = proteinMatch.getPeptideMatchesKeys().get(0);
            if (objectsDB.inCache(peptideKey)) {
                PeptideMatch peptideMatch = (PeptideMatch) retrieveObject(peptideKey);
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
     * 
     * @return true if everything is loaded in the cache
     */
    public boolean peptideDetailsInCache(String peptideKey) {
        if (objectsDB.inCache(peptideKey)) {
            PeptideMatch peptideMatch = (PeptideMatch) retrieveObject(peptideKey);
            return objectsDB.inCache(peptideMatch.getSpectrumMatchesKeys().get(0));
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
        HashSet<String> allKeys = new HashSet<>();
        for (String fileName : spectraPerFile.keySet()) {
            allKeys.addAll(spectraPerFile.get(fileName));
        }
        return allKeys;
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
     */
    public void buildPeptidesAndProteins(WaitingHandler waitingHandler, SequenceMatchingPreferences sequenceMatchingPreferences) {
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(getSpectrumIdentificationSize());
            waitingHandler.setSecondaryProgressCounter(0);
        }
        PsmIterator psmIterator = getPsmIterator(waitingHandler);
        SpectrumMatch spectrumMatch;
        while ((spectrumMatch = psmIterator.next()) != null) {
            buildPeptidesAndProteins(spectrumMatch, sequenceMatchingPreferences);
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
     * @param spectrumMatch the spectrum match to add
     * @param sequenceMatchingPreferences the sequence matching preferences
     */
    public void buildPeptidesAndProteins(SpectrumMatch spectrumMatch, SequenceMatchingPreferences sequenceMatchingPreferences) {

        if (spectrumMatch == null) {
            throw new IllegalArgumentException("Spectrum match not found.");
        }
        String spectrumMatchKey = spectrumMatch.getKey();
        if (spectrumMatch.getBestPeptideAssumption() != null) {
            Peptide peptide = spectrumMatch.getBestPeptideAssumption().getPeptide();
            if (peptide.getParentProteinsNoRemapping() == null) {
                peptide.getParentProteins(sequenceMatchingPreferences);
            }
            String peptideKey = peptide.getMatchingKey(sequenceMatchingPreferences);
            PeptideMatch peptideMatch;

            if (peptideIdentification.contains(peptideKey)) {
                peptideMatch = (PeptideMatch) retrieveObject(peptideKey);
                if (peptideMatch == null) {
                    throw new IllegalArgumentException("Peptide match " + peptideKey + " not found.");
                }
                peptideMatch.addSpectrumMatchKey(spectrumMatchKey);
            } else {
                peptideMatch = new PeptideMatch(peptide, peptideKey);
                peptideMatch.addSpectrumMatchKey(spectrumMatchKey);
                peptideIdentification.add(peptideKey);
                objectsDB.insertObject(peptideKey, peptideMatch);
            }

            String proteinKey = ProteinMatch.getProteinMatchKey(peptide);

            if (proteinIdentification.contains(proteinKey)) {
                ProteinMatch proteinMatch = (ProteinMatch) retrieveObject(proteinKey);
                if (proteinMatch == null) {
                    throw new IllegalArgumentException("Protein match " + proteinKey + " not found.");
                }
                if (!proteinMatch.getPeptideMatchesKeys().contains(peptideKey)) {
                    proteinMatch.addPeptideMatchKey(peptideKey);
                }
            } else {
                ProteinMatch proteinMatch = new ProteinMatch(peptideMatch.getPeptide(), peptideKey);
                if (!proteinMatch.getKey().equals(proteinKey)) {
                    throw new IllegalArgumentException("Protein inference issue: the protein key " + proteinKey + " does not match the peptide proteins " + proteinMatch.getKey() + "."
                            + " Peptide: " + peptideKey + " found in spectrum " + spectrumMatchKey + ".");
                }
                proteinIdentification.add(proteinKey);
                for (String protein : peptide.getParentProteinsNoRemapping()) {
                    if (!proteinMap.containsKey(protein)) {
                        proteinMap.put(protein, new HashSet<>());
                    }
                    if (!proteinMap.get(protein).contains(proteinKey)) {
                        proteinMap.get(protein).add(proteinKey);
                    }
                }
                objectsDB.insertObject(proteinKey, proteinMatch);
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
     */
    public void close() {
        objectsDB.close();
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
     *
     * @return the default reference
     */
    public static String getDefaultReference(String experimentReference) {
        return Util.removeForbiddenCharacters(experimentReference + "_id");
    }

    /**
     * Returns the keys of the protein matches where a peptide can be found.
     * Note: proteins have to be set for the peptide.
     *
     * @param peptide the peptide of interest
     *
     * @return the keys of the protein matches
     */
    public HashSet<String> getProteinMatches(Peptide peptide) {
        ArrayList<String> parentProteins = peptide.getParentProteinsNoRemapping();
        if (parentProteins == null) {
            throw new IllegalArgumentException("Proteins are not mapped for peptide " + peptide.getKey() + ".");
        }
        return parentProteins.stream().flatMap(accession -> proteinMap.get(accession).stream())
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Indicates whether a peptide is found in a single protein match.
     *
     * @param peptide the peptide of interest
     *
     * @return true if peptide is found in a single protein match
     */
    public boolean isUniqueInDatabase(Peptide peptide) {
        return getProteinMatches(peptide).size() == 1;
    }

    /**
     * Returns a psm iterator for a given key list.
     *
     * @param spectrumKeys the keys of the spectra to iterate
     * @param waitingHandler the waiting handler
     *
     * @return a peptide matches iterator
     */
    public PsmIterator getPsmIterator(ArrayList<String> spectrumKeys, WaitingHandler waitingHandler) {
        return new PsmIterator(spectrumKeys, this, waitingHandler, false);
    }

    /**
     * Returns a psm iterator for all SpectrumMatches.
     *
     * @param waitingHandler the waiting handler
     *
     * @return a peptide matches iterator
     */
    public PsmIterator getPsmIterator(WaitingHandler waitingHandler) {
        return new PsmIterator(this, waitingHandler, false);
    }

    /**
     * Returns a psm iterator for all SpectrumMatches.
     *
     * @param waitingHandler the waiting handler
     * @param filters filters for the class
     *
     * @return a peptide matches iterator
     */
    public PsmIterator getPsmIterator(WaitingHandler waitingHandler, String filters) {
        return new PsmIterator(null, this, waitingHandler, false, filters);
    }

    /**
     * Returns a peptide matches iterator for a given key list.
     *
     * @param peptideKeys the keys of the peptides to iterate
     * @param waitingHandler the waiting handler
     *
     * @return a peptide matches iterator
     */
    public PeptideMatchesIterator getPeptideMatchesIterator(ArrayList<String> peptideKeys, WaitingHandler waitingHandler) {
        return new PeptideMatchesIterator(peptideKeys, this, waitingHandler, false);
    }

    /**
     * Returns a peptide matches iterator for all PeptideMatches.
     *
     * @param waitingHandler the waiting handler
     *
     * @return a peptide matches iterator
     */
    public PeptideMatchesIterator getPeptideMatchesIterator(WaitingHandler waitingHandler) {
        return new PeptideMatchesIterator(this, waitingHandler, false);
    }

    /**
     * Returns a protein matches iterator for a given key list.
     *
     * @param proteinKeys the keys of the peptides to iterate
     * @param waitingHandler the waiting handler
     *
     * @return a peptide matches iterator
     */
    public ProteinMatchesIterator getProteinMatchesIterator(ArrayList<String> proteinKeys, WaitingHandler waitingHandler) {
        return new ProteinMatchesIterator(proteinKeys, this, waitingHandler, false);
    }

    /**
     * Returns a protein matches iterator for all PeptideMatches.
     *
     * @param waitingHandler the waiting handler
     *
     * @return a peptide matches iterator
     */
    public ProteinMatchesIterator getProteinMatchesIterator(WaitingHandler waitingHandler) {
        return new ProteinMatchesIterator(this, waitingHandler, false);
    }

}
