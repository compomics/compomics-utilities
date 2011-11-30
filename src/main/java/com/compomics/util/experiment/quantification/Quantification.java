package com.compomics.util.experiment.quantification;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.quantification.reporterion.quantification.ProteinQuantification;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.experiment.quantification.reporterion.QuantificationMatch;
import com.compomics.util.experiment.quantification.reporterion.quantification.PeptideQuantification;
import com.compomics.util.experiment.quantification.reporterion.quantification.PsmQuantification;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import java.util.HashMap;

/**
 * This class contains quantification results.
 * User: Marc
 * Date: Nov 11, 2010
 * Time: 3:46:24 PM
 */
public abstract class Quantification extends ExperimentObject {

    /**
     * The quantification method used
     */
    protected int methodUsed;
    /**
     * Extention for a serialized hit. cuh for Compomics Utilities Hit.
     */
    public static final String EXTENTION = ".cuq";
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
     * Map of the loaded quantification matches
     */
    protected HashMap<String, Object> loadedMatchesMap = new HashMap<String, Object>();
    /**
     * List of the loaded quantification matches with the most used matches in the end
     */
    protected ArrayList<String> loadedMatches = new ArrayList<String>();
    /**
     * Map indicating whether a quantification match is modified. Only modified matches will be serialized.
     */
    protected HashMap<String, Boolean> modifiedMatches = new HashMap<String, Boolean>();
    /**
     * Map of the user's parameters.
     */
    protected HashMap<String, HashMap<String, UrParameter>> urParameters = new HashMap<String, HashMap<String, UrParameter>>();
    /**
     * Map of long keys which will be referenced by their index for file creation
     */
    protected ArrayList<String> longKeys = new ArrayList<String>();
    /**
     * The protein quantification
     */
    protected ArrayList<String> proteinQuantification = new ArrayList<String>();
    /**
     * The peptide quantification
     */
    protected ArrayList<String> peptideQuantification = new ArrayList<String>();
    /**
     * A convenience map indicating all psm quantifying an identified PSM
     */
    protected HashMap<String, ArrayList<String>> psmIDentificationToQuantification = new HashMap<String, ArrayList<String>>();

    /**
     * This method retrieves the quantification result at the protein level
     * @return quantification at the protein level
     */
    public ArrayList<String> getProteinQuantification() {
        return proteinQuantification;
    }

    /**
     * This method retrieves the quantification result at the protein level
     * @return quantification at the protein level
     */
    public ArrayList<String> getPeptideQuantification() {
        return peptideQuantification;
    }

    /**
     * This method retrieves the quantification result at the protein level
     * @return quantification at the protein level
     */
    public HashMap<String, ArrayList<String>> getPsmIDentificationToQuantification() {
        return psmIDentificationToQuantification;
    }

    /**
     * getter for the method used
     * @return the method used
     */
    public int getMethodUsed() {
        return methodUsed;
    }

    /**
     * setter for the method used
     * @param methodUsed the method used
     */
    public void setMethodUsed(int methodUsed) {
        this.methodUsed = methodUsed;
    }

    /**
     * adds a parameter with a corresponding quantification key which will be loaded in the memory. Use this method only for frequently used parameters, otherwise attach the parameters to the matches.
     * @param key           the key of the parameter
     * @param urParameter   the additional parameter
     */
    public void addQuantificationParameter(String key, UrParameter urParameter) {
        if (!urParameters.containsKey(key)) {
            urParameters.put(key, new HashMap<String, UrParameter>());
        }
        urParameters.get(key).put(ExperimentObject.getParameterKey(urParameter), urParameter);
    }

    /**
     * Returns the personalization parameter of the given quantification
     * @param matchKey      the match key
     * @param urParameter   example of parameter to retrieve
     * @return              the personalization parameter
     */
    public UrParameter getQuantificationParameter(String matchKey, UrParameter urParameter) {
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
     * Removes a quantification from the model
     * @param quantificationKey the key of the match to remove
     */
    public void removeQuantification(String quantificationKey) {
        if (proteinQuantification.contains(quantificationKey)) {
            proteinQuantification.remove(quantificationKey);
        } else if (peptideQuantification.contains(quantificationKey)) {
            peptideQuantification.remove(quantificationKey);
        } else if (psmIDentificationToQuantification.keySet().contains(quantificationKey)) {
            psmIDentificationToQuantification.remove(quantificationKey);
        }
        if (loadedMatches.contains(quantificationKey)) {
            loadedMatches.remove(quantificationKey);
            loadedMatchesMap.remove(quantificationKey);
            modifiedMatches.remove(quantificationKey);
        } else {
            File match = new File(serializationDirectory, getFileName(quantificationKey));
            match.delete();
        }
    }

    /**
     * Returns a quantification match
     * @param matchKey      the key of the quantification match
     * @return              the desired quantification match
     * @throws Exception    exception thrown whenever an error occurred while retrieving the quantification match
     */
    private Object getQuantificationMatch(String quantificationKey) throws Exception {
        int index = loadedMatches.indexOf(quantificationKey);
        if (index == -1) {
            try {
                File newMatch = new File(serializationDirectory, getFileName(quantificationKey));
                FileInputStream fis = new FileInputStream(newMatch);
                ObjectInputStream in = new ObjectInputStream(fis);
                Object spectrumMatch = in.readObject();
                fis.close();
                in.close();
                loadedMatchesMap.put(quantificationKey, spectrumMatch);
                loadedMatches.add(quantificationKey);
                modifiedMatches.put(quantificationKey, false);
                updateCache();
                return spectrumMatch;
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Error while loading " + quantificationKey);
            }
        } else {
            if (index < 0.25 * loadedMatches.size()) {
                loadedMatches.remove(quantificationKey);
                loadedMatches.add(quantificationKey);
            }
            return loadedMatchesMap.get(quantificationKey);
        }
    }

    /**
     * Returns a list of PSM quantification matches corresponding to the given psm identification key.
     * 
     * @param identificationMatchKey   the key of the identification match
     * @return                         the desired matches
     * @throws Exception               exception thrown whenever an error occurred while retrieving the match
     */
    public ArrayList<PsmQuantification> getSpectrumMatches(String identificationMatchKey) throws Exception {
        ArrayList<PsmQuantification> result = new ArrayList<PsmQuantification>();
        for (String spectrumKey : psmIDentificationToQuantification.get(identificationMatchKey)) {
            result.add(getSpectrumMatch(spectrumKey));
        }
        return result;
    }

    /**
     * Returns a spectrum quantification match
     * @param spectrumKey   the key of the spectrum match
     * @return              the desired match
     * @throws Exception    exception thrown whenever an error occurred while retrieving the match
     */
    public PsmQuantification getSpectrumMatch(String spectrumKey) throws Exception {
        return (PsmQuantification) getQuantificationMatch(spectrumKey);
    }

    /**
     * Returns a peptide quantification match
     * @param peptideKey    the key of the match
     * @return              the desired match
     * @throws Exception    exception thrown whenever an error occurred while retrieving the match
     */
    public PeptideQuantification getPeptideMatch(String peptideKey) throws Exception {
        return (PeptideQuantification) getQuantificationMatch(peptideKey);
    }

    /**
     * Returns a protein quantification match
     * @param proteinKey    the key of the match
     * @return              the desired match
     * @throws Exception    exception thrown whenever an error occurred while retrieving the match
     */
    public ProteinQuantification getProteinMatch(String proteinKey) throws Exception {
        return (ProteinQuantification) getQuantificationMatch(proteinKey);
    }

    /**
     * Add a spectrum quantification to the spectrum quantification matches map and overwrites if already implemented.
     *
     * @param match the new spectrum match
     * @throws Exception  
     */
    public void addPsmQuantification(PsmQuantification match) throws Exception {
        String spectrumKey = match.getKey();
        String psmKey = match.getSpectrumMatchKey();
        if (!psmIDentificationToQuantification.containsKey(psmKey)) {
            psmIDentificationToQuantification.put(spectrumKey, new ArrayList<String>());
        }
        if (!psmIDentificationToQuantification.get(psmKey).contains(spectrumKey)) {
            psmIDentificationToQuantification.get(psmKey).add(spectrumKey);
        }
        loadedMatchesMap.put(spectrumKey, match);
        if (!loadedMatches.contains(spectrumKey)) {
            loadedMatches.add(spectrumKey);
        }
        modifiedMatches.put(spectrumKey, true);
        updateCache();
    }

    /**
     * Add a peptide quantification match to the peptide quantification matches map if not already implemented.
     *
     * @param match the new spectrum match
     * @throws Exception  
     */
    public void addPeptideQuantification(PeptideQuantification match) throws Exception {
        String peptideKey = match.getKey();
        if (!peptideQuantification.contains(peptideKey)) {
            peptideQuantification.add(peptideKey);
            loadedMatchesMap.put(peptideKey, match);
            loadedMatches.add(peptideKey);
            modifiedMatches.put(peptideKey, true);
            updateCache();
        }
    }

    /**
     * Add a protein quantification match to the peptide quantification matches map if not already implemented.
     *
     * @param match the new spectrum match
     * @throws Exception  
     */
    public void addProteinQuantification(ProteinQuantification match) throws Exception {
        String proteinKey = match.getKey();
        if (!proteinQuantification.contains(proteinKey)) {
            proteinQuantification.add(proteinKey);
            loadedMatchesMap.put(proteinKey, match);
            loadedMatches.add(proteinKey);
            modifiedMatches.put(proteinKey, true);
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
                        File matchFile = new File(serializationDirectory, getFileName(key));
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
     * Indicates whether the memory used by the application is lower than 99% of the heap size
     * @return a boolean indicating whether the memory used by the application is lower than 99% of the heap
     */
    public boolean memoryCheck() {
        return Runtime.getRuntime().totalMemory() < (long) (0.99 * Runtime.getRuntime().maxMemory());
    }

    /**
     * Creates the peptides and protein quantification instances based on the identification and the psm quantification. 
     * This operation will be extremely slow if the cache is already full.
     * 
     * @throws Exception 
     */
    public void buildPeptidesAndProteinQuantifications(Identification identification) throws Exception {
        ProteinMatch proteinMatch;
        ProteinQuantification proteinQuantification;
        for (String proteinKey : identification.getProteinIdentification()) {
            proteinMatch = identification.getProteinMatch(proteinKey);
            proteinQuantification = new ProteinQuantification(proteinKey, proteinMatch.getPeptideMatches());
            addProteinQuantification(proteinQuantification);
        }
        PeptideMatch peptideMatch;
        PeptideQuantification peptideQuantification;
        for (String peptideKey : identification.getPeptideIdentification()) {
            peptideMatch = identification.getPeptideMatch(peptideKey);
            peptideQuantification = new PeptideQuantification(peptideKey, peptideMatch.getSpectrumMatches());
            addPeptideQuantification(peptideQuantification);
        }
        for (String psmKey : identification.getSpectrumIdentification()) {
            if (!psmIDentificationToQuantification.containsKey(psmKey)) {
                psmIDentificationToQuantification.put(psmKey, new ArrayList<String>());
            }
        }
    }

    /**
     * Empties the cache and serializes everything in the specified serialization folder
     * 
     * @param progressDialog 
     * @throws Exception exception thrown whenever an error occurred while serializing a match
     */
    public void emptyCache(ProgressDialogX progressDialog) throws Exception {
        if (progressDialog != null) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(loadedMatchesMap.size());
        }
        int cpt = 0;
        for (String key : loadedMatchesMap.keySet()) {
            if (modifiedMatches.get(key)) {
                try {
                    File matchFile = new File(serializationDirectory, getFileName(key));
                    FileOutputStream fos = new FileOutputStream(matchFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(loadedMatchesMap.get(key));
                    oos.close();
                } catch (Exception e) {
                    throw new Exception("Error while writing match " + key);
                }
            }
            if (progressDialog != null) {
                progressDialog.setValue(++cpt);
            }
        }
        loadedMatches.clear();
        loadedMatchesMap.clear();
        modifiedMatches.clear();
    }

    /**
     * Indicates that a match was changed, it will thus be serialized again if needed.
     * 
     * @param match
     * @throws Exception  
     */
    public void setMatchChanged(QuantificationMatch match) throws Exception {
        String key = match.getKey();
        if (loadedMatches.contains(match.getKey())) {
            modifiedMatches.put(key, true);
        } else {
            try {
                File matchFile = new File(serializationDirectory, getFileName(key));
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
        ArrayList<String> keys = new ArrayList<String>();
        for (ArrayList<String> psmKeys : psmIDentificationToQuantification.values()) {
            keys.addAll(psmKeys);
        }
        keys.addAll(peptideQuantification);
        keys.addAll(proteinQuantification);
        if (progressDialog != null) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(keys.size());
        }
        int cpt = 0;
        for (String key : keys) {
            if (loadedMatches.contains(key)) {
                try {
                    File matchFile = new File(newPath, getFileName(key));
                    FileOutputStream fos = new FileOutputStream(matchFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(loadedMatchesMap.get(key));
                    oos.close();
                    modifiedMatches.put(key, false);
                } catch (Exception e) {
                    throw new Exception("Error while writing match " + key);
                }
            } else {
                File oldFile = new File(serializationDirectory, getFileName(key));
                File newFile = new File(newPath, getFileName(key));
                oldFile.renameTo(newFile);
            }
            if (progressDialog != null) {
                progressDialog.setValue(++cpt);
            }
        }
        serializationDirectory = newFolder.getPath();
    }

    /**
     * Returns the name of the file to use for serialization/deserialization
     * @param key   the key of the match
     * @return      the name of the corresponding file
     */
    public String getFileName(String key) {
        
        for (String fc : Identification.forbiddenCharacters) {
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
}
