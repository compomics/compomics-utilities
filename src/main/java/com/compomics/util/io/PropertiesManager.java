package com.compomics.util.io;

import com.compomics.util.enumeration.CompomicsTools;
import org.apache.log4j.*;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;


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
            is = getResource(aPropertiesFileName);
            Properties lClassPathProperties = new Properties();
            lClassPathProperties.load(is);
            is.close();

            Object lVersion = lClassPathProperties.get("version");
            String lClassPathVersion = null;

            if (lVersion != null) {
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
                is.close();
            } else {
                // And, write the content of this properties file to the user home directory for the next request.
                try {
                    File lOutput;
                    lOutput = getFile(aTool, aPropertiesFileName);
                    if (!lOutput.exists()) {
                        lOutput.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(lOutput);
                    lClassPathProperties.store(fos, aPropertiesFileName + " properties file");
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }

            // During updates, for each property that was is in the classpathversion,
            // but not in the local properties - store the entry to the local file.

            Set lClassPathPropertyKeySet = lClassPathProperties.keySet();
            boolean lRequireDiskUpdate = false;
            for (Iterator lIterator = lClassPathPropertyKeySet.iterator(); lIterator.hasNext();) {
                Object lClasspathKey = lIterator.next();
                if (lProperties.get(lClasspathKey) == null) {
                    lProperties.put(lClasspathKey, lClassPathProperties.get(lClasspathKey));
                    lRequireDiskUpdate = true;
                }

            }
            if (lClassPathVersion != null) {
                // Always keep the version up to date with the classpathversion.
                lProperties.put("version", lClassPathVersion);
                lRequireDiskUpdate = true;
            }

            // If required, then update the properties file on disk!
            if(lRequireDiskUpdate){
                storeProperties(aPropertiesFileName, aTool, lProperties);
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return lProperties;
    }

    /**
     * Private method that attempts to return an inputstream resource for a given filename.
     *
     * @param aResourceFilename
     * @return InputStream (null if not found)
     */
    private InputStream getResource(final String aResourceFilename) {
        InputStream is;
        is = ClassLoader.getSystemResourceAsStream(aResourceFilename);
        if (is == null) {
            is = this.getClass().getClassLoader().getResourceAsStream(aResourceFilename);
        }
        return is;
    }

    /**
     * Update the content of a user properties instance to the ms_lims properties directory.
     *
     * @param aNewProperties      The Properties instance.
     * @param aPropertiesFileName The properties filename (e.g., dbconnection.properties)
     */
    public void updateProperties(final CompomicsTools aTool, final String aPropertiesFileName, final Properties aNewProperties) {

        // First get the existing properties.
        Properties lProperties = getProperties(aTool, aPropertiesFileName);

        // Then iterate over the new properties.
        Iterator lUpdatedProperties = aNewProperties.keySet().iterator();
        while (lUpdatedProperties.hasNext()) {
            // Replace existing entries, include old entries.
            Object aKey = lUpdatedProperties.next();
            lProperties.put(aKey, aNewProperties.get(aKey));
        }

        storeProperties(aPropertiesFileName, aTool, lProperties);
    }

    /**
     * Returns the File handler for the given compomics tool and filename.
     * @param aTool
     * @param aPropertiesFileName
     * @return
     */
    private File getFile(CompomicsTools aTool, String aPropertiesFileName) {
        return new File(getApplicationFolder(aTool), aPropertiesFileName);
    }

    /**
     * Store the given Properties instance to the given direction.
     * @param aPropertiesFileName
     * @param aTool
     * @param lProperties
     */
    private void storeProperties(String aPropertiesFileName, CompomicsTools aTool, Properties lProperties) {
        File lOutput = null;
        lOutput = getFile(aTool, aPropertiesFileName);
        try {
            if (!lOutput.exists()) {
                lOutput.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(lOutput);
            lProperties.store(fos, aPropertiesFileName + " properties file");
            fos.flush();
            fos.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method will delete the log4j log file in the folder of the package and will create a log file in the
     * CompomicsTools specific .compomics folder
     *
     * @param aCompomicsTools The tool
     */
    public void updateLog4jConfiguration(final org.apache.log4j.Logger aLogger, CompomicsTools aCompomicsTools) {
        Properties props = new Properties();
        try {
            InputStream configStream = getResource("log4j.properties");
            props.load(configStream);
            configStream.close();
        } catch (IOException e) {
            System.out.println("Error: Cannot load configuration file ");
        }
        String lFileKey = "log4j.appender.file.File";
        String lOldLogFileName = props.getProperty(lFileKey);
        String lNewLogFileName = getApplicationFolder(aCompomicsTools).getAbsolutePath() + File.separator + aCompomicsTools.getName() + "-log4j.log";
        RollingFileAppender lRollingFileAppender = (RollingFileAppender) aLogger.getParent().getAppender("file");
        lRollingFileAppender.setFile(lNewLogFileName);
        lRollingFileAppender.activateOptions();
        File lOldLogFile = new File(lOldLogFileName);
        if (lOldLogFile.exists()) {
            lOldLogFile.delete();
        }

        // Make all remaining System.err go into the logger as well.
        PrintStream stderrStream = System.err;
        PrintStream newStderrStream = new PrintStream(stderrStream) {

            @Override
            public void println(Object x) {
                if (x instanceof Throwable) {
                    Throwable t = (Throwable) x;
                    aLogger.error(t.getMessage());
                    StackTraceElement[] lElements = t.getStackTrace();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < lElements.length; i++) {
                        StackTraceElement lElement = lElements[i];
                        sb.append(lElement.toString() +"\n");
                    }
                    aLogger.error(sb.toString());
                }
                super.println(x);
            }
        };
        System.setErr(newStderrStream);
    }
}
