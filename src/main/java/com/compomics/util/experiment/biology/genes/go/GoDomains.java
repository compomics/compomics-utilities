package com.compomics.util.experiment.biology.genes.go;

import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Class parsing go domains and storing them in a map.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class GoDomains {

    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\t";
    /**
     * Go term accession to domain map.
     */
    private HashMap<String, String> goAccessionToDomainMap;

    /**
     * Constructor.
     */
    public GoDomains() {
        goAccessionToDomainMap = new HashMap<>();
    }

    /**
     * Reads go mappings from a file. The structure of the file should go
     * accession go name
     *
     * Previous mappings are silently overwritten.
     *
     * @param file the file containing the GO mapping
     *
     * @param waitingHandler a waiting handler allowing canceling of the
     * process.
     *
     * @throws IOException if an exception occurs while reading the file
     */
    public void laodMappingFromFile(File file, WaitingHandler waitingHandler) throws IOException {

        // read the species list
        FileReader r = new FileReader(file);

        try {

            BufferedReader br = new BufferedReader(r);

            try {
                String line;

                while ((line = br.readLine()) != null) {

                    String[] splittedLine = line.split(SEPARATOR);

                    if (splittedLine.length == 3 && !splittedLine[0].equals("") && !splittedLine[1].equals("")) {

                        String goTermId = splittedLine[0];
                        String goTermDomain = splittedLine[1].toLowerCase();
                        goAccessionToDomainMap.put(goTermId, goTermDomain);

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
     * Returns the description of a GO term.
     *
     * @param goAccession the accession number of the GO term of interest
     *
     * @return the first description found, null if not found
     */
    public String getTermDomain(String goAccession) {
        return goAccessionToDomainMap.get(goAccession);
    }

    /**
     * Adds a go domain to the mapping.
     *
     * @param goAccession the accession of the GO term
     * @param goDomain the domain of the GO term
     */
    public void addDomain(String goAccession, String goDomain) {
        goAccessionToDomainMap.put(goAccession, goDomain);
    }

    /**
     * Saves the mapping to the given file.
     *
     * @param destinationFile the destination file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     */
    public void saveMapping(File destinationFile) throws IOException {

        // save the GO domains
        FileWriter fw = new FileWriter(destinationFile, true);
        try {
            BufferedWriter bw = new BufferedWriter(fw);
            try {
                for (String goAccession : goAccessionToDomainMap.keySet()) {
                    String goDomain = goAccessionToDomainMap.get(goAccession);
                    bw.write(goAccession + SEPARATOR + goDomain);
                    bw.newLine();
                }
            } finally {
                bw.close();
            }
        } finally {
            fw.close();
        }
    }
}
