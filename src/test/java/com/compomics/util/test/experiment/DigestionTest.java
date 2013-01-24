/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.xmlpull.v1.XmlPullParserException;

/**
 * This test verifies the digestion by the enzyme class
 *
 * @author Marc
 */
public class DigestionTest extends TestCase {

    public void testDigestion() throws XmlPullParserException, IOException {

        String testSequence = "MKMMKMMRMMMKPMMRMMMMMMMMMMMRMMMMRMM";

        EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
        File enzymeFile = new File("src/test/resources/experiment/enzymes.xml");
        enzymeFactory.importEnzymes(enzymeFile);
        Enzyme enzyme = enzymeFactory.getEnzyme("Trypsin");
        ArrayList<String> peptides = enzyme.digest(testSequence, 2, 4, 8);

        Assert.assertTrue(peptides.size()==6);
        Assert.assertTrue(peptides.contains("MKMMK"));
        Assert.assertTrue(peptides.contains("MMKMMR"));
        Assert.assertTrue(peptides.contains("MKMMKMMR"));
        Assert.assertTrue(peptides.contains("MMKMMR"));
        Assert.assertTrue(peptides.contains("MMMKPMMR"));
        Assert.assertTrue(peptides.contains("MMMMRMM"));
        
        
    }
}
