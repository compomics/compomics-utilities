package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.waiting.WaitingHandler;

/**
 * An iterator of protein matches.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 * @author Dominik Kopczynski
 */
public class ProteinMatchesIterator extends MatchesIterator {

    /**
     * Empty default constructor
     */
    public ProteinMatchesIterator() {
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
    public ProteinMatchesIterator(Identification identification, WaitingHandler waitingHandler, boolean displayProgress) {
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
    public ProteinMatchesIterator(long[] keys, Identification identification, WaitingHandler waitingHandler, boolean displayProgress) {
        super(keys, ProteinMatch.class, identification, waitingHandler, displayProgress, null);
    }
    

    /**
     * Returns the next match and updates the buffer. Null if the iterator is
     * done iterating.
     *
     * @return the next match
     */
    public ProteinMatch next() {
        return (ProteinMatch) nextObject();
    }
}
