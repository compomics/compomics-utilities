package com.compomics.util.experiment.identification;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches_iterators.PeptideMatchesIterator;
import com.compomics.util.experiment.identification.matches_iterators.ProteinMatchesIterator;
import com.compomics.util.experiment.identification.matches_iterators.SpectrumMatchesIterator;
import com.compomics.util.experiment.identification.utils.ProteinUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * This class interacts with the back-end database to manage identification
 * objects. Interacting with the back-end database might cause
 * InterruptedException. These exceptions are passed as runtime exceptions for
 * methods returning identification objects.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 */
public class Identification extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -2551700699384242554L;
    /**
     * List of the keys of all imported proteins.
     */
    protected HashSet<Long> proteinIdentification = new HashSet<>();
    /**
     * List of the keys of all imported peptides.
     */
    protected HashSet<Long> peptideIdentification = new HashSet<>();
    /**
     * Map mapping spectra per file.
     */
    private HashMap<String, HashSet<Long>> spectrumIdentification = null;
    /**
     * A map linking protein accessions to all their protein matches keys.
     */
    protected HashMap<String, HashSet<Long>> proteinMap = new HashMap<>();
    /**
     * The directory where the database stored.
     */
    protected String dbDirectory;
    /**
     * The database which will contain the objects.
     */
    private final ObjectsDB objectsDB;

    /**
     * Constructor.
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
     * Fills the spectra per file map.
     */
    private void fillSpectrumIdentification() {

        try {

            spectrumIdentification = getClassObjects(SpectrumMatch.class).stream()
                    .collect(Collectors.groupingBy(
                            key -> Spectrum.getSpectrumFile(getSpectrumMatch(key).getSpectrumKey()),
                            HashMap::new,
                            Collectors.toCollection(HashSet::new)));

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Returns a map of the spectrum matches keys indexed by spectrum file name.
     *
     * @return a map of the spectrum matches keys indexed by spectrum file name
     */
    public HashMap<String, HashSet<Long>> getSpectrumIdentification() {

        if (spectrumIdentification == null) {

            fillSpectrumIdentification();

        }

        return spectrumIdentification;
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
     *
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

        try {

            return objectsDB.getObjectsIterator(className, filters);

        } catch (InterruptedException ex) {

            throw new RuntimeException(ex);

        }
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
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void loadObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {
        objectsDB.loadObjects(className, waitingHandler, displayProgress);
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
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void loadObjects(ArrayList<Long> keyList, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {
        objectsDB.loadObjects(keyList, waitingHandler, displayProgress);
    }

    /**
     * Returns an array of all objects of a given list of keys
     *
     * @param longKey the hash key
     *
     * @return the objects
     */
    public Object retrieveObject(long longKey) {

        try {

            return objectsDB.retrieveObject(longKey);

        } catch (InterruptedException ex) {

            throw new RuntimeException(ex);

        }
    }

    /**
     * Returns the spectrum match with the given key.
     *
     * @param key the key of the match
     *
     * @return the spectrum match with the given key
     */
    public SpectrumMatch getSpectrumMatch(long key) {

        return (SpectrumMatch) retrieveObject(key);

    }

    /**
     * Returns the peptide match with the given key.
     *
     * @param key the key of the match
     *
     * @return the peptide match with the given key
     */
    public PeptideMatch getPeptideMatch(long key) {

        return (PeptideMatch) retrieveObject(key);

    }

    /**
     * Returns the protein match with the given key.
     *
     * @param key the key of the match
     *
     * @return the protein match with the given key
     */
    public ProteinMatch getProteinMatch(long key) {

        return (ProteinMatch) retrieveObject(key);

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
    public ArrayList<Object> retrieveObjects(Collection<Long> keyList, WaitingHandler waitingHandler, boolean displayProgress) {

        try {

            return objectsDB.retrieveObjects(keyList, waitingHandler, displayProgress);

        } catch (InterruptedException ex) {

            throw new RuntimeException(ex);

        }
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

        try {

            return objectsDB.retrieveObjects(className, waitingHandler, displayProgress);

        } catch (InterruptedException ex) {

            throw new RuntimeException(ex);

        }
    }

    /**
     * Adds an object into the database.
     *
     * @param key the key of the object
     * @param object the object
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void addObject(long key, Object object) throws InterruptedException {
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
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void addObjects(HashMap<Long, Object> objects, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {
        objectsDB.insertObjects(objects, waitingHandler, displayProgress);
    }

    /**
     * Removes an object from the database.
     *
     * @param key the key of the object
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void removeObject(long key) throws InterruptedException {

        Object object = objectsDB.retrieveObject(key);

        if (object instanceof ProteinMatch) {

            ProteinMatch proteinMatch = (ProteinMatch) object;

            for (String accession : proteinMatch.getAccessions()) {

                HashSet<Long> proteinKeys = proteinMap.get(accession);

                if (proteinKeys != null) {

                    proteinKeys.remove(key);

                    if (proteinKeys.isEmpty()) {

                        proteinMap.remove(accession);

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
    public boolean contains(long key) {
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
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void removeObjects(ArrayList<Long> keys, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {
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
     * Returns a list of the keys of all encountered proteins.
     *
     * @return the corresponding identification results
     */
    public HashSet<Long> getProteinIdentification() {
        return proteinIdentification;
    }

    /**
     * Returns a list of the keys of all encountered peptides.
     *
     * @return the corresponding identification results
     */
    public HashSet<Long> getPeptideIdentification() {
        return peptideIdentification;
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
     * @param sequenceProvider a provider of protein sequences
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void buildPeptidesAndProteins(SpectrumMatch spectrumMatch, SequenceMatchingParameters sequenceMatchingPreferences, SequenceProvider sequenceProvider) throws InterruptedException {

        long spectrumMatchKey = spectrumMatch.getKey();

        Peptide peptide = spectrumMatch.getBestPeptideAssumption().getPeptide();
        long peptideMatchKey = peptide.getMatchingKey(sequenceMatchingPreferences);
        PeptideMatch peptideMatch = getPeptideMatch(peptideMatchKey);

        if (peptideMatch == null) {

            peptideMatch = new PeptideMatch(peptide, peptideMatchKey, spectrumMatchKey);
            peptideIdentification.add(peptideMatchKey);
            objectsDB.insertObject(peptideMatchKey, peptideMatch);

        } else {

            peptideMatch.addSpectrumMatchKey(spectrumMatchKey);

        }

        long proteinMatchKey = ProteinMatch.getProteinMatchKey(peptide);
        ProteinMatch proteinMatch = getProteinMatch(proteinMatchKey);

        if (proteinMatch == null) {

            proteinMatch = new ProteinMatch(peptideMatch.getPeptide(), peptideMatchKey);
            proteinMatch.setDecoy(Arrays.stream(proteinMatch.getAccessions())
                    .anyMatch(accession -> ProteinUtils.isDecoy(accession, sequenceProvider)));

            for (String proteinAccession : proteinMatch.getAccessions()) {

                HashSet<Long> proteinMatchKeys = proteinMap.get(proteinAccession);

                if (proteinMatchKeys == null) {

                    proteinMatchKeys = new HashSet<>(1);
                    proteinMap.put(proteinAccession, proteinMatchKeys);

                }

                proteinMatchKeys.add(proteinMatchKey);

            }

            proteinIdentification.add(proteinMatchKey);
            objectsDB.insertObject(proteinMatchKey, proteinMatch);

        } else {

            proteinMatch.addPeptideMatchKey(peptideMatchKey);

        }
    }

    /**
     * Returns a map of all the protein matches which can be ascribed to a
     * protein indexed by its accession.
     *
     * @return a map of all the protein matches which can be ascribed to a
     * protein indexed by its accession.
     */
    public HashMap<String, HashSet<Long>> getProteinMap() {
        return proteinMap;
    }

    /**
     * Closes the database connection.
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void close() throws InterruptedException {
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
     * Returns the keys of the protein matches where a peptide can be found.
     *
     * @param peptide the peptide of interest
     *
     * @return the keys of the protein matches
     */
    public HashSet<Long> getProteinMatches(Peptide peptide) {

        return peptide.getProteinMapping().navigableKeySet().stream()
                .filter(accession -> proteinMap.containsKey(accession))
                .flatMap(accession -> proteinMap.get(accession).stream())
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Returns a spectrum matches iterator for a given key list.
     *
     * @param spectrumMatches the keys of the spectra to iterate
     * @param waitingHandler the waiting handler
     *
     * @return a spectrum matches iterator
     */
    public SpectrumMatchesIterator getSpectrumMatchesIterator(long[] spectrumMatches, WaitingHandler waitingHandler) {
        return new SpectrumMatchesIterator(spectrumMatches, this, waitingHandler, false);
    }

    /**
     * Returns a spectrum matches iterator for all SpectrumMatches.
     *
     * @param waitingHandler the waiting handler
     *
     * @return a spectrum matches iterator
     */
    public SpectrumMatchesIterator getSpectrumMatchesIterator(WaitingHandler waitingHandler) {
        return new SpectrumMatchesIterator(this, waitingHandler, false);
    }

    /**
     * Returns a psm iterator for all SpectrumMatches.
     *
     * @param waitingHandler the waiting handler
     * @param filters filters for the class
     *
     * @return a peptide matches iterator
     */
    public SpectrumMatchesIterator getPsmIterator(WaitingHandler waitingHandler, String filters) {
        return new SpectrumMatchesIterator(null, this, waitingHandler, false, filters);
    }

    /**
     * Returns a peptide matches iterator for a given key list.
     *
     * @param peptideKeys the keys of the peptides to iterate
     * @param waitingHandler the waiting handler
     *
     * @return a peptide matches iterator
     */
    public PeptideMatchesIterator getPeptideMatchesIterator(long[] peptideKeys, WaitingHandler waitingHandler) {
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
    public ProteinMatchesIterator getProteinMatchesIterator(long[] proteinKeys, WaitingHandler waitingHandler) {
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
