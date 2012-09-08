/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.test.general.servlet;

import com.compomics.util.junit.TestCaseLM;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class test the MassCalcServlet.
 *
 * @see com.compomics.util.general.servlet.MassCalcServlet
 * @author	Lennart Martens
 */
public class TstMassCalcServlet extends TestCase {

    // Class specific log4j logger for TstMassCalcServlet instances.
    Logger logger = Logger.getLogger(TstMassCalcServlet.class);

    public TstMassCalcServlet() {
        super("This class test the MassCalcServlet.");
    }

    public TstMassCalcServlet(String aName) {
        super(aName);
    }

    /**
     * This method does not perform an exhaustive test as of yet.
     * Maybe some day it will.
     */
    public void testMassCalcServlet() {
        try {
            Properties p = TestCaseLM.getPropertiesFile("testMCServlet.properties");

            int doTest = Integer.parseInt(p.getProperty("doTest"));
            if (doTest == 0) {
                // Don't do the test, IE no internet connection present.
                return;
            } else {
                String rootURL = p.getProperty("url");
                String realURL = rootURL + "?SEQUENCE=CH3CH2COOH&MASSLISTCHOICE=MONOBIOCHEM";
                URL url = new URL(realURL);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = null;

                boolean test1 = false;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("74.03678") >= 0) {
                        test1 = true;
                    }
                }
                br.close();

                boolean test2 = false;
                realURL = rootURL + "?SEQUENCE=YSFVATAER&MASSLISTCHOICE=MONOAA";
                url = new URL(realURL);
                br = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("1042.508345") >= 0) {
                        test2 = true;
                    }
                }
                br.close();

                boolean test3 = false;
                realURL = rootURL + "?SEQUENCE=RWX&MASSLISTCHOICE=SELFLIST&SELFDEFINEDLIST=R:4_W:2_X:1";
                url = new URL(realURL);
                br = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("7.0") >= 0) {
                        test3 = true;
                    }
                }
                br.close();

                if (test1 && test2 && test3) {
                    // Both succeeded.
                    return;
                }
                // At least one failed.
                fail("The servlet failed to answer correctly.\nResult BIOCHEM: "
                        + test1 + ".\nResult AA: " + test2
                        + ".\nResult self: " + test3 + ".\n");
            }
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            fail("IO went wrong in testMasCalcServlet!");
        }
    }

    /**
     * This method test the additive use of the self-defined list.
     */
    public void testAddSelfList() {
        try {
            Properties p = TestCaseLM.getPropertiesFile("testMCServlet.properties");

            int doTest = Integer.parseInt(p.getProperty("doTest"));
            if (doTest == 0) {
                // Don't do the test, IE no internet connection present.
                return;
            } else {
                String rootURL = p.getProperty("url");

                // First test the absence of a list.
                // It should behave as normal.
                boolean test = false;
                String realURL = rootURL + "?SEQUENCE=YSFVATAER&MASSLISTCHOICE=ADDSELFLISTAA&SELFDEFINEDLIST=";
                URL url = new URL(realURL);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("1042.508345") >= 0) {
                        test = true;
                    }
                }
                br.close();
                Assert.assertTrue(test);

                // Next up: a defined list present.
                // this time: plain overriding.
                test = false;
                realURL = rootURL + "?SEQUENCE=YSFVATAER&MASSLISTCHOICE=ADDSELFLISTAA&SELFDEFINEDLIST=A:72.03711_Y:164.06333";
                url = new URL(realURL);
                br = new BufferedReader(new InputStreamReader(url.openStream()));
                line = null;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("1045.508345") >= 0) {
                        test = true;
                    }
                }
                br.close();
                Assert.assertTrue(test);

                // Next up: a defined list present.
                // this time: plain adding.
                test = false;
                realURL = rootURL + "?SEQUENCE=YSFVAoTAERYo&MASSLISTCHOICE=ADDSELFLISTAA&SELFDEFINEDLIST=Ao:72.03711_Yo:164.06333";
                url = new URL(realURL);
                br = new BufferedReader(new InputStreamReader(url.openStream()));
                line = null;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("1207.571675") >= 0) {
                        test = true;
                    }
                }
                br.close();
                Assert.assertTrue(test);

                // Next up: a defined list present.
                // this time: adding and substituting.
                test = false;
                realURL = rootURL + "?SEQUENCE=YSFVAoTAERYo&MASSLISTCHOICE=ADDSELFLISTAA&SELFDEFINEDLIST=A:72.03711_Ao:72.03711_Y:164.06333_Yo:164.06333";
                url = new URL(realURL);
                br = new BufferedReader(new InputStreamReader(url.openStream()));
                line = null;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("1209.571675") >= 0) {
                        test = true;
                    }
                }
                br.close();
                Assert.assertTrue(test);

                // Next up: a defined list present.
                // this time: testing for M<Ox>.
                test = false;
                realURL = rootURL + "?SEQUENCE=YSFVM<Ox>TAER&MASSLISTCHOICE=ADDSELFLISTAA&SELFDEFINEDLIST=M<Ox>:72.03711";
                url = new URL(realURL);
                br = new BufferedReader(new InputStreamReader(url.openStream()));
                line = null;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("1043.508345") >= 0) {
                        test = true;
                    }
                }
                br.close();
                Assert.assertTrue(test);

                // Start wrapping up.
                // Standard calculation should still be correct.
                test = false;
                realURL = rootURL + "?SEQUENCE=YSFVATAER&MASSLISTCHOICE=MONOAA";
                url = new URL(realURL);
                br = new BufferedReader(new InputStreamReader(url.openStream()));
                line = null;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("1042.508345") >= 0) {
                        test = true;
                    }
                }
                br.close();
                Assert.assertTrue(test);

                // Final one.
                // Error should be reported when Ao is present in String.
                test = false;
                realURL = rootURL + "?SEQUENCE=YSFVAoTAER&MASSLISTCHOICE=MONOAA";
                url = new URL(realURL);
                br = new BufferedReader(new InputStreamReader(url.openStream()));
                line = null;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("Unknown element 'Ao' encountered") >= 0) {
                        test = true;
                    }
                }
                br.close();
                Assert.assertTrue(test);
            }
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            fail("IO went wrong in testMasCalcServlet!");
        }
    }
}
