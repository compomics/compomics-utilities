package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.out files.
 * 
 * Created March 2008
 * 
 * @author  Harald Barsnes
 */
public class OutFileFilter extends FileFilter {

    /**
     * Accept all directories, *.out files.
     *
     * @param f
     * @return boolean
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = FileFilterUtils.getExtension(f);
        if (extension != null) {
            if (extension.equals(FileFilterUtils.out) ||
                    extension.equals(FileFilterUtils.OUT)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * The description of this filter
     *
     * @return String
     */
    public java.lang.String getDescription() {
        return "*.out";
    }
}
