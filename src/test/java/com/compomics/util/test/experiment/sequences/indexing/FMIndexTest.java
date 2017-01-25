package com.compomics.util.test.experiment.sequences.indexing;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import com.compomics.util.experiment.biology.variants.amino_acids.Deletion;
import com.compomics.util.experiment.biology.variants.amino_acids.Insertion;
import com.compomics.util.experiment.biology.variants.amino_acids.Substitution;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.VariantMatch;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.PeptideVariantsPreferences;
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
 * Test for the FM Index.
 *
 * @author Marc Vaudel
 * @author Kenneth Verheggen
 * @author Dominik Kopczynski
 */
public class FMIndexTest extends TestCase {

    boolean testSequenceMatching = true;
    boolean testSequenceMatchingWithVariants = true;
    boolean testSequenceMatchingWithVariantsSpecific = true;
    boolean testTagMatching = true;
    boolean testVariantMatchingGeneric = true;
    boolean testVariantPTMMatching = true;
    boolean testVariantMatchingSpecific = true;

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
        HashMap<String, HashMap<String, ArrayList<Integer>>> testIndexes = PeptideProteinMapping.getPeptideProteinIndexesMap(peptideProteinMappings);
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
    public void testPeptideToProteinMappingWithVariants() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {
        if (!testSequenceMatchingWithVariants) {
            return;
        }

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);
        PeptideVariantsPreferences peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();
        peptideVariantsPreferences.setnVariants(1);
        peptideVariantsPreferences.setUseSpecificCount(false);

        FMIndex fmIndex;
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        int correctVariants = 0;
        boolean isPresent;

        // ECTQDRGKTAFTEAVLLP
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        fmIndex = new FMIndex(null, false, null, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTQDRGKTAFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQDRXKTAFTEAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    ++correctVariants;
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 0);

        peptideProteinMappings = fmIndex.getProteinMapping("ECTQDRGKTAFTEVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQDRXKTAFTEVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 14 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'A') {
                        ++correctVariants;
                    }
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        peptideProteinMappings = fmIndex.getProteinMapping("ECTQDRGKTMAFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQDRXKTMAFTEAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 10 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'M') {
                        ++correctVariants;
                    }
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        peptideProteinMappings = fmIndex.getProteinMapping("ECTQDKGKTAFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQDKXKTAFTEAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 6 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'R' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'K') {
                        ++correctVariants;
                    }
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        peptideVariantsPreferences.setnVariants(2);
        fmIndex = new FMIndex(null, false, null, peptideVariantsPreferences);

        peptideProteinMappings = fmIndex.getProteinMapping("ECTQDKGKTAFTEALLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQDKXKTAFTEALLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 6 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'R' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'K') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 15 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'V') {
                        ++correctVariants;
                    }
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 2);

        peptideProteinMappings = fmIndex.getProteinMapping("ECTDRGKTAFTEAVLTLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTDRXKTAFTEAVLTLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 16 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 4 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'Q') {
                        ++correctVariants;
                    }
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 2);

    }

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
    public void testPeptideToProteinMappingWithVariantsSpecific() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {
        if (!testSequenceMatchingWithVariantsSpecific) {
            return;
        }

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);
        PeptideVariantsPreferences peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();
        peptideVariantsPreferences.setUseSpecificCount(true);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.noSubstitution);
        peptideVariantsPreferences.setnAaSubstitutions(1);

        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        FMIndex fmIndex;
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        int correctVariants = 0;
        boolean isPresent;

        // ECTQDRGKTAFTEAVLLP no variant
        fmIndex = new FMIndex(null, false, null, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTQDRGKTAFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQDRXKTAFTEAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    ++correctVariants;
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 0);

        // ECTQDRGKTAFTEAVLLP two substitutions
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.allSubstitutions);
        peptideVariantsPreferences.setnAaSubstitutions(2);
        fmIndex = new FMIndex(null, false, null, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping("ECPQDRGKTRFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECPQDRXKTRFTEAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 3 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'T' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'P') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 10 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'A' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'R') {
                        ++correctVariants;
                    }
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 2);

        // ECTQDRGKTAFTEAVLLP one insertion, one deletion
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaSubstitutions(1);
        fmIndex = new FMIndex(null, false, null, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTDRGKTAFTEAVLTLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTDRXKTAFTEAVLTLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {

                    if (v.getSite() == 4 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'Q') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 16 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 2);

        // ECTQDRGKTAFTEAVLLP two insertions, one deletion
        peptideVariantsPreferences.setnAaInsertions(2);
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        fmIndex = new FMIndex(null, false, null, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTQTTDRGKTAFTAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQTTDRXKTAFTAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 5 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 6 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 15 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'E') {
                        ++correctVariants;
                    }
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 3);

        // ECTQDRGKTAFTEAVLLP one insertion, one deletion, one substitution
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaSubstitutions(1);
        fmIndex = new FMIndex(null, false, null, peptideVariantsPreferences);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTDRGKPAFTEAKVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTDRXKPAFTEAKVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);

                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 4 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'Q') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 8 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'T' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'P') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 14 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'K') {
                        ++correctVariants;
                    }
                }
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 3);

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
        sequenceMatchingPreferences.setLimitX(0.25);

        PTMFactory ptmFactory = PTMFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = PTMFactory.getInstance();

        PeptideVariantsPreferences peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences_1");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);
        
        PeptideProteinMapping peptideProteinMapping;
        AminoAcidSequence aminoAcidPattern;
        double nTermGap;
        double cTermGap;
        Tag tag;
        PtmSettings ptmSettings;
        FMIndex fmIndex;
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        int numModifications = 0;
        int numMatches = 0;
        ArrayList<ModificationMatch> modificationMatches;
        ModificationMatch modificationMatch;
        
        
        
        ////////////////////////////////////////////////////////////////////////
        // normal tags
        ////////////////////////////////////////////////////////////////////////
        
        
        // some case during testing that should provide no results
        aminoAcidPattern = new AminoAcidSequence("RRRP");
        nTermGap = 143.05824;
        cTermGap = 271.11682;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Pyrolidone from Q"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.2);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
        
        
        // TESTMRITESTCKTESTK with no modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("TMRITESTCK") == 0);
        

        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 9);

        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TESTC");
        nTermGap = -18.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 2);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);

        // TESTMRITESTCKTESTK with two fixed modifications that match nowthere 
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = -18.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 15.99 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 2);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 9);

        
        ////////////////////////////////////////////////////////////////////////
        // tags with modifications at the termini
        ////////////////////////////////////////////////////////////////////////
        
        // TESTMRITESTCKTESTK with one fixed modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTK with one variable modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 2);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);

        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 2);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 1);
        Assert.assertTrue(modificationMatches.get(1).getModificationSite() == 10);

        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 42.01 + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTK with one fixed modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = 238.22 + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Palmitoylation of protein N-term"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("MELTSESTE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 3);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getModificationSite() == 1) ++numModifications;
            if (mm.getModificationSite() == 8) ++numModifications;
            if (mm.getModificationSite() == 9) ++numModifications;
        }
        Assert.assertTrue(numModifications == 3);

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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("KMELTSESTE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getModificationSite() == 1) ++numModifications;
            if (mm.getModificationSite() == 2) ++numModifications;
            if (mm.getModificationSite() == 9) ++numModifications;
            if (mm.getModificationSite() == 10) ++numModifications;
        }
        Assert.assertTrue(numModifications == 4);
        
        
        
        ////////////////////////////////////////////////////////////////////////
        // tags mapping to wildcards X in proteome
        ////////////////////////////////////////////////////////////////////////
        
        
        
        // Substitution of Xs
        // LG(M)PCVVPINMKILD => LGXPCVVPINMKILD
        aminoAcidPattern = new AminoAcidSequence("VVPI");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.G.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        nTermGap += AminoAcid.M.getMonoisotopicMass();
        cTermGap = AminoAcid.N.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LGMPCVVPINMKILD") == 0);
        
        
        // LG(M)PCVVPINMKILD => LGXPCVVPINMKILD
        aminoAcidPattern = new AminoAcidSequence("VVPI");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.G.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        nTermGap += 15.99 + AminoAcid.M.getMonoisotopicMass();
        cTermGap = AminoAcid.N.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LGMPCVVPINMKILD") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 3);
        
        
        // G(C)PCVVPINMKILD => GXPCVVPINMKILD
        aminoAcidPattern = new AminoAcidSequence("VVPI");
        nTermGap = AminoAcid.G.getMonoisotopicMass() + 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        nTermGap += 57.02 + AminoAcid.C.getMonoisotopicMass();
        cTermGap = AminoAcid.N.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("GCPCVVPINMKILD") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 2);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getModificationSite() == 2)  ++numModifications;
            if (mm.getModificationSite() == 4)  ++numModifications;
        }
        Assert.assertTrue(numModifications == 2);
        
        
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        
        
        
        // LATAWOIDN(T)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += 79.96 + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Phosphorylation of T"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNTKRRRP") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        modificationMatch = modificationMatches.get(0);
        Assert.assertTrue(modificationMatch.getModificationSite() == 10);
        
        
        // LATAWOIDN(K)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 42.01 + AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += 42.01 + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of K"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNKKRRRP") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 2);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getModificationSite() == 10) ++numModifications;
            if (mm.getModificationSite() == 11) ++numModifications;
        }
        Assert.assertTrue(numModifications == 2);
        
        
        
        // VKTCF(MY)TEAVLLPFAIT => VKTCFXXTEAVLLPFAIT
        aminoAcidPattern = new AminoAcidSequence("LLPF");
        nTermGap = AminoAcid.V.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.C.getMonoisotopicMass() + AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.E.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass();
        nTermGap += 15.99 + AminoAcid.M.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass();
        cTermGap = AminoAcid.A.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("VKTCFMVTEAVLLPFAIT") == 0) ++numMatches;
            if (pPM.getPeptideSequence().compareTo("VKTCFVMTEAVLLPFAIT") == 0) ++numMatches;
        }
        Assert.assertTrue(numMatches == 2);
        
        
        
        // VKTCF(MY)TEAVLLPFAIT => VKTCFXXTEAVLLPFAIT
        aminoAcidPattern = new AminoAcidSequence("LLPF");
        nTermGap = AminoAcid.V.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.C.getMonoisotopicMass() + AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.E.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass();
        nTermGap += AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        cTermGap = AminoAcid.A.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
        
        
        
        // HQVLYRITDRVKTCF(MW)TE => HQVLYRITDRVKTCFXXTE
        aminoAcidPattern = new AminoAcidSequence("YRIT");
        nTermGap = AminoAcid.H.getMonoisotopicMass() + AminoAcid.Q.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() +
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        cTermGap += AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("HQVLYRITDRVKTCFMWTE") == 0) ++numMatches;
            if (pPM.getPeptideSequence().compareTo("HQVLYRITDRVKTCFWMTE") == 0) ++numMatches;
        }
        Assert.assertTrue(numMatches == 2);
        
        
        
        
        // HQVLYRITDRVKTCF(MW)TE => HQVLYRITDRVKTCFXXTE
        aminoAcidPattern = new AminoAcidSequence("YRIT");
        nTermGap = AminoAcid.H.getMonoisotopicMass() + AminoAcid.Q.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() +
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        cTermGap += 15.99 + AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("HQVLYRITDRVKTCFMWTE") == 0){
                ++numMatches;
                modificationMatches = pPM.getModificationMatches();
                Assert.assertTrue(modificationMatches != null);
                Assert.assertTrue(modificationMatches.size() == 1);
                modificationMatch = modificationMatches.get(0);
                Assert.assertTrue(modificationMatch.getModificationSite() == 16);
            }
            if (pPM.getPeptideSequence().compareTo("HQVLYRITDRVKTCFWMTE") == 0) ++numMatches;
        }
        Assert.assertTrue(numMatches == 2);
        
        
        
        // HQVLYRITDRVKTCF(MW)TE => HQVLYRITDRVKTCFXXTE
        aminoAcidPattern = new AminoAcidSequence("YRIT");
        nTermGap = AminoAcid.H.getMonoisotopicMass() + AminoAcid.Q.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() +
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        cTermGap += AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
        
        
        
        
        // HQVLYRITDRVKTCF(MW)TE => HQVLYRITDRVKTCFXXTE
        aminoAcidPattern = new AminoAcidSequence("YRIT");
        nTermGap = AminoAcid.H.getMonoisotopicMass() + AminoAcid.Q.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() +
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        cTermGap += 15.99 + AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("HQVLYRITDRVKTCFMWTE") == 0){
                ++numMatches;
                modificationMatches = pPM.getModificationMatches();
                Assert.assertTrue(modificationMatches != null);
                Assert.assertTrue(modificationMatches.size() == 1);
                modificationMatch = modificationMatches.get(0);
                Assert.assertTrue(modificationMatch.getModificationSite() == 16);
            }
            if (pPM.getPeptideSequence().compareTo("HQVLYRITDRVKTCFWMTE") == 0) ++numMatches;
        }
        Assert.assertTrue(numMatches == 2);
        
        
        
        
        // DN(P)KRRRPDTIEDI(M)E(T)I => DNXKRRRPDTIEDIXEXI
        aminoAcidPattern = new AminoAcidSequence("RPDT");
        nTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.N.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + 
                AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass();
        nTermGap += AminoAcid.P.getMonoisotopicMass();
        cTermGap = AminoAcid.I.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() +
                AminoAcid.I.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        cTermGap += AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("DNPKRRRPDTIEDIMETI") == 0) ++numMatches;
            if (pPM.getPeptideSequence().compareTo("DNPKRRRPDTIEDITEMI") == 0) ++numMatches;
        }
        Assert.assertTrue(numMatches == 2);
        
        
        ////////////////////////////////////////////////////////////////////////
        // tags mapping to wildcards X in proteome modifications at the termini
        ////////////////////////////////////////////////////////////////////////
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term")); // -0.98
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addVariableModification(ptmFactory.getPTM("Amidation of the peptide C-term")); // -0.98
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the peptide C-term")); // -0.98
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the peptide C-term")); // -0.98
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = 4.01 + AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        //ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the peptide C-term")); // -0.98
        ptmSettings.addFixedModification(ptmFactory.getPTM("18O(2) of peptide C-term")); // +4.01
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        
        // G(C)PCVVPINMKILD => GXPCVVPINMKILD
        aminoAcidPattern = new AminoAcidSequence("VVPI");
        nTermGap = 27.99 + AminoAcid.G.getMonoisotopicMass() + 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        nTermGap += 57.02 + AminoAcid.C.getMonoisotopicMass();
        cTermGap = AminoAcid.N.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Formylation of peptide N-term")); // 27.99
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("GCPCVVPINMKILD") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 3);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getModificationSite() == 1)  ++numModifications;
            if (mm.getModificationSite() == 2)  ++numModifications;
            if (mm.getModificationSite() == 4)  ++numModifications;
        }
        Assert.assertTrue(numModifications == 3);
        
        // VKTCF(MY)TEAVLLPFAIT => VKTCFXXTEAVLLPFAIT
        aminoAcidPattern = new AminoAcidSequence("LLPF");
        nTermGap = 42.01 + AminoAcid.V.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.C.getMonoisotopicMass() + AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.E.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass();
        nTermGap += AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        cTermGap = AminoAcid.A.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("VKTCFMWTEAVLLPFAIT") == 0) ++numMatches;
            if (pPM.getPeptideSequence().compareTo("VKTCFWMTEAVLLPFAIT") == 0) ++numMatches;
        }
        Assert.assertTrue(numMatches == 2);
        
        // VKTCF(MY)TEAVLLPFAIT => VKTCFXXTEAVLLPFAIT
        aminoAcidPattern = new AminoAcidSequence("LLPF");
        nTermGap = 42.01 + AminoAcid.V.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.C.getMonoisotopicMass() + AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.E.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass();
        nTermGap += 15.99 + AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        cTermGap = AminoAcid.A.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addFixedModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("VKTCFMWTEAVLLPFAIT") == 0) ++numMatches;
            if (pPM.getPeptideSequence().compareTo("VKTCFWMTEAVLLPFAIT") == 0) ++numMatches;
        }
        Assert.assertTrue(numMatches == 2);
        
        
        
        // DRVKTCF(DD)TEAVLLPFAITADCY => DRVKTCFXXTEAVLLPFAITADCY
        aminoAcidPattern = new AminoAcidSequence("AVLLPFAI");
        nTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() +
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        nTermGap += AminoAcid.D.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass() + 
                AminoAcid.C.getMonoisotopicMass() + AminoAcid.Y.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the protein C-term")); // -0.98
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("DRVKTCFDDTEAVLLPFAITADCY") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.size() == 1);
        Assert.assertTrue(modificationMatches.get(0).getModificationSite() == 24);
        
        
        
        // DRVKTCF(DD)TEAVLLPFAITADC => DRVKTCFXXTEAVLLPFAITADC
        aminoAcidPattern = new AminoAcidSequence("AVLLPFAI");
        nTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() +
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        nTermGap += AminoAcid.D.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        cTermGap = -0.98 + AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass() + 
                AminoAcid.C.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the protein C-term")); // -0.98
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
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
    public void testTagToProteinMappingWithVariantsGeneric() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        if (!testVariantMatchingGeneric) {
            return;
        }

        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        PeptideVariantsPreferences peptideVariantsPreferences = new PeptideVariantsPreferences();
        peptideVariantsPreferences.setnVariants(1);
        peptideVariantsPreferences.setUseSpecificCount(false);

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File("src/test/resources/experiment/proteinTreeTestSequences_1");
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);

        AminoAcidSequence aminoAcidPattern;
        double nTermGap;
        double cTermGap;
        Tag tag;
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        PtmSettings ptmSettings;
        FMIndex fmIndex;
        boolean isPresent;
        int correctVariants;

        // TESTMRITESTCKTESTKMELTSESTES with no variants
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCKTE")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in sequence
        aminoAcidPattern = new AminoAcidSequence("TST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITSTCKTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 7 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'E') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in sequence
        aminoAcidPattern = new AminoAcidSequence("TGST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITGSTCKTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 7 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'E' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'G') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in sequence
        aminoAcidPattern = new AminoAcidSequence("TGEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITGESTCKTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 7 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'G') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMITESTCKT")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 4 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'R') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("SCMRITESTCKTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 2 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'T' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'C') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + 2 * AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STTMRITESTCKTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 2 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("TMRITESTCTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 10 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'K') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCCTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 11 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'K' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'C') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTACKT")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 10 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'A') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMITESTCKTES")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 4 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'R') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("SCMRITESTCKTEST")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 2 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'T' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'C') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in left mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + 2 * AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + 2 * AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STTMRITESTCKTESTK")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 2 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in right mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("TMRITESTCTEST")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 10 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'K') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in right mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCCTEST")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 11 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'K' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'C') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in right mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTACKTES")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 10 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'A') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);
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
        if (!testVariantPTMMatching) {
            return;
        }
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        PeptideVariantsPreferences peptideVariantsPreferences = new PeptideVariantsPreferences();
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.singleBaseSubstitution);
        peptideVariantsPreferences.setnVariants(1);
        peptideVariantsPreferences.setUseSpecificCount(false);

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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
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
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        numPTMs = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTES")) {
                isPresent = true;
                for (ModificationMatch mm : peptideProteinMapping.getModificationMatches()) {
                    if (mm.getModificationSite() == 2) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 3) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 10) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 12) {
                        ++numPTMs;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(numPTMs == 4);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in right mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = 15.99 - 18.01 + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = 57.02 - 2 * 18.01 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        numPTMs = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTEST")) {
                isPresent = true;
                for (ModificationMatch mm : peptideProteinMapping.getModificationMatches()) {
                    if (mm.getModificationSite() == 2) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 3) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 10) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 12) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 15) {
                        ++numPTMs;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(numPTMs == 5);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in right mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        numPTMs = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTEST")) {
                isPresent = true;
                for (ModificationMatch mm : peptideProteinMapping.getModificationMatches()) {
                    if (mm.getModificationSite() == 2) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 10) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 12) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 15) {
                        ++numPTMs;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(numPTMs == 4);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in right mass with higher right mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass() + 15.99 + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = 57.02 + AminoAcid.C.getMonoisotopicMass() + 15.99 + AminoAcid.M.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        ptmSettings.addFixedModification(ptmFactory.getPTM("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getPTM("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getPTM("Oxidation of M"));
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        numPTMs = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTEST")) {
                isPresent = true;
                for (ModificationMatch mm : peptideProteinMapping.getModificationMatches()) {
                    if (mm.getModificationSite() == 2) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 3) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 10) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 11) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 12) {
                        ++numPTMs;
                    }
                    if (mm.getModificationSite() == 15) {
                        ++numPTMs;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(numPTMs == 6);
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
    public void testTagToProteinMappingWithVariantsSpecific() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        if (!testVariantMatchingSpecific) {
            return;
        }

        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        PeptideVariantsPreferences peptideVariantsPreferences = new PeptideVariantsPreferences();
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        peptideVariantsPreferences.setUseSpecificCount(true);

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
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        PtmSettings ptmSettings;
        FMIndex fmIndex;
        boolean isPresent;
        int correctVariants;

        // TESTMRITESTCKTESTKMELTSESTES with no variants
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCKTE")) {
                isPresent = true;
                break;
            }
        }
        Assert.assertTrue(isPresent);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in sequence
        aminoAcidPattern = new AminoAcidSequence("TST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTKMELTSESTES with deletion in sequence
        aminoAcidPattern = new AminoAcidSequence("TST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTKMELTSESTES with deletion in sequence
        aminoAcidPattern = new AminoAcidSequence("TST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITSTCKTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 7 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'E') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in sequence
        aminoAcidPattern = new AminoAcidSequence("TGST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTKMELTSESTES with insertion in sequence
        aminoAcidPattern = new AminoAcidSequence("TGEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITGESTCKTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 7 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'G') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left mass but with empty substitution matrix
        aminoAcidPattern = new AminoAcidSequence("TAST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(1);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.noSubstitution);
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left non-empty substitution matrix
        aminoAcidPattern = new AminoAcidSequence("TAST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(1);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.singleBaseSubstitution);
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITASTCKTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 7 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'E' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'A') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left non-empty substitution matrix
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(1);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.singleBaseSubstitution);
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STKRITESTCKTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 3 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'M' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'K') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        // TESTMRITESTCKTESTKMELTSESTES with insertion in left mass
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + 2 * AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STTMRITESTCTE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 2 && v.getVariant() instanceof Insertion && ((Insertion) v.getVariant()).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 12 && v.getVariant() instanceof Deletion && ((Deletion) v.getVariant()).getDeletedAminoAcid() == 'K') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 2);

        // TESTMRITESTCKTESTKMELTSESTES with deletion in right mass
        aminoAcidPattern = new AminoAcidSequence("CEST");
        nTermGap = AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = 2 * AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new PtmSettings();
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaSubstitutions(2);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.synonymousVariant);
        fmIndex = new FMIndex(waitingHandlerCLIImpl, false, ptmSettings, peptideVariantsPreferences, 0.02);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, null, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("TMRICESTCKCE")) {
                isPresent = true;
                for (VariantMatch v : peptideProteinMapping.getVariantMatches()) {
                    if (v.getSite() == 5 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'T' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'C') {
                        ++correctVariants;
                    }
                    if (v.getSite() == 11 && v.getVariant() instanceof Substitution && ((Substitution) v.getVariant()).getOriginalAminoAcid() == 'T' && ((Substitution) v.getVariant()).getSubstitutedAminoAcid() == 'C') {
                        ++correctVariants;
                    }
                }
                break;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 2);
    }
}