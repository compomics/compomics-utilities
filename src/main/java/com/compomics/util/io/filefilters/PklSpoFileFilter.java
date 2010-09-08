package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.pkl.spo files.
 * 
 * Created March 2008
 * 
 * @author  Harald Barsnes
 */
public class PklSpoFileFilter extends FileFilter {

    /**
     * Accept all directories, *.pkl.spo files.
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
            if (extension.equals(FileFilterUtils.spo) || extension.equals(FileFilterUtils.SPO)) {
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
        return "*.pkl.spo";
    }
}