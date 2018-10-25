package com.compomics.util.experiment.identification.peptide_fragmentation;

/**
 * Enum for the different peptide fragmentation models implemented.
 *
 * @author Marc Vaudel
 */
public enum PeptideFragmentationModel {

    uniform(0, "uniform", "Returns one for every peak."),
    sequest(1, "sequest", "Original sequest CID intensity model.");

    /**
     * The index of the option.
     */
    public final int index;
    /**
     * The name of the option.
     */
    public final String name;
    /**
     * The description of the option.
     */
    public final String description;

    /**
     * Constructor.
     *
     * @param index the index of the option
     * @param name the name of the option
     * @param description the description of the option
     */
    private PeptideFragmentationModel(int index, String name, String description) {
        this.index = index;
        this.name = name;
        this.description = description;
    }

    /**
     * Empty default constructor
     */
    private PeptideFragmentationModel() {
        index = 0;
        name = "";
        description = "";
    }
}
