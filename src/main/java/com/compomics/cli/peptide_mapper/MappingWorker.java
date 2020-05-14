/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.cli.peptide_mapper;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.identification.protein_inference.FastaMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.concurrent.locks.*;

/**
 *
 * @author dominik.kopczynski
 */
public class MappingWorker implements Runnable {
    WaitingHandlerCLIImpl waitingHandlerCLIImpl = null;
    FastaMapper peptideMapper = null;
    SequenceMatchingParameters sequenceMatchingPreferences = null;
    BufferedReader br = null;
    PrintWriter writer = null;
    int NUM_READS = 1000;
    boolean flanking = false;
    boolean peptideMapping = false;
    ReadWriteLock readWriteLock = null;
    

    public MappingWorker(WaitingHandlerCLIImpl waitingHandlerCLIImpl,
                  FastaMapper peptideMapper,
                  SequenceMatchingParameters sequenceMatchingPreferences,
                  BufferedReader br,
                  PrintWriter writer,
                  boolean peptideMapping,
                  boolean flanking,
                  ReadWriteLock readWriteLock
                  ){
        this.waitingHandlerCLIImpl = waitingHandlerCLIImpl;
        this.peptideMapper = peptideMapper;
        this.sequenceMatchingPreferences = sequenceMatchingPreferences;
        this.br = br;
        this.writer = writer;
        this.flanking = flanking;
        this.peptideMapping = peptideMapping;
        this.readWriteLock = readWriteLock;
    }
    
    
    
    public String flanking(PeptideProteinMapping peptideProteinMapping, FastaMapper peptideMapper){
        String peptide = peptideProteinMapping.getPeptideSequence();
        String accession = peptideProteinMapping.getProteinAccession();
        int peptideLength = peptide.length();
        
        
        char prefixChar = ((FMIndex)peptideMapper).prefixCharacter(accession, peptideProteinMapping.fmIndexPosition);
        if (prefixChar != FMIndex.DELIMITER){
            peptide = Character.toString(prefixChar) + "." + peptide;
        }
        else peptide = "-" + peptide;

        char suffixChar = ((FMIndex)peptideMapper).suffixCharacter(accession, peptideProteinMapping.fmIndexPosition, peptideLength + 1);
        if (suffixChar != FMIndex.DELIMITER){
            peptide += "." + Character.toString(suffixChar);
        
        }
        else peptide += "-";
        
        return peptide;
    }
    


    @Override
    public void run() {
            
        
        ArrayList<String> rows = new ArrayList<>();
        ArrayList<String> outputData = new ArrayList<>();
        

        while (true){
            rows.clear();
            outputData.clear();

            // readin input file batch wise
            try {
                String row = "";
                int i = 0;
                //synchronized(br){
                    while (i++ < NUM_READS && (row = br.readLine()) != null) {
                        rows.add(row);
                        waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                    }
                //}
                if (rows.size() == 0) break;

            } catch (Exception e) {
                System.err.println("Error: cound not open input list");
                System.exit(-1);
            }


            // map peptides sequences
            if (peptideMapping){

                for (String inputPeptide : rows){
                    for (int j = 0; j < inputPeptide.length(); ++j){
                        int c = (int)inputPeptide.charAt(j);
                        if (!(((int)'A' <= c && c <= (int)'Z') || ((int)'a' <= c && c <= (int)'z'))){
                            System.err.println("Error: invalid character in line '" + inputPeptide + "'");
                            System.exit(-1);
                        }
                    }


                    for (PeptideProteinMapping peptideProteinMapping : peptideMapper.getProteinMapping(inputPeptide.toUpperCase(), sequenceMatchingPreferences)) {
                        String peptide = peptideProteinMapping.getPeptideSequence();
                        String accession = peptideProteinMapping.getProteinAccession();
                        int startIndex = peptideProteinMapping.getIndex() + 1;
                        if (flanking) peptide = flanking(peptideProteinMapping, peptideMapper);
                        
                        outputData.add(peptide + "," + accession + "," + startIndex);
                    }
                }


            }
            else {
                for (String tagString : rows){

                    Tag tag = new Tag();
                    for (String part : tagString.split(",")) {

                        if (Pattern.matches("[a-zA-Z]+", part)) {
                            tag.addAminoAcidSequence(new AminoAcidSequence(part));
                        } else {
                            try {
                                double mass = Double.parseDouble(part);
                                tag.addMassGap(mass);
                            } catch (NumberFormatException e) {
                                System.err.println("Error: line contains no valid tag: '" + tagString + "'");
                                System.exit(-1);
                            }
                        }
                    }
                    

                    for (PeptideProteinMapping peptideProteinMapping : peptideMapper.getProteinMapping(tag, sequenceMatchingPreferences)){
                        String peptide = peptideProteinMapping.getPeptideSequence();
                        String accession = peptideProteinMapping.getProteinAccession();
                        int startIndex = peptideProteinMapping.getIndex() + 1;
                        if (flanking) peptide = flanking(peptideProteinMapping, peptideMapper);

                        outputData.add(tagString + "," + peptide + "," + accession + "," + startIndex);
                    }
                }
            }





            // write out processed batch
            try {
                //synchronized(br){
                    for (String output : outputData) writer.println(output);
                //}
            }
            catch (Exception e) {
                System.err.println("Error: could not write into file");
                System.exit(-1);
            }
        }
            
    }
}
