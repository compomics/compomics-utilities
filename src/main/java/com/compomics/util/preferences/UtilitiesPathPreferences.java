package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTreeComponentsFactory;
import com.compomics.util.pride.PrideObjectsFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class sets the path preferences for the files to read/write.
 *
 * @author Marc Vaudel
 */
public class UtilitiesPathPreferences {

    /**
     * The separator between a path ID and a path.
     */
    public final static String separator = "=";

    /**
     * Enum of the paths which can be set in utilities.
     */
    public enum UtilitiesPathKey {

        /**
         * Folder containing the compomics utilities user preferences file.
         */
        utilitiesPreferencesKey("utilities_user_preferences", "Folder containing the compomics utilities user preferences file.", "", true),
        /**
         * Folder containing the PTM user preferences file.
         */
        ptmFactoryKey("ptm_configuration", "Folder containing the PTM user preferences file.", "", true),
        /**
         * Folder containing the indexes of the protein sequences databases.
         */
        fastaIndexesKey("fasta_indexes", "Folder containing the indexes of the protein sequences databases.", "fasta_indexes", true),
        /**
         * Folder containing the gene mapping files.
         */
        geneMappingKey("gene_mapping", "Folder containing the gene mapping files.", "gene_mapping", true),
        /**
         * Folder containing the pride annotation preferences.
         */
        prideAnnotationKey("pride_annotation", "Folder containing the pride annotation preferences.", "pride", true);
        /**
         * The key used to refer to this path.
         */
        private String id;
        /**
         * The description of the path usage.
         */
        private String description;
        /**
         * The default sub directory or file to use in case all paths should be
         * included in a single directory.
         */
        private String defaultSubDirectory;
        /**
         * Indicates whether the path should be a folder.
         */
        private boolean isDirectory;

        /**
         * Constructor.
         *
         * @param id the id used to refer to this path key
         * @param description the description of the path usage
         * @param isDirectory boolean indicating whether a folder is expected
         */
        private UtilitiesPathKey(String id, String description, String defaultSubDirectory, boolean isDirectory) {
            this.id = id;
            this.description = description;
            this.defaultSubDirectory = defaultSubDirectory;
            this.isDirectory = isDirectory;
        }

        /**
         * Returns the id of the path.
         *
         * @return the id of the path
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the description of the path.
         *
         * @return the description of the path
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the key from its id. Null if not found.
         *
         * @param id the id of the key of interest
         *
         * @return the key of interest
         */
        public static UtilitiesPathKey getKeyFromId(String id) {
            for (UtilitiesPathKey pathKey : values()) {
                if (pathKey.id.equals(id)) {
                    return pathKey;
                }
            }
            return null;
        }

    }

    /**
     * Loads the path preferences from a text file.
     *
     * @param inputFile the file to load the path preferences from
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void loadPathPreferencesFromFile(File inputFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.equals("") && !line.startsWith("#")) {
                }
            }
        } finally {
            br.close();
        }
    }

    /**
     * Loads a path to be set from a line.
     *
     * @param line the line where to read the path from
     *
     * @throws java.io.FileNotFoundException
     */
    public static void loadPathPreferenceFromLine(String line) throws FileNotFoundException {
        String id = getPathID(line);
        if (id.equals("")) {
            throw new IllegalArgumentException("Impossible to parse path in " + line + ".");
        }
        UtilitiesPathKey utilitiesPathKey = UtilitiesPathKey.getKeyFromId(id);
        if (utilitiesPathKey == null) {
            throw new IllegalArgumentException("Path " + id + " not recognized");
        } else {
            String path = getPath(line);
            File file = new File(path);
            if (!file.exists()) {
                throw new FileNotFoundException("File " + path + " not found.");
            }
            if (utilitiesPathKey.isDirectory && !file.isDirectory()) {
                throw new FileNotFoundException("Found a file when expecting a directory for " + utilitiesPathKey.id + ".");
            }
            setPathPreference(utilitiesPathKey, path);
        }
    }

    /**
     * Sets the path according to the given key and path.
     *
     * @param utilitiesPathKey the key of the path
     * @param path the path to be set
     */
    public static void setPathPreference(UtilitiesPathKey utilitiesPathKey, String path) {
        switch (utilitiesPathKey) {
            case fastaIndexesKey:
                ProteinTreeComponentsFactory.setDefaultDbFolderPath(path);
                return;
            case geneMappingKey:
                GenePreferences.setGeneMappingFolder(path);
                return;
            case prideAnnotationKey:
                PrideObjectsFactory.setPrideFolder(path);
                return;
            case ptmFactoryKey:
                PTMFactory.setSerializationFolder(path);
                return;
            case utilitiesPreferencesKey:
                UtilitiesUserPreferences.setUserPreferencesFolder(path);
                return;
            default:
                throw new UnsupportedOperationException("Path " + utilitiesPathKey.id + " not implemented.");
        }
    }

    /**
     * Returns the path id line. An empty string if the separator is not found.
     *
     * @param line the line of interest
     *
     * @return the id of the path
     */
    public static String getPathID(String line) {
        int separatorIndex = line.indexOf(separator);
        if (separatorIndex > 0) {
            return line.substring(0, separatorIndex);
        }
        return "";
    }

    /**
     * Returns the path at the given line. An empty string if the separator or
     * the path is not found.
     *
     * @param line the line
     *
     * @return the path after the separator
     */
    public static String getPath(String line) {
        int separatorIndex = line.indexOf(separator);
        if (separatorIndex > 0 && separatorIndex < line.length()) {
            return line.substring(separatorIndex + 1);
        }
        return "";
    }

    /**
     * Sets all the paths inside a given folder.
     *
     * @param path the path of the folder where to redirect all paths.
     *
     * @throws FileNotFoundException
     */
    public static void setAllPathsIn(String path) throws FileNotFoundException {
        for (UtilitiesPathKey utilitiesPathKey : UtilitiesPathKey.values()) {
            String subDirectory = utilitiesPathKey.defaultSubDirectory;
            File newFile = new File(path, subDirectory);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            if (!newFile.exists()) {
                throw new FileNotFoundException(newFile.getAbsolutePath() + "could not be created.");
            }
            setPathPreference(utilitiesPathKey, newFile.getAbsolutePath());
        }
    }

    /**
     * Writes all path configurations to the given file.
     *
     * @param file the destination file
     *
     * @throws IOException
     */
    public static void writeConfigurationToFile(File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        try {
            writeConfigurationToFile(bw);
        } finally {
            bw.close();
        }
    }

    /**
     * Writes the configuration file using the provided buffered writer.
     *
     * @param bw the writer to use for writing
     *
     * @throws IOException
     */
    public static void writeConfigurationToFile(BufferedWriter bw) throws IOException {
        for (UtilitiesPathKey pathKey : UtilitiesPathKey.values()) {
            writePathToFile(bw, pathKey);
        }
    }

    /**
     * Writes the path of interest using the provided buffered writer.
     *
     * @param bw the writer to use for writing
     * @param pathKey the key of the path of interest
     *
     * @throws IOException
     */
    public static void writePathToFile(BufferedWriter bw, UtilitiesPathKey pathKey) throws IOException {

        bw.write(pathKey.id + UtilitiesPathPreferences.separator);

        switch (pathKey) {
            case fastaIndexesKey:
                bw.write(ProteinTreeComponentsFactory.getDefaultDbFolderPath());
                break;
            case geneMappingKey:
                bw.write(GenePreferences.getGeneMappingFolder().getAbsolutePath());
                break;
            case prideAnnotationKey:
                bw.write(PrideObjectsFactory.getPrideFolder());
                break;
            case ptmFactoryKey:
                bw.write(PTMFactory.getSerializationFolder());
                break;
            case utilitiesPreferencesKey:
                bw.write(UtilitiesUserPreferences.getUserPreferencesFolder());
                break;
            default:
                throw new UnsupportedOperationException("Path " + pathKey.id + " not implemented.");
        }

        bw.newLine();
    }
}
