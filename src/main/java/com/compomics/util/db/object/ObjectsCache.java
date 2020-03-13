package com.compomics.util.db.object;

import static com.compomics.util.db.object.DbMutex.loadObjectMutex;
import com.compomics.util.waiting.WaitingHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import org.nustaq.serialization.FSTConfiguration;

/**
 * An object cache can be combined to an ObjectDB to improve its performance. A
 * single cache can be used by different databases. This ought not to be
 * serialized. The length of lists/maps in the cache shall stay independent from
 * the number of objects in cache.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 * @author Harald Barsnes
 */
public class ObjectsCache {

    public class ObjectsCacheElement {
        public Object object;
        public boolean inDB;
        public boolean edited;
        
        
        
        public ObjectsCacheElement(Object object){
            this(object, false, false);
        }
        
        public ObjectsCacheElement(Object object, boolean inDB, boolean edited){
            this.object = object;
            this.inDB = inDB;
            this.edited = edited;
        }
    }
    
    
    
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
    private final HashMap<Long, ObjectsCacheElement> loadedObjects = new HashMap<>();
    /**
     * Linked list to manage a queue for old entries.
     */
    private final LinkedList<Long> objectQueue = new LinkedList<>();
    /**
     * Indicates whether the cache is read only.
     */
    private boolean readOnly = false;
    /**
     * Reference to the objects DB.
     */
    private ObjectsDB objectsDB = null;
    /**
     * Number of objects that should at least be kept.
     */
    private final int keepObjectsThreshold = 10000;
    static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

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
            loadObjectMutex.acquire();
            updateCache();
            loadObjectMutex.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the objects if present in the cache. Null if not.
     *
     * @param objectKey the key of the object
     *
     * @return the object of interest, null if not present in the cache
     */
    public Object getObject(Long objectKey) {
        Object object = null;

        if (loadedObjects.containsKey(objectKey)) {
            object = loadedObjects.get(objectKey).object;
        }

        return object;
    }
    

    /**
     * Removes an object from the cache.
     *
     * @param objectKey the key of the object
     *
     * @return the class name of the object
     */
    public String removeObject(long objectKey) {

        String className = null;

        if (!readOnly) {
            if (loadedObjects.containsKey(objectKey)) {
                className = loadedObjects.get(objectKey).object.getClass().getSimpleName();
                loadedObjects.remove(objectKey);
                objectQueue.removeFirstOccurrence(objectKey);
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

        if (!readOnly) {
            if (!loadedObjects.containsKey(objectKey)) {
                loadedObjects.put(objectKey, new ObjectsCacheElement(object));
                objectQueue.add(objectKey);
            }
            updateCache();
        }

    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objects the key / objects to store in the cache
     *
     */
    public void addObjects(HashMap<Long, Object> objects) {

        loadObjectMutex.acquire();

        if (!readOnly) {
            for (Entry<Long, Object>kv : objects.entrySet()){
               loadedObjects.put(kv.getKey(), new ObjectsCacheElement(kv.getValue()));
            }
            objectQueue.addAll(objects.keySet());

            updateCache();
        }

        loadObjectMutex.release();

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
     */
    public void saveObjects(int numLastEntries) {
        loadObjectMutex.acquire();
        saveObjects(numLastEntries, null, true);
        loadObjectMutex.release();
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

        if (!readOnly) {
            Connection connection = objectsDB.getDB();
            ListIterator<Long> listIterator = objectQueue.listIterator();
            PreparedStatement psInsert = null, psUpdate = null;
            try {
                psInsert = connection.prepareStatement("INSERT INTO data (id, class, data) VALUES (?, ?, ?);");
                psUpdate = connection.prepareStatement("UPDATE data SET data = ? WHERE id = ?;");
    
            }
            catch (Exception e){
                e.printStackTrace();
            }
            
            for (int i = 0; i < numLastEntries && objectQueue.size() > 0; ++i) {

                if (waitingHandler != null) {

                    waitingHandler.increaseSecondaryProgressCounter();

                    if (waitingHandler.isRunCanceled()) {

                        break;

                    }
                }

                long key = clearEntries ? objectQueue.pollFirst() : listIterator.next();
                ObjectsCacheElement obj = loadedObjects.get(key);
                
                byte barray[] = conf.asByteArray(obj.object);
                DbObject a = (DbObject)obj.object;
                try {
                    if (obj.inDB){
                        psUpdate.setBytes(1, barray);
                        psUpdate.setLong(2, ((DbObject)obj.object).getId());
                        psUpdate.addBatch();
                    }
                    else {
                        psInsert.setLong(1, ((DbObject)obj.object).getId());
                        psInsert.setString(2, obj.object.getClass().getSimpleName());
                        psInsert.setBytes(3, barray);
                        psInsert.addBatch();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                    
                if (clearEntries) {
                    loadedObjects.remove(key);
                }

            }

            try {
                loadObjectMutex.acquire();
                psInsert.executeBatch();
                psUpdate.executeBatch();
                connection.commit();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                loadObjectMutex.release();
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
        }
    }

    /**
     * Check if key in cache.
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
     * displayed
     * @param emptyCache boolean indicating whether the cache content shall be
     * cleared while saving displayed as secondary progress. Can be null.
     */
    public void saveCache(WaitingHandler waitingHandler, boolean emptyCache) {

        loadObjectMutex.acquire();


        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(loadedObjects.size() + 1); // @TODO: can this number get bigger than the max integer value? 
        }

        saveObjects(loadedObjects.size(), waitingHandler, emptyCache);

        if (waitingHandler != null) {

            waitingHandler.setSecondaryProgressCounterIndeterminate(true);

        }

        loadObjectMutex.release();

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
     * Clears the cache.
     */
    public void clearCache() {
        loadedObjects.clear();
        objectQueue.clear();
    }

    /**
     * Sets the cache in read only.
     *
     * @param readOnly boolean indicating whether the cache should be in read
     * only
     */
    public void setReadOnly(boolean readOnly) {

        loadObjectMutex.acquire();

        this.readOnly = readOnly;

        loadObjectMutex.release();

    }
}