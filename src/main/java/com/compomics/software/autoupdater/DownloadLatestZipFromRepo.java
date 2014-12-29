package com.compomics.software.autoupdater;

import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;

/**
 * Download the latest zip file from the repository.
 *
 * @author Davy Maddelein
 * @author Harald Barsnes
 */
public class DownloadLatestZipFromRepo {

    /**
     * True of a file is currently being downloaded.
     */
    private static boolean isFileBeingDownloaded = false;
    /**
     * The downloaded version of the tool.
     */
    private static File downloadedFile;

    /**
     * Downloads the latest deploy from the genesis Maven repository of the
     * artifact of the jarPath, starts it without arguments and removes the old
     * jar if there was an update.
     *
     * @param jarPath the path to the jarfile
     * @param toolName the name of the tool being updated, e.g., PeptideShaker
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote Maven repository
     * @throws URISyntaxException if there is a problem with the URI syntax
     */
    public static void downloadLatestZipFromRepo(URL jarPath, String toolName) throws IOException, XMLStreamException, URISyntaxException {
        downloadLatestZipFromRepo(jarPath, toolName, true, true, true, null);
    }

    /**
     * Downloads the latest deploy from the genesis Maven repository of the
     * artifact and starts it without arguments.
     *
     * @param jarPath the path to the jarfile
     * @param toolName the name of the tool being updated, e.g., PeptideShaker
     * @param deleteOldFiles if the jar who starts the update should be deleted
     * @param startDownloadedVersion if the newly downloaded jar should be
     * started after download
     * @param addDesktopIcon if true, a desktop icon will be created
     * @param waitingHandler the waiting handler
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote Maven repository
     * @throws URISyntaxException if there is a problem with the URI syntax
     */
    public static void downloadLatestZipFromRepo(URL jarPath, String toolName, boolean deleteOldFiles, boolean startDownloadedVersion, boolean addDesktopIcon, WaitingHandler waitingHandler) throws IOException, XMLStreamException, URISyntaxException {
        downloadLatestZipFromRepo(jarPath, toolName, deleteOldFiles, new String[0], startDownloadedVersion, addDesktopIcon, waitingHandler);
    }

    /**
     * Downloads the latest zip archive of the jar in the URL from the genesis
     * Maven repository.
     *
     * @param jarPath the path to the jarfile to update
     * @param toolName the name of the tool being updated, e.g., PeptideShaker
     * @param deleteOldFiles if the original jar file should be deleted
     * @param args the arguments for the newly downloaded jar when it starts
     * @param startDownloadedVersion if true, the downloaded version will be
     * started when the download completes
     * @param addDesktopIcon if true, a desktop icon will be created
     * @param waitingHandler the waiting handler
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote Maven repository
     * @throws URISyntaxException if there is a problem with the URI syntax
     */
    public static void downloadLatestZipFromRepo(URL jarPath, String toolName, boolean deleteOldFiles, String[] args,
            boolean startDownloadedVersion, boolean addDesktopIcon, WaitingHandler waitingHandler) throws IOException, XMLStreamException, URISyntaxException {
        downloadLatestZipFromRepo(jarPath, toolName, deleteOldFiles, null, args, new URL("http", "genesis.ugent.be",
                new StringBuilder().append("/maven2/").toString()), startDownloadedVersion, addDesktopIcon, waitingHandler);
    }

    /**
     * Downloads the latest zip archive of the jar in the URL from a given
     * jarRepository.
     *
     * @param jarPath the path to the jarfile to update, cannot be {@code null}
     * @param toolName the name of the tool being updated, e.g., PeptideShaker
     * @param deleteOldFiles if the original jar folder should be deleted,
     * cannot be {@code null}
     * @param addDesktopIcon if true, a desktop icon will be created
     * @param args the arguments for the newly downloaded jar when it starts
     * @param jarRepository the repository to look for the latest deploy of the
     * jar file, cannot be {@code null}
     * @param waitingHandler the waiting handler
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote Maven repository
     * @throws URISyntaxException if there is a problem with the URI syntax
     */
    public static void downloadLatestZipFromRepo(URL jarPath, String toolName, boolean deleteOldFiles, boolean addDesktopIcon, String[] args, URL jarRepository, WaitingHandler waitingHandler) throws IOException, XMLStreamException, URISyntaxException {
        downloadLatestZipFromRepo(jarPath, toolName, deleteOldFiles, null, args, jarRepository, true, addDesktopIcon, waitingHandler);
    }

    /**
     * Retrieves the latest version of a Maven jar file from a Maven repository,
     * also checks if the environment is headless or not.
     *
     * @param jarPath the URL of the location of the jar that needs to be
     * updated on the file system. cannot be {@code null}
     * @param toolName the name of the tool being updated, e.g., PeptideShaker
     * @param deleteOldFiles should the old installation be removed or not
     * cannot be {@code null}
     * @param iconName name of the shortcut image should one be created
     * @param args the arguments that will be passed to the newly downloaded
     * program when started, cannot be {@code null}
     * @param jarRepository the Maven repository to go look in, cannot be
     * {@code null}
     * @param startDownloadedVersion if the newly downloaded version should be
     * started automatically or not
     * @param addDesktopIcon if true, a desktop icon will be created
     * @param waitingHandler the waiting handler
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote Maven repository
     * @throws URISyntaxException if there is a problem with the URI syntax
     */
    public static void downloadLatestZipFromRepo(final URL jarPath, String toolName, boolean deleteOldFiles, String iconName, String[] args,
            URL jarRepository, boolean startDownloadedVersion, boolean addDesktopIcon, WaitingHandler waitingHandler) throws IOException, XMLStreamException, URISyntaxException {
        if (GraphicsEnvironment.isHeadless()) {
            downloadLatestZipFromRepo(jarPath, toolName, deleteOldFiles, iconName, args, jarRepository, startDownloadedVersion, addDesktopIcon, new HeadlessFileDAO(), waitingHandler);
        } else {
            downloadLatestZipFromRepo(jarPath, toolName, deleteOldFiles, iconName, args, jarRepository, startDownloadedVersion, addDesktopIcon, new GUIFileDAO(), waitingHandler);
        }
    }

    /**
     * Retrieves the latest version of a Maven jar file from a Maven repository.
     *
     * @param jarPath the URL of the location of the jar that needs to be
     * updated on the file system. cannot be {@code null}
     * @param toolName the name of the tool being updated, e.g., PeptideShaker
     * @param deleteOldFiles should the old installation be removed or not
     * cannot be {@code null}
     * @param iconName name of the shortcut image should one be created
     * @param args the arguments that will be passed to the newly downloaded
     * program when started, cannot be {@code null}
     * @param jarRepository the Maven repository to go look in, cannot be
     * {@code null}
     * @param startDownloadedVersion if the newly downloaded version should be
     * started automatically or not
     * @param addDesktopIcon if true, a desktop icon will be created
     * @param fileDAO what implementation of FileDAO should be used in the
     * updating
     * @param waitingHandler the waiting handler
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote Maven repository
     * @throws URISyntaxException if there is a problem with the URI syntax
     */
    public static void downloadLatestZipFromRepo(final URL jarPath, String toolName, boolean deleteOldFiles, String iconName, String[] args, URL jarRepository, boolean startDownloadedVersion,
            boolean addDesktopIcon, FileDAO fileDAO, WaitingHandler waitingHandler) throws IOException, XMLStreamException, URISyntaxException {

        MavenJarFile oldMavenJarFile = new MavenJarFile(jarPath.toURI());

        if (WebDAO.newVersionReleased(oldMavenJarFile, jarRepository)) {

            //TL;DR of the next three lines: make the url for the latest version location of a maven jar file
            String artifactInRepoLocation = new StringBuilder(jarRepository.toExternalForm()).append(oldMavenJarFile.getGroupId().replaceAll("\\.", "/")).append("/").append(oldMavenJarFile.getArtifactId()).toString();
            String latestRemoteRelease = WebDAO.getLatestVersionNumberFromRemoteRepo(new URL(new StringBuilder(artifactInRepoLocation).append("/maven-metadata.xml").toString()));
            String latestArtifactLocation = new StringBuilder(artifactInRepoLocation).append("/").append(latestRemoteRelease).toString();

            // download and unzip the files
            MavenJarFile downloadedJarFile = downloadAndUnzipJar(oldMavenJarFile, toolName, new URL(latestArtifactLocation), fileDAO,
                    true, waitingHandler, System.getProperty("os.name").toLowerCase(new Locale("en")).contains("win"));

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled() || waitingHandler.isRunFinished()) {
                    return;
                } else {
                    waitingHandler.setRunFinished();
                }
            }

            final File jarParent = new File(jarPath.toURI()).getParentFile();

            // ask if the user really wants to delete the old folder 
            if (deleteOldFiles && fileDAO instanceof GUIFileDAO) {
                int option = JOptionPane.showConfirmDialog(null,
                        "Remove the old version of " + toolName + "? This will delete the folder\n"
                        + "" + jarParent.getAbsolutePath(), "Remove Old " + toolName + " Version?", JOptionPane.YES_NO_OPTION);

                if (option != JOptionPane.YES_OPTION) {
                    deleteOldFiles = false;
                }
            }

            // add desktop icon
            if (addDesktopIcon) {
                if (System.getProperty("os.name").toLowerCase(new Locale("en")).contains("win")) {
                    //try{
                    fileDAO.createDesktopShortcut(downloadedJarFile, iconName, toolName, deleteOldFiles);
                    //}catch(IOException ioe){ if (!ignoreShortcutCreationErrors){throw ioe}}
                } else {
                    // @TODO: update symlinks?
                }
            }

            // set the new version has the default version
            // @TODO: should be done using enums
            UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
            if (toolName.equalsIgnoreCase("PeptideShaker")) {
                utilitiesUserPreferences.setPeptideShakerPath(downloadedJarFile.getAbsoluteFilePath());
            } else if (toolName.equalsIgnoreCase("SearchGUI")) {
                utilitiesUserPreferences.setSearchGuiPath(downloadedJarFile.getAbsoluteFilePath());
            } else if (toolName.equalsIgnoreCase("Reporter")) {
                utilitiesUserPreferences.setReporterPath(downloadedJarFile.getAbsoluteFilePath());
            }
            UtilitiesUserPreferences.saveUserPreferences(utilitiesUserPreferences);

            try {
                // close the access to the old zip file so that it can be deleted
                oldMavenJarFile.close();

                Process launchedJar = null;
                if (startDownloadedVersion) {
                    launchedJar = launchJar(downloadedJarFile, args);
                }
                if (deleteOldFiles) {
                    if (deleteOldFiles) {
                        Runtime.getRuntime().addShutdownHook(new Thread() {
                            @Override
                            public void run() {
                                try {
                                    if (jarParent.exists()) {
                                        //dangerous, find better way to do this
                                        FileUtils.deleteDirectory(jarParent);
                                    }
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                    //todo handle stuff did not get done
                                }
                            }
                        });
                    }
                    if (launchedJar != null) {
                        launchedJar.waitFor();
                    }
                }
            } catch (InterruptedException ie) {
                throw new InterruptedIOException("jvm ended unexpectedly, old files have not been deleted");
            }
        }
    }

    /**
     * Retrieves the latest version of a Maven jar file from a Maven repository.
     *
     * @param downloadFolder the folder to download to
     * @param groupId the group id
     * @param toolName the name of the tool being updated, e.g., PeptideShaker
     * @param artifactId the artifact id
     * @param iconName name of the shortcut image should one be created
     * @param args the arguments that will be passed to the newly downloaded
     * program when started, cannot be {@code null}
     * @param jarRepository the Maven repository to go look in, cannot be
     * {@code null}
     * @param startDownloadedVersion if the newly downloaded version should be
     * started automatically or not
     * @param addDesktopIcon if true, a desktop icon will be created
     * @param fileDAO what implementation of FileDAO should be used in the
     * updating
     * @param waitingHandler the waiting handler
     * @throws IOException should there be problems with reading or writing
     * files during the updating
     * @throws XMLStreamException if there was a problem reading the meta data
     * from the remote Maven repository
     * @throws URISyntaxException if there is a problem with the URI syntax
     */
    public static void downloadLatestZipFromRepo(final File downloadFolder, String toolName, String groupId, String artifactId, String iconName, String[] args, URL jarRepository, boolean startDownloadedVersion,
            boolean addDesktopIcon, FileDAO fileDAO, WaitingHandler waitingHandler) throws IOException, XMLStreamException, URISyntaxException {

        //TL;DR of the next three lines: make the url for the latest version location of a maven jar file
        String artifactInRepoLocation = new StringBuilder(jarRepository.toExternalForm()).append(groupId.replaceAll("\\.", "/")).append("/").append(artifactId).toString();
        String latestRemoteRelease = WebDAO.getLatestVersionNumberFromRemoteRepo(new URL(new StringBuilder(artifactInRepoLocation).append("/maven-metadata.xml").toString()));
        String latestArtifactLocation = new StringBuilder(artifactInRepoLocation).append("/").append(latestRemoteRelease).toString();

        // download and unzip the files
        MavenJarFile downloadedJarFile = downloadAndUnzipJar(downloadFolder, artifactId, toolName, new URL(latestArtifactLocation), fileDAO,
                true, waitingHandler, System.getProperty("os.name").toLowerCase(new Locale("en")).contains("win"), false);

        if (waitingHandler != null) {
            if (waitingHandler.isRunCanceled() || waitingHandler.isRunFinished()) {
                return;
            } else {
                waitingHandler.setRunFinished();
            }
        }

        final File jarParent = downloadFolder;

        // add desktop icon
        if (addDesktopIcon) {
            if (System.getProperty("os.name").toLowerCase(new Locale("en")).contains("win")) {
                //try{
                fileDAO.createDesktopShortcut(downloadedJarFile, iconName, toolName, false);
                //}catch(IOException ioe){ if (!ignoreShortcutCreationErrors){throw ioe}}
            } else {
                // @TODO: update symlinks?
            }
        }

        // set the new version has the default version
        // @TODO: should be done using enums
        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        if (toolName.equalsIgnoreCase("PeptideShaker")) {
            utilitiesUserPreferences.setPeptideShakerPath(downloadedJarFile.getAbsoluteFilePath());
        } else if (toolName.equalsIgnoreCase("SearchGUI")) {
            utilitiesUserPreferences.setSearchGuiPath(downloadedJarFile.getAbsoluteFilePath());
        } else if (toolName.equalsIgnoreCase("Reporter")) {
            utilitiesUserPreferences.setReporterPath(downloadedJarFile.getAbsoluteFilePath());
        }
        UtilitiesUserPreferences.saveUserPreferences(utilitiesUserPreferences);

        if (startDownloadedVersion) {
            launchJar(downloadedJarFile, args);
        }
    }

    /**
     * Simple jar launch through a {@code ProcessBuilder}.
     *
     * @param downloadedJarFile the downloaded jar file to start
     * @param args the arguments to give to the jar file
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
            if (args != null) {
                processToRun.addAll(Arrays.asList(args));
            }
            p = new ProcessBuilder(processToRun);
            p.directory(new File(downloadedFile.getAbsoluteFilePath()).getParentFile());
            jar = p.start();
        } catch (NullPointerException npe) {
            throw new IOException("could not start the jar");
        }
        return jar;
    }

    /**
     * Aggregation method for downloading and unzipping.
     *
     * @param mavenJarFile the Maven jar file to download update for
     * @param toolName the name of the tool being updated, e.g., PeptideShaker
     * @param jarRepository the URL of the version specific location
     * @param fileDAO which fileDAO implementation that should be used
     * @param isWindows if true, the OS will assumed to be windows
     * @return the downloaded {@code MavenJarFile}
     * @throws MalformedURLException if the URL is malformed
     * @throws IOException if there is an IOException
     * @throws XMLStreamException if there is an XMLStreamException
     */
    private static MavenJarFile downloadAndUnzipJar(MavenJarFile mavenJarFile, final String toolName, URL jarRepository,
            FileDAO fileDAO, boolean cleanupZipFile, final WaitingHandler waitingHandler, boolean isWindows) throws MalformedURLException, IOException, XMLStreamException {

        URL archiveURL;
        String folderName;

        // get the archive url
        if (isWindows) {
            archiveURL = WebDAO.getUrlOfZippedVersion(jarRepository, ".zip", false);
            folderName = archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/"), archiveURL.getFile().lastIndexOf(".zip"));
        } else {
            archiveURL = WebDAO.getUrlOfZippedVersion(jarRepository, ".tar.gz", false);
            if (archiveURL != null) {
                folderName = archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/"), archiveURL.getFile().lastIndexOf(".tar.gz"));
            } else {
                archiveURL = WebDAO.getUrlOfZippedVersion(jarRepository, ".zip", false);
                folderName = archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/"), archiveURL.getFile().lastIndexOf(".zip"));
                isWindows = true; // zip file, handling is same as for windows
            }
        }

        // special fix for tools with separate versions for windows and unix
        if (folderName.endsWith("-windows")) {
            folderName = folderName.substring(0, folderName.indexOf("-windows"));
        } else if (folderName.endsWith("-mac_and_linux")) {
            folderName = folderName.substring(0, folderName.indexOf("-mac_and_linux"));
        }

        // set up the folder to save the new download in
        File downloadFolder;
        if (isWindows) {
            downloadFolder = new File(fileDAO.getLocationToDownloadOnDisk(new File(mavenJarFile.getAbsoluteFilePath()).getParent()), folderName);
        } else {
            downloadFolder = fileDAO.getLocationToDownloadOnDisk(new File(mavenJarFile.getAbsoluteFilePath()).getParent());
        }
        if (!downloadFolder.exists()) {
            if (!downloadFolder.mkdirs()) {
                throw new IOException("could not make the directories needed to download the file in");
            }
        }

        // create an empty dummy file so that progress can be monitored
        downloadedFile = new File(downloadFolder, archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/")));

        isFileBeingDownloaded = true;

        // start a thread to monitor the progress
        if (waitingHandler != null) {
            waitingHandler.setWaitingText("Updating " + toolName + ". Please Wait...");

            URLConnection conn = archiveURL.openConnection();
            int tempLength = conn.getContentLength();
            final int currentUrlContentLength;

            if (isWindows) {
                currentUrlContentLength = tempLength;
            } else {
                if (tempLength != -1) {
                    currentUrlContentLength = conn.getContentLength() * 3; // @TODO: size is not correct for the tar.gz file, as it is unzipped as part of the download
                } else {
                    currentUrlContentLength = tempLength;
                }
            }

            if (currentUrlContentLength != -1) {
                waitingHandler.resetPrimaryProgressCounter();
                waitingHandler.setPrimaryProgressCounterIndeterminate(false);
                waitingHandler.setMaxPrimaryProgressCounter(currentUrlContentLength);

                new Thread("DownloadMonitorThread") {
                    @Override
                    public void run() {

                        long start = System.currentTimeMillis();

                        while (isFileBeingDownloaded) {

                            if (waitingHandler.isRunCanceled()) {
                                waitingHandler.setRunFinished();
                                break;
                            }

                            long now = System.currentTimeMillis();

                            // update the progress dialog every 100 millisecond or so
                            if ((now - start) > 100 && downloadedFile != null) {
                                long length = downloadedFile.length();

                                if (currentUrlContentLength != -1) {
                                    waitingHandler.setSecondaryProgressCounter((int) length);
                                } else {
                                    waitingHandler.setWaitingText("Updating " + toolName + ". Please Wait... (" + (length / (1024L * 1024L)) + " MB)");
                                }

                                start = System.currentTimeMillis();
                            }
                        }
                    }
                }.start();
            } else {
                waitingHandler.setPrimaryProgressCounterIndeterminate(true);
            }
        }

        // download and unzip the file
        if (isWindows) {
            downloadedFile = fileDAO.writeStreamToDisk(archiveURL.openStream(), archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/")), downloadFolder);
            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(true);
            }
            fileDAO.unzipFile(new ZipFile(downloadedFile), downloadFolder.getParentFile());
        } else {
            fileDAO.unGzipAndUntarFile(new GZIPInputStream(archiveURL.openStream()), downloadedFile, waitingHandler);
        }

        // get the new jar file
        MavenJarFile newMavenJar;
        if (isWindows) {
            newMavenJar = fileDAO.getMavenJarFileFromFolderWithArtifactId(downloadFolder, mavenJarFile.getArtifactId());
        } else {
            newMavenJar = fileDAO.getMavenJarFileFromFolderWithArtifactId(new File(downloadFolder, folderName), mavenJarFile.getArtifactId());
        }
        isFileBeingDownloaded = false;

        // delete the downloaded zip file
        if (cleanupZipFile) {
            if (!downloadedFile.delete()) {
                throw new IOException("could not delete the zip file");
            }
        }

        return newMavenJar;
    }

    /**
     * Aggregation method for downloading and unzipping.
     *
     * @param mavenJarFile the Maven jar file to download update for
     * @param toolName the name of the tool being updated, e.g., PeptideShaker
     * @param jarRepository the URL of the version specific location
     * @param fileDAO which fileDAO implementation that should be used
     * @param isWindows if true, the OS will assumed to be windows
     * @param update if true, the waiting handler shows update, false shows
     * download
     * @return the downloaded {@code MavenJarFile}
     * @throws MalformedURLException if the URL is malformed
     * @throws IOException if there is an IOException
     * @throws XMLStreamException if there is an XMLStreamException
     */
    private static MavenJarFile downloadAndUnzipJar(final File aDownloadFolder, final String artifactId, final String toolName, URL jarRepository,
            FileDAO fileDAO, boolean cleanupZipFile, final WaitingHandler waitingHandler, boolean isWindows, final boolean update) throws MalformedURLException, IOException, XMLStreamException {

        URL archiveURL;
        String folderName;

        // get the archive url
        if (isWindows) {
            archiveURL = WebDAO.getUrlOfZippedVersion(jarRepository, ".zip", false);
            folderName = archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/"), archiveURL.getFile().lastIndexOf(".zip"));
        } else {
            archiveURL = WebDAO.getUrlOfZippedVersion(jarRepository, ".tar.gz", false);
            if (archiveURL != null) {
                folderName = archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/"), archiveURL.getFile().lastIndexOf(".tar.gz"));
            } else {
                archiveURL = WebDAO.getUrlOfZippedVersion(jarRepository, ".zip", false);
                folderName = archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/"), archiveURL.getFile().lastIndexOf(".zip"));
                isWindows = true; // zip file, handling is same as for windows
            }
        }

        // special fix for tools with separate versions for windows and unix
        if (folderName.endsWith("-windows")) {
            folderName = folderName.substring(0, folderName.indexOf("-windows"));
        } else if (folderName.endsWith("-mac_and_linux")) {
            folderName = folderName.substring(0, folderName.indexOf("-mac_and_linux"));
        }

        File downloadFolder;
        // set up the folder to save the new download in
        if (isWindows) {
            downloadFolder = new File(aDownloadFolder, folderName);
        } else {
            downloadFolder = aDownloadFolder;
        }
        if (!downloadFolder.exists()) {
            if (!downloadFolder.mkdirs()) {
                throw new IOException("could not make the directories needed to download the file in");
            }
        }

        // create an empty dummy file so that progress can be monitored
        downloadedFile = new File(downloadFolder, archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/")));

        isFileBeingDownloaded = true;

        // start a thread to monitor the progress
        if (waitingHandler != null) {
            if (update) {
                waitingHandler.setWaitingText("Updating " + toolName + ". Please Wait...");
            } else {
                waitingHandler.setWaitingText("Downloading " + toolName + ". Please Wait...");
            }

            URLConnection conn = archiveURL.openConnection();
            int tempLength = conn.getContentLength();
            final int currentUrlContentLength;

            if (isWindows) {
                currentUrlContentLength = tempLength;
            } else {
                if (tempLength != -1) {
                    currentUrlContentLength = conn.getContentLength() * 3; // @TODO: size is not correct for the tar.gz file, as it is unzipped as part of the download
                } else {
                    currentUrlContentLength = tempLength;
                }
            }

            if (currentUrlContentLength != -1) {
                waitingHandler.resetPrimaryProgressCounter();
                waitingHandler.setPrimaryProgressCounterIndeterminate(false);
                waitingHandler.setMaxPrimaryProgressCounter(currentUrlContentLength);

                new Thread("DownloadMonitorThread") {
                    @Override
                    public void run() {

                        long start = System.currentTimeMillis();

                        while (isFileBeingDownloaded) {

                            if (waitingHandler.isRunCanceled()) {
                                waitingHandler.setRunFinished();
                                break;
                            }

                            long now = System.currentTimeMillis();

                            // update the progress dialog every 100 millisecond or so
                            if ((now - start) > 100 && downloadedFile != null) {
                                long length = downloadedFile.length();

                                if (currentUrlContentLength != -1) {
                                    waitingHandler.setSecondaryProgressCounter((int) length);
                                } else {
                                    if (update) {
                                        waitingHandler.setWaitingText("Updating " + toolName + ". Please Wait... (" + (length / (1024L * 1024L)) + " MB)");
                                    } else {
                                        waitingHandler.setWaitingText("Downloading " + toolName + ". Please Wait... (" + (length / (1024L * 1024L)) + " MB)");
                                    }
                                }

                                start = System.currentTimeMillis();
                            }
                        }
                    }
                }.start();
            } else {
                waitingHandler.setPrimaryProgressCounterIndeterminate(true);
            }
        }

        // download and unzip the file
        if (isWindows) {
            downloadedFile = fileDAO.writeStreamToDisk(archiveURL.openStream(), archiveURL.getFile().substring(archiveURL.getFile().lastIndexOf("/")), downloadFolder);
            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(true);
            }
            fileDAO.unzipFile(new ZipFile(downloadedFile), downloadFolder.getParentFile());
        } else {
            fileDAO.unGzipAndUntarFile(new GZIPInputStream(archiveURL.openStream()), downloadedFile, waitingHandler);
        }

        // get the new jar file
        MavenJarFile newMavenJar;
        if (isWindows) {
            newMavenJar = fileDAO.getMavenJarFileFromFolderWithArtifactId(downloadFolder, artifactId);
        } else {
            newMavenJar = fileDAO.getMavenJarFileFromFolderWithArtifactId(new File(downloadFolder, folderName), artifactId);
        }
        isFileBeingDownloaded = false;

        // delete the downloaded zip file
        if (cleanupZipFile) {
            if (!downloadedFile.delete()) {
                throw new IOException("could not delete the zip file");
            }
        }

        return newMavenJar;
    }
}
