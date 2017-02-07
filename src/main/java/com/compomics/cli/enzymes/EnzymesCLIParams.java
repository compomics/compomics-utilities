package com.compomics.cli.enzymes;

import com.compomics.software.cli.CommandLineUtils;
import org.apache.commons.cli.Options;

/**
 * Enum class specifying the EnzymesCLI parameters.
 *
 * @author Marc Vaudel
 */
public enum EnzymesCLIParams {

    IN("in", "An input file (.json).", false, true),
    LIST("l", "Lists all enzymes implemented. All other options than " + IN.id + " will be ignored.", false, false),
    OUT("out", "The destination enzymes file (.json).", true, true),
    RM("rm", "The name of an enzyme to remove.", false, true),
    NAME("name", "The name of an enzyme to add.", false, true),
    RESTRICTION_BEFORE("restriction_before", "Comma separated list of amino acids forbidden before the cleavage site of the enzyme to add. e.g. \"S,T\"", false, true),
    RESTRICTION_AFTER("restriction_after", "Comma separated list of amino acids forbidden after the cleavage site of the enzyme to add. e.g. \"S,T\"", false, true),
    CLEAVE_BEFORE("cleave_before", "Comma separated list of amino acids present before the cleavage site of the enzyme to add. e.g. \"R,K\"", false, true),
    CLEAVE_AFTER("cleave_after", "Comma separated list of amino acids present afterthe cleavage site of the enzyme to add. e.g. \"R,K\"", false, true),;

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
    private EnzymesCLIParams(String id, String description, boolean mandatory, boolean hasArgument) {
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
    public static void createOptionsCLI(Options aOptions) {
        for (EnzymesCLIParams option : values()) {
            aOptions.addOption(option.id, option.hasArgument, option.description);
        }
    }

    /**
     * Returns the options as a string.
     *
     * @return the options as a string
     */
    public static String getOptionsAsString() {

        String output = "";

        output += "Input-Output:\n\n";
        output += "-" + String.format(CommandLineUtils.formatter, IN.id) + " " + IN.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, OUT.id) + " " + OUT.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, LIST.id) + " " + LIST.description + "\n";

        output += "\n\nRemove enzyme:\n";
        output += "-" + String.format(CommandLineUtils.formatter, RM.id) + " " + RM.description + "\n";

        output += "\n\nAdd enzyme:\n";
        output += "-" + String.format(CommandLineUtils.formatter, NAME.id) + " " + NAME.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, CLEAVE_BEFORE.id) + " " + CLEAVE_BEFORE.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, CLEAVE_AFTER.id) + " " + CLEAVE_AFTER.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, RESTRICTION_BEFORE.id) + " " + RESTRICTION_BEFORE.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, RESTRICTION_AFTER.id) + " " + RESTRICTION_AFTER.description + "\n";

        return output;
    }

}
