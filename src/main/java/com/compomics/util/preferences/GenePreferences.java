package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.genes.GeneFactory;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.protein_sequences.FastaIndex;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.httpclient.URIException;

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
     * The taxon of the species selected as background for the GO analysis.
     */
    private Integer selectedBackgroundSpecies;

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
        if (useGeneMapping == null) {
            useGeneMapping = true;
        }
        return useGeneMapping;
    }

    /**
     * Sets whether gene mappings should be used.
     *
     * @param useGeneMapping a boolean indicating whether gene mappings should
     * be used
     */
    public void setUseGeneMapping(Boolean useGeneMapping) {
        this.useGeneMapping = useGeneMapping;
    }

    /**
     * Indicates whether the gene mappings should be automatically updated.
     *
     * @return a boolean indicating whether the gene mappings should be
     * automatically updated
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
     * @param autoUpdate a boolean indicating whether the gene mappings should
     * be automatically updated
     */
    public void setAutoUpdate(Boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    /**
     * Compares these preferences to other preferences.
     *
     * @param genePreferences other preferences to compare to.
     *
     * @return a boolean indicating whether the other preferences are the same
     * as these ones.
     */
    public boolean equals(GenePreferences genePreferences) {
        return getAutoUpdate().equals(genePreferences.getAutoUpdate());
    }

    /**
     * Returns the taxon of the species selected as background species.
     *
     * @return the taxon of the species selected as background species
     */
    public Integer getSelectedBackgroundSpecies() {
        return selectedBackgroundSpecies;
    }

    /**
     * Sets the taxon of the species selected as background species.
     *
     * @param selectedBackgroundSpecies the taxon of the species selected as
     * background species
     */
    public void setSelectedBackgroundSpecies(Integer selectedBackgroundSpecies) {
        this.selectedBackgroundSpecies = selectedBackgroundSpecies;
    }

    /**
     * Sets the background species from the fasta file loaded in the sequence
     * factory.
     */
    public void setPeferencesFromSequenceFactory() {
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        SpeciesFactory speciesFactory = SpeciesFactory.getInstance();
        FastaIndex fastaIndex = sequenceFactory.getCurrentFastaIndex();
        HashMap<String, Integer> speciesOccurrence = fastaIndex.getSpecies();
        Integer occurrenceMax = null;

        // Select the background species based on occurrence in the factory
        for (String uniprotTaxonomy : speciesOccurrence.keySet()) {

            if (!uniprotTaxonomy.equals(SpeciesFactory.UNKNOWN)) {
                Integer occurrence = speciesOccurrence.get(uniprotTaxonomy);

                if (occurrenceMax == null || occurrence > occurrenceMax) {
                    occurrenceMax = occurrence;
                    try {
                        Integer taxon = speciesFactory.getUniprotTaxonomy().getId(uniprotTaxonomy, true);
                        if (taxon != null) {
                            setSelectedBackgroundSpecies(taxon);
                        }
                    } catch (Exception e) {
                        // Taxon not available, ignore
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {

        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        output.append("Use gene mappings: ").append(getUseGeneMapping()).append(".").append(newLine);
        output.append("Automaticall update gene mappings: ").append(getAutoUpdate()).append(".").append(newLine);
        if (selectedBackgroundSpecies != null) {
            SpeciesFactory speciesFactory = SpeciesFactory.getInstance();
            String speciesName = speciesFactory.getName(selectedBackgroundSpecies);
            output.append("Species: ").append(speciesName).append(".").append(newLine);
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
