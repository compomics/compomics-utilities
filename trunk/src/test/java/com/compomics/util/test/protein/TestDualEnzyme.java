/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-sep-2003
 * Time: 11:30:42
 */
package com.compomics.util.test.protein;

import junit.TestCaseLM;
import junit.framework.Assert;
import com.compomics.util.protein.DualEnzyme;
import com.compomics.util.protein.Protein;
import com.compomics.util.protein.Enzyme;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class implements the test scenario for the dualenzyme class.
 *
 * @author Lennart Martens
 */
public class TestDualEnzyme extends TestCaseLM {

    public TestDualEnzyme() {
        this("Test scenario for the DualEnzyme class.");
    }

    public TestDualEnzyme(String aName) {
        super(aName);
    }

    /**
     * This method test the constructor and the accessors.
     */
    public void testCreationAndAccessors() {
        final String nterms = "NTERM";

        DualEnzyme de = new DualEnzyme("TestEnzyme", nterms, "C", "P", "Cterm", 1);
        Assert.assertEquals("TestEnzyme", de.getTitle());
        Assert.assertEquals("METNRXC", new String(de.getCleavage()));
        Assert.assertEquals(1, de.getRestrict().length);
        Assert.assertEquals('P', de.getRestrict()[0]);
        Assert.assertEquals(1, de.getMiscleavages());
        Assert.assertEquals(DualEnzyme.CTERM, de.getPosition());
        Assert.assertEquals(1, de.getCleavage(DualEnzyme.CTERMINAL).length);
        Assert.assertEquals('C', de.getCleavage(DualEnzyme.CTERMINAL)[0]);
        Assert.assertEquals(nterms.length(), de.getCleavage(DualEnzyme.NTERMINAL).length);
        String s = new String(de.getCleavage(DualEnzyme.NTERMINAL));
        for(int i=0;i<nterms.length();i++) {
            Assert.assertTrue(s.indexOf(nterms.charAt(i)) >= 0);
        }
    }

    /**
     * This method test the validation of the 'terminus' flag in 'setCleavage' and
     * 'getCleavage'
     */
    public void testTerminusValidation() {
        final String nterms = "NTERM";
        DualEnzyme de = new DualEnzyme("TestEnzyme", nterms, "C", "P", "Cterm", 1);

        // Both should work fine.
        de.setCleavage("K", DualEnzyme.NTERMINAL);
        Assert.assertEquals(1, de.getCleavage(DualEnzyme.NTERMINAL).length);
        Assert.assertEquals('K', de.getCleavage(DualEnzyme.NTERMINAL)[0]);
        de.setCleavage("W", DualEnzyme.CTERMINAL);
        Assert.assertEquals(1, de.getCleavage(DualEnzyme.CTERMINAL).length);
        Assert.assertEquals('W', de.getCleavage(DualEnzyme.CTERMINAL)[0]);

        // Now the wrong kind.
        try {
            de.setCleavage("K", -1);
            fail("No IllegalArgumentException thrown when confronting DualEnzyme with a cleavable position of '-1' in 'setCleavage(int aTerminal)'!");
        } catch(IllegalArgumentException iae) {
            // Okay!
        }
        try {
            de.setCleavage("K", 2);
            fail("No IllegalArgumentException thrown when confronting DualEnzyme with a cleavable position of '2' in 'setCleavage(int aTerminal)'!");
        } catch(IllegalArgumentException iae) {
            // Okay!
        }
        try {
            de.getCleavage(-1);
            fail("No IllegalArgumentException thrown when confronting DualEnzyme with a cleavable position of '-1' in 'getCleavage(int aTerminal)'!");
        } catch(IllegalArgumentException iae) {
            // Okay!
        }
        try {
            de.getCleavage(2);
            fail("No IllegalArgumentException thrown when confronting DualEnzyme with a cleavable position of '2' in 'getCleavage(int aTerminal)'!");
        } catch(IllegalArgumentException iae) {
            // Okay!
        }
    }

    /**
     * This method test the 'oldCleave' method of the DualEnzyme.
     */
    public void testOldCleave() {
        // Cterm enzyme, 0 missed cleavages.
        DualEnzyme de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 0);
        Protein[] p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMTRVW"));
        Assert.assertEquals(1, p.length);
        Assert.assertEquals("KLMTR", p[0].getSequence().getSequence());
        Assert.assertEquals(19, p[0].getHeader().getStartLocation());
        Assert.assertEquals(23, p[0].getHeader().getEndLocation());

        // Cterm enzyme, 1 missed cleavage.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 1);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMTRVW"));
        Assert.assertEquals(2, p.length);
        Assert.assertEquals("KLMTR", p[0].getSequence().getSequence());
        Assert.assertEquals(19, p[0].getHeader().getStartLocation());
        Assert.assertEquals(23, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMTRVW", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(25, p[1].getHeader().getEndLocation());

        // Nterm enzyme, 0 missed cleavages.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Nterm", 0);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMTRVW"));
        Assert.assertEquals(1, p.length);
        Assert.assertEquals("DKLMT", p[0].getSequence().getSequence());
        Assert.assertEquals(18, p[0].getHeader().getStartLocation());
        Assert.assertEquals(22, p[0].getHeader().getEndLocation());

        // Nterm enzyme, 2 missed cleavages.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Nterm", 2);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMTRVWRGHF"));
        Assert.assertEquals(3, p.length);
        Assert.assertEquals("DKLMT", p[0].getSequence().getSequence());
        Assert.assertEquals(18, p[0].getHeader().getStartLocation());
        Assert.assertEquals(22, p[0].getHeader().getEndLocation());
        Assert.assertEquals("DKLMTRVW", p[1].getSequence().getSequence());
        Assert.assertEquals(18, p[1].getHeader().getStartLocation());
        Assert.assertEquals(25, p[1].getHeader().getEndLocation());
        Assert.assertEquals("DKLMTRVWRGHF", p[2].getSequence().getSequence());
        Assert.assertEquals(18, p[2].getHeader().getStartLocation());
        Assert.assertEquals(29, p[2].getHeader().getEndLocation());

        // Add some restrictors to a Cterm cleavage.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 1);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMTRPVWRGHF"));
        Assert.assertEquals(2, p.length);
        Assert.assertEquals("KLMTRPVWR", p[0].getSequence().getSequence());
        Assert.assertEquals(19, p[0].getHeader().getStartLocation());
        Assert.assertEquals(27, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMTRPVWRGHF", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(30, p[1].getHeader().getEndLocation());

        // Add some restrictors to an Nterm cleavage.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Nterm", 1);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMTRPVWRGHF"));
        Assert.assertEquals(2, p.length);
        Assert.assertEquals("DKLMTRPVW", p[0].getSequence().getSequence());
        Assert.assertEquals(18, p[0].getHeader().getStartLocation());
        Assert.assertEquals(26, p[0].getHeader().getEndLocation());
        Assert.assertEquals("DKLMTRPVWRGHF", p[1].getSequence().getSequence());
        Assert.assertEquals(18, p[1].getHeader().getStartLocation());
        Assert.assertEquals(30, p[1].getHeader().getEndLocation());

        // Nested occurrence, 0 missed cleavages.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 0);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMDTGKRVWRGHF"));
        Assert.assertEquals(2, p.length);
        Assert.assertEquals("KLMDTGKR", p[0].getSequence().getSequence());
        Assert.assertEquals(19, p[0].getHeader().getStartLocation());
        Assert.assertEquals(26, p[0].getHeader().getEndLocation());
        Assert.assertEquals("TGKR", p[1].getSequence().getSequence());
        Assert.assertEquals(23, p[1].getHeader().getStartLocation());
        Assert.assertEquals(26, p[1].getHeader().getEndLocation());

        // Nested occurrence, 1 missed cleavage.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 1);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMDTGKRVWRGHF"));
        Assert.assertEquals(4, p.length);
        Assert.assertEquals("KLMDTGKR", p[0].getSequence().getSequence());
        Assert.assertEquals(19, p[0].getHeader().getStartLocation());
        Assert.assertEquals(26, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMDTGKRVWR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(29, p[1].getHeader().getEndLocation());
        Assert.assertEquals("TGKR", p[2].getSequence().getSequence());
        Assert.assertEquals(23, p[2].getHeader().getStartLocation());
        Assert.assertEquals(26, p[2].getHeader().getEndLocation());
        Assert.assertEquals("TGKRVWR", p[3].getSequence().getSequence());
        Assert.assertEquals(23, p[3].getHeader().getStartLocation());
        Assert.assertEquals(29, p[3].getHeader().getEndLocation());

        // Nested occurrence, 2 missed cleavages.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 2);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMDTGKRVWRGHF"));
        Assert.assertEquals(6, p.length);
        Assert.assertEquals("KLMDTGKR", p[0].getSequence().getSequence());
        Assert.assertEquals(19, p[0].getHeader().getStartLocation());
        Assert.assertEquals(26, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMDTGKRVWR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(29, p[1].getHeader().getEndLocation());
        Assert.assertEquals("KLMDTGKRVWRGHF", p[2].getSequence().getSequence());
        Assert.assertEquals(19, p[2].getHeader().getStartLocation());
        Assert.assertEquals(32, p[2].getHeader().getEndLocation());
        Assert.assertEquals("TGKR", p[3].getSequence().getSequence());
        Assert.assertEquals(23, p[3].getHeader().getStartLocation());
        Assert.assertEquals(26, p[3].getHeader().getEndLocation());
        Assert.assertEquals("TGKRVWR", p[4].getSequence().getSequence());
        Assert.assertEquals(23, p[4].getHeader().getStartLocation());
        Assert.assertEquals(29, p[4].getHeader().getEndLocation());
        Assert.assertEquals("TGKRVWRGHF", p[5].getSequence().getSequence());
        Assert.assertEquals(23, p[5].getHeader().getStartLocation());
        Assert.assertEquals(32, p[5].getHeader().getEndLocation());

        // Consecutive occurrence, 0 missed cleavages.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 0);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMTGKRVDWRGHF"));
        Assert.assertEquals(2, p.length);
        Assert.assertEquals("KLMTGKR", p[0].getSequence().getSequence());
        Assert.assertEquals(19, p[0].getHeader().getStartLocation());
        Assert.assertEquals(25, p[0].getHeader().getEndLocation());
        Assert.assertEquals("WR", p[1].getSequence().getSequence());
        Assert.assertEquals(28, p[1].getHeader().getStartLocation());
        Assert.assertEquals(29, p[1].getHeader().getEndLocation());

        // Consecutive occurrence, 1 missed cleavage.
        de = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 1);
        p = de.oldCleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the oldCleave() method.", "FGHDKLMTGKRVDWRGHF"));
        Assert.assertEquals(4, p.length);
        Assert.assertEquals("KLMTGKR", p[0].getSequence().getSequence());
        Assert.assertEquals(19, p[0].getHeader().getStartLocation());
        Assert.assertEquals(25, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMTGKRVDWR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(29, p[1].getHeader().getEndLocation());
        Assert.assertEquals("WR", p[2].getSequence().getSequence());
        Assert.assertEquals(28, p[2].getHeader().getStartLocation());
        Assert.assertEquals(29, p[2].getHeader().getEndLocation());
        Assert.assertEquals("WRGHF", p[3].getSequence().getSequence());
        Assert.assertEquals(28, p[3].getHeader().getStartLocation());
        Assert.assertEquals(32, p[3].getHeader().getEndLocation());
    }

    /**
     * This method test the 'cleave()' method of the DualEnzyme.
     */
    public void testCleave() {
        // Simple case, C-terminal cleavage, 0 missed cleavages.
        Enzyme dual = new DualEnzyme("TestEnzyme", "D", "R", "P", "Cterm", 0);
        Protein[] p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMTRVW"));
        Assert.assertEquals(3, p.length);
        Assert.assertEquals("FGHD", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(18, p[0].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[0].getHeader().getAccession());
        Assert.assertEquals("KLMTR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(23, p[1].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[1].getHeader().getAccession());
        Assert.assertEquals("VW", p[2].getSequence().getSequence());
        Assert.assertEquals(24, p[2].getHeader().getStartLocation());
        Assert.assertEquals(25, p[2].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[2].getHeader().getAccession());

        // Same simple case, C-terminal cleavage, 1 missed cleavages.
        dual = new DualEnzyme("TestEnzyme", "D", "R", "P", "Cterm", 1);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMTRVW"));
        Assert.assertEquals(4, p.length);
        Assert.assertEquals("FGHD", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(18, p[0].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[0].getHeader().getAccession());
        Assert.assertEquals("KLMTR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(23, p[1].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[1].getHeader().getAccession());
        Assert.assertEquals("KLMTRVW", p[2].getSequence().getSequence());
        Assert.assertEquals(19, p[2].getHeader().getStartLocation());
        Assert.assertEquals(25, p[2].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[2].getHeader().getAccession());
        Assert.assertEquals("VW", p[3].getSequence().getSequence());
        Assert.assertEquals(24, p[3].getHeader().getStartLocation());
        Assert.assertEquals(25, p[3].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[3].getHeader().getAccession());

        // Simple case, N-terminal cleavage, 0 missed cleavages.
        dual = new DualEnzyme("TestEnzyme", "D", "R", "P", "Nterm", 0);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMTRVW"));
        Assert.assertEquals(3, p.length);
        Assert.assertEquals("FGH", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(17, p[0].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[0].getHeader().getAccession());
        Assert.assertEquals("DKLMT", p[1].getSequence().getSequence());
        Assert.assertEquals(18, p[1].getHeader().getStartLocation());
        Assert.assertEquals(22, p[1].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[1].getHeader().getAccession());
        Assert.assertEquals("RVW", p[2].getSequence().getSequence());
        Assert.assertEquals(23, p[2].getHeader().getStartLocation());
        Assert.assertEquals(25, p[2].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[2].getHeader().getAccession());

        // Same simple case, N-terminal cleavage, 1 missed cleavages.
        dual = new DualEnzyme("TestEnzyme", "D", "R", "P", "Nterm", 1);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMTRVW"));
        Assert.assertEquals(4, p.length);
        Assert.assertEquals("FGH", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(17, p[0].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[0].getHeader().getAccession());
        Assert.assertEquals("DKLMT", p[1].getSequence().getSequence());
        Assert.assertEquals(18, p[1].getHeader().getStartLocation());
        Assert.assertEquals(22, p[1].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[1].getHeader().getAccession());
        Assert.assertEquals("DKLMTRVW", p[2].getSequence().getSequence());
        Assert.assertEquals(18, p[2].getHeader().getStartLocation());
        Assert.assertEquals(25, p[2].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[2].getHeader().getAccession());
        Assert.assertEquals("RVW", p[3].getSequence().getSequence());
        Assert.assertEquals(23, p[3].getHeader().getStartLocation());
        Assert.assertEquals(25, p[3].getHeader().getEndLocation());
        Assert.assertEquals("Q55645", p[3].getHeader().getAccession());

        // Nterm enzyme, 2 missed cleavages.
        dual = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Nterm", 2);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMTRVWRGHF"));
        Assert.assertEquals(5, p.length);
        Assert.assertEquals("FGH", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(17, p[0].getHeader().getEndLocation());
        Assert.assertEquals("DKLMT", p[1].getSequence().getSequence());
        Assert.assertEquals(18, p[1].getHeader().getStartLocation());
        Assert.assertEquals(22, p[1].getHeader().getEndLocation());
        Assert.assertEquals("DKLMTRVW", p[2].getSequence().getSequence());
        Assert.assertEquals(18, p[2].getHeader().getStartLocation());
        Assert.assertEquals(25, p[2].getHeader().getEndLocation());
        Assert.assertEquals("DKLMTRVWRGHF", p[3].getSequence().getSequence());
        Assert.assertEquals(18, p[3].getHeader().getStartLocation());
        Assert.assertEquals(29, p[3].getHeader().getEndLocation());
        Assert.assertEquals("RVWRGHF", p[4].getSequence().getSequence());
        Assert.assertEquals(23, p[4].getHeader().getStartLocation());
        Assert.assertEquals(29, p[4].getHeader().getEndLocation());


        // Add some restrictors to a Cterm cleavage.
        dual = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 1);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMTRPVWRGHF"));
        Assert.assertEquals(4, p.length);
        Assert.assertEquals("FGHD", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(18, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMTRPVWR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(27, p[1].getHeader().getEndLocation());
        Assert.assertEquals("KLMTRPVWRGHF", p[2].getSequence().getSequence());
        Assert.assertEquals(19, p[2].getHeader().getStartLocation());
        Assert.assertEquals(30, p[2].getHeader().getEndLocation());
        Assert.assertEquals("GHF", p[3].getSequence().getSequence());
        Assert.assertEquals(28, p[3].getHeader().getStartLocation());
        Assert.assertEquals(30, p[3].getHeader().getEndLocation());

        // Add some restrictors to an Nterm cleavage.
        dual = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Nterm", 1);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMTRPVWRGHF"));
        Assert.assertEquals(4, p.length);
        Assert.assertEquals("FGH", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(17, p[0].getHeader().getEndLocation());
        Assert.assertEquals("DKLMTRPVW", p[1].getSequence().getSequence());
        Assert.assertEquals(18, p[1].getHeader().getStartLocation());
        Assert.assertEquals(26, p[1].getHeader().getEndLocation());
        Assert.assertEquals("DKLMTRPVWRGHF", p[2].getSequence().getSequence());
        Assert.assertEquals(18, p[2].getHeader().getStartLocation());
        Assert.assertEquals(30, p[2].getHeader().getEndLocation());
        Assert.assertEquals("RGHF", p[3].getSequence().getSequence());
        Assert.assertEquals(27, p[3].getHeader().getStartLocation());
        Assert.assertEquals(30, p[3].getHeader().getEndLocation());

        // Nested occurrence, 0 missed cleavages.
        dual = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 0);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMDTGKRVWRGHF"));
        Assert.assertEquals(5, p.length);
        Assert.assertEquals("FGHD", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(18, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMDTGKR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(26, p[1].getHeader().getEndLocation());
        Assert.assertEquals("KLMD", p[2].getSequence().getSequence());
        Assert.assertEquals(19, p[2].getHeader().getStartLocation());
        Assert.assertEquals(22, p[2].getHeader().getEndLocation());
        Assert.assertEquals("TGKR", p[3].getSequence().getSequence());
        Assert.assertEquals(23, p[3].getHeader().getStartLocation());
        Assert.assertEquals(26, p[3].getHeader().getEndLocation());
        Assert.assertEquals("VWRGHF", p[4].getSequence().getSequence());
        Assert.assertEquals(27, p[4].getHeader().getStartLocation());
        Assert.assertEquals(32, p[4].getHeader().getEndLocation());

        // Nested occurrence, 1 missed cleavage.
        dual = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 1);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMDTGKRVWRGHF"));
        Assert.assertEquals(7, p.length);
        Assert.assertEquals("FGHD", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(18, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMDTGKR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(26, p[1].getHeader().getEndLocation());
        Assert.assertEquals("KLMDTGKRVWR", p[2].getSequence().getSequence());
        Assert.assertEquals(19, p[2].getHeader().getStartLocation());
        Assert.assertEquals(29, p[2].getHeader().getEndLocation());
        Assert.assertEquals("KLMD", p[3].getSequence().getSequence());
        Assert.assertEquals(19, p[3].getHeader().getStartLocation());
        Assert.assertEquals(22, p[3].getHeader().getEndLocation());
        Assert.assertEquals("TGKR", p[4].getSequence().getSequence());
        Assert.assertEquals(23, p[4].getHeader().getStartLocation());
        Assert.assertEquals(26, p[4].getHeader().getEndLocation());
        Assert.assertEquals("TGKRVWR", p[5].getSequence().getSequence());
        Assert.assertEquals(23, p[5].getHeader().getStartLocation());
        Assert.assertEquals(29, p[5].getHeader().getEndLocation());
        Assert.assertEquals("VWRGHF", p[6].getSequence().getSequence());
        Assert.assertEquals(27, p[6].getHeader().getStartLocation());
        Assert.assertEquals(32, p[6].getHeader().getEndLocation());

        // Nested occurrence, 2 missed cleavages.
        dual = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 2);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMDTGKRVWRGHF"));
        Assert.assertEquals(9, p.length);
        Assert.assertEquals("FGHD", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(18, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMDTGKR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(26, p[1].getHeader().getEndLocation());
        Assert.assertEquals("KLMDTGKRVWR", p[2].getSequence().getSequence());
        Assert.assertEquals(19, p[2].getHeader().getStartLocation());
        Assert.assertEquals(29, p[2].getHeader().getEndLocation());
        Assert.assertEquals("KLMDTGKRVWRGHF", p[3].getSequence().getSequence());
        Assert.assertEquals(19, p[3].getHeader().getStartLocation());
        Assert.assertEquals(32, p[3].getHeader().getEndLocation());
        Assert.assertEquals("KLMD", p[4].getSequence().getSequence());
        Assert.assertEquals(19, p[4].getHeader().getStartLocation());
        Assert.assertEquals(22, p[4].getHeader().getEndLocation());
        Assert.assertEquals("TGKR", p[5].getSequence().getSequence());
        Assert.assertEquals(23, p[5].getHeader().getStartLocation());
        Assert.assertEquals(26, p[5].getHeader().getEndLocation());
        Assert.assertEquals("TGKRVWR", p[6].getSequence().getSequence());
        Assert.assertEquals(23, p[6].getHeader().getStartLocation());
        Assert.assertEquals(29, p[6].getHeader().getEndLocation());
        Assert.assertEquals("TGKRVWRGHF", p[7].getSequence().getSequence());
        Assert.assertEquals(23, p[7].getHeader().getStartLocation());
        Assert.assertEquals(32, p[7].getHeader().getEndLocation());
        Assert.assertEquals("VWRGHF", p[8].getSequence().getSequence());
        Assert.assertEquals(27, p[8].getHeader().getStartLocation());
        Assert.assertEquals(32, p[8].getHeader().getEndLocation());

        // Consecutive occurrence, 0 missed cleavages.
        dual = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 0);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMTGKRVDWRGHF"));
        Assert.assertEquals(5, p.length);
        Assert.assertEquals("FGHD", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(18, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMTGKR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(25, p[1].getHeader().getEndLocation());
        Assert.assertEquals("VD", p[2].getSequence().getSequence());
        Assert.assertEquals(26, p[2].getHeader().getStartLocation());
        Assert.assertEquals(27, p[2].getHeader().getEndLocation());
        Assert.assertEquals("WR", p[3].getSequence().getSequence());
        Assert.assertEquals(28, p[3].getHeader().getStartLocation());
        Assert.assertEquals(29, p[3].getHeader().getEndLocation());
        Assert.assertEquals("GHF", p[4].getSequence().getSequence());
        Assert.assertEquals(30, p[4].getHeader().getStartLocation());
        Assert.assertEquals(32, p[4].getHeader().getEndLocation());

        // Consecutive occurrence, 1 missed cleavage.
        dual = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 1);
        p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMTGKRVDWRGHF"));
        Assert.assertEquals(7, p.length);
        Assert.assertEquals("FGHD", p[0].getSequence().getSequence());
        Assert.assertEquals(15, p[0].getHeader().getStartLocation());
        Assert.assertEquals(18, p[0].getHeader().getEndLocation());
        Assert.assertEquals("KLMTGKR", p[1].getSequence().getSequence());
        Assert.assertEquals(19, p[1].getHeader().getStartLocation());
        Assert.assertEquals(25, p[1].getHeader().getEndLocation());
        Assert.assertEquals("KLMTGKRVDWR", p[2].getSequence().getSequence());
        Assert.assertEquals(19, p[2].getHeader().getStartLocation());
        Assert.assertEquals(29, p[2].getHeader().getEndLocation());
        Assert.assertEquals("VD", p[3].getSequence().getSequence());
        Assert.assertEquals(26, p[3].getHeader().getStartLocation());
        Assert.assertEquals(27, p[3].getHeader().getEndLocation());
        Assert.assertEquals("WR", p[4].getSequence().getSequence());
        Assert.assertEquals(28, p[4].getHeader().getStartLocation());
        Assert.assertEquals(29, p[4].getHeader().getEndLocation());
        Assert.assertEquals("WRGHF", p[5].getSequence().getSequence());
        Assert.assertEquals(28, p[5].getHeader().getStartLocation());
        Assert.assertEquals(32, p[5].getHeader().getEndLocation());
        Assert.assertEquals("GHF", p[6].getSequence().getSequence());
        Assert.assertEquals(30, p[6].getHeader().getStartLocation());
        Assert.assertEquals(32, p[6].getHeader().getEndLocation());
    }

    /**
     * This method test the cloning of an Enzyme.
     */
    public void testClone() {
        DualEnzyme e = new DualEnzyme("Test", "D", "R", "P", "Cterm", 5);
        DualEnzyme clone = (DualEnzyme)e.clone();

        Assert.assertEquals(new String(e.getCleavage()), new String(clone.getCleavage()));
        Assert.assertEquals(new String(e.getCleavage(DualEnzyme.NTERMINAL)), new String(clone.getCleavage(DualEnzyme.NTERMINAL)));
        Assert.assertEquals(new String(e.getCleavage(DualEnzyme.CTERMINAL)), new String(clone.getCleavage(DualEnzyme.CTERMINAL)));
        Assert.assertEquals(e.getMiscleavages(), clone.getMiscleavages());
        Assert.assertEquals(e.getPosition(), clone.getPosition());
        Assert.assertEquals(new String(e.getRestrict()), new String(clone.getRestrict()));
        Assert.assertEquals(e.getTitle(), clone.getTitle());

        // Get a cleaved set of peptides from a sequence.
        //@TODO Cleavage test needs to be uncommented!!!
        /*
        final Protein p = new Protein(">Test protein.\nLEDNARTMARTENS");
        Protein[] controlCleave = e.cleave(p);
        // Get a cleaved set from the clone.
        Protein[] cloneCleave = clone.cleave(p);
        // Compare the sets. They should be identical.
        for(int i = 0; i < cloneCleave.length; i++) {
            Assert.assertEquals(controlCleave[i], cloneCleave[i]);
        }
        */

        // Noow change the clone and see if the original changes.
        clone.setCleavage("G", DualEnzyme.NTERMINAL);
        clone.setCleavage("W", DualEnzyme.CTERMINAL);
        clone.setMiscleavages(1);
        clone.setPosition(DualEnzyme.NTERM);
        clone.setRestrict("L");
        clone.setTitle("Clone");

        // Clone should have changed.
        Assert.assertEquals(1, clone.getCleavage(DualEnzyme.NTERMINAL).length);
        Assert.assertEquals('G', clone.getCleavage(DualEnzyme.NTERMINAL)[0]);
        Assert.assertEquals(1, clone.getCleavage(DualEnzyme.CTERMINAL).length);
        Assert.assertEquals('W', clone.getCleavage(DualEnzyme.CTERMINAL)[0]);
        Assert.assertEquals(1, clone.getMiscleavages());
        Assert.assertEquals(DualEnzyme.NTERM, clone.getPosition());
        Assert.assertEquals("L", new String(clone.getRestrict()));
        Assert.assertEquals("Clone", clone.getTitle());

        // Original should remain the same.
        Assert.assertEquals(1, e.getCleavage(DualEnzyme.NTERMINAL).length);
        Assert.assertEquals('D', e.getCleavage(DualEnzyme.NTERMINAL)[0]);
        Assert.assertEquals(1, e.getCleavage(DualEnzyme.CTERMINAL).length);
        Assert.assertEquals('R', e.getCleavage(DualEnzyme.CTERMINAL)[0]);
        Assert.assertEquals(5, e.getMiscleavages());
        Assert.assertEquals(DualEnzyme.CTERM, e.getPosition());
        Assert.assertEquals("P", new String(e.getRestrict()));
        Assert.assertEquals("Test", e.getTitle());
    }

    /**
     * This method test whether the isEnzymatic product method functions.
     */
    public void testIsProduct() {
        // C-terminal cleavage.
        DualEnzyme e = new DualEnzyme("Test", "D", "KR", "P", "Cterm", 1);
        Protein p = new Protein(">Test Protein.\nFFFFGHDTSWPRGHFDEAQCCVRPTGHL");

        // Annotated String with human-readable indices.
        // F  F  F  F  G  H  D  T  S  W  P  R  G  H  F  D  E  A  Q  C  C  V  R  P  T  G  H  L
        // 1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28

        // A non-enzymatic product by sequence and subsequently by (human readable!) indices.
        Assert.assertEquals(DualEnzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "WPRG"));
        Assert.assertEquals(DualEnzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 10, 13));

        // A half enzymatic sequence (N-terminal), also with the true N-terminus included.
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "TSWP"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 8, 11));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "FFFFG"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 1, 5));

        // A half enzymatic sequence (C-terminal), also with the true C-terminus included.
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "DTSWPR"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 7, 12));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "PTGHL"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 24, 28));

        // Check the correct behaviour when multiple occurrences take place.
        Assert.assertEquals(DualEnzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("LGHJKLIGHKL", "GH"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("GHJKLIGHKL", "GH"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("FTTDGHJKLIGHKL", "GH"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("FTTGRLIGRPL", "GR"));
        Assert.assertEquals(DualEnzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("FTTDGKLIGRPL", "GK"));

        // Now check the correct handling of restriction locations.
        Assert.assertEquals(DualEnzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("LGHDPHVVGRPLMM", "PHVVGR"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHDHVVGRPLMM", "HVVGR"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("HVVGRPLMM", "HVVGR"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHDPHVVGR", "PHVVGR"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHDPHVVGRLMM", "PHVVGR"));
        Assert.assertEquals(DualEnzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("LGHDHVVGRLMM", "HVVGR"));

        // N-terminal cleavage.
        e = new DualEnzyme("Test2", "D", "R", "P", "Nterm", 1);

        // A non-enzymatic product.
        Assert.assertEquals(DualEnzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("DARVAL", "ARVA"));
        Assert.assertEquals(DualEnzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("DARVAL", 2, 5));

        // A half enzymatic sequence (N-terminal), also with the true N-terminus included.
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("LLDARVAL", "DARV"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("LLDARVAL", 3, 6));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("ARVAL", "ARV"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("ARVAL", 1, 3));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("DARVAL", "DARV"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("DARVAL", 1, 4));

        // A half enzymatic sequence (C-terminal), also with the true C-terminus included.
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LLDARVAL", "LDA"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LLDARVAL", 2, 4));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LLRARAL", "RAL"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LLRARAL", 5, 7));

        // Check the correct behaviour when multiple occurrences take place.
        Assert.assertEquals(DualEnzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("LLKARVALGHGHGLKARVRAL", "LKARV"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("GHDARVALGHKARVAL", "DARV"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("RLKARVALGHGHLKARPAL", "LKA"));
        Assert.assertEquals(DualEnzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("LLDARVALGHGHLKARPAL", "DA"));

        // Now check the correct handling of restriction locations.
        Assert.assertEquals(DualEnzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("LGHDPHVVGRPLMM", "DPHVVG"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHDHVVGRPLMM", "DHVVG"));
        Assert.assertEquals(DualEnzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("HVVGRPLMM", "HVVG"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHRPHVVGR", "RPHVVG"));
        Assert.assertEquals(DualEnzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHRPHVVGRLMM", "RPHVVG"));
        Assert.assertEquals(DualEnzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("LGHDHVVGRLMM", "DHVVG"));

        // Test exception handling.
        try {
            e.isEnzymaticProduct("LENNARTMARTENS", "FILIBERKE");
            fail("No IllegalArgumentException thrown when confronting an Enzyme with subsequence 'FILIBERKE' in 'LENNARTMARTENS' for 'isEnzymaticProduct(String, String)'!");
        } catch(IllegalArgumentException iae) {
            // Perfect.
        }
        try {
            e.isEnzymaticProduct("LENNARTMARTENS", "FILIBERKE");
            fail("No IllegalArgumentException thrown when confronting an Enzyme with subsequence 'FILIBERKE' in 'LENNARTMARTENS' for 'isEnzymaticProduct(String, String)'!");
        } catch(IllegalArgumentException iae) {
            // Perfect.
        }
        try {
            e.isEnzymaticProduct("LENNARTMARTENS", -1, 12);
            fail("No IllegalArgumentException thrown when confronting an Enzyme with subsequence indices (-1, 12) and 'LENNARTMARTENS' for 'isEnzymaticProduct(String, int, int)'!");
        } catch(IllegalArgumentException iae) {
            // Perfect.
        }
        try {
            e.isEnzymaticProduct("LENNARTMARTENS", 1, -1);
            fail("No IllegalArgumentException thrown when confronting an Enzyme with subsequence indices (1, -1) and 'LENNARTMARTENS' for 'isEnzymaticProduct(String, int, int)'!");
        } catch(IllegalArgumentException iae) {
            // Perfect.
        }
        try {
            e.isEnzymaticProduct("LENNARTMARTENS", 3, 2);
            fail("No IllegalArgumentException thrown when confronting an Enzyme with subsequence indices (3, 2) and 'LENNARTMARTENS' for 'isEnzymaticProduct(String, int, int)'!");
        } catch(IllegalArgumentException iae) {
            // Perfect.
        }
        try {
            e.isEnzymaticProduct("LENNARTMARTENS", 3, 3);
            fail("No IllegalArgumentException thrown when confronting an Enzyme with subsequence indices (3, 3) and 'LENNARTMARTENS' for 'isEnzymaticProduct(String, int, int)'!");
        } catch(IllegalArgumentException iae) {
            // Perfect.
        }
        try {
            e.isEnzymaticProduct("LENNARTMARTENS", 6, 15);
            fail("No IllegalArgumentException thrown when confronting an Enzyme with subsequence indices (6, 15) and 'LENNARTMARTENS' (last char at 14) for 'isEnzymaticProduct(String, int, int)'!");
        } catch(IllegalArgumentException iae) {
            // Perfect.
        }
        try {
            e.isEnzymaticProduct("LENNARTMARTENS", 6, 129);
            fail("No IllegalArgumentException thrown when confronting an Enzyme with subsequence indices (6, 129) and 'LENNARTMARTENS' (last char at 14) for 'isEnzymaticProduct(String, int, int)'!");
        } catch(IllegalArgumentException iae) {
            // Perfect.
        }
    }
}
