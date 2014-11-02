/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.MutationMatrix;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class tests the matching of amino acids
 *
 * @author Marc
 */
public class SequenceMatchingTest extends TestCase {
    
    /**
     * Tests the import and the mapping of a few peptide sequences.
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws InterruptedException
     */
    public void testSequenceMatchingPreferences() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {
        
        String ref = "TESTIKTEST";
        String testAminoAcid = "TESTJKTEST";
        String testIndistinguishible = "TESTLQTEST";
        String test1Mutation = "TESTLQTETT";
        String test2Mutations = "TESTLQTETS";
        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("TESTIKTEST");
        
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.string);
        Assert.assertTrue(aminoAcidSequence.matches(ref, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(testAminoAcid, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(testIndistinguishible, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(test1Mutation, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(test2Mutations, sequenceMatchingPreferences));
        
        sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.aminoAcid);
        Assert.assertTrue(aminoAcidSequence.matches(ref, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(testAminoAcid, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(testIndistinguishible, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(test1Mutation, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(test2Mutations, sequenceMatchingPreferences));
        
        sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setMs2MzTolerance(0.5);
        Assert.assertTrue(aminoAcidSequence.matches(ref, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(testAminoAcid, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(testIndistinguishible, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(test1Mutation, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(test2Mutations, sequenceMatchingPreferences));
        
        
        sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setMs2MzTolerance(0.5);
        MutationMatrix mutationMatrix = MutationMatrix.synonymousMutation;
        sequenceMatchingPreferences.setMutationMatrix(mutationMatrix);
        sequenceMatchingPreferences.setMaxMutationsPerPeptide(1);
        Assert.assertTrue(aminoAcidSequence.matches(ref, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(testAminoAcid, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(testIndistinguishible, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(test1Mutation, sequenceMatchingPreferences));
        Assert.assertTrue(!aminoAcidSequence.matches(test2Mutations, sequenceMatchingPreferences));
        
        
        sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setMs2MzTolerance(0.5);
        sequenceMatchingPreferences.setMutationMatrix(mutationMatrix);
        Assert.assertTrue(aminoAcidSequence.matches(ref, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(testAminoAcid, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(testIndistinguishible, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(test1Mutation, sequenceMatchingPreferences));
        Assert.assertTrue(aminoAcidSequence.matches(test2Mutations, sequenceMatchingPreferences));
        
    }
}
