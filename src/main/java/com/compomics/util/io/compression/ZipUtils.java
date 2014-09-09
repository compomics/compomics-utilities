package com.compomics.util.io.compression;

import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Convenience class for the handling of zip files.
 *
 * @author Marc Vaudel
 */
public class ZipUtils {

    /**
     * The buffer size.
     */
    private static final int BUFFER = 2048;

    /**
     * Zips a file.
     *
     * @param originFile the file to zip, can be a folder
     * @param destinationFile the destination file
     * @param waitingHandler a waiting handler allowing canceling the process
     * (can be null)
     *
     * @throws IOException
     */
    public static void zip(File originFile, File destinationFile, WaitingHandler waitingHandler) throws IOException {

        FileOutputStream fos = new FileOutputStream(destinationFile);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try {
                ZipOutputStream out = new ZipOutputStream(bos);
                try {
                    addToZip(originFile, out, waitingHandler);
                } finally {
                    out.close();
                }
            } finally {
                bos.close();
            }
        } finally {
            fos.close();
        }
    }

    /**
     * Adds a new file to the zip stream. If the file is a folder it will be
     * added as well
     *
     * @param file the file to add to the zip
     * @param out the zip stream
     * @param waitingHandler a waiting handler allowing canceling the process
     * (can be null)
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addToZip(File file, ZipOutputStream out, WaitingHandler waitingHandler) throws IOException {
        addToZip(file, "", out, waitingHandler);
    }

    /**
     * Adds a new file to the zip stream. If the file is a folder it will be
     * added as well.
     *
     * @param subDirectory the subdirectory relative to the zip file location
     * (e.g. "data", note that there is no tailing "/")
     * @param file the file to add to the zip
     * @param out the zip stream
     * @param waitingHandler a waiting handler allowing canceling the process
     * (can be null)
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addToZip(File file, String subDirectory, ZipOutputStream out, WaitingHandler waitingHandler) throws IOException {
        if (file.isDirectory()) {
            String directory = subDirectory;
            if (!subDirectory.equals("")) {
                directory += "/";
            }
            directory += file.getName();
            addFolderToZip(directory, out);
            for (File subFile : file.listFiles()) {
                addToZip(subFile, subDirectory, out, waitingHandler);
            }
        } else {
            addFileToZip(subDirectory, file, out, waitingHandler);
        }
    }

    /**
     * Adds a new file to the zip stream. The file should not be a folder.
     *
     * @param file the file to add to the zip
     * @param out the zip stream
     * @param waitingHandler a waiting handler allowing canceling the process
     * (can be null)
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addFileToZip(File file, ZipOutputStream out, WaitingHandler waitingHandler) throws IOException {
        addFileToZip("", file, out, waitingHandler);
    }

    /**
     * Adds a new file to the zip stream. The file should not be a folder.
     *
     * @param subDirectory the subdirectory relative to the zip file location
     * (e.g. "data", note that there is no tailing "/")
     * @param file the file to add to the zip
     * @param out the zip stream
     * @param waitingHandler a waiting handler allowing canceling the process
     * (can be null)
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addFileToZip(String subDirectory, File file, ZipOutputStream out, WaitingHandler waitingHandler) throws IOException {

        if (file.isDirectory()) {
            throw new IllegalArgumentException("Attempting to add a folder as a file. Use addToZip instead.");
        }

        FileInputStream fi = new FileInputStream(file);

        try {
            BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);

            try {
                String entryName = subDirectory;
                if (!subDirectory.equals("")) {
                    entryName += "/";
                }

                entryName += file.getName();
                ZipEntry entry = new ZipEntry(entryName);
                entry.setSize(file.length());
                out.putNextEntry(entry);
                byte data[] = new byte[BUFFER];
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        return;
                    }
                    out.write(data, 0, count);
                }
            } finally {
                origin.close();
            }
        } finally {
            fi.close();
        }
    }

    /**
     * Adds a new entry to the zip file corresponding to a new folder.
     *
     * @param folderPath the path to the folder relative to the zip file (e.g.
     * "data", note that there is no tailing "/")
     * @param out the zip stream
     *
     * @throws IOException
     */
    public static void addFolderToZip(String folderPath, ZipOutputStream out) throws IOException {
        out.putNextEntry(new ZipEntry(folderPath + "/"));
    }

    /**
     * Unzips the content of an archive into a given folder. The folder needs to
     * exist.
     *
     * @param zipFile the file to unzip
     * @param destinationFolder the destination folder
     * @param waitingHandler a waiting handler displaying progress and allowing
     * canceling the process (can be null)
     *
     * @throws IOException
     */
    public static void unzip(File zipFile, File destinationFolder, WaitingHandler waitingHandler) throws IOException {

        long fileLength = getUnzippedSize(zipFile);
        if (fileLength <= 0) {
            fileLength = zipFile.length(); //@TODO: find a better solution when no size is available
        }
        
        FileInputStream fi = new FileInputStream(zipFile);

        try {
            BufferedInputStream bis = new BufferedInputStream(fi, BUFFER);

            try {
                ZipInputStream zis = new ZipInputStream(bis);

                try {
                    byte data[] = new byte[BUFFER];
                    long read = 0;    
                    ZipEntry entry;

                    while ((entry = zis.getNextEntry()) != null) {

                        String entryName = entry.getName();

                        // dirty fix to be able to open windows files on linux/mac and the other way around
                        if (System.getProperty("os.name").lastIndexOf("Windows") == -1) {
                            entryName = entryName.replaceAll("\\\\", "/");
                        } else {
                            entryName = entryName.replaceAll("/", "\\\\");
                        }

                        File destinationFile = new File(destinationFolder, entryName);
                        File entryFolder = destinationFile.getParentFile();

                        if (entry.isDirectory()) {
                            destinationFile.mkdirs();
                        } else if (entryFolder.exists() || entryFolder.mkdirs()) {

                            FileOutputStream fos = new FileOutputStream(destinationFile);

                            try {
                                BufferedOutputStream bos = new BufferedOutputStream(fos);

                                try {
                                    int count;

                                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                                        bos.write(data, 0, count);
                                        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                                            break;
                                        }
                                        if (waitingHandler != null && fileLength > 0) {
                                            read += count;
                                            int progress = (int) (100 * read / fileLength);
                                            waitingHandler.setSecondaryProgressCounter(progress);
                                        }
                                    }
                                } finally {
                                    bos.close();
                                }
                            } finally {
                                fos.close();
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
                    zis.close();
                }
            } finally {
                bis.close();
            }
        } finally {
            fi.close();
        }
    }
    
    /**
     * Returns the uncompressed size of the archive in bytes as sum of the uncompressed entry sizes.
     * 
     * @param zipFile the file of interest
     * 
     * @return the uncompressed size of the archive
     * 
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static long getUnzippedSize(File zipFile) throws FileNotFoundException, IOException {
        
        long size = 0;
        
        FileInputStream fi = new FileInputStream(zipFile);

        try {
            BufferedInputStream bis = new BufferedInputStream(fi, BUFFER);

            try {
                ZipInputStream zis = new ZipInputStream(bis);

                try {
                    ZipEntry entry;

                    while ((entry = zis.getNextEntry()) != null) {
                        size += entry.getSize();
                    }
                } finally {
                    zis.close();
                }
            } finally {
                bis.close();
            }
        } finally {
            fi.close();
        }
        
        return size;
    }
}
