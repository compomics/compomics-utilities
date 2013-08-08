package com.compomics.software.autoupdater;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;

/**
 * Download the latest zip file from the repository.
 *
 * @author Davy Maddelein
 */
public class DownloadLatestZipFromRepo {

    /**
     * Downloads the latest deploy from the genesis maven repository of the
     * artifact of the jarPath, starts it without arguments and removes the old
     * jar if there was an update.
     *
     * @param jarPath the path to the jarfile
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote maven repository
     * @throws URISyntaxException
     */
    public static void downloadLatestZipFromRepo(URL jarPath) throws IOException, XMLStreamException, URISyntaxException {
        downloadLatestZipFromRepo(jarPath, true, true);
    }

    /**
     * Downloads the latest deploy from the genesis maven repository of the
     * artifact and starts it without arguments.
     *
     * @param jarPath the path to the jarfile
     * @param deleteOldFiles if the jar who starts the update should be deleted
     * @param startDownloadedVersion if the newly downloaded jar should be
     * started after download
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote maven repository
     * @throws URISyntaxException
     */
    public static void downloadLatestZipFromRepo(URL jarPath, boolean deleteOldFiles, boolean startDownloadedVersion) throws IOException, XMLStreamException, URISyntaxException {
        downloadLatestZipFromRepo(jarPath, deleteOldFiles, new String[0], startDownloadedVersion);
    }

    /**
     * Downloads the latest zip archive of the jar in the url from the genesis
     * maven repo.
     *
     * @param jarPath the path to the jarfile to update
     * @param deleteOldFiles if the original jar file should be deleted
     * @param args the args for the newly downloaded jar when it starts
     * @param startDownloadedVersion if true, the downloaded version will be
     * started when the download completes
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote maven repository
     * @throws URISyntaxException
     */
    public static void downloadLatestZipFromRepo(URL jarPath, boolean deleteOldFiles, String[] args, boolean startDownloadedVersion) throws IOException, XMLStreamException, URISyntaxException {
        downloadLatestZipFromRepo(jarPath, deleteOldFiles, null, args, new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString()), startDownloadedVersion);
    }

    /**
     * Downloads the latest zip archive of the jar in the url from a given
     * jarRepository.
     *
     * @param jarPath the path to the jarfile to update, cannot be {@code null}
     * @param deleteOldFiles if the original jar folder should be deleted,
     * cannot be {@code null}
     * @param args the args for the newly downloaded jar when it starts
     * @param jarRepository the repository to look for the latest deploy of the
     * jar file, cannot be {@code null}
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote maven repository
     * @throws URISyntaxException
     */
    public static void downloadLatestZipFromRepo(URL jarPath, boolean deleteOldFiles, String[] args, URL jarRepository) throws IOException, XMLStreamException, URISyntaxException {
        downloadLatestZipFromRepo(jarPath, deleteOldFiles, null, args, jarRepository, true);
    }

    /**
     * Retrieves the latest version of a maven jar file from a maven repository,
     * also checks if the environment is headless or not.
     *
     * @param jarPath the URL of the location of the jar that needs to be
     * updated on the file system. cannot be {@code null}
     * @param deleteOldFiles should the old installation be removed or not
     * cannot be {@code null}
     * @param iconName name of the shortcut image should one be created
     * @param args the args that will be passed to the newly downloaded program
     * when started, cannot be {@code null}
     * @param jarRepository the maven repository to go look in, cannot be
     * {@code null}
     * @param startDownloadedVersion if the newly downloaded version should be
     * started automatically or not
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote maven repository
     * @throws URISyntaxException
     */
    public static void downloadLatestZipFromRepo(final URL jarPath, boolean deleteOldFiles, String iconName, String[] args, URL jarRepository, boolean startDownloadedVersion) throws IOException, XMLStreamException, URISyntaxException {
        if (GraphicsEnvironment.isHeadless()) {
            downloadLatestZipFromRepo(jarPath, deleteOldFiles, iconName, args, jarRepository, startDownloadedVersion, new HeadlessFileDAO());
        } else {
            downloadLatestZipFromRepo(jarPath, deleteOldFiles, iconName, args, jarRepository, startDownloadedVersion, new GUIFileDAO());

        }
    }

    /**
     * Retrieves the latest version of a maven jar file from a maven repository.
     *
     * @param jarPath the URL of the location of the jar that needs to be
     * updated on the file system. cannot be {@code null}
     * @param deleteOldFiles should the old installation be removed or not
     * cannot be {@code null}
     * @param iconName name of the shortcut image should one be created
     * @param args the args that will be passed to the newly downloaded program
     * when started, cannot be {@code null}
     * @param jarRepository the maven repository to go look in, cannot be
     * {@code null}
     * @param startDownloadedVersion if the newly downloaded version should be
     * started automatically or not
     * @param fileDAO what implementation of FileDAO should be used in the
     * updating
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote maven repository
     * @throws URISyntaxException
     */
    public static void downloadLatestZipFromRepo(final URL jarPath, boolean deleteOldFiles, String iconName, String[] args, URL jarRepository, boolean startDownloadedVersion, FileDAO fileDAO) throws IOException, XMLStreamException, URISyntaxException {
        MavenJarFile oldMavenJarFile = new MavenJarFile(jarPath.toURI());
        if (WebDAO.newVersionReleased(oldMavenJarFile, jarRepository)) {
            MavenJarFile downloadedJarFile;

            //TL;DR of the next three lines: make the url for the latest version location of a maven jar file
            String artifactInRepoLocation = new StringBuilder(jarRepository.toExternalForm()).append(oldMavenJarFile.getGroupId().replaceAll("\\.", "/")).append("/").append(oldMavenJarFile.getArtifactId()).toString();
            String latestRemoteRelease = WebDAO.getLatestVersionNumberFromRemoteRepo(new URL(new StringBuilder(artifactInRepoLocation).append("/maven-metadata.xml").toString()));
            String latestArtifactLocation = new StringBuilder(artifactInRepoLocation).append("/").append(latestRemoteRelease).toString();

            if (System.getProperty("os.name").toLowerCase(new Locale("en")).contains("win")) {
                downloadedJarFile = downloadAndUnzipJarForWindows(oldMavenJarFile, new URL(latestArtifactLocation), fileDAO, true);
                //try{
                fileDAO.createDesktopShortcut(downloadedJarFile, iconName, deleteOldFiles);
                //}catch(IOException ioe){ if (!ignoreShortcutCreationErrors){throw ioe}}
            } else {
                downloadedJarFile = downloadAndUnzipJarForUnix(oldMavenJarFile, new URL(latestArtifactLocation), fileDAO);
                //update symlinks?
            }
            try {
                Process launchedJar = null;
                if (startDownloadedVersion) {
                    launchedJar = launchJar(downloadedJarFile, args);
                }
                if (deleteOldFiles && launchedJar != null) {
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            try {
                                File jarParent = new File(jarPath.toURI()).getParentFile();
                                if (jarParent.exists()) {
                                    //dangerous, find better way to do this
                                    FileUtils.deleteDirectory(jarParent);
                                }
                            } catch (URISyntaxException ex) {
                            } catch (IOException ex) {
                                //todo handle stuff did not get done
                            }
                        }
                    });
                    launchedJar.waitFor();
                }
            } catch (InterruptedException ie) {
                throw new InterruptedIOException("jvm ended unexpectedly, old files have not been deleted");
            }
        }
    }

    /**
     * Simple jar launch through a {@code ProcessBuilder}.
     *
     * @param downloadedJarFile the downloaded jar file to start
     * @param args the args to give to the jar file
     * @return true if the launch succeeded
     * @throws IOException if the process could not start
     */
    private static Process launchJar(MavenJarFile downloadedFile, String[] args) throws NullPointerException, IOException {
        Process jar;
        ProcessBuilder p;
        List<String> processToRun = new ArrayList<String>();
        try {
            processToRun.add("java");
            processToRun.add("-jar");
            processToRun.add(downloadedFile.getAbsoluteFilePath());
            processToRun.addAll(Arrays.asList(args));
            p = new ProcessBuilder(processToRun);
            jar = p.start();
        } catch (NullPointerException npe) {
            throw new IOException("could not start the jar");
        }
        return jar;
    }

    /**
     * Aggregation method for downloading and unzipping for windows.
     *
     * @param mavenJarFile the maven jar file to download update for
     * @param jarRepository the url of the version specific location
     * @param fileDAO which fileDAO implementation that should be used
     * @return the downloaded {@code MavenJarFile}
     * @throws MalformedURLException
     * @throws IOException
     * @throws XMLStreamException
     */
    private static MavenJarFile downloadAndUnzipJarForWindows(MavenJarFile mavenJarFile, URL jarRepository, FileDAO fileDAO, boolean cleanupZipFile) throws MalformedURLException, IOException, XMLStreamException {
        MavenJarFile newMavenJar;
        URL archiveURL = WebDAO.getUrlOfZippedVersion(jarRepository, ".zip", false);
        String folderName = archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/"), archiveURL.getFile().lastIndexOf(".zip"));
        File downloadFolder = new File(fileDAO.getLocationToDownloadOnDisk(new File(mavenJarFile.getAbsoluteFilePath()).getParent()), folderName);
        if (!downloadFolder.exists()) {
            if (!downloadFolder.mkdirs()) {
                throw new IOException("could not make the directories needed to download the file in");
            }
        }
        File downloadedFile = fileDAO.writeStreamToDisk(archiveURL.openStream(), archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/")), downloadFolder);
        fileDAO.unzipFile(new ZipFile(downloadedFile), downloadFolder);
        newMavenJar = fileDAO.getMavenJarFileFromFolderWithArtifactId(downloadFolder, mavenJarFile.getArtifactId());
        if (cleanupZipFile) {
            if (!downloadedFile.delete()) {
                throw new IOException("could not delete the zip file");
            }
        }
        return newMavenJar;
    }

    /**
     * Aggregation method for downloading and unzipping for linux/mac.
     *
     * @param mavenJarFile the maven jar file to download update for
     * @param jarRepository the url of the version specific location
     * @param fileDAO which fileDAO implementation that should be used
     * @return the downloaded {@code MavenJarFile}
     * @throws MalformedURLException
     * @throws IOException
     * @throws XMLStreamException
     */
    private static MavenJarFile downloadAndUnzipJarForUnix(MavenJarFile oldMavenJarFile, URL jarRepository, FileDAO fileDAO) throws MalformedURLException, IOException, XMLStreamException {
        URL archiveURL = WebDAO.getUrlOfZippedVersion(jarRepository, ".tar.gz", true);
        if (archiveURL == null) {
            archiveURL = WebDAO.getUrlOfZippedVersion(jarRepository, ".zip", true);
        }
        File downloadFolder = new File(fileDAO.getLocationToDownloadOnDisk(oldMavenJarFile.getAbsoluteFilePath()), archiveURL.getFile());
        fileDAO.unGzipAndUntarFile(new GZIPInputStream(archiveURL.openStream()), downloadFolder);
        MavenJarFile downloadedJarFile = fileDAO.getMavenJarFileFromFolderWithArtifactId(downloadFolder, oldMavenJarFile.getArtifactId());
        return downloadedJarFile;
    }
}
