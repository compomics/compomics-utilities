package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.jpeg files.
 *
 * @author  Harald Barsnes
 */
public class JpegFileFilter extends FileFilter {
    
    /**
     * Accept all directories, *.jpeg files.
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
            if (extension.equals(FileFilterUtils.jpg)
                    || extension.equals(FileFilterUtils.JPG)
                    || extension.equals(FileFilterUtils.jpeg)
                    || extension.equals(FileFilterUtils.JPEG)){
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
        return "*.jpg";
    }
}