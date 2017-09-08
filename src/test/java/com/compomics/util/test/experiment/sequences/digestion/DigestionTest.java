package com.compomics.util.test.experiment.sequences.digestion;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.xmlpull.v1.XmlPullParserException;

/**
 * This test verifies the digestion by the enzyme class.
 *
 * @author Marc Vaudel
 */
public class DigestionTest extends TestCase {

    public void testEnzymeDigestion() throws XmlPullParserException, IOException {

        String testSequence = "MKMMKMMRMMMKPMMRMMMMMMMMMMMRMMMMRMM";

        EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
        Enzyme enzyme = enzymeFactory.getEnzyme("Trypsin");
        HashSet<String> peptides = enzyme.digest(testSequence, 2, 4, 8);

        Assert.assertTrue(peptides.size() == 6);
        Assert.assertTrue(peptides.contains("MKMMK"));
        Assert.assertTrue(peptides.contains("MMKMMR"));
        Assert.assertTrue(peptides.contains("MKMMKMMR"));
        Assert.assertTrue(peptides.contains("MMKMMR"));
        Assert.assertTrue(peptides.contains("MMMKPMMR"));
        Assert.assertTrue(peptides.contains("MMMMRMM"));
    }
    
    
}