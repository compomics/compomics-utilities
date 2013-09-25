/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import java.util.ArrayList;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class tests the application of amino acid patterns on an amino acid
 * sequence
 *
 * @author Marc
 */
public class AminoAcidPatternTest extends TestCase {

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
        input = "IJX";
        AminoAcidPattern pattern = new AminoAcidPattern("IJX");
        Assert.assertTrue(pattern.matches(input, ProteinMatch.MatchingType.indistiguishibleAminoAcids, 0.5));
        pattern = new AminoAcidPattern("IIX");
        Assert.assertTrue(pattern.matches(input, ProteinMatch.MatchingType.indistiguishibleAminoAcids, 0.5));
        pattern = new AminoAcidPattern("JJX");
        Assert.assertTrue(pattern.matches(input, ProteinMatch.MatchingType.indistiguishibleAminoAcids, 0.5));
        pattern = new AminoAcidPattern("JJJ");
        Assert.assertTrue(pattern.matches(input, ProteinMatch.MatchingType.indistiguishibleAminoAcids, 0.5));
        pattern = new AminoAcidPattern("XXX");
        Assert.assertTrue(pattern.matches(input, ProteinMatch.MatchingType.indistiguishibleAminoAcids, 0.5));
    }
}
