package com.compomics.util.experiment.biology.taxonomy.mappings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

/**
 * Mapping of the UniProt taxonomy.
 *
 * @author Marc Vaudel
 */
public class UniprotTaxonomy {

    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\t";
    /**
     * UniProt species name to NCBI ID.
     */
    private HashMap<String, Integer> nameToIdMap;
    /**
     * NCBI ID to Latin name.
     */
    private HashMap<Integer, String> idToNameMap;
    /**
     * NCBI ID to common name.
     */
    private HashMap<Integer, String> idToCommonNameMap;
    /**
     * The local file to store the mappings.
     */
    private File mappingFile;

    /**
     * Constructor.
     */
    public UniprotTaxonomy() {
        nameToIdMap = new HashMap<>();
        idToNameMap = new HashMap<>();
        idToCommonNameMap = new HashMap<>();
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

        this.mappingFile = speciesFile;

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
                        Integer id = new Integer(elements[0].trim());
                        String latinName = elements[2].trim();
                        String commonName = elements[3].trim();

                        nameToIdMap.put(latinName, id);
                        idToNameMap.put(id, latinName);
                        if (!commonName.equals("")) {
                            idToCommonNameMap.put(id, commonName);
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
     * Downloads the mapping for the given species name from UniProt and saves
     * it to the mapping file.
     *
     * @param name the name of the species to query
     *
     * @throws MalformedURLException exception thrown whenever the query URL is
     * malformed
     * @throws URIException exception thrown whenever an error occurred while
     * downloading the mapping
     * @throws IOException exception thrown whenever an error occurred while
     * downloading the mapping
     */
    public void downloadMapping(String name) throws MalformedURLException, URIException, IOException {

        String query = URIUtil.encodeQuery(name);

        URL url = new URL("http://www.uniprot.org/taxonomy/?sort=score&desc=&compress=no&query=" + query + "&format=tab&columns=id");

        URLConnection conn = url.openConnection();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())); BufferedWriter bw = new BufferedWriter(new FileWriter(mappingFile, true))) {

            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                
                line = line.trim();
                
                if (line.length() > 0) {
                    
                    String[] elements = line.split(SEPARATOR);
                    Integer id = new Integer(elements[0].trim());
                    String latinName = elements[2].trim();
                    String commonName = elements[3].trim();
                    
                    if (!idToNameMap.containsKey(id)) {
                        nameToIdMap.put(latinName, id);
                        idToNameMap.put(id, latinName);
                        if (!commonName.equals("")) {
                            idToCommonNameMap.put(id, commonName);
                        }
                        
                        // Try to save the new mapping
                        try {
                            bw.write(line);
                            bw.newLine();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the NCBI taxon corresponding to the given species name. Null if
     * not found.
     *
     * @param name the species name
     * @param query boolean indicating whether UniProt should be queried if the
     * species is not found
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
    public Integer getId(String name, boolean query) throws MalformedURLException, URIException, IOException {
        Integer result = nameToIdMap.get(name);
        if (result == null && query) {
            downloadMapping(name);
            result = nameToIdMap.get(name);
        }
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
     * Downloads the UniProt taxonomy mapping to the given file.
     *
     * @param destinationFile the file where to write the taxonomy file
     *
     * @throws IOException Exception thrown whenever an error occurred while
     * reading or writing data.
     */
    public static void downloadTaxonomyFile(File destinationFile) throws IOException {

        URL url = new URL("http://www.uniprot.org/taxonomy/?format=tab&columns=id");

        URLConnection conn = url.openConnection();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFile));

            try {
                String rowLine;
                while ((rowLine = br.readLine()) != null) {
                    bw.write(rowLine);
                    bw.newLine();
                }
            } finally {
                bw.close();
            }

        } finally {
            br.close();
        }
    }
}
