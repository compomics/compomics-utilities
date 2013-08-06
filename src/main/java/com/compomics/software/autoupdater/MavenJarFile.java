package com.compomics.software.autoupdater;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author Davy
 */
public class MavenJarFile extends JarFile {

private Properties mavenProperties = new Properties();
private String absoluteFilePath;
    
    public MavenJarFile(URI jarPath) throws IOException {
        super(new File(jarPath));
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
    
    public MavenJarFile(File aJarFile) throws IOException{
        this(aJarFile.toURI());
    }
    
    public String getArtifactId(){
        return mavenProperties.getProperty("artifactId");
    }
    
    public String getGroupId(){
        return mavenProperties.getProperty("groupId");
    }
    
    public String getVersionNumber(){
        return mavenProperties.getProperty("version");
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }
}
