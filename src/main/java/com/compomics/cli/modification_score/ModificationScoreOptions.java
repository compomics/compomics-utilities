package com.compomics.cli.modification_score;

import com.compomics.software.cli.CommandLineUtils;
import org.apache.commons.cli.Options;

/**
 * Enum class specifying the ModificationsCLI parameters.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum ModificationScoreOptions {

    IN("i", "The file containing the peptides. See documentation for details.", true, true),
    LOG("l", "The file to write the log to. Defualt: next to output file.", false, true),
    OUT("o", "The where to write the results. See documentation for details.", true, true);

    /**
     * Short Id for the CLI parameter.
     */
    public final String id;
    /**
     * Explanation for the CLI parameter.
     */
    public final String description;
    /**
     * Boolean indicating whether the parameter is mandatory.
     */
    public final boolean mandatory;
    /**
     * Boolean indicating whether this command line option needs an argument.
     */
    public final boolean hasArgument;

    /**
     * Private constructor managing the various variables for the enum
     * instances.
     *
     * @param id the id
     * @param description the description
     * @param mandatory is the parameter mandatory
     * @param hasArgument boolean indicating whether this command line option
     * needs an argument
     */
    private ModificationScoreOptions(
            String id,
            String description,
            boolean mandatory,
            boolean hasArgument
    ) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
        this.hasArgument = hasArgument;
    }

    /**
     * Creates the options for the command line interface based on the possible
     * values.
     *
     * @param aOptions the options object where the options will be added
     */
    public static void createOptionsCLI(
            Options aOptions
    ) {

        for (ModificationScoreOptions option : values()) {

            aOptions.addOption(
                    option.id,
                    option.hasArgument,
                    option.description
            );

        }
    }

    /**
     * Returns the options as a string.
     *
     * @return the options as a string
     */
    public static String getOptionsAsString() {

        String output = "";

        output += "Mandatory arguments:\n";

        for (ModificationScoreOptions option : values()) {

            if (option.mandatory) {

                output += "-" + String.format(CommandLineUtils.FORMATTER, option.id) + " " + option.description + "\n";

            }
        }

        output += "\n\nOptional arguments:\n";

        for (ModificationScoreOptions option : values()) {

            if (option.mandatory) {

                output += "-" + String.format(CommandLineUtils.FORMATTER, option.id) + " " + option.description + "\n";

            }
        }

        return output;
    }
}
