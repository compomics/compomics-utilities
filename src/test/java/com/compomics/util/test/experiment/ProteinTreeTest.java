package com.compomics.util.test.experiment;

import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

/**
 * Test for the protein tree.
 *
 * @author Marc Vaudel
 * @author Kenneth Verheggen
 */
public class ProteinTreeTest extends TestCase {
    
/**
 * Tests the import and the mapping of few sequences.
 * @throws FileNotFoundException
 * @throws IOException
 * @throws ClassNotFoundException
 * @throws SQLException
 * @throws InterruptedException 
 */
    public void testProteinTree() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {

        System.out.println("Regular test");
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences);

        ProteinTree proteinTree = new ProteinTree(1000);
        proteinTree.initiateTree(3, 500, 15, null, true, false);
        
        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        utilitiesUserPreferences.clearProteinTreeImportTimes();
        utilitiesUserPreferences.saveUserPreferences(utilitiesUserPreferences);

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

    }

}