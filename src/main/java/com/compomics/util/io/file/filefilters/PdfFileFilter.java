package com.compomics.util.io.file.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.pdf files.
 *
 * @author Harald Barsnes
 */
public class PdfFileFilter extends FileFilter {

    /**
     * Empty default constructor
     */
    public PdfFileFilter() {
    }

    /**
     * Accept all directories, *.pdf files.
     *
     * @param f the file
     * @return true if the file passes the filter
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = FileFilterUtils.getExtension(f);
        if (extension != null) {
            if (extension.equals(FileFilterUtils.pdf)
                    || extension.equals(FileFilterUtils.PDF)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * The description of the filter.
     *
     * @return String the description of the filter
     */
    public java.lang.String getDescription() {
        return "*.pdf";
    }
}
