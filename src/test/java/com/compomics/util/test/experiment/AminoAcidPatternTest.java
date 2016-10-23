package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class tests the application of amino acid patterns on an amino acid
 * sequence.
 *
 * @author Marc Vaudel
 */
public class AminoAcidPatternTest extends TestCase {

    public void testIndexes() {
        String input = "KTESTRTESTKPTESTK";
        AminoAcidPattern trypsinExample = AminoAcidPattern.getTrypsinExample();
        ArrayList<Integer> indexes = trypsinExample.getIndexes(input, SequenceMatchingPreferences.defaultStringMatching);
        Assert.assertTrue(indexes.size() == 2);
        Assert.assertTrue(indexes.get(0) == 1);
        Assert.assertTrue(indexes.get(1) == 6);
        input = "KTESTRTESTKPTESTKT";
        trypsinExample = AminoAcidPattern.getTrypsinExample();
        indexes = trypsinExample.getIndexes(input, SequenceMatchingPreferences.defaultStringMatching);
        Assert.assertTrue(indexes.size() == 3);
        Assert.assertTrue(indexes.get(0) == 1);
        Assert.assertTrue(indexes.get(1) == 6);
        Assert.assertTrue(indexes.get(2) == 17);
        input = "RRR";
        trypsinExample = AminoAcidPattern.getTrypsinExample();
        indexes = trypsinExample.getIndexes(input, SequenceMatchingPreferences.defaultStringMatching);
        Assert.assertTrue(indexes.size() == 2);
        Assert.assertTrue(indexes.get(0) == 1);
        Assert.assertTrue(indexes.get(1) == 2);
        input = "IJX";
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        AminoAcidPattern pattern = AminoAcidPattern.getAminoAcidPatternFromString("IJX");
        Assert.assertTrue(pattern.matches(input, sequenceMatchingPreferences));
        pattern = AminoAcidPattern.getAminoAcidPatternFromString("IIX");
        Assert.assertTrue(pattern.matches(input, sequenceMatchingPreferences));
        pattern = AminoAcidPattern.getAminoAcidPatternFromString("JJX");
        Assert.assertTrue(pattern.matches(input, sequenceMatchingPreferences));
        pattern = AminoAcidPattern.getAminoAcidPatternFromString("JJJ");
        Assert.assertTrue(pattern.matches(input, sequenceMatchingPreferences));
        pattern = AminoAcidPattern.getAminoAcidPatternFromString("XXX");
        Assert.assertTrue(pattern.matches(input, sequenceMatchingPreferences));
    }
}
