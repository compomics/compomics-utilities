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
 */
public class BiomartMapping {

    /**
     * The separator used to separate line contents.
     */
    public final static String SEPARATOR = "\t";
    /**
     * Ensembl assembly to BioMart dataset.
     */
    private HashMap<String, String> assemblyToDataset;

    /**
     * Constructor.
     */
    public BiomartMapping() {
        assemblyToDataset = new HashMap<String, String>();
    }

    /**
     * Loads the species mapping from a file. Previous mapping will be
     * overwritten.
     *
     * @param ensemblFile the Ensembl BioMart file
     * @param ensemblGenomeFile the Ensembl genome BioMart file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file.
     */
    public void loadMapping(File ensemblFile, File ensemblGenomeFile) throws IOException {

        FileReader r = new FileReader(ensemblFile);

        try {
            BufferedReader br = new BufferedReader(r);

            try {
                String line;

                while ((line = br.readLine()) != null) {

                    line = line.trim();

                    if (line.length() > 0) {

                        String[] elements = line.split(SEPARATOR);
                        String dataset = elements[0].trim();
                        String assembly = elements[1].trim();
                        if (!assembly.equals("") && !dataset.equals("")) {
                            assemblyToDataset.put(assembly, dataset);
                        }
                    }
                }
            } finally {
                br.close();
            }
        } finally {
            r.close();
        }

        r = new FileReader(ensemblGenomeFile);

        try {
            BufferedReader br = new BufferedReader(r);

            try {
                String line;

                while ((line = br.readLine()) != null) {

                    line = line.trim();

                    if (line.length() > 0) {

                        String[] elements = line.split(SEPARATOR);
                        String dataset = elements[0].trim();
                        String version = elements[1].trim();
                        elements = version.split(" ");
                        String assembly = elements[0].trim();
                        if (!assembly.equals("") && !dataset.equals("")) {
                            assemblyToDataset.put(assembly, dataset);
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
     * Returns the Ensembl dataset for the given assembly. Null if not found.
     *
     * @param assembly the assembly
     *
     * @return the Ensembl dataset
     */
    public String getDataset(String assembly) {
        return assemblyToDataset.get(assembly);
    }
}
