package com.compomics.util.junit;

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 4-sep-02
 * Time: 10:34:31
 */

import java.io.*;
import java.net.URL;
import java.util.Properties;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/05/01 11:11:52 $
 */

/**
 * This TestCase extension contains some useful methods for loading Properties files
 * from the classpath and getting general files' full name or an InputStream from
 * the classpath.
 *
 * @author  Lennart Martens
 */
public class TestCaseLM {

    private TestCaseLM() {
    }

    /**
     * This method will read a Properties file from the current ClassLoader
     * and return a Properties instance with the Properties from the file. <br>
     * Note: if this doesn't succeed, it'll throw an IllegalArgumentException with
     * some information.
     *
     * @param   aFilename   String with the filename to locate.
     * @return  Properties  instance with the props (or IllegalArgumentException if
     *                      the specified file was not found in the classpath!)
     */
    public static Properties getPropertiesFile(String aFilename) throws IllegalArgumentException {
        Properties props = new Properties();
        try {
            ClassLoader cl = TestCaseLM.class.getClassLoader();
            InputStream is = cl.getResourceAsStream(aFilename);
            if(is == null) {
                throw new IllegalArgumentException("File '" + aFilename + "' was not found in the classpath!");
            } else {
                props.load(is);
                is.close();
            }
        } catch(IOException ioe) {
            throw new IllegalArgumentException("IOException for file '" + aFilename + "': "
                                                + ioe.getMessage());
        }
        return props;
    }

    /**
     * This method finds a file from the current classpath and attempts to reconstruct its
     * full filename. It should work on UNIX as well as Windows platforms. <br>
     * Note that a file that is not found results in an IllegalArgumentException with a
     * message.
     *
     * @param   aFilename   String with the filename to locate.
     * @return  String  with the full pathname for the file or an IllegalArgumentException if
     *                  the file was not found in the classpath.
     */
    public static String getFullFilePath(String aFilename) {
        String result = null;
        ClassLoader cl = TestCaseLM.class.getClassLoader();
        URL url = cl.getResource(aFilename);
        if(url == null) {
            throw new IllegalArgumentException("File '" + aFilename + "' was not found in the classpath!");
        } else {
            result = url.getFile();
            // Corrections for Windows platforms.
            if(File.separatorChar != '/') {
                // Windows platform. Delete the leading '/'
                result = result.substring(1);
            }
            // And this corrects for spaces...
            while(result.indexOf("%20") >= 0) {
                int start = result.indexOf("%20");
                String temp = result.substring(0, start) + " " + result.substring(start+3);
                result = temp;
            }
        }
        return result;
    }
}