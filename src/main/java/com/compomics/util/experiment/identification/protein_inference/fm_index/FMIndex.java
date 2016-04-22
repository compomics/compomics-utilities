/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory.ProteinIterator;
import com.compomics.util.waiting.Duration;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author dominik.kopczynski
 * @author Marc Vaudel
 */
public class FMIndex implements PeptideMapper {
    
    private int binarySearch(ArrayList<Integer> array, int key){
        int low = 0;
        int mid = 0;
        int high = array.size() - 1;
        while (low <= high){
            mid = (low + high) >> 1;
            if (array.get(mid) == key) break;
            else if (array.get(mid) <= key) low = mid + 1;
            else high = mid - 1;
        }
        if (mid > 0 && key < array.get(mid)) mid -= 1;
        return mid;
    }
    
    
    
    private int[] SA; // Suffix array
    private Wavelet occ;
    private int[] less;
    private int n; // length of indexed string
    private final int samplingShift = 3; // every 2^samplingShift Suffix array entry will be sampled
    private final int samplingMask = (1 << samplingShift) - 1;
    private final int sampling = 1 << samplingShift;
    ArrayList<Integer> boundaries;
    ArrayList<String> accessions;
    
    
    public FMIndex(){
        System.out.println("Efficient FM-Index creation started");
        SequenceFactory sf = SequenceFactory.getInstance(100000);
        
        StringBuilder TT = new StringBuilder();
        boundaries = new ArrayList<Integer>();
        accessions = new ArrayList<String>();
        boundaries.add(0);
        
        Duration d = new Duration();
        d.start();
        
        n = 0;
        int numProteins = 0;
        try {
            ProteinIterator pi = sf.getProteinIterator(false);
            while (pi.hasNext()){
                Protein currentProtein = pi.getNextProtein();
                int proteinLen = currentProtein.getLength();
                n += proteinLen;
                ++numProteins;
                boundaries.add(n + numProteins);
                accessions.add(currentProtein.getAccession());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
        d.end();
        System.out.println("t1 " + d.toString());
        d.start();
        
        n += Math.max(0, numProteins - 1); // delimiters between protein sequences
        n += 1; // sentinal
        
        byte[] T = new byte[n];
        T[n - 1] = '$'; // adding the sentinal
        int tmpN = 0;
        try {
            ProteinIterator pi = sf.getProteinIterator(false);
            while (pi.hasNext()){

                Protein currentProtein = pi.getNextProtein();
                int proteinLen = currentProtein.getLength();
                if (tmpN > 0) {
                    T[tmpN++] = '/'; // adding the delimiters
                }
                
                System.arraycopy(currentProtein.getSequence().getBytes(), 0, T, tmpN, proteinLen);
                tmpN += proteinLen;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
        d.end();
        System.out.println("t2 " + d.toString());
        d.start();
        
        SA = new int[n];
        Sais.suffixsort(T, SA, n);
        
        d.end();
        System.out.println("t3 " + d.toString());
        d.start();
        
        // create Burrows-Wheeler-Transform
        byte[] bwt = new byte[n];
        for (int i = 0; i < n; ++i) bwt[i] = (SA[i] != 0) ? T[SA[i] - 1] : T[n - 1];
        
        d.end();
        System.out.println("t4 " + d.toString());
        d.start();
        
        // Sampling Suffix array
        int[] sampledSA = new int[((n + 1) >> samplingShift) + 1];
        int sampledIndex = 0;
        for (int i = 0; i < n; i += sampling) sampledSA[sampledIndex++] = SA[i];
        SA = sampledSA;
        
        
        d.end();
        System.out.println("t5 " + d.toString());
        d.start();
        
        
        char[] sortedAas = new char[AminoAcid.getAminoAcids().length + 2];
        System.arraycopy(AminoAcid.getAminoAcids(), 0, sortedAas, 0, AminoAcid.getAminoAcids().length);
        sortedAas[AminoAcid.getAminoAcids().length] = '$';
        sortedAas[AminoAcid.getAminoAcids().length + 1] = '/';
        Arrays.sort(sortedAas);
        
        long[] alphabet = new long[]{0, 0};
        for (int i = 0; i < sortedAas.length; ++i){
            int shift = sortedAas[i] - (sortedAas[i] & 64);//((sortedAas[i] >> 6) << 6);//(sortedAas[i] & 63);
            alphabet[(int)(sortedAas[i] >> 6)] |= 1L << shift;
        }
        occ = new Wavelet(bwt, alphabet);
        less = occ.createLessTable();
        d.end();
        System.out.println("finished efficient FM-Index on " + numProteins + " in " + d.toString());
    }
    
    
    public ArrayList<String> createPeptideCombinations(String peptide, SequenceMatchingPreferences seqMatchPref, int[] numPositions){
        ArrayList<String> combinations = new ArrayList<String>();
        int numCombination = 1;
                    
        SequenceMatchingPreferences.MatchingType sequenceMatchingType = seqMatchPref.getSequenceMatchingType();
        if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.string){
            for (int i = 0; i < peptide.length(); ++i){
                combinations.add(peptide.substring(i, i + 1));
            }
        }
        else {
            double maxX = seqMatchPref.getLimitX();
            double countX = 0;
            for (int i = 0; i < peptide.length(); ++i) if (peptide.charAt(i) == 'X') ++countX;
            if (countX / (double)(peptide.length()) < maxX){
                if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.aminoAcid || sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids){
                    boolean indistinghuishable = sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids;
                    
                    for (int i = 0; i < peptide.length(); ++i){
                        if (AminoAcid.getAminoAcid(peptide.charAt(i)).iscombination()){
                            String chars = peptide.substring(i, i + 1);
                            char[] aaCombinations = AminoAcid.getAminoAcid(peptide.charAt(i)).getSubAminoAcids();
                            for (int j = 0; j < aaCombinations.length; ++j) chars += aaCombinations[j];
                            combinations.add(chars);
                            numCombination *= chars.length();
                            numPositions[0] += 1;
                        }
                        else if (indistinghuishable && (peptide.charAt(i) == 'I' || peptide.charAt(i) == 'L')){
                            String chars = peptide.substring(i, i + 1);
                            char[] aaCombinations = AminoAcid.getAminoAcid(peptide.charAt(i)).getCombinations();
                            for (int j = 0; j < aaCombinations.length; ++j) chars += aaCombinations[j];
                            switch (peptide.charAt(i)){
                                case 'I':
                                    chars += "L";
                                    break;
                                case 'L':
                                    chars += "I";
                                    break;
                            }
                            
                            combinations.add(chars);
                            numCombination *= chars.length();
                            numPositions[0] += 1;
                        }
                        else {
                            combinations.add(peptide.substring(i, i + 1));
                        }
                    }
                }
            }
            else {
                numPositions[1] = 0;
            }
        }
        return combinations;
    }
    
    
    private int getPos(int i){
        int t = 0;
        while (((i & samplingMask) != 0) && (i != 0)){
            int aa = occ.getCharacter(i);
            i = less[aa] + occ.getRank(i - 1, aa);
            ++t;
        }
        return SA[i >> samplingShift] + t;
    }
    
    
    @Override
    public HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptide, SequenceMatchingPreferences seqMatchPref){
        HashMap<String, HashMap<String, ArrayList<Integer>>> allMatches = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
        
        String pep_rev = new StringBuilder(peptide).reverse().toString();
        int p = peptide.length();
        int[] numPositions = new int[]{0, 1};
        ArrayList<String> combinations = createPeptideCombinations(pep_rev, seqMatchPref, numPositions);
        int k = numPositions[0];
        
        if (numPositions[1] > 0){
            ArrayList<ArrayList<HashMap<Long, MatrixContent>>> matrix = new ArrayList<ArrayList<HashMap<Long, MatrixContent>>>();
            for (int i = 0; i <= k; ++i){
                matrix.add(new ArrayList<HashMap<Long, MatrixContent>>());
                for (int j = 0; j <= p; ++j){
                    matrix.get(i).add(new HashMap<Long, MatrixContent>());
                }
            }
            matrix.get(0).get(0).put(0L, new MatrixContent(1, n - 1, '\0', -1, 0)); // L, R, char, traceback, last_index
            for (int i = 0; i <= k; ++i){
                ArrayList<HashMap<Long, MatrixContent>> row = matrix.get(i);
                for (int j = 0; j < p; ++j){
                    HashMap<Long, MatrixContent> cell = row.get(j);
                    for (Long key : cell.keySet()){
                        boolean first = true;
                        MatrixContent content = cell.get(key);
                        int L_old = content.L;
                        int R_old = content.R;
                        for (int l = 0; l < combinations.get(j).length(); ++l){
                            int aa = (int)combinations.get(j).charAt(l);
                            int L = less[aa] + occ.getRank(L_old - 1, aa);
                            int R = less[aa] + occ.getRank(R_old, aa) - 1;
                            
                            if (L <= R){
                                Long newKey = new Long(L * n + R);
                                if (first){
                                    row.get(j + 1).put(newKey, new MatrixContent(L, R, (char)aa, 0, key));
                                }
                                else if (i < k) {
                                    matrix.get(i + 1).get(j + 1).put(newKey, new MatrixContent(L, R, (char)aa, 1, key));
                                }
                                
                            }
                            first = false;
                        }
                        
                    }
                    
                }
                
            }
            
            
            // Traceback
            
            for (int i = 0; i <= k; ++i){
                
                for (Long key : matrix.get(i).get(p).keySet()){
                    int currentI = i;
                    int currentJ = p;
                    Long currentKey = key;
                    String currentPeptide = "";
                    while (true){
                        MatrixContent content = matrix.get(currentI).get(currentJ).get(currentKey);
                        if (content.traceback == -1) break;
                        currentI -= content.traceback;
                        currentJ -= 1;
                        currentKey = content.lastIndex;
                        currentPeptide += content.character;
                    }
                    MatrixContent contentLR = matrix.get(i).get(p).get(key);
                    
                    int L = contentLR.L;
                    int R = contentLR.R;
                    
                    HashMap<String, ArrayList<Integer>> matches = new HashMap<String, ArrayList<Integer>>();
                    for (int j = L; j <= R; ++j){
                        int pos = getPos(j);
                        int index = binarySearch(boundaries, pos);
                        String accession = accessions.get(index);
                        if (!matches.containsKey(accession)){
                            matches.put(accession, new ArrayList<Integer>());
                        }
                        matches.get(accession).add(pos - boundaries.get(index));
                    }
                    allMatches.put(currentPeptide, matches);
                    
                }
                
            }
        }
        return allMatches;
    }

    @Override
    public void emptyCache() {
        // No cache here
    }

    @Override
    public void close() throws IOException, SQLException {
        // No open connection here
    }

    @Override
    public HashMap<Peptide, HashMap<String, ArrayList<Integer>>> getProteinMapping(Tag tag, TagMatcher tagMatcher, SequenceMatchingPreferences sequenceMatchingPreferences, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
