/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-nov-02
 * Time: 16:17:26
 */
package com.compomics.util.test.io;

import com.compomics.util.io.file.FilenameExtensionFilter;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.File;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements the test scenario for the FilenameExtensionFilter class.
 *
 * @author Lennart Martens
 * @see com.compomics.util.io.file.FilenameExtensionFilter
 */
public class TestFilenameExtensionFilter extends TestCase {

    // Class specific log4j logger for TestFilenameExtensionFilter instances.
    Logger logger = Logger.getLogger(TestFilenameExtensionFilter.class);

    public TestFilenameExtensionFilter() {
        this("Test for the FilenameExtensionFilter class.");
    }

    public TestFilenameExtensionFilter(String aName) {
        super(aName);
    }

    /**
     * This method test the filter.
     */
    public void testFilter() {
        final String filter = ".properties";
        int counter = 0;

        // First get the data we need to verify the results afterwards.
        File f = new File(TestCaseLM.getFullFilePath("FTPClient.properties").replace("%20", " ")).getParentFile();
        String[] names = f.list();
        for(int i = 0; i < names.length; i++) {
            String lName = names[i];
            if(lName.endsWith(filter)) {
                counter++;
            }
        }

        // Okay, we now know how many results we can expect.
        // Test the filter.
        FilenameExtensionFilter fef = new FilenameExtensionFilter(filter);
        names = f.list(fef);
        Assert.assertEquals(counter, names.length);
        // Test the filter without the leading '.'.
        fef = new FilenameExtensionFilter(filter.substring(1));
        names = f.list(fef);
        Assert.assertEquals(counter, names.length);

        // Test the filter with the leading '.' and a leading '*'.
        fef = new FilenameExtensionFilter("*" + filter);
        names = f.list(fef);
        Assert.assertEquals(counter, names.length);

        // Test the filter without the leading '.', but with a leading '*'.
        fef = new FilenameExtensionFilter("*" + filter.substring(1));
        names = f.list(fef);
        Assert.assertEquals(counter, names.length);
    }
}
