/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 8-okt-02
 * Time: 18:55:02
 */
package com.compomics.util.test.io;

import com.compomics.util.io.MascotEnzymeReader;
import com.compomics.util.junit.TestCaseLM;
import com.compomics.util.protein.DualEnzyme;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.RegExEnzyme;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2008/11/18 11:39:11 $
 */

/**
 * This class implements the complete test scenario for the
 * MascotEnzymeReader class.
 *
 * @author Lennart
 * @see com.compomics.util.io.MascotEnzymeReader
 */
public class TestMascotEnzymeReader extends TestCase {

    // Class specific log4j logger for TestMascotEnzymeReader instances.
    Logger logger = Logger.getLogger(TestMascotEnzymeReader.class);

    public TestMascotEnzymeReader() {
        this("Test scenario for the MascotEnzymeReader class.");
    }

    public TestMascotEnzymeReader(String aName) {
        super(aName);
    }

    /**
     * This method test the creation and reading behaviour of a MascotEnzymeReader instance.
     */
    public void testCreationAndReading() {
        final String inputFile = TestCaseLM.getFullFilePath("enzymes_test.txt").replace("%20", " ");
        final Vector control = new Vector(5);
        control.add("Trypsin");
        control.add("Trypsin/P");
        control.add("TrypChymo");
        control.add("PepsinA");
        control.add("None");
        control.add("dualTrypCathep");
        control.add("DuAlTrypCathep2");
        control.add("ReGeXTrypCathep");
        control.add("regexTrypCathep2");

        // With file.
        try {
            MascotEnzymeReader mer = new MascotEnzymeReader(inputFile);
            String[] names = mer.getEnzymeNames();

            // First see if the arrays are equal size.
            Assert.assertEquals(control.size(), names.length);

            Vector controlCopy = (Vector)control.clone();
            // Now check whether we can find all entries.
            for(int i = 0; i < names.length; i++) {
                for(int j = 0; j < controlCopy.size(); j++) {
                    if(names[i].equals(controlCopy.get(j))) {
                        controlCopy.remove(j);
                        break;
                    }
                }
            }
            // The control Vector should now be empty.
            Assert.assertEquals(0, controlCopy.size());

            // Next test each separate Enzyme entry.
            Enzyme e = mer.getEnzyme("Trypsin");
            Assert.assertEquals("Trypsin", e.getTitle());
            Assert.assertEquals("KR", new String(e.getCleavage()));
            Assert.assertEquals("P", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("Trypsin/P");
            Assert.assertEquals("Trypsin/P", e.getTitle());
            Assert.assertEquals("KR", new String(e.getCleavage()));
            Assert.assertTrue(e.getRestrict() == null);
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("TrypChymo");
            Assert.assertEquals("TrypChymo", e.getTitle());
            Assert.assertEquals("FYWLKR", new String(e.getCleavage()));
            Assert.assertEquals("P", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("PepsinA");
            Assert.assertEquals("PepsinA", e.getTitle());
            Assert.assertEquals("FL", new String(e.getCleavage()));
            Assert.assertTrue(e.getRestrict() == null);
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("None");
            Assert.assertEquals("None", e.getTitle());
            Assert.assertEquals("NONE", new String(e.getCleavage()));
            Assert.assertEquals("NONE", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.NTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("dualTrypCathep");
            Assert.assertEquals("dualTrypCathep", e.getTitle());
            Assert.assertEquals("DXR", new String(e.getCleavage()));
            Assert.assertEquals("P", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());
            Assert.assertTrue(e instanceof DualEnzyme);
            if(e instanceof DualEnzyme) {
                DualEnzyme dual = (DualEnzyme)e;
                Assert.assertEquals("D", new String(dual.getCleavage(DualEnzyme.NTERMINAL)));
                Assert.assertEquals("R", new String(dual.getCleavage(DualEnzyme.CTERMINAL)));
            }
            e = mer.getEnzyme("DuAlTrypCathep2");
            Assert.assertEquals("DuAlTrypCathep2", e.getTitle());
            Assert.assertTrue(e.getRestrict() == null);
            Assert.assertEquals(Enzyme.NTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());
            Assert.assertTrue(e instanceof DualEnzyme);
            if(e instanceof DualEnzyme) {
                DualEnzyme dual = (DualEnzyme)e;
                Assert.assertEquals("DK", new String(dual.getCleavage(DualEnzyme.NTERMINAL)));
                Assert.assertEquals("RW", new String(dual.getCleavage(DualEnzyme.CTERMINAL)));
            }

            e = mer.getEnzyme("ReGeXTrypCathep");
            Assert.assertEquals("ReGeXTrypCathep", e.getTitle());
            Assert.assertEquals("P", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());
            Assert.assertTrue(e instanceof RegExEnzyme);
            Assert.assertTrue(e.getClass().getName().equals("com.compomics.util.protein.RegExEnzyme"));
            RegExEnzyme regex = (RegExEnzyme)e;
            Assert.assertEquals("[KR]", new String(regex.getCleavage()));

            e = mer.getEnzyme("regexTrypCathep2");
            Assert.assertEquals("regexTrypCathep2", e.getTitle());
            Assert.assertTrue(e.getRestrict() == null);
            Assert.assertEquals(Enzyme.NTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());
            Assert.assertTrue(e instanceof RegExEnzyme);
            Assert.assertTrue(e.getClass().getName().equals("com.compomics.util.protein.RegExEnzyme"));
            regex = (RegExEnzyme)e;
            Assert.assertEquals("P+R", new String(regex.getCleavage()));
        } catch(IOException ioe) {
            fail("IOException while testing MascotEnzymeReader creation: '" + ioe.getMessage() + "'.");
        }
        // With inputstream.
        try {
            MascotEnzymeReader mer = new MascotEnzymeReader(new FileInputStream(inputFile));
            String[] names = mer.getEnzymeNames();

            // First see if the arrays are equal size.
            Assert.assertEquals(control.size(), names.length);

            Vector controlCopy = (Vector)control.clone();
            // Now check whether we can find all entries.
            for(int i = 0; i < names.length; i++) {
                for(int j = 0; j < controlCopy.size(); j++) {
                    if(names[i].equals(controlCopy.get(j))) {
                        controlCopy.remove(j);
                        break;
                    }
                }
            }
            // The control Vector should now be empty.
            Assert.assertEquals(0, controlCopy.size());

            // Next test each separate Enzyme entry.
            Enzyme e = mer.getEnzyme("Trypsin");
            Assert.assertEquals("Trypsin", e.getTitle());
            Assert.assertEquals("KR", new String(e.getCleavage()));
            Assert.assertEquals("P", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("Trypsin/P");
            Assert.assertEquals("Trypsin/P", e.getTitle());
            Assert.assertEquals("KR", new String(e.getCleavage()));
            Assert.assertTrue(e.getRestrict() == null);
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("TrypChymo");
            Assert.assertEquals("TrypChymo", e.getTitle());
            Assert.assertEquals("FYWLKR", new String(e.getCleavage()));
            Assert.assertEquals("P", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("PepsinA");
            Assert.assertEquals("PepsinA", e.getTitle());
            Assert.assertEquals("FL", new String(e.getCleavage()));
            Assert.assertTrue(e.getRestrict() == null);
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("None");
            Assert.assertEquals("None", e.getTitle());
            Assert.assertEquals("NONE", new String(e.getCleavage()));
            Assert.assertEquals("NONE", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.NTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());

            e = mer.getEnzyme("dualTrypCathep");
            Assert.assertEquals("dualTrypCathep", e.getTitle());
            Assert.assertEquals("DXR", new String(e.getCleavage()));
            Assert.assertEquals("P", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());
            Assert.assertTrue(e instanceof DualEnzyme);
            if(e instanceof DualEnzyme) {
                DualEnzyme dual = (DualEnzyme)e;
                Assert.assertEquals("D", new String(dual.getCleavage(DualEnzyme.NTERMINAL)));
                Assert.assertEquals("R", new String(dual.getCleavage(DualEnzyme.CTERMINAL)));
            }
            e = mer.getEnzyme("DuAlTrypCathep2");
            Assert.assertEquals("DuAlTrypCathep2", e.getTitle());
            //Assert.assertEquals("", new String(e.getCleavage()));
            Assert.assertTrue(e.getRestrict() == null);
            Assert.assertEquals(Enzyme.NTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());
            Assert.assertTrue(e instanceof DualEnzyme);
            if(e instanceof DualEnzyme) {
                DualEnzyme dual = (DualEnzyme)e;
                Assert.assertEquals("DK", new String(dual.getCleavage(DualEnzyme.NTERMINAL)));
                Assert.assertEquals("RW", new String(dual.getCleavage(DualEnzyme.CTERMINAL)));
            }

            e = mer.getEnzyme("ReGeXTrypCathep");
            Assert.assertEquals("ReGeXTrypCathep", e.getTitle());
            Assert.assertEquals("P", new String(e.getRestrict()));
            Assert.assertEquals(Enzyme.CTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());
            Assert.assertTrue(e instanceof RegExEnzyme);
            Assert.assertTrue(e.getClass().getName().equals("com.compomics.util.protein.RegExEnzyme"));
            RegExEnzyme regex = (RegExEnzyme)e;
            Assert.assertEquals("[KR]", new String(regex.getCleavage()));

            e = mer.getEnzyme("regexTrypCathep2");
            Assert.assertEquals("regexTrypCathep2", e.getTitle());
            Assert.assertTrue(e.getRestrict() == null);
            Assert.assertEquals(Enzyme.NTERM, e.getPosition());
            Assert.assertEquals(1, e.getMiscleavages());
            Assert.assertTrue(e instanceof RegExEnzyme);
            Assert.assertTrue(e.getClass().getName().equals("com.compomics.util.protein.RegExEnzyme"));
            regex = (RegExEnzyme)e;
            Assert.assertEquals("P+R", new String(regex.getCleavage()));

        } catch(IOException ioe) {
            fail("IOException while testing MascotEnzymeReader creation: '" + ioe.getMessage() + "'.");
        }

        try {
            MascotEnzymeReader mer = new MascotEnzymeReader("ThisFileIsNowhereInTHeFileSystem.grbl");
            fail("MascotEnzymeReader constructor did not respond with an IOException when confrontend with fictious file 'ThisFileIsNowhereInTHeFileSystem.grbl'!");
        } catch(IOException ioe) {
            // Correct throwing of the IOException.
        }
    }

    /**
     * This method test the 'return copy of enzyme' functionality when requesting an
     * enzyme instance.
     */
    public void testCopyOfEnzyme() {
        final String inputFile = TestCaseLM.getFullFilePath("enzymes_test.txt").replace("%20", " ");
        // Try it for a regular Enzyme.
        try {
            MascotEnzymeReader mer = new MascotEnzymeReader(inputFile);
            Enzyme mod = mer.getEnzyme(mer.getEnzymeNames()[0]);

            int mc = mod.getMiscleavages();
            String cleave = new String(mod.getCleavage());

            mod.setMiscleavages(mc+1);
            mod.setCleavage(cleave + "H");

            // Make sure 'mod' has changed.
            Assert.assertEquals(mc+1, mod.getMiscleavages());
            Assert.assertEquals(cleave+"H", new String(mod.getCleavage()));

            // Now get another copy of the same enzyme, and see if it has retained the original values.
            Enzyme original = mer.getEnzyme(mer.getEnzymeNames()[0]);
            Assert.assertEquals(mc, original.getMiscleavages());
            Assert.assertEquals(cleave, new String(original.getCleavage()));
        } catch(IOException ioe) {
            fail("IOException while testing whether a received Enzyme is in fact a copy: '" + ioe.getMessage() + "'.");
        }
        // Try it for a DualEnzyme.
        try {
            MascotEnzymeReader mer = new MascotEnzymeReader(inputFile);
            DualEnzyme mod = (DualEnzyme)mer.getEnzyme("dualTrypCathep");

            int mc = mod.getMiscleavages();
            String ntermCleave = new String(mod.getCleavage(DualEnzyme.NTERMINAL));
            String ctermCleave = new String(mod.getCleavage(DualEnzyme.CTERMINAL));

            mod.setMiscleavages(mc+1);
            mod.setCleavage(ntermCleave + "H", DualEnzyme.NTERMINAL);
            mod.setCleavage(ctermCleave + "W", DualEnzyme.CTERMINAL);

            // Make sure 'mod' has changed.
            Assert.assertEquals(mc+1, mod.getMiscleavages());
            Assert.assertEquals(ntermCleave+"H", new String(mod.getCleavage(DualEnzyme.NTERMINAL)));
            Assert.assertEquals(ctermCleave+"W", new String(mod.getCleavage(DualEnzyme.CTERMINAL)));

            // Now get another copy of the same enzyme, and see if it has retained the original values.
            DualEnzyme original = (DualEnzyme)mer.getEnzyme("dualTrypCathep");
            Assert.assertEquals(mc, original.getMiscleavages());
            Assert.assertEquals(ntermCleave, new String(original.getCleavage(DualEnzyme.NTERMINAL)));
            Assert.assertEquals(ctermCleave, new String(original.getCleavage(DualEnzyme.CTERMINAL)));
        } catch(IOException ioe) {
            fail("IOException while testing whether a received Enzyme is in fact a copy: '" + ioe.getMessage() + "'.");
        }
        // Try it for a RegExEnzyme.
        try {
            MascotEnzymeReader mer = new MascotEnzymeReader(inputFile);
            RegExEnzyme mod = (RegExEnzyme)mer.getEnzyme("regexTrypCathep2");

            int mc = mod.getMiscleavages();
            String cleave = new String(mod.getCleavage());

            mod.setMiscleavages(mc+1);
            mod.setCleavage(cleave + "H");

            // Make sure 'mod' has changed.
            Assert.assertEquals(mc+1, mod.getMiscleavages());
            Assert.assertEquals(cleave+"H", new String(mod.getCleavage()));

            // Now get another copy of the same enzyme, and see if it has retained the original values.
            Enzyme original = mer.getEnzyme("regexTrypCathep2");
            Assert.assertEquals(mc, original.getMiscleavages());
            Assert.assertEquals(cleave, new String(original.getCleavage()));
        } catch(IOException ioe) {
            fail("IOException while testing whether a received Enzyme is in fact a copy: '" + ioe.getMessage() + "'.");
        }
    }
}
