/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 1-jul-2004
 * Time: 16:18:51
 */
package com.compomics.util.test.protein;

import com.compomics.util.protein.ModificationImplementation;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class provides the test scenario for the ModificationImplementation class.
 *
 * @see com.compomics.util.protein.ModificationImplementation
 * @author Lennart Martens
 */
public class TestModificationImplementation extends TestCase {

    // Class specific log4j logger for TestModificationImplementation instances.
    Logger logger = Logger.getLogger(TestModificationImplementation.class);

    public TestModificationImplementation() {
        this("Test Scenario for the ModificationImplementation class.");
    }

    public TestModificationImplementation(String aName) {
        super(aName);
    }

    /**
     * This method test the creation of the ModificationImplementation and
     * the simple getters.
     */
    public void testCreation() {
        HashMap massMap = new HashMap(2);
        massMap.put("R", new double[]{3.456, 3.1415});
        massMap.put("K", new double[]{6.456, 6.1415});

        // First test both constructors with correct data.
        ModificationImplementation mod = new ModificationImplementation("TestTitle", "tt", massMap, 0);
        Assert.assertEquals("TestTitle", mod.getTitle());
        Assert.assertEquals("tt", mod.getCode());
        Assert.assertEquals(0, mod.getLocation());

        mod = new ModificationImplementation("TestTitle", "tt", massMap, 3);
        Assert.assertEquals("TestTitle", mod.getTitle());
        Assert.assertEquals("tt", mod.getCode());
        Assert.assertEquals(3, mod.getLocation());

        // Let's get it to throw some IllegalArgumentExceptions.
        HashMap dodgy = new HashMap();
        dodgy.put(Integer.valueOf(3), "test");
        try {
            mod = new ModificationImplementation("test", "tt", dodgy, 1);
            fail("No IllegalArgumentException thrown when attempting to pass a HashMap with Integer key and String value!!");
        } catch(IllegalArgumentException iae) {
            // This is correct.
        }

        dodgy = new HashMap();
        dodgy.put("test", "test");
        try {
            mod = new ModificationImplementation("test", "tt", dodgy, 1);
            fail("No IllegalArgumentException thrown when attempting to pass a HashMap with String key and String value!!");
        } catch(IllegalArgumentException iae) {
            // This is correct.
        }

        dodgy = new HashMap();
        dodgy.put("test", new Object[]{});
        try {
            mod = new ModificationImplementation("test", "tt", dodgy, 1);
            fail("No IllegalArgumentException thrown when attempting to pass a HashMap with String key and empty Object[] value!!");
        } catch(IllegalArgumentException iae) {
            // This is correct.
        }

        dodgy = new HashMap();
        dodgy.put("test", new double[]{});
        try {
            mod = new ModificationImplementation("test", "tt", dodgy, 1);
            fail("No IllegalArgumentException thrown when attempting to pass a HashMap with String key and empty double[] value!!");
        } catch(IllegalArgumentException iae) {
            // This is correct.
        }

        dodgy = new HashMap();
        dodgy.put("test", new double[]{3.333});
        try {
            mod = new ModificationImplementation("test", "tt", dodgy, 1);
            fail("No IllegalArgumentException thrown when attempting to pass a HashMap with String key and double[] (length 1) value!!");
        } catch(IllegalArgumentException iae) {
            // This is correct.
        }
    }

    /**
     * This method test the massdelta getters.
     */
    public void testMassDeltaGetters() {
        // First check with two 'normal' residues.
        HashMap massMap = new HashMap(2);
        massMap.put("R", new double[]{3.456, 3.1415});
        massMap.put("K", new double[]{6.456, 6.1415});

        ModificationImplementation mod = new ModificationImplementation("TestTitle", "tt", massMap, 1);
        Assert.assertEquals(3.456, mod.getMonoisotopicMassDelta("R"), Double.MIN_VALUE);
        Assert.assertEquals(3.1415, mod.getAverageMassDelta("R"), Double.MIN_VALUE);
        Assert.assertEquals(6.456, mod.getMonoisotopicMassDelta("K"), Double.MIN_VALUE);
        Assert.assertEquals(6.1415, mod.getAverageMassDelta("K"), Double.MIN_VALUE);

        Assert.assertEquals(0.0, mod.getMonoisotopicMassDelta("H"), Double.MIN_VALUE);
        Assert.assertEquals(0.0, mod.getAverageMassDelta("H"), Double.MIN_VALUE);
        Assert.assertEquals(0.0, mod.getMonoisotopicMassDelta(ModificationImplementation.NTERMINUS), Double.MIN_VALUE);
        Assert.assertEquals(0.0, mod.getAverageMassDelta(ModificationImplementation.CTERMINUS), Double.MIN_VALUE);

        // Next check with N-terminal residue.
        massMap = new HashMap(1);
        massMap.put(ModificationImplementation.NTERMINUS, new double[]{5.4321, -6.54321});
        mod = new ModificationImplementation("TestTitle", "tt", massMap, 1);
        Assert.assertEquals(5.4321, mod.getMonoisotopicMassDelta(ModificationImplementation.NTERMINUS), Double.MIN_VALUE);
        Assert.assertEquals(-6.54321, mod.getAverageMassDelta(ModificationImplementation.NTERMINUS), Double.MIN_VALUE);

        Assert.assertEquals(0.0, mod.getMonoisotopicMassDelta(ModificationImplementation.CTERMINUS), Double.MIN_VALUE);
        Assert.assertEquals(0.0, mod.getAverageMassDelta(ModificationImplementation.CTERMINUS), Double.MIN_VALUE);
        Assert.assertEquals(0.0, mod.getMonoisotopicMassDelta("H"), Double.MIN_VALUE);
        Assert.assertEquals(0.0, mod.getAverageMassDelta("H"), Double.MIN_VALUE);

        // Finally check with C-terminal residue.
        massMap = new HashMap(1);
        massMap.put(ModificationImplementation.CTERMINUS, new double[]{-5.3421, 6.45321});
        mod = new ModificationImplementation("TestTitle", "tt", massMap, 1);
        Assert.assertEquals(-5.3421, mod.getMonoisotopicMassDelta(ModificationImplementation.CTERMINUS), Double.MIN_VALUE);
        Assert.assertEquals(6.45321, mod.getAverageMassDelta(ModificationImplementation.CTERMINUS), Double.MIN_VALUE);

        Assert.assertEquals(0.0, mod.getMonoisotopicMassDelta(ModificationImplementation.NTERMINUS), Double.MIN_VALUE);
        Assert.assertEquals(0.0, mod.getAverageMassDelta(ModificationImplementation.NTERMINUS), Double.MIN_VALUE);
        Assert.assertEquals(0.0, mod.getMonoisotopicMassDelta("H"), Double.MIN_VALUE);
        Assert.assertEquals(0.0, mod.getAverageMassDelta("H"), Double.MIN_VALUE);
    }

    /**
     * This method test the behaviour of the 'equals' and the 'hashCode' methods.
     */
    public void testEqualsAndHashCode() {
        HashMap massMap1 = new HashMap(1);
        massMap1.put(ModificationImplementation.CTERMINUS, new double[]{5.3421, 6.45321});

        HashMap massMap2 = new HashMap(1);
        massMap2.put("H", new double[]{0.01, 0.02});

        // Two mods, same title, different code and massmap, same location (1).
        ModificationImplementation mod1 = new ModificationImplementation("Title1", "tt1", massMap1, 1);
        ModificationImplementation mod2 = new ModificationImplementation("Title1", "tt2", massMap2, 1);
        Assert.assertEquals(mod1.hashCode(), mod2.hashCode());
        Assert.assertEquals(mod1, mod2);
        // Test equals with 'null'.
        Assert.assertTrue(!mod1.equals(null));
        // Test in the HashMap.
        // Now we should have an overwrite.
        HashMap testMap = new HashMap(2);
        Object kicked = testMap.put(mod1, "1");
        Assert.assertTrue(kicked == null);
        kicked = testMap.put(mod2, "2");
        Assert.assertTrue(kicked != null);
        Assert.assertEquals(kicked, "1");
        Assert.assertEquals(1, testMap.size());

        // Set the location differently. HashCode must remain the same, equals should differ.
        mod1.setLocation(1);
        mod1.setLocation(2);
        Assert.assertEquals(mod1.hashCode(), mod2.hashCode());
        Assert.assertFalse(mod1.equals(mod2));
        Assert.assertFalse(mod2.equals(mod1));
        // Test in the HashMap.
        // Now we should NOT have an overwrite (even though the hashCode is identical,
        // the equals will allow a difference to be made).
        testMap = new HashMap(2);
        kicked = testMap.put(mod1, "1");
        Assert.assertTrue(kicked == null);
        kicked = testMap.put(mod2, "2");
        Assert.assertTrue(kicked == null);
        Assert.assertEquals(2, testMap.size());

        // Now two mods with different titles.
        mod2 = new ModificationImplementation("Title2", "tt2", massMap2, 1);
        Assert.assertFalse(mod2.equals(mod1));
        Assert.assertFalse(mod1.equals(mod2));
        Assert.assertFalse(mod1.hashCode() == mod2.hashCode());
        // Test in the HashMap.
        // Now we should NOT have an overwrite (since even the hashCode is different here)
        testMap = new HashMap(2);
        kicked = testMap.put(mod1, "1");
        Assert.assertTrue(kicked == null);
        kicked = testMap.put(mod2, "2");
        Assert.assertTrue(kicked == null);
        Assert.assertEquals(2, testMap.size());
    }

    /**
     * This method test the 'equals' and 'compareTo' (interface Comparable) methods.
     */
    public void testEqualsAndCompareTo() {
        HashMap deltas = new HashMap(2);
        deltas.put("K", new double[]{3.14, 6.14});
        deltas.put("R", new double[]{7.14, 4.14});
        HashMap deltas2 = new HashMap(3);
        deltas.put("P", new double[]{9.14, 5.14});
        deltas.put("T", new double[]{2.14, 1.14});
        ModificationImplementation mip = new ModificationImplementation("Test", "tt", deltas, 1);
        // Highly trivial case.
        Assert.assertEquals(mip, mip);
        Assert.assertTrue(mip.compareTo(mip) == 0);
        // Add up the ante with a new object.
        ModificationImplementation mip2 = new ModificationImplementation("Test", "tt2", deltas2, 1);
        Assert.assertEquals(mip, mip);
        Assert.assertTrue(mip.compareTo(mip2) == 0);
        Assert.assertTrue(mip2.compareTo(mip) == 0);
        // And we go on...
        ModificationImplementation mip3 = new ModificationImplementation("Test", "tt3", deltas, 0);
        Assert.assertFalse(mip.equals(mip3));
        Assert.assertFalse(mip3.equals(mip));
        Assert.assertTrue(mip.compareTo(mip3) > 0);
        Assert.assertTrue(mip3.compareTo(mip) < 0);
        // And one more.
        ModificationImplementation mip4 = new ModificationImplementation("Test4", "tt", deltas, 1);
        // These test use title because location is the same.
        // And alphabetically, "Test4" comes after "test".
        Assert.assertFalse(mip.equals(mip4));
        Assert.assertFalse(mip4.equals(mip));
        Assert.assertTrue(mip.compareTo(mip4) < 0);
        Assert.assertTrue(mip4.compareTo(mip) > 0);
        // Now the location is different, and the order should be reversed.
        mip4.setLocation(0);
        Assert.assertFalse(mip.equals(mip4));
        Assert.assertFalse(mip4.equals(mip));
        Assert.assertTrue(mip.compareTo(mip4) > 0);
        Assert.assertTrue(mip4.compareTo(mip) < 0);
    }

    /**
     * This method test the 'clone' method.
     */
    public void testClone() {
        HashMap massMap = new HashMap(1);
        massMap.put(ModificationImplementation.CTERMINUS, new double[]{5.3421, 6.45321});
        ModificationImplementation mod = new ModificationImplementation("Title", "tt", massMap, 1);
        // Clone and test for different pointer and equality.
        ModificationImplementation modClone = (ModificationImplementation)mod.clone();
        Assert.assertFalse(modClone == mod);
        Assert.assertEquals(mod, modClone);
        Assert.assertTrue(mod.hashCode() == modClone.hashCode());
        Assert.assertFalse(mod.isArtifact());
        Assert.assertFalse(modClone.isArtifact());

        // Check changing some attributes.
        modClone.setLocation(523);
        Assert.assertEquals(1, mod.getLocation());
        Assert.assertEquals(523, modClone.getLocation());
    }
}
