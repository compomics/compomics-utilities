package com.compomics.software.autoupdater;

import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.jimmc.jshortcut.JShellLink;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

/**
 * FileDAO file access.
 *
 * @author Davy Maddelein
 * @author Harald Barsnes
 */
public abstract class FileDAO {

    /**
     * Creates a new Desktop Shortcut to the Maven jar file, atm windows only.
     *
     * @param file the Maven jarfile to make a shortcut to
     * @param iconName the name of the icon file in the resources folder
     * @param toolName the name of the tool, e.g., PeptideShaker
     * @param deleteOldShortcut if previous shortcuts containing the Maven jar
     * file artifact id should be removed
     * @return true id the shortcut was created (?)
     * @throws IOException if an IOException occurs
     */
    public abstract boolean createDesktopShortcut(MavenJarFile file, String iconName, String toolName, boolean deleteOldShortcut) throws IOException;

    /**
     * Add desktop shortcut.
     *
     * @param mavenJarFile the Maven jar file
     * @return true id the shortcut was created (?)
     */
    public boolean addShortcutAtDeskTop(MavenJarFile mavenJarFile) {
        return addShortcutAtDeskTop(mavenJarFile, null);
    }

    /**
     * Adds a shortcut to the desktop. At the moment for Windows only.
     *
     * @param mavenJarFile the {@code MavenJarFile} to create the shortcut for
     * @param iconName the name of the icon in the resource folder of the
     * {@code MavenJarFile} to link to
     * @return true if the shortcut was created otherwise false
     * @throws NullPointerException if a NullPointerException occurs
     * @throws RuntimeException if a RuntimeException occurs
     */
    public boolean addShortcutAtDeskTop(MavenJarFile mavenJarFile, String iconName) throws NullPointerException, RuntimeException {

        JShellLink link = new JShellLink();
        link.setFolder(JShellLink.getDirectory("desktop"));
        link.setName(new StringBuilder().append(mavenJarFile.getArtifactId()).append("-").append(mavenJarFile.getVersionNumber()).toString());
        if (iconName != null) {
            link.setIconLocation(new StringBuilder().append(new File(mavenJarFile.getAbsoluteFilePath()).getParentFile().getAbsolutePath()).append("/resources/").append(iconName).toString());
        }
        link.setPath(mavenJarFile.getAbsoluteFilePath());
        link.save();
        return true;
    }

    /**
     * Try to find an at least somewhat sane location to download files to.
     *
     * @param targetDownloadFolder first place to check if it is a possible
     * download location
     * @return the folder to download in (in best case scenario this is the
     * passed parameter targetDownloadFolder)
     * @throws IOException if an IOException occurs
     */
    public abstract File getLocationToDownloadOnDisk(String targetDownloadFolder) throws IOException;

    // @TODO: rewrite both downloadAndUnzipFiles to use apache commons compress library?
    /**
     * Unzips a zip archive.
     *
     * @param zip the zipfile to unzip
     * @param fileLocationOnDiskToDownloadTo the folder to unzip in
     * @return true if successful
     * @throws IOException if an IOException occurs
     */
    public boolean unzipFile(ZipFile zip, File fileLocationOnDiskToDownloadTo) throws IOException {
        FileOutputStream dest = null;
        InputStream inStream = null;
        Enumeration<? extends ZipEntry> zipFileEnum = zip.entries();
        while (zipFileEnum.hasMoreElements()) {
            ZipEntry entry = zipFileEnum.nextElement();
            File destFile = new File(String.format("%s/%s", fileLocationOnDiskToDownloadTo, entry.getName()));
            if (!destFile.getParentFile().exists()) {
                if (!destFile.getParentFile().mkdirs()) {
                    throw new IOException("could not create the folders to unzip in");
                }
            }
            if (!entry.isDirectory()) {
                try {
                    dest = new FileOutputStream(destFile);
                    inStream = zip.getInputStream(entry);
                    IOUtils.copyLarge(inStream, dest);
                } finally {
                    if (dest != null) {
                        dest.close();
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                }
            } else {
                if (!destFile.exists()) {
                    if (!destFile.mkdirs()) {
                        throw new IOException("could not create folders to unzip file");
                    }
                }
            }
        }
        zip.close();
        return true;
    }

    /**
     * Untars and ungzips a .tar.gz file.
     *
     * @param in a {@code GZIPInputStream} of the file that needs to be
     * ungzipped and untarred
     * @param fileLocationOnDiskToDownloadTo the file to ungzip and untar to
     * @param waitingHandler the waiting handler
     * @return true if successful
     * @throws IOException if an IOException occurs
     */
    public boolean unGzipAndUntarFile(GZIPInputStream in, File fileLocationOnDiskToDownloadTo, WaitingHandler waitingHandler) throws IOException {

        try {
            FileOutputStream fos = new FileOutputStream(fileLocationOnDiskToDownloadTo);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            //close resources
            fos.close();
            in.close();

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(true);
            }

            untar(fileLocationOnDiskToDownloadTo);
        } catch (IOException e) {
            if (waitingHandler != null) {
                waitingHandler.setRunCanceled();
            }
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Untars a .tar.
     *
     * @param fileToUntar
     * @return true if successful
     * @throws FileNotFoundException if an FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    private boolean untar(File fileToUntar) throws FileNotFoundException, IOException {
        boolean fileUntarred = false;
        String untarLocation = fileToUntar.getParentFile().getAbsolutePath();
        TarArchiveInputStream tarStream = null;
        try {
            tarStream = new TarArchiveInputStream(new FileInputStream(fileToUntar));
            BufferedReader bufferedTarReader = null;
            try {
                bufferedTarReader = new BufferedReader(new InputStreamReader(tarStream));
                ArchiveEntry entry;
                while ((entry = tarStream.getNextEntry()) != null) {
                    byte[] buffer = new byte[8 * 1024];
                    File tempFile = new File(String.format("%s/%s", untarLocation, entry.getName()));
                    if (entry.isDirectory()) {
                        if (!tempFile.exists()) {
                            tempFile.mkdir();
                        }
                    } else {
                        OutputStream output = new FileOutputStream(tempFile);
                        try {
                            int bytesRead;
                            while ((bytesRead = tarStream.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }
                        } finally {
                            output.close();
                        }
                        tempFile.setExecutable(true); // make sure the binary files can be executed
                    }
                }
            } finally {
                if (bufferedTarReader != null) {
                    bufferedTarReader.close();
                }
            }
        } finally {
            if (tarStream != null) {
                tarStream.close();
            }
        }
        return fileUntarred;
    }

    /**
     * Fetches a Maven built jar file from a folder for the given artifact id
     * (e.g peptideshaker or ms-lims).
     *
     * @param folder the folder to look in
     * @param artifactId the artifactid in the properties of the (@code
     * MavenJarFile) in the folder
     * @return the last found {@code MavenJarFile} with the given artifactid,
     * can be null
     * @throws IOException if an IOException occurs
     */
    public MavenJarFile getMavenJarFileFromFolderWithArtifactId(File folder, String artifactId) throws IOException {
        MavenJarFile mainJarFile = null;
        for (File aFile : folder.listFiles()) {
            if (aFile.isDirectory()) {
                mainJarFile = getMavenJarFileFromFolderWithArtifactId(aFile, artifactId);
                if (mainJarFile != null) {
                    break;
                }
            } else {
                if (aFile.getName().contains(artifactId) && aFile.getName().contains("jar")) {
                    mainJarFile = new MavenJarFile(aFile);
                    break;
                }
            }
        }
        /**
         * if (mainJarFile == null) { throw new IOException("could not find jar
         * file in folder and child folders"); }
         */
        return mainJarFile;
    }

    /**
     * Writes a stream to disk.
     *
     * @param in the stream to write to disk
     * @param name the name the file that will be created
     * @param outputLocationFolder the location to write to
     * @return the written file
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public File writeStreamToDisk(InputStream in, String name, File outputLocationFolder) throws FileNotFoundException, IOException {
        if (!outputLocationFolder.exists()) {
            if (!outputLocationFolder.mkdirs()) {
                throw new IOException("could not create the folders to write stream to disk");
            }
        }
        File outputFile = new File(outputLocationFolder, name);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            IOUtils.copyLarge(in, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        in.close();
        return outputFile;
    }
}
