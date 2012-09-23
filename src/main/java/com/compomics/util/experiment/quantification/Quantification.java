package com.compomics.util.experiment.quantification;

import com.compomics.util.db.ObjectsCache;
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
            return quantificationDB.getSpectrumMatch(spectrumKey);
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
            return quantificationDB.getPeptideMatch(peptideKey);
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
            return quantificationDB.getProteinMatch(proteinKey);
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
        quantificationDB.addSpectrumMatch(match);
    }

    /**
     * Add a peptide quantification match to the peptide quantification matches
     * map if not already implemented.
     *
     * @param match the new spectrum match
     * @throws Exception
     */
    public void addPeptideQuantification(PeptideQuantification match) throws Exception {
            peptideQuantification.add(match.getKey());
            quantificationDB.addPeptideMatch(match);
    }

    /**
     * Add a protein quantification match to the peptide quantification matches
     * map if not already implemented.
     *
     * @param match the new spectrum match
     * @throws Exception
     */
    public void addProteinQuantification(ProteinQuantification match) throws Exception {
            proteinQuantification.add(match.getKey());
            quantificationDB.addProteinMatch(match);
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
                    + identification.getSpectrumIdentificationSize());
        }

        ProteinQuantification tempProteinQuantification;
identification.loadProteinMatches(null);
        for (String proteinKey : identification.getProteinIdentification()) {
            ProteinMatch proteinMatch = identification.getProteinMatch(proteinKey);
            if (proteinMatch == null) {
                throw new IllegalArgumentException("Protein match " + proteinKey + " not found.");
            }
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
        identification.loadPeptideMatches(null);
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
        for (String fileName : identification.getSpectrumFiles()) {
        for (String psmKey : identification.getSpectrumIdentification(fileName)) {
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
 try {
        quantificationDB.updateMatch(match);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error while writing match " + match.getKey() + " in the database.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error while writing match " + match.getKey() + " in the database.");
            }
    }

    /**
     * Establishes connection to the database.
     *
     * @param dbFolder the absolute path to the folder where the database is
     * located
     * @param deleteOldDatabase if true, tries to delete the old database
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection
     */
    public void establishConnection(String dbFolder, String name, boolean deleteOldDatabase, ObjectsCache objectsCache) throws SQLException {
        quantificationDB = new QuantificationDB(dbFolder, name, deleteOldDatabase, objectsCache);
    }
}
