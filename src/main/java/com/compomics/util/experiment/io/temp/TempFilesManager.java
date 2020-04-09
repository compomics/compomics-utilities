package com.compomics.util.experiment.io.temp;

import com.compomics.util.io.IoUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class can be used to reference temp files and manage them.
 *
 * @author Marc Vaudel
 */
public class TempFilesManager {

    /**
     * List of the temp folders created using this manager.
     */
    private static final ArrayList<File> TEMP_FOLDERS_MAP = new ArrayList<File>(0);

    /**
     * Adds a temp folder to the references temp folders.
     *
     * @param tempFolder the temp folder to register
     */
    public static void registerTempFolder(
            File tempFolder
    ) {
        
        TEMP_FOLDERS_MAP.add(tempFolder);
 
    }

    /**
     * Deletes the temp folders created.
     *
     * @throws IOException if an IOException occurs
     */
    public static void deleteTempFolders() throws IOException {
        
        ArrayList<String> exceptions = new ArrayList<>();
        
        for (File tempFolder : TEMP_FOLDERS_MAP) {

            try {

                if (tempFolder.exists()) {

                    boolean success = IoUtil.deleteDir(tempFolder); // @TODO: what the file could not be deleted?

                }

            } catch (Exception e) {

                e.printStackTrace();
                exceptions.add(tempFolder.getAbsolutePath());

            }
        }
        if (!exceptions.isEmpty()) {

            String error = "An error occurred while attempting to delete the following temporary folder(s):\n";

            for (String filePath : exceptions) {

                error += filePath + "\n";

            }

            throw new IOException(error);

        }
    }
}
