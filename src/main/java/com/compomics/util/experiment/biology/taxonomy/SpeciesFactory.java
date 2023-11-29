package com.compomics.util.experiment.biology.taxonomy;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.taxonomy.mappings.BiomartMapping;
import com.compomics.util.experiment.biology.taxonomy.mappings.EnsemblSpecies;
import com.compomics.util.experiment.biology.taxonomy.mappings.EnsemblSpecies.EnsemblDivision;
import com.compomics.util.experiment.biology.taxonomy.mappings.UniprotTaxonomy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class related to the handling of species.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SpeciesFactory {

    /**
     * The instance of the factory.
     */
    private static SpeciesFactory instance = null;
    /**
     * Tag for unknown species.
     */
    public static final String UNKNOWN = "Unknown";
    /**
     * The subfolder relative to the jar file where gene mapping files are
     * stored in tools.
     */
    private final static String TOOL_SPECIES_MAPPING_SUBFOLDER = "resources/conf/taxonomy/";
    /**
     * The name of the UniProt taxonomy file.
     */
    public static final String UNIPROT_TAXONOMY_FILENAME = "uniprot_species";
    /**
     * The names of the Ensembl species files.
     */
    public static final String ENSEMBL_SPECIES = "ensembl_all_species";
    /**
     * The name of the Ensembl BioMart datasets file.
     */
    public static final String BIOMART_ENSEMBL_FILENAME = "ensembl_biomart_databases";
    /**
     * The Ensembl species mapping.
     */
    private EnsemblSpecies ensemblSpecies;
    /**
     * The UniProt taxonomy.
     */
    private UniprotTaxonomy uniprotTaxonomy;
    /**
     * The BioMart mapping.
     */
    private BiomartMapping biomartMapping;

    /**
     * Static method returning the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static SpeciesFactory getInstance() {

        if (instance == null) {
            instance = new SpeciesFactory();
        }

        return instance;

    }

    /**
     * Constructor.
     */
    private SpeciesFactory() {
    }

    /**
     * Initiates the factory using the files of the static fields.
     *
     * @param configFolder the config folder
     *
     * @throws IOException Exception thrown whenever an error occurred while
     * reading a mapping file.
     */
    public void initiate(File configFolder) throws IOException {

        ensemblSpecies = new EnsemblSpecies();
        ensemblSpecies.loadMapping(getEnsemblSpeciesFile(configFolder));
        uniprotTaxonomy = new UniprotTaxonomy();
        uniprotTaxonomy.loadMapping(getUniprotTaxonomyFile(configFolder));
        biomartMapping = new BiomartMapping();
        biomartMapping.loadMapping(getBiomartEnsemblMappingFile(configFolder));

    }

    /**
     * Returns a listing of the species occurrence map provided.
     *
     * @param speciesOccurrence a map containing the occurrence of different
     * species
     *
     * @return a listing of the species occurrence map provided
     */
    public static String getSpeciesDescription(TreeMap<String, Integer> speciesOccurrence) {

        TreeMap<Integer, TreeSet<String>> occurrenceToSpecies = new TreeMap<>();
        double total = 0.0;

        for (Map.Entry<String, Integer> entry : speciesOccurrence.entrySet()) {

            String taxonomy = entry.getKey();
            Integer occurrence = entry.getValue();
            total += occurrence;
            TreeSet<String> species = occurrenceToSpecies.get(occurrence);

            if (species == null) {

                species = new TreeSet<>();
                occurrenceToSpecies.put(occurrence, species);

            }

            species.add(taxonomy);

        }

        StringBuilder description = new StringBuilder();

        for (Entry<Integer, TreeSet<String>> entry : occurrenceToSpecies.descendingMap().entrySet()) {

            int occurrence = entry.getKey();
            TreeSet<String> species = entry.getValue();

            for (String taxonomy : species) {

                double percentage = 100.0 * occurrence / total;

                if (description.length() > 0) {

                    description.append(", ");

                }

                description.append(taxonomy);

                if (speciesOccurrence.size() > 1) {

                    String occurrencePercentage;

                    if (percentage > 99.9) {

                        occurrencePercentage = ">99.9";

                    } else if (percentage < 0.1) {

                        occurrencePercentage = "<0.1";

                    } else {

                        double roundedDouble = Util.roundDouble(percentage, 1);
                        occurrencePercentage = Double.toString(roundedDouble);

                    }

                    description.append(" (")
                            .append(occurrence)
                            .append(", ")
                            .append(occurrencePercentage)
                            .append("%)");

                }
            }
        }

        return description.toString();

    }

    /**
     * Returns the Ensembl species file.
     *
     * @param configFolder the config folder
     *
     * @return the Ensembl species file
     */
    public static File getEnsemblSpeciesFile(File configFolder) {
        return new File(configFolder, TOOL_SPECIES_MAPPING_SUBFOLDER + ENSEMBL_SPECIES);
    }

    /**
     * Returns the UniProt taxonomy file.
     *
     * @param configFolder the config folder
     *
     * @return the UniProt taxonomy species file
     */
    public static File getUniprotTaxonomyFile(File configFolder) {
        return new File(configFolder, TOOL_SPECIES_MAPPING_SUBFOLDER + UNIPROT_TAXONOMY_FILENAME);
    }

    /**
     * Returns the Ensembl BioMart file.
     *
     * @param configFolder the config folder
     *
     * @return the Ensembl BioMart file
     */
    public static File getBiomartEnsemblMappingFile(File configFolder) {
        return new File(configFolder, TOOL_SPECIES_MAPPING_SUBFOLDER + BIOMART_ENSEMBL_FILENAME);
    }

    /**
     * Returns the Latin name of the species corresponding to the given taxon
     * according to the UniProt mapping. Null if not found.
     *
     * @param taxon the NCBI taxon ID
     *
     * @return the Latin name of the species
     */
    public String getLatinName(Integer taxon) {
        return uniprotTaxonomy.getLatinName(taxon);
    }

//    /**
//     * Returns the name of the species corresponding to the given taxon
//     * according to the UniProt mapping. Null if not found. For species mapping
//     * to plants in the Ensembl genome mapping, the name is Latin name (common
//     * name); common name (Latin Name) for the other species. If no common name
//     * is present the Latin name is used.
//     *
//     * @param taxon the NCBI taxon ID
//     *
//     * @return the Latin name of the species
//     */
//    public String getName(Integer taxon) {
//
//        if (uniprotTaxonomy == null || uniprotTaxonomy.getLatinName(taxon) == null) {
//            return null;
//        }
//
//        boolean plant = false;
//
//        EnsemblDivision division = ensemblSpecies.getDivision(taxon);
//
//        if (division != null && division == EnsemblDivision.plants) {
//            plant = true;
//        }
//
//        String latinName = uniprotTaxonomy.getLatinName(taxon);
//        String commonName = uniprotTaxonomy.getCommonName(taxon);
//        StringBuilder name = new StringBuilder();
//
//        if (plant) {
//
//            name.append(latinName);
//
//            if (commonName != null) {
//                name.append(" (").append(commonName).append(")");
//            }
//
//        } else {
//
//            if (commonName != null) {
//                name.append(commonName).append(" (");
//            }
//
//            name.append(latinName);
//
//            if (commonName != null) {
//                name.append(")");
//            }
//
//        }
//
//        return name.toString();
//
//    }
    /**
     * Returns the Ensembl assembly to use for the given Latin name.
     *
     * @param latinName the Latin name, e.g. 'homo_sapiens'.
     *
     * @return the Ensembl assembly to use
     */
    public String getEnsemblAssembly(String latinName) {
        return ensemblSpecies.getAssembly(latinName);
    }

    /**
     * Returns the Ensembl dataset to use for the given Latin name.
     *
     * @param latinName the Latin name, e.g. 'homo_sapiens'.
     *
     * @return the Ensembl dataset to use
     */
    public String getEnsemblDataset(String latinName) {

        String assembly = getEnsemblAssembly(latinName);

        if (assembly == null) {
            return null;
        }

        return biomartMapping.getDataset(assembly);

    }

    /**
     * Returns the Ensemble dataset name. E.g. 'hsapiens_gene_ensembl'.
     * 
     * @param latinName the Latin name
     * @param ensemblDivision the Ensembl division
     * @return the Ensemble dataset name
     */
    public String getEnsemblDatasetName(String latinName, EnsemblDivision ensemblDivision) {

        String ensemblDatasetName = "";
        String[] ensemblDatasetNameElements = latinName.split("_");

        for (int i = 0; i < ensemblDatasetNameElements.length - 1; i++) {
            ensemblDatasetName += ensemblDatasetNameElements[i].substring(0, 1);
        }

        ensemblDatasetName += ensemblDatasetNameElements[ensemblDatasetNameElements.length - 1];

        if (ensemblDivision == EnsemblDivision.vertebrates) {

            ensemblDatasetName += "_gene_ensembl";

        } else {

            ensemblDatasetName += "_eg_gene";

        }
        
        return ensemblDatasetName;

    }

    /**
     * Returns the Ensembl species mapping.
     *
     * @return the Ensembl species mapping
     */
    public EnsemblSpecies getEnsemblSpecies() {
        return ensemblSpecies;
    }

    /**
     * Returns the UniProt taxonomy mapping.
     *
     * @return the UniProt taxonomy mapping
     */
    public UniprotTaxonomy getUniprotTaxonomy() {
        return uniprotTaxonomy;
    }

    /**
     * Returns the BioMart mapping.
     *
     * @return the BioMart mapping
     */
    public BiomartMapping getBiomartMapping() {
        return biomartMapping;
    }

    /**
     * Returns a map of the species in Ensembl.
     *
     * @return a map of the species in Ensembl
     */
    public HashMap<String, HashSet<String>> getEnsembleSpecies() {

        HashMap<String, HashSet<String>> speciesMap = new HashMap<>(EnsemblDivision.values().length + 1);

        for (String tempLatinName : ensemblSpecies.getLatinNames()) {

            String divisionName = ensemblSpecies.getDivision(tempLatinName).ensemblType;
            HashSet<String> latinNames = speciesMap.get(divisionName);

            if (latinNames == null) {
                latinNames = new HashSet<>();
                speciesMap.put(divisionName, latinNames);
            }

            latinNames.add(tempLatinName);

        }

        //speciesMap.put("vertebrates", ensemblSpecies.getLatinNames());
        return speciesMap;

    }

}
