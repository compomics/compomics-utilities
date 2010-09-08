package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.ms2 files.
 *
 * Created October 2008
 * 
 * @author  Harald Barsnes
 */
public class Ms2FileFilter extends FileFilter {
    
    /**
     * Accept all directories, *.ms2 files.
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
            if (extension.equals(FileFilterUtils.ms2)
                    || extension.equals(FileFilterUtils.MS2)){
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
        return "*.ms2";
    }
}