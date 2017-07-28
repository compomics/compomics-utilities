package com.compomics.util.db;

import com.compomics.util.IdObject;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;
import javax.jdo.PersistenceManager;

/**
 * An object cache can be combined to an ObjectDB to improve its performance. A
 * single cache can be used by different databases. This ough not to be
 * serialized. The length of lists/maps in the cache shall stay independent from
 * the number of objects in cache.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 */
public class ObjectsCache {
    /**
     * Share of the memory to be used.
     */
    private double memoryShare = 0.8;
    /**
     * Map of the loaded matches. db &gt; table &gt; object key &gt; object.
     */
    private final HashMap<Long, Object> loadedObjects = new HashMap<>();
    /**
     * Linked list to manage a queue for old entries
     */
    private final LinkedList<Long> objectQueue = new LinkedList<>();
    /**
     * Mutex for the edition of the object keys list.
     */
    private final Semaphore loadedObjectMutex = new Semaphore(1);
    /**
     * Indicates whether the cache is read only.
     */
    private boolean readOnly = false;
    /**
     * reference to the objects DB
     */
    private ObjectsDB objectsDB = null;
    
    private boolean semaphore = false;
    
    private final int keepObjectsThreshold = 1000;
    
    private final int numToCommit = 1000;

    /**
     * Constructor.
     * @param objectsDB the object database
     */
    public ObjectsCache(ObjectsDB objectsDB) {
        this.objectsDB = objectsDB;
    }

    /**
     * Returns the cache size in number of objects.
     *
     * @return the cache size in number of objects
     */
    public int getCacheSize() {
        return loadedObjects.size();
    }

    /**
     * Returns the share of heap size which can be used before emptying the
     * cache. 0.99 (default) means that objects will be removed from the cache
     * as long as more than 99% of the heap size is used.
     *
     * @return the share of heap size which can be used before emptying the
     * cache
     */
    public double getMemoryShare() {
        return memoryShare;
    }

    /**
     * Sets the share of heap size which can be used before emptying the cache.
     *
     * @param memoryShare the share of heap size which can be used before
     * emptying the cache
     */
    public void setMemoryShare(double memoryShare) {
        this.memoryShare = memoryShare;
    }

    

    /**
     * Returns the objects if present in the cache. Null if not. Warning: this
     * method returns the object as it is and does not wait for cache edition
     * operations to finish.
     *
     * @param objectKey the key of the object
     *
     * @return the object of interest, null if not present in the cache
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public Object getObject(Long objectKey) throws InterruptedException {
        loadedObjectMutex.acquire();
        Object object = null;
        if (loadedObjects.containsKey(objectKey)) object = loadedObjects.get(objectKey);
        loadedObjectMutex.release();
        return object;
    }
    
    /**
     * Removes an object from the cache
     *
     * @param objectKey the key of the object
     * @return the class name of the object
     *
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public String removeObject(long objectKey) throws InterruptedException {
        String className = null;
        loadedObjectMutex.acquire();
        if (!readOnly) {
            if (loadedObjects.containsKey(objectKey)){
                className = loadedObjects.get(objectKey).getClass().getSimpleName();
                loadedObjects.remove(objectKey);
                objectQueue.removeFirstOccurrence(objectKey);
            }
        }
        loadedObjectMutex.release();
        return className;
    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objectKey the key of the object
     * @param object the object to store in the cache
     *
     * @throws IOException if an IOException occurs while writing to the
     * database
     * @throws SQLException if an SQLException occurs while writing to the
     * database
     * @throws java.lang.InterruptedException if a threading error occurs
     * writing to the database
     */
    public void addObject(Long objectKey, Object object) throws IOException, SQLException, InterruptedException {
        loadedObjectMutex.acquire();
        semaphore = true;
        if (!readOnly) {            
            
            if (!loadedObjects.containsKey(objectKey)){
                loadedObjects.put(objectKey, object);
                objectQueue.add(objectKey);
                
                
                
                if (!((IdObject)object).getStoredInDB()){
                    ((IdObject)object).setStoredInDB(true);
                    objectsDB.getDB().makePersistent(object);
                    Long zooid = (Long)objectsDB.getDB().getObjectId(object);
                    objectsDB.getIdMap().put(objectKey, zooid);
                }
                if (objectsDB.getCurrentAdded() > numToCommit){
                    objectsDB.commit();
                    objectsDB.resetCurrentAdded();
                }
                
                
            }
            updateCache();
        }
        semaphore = false;
        loadedObjectMutex.release();
    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objects the key / objects to store in the cache
     *
     * @throws IOException if an IOException occurs while writing to the
     * database
     * @throws SQLException if an SQLException occurs while writing to the
     * database
     * @throws java.lang.InterruptedException if a threading error occurs
     * writing to the database
     */
    public void addObjects(HashMap<Long, Object> objects) throws IOException, SQLException, InterruptedException {
        loadedObjectMutex.acquire();
        semaphore = true;
        if (!readOnly) {            
            
            loadedObjects.putAll(objects);
            objectQueue.addAll(objects.keySet());
            for (Long objectKey : objects.keySet()){
                Object object = objects.get(objectKey);
                if (!((IdObject)object).getStoredInDB()){
                    ((IdObject)object).setStoredInDB(true);
                    objectsDB.getDB().makePersistent(object);
                    Long zooid = (Long)objectsDB.getDB().getObjectId(object);
                    objectsDB.getIdMap().put(objectKey, zooid);
                }
            }
            
            if (objectsDB.getCurrentAdded() > numToCommit){
                objectsDB.commit();
                objectsDB.resetCurrentAdded();
            }
            
            updateCache();
        }
        semaphore = false;
        loadedObjectMutex.release();
    }

    /**
     * Indicates whether the memory used by the application is lower than 99% of
     * the heap size.
     *
     * @return a boolean indicating whether the memory used by the application
     * is lower than 99% of the heap
     */
    private boolean memoryCheck() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() < (long) (memoryShare * Runtime.getRuntime().maxMemory());
    }
    

    /**
     * Saves an entry in the database if modified and clears it from the cache.
     *
     * @param numLastEntries number of keys of the entries
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void saveObjects(int numLastEntries) throws IOException, SQLException, InterruptedException {
        saveObjects(numLastEntries, null, true);
    }

    /**
     * Saves an entry in the database if modified and clears it from the cache.
     *
     * @param numLastEntries number of keys of the entries
     * @param waitingHandler a waiting handler displaying progress to the user.
     * Can be null. Progress will be displayed as secondary.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void saveObjects(int numLastEntries, WaitingHandler waitingHandler) throws IOException, SQLException, InterruptedException {
        saveObjects(numLastEntries, waitingHandler, true);
    }

    /**
     * Saves an entry in the database if modified.
     *
     * @param numLastEntries number of keys of the entries
     * @param waitingHandler a waiting handler displaying progress to the user.
     * Can be null. Progress will be displayed as secondary.
     * @param clearEntries a boolean indicating whether the entry shall be
     * cleared from the cache
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void saveObjects(int numLastEntries, WaitingHandler waitingHandler, boolean clearEntries) throws IOException, SQLException, InterruptedException {
        if (!semaphore) loadedObjectMutex.acquire();
        if (!readOnly) {
            System.out.println("storing " + numLastEntries + (clearEntries ? " with deleting" : " without deleting"));
            if (waitingHandler != null) {
                waitingHandler.resetSecondaryProgressCounter();
                waitingHandler.setMaxSecondaryProgressCounter(numLastEntries);
            }
            
            ListIterator<Long> listIterator = objectQueue.listIterator();
            PersistenceManager pm = objectsDB.getDB();
            
            for (int i = 0; i < numLastEntries && objectQueue.size() > 0; ++i){
                if (waitingHandler != null) {
                    waitingHandler.increaseSecondaryProgressCounter();
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                }
                long key = clearEntries ? objectQueue.pollFirst() : listIterator.next();
                
                Object obj = loadedObjects.get(key);
                if (!((IdObject)obj).getStoredInDB()){
                    ((IdObject)obj).setStoredInDB(true);
                    pm.makePersistent(obj);
                    Long zooid = (Long)pm.getObjectId(obj);
                    objectsDB.getIdMap().put(key, zooid);
                }
                
                if (clearEntries) loadedObjects.remove(key);
            }
            objectsDB.commit();
            
            
            System.out.println("storing out");
        }
        if (!semaphore) loadedObjectMutex.release();
    }
    

    /**
     * Updates the cache according to the memory settings.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the objectlongKey in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    private void updateCache() throws IOException, SQLException, InterruptedException {
        semaphore = true;
        while (!memoryCheck()){
            int toRemove = (int) (((double) loadedObjects.size()) * 0.25);
            if (loadedObjects.size() <= keepObjectsThreshold || toRemove == 0) break;
            saveObjects(toRemove, null, true);
        }
        semaphore = false;
    }
    
    /**
     * Check if key in cache
     * 
     * @param longKey key of the entry
     * @return if key in cache
     */
    public boolean inCache(long longKey){
        return loadedObjects.containsKey(longKey);
    }

    /**
     * Saves the cache content in the database.
     *
     * @param waitingHandler a waiting handler on which the progress will be
     * @param emptyCache boolean indicating whether the cache content shall be
     * cleared while saving displayed as secondary progress. can be null
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void saveCache(WaitingHandler waitingHandler, boolean emptyCache) throws IOException, SQLException, InterruptedException {
        saveObjects(loadedObjects.size(), waitingHandler, emptyCache);
    }

    /**
     * Indicates whether the cache is empty.
     *
     * @return a boolean indicating whether the cache is empty
     */
    public boolean isEmpty() {
        return loadedObjects.isEmpty();
    }

    /**
     * Sets the cache in read only.
     *
     * @param readOnly boolean indicating whether the cache should be in read
     * only
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void setReadOnly(boolean readOnly) throws InterruptedException {
        loadedObjectMutex.acquire();
        this.readOnly = readOnly;
        loadedObjectMutex.release();
    }
}
