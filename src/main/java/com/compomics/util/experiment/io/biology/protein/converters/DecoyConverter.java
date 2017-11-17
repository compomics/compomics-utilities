package com.compomics.util.experiment.io.biology.protein.converters;

import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.experiment.io.biology.protein.FastaSummary;
import com.compomics.util.experiment.io.biology.protein.Header;
import com.compomics.util.experiment.io.biology.protein.ProteinDatabase;
import com.compomics.util.experiment.io.biology.protein.iterators.FastaIterator;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This class appends decoy sequences to the given fasta file.
 *
 * @author Marc Vaudel
 */
public class DecoyConverter {

    /**
     * The flag to append to the accessions of decoy proteins.
     */
    public static final String decoyFlag = "-REVERSED";

    /**
     * Appends decoy sequences to the provided fasta file.
     *
     * @param fastaIn the fasta file to read
     * @param fastaOut the fasta file to write
     * @param waitingHandler a handler to allow canceling the import and
     * displaying progress
     *
     * @throws IOException exception thrown whenever an error happened while
     * reading or writing a fasta file
     */
    public static void appendDecoySequences(File fastaIn, File fastaOut, WaitingHandler waitingHandler) throws IOException {

        FastaIterator fastaIterator = new FastaIterator(fastaIn);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fastaOut))) {

            Protein protein;
            while ((protein = fastaIterator.getNextProtein()) != null) {

                String accession = protein.getAccession();
                String sequence = protein.getSequence();

                Header header = fastaIterator.getLastHeader();
                String rawHeader = header.getRawHeader();

                bw.write(rawHeader);
                bw.newLine();
                bw.write(sequence);

                bw.newLine();
                bw.newLine();

                int accessionEndIndex = rawHeader.indexOf(accession) + accession.length();
                String part1 = rawHeader.substring(0, accessionEndIndex);
                String part2 = rawHeader.substring(accessionEndIndex);

                bw.write(part1);
                bw.write(decoyFlag);
                bw.write(part2);
                bw.newLine();

                char[] sequenceAsArray = protein.getSequence().toCharArray();

                for (int i = sequenceAsArray.length - 1; i >= 0; i--) {

                    char aa = sequenceAsArray[i];

                    bw.write(aa);

                }

                bw.newLine();
                bw.newLine();

                if (waitingHandler != null) {

                    if (waitingHandler.isRunCanceled()) {

                        return;

                    }

                    waitingHandler.increaseSecondaryProgressCounter();

                }

            }
        }
    }

    /**
     * Returns the fasta parameters of the target-decoy database based on the parameters of the target database.
     * 
     * @param targetParameters the parameters of the target database
     * 
     * @return the fasta parameters of the target-decoy database
     */
    public static FastaParameters getDecoyParameters(FastaParameters targetParameters) {
        
        FastaParameters decoyParameters = new FastaParameters();
        
        decoyParameters.setTargetDecoy(true);
        decoyParameters.setDecoyFlag(decoyFlag);
        decoyParameters.setDecoySuffix(true);
        decoyParameters.setName(targetParameters.getName() + " (target-decoy)");
        decoyParameters.setDescription(targetParameters.getDescription());
                
        return decoyParameters;
    }

    /**
     * Returns the fasta summary of the target-decoy database based on the summary of the target database.
     * 
     * @param newFastaFile the new fasta file
     * @param targetSummary the summary of the target database
     * 
     * @return the fasta summary of the target-decoy database
     */
    public static FastaSummary getDecoySummary(File newFastaFile, FastaSummary targetSummary) {
        
        TreeMap<String, Integer> speciesOccurrence = targetSummary.speciesOccurrence.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, 
                        entry -> 2 * entry.getValue(), 
                        (oldValue, newValue) -> oldValue + newValue,
                        TreeMap::new));
        
        HashMap<ProteinDatabase, Integer> dbOccurrence = targetSummary.databaseType.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, 
                        entry -> 2 * entry.getValue(), 
                        (oldValue, newValue) -> oldValue + newValue,
                        HashMap::new));
        
        int nSequences = 2 * targetSummary.nSequences;
        
        int nTarget = targetSummary.nTarget;
        
        return new FastaSummary(newFastaFile, speciesOccurrence, dbOccurrence, nSequences, nTarget, newFastaFile.lastModified());
        
    }
}
