package com.compomics.util.experiment.io.identifications;

/**
 * Enum of the versions of the mzIdentML format supported.
 *
 * @author Marc Vaudel
 */
public enum MzIdentMLVersion {

    v1_1("v1.1", "Version 1.1"),
    v1_2("v1.2", "Version 1.2");

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
     * @param name the name of the option
     * @param description the description of the option
     */
    private MzIdentMLVersion(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the mzIdentML format corresponding to the given index.
     *
     * @param index the index
     *
     * @return the mzIdentML format corresponding to the given index
     */
    public static MzIdentMLVersion getMzIdentMLVersion(int index) {
        MzIdentMLVersion[] versions = values();
        if (index < 0 || index >= versions.length) {
            throw new IllegalArgumentException("No mzIdentML version found for index " + index + ".");
        }
        return values()[index];
    }

    /**
     * Returns a string describing the different options.
     *
     * @return a string describing the different options
     */
    public static String getCommandLineOptions() {

        StringBuilder stringBuilder = new StringBuilder();
        MzIdentMLVersion[] versions = values();
        stringBuilder.append(0).append(": ").append(versions[0].name);
        for (int i = 1; i < versions.length; i++) {
            stringBuilder.append(", ").append(i).append(": ").append(versions[i].name);
        }
        return stringBuilder.toString();
    }
}
