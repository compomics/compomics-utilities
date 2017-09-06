package com.compomics.util.experiment.io.biology.protein;

import com.compomics.util.experiment.identification.protein_sequences.ProteinUtils;
import com.compomics.util.experiment.io.biology.protein.iterators.HeaderIterator;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * This class parses a fasta file and gathers summary statistics.
 *
 * @author Marc Vaudel
 */
public class FastaSummary {
    
    /**
     * The species occurrence in the fasta file.
     */
    public final HashMap<String, Integer> speciesOccurrence;
    /**
     * The database type occurrence in the fasta file.
     */
    public final HashMap<ProteinDatabase, Integer> databaseType;
    /**
     * The number of sequences.
     */
    public final int nSequences;
    /**
     * The number of target sequences.
     */
    public final int nTarget;
    
    /**
     * Constructor.
     * 
     * @param speciesOccurrence the occurrence of every species
     * @param databaseType the occurrence of every database type
     * @param nSequences the number of sequences
     * @param nTarget the number of target sequences
     */
    public FastaSummary(HashMap<String, Integer> speciesOccurrence, HashMap<ProteinDatabase, Integer> databaseType, int nSequences, int nTarget) {
        
        this.speciesOccurrence = speciesOccurrence;
        this.databaseType = databaseType;
        this.nSequences = nSequences;
        this.nTarget = nTarget;
        
    }

    /**
     * Gathers summary data on the fasta file content.
     * 
     * @param fastaFile a fasta file
     * @param fastaParameters the parameters to use to parse the file
     * @param waitingHandler a handler to allow canceling the import
     * 
     * @return returns fasta parameters inferred from the file
     * 
     * @throws IOException exception thrown if an error occurred while iterating the file
     */
    public static FastaSummary getSummary(File fastaFile, FastaParameters fastaParameters, WaitingHandler waitingHandler) throws IOException {

        HashMap<String, Integer> speciesOccurrence = new HashMap<>(1);
        HashMap<ProteinDatabase, Integer> databaseType = new HashMap<>(1);
        int nSequences = 0;
        int nTarget = 0;

        HeaderIterator headerIterator = new HeaderIterator(fastaFile);
        String fastaHeader;

        while ((fastaHeader = headerIterator.getNextHeader()) != null) {

            Header header = Header.parseFromFASTA(fastaHeader);

            String species = header.getTaxonomy();

            Integer occurrence = speciesOccurrence.get(species);

            if (occurrence == null) {

                speciesOccurrence.put(species, 1);

            } else {

                speciesOccurrence.put(species, occurrence + 1);

            }

            ProteinDatabase proteinDatabase = header.getDatabaseType();

            occurrence = databaseType.get(proteinDatabase);

            if (occurrence == null) {

                databaseType.put(proteinDatabase, 1);

            } else {

                databaseType.put(proteinDatabase, occurrence + 1);

            }

            String accession = header.getAccession();
            
            if (!ProteinUtils.isDecoy(accession, fastaParameters)) {
                
                nTarget++;
                
            }

            nSequences++;
            
            if (waitingHandler != null) {
                
                if (waitingHandler.isRunCanceled()) {
                    
                    return null;
                    
                }
                
                waitingHandler.increaseSecondaryProgressCounter();
                
            }
        }
        
        return new FastaSummary(speciesOccurrence, databaseType, nSequences, nTarget);

    }
    
}
