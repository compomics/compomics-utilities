package com.compomics.util.experiment.biology.taxonomy.mappings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import org.apache.commons.httpclient.URIException;

/**
 * Mapping of the UniProt species taken from
 * https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/docs/speclist.txt.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class UniprotTaxonomy {

    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\t";
    /**
     * UniProt species name to NCBI ID.
     */
    private final HashMap<String, Integer> nameToIdMap;
    /**
     * NCBI ID to Latin name.
     */
    private final HashMap<Integer, String> idToNameMap;
    /**
     * NCBI ID to common name.
     */
    private final HashMap<Integer, String> idToCommonNameMap;
    /**
     * NCBI ID to synonym.
     */
    private final HashMap<String, String> nameToSynonymMap;

    /**
     * Constructor.
     */
    public UniprotTaxonomy() {
        nameToIdMap = new HashMap<>();
        idToNameMap = new HashMap<>();
        idToCommonNameMap = new HashMap<>();
        nameToSynonymMap = new HashMap<>();
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

                // forward to the taxon details
                while (line != null && !line.startsWith("_____")) {
                    line = br.readLine();
                }

                line = br.readLine();

                // now we expect a repeat of this format:
                // -----
                // ABANI E   72259: N=Abaeis nicippe
                //                  C=Sleepy orange butterfly
                //                  S=Eurema nicippe
                // -----
                // however, the C and S lines are not mandatory
                Integer taxon = null;
                String scientificName = null;

                while (line != null && !line.startsWith("==========")) {

                    if (line.lastIndexOf(": N=") != -1) {

                        String[] elements = line.split("\\: N=");

                        String[] codeAndTaxon = elements[0].trim().split("\\s+");
                        taxon = Integer.valueOf(codeAndTaxon[2]);
                        scientificName = elements[1];

                        nameToIdMap.put(scientificName, taxon);
                        idToNameMap.put(taxon, scientificName);

                    } else if (line.startsWith("                 C=")) {

                        String commonName = line.split("C=")[1].trim();

                        idToCommonNameMap.put(taxon, commonName);

                    } else if (line.startsWith("                 S=")) {

                        String synonym = line.split("S=")[1].trim();

                        nameToSynonymMap.put(scientificName, synonym);

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
     * Returns the NCBI taxon corresponding to the given species name. Null if
     * not found.
     *
     * @param name the species name
     *
     * @return the taxon
     *
     * @throws MalformedURLException exception thrown whenever the query URL is
     * malformed
     * @throws URIException exception thrown whenever an error occurred while
     * downloading the mapping
     * @throws IOException exception thrown whenever an error occurred while
     * downloading the mapping
     */
    public Integer getId(String name
    ) throws MalformedURLException, URIException, IOException {

        Integer result = nameToIdMap.get(name);
        return result;

    }

    /**
     * Returns the Latin name corresponding to the given NCBI taxon.
     *
     * @param id the NCBI taxon
     *
     * @return the Latin name
     */
    public String getLatinName(Integer id) {
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
     * Returns the synonym corresponding to the scientific name. E.g.
     * 'Ajellomyces capsulatus (strain H143)' returns 'Histoplasma capsulatum'.
     *
     * @param scientificName the scientific name
     *
     * @return the synonym
     */
    public String getSynonym(String scientificName) {
        return nameToSynonymMap.get(scientificName);
    }

}
