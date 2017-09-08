package com.compomics.util.test.experiment.io;

import com.compomics.util.experiment.biology.atoms.AtomChain;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class tests the parsing of atom chains.
 * 
 * @author Marc Vaudel
 */
public class AtomChainParsingTest extends TestCase {

    
    public void testParsing() {
        
        String testInput = "CO";
        String expectedOutput = testInput;
        AtomChain testAtomChain = AtomChain.getAtomChain(testInput);
        String testOutput = testAtomChain.toString();
        Assert.assertTrue(expectedOutput.equals(testOutput));
        
        testInput = "OC";
        expectedOutput = "CO";
        testAtomChain = AtomChain.getAtomChain(testInput);
        testOutput = testAtomChain.toString();
        Assert.assertTrue(expectedOutput.equals(testOutput));
        
        testInput = "18O13C";
        expectedOutput = "13C18O";
        testAtomChain = AtomChain.getAtomChain(testInput);
        testOutput = testAtomChain.toString();
        Assert.assertTrue(expectedOutput.equals(testOutput));
        
        testInput = "18O(4)13C";
        expectedOutput = "13C18O(4)";
        testAtomChain = AtomChain.getAtomChain(testInput);
        testOutput = testAtomChain.toString();
        Assert.assertTrue(expectedOutput.equals(testOutput));
        
        testInput = "18O(4) 13C";
        expectedOutput = "13C18O(4)";
        testAtomChain = AtomChain.getAtomChain(testInput);
        testOutput = testAtomChain.toString();
        Assert.assertTrue(expectedOutput.equals(testOutput));
        
        testInput = "18O(24)13C";
        expectedOutput = "13C18O(24)";
        testAtomChain = AtomChain.getAtomChain(testInput);
        testOutput = testAtomChain.toString();
        Assert.assertTrue(expectedOutput.equals(testOutput));
        
        testInput = "18O(4)Na13C";
        expectedOutput = "13CNa18O(4)";
        testAtomChain = AtomChain.getAtomChain(testInput);
        testOutput = testAtomChain.toString();
        Assert.assertTrue(expectedOutput.equals(testOutput));
    }
    
}
