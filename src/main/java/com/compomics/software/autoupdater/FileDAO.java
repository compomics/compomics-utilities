package com.compomics.software.autoupdater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.jimmc.jshortcut.JShellLink;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Davy
 */
public abstract class FileDAO {

    /**
     * creates a new Desktop Shortcut to the maven jar file, atm windows only
     *
     * @param file the maven jarfile to make a shortcut to
     * @param iconName the name of the icon file in the resources folder
     * @param deleteOldShortcut if previous shortcuts containing the maven jar
     * file artifact id should be removed
     * @throws IOException
     */
    public abstract boolean createDesktopShortcut(MavenJarFile file, String iconName, boolean deleteOldShortcut) throws IOException;

    public boolean addShortcutAtDeskTop(MavenJarFile mavenJarFile) {
        return addShortcutAtDeskTop(mavenJarFile, null);
    }

    /**
     * adds a shortcut to the desktop, atm windows only
     * 
     * @param mavenJarFile the {@code MavenJarFile} to create the shortcut for
     * @param iconName the name of the icon in the resource folder of the {@code MavenJarFile} to link to
     * @return true if the shortcut was created otherwise false
     */
    public boolean addShortcutAtDeskTop(MavenJarFile mavenJarFile, String iconName) throws NullPointerException,RuntimeException {

        JShellLink link = new JShellLink();
        link.setFolder(JShellLink.getDirectory("desktop"));
        link.setName(new StringBuilder().append(mavenJarFile.getArtifactId()).append("-").append(mavenJarFile.getVersionNumber()).toString());
        if (iconName != null) {
            link.setIconLocation(new StringBuilder().append(mavenJarFile.getAbsoluteFilePath()).append("/resources/").append(iconName).toString());
        }
        link.setPath(mavenJarFile.getAbsoluteFilePath());
        link.save();
        return true;
    }

    /**
     * try to find an at least somewhat sane location to download files to
     * @param targetDownloadFolder first place to check if it is a possible download location
     * @return the folder to download in (in best case scenario this is the passed parameter targetDownloadFolder)
     * @throws IOException
     */
    public abstract File getLocationToDownloadOnDisk(String targetDownloadFolder) throws IOException;

    //rewrite both downloadAndUnzipFiles to use apache commons compress library?
    /**
     * unzips a zip archive
     * @param zip the zipfile to unzip
     * @param fileLocationOnDiskToDownloadTo the folder to unzip in
     * @return true if successful
     * @throws IOException 
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
                if(!destFile.exists()){
                if (!destFile.mkdirs()) {
                    throw new IOException("could not create folders to unzip file");
                }
            }}
        }
        zip.close();
        return true;
    }

    /**
     * untars and ungzips a .tar.gz file
     * @param in a {@code GZIPInputStream} of the file that needs to be ungzipped and untarred
     * @param fileLocationOnDiskToDownloadTo the folder to ungzip and untar in
     * @return true if successful
     * @throws IOException 
     */
    public boolean unGzipAndUntarFile(GZIPInputStream in, File fileLocationOnDiskToDownloadTo) throws IOException {

        InputStreamReader isr = new InputStreamReader(in);
        int count;
        char data[] = new char[1024];
        BufferedWriter dest = null;
        try {
            dest = new BufferedWriter(new FileWriter(fileLocationOnDiskToDownloadTo), 1024);
            while ((count = isr.read(data, 0, 1024)) != -1) {
                dest.write(data, 0, count);
            }
        } finally {
            if (dest != null) {
                dest.close();
            }
        }
        isr.close();
        untar(fileLocationOnDiskToDownloadTo);
        return true;
    }

    /**
     * untars a .tar
     * @param fileToUntar
     * @return true if successful
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private boolean untar(File fileToUntar) throws FileNotFoundException, IOException {
        boolean fileUntarred = false;
        String untarLocation = fileToUntar.getAbsolutePath();
        TarArchiveInputStream tarStream = null;
        try {
            tarStream = new TarArchiveInputStream(new FileInputStream(fileToUntar));
            BufferedReader bufferedTarReader = null;
            try {
                bufferedTarReader = new BufferedReader(new InputStreamReader(tarStream));
                ArchiveEntry entry;
                while ((entry = tarStream.getNextEntry()) != null) {
                    char[] cbuf = new char[1024];
                    int count;
                    FileWriter out = null;
                    try {
                        out = new FileWriter(new File(String.format("%s/%s", untarLocation, entry.getName())));
                        while ((count = bufferedTarReader.read(cbuf, 0, 1024)) != -1) {
                            out.write(cbuf, 0, count);
                        }
                        out.flush();
                    } finally {
                        if (out != null) {
                            out.close();
                        }
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
     * fetches a maven built jar file from a folder for the given artifact id (e.g peptideshaker or ms-lims)
     * @param folder the folder to look in
     * @param artifactId the artifactid in the properties of the (@code MavenJarFile) in the folder
     * @return the last found {@code MavenJarFile} with the given artifactid, can be null
     * @throws IOException 
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
     * writes a stream to disk
     * @param in the stream to write to disk
     * @param name the name the file that will be created
     * @param outputLocationFolder the location to write to
     * @return the written file
     * @throws FileNotFoundException
     * @throws IOException 
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
