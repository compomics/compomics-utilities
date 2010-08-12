package com.compomics.util.test.protein;
import org.apache.log4j.Logger;

import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Protein;
import com.compomics.util.protein.RegExEnzyme;
import junit.TestCaseLM;
import junit.framework.Assert;

import java.io.*;

/**
 * @author Florian Reisinger
 * @since 2.9
 */
public class TestRegExEnzyme  extends TestCaseLM {

    // Class specific log4j logger for TestRegExEnzyme instances.
    Logger logger = Logger.getLogger(TestRegExEnzyme.class);

    private static final boolean recordResult = false;
    private static final String resultFileName = "regExEmzymeTest1.fas";

    public TestRegExEnzyme() {
        this("Test scenario for the RegExEnzyme class.");
    }

    public TestRegExEnzyme(String s) {
        super(s);
    }

    /**
     * This method test the creation, setters and getters of an RegExEnzyme instance.
     * (Same tests as for Enzyme class! only slightly modified to fit RegExEnzyme)
     */
    public void testCreation() {
        // First the two standard constructors with meaningfull data.
        // Also test the different setters.
        final String title1 = "Title1";
        final String cleavage1 = "[ARNDCQ]";
        final String restrict1 = "PG";
        final char[] cv1 = cleavage1.toCharArray();
        final char[] rs1 = restrict1.toCharArray();
        final String pos1 = "Cterm";
        final String pos2 = "nteRm";
        final int miscleavage = 3;

        RegExEnzyme e = new RegExEnzyme(title1, cleavage1, restrict1, pos1);
        Assert.assertEquals(title1, e.getTitle());
        Assert.assertEquals(new String(cv1), new String(e.getCleavage()));
        Assert.assertEquals(new String(rs1), new String(e.getRestrict()));
        Assert.assertEquals(RegExEnzyme.CTERM, e.getPosition());
        Assert.assertEquals(1, e.getMiscleavages());

        final String otherTitle = "other";
        final String otherCleavage = "[HIK]";
        final String otherRestrict = "MN";
        final char[] otherCv = otherCleavage.toCharArray();
        final char[] otherRs = otherRestrict.toCharArray();

        e.setTitle(otherTitle);
        e.setCleavage(otherCleavage);
        e.setRestrict(otherRestrict);
        e.setPosition(RegExEnzyme.NTERM);
        e.setMiscleavages(miscleavage);

        Assert.assertEquals(otherTitle, e.getTitle());
        Assert.assertEquals(new String(otherCv), new String(e.getCleavage()));
        Assert.assertEquals(new String(otherRs), new String(e.getRestrict()));
        Assert.assertEquals(RegExEnzyme.NTERM, e.getPosition());
        Assert.assertEquals(miscleavage, e.getMiscleavages());

        e.setCleavage(cleavage1);
        e.setRestrict(rs1);
        Assert.assertEquals(new String(cv1), new String(e.getCleavage()));
        Assert.assertEquals(new String(rs1), new String(e.getRestrict()));

        e = new RegExEnzyme(null, cleavage1, null, pos2, 5);
        Assert.assertTrue(e.getTitle() == null);
        Assert.assertEquals(new String(cv1), new String(e.getCleavage()));
        Assert.assertTrue(e.getRestrict() == null);
        Assert.assertEquals(RegExEnzyme.NTERM, e.getPosition());
        Assert.assertEquals(5, e.getMiscleavages());

        try {
            e = new RegExEnzyme(title1, null, restrict1, null);
            fail("No NullPointerException thrown when Enzyme constructor was presented with a 'null' cleavage and position String!");
        } catch(NullPointerException npe) {
            // Okay, this is what we wanted.
        }

        try {
            e = new RegExEnzyme(title1, cleavage1, restrict1, null);
            fail("No NullPointerException thrown when Enzyme constructor was presented with a 'null' position String!");
        } catch(NullPointerException npe) {
            // Okay, this is what we wanted.
        }
    }

    /**
     * This method test the cleaving behaviour of the RegExEnzyme class.
     * (Same tests as for Enzyme class! only slightly modified to fit RegExEnzyme)
     */
    public void testCleave() {
        final String inputFile = "testCleave.fas";
        final String input = super.getFullFilePath(inputFile);

        try {
            // We need to obtain a pointer the control file before anything else.
            BufferedReader br = new BufferedReader(new FileReader(input));

            // First one with zero MC's.
            RegExEnzyme e = new RegExEnzyme("TestEnzyme", "[KR]", "P", "Cterm", 0);
            Protein p = new Protein(">sw|Q55645|TEST_HUMAN Test Protein for the cleave() method.", "GHIKLMVSTRPIGASDNPKLHGFVNRTGFDA");

            Protein[] result = e.cleave(p);
            if (recordResult) {
                // if there is a old file, delete it
                File resultFile = new File(resultFileName);
                if (resultFile.exists()) { resultFile.delete(); }
                writeResults(result, resultFileName);
            }
            boolean once = false;
            for(int i = 0; i < result.length; i++) {
                once = true;
                Protein lProtein = result[i];
                String header = lProtein.getHeader().getFullHeaderWithAddenda();
                String sequence = lProtein.getSequence().getSequence();
                Assert.assertEquals(br.readLine(), header);
                Assert.assertEquals(br.readLine(), sequence);
            }

            if(!once) {
                fail("NO peptides AT ALL were returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }

            // Skip to the next part of the controlfile.
            br.readLine();  // 1
            once = false;

            // Now with one miscleavage.
            e.setMiscleavages(1);
            result = e.cleave(p);
            if (recordResult) { writeResults(result, resultFileName); }
            for(int i = 0; i < result.length; i++) {
                once = true;
                Protein lProtein = result[i];
                String header = lProtein.getHeader().getFullHeaderWithAddenda();
                String sequence = lProtein.getSequence().getSequence();
                Assert.assertEquals(br.readLine(), header);
                Assert.assertEquals(br.readLine(), sequence);
            }

            if(!once) {
                fail("NO peptides AT ALL were returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }


            // Skip to the next part of the controlfile.
            br.readLine(); // 2
            once = false;

            // Now with three miscleavages.
            e.setMiscleavages(3);
            result = e.cleave(p);
            if (recordResult) { writeResults(result, resultFileName); }
            for(int i = 0; i < result.length; i++) {
                once = true;
                Protein lProtein = result[i];
                String header = lProtein.getHeader().getFullHeaderWithAddenda();
                String sequence = lProtein.getSequence().getSequence();
                Assert.assertEquals(br.readLine(), header);
                Assert.assertEquals(br.readLine(), sequence);
            }

            if(!once) {
                fail("NO peptides AT ALL were returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }

            // Now check for the edge case where there's only one amino acid after the last cleavage position
            // (used to be a bug that this amino acid was never considered).
            e.setMiscleavages(1);
            result = e.cleave(new Protein(">sw|Q55645|TEST_HUMAN Test Protein for the cleave() method.", "GHIKLMVSTRPIGASDNPKLHGFVNRTGFDRA"));
            boolean foundIt = false;
            for(int i = 0; i < result.length; i++) {
                Protein lProtein = result[i];
                if(lProtein.getSequence().getSequence().equals("TGFDRA")){
                    foundIt = true;
                    break;
                }
            }

            if(!foundIt) {
                fail("The peptide 'TGFDRA' (penultimate amino acid is cleavage site, one missed cleavage allowed) was NOT returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }

//            // skip next section
//            for (int i = 0; i < 15; i++) {
//                br.readLine();
//            }
            // Skip to the next part of the controlfile.
            br.readLine();  // 3
            once = false;

            // Now Nterm position and 1 miscleavage.
            e.setPosition(Enzyme.NTERM);
            e.setMiscleavages(1);
            result = e.cleave(p);
            if (recordResult) { writeResults(result, resultFileName); }

            for(int i = 0; i < result.length; i++) {
                once = true;
                Protein lProtein = result[i];
                String header = lProtein.getHeader().getFullHeaderWithAddenda();
                String sequence = lProtein.getSequence().getSequence();
                Assert.assertEquals(br.readLine(), header);
                Assert.assertEquals(br.readLine(), sequence);
            }

            if(!once) {
                fail("NO peptides AT ALL were returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }

            // Skip to the next part of the controlfile.
            br.readLine();  // 4
            once = false;

            // Next, take an enzyme that does not have restricting residus.
            e = new RegExEnzyme("Test", "[KR]", null, "Cterm", 0);
            result = e.cleave(p);
            if (recordResult) { writeResults(result, resultFileName); }
            for(int i = 0; i < result.length; i++) {
                once = true;
                Protein lProtein = result[i];
                String header = lProtein.getHeader().getFullHeaderWithAddenda();
                String sequence = lProtein.getSequence().getSequence();
                Assert.assertEquals(br.readLine(), header);
                Assert.assertEquals(br.readLine(), sequence);
            }

            if(!once) {
                fail("NO peptides AT ALL were returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }

            // Skip to the next part of the controlfile.
            br.readLine();  // 5
            once = false;

            // And now take an entry that has a location set and see if the cleavage is adjusted accordingly.
            e = new RegExEnzyme("TestEnzyme", "[KR]", "P", "Cterm", 1);
            p = new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "GHIKLMVSTRPIGASDNPKLHGFVNRTGFDA");

            result = e.cleave(p);
            if (recordResult) { writeResults(result, resultFileName); }
            for(int i = 0; i < result.length; i++) {
                once = true;
                Protein lProtein = result[i];
                String header = lProtein.getHeader().getFullHeaderWithAddenda();
                String sequence = lProtein.getSequence().getSequence();
                Assert.assertEquals(br.readLine(), header);
                Assert.assertEquals(br.readLine(), sequence);
            }

            if(!once) {
                fail("NO peptides AT ALL were returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }

            // Skip to the next part of the controlfile.
            br.readLine();  // 6
            once = false;

            // Now take a C-terminally truncated sequence and see if it is cleaved correctly.
            e = new RegExEnzyme("TestEnzyme", "[KR]", "P", "Cterm", 1);
            p = new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "GHIKLMVSTRPIGASDNPKLHGFVNRTGFDA", true, Protein.CTERMTRUNC);

            result = e.cleave(p);
            if (recordResult) { writeResults(result, resultFileName); }
            for(int i = 0; i < result.length; i++) {
                once = true;
                Protein lProtein = result[i];
                String header = lProtein.getHeader().getFullHeaderWithAddenda();
                String sequence = lProtein.getSequence().getSequence();
                Assert.assertEquals(br.readLine(), header);
                Assert.assertEquals(br.readLine(), sequence);
            }

            if(!once) {
                fail("NO peptides AT ALL were returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }

            // Skip to the next part of the controlfile.
            br.readLine();  // 7
            once = false;

            // Now take an N-terminally truncated sequence and see if it is cleaved correctly.
            e = new RegExEnzyme("TestEnzyme", "[KR]", "P", "Cterm", 1);
            p = new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "GHIKLMVSTRPIGASDNPKLHGFVNRTGFDA", true, Protein.NTERMTRUNC);

            result = e.cleave(p);
            if (recordResult) { writeResults(result, resultFileName); }
            for(int i = 0; i < result.length; i++) {
                once = true;
                Protein lProtein = result[i];
                String header = lProtein.getHeader().getFullHeaderWithAddenda();
                String sequence = lProtein.getSequence().getSequence();
                Assert.assertEquals(br.readLine(), header);
                Assert.assertEquals(br.readLine(), sequence);
            }

            if(!once) {
                fail("NO peptides AT ALL were returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }

            // Skip to the next part of the controlfile.
            br.readLine();  // 8
            once = false;

            // Now take a translated sequence, containing an '_' (stopcodon).
            e = new RegExEnzyme("TestEnzyme", "[KR]", "P", "Cterm", 1);
            p = new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "GHIKLMVSTRPIGASDNPKLHG_FVNRTGFDA");

            result = e.cleave(p);
            if (recordResult) { writeResults(result, resultFileName); }
            for(int i = 0; i < result.length; i++) {
                once = true;
                Protein lProtein = result[i];
                String header = lProtein.getHeader().getFullHeaderWithAddenda();
                String sequence = lProtein.getSequence().getSequence();
                Assert.assertEquals(br.readLine(), header);
                Assert.assertEquals(br.readLine(), sequence);
            }

            if(!once) {
                fail("NO peptides AT ALL were returned when cleaving '" + p.getSequence().getSequence() + "'!");
            }

            // END of file reached.
            // Check this!
            String line;
            while((line = br.readLine()) != null) {
                if(!line.trim().equals("")) {
                    fail("More lines in testCleave.fas then were generated by the test cleavage!");
                }
            }
            br.close();
        } catch(IOException ioe) {
            fail("IOException occurred while testing the cleave() method: '" + ioe.getMessage() + "'.");
        }
    }

    /**
     * This method test the cloning of an RegExEnzyme.
     * (Same tests as for Enzyme class! only slightly modified to fit RegExEnzyme)
     */
    public void testClone() {
        RegExEnzyme e = new RegExEnzyme("Test", "[KR]", "P", "Cterm", 5);
        Enzyme clone = (Enzyme)e.clone();

        Assert.assertEquals(new String(e.getCleavage()), new String(clone.getCleavage()));
        Assert.assertEquals(e.getMiscleavages(), clone.getMiscleavages());
        Assert.assertEquals(e.getPosition(), clone.getPosition());
        Assert.assertEquals(new String(e.getRestrict()), new String(clone.getRestrict()));
        Assert.assertEquals(e.getTitle(), clone.getTitle());

        // Get a cleaved set of peptides from a sequence.
        final Protein p = new Protein(">Test protein.\nLENNARTMARTENS");
        Protein[] controlCleave = e.cleave(p);
        // Get a cleaved set from the clone.
        Protein[] cloneCleave = clone.cleave(p);
        // Compare the sets. They should be identical.
        for(int i = 0; i < cloneCleave.length; i++) {
            Assert.assertEquals(controlCleave[i], cloneCleave[i]);
        }

        // Now change the clone and see if the original changes.
        clone.setCleavage("GH");
        clone.setMiscleavages(1);
        clone.setPosition(Enzyme.NTERM);
        clone.setRestrict("L");
        clone.setTitle("Clone");

        // Clone should have changed.
        Assert.assertEquals("GH", new String(clone.getCleavage()));
        Assert.assertEquals(1, clone.getMiscleavages());
        Assert.assertEquals(Enzyme.NTERM, clone.getPosition());
        Assert.assertEquals("L", new String(clone.getRestrict()));
        Assert.assertEquals("Clone", clone.getTitle());

        // Original should remain the same.
        Assert.assertEquals("[KR]", new String(e.getCleavage()));
        Assert.assertEquals(5, e.getMiscleavages());
        Assert.assertEquals(Enzyme.CTERM, e.getPosition());
        Assert.assertEquals("P", new String(e.getRestrict()));
        Assert.assertEquals("Test", e.getTitle());
    }

    /**
     * This method test whether the isEnzymaticProduct method functions.
     * (Same tests as for Enzyme class! only slightly modified to fit RegExEnzyme)
     */
    public void testIsProduct() {
        // C-terminal cleavage.
        RegExEnzyme e = new RegExEnzyme("Test", "[KR]", "P", "Cterm", 1);
        Protein p = new Protein(">Test Protein.\nLENNARTMARTENS");

        // A non-enzymatic product by sequence and subsequently by (human readable!) indices.
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "ENN"));
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 2, 4));

        // A half enzymatic sequence (N-terminal), also with the true N-terminus included.
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "TMARTE"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 7, 12));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "LEN"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 1, 3));

        // A half enzymatic sequence (C-terminal), also with the true C-terminus included.
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "NNAR"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 3, 6));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), "RTENS"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct(p.getSequence().getSequence(), 10, 14));

        // Check the correct behaviour when multiple occurrences take place.
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("LGHJKLIGHKL", "GH"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("GHJKLIGHKL", "GH"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("FTTRGHJKLIGHKL", "GH"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("FTTGKLIGKPL", "GK"));
        Assert.assertEquals(Enzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("FTTRGKLIGKPL", "GK"));

        // Now check the correct handling of restriction locations.
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("LGHRPHVVGRPLMM", "PHVVGR"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHRHVVGRPLMM", "HVVGR"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("HVVGRPLMM", "HVVGR"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHRPHVVGR", "PHVVGR"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHRPHVVGRLMM", "PHVVGR"));
        Assert.assertEquals(Enzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("LGHRHVVGRLMM", "HVVGR"));

        // N-terminal cleavage.
        e = new RegExEnzyme("Test2", "[KR]", "P", "Nterm", 1);

        // A non-enzymatic product.
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("KARVAL", "ARVA"));
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("KARVAL", 2, 5));

        // A half enzymatic sequence (N-terminal), also with the true N-terminus included.
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("LLKARVAL", "KARV"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("LLKARVAL", 3, 6));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("ARVAL", "ARV"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("ARVAL", 1, 3));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("KARVAL", "KARV"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("KARVAL", 1, 4));


        // A half enzymatic sequence (C-terminal), also with the true C-terminus included.
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LLKARVAL", "LKA"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LLKARVAL", 2, 4));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LLKARVAL", "VAL"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LLKARVAL", 6, 8));

        // Check the correct behaviour when multiple occurrences take place.
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("LLKARVALGHGHGLKARVRAL", "LKARV"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("GHKARVALGHKARVAL", "KARV"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LLKARVALGHGHLKARPAL", "LKA"));
        Assert.assertEquals(Enzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("LLKARVALGHGHLKARPAL", "KA"));

        // Now check the correct handling of restriction locations.
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("LGHRPHVVGRPLMM", "RPHVVG"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHRHVVGRPLMM", "RHVVG"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("HVVGRPLMM", "HVVG"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHRPHVVGR", "RPHVVG"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("LGHRPHVVGRLMM", "RPHVVG"));
        Assert.assertEquals(Enzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("LGHRHVVGRLMM", "RHVVG"));

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

    /**
     * This method test a simple Caspase-like cleaving, since it is a real,
     * multi-residue matching regular expression.
     */
    public void testCaspaseCleaving() {

        // C-terminal cleavage.
        final String title1 = "regexCaspase";
        final String cleavage1 = "D..D";
        final char[] cv1 = cleavage1.toCharArray();
        final String pos1 = "Cterm";
        final String pos2 = "nteRm";

        RegExEnzyme e = new RegExEnzyme(title1, cleavage1, null, pos1);
        e.setMiscleavages(0);
        Assert.assertEquals(title1, e.getTitle());
        Assert.assertEquals(new String(cv1), new String(e.getCleavage()));
        Assert.assertEquals(RegExEnzyme.CTERM, e.getPosition());

        Protein p = new Protein(">Test Protein.\nGHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS");
        Protein[] products = e.cleave(p);
        Assert.assertEquals(4, products.length);
        Assert.assertEquals("GHLKFDMTD", products[0].getSequence().getSequence());
        Assert.assertEquals("NRSGHLKFDMTD", products[1].getSequence().getSequence());
        Assert.assertEquals("NRSGHLKFDMTD", products[2].getSequence().getSequence());
        Assert.assertEquals("NRS", products[3].getSequence().getSequence());
        Assert.assertEquals(1, products[0].getHeader().getStartLocation());
        Assert.assertEquals(9, products[0].getHeader().getEndLocation());
        Assert.assertEquals(10, products[1].getHeader().getStartLocation());
        Assert.assertEquals(21, products[1].getHeader().getEndLocation());
        Assert.assertEquals(22, products[2].getHeader().getStartLocation());
        Assert.assertEquals(33, products[2].getHeader().getEndLocation());
        Assert.assertEquals(34, products[3].getHeader().getStartLocation());
        Assert.assertEquals(36, products[3].getHeader().getEndLocation());

        Assert.assertEquals(Enzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("GHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS", "NRSGHLKFDMTD"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("GHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS", "NRSGHLKF"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("GHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS", "LKFDMTD"));
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("GHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS", "LKFD"));

        // N-terminal cleavage.
        e = new RegExEnzyme(title1, cleavage1, null, pos2);
        e.setMiscleavages(0);
        Assert.assertEquals(title1, e.getTitle());
        Assert.assertEquals(new String(cv1), new String(e.getCleavage()));
        Assert.assertEquals(RegExEnzyme.NTERM, e.getPosition());

        p = new Protein(">Test Protein.\nGHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS");
        products = e.cleave(p);
        Assert.assertEquals(4, products.length);
        Assert.assertEquals("GHLKFDMT", products[0].getSequence().getSequence());
        Assert.assertEquals("DNRSGHLKFDMT", products[1].getSequence().getSequence());
        Assert.assertEquals("DNRSGHLKFDMT", products[2].getSequence().getSequence());
        Assert.assertEquals("DNRS", products[3].getSequence().getSequence());
        Assert.assertEquals(1, products[0].getHeader().getStartLocation());
        Assert.assertEquals(8, products[0].getHeader().getEndLocation());
        Assert.assertEquals(9, products[1].getHeader().getStartLocation());
        Assert.assertEquals(20, products[1].getHeader().getEndLocation());
        Assert.assertEquals(21, products[2].getHeader().getStartLocation());
        Assert.assertEquals(32, products[2].getHeader().getEndLocation());
        Assert.assertEquals(33, products[3].getHeader().getStartLocation());
        Assert.assertEquals(36, products[3].getHeader().getEndLocation());

        Assert.assertEquals(Enzyme.FULLY_ENZYMATIC, e.isEnzymaticProduct("GHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS", "DNRSGHLKFDMT"));
        Assert.assertEquals(Enzyme.N_TERM_ENZYMATIC, e.isEnzymaticProduct("GHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS", "DNRSGHLKF"));
        Assert.assertEquals(Enzyme.C_TERM_ENZYMATIC, e.isEnzymaticProduct("GHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS", "LKFDMT"));
        Assert.assertEquals(Enzyme.ENTIRELY_NOT_ENZYMATIC, e.isEnzymaticProduct("GHLKFDMTDNRSGHLKFDMTDNRSGHLKFDMTDNRS", "LKFD"));
    }


    private void writeResults(Protein[] proteins, String filename) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));

        for (int i = 0; i < proteins.length; i++) {
            Protein protein = proteins[i];
            String header = protein.getHeader().getFullHeaderWithAddenda();
            String sequence = protein.getSequence().getSequence();
            writer.write(header); writer.newLine();
            writer.write(sequence); writer.newLine();
        }
        writer.newLine();
        writer.flush();
        writer.close();
    }
}
