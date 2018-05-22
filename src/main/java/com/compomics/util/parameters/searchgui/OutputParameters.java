package com.compomics.util.parameters.searchgui;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This enum lists the possible output options.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum OutputParameters {

    /**
     * Groups all files in a single compressed zip folder.
     */
    grouped(0, "Single Zip File", "Group all files in a single compressed zip folder"),
    /**
     * Groups files per run (i.e. spectrum file).
     */
    run(1, "Zip File per Mgf", "Group files per run (i.e. spectrum file)"),
    /**
     * Groups files per identification algorithm.
     */
    algorithm(2, "Zip File per Algorithm", "Group files per identification algorithm"),
    /**
     * No file grouping.
     */
    no_zip(3, "No Zipping", "No file grouping");

    /**
     * The index of the option.
     */
    public final int id;
    /**
     * Name of the option.
     */
    public final String name;
    /**
     * The description of the option.
     */
    public final String description;

    /**
     * Constructor.
     *
     * @param id the index of the option
     * @param name the name of the option
     * @param description the description of the option
     */
    private OutputParameters(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns an array of the indexes of the different output options.
     *
     * @return an array of the indexes of the different output options
     */
    public static int[] getOutputOptions() {
        OutputParameters[] options = values();
        int[] result = new int[options.length];
        int i = 0;
        for (OutputParameters outputOption : options) {
            result[i] = outputOption.id;
            i++;
        }
        return result;
    }
    
    /**
     * Returns an array of the names of the different options.
     * 
     * @return an array of the names of the different options
     */
    public static String[] getOutputParametersNames() {
        OutputParameters[] options = values();
        String[] result = new String[options.length];
        int i = 0;
        for (OutputParameters outputOption : options) {
            result[i] = outputOption.name;
            i++;
        }
        return result;
    }

    /**
     * Returns the output option of the given index.
     *
     * @param id the index of the output option of interest
     *
     * @return the output option of interest
     */
    public static OutputParameters getOutputParameters(int id) {
        for (OutputParameters outputOption : values()) {
            if (outputOption.id == id) {
                return outputOption;
            }
        }
        return null;
    }
    
    /**
     * Convenience method returning all possibilities in a command line option description format.
     * 
     * @return all possibilities in a command line option description format
     */
    public static String getCommandLineOptions() {
        OutputParameters[] values = values();
        ArrayList<Integer> options = new ArrayList<>(values.length);
        for (OutputParameters option : values) {
            options.add(option.id);
        }
        Collections.sort(options);
        StringBuilder commandLine = new StringBuilder();
        for (int option : options) {
            if (commandLine.length() > 0) {
                commandLine.append(", ");
            }
            commandLine.append(option).append(": ").append(getOutputParameters(option).description);
        }
        return commandLine.toString();
    }
}
