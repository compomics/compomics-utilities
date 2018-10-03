package com.compomics.util.experiment.quantification.spectrumcounting;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.features.IdentificationFeaturesGenerator;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches_iterators.ProteinMatchesIterator;
import com.compomics.util.experiment.identification.peptide_shaker.Metrics;
import com.compomics.util.experiment.identification.peptide_shaker.PSParameter;
import com.compomics.util.experiment.identification.utils.ProteinUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.quantification.spectrum_counting.SpectrumCountingParameters;
import com.compomics.util.parameters.tools.ProcessingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class estimates spectrum counting scaling factors.
 *
 * @author Marc Vaudel
 */
public class ScalingFactorsEstimators {

    /**
     * The spectrum counting parameters.
     */
    private final SpectrumCountingParameters spectrumCountingParameters;

    /**
     * Constructor.
     * 
     * @param spectrumCountingParameters the spectrum counting parameters
     */
    public ScalingFactorsEstimators(SpectrumCountingParameters spectrumCountingParameters) {

        this.spectrumCountingParameters = spectrumCountingParameters;

    }

    /**
     * Estimates the scaling factors and stores them in the given metrics.
     * 
     * @param identification the identification
     * @param metrics the metrics where to save the results
     * @param sequenceProvider a sequence provider
     * @param identificationFeaturesGenerator the identification features generator
     * @param waitingHandler a waiting handler
     * @param exceptionHandler an exception handler
     * @param processingParameters the processing parameters
     * 
     * @throws java.lang.InterruptedException exception thrown if a thread gets
     * interrupted
     * @throws java.util.concurrent.TimeoutException exception thrown if the
     * operation times out
     */
    public void estimateScalingFactors(Identification identification, Metrics metrics, 
            SequenceProvider sequenceProvider, IdentificationFeaturesGenerator identificationFeaturesGenerator, 
            WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, ProcessingParameters processingParameters) throws InterruptedException, TimeoutException {

        // validate the proteins
        ExecutorService pool = Executors.newFixedThreadPool(processingParameters.getnThreads());

        ProteinMatchesIterator proteinMatchesIterator = identification.getProteinMatchesIterator(waitingHandler);
        ArrayList<ScalingRunnable> runnables = new ArrayList<>(processingParameters.getnThreads());

        for (int i = 1; i <= processingParameters.getnThreads(); i++) {

            ScalingRunnable runnable = new ScalingRunnable(proteinMatchesIterator, sequenceProvider, identificationFeaturesGenerator, waitingHandler, exceptionHandler);
            pool.submit(runnable);
            runnables.add(runnable);

        }

        if (waitingHandler.isRunCanceled()) {

            pool.shutdownNow();
            return;

        }

        pool.shutdown();

        if (!pool.awaitTermination(identification.getProteinIdentification().size(), TimeUnit.MINUTES)) {
            throw new InterruptedException("Protein matches validation timed out. Please contact the developers.");
        }
        
        metrics.setTotalSpectrumCounting(runnables.stream()
                        .mapToDouble(ScalingRunnable::getTotalSpectrumCounting)
                        .sum());
        
        metrics.setTotalSpectrumCountingMass(runnables.stream()
                        .mapToDouble(ScalingRunnable::getTotalSpectrumCountingMass)
                        .sum());

    }
    

    /**
     * Runnable to gather scaling factors in multiple threads.
     *
     * @author Marc Vaudel
     */
    private class ScalingRunnable implements Runnable {

        /**
         * An iterator for the protein matches.
         */
        private final ProteinMatchesIterator proteinMatchesIterator;
        /**
         * The sequence provider.
         */
        private final SequenceProvider sequenceProvider;
        /**
         * The identification features generator used. to estimate, store and
         * retrieve identification features
         */
        private final IdentificationFeaturesGenerator identificationFeaturesGenerator;
        /**
         * The waiting handler.
         */
        private final WaitingHandler waitingHandler;
        /**
         * Handler for the exceptions.
         */
        private final ExceptionHandler exceptionHandler;
        /**
         * The total spectrum counting mass contribution of the proteins
         * according to the validation level specified in the parameters.
         */
        private double totalSpectrumCountingMass = 0;
        /**
         * The total spectrum counting contribution of the proteins according to
         * the validation level specified in the parameters.
         */
        private double totalSpectrumCounting = 0;

        /**
         * Constructor.
         *
         * @param proteinMatchesIterator an iterator of the protein matches to
         * inspect
         * @param sequenceProvider a sequence provider
         * @param identificationFeaturesGenerator the identification features
         * generator
         * @param waitingHandler a waiting handler
         * @param exceptionHandler an exception handler
         */
        public ScalingRunnable(ProteinMatchesIterator proteinMatchesIterator, SequenceProvider sequenceProvider,
                IdentificationFeaturesGenerator identificationFeaturesGenerator, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {

            this.proteinMatchesIterator = proteinMatchesIterator;
            this.sequenceProvider = sequenceProvider;
            this.identificationFeaturesGenerator = identificationFeaturesGenerator;
            this.waitingHandler = waitingHandler;
            this.exceptionHandler = exceptionHandler;

        }

        @Override
        public void run() {
            try {

                ProteinMatch proteinMatch;
                while ((proteinMatch = proteinMatchesIterator.next()) != null && !waitingHandler.isRunCanceled()) {

                    long proteinKey = proteinMatch.getKey();

                    // set the fraction details
                    PSParameter psParameter = new PSParameter();
                    psParameter = (PSParameter) proteinMatch.getUrParam(psParameter);

                    if (!proteinMatch.isDecoy() && psParameter.getMatchValidationLevel().getIndex() >= spectrumCountingParameters.getMatchValidationLevel()) {

                        double tempSpectrumCounting = identificationFeaturesGenerator.getSpectrumCounting(proteinKey);
                        totalSpectrumCounting += tempSpectrumCounting;

                        double molecularWeight = ProteinUtils.computeMolecularWeight(
                                sequenceProvider.getSequence(proteinMatch.getLeadingAccession()));
                        double massContribution = molecularWeight * tempSpectrumCounting;
                        totalSpectrumCountingMass += massContribution;

                    }

                    if (waitingHandler.isRunCanceled()) {
                        return;
                    }
                }

            } catch (Exception e) {
                exceptionHandler.catchException(e);
            }
        }

        /**
         * Returns the spectrum counting mass contribution of the validated
         * proteins.
         *
         * @return the spectrum counting mass contribution of the validated
         * proteins
         */
        public double getTotalSpectrumCountingMass() {

            return totalSpectrumCountingMass;

        }

        /**
         * Returns the spectrum counting contribution of the proteins iterated
         * by this runnable.
         *
         * @return the spectrum counting contribution of the proteins iterated
         * by this runnable
         */
        public double getTotalSpectrumCounting() {

            return totalSpectrumCounting;

        }
    }

}
