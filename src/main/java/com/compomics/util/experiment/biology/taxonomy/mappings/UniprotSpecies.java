package com.compomics.util.experiment.biology.taxonomy.mappings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Mapping of the uniprot species to compomics utilities.
 *
 * @author Marc Vaudel
 */
public class UniprotSpecies {

    /**
     * The separator used to separate line contents.
     */
    public final static String separator = "\t";
    /**
     * Uniprot species name to Latin name.
     */
    private HashMap<String, String> speciesNameMap;

    /**
     * Constructor.
     */
    public UniprotSpecies() {
        speciesNameMap = new HashMap<String, String>();
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

        speciesNameMap = new HashMap<String, String>();

        // read the species list
        FileReader r = new FileReader(speciesFile);
        try {
            BufferedReader br = new BufferedReader(r);
            try {

                String line;

                while ((line = br.readLine()) != null) {

                    line = line.trim();

                    if (line.length() > 0) {

                        String[] elements = line.split(separator);
                        String currentUniprotName = elements[0].trim();
                        String currentUtilitiesNAme = elements[1].trim();

                        speciesNameMap.put(currentUniprotName, currentUtilitiesNAme);
                    }
                }

            } finally {
                br.close();
            }
        } finally {
            r.close();
        }

    }
    
    /**
     * Returns the utilities name for the Ensembl name of a species.
     * 
     * @param uniprotName the Uniprot name of the species
     * 
     * @return the utilities name of the species
     */
    public String getUtilitiesName(String uniprotName) {
        return speciesNameMap.get(uniprotName);
    }

}
