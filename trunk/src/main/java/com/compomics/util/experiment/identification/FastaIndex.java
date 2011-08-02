/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.HashMap;

/**
 * This class contains the index of a fasta file
 *
 * @author marc
 */
public class FastaIndex extends ExperimentObject {

    /**
     * The indexes of the inspected fasta file
     */
    private HashMap<String, Long> indexes;
    /**
     * The fasta file name
     */
    private String fileName;
    /**
     * boolean indicating whether the database contains decoy sequences
     */
    private boolean isDecoy;
    /**
     * number of target sequences found in the database
     */
    private int nTarget;

    /**
     * Constructor
     * @param indexes   The indexes of the inspected fasta file
     * @param fileName  The fasta file name
     */
    public FastaIndex(HashMap<String, Long> indexes, String fileName, boolean isDecoy, int nTarget) {
        this.indexes = indexes;
        this.fileName = fileName;
        this.isDecoy = isDecoy;
        this.nTarget = nTarget;
    }

    /**
     * Returns a map of all indexes of the fasta file (accession -> index)
     * @return a map of all indexes of the fasta file (accession -> index) 
     */
    public HashMap<String, Long> getIndexes() {
        return indexes;
    }

    /**
     * Returns the index of the accession of interest
     * @param accession the accession of interest
     * @return the index of the accession of interest
     */
    public Long getIndex(String accession) {
        return indexes.get(accession);
    }

    /**
     * Returns the file name of the inspected fasta file
     * @return the file name of the inspected fasta file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns a boolean indicating whether the database contains decoy sequences
     * @return a boolean indicating whether the database contains decoy sequences 
     */
    public boolean isDecoy() {
        return isDecoy;
    }

    /**
     * Returns the number of target sequences in the database
     * @return the number of target sequences in the database 
     */
    public int getNTarget() {
        return nTarget;
    }
}
