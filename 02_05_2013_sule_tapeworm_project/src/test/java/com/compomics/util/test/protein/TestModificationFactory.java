/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 1-jul-2004
 * Time: 16:14:00
 */
package com.compomics.util.test.protein;

import com.compomics.util.interfaces.Modification;
import com.compomics.util.junit.TestCaseLM;
import com.compomics.util.protein.ModificationFactory;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class implements the test scenario for the ModificationFactory class.
 *
 * @see com.compomics.util.protein.ModificationFactory
 * @author Lennart Martens
 */
public class TestModificationFactory extends TestCase {

    // Class specific log4j logger for TestModificationFactory instances.
    Logger logger = Logger.getLogger(TestModificationFactory.class);

    public TestModificationFactory() {
        this("Test scenario for the ModificationFactory class.");
    }

    public TestModificationFactory(String aName) {
        super(aName);
    }

    /**
     * This method test the correct parsing of the 'modifications.txt' file .
     */
    public void testModificationsParsing() {
        try {
            String result = ModificationFactory.modificationsToString();

            StringReader sr = new StringReader(result);
            BufferedReader test = new BufferedReader(sr);
            BufferedReader control = new BufferedReader(new FileReader(TestCaseLM.getFullFilePath("testModificationParser_control.txt").replace("%20", " ")));
            String line = null;
            while((line = test.readLine()) != null) {
                Assert.assertEquals(control.readLine(), line);
            }
            Assert.assertTrue(control.readLine() == null);
            control.close();
            test.close();
            sr.close();
        } catch(IOException ioe) {
            fail("IOException occurred while testing the parsing of the 'modifications.txt' file by the ModificationFactory class: " + ioe.getMessage());
        }
    }

    /**
     * This method test the reading of the title-to-code conversion mapping from
     * the 'modificationConversion.txt' file.
     */
    public void testCodesParsing() {
        // First part of the test: has the file been read correctly.
        try {
            String result = ModificationFactory.modificationConversionToString();

            StringReader sr = new StringReader(result);
            BufferedReader test = new BufferedReader(sr);
            BufferedReader control = new BufferedReader(new FileReader(TestCaseLM.getFullFilePath("testModificationConversionParser_control.txt").replace("%20", " ")));
            String line = null;
            while((line = test.readLine()) != null) {
                Assert.assertEquals(control.readLine(), line);
            }
            Assert.assertTrue(control.readLine() == null);
            control.close();
            test.close();
            sr.close();
        } catch(IOException ioe) {
            fail("IOException occurred while testing the parsing of the 'modificationConversion.txt' file by the ModificationFactory class: " + ioe.getMessage());
        }
    }

    /**
     * This method test the retrieval of Modification objects based on title.
     */
    public void testModificationRetrievalByTitle() {
        // Correct ones.
        Modification mod = ModificationFactory.getModification("Argbiotinhydrazide (R)", 1);
        Assert.assertTrue(mod != null);
        mod = ModificationFactory.getModification("Biotin-S-S-N-term (N-term)", 1);
        Assert.assertTrue(mod != null);
        mod = ModificationFactory.getModification("S-pyridylethyl (C)", 1);
        Assert.assertTrue(mod != null);

        // Incorrect ones.
        mod = ModificationFactory.getModification("S-pyridylethyl (C", 1);
        Assert.assertTrue(mod == null);
        mod = ModificationFactory.getModification("-pyridylethyl (C)", 1);
        Assert.assertTrue(mod == null);
        mod = ModificationFactory.getModification("StupidModThatNooneWillEverThinkOfLetAloneImplement", 1);
        Assert.assertTrue(mod == null);
    }

    /**
     * This method test the retrieval of Modification objects based on a code/residue combination.
     */
    public void testModificationRetrievalByCodeResidueCombination() {
        // Correct ones.
        Modification mod = ModificationFactory.getModification("Ace", "K", 1);
        Assert.assertTrue(mod != null);
        mod = ModificationFactory.getModification("Ace", Modification.NTERMINUS, 1);
        Assert.assertTrue(mod != null);
        mod = ModificationFactory.getModification("Pyr", "E", 1);
        Assert.assertTrue(mod != null);
        mod = ModificationFactory.getModification("Pyr", "Q", 1);
        Assert.assertTrue(mod != null);
        mod = ModificationFactory.getModification("Met", Modification.CTERMINUS, 1);
        Assert.assertTrue(mod != null);
        mod = ModificationFactory.getModification("Cmm", "C", 1);
        Assert.assertTrue(mod != null);

        // Incorrect ones.
        mod = ModificationFactory.getModification("Ace", "P", 1);
        Assert.assertTrue(mod == null);
        mod = ModificationFactory.getModification("Ace", Modification.CTERMINUS, 1);
        Assert.assertTrue(mod == null);
        mod = ModificationFactory.getModification("Ace", "L", 1);
        Assert.assertTrue(mod == null);
        mod = ModificationFactory.getModification("Ace", "G", 1);
        Assert.assertTrue(mod == null);
        mod = ModificationFactory.getModification("Met", Modification.NTERMINUS, 1);
        Assert.assertTrue(mod == null);
        mod = ModificationFactory.getModification("Cmm", "K", 1);
        Assert.assertTrue(mod == null);
    }
}
