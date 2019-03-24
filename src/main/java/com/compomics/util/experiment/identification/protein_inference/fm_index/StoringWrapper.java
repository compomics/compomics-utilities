/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.io.File;
import java.util.*;

/**
 *
 * @author dominik.kopczynski
 */
public class StoringWrapper extends ExperimentObject {
    
    /**
     * Number of chunks of complete index.
     */
    private int indexParts = 0;
    /**
     * Sampled suffix array.
     */
    private ArrayList<int[]> suffixArraysPrimary = null;
    /**
     * Wavelet tree for storing the burrows wheeler transform.
     */
    private ArrayList<WaveletTree> occurrenceTablesPrimary = null;
    /**
     * Wavelet tree for storing the burrows wheeler transform reversed.
     */
    private ArrayList<WaveletTree> occurrenceTablesReversed = null;
    /**
     * Less table for doing an update step according to the LF step.
     */
    private ArrayList<int[]> lessTablesPrimary = null;
    /**
     * Less table for doing an update step according to the LF step reversed.
     */
    private ArrayList<int[]> lessTablesReversed = null;
    /**
     * Length of the indexed string (all concatenated protein sequences).
     */
    private ArrayList<Integer> indexStringLengths = null;
    /**
     * Storing the starting positions of the protein sequences.
     */
    private ArrayList<int[]> boundaries = null;
    /**
     * List of all accession IDs in the FASTA file.
     */
    private ArrayList<String[]> accessions = null;
    /**
     * The decoy accessions contained in this index.
     */
    private HashSet<String> decoyAccessions = null;
    /**
     * The accessions ending positions in the index, important for getSequences
     * function
     */
    private HashMap<String, AccessionMetaData> accessionMetaData = null;
    
    
    public int getIndexParts(){
        readDBMode();
        return indexParts;
    }
    
    
    public ArrayList<int[]> getSuffixArraysPrimary(){
        readDBMode();
        return suffixArraysPrimary;
    }
    
    
    public ArrayList<WaveletTree> getOccurrenceTablesPrimary(){
        readDBMode();
        return occurrenceTablesPrimary;
    }
    
    
    public ArrayList<WaveletTree> getOccurrenceTablesReversed(){
        readDBMode();
        return occurrenceTablesReversed;
    }
    
    
    public ArrayList<int[]> getLessTablesPrimary(){
        readDBMode();
        return lessTablesPrimary;
    }
    
    
    public ArrayList<int[]> getLessTablesReversed(){
        readDBMode();
        return lessTablesReversed;
    }
    
    public ArrayList<Integer> getIndexStringLengths(){
        readDBMode();
        return indexStringLengths;
    }
    
    
    public ArrayList<int[]> getBoundaries(){
        readDBMode();
        return boundaries;
    }
    
    
    public ArrayList<String[]> getAccessions(){
        readDBMode();
        return accessions;
    }
    
    
    public HashSet<String> getDecoyAccessions(){
        readDBMode();
        return decoyAccessions;
    }
    
    
    public HashMap<String, AccessionMetaData> getAccessionMetaData(){
        readDBMode();
        return accessionMetaData;
    }
    
    
    
    public void setIndexParts(int _indexParts){
        writeDBMode();
        indexParts = _indexParts;
    }
    
    
    public void setSuffixArraysPrimary(ArrayList<int[]> _suffixArraysPrimary){
        writeDBMode();
        suffixArraysPrimary = _suffixArraysPrimary;
    }
    
    
    public void setOccurrenceTablesPrimary(ArrayList<WaveletTree> _occurrenceTablesPrimary){
        writeDBMode();
        occurrenceTablesPrimary = _occurrenceTablesPrimary;
    }
    
    
    public void setOccurrenceTablesReversed(ArrayList<WaveletTree> _occurrenceTablesReversed){
        writeDBMode();
        occurrenceTablesReversed = _occurrenceTablesReversed;
    }
    
    
    public void setLessTablesPrimary(ArrayList<int[]> _lessTablesPrimary){
        writeDBMode();
        lessTablesPrimary = _lessTablesPrimary;
    }
    
    
    public void setLessTablesReversed(ArrayList<int[]> _lessTablesReversed){
        writeDBMode();
        lessTablesReversed = _lessTablesReversed;
    }
    
    public void setIndexStringLengths(ArrayList<Integer> _indexStringLengths){
        writeDBMode();
        indexStringLengths = _indexStringLengths;
    }
    
    
    public void setBoundaries(ArrayList<int[]> _boundaries){
        writeDBMode();
        boundaries = _boundaries;
    }
    
    
    public void setAccessions(ArrayList<String[]> _accessions){
        writeDBMode();
        accessions = _accessions;
    }
    
    
    public void setDecoyAccessions(HashSet<String> _decoyAccessions){
        writeDBMode();
        decoyAccessions = _decoyAccessions;
    }
    
    
    public void setAccessionMetaData(HashMap<String, AccessionMetaData> _accessionMetaData){
        writeDBMode();
        accessionMetaData = _accessionMetaData;
    }
    
    
    public static String getFileExtension(File file) {
        String extension = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                extension = name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) {
            extension = "";
        }

        return extension;

    }
}
