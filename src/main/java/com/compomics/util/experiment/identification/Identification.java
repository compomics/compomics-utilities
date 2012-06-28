package com.compomics.util.experiment.identification;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch.MatchType;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.io.*;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JProgressBar;

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
     * List of the keys of all imported psms.
     */
    protected ArrayList<String> spectrumIdentification = new ArrayList<String>();
    /**
     * a map linking protein accessions to all their protein matches keys.
     */
    protected HashMap<String, ArrayList<String>> proteinMap = new HashMap<String, ArrayList<String>>();
    /**
     * The method used.
     */
    protected int methodUsed;
    /**
     * The cache size in number of matches. 20000 by default: should be enough
     * to contain a velos file.
     */
    protected int cacheSize = 20000;
    /**
     * the directory where matches will be serialized/the database stored
     */
    protected String serializationDirectory;
    /**
     * boolean indicating whether the identification should be stored in memory
     * or not. True by default, the serialization directory should be set
     * otherwise.
     */
    protected boolean inMemory = true;
    /**
     * boolean indicating whether the memory management should be done
     * automatically. If true, the cache size will be extended to reach 99% of
     * the available heap size when inMemory is wrong. True by default.
     */
    protected boolean automatedMemoryManagement = true;
    /**
     * Map of the loaded matches.
     */
    protected HashMap<String, IdentificationMatch> loadedMatchesMap = new HashMap<String, IdentificationMatch>();
    /**
     * List of the loaded matches with the most used matches in the end.
     */
    protected ArrayList<String> loadedMatches = new ArrayList<String>();
    /**
     * Map indicating whether a match is modified. Only modified matches will be
     * serialized.
     */
    protected HashMap<String, Boolean> modifiedMatches = new HashMap<String, Boolean>();
    /**
     * Map of the user's parameters.
     *
     * @deprecated use the database instead
     */
    protected HashMap<String, HashMap<String, UrParameter>> urParameters = new HashMap<String, HashMap<String, UrParameter>>();
    /**
     * Map of long keys (>100 characters) which will be referenced by their
     * index for file creation/database storage. @TODO implement this for db
     * keys?
     */
    protected ArrayList<String> longKeys = new ArrayList<String>();
    /**
     * Boolean indicating whether the matches should be stored in a database
     * (default) or in serialized files.
     */
    private Boolean isDB = true;
    /**
     * the identificationDB object interacting with the database
     */
    private IdentificationDB identificationDB;

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
            return identificationDB.getMatchPArameter(matchKey, urParameter);
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
        if (isDB) {
            return identificationDB.getSpectrumMatchParameter(key, urParameter);
        } else {
            return getMatchParameter(key, urParameter);
        }
    }

    /**
     * Adds a spectrum match parameter to the database
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
     * Returns the desired peptide match parameter
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
        if (isDB) {
            return identificationDB.getPeptideMatchParameter(key, urParameter);
        } else {
            return getMatchParameter(key, urParameter);
        }
    }

    /**
     * Adds a peptide match parameter to the database
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
     * Returns the desired protein match parameter
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
        if (isDB) {
            return identificationDB.getProteinMatchParameter(key, urParameter);
        } else {
            return getMatchParameter(key, urParameter);
        }
    }

    /**
     * Adds a protein match parameter to the database
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
     * Updates a protein match parameter in the database
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
     * Updates a peptide match parameter in the database
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
     * Updates a spectrum match parameter in the database
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
     * Returns whether the memory management is automated.
     *
     * @return whether the memory management is automated.
     */
    public boolean isAutomatedMemoryManagement() {
        return automatedMemoryManagement;
    }

    /**
     * Sets whether the memory management should be automated.
     *
     * @param automatedMemoryManagement a boolean indicating whether the memory
     * management should be automated
     */
    public void setAutomatedMemoryManagement(boolean automatedMemoryManagement) {
        this.automatedMemoryManagement = automatedMemoryManagement;
    }

    /**
     * Returns the cache size in number of matches.
     *
     * @return the cache size in number of matches
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Sets the cache size.
     *
     * @param cacheSize number of matches to allow in the cache size
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Indicates whether matches will be stored in memory.
     *
     * @return a boolean indicating whether matches will be stored in memory
     */
    public boolean isInMemory() {
        return inMemory;
    }

    /**
     * Sets whether matches shall be stored in memory.
     *
     * @param inMemory a boolean indicating whether matches shall be stored in
     * memory
     */
    public void setInMemory(boolean inMemory) {
        this.inMemory = inMemory;
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
     * sets the directory where matches will be stored in order to save memory.
     * Matches can be stored in a database (default) or serialized files. If the
     * database option is chosen (see setIsDB(Boolean isDB)) and no database
     * created, the database will be created in the folder.
     *
     * @param serializationDirectory the path of the directory
     * @throws SQLException 
     * @deprecated use establishConnection(String dbFolder) instead
     */
    public void setDirectory(String serializationDirectory) throws SQLException {
        this.serializationDirectory = serializationDirectory;
        if (isDB && identificationDB == null) {
            identificationDB = new IdentificationDB(serializationDirectory);
        }
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
     */
    public void removeMatch(String matchKey) throws IllegalArgumentException, SQLException {
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
        spectrumIdentification.remove(matchKey);
        peptideIdentification.remove(matchKey);
        if (loadedMatches.contains(matchKey)) {
            loadedMatches.remove(matchKey);
            loadedMatchesMap.remove(matchKey);
            modifiedMatches.remove(matchKey);
        } else {
            if (isDB) {
                identificationDB.removeMatch(matchKey);
            } else {
                File matchFile = new File(serializationDirectory, getFileName(matchKey));
                matchFile.delete();
            }
        }
    }

    /**
     * Removes a spectrum match from the model.
     *
     * @param matchKey the key of the match to remove
     * @throws IllegalArgumentException
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     */
    public void removeSpectrumMatch(String matchKey) throws IllegalArgumentException, SQLException {
        spectrumIdentification.remove(matchKey);
        if (loadedMatches.contains(matchKey)) {
            loadedMatches.remove(matchKey);
            loadedMatchesMap.remove(matchKey);
            modifiedMatches.remove(matchKey);
        } else {
            if (isDB) {
                identificationDB.removeSpectrumMatch(matchKey);
            } else {
                File matchFile = new File(serializationDirectory, getFileName(matchKey));
                matchFile.delete();
            }
        }
    }

    /**
     * Removes a peptide match from the model.
     *
     * @param matchKey the key of the match to remove
     * @throws IllegalArgumentException
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     */
    public void removePeptideMatch(String matchKey) throws IllegalArgumentException, SQLException {
        peptideIdentification.remove(matchKey);
        if (loadedMatches.contains(matchKey)) {
            loadedMatches.remove(matchKey);
            loadedMatchesMap.remove(matchKey);
            modifiedMatches.remove(matchKey);
        } else {
            if (isDB) {
                identificationDB.removePeptideMatch(matchKey);
            } else {
                File matchFile = new File(serializationDirectory, getFileName(matchKey));
                matchFile.delete();
            }
        }
    }

    /**
     * Removes a protein match from the model.
     *
     * @param matchKey the key of the match to remove
     * @throws IllegalArgumentException
     * @throws SQLException exception thrown whenever an error occurred while
     * deleting the match
     */
    public void removeProteinMatch(String matchKey) throws IllegalArgumentException, SQLException {
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
        if (loadedMatches.contains(matchKey)) {
            loadedMatches.remove(matchKey);
            loadedMatchesMap.remove(matchKey);
            modifiedMatches.remove(matchKey);
        } else {
            if (isDB) {
                identificationDB.removeProteinMatch(matchKey);
            } else {
                File matchFile = new File(serializationDirectory, getFileName(matchKey));
                matchFile.delete();
            }
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
            loadedMatchesMap.put(matchKey, match);
            loadedMatches.add(matchKey);
            modifiedMatches.put(matchKey, false);
            updateCache();
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
        int index = loadedMatches.indexOf(spectrumKey);
        if (index == -1) {
            if (isDB) {
                SpectrumMatch match = identificationDB.getSpectrumMatch(spectrumKey);
                loadedMatchesMap.put(spectrumKey, match);
                loadedMatches.add(spectrumKey);
                modifiedMatches.put(spectrumKey, false);
                updateCache();
                return match;
            } else {
                return (SpectrumMatch) getMatch(spectrumKey);
            }
        } else {
            if (index < 0.25 * loadedMatches.size()) {
                loadedMatches.remove(spectrumKey);
                loadedMatches.add(spectrumKey);
            }
            return (SpectrumMatch) loadedMatchesMap.get(spectrumKey);
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
        int index = loadedMatches.indexOf(peptideKey);
        if (index == -1) {
            if (isDB) {
                PeptideMatch match = identificationDB.getPeptideMatch(peptideKey);
                loadedMatchesMap.put(peptideKey, match);
                loadedMatches.add(peptideKey);
                modifiedMatches.put(peptideKey, false);
                updateCache();
                return match;
            } else {
                return (PeptideMatch) getMatch(peptideKey);
            }
        } else {
            if (index < 0.25 * loadedMatches.size()) {
                loadedMatches.remove(peptideKey);
                loadedMatches.add(peptideKey);
            }
            return (PeptideMatch) loadedMatchesMap.get(peptideKey);
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
        int index = loadedMatches.indexOf(proteinKey);
        if (index == -1) {
            if (isDB) {
                ProteinMatch match = identificationDB.getProteinMatch(proteinKey);
                loadedMatchesMap.put(proteinKey, match);
                loadedMatches.add(proteinKey);
                modifiedMatches.put(proteinKey, false);
                updateCache();
                return match;
            } else {
                return (ProteinMatch) getMatch(proteinKey);
            }
        } else {
            if (index < 0.25 * loadedMatches.size()) {
                loadedMatches.remove(proteinKey);
                loadedMatches.add(proteinKey);
            }
            return (ProteinMatch) loadedMatchesMap.get(proteinKey);
        }
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
     * @return the corresponding identification results
     */
    public ArrayList<String> getSpectrumIdentification() {
        return spectrumIdentification;
    }

    /**
     * Adds a spectrum match to the identification
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
        if (spectrumIdentification.contains(spectrumKey)) {
            SpectrumMatch oldMatch = getSpectrumMatch(spectrumKey);
            for (int searchEngine : newMatch.getAdvocates()) {
                oldMatch.addHit(searchEngine, newMatch.getFirstHit(searchEngine));
            }
            setMatchChanged(oldMatch);
        } else {
            spectrumIdentification.add(spectrumKey);
            loadedMatchesMap.put(spectrumKey, newMatch);
            loadedMatches.add(spectrumKey);
            modifiedMatches.put(spectrumKey, true);
            updateCache();
        }
    }

    /**
     * Updates the cache according to the memory settings.
     *
     * @throws FileNotFoundException exception thrown whenever an error occurred
     * while serializing a match
     * @throws IOException exception thrown whenever an error occurred while
     * serializing a match
     * @throws SQLException
     * @throws ClassNotFoundException  
     */
    public void updateCache() throws FileNotFoundException, IOException, SQLException, ClassNotFoundException {
        if (!inMemory) {
            while (!automatedMemoryManagement && loadedMatches.size() > cacheSize
                    || !memoryCheck()) {
                String key = loadedMatches.get(0);
                if (modifiedMatches.get(key)) {
                    if (isDB) {
                        try {
                            identificationDB.addMatch((IdentificationMatch) loadedMatchesMap.get(key));
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new IOException("Error while writing match " + key + "in the database.");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            throw new SQLException("Error while writing match " + key + "in the database.");
                        }
                    } else {
                        try {
                            File matchFile = new File(serializationDirectory, getFileName(key));
                            FileOutputStream fos = new FileOutputStream(matchFile);
                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                            ObjectOutputStream oos = new ObjectOutputStream(bos);
                            oos.writeObject(loadedMatchesMap.get(key));
                            oos.close();
                            bos.close();
                            fos.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            throw new FileNotFoundException("Error while writing match " + key);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new IOException("Error while writing match " + key);
                        }
                    }
                }
                loadedMatches.remove(0);
                loadedMatchesMap.remove(key);
                modifiedMatches.remove(key);
                if (loadedMatches.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * Indicates whether the memory used by the application is lower than 99% of
     * the heap size.
     *
     * @return a boolean indicating whether the memory used by the application
     * is lower than 99% of the heap
     */
    public boolean memoryCheck() {
        return Runtime.getRuntime().totalMemory() < (long) (0.99 * Runtime.getRuntime().maxMemory());
    }

    /**
     * Reduces the amount of identification saved in memory by 20%.
     *
     * @param progressBar the progress bar
     * @throws FileNotFoundException exception thrown whenever an error occurred
     * while serializing a match
     * @throws IOException exception thrown whenever an error occurred while
     * serializing a match or reading the database
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     */
    public void reduceMemoryConsumtion(JProgressBar progressBar) throws FileNotFoundException, IOException, SQLException {
        if (progressBar != null) {
            progressBar.setValue(0);
            progressBar.setMaximum((int) (0.20 * loadedMatches.size()));
        }
        for (int cpt = 0; cpt < 0.20 * loadedMatches.size(); cpt++) {
            String key = loadedMatches.get(0);
            if (modifiedMatches.get(key)) {
                if (isDB) {
                    try {
                        identificationDB.addMatch((IdentificationMatch) loadedMatchesMap.get(key));
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IOException("Error while writing match " + key + "in the database.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new SQLException("Error while writing match " + key + "in the database.");
                    }
                } else {
                    try {
                        File matchFile = new File(serializationDirectory, getFileName(key));
                        FileOutputStream fos = new FileOutputStream(matchFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(loadedMatchesMap.get(key));
                        oos.close();
                        bos.close();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        throw new FileNotFoundException("Error while writing match " + key);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IOException("Error while writing match " + key);
                    }
                }
            }
            loadedMatches.remove(0);
            loadedMatchesMap.remove(key);
            modifiedMatches.remove(key);
            if (progressBar != null) {
                progressBar.setValue(cpt);
            }
            if (loadedMatches.isEmpty()) {
                break;
            }
        }
        System.gc();
    }

    /**
     * Creates the peptides and protein instances based on the spectrum matches.
     * Note that the attribute bestAssumption should be set for every spectrum
     * match at this point. This operation will be very slow if the cache is
     * already full.
     *
     * @param progressBar the progress bar
     * @throws IllegalArgumentException
     * @throws SQLException 
     * @throws IOException
     * @throws ClassNotFoundException  
     */
    public void buildPeptidesAndProteins(JProgressBar progressBar) throws IllegalArgumentException, SQLException, IOException, ClassNotFoundException {
        if (progressBar != null) {
            progressBar.setValue(0);
            progressBar.setMaximum(getSpectrumIdentification().size());
        }
        int cpt = 0;
        for (String spectrumMatchKey : getSpectrumIdentification()) {
            buildPeptidesAndProteins(spectrumMatchKey);
            if (progressBar != null) {
                progressBar.setValue(++cpt);
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

        SpectrumMatch spectrumMatch = getSpectrumMatch(spectrumMatchKey);
        Peptide peptide = spectrumMatch.getBestAssumption().getPeptide();
        String peptideKey = peptide.getKey();
        PeptideMatch peptideMatch;

        if (peptideIdentification.contains(peptideKey)) {
            peptideMatch = getPeptideMatch(peptideKey);
            peptideMatch.addSpectrumMatch(spectrumMatchKey);
            setMatchChanged(peptideMatch);
        } else {
            peptideMatch = new PeptideMatch(peptide, spectrumMatchKey);
            peptideIdentification.add(peptideKey);
            loadedMatches.add(peptideKey);
            loadedMatchesMap.put(peptideKey, peptideMatch);
            modifiedMatches.put(peptideKey, true);
        }

        String proteinKey = ProteinMatch.getProteinMatchKey(peptide);

        if (proteinIdentification.contains(proteinKey)) {
            ProteinMatch proteinMatch = getProteinMatch(proteinKey);
            if (!proteinMatch.getPeptideMatches().contains(peptideKey)) {
                proteinMatch.addPeptideMatch(peptideKey);
                setMatchChanged(proteinMatch);
            }
        } else {
            ProteinMatch proteinMatch = new ProteinMatch(peptideMatch.getTheoreticPeptide());
            proteinIdentification.add(proteinKey);
            loadedMatches.add(proteinKey);
            loadedMatchesMap.put(proteinKey, proteinMatch);
            modifiedMatches.put(proteinKey, true);

            for (String protein : peptide.getParentProteins()) {
                if (!proteinMap.containsKey(protein)) {
                    proteinMap.put(protein, new ArrayList<String>(5));
                }
                if (!proteinMap.get(protein).contains(proteinKey)) {
                    proteinMap.get(protein).add(proteinKey);
                }
            }
        }
    }

    /**
     * Empties the cache and serializes everything in the specified
     * serialization folder or in the database.
     *
     * @param progressDialog
     * @throws FileNotFoundException exception thrown whenever an error occurred
     * while serializing a match
     * @throws IOException exception thrown whenever an error occurred while
     * serializing a match
     * @throws SQLException exception thrown whenever an error occurred while
     * serializing a match
     */
    public void emptyCache(ProgressDialogX progressDialog) throws FileNotFoundException, IOException, SQLException {
        if (progressDialog != null) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMaxProgressValue(loadedMatchesMap.size());
        }
        int cpt = 0;
        for (String key : loadedMatchesMap.keySet()) {

            if (progressDialog.isRunCanceled()) {
                break;
            }

            if (modifiedMatches.get(key)) {
                if (isDB) {
                    try {
                        identificationDB.addMatch((IdentificationMatch) loadedMatchesMap.get(key));
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IOException("Error while writing match " + key + "in the database.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new SQLException("Error while writing match " + key + "in the database.");
                    }
                } else {
                    try {
                        File matchFile = new File(serializationDirectory, getFileName(key));
                        FileOutputStream fos = new FileOutputStream(matchFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(loadedMatchesMap.get(key));
                        oos.close();
                        bos.close();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        throw new FileNotFoundException("Error while writing match " + key);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IOException("Error while writing match " + key);
                    }
                }
            }
            if (progressDialog != null) {
                progressDialog.setValue(++cpt);
            }
        }

        if (!progressDialog.isRunCanceled()) {
            loadedMatches.clear();
            loadedMatchesMap.clear();
            modifiedMatches.clear();
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
     * Indicates that a match was changed, it will thus be serialized again if
     * needed.
     *
     * @param match
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws SQLException  
     */
    public void setMatchChanged(IdentificationMatch match) throws IllegalArgumentException, IOException, SQLException {

        String key = match.getKey();

        if (loadedMatches.contains(key)) {
            modifiedMatches.put(key, true);
        } else {
            if (isDB) {
                try {
                    identificationDB.updateMatch(match);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IOException("Error while writing match " + key + "in the database.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new SQLException("Error while writing match " + key + "in the database.");
                }
            } else {
                try {
                    File matchFile = new File(serializationDirectory, getFileName(key));
                    FileOutputStream fos = new FileOutputStream(matchFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(match);
                    oos.close();
                    bos.close();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Error while writing match " + key);
                }
            }
        }
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
     * serialized files
     *
     * @param isDB a boolean indicating whether the identification matches
     * should be stored in a database or serialized files
     */
    public void setIsDB(Boolean isDB) {
        this.isDB = isDB;
    }

    /**
     * Closes the database connection
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     */
    public void close() throws SQLException {
        if (isDB) {
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
        }
        return null;
    }

    /**
     * Establishes a connection to the database
     *
     * @param dbFolder the absolute path to the folder where the database is
     * located
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection
     */
    public void establishConnection(String dbFolder) throws SQLException {
        if (identificationDB == null) {
            identificationDB = new IdentificationDB(serializationDirectory);
        }
        identificationDB.establishConnection(dbFolder);
    }

    /**
     * Converts a serlialization based structure into a database based one
     *
     * @param progressDialog a dialog to give progress feedback to the user
     * @param newDirectory the new directory where to store the data
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a match
     * @throws SQLException exception thrown whenever an error occurred while
     * interacting with the database
     */
    public void convert(ProgressDialogX progressDialog, String newDirectory) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        setIsDB(true);
        if (identificationDB == null) {
            identificationDB = new IdentificationDB(newDirectory);
        }
        File directory = new File(serializationDirectory);
        File[] files = directory.listFiles();
        int nParameters = 0;
        for (HashMap<String, UrParameter> map : urParameters.values()) {
            nParameters += map.size();
        }
        if (progressDialog != null) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMaxProgressValue(files.length + nParameters);
        }
        MatchType matchType;
        for (String matchKey : urParameters.keySet()) {
            matchType = getMatchType(matchKey);
            for (UrParameter urParameter : urParameters.get(matchKey).values()) {
                if (matchType == MatchType.Protein) {
                    addProteinMatchParameter(matchKey, urParameter);
                } else if (matchType == MatchType.Peptide) {
                    addPeptideMatchParameter(matchKey, urParameter);
                } else if (matchType == MatchType.Spectrum) {
                    addSpectrumMatchParameter(matchKey, urParameter);
                }
            }
            if (progressDialog != null) {
                progressDialog.increaseProgressValue();
            }
            if (progressDialog.isRunCanceled()) {
                break;
            }
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
                String matchKey = match.getKey();
                loadedMatchesMap.put(matchKey, match);
                loadedMatches.add(matchKey);
                modifiedMatches.put(matchKey, true);
                updateCache();
            }
            if (progressDialog != null) {
                progressDialog.increaseProgressValue();
            }
            if (progressDialog.isRunCanceled()) {
                break;
            }
        }
        if (progressDialog != null) {
            progressDialog.setIndeterminate(true);
        }
        Util.deleteDir(directory);
        setDirectory(newDirectory);
    }
}
