package com.compomics.software.autoupdater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * WebDAO.
 *
 * @author Davy Maddelein
 */
public class WebDAO {

    /**
     * Empty default constructor
     */
    public WebDAO() {
    }

    /**
     * The locale.
     */
    private static final Locale LOCALE = new Locale("en");

    /**
     * Fetches the latest Maven deployed version from a Maven built repository.
     *
     * @param remoteVersionXMLFileLocation the version of the remote XML file
     * @return the latest Maven deployed version
     * @throws XMLStreamException if an XMLStreamException occurs
     * @throws IOException if an IOException occurs
     */
    public static String getLatestVersionNumberFromRemoteRepo(URL remoteVersionXMLFileLocation) throws XMLStreamException, IOException {
        BufferedReader remoteVersionsReader = new BufferedReader(new InputStreamReader(remoteVersionXMLFileLocation.openStream()));
        XMLInputFactory xmlParseFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlReader = xmlParseFactory.createXMLEventReader(remoteVersionsReader);
        MetaDataXMLParser xmlParser = new MetaDataXMLParser(xmlReader);
        xmlReader.close();
        return xmlParser.getHighestVersionNumber();
    }

    /**
     * Gets the first zip file from a URL, in case of a Maven repo deploy this
     * should be the only zip in the folder.
     *
     * @param repoURL the URL to get the zip from
     * @param suffix what file extension should be looked for
     * @param returnAlternateArchives if the requested file extension isn't
     * found, return the first .zip/tar.gz found
     * @return URL to the archive file
     * @throws MalformedURLException if the URL of the zip could not be found
     * @throws IOException if the stream to the web page could not be read
     */
    public static URL getUrlOfZippedVersion(URL repoURL, String suffix, boolean returnAlternateArchives) throws MalformedURLException, IOException {
        BufferedReader reader = null;
        suffix = suffix.trim();
        String line;
        String toReturn = null;
        String alternativeReturn = null;
        try {
            reader = new BufferedReader(new InputStreamReader(repoURL.openStream()));
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase(LOCALE).contains("href=") && line.toLowerCase(LOCALE).contains(suffix)) {
                    toReturn = line.substring(line.indexOf("href=\"") + 6, line.indexOf(suffix, line.indexOf("href=\"")) + suffix.length());
                    break;
                } else if (returnAlternateArchives && (line.toLowerCase(LOCALE).contains(".zip") || line.toLowerCase(LOCALE).contains(".tar.gz") || line.toLowerCase(LOCALE).contains(".bz"))) {
                    alternativeReturn = line.substring(line.indexOf("href=\"") + 6, line.indexOf(line.indexOf(">"), line.indexOf("href=\"") + 6));
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        if (returnAlternateArchives && toReturn == null) {
            toReturn = alternativeReturn;
        }
        if (toReturn == null) {
            return null;
        }
        return new URL(new StringBuilder().append(repoURL.toExternalForm()).append("/").append(toReturn).toString());
    }

    /**
     * Returns true if a new version is available.
     *
     * @param jarFile the Maven jar file
     * @param jarRepository the repository
     * @return true if a new version is available
     * @throws IOException if an IOException occurs
     * @throws XMLStreamException if an XMLStreamException occurs
     */
    public static boolean newVersionReleased(MavenJarFile jarFile, URL jarRepository) throws IOException, XMLStreamException {

        String currentVersionNumber = jarFile.getVersionNumber();

        // check if beta version
        if (currentVersionNumber.contains("b") || currentVersionNumber.contains("beta")) {
            return false;
        }

        boolean newVersion = false;
        String versionRepoURLString = new StringBuilder(jarRepository.toExternalForm()).append(
                jarFile.getGroupId().replaceAll("\\.", "/")).append("/").append(jarFile.getArtifactId()).append("/maven-metadata.xml").toString();
        String latestRemoteRelease = WebDAO.getLatestVersionNumberFromRemoteRepo(new URL(versionRepoURLString));
        if (latestRemoteRelease != null && new CompareVersionNumbers().compare(currentVersionNumber, latestRemoteRelease) == 1) {
            newVersion = true;
        }
        return newVersion;
    }
}
