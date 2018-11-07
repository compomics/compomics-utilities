package com.compomics.util.io.file.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.prot.xml, *.protxml and *.xml files.
 *
 * @author Harald Barsnes
 */
public class ProtXmlFileFilter extends FileFilter {

    /**
     * Empty default constructor
     */
    public ProtXmlFileFilter() {
    }

    /**
     * Accept all directories, *.prot.xml, *.protxml and *.xml files.
     *
     * @param f the file
     * @return true if the file passes the filter
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        if (f.getPath().endsWith(FileFilterUtils.prot_xml)
                || f.getPath().endsWith(FileFilterUtils.PROT_XML)
                || f.getPath().endsWith(FileFilterUtils.PROTXML)
                || f.getPath().endsWith(FileFilterUtils.protxml)
                || f.getPath().endsWith("." + FileFilterUtils.xml)
                || f.getPath().endsWith("." + FileFilterUtils.XML)) {

            if (f.getPath().endsWith(FileFilterUtils.PEP_XML)
                    || f.getPath().endsWith(FileFilterUtils.pep_xml)
                    || f.getPath().endsWith(FileFilterUtils.PEPXML)
                    || f.getPath().endsWith(FileFilterUtils.pepxml)) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * The description of the filter.
     *
     * @return String the description of the filter
     */
    public java.lang.String getDescription() {
        return "*.prot.xml";
    }
}
