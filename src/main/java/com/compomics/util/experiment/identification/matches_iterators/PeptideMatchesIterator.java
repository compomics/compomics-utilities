package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;

/**
 * An iterator for peptide matches.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 * @author Dominik Kopczynski
 */
public class PeptideMatchesIterator extends MatchesIterator {

    /**
     * Empty default constructor
     */
    public PeptideMatchesIterator() {
    }
    
    /**
     * Constructor.
     *
     * @param identification the identification where to get the matches from
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     */
    public PeptideMatchesIterator(Identification identification, WaitingHandler waitingHandler, boolean displayProgress) {
        this(null, identification, waitingHandler, displayProgress);
    }
    
    /**
     * Constructor.
     *
     * @param keys the keys of the objects
     * @param identification the identification where to get the matchesloadPs from
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     */
    public PeptideMatchesIterator(long[] keys, Identification identification, WaitingHandler waitingHandler, boolean displayProgress) {
        super(keys, PeptideMatch.class, identification, waitingHandler, displayProgress, null);
    }
    

    /**
     * Returns the next match and updates the buffer. Null if the iterator is
     * done iterating.
     *
     * @return the next match
     */
    public PeptideMatch next() {
        return (PeptideMatch)nextObject();
    }
}
