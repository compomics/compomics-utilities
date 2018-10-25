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
import java.util.Arrays;

/**
 * This class builds peptides and proteins based on PSMs.
 *
 * @author Marc Vaudel
 */
public class PeptideAndProteinBuilder {

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
     * Constructor.
     *
     * @param identification the identification object
     */
    public PeptideAndProteinBuilder(Identification identification) {
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
    public void buildPeptidesAndProteins(SpectrumMatch spectrumMatch, SequenceMatchingParameters sequenceMatchingPreferences, SequenceProvider sequenceProvider, boolean protein) {

        long spectrumMatchKey = spectrumMatch.getKey();

        Peptide peptide = spectrumMatch.getBestPeptideAssumption().getPeptide();
        long peptideMatchKey = peptide.getMatchingKey(sequenceMatchingPreferences);

        objectMutex.acquire(peptideMatchKey);

        PeptideMatch peptideMatch = identification.getPeptideMatch(peptideMatchKey);

        if (peptideMatch == null) {

            peptideMatch = new PeptideMatch(peptide, peptideMatchKey, spectrumMatchKey);

            identification.addPeptideMatch(peptideMatchKey, peptideMatch);

        } else {

            peptideMatch.addSpectrumMatchKey(spectrumMatchKey);

        }

        objectMutex.release(peptideMatchKey);

        if (protein) {

            long proteinMatchKey = ProteinMatch.getProteinMatchKey(peptide);

            objectMutex.acquire(proteinMatchKey);

            ProteinMatch proteinMatch = identification.getProteinMatch(proteinMatchKey);

            if (proteinMatch == null) {

                proteinMatch = new ProteinMatch(peptideMatch.getPeptide(), peptideMatchKey);
                proteinMatch.setDecoy(Arrays.stream(proteinMatch.getAccessions())
                        .anyMatch(accession -> ProteinUtils.isDecoy(accession, sequenceProvider)));

                identification.addProteinMatch(proteinMatchKey, proteinMatch);

            } else if (Arrays.stream(proteinMatch.getPeptideMatchesKeys()).allMatch(key -> key != peptideMatchKey)) {

                proteinMatch.addPeptideMatchKey(peptideMatchKey);

            }

            objectMutex.release(proteinMatchKey);

        }
    }

}
