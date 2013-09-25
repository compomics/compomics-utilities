package com.compomics.util.test.experiment;

import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

/**
 * Test for the protein tree.
 *
 * @author Marc Vaudel
 */
public class ProteinTreeTest extends TestCase {

    @Before
    @After
    public static void deleteTestingResults() {
        File resultFolder = new File(System.getProperty("user.home") + "/.compomics/proteins");
        File[] filesToDelete = resultFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("proteinTreeTestSequences_");
            }
        });
        for (File aFile : filesToDelete) {
            if (aFile.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(aFile);
                } catch (IOException ex) {
                    Logger.getLogger(ProteinTreeTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                FileUtils.deleteQuietly(aFile);
            }
        }
    }

    /*  public void testLoopedProteinTree() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {
     //THIS TEST IF TO SEE IF THE MULTITHREADING CAUSES LOCKS !!!!!! (Building the code once is not enough)
     int max = 20;
     for (int i = 0; i <= max; i++) {
     System.out.println("\n--------------------------\n");
     System.out.println("CLEARING PREVIOUS RESULTS\n");
     System.out.println("--------------------------\n");
     deleteTestingResults();
     System.out.println("Round " + i + " out of " + max);
     createProteinTree(i);
     }
     }
     */
    
    public void testProteinTree() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {
        createProteinTree(1);
    }

    public void createProteinTree(int i) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences");
        File sequenceFileToRun = new File("src/test/resources/experiment/proteinTreeTestSequences_" + i);
        sequenceFileToRun.deleteOnExit();
        FileUtils.copyFile(sequences, sequenceFileToRun);
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequenceFileToRun);

        ProteinTree proteinTree = new ProteinTree(1);
        proteinTree.initiateTree(3, 500, 15, null, true);

        ConcurrentHashMap<String, ArrayList<Integer>> testIndexes = proteinTree.getProteinMapping("SSS");
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
