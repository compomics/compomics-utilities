package com.compomics.software.autoupdater;

import java.io.File;
import java.io.IOException;

/**
 * HeadlessFileDAO.
 *
 * @author Davy Maddelein
 */
public class HeadlessFileDAO extends FileDAO {

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean createDesktopShortcut(MavenJarFile file, String iconName, boolean deleteOldShortcut) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // @TODO: implement me...
    }

    /**
     *
     * {@inheritDoc }
     */
    @Override
    public File getLocationToDownloadOnDisk(String targetDownloadFolder) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // @TODO: implement me...
    }
}
