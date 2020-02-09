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
     * The buffer size.
     */
    public static final int BUFFER_SIZE = 1024;

    /**
     * Empty default constructor
     */
    public TarUtils() {
    }

    /**
     * Tar a given folder to a file.
     *
     * @param folder The original folder to tar.
     * @param destinationFile The destination file.
     * @param waitingHandler A waiting handler used to cancel the process (can
     * be null).
     *
     * @throws ArchiveException Thrown whenever an error occurred while taring.
     * @throws IOException Thrown whenever an error occurred while
     * reading/writing files.
     */
    public static void tarFolder(
            File folder,
            File destinationFile,
            WaitingHandler waitingHandler
    ) throws ArchiveException, IOException {

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destinationFile));

        try ( TarArchiveOutputStream tarOutput = (TarArchiveOutputStream) new ArchiveStreamFactory()
                .createArchiveOutputStream(
                        ArchiveStreamFactory.TAR,
                        bos
                )) {
            tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            File matchFolder = folder;
            addFolderContent(tarOutput, matchFolder, waitingHandler);

        }
    }

    /**
     * Tar the content of a given folder to a file.
     *
     * @param folder the original folder to tar
     * @param destinationFile the destination file
     * @param exceptionsPaths a list of paths to files or folders which should
     * be excluded from taring
     * @param waitingHandler a waiting handler used to cancel the process (can
     * be null)
     *
     * @throws ArchiveException exception thrown whenever an error occurred
     * while taring
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    public static void tarFolderContent(
            File folder,
            File destinationFile,
            HashSet<String> exceptionsPaths,
            WaitingHandler waitingHandler
    ) throws ArchiveException, IOException {

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destinationFile));

        try ( TarArchiveOutputStream tarOutput = (TarArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, bos)) {

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
        }
    }

    /**
     * Add content to the tar file.
     *
     * @param tarOutput the archive output stream
     * @param folder the folder to add
     * @param waitingHandler a waiting handler used to cancel the process (can
     * be null)
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    public static void addFolderContent(
            ArchiveOutputStream tarOutput,
            File folder,
            WaitingHandler waitingHandler
    ) throws IOException {

        addFolderContent(
                tarOutput,
                folder,
                null,
                waitingHandler
        );
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
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    private static void addFolderContent(
            ArchiveOutputStream tarOutput,
            File folder,
            String parentFolder,
            WaitingHandler waitingHandler
    ) throws IOException {

        if (parentFolder == null) {

            parentFolder = folder.getParentFile().getAbsolutePath();

        }

        for (File file : folder.listFiles()) {

            if (file.isDirectory()) {

                addFolderContent(
                        tarOutput,
                        file,
                        parentFolder,
                        waitingHandler
                );

            } else {

                addFile(
                        tarOutput,
                        file,
                        parentFolder,
                        waitingHandler
                );

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
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    private static void addFile(
            ArchiveOutputStream tarOutput,
            File file,
            WaitingHandler waitingHandler
    ) throws IOException {

        addFile(
                tarOutput,
                file,
                null,
                waitingHandler
        );
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
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing files
     */
    private static void addFile(
            ArchiveOutputStream tarOutput,
            File file,
            String parentFolder,
            WaitingHandler waitingHandler
    ) throws IOException {

        if (parentFolder == null) {

            parentFolder = file.getParentFile().getAbsolutePath();

        }
        try ( BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {

            byte data[] = new byte[BUFFER_SIZE];

            String filePath = file.getAbsolutePath();
            String relativePath = filePath.substring(parentFolder.length() + 1);
            TarArchiveEntry entry = new TarArchiveEntry(file, relativePath);

            tarOutput.putArchiveEntry(entry);

            int count;
            while ((count = input.read(data, 0, BUFFER_SIZE)) != -1) {

                tarOutput.write(data, 0, count);

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    break;
                }
            }

            tarOutput.closeArchiveEntry();

        }
    }

    /**
     * Extracts files from a tar.
     *
     * @param tarFile the tar file
     * @param waitingHandler a waiting handler displaying progress and allowing
     * canceling the process
     *
     * @throws ArchiveException if an ArchiveException occurs
     * @throws IOException if an IOException occurs
     */
    public static void extractFile(
            File tarFile,
            WaitingHandler waitingHandler
    ) throws ArchiveException, IOException {

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
     * @throws ArchiveException if an ArchiveException occurs
     * @throws IOException if an IOException occurs
     */
    public static void extractFile(
            File tarFile,
            File destinationFolder,
            WaitingHandler waitingHandler
    ) throws ArchiveException, IOException {

        byte data[] = new byte[BUFFER_SIZE];

        try ( BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tarFile))) {

            boolean isWindowsPlatform = (System.getProperty("os.name").lastIndexOf("Windows") == -1);

            try ( ArchiveInputStream tarInput = new ArchiveStreamFactory().createArchiveInputStream(bis)) {

                long fileLength = tarFile.length();

                ArchiveEntry archiveEntry;
                while ((archiveEntry = tarInput.getNextEntry()) != null) {

                    String entryName = archiveEntry.getName();

                    // Fix to be able to open windows files on linux/mac and the other way around
                    if (isWindowsPlatform) {
                        entryName = entryName.replaceAll("\\\\", "/");
                    } else {
                        entryName = entryName.replaceAll("/", "\\\\");
                    }

                    File entryFile = new File(entryName);
                    File entryFolder = destinationFolder == null
                            ? entryFile.getParentFile()
                            : (new File(destinationFolder, entryName)).getParentFile();
                    File destinationFile = new File(entryFolder, entryFile.getName());

                    if (archiveEntry.isDirectory()) {

                        destinationFile.mkdirs();

                    } else if (entryFolder.exists() || entryFolder.mkdirs()) {

                        try ( BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destinationFile))) {

                            int count;
                            while ((count = tarInput.read(data, 0, BUFFER_SIZE)) != -1) {

                                bos.write(data, 0, count);

                                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                                    break;
                                }
                            }
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
            }
        }
    }
}
