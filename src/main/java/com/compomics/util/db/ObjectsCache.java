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
    private double memoryShare = 0.11;
    /**
     * Map of the loaded matches. db &gt; table &gt; object key &gt; object.
     */
    private final HashMap<Long, CacheEntry> loadedObjects = new HashMap<Long, CacheEntry>();
    /**
     * Linked list to manage a queue for old entries
     */
    private final LinkedList<Long> objectQueue = new LinkedList<Long>();
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
        Object object = null;
        loadedObjectMutex.acquire();
        if (loadedObjects.containsKey(objectKey)) object = loadedObjects.get(objectKey).getObject();
        loadedObjectMutex.release();
        return object;
    }

    /**
     * Sets that a match has been modified and returns true in case of success.
     *
     * @param objectKey the key of the object
     * @param object the object
     *
     * @return returns a boolean indicating that the entry was in cache and has
     * been updated. False otherwise.
     *
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public boolean updateObject(long objectKey, Object object) throws InterruptedException {
        if (!readOnly) {
            loadedObjectMutex.acquire();
            CacheEntry entry = loadedObjects.get(objectKey);
            boolean result = false;
            if (entry != null) {
                entry.setObject(object);
                result = true;
            }
            loadedObjectMutex.release();
            return result;
        }
        return false;
    }
    
    /**
     * Removes an object from the cache
     *
     * @param objectKey the key of the object
     *
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void removeObject(long objectKey) throws InterruptedException {
        if (!readOnly) {
            loadedObjectMutex.acquire();
            if (loadedObjects.containsKey(objectKey)){
                loadedObjects.remove(objectKey);
                objectQueue.removeFirstOccurrence(objectKey);
            }
            loadedObjectMutex.release();
        }
    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objectKey the key of the object
     * @param object the object to store in the cache
     * @param modifiedOrNew true if the object is modified or new
     *
     * @throws IOException if an IOException occurs while writing to the
     * database
     * @throws SQLException if an SQLException occurs while writing to the
     * database
     * @throws java.lang.InterruptedException if a threading error occurs
     * writing to the database
     */
    public void addObject(Long objectKey, Object object, boolean modifiedOrNew) throws IOException, SQLException, InterruptedException {
        if (!readOnly) {            
            
            loadedObjectMutex.acquire();
            if (!loadedObjects.containsKey(objectKey)){
                loadedObjects.put(objectKey, new CacheEntry(object, modifiedOrNew));
                objectQueue.add(objectKey);
            }
            loadedObjectMutex.release();
            updateCache();
        }
    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objects the key / objects to store in the cache
     * @param modifiedOrNew true if the object is modified or new
     *
     * @throws IOException if an IOException occurs while writing to the
     * database
     * @throws SQLException if an SQLException occurs while writing to the
     * database
     * @throws java.lang.InterruptedException if a threading error occurs
     * writing to the database
     */
    public void addObjects(HashMap<Long, Object> objects, boolean modifiedOrNew) throws IOException, SQLException, InterruptedException {
        if (!readOnly) {            
            
            loadedObjectMutex.acquire();
            for (Long objectKey : objects.keySet()){
                if (!loadedObjects.containsKey(objectKey)){
                    loadedObjects.put(objectKey, new CacheEntry(objects.get(objectKey), modifiedOrNew));
                    objectQueue.add(objectKey);
                }
                loadedObjectMutex.release();
                updateCache();
            }
        }
    }

    /**
     * Indicates whether the memory used by the application is lower than 99% of
     * the heap size.
     *
     * @return a boolean indicating whether the memory used by the application
     * is lower than 99% of the heap
     */
    public boolean memoryCheck() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() < (long) (memoryShare * Runtime.getRuntime().maxMemory());
    }

    
    /**
     * Clears the cache and dumps everything into the database.
     * 
     *
     * @throws IOException if an IOException occurs while writing to the
     * database
     * @throws SQLException if an SQLException occurs while writing to the
     * database
     * @throws java.lang.InterruptedException if a threading error occurs
     * writing to the database
     */
    public void clearCache() throws IOException, SQLException, InterruptedException {
        if (!readOnly) {
            saveObjects(loadedObjects.size());
        }
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
        if (!readOnly) {
            loadedObjectMutex.acquire();
            if (waitingHandler != null) {
                waitingHandler.resetSecondaryProgressCounter();
                waitingHandler.setMaxSecondaryProgressCounter(numLastEntries);
            }
            
            ListIterator<Long> listIterator = objectQueue.listIterator();
            PersistenceManager pm = objectsDB.getDB();
            for (int i = 0; i < numLastEntries; ++i){
                if (waitingHandler != null) {
                    waitingHandler.increaseSecondaryProgressCounter();
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                }
            

                long key = -1;
                if (clearEntries){
                    key = objectQueue.pollFirst();
                }
                else {
                    key = listIterator.next();
                }
                
                CacheEntry entry = loadedObjects.get(key);
                Object obj = entry.getObject();
                if (!objectsDB.getIdMap().containsKey(key)){
                    System.out.println("storing " + obj.getClass().getSimpleName());
                    ((IdObject)obj).setModified(false);
                    pm.makePersistent(obj);
                    Long zooid = (Long)pm.getObjectId(obj);
                    ((IdObject)obj).setId(zooid);
                    objectsDB.getIdMap().put(key, zooid);
                }
                else if (((IdObject)obj).getModified()){
                    System.out.println("storing " + obj.getClass().getSimpleName());
                    ((IdObject)obj).setModified(false);
                }
                
                if (clearEntries) loadedObjects.remove(key);
            }
            pm.currentTransaction().commit();
            pm.currentTransaction().begin();
            loadedObjectMutex.release();
            
            
        }
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
    public void updateCache() throws IOException, SQLException, InterruptedException {
        if (!memoryCheck()){
            int toRemove = (int) (((double) loadedObjects.size()) * 0.25);
            saveObjects(toRemove, null, true);
        }
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
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Class representing a cache entry.
     */
    private class CacheEntry {

        /**
         * The object of this entry.
         */
        private Object object;
        /**
         * A boolean indicating whether this entry is modified when compared to
         * the version of the database. Only modified entries will be saved when
         * the cache is emptied.
         */
        private boolean modified;

        /**
         * Constructor.
         *
         * @param object the object of the entry
         * @param modified boolean indicating whether the entry is modified
         */
        public CacheEntry(Object object, boolean modified) {
            this.object = object;
            this.modified = modified;
        }

        /**
         * Returns the object of this entry.
         *
         * @return the object contained by this entry
         */
        public Object getObject() {
            return object;
        }

        /**
         * Sets the object of this cache entry.
         *
         * @param object the object for this entry
         */
        public void setObject(Object object) {
            this.object = object;
        }
    }
}
