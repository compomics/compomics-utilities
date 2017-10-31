package com.compomics.util.experiment.identification.utils;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Convenience functions for the handling of proteins.
 *
 * @author Marc Vaudel
 */
public class ProteinUtils {

    /**
     * Returns a boolean indicating whether the given accession corresponds to a decoy sequence according to the given fasta parameters.
     * 
     * @param accession the protein accession
     * @param fastaParameters the fasta parameters
     * 
     * @return a boolean indicating whether the given accession corresponds to a decoy sequence
     */
    public static boolean isDecoy(String accession, FastaParameters fastaParameters) {
        
        String fastaFlag = fastaParameters.getDecoyFlag();
        
        if (fastaFlag == null || fastaFlag.length() > accession.length()) {
            
            return false;
            
        }
        
        String subString;
        
        if (fastaParameters.isDecoySuffix()) {
            
            subString = accession.substring(accession.length() - fastaFlag.length());
            
        } else {
            
            subString = accession.substring(0, fastaFlag.length());
            
        }
        
        return subString.equals(fastaFlag);
    }

    /**
     * Returns a boolean indicating whether the given accession corresponds to a decoy sequence according to the given sequence provider.
     * 
     * @param accession the protein accession
     * @param sequenceProvider the sequence provider
     * 
     * @return a boolean indicating whether the given accession corresponds to a decoy sequence
     */
    public static boolean isDecoy(String accession, SequenceProvider sequenceProvider) {
        
        return sequenceProvider.getDecoyAccessions().contains(accession);
        
    }

    /**
     * Returns the observable amino acids in the sequence when using the given
     * enzymes with the given maximal peptide length.
     *
     * @param sequence  the protein sequence
     * @param enzymes the enzymes to use
     * @param pepMaxLength the max peptide length
     *
     * @return the number of observable amino acids of the sequence
     */
    public static int[] getObservableAminoAcids(String sequence, ArrayList<Enzyme> enzymes, double pepMaxLength) {
        
        int lastCleavage = -1;
        
        int[] observableAas = new int[sequence.length()];
        
        for (int i = 0 ; i < sequence.length() - 1 ; i++) {
            
            char charati = sequence.charAt(i), charatiPlusOne = sequence.charAt(i + 1);
            
            if (enzymes.stream().anyMatch(enzyme -> enzyme.isCleavageSite(charati, charatiPlusOne))) {
                
                if (i - lastCleavage <= pepMaxLength) {
                    
                    for (int k = lastCleavage ; k < i ; k++) {
                        
                        observableAas[k] = 1;
                        
                    }
                }
                
                lastCleavage = i;
                
            }
        }
        
        if (sequence.length() - 1 - lastCleavage <= pepMaxLength) {
            
            for (int k = lastCleavage ; k < sequence.length() ; k++) {
                
                observableAas[k] = 1;
                
            }
        }
        
        return observableAas;
    }

    /**
     * Returns the number of observable amino acids in the sequence.
     *
     * @param sequence  the protein sequence
     * @param enzymes the enzymes to use
     * @param pepMaxLength the max peptide length
     *
     * @return the number of observable amino acids of the sequence
     */
    public static int getObservableLength(String sequence, ArrayList<Enzyme> enzymes, double pepMaxLength) {
        
        int[] observalbeAas = getObservableAminoAcids(sequence, enzymes, pepMaxLength);
        
        return Arrays.stream(observalbeAas).sum();
    }

    /**
     * Returns the number of cleavage sites.
     *
     * @param sequence  the protein sequence
     * @param enzymes the enzymes to use
     *
     * @return the number of possible peptides
     */
    public static int getNCleavageSites(String sequence, ArrayList<Enzyme> enzymes) {
        
        int nCleavageSites = 0;
        
        for (int i = 0; i < sequence.length() - 1; i++) {
            
            char charati = sequence.charAt(i), charatiPlusOne = sequence.charAt(i + 1);
            
            if (enzymes.stream().anyMatch(enzyme -> enzyme.isCleavageSite(charati, charatiPlusOne))) {
                
                nCleavageSites++;
            
            }
        }
        
        return nCleavageSites;
    }
    
}
