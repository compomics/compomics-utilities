package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.dat files.
 *
 * @author  Harald Barsnes
 */
public class DatFileFilter extends FileFilter {
    
    /**
     * Accept all directories, *.dat files.
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
            if (extension.equals(FileFilterUtils.dat)
                    || extension.equals(FileFilterUtils.DAT)){
                return true;
            } 
            else {
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
        return "*.dat";
    }
}