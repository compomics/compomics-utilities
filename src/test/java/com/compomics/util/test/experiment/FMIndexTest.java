/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dominik.kopczynski
 */
package com.compomics.util.test.experiment;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.identification.protein_inference.fm_index.SuffixArraySorter;
import com.compomics.util.experiment.identification.protein_inference.fm_index.WaveletTree;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
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
public class FMIndexTest extends TestCase {

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
        //File sequences = new File("/home/dominik.kopczynski/Data/ps/uniprot-human-reviewed-trypsin-april-2016_concatenated_target_decoy.fasta");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);

        FMIndex fmIndex = new FMIndex(null, false, null);
        
        /*
        HashMap<String, HashMap<String, ArrayList<Integer>>> testIndexesFirst = fmIndex.getProteinMapping("FNPDGTPVYSIGLKTSSTXS", SequenceMatchingPreferences.getDefaultSequenceMatching());
        
        Assert.assertTrue(testIndexesFirst.containsKey("FNPDGTPVYSLGIKTSSTHS"));
        Assert.assertTrue(testIndexesFirst.get("FNPDGTPVYSLGIKTSSTHS").containsKey("Q9FHX5"));
        Assert.assertTrue(testIndexesFirst.get("FNPDGTPVYSLGIKTSSTHS").get("Q9FHX5").get(0) == 335);
        */
        
        
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);
        HashMap<String, HashMap<String, ArrayList<Integer>>> testIndexesX = fmIndex.getProteinMapping("ECTQDRGKTAFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(testIndexesX.size() == 1);
        Assert.assertTrue(testIndexesX.get("ECTQDRXKTAFTEAVLLP").containsKey("TEST_ACCESSION"));
        Assert.assertTrue(testIndexesX.get("ECTQDRXKTAFTEAVLLP").get("TEST_ACCESSION").get(0) == 3);
        
        
        HashMap<String, HashMap<String, ArrayList<Integer>>> testIndexes = fmIndex.getProteinMapping("SSS", SequenceMatchingPreferences.defaultStringMatching);
        
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
        
        
        
        
        /*
        // VYSLGIKT
        int l = 0, r = fmIndex.indexStringLength - 1;
        int lf = 0, rf = fmIndex.indexStringLength - 1;
        int lt = 0, rt = fmIndex.indexStringLength - 1;
        String p = "SSGGS";
        String p_r = "GGSS";
        
        
        for (int i = 0; i < p_r.length(); ++i){
            int c = p_r.charAt(i);
            lt = fmIndex.lessTablePrimary[c] + fmIndex.occurrenceTablePrimary.getRank(lt - 1, c);
            rt = fmIndex.lessTablePrimary[c] + fmIndex.occurrenceTablePrimary.getRank(rt, c) - 1;
        }
        
        for (int i = 0; i < p.length(); ++i){
            //l = fmIndex.lessTablePrimary[c] + fmIndex.occurrenceTablePrimary.getRank(l - 1, c);
            //r = fmIndex.lessTablePrimary[c] + fmIndex.occurrenceTablePrimary.getRank(r, c) - 1;
            
            int c = p.charAt(i);
            
            ArrayList<Integer[]> setCharacter = new ArrayList<Integer[]>(26);
            fmIndex.occurrenceTableReversed.rangeQuery(lf - 1, rf, setCharacter);
            int ct = 0;
            int cum = 0;
            while (setCharacter.get(ct)[0] < c){
                cum += setCharacter.get(ct)[2] - setCharacter.get(ct)[1];
                ++ct;
            }
            l += cum;
            r = l + ((setCharacter.size() > 0 && setCharacter.get(ct)[0] == c) ? setCharacter.get(ct)[2] - setCharacter.get(ct)[1] : 0) - 1;
            lf = fmIndex.lessTableReversed[c] + fmIndex.occurrenceTableReversed.getRank(lf - 1, c);
            rf = fmIndex.lessTableReversed[c] + fmIndex.occurrenceTableReversed.getRank(rf, c) - 1;
        }
        
        System.out.println("out: " + lt + " " + rt);
        
        System.out.println("got: " + l + " " + r);
        */
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
        //File sequences = new File("/home/dominik.kopczynski/Data/ps/uniprot-human-reviewed-trypsin-april-2016_concatenated_target_decoy.fasta");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);

        //ProteinTree proteinTree = new ProteinTree(1000, 1000);
        //proteinTree.initiateTree(3, 50, 50, waitingHandlerCLIImpl, exceptionHandler, true, false, 1);
        
        FMIndex proteinTree = new FMIndex(null, false, null);

        //Duration: 54693 <1029.5805646837> DIDS <904.0084353163> 
        //Duration: 72384 <531.243> SDPI <2126.88468196889> 
        
        //Tag tag = new Tag(531.243, new AminoAcidSequence("SDPI"), 2126.88468196889);
        
        /*
        // TESTMRITESTCKTESTK
        AminoAcidSequence aminoAcidPattern = new AminoAcidSequence("TEST");
        double nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
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
        TagMatcher tagMatcher = new TagMatcher(fixedModifications, variableModifications, sequenceMatchingPreferences);

        
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        Assert.assertTrue(proteinMapping.keySet().iterator().next().getSequence().compareTo("TMRITESTCK") == 0);
        
        
        // TESTMRITESTCKTESTK // testing cache
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping2 = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.02);
        //Assert.assertTrue(!proteinMapping2.isEmpty());
        //Assert.assertTrue(proteinMapping2.keySet().iterator().next().getSequence().compareTo("TMRITESTCK") == 0);
        
        
        // TESTMRITESTCKTESTK // testing cache
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping3 = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.02);
        //Assert.assertTrue(!proteinMapping3.isEmpty());
        //Assert.assertTrue(proteinMapping3.keySet().iterator().next().getSequence().compareTo("TMRITESTC") == 0);
        
        
        
        // TESTMRITESTCKTESTK
        AminoAcidSequence aminoAcidPattern3 = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern3, cTermGap);
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping4 = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping4.isEmpty());
        Assert.assertTrue(proteinMapping4.keySet().iterator().next().getSequence().compareTo("RITESTCKTE") == 0);
        
        
        
        nTermGap = AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern3, cTermGap);
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping5 = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping5.isEmpty());
        Assert.assertTrue(proteinMapping5.keySet().iterator().next().getSequence().compareTo("ITESTCKTE") == 0);
        
        
        nTermGap = AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern3, cTermGap);
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping6 = proteinTree.getProteinMapping(tag, tagMatcher, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping6.isEmpty());
        Assert.assertTrue(proteinMapping6.keySet().iterator().next().getSequence().compareTo("RITESTCKT") == 0);
        */
    }
    
}
