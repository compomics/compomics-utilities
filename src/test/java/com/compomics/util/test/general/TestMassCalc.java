/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.test.general;

import com.compomics.util.general.MassCalc;
import com.compomics.util.general.UnknownElementMassException;
import com.compomics.util.junit.TestCaseLM;
import org.junit.Assert;
import junit.framework.TestCase;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * Test for the MassCalc class.
 * 
 * @see com.compomics.util.general.MassCalc
 * @author	Lennart Martens
 */
public class TestMassCalc extends TestCase {

    // Class specific log4j logger for TestMassCalc instances.
    Logger logger = LogManager.getLogger(TestMassCalc.class);

    public TestMassCalc() {
        this("Test for the MassCalc class.");
    }

    public TestMassCalc(String aName) {
        super(aName);
    }

    /**
     * This method test the actual calculation algorithm.
     */
    public void testCalculateMass() {
        MassCalc mc = new MassCalc();
        try {
            Properties p = TestCaseLM.getPropertiesFile("testMassCalc.properties");
            Iterator iter = p.keySet().iterator();
            while (iter.hasNext()) {
                String formula = (String) iter.next();
                double mass = (Double.valueOf(p.getProperty(formula))).doubleValue();
                Assert.assertEquals(mass, mc.calculateMass(formula), 1e-10);
            }
        } catch (UnknownElementMassException uem) {
            logger.error(uem.getMessage(), uem);
            fail(uem.getMessage());
        }
    }

    /**
     * This method test the correct exception generation if an unknown
     * element is presented.
     */
    public void testExceptionGeneration() {
        try {
            new MassCalc().calculateMass("CH3CH2ZzOH");
            fail("Element Zz was passed and it should throw an UnknownElementMassException!");
        } catch (UnknownElementMassException uem) {
            // All clear, this is what's supposed to happen.
        }
    }

    /**
     * This method test the setting of the elementlist to use via constructor.
     * <br />
     * All possible settings are tested.
     */
    public void testSettingElementList() {
        try {
            Assert.assertEquals(74.03678, new MassCalc(MassCalc.MONOELEMENTS).calculateMass("CH3CH2COOH"), 1e-10);
            Assert.assertEquals(1042.508345, new MassCalc(MassCalc.MONOAA).calculateMass("YSFVATAER"), 1e-10);
            Assert.assertEquals(5523.659513, new MassCalc(MassCalc.MONONUCLEOTIDES).calculateMass("AGCTAGCTAGCTAGCTAG"), 1e-10);
        } catch (UnknownElementMassException uem) {
            logger.error(uem.getMessage(), uem);
            fail(uem.getMessage());
        }
    }

    /**
     * This method test the setting of a self-defined elementlist as a file.
     */
    public void testSelfDefinedFileList() {
        try {
            MassCalc mc = new MassCalc("testSelfDefinedList.properties");
            Assert.assertEquals(7.0, mc.calculateMass("RWX"), 1e-10);
            Assert.assertEquals(5.0, mc.calculateMass("RX"), 1e-10);
            Assert.assertEquals(6.0, mc.calculateMass("RW"), 1e-10);
            Assert.assertEquals(4.0, mc.calculateMass("R"), 1e-10);
            Assert.assertEquals(2.0, mc.calculateMass("W"), 1e-10);
            Assert.assertEquals(1.0, mc.calculateMass("X"), 1e-10);
            Assert.assertEquals(3.0, mc.calculateMass("WX"), 1e-10);
        } catch (UnknownElementMassException uem) {
            logger.error(uem.getMessage(), uem);
            fail(uem.getMessage());
        }
    }

    /**
     * This method test the setting of a self-defined elementlist as a HashMap.
     */
    public void testSelfDefinedHashMapList() {
        try {
            HashMap hm = new HashMap(3);
            hm.put("R", Double.valueOf(4));
            hm.put("W", Double.valueOf(2));
            hm.put("X", Double.valueOf(1));
            MassCalc mc = new MassCalc(hm);
            Assert.assertEquals(7.0, mc.calculateMass("RWX"), 1e-10);
            Assert.assertEquals(5.0, mc.calculateMass("RX"), 1e-10);
            Assert.assertEquals(6.0, mc.calculateMass("RW"), 1e-10);
            Assert.assertEquals(4.0, mc.calculateMass("R"), 1e-10);
            Assert.assertEquals(2.0, mc.calculateMass("W"), 1e-10);
            Assert.assertEquals(1.0, mc.calculateMass("X"), 1e-10);
            Assert.assertEquals(3.0, mc.calculateMass("WX"), 1e-10);
        } catch (UnknownElementMassException uem) {
            logger.error(uem.getMessage(), uem);
            fail(uem.getMessage());
        }
    }

    /**
     * This method test the choosing of an elementlist and then the subsequent
     * addition/overriding of user-defined elements in a HashMap.
     */
    public void testAddSelfDefined() {
        try {

            Properties elProps = TestCaseLM.getPropertiesFile("testAddSelfDefinedList_BiochemElements.properties");
            Properties aaProps = TestCaseLM.getPropertiesFile("testAddSelfDefinedList_AA.properties");

            // First for the biochemical elements list.
            HashMap hm = new HashMap();
            Iterator it = elProps.keySet().iterator();
            while (it.hasNext()) {
                Object o = it.next();
                hm.put(o, Double.valueOf(elProps.getProperty((String) o)));
            }

            MassCalc mc = new MassCalc(MassCalc.MONOELEMENTS, hm);
            Assert.assertEquals(74.03678, mc.calculateMass("CH3CH2COOH"), 1e-10);
            Assert.assertEquals(59.96673, mc.calculateMass("SiO2"), 1e-10);
            Assert.assertEquals(95.941811, mc.calculateMass("CH3Br"), 1e-10);

            // Now for the AA list.
            hm = new HashMap();
            it = aaProps.keySet().iterator();
            while (it.hasNext()) {
                Object o = it.next();
                hm.put(o, Double.valueOf(aaProps.getProperty((String) o)));
            }

            mc = new MassCalc(MassCalc.MONOAA, hm);
            Assert.assertEquals(1042.508345, mc.calculateMass("YSFVATAER"), 0.00001);
            Assert.assertEquals(1102.511725, mc.calculateMass("YSFVMTAER"), 0.00001);
            Assert.assertEquals(1120.511725, mc.calculateMass("YSFVMoTAER"), 0.00001);
            Assert.assertEquals(1159.550545, mc.calculateMass("YSFVWTAER"), 0.00001);
        } catch (UnknownElementMassException uem) {
            logger.error(uem.getMessage(), uem);
            fail(uem.getMessage());
        }
    }

    /**
     * This method specifically test the parsing of modifications,
     * appended to AA through the use of '<>' enclosure.
     */
    public void testParsingOfAAModifications() {
        // Regular one.
        try {
            MassCalc mc = new MassCalc(MassCalc.MONOAA);
            Assert.assertEquals(1102.511725, mc.calculateMass("YSFVMTAER"), 1e-10);
            // This one must fail, but it must fail on the element 'M<Ox>'!
            Assert.assertEquals(1102.511725, mc.calculateMass("YSFVM<Ox>TAER"), 1e-10);
            // If we get here: panic!
            fail("'calculateMass' method should've failed on M<Ox> but apparently didn't!\n");
        } catch (UnknownElementMassException uem) {
            // See if the element we failed on was 'M<Ox>'.
            Assert.assertEquals("M<Ox>", uem.getElement());
        }

        // One at the start.
        try {
            MassCalc mc = new MassCalc(MassCalc.MONOAA);
            Assert.assertEquals(1102.511725, mc.calculateMass("YSFVMTAER"), 1e-10);
            // This one must fail, but it must fail on the element 'M<Ox>'!
            Assert.assertEquals(1102.511725, mc.calculateMass("M<Ox>YSFVTAER"), 1e-10);
            // If we get here: panic!
            fail("'calculateMass' method should've failed on M<Ox> but apparently didn't!\n");
        } catch (UnknownElementMassException uem) {
            // See if the element we failed on was 'M<Ox>'.
            Assert.assertEquals("M<Ox>", uem.getElement());
        }

        // One at the end.
        try {
            MassCalc mc = new MassCalc(MassCalc.MONOAA);
            Assert.assertEquals(1102.511725, mc.calculateMass("YSFVMTAER"), 1e-10);
            // This one must fail, but it must fail on the element 'M<Ox>'!
            Assert.assertEquals(1102.511725, mc.calculateMass("YSFVTAERM<Ox>"), 1e-10);
            // If we get here: panic!
            fail("'calculateMass' method should've failed on M<Ox> but apparently didn't!\n");
        } catch (UnknownElementMassException uem) {
            // See if the element we failed on was 'M<Ox>'.
            Assert.assertEquals("M<Ox>", uem.getElement());
        }
    }
}
