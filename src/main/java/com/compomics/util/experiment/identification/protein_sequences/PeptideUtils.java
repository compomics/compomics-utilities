package com.compomics.util.experiment.identification.protein_sequences;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class groups functions that can be used to work with peptides.
 *
 * @author Marc Vaudel
 */
public class PeptideUtils {
    
    /**
     * Returns a boolean indicating whether the peptide matches a decoy sequence.
     * 
     * @param peptide the peptide
     * @param sequenceProvider a sequence provider.
     * 
     * @return a boolean indicating whether the peptide matches a decoy sequence
     */
    public static boolean isDecoy(Peptide peptide, SequenceProvider sequenceProvider) {
        
        return peptide.getProteinMapping().keySet().stream()
                .anyMatch(accession -> sequenceProvider.getDecoyAccessions().contains(accession));
        
    }
    
    /**
     * Returns the amino acids before the given peptide as a string in a map based on the peptide protein mapping.
     * 
     * @param peptide the peptide
     * @param nAa the number of amino acids to include
     * @param sequenceProvider the sequence provider
     * 
     * @return the amino acids before the given peptide as a string in a map based on the peptide protein mapping
     */
    public static Map<String, String[]> getAaBefore(Peptide peptide, int nAa, SequenceProvider sequenceProvider) {
        
        return peptide.getProteinMapping().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, 
                        entry -> Arrays.stream(entry.getValue())
                        .mapToObj(index -> sequenceProvider.getSubsequence(entry.getKey(), index - nAa - 1, index - 1))
                        .toArray(String[]::new), 
                        (a ,b) -> {throw new IllegalArgumentException("Duplicate key.");}));
        
    }
    
    /**
     * Returns the amino acids before the given peptide as a string in a map based on the peptide protein mapping.
     * 
     * @param peptide the peptide
     * @param nAa the number of amino acids to include
     * @param sequenceProvider the sequence provider
     * 
     * @return the amino acids before the given peptide as a string in a map based on the peptide protein mapping
     */
    public static Map<String, String[]> getAaAfter(Peptide peptide, int nAa, SequenceProvider sequenceProvider) {
                
        return peptide.getProteinMapping().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, 
                        entry -> Arrays.stream(entry.getValue())
                        .mapToObj(index -> sequenceProvider.getSubsequence(entry.getKey(), index + peptide.getSequence().length(), index + peptide.getSequence().length() + nAa))
                        .toArray(String[]::new), 
                        (a ,b) -> {throw new IllegalArgumentException("Duplicate key.");}));
        
    }

}
