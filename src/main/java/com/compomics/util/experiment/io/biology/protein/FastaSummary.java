package com.compomics.util.experiment.io.biology.protein;

import com.compomics.util.experiment.identification.utils.ProteinUtils;
import com.compomics.util.experiment.io.biology.protein.iterators.HeaderIterator;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.parameters.tools.UtilitiesUserParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import no.uib.jsparklines.renderers.util.Util;

/**
 * This class parses a fasta file and gathers summary statistics.
 *
 * @author Marc Vaudel
 */
public class FastaSummary {

    /**
     * The file this summary represents.
     */
    public final File fastaFile;
    /**
     * The species occurrence in the fasta file.
     */
    public final TreeMap<String, Integer> speciesOccurrence;
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
     * The last time the file was modified.
     */
    public final long lastModified;

    /**
     * Constructor.
     *
     * @param fastaFile the fasta file
     * @param speciesOccurrence the occurrence of every species
     * @param databaseType the occurrence of every database type
     * @param nSequences the number of sequences
     * @param nTarget the number of target sequences
     * @param lastModified the last time the file was modified
     */
    public FastaSummary(File fastaFile, TreeMap<String, Integer> speciesOccurrence, HashMap<ProteinDatabase, Integer> databaseType, int nSequences, int nTarget, long lastModified) {

        this.fastaFile = fastaFile;
        this.speciesOccurrence = speciesOccurrence;
        this.databaseType = databaseType;
        this.nSequences = nSequences;
        this.nTarget = nTarget;
        this.lastModified = lastModified;

    }

    /**
     * Gathers summary data on the fasta file content.
     *
     * @param fastaFile a fasta file
     * @param fastaParameters the parameters to use to parse the file
     * @param waitingHandler a handler to allow canceling the import and
     * displaying progress
     *
     * @return returns fasta parameters inferred from the file
     *
     * @throws IOException exception thrown if an error occurred while iterating
     * the file
     */
    public static FastaSummary getSummary(File fastaFile, FastaParameters fastaParameters, WaitingHandler waitingHandler) throws IOException {

        FastaSummary fastaSummary = null;

        try {

           fastaSummary = getSavedSummary(fastaFile);

        } catch (Exception e) {

            // ignore and overwrite corrupted file
        }

        if (fastaSummary == null) {

            fastaSummary = parseSummary(fastaFile, fastaParameters, waitingHandler);

            if (fastaSummary != null) {

                try {

                    saveSummary(fastaFile, fastaSummary);

                } catch (Exception e) {

                    // ignore
                }

            }

        }

        return fastaSummary;

    }

    /**
     * Returns the summary as saved in the user folder, null if not found.
     *
     * @param fastaFile the fasta file
     *
     * @return the summary as saved in the user folder
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the file
     */
    private static FastaSummary getSavedSummary(File fastaFile) throws IOException {

        File savedFile = getSummaryFile(fastaFile);

        if (savedFile.exists()) {

            JsonMarshaller marshaller = new JsonMarshaller();
            FastaSummary fastaSummary = (FastaSummary) marshaller.fromJson(FastaSummary.class, savedFile);

            return fastaSummary;

        }

        return null;
    }

    /**
     * Saves the summary in the user folder.
     *
     * @param fastaFile the fasta file
     * @param fastaSummary the summary
     *
     * @throws IOException exception thrown if an error occurred while writing
     * the file
     */
    private static void saveSummary(File fastaFile, FastaSummary fastaSummary) throws IOException {

        File destinationFile = getSummaryFile(fastaFile);
        File destinationFolder = destinationFile.getParentFile();

        if (!destinationFolder.exists()) {

            if (!destinationFolder.mkdir()) {

                return;

            }

        }

        JsonMarshaller marshaller = new JsonMarshaller();
        marshaller.saveObjectToJson(fastaSummary, destinationFile);

    }

    /**
     * Returns the file used to store the summary file in the user folder.
     *
     * @param fastaFile the fasta file
     *
     * @return the file used to store the summary file in the user folder
     */
    private static File getSummaryFile(File fastaFile) {

        UtilitiesUserParameters utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();

        File summaryFolder = utilitiesUserParameters.getDbSummaryFolder();

        int pathHash = fastaFile.hashCode();

        return new File(summaryFolder, pathHash + "");

    }

    /**
     * Gathers summary data on the fasta file content.
     *
     * @param fastaFile a fasta file
     * @param fastaParameters the parameters to use to parse the file
     * @param waitingHandler a handler to allow canceling the import and
     * displaying progress
     *
     * @return returns fasta parameters inferred from the file
     *
     * @throws IOException exception thrown if an error occurred while iterating
     * the file
     */
    private static FastaSummary parseSummary(File fastaFile, FastaParameters fastaParameters, WaitingHandler waitingHandler) throws IOException {

        long lastModified = fastaFile.lastModified();
        
        TreeMap<String, Integer> speciesOccurrence = new TreeMap<>();
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

        return new FastaSummary(fastaFile, speciesOccurrence, databaseType, nSequences, nTarget, lastModified);

    }
    
    /**
     * Returns a string with the different database types found.
     * 
     * @return a string with the different database types found
     */
    public String getTypeAsString() {
        
        if (databaseType.isEmpty()) {
            
            return "Unknown";
            
        } else if (databaseType.size() == 1) {
            
            return databaseType.keySet().stream().findAny().get().getFullName();
            
        }
        
        int sum = databaseType.values().stream().mapToInt(Integer::intValue).sum();
        
        return databaseType.entrySet().stream()
                .map(entry -> new SimpleEntry<>(entry.getKey(), ((double) entry.getValue()) / sum))
                .map(entry -> entry.getKey().getFullName() + " (" + Util.roundDouble(entry.getValue(), 1) + "%)")
                .collect(Collectors.joining(", "));
        
    }

}
