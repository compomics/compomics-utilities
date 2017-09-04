package com.compomics.util.experiment.io.biology.protein;

import java.util.HashMap;

/**
 * This class parses a fasta file and gathers summary statistics.
 *
 * @author Marc Vaudel
 */
public class FastaSummary {
    
    /**
     * The species occurrence in the database.
     */
    private final HashMap<String, Integer> speciesOccurrence;
    /**
     * The occurrence of every amino acid letter in the database, including combinations, in per mille.
     */
    private final int[] aaOccurrence;
    
    /**
     * Constructor.
     * 
     * @param speciesOccurrence the occurrence of every species
     * @param aaOccurrence the occurrence of every amino acid
     */
    public FastaSummary(HashMap<String, Integer> speciesOccurrence, int[] aaOccurrence) {
        
        this.speciesOccurrence = speciesOccurrence;
        this.aaOccurrence = aaOccurrence;
        
    }

    /**
     * Returns the occurrence of every species.
     * 
     * @return the occurrence of every species
     */
    public HashMap<String, Integer> getSpeciesOccurrence() {
        return speciesOccurrence;
    }

    /**
     * Returns the occurrence of every amino acid letter in the database, including combinations, in per mille.
     * 
     * @return the occurrence of every amino acid letter in the database
     */
    public int[] getAaOccurrence() {
        return aaOccurrence;
    }
    
    
}
