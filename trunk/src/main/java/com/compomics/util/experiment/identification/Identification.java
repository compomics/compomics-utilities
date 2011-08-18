package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * This class contains identification results.
 * User: Marc
 * Date: Nov 11, 2010
 * Time: 3:56:15 PM
 */
public abstract class Identification extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -2551700699384242554L;
    /**
     * Extention for a serialized hit. cuh for Compomics Utilities Hit.
     */
    public static final String EXTENTION = ".cuh";
    /**
     * List of the keys of all imported proteins
     */
    protected ArrayList<String> proteinIdentification = new ArrayList<String>();
    /**
     * List of the keys of all imported peptides
     */
    protected ArrayList<String> peptideIdentification = new ArrayList<String>();
    /**
     * List of the keys of all imported psms
     */
    protected ArrayList<String> spectrumIdentification = new ArrayList<String>();
    /**
     * a map linking protein accessions to all their protein matches keys
     */
    protected HashMap<String, ArrayList<String>> proteinMap = new HashMap<String, ArrayList<String>>();
    /**
     * The method used.
     */
    protected int methodUsed;
    /**
     * The cache size in number of matches. 20000 by default: should be enough to contain a velos file.
     */
    protected int cacheSize = 20000;
    /**
     * the directory where matches will be serialized
     */
    protected String serializationDirectory;
    /**
     * boolean indicating whether the identification should be stored in memory or not. 
     * True by default, the serialization directory should be set otherwise!
     */
    protected boolean inMemory = true;
    /**
     * boolean indicating whether the memory management should be done automatically. If true, the cache size will be extended to reach 90% of the available heap size when inMemory is wrong. True by default.
     */
    protected boolean automatedMemoryManagement = true;
    /**
     * Map of the loaded matches
     */
    protected HashMap<String, Object> loadedMatchesMap = new HashMap<String, Object>();
    /**
     * List of the loaded matches with the most used matches in the end
     */
    protected ArrayList<String> loadedMatches = new ArrayList<String>();
    /**
     * Map indicating whether a match is modified. Only modified matches will be serialized.
     */
    protected HashMap<String, Boolean> modifiedMatches = new HashMap<String, Boolean>();
    /**
     * Map of the user's parameters.
     */
    protected HashMap<String, HashMap<String, UrParameter>> urParameters = new HashMap<String, HashMap<String, UrParameter>>();

    /**
     * adds a parameter with a corresponding match key which will be loaded in the memory. Use this method only for frequently used parameters, otherwise attach the parameters to the matches.
     * @param key           the key of the parameter
     * @param urParameter   the additional parameter
     */
    public void addMatchParameter(String key, UrParameter urParameter) {
        if (!urParameters.containsKey(key)) {
            urParameters.put(key, new HashMap<String, UrParameter>());
        }
        urParameters.get(key).put(ExperimentObject.getParameterKey(urParameter), urParameter);
    }

    /**
     * Returns the personalization parameter of the given match
     * @param matchKey      the match key
     * @param urParameter   example of parameter to retrieve
     * @return              the personalization parameter
     */
    public UrParameter getMatchParameter(String matchKey, UrParameter urParameter) {
        return urParameters.get(matchKey).get(ExperimentObject.getParameterKey(urParameter));
    }

    /**
     * Returns whether the memory management is automated.
     * @return whether the memory management is automated.
     */
    public boolean isAutomatedMemoryManagement() {
        return automatedMemoryManagement;
    }

    /**
     * Sets whether the memory management should be automated
     * @param automatedMemoryManagement a boolean indicating whether the memory management should be automated
     */
    public void setAutomatedMemoryManagement(boolean automatedMemoryManagement) {
        this.automatedMemoryManagement = automatedMemoryManagement;
    }

    /**
     * Returns the cache size in number of matches
     * @return the cache size in number of matches
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Sets the cache size
     * @param cacheSize number of matches to allow in the cache size
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Indicates whether matches will be stored in memory
     * @return a boolean indicating whether matches will be stored in memory
     */
    public boolean isInMemory() {
        return inMemory;
    }

    /**
     * Sets whether matches shall be stored in memory
     * @param inMemory a boolean indicating whether matches shall be stored in memory
     */
    public void setInMemory(boolean inMemory) {
        this.inMemory = inMemory;
    }

    /**
     * Returns the serialization directory
     * @return the serialization directory
     */
    public String getSerializationDirectory() {
        return serializationDirectory;
    }

    /**
     * sets the serialization directory
     * @param serializationDirectory the path of the serialization directory
     */
    public void setSerializationDirectory(String serializationDirectory) {
        this.serializationDirectory = serializationDirectory;
    }

    /**
     * Removes a match from the model
     * @param matchKey the key of the match to remove
     */
    public void removeMatch(String matchKey) {
        if (proteinIdentification.contains(matchKey)) {
            proteinIdentification.remove(matchKey);
        } else if (peptideIdentification.contains(matchKey)) {
            peptideIdentification.remove(matchKey);
        } else if (spectrumIdentification.contains(matchKey)) {
            spectrumIdentification.remove(matchKey);
        }
        if (loadedMatches.contains(matchKey)) {
            loadedMatches.remove(matchKey);
            loadedMatchesMap.remove(matchKey);
            modifiedMatches.remove(matchKey);
        } else {
            File match = new File(serializationDirectory, matchKey + EXTENTION);
            match.delete();
        }
    }

    /**
     * Returns a match
     * @param matchKey      the key of the match
     * @return              the desired match
     * @throws Exception    exception thrown whenever an error occurred while retrieving the match
     */
    private Object getMatch(String matchKey) throws Exception {
        int index = loadedMatches.indexOf(matchKey);
        if (index == -1) {
            try {
                File newMatch = new File(serializationDirectory, matchKey + EXTENTION);
                FileInputStream fis = new FileInputStream(newMatch);
                ObjectInputStream in = new ObjectInputStream(fis);
                Object spectrumMatch = in.readObject();
                in.close();
                loadedMatchesMap.put(matchKey, spectrumMatch);
                loadedMatches.add(matchKey);
                modifiedMatches.put(matchKey, false);
                updateCache();
                return spectrumMatch;
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Error while loading " + matchKey);
            }
        } else {
            if (index < 0.25 * loadedMatches.size()) {
                loadedMatches.remove(matchKey);
                loadedMatches.add(matchKey);
            }
            return loadedMatchesMap.get(matchKey);
        }
    }

    /**
     * Returns a spectrum match
     * @param spectrumKey   the key of the match
     * @return              the desired match
     * @throws Exception    exception thrown whenever an error occurred while retrieving the match
     */
    public SpectrumMatch getSpectrumMatch(String spectrumKey) throws Exception {
        return (SpectrumMatch) getMatch(spectrumKey);
    }

    /**
     * Returns a peptide match
     * @param peptideKey    the key of the match
     * @return              the desired match
     * @throws Exception    exception thrown whenever an error occurred while retrieving the match
     */
    public PeptideMatch getPeptideMatch(String peptideKey) throws Exception {
        return (PeptideMatch) getMatch(peptideKey);
    }

    /**
     * Returns a protein match
     * @param proteinKey    the key of the match
     * @return              the desired match
     * @throws Exception    exception thrown whenever an error occurred while retrieving the match
     */
    public ProteinMatch getProteinMatch(String proteinKey) throws Exception {
        return (ProteinMatch) getMatch(proteinKey);
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
     * @return the corresponding identification resutls
     */
    public ArrayList<String> getSpectrumIdentification() {
        return spectrumIdentification;
    }

    /**
     * Add a spectrum match to the spectrum matches map.
     *
     * @param newMatch the new spectrum match
     */
    public void addSpectrumMatch(SpectrumMatch newMatch) throws Exception {
        String spectrumKey = newMatch.getKey();
        modifiedMatches.put(spectrumKey, true);
        if (spectrumIdentification.contains(spectrumKey)) {
            SpectrumMatch oldMatch = getSpectrumMatch(spectrumKey);
            for (int searchEngine : newMatch.getAdvocates()) {
                oldMatch.addHit(searchEngine, newMatch.getFirstHit(searchEngine));
            }
        } else {
            spectrumIdentification.add(spectrumKey);
            loadedMatchesMap.put(spectrumKey, newMatch);
            loadedMatches.add(spectrumKey);
            updateCache();
        }
    }

    /**
     * updates the cache according to the memory settings.
     * @throws Exception exception thrown whenever an error occurred while serializing a match
     */
    public void updateCache() throws Exception {
        if (!inMemory) {
            while (!automatedMemoryManagement && loadedMatches.size() > cacheSize
                    || !memoryCheck()) {
                String key = loadedMatches.get(0);
                if (modifiedMatches.get(key)) {
                    try {
                        File matchFile = new File(serializationDirectory, key + EXTENTION);
                        FileOutputStream fos = new FileOutputStream(matchFile);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(loadedMatchesMap.get(key));
                        oos.close();
                    } catch (Exception e) {
                        throw new Exception("Error while writing match " + key);
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
     * Indicates whether the memory used by the application is lower than 90% of the heap size
     * @return a boolean indicating whether the memory used by the application is lower than 90% of the heap
     */
    public boolean memoryCheck() {
        return Runtime.getRuntime().totalMemory() < (long) (0.9 * Runtime.getRuntime().maxMemory());
    }

    /**
     * Creates the peptides and protein instances based on the spectrum matches. Note that the attribute bestAssumption should be set for every spectrum match at this point. This operation will be very slow if the cache is already full.
     */
    public void buildPeptidesAndProteins() throws Exception {
        String peptideKey, proteinKey;
        Peptide peptide;
        SpectrumMatch spectrumMatch;
        PeptideMatch peptideMatch;
        ProteinMatch proteinMatch;
        for (String spectrumMatchKey : getSpectrumIdentification()) {
            spectrumMatch = getSpectrumMatch(spectrumMatchKey);
            peptide = spectrumMatch.getBestAssumption().getPeptide();
            peptideKey = peptide.getKey();
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
            proteinKey = ProteinMatch.getProteinMatchKey(peptide);
            if (proteinIdentification.contains(proteinKey)) {
                proteinMatch = getProteinMatch(proteinKey);
                if (!proteinMatch.getPeptideMatches().contains(peptideKey)) {
                    proteinMatch.addPeptideMatch(peptideKey);
                    setMatchChanged(proteinMatch);
                }
            } else {
                proteinMatch = new ProteinMatch(peptideMatch.getTheoreticPeptide());
                proteinIdentification.add(proteinKey);
                loadedMatches.add(proteinKey);
                loadedMatchesMap.put(proteinKey, proteinMatch);
                modifiedMatches.put(proteinKey, true);
                for (String protein : peptide.getParentProteins()) {
                    if (!proteinMap.containsKey(protein)) {
                        proteinMap.put(protein, new ArrayList<String>());
                    }
                    proteinMap.get(protein).add(proteinKey);
                }
            }
        }
    }

    /**
     * Empties the cache and serializes everything in the specified serialization folder
     * @throws Exception exception thrown whenever an error occurred while serializing a match
     */
    public void emptyCache() throws Exception {
        for (String key : loadedMatchesMap.keySet()) {
            if (modifiedMatches.get(key)) {
                try {
                    File matchFile = new File(serializationDirectory, key + EXTENTION);
                    FileOutputStream fos = new FileOutputStream(matchFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(loadedMatchesMap.get(key));
                    oos.close();
                } catch (Exception e) {
                    throw new Exception("Error while writing match " + key);
                }
            }
            loadedMatches.remove(key);
            loadedMatchesMap.remove(key);
            modifiedMatches.remove(key);
        }
    }

    /**
     * Add a set of spectrumMatches to the model
     *
     * @param spectrumMatches The spectrum matches
     * @throws Exception exception thrown when one tries to assign more than one identification per advocate to the same spectrum
     */
    public void addSpectrumMatch(Set<SpectrumMatch> spectrumMatches) throws Exception {
        for (SpectrumMatch spectrumMatch : spectrumMatches) {
            addSpectrumMatch(spectrumMatch);
        }
    }

    /**
     * Getter for the identification method used
     *
     * @return the identification method used
     */
    public int getMethodUsed() {
        return methodUsed;
    }

    /**
     * Returns a map of all the protein matches which can be ascribed to a protein indexed by its accession.
     * @return a map of all the protein matches which can be ascribed to a protein indexed by its accession.
     */
    public HashMap<String, ArrayList<String>> getProteinMap() {
        return proteinMap;
    }

    /**
     * Indicates that a match was changed, it will thus be serialized again if needed.
     */
    public void setMatchChanged(Match match) throws Exception {
        String key = match.getKey();
        if (loadedMatches.contains(match.getKey())) {
            modifiedMatches.put(key, true);
        } else {
            try {
                File matchFile = new File(serializationDirectory, key + EXTENTION);
                FileOutputStream fos = new FileOutputStream(matchFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(match);
                oos.close();
            } catch (Exception e) {
                throw new Exception("Error while writing match " + key);
            }
        }
    }

    /**
     * Saves the identification matches in the desired folder
     * 
     * @param newFolder         the new folder
     * @param progressDialog    a progress dialog to display the progress (can be null)
     * @throws Exception        Exception thrown whenever a problem occurred during the serialization process
     */
    public void save(File newFolder, ProgressDialogX progressDialog) throws Exception {
        String newPath = newFolder.getPath();
        ArrayList<String> keys = new ArrayList<String>(spectrumIdentification);
        keys.addAll(peptideIdentification);
        keys.addAll(proteinIdentification);
        if (progressDialog != null) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(proteinIdentification.size());
        }
        int cpt = 0;
        for (String key : keys) {
            if (loadedMatches.contains(key)) {
                try {
                    File matchFile = new File(serializationDirectory, key + EXTENTION);
                    FileOutputStream fos = new FileOutputStream(matchFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(loadedMatchesMap.get(key));
                    oos.close();
                } catch (Exception e) {
                    throw new Exception("Error while writing match " + key);
                }
            } else {
                File oldFile = new File(serializationDirectory, key + EXTENTION);
                File newFile = new File(newPath, key + EXTENTION);
                oldFile.renameTo(newFile);
            }
            cpt++;
            if (progressDialog != null) {
                progressDialog.setValue(cpt);
            }
        }
        serializationDirectory = newFolder.getPath();
        for (String key : modifiedMatches.keySet()) {
            modifiedMatches.put(key, false);
        }
    }
}
