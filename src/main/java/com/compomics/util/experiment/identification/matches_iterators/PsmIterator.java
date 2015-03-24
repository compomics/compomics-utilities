package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * An iterator for spectrum matches.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PsmIterator {

    /**
     * The identification where to get the matches from.
     */
    private final Identification identification;
    /**
     * The keys of the matches to load.
     */
    private final ArrayList<String> spectrumKeys;
    /**
     * The PSM parameters to load along with the matches.
     */
    private final ArrayList<UrParameter> psmParameters;
    /**
     * If true the assumptions will be loaded as well.
     */
    private final boolean loadAssumptions;
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
     * @param spectrumFile the spectrum file to iterate
     * @param spectrumKeys the keys to iterate
     * @param identification the identification where to get the matches from
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     */
    public PsmIterator(String spectrumFile, ArrayList<String> spectrumKeys, Identification identification, ArrayList<UrParameter> psmParameters, boolean loadAssumptions, WaitingHandler waitingHandler) {
        this.identification = identification;
        if (spectrumKeys == null) {
            if (spectrumFile != null) {
                this.spectrumKeys = identification.getSpectrumIdentification(spectrumFile);
            } else {
                this.spectrumKeys = new ArrayList<String>(identification.getSpectrumIdentificationSize());
                for (String tempSpectrumFile : identification.getOrderedSpectrumFileNames()) {
                    spectrumKeys.addAll(identification.getSpectrumIdentification(tempSpectrumFile));
                }
            }
        } else {
            this.spectrumKeys = spectrumKeys;
        }
        nMatches = this.spectrumKeys.size();
        this.psmParameters = psmParameters;
        this.loadAssumptions = loadAssumptions;
        this.waitingHandler = waitingHandler;
    }

    /**
     * Constructor.
     *
     * @param spectrumKeys the keys to iterate
     * @param identification the identification where to get the matches from
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     */
    public PsmIterator(ArrayList<String> spectrumKeys, Identification identification, ArrayList<UrParameter> psmParameters, boolean loadAssumptions, WaitingHandler waitingHandler) {
        this(null, spectrumKeys, identification, psmParameters, loadAssumptions, waitingHandler);
    }

    /**
     * Constructor.
     *
     * @param spectrumKeys the keys to iterate
     * @param identification the identification where to get the matches from
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     */
    public PsmIterator(ArrayList<String> spectrumKeys, Identification identification, boolean loadAssumptions, WaitingHandler waitingHandler) {
        this(null, spectrumKeys, identification, null, loadAssumptions, waitingHandler);
    }

    /**
     * Constructor.
     *
     * @param spectrumFile the spectrum file to iterate
     * @param spectrumKeys the keys to iterate
     * @param identification the identification where to get the matches from
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     */
    public PsmIterator(String spectrumFile, ArrayList<String> spectrumKeys, Identification identification, boolean loadAssumptions, WaitingHandler waitingHandler) {
        this(spectrumFile, spectrumKeys, identification, null, loadAssumptions, waitingHandler);
    }

    /**
     * Constructor.
     *
     * @param spectrumFile the spectrum file to iterate
     * @param identification the identification where to get the matches from
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     */
    public PsmIterator(String spectrumFile, Identification identification, ArrayList<UrParameter> psmParameters, boolean loadAssumptions, WaitingHandler waitingHandler) {
        this(spectrumFile, null, identification, psmParameters, loadAssumptions, waitingHandler);
    }

    /**
     * Constructor.
     *
     * @param spectrumFile the spectrum file to iterate
     * @param identification the identification where to get the matches from
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     */
    public PsmIterator(String spectrumFile, Identification identification, boolean loadAssumptions, WaitingHandler waitingHandler) {
        this(spectrumFile, identification, null, loadAssumptions, waitingHandler);
    }

    /**
     * Constructor.
     *
     * @param spectrumFile the spectrum file to iterate
     * @param identification the identification where to get the matches from
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     */
    public PsmIterator(String spectrumFile, Identification identification, boolean loadAssumptions, ArrayList<UrParameter> psmParameters, WaitingHandler waitingHandler) {
        this(spectrumFile, identification, null, loadAssumptions, waitingHandler);
    }

    /**
     * Constructor.
     *
     * @param identification the identification where to get the matches from
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     */
    public PsmIterator(Identification identification, boolean loadAssumptions, WaitingHandler waitingHandler) {
        this(null, null, identification, null, loadAssumptions, waitingHandler);
    }

    /**
     * Constructor.
     *
     * @param identification the identification where to get the matches from
     * @param psmParameters the parameters to load along with the matches
     * @param loadAssumptions if true the assumptions will be loaded as well
     * @param waitingHandler the waiting handler
     */
    public PsmIterator(Identification identification, ArrayList<UrParameter> psmParameters, boolean loadAssumptions, WaitingHandler waitingHandler) {
        this(null, null, identification, psmParameters, loadAssumptions, waitingHandler);
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
    public SpectrumMatch next() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        int threadIndex = incrementIndex();
        if (threadIndex < nMatches) {
            checkBuffer();
            String key = spectrumKeys.get(threadIndex);
            SpectrumMatch match = identification.getSpectrumMatch(key);
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

                int newLoadingIndex = Math.min(loadingIndex + batchSize, nMatches - 1);
                ArrayList<String> keysInBatch = new ArrayList<String>(spectrumKeys.subList(loadingIndex + 1, newLoadingIndex + 1));
                if (waitingHandler != null) {
                    waitingHandler.setDisplayProgress(false);
                }
                identification.loadSpectrumMatches(keysInBatch, waitingHandler);
                if (waitingHandler != null) {
                    waitingHandler.setDisplayProgress(true);
                }

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    return;
                }

                if (psmParameters != null) {
                    for (UrParameter urParameter : psmParameters) {
                        if (urParameter == null) {
                            throw new IllegalArgumentException("Parameter to batch load is null.");
                        }
                        if (waitingHandler != null) {
                            waitingHandler.setDisplayProgress(false);
                        }
                        identification.loadSpectrumMatchParameters(keysInBatch, urParameter, waitingHandler);
                        if (waitingHandler != null) {
                            waitingHandler.setDisplayProgress(true);
                        }
                        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                            return;
                        }
                    }
                }
                if (loadAssumptions) {
                    if (waitingHandler != null) {
                        waitingHandler.setDisplayProgress(false);
                    }
                    identification.loadAssumptions(keysInBatch, waitingHandler);
                    if (waitingHandler != null) {
                        waitingHandler.setDisplayProgress(true);
                    }
                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        return;
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

                buffering = false;
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
