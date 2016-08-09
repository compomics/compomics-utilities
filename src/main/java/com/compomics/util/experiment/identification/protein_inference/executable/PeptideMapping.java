/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.executable;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.MassGap;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.IdentificationParameters;
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
public class PeptideMapping {
    public static void main(String[] args){
        if ((args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) || args.length < 4 || (!args[0].equals("-p") && !args[0].equals("-t"))){
            System.err.println("PeptideMapping: a tool to map peptides or sequence tags against a given proteome.");
            System.err.println("usage: PeptideMapping -[p|t] input-fasta input-peptide/tag-csv output-csv [utilities-parameter-filewwwwwwaeaaeaeanrt]");
            System.err.println();
            System.err.println("Options are:");
            System.err.println("\t-p\tpeptide mapping");
            System.err.println("\t-t\tsequence tag mapping");
            System.err.println("\t-h\tprint this info");
            System.err.println();
            System.err.println("Default parameters:");
            System.err.println("\tindexing method:\t\tfm-index");
            System.err.println("\tframentation tolerance [Da]:\t0.02");
            
            System.exit(-1);
        }
        
        System.err.println("Start reading fasta file");
        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        File sequences = new File(args[1]);
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        try {
            sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);
        }
        catch (Exception e){
            System.err.println("Error: cound not open fasta file");
            System.exit(-1);
        }
        
        double tolerance = 0.02;
        PtmSettings ptmSettings = null;
        PeptideVariantsPreferences peptideVariantsPreferences = null;
        SequenceMatchingPreferences sequenceMatchingPreferences = null;
        if (args.length >= 5){
            File parameterFile = new File(args[4]);
            IdentificationParameters identificationParameters = null;
            try {
                identificationParameters = IdentificationParameters.getIdentificationParameters(parameterFile);
            } catch (Exception e){
                System.err.println("Error: cound not open / parse parameter file");
                System.exit(-1);
            }

            tolerance = identificationParameters.getSearchParameters().getFragmentIonAccuracy();
            
            ptmSettings = identificationParameters.getSearchParameters().getPtmSettings();
            peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();
            sequenceMatchingPreferences = identificationParameters.getSequenceMatchingPreferences();

        }
            else {
            ptmSettings = new PtmSettings();
            peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();
            sequenceMatchingPreferences = new SequenceMatchingPreferences();
            sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
            sequenceMatchingPreferences.setLimitX(0.25);

        }
        
        
        System.err.println("Start indexing proteome");
        long startTime = System.nanoTime();
        FMIndex fmIndex = new FMIndex(waitingHandlerCLIImpl, true, ptmSettings, peptideVariantsPreferences);
        double diffTime = System.nanoTime() - startTime;
        System.err.println();
        System.err.println("Indexing took " + (diffTime / 1e9) + " seconds and consumes " + (((float)fmIndex.getAllocatedBytes()) / 1e6) + " MB");
        
        if (args[0].equals("-p")){
            ArrayList<String> peptides = new ArrayList<String>();
            try {
                for (String line : Files.readAllLines(Paths.get(args[2]))) {
                    if (!Pattern.matches("[a-zA-Z]+", line)){
                        System.err.println("Error: invalid character in line '" + line + "'");
                        System.exit(-1);
                    }
                    peptides.add(line.toUpperCase());
                }
            }
            catch(Exception e){
                System.err.println("Error: cound not open input list");
                System.exit(-1);
            }
            waitingHandlerCLIImpl.setSecondaryProgressCounterIndeterminate(false);
            waitingHandlerCLIImpl.setMaxSecondaryProgressCounter(peptides.size());
            waitingHandlerCLIImpl.setSecondaryProgressCounter(0);
            ArrayList<PeptideProteinMapping> allPeptideProteinMappings = new ArrayList<PeptideProteinMapping>();

            // starting the mapping
            startTime = System.nanoTime();
            
            for (int i = 0; i < peptides.size(); ++i){
                String peptide = peptides.get(i);
                waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping(peptide, sequenceMatchingPreferences);
                allPeptideProteinMappings.addAll(peptideProteinMappings);
            }
            diffTime = System.nanoTime() - startTime;
            System.err.println();
            System.err.println("Mapping " + peptides.size() + " peptides took " + (diffTime / 1e6) + " milliseconds");

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
                System.err.println("Error: could not write into file '" + args[3] + "'");
                System.exit(-1);
            }
        }
        else {
            ArrayList<Tag> tags = new ArrayList<Tag>();
            ArrayList<Integer> tagIndexes = new ArrayList<Integer>();
            try {
                for (String line : Files.readAllLines(Paths.get(args[2]))) {
                    Tag tag = new Tag();
                    for (String part : line.split(",")) {
                    
                        if (Pattern.matches("[a-zA-Z]+", part)){
                            tag.addAminoAcidSequence(new AminoAcidSequence(part));
                        }
                        else {
                            try {
                                double mass = Double.parseDouble(part);
                                tag.addMassGap(mass);
                            } catch (NumberFormatException e) {
                                System.err.println("Error: line contains no valid tag: '" + line + "'");
                                System.exit(-1);
                            }
                        }
                    }
                    tags.add(tag);
                }
            }
            catch(Exception e){
                System.err.println("Error: cound not open input list");
                System.exit(-1);
            }
            
            waitingHandlerCLIImpl.setSecondaryProgressCounterIndeterminate(false);
            waitingHandlerCLIImpl.setMaxSecondaryProgressCounter(tags.size());
            waitingHandlerCLIImpl.setSecondaryProgressCounter(0);
            ArrayList<PeptideProteinMapping> allPeptideProteinMappings = new ArrayList<PeptideProteinMapping>();

            // starting the mapping
            startTime = System.nanoTime();
            try {
                for (int i = 0; i < tags.size(); ++i){
                    waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                    ArrayList<PeptideProteinMapping> peptideProteinMappings = fmIndex.getProteinMapping(tags.get(i), null, sequenceMatchingPreferences, tolerance);
                    allPeptideProteinMappings.addAll(peptideProteinMappings);
                    for(int j = 0; j < peptideProteinMappings.size(); ++j) tagIndexes.add(i);
                }
            } catch (Exception e){
                e.printStackTrace();
                System.err.println("Error: an unexpected error happened.");
                System.exit(-1);
            }
            diffTime = System.nanoTime() - startTime;
            System.err.println();
            System.err.println("Mapping " + tags.size() + " tags took " + (diffTime / 1e6) + " milliseconds");

            try {
                PrintWriter writer = new PrintWriter(args[3], "UTF-8");
                for (int i = 0; i < allPeptideProteinMappings.size(); ++i){
                    PeptideProteinMapping peptideProteinMapping = allPeptideProteinMappings.get(i);
                    String peptide = peptideProteinMapping.getPeptideSequence();
                    String accession = peptideProteinMapping.getProteinAccession();
                    int startIndex = peptideProteinMapping.getIndex();
                    for (TagComponent tagComponent : tags.get(tagIndexes.get(i)).getContent()){
                        if (tagComponent instanceof MassGap){
                            writer.print(tagComponent.getMass());
                        }
                        if (tagComponent instanceof AminoAcidSequence){
                            writer.print(tagComponent.asSequence());
                        }
                        writer.print(",");
                    }
                    writer.println(peptide + "," + accession + "," + startIndex);
                }
                writer.close();
            }
            catch(Exception e){
                System.err.println("Error: could not write into file '" + args[3] + "'");
                System.exit(-1);
            }
        }
    }
}
