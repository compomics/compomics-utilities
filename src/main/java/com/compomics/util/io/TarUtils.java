/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.io;

import com.compomics.util.gui.waiting.WaitingHandler;
import java.io.*;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * This class contains convenience methods for taring files
 *
 * @author Marc
 */
public class TarUtils {
    
    /**
     * Tar a given folder in a file.
     *
     * @param folder the original folder to tar
     * @param destinationFile the destination file
     * @param waitingHandler a waiting handler used to cancel the process (can be null)
     * @throws FileNotFoundException exception thrown whenever a file is not found
     * @throws ArchiveException exception thrown whenever an error occurred while taring
     * @throws IOException exception thrown whenever an error occurred while reading/writing files
     */
    public static void tarFolder(File folder, File destinationFile, WaitingHandler waitingHandler) throws FileNotFoundException, ArchiveException, IOException {
        FileOutputStream fos = new FileOutputStream(destinationFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        TarArchiveOutputStream tarOutput = (TarArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, bos);
        tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        File matchFolder = folder;
        addFolderContent(tarOutput, matchFolder, waitingHandler);
        tarOutput.close();
        bos.close();
        fos.close();
    }

    /**
     * Add content to the tar file.
     *
     * @param tarOutput the archive output stream
     * @param folder the folder to add
     * @param waitingHandler a waiting handler used to cancel the process (can be null)
     * @throws FileNotFoundException exception thrown whenever a file is not found
     * @throws IOException exception thrown whenever an error occurred while reading/writing files
     */
    public static void addFolderContent(ArchiveOutputStream tarOutput, File folder, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderContent(tarOutput, file, waitingHandler);
            } else {
                final int BUFFER = 2048;
                FileInputStream fi = new FileInputStream(file);
                BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
                byte data[] = new byte[BUFFER];
                TarArchiveEntry entry = new TarArchiveEntry(file);

                tarOutput.putArchiveEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    if (waitingHandler!= null && waitingHandler.isRunCanceled()) {
                        break;
                    }
                    tarOutput.write(data, 0, count);
                }
                tarOutput.closeArchiveEntry();
                origin.close();
                fi.close();
            }
        }
    }
}
