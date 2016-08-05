/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.PeptideVariantsPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author dominik.kopczynski
 */
public class ProteinInferrer {
    public static void main(String[] args){
        if ((args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) || args.length < 4 || (!args[0].equals("-p") && !args[0].equals("-t"))){
            System.out.println("usage: ProteinInferrer -[p|t] input-fasta input-peptide/tag-csv output-csv");
            System.out.println();
            System.out.println("options are:");
            System.out.println("\t-p\tpeptide mapping");
            System.out.println("\t-t\tsequence tag mapping");
            System.out.println("\t-h\tprint this info");
            System.exit(-1);
        }
        
        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        File sequences = new File(args[1]);
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        try {
            sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);
        }
        catch (Exception e){
            System.out.println("cound open fasta file");
            System.exit(-1);
        }
        PeptideVariantsPreferences peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();
        SequenceMatchingPreferences sequenceMatchingPreferences = new SequenceMatchingPreferences();
        sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
        sequenceMatchingPreferences.setLimitX(0.25);
        

        long startTime = System.nanoTime();
        FMIndex fmIndex = new FMIndex(null, false, null, peptideVariantsPreferences);
        System.out.println("Indexing took " + (((float)(System.nanoTime() - startTime)) / 1e9) + " seconds and consumes " + (((float)fmIndex.getAllocatedBytes()) / 1e6) + " MB");
        
        if (args[0].equals("-p")){
            ArrayList<String> peptides = new ArrayList<String>();
            try {
                for (String line : Files.readAllLines(Paths.get(args[2]))) {
                    if (!Pattern.matches("[a-zA-Z]+", line)){
                        System.out.println("error: invalid character in line '" + line + "'");
                        System.exit(-1);
                    }
                    peptides.add(line.toUpperCase());
                    /*
                    for (String part : line.split("\\s+")) {
                        Integer i = Integer.valueOf(part);
                        numbers.add(i);
                    }*/
                }
            }
            catch(Exception e){
                System.out.println("cound open input list");
                System.exit(-1);
            }

            // starting the mapping
            startTime = System.nanoTime();
            ArrayList<PeptideProteinMapping> allPeptideProteinMappings = new ArrayList<PeptideProteinMapping>();
            for (String peptide : peptides){
                ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping(peptide, sequenceMatchingPreferences);
                allPeptideProteinMappings.addAll(peptideProteinMappings);
            }
            System.out.println("Mapping " + peptides.size() + " peptides took " + (((float)(System.nanoTime() - startTime)) / 1e6) + " milliseconds");

            try {
                PrintWriter writer = new PrintWriter(args[3], "UTF-8");
                for (PeptideProteinMapping peptideProteinMapping : allPeptideProteinMappings){
                    String peptide = peptideProteinMapping.getPeptideSequence();
                    String accession = peptideProteinMapping.getProteinAccession();
                    int startIndex = peptideProteinMapping.getIndex();
                    writer.println(peptide + "," + accession + "," + startIndex);
                }
                writer.close();
            }
            catch(Exception e){
                System.out.println("error: could not write into file '" + args[2] + "'");
                System.exit(-1);
            }
        }
        else {
            ArrayList<String> peptides = new ArrayList<String>();
            try {
                for (String line : Files.readAllLines(Paths.get(args[2]))) {
                    if (!Pattern.matches("[a-zA-Z]+", line)){
                        System.out.println("error: invalid character in line '" + line + "'");
                        System.exit(-1);
                    }
                    peptides.add(line.toUpperCase());
                    /*
                    for (String part : line.split("\\s+")) {
                        Integer i = Integer.valueOf(part);
                        numbers.add(i);
                    }*/
                }
            }
            catch(Exception e){
                System.out.println("cound open input list");
                System.exit(-1);
            }

            // starting the mapping
            startTime = System.nanoTime();
            ArrayList<PeptideProteinMapping> allPeptideProteinMappings = new ArrayList<PeptideProteinMapping>();
            for (String peptide : peptides){
                ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping(peptide, sequenceMatchingPreferences);
                allPeptideProteinMappings.addAll(peptideProteinMappings);
            }
            System.out.println("Mapping " + peptides.size() + " peptides took " + (((float)(System.nanoTime() - startTime)) / 1e6) + " milliseconds");

            try {
                PrintWriter writer = new PrintWriter(args[3], "UTF-8");
                for (PeptideProteinMapping peptideProteinMapping : allPeptideProteinMappings){
                    String peptide = peptideProteinMapping.getPeptideSequence();
                    String accession = peptideProteinMapping.getProteinAccession();
                    int startIndex = peptideProteinMapping.getIndex();
                    writer.println(peptide + "," + accession + "," + startIndex);
                }
                writer.close();
            }
            catch(Exception e){
                System.out.println("error: could not write into file '" + args[2] + "'");
                System.exit(-1);
            }
        }
    }
}
