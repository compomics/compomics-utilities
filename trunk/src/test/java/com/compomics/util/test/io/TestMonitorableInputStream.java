/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 16-okt-02
 * Time: 13:45:05
 */
package com.compomics.util.test.io;

import com.compomics.util.io.MonitorableInputStream;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements the test scenario for the MonitorableInpuStream
 * class.
 *
 * @author Lennart Martens
 * @see com.compomics.util.io.MonitorableInputStream
 */
public class TestMonitorableInputStream extends TestCase {

    // Class specific log4j logger for TestMonitorableInputStream instances.
    Logger logger = Logger.getLogger(TestMonitorableInputStream.class);

    public TestMonitorableInputStream() {
        this("Test scenario for the MonitorableInputStream.");
    }

    public TestMonitorableInputStream(String aName) {
        super(aName);
    }

    /**
     * This method test the monitoring of the InputStream.
     */
    public void testMonitoring() {
        // First just the InputStream.
        final String input = TestCaseLM.getFullFilePath("fastaFile.fas").replace("%20", " ");
        try {
            MonitorableInputStream mis = new MonitorableInputStream(new FileInputStream(input));
            int full = mis.getMaximum();
            int counter = 0;
            int read = -1;
            while((read = mis.read()) != -1) {
                counter++;
                Assert.assertEquals(counter, mis.monitorProgress());
            }
            mis.close();
            Assert.assertEquals(full, counter);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the MonitorableInputStream: "+ ioe.getMessage() + ".");
        }
    }

    /**
     * This method test the monitoring of the InputStream when a maximum size has been set.
     */
    public void testMonitoringWithMaxSet() {
        // First just the InputStream.
        final String input = TestCaseLM.getFullFilePath("fastaFile.fas").replace("%20", " ");
        try {
            File inputFile = new File(input);
            int max = (int)inputFile.length();
            MonitorableInputStream mis = new MonitorableInputStream(new FileInputStream(input), max);
            int full = mis.getMaximum();
            Assert.assertEquals("Set maximum readable size was not returned by MonitorableInputStream!", max, full);
            int counter = 0;
            int read = -1;
            while((read = mis.read()) != -1) {
                counter++;
                Assert.assertEquals(counter, mis.monitorProgress());
            }
            mis.close();
            Assert.assertEquals(full, counter);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the MonitorableInputStream: "+ ioe.getMessage() + ".");
        }
    }

    /**
     * This method test the monitoring of the InputStream for a zip file.
     */
    public void testMonitoringZipfile() {
        // First just the InputStream.
        final String input = TestCaseLM.getFullFilePath("testMonitor.zip").replace("%20", " ");
        try {
            MonitorableInputStream mis = new MonitorableInputStream(new FileInputStream(input), true);
            ZipInputStream zis = new ZipInputStream(mis);
            ZipEntry ze = zis.getNextEntry();
            int full = mis.getMaximum();
            int counter = 0;
            int read = -1;
            while((read = zis.read()) != -1) {
                counter++;
            }
            Assert.assertTrue((full>=mis.monitorProgress()) && (mis.monitorProgress()>=0));
            mis.close();
            Assert.assertEquals(ze.getSize(), counter);
            zis.close();
        } catch(IOException ioe) {
            fail("IOException occurred while testing the MonitorableInputStream: "+ ioe.getMessage() + ".");
        }
    }

    /**
     * This method test the monitoring of the InputStream for a gzip file.
     */
    public void testMonitoringGZIPfile() {
        // First just the InputStream.
        final String input = TestCaseLM.getFullFilePath("test.spr.gz").replace("%20", " ");
        try {
            MonitorableInputStream mis = new MonitorableInputStream(new FileInputStream(input), true);
            GZIPInputStream zis = new GZIPInputStream(mis);
            int full = mis.getMaximum();
            int counter = 0;
            int read = -1;
            while((read = zis.read()) != -1) {
                counter++;
            }
            Assert.assertTrue((full>=mis.monitorProgress()) && (mis.monitorProgress()>=0));
            zis.close();
            mis.close();
        } catch(IOException ioe) {
            fail("IOException occurred while testing the MonitorableInputStream: "+ ioe.getMessage() + ".");
        }
    }
}
