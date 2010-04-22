package com.compomics.util.enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: kenny
 * Date: Feb 23, 2010
 * Time: 12:58:32 PM
 * <p/>
 * This enum types all computational omics tools and libraries that make use of the utilities library.
 */
public enum CompomicsTools {
    MSLIMS("mslims", "com.compomics.lims"),
    PEPTIZER("peptizer", "com.compomics.peptizer"),
    ROVER("rover", "com.compomics.rover"),
    MASCOTDATFILE("mascotdatfile", "com.compomics.mascotdatfile");

    /**
     * A name for each tool or library.
     */
    private String iName;

    /**
     * The package name of each tool.
     */
    private String iPackageName;

    /**
     * Private constructor to initialize variables for each type.
     * @param aName The identifying name for the enum type.
     */
    private CompomicsTools(String aName, String aPackageName) {
        iName = aName;
        iPackageName = aPackageName;
    }

    /**
     * Getter for the name of the tool or library.
     * @return The name of the tool or library.
     */
    public String getName() {
        return iName;
    }

    /**
     * Return the package name of the tool.
     * @return
     */
    public String getPackageName() {
        return iPackageName;
    }
}
