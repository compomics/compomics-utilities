package com.compomics.util.test.experiment;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.protein.Protein;
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
 * Test for the FM Index.
 *
 * @author Marc Vaudel
 * @author Kenneth Verheggen
 * @author dominik.kopczynski
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
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);

        FMIndex fmIndex = new FMIndex(null, false, null);

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
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);

        PTMFactory ptmFactory = PTMFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = PTMFactory.getInstance();

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences_1");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);
        
        
        AminoAcidSequence aminoAcidPattern;
        double nTermGap;
        double cTermGap;
        Tag tag;
        PtmSettings ptmSettings;
        FMIndex fmIndex;
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping;
        Peptide outputProtein;
        
        

        // TESTMRITESTCKTESTK with no modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        
        

        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 9);
        
        

        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TESTC");
        nTermGap = -18.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 1);
        
        
        
        
        // TESTMRITESTCKTESTK with two fixed modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = -18.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 2);
        
        
        
        
        // TESTMRITESTCKTESTK with two fixed modifications that match nowthere 
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = -18.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(proteinMapping.isEmpty());
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 15.99 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 2);
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 9);
    }

}
