/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.AminoAcidPattern;
import java.util.ArrayList;
import junit.framework.Assert;

/**
 * This class tests the application of amino acid patterns on an amino acid sequence
 *
 * @author Marc
 */
public class AminoAcidPatternTest {
    
    public void testIndexes() {
        String input = "KTESTRTESTKPTESTK";
        ArrayList<Integer> indexes = AminoAcidPattern.getTrypsinExample().getIndexes(input);
        Assert.assertTrue(indexes.size() == 2);
        Assert.assertTrue(indexes.get(0) == 1);
        Assert.assertTrue(indexes.get(1) == 6);
        input = "KTESTRTESTKPTESTKT";
        indexes = AminoAcidPattern.getTrypsinExample().getIndexes(input);
        Assert.assertTrue(indexes.size() == 3);
        Assert.assertTrue(indexes.get(0) == 1);
        Assert.assertTrue(indexes.get(1) == 6);
        Assert.assertTrue(indexes.get(2) == 17);
        input = "RRR";
        indexes = AminoAcidPattern.getTrypsinExample().getIndexes(input);
        Assert.assertTrue(indexes.size() == 2);
        Assert.assertTrue(indexes.get(0) == 1);
        Assert.assertTrue(indexes.get(1) == 2);
    }
    
}
