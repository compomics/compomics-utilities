package com.compomics.util.experiment.identification.protein_inference;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.utils.ProteinUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.threading.ObjectMutex;
import com.compomics.util.threading.SimpleSemaphore;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class builds peptides and proteins based on PSMs. Note that the builder
 * is buffered and needs to be closed.
 *
 * @author Marc Vaudel
 */
public class PeptideAndProteinBuilder implements AutoCloseable {

    /**
     * Empty default constructor
     */
    public PeptideAndProteinBuilder() {
        identification = null;
    }

    /**
     * The identification object.
     */
    private final Identification identification;
    /**
     * An object mutex.
     */
    private final ObjectMutex objectMutex = new ObjectMutex();
    /**
     * The size of the buffers.
     */
    private final int bufferSize = 1024;
    /**
     * The peptide matches buffer.
     */
    private final HashMap<Long, Object> peptideBuffer = new HashMap<>(bufferSize);
    /**
     * Mutex for the peptide buffer.
     */
    private final SimpleSemaphore peptideBufferMutex = new SimpleSemaphore(1);
    /**
     * The protein matches buffer.
     */
    private final HashMap<Long, Object> proteinBuffer = new HashMap<>(bufferSize);
    /**
     * Mutex for the protein buffer.
     */
    private final SimpleSemaphore proteinBufferMutex = new SimpleSemaphore(1);

    /**
     * Constructor.
     *
     * @param identification the identification object
     */
    public PeptideAndProteinBuilder(
            Identification identification
    ) {

        this.identification = identification;

    }

    /**
     * Creates the peptides and protein instances based on the given spectrum
     * match. Note that only the best peptide assumption is used, the method has
     * no effect if it is null. This operation will be very slow if the cache is
     * already full. Note: if proteins are not set for a peptide they will be
     * assigned using the default protein tree and the given matching
     * parameters.
     *
     * @param spectrumMatch the spectrum match to add
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param sequenceProvider a provider of protein sequences
     * @param protein boolean indicating whether proteins should be built
     */
    public void buildPeptidesAndProteins(
            SpectrumMatch spectrumMatch,
            SequenceMatchingParameters sequenceMatchingPreferences,
            SequenceProvider sequenceProvider,
            boolean protein
    ) {

        long spectrumMatchKey = spectrumMatch.getKey();

        Peptide peptide = spectrumMatch.getBestPeptideAssumption().getPeptide();
        long peptideMatchKey = peptide.getMatchingKey(sequenceMatchingPreferences);

        //objectMutex.acquire(peptideMatchKey);

        PeptideMatch peptideMatch = identification.getPeptideMatch(peptideMatchKey);

        /*
        if (peptideMatch == null) {

            Object cacheObject = peptideBuffer.get(peptideMatchKey);

            if (cacheObject != null) {

                peptideMatch = (PeptideMatch) cacheObject;

            }
        }*/
        
        String output = Long.toString(spectrumMatchKey);

        if (peptideMatch == null) {

            peptideMatch = new PeptideMatch(peptide, peptideMatchKey, spectrumMatchKey);
            identification.addPeptideMatch(peptideMatchKey, peptideMatch);
            //addPeptideMatch(peptideMatchKey, peptideMatch);
            output += " new pep " + peptide.getSequence() + " "  + Long.toString(peptideMatchKey);

        } else {

            peptideMatch.addSpectrumMatchKey(spectrumMatchKey);
            //addPeptideMatch(peptideMatchKey, peptideMatch);
            output += " add pep " + Long.toString(peptideMatchKey);

        }

        //objectMutex.release(peptideMatchKey);

        if (protein) {

            long proteinMatchKey = ProteinMatch.getProteinMatchKey(peptide);

            //objectMutex.acquire(proteinMatchKey);

            ProteinMatch proteinMatch = identification.getProteinMatch(proteinMatchKey);

            /*
            if (proteinMatch == null) {

                Object cacheObject = proteinBuffer.get(proteinMatchKey);

                if (cacheObject != null) {

                    proteinMatch = (ProteinMatch) cacheObject;

                }
            }*/

            if (proteinMatch == null) {
                output += " new pro " + Long.toString(proteinMatchKey);

                proteinMatch = new ProteinMatch(peptideMatch.getPeptide(), peptideMatchKey);
                proteinMatch.setDecoy(Arrays.stream(proteinMatch.getAccessions())
                        .anyMatch(accession -> ProteinUtils.isDecoy(accession, sequenceProvider)));

                //addProteinMatch(proteinMatchKey, proteinMatch);
                identification.addProteinMatch(proteinMatchKey, proteinMatch);

            } else if (Arrays.stream(proteinMatch.getPeptideMatchesKeys()).allMatch(key -> key != peptideMatchKey)) {

                proteinMatch.addPeptideMatchKey(peptideMatchKey);
                output += " add pro " + Long.toString(proteinMatchKey);

            }
            
            //objectMutex.release(proteinMatchKey);

        }
        System.out.println(output);
    }

    /**
     * Adds a peptide match to the buffer. If the buffer is full, adds it to the
     * db.
     *
     * @param key The key of the peptide match.
     * @param peptideMatch The peptide Match.
     */
    private void addPeptideMatch(
            long key,
            PeptideMatch peptideMatch
    ) {

        peptideBufferMutex.acquire();

        if (peptideBuffer.size() == bufferSize) {

            identification.addPeptideMatches(peptideBuffer);
            peptideBuffer.clear();

        }

        peptideBuffer.put(key, peptideMatch);

        peptideBufferMutex.release();

    }

    /**
     * Adds a protein match to the buffer. If the buffer is full, adds it to the
     * db.
     *
     * @param key The key of the protein match.
     * @param proteinMatch The protein Match.
     */
    private void addProteinMatch(
            long key,
            ProteinMatch proteinMatch
    ) {

        proteinBufferMutex.acquire();

        if (proteinBuffer.size() == bufferSize) {

            identification.addProteinMatches(proteinBuffer);
            proteinBuffer.clear();

        }

        proteinBuffer.put(key, proteinMatch);

        proteinBufferMutex.release();

    }

    @Override
    public void close() {

        identification.addPeptideMatches(peptideBuffer);
        identification.addProteinMatches(proteinBuffer);

    }
}
