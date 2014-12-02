/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.personalization.UrParameter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * An iterator for spectrum matches.
 *
 * @author Marc
 */
public class PsmIterator {

    /**
     * The identification where to get the matches from
     */
    private final Identification identification;
    /**
     * The keys of the matches to load
     */
    private final ArrayList<String> spectrumKeys;
    /**
     * The psm parameters to load along with the matches
     */
    private final ArrayList<UrParameter> psmParameters;
    /**
     * The total number of matches to load
     */
    private final int nMatches;
    /**
     * The current index of the iterator
     */
    private int index = -1;
    /**
     * The default batch size
     */
    public final int defaultBatchSize = 1000;
    /**
     * The current batch size
     */
    private int batchSize = defaultBatchSize;
    /**
     * The current index of the batch loading
     */
    private int loadingIndex = -1;
    /**
     * boolean indicating whether a thread is buffering
     */
    private boolean buffering = false;
    /**
     * The default margin to use to start batch loading before the loading index
     * is reached
     */
    public final double defaultMargin = 0.1;
    /**
     * The margin to use to start batch loading before the loading index is
     * reached
     */
    private double margin = defaultMargin;

    /**
     * Constructor.
     *
     * @param spectrumFile the spectrum file to iterate
     * @param spectrumKeys the keys to iterate
     * @param identification the identification where to get the matches from
     * @param psmParameters the parameters to load along with the matches
     */
    public PsmIterator(String spectrumFile, ArrayList<String> spectrumKeys, Identification identification, ArrayList<UrParameter> psmParameters) {
        this.identification = identification;
        if (spectrumKeys == null) {
            this.spectrumKeys = identification.getSpectrumIdentification(spectrumFile);
        } else {
            this.spectrumKeys = spectrumKeys;
        }
        nMatches = spectrumKeys.size();
        this.psmParameters = psmParameters;
    }

    /**
     * Constructor.
     *
     * @param spectrumFile the spectrum file to iterate
     * @param spectrumKeys the keys to iterate
     * @param identification the identification where to get the matches from
     */
    public PsmIterator(String spectrumFile, ArrayList<String> spectrumKeys, Identification identification) {
        this(spectrumFile, spectrumKeys, identification, null);
    }

    /**
     * Constructor.
     *
     * @param spectrumFile the spectrum file to iterate
     * @param identification the identification where to get the matches from
     * @param psmParameters the parameters to load along with the matches
     */
    public PsmIterator(String spectrumFile, Identification identification, ArrayList<UrParameter> psmParameters) {
        this(spectrumFile, null, identification, psmParameters);
    }

    /**
     * Constructor.
     *
     * @param spectrumFile the spectrum file to iterate
     * @param identification the identification where to get the matches from
     */
    public PsmIterator(String spectrumFile, Identification identification) {
        this(spectrumFile, identification, null);
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
                identification.loadSpectrumMatches(keysInBatch, null);
                if (psmParameters != null) {
                    for (UrParameter urParameter : psmParameters) {
                        if (urParameter == null) {
                            throw new IllegalArgumentException("Parameter to for batch load is null.");
                        }
                        identification.loadSpectrumMatchParameters(keysInBatch, urParameter, null);
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

}
