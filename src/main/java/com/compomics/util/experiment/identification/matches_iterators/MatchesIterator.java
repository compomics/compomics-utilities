package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.IdObject;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.Iterator;

/**
 * An abstract iterator class.
 *
 * @author Dominik Kopczynski
 */
public abstract class MatchesIterator {
    /**
     * Iterator for spectrum matches
     */
    private Iterator<?> iterator = null;
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
    private int index = 0;
    /**
     * absolute number of the objects in the iterator
     */
    private int num = 0;
    /**
     * waiting handler
     */
    private WaitingHandler waitingHandler;
    /**
     * display progress
     */
    private boolean displayProgress;
    /**
     * list of long keys to iterate
     */
    private ArrayList<Long> longKeys = null;
    
    
    private final Semaphore nextMutex = new Semaphore(1);
    
    /**
     * Constructor.
     *
     * @param className the class name
     * @param identification the identification where to get the matches from
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
    public MatchesIterator(Class className, Identification identification, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        this(null, className, identification, waitingHandler, displayProgress, null);
    }
    
    /**
     * Constructor.
     *
     * @param keys the keys of the objects
     * @param className the className
     * @param identification the identification where to get the matchesloadPs from
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
    public MatchesIterator(ArrayList<String> keys, Class className, Identification identification, WaitingHandler waitingHandler, boolean displayProgress, String filters) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (keys != null){
            num = keys.size();
            this.keys = keys;
        }
        else {
            if (filters == null){
                longKeys = new ArrayList<Long>(identification.getClassObjects(className));
            }
            else {
                iterator = identification.getIterator(className, filters);
                longKeys = new ArrayList<Long>(identification.getNumber(className));
                while (iterator.hasNext()){
                    longKeys.add(((IdObject)iterator.next()).getId());
                }
            }
            num = longKeys.size();
            System.out.println("get It " + className.getSimpleName() + " / " + num);
        }
        
        index = 0;
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
    public Object nextObject() throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        nextMutex.acquire();
        Object obj = null;
        if (index < num){
            if (keys == null){
                obj = identification.retrieveObject(longKeys.get(index));
            }
            else {
                obj = identification.retrieveObject(keys.get(index));
            }
            index++;
        }
        nextMutex.release();
        return obj;
    }
}
