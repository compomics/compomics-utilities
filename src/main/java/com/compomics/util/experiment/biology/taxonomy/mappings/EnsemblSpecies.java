package com.compomics.util.experiment.biology.taxonomy.mappings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Mapping of the Ensembl species.
 *
 * @author Marc Vaudel
 */
public class EnsemblSpecies {

    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\",\"";
    /**
     * NCBI ID to scientific name.
     */
    private HashMap<Integer, String> idToNameMap;
    /**
     * NCBI ID to common name.
     */
    private HashMap<Integer, String> idToCommonNameMap;
    /**
     * NCBI ID to Ensembl assembly.
     */
    private HashMap<Integer, String> idToAssemblyMap;

    /**
     * Constructor.
     */
    public EnsemblSpecies() {
        idToNameMap = new HashMap<Integer, String>();
        idToCommonNameMap = new HashMap<Integer, String>();
        idToAssemblyMap = new HashMap<Integer, String>();
    }

    /**
     * Loads the species mapping from a file. Previous mapping will be
     * overwritten.
     *
     * @param speciesFile the species file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file.
     */
    public void loadMapping(File speciesFile) throws IOException {

        // read the species list
        FileReader r = new FileReader(speciesFile);

        try {
            BufferedReader br = new BufferedReader(r);

            try {
                String line = br.readLine();

                while ((line = br.readLine()) != null) {

                    line = line.trim();

                    if (line.length() > 0) {

                        line = line.substring(1, line.length() - 1);
                        String[] elements = line.split(SEPARATOR);
                        String id = elements[2].trim();
                        String scientificName = elements[1].trim();
                        String commonName = elements[0].trim();
                        String assembly = elements[3].trim();

                        if (!id.equals("") && !id.equals("-")) {
                            Integer taxon = new Integer(id);
                            if (!scientificName.equals("-")) {
                                idToNameMap.put(taxon, scientificName);
                            }
                            if (!commonName.equals("-")) {
                                idToCommonNameMap.put(taxon, commonName);
                            }
                            if (!assembly.equals("-")) {
                                idToAssemblyMap.put(taxon, assembly);
                            }
                        }
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
     * Returns the scientific name corresponding to the given NCBI taxon.
     *
     * @param id the NCBI taxon
     *
     * @return the scientific name
     */
    public String getScientificName(Integer id) {
        return idToNameMap.get(id);
    }

    /**
     * Returns the common name corresponding to the given NCBI taxon.
     *
     * @param id the NCBI taxon
     *
     * @return the common name
     */
    public String getCommonName(Integer id) {
        return idToCommonNameMap.get(id);
    }

    /**
     * Returns the Ensembl assembly corresponding to the given NCBI taxon.
     *
     * @param id the NCBI taxon
     *
     * @return the Ensembl assembly
     */
    public String getAssembly(Integer id) {
        return idToAssemblyMap.get(id);
    }

    /**
     * Returns the taxons in this map.
     *
     * @return the taxons in this map
     */
    public HashSet<Integer> getTaxons() {
        return new HashSet<Integer>(idToAssemblyMap.keySet());
    }
}
