package com.compomics.util.io.compression;

import com.compomics.util.waiting.WaitingHandler;
import java.io.*;
import java.util.HashSet;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * This class contains convenience methods for taring files.
 *
 * @author Marc Vaudel
 */
public class TarUtils {

    /**
     * Empty default constructor
     */
    public TarUtils() {
    }

    /**
     * Tar a given folder to a file.
     *
     * @param folder the original folder to tar
     * @param destinationFile the destination file
     * @param waitingHandler a waiting handler used to cancel the process (can
     * be null)
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     * @throws ArchiveException exception thrown whenever an error occurred
     * while taring
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    public static void tarFolder(File folder, File destinationFile, WaitingHandler waitingHandler) throws FileNotFoundException, ArchiveException, IOException {
        FileOutputStream fos = new FileOutputStream(destinationFile);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try {
                TarArchiveOutputStream tarOutput = (TarArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, bos);
                try {
                    tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                    File matchFolder = folder;
                    addFolderContent(tarOutput, matchFolder, waitingHandler);
                } finally {
                    tarOutput.close();
                }
            } finally {
                bos.close();
            }
        } finally {
            fos.close();
        }
    }

    /**
     * Tar the content of a given folder to a file.
     *
     * @param folder the original folder to tar
     * @param destinationFile the destination file
     * @param exceptionsPaths a list of paths to files or folders which should be excluded from taring
     * @param waitingHandler a waiting handler used to cancel the process (can
     * be null)
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     * @throws ArchiveException exception thrown whenever an error occurred
     * while taring
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    public static void tarFolderContent(File folder, File destinationFile, HashSet<String> exceptionsPaths, WaitingHandler waitingHandler) throws FileNotFoundException, ArchiveException, IOException {
        FileOutputStream fos = new FileOutputStream(destinationFile);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try {
                TarArchiveOutputStream tarOutput = (TarArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, bos);
                try {
                    tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                    for (File file : folder.listFiles()) {
                        String path = file.getAbsolutePath();
                        if (!exceptionsPaths.contains(path)) {
                            if (file.isDirectory()) {
                                addFolderContent(tarOutput, file, waitingHandler);
                            } else {
                                addFile(tarOutput, file, waitingHandler);
                            }
                        }
                    }
                } finally {
                    tarOutput.close();
                }
            } finally {
                bos.close();
            }
        } finally {
            fos.close();
        }
    }

    /**
     * Add content to the tar file.
     *
     * @param tarOutput the archive output stream
     * @param folder the folder to add
     * @param waitingHandler a waiting handler used to cancel the process (can
     * be null)
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    public static void addFolderContent(ArchiveOutputStream tarOutput, File folder, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {
        addFolderContent(tarOutput, folder, null, waitingHandler);
    }

    /**
     * Add content to the tar file.
     *
     * @param tarOutput the archive output stream
     * @param folder the folder to add
     * @param parentFolder the parent folder to remove from the folder path
     * @param waitingHandler a waiting handler used to cancel the process (can
     * be null)
     *
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    private static void addFolderContent(ArchiveOutputStream tarOutput, File folder, String parentFolder, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

        if (parentFolder == null) {
            parentFolder = folder.getParentFile().getAbsolutePath();
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderContent(tarOutput, file, parentFolder, waitingHandler);
            } else {
                addFile(tarOutput, file, parentFolder, waitingHandler);
            }
        }
    }

    /**
     * Add content to the tar file.
     *
     * @param tarOutput the archive output stream
     * @param file the file to add
     * @param waitingHandler a waiting handler used to cancel the process (can
     * be null)
     *
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    private static void addFile(ArchiveOutputStream tarOutput, File file, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {
        addFile(tarOutput, file, null, waitingHandler);
    }

    /**
     * Add content to the tar file.
     *
     * @param tarOutput the archive output stream
     * @param file the file to add
     * @param parentFolder the parent folder to remove from the folder path
     * @param waitingHandler a waiting handler used to cancel the process (can
     * be null)
     *
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    private static void addFile(ArchiveOutputStream tarOutput, File file, String parentFolder, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

        if (parentFolder == null) {
            parentFolder = file.getParentFile().getAbsolutePath();
        }
        final int BUFFER = 2048;
        FileInputStream fi = new FileInputStream(file);
        try {
            BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
            try {
                byte data[] = new byte[BUFFER];
                String filePath = file.getAbsolutePath();
                String relativePath = filePath.substring(parentFolder.length() + 1);
                TarArchiveEntry entry = new TarArchiveEntry(file, relativePath);

                tarOutput.putArchiveEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        break;
                    }
                    tarOutput.write(data, 0, count);
                }
                tarOutput.closeArchiveEntry();
            } finally {
                origin.close();
            }
        } finally {
            fi.close();
        }
    }

    /**
     * Extracts files from a tar.
     *
     * @param tarFile the tar file
     * @param waitingHandler a waiting handler displaying progress and allowing
     * canceling the process
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ArchiveException if an ArchiveException occurs
     * @throws IOException if an IOException occurs
     */
    public static void extractFile(File tarFile, WaitingHandler waitingHandler) throws FileNotFoundException, ArchiveException, IOException {
        extractFile(tarFile, null, waitingHandler);
    }

    /**
     * Extracts files from a tar.
     *
     * @param tarFile the tar file
     * @param destinationFolder the destination folder, if null the file will be
     * extracted according to the archive name
     * @param waitingHandler a waiting handler displaying progress and allowing
     * canceling the process
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ArchiveException if an ArchiveException occurs
     * @throws IOException if an IOException occurs
     */
    public static void extractFile(File tarFile, File destinationFolder, WaitingHandler waitingHandler) throws FileNotFoundException, ArchiveException, IOException {

        final int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        FileInputStream fi = new FileInputStream(tarFile);

        try {
            BufferedInputStream bis = new BufferedInputStream(fi, BUFFER);
            boolean isWindowsPlatform = (System.getProperty("os.name").lastIndexOf("Windows") == -1);
            try {
                ArchiveInputStream tarInput = new ArchiveStreamFactory().createArchiveInputStream(bis);
                try {
                    long fileLength = tarFile.length();

                    ArchiveEntry archiveEntry;

                    while ((archiveEntry = tarInput.getNextEntry()) != null) {

                        String entryName = archiveEntry.getName();

                        // dirty fix to be able to open windows cps files on linux/mac and the other way around
                        if (isWindowsPlatform) {
                            entryName = entryName.replaceAll("\\\\", "/");
                        } else {
                            entryName = entryName.replaceAll("/", "\\\\");
                        }

                        File entryFile = new File(entryName);
                        File entryFolder;
                        if (destinationFolder == null) {
                            entryFolder = entryFile.getParentFile();
                        } else {
                            entryFolder = (new File(destinationFolder, entryName)).getParentFile();
                        }
                        File destinationFile = new File(entryFolder, entryFile.getName());

                        if (archiveEntry.isDirectory()) {
                            destinationFile.mkdirs();
                        } else if (entryFolder.exists() || entryFolder.mkdirs()) {
                            FileOutputStream fos = new FileOutputStream(destinationFile);
                            try {
                                BufferedOutputStream bos = new BufferedOutputStream(fos);
                                try {
                                    int count;

                                    while ((count = tarInput.read(data, 0, BUFFER)) != -1) {
                                        bos.write(data, 0, count);
                                        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                                            break;
                                        }
                                    }
                                } finally {
                                    bos.close();
                                }
                            } finally {
                                fos.close();
                            }
                            if (waitingHandler != null) {
                                int progress = (int) (100 * tarInput.getBytesRead() / fileLength);
                                waitingHandler.setSecondaryProgressCounter(progress);
                            }
                        } else {
                            throw new IOException("Folder " + destinationFolder.getAbsolutePath()
                                    + " does not exist and could not be created. "
                                    + "Verify that you have the right to write in this directory.");
                        }
                        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                            break;
                        }
                    }
                } finally {
                    tarInput.close();
                }
            } finally {
                bis.close();
            }
        } finally {
            fi.close();
        }
    }
}
