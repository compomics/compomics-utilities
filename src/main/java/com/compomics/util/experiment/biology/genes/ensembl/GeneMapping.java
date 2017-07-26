package com.compomics.util.experiment.biology.genes.ensembl;

import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Class for the handling of gene mappings.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class GeneMapping {

    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\t";
    /**
     * Gene name to chromosome map.
     */
    private HashMap<String, String> geneNameToChromosome;
    /**
     * Gene name to Ensembl accession map.
     */
    private HashMap<String, String> geneNameToAccession;

    /**
     * Constructor.
     */
    public GeneMapping() {
        geneNameToChromosome = new HashMap<>();
        geneNameToAccession = new HashMap<>();
    }

    /**
     * Reads go mappings from a BioMart file. The structure of the file should
     * be Ensembl Accession Gene name Chromosome name.
     *
     * Previous mappings are silently overwritten.
     *
     * @param file the file containing the mapping
     * @param waitingHandler a waiting handler allowing display of the progress
     * and canceling of the process.
     *
     * @throws IOException if an exception occurs while reading the file
     */
    public void importFromFile(File file, WaitingHandler waitingHandler) throws IOException {

        // read the species list
        FileReader r = new FileReader(file);
        try {
            BufferedReader br = new BufferedReader(r);
            try {

                String line;

                while ((line = br.readLine()) != null) {

                    String[] splittedLine = line.split(SEPARATOR);

                    if (splittedLine.length == 3 && !splittedLine[0].equals("") && !splittedLine[1].equals("")) {

                        String accession = splittedLine[0];
                        String geneName = splittedLine[1];
                        String chromosome = splittedLine[2];
                        geneNameToChromosome.put(geneName, chromosome);
                        geneNameToAccession.put(geneName, accession);
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
     * Returns the chromosome for a given gene.
     *
     * @param geneName the gene name
     *
     * @return the chromosome for a given gene
     */
    public String getChromosome(String geneName) {
        return geneNameToChromosome.get(geneName);
    }

    /**
     * Returns the Ensembl accession for a given gene.
     *
     * @param geneName the gene name
     *
     * @return the Ensembl accession for a given gene
     */
    public String getEnsemblAccession(String geneName) {
        return geneNameToAccession.get(geneName);
    }

    /**
     * Returns the gene name to chromosome map.
     *
     * @return the gene name to chromosome map
     */
    public HashMap<String, String> getGeneNameToChromosome() {
        return geneNameToChromosome;
    }

    /**
     * Returns the gene name to protein accession map.
     *
     * @return the gene name to protein accession map
     */
    public HashMap<String, String> getGeneNameToAccession() {
        return geneNameToAccession;
    }
}
