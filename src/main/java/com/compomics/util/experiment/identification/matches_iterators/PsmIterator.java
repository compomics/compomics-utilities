package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * An iterator for spectrum matches.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 * @author Dominik Kopczynski
 */
public class PsmIterator extends MatchesIterator {
    /**
     * Constructor.
     *
     * @param identification the identification where to get the matches from
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public PsmIterator(Identification identification, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {
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
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public PsmIterator(ArrayList<String> keys, Identification identification, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {
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
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public PsmIterator(ArrayList<String> keys, Identification identification, WaitingHandler waitingHandler, boolean displayProgress, String filters) throws InterruptedException {
        super(keys, SpectrumMatch.class, identification, waitingHandler, displayProgress, filters);
    }
    


    /**
     * Returns the next match and updates the buffer. Null if the iterator is
     * done iterating.
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     * @return the next match
     */
    public SpectrumMatch next() throws InterruptedException {
        
        return (SpectrumMatch)nextObject();
    }
}
