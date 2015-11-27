package com.compomics.util.experiment.biology.genes.go;

import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class parsing BioMart protein go mappings and storing them in maps.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class GoMapping {

    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\t";
    /**
     * Protein accession to go terms map.
     */
    private HashMap<String, HashSet<String>> proteinToGoMap;
    /**
     * Go terms to Protein accession map.
     */
    private HashMap<String, HashSet<String>> goToProteinMap;
    /**
     * Go term accession to name map.
     */
    private HashMap<String, String> goAccessionsToNamesMap;
    /**
     * Go term name to accession map.
     */
    private HashMap<String, String> goNamesToAccessionsMap;
    /**
     * A sorted list of GO terms names.
     */
    private ArrayList<String> sortedTermNames;

    /**
     * Constructor.
     */
    public GoMapping() {
        proteinToGoMap = new HashMap<String, HashSet<String>>();
        goToProteinMap = new HashMap<String, HashSet<String>>();
        goAccessionsToNamesMap = new HashMap<String, String>();
        goNamesToAccessionsMap = new HashMap<String, String>();
    }

    /**
     * Reads go mappings from a BioMart file. The structure of the file should
     * be protein accession go accession go name.
     *
     * Previous mappings are silently overwritten.
     *
     * @param file the file containing the GO mapping
     * @param waitingHandler a waiting handler allowing canceling of the
     * process.
     *
     * @throws IOException if an exception occurs while reading the file
     */
    public void loadMappingsFromFile(File file, WaitingHandler waitingHandler) throws IOException {

        sortedTermNames = null;

        // read the species list
        FileReader r = new FileReader(file);

        try {
            BufferedReader br = new BufferedReader(r);

            try {
                String line;

                while ((line = br.readLine()) != null) {

                    String[] splittedLine = line.split(SEPARATOR);

                    if (splittedLine.length == 3 && !splittedLine[0].equals("") && !splittedLine[1].equals("")) {

                        String proteinAccession = splittedLine[0];
                        String goTermAccession = splittedLine[1];
                        String goTermName = splittedLine[2].toLowerCase();

                        HashSet<String> goTerms = proteinToGoMap.get(proteinAccession);
                        if (goTerms == null) {
                            goTerms = new HashSet<String>();
                            proteinToGoMap.put(proteinAccession, goTerms);
                        }
                        goTerms.add(goTermAccession);

                        HashSet<String> proteinAccessions = goToProteinMap.get(goTermAccession);
                        if (proteinAccessions == null) {
                            proteinAccessions = new HashSet<String>();
                            goToProteinMap.put(goTermAccession, proteinAccessions);
                        }
                        proteinAccessions.add(proteinAccession);

                        goAccessionsToNamesMap.put(goTermAccession, goTermName);
                        goNamesToAccessionsMap.put(goTermName, goTermAccession);
                    }

                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        return;
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
     * Returns the GO accessions linked to a given protein accession. Null if
     * not found.
     *
     * @param proteinAccession the accession of the protein of interest
     *
     * @return a list of GO accession numbers, an empty list if no mapping is
     * found
     */
    public HashSet<String> getGoAccessions(String proteinAccession) {
        return proteinToGoMap.get(proteinAccession);
    }

    /**
     * Returns the protein accessions linked to a given GO term. Null if not
     * found.
     *
     * @param goTermAccession the accession of the GO term
     *
     * @return a list of GO accession numbers, an empty list if no mapping is
     * found
     */
    public HashSet<String> getProteinAccessions(String goTermAccession) {
        return goToProteinMap.get(goTermAccession);
    }

    /**
     * Returns the name of a GO term.
     *
     * @param goAccession the accession number of the GO term of interest
     *
     * @return the name, null if not found
     */
    public String getTermName(String goAccession) {
        return goAccessionsToNamesMap.get(goAccession);
    }

    /**
     * Returns the accession of a GO term.
     *
     * @param goName the name of the GO term of interest
     *
     * @return the accession, null if not found
     */
    public String getTermAccession(String goName) {
        return goNamesToAccessionsMap.get(goName);
    }

    /**
     * Returns the GO accession to name map.
     *
     * @return the GO accession to name map
     */
    public HashMap<String, String> getGoNamesMap() {
        return goAccessionsToNamesMap;
    }

    /**
     * Returns the protein to GO accession map.
     *
     * @return the protein to GO accession map
     */
    public HashMap<String, HashSet<String>> getProteinToGoMap() {
        return proteinToGoMap;
    }

    /**
     * Returns the GO to protein accession map.
     *
     * @return the GO to protein accession map
     */
    public HashMap<String, HashSet<String>> getGoToProteinMap() {
        return goToProteinMap;
    }

    /**
     * Returns a sorted list of all GO Terms names linked to proteins in the
     * proteinToGoMap.
     *
     * @return a sorted list of all GO Terms names
     */
    public ArrayList<String> getSortedTermNames() {
        if (sortedTermNames == null) {
            HashSet<String> goNames = new HashSet<String>(goAccessionsToNamesMap.size());
            for (HashSet<String> goAccessions : proteinToGoMap.values()) {
                for (String goAccession : goAccessions) {
                    String goName = getTermName(goAccession);
                    if (goName != null) {
                        goNames.add(goName);
                    }
                }
            }
            sortedTermNames = new ArrayList<String>(goNames);
            Collections.sort(sortedTermNames);
        }
        return sortedTermNames;
    }
}
