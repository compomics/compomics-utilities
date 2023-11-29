package com.compomics.util.experiment.biology.taxonomy.mappings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Mapping of the species to BioMart dataset.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class BiomartMapping {

    /////////////////////////////
    // How to update this file:
    //
    // install.packages("BiocManager")
    // library(biomaRt)
    //
    // ensembl=useMart("ensembl")
    // vertibrate_table = listDatasets(ensembl)
    // ensembl=useMart("plants_mart", host="https://plants.ensembl.org")
    // plants_table = listDatasets(ensembl)
    // ensembl=useMart("fungi_mart", host="https://fungi.ensembl.org")
    // fungi_table = listDatasets(ensembl)
    // ensembl=useMart("metazoa_mart", host="https://metazoa.ensembl.org")
    // metazoa_table = listDatasets(ensembl)
    // ensembl=useMart("protists_mart", host="https://protists.ensembl.org")
    // protists_table = listDatasets(ensembl)
    // combined_table = do.call("rbind", list(vertibrate_table, plants_table, fungi_table, metazoa_table, protists_table))
    // write.table(combined_table, "[..]\\ensembl_biomart_databases", sep ="\t", quote = F, col.names = T, row.names = F)
    //
    // Note: There is no biomart for bacteria...
    /////////////////////////////
    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\t";
    /**
     * Dataset name to dataset version.
     */
    private final HashMap<String, String> datasetNameToDatasetVersion;

    /**
     * Constructor.
     */
    public BiomartMapping() {
        datasetNameToDatasetVersion = new HashMap<>();
    }

    /**
     * Loads the species mapping from a file. Previous mapping will be
     * overwritten.
     *
     * @param ensemblFile the Ensembl BioMart file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file.
     */
    public void loadMapping(File ensemblFile) throws IOException {

        FileReader r = new FileReader(ensemblFile);

        try {

            BufferedReader br = new BufferedReader(r);

            try {
                br.readLine(); // skip the header

                String line;

                while ((line = br.readLine()) != null) {

                    line = line.trim();

                    if (line.length() > 0) {

                        String[] elements = line.split(SEPARATOR);
                        String datasetName = elements[0].trim();
                        String datasetVersion = elements[2].trim();

                        datasetNameToDatasetVersion.put(datasetName, datasetVersion);

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
     * Returns the Ensembl dataset for the given assembly. Null if not found.
     *
     * @param datasetName the dataset name. E.g. 'hsapiens_gene_ensembl' or
     * 'acomosus_eg_gene'.
     *
     * @return the Ensembl dataset version
     */
    public String getDataset(String datasetName) {
        return datasetNameToDatasetVersion.get(datasetName);
    }

}
