package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.waiting.WaitingHandler;
import com.orientechnologies.orient.object.iterator.OObjectIteratorClass;
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
public class PsmIterator {
    /**
     * Iterator for spectrum matches
     */
    private OObjectIteratorClass<?> iterator = null;
    private Identification identification = null;
    private ArrayList<String> keys = null;
    private int index;
    private int num;
    private boolean lazyLoading;
    private final int batchSize = 1024;
    private WaitingHandler waitingHandler;
    private boolean displayProgress;
    
    
    /**
     * Constructor.
     *
     * @param identification the identification where to get the matches from
     * @param lazyLoading indicates wheather the iterator should load data lazy from the db
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InterruptedException
     */
    public PsmIterator(Identification identification, boolean lazyLoading, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        this(null, identification, lazyLoading, waitingHandler, displayProgress);
    }
    
    /**
     * Constructor.
     *
     * @param keys the keys of the objects
     * @param identification the identification where to get the matches from
     * @param lazyLoading indicates wheather the iterator should load data lazy from the db
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InterruptedException
     */
    public PsmIterator(ArrayList<String> keys, Identification identification, boolean lazyLoading, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (keys != null){
            num = keys.size();
            this.keys = keys;
        }
        else {
            num = identification.getNumber(SpectrumMatch.class.getSimpleName());
            iterator = identification.getIterator(SpectrumMatch.class.getSimpleName());
        }
        index = 0;
        this.lazyLoading = lazyLoading;
        this.identification = identification;
        this.waitingHandler = waitingHandler;
        this.displayProgress = displayProgress;
    }
    

    /**
     * Indicates whether the iterator is done iterating. Warning: this method
     * can be wrong when multi threading.
     *
     * @return false if the iterator is done iterating
     */
    public boolean hasNext() {
        return index < num;
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
    public synchronized SpectrumMatch next() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        // loading data from db
        if (index % batchSize == 0) {
            if (keys != null){
                ArrayList<String> subKeyList = (ArrayList<String>)keys.subList(index, index + batchSize);
                identification.loadObjects(subKeyList, lazyLoading, waitingHandler, displayProgress);
            }
            else {
                identification.loadObjects(iterator, batchSize, lazyLoading, waitingHandler, displayProgress);
                
            }
        }
            
            
            
        if (spectrumMatches == null) return (SpectrumMatch)it.next();
        return (SpectrumMatch)spectrumMatches.get(index++);
    }
}
