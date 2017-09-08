/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-nov-02
 * Time: 16:11:33
 */
package com.compomics.util.io.file;
import org.apache.log4j.Logger;

import javax.swing.filechooser.FileFilter;
import java.io.FilenameFilter;
import java.io.File;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class will implement a FilenameFilter that filters on the extension of files.
 *
 * @author Lennart Martens
 */
public class FilenameExtensionFilter extends FileFilter implements FilenameFilter {

    // Class specific log4j logger for FilenameExtensionFilter instances.
    Logger logger = Logger.getLogger(FilenameExtensionFilter.class);

    /**
     * This is the extension to filter on.
     */
    private String iExtension = null;

    /**
     * This is the description for the FileFilter.
     */
    private String iDescription = "";

    /**
     * This constructor takes an extension to filter on. It doesn't care about
     * a leading dot, so specifying '.class' is identical to 'class'.
     *
     * @param   aExtension  String with the extension to filter. Note that
     *                      '.class' is identical to 'class'.
     */
    public FilenameExtensionFilter(String aExtension) {
        this(aExtension, null);
    }

    /**
     * This constructor takes an extension to filter on. It doesn't care about
     * a leading dot, so specifying '.class' is identical to 'class'.
     *
     * @param   aExtension  String with the extension to filter. Note that
     *                      '.class' is identical to 'class'.
     * @param aDescription String with the description for the FileFilter.
     *                     Can be null in which case the description will be an empty String.
     *
     */
    public FilenameExtensionFilter(String aExtension, String aDescription) {
        // Removing leading '*', if present.
        if(aExtension.startsWith("*")) {
            aExtension = aExtension.substring(1);
        }

        // Adding leading dot, if not yet present.
        if(!aExtension.startsWith(".")) {
            aExtension = "." + aExtension;
        }

        this.iExtension = aExtension;
        if(aDescription != null){
            this.iDescription = aDescription;
        }
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param   dir    the directory in which the file was found.
     * @param   name   the name of the file.
     * @return  <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
    public boolean accept(File dir, String name) {
        boolean result = false;

        if((!(new File(dir, name).isDirectory())) && (name.endsWith(iExtension))) {
            result = true;
        }

        return result;
    }

    @Override
    public boolean accept(File f) {
        return accept(f.getParentFile(), f.getName());
    }

    @Override
    public String getDescription() {
        return iDescription;
    }
}
