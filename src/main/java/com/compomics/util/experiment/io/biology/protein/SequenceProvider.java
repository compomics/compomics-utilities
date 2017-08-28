package com.compomics.util.experiment.io.biology.protein;

/**
 * Interface for a class able to retrieve the sequence of a given protein.
 *
 * @author Marc Vaudel
 */
public interface SequenceProvider {
    
    
    /**
     * Returns the protein sequence for the given accession.
     * 
     * @param proteinAccession the accession of the protein
     * 
     * @return the sequence of the protein
     */
    public String getSequence(String proteinAccession);

}
