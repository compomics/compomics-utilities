/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 27-sep-02
 * Time: 14:18:50
 */
package com.compomics.util.test.io;
import org.apache.log4j.Logger;

import junit.TestCaseLM;
import com.compomics.util.io.PushBackStringReader;
import junit.framework.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class provides the full test scenario for the PushBackStringReader.
 *
 * @author Lennart Martens
 * @see com.compomics.util.io.PushBackStringReader
 */
public class TestPushBackStringReader extends TestCaseLM {
	// Class specific log4j logger for TestPushBackStringReader instances.
	Logger logger = Logger.getLogger(TestPushBackStringReader.class);

    public TestPushBackStringReader() {
        this("Test scenario for the PushBackStringReader");
    }

    public TestPushBackStringReader(String aName) {
        super(aName);
    }

    /**
     * This method test the line reading behaviour of the
     * Reader.
     */
    public void testReading() {
        final String intro = "This is a test.";
        final String one = "1";
        final String two = "2";
        final String three = "3 4 5";
        final String four = " 6 7 8 9  -testing.";

        final String toRead = intro + "\n" + one + "\n" + two + "\n" + three + "\n" + four;
        PushBackStringReader pbr = new PushBackStringReader(toRead);

        String s1 = pbr.readLine();
        Assert.assertEquals(intro, s1);
        String s2 = pbr.readLine();
        Assert.assertEquals(one, s2);
        String s3 = pbr.readLine();
        Assert.assertEquals(two, s3);
        String s4 = pbr.readLine();
        Assert.assertEquals(three, s4);
        String s5 = pbr.readLine();
        Assert.assertEquals(four, s5);
        String s6 = pbr.readLine();
        Assert.assertTrue(s6 == null);
    }

    /**
     * This method test the line unreading behaviour fo the Reader.
     */
    public void testUnreading() {
        final String intro = "This is a test.";
        final String one = "1";
        final String two = "2";
        final String three = "3 4 5";
        final String four = " 6 7 8 9  -testing.";

        final String toRead = intro + "\n" + one + "\n" + two + "\n" + three + "\n" + four;
        PushBackStringReader pbr = new PushBackStringReader(toRead);

        String s1 = pbr.readLine();
        Assert.assertEquals(intro, s1);
        String s2 = pbr.readLine();
        Assert.assertEquals(one, s2);
        String s3 = pbr.readLine();
        Assert.assertEquals(two, s3);
        // Unread line.
        pbr.unreadLine();
        s3 = pbr.readLine();
        Assert.assertEquals(two, s3);
        // Unread two lines.
        pbr.unreadLine();
        pbr.unreadLine();
        s2 = pbr.readLine();
        Assert.assertEquals(one, s2);
        s3 = pbr.readLine();
        Assert.assertEquals(two, s3);
        // Continue reading normally.
        String s4 = pbr.readLine();
        Assert.assertEquals(three, s4);
        String s5 = pbr.readLine();
        Assert.assertEquals(four, s5);
        String s6 = pbr.readLine();
        Assert.assertTrue(s6 == null);

        // Check unreading at EOF.
        pbr.unreadLine();
        s5 = pbr.readLine();
        Assert.assertEquals(four, s5);
        s6 = pbr.readLine();
        Assert.assertTrue(s6 == null);

        // Unreading two lines at EOF.
        pbr.unreadLine();
        pbr.unreadLine();
        s4 = pbr.readLine();
        Assert.assertEquals(three, s4);
        s5 = pbr.readLine();
        Assert.assertEquals(four, s5);
        s6 = pbr.readLine();
        Assert.assertTrue(s6 == null);
    }
}
