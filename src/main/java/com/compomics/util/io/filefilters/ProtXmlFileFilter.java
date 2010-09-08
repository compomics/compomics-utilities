package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.prot.xml, *.protxml and *.xml files.
 *
 * Created March 2008
 * 
 * @author  Harald Barsnes
 */
public class ProtXmlFileFilter extends FileFilter {
    
    /**
     * Accept all directories, *.prot.xml, *.protxml and *.xml files.
     *
     * @param f
     * @return boolean
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        
        if(f.getPath().endsWith(FileFilterUtils.prot_xml) || 
                f.getPath().endsWith(FileFilterUtils.PROT_XML) ||
                f.getPath().endsWith(FileFilterUtils.PROTXML) ||
                f.getPath().endsWith(FileFilterUtils.protxml) ||
                f.getPath().endsWith("." + FileFilterUtils.xml) ||
                f.getPath().endsWith("." + FileFilterUtils.XML)) {

            if (f.getPath().endsWith(FileFilterUtils.PEP_XML) ||
                    f.getPath().endsWith(FileFilterUtils.pep_xml) ||
                    f.getPath().endsWith(FileFilterUtils.PEPXML) ||
                    f.getPath().endsWith(FileFilterUtils.pepxml)) {
                return false;
            } else {
                return true;
            }
        } else{
            return false;
        }
    }
    
    /**
     * The description of this filter
     *
     * @return String
     */
    public java.lang.String getDescription() {
        return "*.prot.xml";
    }
}