package com.compomics.util.experiment.io.biology.protein;

/**
 * Interface for a class able to provide metadata on proteins.
 *
 * @author Marc Vaudel
 */
public interface ProteinDetailsProvider {
    
    /**
     * Returns the description of the protein with the given accession.
     * 
     * @param accession the accession of the protein
     * 
     * @return the description of the protein with the given accession
     */
    public String getDescription(String accession);
    
    /**
     * Returns the name of the protein database.
     * 
     * @param accession the accession of the protein
     * 
     * @return the name of the protein database
     */
    public String getProteinDatabaseName(String accession);
    
    /**
     * Returns the gene name for the given protein.
     * 
     * @param accession the accession of the protein
     * 
     * @return the gene name for the given protein
     */
    public String getGeneName(String accession);

}
