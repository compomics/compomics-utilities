package com.compomics.software.settings;

import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.genes.GeneFactory;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationParametersFactory;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.pride.PrideObjectsFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class sets the path preferences for the files to read/write.
 *
 * @author Marc Vaudel
 */
public class UtilitiesPathPreferences {

    /**
     * Default name for the path configuration file.
     */
    public static final String configurationFileName = "resources/conf/paths.txt";
    /**
     * The separator between a path ID and a path.
     */
    public final static String separator = "=";
    /**
     * Replacement for the path when not available.
     */
    public static final String defaultPath = "default";

    /**
     * Enum of the paths which can be set in utilities.
     */
    public enum UtilitiesPathKey implements PathKey {

        /**
         * Folder containing the compomics utilities user preferences file.
         */
        utilitiesPreferencesKey("utilities_user_preferences", "Folder containing the compomics utilities user preferences file.", "", true),
        /**
         * Folder containing the PTM user preferences file.
         */
        ptmFactoryKey("ptm_configuration", "Folder containing the supported PTMs.", "", true),
        /**
         * File containing the enzymes implemented.
         */
        enzymeFactoryKey("enzyme_configuration", "File containing the supported enzymes.", "", true),
        /**
         * Folder containing the gene mapping files.
         */
        geneMappingKey("gene_mapping", "Folder containing the gene mapping files.", "gene_mapping", true),
        /**
         * Folder containing the pride annotation preferences.
         */
        prideAnnotationKey("pride_annotation", "Folder containing the PRIDE annotation preferences.", "pride", true),
        /**
         * Folder containing the identification parameters
         */
        identificationParametersKey("identification_parameters", "Folder containing the identification parameters.", IdentificationParametersFactory.PARAMETERS_FOLDER, true);
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

        @Override
        public String getId() {
            return id;
        }

        @Override
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
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
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
     * @throws FileNotFoundException if a FileNotFoundException occurs
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
            if (!path.equals(UtilitiesPathPreferences.defaultPath)) {
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
    }

    /**
     * Sets the path according to the given key and path.
     *
     * @param utilitiesPathKey the key of the path
     * @param path the path to be set
     */
    public static void setPathPreference(UtilitiesPathKey utilitiesPathKey, String path) {
        switch (utilitiesPathKey) {
            case geneMappingKey:
                GeneFactory.setGeneMappingFolder(path);
                return;
            case prideAnnotationKey:
                PrideObjectsFactory.setPrideFolder(path);
                return;
            case ptmFactoryKey:
                ModificationFactory.setSerializationFolder(path);
                return;
            case enzymeFactoryKey:
                EnzymeFactory.setSerializationFile(path);
                return;
            case utilitiesPreferencesKey:
                UtilitiesUserPreferences.setUserPreferencesFolder(path);
                return;
            case identificationParametersKey:
                IdentificationParametersFactory.setParentFolder(path);
                return;
            default:
                throw new UnsupportedOperationException("Path " + utilitiesPathKey.id + " not implemented.");
        }
    }

    /**
     * Returns the path according to the given key and path.
     *
     * @param utilitiesPathKey the key of the path
     *
     * @return the path to be set
     */
    public static String getPathPreference(UtilitiesPathKey utilitiesPathKey) {
        switch (utilitiesPathKey) {
            case geneMappingKey:
                return GeneFactory.getGeneMappingFolder().getAbsolutePath();
            case prideAnnotationKey:
                return PrideObjectsFactory.getPrideFolder();
            case ptmFactoryKey:
                return ModificationFactory.getSerializationFolder();
            case enzymeFactoryKey:
                return EnzymeFactory.getSerializationFile();
            case utilitiesPreferencesKey:
                return UtilitiesUserPreferences.getUserPreferencesFolder();
            case identificationParametersKey:
                return IdentificationParametersFactory.getParentFolder();
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
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public static void setAllPathsIn(String path) throws FileNotFoundException {
        for (UtilitiesPathKey utilitiesPathKey : UtilitiesPathKey.values()) {
            String subDirectory = utilitiesPathKey.defaultSubDirectory;
            File newFile = new File(path, subDirectory);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            if (!newFile.exists()) {
                throw new FileNotFoundException(newFile.getAbsolutePath() + " could not be created.");
            }
            setPathPreference(utilitiesPathKey, newFile.getAbsolutePath());
        }
    }

    /**
     * Writes all path configurations to the given file.
     *
     * @param file the destination file
     *
     * @throws IOException if an IOException occurs
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
     * @throws IOException if an IOException occurs
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
     * @throws IOException if an IOException occurs
     */
    public static void writePathToFile(BufferedWriter bw, UtilitiesPathKey pathKey) throws IOException {

        bw.write(pathKey.id + UtilitiesPathPreferences.separator);
        String toWrite = "";
        switch (pathKey) {
            case geneMappingKey:
                toWrite = GeneFactory.getGeneMappingFolder().getAbsolutePath();
                if (toWrite == null) {
                    toWrite = UtilitiesPathPreferences.defaultPath;
                }
                bw.write(toWrite);
                break;
            case prideAnnotationKey:
                toWrite = PrideObjectsFactory.getPrideFolder();
                if (toWrite == null) {
                    toWrite = UtilitiesPathPreferences.defaultPath;
                }
                bw.write(toWrite);
                break;
            case ptmFactoryKey:
                toWrite = ModificationFactory.getSerializationFolder();
                if (toWrite == null) {
                    toWrite = UtilitiesPathPreferences.defaultPath;
                }
                bw.write(toWrite);
                break;
            case enzymeFactoryKey:
                toWrite = EnzymeFactory.getSerializationFile();
                if (toWrite == null) {
                    toWrite = UtilitiesPathPreferences.defaultPath;
                }
                bw.write(toWrite);
                break;
            case utilitiesPreferencesKey:
                toWrite = UtilitiesUserPreferences.getUserPreferencesFolder();
                if (toWrite == null) {
                    toWrite = UtilitiesPathPreferences.defaultPath;
                }
                bw.write(toWrite);
                break;
            case identificationParametersKey:
                toWrite = IdentificationParametersFactory.getParentFolder();
                if (toWrite == null) {
                    toWrite = IdentificationParametersFactory.PARAMETERS_FOLDER;
                }
                bw.write(toWrite);
                break;
            default:
                throw new UnsupportedOperationException("Path " + pathKey.id + " not implemented.");
        }

        bw.newLine();
    }

    /**
     * Tests whether it is possible to write in a destination folder.
     *
     * @param destinationPath the folder to test
     *
     * @return a boolean indicating whether it is possible to write in the
     * destination folder
     */
    public static boolean testPath(String destinationPath) {
        try {
            
            File destinationFile = new File(destinationPath);
            if (!destinationFile.exists()) {
                try {
                    if (!destinationFile.mkdirs()) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }

            File testFile = new File(destinationPath, "test_path_configuration.tmp");
            BufferedWriter bw = new BufferedWriter(new FileWriter(testFile));
            try {
                bw.write("test");
            } finally {
                try {
                    bw.close();
                } finally {
                    if (testFile.exists()) {
                        testFile.delete();
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Returns a list containing the keys of the paths where the tool is not
     * able to write.
     *
     * @return a list containing the keys of the paths where the tool is not
     * able to write
     *
     * @throws IOException exception thrown whenever an error occurred while
     * loading the path configuration
     */
    public static ArrayList<PathKey> getErrorKeys() throws IOException {
        ArrayList<PathKey> result = new ArrayList<>();
        for (UtilitiesPathPreferences.UtilitiesPathKey utilitiesPathKey : UtilitiesPathPreferences.UtilitiesPathKey.values()) {
            String folder = UtilitiesPathPreferences.getPathPreference(utilitiesPathKey);
            if (folder != null && !testPath(folder)) {
                result.add(utilitiesPathKey);
            }
        }
        return result;
    }
}
