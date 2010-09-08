package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.mzXML files.
 * 
 * Created March 2008
 * 
 * @author  Harald Barsnes
 */

public class MzXmlFileFilter extends FileFilter {
    
    /**
     * Accept all directories, and *.mzXML files.
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
            
            if (extension.equals(FileFilterUtils.mzXML)
                    || extension.equals(FileFilterUtils.MZXML)
                    || extension.equals(FileFilterUtils.mzxml)){
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
        return "*.mzXML";
    }
}