package com.compomics.util.experiment.io.mass_spectrometry.cms;

/**
 * Placeholder for the temp folder to use for cms files.
 *
 * @author Marc Vaudel
 */
public class CmsFolder {

    /**
     * The folder to use when creating cms files.
     */
    private static String parentFolder = null;
    /**
     * The sub folder where the cms files should be stored.
     */
    private static final String SUB_FOLDER = ".cms_temp";

    /**
     * Returns the parent folder where to write cms files. Null if not set.
     *
     * @return The parent folder where to write cms files.
     */
    public static String getParentFolder() {
        return parentFolder;
    }

    /**
     * Returns the sub-folder where to write cms files. Null if not set.
     *
     * @return the sub-folder where to write cms files
     */
    public static String getSubFolder() {
        return SUB_FOLDER;
    }

    /**
     * Sets the parent folder where to write cms files to.
     *
     * @param newParentFolder The parent folder where to write cms files to.
     */
    public static void setParentFolder(
            String newParentFolder
    ) {
        parentFolder = newParentFolder;
    }

}
