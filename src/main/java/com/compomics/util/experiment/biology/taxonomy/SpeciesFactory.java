package com.compomics.util.experiment.biology.taxonomy;

import com.compomics.util.Util;
import static com.compomics.util.experiment.biology.genes.GeneFactory.getGeneMappingFolder;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Class related to the handling of species
 *
 * @author Marc Vaudel
 */
public class SpeciesFactory {

    /**
     * The instance of the factory.
     */
    private static SpeciesFactory instance = null;

    /**
     * Tag for unknown species.
     */
    public static final String unknown = "Unknown";
    /**
     * The subfolder relative to the jar file where gene mapping files are
     * stored in tools.
     */
    private final static String TOOL_GENE_MAPPING_SUBFOLDER = "resources/conf/gene_mappings/";
    /**
     * The name of the taxonomy file.
     */
    private static final String TAXONOMY_FILENAME = "taxonomy";

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
     * Returns the Uniprot species file.
     *
     * @param jarFilePath the path to the jar file
     * 
     * @return the Uniprot species file
     */
    public static File getSpeciesFile(String jarFilePath) {
        return new File(jarFilePath, TOOL_GENE_MAPPING_SUBFOLDER + TAXONOMY_FILENAME);
    }

}
