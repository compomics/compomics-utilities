package com.compomics.util.experiment.annotation.go;

import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * The GOFactory annotates identification results based on Ensembl Gene Ontology
 * terms.
 *
 * @author Marc Vaudel
 */
public class GOFactory {

    /**
     * The instance of the factory.
     */
    private static GOFactory instance = null;
    /**
     * Random access file of the selected file.
     */
    private BufferedRandomAccessFile bufferedRandomAccessFile = null;
    /**
     * The separator used to separate line contents.
     */
    public final static String separator = "\t";
    /**
     * Map of all the indexes where a protein can be found: accession ->
     * indexes.
     */
    private HashMap<String, ArrayList<Long>> proteinIndexes = new HashMap<String, ArrayList<Long>>();
    /**
     * Map of all the indexes where a GO accession number can be found: GO
     * accession number -> indexes.
     */
    private HashMap<String, ArrayList<Long>> termIndexes = new HashMap<String, ArrayList<Long>>();
    /**
     * Map of all the indexes where a GO term can be found: GO term name ->
     * indexes.
     */
    private HashMap<String, ArrayList<Long>> termNameIndexes = new HashMap<String, ArrayList<Long>>();
    /**
     * Boolean indicating if the mapping file is currently open.
     */
    private boolean mappingFileOpen = false;

    /**
     * Static method returning the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static GOFactory getInstance() {
        if (instance == null) {
            instance = new GOFactory();
        }
        return instance;
    }

    /**
     * Constructor.
     */
    private GOFactory() {
    }

    /**
     * Initializes the factory on the given file
     *
     * @param file the file containing the GO mapping
     * @param waitingHandler a waiting handler allowing display of the progress
     * and canceling of the process.
     * @throws IOException
     */
    public void initialize(File file, WaitingHandler waitingHandler) throws IOException {

        // remove the old data
        clearFactory();

        if (bufferedRandomAccessFile != null) {
            bufferedRandomAccessFile.close();
        }

        bufferedRandomAccessFile = new BufferedRandomAccessFile(file, "r", 1024 * 100);
        mappingFileOpen = true;

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        String line;
        long index = bufferedRandomAccessFile.getFilePointer();

        while ((line = bufferedRandomAccessFile.readLine()) != null) {

            String[] splittedLine = line.split(separator);

            if (splittedLine.length == 3 && !splittedLine[0].equals("") && !splittedLine[1].equals("")) {

                String accession = splittedLine[0];
                ArrayList<Long> indexes = proteinIndexes.get(accession);
                if (indexes == null) {
                    indexes = new ArrayList<Long>();
                    proteinIndexes.put(accession, indexes);
                }
                indexes.add(index);

                String goTermId = splittedLine[1];
                indexes = termIndexes.get(goTermId);
                if (indexes == null) {
                    indexes = new ArrayList<Long>();
                    termIndexes.put(goTermId, indexes);
                }
                indexes.add(index);

                String goTerm = splittedLine[2].toLowerCase();
                indexes = termNameIndexes.get(goTerm);
                if (indexes == null) {
                    indexes = new ArrayList<Long>();
                    termNameIndexes.put(goTerm, indexes);
                }
                indexes.add(index);
            }

            index = bufferedRandomAccessFile.getFilePointer();

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressCounter((int) (index / progressUnit));
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }
    }

    /**
     * Returns the GO accession numbers linked to a given protein accession
     * number.
     *
     * @param proteinAccession the accession number of the protein of interest
     * @return a list of GO accession numbers, an empty list if no mapping is
     * found
     * @throws IOException
     */
    public ArrayList<String> getGoAccessions(String proteinAccession) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<Long> indexes = proteinIndexes.get(proteinAccession);
        if (indexes != null) {
            for (long index : indexes) {
                bufferedRandomAccessFile.seek(index);
                String line = bufferedRandomAccessFile.getNextLine();
                String[] splittedLine = line.split(separator);
                if (splittedLine.length != 3 || !splittedLine[0].equals(proteinAccession)) {
                    throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to accession " + proteinAccession + ".");
                }
                result.add(splittedLine[1]);
            }
        }
        return result;
    }

    /**
     * Returns a list of non redundant GO accession numbers corresponding to a
     * protein match.
     *
     * @param matchKey the key of the protein match
     * @return a list of non redundant GO accession numbers corresponding to a
     * protein match
     * @throws IOException
     */
    public ArrayList<String> getProteinGoAccessions(String matchKey) throws IOException {
        String[] accessions = ProteinMatch.getAccessions(matchKey);
        ArrayList<String> goAccessions = new ArrayList<String>();
        for (String accession : accessions) {
            for (String goTerm : getGoAccessions(accession)) {
                if (!goAccessions.contains(goTerm)) {
                    goAccessions.add(goTerm);
                }
            }
        }
        return goAccessions;
    }

    /**
     * Returns a list of non redundant GO term descriptions corresponding to a
     * protein match.
     *
     * @param matchKey the key of the protein match
     * @return a list of non redundant GO term descriptions corresponding to a
     * protein match
     * @throws IOException
     */
    public ArrayList<String> getProteinGoDescriptions(String matchKey) throws IOException {
        String[] accessions = ProteinMatch.getAccessions(matchKey);
        ArrayList<String> goDescriptions = new ArrayList<String>();
        for (String accession : accessions) {
            for (String goAccession : getGoAccessions(accession)) {
                String goDescription = getTermDescription(goAccession).toLowerCase();
                if (!goDescriptions.contains(goDescription)) {
                    goDescriptions.add(goDescription);
                }
            }
        }
        return goDescriptions;
    }

    /**
     * Returns the protein accessions linked to a GO accession number.
     *
     * @param goAccession the GO accession number
     * @return a list of GO accessions numbers, an empty list if none found
     * @throws IOException
     */
    public ArrayList<String> getAccessions(String goAccession) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<Long> indexes = termIndexes.get(goAccession);
        if (indexes != null) {
            for (long index : indexes) {
                bufferedRandomAccessFile.seek(index);
                String line = bufferedRandomAccessFile.getNextLine();
                String[] splittedLine = line.split(separator);
                if (splittedLine.length != 3 || !splittedLine[1].equals(goAccession)) {
                    throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to GO accession " + goAccession + ".");
                }
                result.add(splittedLine[0]);
            }
        }
        return result;
    }

    /**
     * Returns the description of a GO term.
     *
     * @param goAccession the accession number of the GO term of interest
     * @return the first description found, null if not found
     * @throws IOException
     */
    public String getTermDescription(String goAccession) throws IOException {
        ArrayList<Long> indexes = termIndexes.get(goAccession);
        if (indexes != null && !indexes.isEmpty()) {
            long index = indexes.get(0);
            bufferedRandomAccessFile.seek(index);
            String line = bufferedRandomAccessFile.getNextLine();
            String[] splittedLine = line.split(separator);
            if (splittedLine.length != 3 || !splittedLine[1].equals(goAccession)) {
                throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to GO accession " + goAccession + ".");
            }
            return splittedLine[2];
        }
        return null;
    }

    /**
     * Returns the accession number of a GO term.
     *
     * @param goTerm the description of the GO term of interest
     * @return the first GO accession number found, null if not found
     * @throws IOException
     */
    public String getTermAccession(String goTerm) throws IOException {
        goTerm = goTerm.toLowerCase();
        ArrayList<Long> indexes = termNameIndexes.get(goTerm);
        if (indexes != null && !indexes.isEmpty()) {
            long index = indexes.get(0);
            bufferedRandomAccessFile.seek(index);
            String line = bufferedRandomAccessFile.getNextLine();
            String[] splittedLine = line.split(separator);
            if (splittedLine.length != 3 || !splittedLine[2].equalsIgnoreCase(goTerm)) {
                throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to GO term " + goTerm + ".");
            }
            return splittedLine[1];
        }
        return null;
    }

    /**
     * Returns the total number of proteins in this mapping.
     *
     * @return the total number of proteins in this mapping
     */
    public int getNumberOfProteins() {
        return proteinIndexes.size();
    }

    /**
     * Returns the total number of GO terms in this mapping.
     *
     * @return the total number of GO terms in this mapping
     */
    public int getNumberOfTerms() {
        return termIndexes.size();
    }

    /**
     * Returns the total number of protein accessions mapping to a given GO
     * term.
     *
     * @param goAccession the GO accession number of interest
     * @return the total number of proteins mapping to a given GO accession
     * number
     */
    public int getNProteinsForTerm(String goAccession) {
        ArrayList<Long> indexes = termIndexes.get(goAccession);
        if (indexes == null) {
            return 0;
        }
        return indexes.size();
    }

    /**
     * Returns the total number of GO terms mapping to a given protein.
     *
     * @param accession the accession of the protein
     * @return the total number of GO terms mapping to a given protein
     */
    public int getNTermsForProtein(String accession) {
        ArrayList<Long> indexes = termIndexes.get(accession);
        if (indexes == null) {
            return 0;
        }
        return indexes.size();
    }

    /**
     * Returns a non redundant list of all the proteins mapped.
     *
     * @return a non redundant list of all the proteins mapped
     */
    public ArrayList<String> getProteinMapped() {
        return new ArrayList<String>(proteinIndexes.keySet());
    }

    /**
     * Returns a non redundant list of all the GO accession numbers mapped.
     *
     * @return a non redundant list of all the GO accession numbers mapped
     */
    public ArrayList<String> getTermsMapped() {
        return new ArrayList<String>(termIndexes.keySet());
    }

    /**
     * Returns a non redundant list of all the GO term descriptions mapped.
     *
     * @return a non redundant list of all the GO term descriptions mapped
     */
    public ArrayList<String> getTermNamesMapped() {
        return new ArrayList<String>(termNameIndexes.keySet());
    }

    /**
     * Closes connections.
     *
     * @throws IOException
     */
    public void closeFiles() throws IOException {
        if (bufferedRandomAccessFile != null) {
            bufferedRandomAccessFile.close();
            mappingFileOpen = false;
        }
    }

    /**
     * Clears the mappings.
     */
    public void clearFactory() {
        proteinIndexes.clear();
        termIndexes.clear();
        termNameIndexes.clear();
    }

    /**
     * Returns true of the mapping file is currently open.
     *
     * @return true of the mapping file is currently open
     */
    public boolean isMappingFileOpen() {
        return mappingFileOpen;
    }
}
