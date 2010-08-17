/*
 * Copyright (C) Lennart Martens
 *
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.test.protein;
import com.compomics.util.enumeration.MolecularElement;
import org.apache.log4j.Logger;


import java.util.Vector;
import java.util.HashMap;

import junit.framework.*;

import com.compomics.util.interfaces.Sequence;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.ModificationImplementation;
import com.compomics.util.protein.ModificationFactory;
import com.compomics.util.interfaces.Modification;


/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * Test for the AASequenceImpl class
 *
 * @see com.compomics.util.protein.AASequenceImpl
 * @author	Lennart Martens
 */
public class TestAASequenceImpl extends TestCase {

    // Class specific log4j logger for TestAASequenceImpl instances.
    Logger logger = Logger.getLogger(TestAASequenceImpl.class);

    public TestAASequenceImpl() {
        this("Test for the AASequenceImpl class.");
    }

    public TestAASequenceImpl(String aMsg) {
        super(aMsg);
    }

    /**
     * This method test the correct behaviour of the constructor
     * and in the same time that of the setSequence method, since
     * it is called from the constructor. <br />
     * It also test the setSequence method separately for safety.
     */
    public void testConstructor() {
        String seqString = "YSFVATAER";
        String replaceSeq = "LENNARTMARTENS";
        // Plain constructor.
        AASequenceImpl seq = new AASequenceImpl(seqString);
        Assert.assertEquals(seqString, seq.getSequence());
        Assert.assertTrue(seq.getModifications() == null);
        seq.setSequence(replaceSeq);
        Assert.assertEquals(replaceSeq, seq.getSequence());
    }

    /**
     * This method test the isotopic distribution method.
     */
    public void testIsotopicDistribution() {
        String seqString = "YSFVATAER";
        // Plain constructor.
        AASequenceImpl seq = new AASequenceImpl(seqString);
        Assert.assertEquals(71, seq.getMolecularFormula().getElementCount(MolecularElement.H));
        Assert.assertEquals(47, seq.getMolecularFormula().getElementCount(MolecularElement.C));
        Assert.assertEquals(0.10799336006817781, seq.getIsotopicDistribution().getPercTot()[2]);
    }

    /**
     * This method test the mass calculation method.
     */
    public void testMassCalculation() {
        // Plain one.
        String seqString = "YSFVATAER";
        Sequence seq = new AASequenceImpl(seqString);
        Assert.assertEquals(1042.508345, seq.getMass(), Double.MIN_VALUE * 2);

        // Now one with modifications.

        // See if the cache is correctly emptied!
        seqString = "YSFVATAER";
        seq = new AASequenceImpl(seqString);
        Assert.assertEquals(1042.508345, seq.getMass(), Double.MIN_VALUE * 2);
        seq.setSequence("LENNARTMARTENS");
        Assert.assertEquals(1605.752915, seq.getMass(), Double.MIN_VALUE * 2);

        // See if the '_' is correctly interpreted (no errors thrown)!
        seqString = "YSFV_ATAER";
        seq = new AASequenceImpl(seqString);
        Assert.assertEquals(1042.508345, seq.getMass(), Double.MIN_VALUE * 2);
        seq.setSequence("LENNART_MARTENS");
        Assert.assertEquals(1605.752915, seq.getMass(), Double.MIN_VALUE * 2);
    }

    /**
     * This method test whether the AASequenceImpl Object
     * correctly throws a NullPointerException when presented
     * with a 'null' sequence String. And this for both the
     * constructor and the setSequence method.
     */
    public void testNullPointerException() {
        try {
            new AASequenceImpl(null);
            fail("NullPointerException should have been thrown in the constructor!\n");
        } catch (NullPointerException npe) {
            // Expected behaviour. Do nothing.
        }

        try {
            Sequence s = new AASequenceImpl("YSFVATAER");
            s.setSequence(null);
            fail("NullPointerException should have been thrown in the setSequence method!\n");
        } catch (NullPointerException npe) {
            // Expected behaviour. Do nothing.
        }
    }

    /**
     * This method test whether the AASequenceImpl Object
     * correctly throws an IllegalArgumentException when presented
     * with an '""' (empty String) sequence String. And this for both the
     * constructor and the setSequence method.
     */
    public void testIllegalArgumentException() {
        try {
            new AASequenceImpl("");
            fail("IllegalArgumentException should have been thrown in the constructor!\n");
        } catch (IllegalArgumentException iae) {
            // Expected behaviour. Do nothing.
        }

        try {
            new AASequenceImpl("   	   ");
            fail("IllegalArgumentException should have been thrown in the constructor (trimming case)!\n");
        } catch (IllegalArgumentException iae) {
            // Expected behaviour. Do nothing.
        }

        try {
            Sequence s = new AASequenceImpl("YSFVATAER");
            s.setSequence("");
            fail("IllegalArgumentException should have been thrown in the setSequence method!\n");
        } catch (IllegalArgumentException iae) {
            // Expected behaviour. Do nothing.
        }

        try {
            Sequence s = new AASequenceImpl("YSFVATAER");
            s.setSequence(" 	   ");
            fail("IllegalArgumentException should have been thrown in the setSequence method (trimming case)!\n");
        } catch (IllegalArgumentException iae) {
            // Expected behaviour. Do nothing.
        }
    }

    /**
     * This method test the trimming behaviour of both the constructor
     * and the setSequence method.
     */
    public void testTrimmingBehaviour() {
        String sequence1 = "YSFVATAER 	 ";
        String sequence2 = "	 YSFVATAER";
        String sequence3 = "	 YSFVATAER 	 	";
        String sequence = "YSFVATAER";
        Assert.assertEquals(sequence, new AASequenceImpl(sequence1).getSequence());
        Assert.assertEquals(sequence, new AASequenceImpl(sequence2).getSequence());
        Assert.assertEquals(sequence, new AASequenceImpl(sequence3).getSequence());

        Sequence asi1 = new AASequenceImpl(sequence1);
        Sequence asi2 = new AASequenceImpl(sequence2);
        Sequence asi3 = new AASequenceImpl(sequence3);

        Assert.assertEquals(sequence, asi1.getSequence());
        Assert.assertEquals(sequence, asi2.getSequence());
        Assert.assertEquals(sequence, asi3.getSequence());
    }

    /**
     * This method test the generation of the annotated sequence.
     */
    public void testModifiedSequence() {
        // First go is without modifications.
        AASequenceImpl seq = new AASequenceImpl("LENNARTMARTENS");
        String withMods = seq.getModifiedSequence();
        Assert.assertEquals("NH2-LENNARTMARTENS-COOH", withMods);

        // Okay, with modifications this time.
        Vector mods = new Vector(4);
        mods.add(new ModificationImplementation("TestMod", "Ace", new HashMap(), 0));
        mods.add(new ModificationImplementation("TestMod2", "Ox", new HashMap(), 8));
        mods.add(new ModificationImplementation("TestMod3", "Ace", new HashMap(), 6));
        mods.add(new ModificationImplementation("TestMod4", "Spe", new HashMap(), 14));
        seq = new AASequenceImpl("LENNARTMARTENS", mods);
        withMods = seq.getModifiedSequence();
        Assert.assertEquals("Ace-LENNAR<Ace>TM<Ox>ARTENS<Spe>-COOH", withMods);

        mods = new Vector(4);
        mods.add(new ModificationImplementation("TestMod", "Ace", new HashMap(), 0));
        mods.add(new ModificationImplementation("TestMod2", "Ox", new HashMap(), 8));
        mods.add(new ModificationImplementation("TestMod3", "Ace", new HashMap(), 6));
        mods.add(new ModificationImplementation("TestMod4", "Spe", new HashMap(), 14));
        mods.add(new ModificationImplementation("TestMod5", "Met", new HashMap(), 15));
        seq = new AASequenceImpl("LENNARTMARTENS", mods);
        withMods = seq.getModifiedSequence();
        Assert.assertEquals("Ace-LENNAR<Ace>TM<Ox>ARTENS<Spe>-Met", withMods);
    }

    /**
     * This method test adding a modification.
     */
    public void testAddModification() {
        AASequenceImpl seq = new AASequenceImpl("LENNARTMARTENS");
        Assert.assertEquals("NH2-LENNARTMARTENS-COOH", seq.getModifiedSequence());
        Assert.assertTrue(seq.getModifications() == null);

        seq.addModification(new ModificationImplementation("Title", "Met", new HashMap(), 15));
        Assert.assertEquals("NH2-LENNARTMARTENS-Met", seq.getModifiedSequence());
        Assert.assertTrue(seq.getModifications() != null);
        Assert.assertEquals(1, seq.getModifications().size());

        seq.addModification(new ModificationImplementation("Nterm", "Ace", new HashMap(), 0));
        seq.addModification(new ModificationImplementation("Mid", "P", new HashMap(), 7));
        Assert.assertEquals("Ace-LENNART<P>MARTENS-Met", seq.getModifiedSequence());
        Assert.assertTrue(seq.getModifications() != null);
        Assert.assertEquals(3, seq.getModifications().size());
    }

    /**
     * This method test the reading of an annotated sequence String.
     */
    public void testReadingFromAnnotatedSequenceString() {
        // These should all work.
        String annotated = "NH2-YSFVATAER-COOH";
        AASequenceImpl p = AASequenceImpl.parsePeptideFromAnnotatedSequence(annotated);
        Assert.assertEquals("YSFVATAER", p.getSequence());
        Assert.assertEquals(annotated, p.getModifiedSequence());

        annotated = "NH2-YS<P>FVAT<P>AER-COOH";
        p = AASequenceImpl.parsePeptideFromAnnotatedSequence(annotated);
        Assert.assertEquals("YSFVATAER", p.getSequence());
        Assert.assertEquals(annotated, p.getModifiedSequence());

        annotated = "Ace-MPLHGM<Mox>TSR-COOH";
        p = AASequenceImpl.parsePeptideFromAnnotatedSequence(annotated);
        Assert.assertEquals("MPLHGMTSR", p.getSequence());
        Assert.assertEquals(annotated, p.getModifiedSequence());

        annotated = "Ace-MPLHGM<Mox>TSR-Met";
        p = AASequenceImpl.parsePeptideFromAnnotatedSequence(annotated);
        Assert.assertEquals("MPLHGMTSR", p.getSequence());
        Assert.assertEquals(annotated, p.getModifiedSequence());

        annotated = "NH2-Q<Pyr>PLHGM<Mox>TSR-Met";
        p = AASequenceImpl.parsePeptideFromAnnotatedSequence(annotated);
        Assert.assertEquals("QPLHGMTSR", p.getSequence());
        Assert.assertEquals(annotated, p.getModifiedSequence());

        // These should fail.
        try {
            annotated = "Ace-<Pyr>MPLHGM<Mox>TSR-Met";
            p = AASequenceImpl.parsePeptideFromAnnotatedSequence(annotated);
            fail("No IllegalArgumentException thrown when attempting to parse an annotated String starting with <Pyr>!");
        } catch (RuntimeException re) {
            // This is correct.
        }
        try {
            annotated = "Ace-MPLHGM<Mox>TS<R-Met";
            p = AASequenceImpl.parsePeptideFromAnnotatedSequence(annotated);
            fail("No IllegalArgumentException thrown when attempting to parse an annotated String with an unclosed '<'!");
        } catch (RuntimeException re) {
            // This is correct.
        }
        try {
            annotated = "Ace-MPLHGM<Mox>TS>R-Met";
            p = AASequenceImpl.parsePeptideFromAnnotatedSequence(annotated);
            fail("No IllegalArgumentException thrown when attempting to parse an annotated String with unbalanced '>'!");
        } catch (RuntimeException re) {
            // This is correct.
        }
        try {
            annotated = "Ace-MPLHGM<Mox>TS<zwazwa>R-Met";
            p = AASequenceImpl.parsePeptideFromAnnotatedSequence(annotated);
            fail("No IllegalArgumentException thrown when attempting to parse an annotated String with unknown modification code 'zwazwa'!");
        } catch (RuntimeException re) {
            // This is correct.
        }
    }

    /**
     * This method test the calculation of the Kyte & Doolittle
     * GRAVY coefficient. It also test the caching behaviour.
     */
    public void testGravy() {
        AASequenceImpl seq = new AASequenceImpl("LENNART", null);
        Assert.assertEquals(-1.443, seq.getGravy(), Double.MIN_VALUE * 2);
        // Get it from cache. I would be MOST surprised to see this fail,
        // yet one never knows.
        Assert.assertEquals(-1.443, seq.getGravy(), Double.MIN_VALUE * 2);

        seq.setSequence("MARTENS");
        Assert.assertEquals(-1.329, seq.getGravy(), Double.MIN_VALUE * 2);

        seq.setSequence("LENNARTMARTENS");
        Assert.assertEquals(-1.386, seq.getGravy(), Double.MIN_VALUE * 2);

        seq.setSequence("KRISGEVAERT");
        Assert.assertEquals(-1.027, seq.getGravy(), Double.MIN_VALUE * 2);

        seq.setSequence("ARNDCQEGHILKMFPSTWYV");
        Assert.assertEquals(-0.490, seq.getGravy(), Double.MIN_VALUE * 2);
    }

    /**
     * This method test the calculation of the Meek HPLC retention
     * time coefficient. It also test the caching behaviour.
     */
    public void testMeek() {
        AASequenceImpl seq = new AASequenceImpl("LENNARTMARTENSKRISGEVAERT", null);
        Assert.assertEquals(-0.520, seq.getMeek(), Double.MIN_VALUE * 2);
        // Get it from cache. I would be MOST surprised to see this fail,
        // yet one never knows.
        Assert.assertEquals(-0.520, seq.getMeek(), Double.MIN_VALUE * 2);

        seq.setSequence("LENNARTMARTENSGEVAERT");
        Assert.assertEquals(-0.390, seq.getMeek(), Double.MIN_VALUE * 2);

        seq.setSequence("ARNDCQEGHILKMFPSTWYV");
        Assert.assertEquals(2.52, seq.getMeek(), Double.MIN_VALUE * 2);

        seq.setSequence("ARNDCQEGHILKMFPSTWYVK");
        Assert.assertEquals(2.224, seq.getMeek(), Double.MIN_VALUE * 2);
    }

    /**
     * This method test the mass calculation.
     */
    public void testMassCalc() {
        AASequenceImpl seq = new AASequenceImpl("YSFVATAER");
        Assert.assertEquals(1042.508345, seq.getMass(), Double.MIN_VALUE * 2);

        // Add a modification.
        seq.addModification(ModificationFactory.getModification("Ace", Modification.NTERMINUS, 0));
        Assert.assertEquals(1085.526735, seq.getMass(), Double.MIN_VALUE * 2);

        // Add a different modification.
        seq = new AASequenceImpl("YSMVATAER");
        seq.addModification(ModificationFactory.getModification("Mox", "M", 3));
        Assert.assertEquals(1042.4753349999999, seq.getMass(), Double.MIN_VALUE * 2);
        seq.addModification(ModificationFactory.getModification("Ace", Modification.NTERMINUS, 0));
        Assert.assertEquals(1085.4937249999999, seq.getMass(), Double.MIN_VALUE * 2);
        seq.addModification(ModificationFactory.getModification("Amide (C-term)", seq.getLength() + 1));
        Assert.assertEquals(1101.5124489999999, seq.getMass(), Double.MIN_VALUE * 2);
    }

    /**
     * This method test the length reporting of the sequence.
     */
    public void testLength() {
        AASequenceImpl seq = new AASequenceImpl("LENNARTMARTENS");
        Assert.assertEquals(14, seq.getLength());
    }

    /**
     * This method test N-terminal truncation of the AASequenceImpl instance.
     */
    public void testNtermTruncation() {
        // First without modifications.
        AASequenceImpl seq = new AASequenceImpl("LENNARTMARTENS");
        seq = seq.getNTermTruncatedSequence(10);
        // Check the length.
        Assert.assertEquals(10, seq.getLength());
        // Check the sequence.
        Assert.assertEquals("LENNARTMAR", seq.getSequence());
        // Check for the absence of modifications.
        Assert.assertTrue(seq.getModifications() == null);

        // Now truncate to more than the original size.
        seq = seq.getNTermTruncatedSequence(100);
        Assert.assertEquals(10, seq.getLength());
        // Check the sequence.
        Assert.assertEquals("LENNARTMAR", seq.getSequence());
        // Check for the absence of modifications.
        Assert.assertTrue(seq.getModifications() == null);

        // Now truncate to exactly the original size.
        seq = seq.getNTermTruncatedSequence(10);
        Assert.assertEquals(10, seq.getLength());
        // Check the sequence.
        Assert.assertEquals("LENNARTMAR", seq.getSequence());
        // Check for the absence of modifications.
        Assert.assertTrue(seq.getModifications() == null);


        // Okay, with modifications this time.
        Vector mods = new Vector(4);
        mods.add(new ModificationImplementation("TestMod", "Ace", new HashMap(), 0));
        mods.add(new ModificationImplementation("TestMod2", "Ox", new HashMap(), 8));
        mods.add(new ModificationImplementation("TestMod3", "Ace", new HashMap(), 6));
        mods.add(new ModificationImplementation("TestMod4", "Spe", new HashMap(), 14));
        seq = new AASequenceImpl("LENNARTMARTENS", mods);
        Assert.assertEquals("Ace-LENNAR<Ace>TM<Ox>ARTENS<Spe>-COOH", seq.getModifiedSequence());
        // Truncate.
        seq = seq.getNTermTruncatedSequence(8);
        // Check the length.
        Assert.assertEquals(8, seq.getLength());
        // Check the sequence.
        Assert.assertEquals("LENNARTM", seq.getSequence());
        Assert.assertEquals("Ace-LENNAR<Ace>TM<Ox>-COOH", seq.getModifiedSequence());
        // Check for the presence of modifications.
        Assert.assertTrue(seq.getModifications() != null);
        // There should be three modifications, on positions 0, 6 and 8.
        Vector modifs = seq.getModifications();
        // To check against.
        Vector ints = new Vector(3);
        ints.add(new Integer(0));
        ints.add(new Integer(6));
        ints.add(new Integer(8));

        int liSize = modifs.size();
        Assert.assertEquals(3, liSize);
        for (int i = 0; i < liSize; i++) {
            int loc = ((Modification) modifs.get(i)).getLocation();
            for (int j = 0; j < ints.size(); j++) {
                int control = ((Integer) ints.get(j)).intValue();
                if (control == loc) {
                    ints.remove(j);
                    break;
                }
            }
        }
        Assert.assertEquals(0, ints.size());
    }

    /**
     * This method test C-terminal truncation of the AASequenceImpl instance.
     */
    public void testCtermTruncation() {
        // First without modifications.
        AASequenceImpl seq = new AASequenceImpl("LENNARTMARTENS");
        seq = seq.getCTermTruncatedSequence(10);
        // Check the length.
        Assert.assertEquals(10, seq.getLength());
        // Check the sequence.
        Assert.assertEquals("ARTMARTENS", seq.getSequence());
        // Check for the absence of modifications.
        Assert.assertTrue(seq.getModifications() == null);

        // Now truncate to more than the original size.
        seq = seq.getCTermTruncatedSequence(100);
        Assert.assertEquals(10, seq.getLength());
        // Check the sequence.
        Assert.assertEquals("ARTMARTENS", seq.getSequence());
        // Check for the absence of modifications.
        Assert.assertTrue(seq.getModifications() == null);

        // Now truncate to exactly the original size.
        seq = seq.getCTermTruncatedSequence(10);
        Assert.assertEquals(10, seq.getLength());
        // Check the sequence.
        Assert.assertEquals("ARTMARTENS", seq.getSequence());
        // Check for the absence of modifications.
        Assert.assertTrue(seq.getModifications() == null);


        // Okay, with modifications this time.
        Vector mods = new Vector(4);
        mods.add(new ModificationImplementation("TestMod", "Ace", new HashMap(), 0));
        mods.add(new ModificationImplementation("TestMod2", "Ox", new HashMap(), 8));
        mods.add(new ModificationImplementation("TestMod3", "Ace", new HashMap(), 6));
        mods.add(new ModificationImplementation("TestMod4", "Spe", new HashMap(), 14));
        seq = new AASequenceImpl("LENNARTMARTENS", mods);
        Assert.assertEquals("Ace-LENNAR<Ace>TM<Ox>ARTENS<Spe>-COOH", seq.getModifiedSequence());
        // Truncate.
        seq = seq.getCTermTruncatedSequence(10);
        // Check the length.
        Assert.assertEquals(10, seq.getLength());
        // Check the sequence.
        Assert.assertEquals("ARTMARTENS", seq.getSequence());
        Assert.assertEquals("NH2-AR<Ace>TM<Ox>ARTENS<Spe>-COOH", seq.getModifiedSequence());
        // Check for the presence of modifications.
        Assert.assertTrue(seq.getModifications() != null);
        // There should be three modifications, on new positions 2, 4 and 10.
        Vector modifs = seq.getModifications();
        // To check against.
        Vector ints = new Vector(3);
        ints.add(new Integer(2));
        ints.add(new Integer(4));
        ints.add(new Integer(10));

        int liSize = modifs.size();
        Assert.assertEquals(3, liSize);
        for (int i = 0; i < liSize; i++) {
            int loc = ((Modification) modifs.get(i)).getLocation();
            for (int j = 0; j < ints.size(); j++) {
                int control = ((Integer) ints.get(j)).intValue();
                if (control == loc) {
                    ints.remove(j);
                    break;
                }
            }
        }
        Assert.assertEquals(0, ints.size());
    }

    /**
     * This method test sequence truncation.
     */
    public void testTruncation() {
        AASequenceImpl seq = new AASequenceImpl("LENNARTMARTENS");
        AASequenceImpl trunc = seq.getTruncatedSequence(3, 7);
        Assert.assertEquals("NNAR", trunc.getSequence());
        Assert.assertEquals("NH2-NNAR-COOH", trunc.getModifiedSequence());
        Assert.assertTrue(trunc.getModifications() == null);

        // Now with mods.
        seq.addModification(new ModificationImplementation("Test", "Ace", new HashMap(), 0));
        seq.addModification(new ModificationImplementation("Test", "Met", new HashMap(), 15));
        seq.addModification(new ModificationImplementation("Test", "Ox", new HashMap(), 8));
        trunc = seq.getTruncatedSequence(3, 7);
        Assert.assertEquals("NNAR", trunc.getSequence());
        Assert.assertEquals("NH2-NNAR-COOH", trunc.getModifiedSequence());
        Assert.assertTrue(trunc.getModifications() == null);

        trunc = seq.getTruncatedSequence(3, 9);
        Assert.assertEquals("NNARTM", trunc.getSequence());
        Assert.assertEquals("NH2-NNARTM<Ox>-COOH", trunc.getModifiedSequence());
        Assert.assertTrue(trunc.getModifications() != null);
        Assert.assertEquals(1, trunc.getModifications().size());

        trunc = seq.getTruncatedSequence(1, 9);
        Assert.assertEquals("LENNARTM", trunc.getSequence());
        Assert.assertEquals("Ace-LENNARTM<Ox>-COOH", trunc.getModifiedSequence());
        Assert.assertTrue(trunc.getModifications() != null);
        Assert.assertEquals(2, trunc.getModifications().size());

        trunc = seq.getTruncatedSequence(1, 4);
        Assert.assertEquals("LEN", trunc.getSequence());
        Assert.assertEquals("Ace-LEN-COOH", trunc.getModifiedSequence());
        Assert.assertTrue(trunc.getModifications() != null);
        Assert.assertEquals(1, trunc.getModifications().size());

        trunc = seq.getTruncatedSequence(3, 15);
        Assert.assertEquals("NNARTMARTENS", trunc.getSequence());
        Assert.assertEquals("NH2-NNARTM<Ox>ARTENS-Met", trunc.getModifiedSequence());
        Assert.assertTrue(trunc.getModifications() != null);
        Assert.assertEquals(2, trunc.getModifications().size());

        trunc = seq.getTruncatedSequence(10, 15);
        Assert.assertEquals("RTENS", trunc.getSequence());
        Assert.assertEquals("NH2-RTENS-Met", trunc.getModifiedSequence());
        Assert.assertTrue(trunc.getModifications() != null);
        Assert.assertEquals(1, trunc.getModifications().size());

        trunc = seq.getTruncatedSequence(10, 14);
        Assert.assertEquals("RTEN", trunc.getSequence());
        Assert.assertEquals("NH2-RTEN-COOH", trunc.getModifiedSequence());
        Assert.assertTrue(trunc.getModifications() == null);
    }

    /**
     * This method test whether the 'contains' method functions correctly.
     */
    public void testContains() {
        final String sequence = "LENNARTMARTENS";

        AASequenceImpl seq = new AASequenceImpl(sequence);
        Assert.assertTrue(seq.contains("L"));
        Assert.assertTrue(seq.contains("M"));
        Assert.assertTrue(seq.contains("S"));
        Assert.assertTrue(seq.contains("ENNARTM"));
        Assert.assertTrue(seq.contains("ARTE"));
        Assert.assertFalse(seq.contains("X"));
        Assert.assertFalse(seq.contains("RENNERT"));
    }
}
