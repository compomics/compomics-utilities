package com.compomics.software.autoupdater;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Davy
 */
public class JarFileFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        boolean accept = false;
        if (dir.getName().contains(name)) {
            accept = true;
        }
        return accept;
    }
}
