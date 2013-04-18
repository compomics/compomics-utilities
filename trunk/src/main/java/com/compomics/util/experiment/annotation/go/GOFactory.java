/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.annotation.go;

import com.compomics.util.gui.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * The GOFactory annotates identification results based on Ensembl Gene Ontology
 * terms
 *
 * @author Marc
 */
public class GOFactory {

    /**
     * The instance of the factory.
     */
    private static GOFactory instance = null;

    /**
     * Constructor.
     */
    private GOFactory() {
    }

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
     * Random access file of the selected file.
     */
    private BufferedRandomAccessFile bufferedRandomAccessFile = null;
    /**
     * The separator used to separate line contents.
     */
    public final static String separator = "\t";
    /**
     * Map of all the indexes where a protein can be found Accession -> indexes
     */
    private HashMap<String, ArrayList<Long>> proteinIndexes = new HashMap<String, ArrayList<Long>>();
    /**
     * Map of all the indexes where a go term can be found go term -> indexes
     */
    private HashMap<String, ArrayList<Long>> termIndexes = new HashMap<String, ArrayList<Long>>();

    public void initialize(File file, WaitingHandler waitingHandler) throws IOException {
        if (bufferedRandomAccessFile != null) {
            bufferedRandomAccessFile.close();
        }
        bufferedRandomAccessFile = new BufferedRandomAccessFile(file, "r", 1024 * 100);


        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressValue(100);
            waitingHandler.setSecondaryProgressValue(0);
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
                String goTerm = splittedLine[1];
                indexes = termIndexes.get(goTerm);
                if (indexes == null) {
                    indexes = new ArrayList<Long>();
                    termIndexes.put(goTerm, indexes);
                }
                indexes.add(index);
            }

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressValue((int) (index / progressUnit));
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }
    }

    /**
     * Returns the go terms linked to a given accession.
     *
     * @param accession the accession of the protein of interest
     * @return a list of go terms, an empty list if no mapping is found
     * @throws IOException
     */
    public ArrayList<String> getGoTerms(String accession) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<Long> indexes = proteinIndexes.get(accession);
        if (indexes != null) {
            for (long index : indexes) {
                bufferedRandomAccessFile.seek(index);
                String line = bufferedRandomAccessFile.getNextLine();
                String[] splittedLine = line.split(separator);
                if (splittedLine.length != 3 || !splittedLine[0].equals(accession)) {
                    throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to accession " + accession + ".");
                }
                result.add(splittedLine[1]);
            }
        }
        return result;
    }

    /**
     * Returns the protein accessions linked to a GO term
     *
     * @param goTerm the go term
     * @return a list of accessions, an emtpy list if none found
     * @throws IOException
     */
    public ArrayList<String> getAccessions(String goTerm) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<Long> indexes = termIndexes.get(goTerm);
        if (indexes != null) {
            for (long index : indexes) {
                bufferedRandomAccessFile.seek(index);
                String line = bufferedRandomAccessFile.getNextLine();
                String[] splittedLine = line.split(separator);
                if (splittedLine.length != 3 || !splittedLine[1].equals(goTerm)) {
                    throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to GO term " + goTerm + ".");
                }
                result.add(splittedLine[1]);
            }
        }
        return result;
    }

    /**
     * Returns the description of a GO term
     *
     * @param goTerm the go term of interest
     * @return the first description found, null if not found
     * @throws IOException
     */
    public String getTermDescription(String goTerm) throws IOException {
        ArrayList<Long> indexes = termIndexes.get(goTerm);
        if (indexes != null && !indexes.isEmpty()) {
            long index = indexes.get(0);
            bufferedRandomAccessFile.seek(index);
            String line = bufferedRandomAccessFile.getNextLine();
            String[] splittedLine = line.split(separator);
            if (splittedLine.length != 3 || !splittedLine[1].equals(goTerm)) {
                throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to GO term " + goTerm + ".");
            }
            return splittedLine[3];
        }
        return null;
    }

    /**
     * Closes connections
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (bufferedRandomAccessFile != null) {
            bufferedRandomAccessFile.close();
        }
    }
}
