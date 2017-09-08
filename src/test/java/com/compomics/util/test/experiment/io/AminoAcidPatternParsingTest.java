package com.compomics.util.test.experiment.io;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class tests the parsing of amino acid patterns.
 *
 * @author Marc Vaudel
 */
public class AminoAcidPatternParsingTest extends TestCase {

    
    public void testParsing() {
        
        String testInput = "TEST";
        String expectedOutput = testInput;
        AminoAcidPattern aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString(testInput);
        String testOutput = aminoAcidPattern.toString();
        Assert.assertTrue(expectedOutput.equals(testOutput));
        
        testInput = "[TEST]TE[ST]";
        expectedOutput = testInput;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString(testInput);
        testOutput = aminoAcidPattern.toString();
        Assert.assertTrue(expectedOutput.equals(testOutput));
    }

}
