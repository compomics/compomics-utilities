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
 * This class appends decoy sequences to the given FASTA file.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class DecoyConverter {

    /**
     * Empty default constructor.
     */
    public DecoyConverter() {
    }

    /**
     * Appends decoy sequences to the provided FASTA file.
     *
     * @param fastaIn the FASTA file to read
     * @param fastaOut the FASTA file to write
     * @param fastaParameters the FASTA parameters
     * @param waitingHandler a handler to allow canceling the import and
     * displaying progress
     *
     * @throws IOException exception thrown whenever an error happened while
     * reading or writing a FASTA file
     */
    public static void appendDecoySequences(File fastaIn, File fastaOut, FastaParameters fastaParameters, WaitingHandler waitingHandler) throws IOException {

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
                
                String part0 = rawHeader.substring(0, rawHeader.indexOf(accession));
                String part1 = rawHeader.substring(rawHeader.indexOf(accession), accessionEndIndex);
                String part2 = rawHeader.substring(accessionEndIndex);

                bw.write(part0);
                if (!fastaParameters.isDecoySuffix())
                    bw.write(fastaParameters.getDecoyFlag());
                bw.write(part1);
                if (fastaParameters.isDecoySuffix())
                    bw.write(fastaParameters.getDecoyFlag());
                bw.write(part2);
                bw.write(fastaParameters.getDecoyFlag());
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
     * Returns the FASTA parameters of the target-decoy database based on the
     * parameters of the target database.
     *
     * @param targetParameters the parameters of the target database
     *
     * @return the FASTA parameters of the target-decoy database
     */
    public static FastaParameters getDecoyParameters(FastaParameters targetParameters) {

        FastaParameters decoyParameters = new FastaParameters();
        decoyParameters.setTargetDecoy(true);
        decoyParameters.setDecoyFlag(targetParameters.getDecoyFlag());
        decoyParameters.setDecoySuffix(true);

        return decoyParameters;
    }

    /**
     * Returns the FASTA summary of the target-decoy database based on the
     * summary of the target database.
     *
     * @param newFastaFile the new FASTA file
     * @param targetSummary the summary of the target database
     *
     * @return the FASTA summary of the target-decoy database
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

        return new FastaSummary(targetSummary.getName() + " (target-decoy)", 
                targetSummary.getDescription(), targetSummary.getVersion(), 
                newFastaFile, speciesOccurrence, dbOccurrence, nSequences, 
                nTarget, newFastaFile.lastModified());
        
    }
}
