/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-nov-02
 * Time: 15:46:29
 */
package com.compomics.util.test.io;
import org.apache.log4j.Logger;

import junit.TestCaseLM;
import com.compomics.util.io.FolderMonitor;

import java.io.File;
import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements the test scenario for the FolderMonitor class.
 *
 * @author Lennart Martens
 * @see com.compomics.util.io.FolderMonitor
 */
public class TestFolderMonitor extends TestCaseLM {

    // Class specific log4j logger for TestFolderMonitor instances.
    Logger logger = Logger.getLogger(TestFolderMonitor.class);

    public TestFolderMonitor() {
        this("Test scenario for the FolderMonitor class.");
    }

    public TestFolderMonitor(String aName) {
        super(aName);
    }

    /**
     * This method test the validations done by the constructor.
     */
    public void testConstructorValidations() {
        // This one is correct, no exception is expected.
        try {
            HashMap params = new HashMap(3);
            params.put(FolderMonitor.HOST,  "host");
            params.put(FolderMonitor.USER,  "user");
            params.put(FolderMonitor.PASSWORD,  "password");
            FolderMonitor fm = new FolderMonitor(new File(super.getFullFilePath("FTPClient.properties").replace("%20", " ")).getParentFile(), 1000, FolderMonitor.FTP_TO_SPECIFIED_DESTINATION, params);
        } catch(IllegalArgumentException iae) {
            fail("The constructor for FolderMonitor threw an IllegalArgumentException when confronted with correct data! " + iae.getMessage());
        }

        // This one is correct, no exception is expected.
        try {
            HashMap params = new HashMap(3);
            params.put(FolderMonitor.HOST,  "host");
            params.put(FolderMonitor.USER,  "user");
            params.put(FolderMonitor.PASSWORD,  "password");
            FolderMonitor fm = new FolderMonitor(new File(super.getFullFilePath("FTPClient.properties").replace("%20", " ")).getParentFile(), 1000, ".whatever", FolderMonitor.FTP_TO_SPECIFIED_DESTINATION, params);
        } catch(IllegalArgumentException iae) {
            fail("The constructor for FolderMonitor threw an IllegalArgumentException when confronted with correct data! " + iae.getMessage());
        }

        // All the wrong ones.
        try {
            HashMap hm = new HashMap(3);
            hm.put(FolderMonitor.HOST,  "host");
            hm.put(FolderMonitor.USER,  "user");
            hm.put(FolderMonitor.PASSWORD,  "password");

            FolderMonitor fm = new FolderMonitor(new File("no_such_fodler_does_in_fact_exist"), 1000, FolderMonitor.FTP_TO_SPECIFIED_DESTINATION, hm);
            fail("No IllegalArgumentException thrown by FolderMonitor constructor when confronted by a non-existant folder!");
        } catch(IllegalArgumentException iae) {
            // We want this to happen.
        }

        try {
            HashMap hm = new HashMap(3);
            hm.put(FolderMonitor.HOST,  "host");
            hm.put(FolderMonitor.USER,  "user");
            hm.put(FolderMonitor.PASSWORD,  "password");

            FolderMonitor fm = new FolderMonitor(new File(super.getFullFilePath("FTPClient.properties")), 1000, FolderMonitor.FTP_TO_SPECIFIED_DESTINATION, hm);
            fail("No IllegalArgumentException thrown by FolderMonitor constructor when confronted by a file instead of a directory!");
        } catch(IllegalArgumentException iae) {
            // We want this to happen.
        }

        try {
            HashMap hm = new HashMap(3);
            hm.put(FolderMonitor.HOST,  "host");
            hm.put(FolderMonitor.USER,  "user");
            hm.put(FolderMonitor.PASSWORD,  "password");

            FolderMonitor fm = new FolderMonitor(new File(super.getFullFilePath("FTPClient.properties")), 1000, -1, hm);
            fail("No IllegalArgumentException thrown by FolderMonitor constructor when confronted by an unknown operation code (-1)!");
        } catch(IllegalArgumentException iae) {
            // We want this to happen.
        }

        try {
            HashMap hm = new HashMap(3);
            hm.put(FolderMonitor.USER,  "user");
            hm.put(FolderMonitor.PASSWORD,  "password");

            FolderMonitor fm = new FolderMonitor(new File(super.getFullFilePath("FTPClient.properties")).getParentFile(), 1000, FolderMonitor.FTP_TO_SPECIFIED_DESTINATION, hm);
            fail("No IllegalArgumentException thrown by FolderMonitor constructor when confronted by missing HOST parameter!");
        } catch(IllegalArgumentException iae) {
            // We want this to happen.
        }

        try {
            HashMap hm = new HashMap(3);
            hm.put(FolderMonitor.HOST,  "host");
            hm.put(FolderMonitor.PASSWORD,  "password");

            FolderMonitor fm = new FolderMonitor(new File(super.getFullFilePath("FTPClient.properties")).getParentFile(), 1000, FolderMonitor.FTP_TO_SPECIFIED_DESTINATION, hm);
            fail("No IllegalArgumentException thrown by FolderMonitor constructor when confronted by missing USER parameter!");
        } catch(IllegalArgumentException iae) {
            // We want this to happen.
        }

        try {
            HashMap hm = new HashMap(3);
            hm.put(FolderMonitor.HOST,  "host");
            hm.put(FolderMonitor.USER,  "user");

            FolderMonitor fm = new FolderMonitor(new File(super.getFullFilePath("FTPClient.properties")).getParentFile(), 1000, FolderMonitor.FTP_TO_SPECIFIED_DESTINATION, hm);
            fail("No IllegalArgumentException thrown by FolderMonitor constructor when confronted by missing PASSWORD parameter!");
        } catch(IllegalArgumentException iae) {
            // We want this to happen.
        }
    }
}
