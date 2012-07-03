package com.compomics.util.experiment.quantification;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.quantification.matches.ProteinQuantification;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.quantification.matches.PeptideQuantification;
import com.compomics.util.experiment.quantification.matches.PsmQuantification;
import com.compomics.util.gui.waiting.WaitingHandler;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.HashMap;

/**
 * This class contains quantification results. User: Marc Date: Nov 11, 2010
 * Time: 3:46:24 PM
 */
public abstract class Quantification extends ExperimentObject {

    /**
     * The implemented quantification methods. Well implemented, as soon as you
     * do it.
     */
    public enum QuantificationMethod {

        /**
         * relative quantification by comparison of peptide intensities
         * extracted from the MS1 map or XIC
         */
        MS1_LABEL_FREE,
        /**
         * Relative quantification by comparison of labeled versions of the
         * peptides in the same MS1 map (like SILAC)
         */
        MS1_LABEL,
        /**
         * Relative or absolute quantification by counting identified spectra
         * (like emPAI or NSAF)
         */
        SPECTRUM_COUNTING,
        /**
         * Relative quantification by comparison of reporter ion intensities
         */
        REPORTER_IONS
    }
    /**
     * The quantification method used
     */
    protected QuantificationMethod methodUsed;
    /**
     * The cache size in number of matches. 20000 by default: should be enough
     * to contain a velos file.
     */
    protected int cacheSize = 20000;
    /**
     * boolean indicating whether the identification should be stored in memory
     * or not. True by default, the serialization directory should be set
     * otherwise!
     */
    protected boolean inMemory = true;
    /**
     * boolean indicating whether the memory management should be done
     * automatically. If true, the cache size will be extended to reach 90% of
     * the available heap size when inMemory is wrong. True by default.
     */
    protected boolean automatedMemoryManagement = true;
    /**
     * Map of the loaded quantification matches
     */
    protected HashMap<String, QuantificationMatch> loadedMatchesMap = new HashMap<String, QuantificationMatch>();
    /**
     * List of the loaded quantification matches with the most used matches in
     * the end
     */
    protected ArrayList<String> loadedMatches = new ArrayList<String>();
    /**
     * Map indicating whether a quantification match is modified. Only modified
     * matches will be serialized.
     */
    protected HashMap<String, Boolean> modifiedMatches = new HashMap<String, Boolean>();
    /**
     * Map of long keys which will be referenced by their index for file
     * creation
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
     * The modified peptides quantification
     */
    protected HashMap<String, ArrayList<String>> modifiedPeptidesQuantification = new HashMap<String, ArrayList<String>>();
    /**
     * A convenience map indicating all psm quantifying an identified PSM
     */
    protected HashMap<String, ArrayList<String>> psmIDentificationToQuantification = new HashMap<String, ArrayList<String>>();

    /**
     * This method retrieves the quantification result at the protein level
     *
     * @return quantification at the protein level
     */
    public ArrayList<String> getProteinQuantification() {
        return proteinQuantification;
    }

    /**
     * This method retrieves the quantification result at the peptide level
     *
     * @return quantification at the protein level
     */
    public ArrayList<String> getPeptideQuantification() {
        return peptideQuantification;
    }

    /**
     * This method retrieves the quantification result at the modified peptides
     * level
     *
     * @param modificationName the name of the modification
     * @return quantification at the protein level
     */
    public ArrayList<String> getModifiedPeptideQuantification(String modificationName) {
        return modifiedPeptidesQuantification.get(modificationName);
    }

    /**
     * This method retrieves the quantification result at the spectrum level
     *
     * @return quantification at the protein level
     */
    public HashMap<String, ArrayList<String>> getPsmIDentificationToQuantification() {
        return psmIDentificationToQuantification;
    }

    /**
     * getter for the method used
     *
     * @return the method used
     */
    public QuantificationMethod getMethodUsed() {
        return methodUsed;
    }

    /**
     * setter for the method used
     *
     * @param methodUsed the method used
     */
    public void setMethodUsed(QuantificationMethod methodUsed) {
        this.methodUsed = methodUsed;
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
     * Sets whether the memory management should be automated
     *
     * @param automatedMemoryManagement a boolean indicating whether the memory
     * management should be automated
     */
    public void setAutomatedMemoryManagement(boolean automatedMemoryManagement) {
        this.automatedMemoryManagement = automatedMemoryManagement;
    }

    /**
     * Returns the cache size in number of matches
     *
     * @return the cache size in number of matches
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Sets the cache size
     *
     * @param cacheSize number of matches to allow in the cache size
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Indicates whether matches will be stored in memory
     *
     * @return a boolean indicating whether matches will be stored in memory
     */
    public boolean isInMemory() {
        return inMemory;
    }

    /**
     * Sets whether matches shall be stored in memory
     *
     * @param inMemory a boolean indicating whether matches shall be stored in
     * memory
     */
    public void setInMemory(boolean inMemory) {
        this.inMemory = inMemory;
    }
    /**
     * the quantificationDB object interacting with the database
     */
    private QuantificationDB quantificationDB;

    /**
     * Returns a list of PSM quantification matches corresponding to the given
     * psm identification key.
     *
     * @param identificationMatchKey the key of the identification match
     * @return the desired matches
     * @throws Exception exception thrown whenever an error occurred while
     * retrieving the match
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
     *
     * @param spectrumKey the key of the spectrum match
     * @return the desired match
     * @throws Exception exception thrown whenever an error occurred while
     * retrieving the match
     */
    public PsmQuantification getSpectrumMatch(String spectrumKey) throws Exception {
        int index = loadedMatches.indexOf(spectrumKey);
        if (index == -1) {
            PsmQuantification match = quantificationDB.getSpectrumMatch(spectrumKey);
            loadedMatchesMap.put(spectrumKey, match);
            loadedMatches.add(spectrumKey);
            modifiedMatches.put(spectrumKey, false);
            updateCache();
            return match;
        } else {
            if (index < 0.25 * loadedMatches.size()) {
                loadedMatches.remove(spectrumKey);
                loadedMatches.add(spectrumKey);
            }
            return (PsmQuantification) loadedMatchesMap.get(spectrumKey);
        }
    }

    /**
     * Returns a peptide quantification match
     *
     * @param peptideKey the key of the match
     * @return the desired match
     * @throws Exception exception thrown whenever an error occurred while
     * retrieving the match
     */
    public PeptideQuantification getPeptideMatch(String peptideKey) throws Exception {
        int index = loadedMatches.indexOf(peptideKey);
        if (index == -1) {
            PeptideQuantification match = quantificationDB.getPeptideMatch(peptideKey);
            loadedMatchesMap.put(peptideKey, match);
            loadedMatches.add(peptideKey);
            modifiedMatches.put(peptideKey, false);
            updateCache();
            return match;
        } else {
            if (index < 0.25 * loadedMatches.size()) {
                loadedMatches.remove(peptideKey);
                loadedMatches.add(peptideKey);
            }
            return (PeptideQuantification) loadedMatchesMap.get(peptideKey);
        }
    }

    /**
     * Returns a protein quantification match
     *
     * @param proteinKey the key of the match
     * @return the desired match
     * @throws Exception exception thrown whenever an error occurred while
     * retrieving the match
     */
    public ProteinQuantification getProteinMatch(String proteinKey) throws Exception {
        int index = loadedMatches.indexOf(proteinKey);
        if (index == -1) {
            ProteinQuantification match = quantificationDB.getProteinMatch(proteinKey);
            loadedMatchesMap.put(proteinKey, match);
            loadedMatches.add(proteinKey);
            modifiedMatches.put(proteinKey, false);
            updateCache();
            return match;
        } else {
            if (index < 0.25 * loadedMatches.size()) {
                loadedMatches.remove(proteinKey);
                loadedMatches.add(proteinKey);
            }
            return (ProteinQuantification) loadedMatchesMap.get(proteinKey);
        }
    }

    /**
     * Add a spectrum quantification to the spectrum quantification matches map
     * and overwrites if already implemented.
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
     * Add a peptide quantification match to the peptide quantification matches
     * map if not already implemented.
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
     * Add a protein quantification match to the peptide quantification matches
     * map if not already implemented.
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
     *
     * @throws Exception exception thrown whenever an error occurred while
     * serializing a match
     */
    public void updateCache() throws Exception {
        if (!inMemory) {
            while (!automatedMemoryManagement && loadedMatches.size() > cacheSize
                    || !memoryCheck()) {
                String key = loadedMatches.get(0);
                if (modifiedMatches.get(key)) {
                    try {
                        quantificationDB.addMatch(loadedMatchesMap.get(key));
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IOException("Error while writing match " + key + " in the database.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new SQLException("Error while writing match " + key + " in the database.");
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
     * the heap size
     *
     * @return a boolean indicating whether the memory used by the application
     * is lower than 99% of the heap
     */
    public boolean memoryCheck() {
        return Runtime.getRuntime().totalMemory() < (long) (0.99 * Runtime.getRuntime().maxMemory());
    }

    /**
     * Creates the peptides and protein quantification instances based on the
     * identification and the psm quantification. This operation will be
     * extremely slow if the cache is already full.
     *
     * @param identification
     * @param waitingHandler 
     * @throws Exception
     */
    public void buildPeptidesAndProteinQuantifications(Identification identification, WaitingHandler waitingHandler) throws Exception {

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressValue(identification.getProteinIdentification().size()
                    + identification.getPeptideIdentification().size()
                    + identification.getSpectrumIdentification().size());
        }

        ProteinQuantification tempProteinQuantification;

        for (String proteinKey : identification.getProteinIdentification()) {
            ProteinMatch proteinMatch = identification.getProteinMatch(proteinKey);
            tempProteinQuantification = new ProteinQuantification(proteinKey, proteinMatch.getPeptideMatches());
            addProteinQuantification(tempProteinQuantification);
            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
            }
        }

        PeptideQuantification tempPeptideQuantification;

        for (String peptideKey : identification.getPeptideIdentification()) {
            PeptideMatch peptideMatch = identification.getPeptideMatch(peptideKey);
            tempPeptideQuantification = new PeptideQuantification(peptideKey, peptideMatch.getSpectrumMatches());
            addPeptideQuantification(tempPeptideQuantification);
            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
            }
        }

        for (String psmKey : identification.getSpectrumIdentification()) {
            if (!psmIDentificationToQuantification.containsKey(psmKey)) {
                psmIDentificationToQuantification.put(psmKey, new ArrayList<String>());
            }
            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
            }
        }
    }

    /**
     * Empties the cache and saves everything in the database.
     *
     * @param progressDialog
     * @throws FileNotFoundException exception thrown whenever an error occurred
     * while serializing a match
     * @throws IOException exception thrown whenever an error occurred while
     * serializing a match
     * @throws SQLException  
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
                try {
                    quantificationDB.addMatch(loadedMatchesMap.get(key));
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IOException("Error while writing match " + key + " in the database.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new SQLException("Error while writing match " + key + " in the database.");
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
     * Indicates that a match was changed, it will thus be serialized again if
     * needed.
     *
     * @param match
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws SQLException  
     */
    public void setMatchChanged(QuantificationMatch match) throws IllegalArgumentException, IOException, SQLException {

        String key = match.getKey();

        if (loadedMatches.contains(key)) {
            modifiedMatches.put(key, true);
        } else {
            try {
                quantificationDB.updateMatch(match);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error while writing match " + key + " in the database.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error while writing match " + key + " in the database.");
            }
        }
    }

    /**
     * Establishes connection to the database.
     *
     * @param dbFolder the absolute path to the folder where the database is
     * located
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection
     */
    public void establishConnection(String dbFolder) throws SQLException {
        if (quantificationDB == null) {
            quantificationDB = new QuantificationDB(dbFolder);
        }
        quantificationDB.establishConnection(dbFolder);
    }
}
