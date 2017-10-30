package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;

/**
 * An iterator for spectrum matches.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 * @author Dominik Kopczynski
 */
public class SpectrumMatchesIterator extends MatchesIterator {
    /**
     * Constructor.
     *
     * @param identification the identification where to get the matches from
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     */
    public SpectrumMatchesIterator(Identification identification, WaitingHandler waitingHandler, boolean displayProgress) {
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
    public SpectrumMatchesIterator(ArrayList<Long> keys, Identification identification, WaitingHandler waitingHandler, boolean displayProgress) {
        super(keys, SpectrumMatch.class, identification, waitingHandler, displayProgress, null);
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
     * @param filters filters for the class
     */
    public SpectrumMatchesIterator(ArrayList<Long> keys, Identification identification, WaitingHandler waitingHandler, boolean displayProgress, String filters) {
        super(keys, SpectrumMatch.class, identification, waitingHandler, displayProgress, filters);
    }
    


    /**
     * Returns the next match and updates the buffer. Null if the iterator is
     * done iterating.
     *
     * @return the next match
     */
    public SpectrumMatch next() {
        
        return (SpectrumMatch) nextObject();
        
    }
}
