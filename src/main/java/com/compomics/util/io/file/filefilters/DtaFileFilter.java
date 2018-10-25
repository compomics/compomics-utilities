package com.compomics.util.io.file.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.dta files.
 *
 * @author Harald Barsnes
 */
public class DtaFileFilter extends FileFilter {

    /**
     * Empty default constructor
     */
    public DtaFileFilter() {
    }

    /**
     * Accept all directories, *.dta files.
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
            if (extension.equals(FileFilterUtils.dta)
                    || extension.equals(FileFilterUtils.DTA)) {
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
        return "*.dta";
    }
}
