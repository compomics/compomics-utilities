package com.compomics.util.experiment.identification.utils;

import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;

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
    
}
