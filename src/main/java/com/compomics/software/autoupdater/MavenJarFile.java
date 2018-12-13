package com.compomics.software.autoupdater;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * MavenJarFile.
 *
 * @author Davy Maddelein
 */
public class MavenJarFile extends JarFile {

    /**
     * The Maven properties.
     */
    private Properties mavenProperties = new Properties();
    /**
     * The absolute file path.
     */
    private String absoluteFilePath;
    /**
     * The path to the jar file.
     */
    private URI jarPath;

    /**
     * Create a new MavenJarFile object.
     *
     * @param jarPath the path to the jar file
     * @throws IOException if an IOException occurs
     */
    public MavenJarFile(URI jarPath) throws IOException {
        super(new File(jarPath));
        this.jarPath = jarPath;
        this.absoluteFilePath = new File(jarPath).getAbsolutePath();
        Enumeration<JarEntry> entries = this.entries();
        //no cleaner way to do this without asking for the group and artifact id, which defeats the point
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().contains("pom.properties")) {
                mavenProperties.load(this.getInputStream(entry));
                break;
            }
        }
    }

    /**
     * Create a new MavenJarFile object.
     *
     * @param aJarFile the jar file
     * @throws IOException if an IOException occurs
     */
    public MavenJarFile(File aJarFile) throws IOException {
        this(aJarFile.toURI());
    }

    /**
     * Returns the artifact id.
     *
     * @return the artifact id
     */
    public String getArtifactId() {
        return mavenProperties.getProperty("artifactId");
    }

    /**
     * Returns the group id.
     *
     * @return the group id
     */
    public String getGroupId() {
        return mavenProperties.getProperty("groupId");
    }

    /**
     * Returns the version number.
     *
     * @return the version number
     */
    public String getVersionNumber() {
        return mavenProperties.getProperty("version");
    }

    /**
     * Returns the absolute file path.
     *
     * @return the absolute file path
     */
    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public URI getJarPath() {
        return jarPath;
    }
}
