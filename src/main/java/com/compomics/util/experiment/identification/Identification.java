package com.compomics.util.experiment.identification;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches_iterators.PeptideMatchesIterator;
import com.compomics.util.experiment.identification.matches_iterators.ProteinMatchesIterator;
import com.compomics.util.experiment.identification.matches_iterators.SpectrumMatchesIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.waiting.WaitingHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
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
     * Empty default constructor
     */
    public Identification() {
        objectsDB = null;
    }
    /**
     * The keys of the objects in the identification.
     */
    private IdentificationKeys identificationKeys = new IdentificationKeys();
    /**
     * The directory where the database stored.
     */
    private String dbDirectory;
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
     * Returns the identification keys.
     * 
     * @return the identification keys
     */
    public IdentificationKeys getIdentificationKeys() {
        return identificationKeys;
    }

    /**
     * Sets the identification keys.
     * 
     * @param identificationKeys the identification keys
     */
    public void setIdentificationKeys(IdentificationKeys identificationKeys) {
        this.identificationKeys = identificationKeys;
    }
    
    /**
     * Fills the spectra per file map.
     */
    public synchronized void fillSpectrumIdentification() {

            identificationKeys.spectrumIdentification = getClassObjects(SpectrumMatch.class).stream()
                    .collect(Collectors.groupingBy(
                            key -> Spectrum.getSpectrumFile(getSpectrumMatch(key).getSpectrumKey()),
                            HashMap::new,
                            Collectors.toCollection(HashSet::new)));
}

    /**
     * Returns a map of the spectrum matches keys indexed by spectrum file name.
     *
     * @return a map of the spectrum matches keys indexed by spectrum file name
     */
    public HashMap<String, HashSet<Long>> getSpectrumIdentification() {
        return identificationKeys.spectrumIdentification;
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
        return objectsDB.retrieveObject(longKey);

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
    public void addObject(long key, Object object) {

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
    public void addObjects(HashMap<Long, Object> objects, WaitingHandler waitingHandler, boolean displayProgress) {
        objectsDB.insertObjects(objects, waitingHandler, displayProgress);
    }

    /**
     * Removes an object from the database.
     *
     * @param key the key of the object
     */
    public void removeObject(long key) {

        Object object = objectsDB.retrieveObject(key);

        if (object instanceof ProteinMatch) {

            ProteinMatch proteinMatch = (ProteinMatch) object;

            for (String accession : proteinMatch.getAccessions()) {

                HashSet<Long> proteinKeys = identificationKeys.proteinMap.get(accession);

                if (proteinKeys != null) {

                    proteinKeys.remove(key);

                    if (proteinKeys.isEmpty()) {

                        identificationKeys.proteinMap.remove(accession);

                    }
                }
            }

            identificationKeys.proteinIdentification.remove(key);

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
     */
    public void removeObjects(ArrayList<Long> keys, WaitingHandler waitingHandler, boolean displayProgress) {
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
        return identificationKeys.proteinIdentification;
    }

    /**
     * Returns a list of the keys of all encountered peptides.
     *
     * @return the corresponding identification results
     */
    public HashSet<Long> getPeptideIdentification() {
        return identificationKeys.peptideIdentification;
    }

    /**
     * Adds a peptide match. If an exception occurs when saving to the db it is
     * thrown as runtime exception.
     *
     * @param key the peptide match key
     * @param peptideMatch the peptide match
     */
    public void addPeptideMatch(long key, PeptideMatch peptideMatch) {

        identificationKeys.peptideIdentification.add(key);

            objectsDB.insertObject(key, peptideMatch);
    }

    /**
     * Adds a protein match. If an exception occurs when saving to the db it is
     * thrown as runtime exception.
     *
     * @param key the peptide match key
     * @param proteinMatch the protein match
     */
    public void addProteinMatch(long key, ProteinMatch proteinMatch) {

        for (String proteinAccession : proteinMatch.getAccessions()) {

            HashSet<Long> proteinMatchKeys = identificationKeys.proteinMap.get(proteinAccession);

            if (proteinMatchKeys == null) {

                proteinMatchKeys = new HashSet<>(1);
                identificationKeys.proteinMap.put(proteinAccession, proteinMatchKeys);

            }

            proteinMatchKeys.add(key);

        }

        identificationKeys.proteinIdentification.add(key);

            objectsDB.insertObject(key, proteinMatch);
            
    }

    /**
     * Returns a map of all the protein matches which can be ascribed to a
     * protein indexed by its accession.
     *
     * @return a map of all the protein matches which can be ascribed to a
     * protein indexed by its accession.
     */
    public HashMap<String, HashSet<Long>> getProteinMap() {
        return identificationKeys.proteinMap;
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
     * Returns the keys of the protein matches where a peptide can be found.
     *
     * @param peptide the peptide of interest
     *
     * @return the keys of the protein matches
     */
    public HashSet<Long> getProteinMatches(Peptide peptide) {

        return peptide.getProteinMapping().navigableKeySet().stream()
                .filter(accession -> identificationKeys.proteinMap.containsKey(accession))
                .flatMap(accession -> identificationKeys.proteinMap.get(accession).stream())
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
    public SpectrumMatchesIterator getSpectrumMatchesIterator(WaitingHandler waitingHandler, String filters) {
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

    /**
     * Adds a fraction, fractions correspond to the PSM files names. Fractions
     * are ordered alphabetically upon adding of a new fraction.
     *
     * @param fraction the fraction name
     */
    public synchronized void addFraction(String fraction) {

        TreeSet orderedFractions = new TreeSet(identificationKeys.fractions);
        orderedFractions.add(fraction);

        setFractions(new ArrayList<>(orderedFractions));

    }

    /**
     * Returns the fractions.
     *
     * @return the fractions
     */
    public ArrayList<String> getFractions() {
        return identificationKeys.fractions;
    }

    /**
     * Sets the fractions.
     *
     * @param fractions the fractions
     */
    public synchronized void setFractions(ArrayList<String> fractions) {

        identificationKeys.fractions = fractions;

    }

}
