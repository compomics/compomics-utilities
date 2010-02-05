/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.test.general;


import java.util.*;
import junit.framework.*;

import com.compomics.util.general.MassCalc;
import com.compomics.util.general.UnknownElementMassException;
import junit.TestCaseLM;

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
public class TestMassCalc extends TestCaseLM {
	
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
            Properties p = super.getPropertiesFile("testMassCalc.properties");
            Iterator iter = p.keySet().iterator();
            while(iter.hasNext()) {
                String formula = (String)iter.next();
                double mass = (new Double(p.getProperty(formula))).doubleValue();
                Assert.assertEquals(mass, mc.calculateMass(formula), 1e-10);
            }
        } catch(UnknownElementMassException uem) {
            uem.printStackTrace();
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
		} catch(UnknownElementMassException uem) {
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
			Assert.assertEquals(74.03678, new MassCalc(MassCalc.MONOELEMENTS).calculateMass("CH3CH2COOH"), Double.MIN_VALUE*2);
			Assert.assertEquals(1042.508345, new MassCalc(MassCalc.MONOAA).calculateMass("YSFVATAER"), Double.MIN_VALUE*2);
            Assert.assertEquals(5523.659513, new MassCalc(MassCalc.MONONUCLEOTIDES).calculateMass("AGCTAGCTAGCTAGCTAG"), Double.MIN_VALUE*2);
		} catch(UnknownElementMassException uem) {
			uem.printStackTrace();
			fail(uem.getMessage());
		}
	}
	
	/**
	 * This method test the setting of a self-defined elementlist as a file.
	 */
	public void testSelfDefinedFileList() {
		try {
			MassCalc mc = new MassCalc("testSelfDefinedList.properties");
			Assert.assertEquals(7.0, mc.calculateMass("RWX"), Double.MIN_VALUE*2);
			Assert.assertEquals(5.0, mc.calculateMass("RX"), Double.MIN_VALUE*2);
			Assert.assertEquals(6.0, mc.calculateMass("RW"), Double.MIN_VALUE*2);
			Assert.assertEquals(4.0, mc.calculateMass("R"), Double.MIN_VALUE*2);
			Assert.assertEquals(2.0, mc.calculateMass("W"), Double.MIN_VALUE*2);
			Assert.assertEquals(1.0, mc.calculateMass("X"), Double.MIN_VALUE*2);
			Assert.assertEquals(3.0, mc.calculateMass("WX"), Double.MIN_VALUE*2);
		} catch(UnknownElementMassException uem) {
			uem.printStackTrace();
			fail(uem.getMessage());
		}
	}
	
	/**
	 * This method test the setting of a self-defined elementlist as a HashMap.
	 */
	public void testSelfDefinedHashMapList() {
		try {
			HashMap hm = new HashMap(3);
			hm.put("R", new Double(4));
			hm.put("W", new Double(2));
			hm.put("X", new Double(1));
			MassCalc mc = new MassCalc(hm);
			Assert.assertEquals(7.0, mc.calculateMass("RWX"), Double.MIN_VALUE*2);
			Assert.assertEquals(5.0, mc.calculateMass("RX"), Double.MIN_VALUE*2);
			Assert.assertEquals(6.0, mc.calculateMass("RW"), Double.MIN_VALUE*2);
			Assert.assertEquals(4.0, mc.calculateMass("R"), Double.MIN_VALUE*2);
			Assert.assertEquals(2.0, mc.calculateMass("W"), Double.MIN_VALUE*2);
			Assert.assertEquals(1.0, mc.calculateMass("X"), Double.MIN_VALUE*2);
			Assert.assertEquals(3.0, mc.calculateMass("WX"), Double.MIN_VALUE*2);
		} catch(UnknownElementMassException uem) {
			uem.printStackTrace();
			fail(uem.getMessage());
		}
	}
	
	/**
	 * This method test the choosing of an elementlist and then the subsequent
	 * addition/overriding of user-defined elements in a HashMap.
	 */
	public void testAddSelfDefined() {
		try {
			
			Properties elProps = super.getPropertiesFile("testAddSelfDefinedList_BiochemElements.properties");
			Properties aaProps = super.getPropertiesFile("testAddSelfDefinedList_AA.properties");

			// First for the biochemical elements list.
			HashMap hm = new HashMap();
			Iterator it = elProps.keySet().iterator();
			while(it.hasNext()) {
				Object o = it.next();
				hm.put(o, new Double(elProps.getProperty((String)o)));
			}
			
			MassCalc mc = new MassCalc(MassCalc.MONOELEMENTS, hm);
			Assert.assertEquals(74.03678, mc.calculateMass("CH3CH2COOH"), Double.MIN_VALUE*2);
			Assert.assertEquals(59.96673, mc.calculateMass("SiO2"), Double.MIN_VALUE*2);
			Assert.assertEquals(95.941811, mc.calculateMass("CH3Br"), Double.MIN_VALUE*2);
			
			// Now for the AA list.
			hm = new HashMap();
			it = aaProps.keySet().iterator();
			while(it.hasNext()) {
				Object o = it.next();
				hm.put(o, new Double(aaProps.getProperty((String)o)));
			}
			
			mc = new MassCalc(MassCalc.MONOAA, hm);
			Assert.assertEquals(1042.508345, mc.calculateMass("YSFVATAER"), 0.00001);
			Assert.assertEquals(1102.511725, mc.calculateMass("YSFVMTAER"), 0.00001);
			Assert.assertEquals(1120.511725, mc.calculateMass("YSFVMoTAER"), 0.00001);
			Assert.assertEquals(1159.550545, mc.calculateMass("YSFVWTAER"), 0.00001);
		} catch(UnknownElementMassException uem) {
			uem.printStackTrace();
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
			Assert.assertEquals(1102.511725, mc.calculateMass("YSFVMTAER"), Double.MIN_VALUE*2);
			// This one must fail, but it must fail on the element 'M<Ox>'!
			Assert.assertEquals(1102.511725, mc.calculateMass("YSFVM<Ox>TAER"), Double.MIN_VALUE*2);
			// If we get here: panic!
			fail("'calculateMass' method should've failed on M<Ox> but apparently didn't!\n");
		} catch(UnknownElementMassException uem) {
			// See if the element we failed on was 'M<Ox>'.
			Assert.assertEquals("M<Ox>", uem.getElement());
		}
		
		// One at the start.
		try {
			MassCalc mc = new MassCalc(MassCalc.MONOAA);
			Assert.assertEquals(1102.511725, mc.calculateMass("YSFVMTAER"), Double.MIN_VALUE*2);
			// This one must fail, but it must fail on the element 'M<Ox>'!
			Assert.assertEquals(1102.511725, mc.calculateMass("M<Ox>YSFVTAER"), Double.MIN_VALUE*2);
			// If we get here: panic!
			fail("'calculateMass' method should've failed on M<Ox> but apparently didn't!\n");
		} catch(UnknownElementMassException uem) {
			// See if the element we failed on was 'M<Ox>'.
			Assert.assertEquals("M<Ox>", uem.getElement());
		}
		
		// One at the end.
		try {
			MassCalc mc = new MassCalc(MassCalc.MONOAA);
			Assert.assertEquals(1102.511725, mc.calculateMass("YSFVMTAER"), Double.MIN_VALUE*2);
			// This one must fail, but it must fail on the element 'M<Ox>'!
			Assert.assertEquals(1102.511725, mc.calculateMass("YSFVTAERM<Ox>"), Double.MIN_VALUE*2);
			// If we get here: panic!
			fail("'calculateMass' method should've failed on M<Ox> but apparently didn't!\n");
		} catch(UnknownElementMassException uem) {
			// See if the element we failed on was 'M<Ox>'.
			Assert.assertEquals("M<Ox>", uem.getElement());
		}
	}
}
