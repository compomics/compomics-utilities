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
    public PsmIterator(Identification identification, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
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
    public PsmIterator(ArrayList<String> keys, Identification identification, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
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
    public PsmIterator(ArrayList<String> keys, Identification identification, WaitingHandler waitingHandler, boolean displayProgress, String filters) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        super(keys, SpectrumMatch.class, identification, waitingHandler, displayProgress, filters);
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
        return (SpectrumMatch)nextObject();
    }
}
