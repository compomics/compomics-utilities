
package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.filechooser.*;


/**
 * File filter for *.mzData files.
 * 
 * Created March 2008
 * 
 * @author  Harald Barsnes
 */

public class MzDataFileFilter extends FileFilter {
    
    /**
     * Accept all directories, and *.mzData files.
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
            
            if (extension.equals(FileFilterUtils.mzData)
                    || extension.equals(FileFilterUtils.MZDATA)
                    || extension.equals(FileFilterUtils.mzdata)
                    || extension.equals(FileFilterUtils.mzDATA)){
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
        return "*.mzData";
    }
}