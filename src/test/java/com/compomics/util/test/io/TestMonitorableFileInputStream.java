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

import com.compomics.util.io.file.MonitorableFileInputStream;
import com.compomics.util.io.file.MonitorableInputStream;
import com.compomics.util.junit.TestCaseLM;
import org.junit.Assert;
import junit.framework.TestCase;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
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
 * This class implements the test scenario for the MonitorableFileInpuStream
 * class.
 *
 * @author Lennart Martens
 * @see com.compomics.util.io.file.MonitorableFileInputStream
 */
public class TestMonitorableFileInputStream extends TestCase {

    // Class specific log4j logger for TestMonitorableFileInputStream instances.
    Logger logger = LogManager.getLogger(TestMonitorableFileInputStream.class);

    public TestMonitorableFileInputStream() {
        this("Test scenario for the MonitorableFileInputStream.");
    }

    public TestMonitorableFileInputStream(String aName) {
        super(aName);
    }

    /**
     * This method test the monitoring of the InputStream.
     */
    public void testMonitoring() {
        // First just the InputStream.
        final String input = TestCaseLM.getFullFilePath("fastaFile.fas").replace("%20", " ");
        try {
            MonitorableInputStream mis = new MonitorableFileInputStream(input);
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
     * This method test the monitoring of the InputStream for a zip file.
     */
    public void testMonitoringZipfile() {
        // First just the InputStream.
        final String input = TestCaseLM.getFullFilePath("testMonitor.zip").replace("%20", " ");
        try {
            File inputFile = new File(input);
            MonitorableInputStream mis = new MonitorableFileInputStream(input);
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
            mis.close();
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
            File inputFile = new File(input);
            MonitorableInputStream mis = new MonitorableFileInputStream(input);
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
