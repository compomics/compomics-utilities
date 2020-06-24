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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import com.compomics.util.experiment.identification.utils.PeptideUtils;
import com.compomics.util.parameters.identification.IdentificationParameters;

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
    public Exception exception = null;
    

    public MappingWorker(WaitingHandlerCLIImpl waitingHandlerCLIImpl,
                  FastaMapper peptideMapper,
                  IdentificationParameters identificationParameters,
                  BufferedReader br,
                  PrintWriter writer,
                  boolean peptideMapping
                  ){
        this.waitingHandlerCLIImpl = waitingHandlerCLIImpl;
        this.peptideMapper = peptideMapper;
        this.sequenceMatchingPreferences = identificationParameters.getSequenceMatchingParameters();
        this.br = br;
        this.writer = writer;
        this.flanking = identificationParameters.getSearchParameters().getFlanking();
        this.peptideMapping = peptideMapping;
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
        HashSet<String> outputData = new HashSet<>();
        

        while (true){
            rows.clear();
            outputData.clear();

            // readin input file batch wise
            try {
                String row = "";
                int i = 0;
                //synchronized(br){
                    while (!waitingHandlerCLIImpl.isRunCanceled() && i++ < NUM_READS && (row = br.readLine()) != null) {
                        rows.add(row);
                    }
                //}
                if (waitingHandlerCLIImpl.isRunCanceled() || rows.isEmpty()) break;

            } catch (Exception e) {
                waitingHandlerCLIImpl.setRunCanceled();
                exception = new IOException("Error: cound not open input list.\n\n" + e);
                return;
            }
            

            // map peptides sequences
            if (peptideMapping){
                for (String inputPeptide : rows){
                    if (waitingHandlerCLIImpl.isRunCanceled()) break;
                    for (char c : inputPeptide.toCharArray()){
                        if (!(((int)'A' <= c && c <= (int)'Z') || ((int)'a' <= c && c <= (int)'z'))){
                            waitingHandlerCLIImpl.setRunCanceled();
                            exception = new RuntimeException("Error: invalid character in line '" + inputPeptide + "' -> '" + (char)c + "'.");
                            return;
                        }
                    }

                    try {
                        for (PeptideProteinMapping peptideProteinMapping : peptideMapper.getProteinMapping(inputPeptide.toUpperCase(), sequenceMatchingPreferences)) {
                            String peptide = peptideProteinMapping.getPeptideSequence();
                            
                            String accession = peptideProteinMapping.getProteinAccession();
                            int startIndex = peptideProteinMapping.getIndex() + 1;
                            if (flanking) peptide = flanking(peptideProteinMapping, peptideMapper);
                            
                            outputData.add(peptide + "," + accession + "," + startIndex);
                        }
                        waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                    }
                    
                    catch (Exception e){
                        exception = new RuntimeException("An error occurred during the mapping of '" + inputPeptide + "'\n\n" + e);
                        waitingHandlerCLIImpl.setRunCanceled();
                    }
                }


            }
            else {
                for (String tagString : rows){
                    if (waitingHandlerCLIImpl.isRunCanceled()) break;

                    Tag tag = new Tag();
                    for (String part : tagString.split(",")) {

                        if (Pattern.matches("[a-zA-Z]+", part)) {
                            tag.addAminoAcidSequence(new AminoAcidSequence(part));
                        } else {
                            try {
                                double mass = Double.parseDouble(part);
                                tag.addMassGap(mass);
                            } catch (NumberFormatException e) {
                                waitingHandlerCLIImpl.setRunCanceled();
                                exception = new RuntimeException("Error: line contains no valid tag: '" + tagString + "'.\n\n" + e);
                                return;
                            }
                        }
                    }
                    
                    try {
                        for (PeptideProteinMapping peptideProteinMapping : peptideMapper.getProteinMapping(tag, sequenceMatchingPreferences)){
                            String peptide = peptideProteinMapping.getPeptideSequence();
                            
                            String accession = peptideProteinMapping.getProteinAccession();
                            int startIndex = peptideProteinMapping.getIndex() + 1;
                            if (flanking) peptide = flanking(peptideProteinMapping, peptideMapper);
                            
                            outputData.add(peptide + "," + accession + "," + startIndex + "," + PeptideUtils.getVariableModificationsAsString(peptideProteinMapping.getVariableModifications()));
                        }
                        waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                    }
                    catch (Exception e){
                        exception = new RuntimeException("An error occurred during the mapping of '" + tagString + "'\n\n" + e);
                        waitingHandlerCLIImpl.setRunCanceled();
                    }
                }
            }





            // write out processed batch
            try {
                //synchronized(br){
                    for (String output : outputData){
                    if (waitingHandlerCLIImpl.isRunCanceled()) break;
                        writer.println(output);
                    }
                //}
            }
            catch (Exception e) {
                exception = new IOException("Error: could not write into file.\n\n" + e);
                waitingHandlerCLIImpl.setRunCanceled();
                return;
            }
        }
    }
}
