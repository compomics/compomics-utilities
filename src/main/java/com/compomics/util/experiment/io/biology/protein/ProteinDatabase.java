package com.compomics.util.experiment.io.biology.protein;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

/**
 * Enum of the supported protein databases.
 *
 * @author Marc Vaudel
 */
public enum ProteinDatabase {

    UniProt("UniProtKB", "14681372"), 
    EnsemblGenomes("Ensembl Genomes", "26578574"), 
    SGD("Saccharomyces Genome Database (SGD)", "9399804"), 
    Arabidopsis_thaliana_TAIR("The Arabidopsis Information Resource (TAIR)", "12519987"),
    PSB_Arabidopsis_thaliana("PSB Arabidopsis thaliana", null), 
    Drosophile("Drosophile", null), 
    Flybase("Flybase", null), 
    NCBI("NCBI Reference Sequences (RefSeq)", "22121212"),
    M_Tuberculosis("TBDatabase (TBDB)", "18835847"), 
    H_Invitation("H_Invitation", null), 
    Halobacterium("Halobacterium", null), 
    H_Influenza("H_Influenza", null),
    C_Trachomatis("C_Trachomatis", null), 
    GenomeTranslation("Genome Translation", null), 
    Listeria("Listeria", null), 
    GAFFA("GAFFA", null),
    UPS("Universal Proteomic Standard (UPS)", null), 
    Generic_Header(null, null), 
    IPI("International Protein Index (IPI)", "15221759"), 
    Generic_Split_Header(null, null),
    NextProt("neXtProt", "22139911"), 
    UniRef("UniRef", null), 
    Unknown(null, null); // @TODO: add support for Ensembl headers?

    /**
     * The full name of the database.
     */
    public final String fullName;
    /**
     * The PubMed id of the database.
     */
    public final String pmid;

    /**
     * Constructor.
     *
     * @param fullName the full name
     * @param pmid the PubMed ID.
     */
    private ProteinDatabase(String fullName, String pmid) {
        this.fullName = fullName;
        this.pmid = pmid;
    }

    /**
     * Returns the full name of the database, null if not set.
     *
     * @return the full name of the database
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the PubMed id of the database, null if not set.
     *
     * @return the PubMed id of the database
     */
    public String getPmid() {
        return pmid;
    }
    
    /**
     * Returns the protein database with the given full name, null if not found.
     * 
     * @param fullName the database full name
     * 
     * @return the protein database
     */
    public ProteinDatabase getProteinDatabase(String fullName) {
        
        return Arrays.stream(values())
                .filter(database -> database.getFullName().equals(fullName))
                .findAny()
                .orElse(null);
    }
    
}
