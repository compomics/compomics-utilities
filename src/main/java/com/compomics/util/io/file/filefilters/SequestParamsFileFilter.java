package com.compomics.util.io.file.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for sequest.params files.
 *
 * @author Harald Barsnes
 */
public class SequestParamsFileFilter extends FileFilter {

    /**
     * Empty default constructor
     */
    public SequestParamsFileFilter() {
    }

    /**
     * Accept all directories, sequest.params files.
     *
     * @param f the file
     * @return true if the file passes the filter
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        return f.getName().toLowerCase().equalsIgnoreCase("sequest.params");
    }

    /**
     * The description of the filter.
     *
     * @return String the description of the filter
     */
    public java.lang.String getDescription() {
        return "sequest.params";
    }
}
