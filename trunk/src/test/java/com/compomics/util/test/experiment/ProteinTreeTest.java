package com.compomics.util.test.experiment;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.identification.tags.matchers.TagMatcher;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Test for the protein tree.
 *
 * @author Marc Vaudel
 * @author Kenneth Verheggen
 */
public class ProteinTreeTest extends TestCase {

    /**
     * Tests the import and the mapping of a few peptide sequences.
     *
     * @throws FileNotFoundException thrown whenever a file is not found
     * @throws IOException thrown whenever an error occurs while reading or
     * writing a file
     * @throws ClassNotFoundException thrown whenever an error occurs while
     * deserializing an object
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the tree
     * @throws SQLException if an SQLException thrown whenever a problem
     * occurred while interacting with the tree database
     */
    public void testPeptideToProteinMapping() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);

        ProteinTree proteinTree = new ProteinTree(1000, 1000);
        proteinTree.initiateTree(3, 50, 50, waitingHandlerCLIImpl, exceptionHandler, true, false, 1);

        HashMap<String, HashMap<String, ArrayList<Integer>>> testIndexes = proteinTree.getProteinMapping("SSS", SequenceMatchingPreferences.defaultStringMatching);
        Assert.assertTrue(testIndexes.size() == 1);
        HashMap<String, ArrayList<Integer>> proteinMapping = testIndexes.get("SSS");
        Assert.assertTrue(proteinMapping.size() == 2);
        ArrayList<Integer> indexes = proteinMapping.get("Q9FHX5");
        String sequence = sequenceFactory.getProtein("Q9FHX5").getSequence();
        Assert.assertTrue(indexes.size() == 3);
        Collections.sort(indexes);
        int index = sequence.indexOf("SSS");
        Assert.assertTrue(indexes.get(0) == index);
        index += sequence.substring(index + 1).indexOf("SSS") + 1;
        Assert.assertTrue(indexes.get(1) == index);
        index = sequence.lastIndexOf("SSS");
        Assert.assertTrue(indexes.get(2) == index);
        indexes = proteinMapping.get("Q9FHX5_REVERSED");
        sequence = sequenceFactory.getProtein("Q9FHX5_REVERSED").getSequence();
        Assert.assertTrue(indexes.size() == 3);
        Collections.sort(indexes);
        index = sequence.indexOf("SSS");
        Assert.assertTrue(indexes.get(0) == index);
        index += sequence.substring(index + 1).indexOf("SSS") + 1;
        Assert.assertTrue(indexes.get(1) == index);
        index = sequence.lastIndexOf("SSS");
        Assert.assertTrue(indexes.get(2) == index);

        proteinTree.deleteDb();
    }

    /**
     * Tests the mapping of de novo sequence tags to the database.
     *
     * @throws FileNotFoundException thrown whenever a file is not found
     * @throws IOException thrown whenever an error occurs while reading or
     * writing a file
     * @throws ClassNotFoundException thrown whenever an error occurs while
     * deserializing an object
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the tree
     * @throws SQLException if an SQLException thrown whenever a problem
     * occurred while interacting with the tree database
     * @throws org.xmlpull.v1.XmlPullParserException thrown whenever a problem
     * occurred while interacting with the tree database
     */
    public void testTagToProteinMapping() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {

        PTMFactory ptmFactory = PTMFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = PTMFactory.getInstance();

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences_1");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);

        ProteinTree proteinTree = new ProteinTree(1000, 1000);
        proteinTree.initiateTree(3, 50, 50, waitingHandlerCLIImpl, exceptionHandler, true, false, 1);

        // TESTMRITESTCKTESTK
        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern("LTEST");
        double nTermGap = AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        double cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        Tag tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);

        ArrayList<String> fixedModifications = new ArrayList<String>();
        fixedModifications.add("Carbamidomethylation of C");
        for (String ptmName : fixedModifications) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            if (ptm.getName().equals(PTMFactory.unknownPTM.getName())) {
                throw new IllegalArgumentException("PTM " + ptmName + " not in the PTM factory.");
            }
        }

        ArrayList<String> variableModifications = new ArrayList<String>();
        variableModifications.add("Oxidation of M");
        variableModifications.add("Pyrolidone from carbamidomethylated C");
        for (String ptmName : variableModifications) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            if (ptm.getName().equals(PTMFactory.unknownPTM.getName())) {
                throw new IllegalArgumentException("PTM " + ptmName + " not in the PTM factory.");
            }
        }

        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setMs2MzTolerance(0.5);

        TagMatcher tagMatcher = new TagMatcher(fixedModifications, variableModifications, sequenceMatchingPreferences);
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.5);
        Assert.assertTrue(proteinMapping.isEmpty());

        cTermGap += 57.02;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        proteinMapping = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.5);
        Assert.assertTrue(proteinMapping.size() == 1);
        ArrayList<Peptide> peptides = new ArrayList<Peptide>(proteinMapping.keySet());
        ArrayList<Integer> indexes = proteinMapping.get(peptides.get(0)).get("test");
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == 3);

        nTermGap += 15.99;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        proteinMapping = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.5);
        Assert.assertTrue(proteinMapping.size() == 1);
        peptides = new ArrayList<Peptide>(proteinMapping.keySet());
        indexes = proteinMapping.get(peptides.get(0)).get("test");
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == 3);

        aminoAcidPattern = new AminoAcidPattern("TEST");
        nTermGap = AminoAcid.K.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + 57.02 - 17.0265;
        cTermGap = AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);

        proteinMapping = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.5);
        Assert.assertTrue(proteinMapping.size() == 1);
        peptides = new ArrayList<Peptide>(proteinMapping.keySet());
        indexes = proteinMapping.get(peptides.get(0)).get("test");
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == 11);

        tagMatcher.clearCache();

        proteinTree.deleteDb();
    }
}
