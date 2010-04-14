package com.compomics.util.io;
import org.apache.log4j.Logger;

import com.compomics.util.enumeration.CompomicsTools;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import static java.util.logging.Logger.global;


/**
 * Created by IntelliJ IDEA. User: kenny Date: Feb 23, 2010 Time: 10:03:33 AM
 * <p/>
 * This class
 */
public class PropertiesManager {
	// Class specific log4j logger for PropertiesManager instances.
	Logger logger = Logger.getLogger(PropertiesManager.class);


/**
 * Created by IntelliJ IDEA.
 * User: kenny
 * Date: Feb 22, 2010
 * Time: 1:27:41 PM
 * <p/>
 * This class holds all user properties for ms_lims.
 */

    /**
     * The singleton instance of the ms properties.
     */
    private static PropertiesManager singleton = null;

    /**
     * This folder is located in the user home directory and captures the user properties of ms_lims.
     */
    private File iHomeFolder;

    /**
     * This private constructor manages a single instance to access the properties.
     */
    private PropertiesManager() {
        // Get the user home directory.
        File lHome = FileSystemAccessor.getHomeFolder();
        ;
        iHomeFolder = new File(lHome, ".compomics");
        if (!iHomeFolder.exists()) {
            iHomeFolder.mkdir();
        }
    }

    /**
     * Get the application folder that contains the appropriate properties.
     *
     * @param aTool The tool for which the properties are requisted.
     * @return File (Directory) that contains the properties.
     */
    public File getApplicationFolder(CompomicsTools aTool) {
        // Make the folder for the required application, if non-existing.
        File iApplicationFolder = new File(iHomeFolder, aTool.getName());
        if (!iApplicationFolder.exists()) {
            iApplicationFolder.mkdir();
        }
        return iApplicationFolder;
    }

    /**
     * Get the singleton instance to access properties of Computational Omics tools.
     *
     * @return The Propertiesmanger to access user properties in their home directory.
     */
    public static PropertiesManager getInstance() {
        if (singleton == null) {
            singleton = new PropertiesManager();
        }
        return singleton;
    }

    /**
     * DO NOT RUN. For testing purpose.
     */
    public static void main(String[] args) {
        new PropertiesManager();
    }

    /**
     * Get a Properties instance for the parameter properties filename.
     *
     * @param aPropertiesFileName - e.g.: "mascotdaemon.properties"
     * @return Properties instance of the given properties file. Null if the filename was not found.
     */
    public Properties getProperties(CompomicsTools aTool, String aPropertiesFileName) {
        Properties lProperties = new Properties();
        InputStream is;
        try {

            // Always get the properties from the classpath. the requested properties are not found,
            // we will try to read the properties from the the classpath.
            is = ClassLoader.getSystemResourceAsStream(aPropertiesFileName);
            if (is == null) {
                is = this.getClass().getClassLoader().getResourceAsStream(aPropertiesFileName);
            }
            Properties lClassPathProperties = new Properties();
            lClassPathProperties.load(is);

            Object lVersion = lClassPathProperties.get("version");
            String lClassPathVersion = null;

            if(lVersion == null){
               logger.warn("ms-lims.properties in classpath lacks a version number!!");
                lClassPathVersion = "NA";
            } else{
                lClassPathVersion = lVersion.toString();
            }

            // Make a filename filter for '.properties' files.
            FilenameFilter lPropertiesFileNameFilter = new FilenameFilter() {
                public boolean accept(final File dir, final String name) {
                    return name.endsWith(".properties");
                }
            };
            // Get the application folder.
            File lApplicationFolder = getApplicationFolder(aTool);

            // Get all the properties files in the mslims folder.
            File[] lPropertiesFiles = lApplicationFolder.listFiles(lPropertiesFileNameFilter);

            // Iterate and try to match the requested file.
            File lRequestedPropertiesFile = null;
            boolean lPropertiesFound = false;
            for (int i = 0; i < lPropertiesFiles.length; i++) {
                File lLastFile = lPropertiesFiles[i];
                if (lLastFile.getName().equals(aPropertiesFileName)) {
                    lRequestedPropertiesFile = lLastFile;
                    lPropertiesFound = true;
                    break;
                }
            }

            // Verify that the properties have been found.
            if (lPropertiesFound == true) {
                // Create the properties via a file inputstream.
                is = new FileInputStream(lRequestedPropertiesFile);
                lProperties.load(is);

            } else {
                // And, write the content of this properties file to the user home directory for the next request.
                try {
                    File lOutput;
                    lOutput = new File(getApplicationFolder(aTool), aPropertiesFileName);
                    if (!lOutput.exists()) {
                        lOutput.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(lOutput);
                    lClassPathProperties.store(fos, aPropertiesFileName + " properties file");
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            // During updates, for each property that was is in the classpathversion,
            // but not in the local properties - store the entry to the local file.

            Set lClassPathPropertyKeySet = lClassPathProperties.keySet();
            for (Iterator lIterator = lClassPathPropertyKeySet.iterator(); lIterator.hasNext();) {
                Object lClasspathKey = lIterator.next();
                if(lProperties.get(lClasspathKey) == null){
                    lProperties.put(lClasspathKey, lClassPathProperties.get(lClasspathKey));
                }
                if(lClassPathVersion != null){
                    // Always keep the version up to date with the classpathversion.
                    lProperties.put("version", lClassPathVersion);
                }
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return lProperties;
    }

    /**
     * Store the content of a user properties instance to the ms_lims properties directory.
     *
     * @param aNewProperties      The Properties instance.
     * @param aPropertiesFileName The properties filename (e.g., dbconnection.properties)
     */
    public void updateProperties(final CompomicsTools aTool, final String aPropertiesFileName, final Properties aNewProperties) {
        File lOutput;
        lOutput = new File(getApplicationFolder(aTool), aPropertiesFileName);

        // First get the existing properties.
        Properties lProperties = getProperties(aTool, aPropertiesFileName);

        // Then iterate over the new properties.
        Iterator lUpdatedProperties = aNewProperties.keySet().iterator();
        while (lUpdatedProperties.hasNext()) {
            // Replace existing entries, include old entries.
            Object aKey = lUpdatedProperties.next();
            lProperties.put(aKey, aNewProperties.get(aKey));
        }

        try {
            if (!lOutput.exists()) {
                lOutput.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(lOutput);
            lProperties.store(fos, aPropertiesFileName + " properties file");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
