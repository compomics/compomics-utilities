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
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author dominik.kopczynski
 */
public class MappingWorker implements Runnable {
    public Iterator<String> peptidesIterator;
    public ArrayList<PeptideProteinMapping> allPeptideProteinMappings;
    public WaitingHandlerCLIImpl waitingHandlerCLIImpl;
    public FastaMapper peptideMapper;
    public SequenceMatchingParameters sequenceMatchingPreferences;

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


    @Override
    public void run() {
        while (true){
            String peptide = null;
            synchronized(waitingHandlerCLIImpl){
                if (peptidesIterator.hasNext()){
                    peptide = peptidesIterator.next();
                    waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                }
                else {
                    break;
                }
            }
            ArrayList<PeptideProteinMapping> peptideProteinMappings = peptideMapper.getProteinMapping(peptide, sequenceMatchingPreferences);

            synchronized(waitingHandlerCLIImpl){
                allPeptideProteinMappings.addAll(peptideProteinMappings);
            }

        }
    }
}
