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
 * @author Harald Barsnes
 */
public class EnsemblSpecies {

    /////////////////////////////
    // How to update this file:
    //
    // library(readr)
    // vertibrates<-read.delim("https://ftp.ensembl.org/pub/release-110/species_EnsemblVertebrates.txt", sep="\t", row.names=NULL)
    // plants<-read.delim("http://ftp.ensemblgenomes.org/pub/plants/current/species_EnsemblPlants.txt", sep="\t", row.names=NULL)
    // fungi<-read.delim("http://ftp.ensemblgenomes.org/pub/fungi/current/species_EnsemblFungi.txt", sep="\t", row.names=NULL)
    // metazoa<-read.delim("http://ftp.ensemblgenomes.org/pub/metazoa/current/species_EnsemblMetazoa.txt", sep="\t", row.names=NULL)
    // protists<-read.delim("http://ftp.ensemblgenomes.org/pub/protists/current/species_EnsemblProtists.txt", sep="\t", row.names=NULL)
    // combined_table = do.call("rbind", list(vertibrates, plants, fungi, metazoa, protists))
    // write.table(combined_table, "[..]\\ensembl_all_species", sep ="\t", quote = F, col.names = T)
    /////////////////////////////
    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\t";
    /**
     * Latin species name to common name. E.g. 'homo_sapiens' to 'Human'.
     */
    private final HashMap<String, String> latinNameToCommonNameMap;
    /**
     * Latin species name to Ensembl division. E.g. 'homo_sapiens' to
     * 'EnsemblVertebrates'.
     */
    private final HashMap<String, String> latinNameToDivisionMap;
    /**
     * Latin species name to Ensembl assembly. E.g. 'homo_sapiens' to
     * 'GRCh38.p14'.
     */
    private final HashMap<String, String> latinNameToAssemblyMap;
    /**
     * Latin species name to NCBI taxon ID. E.g. 'homo_sapiens' to '9606'.
     */
    private final HashMap<String, String> latinNameToTaxonMap;

    /**
     * Enum of the different Ensembl divisions.
     */
    public static enum EnsemblDivision {

        vertebrates("EnsemblVertebrates", "vertebrates"),
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
        private EnsemblDivision(String ensemblName, String ensemblType) {
            this.ensemblName = ensemblName;
            this.ensemblType = ensemblType;
        }

        /**
         * Returns the EnsemblDivision corresponding to the given Ensembl name.
         * Null if not found.
         *
         * @param ensemblName the Ensembl name
         *
         * @return the EnsemblDivision
         */
        public static EnsemblDivision getEnsemblDivisionFromName(String ensemblName) {

            for (EnsemblDivision ensemblDivision : values()) {

                if (ensemblDivision.ensemblName.equals(ensemblName)) {
                    return ensemblDivision;
                }

            }

            return null;
        }

    }

    /**
     * Constructor.
     */
    public EnsemblSpecies() {

        latinNameToCommonNameMap = new HashMap<>();
        latinNameToDivisionMap = new HashMap<>();
        latinNameToAssemblyMap = new HashMap<>();
        latinNameToTaxonMap = new HashMap<>();

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

                // skip the header;
                br.readLine();

                String line = br.readLine();

                while (line != null) {

                    line = line.trim();

                    if (line.length() > 0) {

                        line = line.substring(1, line.length() - 1);

                        String[] elements = line.split(SEPARATOR);

                        String commonName = elements[1].trim();
                        String scientificName = elements[2].trim();
                        String division = elements[3].trim();
                        String taxon = elements[4].trim();
                        String assembly = elements[5].trim();

                        latinNameToCommonNameMap.put(scientificName, commonName);
                        latinNameToDivisionMap.put(scientificName, division);
                        latinNameToAssemblyMap.put(scientificName, assembly);
                        latinNameToTaxonMap.put(scientificName, taxon);

                    }

                    line = br.readLine();

                }

            } finally {
                br.close();
            }

        } finally {
            r.close();
        }

    }

    /**
     * Returns the common name corresponding to the given Latin name.
     *
     * @param latinName the Latin name, e.g. 'homo_sapiens'.
     *
     * @return the common name
     */
    public String getCommonName(String latinName) {
        return latinNameToCommonNameMap.get(latinName);
    }

    /**
     * Returns the division corresponding to the given Latin name.
     *
     * @param latinName the Latin name, e.g. 'homo_sapiens'.
     *
     * @return the Ensembl division
     */
    public EnsemblDivision getDivision(String latinName) {

        String ensemblDivisionName = latinNameToDivisionMap.get(latinName);

        if (ensemblDivisionName == null) {
            return null;
        }

        return EnsemblDivision.getEnsemblDivisionFromName(ensemblDivisionName);

    }

    /**
     * Returns the Ensembl assembly corresponding to the given Latin name.
     *
     * @param latinName the Latin name, e.g. 'homo_sapiens'.
     *
     * @return the Ensembl assembly
     */
    public String getAssembly(String latinName) {
        return latinNameToAssemblyMap.get(latinName);
    }

    /**
     * Returns the NCBI taxon corresponding to the given Latin name.
     *
     * @param latinName the Latin name, e.g. 'homo_sapiens'.
     *
     * @return the NCBI taxon
     */
    public String getTaxon(String latinName) {
        return latinNameToTaxonMap.get(latinName);
    }

    /**
     * Returns the Latin names in this map.
     *
     * @return the Latin names in this map
     */
    public HashSet<String> getLatinNames() {
        return new HashSet<>(latinNameToAssemblyMap.keySet());
    }

}
