package com.compomics.util.experiment.biology.taxonomy.mappings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class provides information about the species mapping in Ensembl Genomes
 * (Bacteria, Fungi, Metazoa, Plants, Protists).
 *
 * @author Marc Vaudel
 */
public class EnsemblGenomesSpecies {

    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\t";
    /**
     * NCBI ID to name.
     */
    private final HashMap<Integer, String> idToNameMap;
    /**
     * NCBI ID to Ensembl division.
     */
    private final HashMap<Integer, String> idToDivisionMap;
    /**
     * NCBI ID to Ensembl assembly.
     */
    private final HashMap<Integer, String> idToAssemblyMap;

    /**
     * Enum of the different Ensembl genome divisions.
     */
    public static enum EnsemblGenomeDivision {

        bacteria("EnsemblBacteria", "bacteria"),
        fungi("EnsemblFungi", "fungi"),
        metazoa("EnsemblMetazoa", "metazoa"),
        plants("EnsemblPlants", "plants"),
        protists("EnsemblProtists", "protists");
        /**
         * The name in the Ensembl mapping file.
         */
        public final String ensemblName;
        /**
         * The schema name for XML queries.
         */
        public final String ensemblType;

        /**
         * Constructor.
         *
         * @param ensemblName the name in the Ensembl mapping file
         * @param ensemblType the Ensembl type for XML queries
         */
        private EnsemblGenomeDivision(String ensemblName, String ensemblType) {
            this.ensemblName = ensemblName;
            this.ensemblType = ensemblType;
        }

        /**
         * Returns the EnsemblGenomeDivision corresponding to the given Ensembl name. Null if not found.
         * 
         * @param ensemblName the Ensembl name
         * 
         * @return the EnsemblGenomeDivision
         */
        public static EnsemblGenomeDivision getEnsemblGenomeDivisionFromName(String ensemblName) {
            for (EnsemblGenomeDivision ensemblGenomeDivision : values()) {
                if (ensemblGenomeDivision.ensemblName.equals(ensemblName)) {
                    return ensemblGenomeDivision;
                }
            }
            return null;
        }
    }

    /**
     * Constructor.
     */
    public EnsemblGenomesSpecies() {
        idToNameMap = new HashMap<>();
        idToDivisionMap = new HashMap<>();
        idToAssemblyMap = new HashMap<>();
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
                        String[] elements = line.split(SEPARATOR);
                        Integer id = new Integer(elements[3].trim());
                        String name = elements[0].trim();
                        String division = elements[2].trim();
                        String assembly = elements[4].trim();

                        idToNameMap.put(id, name);
                        idToDivisionMap.put(id, division);
                        idToAssemblyMap.put(id, assembly);
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
     * Returns the name corresponding to the given NCBI taxon.
     *
     * @param taxon the NCBI taxon
     *
     * @return the name
     */
    public String getName(Integer taxon) {
        return idToNameMap.get(taxon);
    }

    /**
     * Returns the division corresponding to the given NCBI taxon.
     *
     * @param taxon the NCBI taxon
     *
     * @return the division
     */
    public EnsemblGenomeDivision getDivision(Integer taxon) {
        String ensemblDivisionName = idToDivisionMap.get(taxon);
        if (ensemblDivisionName == null) {
            return null;
        }
        return EnsemblGenomeDivision.getEnsemblGenomeDivisionFromName(ensemblDivisionName);
    }

    /**
     * Returns the Ensembl assembly corresponding to the given NCBI taxon.
     *
     * @param taxon the NCBI taxon
     *
     * @return the Ensembl assembly
     */
    public String getAssembly(Integer taxon) {
        return idToAssemblyMap.get(taxon);
    }
    
    /**
     * Returns the taxons in this map.
     * 
     * @return the taxons in this map
     */
    public HashSet<Integer> getTaxons() {
        return new HashSet<>(idToAssemblyMap.keySet());
    }
}
