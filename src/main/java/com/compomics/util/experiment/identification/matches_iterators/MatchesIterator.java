package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.waiting.WaitingHandler;
import com.orientechnologies.orient.object.iterator.OObjectIteratorClass;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * An abstract iterator class.
 *
 * @author Dominik Kopczynski
 */
public abstract class MatchesIterator {
    /**
     * Iterator for spectrum matches
     */
    private OObjectIteratorClass<?> iterator = null;
    /**
     * the identification
     */
    private Identification identification = null;
    /**
     * list of potential keys for the iterator, if empty all instances of the class are being iterated
     */
    private ArrayList<String> keys = null;
    /**
     * current absolute index
     */
    private int index;
    /**
     * absolute number of the objects in the iterator
     */
    private int num;
    /**
     * flag for lazy loading from the database
     */
    private boolean lazyLoading;
    /**
     * batch size for loading from the db
     */
    private final int batchSize = 1024;
    /**
     * waiting handler
     */
    private WaitingHandler waitingHandler;
    /**
     * display progress
     */
    private boolean displayProgress;
    /**
     * list of current keys within the batch
     */
    private ArrayList<Long> currentKeys = null;
    /** 
     * batch index
     */
    private int currentIndex;
    
    
    /**
     * Constructor.
     *
     * @param className the class name
     * @param identification the identification where to get the matches from
     * @param lazyLoading indicates wheather the iterator should load data lazy from the db
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public MatchesIterator(String className, Identification identification, boolean lazyLoading, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        this(null, className, identification, lazyLoading, waitingHandler, displayProgress);
    }
    
    /**
     * Constructor.
     *
     * @param keys the keys of the objects
     * @param className the className
     * @param identification the identification where to get the matchesloadPs from
     * @param lazyLoading indicates wheather the iterator should load data lazy from the db
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * 
     * @throws SQLException exception thrown whenever an error occurred while
     * loading the object from the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the object in the database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while casting the database input in the desired match class
     * @throws InterruptedException thrown whenever a threading issue occurred
     * while interacting with the database
     */
    public MatchesIterator(ArrayList<String> keys, String className, Identification identification, boolean lazyLoading, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (keys != null){
            num = keys.size();
            this.keys = keys;
        }
        else {
            num = identification.getNumber(className);
            iterator = identification.getIterator(className);
        }
        index = 0;
        currentIndex = 0;
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
    public synchronized Object nextObject() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        // loading data from db
        if (index % batchSize == 0) {
            if (keys != null){
                ArrayList<String> subKeyList = (ArrayList<String>)keys.subList(index, index + batchSize);
                identification.loadObjects(subKeyList, lazyLoading, waitingHandler, displayProgress);
            }
            else {
                identification.loadObjects(iterator, batchSize, lazyLoading, waitingHandler, displayProgress);
            }
            currentIndex = 0;
        }
            
        index++;
        return identification.retrieveObject(currentKeys.get(currentIndex++));
    }
}
