package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for sequest.params files.
 * 
 * Created February 2009
 * 
 * @author  Harald Barsnes
 */
public class SequestParamsFileFilter extends FileFilter {

    /**
     * Accept all directories, sequest.params files.
     *
     * @param f
     * @return boolean
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        return f.getName().toLowerCase().equalsIgnoreCase("sequest.params");
    }

    /**
     * The description of this filter
     *
     * @return String
     */
    public java.lang.String getDescription() {
        return "sequest.params";
    }
}
