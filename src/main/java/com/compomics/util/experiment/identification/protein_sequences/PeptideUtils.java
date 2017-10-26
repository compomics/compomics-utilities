package com.compomics.util.experiment.identification.protein_sequences;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
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
        
        return peptide.getProteinMapping().navigableKeySet().stream()
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
    public static TreeMap<String, String[]> getAaBefore(Peptide peptide, int nAa, SequenceProvider sequenceProvider) {
        
        return peptide.getProteinMapping().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, 
                        entry -> Arrays.stream(entry.getValue())
                        .mapToObj(index -> sequenceProvider.getSubsequence(entry.getKey(), index - nAa - 1, index - 1))
                        .toArray(String[]::new), 
                        (a ,b) -> {throw new IllegalArgumentException("Duplicate key.");},
                        TreeMap::new));
        
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
    public static TreeMap<String, String[]> getAaAfter(Peptide peptide, int nAa, SequenceProvider sequenceProvider) {
                
        return peptide.getProteinMapping().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, 
                        entry -> Arrays.stream(entry.getValue())
                        .mapToObj(index -> sequenceProvider.getSubsequence(entry.getKey(), index + peptide.getSequence().length(), index + peptide.getSequence().length() + nAa))
                        .toArray(String[]::new), 
                        (a ,b) -> {throw new IllegalArgumentException("Duplicate key.");},
                        TreeMap::new));
        
    }

    /**
     * Returns the peptide modifications as a string.
     *
     * @param peptide the peptide
     * @param variable if true, only variable Modifications are shown, false return
     * only the fixed Modifications
     *
     * @return the peptide modifications as a string
     */
    public static String getPeptideModificationsAsString(Peptide peptide, boolean variable) {

        TreeMap<String, HashSet<Integer>> modMap = peptide.getModificationMatches().stream()
                .filter(modificationMatch -> modificationMatch.getVariable() == variable)
                .collect(Collectors.groupingBy(ModificationMatch::getModification, 
                        Collectors.mapping(ModificationMatch::getModificationSite, HashSet::new)));
        
        return modMap.entrySet().stream()
                .map(entry -> getModificationString(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(";"));
    }
    
    /**
     * Returns the modification and sites as string in the form modName(site1,site2).
     * 
     * @param modificationName the name of the modification
     * @param sites the modification sites
     * 
     * @return the modification and sites as string
     */
    private static String getModificationString(String modificationName, HashSet<Integer> sites) {
        
        String sitesString = sites.stream()
                .sorted()
                .map(site -> site.toString())
                .collect(Collectors.joining(","));
        
        StringBuilder sb = new StringBuilder(modificationName.length() + sitesString.length() + 2);
        
        sb.append(modificationName).append("(").append(sitesString).append(")");
        
        return sb.toString();
    }

}
