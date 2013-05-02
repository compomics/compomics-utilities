package com.compomics.util.experiment.annotation.gene;

import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.gui.waiting.WaitingHandler;
import com.compomics.util.protein.Header;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * The gene factory provides gene information for protein accessions.
 *
 * @author Marc Vaudel
 */
public class GeneFactory {

    /**
     * The instance of the factory.
     */
    private static GeneFactory instance = null;

    /**
     * Constructor.
     */
    private GeneFactory() {
    }

    /**
     * Static method returning the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static GeneFactory getInstance() {
        if (instance == null) {
            instance = new GeneFactory();
        }
        return instance;
    }
    /**
     * Random access file of the selected gene mapping file.
     */
    private BufferedRandomAccessFile geneMappingFile = null;
    /**
     * The separator used to separate line contents.
     */
    public final static String separator = "\t";
    /**
     * Map of the index where a gene can be found Ensembl gene ID -> index.
     */
    private HashMap<String, Long> geneIdIndexes = new HashMap<String, Long>();

    /**
     * Initializes the factory on the given file. If gene Ids are duplicate only
     * the last one will be retained.
     *
     * @param file the file containing the mapping
     * @param waitingHandler a waiting handler allowing display of the progress
     * and canceling of the process.
     * @throws IOException
     */
    public void initialize(File file, WaitingHandler waitingHandler) throws IOException {

        if (geneMappingFile != null) {
            geneMappingFile.close();
        }

        geneMappingFile = new BufferedRandomAccessFile(file, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressValue(100);
            waitingHandler.setSecondaryProgressValue(0);
        }

        long progressUnit = geneMappingFile.length() / 100;

        String line = geneMappingFile.readLine();
        long index = geneMappingFile.getFilePointer();

        while ((line = geneMappingFile.readLine()) != null) {

            String[] splittedLine = line.split(separator);

            if (splittedLine.length == 3 && !splittedLine[0].equals("") && !splittedLine[1].equals("")) {
                String accession = splittedLine[0];
                geneIdIndexes.put(accession, index);
            }
            index = geneMappingFile.getFilePointer();

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressValue((int) (index / progressUnit));
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }
    }

    /**
     * Returns a list of the mapped genes indexed by their Ensembl Gene ID.
     *
     * @return a list of the mapped genes
     */
    public ArrayList<String> getMappedGenes() {
        return new ArrayList<String>(geneIdIndexes.keySet());
    }

    /**
     * Returns the name of a gene, null if not found.
     *
     * @param geneID the Ensembl ID of the gene of interest
     * @return the name of a gene
     * @throws IOException
     */
    public String getGeneName(String geneID) throws IOException {
        Long index = geneIdIndexes.get(geneID);
        if (index != null) {
            geneMappingFile.seek(index);
            String line = geneMappingFile.getNextLine();
            String[] splittedLine = line.split(separator);
            if (splittedLine.length != 3 || !splittedLine[0].equals(geneID)) {
                throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to gene ID " + geneID + ".");
            }
            return splittedLine[1];
        }
        return null;
    }

    /**
     * Returns the chromosome where a gene can be located, null if not found.
     *
     * @param geneID the Ensembl ID of the gene of interest
     * @return the chromosome where a gene can be located
     * @throws IOException
     */
    public String getChromosome(String geneID) throws IOException {
        Long index = geneIdIndexes.get(geneID);
        if (index != null) {
            geneMappingFile.seek(index);
            String line = geneMappingFile.getNextLine();
            String[] splittedLine = line.split(separator);
            if (splittedLine.length != 3 || !splittedLine[0].equals(geneID)) {
                throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to gene ID " + geneID + ".");
            }
            return splittedLine[2];
        }
        return null;
    }

    /**
     * Returns the gene gene attached to a protein. Note, for now this is
     * implemented only for UniProt sequences. The sequences must be imported in
     * the SequenceFactory.
     *
     * @param proteinAccession the accession of the protein of interest
     * @return the ID of the gene, null if not found
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException RE
     */
    public String getGeneId(String proteinAccession) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {
        Header header = SequenceFactory.getInstance().getHeader(proteinAccession);
        return header.getGeneName();
    }

    /**
     * Closes connections.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (geneMappingFile != null) {
            geneMappingFile.close();
        }
    }

    /**
     * Clears the mappings.
     */
    public void clearFactory() {
        geneIdIndexes.clear();
    }
}
