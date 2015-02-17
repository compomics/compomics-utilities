package com.compomics.util;

import java.io.File;

/**
 * Store the selected file and the file type description.
 *
 * @author Harald Barsnes
 */
public class FileAndFileFilter {

    /**
     * The selected file.
     */
    private File file;
    /**
     * The file filter used.
     */
    private javax.swing.filechooser.FileFilter fileFilter;

    /**
     * Create a new FileAndFileFilter object.
     *
     * @param file the selected file
     * @param fileFilter the file filter
     */
    public FileAndFileFilter(File file, javax.swing.filechooser.FileFilter fileFilter) {
        this.file = file;
        this.fileFilter = fileFilter;
    }

    /**
     * Returns the file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the file.
     *
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Returns the file filter.
     *
     * @return the fileFilter
     */
    public javax.swing.filechooser.FileFilter getFileFilter() {
        return fileFilter;
    }

    /**
     * Set the file filter.
     *
     * @param fileFilter the fileFilter to set
     */
    public void setFileFilter(javax.swing.filechooser.FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }
}
