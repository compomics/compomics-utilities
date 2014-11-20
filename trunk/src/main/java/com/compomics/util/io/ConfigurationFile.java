package com.compomics.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Generic configuration text file.
 *
 * @author Marc Vaudel
 */
public class ConfigurationFile {

    /**
     * The file where to read/write configuration from/to.
     */
    private File configurationFile;

    public ConfigurationFile(File configurationFile) {
        this.configurationFile = configurationFile;
    }

    /**
     * Sets the value for a given parameter.
     *
     * @param parameterName the name of the parameter of interest
     * @param value the value to set
     *
     * @throws FileNotFoundException exception thrown whenever the connection to
     * the file was dropped before execution is finished
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing to the file
     */
    public void setParameter(String parameterName, String value) throws FileNotFoundException, IOException {

        StringBuilder newContent = new StringBuilder();

        boolean found = false;
        if (configurationFile.exists()) {
            FileReader fileReader = new FileReader(configurationFile);
            try {
                BufferedReader br = new BufferedReader(fileReader);
                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        newContent.append(line).append(System.getProperty("line.separator"));
                        // Skip empty lines and comment ('#') lines.
                        line = line.trim();
                        if (!line.equals("") && !line.startsWith("#") && line.equals(parameterName)) {
                            found = true;
                            newContent.append(value).append(System.getProperty("line.separator"));
                            br.readLine();
                        }
                    }
                } finally {
                    br.close();
                }
            } finally {
                fileReader.close();
            }

            if (!found) {
                newContent.append(parameterName).append(System.getProperty("line.separator"));
                newContent.append(value).append(System.getProperty("line.separator"));
            }
            FileWriter fileWriter = new FileWriter(configurationFile);
            try {
                fileWriter.write(newContent.toString());
            } finally {
                fileWriter.close();
            }
        }
    }

    /**
     * Returns a parameter line corresponding to the given parameter name. Null
     * if not found.
     *
     * @param parameterName the name of the parameter of interest
     *
     * @return line corresponding to the given parameter
     *
     * @throws FileNotFoundException exception thrown when the connection to the
     * file is broken while reading
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     */
    public String getParameterLine(String parameterName) throws FileNotFoundException, IOException {

        String parameterLine = null;
        if (configurationFile.exists()) {

            FileReader fileReader = new FileReader(configurationFile);
            try {
                BufferedReader br = new BufferedReader(fileReader);
                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        // Skip empty lines and comment ('#') lines.
                        line = line.trim();
                        if (!line.equals("") && !line.startsWith("#") && line.equals(parameterName)) {
                            parameterLine = br.readLine().trim();
                        }
                    }
                } finally {
                    br.close();
                }
            } finally {
                fileReader.close();
            }
        }
        return parameterLine;
    }
}
