package com.compomics.util.test.experiment;

import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test for the protein tree.
 *
 * @author Marc Vaudel
 */
public class ProteinTreeTest extends TestCase {

    public void testProteinTree() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences");

        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences);

        ProteinTree proteinTree = new ProteinTree(1);
        proteinTree.initiateTree(3, 500, 15, null, true);

        HashMap<String, ArrayList<Integer>> testIndexes = proteinTree.getProteinMapping("SSS");
        Assert.assertTrue(testIndexes.size() == 2);
        ArrayList<Integer> indexes = testIndexes.get("Q9FHX5");
        String sequence = sequenceFactory.getProtein("Q9FHX5").getSequence();
        Assert.assertTrue(indexes.size() == 3);
        Collections.sort(indexes);
        int index = sequence.indexOf("SSS");
        Assert.assertTrue(indexes.get(0) == index);
        index += sequence.substring(index + 1).indexOf("SSS") + 1;
        Assert.assertTrue(indexes.get(1) == index);
        index = sequence.lastIndexOf("SSS");
        Assert.assertTrue(indexes.get(2) == index);
        indexes = testIndexes.get("Q9FHX5_REVERSED");
        sequence = sequenceFactory.getProtein("Q9FHX5_REVERSED").getSequence();
        Assert.assertTrue(indexes.size() == 3);
        Collections.sort(indexes);
        index = sequence.indexOf("SSS");
        Assert.assertTrue(indexes.get(0) == index);
        index += sequence.substring(index + 1).indexOf("SSS") + 1;
        Assert.assertTrue(indexes.get(1) == index);
        index = sequence.lastIndexOf("SSS");
        Assert.assertTrue(indexes.get(2) == index);

        testIndexes = proteinTree.getProteinMapping("MEDDRV");
        Assert.assertTrue(testIndexes.size() == 1);
        indexes = testIndexes.get("Q9FI94");
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == 0);
        testIndexes = proteinTree.getProteinMapping("QTCESKT");
        Assert.assertTrue(testIndexes.size() == 1);
        indexes = testIndexes.get("Q9FI94");
        Assert.assertTrue(indexes.size() == 1);
        sequence = sequenceFactory.getProtein("Q9FI94").getSequence();
        index = sequence.indexOf("QTCESKT");
        Assert.assertTrue(indexes.get(0) == index);
        testIndexes = proteinTree.getProteinMapping("TKSECTQ");
        indexes = testIndexes.get("Q9FI94_REVERSED");
        Assert.assertTrue(testIndexes.size() == 1);
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == 0);
        testIndexes = proteinTree.getProteinMapping("TKSECT");
        indexes = testIndexes.get("Q9FI94_REVERSED");
        Assert.assertTrue(testIndexes.size() == 1);
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == 0);
        testIndexes = proteinTree.getProteinMapping("DEM");
        Assert.assertTrue(testIndexes.size() == 2);
        indexes = testIndexes.get("Q9FI94");
        Assert.assertTrue(indexes.size() == 1);
        index = sequence.indexOf("DEM");
        Assert.assertTrue(indexes.get(0) == index);
        indexes = testIndexes.get("Q9FI94_REVERSED");
        Assert.assertTrue(indexes.size() == 1);
        sequence = sequenceFactory.getProtein("Q9FI94_REVERSED").getSequence();
        index = sequence.indexOf("DEM");
        Assert.assertTrue(indexes.get(0) == index);
        
        sequence = sequenceFactory.getProtein("Q9FHX5").getSequence();
        testIndexes = proteinTree.getProteinMapping("VANB");
        Assert.assertTrue(testIndexes.size() == 1);
        indexes = testIndexes.get("Q9FHX5");
        Assert.assertTrue(indexes.size() == 1);
        index = sequence.indexOf("VANB");
        Assert.assertTrue(testIndexes.get("Q9FHX5").get(0) == index);
        testIndexes = proteinTree.getProteinMapping("VANN");
        Assert.assertTrue(testIndexes.isEmpty());
        HashMap<String, HashMap<String, ArrayList<Integer>>> extendedIndexes = proteinTree.getProteinMapping("VANN", ProteinMatch.MatchingType.aminoAcid, Double.NaN);
        Assert.assertTrue(extendedIndexes.size() == 1);
        Assert.assertTrue(extendedIndexes.get("VANB").size() == 1);
        indexes = extendedIndexes.get("VANB").get("Q9FHX5");
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == index);
        extendedIndexes = proteinTree.getProteinMapping("VAND", ProteinMatch.MatchingType.aminoAcid, Double.NaN);
        Assert.assertTrue(extendedIndexes.get("VANB").size() == 1);
        indexes = extendedIndexes.get("VANB").get("Q9FHX5");
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == index);
        Assert.assertTrue(extendedIndexes.get("VAND").size() == 1);
        indexes = extendedIndexes.get("VAND").get("Q9FHX5");
        Assert.assertTrue(indexes.size() == 1);
        index = sequence.indexOf("VAND");
        Assert.assertTrue(indexes.get(0) == index);
        extendedIndexes = proteinTree.getProteinMapping("VANB", ProteinMatch.MatchingType.aminoAcid, Double.NaN);
        Assert.assertTrue(extendedIndexes.size() == 2);
        
        extendedIndexes = proteinTree.getProteinMapping("IVAI", ProteinMatch.MatchingType.indistiguishibleAminoAcids, 0.5);
        Assert.assertTrue(extendedIndexes.size() == 4);
        Assert.assertTrue(extendedIndexes.get("IVAI").size() == 1);
        indexes = extendedIndexes.get("IVAI").get("Q9FHX5");
        Assert.assertTrue(indexes.size() == 1);
        index = sequence.indexOf("IVAI");
        Assert.assertTrue(indexes.get(0) == index);
        Assert.assertTrue(extendedIndexes.get("LVAI").size() == 1);
        indexes = extendedIndexes.get("LVAI").get("Q9FHX5");
        Assert.assertTrue(indexes.size() == 1);
        index = sequence.indexOf("LVAI");
        Assert.assertTrue(indexes.get(0) == index);
        Assert.assertTrue(extendedIndexes.get("IVAL").size() == 1);
        indexes = extendedIndexes.get("IVAL").get("Q9FHX5");
        Assert.assertTrue(indexes.size() == 1);
        index = sequence.indexOf("IVAL");
        Assert.assertTrue(indexes.get(0) == index);
        Assert.assertTrue(extendedIndexes.get("LVAL").size() == 1);
        indexes = extendedIndexes.get("LVAL").get("Q9FHX5");
        Assert.assertTrue(indexes.size() == 1);
        index = sequence.indexOf("LVAL");
        Assert.assertTrue(indexes.get(0) == index);
        
    }
}
