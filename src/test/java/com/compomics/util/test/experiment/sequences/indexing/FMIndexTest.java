package com.compomics.util.test.experiment.sequences.indexing;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import com.compomics.util.experiment.biology.variants.Variant;
import com.compomics.util.experiment.biology.variants.amino_acids.Deletion;
import com.compomics.util.experiment.biology.variants.amino_acids.Insertion;
import com.compomics.util.experiment.biology.variants.amino_acids.Substitution;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.PeptideVariantMatches;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.experiment.io.biology.protein.ProteinIterator;
import com.compomics.util.experiment.io.biology.protein.converters.DecoyConverter;
import com.compomics.util.experiment.io.biology.protein.iterators.FastaIterator;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.advanced.PeptideVariantsParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.poi.hpsf.VariantTypeException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Test for the FM Index.
 *
 * @author Marc Vaudel
 * @author Kenneth Verheggen
 * @author Dominik Kopczynski
 */
public class FMIndexTest extends TestCase {
    
    public void testWhatHasToBeTested(){
        try {
            terminiPTMTagMapping();
            getSequences();
            peptideToProteinMapping();
            peptideToProteinMappingWithVariants();
            peptideToProteinMappingWithVariantsSpecific();
            tagToProteinMapping();
            tagToProteinMappingWithPTMsAndVariants();
            tagToProteinMappingWithVariantsGeneric();
            tagToProteinMappingWithVariantsSpecific();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    public void terminiPTMTagMapping() throws IOException {
        
        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = ModificationFactory.getInstance();

        PeptideVariantsParameters peptideVariantsPreferences = PeptideVariantsParameters.getNoVariantPreferences();

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        
        File fastaFile = new File("src/test/resources/experiment/terminiSequence.fasta");
        FastaParameters fastaParameters = new FastaParameters();
        fastaParameters.setDefaultAttributes(fastaFile);
        fastaParameters = DecoyConverter.getDecoyParameters(fastaParameters);
        
        PeptideProteinMapping peptideProteinMapping;
        AminoAcidSequence aminoAcidPattern;
        double nTermGap;
        double cTermGap;
        Tag tag;
        ModificationParameters ptmSettings;
        FMIndex fmIndex;
        
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        //ArrayList<ModificationMatch> modificationMatches;
        //ModificationMatch modificationMatch;
        
        SearchParameters searchParameters = new SearchParameters();
        
        
        
        
        searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        searchParameters.setFragmentIonAccuracy(0.02);
        
        
        aminoAcidPattern = new AminoAcidSequence("AIIG");
        nTermGap = 2626.29;
        cTermGap = 470.224;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of protein N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
                
        try {
            peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
            Assert.assertTrue(peptideProteinMappings.isEmpty());
        }
        catch(Exception e){
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }
    

    /**
     * Tests the retrieving of protein sequences from the index
     *
     * @throws IOException thrown whenever an error occurs while reading or
     * writing a file
     */
    public void getSequences() throws IOException {

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        
        File fastaFile = new File("src/test/resources/experiment/testSequences.fasta");
        FastaParameters fastaParameters = new FastaParameters();
        fastaParameters.setDefaultAttributes(fastaFile);
        fastaParameters = DecoyConverter.getDecoyParameters(fastaParameters);
        
        PeptideVariantsParameters peptideVariantsPreferences = PeptideVariantsParameters.getNoVariantPreferences();

        FMIndex fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, null);

        ProteinIterator pi = new FastaIterator(fastaFile);
        Protein protein;
        while ((protein = pi.getNextProtein()) != null) {
            String accession = protein.getAccession();
            String originalSequence = protein.getSequence().toUpperCase();
            String sequence = fmIndex.getSequence(accession);
            Assert.assertTrue(originalSequence.equals(sequence));
        }
    }    

    /**
     * Tests the import and the mapping of a few peptide sequences.
     *
     * @throws IOException thrown whenever an error occurs while reading or
     * writing a file
     */
    public void peptideToProteinMapping() throws IOException {

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        
        File fastaFile = new File("src/test/resources/experiment/testSequences.fasta");
        FastaParameters fastaParameters = new FastaParameters();
        fastaParameters.setDefaultAttributes(fastaFile);
        fastaParameters = DecoyConverter.getDecoyParameters(fastaParameters);
        
        PeptideVariantsParameters peptideVariantsPreferences = PeptideVariantsParameters.getNoVariantPreferences();

        FMIndex fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, null);

        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);
        ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping("ECTQDRGKTAFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        PeptideProteinMapping peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("ECTQDRXKTAFTEAVLLP"));
        Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
        
        
        String sequence = fmIndex.getSequence("Q9FHX5");
        
        
        peptideProteinMappings = fmIndex.getProteinMapping("SSS", SequenceMatchingParameters.defaultStringMatching);
        HashMap<String, HashMap<String, int[]>> testIndexes = PeptideProteinMapping.getPeptideProteinIndexesMap(peptideProteinMappings);

        HashMap<String, int[]> proteinMapping = testIndexes.get("SSS");
        Assert.assertTrue(proteinMapping.size() == 2);
        int[] indexes = proteinMapping.get("Q9FHX5");
        Assert.assertTrue(indexes.length == 3);
        int index = sequence.indexOf("SSS");
        Assert.assertTrue(indexes[0] == index);
        index += sequence.substring(index + 1).indexOf("SSS") + 1;
        Assert.assertTrue(indexes[1] == index);
        index = sequence.lastIndexOf("SSS");
        Assert.assertTrue(indexes[2] == index);
        indexes = proteinMapping.get("Q9FHX5-REVERSED");
        
        sequence = fmIndex.getSequence("Q9FHX5-REVERSED");
        Assert.assertTrue(sequence.equals("LRLKIICLLFFFLVCEVFRGKGGASSISMYDPSPNGTVPQYIGGGSSSGGTNGGGGSSSGGTSNSSGSGSSHTSSTKIGLSYVPTGDPNFLGYNRESTPGPKMNENFLAFVFITLDCEPRIPTRMKKSMMMKILNGNYKRANDCTAGVEQPDGNSPWGTESVVIPVKKYSIGVADLAHYVADVQAFLMNDYHFNSGPDTFGQNPQFLVFDLSVHKPNEEYAFFPYANILIPSGTKVHFDLIPTLSGLLDRRFSTASPPYSVDLIALSHATTVFIQKNLGCDVLAGHISQMAPFLAATLALAVLLAVIIAVLIAVIKTNPLDNAVNEKVWGQAKIPDSMQALYENGLAVTLEFGSGAFARLAQPDADYLKVKTAGVSKLLPIVNKPPPLBNAVQGYNIGISSVILPLSFLALCFLSFLSQLSSSAM"));
        Assert.assertTrue(indexes.length == 3);
        index = sequence.indexOf("SSS");
        Assert.assertTrue(indexes[0] == index);
        index += sequence.substring(index + 1).indexOf("SSS") + 1;
        Assert.assertTrue(indexes[1] == index);
        index = sequence.lastIndexOf("SSS");
        Assert.assertTrue(indexes[2] == index);

        HashSet<String> accessions = new HashSet<>(fmIndex.getAccessions());
        Assert.assertTrue(accessions.size() == 6);
        Assert.assertTrue(accessions.contains("Q9FHX5"));
        Assert.assertTrue(fmIndex.getHeader("Q9FHX5").equals("sw|Q9FHX5|E1310_ARATH Glucan endo-1,3-beta-glucosidase 10 OS=Arabidopsis thaliana GN=At5g42100 PE=1 SV=1"));
        
        Assert.assertTrue(accessions.contains("Q9FHX5-REVERSED"));
        Assert.assertTrue(fmIndex.getHeader("Q9FHX5-REVERSED").equals("sw|Q9FHX5-REVERSED|E1310_ARATH Glucan endo-1,3-beta-glucosidase 10 OS=Arabidopsis thaliana GN=At5g42100 PE=1 SV=1-REVERSED"));
        
        Assert.assertTrue(accessions.contains("Q9FI94"));
        Assert.assertTrue(fmIndex.getHeader("Q9FI94").equals("sw|Q9FI94|DHYS_ARATH Deoxyhypusine synthase OS=Arabidopsis thaliana GN=DHS PE=2 SV=1"));
        
        Assert.assertTrue(accessions.contains("Q9FI94-REVERSED"));
        Assert.assertTrue(fmIndex.getHeader("Q9FI94-REVERSED").equals("sw|Q9FI94-REVERSED|DHYS_ARATH Deoxyhypusine synthase OS=Arabidopsis thaliana GN=DHS PE=2 SV=1-REVERSED"));
        
        Assert.assertTrue(accessions.contains("TEST_ACCESSION"));
        Assert.assertTrue(fmIndex.getHeader("TEST_ACCESSION").equals("sw|TEST_ACCESSION|DHYS_ARATH Deoxyhypusine synthase OS=Arabidopsis thaliana GN=DHS PE=2 SV=1-REVERSED"));
        
        Assert.assertTrue(accessions.contains("TEST_ACCESSION-REVERSED"));
        Assert.assertTrue(fmIndex.getHeader("TEST_ACCESSION-REVERSED").equals("sw|TEST_ACCESSION-REVERSED|DHYS_ARATH Deoxyhypusine synthase OS=Arabidopsis thaliana GN=DHS PE=3 SV=1-REVERSED"));
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
    public void peptideToProteinMappingWithVariants() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {
        
        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        
        File fastaFile = new File("src/test/resources/experiment/testSequences.fasta");
        FastaParameters fastaParameters = new FastaParameters();
        fastaParameters.setDefaultAttributes(fastaFile);
        fastaParameters = DecoyConverter.getDecoyParameters(fastaParameters);
        
        PeptideVariantsParameters peptideVariantsPreferences = PeptideVariantsParameters.getNoVariantPreferences();
        peptideVariantsPreferences.setnVariants(1);
        peptideVariantsPreferences.setUseSpecificCount(false);

        FMIndex fmIndex;
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        int correctVariants = 0;
        boolean isPresent;

        // ECTQDRGKTAFTEAVLLP
        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, null);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTQDRGKTAFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQDRXKTAFTEAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();
                Assert.assertTrue(peptideVariantMatches == null);
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
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 14 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'A') {
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
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 10 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'M') {
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
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 6 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'R' && ((Substitution) variant).getSubstitutedAminoAcid() == 'K') {
                        ++correctVariants;
                    }
                    
                }
                
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);
        Assert.assertTrue(correctVariants == 1);

        peptideVariantsPreferences.setnVariants(2);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, null);

        peptideProteinMappings = fmIndex.getProteinMapping("ECTQDKGKTAFTEALLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQDKXKTAFTEALLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 6 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'R' && ((Substitution) variant).getSubstitutedAminoAcid() == 'K') {
                        ++correctVariants;
                    }
                    
                    if (site == 15 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'V') {
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
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 16 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                    
                    if (site == 4 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'Q') {
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
    public void peptideToProteinMappingWithVariantsSpecific() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        
        File fastaFile = new File("src/test/resources/experiment/testSequences.fasta");
        FastaParameters fastaParameters = new FastaParameters();
        fastaParameters.setDefaultAttributes(fastaFile);
        fastaParameters = DecoyConverter.getDecoyParameters(fastaParameters);
        
        PeptideVariantsParameters peptideVariantsPreferences = PeptideVariantsParameters.getNoVariantPreferences();
        peptideVariantsPreferences.setUseSpecificCount(true);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.noSubstitution);
        peptideVariantsPreferences.setnAaSubstitutions(1);

        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        FMIndex fmIndex;
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        int correctVariants = 0;
        boolean isPresent;

        // ECTQDRGKTAFTEAVLLP no variant
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, null);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTQDRGKTAFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQDRXKTAFTEAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();
                Assert.assertTrue(peptideVariantMatches == null);
                isPresent = true;
            }
        }
        Assert.assertTrue(isPresent);

        // ECTQDRGKTAFTEAVLLP two substitutions
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.allSubstitutions);
        peptideVariantsPreferences.setnAaSubstitutions(2);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, null);
        peptideProteinMappings = fmIndex.getProteinMapping("ECPQDRGKTRFTEAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECPQDRXKTRFTEAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 3 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'T' && ((Substitution) variant).getSubstitutedAminoAcid() == 'P') {
                        ++correctVariants;
                    }
                    
                    if (site == 10 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'A' && ((Substitution) variant).getSubstitutedAminoAcid() == 'R') {
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
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, null);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTDRGKTAFTEAVLTLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTDRXKTAFTEAVLTLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 4 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'Q') {
                        ++correctVariants;
                    }
                    
                    if (site == 16 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'T') {
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
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, null);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTQTTDRGKTAFTAVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTQTTDRXKTAFTAVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 5 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                    
                    if (site == 6 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                    
                    if (site == 15 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'E') {
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
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, null);
        peptideProteinMappings = fmIndex.getProteinMapping("ECTDRGKPAFTEAKVLLP", sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        correctVariants = 0;
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("ECTDRXKPAFTEAKVLLP")) {
                Assert.assertTrue(peptideProteinMapping.getProteinAccession().equals("TEST_ACCESSION"));
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 4 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'Q') {
                        ++correctVariants;
                    }
                    
                    if (site == 8 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'T' && ((Substitution) variant).getSubstitutedAminoAcid() == 'P') {
                        ++correctVariants;
                    }
                    
                    if (site == 14 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'K') {
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
    public void tagToProteinMapping() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        
        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = ModificationFactory.getInstance();

        PeptideVariantsParameters peptideVariantsPreferences = PeptideVariantsParameters.getNoVariantPreferences();

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        
        File fastaFile = new File("src/test/resources/experiment/testSequences_1.fasta");
        FastaParameters fastaParameters = new FastaParameters();
        fastaParameters.setDefaultAttributes(fastaFile);
        fastaParameters = DecoyConverter.getDecoyParameters(fastaParameters);
        
        PeptideProteinMapping peptideProteinMapping;
        AminoAcidSequence aminoAcidPattern;
        double nTermGap;
        double cTermGap;
        Tag tag;
        ModificationParameters ptmSettings;
        FMIndex fmIndex;
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        int numModifications = 0;
        int numMatches = 0;
        ModificationMatch[] modificationMatches;
        ModificationMatch modificationMatch;
        
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setFragmentIonAccuracy(0.02);
        searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        
        AtomChain atomChainAdded;
        AtomChain atomChainRemoved;
        AminoAcidPattern aminoAcidPatternPattern;
        Modification ptmPattern;
        
        
        ////////////////////////////////////////////////////////////////////////
        // normal tags mass accuracy in PPM
        ////////////////////////////////////////////////////////////////////////
        
        searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.PPM);
        searchParameters.setFragmentIonAccuracy(5.);
        
        
        // TESTMRITESTCKTESTK with no modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        
        
        
        
        // TESTMRITESTCKTESTK with no modifications
        aminoAcidPattern = new AminoAcidSequence("TMRITESTCK");
        nTermGap = 0;
        cTermGap = 0;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        
        
        
        // TESTMRITESTCKTESTK with no modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        
        
        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 9);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);

        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TESTC");
        nTermGap = ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Dehydration of T"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);

         
        // TESTMRITESTCKTESTK with two fixed modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Dehydration of T"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 2);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);

        
        
        
        ////////////////////////////////////////////////////////////////////////
        // normal tags mass accuracy in DA
        ////////////////////////////////////////////////////////////////////////
        
        
        searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        searchParameters.setFragmentIonAccuracy(0.02);
        // TESTMRITESTCKTESTK with no modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        
        
        
        // TESTMRITESTCKTESTK with no modifications
        aminoAcidPattern = new AminoAcidSequence("TMRITESTCK");
        nTermGap = 0;
        cTermGap = 0;
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        
        
        
        // TESTMRITESTCKTESTK with no modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        //searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.PPM);
        //searchParameters.setFragmentIonAccuracy(5.);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("TMRITESTCK") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        
        
        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 9);

        // TESTMRITESTCKTESTK with one fixed modification
        aminoAcidPattern = new AminoAcidSequence("TESTC");
        nTermGap = ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Dehydration of T"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);

         
        // TESTMRITESTCKTESTK with two fixed modifications
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Dehydration of T"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 2);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);
        
        
        
        
        
        // TESTMRITESTCKTESTK with two fixed modifications that match nowthere 
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Dehydration of T"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
        
        
        
        

        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 2);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 2);

        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 9);

        // TESTMRITESTCKTESTK with one fixed and one variable modification
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 9);
        
        
        //TTGFQ ASNL GKTGMII LGG GLPKHH
        aminoAcidPattern = new AminoAcidSequence("ASNL");
        nTermGap = AminoAcid.T.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.G.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.Q.getMonoisotopicMass();
        double mTermGap = AminoAcid.G.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + 
                AminoAcid.G.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() +
                AminoAcid.I.getMonoisotopicMass();
        cTermGap = AminoAcid.G.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass() + 
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.H.getMonoisotopicMass() + AminoAcid.H.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, mTermGap);
        tag.addAminoAcidSequence(new AminoAcidSequence("LGG"));
        tag.addMassGap(cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TTGFQASNLGKTGMIILGGGLPKHH"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        
        
        ////////////////////////////////////////////////////////////////////////
        // tags with modification patterns 
        ////////////////////////////////////////////////////////////////////////
        
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed pattern modification
        atomChainAdded = ptmFactory.getModification("Carbamidomethylation of C").getAtomChainAdded();
        atomChainRemoved = null;
        aminoAcidPatternPattern = AminoAcidPattern.getAminoAcidPatternFromString("[AST][MPST][RS]", 2);
        ptmPattern = new Modification(ModificationType.modaa, "Domification of R", "doc", atomChainAdded, atomChainRemoved, aminoAcidPatternPattern);
        ptmFactory.addUserModification(ptmPattern);
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmPattern);
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 3);
        
        
        
        
        
        // TESTMRITESTCKTESTK with one fixed pattern modification
        atomChainAdded = ptmFactory.getModification("Carbamidomethylation of C").getAtomChainAdded();
        atomChainRemoved = null;
        aminoAcidPatternPattern = AminoAcidPattern.getAminoAcidPatternFromString("[PST][IJT][CMP]", 2);
        ptmPattern = new Modification(ModificationType.modaa, "Domification of R", "doc", atomChainAdded, atomChainRemoved, aminoAcidPatternPattern);
        ptmFactory.addUserModification(ptmPattern);
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmPattern);
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 9);
        
        
        
        
        ////////////////////////////////////////////////////////////////////////
        // tags with modifications at the termini
        ////////////////////////////////////////////////////////////////////////
        
        // TESTMRITESTCKTESTK with one fixed modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Acetylation of peptide N-term").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Acetylation of peptide N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);
        
        
        

        // TESTMRITESTCKTESTK with one fixed modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Acetylation of peptide N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
        
        
        

        // TESTMRITESTCKTESTK with one variable modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Acetylation of peptide N-term").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of peptide N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);
        

        // TESTMRITESTCKTESTK with one variable modification at peptide n-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of peptide N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 0);

        // TESTMRITESTCKTESTK with two fixed modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Acetylation of peptide N-term").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Amidation of the peptide C-term").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Acetylation of peptide N-term"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Amidation of the peptide C-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 2);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);

        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Acetylation of peptide N-term").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Amidation of the peptide C-term").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Amidation of the peptide C-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 2);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);
        Assert.assertTrue(modificationMatches[1].getSite() == 10);

        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = ptmFactory.getModification("Acetylation of peptide N-term").getMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Amidation of the peptide C-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);

        // TESTMRITESTCKTESTK with one fixed and one variable modification at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Amidation of the peptide C-term").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of peptide N-term"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Amidation of the peptide C-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 10);

        // TESTMRITESTCKTESTK with two variable modifications at peptide n-terminus and c-terminus
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of peptide N-term"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Amidation of the peptide C-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TMRITESTCK"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 0);

        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of protein N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TESTMRITE"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 0);
        

        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = ptmFactory.getModification("Acetylation of protein N-term").getMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of protein N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TESTMRITE"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);
        

        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = ptmFactory.getModification("Acetylation of protein N-term").getMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Acetylation of protein N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TESTMRITE"));
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);

        // TESTMRITESTCKTESTK with one variable modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Acetylation of protein N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTK with one fixed modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("STMR");
        nTermGap = ptmFactory.getModification("Palmitoylation of protein N-term").getMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Palmitoylation of protein N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("TESTMRITE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);

        // TESTMRITESTCKTESTKMELTSESTE with one variable modifications at protein c-terminus
        aminoAcidPattern = new AminoAcidSequence("LTSE");
        nTermGap = AminoAcid.E.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Amidation of the protein C-term").getMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Amidation of the protein C-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("ELTSESTE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 8);

        // TESTMRITESTCKTESTKMELTSESTE with several modifictations
        aminoAcidPattern = new AminoAcidSequence("LTSE");
        nTermGap = ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Amidation of the protein C-term").getMass() + ptmFactory.getModification("HexNAc of T").getMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Amidation of the protein C-term"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Oxidation of M"));
        ptmSettings.addVariableModification(ptmFactory.getModification("HexNAc of T"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("MELTSESTE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 3);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getSite() == 1) ++numModifications;
            if (mm.getSite() == 8) ++numModifications;
            if (mm.getSite() == 9) ++numModifications;
        }
        Assert.assertTrue(numModifications == 3);

        // TESTMRITESTCKTESTKMELTSESTE with several modifictations
        aminoAcidPattern = new AminoAcidSequence("ELTS");
        nTermGap = ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.M.getMonoisotopicMass() + 42.01 + AminoAcid.K.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Amidation of the protein C-term").getMass() + ptmFactory.getModification("HexNAc of T").getMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + 2 * AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Amidation of the protein C-term"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Oxidation of M"));
        ptmSettings.addVariableModification(ptmFactory.getModification("HexNAc of T"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of K"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);

        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("KMELTSESTE"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getSite() == 1) ++numModifications;
            if (mm.getSite() == 2) ++numModifications;
            if (mm.getSite() == 9) ++numModifications;
            if (mm.getSite() == 10) ++numModifications;
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LGMPCVVPINMKILD") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 49);
        
        
        // LG(M)PCVVPINMKILD => LGXPCVVPINMKILD
        aminoAcidPattern = new AminoAcidSequence("VVPI");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.G.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        nTermGap += ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.M.getMonoisotopicMass();
        cTermGap = AminoAcid.N.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LGMPCVVPINMKILD") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 3);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 49);
        
        
        // G(C)PCVVPINMKILD => GXPCVVPINMKILD
        aminoAcidPattern = new AminoAcidSequence("VVPI");
        nTermGap = AminoAcid.G.getMonoisotopicMass() + ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        nTermGap += ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass();
        cTermGap = AminoAcid.N.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("GCPCVVPINMKILD") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 50);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 2);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getSite() == 2)  ++numModifications;
            if (mm.getSite() == 4)  ++numModifications;
        }
        Assert.assertTrue(numModifications == 2);
        
        
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        
        
        
        // LATAWOIDN(T)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += ptmFactory.getModification("Phosphorylation of T").getMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Phosphorylation of T"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNTKRRRP") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 10);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        
        
        // LATAWOIDN(K)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Acetylation of K").getMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += ptmFactory.getModification("Acetylation of K").getMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Acetylation of K"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNKKRRRP") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 2);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getSite() == 10) ++numModifications;
            if (mm.getSite() == 11) ++numModifications;
        }
        Assert.assertTrue(numModifications == 2);
        
        
        
        // VKTCF(MY)TEAVLLPFAIT => VKTCFXXTEAVLLPFAIT
        aminoAcidPattern = new AminoAcidSequence("LLPF");
        nTermGap = AminoAcid.V.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.C.getMonoisotopicMass() + AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.E.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass();
        nTermGap += ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass();
        cTermGap = AminoAcid.A.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
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
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
        
        
        
        // HQVLYRITDRVKTCF(MW)TE => HQVLYRITDRVKTCFXXTE
        aminoAcidPattern = new AminoAcidSequence("YRIT");
        nTermGap = AminoAcid.H.getMonoisotopicMass() + AminoAcid.Q.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() +
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        cTermGap += AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
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
        cTermGap += ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("HQVLYRITDRVKTCFMWTE") == 0){
                ++numMatches;
                modificationMatches = pPM.getModificationMatches();
                Assert.assertTrue(modificationMatches != null);
                Assert.assertTrue(modificationMatches.length == 1);
                modificationMatch = modificationMatches[0];
                Assert.assertTrue(modificationMatch.getSite() == 16);
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
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
        
        
        
        
        // HQVLYRITDRVKTCF(MW)TE => HQVLYRITDRVKTCFXXTE
        aminoAcidPattern = new AminoAcidSequence("YRIT");
        nTermGap = AminoAcid.H.getMonoisotopicMass() + AminoAcid.Q.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() +
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        cTermGap += ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("HQVLYRITDRVKTCFMWTE") == 0){
                ++numMatches;
                modificationMatches = pPM.getModificationMatches();
                Assert.assertTrue(modificationMatches != null);
                Assert.assertTrue(modificationMatches.length == 1);
                modificationMatch = modificationMatches[0];
                Assert.assertTrue(modificationMatch.getSite() == 16);
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
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
        
        
        // FIXTHISSHITY with one fixed modifications at protein n-terminus should not map because X has value zero here
        aminoAcidPattern = new AminoAcidSequence("ISSH");
        nTermGap = ptmFactory.getModification("Acetylation of protein N-term").getMass() + AminoAcid.F.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.H.getMonoisotopicMass();
        cTermGap = AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.Y.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Palmitoylation of protein N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 0);
        
        
        
        // FITTHISSHITTY => FIXTHISSHITTY with one fixed modifications at protein n-terminus
        aminoAcidPattern = new AminoAcidSequence("ISSH");
        nTermGap = ptmFactory.getModification("Acetylation of protein N-term").getMass() + AminoAcid.F.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.H.getMonoisotopicMass();
        cTermGap = AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.Y.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of protein N-term"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().equals("FITTHISSHITTY"));
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        modificationMatch = modificationMatches[0];
        Assert.assertTrue(modificationMatch.getSite() == 1);
        
        
        
        
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addVariableModification(ptmFactory.getModification("Amidation of the peptide C-term")); // -0.98
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Amidation of the peptide C-term").getMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addVariableModification(ptmFactory.getModification("Amidation of the peptide C-term")); // -0.98
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Amidation of the peptide C-term").getMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addFixedModification(ptmFactory.getModification("Amidation of the peptide C-term")); // -0.98
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addFixedModification(ptmFactory.getModification("Amidation of the peptide C-term")); // -0.98
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());
        
        // LATAWOIDN(P)KRRRP => LATAWOIDNXKRRRP
        aminoAcidPattern = new AminoAcidSequence("AWOIDN");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("18O(2) of peptide C-term").getMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        cTermGap += AminoAcid.P.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        //ptmSettings.addVariableModification(ptmFactory.getPTM("Acetylation of peptide N-term")); // +42.01
        //ptmSettings.addFixedModification(ptmFactory.getPTM("Amidation of the peptide C-term")); // -0.98
        ptmSettings.addFixedModification(ptmFactory.getModification("18O(2) of peptide C-term")); // +4.01
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("LATAWOIDNPKRRRP") == 0);
        Assert.assertTrue(peptideProteinMapping.getIndex() == 1);
        
        // G(C)PCVVPINMKILD => GXPCVVPINMKILD
        aminoAcidPattern = new AminoAcidSequence("VVPI");
        nTermGap = ptmFactory.getModification("Formylation of peptide N-term").getMass() + AminoAcid.G.getMonoisotopicMass() + ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.P.getMonoisotopicMass();
        nTermGap += ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass();
        cTermGap = AminoAcid.N.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Formylation of peptide N-term")); // 27.99
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("GCPCVVPINMKILD") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 3);
        numModifications = 0;
        for (ModificationMatch mm : modificationMatches) {
            if (mm.getSite() == 1)  ++numModifications;
            if (mm.getSite() == 2)  ++numModifications;
            if (mm.getSite() == 4)  ++numModifications;
        }
        Assert.assertTrue(numModifications == 3);
        
        // VKTCF(MY)TEAVLLPFAIT => VKTCFXXTEAVLLPFAIT
        aminoAcidPattern = new AminoAcidSequence("LLPF");
        nTermGap = ptmFactory.getModification("Acetylation of peptide N-term").getMass() + AminoAcid.V.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.C.getMonoisotopicMass() + AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.E.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass();
        nTermGap += AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        cTermGap = AminoAcid.A.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addVariableModification(ptmFactory.getModification("Acetylation of peptide N-term")); // +42.01
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 2);
        numMatches = 0;
        for (PeptideProteinMapping pPM : peptideProteinMappings) {
            if (pPM.getPeptideSequence().compareTo("VKTCFMWTEAVLLPFAIT") == 0) ++numMatches;
            if (pPM.getPeptideSequence().compareTo("VKTCFWMTEAVLLPFAIT") == 0) ++numMatches;
        }
        Assert.assertTrue(numMatches == 2);
        
        // VKTCF(MY)TEAVLLPFAIT => VKTCFXXTEAVLLPFAIT
        aminoAcidPattern = new AminoAcidSequence("LLPF");
        nTermGap = ptmFactory.getModification("Acetylation of peptide N-term").getMass() + AminoAcid.V.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.C.getMonoisotopicMass() + AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() +
                AminoAcid.E.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass();
        nTermGap += ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.W.getMonoisotopicMass();
        cTermGap = AminoAcid.A.getMonoisotopicMass() + AminoAcid.I.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Acetylation of peptide N-term")); // +42.01
        ptmSettings.addFixedModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
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
        cTermGap = ptmFactory.getModification("Amidation of the protein C-term").getMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass() + 
                AminoAcid.C.getMonoisotopicMass() + AminoAcid.Y.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Amidation of the protein C-term")); // -0.98
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.size() == 1);
        peptideProteinMapping = peptideProteinMappings.get(0);
        Assert.assertTrue(peptideProteinMapping.getPeptideSequence().compareTo("DRVKTCFDDTEAVLLPFAITADCY") == 0);
        modificationMatches = peptideProteinMapping.getModificationMatches();
        Assert.assertTrue(modificationMatches != null);
        Assert.assertTrue(modificationMatches.length == 1);
        Assert.assertTrue(modificationMatches[0].getSite() == 24);
        
        
        
        // DRVKTCF(DD)TEAVLLPFAITADC => DRVKTCFXXTEAVLLPFAITADC
        aminoAcidPattern = new AminoAcidSequence("AVLLPFAI");
        nTermGap = AminoAcid.D.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.V.getMonoisotopicMass() +
                AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.C.getMonoisotopicMass() +
                AminoAcid.F.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        nTermGap += AminoAcid.D.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Amidation of the protein C-term").getMass()+ AminoAcid.T.getMonoisotopicMass() + AminoAcid.A.getMonoisotopicMass() + AminoAcid.D.getMonoisotopicMass() + 
                AminoAcid.C.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Amidation of the protein C-term")); // -0.98
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
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
    public void tagToProteinMappingWithVariantsGeneric() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        
        
        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        PeptideVariantsParameters peptideVariantsPreferences = new PeptideVariantsParameters();
        peptideVariantsPreferences.setnVariants(1);
        peptideVariantsPreferences.setUseSpecificCount(false);

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        
        File fastaFile = new File("src/test/resources/experiment/testSequences_1.fasta");
        FastaParameters fastaParameters = new FastaParameters();
        fastaParameters.setDefaultAttributes(fastaFile);
        fastaParameters = DecoyConverter.getDecoyParameters(fastaParameters);

        AminoAcidSequence aminoAcidPattern;
        double nTermGap;
        double cTermGap;
        Tag tag;
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        ModificationParameters ptmSettings;
        FMIndex fmIndex;
        boolean isPresent;
        int correctVariants;
        
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setFragmentIonAccuracy(0.02);
        searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);

        // TESTMRITESTCKTESTKMELTSESTES with no variants
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITSTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 7 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'E') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITGSTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 7 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'E' && ((Substitution) variant).getSubstitutedAminoAcid() == 'G') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITGESTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 7 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'G') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMITESTCKT")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 4 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'R') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("SCMRITESTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 2 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'T' && ((Substitution) variant).getSubstitutedAminoAcid() == 'C') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STTMRITESTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 2 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'T') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("TMRITESTCTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 10 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'K') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCCTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 11 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'K' && ((Substitution) variant).getSubstitutedAminoAcid() == 'C') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTACKT")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 10 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'A') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMITESTCKTES")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 4 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'R') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("SCMRITESTCKTEST")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 2 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'T' && ((Substitution) variant).getSubstitutedAminoAcid() == 'C') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STTMRITESTCKTESTK")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 2 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'T') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("TMRITESTCTEST")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 10 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'K') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCCTEST")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 11 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'K' && ((Substitution) variant).getSubstitutedAminoAcid() == 'C') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTACKTES")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 10 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'A') {
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
    public void tagToProteinMappingWithPTMsAndVariants() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        
        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        PeptideVariantsParameters peptideVariantsPreferences = new PeptideVariantsParameters();
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.singleBaseSubstitution);
        peptideVariantsPreferences.setnVariants(1);
        peptideVariantsPreferences.setUseSpecificCount(false);

        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = ModificationFactory.getInstance();

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        
        File fastaFile = new File("src/test/resources/experiment/testSequences_1.fasta");
        FastaParameters fastaParameters = new FastaParameters();
        fastaParameters.setDefaultAttributes(fastaFile);
        fastaParameters = DecoyConverter.getDecoyParameters(fastaParameters);

        AminoAcidSequence aminoAcidPattern;
        double nTermGap;
        double cTermGap;
        Tag tag;
        ModificationParameters ptmSettings;
        FMIndex fmIndex;
        boolean isPresent;
        int numPTMs = 0;
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setFragmentIonAccuracy(0.02);
        searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);

        // TESTMRITESTCKTESTK with one fixed modification and one variant
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.L.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + 0 * AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
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
        nTermGap = ptmFactory.getModification("Oxidation of M").getMass() + ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() - 18.01 + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + 0 * AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        numPTMs = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTES")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                for (ModificationMatch mm : peptideProteinMapping.getModificationMatches()) {
                    if (mm.getSite() == 2) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 3) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 10) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 12) {
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
        nTermGap = ptmFactory.getModification("Oxidation of M").getMass() + ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + 2 * ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        numPTMs = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTEST")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                for (ModificationMatch mm : peptideProteinMapping.getModificationMatches()) {
                    if (mm.getSite() == 2) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 3) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 10) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 12) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 15) {
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
        nTermGap = AminoAcid.S.getMonoisotopicMass() + ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() + ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        numPTMs = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTEST")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                for (ModificationMatch mm : peptideProteinMapping.getModificationMatches()) {
                    if (mm.getSite() == 2) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 10) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 12) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 15) {
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
        nTermGap = AminoAcid.S.getMonoisotopicMass() + ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.T.getMonoisotopicMass() + ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = ptmFactory.getModification("Carbamidomethylation of C").getMass() + AminoAcid.C.getMonoisotopicMass() + ptmFactory.getModification("Oxidation of M").getMass() + AminoAcid.M.getMonoisotopicMass() + ptmFactory.getModification("Dehydration of T").getMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass() + AminoAcid.S.getMonoisotopicMass() - 18.01 + AminoAcid.T.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        ptmSettings.addFixedModification(ptmFactory.getModification("Carbamidomethylation of C"));
        ptmSettings.addFixedModification(ptmFactory.getModification("Dehydration of T"));
        ptmSettings.addVariableModification(ptmFactory.getModification("Oxidation of M"));
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        numPTMs = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCMTEST")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                for (ModificationMatch mm : peptideProteinMapping.getModificationMatches()) {
                    if (mm.getSite() == 2) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 3) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 10) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 11) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 12) {
                        ++numPTMs;
                    }
                    if (mm.getSite() == 15) {
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
    public void tagToProteinMappingWithVariantsSpecific() throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, SQLException, XmlPullParserException {
        
        SequenceMatchingParameters sequenceMatchingPreferences = new SequenceMatchingParameters();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);

        PeptideVariantsParameters peptideVariantsPreferences = new PeptideVariantsParameters();
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        peptideVariantsPreferences.setUseSpecificCount(true);

        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = ModificationFactory.getInstance();

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        
        File fastaFile = new File("src/test/resources/experiment/testSequences_1.fasta");
        FastaParameters fastaParameters = new FastaParameters();
        fastaParameters.setDefaultAttributes(fastaFile);
        fastaParameters = DecoyConverter.getDecoyParameters(fastaParameters);

        AminoAcidSequence aminoAcidPattern;
        double nTermGap;
        double cTermGap;
        Tag tag;
        ArrayList<PeptideProteinMapping> peptideProteinMappings;
        ModificationParameters ptmSettings;
        FMIndex fmIndex;
        boolean isPresent;
        int correctVariants;
        
        
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setFragmentIonAccuracy(0.02);
        searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);

        // TESTMRITESTCKTESTKMELTSESTES with no variants
        aminoAcidPattern = new AminoAcidSequence("TEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITESTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTKMELTSESTES with deletion in sequence
        aminoAcidPattern = new AminoAcidSequence("TST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTKMELTSESTES with deletion in sequence
        aminoAcidPattern = new AminoAcidSequence("TST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITSTCKTE")) {
                isPresent = true;
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 7 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'E') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTKMELTSESTES with insertion in sequence
        aminoAcidPattern = new AminoAcidSequence("TGEST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITGESTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 7 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'G') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(1);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.noSubstitution);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(peptideProteinMappings.isEmpty());

        // TESTMRITESTCKTESTKMELTSESTES with substitution in left non-empty substitution matrix
        aminoAcidPattern = new AminoAcidSequence("TAST");
        nTermGap = AminoAcid.S.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.M.getMonoisotopicMass() + AminoAcid.R.getMonoisotopicMass() + AminoAcid.L.getMonoisotopicMass();
        cTermGap = AminoAcid.C.getMonoisotopicMass() + AminoAcid.K.getMonoisotopicMass() + AminoAcid.T.getMonoisotopicMass() + AminoAcid.E.getMonoisotopicMass();
        tag = new Tag(nTermGap, aminoAcidPattern, cTermGap);
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(1);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.singleBaseSubstitution);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STMRITASTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 7 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'E' && ((Substitution) variant).getSubstitutedAminoAcid() == 'A') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(1);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.singleBaseSubstitution);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STKRITESTCKTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 3 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'M' && ((Substitution) variant).getSubstitutedAminoAcid() == 'K') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("STTMRITESTCTE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 3);
                isPresent = true;
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 2 && variant instanceof Insertion && ((Insertion) variant).getInsertedAminoAcid() == 'T') {
                        ++correctVariants;
                    }
                    
                    if (site == 12 && variant instanceof Deletion && ((Deletion) variant).getDeletedAminoAcid() == 'K') {
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
        ptmSettings = new ModificationParameters();
        searchParameters.setModificationParameters(ptmSettings);
        peptideVariantsPreferences.setnAaDeletions(1);
        peptideVariantsPreferences.setnAaInsertions(1);
        peptideVariantsPreferences.setnAaSubstitutions(2);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.synonymousVariant);
        fmIndex = new FMIndex(fastaFile, fastaParameters, waitingHandlerCLIImpl, false, peptideVariantsPreferences, searchParameters);
        peptideProteinMappings = fmIndex.getProteinMapping(tag, sequenceMatchingPreferences);
        Assert.assertTrue(!peptideProteinMappings.isEmpty());
        isPresent = false;
        correctVariants = 0;
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            if (peptideProteinMapping.getPeptideSequence().equals("TMRICESTCKCE")) {
                Assert.assertTrue(peptideProteinMapping.getIndex() == 4);
                isPresent = true;
                PeptideVariantMatches peptideVariantMatches = peptideProteinMapping.getPeptideVariantMatches();

                for (Entry<Integer, Variant> variantEntry : peptideVariantMatches.getVariantMatches().entrySet()) {
                    
                    int site = variantEntry.getKey();
                    Variant variant = variantEntry.getValue();
                    
                    if (site == 5 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'T' && ((Substitution) variant).getSubstitutedAminoAcid() == 'C') {
                        ++correctVariants;
                    }
                    
                    if (site == 11 && variant instanceof Substitution && ((Substitution) variant).getOriginalAminoAcid() == 'T' && ((Substitution) variant).getSubstitutedAminoAcid() == 'C') {
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