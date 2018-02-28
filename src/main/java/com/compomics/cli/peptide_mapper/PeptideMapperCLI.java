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
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.compomics.util.experiment.identification.protein_inference.FastaMapper;

/**
 * Command line peptide mapping.
 *
 * @author Dominik Kopczynski
 */
public class PeptideMapperCLI {

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
            
            System.out.println("Additional options are:");
            System.out.println("\t-u [utilities-parameter-file]\tpeptide mapping");
            System.out.println("\t-f\tadd flanking amino acids to peptide in output");
            
            System.out.println();
            System.out.println("Default parameters:");
            System.out.println("\tindexing method:\t\tfm-index");
            System.out.println("\tframentation tolerance [Da]:\t0.02");

            System.exit(-1);
        }

        System.out.println("Start reading FASTA file");
        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        File fastaFile = new File(args[1]);
        boolean flanking = false;

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
                        
                    case "-u":  // use utilities parameter file
                        
                        IdentificationParameters identificationParameters = null;
                        try {
                            File parameterFile = new File(args[argPos + 1]);
                            identificationParameters = IdentificationParameters.getIdentificationParameters(parameterFile);
                        } catch (Exception e) {
                            System.err.println("Error: cound not open / parse parameter file");
                            e.printStackTrace();
                            System.exit(-1);
                        }

                        sequenceMatchingPreferences = identificationParameters.getSequenceMatchingParameters();
                        searchParameters = identificationParameters.getSearchParameters();
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

        System.out.println("Start indexing fasta file");
        long startTimeIndex = System.nanoTime();
        FastaMapper peptideMapper = null;
        try {
            peptideMapper = new FMIndex(fastaFile, null, waitingHandlerCLIImpl, true, peptideVariantsPreferences, searchParameters);
        } catch (IOException e) {
            System.err.println("Error: cound not index the fasta file");
            e.printStackTrace();
            System.exit(-1);
        }
        double diffTimeIndex = System.nanoTime() - startTimeIndex;
        System.out.println();
        System.out.println("Indexing took " + (diffTimeIndex / 1e9) + " seconds and consumes " + (((float) ((FMIndex) peptideMapper).getAllocatedBytes()) / 1e6) + " MB");

        if (args[0].equals("-p")) {
            ArrayList<String> peptides = new ArrayList<>();
            try {
                String line = "";
                BufferedReader br = new BufferedReader(new FileReader(args[2]));
                while ((line = br.readLine()) != null) {
                    if (!Pattern.matches("[a-zA-Z]+", line)) {
                        System.err.println("Error: invalid character in line '" + line + "'");
                        System.exit(-1);
                    }
                    peptides.add(line.toUpperCase());
                }
                br.close();
            } catch (Exception e) {
                System.err.println("Error: cound not open input list");
                e.printStackTrace();
                System.exit(-1);
            }
            waitingHandlerCLIImpl.setSecondaryProgressCounterIndeterminate(false);
            waitingHandlerCLIImpl.setMaxSecondaryProgressCounter(peptides.size());
            waitingHandlerCLIImpl.setSecondaryProgressCounter(0);
            ArrayList<PeptideProteinMapping> allPeptideProteinMappings = new ArrayList<>();

            // starting the mapping
            try {
                long startTimeMapping = System.nanoTime();
                for (int i = 0; i < peptides.size(); ++i) {
                    String peptide = peptides.get(i);
                    waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                    ArrayList<PeptideProteinMapping> peptideProteinMappings = peptideMapper.getProteinMapping(peptide, sequenceMatchingPreferences);
                    allPeptideProteinMappings.addAll(peptideProteinMappings);
                }
                long diffTimeMapping = System.nanoTime() - startTimeMapping;
                System.out.println();
                System.out.println("Mapping " + peptides.size() + " peptides took " + (diffTimeMapping / 1e9) + " seconds");
            } catch (Exception e) {
                System.err.println("Error: mapping went wrong");
                e.printStackTrace();
                System.exit(-1);
            }

            try {
                PrintWriter writer = new PrintWriter(args[3], "UTF-8");
                for (PeptideProteinMapping peptideProteinMapping : allPeptideProteinMappings) {
                    String peptide = peptideProteinMapping.getPeptideSequence();
                    String accession = peptideProteinMapping.getProteinAccession();
                    int startIndex = peptideProteinMapping.getIndex();
                    if (flanking){
                        int peptideLength = peptide.length();
                        String proteinSequence = ((FMIndex)peptideMapper).getSequence(accession);
                        
                        if (startIndex > 0) peptide = proteinSequence.charAt(startIndex - 1) + "." + peptide;
                        else peptide = "-" + peptide;
                        
                        if (startIndex + peptideLength + 1 < proteinSequence.length()) peptide = peptide + "." + proteinSequence.charAt(startIndex + peptideLength);
                        else peptide = peptide + "-";
                    }
                    ++startIndex; // + 1 because we start counting from one -_-
                    writer.println(peptide + "," + accession + "," + startIndex);
                }
                writer.close();
            } catch (Exception e) {
                System.err.println("Error: could not write into file '" + args[3] + "'");
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            ArrayList<Tag> tags = new ArrayList<>();
            ArrayList<Integer> tagIndexes = new ArrayList<>();
            try {
                String line = "";
                BufferedReader br = new BufferedReader(new FileReader(args[2]));
                while ((line = br.readLine()) != null) {
                    Tag tag = new Tag();
                    for (String part : line.split(",")) {

                        if (Pattern.matches("[a-zA-Z]+", part)) {
                            tag.addAminoAcidSequence(new AminoAcidSequence(part));
                        } else {
                            try {
                                double mass = Double.parseDouble(part);
                                tag.addMassGap(mass);
                            } catch (NumberFormatException e) {
                                System.err.println("Error: line contains no valid tag: '" + line + "'");
                                e.printStackTrace();
                                System.exit(-1);
                            }
                        }
                    }
                    tags.add(tag);
                }
            } catch (Exception e) {
                System.err.println("Error: cound not open input list");
                System.exit(-1);
            }

            waitingHandlerCLIImpl.setSecondaryProgressCounterIndeterminate(false);
            waitingHandlerCLIImpl.setMaxSecondaryProgressCounter(tags.size());
            waitingHandlerCLIImpl.setSecondaryProgressCounter(0);
            ArrayList<PeptideProteinMapping> allPeptideProteinMappings = new ArrayList<>();

            // starting the mapping
            try {
                // setting up modifications lists, only relevant for protein tree
                ArrayList<String> variableModifications = searchParameters.getModificationParameters().getVariableModifications();
                ArrayList<String> fixedModifications = searchParameters.getModificationParameters().getFixedModifications();

                long startTimeMapping = System.nanoTime();
                for (int i = 0; i < tags.size(); ++i) {
                    waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                    ArrayList<PeptideProteinMapping> peptideProteinMappings = peptideMapper.getProteinMapping(tags.get(i), sequenceMatchingPreferences);
                    allPeptideProteinMappings.addAll(peptideProteinMappings);
                    for (int j = 0; j < peptideProteinMappings.size(); ++j) {
                        tagIndexes.add(i);
                    }
                }
                long diffTimeMapping = System.nanoTime() - startTimeMapping;
                System.out.println();
                System.out.println("Mapping " + tags.size() + " tags took " + (diffTimeMapping / 1e9) + " seconds");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error: an unexpected error happened.");
                e.printStackTrace();
                System.exit(-1);
            }

            try {
                
                PrintWriter writer = new PrintWriter(args[3], "UTF-8");
                
                for (int i = 0; i < allPeptideProteinMappings.size(); ++i) {
                
                    PeptideProteinMapping peptideProteinMapping = allPeptideProteinMappings.get(i);
                    String peptide = peptideProteinMapping.getPeptideSequence();
                    String accession = peptideProteinMapping.getProteinAccession();
                    int startIndex = peptideProteinMapping.getIndex();
                    
                    for (TagComponent tagComponent : tags.get(tagIndexes.get(i)).getContent()) {
                        
                        if (tagComponent instanceof MassGap) {
                        
                            writer.print(tagComponent.getMass());
                        
                        } else if (tagComponent instanceof AminoAcidSequence) {
                        
                            writer.print(tagComponent.asSequence());
                        
                        } else {
                        
                            throw new UnsupportedOperationException("Tag component of class " + tagComponent.getClass().getName() + " not supported.");
                        
                        }
                        
                        writer.print(",");
                        
                    }
                    
                    writer.println(peptide + "," + accession + "," + startIndex);
                
                }
                
                writer.close();
            
            } catch (Exception e) {
                System.err.println("Error: could not write into file '" + args[3] + "'");
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
