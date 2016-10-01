package com.compomics.util.experiment.identification.parameters_cli;

/**
 * Enum class specifying the EnzymeCLI parameters.
 *
 * @author Marc Vaudel
 */
public enum EnzymesCLIParams {
    
    IN("in", "An input file (.json).", false, true),
    OUT("out", "The destination enzymes file (.json).", true, true),
    
    RM("rm", "The name of an enzyme to remove.", false, true),
    
    NAME("name", "The name of an enzyme to add.", false, true),
    RESTRICTION_BEFORE("restriction_before", "Comma separated list of amino acids forbidden before the cleavage site. e.g. \"S,T\"", false, true),
    RESTRICTION_AFTER("restriction_after", "Comma separated list of amino acids forbidden after the cleavage site. e.g. \"S,T\"", false, true),
    CLEAVE_BEFORE("cleave_before", "Comma separated list of amino acids present before the cleavage site. e.g. \"R,K\"", false, true),
    CLEAVE_AFTER("cleave_after", "Comma separated list of amino acids present afterthe cleavage site. e.g. \"R,K\"", false, true),;
    
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
     * @param hasArgument boolean indicating whether this command line option needs an argument
     */
    private EnzymesCLIParams(String id, String description, boolean mandatory, boolean hasArgument) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
        this.hasArgument = hasArgument;
    }

}
