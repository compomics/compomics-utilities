package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * An iterator of protein matches.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ProteinMatchesIterator {

    /**
     * The identification where to get the matches from.
     */
    private final Identification identification;
    /**
     * The keys of the matches to load.
     */
    private final ArrayList<String> proteinKeys;
    /**
     * The protein parameters to load along with the matches.
     */
    private final ArrayList<UrParameter> proteinParameters;
    /**
     * If true the peptides corresponding to these proteins will be batch loaded
     * along with the proteins.
     */
    private final boolean loadPeptides;
    /**
     * The peptides parameters to load along with the matches.
     */
    private final ArrayList<UrParameter> peptidesParameters;
    /**
     * If true the PSMs corresponding to these peptides will be batch loaded
     * along with the peptides.
     */
    private final boolean loadPsms;
    /**
     * The PSM parameters to load along with the matches.
     */
    private final ArrayList<UrParameter> psmParameters;
    /**
     * The total number of matches to load.
     */
    private final int nMatches;
    /**
     * The current index of the iterator.
     */
    private int index = -1;
    /**
     * The default batch size.
     */
    public final int defaultBatchSize = 1000;
    /**
     * The current batch size.
     */
    private int batchSize = defaultBatchSize;
    /**
     * The current index of the batch loading.
     */
    private int loadingIndex = -1;
    /**
     * Boolean indicating whether a thread is buffering.
     */
    private boolean buffering = false;
    /**
     * Mutex for the buffering.
     */
    private Semaphore bufferingMutex = new Semaphore(1);
    /**
     * The default margin to use to start batch loading before the loading index
     * is reached.
     */
    public final double defaultMargin = 0.1;
    /**
     * The margin to use to start batch loading before the loading index is
     * reached.
     */
    private double margin = defaultMargin;
    /**
     * The waiting handler.
     */
    private WaitingHandler waitingHandler;

    /**
     * Constructor.
     *
     * @param proteinKeys the keys of the proteins to iterate
     * @param identification the identification where to get the matches from
     * @param proteinParameters the protein parameters to load along with the
     * matches
     * @param loadPeptides if true the peptides corresponding to these proteins
     * will be batch loaded along with the proteins
     * @param peptideParameters the parameters to load along with the peptide
     * matches
     * @param loadPsms if true the PSMs of the peptides will be batch loaded
     * along with the matches
     * @param psmParameters the parameters to load along with the matches
     * @param waitingHandler the waiting handler
     */
    public ProteinMatchesIterator(ArrayList<String> proteinKeys, Identification identification, ArrayList<UrParameter> proteinParameters,
            boolean loadPeptides, ArrayList<UrParameter> peptideParameters, boolean loadPsms, ArrayList<UrParameter> psmParameters, WaitingHandler waitingHandler) {

        this.identification = identification;
        if (proteinKeys == null) {
            this.proteinKeys = new ArrayList<String>(identification.getProteinIdentification());
        } else {
            this.proteinKeys = proteinKeys;
        }
        nMatches = this.proteinKeys.size();
        this.proteinParameters = proteinParameters;
        this.loadPeptides = loadPeptides;
        this.peptidesParameters = peptideParameters;
        this.loadPsms = loadPsms;
        this.psmParameters = psmParameters;
        this.waitingHandler = waitingHandler;
        if (proteinKeys != null) {
            // adapt the batch size to avoid the hanging of the progress bar
            batchSize = Math.max(50, proteinKeys.size() / 100);
            batchSize = Math.min(1000, Math.max(batchSize, proteinKeys.size() / 1000));
        }
    }

    /**
     * Constructor for an iterator iterating all protein keys.
     *
     * @param identification the identification where to get the matches from
     * @param proteinParameters the protein parameters to load along with the
     * matches
     * @param loadPeptides if true the peptides corresponding to these proteins
     * will be batch loaded along with the proteins
     * @param peptideParameters the parameters to load along with the peptide
     * matches
     * @param loadPsms if true the PSMs of the peptides will be batch loaded
     * along with the matches
     * @param psmParameters the parameters to load along with the matches
     * @param waitingHandler the waiting handler
     */
    public ProteinMatchesIterator(Identification identification, ArrayList<UrParameter> proteinParameters, boolean loadPeptides,
            ArrayList<UrParameter> peptideParameters, boolean loadPsms, ArrayList<UrParameter> psmParameters, WaitingHandler waitingHandler) {
        this(null, identification, proteinParameters, loadPeptides, peptideParameters, loadPsms, psmParameters, waitingHandler);
    }

    /**
     * Indicates whether the iterator is done iterating. Warning: this method
     * can be wrong when multi threading.
     *
     * @return false if the iterator is done iterating
     */
    public boolean hasNext() {
        return index < nMatches - 1;
    }

    /**
     * Returns the next match and updates the buffer. Null if the iterator is
     * done iterating.
     *
     * @return the next match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * interacting with the matches database
     * @throws IOException exception thrown whenever an error occurred while
     * interacting with the matches database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a match from the database
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while retrieving the match
     */
    public ProteinMatch next() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        int threadIndex = incrementIndex();
        if (threadIndex < nMatches) {
            checkBuffer();
            String key = proteinKeys.get(threadIndex);
            ProteinMatch match = identification.getProteinMatch(key);
            return match;
        }
        return null;
    }

    /**
     * Makes sure that the next matches are buffered in the identification
     * cache.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * interacting with the matches database
     * @throws IOException exception thrown whenever an error occurred while
     * interacting with the matches database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a match from the database
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while retrieving the match
     */
    private void checkBuffer() throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        if (!buffering) {

            int trigger = loadingIndex - ((int) (margin * batchSize));

            if (index >= trigger) {

                bufferingMutex.acquire();

                trigger = loadingIndex - ((int) (margin * batchSize));

                if (index >= trigger) {

                    buffering = true;

                    int newLoadingIndex = Math.min(loadingIndex + batchSize, nMatches - 1);
                    ArrayList<String> keysInBatch = new ArrayList<String>(proteinKeys.subList(loadingIndex + 1, newLoadingIndex + 1));
                    identification.loadProteinMatches(keysInBatch, waitingHandler, false);

                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        return;
                    }

                    if (proteinParameters != null) {
                        for (UrParameter urParameter : proteinParameters) {
                            if (urParameter == null) {
                                throw new IllegalArgumentException("Parameter to batch load is null.");
                            }
                            identification.loadProteinMatchParameters(keysInBatch, urParameter, waitingHandler, false);

                            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                                return;
                            }
                        }
                    }
                    ArrayList<String> peptideKeys = null;
                    if (loadPeptides || peptidesParameters != null) {
                        peptideKeys = new ArrayList<String>(batchSize);
                        for (String proteinKey : keysInBatch) {
                            ProteinMatch proteinMatch = identification.getProteinMatch(proteinKey);
                            peptideKeys.addAll(proteinMatch.getPeptideMatchesKeys());
                        }
                    }
                    if (loadPeptides) {
                        identification.loadPeptideMatches(peptideKeys, waitingHandler, false);

                        if (loadPsms) {
                            ArrayList<String> psmKeys = new ArrayList<String>(peptideKeys.size());
                            for (String peptideKey : peptideKeys) {
                                PeptideMatch peptideMatch = identification.getPeptideMatch(peptideKey);
                                psmKeys.addAll(peptideMatch.getSpectrumMatchesKeys());
                            }
                            identification.loadSpectrumMatches(psmKeys, waitingHandler, false);
                            if (psmParameters != null) {
                                for (UrParameter urParameter : psmParameters) {
                                    if (urParameter == null) {
                                        throw new IllegalArgumentException("Parameter to batch load is null.");
                                    }

                                    identification.loadSpectrumMatchParameters(psmKeys, urParameter, waitingHandler, false);

                                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    if (peptidesParameters != null) {
                        for (UrParameter urParameter : peptidesParameters) {
                            if (urParameter == null) {
                                throw new IllegalArgumentException("Parameter to batch load is null.");
                            }
                            identification.loadPeptideMatchParameters(peptideKeys, urParameter, waitingHandler, false);

                            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                                return;
                            }
                        }
                    }

                    loadingIndex = newLoadingIndex;
                    trigger += (int) (margin * batchSize / 2);
                    trigger = Math.max(0, trigger);

                    if (index < trigger) {
                        if (batchSize > defaultBatchSize) {
                            batchSize = Math.max(defaultBatchSize, (int) 0.9 * batchSize);
                        } else if (margin > defaultMargin) {
                            margin = Math.max(defaultMargin, 0.9 * margin);
                        }
                    }
                }

                if (!bufferingMutex.hasQueuedThreads()) {
                    buffering = false;
                }
                bufferingMutex.release();
            }
        } else if (index == loadingIndex) {
            margin *= 1.1;
            if (margin >= 0.25) {
                batchSize *= 1.1;
                margin = defaultMargin;
            }
        }
    }

    /**
     * Increments the index of the iterator.
     *
     * @return an integer with value the incremented index
     */
    private synchronized int incrementIndex() {
        int localIndex = ++index;
        return localIndex;
    }

    /**
     * Set the batch size.
     *
     * @param batchSize the batch size
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
