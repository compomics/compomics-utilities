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
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
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
        if (!testSequenceMatching) {
            return;
        }

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
        ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping("ECTQDRGKTAFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        PeptideProteinMapping peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("ECTQDRXKTAFTEAVLLP"));
        Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

        peptideProteinMappings = fmIndex.getProteinMapping("SSS", SequenceMatchingPreferences.defaultStringMatching);
        HashMap<String, HashMap<String, ArrayList<Integer>>> testIndexes = PeptideProteinMapping.getPeptideProteinMap(peptideProteinMappings);
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
        if (!testTagMatching) {
            return;
        }

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
        ArrayList<PeptideProteinMapping> peptideProteinMappings;

        // TESTMRITESTCKTESTK with no modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        PeptideProteinMapping peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("TMRITESTCK") == 0);

        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        ArrayList<ModificationMatch> modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        ModificationMatch modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 9);

        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TESTC");
        nTermGap = -18.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);

        // TESTMRITESTCKTESTK with two fixed modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = -18.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 2);

        // TESTMRITESTCKTESTK with two fixed modifications that match nowthere 
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = -18.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 15.99 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 2);

        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 9);

        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 9);

        /////////////////////////////////////////////////////////////////////////
        // TESTMRITESTCKTESTK with one fixed modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);

        // TESTMRITESTCKTESTK with one fixed modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTK with one variable modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);

        // TESTMRITESTCKTESTK with one variable modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.isEmpty());

        // TESTMRITESTCKTESTK with two fixed modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 2);

        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 2);

        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);

        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 10);

        // TESTMRITESTCKTESTK with two variable modifications at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.isEmpty());

        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TESTMRITE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.isEmpty());

        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = 42.01 + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TESTMRITE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);

        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = 42.01 + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TESTMRITE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);

        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTK with one fixed modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = 238.22 + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Palmitoylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TESTMRITE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);

        // TESTMRITESTCKTESTKMELTSESTE with one variable modifications at protein c-terminus
        aminoAcidPattern = new AminoAcidSequence("LTSE");
        nTermGap = AminoAcid.E.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the protein C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("ELTSESTE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 8);

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

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("MELTSESTE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 3);

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

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("KMELTSESTE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 4);
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
        boolean isPresent;

        // TESTMRITESTCKTESTKMELTSESTES with no variants
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCKTE")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in sequence
        aminoAcidPattern = new AminoAcidSequence("TST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITSTCKTE")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in sequence
        aminoAcidPattern = new AminoAcidSequence("TGST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITGSTCKTE")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in sequence
        aminoAcidPattern = new AminoAcidSequence("TGEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITGSTCKTE")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMITESTCKT")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMITESTCKT")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + 2 * AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STTMRITESTCKTE")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("TMRITESTCTE")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCCTE")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTACKT")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMITESTCKTES")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("SCMRITESTCKTEST")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + 2 * AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + 2 * AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STTMRITESTCKTESTK")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in right mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("TMRITESTCTEST")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCCTEST")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTACKTES")) {
                isPresent = true;
                break;
            }
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
        boolean isPresent;
        int numPTMs = 0;

        // TESTMRITESTCKTESTK with one fixed modification and one variant
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + 0 * AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("TRITESTCK")) {
                isPresent = true;
                break;
            }
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
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTES")) {
                isPresent = true;
                break;
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
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTEST")) {
                isPresent = true;
                break;
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
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTEST")) {
                isPresent = true;
                break;
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
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        numPTMs = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTEST")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(numPTMs == 6);
    }

    public void testExperiment() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        if (true) return;
        AminoAcidSequence aminoAcidPattern;
        double nTermGap;
        double cTermGap;
        Tag tag;
        FMIndex fmIndex;
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> proteinMapping;
        Peptide outputProtein;
        boolean isPresent = false;
        Iterator<Peptide> it;
        int numPTMs = 0;

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
        File sequences = new File("../../Data/ps/uniprot-human-reviewed-trypsin-april-2016_concatenated_target_decoy.fasta");
        //File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences_1");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);

        /*
         // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
         aminoAcidPattern = new AminoAcidSequence("TEST");
         nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
         cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
         tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
         fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
         proteinMapping = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
         Assert.assertTrue(!proteinMapping.isEmpty());
         isPresent = false;
         it = proteinMapping.keySet().iterator();
         while (it.hasNext()){
         outputProtein = it.next();
         if (outputProtein.getSequence().compareTo("STMRITESTCMTEST") == 0){
         isPresent = true;
         numPTMs = outputProtein.getModificationMatches().size();
         }
         }
         Assert.assertTrue(isPresent);
         */
        aminoAcidPattern = new AminoAcidSequence("SFAS");
        nTermGap = 402.186;
        cTermGap = 1651.92;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences);
        long start = System.nanoTime();
        fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences, 0.02);
        System.out.println("time: " + (System.nanoTime() - start));

    }
}
