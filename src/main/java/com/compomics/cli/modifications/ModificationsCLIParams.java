package com.compomics.cli.modifications;

import com.compomics.software.cli.CommandLineUtils;
import org.apache.commons.cli.Options;

/**
 * Enum class specifying the ModificationsCLI parameters.
 *
 * @author Marc Vaudel
 */
public enum ModificationsCLIParams {

    //TODO: Add CvTerm, Reporter Ions and Neutral Losses
    IN("in", "An input file (.json).", false, true),
    LIST("l", "Lists all modifications implemented. All other options than " + IN.id + " will be ignored.", false, false),
    OUT("out", "The destination modifications file (.json).", true, true),
    RM("rm", "The name of a modification to remove.", false, true),
    TYPE("type", "The typr of the modification to add. 0: Modification at particular amino acids; 1: Modification at the N terminus of a protein; 2: Modification at the N terminus of a protein at particular amino acids; 3: Modification at the C terminus of a protein; 4: Modification at the C terminus of a protein at particular amino acids; 5: Modification at the N terminus of a peptide; 6: Modification at the N terminus of a peptide at particular amino acids; 7: Modification at the C terminus of a peptide; 8: Modification at the C terminus of a peptide at particular amino acids.", false, true),
    NAME("name", "The name of a modification to add.", false, true),
    SHORT_NAME("short_name", "The short name of the modification to add.", false, true),
    COMPOSITION_ADDED("composition_added", "The atomic composition of the modification to add.", false, true),
    COMPOSITION_REMOVED("composition_removed", "The atomic composition of the modification to remove.", false, true),
    PATTERN("pattern", "For modifications that target specific amino-acids, the pattern of amino-acids targetted. E.g. N[ACDEFGHIKLMNQRSTYUOVW][ST] for a modification targetting N followed by any amino acid but P followed by S or T.", false, true),
    PATTERN_INDEX("pattern_index", "For modifications targetting a pattern of amino-acids longer than one amino acid, the index on the patter where the modification is located, 0 being the first amino acid. E.g. 0 in N[ACDEFGHIKLMNQRSTYUOVW][ST] means that the modification is located on N, 2 means that the modification is located on S or T. 0 by default.", false, true);

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
    private ModificationsCLIParams(String id, String description, boolean mandatory, boolean hasArgument) {
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
        for (ModificationsCLIParams option : values()) {
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

        output += "\n\nRemove modification:\n";
        output += "-" + String.format(CommandLineUtils.formatter, RM.id) + " " + RM.description + "\n";

        output += "\n\nAdd modification:\n";
        output += "-" + String.format(CommandLineUtils.formatter, TYPE.id) + " " + TYPE.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, NAME.id) + " " + NAME.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, SHORT_NAME.id) + " " + SHORT_NAME.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, COMPOSITION_ADDED.id) + " " + COMPOSITION_ADDED.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, COMPOSITION_REMOVED.id) + " " + COMPOSITION_REMOVED.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, PATTERN.id) + " " + PATTERN.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, PATTERN_INDEX.id) + " " + PATTERN_INDEX.description + "\n";

        return output;
    }

}
