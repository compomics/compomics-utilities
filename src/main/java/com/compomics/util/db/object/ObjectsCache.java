package com.compomics.util.db.object;

import com.compomics.util.waiting.WaitingHandler;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
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
     * Empty default constructor
     */
    public ObjectsCache() {
    }

    /**
     * Share of the memory to be used.
     */
    private double memoryShare = 0.75;
    /**
     * Map of the loaded matches. db &gt; table &gt; object key &gt; object.
     */
    private final HashMap<Long, Object> loadedObjects = new HashMap<>();
    /**
     * Linked list to manage a queue for old entries.
     */
    private final LinkedList<Long> objectQueue = new LinkedList<>();
    /**
     * Mutex for the edition of the object keys list.
     */
    private final Object loadedObjectMutex = new Object();
    /**
     * Indicates whether the cache is read only.
     */
    private boolean readOnly = false;
    /**
     * Reference to the objects DB.
     */
    private ObjectsDB objectsDB = null;
    /**
     * Number of objects thats should be at least kept.
     */
    private final int keepObjectsThreshold = 10000;
    /**
     * If number number of registered objects exceeds value, commit to db should
     * be triggered.
     */
    private final int numToCommit = 10000;

    /**
     * Constructor.
     *
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
     * cache. 0.75 (default) means that objects will be removed from the cache
     * as long as more than 75% of the heap size is used.
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
        try {
            updateCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the objects if present in the cache. Null if not. Warning: this
     * method returns the object as it is and does not wait for cache edition
     * operations to finish.
     *
     * @param objectKey the key of the object
     *
     * @return the object of interest, null if not present in the cache
     */
    public Object getObject(Long objectKey) {

        updateCache();
        Object object = null;

        synchronized (loadedObjectMutex) {

            if (loadedObjects.containsKey(objectKey)) {
                object = loadedObjects.get(objectKey);
            }

        }

        return object;
    }

    /**
     * Removes an object from the cache
     *
     * @param objectKey the key of the object
     * @return the class name of the object
     *
     * @throws InterruptedException if the thread is interrupted
     */
    public String removeObject(long objectKey) throws InterruptedException {
        String className = null;
        synchronized (loadedObjectMutex) {
            if (!readOnly) {
                if (loadedObjects.containsKey(objectKey)) {
                    className = loadedObjects.get(objectKey).getClass().getSimpleName();
                    loadedObjects.remove(objectKey);
                    objectQueue.removeFirstOccurrence(objectKey);
                }
            }
        }
        return className;
    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objectKey the key of the object
     * @param object the object to store in the cache
     */
    public void addObject(Long objectKey, Object object) {

        synchronized (loadedObjectMutex) {

            if (!readOnly) {

                if (!loadedObjects.containsKey(objectKey)) {

                    loadedObjects.put(objectKey, object);
                    objectQueue.add(objectKey);

                    if (objectsDB.getCurrentAdded() > numToCommit) {
                        objectsDB.commit();
                    }
                }

                updateCache();

            }
        }
    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objects the key / objects to store in the cache
     * @throws InterruptedException if the thread is interrupted
     *
     */
    public void addObjects(HashMap<Long, Object> objects) throws InterruptedException {
        synchronized (loadedObjectMutex) {
            if (!readOnly) {

                loadedObjects.putAll(objects);
                objectQueue.addAll(objects.keySet());

                if (objectsDB.getCurrentAdded() > numToCommit) {
                    objectsDB.commit();
                }

                updateCache();
            }
        }
    }

    /**
     * Indicates whether the memory used by the application is lower than 99% of
     * the heap size.
     *
     * @return a boolean indicating whether the memory used by the application
     * is lower than 75% of the heap
     */
    private boolean memoryCheck() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() < (long) (memoryShare * Runtime.getRuntime().maxMemory());
    }

    /**
     * Saves an entry in the database if modified and clears it from the cache.
     *
     * @param numLastEntries number of keys of the entries
     *
     * @throws InterruptedException if the thread is interrupted
     */
    public void saveObjects(int numLastEntries) throws InterruptedException {
        saveObjects(numLastEntries, null, true);
    }

    /**
     * Saves an entry in the database if modified and clears it from the cache.
     *
     * @param numLastEntries number of keys of the entries
     * @param waitingHandler a waiting handler displaying progress to the user.
     * Can be null. Progress will be displayed as secondary.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    public void saveObjects(int numLastEntries, WaitingHandler waitingHandler) throws InterruptedException {
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
     */
    public void saveObjects(int numLastEntries, WaitingHandler waitingHandler, boolean clearEntries) {

        synchronized (loadedObjectMutex) {

            if (!readOnly) {
                if (waitingHandler != null) {

                    waitingHandler.resetSecondaryProgressCounter();
                    waitingHandler.setMaxSecondaryProgressCounter(numLastEntries);

                }

                ListIterator<Long> listIterator = objectQueue.listIterator();
                PersistenceManager pm = objectsDB.getDB();


                for (int i = 0; i < numLastEntries && objectQueue.size() > 0; ++i) {

                    if (waitingHandler != null) {

                        waitingHandler.increaseSecondaryProgressCounter();

                        if (waitingHandler.isRunCanceled()) {

                            break;

                        }
                    }

                    long key = clearEntries ? objectQueue.pollFirst() : listIterator.next();

                    Object obj = loadedObjects.get(key);

                    if (!((DbObject) obj).jdoZooIsPersistent()) {

                        pm.makePersistent(obj);
                        objectsDB.getIdMap().put(key, ((DbObject) obj).jdoZooGetOid());

                    }

                    if (clearEntries) {
                        loadedObjects.remove(key);
                    }

                }

                objectsDB.commit();

            }
        }
    }

    /**
     * Updates the cache according to the memory settings.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    private void updateCache() {

        while (loadedObjects.size() > keepObjectsThreshold && !memoryCheck()) {

            int toRemove = loadedObjects.size() >> 2;
            saveObjects(toRemove, null, true);

            // turning on the garbage collector from time to time
            // helps to keep the memory clean. Performance becomes
            // better, the mass for cleaning is lower
            System.gc();

        }
    }

    /**
     * Check if key in cache
     *
     * @param longKey key of the entry
     * @return if key in cache
     */
    public boolean inCache(long longKey) {
        return loadedObjects.containsKey(longKey);
    }

    /**
     * Saves the cache content in the database.
     *
     * @param waitingHandler a waiting handler on which the progress will be
     * @param emptyCache boolean indicating whether the cache content shall be
     * cleared while saving displayed as secondary progress. can be null
     */
    public void saveCache(WaitingHandler waitingHandler, boolean emptyCache) {

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
     * @throws InterruptedException if the thread is interrupted
     */
    public void setReadOnly(boolean readOnly) throws InterruptedException {
        synchronized (loadedObjectMutex) {
            this.readOnly = readOnly;
        }
    }
}
