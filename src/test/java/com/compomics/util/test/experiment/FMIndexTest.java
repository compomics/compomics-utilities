package com.compomics.util.test.experiment;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.PeptideVariantsPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.protein.Protein;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
    
    
    boolean testSequenceMatching = false;
    boolean testTagMatching = false;
    boolean testVariantMatching = false;
    boolean testVariantPTMMatching = false;

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
        if (!testSequenceMatching) return;
        
        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);
        PeptideVariantsPreferences peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();

        FMIndex fmIndex = new FMIndex(null, false, null, peptideVariantsPreferences);

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
        if (!testTagMatching) return;
        
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);

        PTMFactory ptmFactory = PTMFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = PTMFactory.getInstance();
        
        PeptideVariantsPreferences peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();

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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 9);
        
        

        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TESTC");
        nTermGap = -18.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 9);
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 9);
        
        
        
        
        
        /////////////////////////////////////////////////////////////////////////
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 1);
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(proteinMapping.isEmpty());
        
        
        
        
        // TESTMRITESTCKTESTK with one variable modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 1);
        
        
        
        
        // TESTMRITESTCKTESTK with one variable modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 0);
        
        
        
        
        // TESTMRITESTCKTESTK with two fixed modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 2);
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 2);
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 1);
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 10);
        
        
        
        
        // TESTMRITESTCKTESTK with two variable modifications at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 0);
        
        
        
        
        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TESTMRITE") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 0);
        
        
        
        
        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = 42.01 + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TESTMRITE") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 1);
        
        
        
        
        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = 42.01 + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TESTMRITE") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 1);
        
        
        
        
        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(proteinMapping.isEmpty());
        
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = 238.22 + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Palmitoylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("TESTMRITE") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 1);
        
        
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTE with one variable modifications at protein c-terminus
        aminoAcidPattern = new AminoAcidSequence("LTSE");
        nTermGap = AminoAcid.E.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the protein C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("ELTSESTE") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 1);
        Assert.assertTrue(outputProtein.getModificationMatches().get(0).getModificationSite() == 8);
        
        
        // TESTMRITESTCKTESTKMELTSESTE with several modifictations
        aminoAcidPattern = new AminoAcidSequence("LTSE");
        nTermGap = 15.99 + AminoAcid.M.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        cTermGap = -0.98 + 203.07 + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the protein C-term"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Oxidation of M"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("HexNAc of T"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("MELTSESTE") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 3);
        
        
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTE with several modifictations
        aminoAcidPattern = new AminoAcidSequence("ELTS");
        nTermGap = 15.99 + AminoAcid.M.getMonoisotopicMass() + 42.01 + AminoAcid.K.getMonoisotopicMass();
        cTermGap = -0.98 + 203.07 + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + 2 * AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the protein C-term"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Oxidation of M"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("HexNAc of T"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of K"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        outputProtein = proteinMapping.keySet().iterator().next();
        Assert.assertTrue(outputProtein.getSequence().compareTo("KMELTSESTE") == 0);
        Assert.assertTrue(outputProtein.getModificationMatches().size() == 4);
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
    public void testTagToProteinMappingWithVariants() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        if (!testVariantMatching) return;
        
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        
        PeptideVariantsPreferences peptideVariantsPreferences = new PeptideVariantsPreferences();
        peptideVariantsPreferences.setnAaSubstitutions(1);

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
        boolean isPresent;
        Iterator<Peptide> it;
        
        
        // TESTMRITESTCKTESTKMELTSESTES with no variants
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITESTCKTE") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with insertion in sequence
        aminoAcidPattern = new AminoAcidSequence("TST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITSTCKTE") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with substitution in sequence
        aminoAcidPattern = new AminoAcidSequence("TGST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITGSTCKTE") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with deletion in sequence
        aminoAcidPattern = new AminoAcidSequence("TGEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITGESTCKTE") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with insertion in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMITESTCKT") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("SCMRITESTCKTE") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + 2 * AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STTMRITESTCKTE") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with insertion in right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("TMRITESTCTE") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITESTCCTE") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITESTACKT") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with insertion in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMITESTCKTES") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("SCMRITESTCKTEST") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + 2 * AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + 2 * AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STTMRITESTCKTESTK") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with insertion in right mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("TMRITESTCTEST") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITESTCCTEST") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITESTACKTES") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
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
    
    public void testTagToProteinMappingWithPTMsAndVariants() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        if (!testVariantPTMMatching) return;
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);

        PeptideVariantsPreferences peptideVariantsPreferences = new PeptideVariantsPreferences();
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.singleBaseSubstitution);
        peptideVariantsPreferences.setnAaSubstitutions(1);
        
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
        boolean isPresent = false;
        Iterator<Peptide> it;
        int numPTMs = 0;
        
        

        // TESTMRITESTCKTESTK with one fixed modification and one variant
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + 0 * AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        it = proteinMapping.keySet().iterator();
        isPresent = false;
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("TRITESTCK") == 0) isPresent = true;
        }
        Assert.assertTrue(isPresent);
        
        
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 15.99 - 18.01 + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = 57.02 - 18.01 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + 0 * AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        numPTMs = 0;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITESTCMTES") == 0){
                isPresent = true;
                numPTMs = outputProtein.getModificationMatches().size();
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(numPTMs == 4);
        
        
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 15.99 - 18.01 + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = 57.02 - 2 * 18.01 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        numPTMs = 0;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITESTCMTEST") == 0){
                isPresent = true;
                numPTMs = outputProtein.getModificationMatches().size();
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(numPTMs == 5);
        
        
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        numPTMs = 0;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITESTCMTEST") == 0){
                isPresent = true;
                numPTMs = outputProtein.getModificationMatches().size();
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(numPTMs == 4);
        
        
        
        
        
        
        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass() + 15.99 + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + 15.99 + AminoAcid.M.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!proteinMapping.isEmpty());
        isPresent = false;
        numPTMs = 0;
        it = proteinMapping.keySet().iterator();
        while (it.hasNext()){
            outputProtein = it.next();
            if (outputProtein.getSequence().compareTo("STMRITESTCMTEST") == 0){
                isPresent = true;
                numPTMs = outputProtein.getModificationMatches().size();
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(numPTMs == 6);
    }
    
    
    public void testExperiment() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);
        
        PeptideVariantsPreferences peptideVariantsPreferences = new PeptideVariantsPreferences();
        peptideVariantsPreferences.setnAaSubstitutions(2);

        PTMFactory ptmFactory = PTMFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = PTMFactory.getInstance();
        
        PtmSettings ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File("/scratch/U507_human_TD_2015_07_22.fasta");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);
        
        FMIndex fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        HashMap<String, HashMap<String, ArrayList<Integer>>> testIndexesX = fmIndex.getProteinMapping("EDNEGVYNGSWGGR", sequenceMatchingPreferences);
    }
}