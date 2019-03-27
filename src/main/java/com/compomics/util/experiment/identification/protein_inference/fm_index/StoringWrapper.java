/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.fm_index;

import java.io.File;
import java.util.*;

/**
 *
 * @author dominik.kopczynski
 */
public class StoringWrapper {
    
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
        /*
        ArrayList<int[]> sc = new ArrayList<>();
        for (int[] sa : suffixArraysPrimary){
            int[] suffixArray = new int[sa.length];
            for (int i = 0; i < sa.length; ++i) suffixArray[i] = sa[i];
            sc.add(suffixArray);
        }
        return sc;
        */
    }
    
    
    public ArrayList<WaveletTree> getOccurrenceTablesPrimary(){
        readDBMode();
        
        return occurrenceTablesPrimary;
        /*
        ArrayList<WaveletTree> oc = new ArrayList<WaveletTree>();
        for (WaveletTree wt : occurrenceTablesPrimary){
            oc.add(new WaveletTree(wt));
        }
        return oc;
        */
    }
    
    
    public ArrayList<WaveletTree> getOccurrenceTablesReversed(){
        readDBMode();
        
        return occurrenceTablesReversed;
        /*
        ArrayList<WaveletTree> oc = new ArrayList<>();
        for (WaveletTree wt : occurrenceTablesReversed){
            oc.add(new WaveletTree(wt));
        }
        return oc;
        */
    }
    
    
    public ArrayList<int[]> getLessTablesPrimary(){
        readDBMode();
        
        return lessTablesPrimary;
        /*
        ArrayList<int[]> lt = new ArrayList<>();
        for (int[] ltp : lessTablesPrimary){
            int[] less = new int[ltp.length];
            for (int i = 0; i < ltp.length; ++i) less[i] = ltp[i];
            lt.add(less);
        }
        return lt;
        */
    }
    
    
    public ArrayList<int[]> getLessTablesReversed(){
        readDBMode();
        
        return lessTablesReversed;
        /*
        ArrayList<int[]> lt = new ArrayList<>();
        for (int[] ltr : lessTablesReversed){
            int[] less = new int[ltr.length];
            for (int i = 0; i < ltr.length; ++i) less[i] = ltr[i];
            lt.add(less);
        }
        return lt;
        */
    }
    
    public ArrayList<Integer> getIndexStringLengths(){
        readDBMode();
        
        return indexStringLengths;
        /*
        ArrayList<Integer> isl = new ArrayList<>();
        for (int i : indexStringLengths){
            isl.add(i);
        }
        return isl;
        */
    }
    
    
    public ArrayList<int[]> getBoundaries(){
        readDBMode();
        
        return boundaries;
        /*
        ArrayList<int[]> gb = new ArrayList<>();
        for (int[] bd : boundaries){
            int[] bound = new int[bd.length];
            for (int i = 0; i < bd.length; ++i) bound[i] = bd[i];
            gb.add(bound);
        }
        return gb;
        */
    }
    
    
    public ArrayList<String[]> getAccessions(){
        readDBMode();
        
        return accessions;
        /*
        ArrayList<String[]> a = new ArrayList<>();
        for (String[] accs : accessions){
            String[] accessions = new String[accs.length];
            for (int i = 0; i < accs.length; ++i) accessions[i] = accs[i];
            a.add(accessions);
        }
        return accessions;
        */
    }
    
    
    public HashSet<String> getDecoyAccessions(){
        readDBMode();
        
        return decoyAccessions;
        /*
        HashSet<String> da = new HashSet<>();
        for (String s : decoyAccessions){
            da.add(s);
        }
        return da;
        */
    }
    
    
    public HashMap<String, AccessionMetaData> getAccessionMetaData(){
        readDBMode();
        
        return accessionMetaData;
        /*
        HashMap<String, AccessionMetaData> amd = new HashMap<>();
        for (String key : accessionMetaData.keySet()){
            amd.put(key, new AccessionMetaData(accessionMetaData.get(key)));
        }
        return amd;
        */
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
