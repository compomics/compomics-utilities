/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.cli.peptide_mapper;

import com.compomics.util.experiment.identification.protein_inference.FastaMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author dominik.kopczynski
 */
public class MappingWorker implements Runnable {
    public Iterator<String> peptidesIterator = null;
    public Iterator<Tag> tagsIterator = null;
    public ArrayList<PeptideProteinMapping> allPeptideProteinMappings = null;
    public WaitingHandlerCLIImpl waitingHandlerCLIImpl = null;
    public FastaMapper peptideMapper = null;
    public SequenceMatchingParameters sequenceMatchingPreferences = null;
    public Integer counter = 0;
    public ArrayList<Integer> tagIndexes;
    
    

    public MappingWorker(Iterator<String> peptidesIterator,
                  WaitingHandlerCLIImpl waitingHandlerCLIImpl,
                  ArrayList<PeptideProteinMapping> allPeptideProteinMappings,
                  FastaMapper peptideMapper,
                  SequenceMatchingParameters sequenceMatchingPreferences
                  ){
        this.peptidesIterator = peptidesIterator;
        this.allPeptideProteinMappings = allPeptideProteinMappings;
        this.waitingHandlerCLIImpl = waitingHandlerCLIImpl;
        this.peptideMapper = peptideMapper;
        this.sequenceMatchingPreferences = sequenceMatchingPreferences;
    }
    
    
    public MappingWorker(Iterator<Tag> tagsIterator,
                  WaitingHandlerCLIImpl waitingHandlerCLIImpl,
                  ArrayList<PeptideProteinMapping> allPeptideProteinMappings,
                  FastaMapper peptideMapper,
                  SequenceMatchingParameters sequenceMatchingPreferences,
                  Integer counter,
                  ArrayList<Integer> tagIndexes
                  ){
        this.tagsIterator = tagsIterator;
        this.allPeptideProteinMappings = allPeptideProteinMappings;
        this.waitingHandlerCLIImpl = waitingHandlerCLIImpl;
        this.peptideMapper = peptideMapper;
        this.sequenceMatchingPreferences = sequenceMatchingPreferences;
        this.counter = counter;
        this.tagIndexes = tagIndexes;
    }
    
    
    


    @Override
    public void run() {
        if (peptidesIterator != null){
            ArrayList<String> peptides = new ArrayList<>();
            ArrayList<PeptideProteinMapping> peptideProteinMappings = new ArrayList<>();
            int n = 100;
            
            while (true){
                peptides.clear();
                peptideProteinMappings.clear();
                synchronized(waitingHandlerCLIImpl){
                    int i = 0;
                    while (peptidesIterator.hasNext() && i++ < n){
                        peptides.add(peptidesIterator.next());
                        waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                    }
                    if (peptides.size() == 0){
                        break;
                    }
                }
                
                for (String peptide : peptides){
                    peptideProteinMappings.addAll(peptideMapper.getProteinMapping(peptide, sequenceMatchingPreferences));
                }
                synchronized(allPeptideProteinMappings){
                    allPeptideProteinMappings.addAll(peptideProteinMappings);
                }

            }
        }
        
        else if (tagsIterator != null){
            ArrayList<Tag> tags = new ArrayList<>();
            ArrayList<Integer> tmpTagIndexes = new ArrayList<>();
            ArrayList<PeptideProteinMapping> tmpPeptideProteinMappings = new ArrayList<>();
            int n = 100;
            while (true){
                tags.clear();
                tmpTagIndexes.clear();
                tmpPeptideProteinMappings.clear();
                int cnt = 0;
                synchronized(tagsIterator){
                    int i = 0;
                    while (tagsIterator.hasNext() && i < n){
                        tags.add(tagsIterator.next());
                        waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                        if (i == 0) cnt = counter;
                        counter++;
                        i++;
                    }
                    if (tags.size() == 0) break;
                }
                
                for (Tag tag : tags){
                    ArrayList<PeptideProteinMapping> peptideProteinMappings = peptideMapper.getProteinMapping(tag, sequenceMatchingPreferences);
                    for (int j = 0; j < peptideProteinMappings.size(); ++j) {
                        tmpTagIndexes.add(cnt);
                    }
                    tmpPeptideProteinMappings.addAll(peptideProteinMappings);
                    cnt++;
                }
                
                synchronized(allPeptideProteinMappings){
                    allPeptideProteinMappings.addAll(tmpPeptideProteinMappings);
                    tagIndexes.addAll(tmpTagIndexes);
                }

            }
        }
    }
}
