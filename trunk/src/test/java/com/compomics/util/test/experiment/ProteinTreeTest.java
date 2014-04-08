package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
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
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws InterruptedException
     */
    public void testPeptideToProteinMapping() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);

        ProteinTree proteinTree = new ProteinTree(1000, 1000);
        proteinTree.initiateTree(3, 50, 50, null, true, false);

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
        
        proteinTree.close();
        proteinTree.deleteDb();
    }

    public void testTagToProteinMapping() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        
        PTMFactory ptmFactory = PTMFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = PTMFactory.getInstance();
        File ptmFile = new File("src/test/resources/experiment/mods.xml");
        ptmFactory.importModifications(ptmFile, false);

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences_1");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);

        ProteinTree proteinTree = new ProteinTree(1000, 1000);
        proteinTree.initiateTree(3, 50, 50, null, true, false);

        // TESTMRITESTCKTESTK
        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern("LTEST");
        double nTermGap = AminoAcid.R.monoisotopicMass + AminoAcid.M.monoisotopicMass + AminoAcid.T.monoisotopicMass;
        double cTermGap = AminoAcid.C.monoisotopicMass + AminoAcid.K.monoisotopicMass;
        Tag tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);

        ArrayList<String> fixedModifications = new ArrayList<String>();
        fixedModifications.add("carbamidomethyl c");
        for (String ptmName : fixedModifications) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            if (ptm.getName().equals(PTMFactory.unknownPTM.getName())) {
                throw new IllegalArgumentException("PTM " + ptmName + " not in the PTM factory.");
            }
        }
        
        ArrayList<String> variableModifications = new ArrayList<String>();
        variableModifications.add("oxidation of m");
        variableModifications.add("pyro-cmc");
        for (String ptmName : variableModifications) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            if (ptm.getName().equals(PTMFactory.unknownPTM.getName())) {
                throw new IllegalArgumentException("PTM " + ptmName + " not in the PTM factory.");
            }
        }
        
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping = proteinTree.getProteinMapping(tag, AminoAcidPattern.MatchingType.indistiguishibleAminoAcids, 0.5, fixedModifications, variableModifications, true, false);
        Assert.assertTrue(proteinMapping.isEmpty());

        cTermGap += 57.02;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        proteinMapping = proteinTree.getProteinMapping(tag, AminoAcidPattern.MatchingType.indistiguishibleAminoAcids, 0.5, fixedModifications, variableModifications, true, false);
        Assert.assertTrue(proteinMapping.size() == 1);
        ArrayList<Peptide> peptides = new ArrayList<Peptide>(proteinMapping.keySet());
        ArrayList<Integer> indexes = proteinMapping.get(peptides.get(0)).get("test");
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == 3);

        nTermGap += 15.99;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        proteinMapping = proteinTree.getProteinMapping(tag, AminoAcidPattern.MatchingType.indistiguishibleAminoAcids, 0.5, fixedModifications, variableModifications, true, false);
        Assert.assertTrue(proteinMapping.size() == 1);
        peptides = new ArrayList<Peptide>(proteinMapping.keySet());
        indexes = proteinMapping.get(peptides.get(0)).get("test");
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == 3);

        aminoAcidPattern = new AminoAcidPattern("TEST");
        nTermGap = AminoAcid.K.monoisotopicMass + AminoAcid.C.monoisotopicMass + 57.02 - 17.0265;
        cTermGap = AminoAcid.K.monoisotopicMass;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);

        proteinMapping = proteinTree.getProteinMapping(tag, AminoAcidPattern.MatchingType.indistiguishibleAminoAcids, 0.5, fixedModifications, variableModifications, true, false);
        Assert.assertTrue(proteinMapping.size() == 1);
        peptides = new ArrayList<Peptide>(proteinMapping.keySet());
        indexes = proteinMapping.get(peptides.get(0)).get("test");
        Assert.assertTrue(indexes.size() == 1);
        Assert.assertTrue(indexes.get(0) == 11);
        
        proteinTree.close();
        proteinTree.deleteDb();
    }
}
