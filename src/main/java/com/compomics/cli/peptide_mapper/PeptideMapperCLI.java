package com.compomics.cli.peptide_mapper;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.identification.amino_acid_tags.MassGap;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.advanced.PeptideVariantsParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import com.compomics.util.experiment.identification.protein_inference.FastaMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.*;



/**
 * Command line peptide mapping.
 *
 * @author Dominik Kopczynski
 */
public class PeptideMapperCLI {
    public static int TIMEOUT_DAYS = 1;
    /**
     * Main class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if ((args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) || args.length < 4 || (!args[0].equals("-p") && !args[0].equals("-t"))) {

            System.out.println("PeptideMapping: a tool to map peptides or sequence tags against a given proteome.");
            System.out.println("usage: PeptideMapping -[p|t] input-fasta input-peptide/tag-csv output-csv [additonal options]");
            System.out.println();
            System.out.println("Options are:");
            System.out.println("\t-p\tpeptide mapping");
            System.out.println("\t-t\tsequence tag mapping");
            System.out.println("\t-h\tprint this info");
            System.out.println();
            System.out.println("Additional options are:");
            System.out.println("\t-u [utilities-parameter-file]\tpeptide mapping");
            System.out.println("\t-f\tadd flanking amino acids to peptide in output");
            System.out.println("\t-c\tspecify the number of cores used");
            
            System.out.println();
            System.out.println("Default parameters:");
            System.out.println("\tindexing method:\t\tfm-index");
            System.out.println("\tframentation tolerance [Da]:\t0.02");

            System.exit(-1);
        }

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        File fastaFile = new File(args[1]);
        String inputFileName = args[2];
        String outputFileName = args[3];
        boolean flanking = false;
        boolean peptideMapping = args[0].equals("-p");
        int nCores = Runtime.getRuntime().availableProcessors();
        
        if (!args[0].equals("-p") && !args[0].equals("-t")){
            System.out.println("Invalid first parameter: " + args[0]);
            System.exit(-1);
        }

        // read in the parameters
        SearchParameters searchParameters = null;
        PeptideVariantsParameters peptideVariantsPreferences = PeptideVariantsParameters.getNoVariantPreferences();
        SequenceMatchingParameters sequenceMatchingPreferences = null;
        boolean customParameters = false;
        if (args.length >= 5) {
            int argPos = 4;
            while (argPos < args.length){
                switch(args[argPos]){
                    case "-f":  // flanking
                        flanking = true;
                        ++argPos;
                        break;
                        
                    case "-c": // number of cores
                        try {
                            nCores = Integer.parseInt(args[argPos + 1]);
                            if (nCores < 1 || 100000 < nCores) throw new Exception();
                        }
                        catch (Exception e){
                            System.out.println("Parameter -c has no valid number.");
                            System.exit(-1);
                        }
                        argPos += 2;
                        break;
                        
                    case "-u":  // use utilities parameter file
                        
                        IdentificationParameters identificationParameters = null;
                        try {
                            File parameterFile = new File(args[argPos + 1]);
                            identificationParameters = IdentificationParameters.getIdentificationParameters(parameterFile);
                        } catch (Exception e) {
                            System.err.println("Error: cound not open / parse parameter file");
                            System.exit(-1);
                        }

                        sequenceMatchingPreferences = identificationParameters.getSequenceMatchingParameters();
                        searchParameters = identificationParameters.getSearchParameters();
                        peptideVariantsPreferences = identificationParameters.getPeptideVariantsParameters();
                        customParameters = true;
                        argPos += 2;
                        break;
                        
                    default:
                        ++argPos;
                        break;
                }
            }

        }
        if (!customParameters) {
            searchParameters = new SearchParameters();
            searchParameters.setModificationParameters(new ModificationParameters());
            searchParameters.setFragmentIonAccuracy(0.02);
            searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
            sequenceMatchingPreferences = new SequenceMatchingParameters();
            sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids);
            sequenceMatchingPreferences.setLimitX(0.25);
        }
        if (searchParameters != null) searchParameters.setFlanking(flanking);
        
        runMapping(fastaFile,
                   waitingHandlerCLIImpl,
                   peptideVariantsPreferences,
                   searchParameters,
                   inputFileName,
                   outputFileName,
                   nCores,
                   sequenceMatchingPreferences,
                   peptideMapping);
    }
    
    
    
    
    public static void handleError(String outputFileName, String errorMessage, Throwable e){
        PrintWriter writer = null;
        System.out.println(errorMessage);
        
        String path = (new File(outputFileName)).getParent();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
        
        String logFileName = Paths.get(path, "PeptideMapper_Error-log_" + timeStamp + ".txt").toString();
        
        
        try {
            if (e instanceof OutOfMemoryError) System.gc();
            writer = new PrintWriter(logFileName, "UTF-8");
            e.printStackTrace(writer);
            writer.close();
            System.out.println("A proper error log was stored in '" + logFileName + "'");
            
        } catch (Exception e2){
            System.out.println("Could not store the error log into a file, it will be printed directly:");
            System.out.println();
            System.out.println(e.getMessage());
        }
        System.exit(-1);
    }
    
    
    
    
        
    public static void runMapping(File fastaFile,
                                  WaitingHandlerCLIImpl waitingHandlerCLIImpl,
                                  PeptideVariantsParameters peptideVariantsPreferences,
                                  SearchParameters searchParameters,
                                  String inputFileName,
                                  String outputFileName,
                                  int nCores,
                                  SequenceMatchingParameters sequenceMatchingPreferences,
                                  boolean peptideMapping){
        
        
        
        // setting up the mapper
        FastaMapper peptideMapper = null;
        System.out.println("Start indexing fasta file");
        long startTimeIndex = System.nanoTime();
        try {
            peptideMapper = new FMIndex(fastaFile, null, waitingHandlerCLIImpl, true, peptideVariantsPreferences, searchParameters);
        } catch (Exception e) {
            handleError(outputFileName, "Error: cound not index the fasta file", e);
        } catch (OutOfMemoryError e){
            handleError(outputFileName, "Error: not enough memory available. Please try to run Java with more memory, e.g.: java -Xmx16G ...\n\n", e);
        }
        
        double diffTimeIndex = System.nanoTime() - startTimeIndex;
        System.out.println();
        System.out.println("Indexing took " + (diffTimeIndex / 1e9) + " seconds and consumes " + (((float) ((FMIndex) peptideMapper).getAllocatedBytes()) / 1e6) + " MB");
        System.out.println();
        System.out.println("Start mapping using " + nCores + " threads");
        
        
        
        // open input / output files
        BufferedReader br = null;
        PrintWriter writer = null;
        long lineCount = 0;
        try {
            br = new BufferedReader(new FileReader(inputFileName), 1024 * 1024 * 10);
            Path path = Paths.get(inputFileName);
            lineCount = Files.lines(path).count();
            writer = new PrintWriter(outputFileName, "UTF-8");
        }
        catch (Exception e){
            handleError(outputFileName, "Error: could not open files properly.", e);
        }
        waitingHandlerCLIImpl.setSecondaryProgressCounterIndeterminate(false);
        waitingHandlerCLIImpl.setMaxSecondaryProgressCounter((int)lineCount);
        waitingHandlerCLIImpl.setSecondaryProgressCounter(0);



        
        
        // starting the mapping
        try {
            long startTimeMapping = System.nanoTime();

            ArrayList<MappingWorker> workers = new ArrayList<>();
            ExecutorService importPool = Executors.newFixedThreadPool(nCores);
            for (int i = 0; i < nCores; ++i){
                MappingWorker mw = new MappingWorker(waitingHandlerCLIImpl, peptideMapper, sequenceMatchingPreferences, br, writer, peptideMapping, searchParameters.getFlanking());
                importPool.submit(mw);
                workers.add(mw);
            };
            importPool.shutdown();
            if (!importPool.awaitTermination(TIMEOUT_DAYS, TimeUnit.DAYS)) {
                System.out.println("Analysis timed out (time out: " + TIMEOUT_DAYS + " days)");
            }
            for (MappingWorker mw : workers){
                if (mw.exception != null) throw mw.exception;
            }

            long diffTimeMapping = System.nanoTime() - startTimeMapping;
            System.out.println();
            System.out.println("Mapping " + lineCount + " peptides took " + (diffTimeMapping / 1e9) + " seconds");

        } catch (Exception e) {
            handleError(outputFileName, "Error: mapping went wrong", e);
        }
            
            
            
        // close everything
        try {
            writer.close();
            br.close();
        }
        catch (Exception e) {
            handleError(outputFileName, "Error: could not close files properly", e);
        }
    }
}
