package com.compomics.util.experiment.annotation.gene;

import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.waiting.WaitingHandler;
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
     * Gene name to chromosome number mapping.
     */
    private HashMap<String, String> geneNameToChromosome = new HashMap<String, String>();
    /**
     * Random access file of the selected gene mapping file.
     */
    private BufferedRandomAccessFile geneMappingFile = null;
    /**
     * The separator used to separate line contents.
     */
    public final static String separator = "\t";
    /**
     * Map of the index where a gene can be found Ensembl gene ID &gt; index.
     */
    private HashMap<String, Long> geneIdIndexes = new HashMap<String, Long>();
    /**
     * Map of the index where a gene can be found gene name &gt; index.
     */
    private HashMap<String, Long> geneNameIndexes = new HashMap<String, Long>();
    /**
     * Boolean indicating if the mapping file is currently open.
     */
    private boolean mappingFileOpen = false;
    /**
     * The current Ensembl versions. Key is the Ensembl type, e.g., default or
     * plans.
     */
    private HashMap<String, Integer> ensemblVersions = new HashMap<String, Integer>();

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
     * Constructor.
     */
    private GeneFactory() {
    }

    /**
     * Initializes the factory on the given file. If gene IDs are duplicate only
     * the last one will be retained.
     *
     * @param file the file containing the mapping
     * @param waitingHandler a waiting handler allowing display of the progress
     * and canceling of the process.
     * @throws IOException
     */
    public void initialize(File file, WaitingHandler waitingHandler) throws IOException {

        // remove the told data
        clearFactory();

        if (geneMappingFile != null) {
            geneMappingFile.close();
        }

        geneMappingFile = new BufferedRandomAccessFile(file, "r", 1024 * 100);
        mappingFileOpen = true;

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        long progressUnit = geneMappingFile.length() / 100;

        String line = geneMappingFile.readLine();
        long index = geneMappingFile.getFilePointer();

        while ((line = geneMappingFile.readLine()) != null) {

            String[] splittedLine = line.split(separator);

            if (splittedLine.length == 3 && !splittedLine[0].equals("") && !splittedLine[1].equals("")) {

                // check if the chromosome mapping is actually a chromosome
                boolean realChromosome = false;
                String chromosome = splittedLine[2];
                try {
                    Integer.parseInt(splittedLine[2]); // @TODO: is this a suitable test for all species..?
                    realChromosome = true;
                } catch (NumberFormatException e) {
                    // see if it is X or Y
                    if (chromosome.equalsIgnoreCase("X") || chromosome.equalsIgnoreCase("Y")
                            || chromosome.equalsIgnoreCase("Z") || chromosome.equalsIgnoreCase("W")) { // @TODO: is this a suitable test for all species..?
                        realChromosome = true;
                    }
                }

                if (realChromosome) {
                    String accession = splittedLine[0];
                    String geneName = splittedLine[1];
                    geneIdIndexes.put(accession, index);
                    geneNameIndexes.put(geneName, index);

                    geneNameToChromosome.put(geneName, chromosome);
                }
            }

            index = geneMappingFile.getFilePointer();

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressCounter((int) (index / progressUnit));
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }
    }

    /**
     * Returns the chromosome for a given gene.
     *
     * @param geneName the gene name
     * @return the chromosome for a given gene
     */
    public String getChromosomeForGeneName(String geneName) {
        return geneNameToChromosome.get(geneName);
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
     * Returns a list of the mapped genes indexed by their gene names.
     *
     * @return a list of the mapped genes
     */
    public ArrayList<String> getMappedGeneNames() {
        return new ArrayList<String>(geneNameIndexes.keySet());
    }

    /**
     * Returns the name of a gene, null if not found.
     *
     * @param geneID the Ensembl ID of the gene of interest
     * @return the name of the gene
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
     * Returns the Ensembl ID of a gene, null if not found.
     *
     * @param geneName the gene name of the gene of interest
     * @return the Ensembl ID of the gene
     * @throws IOException
     */
    public String getGeneEnsemblId(String geneName) throws IOException {
        Long index = geneNameIndexes.get(geneName);
        if (index != null) {
            geneMappingFile.seek(index);
            String line = geneMappingFile.getNextLine();
            String[] splittedLine = line.split(separator);
            if (splittedLine.length != 3 || !splittedLine[1].equals(geneName)) {
                throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to gene name " + geneName + ".");
            }
            return splittedLine[0];
        }
        return null;
    }

    /**
     * Returns the chromosome where a gene can be located, null if not found.
     *
     * @param geneID the Ensembl ID of the gene of interest
     * @return the chromosome where the gene can be located
     * @throws IOException
     */
    public String getChromosomeFromGeneId(String geneID) throws IOException {
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
     * Returns the chromosome where a gene can be located, null if not found.
     *
     * @param geneName the gene name of the gene of interest
     * @return the chromosome where the gene can be located
     * @throws IOException
     */
    public String getChromosomeFromGeneName(String geneName) throws IOException {
        Long index = geneNameIndexes.get(geneName);
        if (index != null) {
            geneMappingFile.seek(index);
            String line = geneMappingFile.getNextLine();
            String[] splittedLine = line.split(separator);
            if (splittedLine.length != 3 || !splittedLine[1].equals(geneName)) {
                throw new IllegalArgumentException("Line \"" + line + "\" at index " + index + " does not correspond to gene name " + geneName + ".");
            }
            return splittedLine[2];
        }
        return null;
    }

    /**
     * Returns the gene name attached to a protein. Note, for now this is
     * implemented only for UniProt sequences. The sequences must be imported in
     * the SequenceFactory.
     *
     * @param proteinAccession the accession of the protein of interest
     * @return the name of the gene, null if not found
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException RE
     */
    public String getGeneNameForUniProtProtein(String proteinAccession) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {
        Header header = SequenceFactory.getInstance().getHeader(proteinAccession);
        return header.getGeneName();
    }

    /**
     * Closes files.
     *
     * @throws IOException
     */
    public void closeFiles() throws IOException {
        if (geneMappingFile != null) {
            geneMappingFile.close();
            mappingFileOpen = false;
        }
    }

    /**
     * Clears the mappings.
     */
    public void clearFactory() {
        geneIdIndexes.clear();
        geneNameIndexes.clear();
        geneNameToChromosome.clear();
    }

    /**
     * Returns true of the mapping file is currently open.
     *
     * @return true of the mapping file is currently open
     */
    public boolean isMappingFileOpen() {
        return mappingFileOpen;
    }

    /**
     * Returns the current Ensembl version number. Null if not found.
     *
     * @param ensemblType the Ensembl type, e.g., ensembl or plants
     * @return the current Ensembl version number
     */
    public Integer getCurrentEnsemblVersion(String ensemblType) {

        // @TODO: find a less hard coded way of finding the current ensembl versions!!!
        if (ensemblType.equalsIgnoreCase("fungi")
                || ensemblType.equalsIgnoreCase("plants")
                || ensemblType.equalsIgnoreCase("protists")
                || ensemblType.equalsIgnoreCase("metazoa")) {
            return 23;
        } else {
            return 77;
        }

        // the code below used to work but is not always updated when new ensembl versions are released
//        if (ensemblVersions == null) {
//            ensemblVersions = new HashMap<String, Integer>();
//        }
//        if (!ensemblVersions.containsKey(ensemblType)) {
//
//            try {
//                // get the current Ensembl version
//                URL url = new URL("http://www.biomart.org/biomart/martservice?type=registry");
//
//                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//
//                String inputLine;
//                boolean ensemblVersionFound = false;
//                String ensemblVersionAsText = "?";
//
//                while ((inputLine = in.readLine()) != null && !ensemblVersionFound) {
//                    if (inputLine.indexOf("database=\"" + ensemblType + "_mart_") != -1) {
//                        ensemblVersionAsText = inputLine.substring(inputLine.indexOf("database=\"" + ensemblType + "_mart_") + ("database=\"" + ensemblType + "_mart_").length());
//                        ensemblVersionAsText = ensemblVersionAsText.substring(0, ensemblVersionAsText.indexOf("\""));
//                        ensemblVersionFound = true;
//                    }
//                }
//
//                in.close();
//
//                if (ensemblVersionFound) {
//                    try {
//                        Integer ensemblVersion = new Integer(ensemblVersionAsText);
//                        ensemblVersions.put(ensemblType, ensemblVersion);
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return ensemblVersions.get(ensemblType);
    }

    /**
     * Returns the name of the Ensembl database for BioMart queries.
     *
     * @param speciesTypeIndex the species type index: 1: fungi, 2: plants, 3:
     * protist, 4: metazoa or 5: default.
     * @return the name of the Ensembl database for BioMart queries
     */
    public String getEnsemblDbName(int speciesTypeIndex) {

        switch (speciesTypeIndex) {
            case 1:
                return "fungi_mart_" + getCurrentEnsemblVersion("fungi");
            case 2:
                return "plants_mart_" + getCurrentEnsemblVersion("plants");
            case 3:
                return "protists_mart_" + getCurrentEnsemblVersion("protists");
            case 4:
                return "metazoa_mart_" + getCurrentEnsemblVersion("metazoa");
            case 5:
                return "default";
        }

        return "unknown"; // should not happen!!!
    }
}
