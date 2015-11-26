package com.compomics.util.experiment.biology.taxonomy;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.taxonomy.mappings.BiomartMapping;
import com.compomics.util.experiment.biology.taxonomy.mappings.EnsemblGenomesSpecies;
import com.compomics.util.experiment.biology.taxonomy.mappings.EnsemblGenomesSpecies.EnsemblGenomeDivision;
import com.compomics.util.experiment.biology.taxonomy.mappings.EnsemblSpecies;
import com.compomics.util.experiment.biology.taxonomy.mappings.UniprotTaxonomy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

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
    public static final String UNIPROT_TAXONOMY_FILENAME = "uniprot_taxonomy";
    /**
     * The names of the Ensembl species files.
     */
    public static final String ENSEMBL_SPECIES = "ensembl_species";
    /**
     * The names of the Ensembl genome species files.
     */
    public static final String ENSEMBL_GENOME_SPECIES = "ensembl-genome_species";
    /**
     * The name of the Ensembl BioMart datasets file.
     */
    public static final String BIOMART_ENSEMBL_FILENAME = "ensembl_biomart";
    /**
     * The name of the Ensembl Genome BioMart datasets file.
     */
    public static final String BIOMART_ENSEMBL_GENOME_FILENAME = "ensembl-genome_biomart";
    /**
     * The Ensembl species mapping.
     */
    private EnsemblSpecies ensemblSpecies;
    /**
     * The Ensembl genome species mapping.
     */
    private EnsemblGenomesSpecies ensemblGenomesSpecies;
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
     * @param jarFilePath path to the jar file
     *
     * @throws IOException Exception thrown whenever an error occurred while
     * reading a mapping file.
     */
    public void initiate(String jarFilePath) throws IOException {
        ensemblSpecies = new EnsemblSpecies();
        ensemblSpecies.loadMapping(getEnsemblSpeciesFile(jarFilePath));
        ensemblGenomesSpecies = new EnsemblGenomesSpecies();
        ensemblGenomesSpecies.loadMapping(getEnsemblGenomesSpeciesFile(jarFilePath));
        uniprotTaxonomy = new UniprotTaxonomy();
        uniprotTaxonomy.loadMapping(getUniprotTaxonomyFile(jarFilePath));
        biomartMapping = new BiomartMapping();
        biomartMapping.loadMapping(getBiomartEnsemblMappingFile(jarFilePath), getBiomartEnsemblGenomeMappingFile(jarFilePath));
    }

    /**
     * Returns a listing of the species occurrence map provided.
     *
     * @param speciesOccurrence a map containing the occurrence of different
     * species
     *
     * @return a listing of the species occurrence map provided
     */
    public static String getSpeciesDescription(HashMap<String, Integer> speciesOccurrence) {

        HashMap<Integer, ArrayList<String>> occurrenceToSpecies = new HashMap<Integer, ArrayList<String>>(speciesOccurrence.size());
        double total = 0.0;
        for (String taxonomy : speciesOccurrence.keySet()) {
            Integer occurrence = speciesOccurrence.get(taxonomy);
            total += occurrence;
            ArrayList<String> species = occurrenceToSpecies.get(occurrence);
            if (species == null) {
                species = new ArrayList<String>(1);
                occurrenceToSpecies.put(occurrence, species);
            }
            species.add(taxonomy);
        }

        StringBuilder description = new StringBuilder();
        ArrayList<Integer> occurrences = new ArrayList<Integer>(occurrenceToSpecies.keySet());
        Collections.sort(occurrences, Collections.reverseOrder());
        for (Integer occurrence : occurrences) {
            ArrayList<String> species = occurrenceToSpecies.get(occurrence);
            Collections.sort(species);
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
                        occurrencePercentage = roundedDouble + "";
                    }
                    description.append(" (").append(occurrence).append(", ").append(occurrencePercentage).append("%)");
                }
            }
        }

        return description.toString();
    }

    /**
     * Returns the Ensembl species file.
     *
     * @param jarFilePath the path to the jar file
     *
     * @return the Ensembl species file
     */
    public static File getEnsemblSpeciesFile(String jarFilePath) {
        return new File(jarFilePath, TOOL_SPECIES_MAPPING_SUBFOLDER + ENSEMBL_SPECIES);
    }

    /**
     * Returns the Ensembl genome species file.
     *
     * @param jarFilePath the path to the jar file
     *
     * @return the Ensembl genome species file
     */
    public static File getEnsemblGenomesSpeciesFile(String jarFilePath) {
        return new File(jarFilePath, TOOL_SPECIES_MAPPING_SUBFOLDER + ENSEMBL_GENOME_SPECIES);
    }

    /**
     * Returns the UniProt taxonomy file.
     *
     * @param jarFilePath the path to the jar file
     *
     * @return the UniProt taxonomy species file
     */
    public static File getUniprotTaxonomyFile(String jarFilePath) {
        return new File(jarFilePath, TOOL_SPECIES_MAPPING_SUBFOLDER + UNIPROT_TAXONOMY_FILENAME);
    }

    /**
     * Returns the Ensembl BioMart file.
     *
     * @param jarFilePath the path to the jar file
     *
     * @return the Ensembl BioMart file
     */
    public static File getBiomartEnsemblMappingFile(String jarFilePath) {
        return new File(jarFilePath, TOOL_SPECIES_MAPPING_SUBFOLDER + BIOMART_ENSEMBL_FILENAME);
    }

    /**
     * Returns the Ensembl Genome BioMart file.
     *
     * @param jarFilePath the path to the jar file
     *
     * @return the Ensembl Genome BioMart file
     */
    public static File getBiomartEnsemblGenomeMappingFile(String jarFilePath) {
        return new File(jarFilePath, TOOL_SPECIES_MAPPING_SUBFOLDER + BIOMART_ENSEMBL_GENOME_FILENAME);
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

    /**
     * Returns the name of the species corresponding to the given taxon
     * according to the UniProt mapping. Null if not found. For species mapping
     * to plants in the Ensembl genome mapping, the name is Latin name (common
     * name); common name (Latin Name) for the other species. If no common name
     * is present the Latin name is used.
     *
     * @param taxon the NCBI taxon ID
     *
     * @return the Latin name of the species
     */
    public String getName(Integer taxon) {
        if (uniprotTaxonomy == null || uniprotTaxonomy.getLatinName(taxon) == null) {
            return null;
        }
        boolean plant = false;
        if (ensemblGenomesSpecies != null) {
            EnsemblGenomeDivision division = ensemblGenomesSpecies.getDivision(taxon);
            if (division != null && division == EnsemblGenomeDivision.plants) {
                plant = true;
            }
        }
        String latinName = uniprotTaxonomy.getLatinName(taxon);
        String commonName = uniprotTaxonomy.getCommonName(taxon);
        StringBuilder name = new StringBuilder();
        if (plant) {
            name.append(latinName);
            if (commonName != null) {
                name.append(" (").append(commonName).append(")");
            }
        } else {
            if (commonName != null) {
                name.append(commonName).append(" (");
            }
            name.append(latinName);
            if (commonName != null) {
                name.append(")");
            }
        }

        return name.toString();
    }

    /**
     * Returns the Ensembl assembly to use for the given taxon.
     *
     * @param taxon the taxon number
     *
     * @return the Ensembl assembly to use
     */
    public String getEnsemblAssembly(Integer taxon) {
        EnsemblGenomeDivision ensemblGenomeDivision = ensemblGenomesSpecies.getDivision(taxon);
        if (ensemblGenomeDivision == null) {
            return ensemblSpecies.getAssembly(taxon);
        } else {
            return ensemblGenomesSpecies.getAssembly(taxon);
        }
    }

    /**
     * Returns the Ensembl dataset to use for the given taxon.
     *
     * @param taxon the taxon number
     *
     * @return the Ensembl dataset to use
     */
    public String getEnsemblDataset(Integer taxon) {
        String assembly = getEnsemblAssembly(taxon);
        if (assembly == null) {
            return null;
        }
        return biomartMapping.getDataset(assembly);
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
     * Returns the Ensembl genome species mapping.
     *
     * @return the Ensembl genome species mapping
     */
    public EnsemblGenomesSpecies getEnsemblGenomesSpecies() {
        return ensemblGenomesSpecies;
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
    public HashMap<String, HashSet<Integer>> getEnsembleSpecies() {
        HashMap<String, HashSet<Integer>> speciesMap = new HashMap<String, HashSet<Integer>>(EnsemblGenomeDivision.values().length + 1);
        for (Integer taxon : ensemblGenomesSpecies.getTaxons()) {
            String divisionName = ensemblGenomesSpecies.getDivision(taxon).ensemblType;
            HashSet<Integer> taxons = speciesMap.get(divisionName);
            if (taxons == null) {
                taxons = new HashSet<Integer>();
                speciesMap.put(divisionName, taxons);
            }
            taxons.add(taxon);
        }
        speciesMap.put("vertebrates", ensemblSpecies.getTaxons());
        return speciesMap;
    }
}
