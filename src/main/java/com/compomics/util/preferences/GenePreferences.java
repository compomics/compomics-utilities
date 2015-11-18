package com.compomics.util.preferences;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains methods for downloading gene and GO mappings.
 *
 * @author Harald Barsnes
 */
public class GenePreferences implements Serializable {

    /**
     * The serial number for serialization compatibility.
     */
    static final long serialVersionUID = -1286840382594446279L;
    
    /**
     * If true the gene mappings will auto update.
     */
    private Boolean autoUpdate;
    /**
     * If true the gene mappings will be used.
     */
    private Boolean useGeneMapping;

    /**
     * Create a new GenePreferences object.
     */
    public GenePreferences() {

    }

    /**
     * Creates new gene preferences based on a GenePreferences object.
     *
     * @param genePreferences the gene preferences
     */
    public GenePreferences(GenePreferences genePreferences) {

    }

    /**
     * Returns a boolean indicating whether gene mappings should be used.
     * 
     * @return a boolean indicating whether gene mappings should be used
     */
    public Boolean getUseGeneMapping() {
        return useGeneMapping;
    }

    /**
     * Sets whether gene mappings should be used.
     * 
     * @param useGeneMapping a boolean indicating whether gene mappings should be used
     */
    public void setUseGeneMapping(Boolean useGeneMapping) {
        this.useGeneMapping = useGeneMapping;
    }

    /**
     * Indicates whether the gene mappings should be automatically updated.
     * 
     * @return a boolean indicating whether the gene mappings should be automatically updated
     */
    public Boolean getAutoUpdate() {
        if (autoUpdate == null) {
            autoUpdate = true;
        }
        return autoUpdate;
    }

    /**
     * Sets whether the gene mappings should be automatically updated.
     * 
     * @param autoUpdate a boolean indicating whether the gene mappings should be automatically updated
     */
    public void setAutoUpdate(Boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }
    
    /**
     * Compares these preferences to other preferences.
     * 
     * @param genePreferences other preferences to compare to.
     * 
     * @return a boolean indicating whether the other preferences are the same as these ones.
     */
    public boolean equals(GenePreferences genePreferences) {
        return getAutoUpdate().equals(genePreferences.getAutoUpdate());
    }
    
    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        
        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        if (currentSpecies != null) {
            output.append("Species: ").append(currentSpecies).append(".").append(newLine);
        } else {
            output.append("Species: ").append("(not selected)").append(newLine);
        }

        return output.toString();
    }
    
    

    //////////////////////////////////
    // Deprecated code, kept for backward compatibility with parameters files of utilities version older than 4.2.0.
    //////////////////////////////////
    /**
     * The folder where gene mapping related info is stored.
     *
     * @deprecated use the gene factory.
     */
    private static String GENE_MAPPING_FOLDER = System.getProperty("user.home") + "/.compomics/gene_mappings/";
    /**
     * The suffix to use for files containing gene mappings.
     *
     * @deprecated use the gene factory.
     */
    public final static String GENE_MAPPING_FILE_SUFFIX = "_gene_mappings";
    /**
     * The suffix to use for files containing GO mappings.
     *
     * @deprecated use the gene factory.
     */
    public final static String GO_MAPPING_FILE_SUFFIX = "_go_mappings";
    /**
     * The current species. Used for the gene mappings.
     *
     * @deprecated use the species in the database.
     */
    private String currentSpecies = null;
    /**
     * The current species type. Used for the gene mappings.
     *
     * @deprecated use gene factory map.
     */
    private String currentSpeciesType = null;
    /**
     * The GO domain map. e.g., key: GO term: GO:0007568, element:
     * biological_process.
     *
     * @deprecated use gene factory map.
     */
    private HashMap<String, String> goDomainMap;
    /**
     * The species map. Main key: Ensembl type, e.g., Vertebrates or Plants.
     * Next level: key: latin name, element: ensembl database name, e.g., key:
     * Homo sapiens, element: hsapiens_gene_ensembl.
     *
     * @deprecated use gene factory map.
     */
    private HashMap<String, HashMap<String, String>> allSpeciesMap;
    /**
     * Map of all the species.
     *
     * @deprecated use gene factory map.
     */
    private HashMap<String, ArrayList<String>> allSpecies;
}
