package com.compomics.util.experiment.biology.taxonomy.mappings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class provides information about the species mapping in Ensembl.
 *
 * @author Marc Vaudel
 */
public class EnsemblSpecies {

    /**
     * The separator used to separate line contents.
     */
    public final static String separator = "\t";
    /**
     * Species Latin name to type map.
     */
    private HashMap<String, String> speciesToTypeMap;
    /**
     * Species type to Latin name map.
     */
    private HashMap<String, ArrayList<String>> typeToSpeciesMap;
    /**
     * Species Latin name to type map.
     */
    private HashMap<String, String> speciesToDatabaseMap;

    /**
     * Constructor.
     */
    public EnsemblSpecies() {

    }

    /**
     * Returns the species for the given type as Latin names.
     *
     * @param speciesType the Ensembl species type
     *
     * @return the list of species for the given type
     */
    public ArrayList<String> getSpecies(String speciesType) {
        return typeToSpeciesMap.get(speciesType);
    }

    /**
     * Returns the Ensembl species type for the given species.
     *
     * @param speciesName the Latin name of the species of interest
     *
     * @return the Ensembl species type
     */
    public String getSpeciesType(String speciesName) {
        return speciesToTypeMap.get(speciesName);
    }

    /**
     * Returns the database name corresponding to the given species.
     *
     * @param speciesName the Latin name of the species of interest
     *
     * @return the Ensembl database name
     */
    public String getDatabaseName(String speciesName) {
        return speciesToDatabaseMap.get(speciesName);
    }

    /**
     * Loads the species mapping from a file.
     *
     * @param speciesFile the species file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file.
     */
    public void loadMapping(File speciesFile) throws IOException {

        speciesToDatabaseMap = new HashMap<String, String>();
        speciesToTypeMap = new HashMap<String, String>();
        typeToSpeciesMap = new HashMap<String, ArrayList<String>>();
        
        // read the species list
        FileReader r = new FileReader(speciesFile);
        try {
            BufferedReader br = new BufferedReader(r);
            try {

                String line = br.readLine();
                String currentSpeciesType = line.substring(1);

                while ((line = br.readLine()) != null) {

                    line = line.trim();

                    if (line.length() > 0) {

                        if (line.startsWith(">")) {

                            currentSpeciesType = line.substring(1);

                        } else {

                            String[] elements = line.split(separator);
                            String currentSpeciesLatinName = elements[0].trim();
                            String currentEnsemblDatabaseName = elements[1].trim();

                            speciesToTypeMap.put(currentSpeciesLatinName, currentSpeciesType);
                            speciesToDatabaseMap.put(currentSpeciesLatinName, currentEnsemblDatabaseName);

                            ArrayList<String> speciesForType = typeToSpeciesMap.get(currentSpeciesType);
                            if (speciesForType == null) {
                                speciesForType = new ArrayList<String>();
                                typeToSpeciesMap.put(currentSpeciesType, speciesForType);
                            }
                            speciesForType.add(currentSpeciesLatinName);
                        }
                    }
                }

            } finally {
                br.close();
            }
        } finally {
            r.close();
        }
        
        // Make sure species are always listed in the same order
        for (ArrayList<String> speciesList : typeToSpeciesMap.values()) {
            Collections.sort(speciesList);
        }

    }

}
