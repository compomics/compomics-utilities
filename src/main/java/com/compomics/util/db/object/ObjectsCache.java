package com.compomics.util.db.object;

import static com.compomics.util.db.object.DbMutex.loadObjectMutex;
import com.compomics.util.waiting.WaitingHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * Share of the memory to be used.
     */
    private double memoryShare = 0.75;
    /**
     * Map of the loaded matches. db &gt; table &gt; object key &gt; object.
     */
    private final ConcurrentHashMap<Integer, ObjectsCacheElement> loadedObjects = new ConcurrentHashMap<>();
    /**
     * HashMap to store the class type of the objects in cache.
     */
    private HashMap<Class, HashSet<Integer>> classMap = new HashMap<>();
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

        loadObjectMutex.acquire();
        int size = loadedObjects.size();
        loadObjectMutex.release();

        return size;
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
     * Returns the object if present in the cache. Null if not.
     *
     * @param objectKey the key of the object
     *
     * @return the object of interest, null if not present in the cache
     */
    public Object getObject(int objectKey) {

        loadObjectMutex.acquire();

        Object object = loadedObjects.get(objectKey).object;

        loadObjectMutex.release();

        return object;
    }

    /**
     * Removes an object from the cache.
     *
     * @param objectKey the key of the object
     */
    public void removeObject(int objectKey) {

        if (!readOnly) {

            loadObjectMutex.acquire();
            boolean contains = loadedObjects.containsKey(objectKey);
            loadObjectMutex.release();

            if (contains) {

                Object object = getObject(objectKey);
                loadObjectMutex.acquire();
                classMap.get(object.getClass()).remove(objectKey);
                loadedObjects.remove(objectKey);
                loadObjectMutex.release();

            }
        }
    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objectKey the key of the object
     * @param object the object to store in the cache
     */
    public void addObject(int objectKey, Object object) {

        addObject(objectKey, object, false, false);

    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objectKey the key of the object
     * @param object the object to store in the cache
     * @param inDB the database state
     * @param edited the edited state
     */
    public void addObject(int objectKey, Object object, boolean inDB, boolean edited) {

        if (!readOnly) {

            loadObjectMutex.acquire();

            ObjectsCacheElement objectsCacheElement = loadedObjects.get(objectKey);

            if (objectsCacheElement == null) {

                loadedObjects.put(objectKey, new ObjectsCacheElement(object, inDB, edited));

            } else {

                objectsCacheElement.object = object;
                objectsCacheElement.inDB = inDB;
                objectsCacheElement.edited = edited;

            }

            HashSet<Integer> keysForClass = classMap.get(object.getClass());

            if (keysForClass == null) {

                keysForClass = new HashSet<>();
                classMap.put(object.getClass(), keysForClass);
            }

            keysForClass.add(objectKey);

            loadObjectMutex.release();

            updateCache();

        }

    }

    /**
     * Adds objects to the cache. The objects must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objects the key / objects to store in the cache
     *
     */
    public void addObjects(HashMap<Integer, Object> objects) {
        addObjects(objects, false, false);
    }

    /**
     * Adds objects to the cache. The objects must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently overwritten.
     *
     * @param objects the key / objects to store in the cache
     * @param inDB their database state
     * @param edited their editing state
     *
     */
    public void addObjects(HashMap<Integer, Object> objects, boolean inDB, boolean edited) {

        if (!readOnly) {

            loadObjectMutex.acquire();

            for (Entry<Integer, Object> kv : objects.entrySet()) {

                int objectKey = kv.getKey();
                Object object = kv.getValue();

                ObjectsCacheElement objectsCacheElement = loadedObjects.get(objectKey);

                if (objectsCacheElement == null) {

                    loadedObjects.put(objectKey, new ObjectsCacheElement(object, inDB, edited));

                } else {

                    objectsCacheElement.object = object;
                    objectsCacheElement.inDB = inDB;
                    objectsCacheElement.edited = edited;

                }

                HashSet<Integer> keysForClass = classMap.get(object.getClass());

                if (keysForClass == null) {

                    keysForClass = new HashSet<>();
                    classMap.put(object.getClass(), keysForClass);
                }

                keysForClass.add(objectKey);

            }

            loadObjectMutex.release();

            updateCache();

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

        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                < (long) (memoryShare * Runtime.getRuntime().maxMemory());

    }

    /**
     * Saves entries in the database if modified and clears it from the cache.
     *
     * @param numLastEntries number of keys of the entries
     */
    public void saveObjects(int numLastEntries) {
        saveObjects(numLastEntries, null, true);
    }

    /**
     * Saves entries in the database if modified.
     *
     * @param numLastEntries number of keys of the entries
     * @param waitingHandler a waiting handler displaying progress to the user.
     * Can be null. Progress will be displayed as secondary.
     * @param clearEntries a boolean indicating whether the entry shall be
     * cleared from the cache
     */
    public void saveObjects(
            int numLastEntries,
            WaitingHandler waitingHandler,
            boolean clearEntries
    ) {

        if (!readOnly) {

            Connection connection = objectsDB.getDB();

            try {

                PreparedStatement psInsert = connection.prepareStatement("INSERT INTO data (id, class, data) VALUES (?, ?, ?);");
                PreparedStatement psUpdate = connection.prepareStatement("UPDATE data SET data = ? WHERE id = ?;");

                loadObjectMutex.acquire();

                HashSet<Integer> keysInBackend = objectsDB.getKeysInBackend();
                Set<Integer> removeKeys = new HashSet<>();

                int i = 0;

                for (Entry<Integer, ObjectsCacheElement> entry : loadedObjects.entrySet()) {

                    if (numLastEntries <= i++) {
                        break;
                    }

                    if (waitingHandler != null) {

                        waitingHandler.increaseSecondaryProgressCounter();

                        if (waitingHandler.isRunCanceled()) {
                            break;
                        }
                    }

                    int key = entry.getKey();

                    ObjectsCacheElement obj = loadedObjects.get(key);
                    obj.edited = false;

                    // standard java serialization
                    byte[] barray;
                    try ( ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                        try ( ObjectOutputStream out = new ObjectOutputStream(bos)) {
                            out.writeObject(obj.object);
                            out.flush();
                        }

                        barray = bos.toByteArray();
                        
                    }

                    if (obj.inDB) {

                        psUpdate.setBytes(1, barray);
                        psUpdate.setInt(2, key);
                        psUpdate.addBatch();

                    } else {

                        psInsert.setInt(1, key);
                        psInsert.setString(2, obj.object.getClass().getName());
                        psInsert.setBytes(3, barray);
                        psInsert.addBatch();
                        keysInBackend.add(key);

                    }

                    obj.inDB = true;

                    if (clearEntries) {

                        removeKeys.add(key);
                        classMap.get(obj.object.getClass()).remove(key);

                    }
                }

                psInsert.executeBatch();
                psUpdate.executeBatch();

                if (!removeKeys.isEmpty()) {

                    loadedObjects.keySet().removeAll(removeKeys);

                }

                connection.commit();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
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

        int cacheSize = getCacheSize();

        while (cacheSize > keepObjectsThreshold && !memoryCheck()) {

            int toRemove = cacheSize >> 2;
            saveObjects(toRemove, null, true);
            cacheSize = getCacheSize();

        }
    }

    /**
     * Checks if a given key is in the cache.
     *
     * @param key key of the entry
     * @return if key in cache
     */
    public boolean inCache(int key) {

        loadObjectMutex.acquire();
        boolean contains = loadedObjects.containsKey(key);
        loadObjectMutex.release();

        return contains;
        
    }

    /**
     * Saves the cache content to the database.
     *
     * @param waitingHandler a waiting handler on which the progress will be
     * displayed
     * @param emptyCache boolean indicating whether the cache content shall be
     * cleared while saving displayed as secondary progress. Can be null.
     */
    public void saveCache(WaitingHandler waitingHandler, boolean emptyCache) {

        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(loadedObjects.size() + 1);
        }

        int cacheSize = getCacheSize();
        saveObjects(cacheSize, waitingHandler, emptyCache);

        if (waitingHandler != null) {

            waitingHandler.setSecondaryProgressCounterIndeterminate(true);

        }
    }

    /**
     * Indicates whether the cache is empty.
     *
     * @return a boolean indicating whether the cache is empty
     */
    public boolean isEmpty() {

        loadObjectMutex.acquire();
        boolean isEmpty = loadedObjects.isEmpty();
        loadObjectMutex.release();

        return isEmpty;
    }

    /**
     * Clears the cache.
     */
    public void clearCache() {

        loadObjectMutex.acquire();
        loadedObjects.clear();
        loadObjectMutex.release();

    }

    /**
     * Sets the cache in read only mode.
     *
     * @param readOnly boolean indicating whether the cache should be in read
     * only mode
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Returns the class type of the objects in cache.
     *
     * @param className the class name
     * @return the class type of the objects in cache
     */
    public HashSet<Integer> getClassInCache(Class className) {
        return classMap.get(className);
    }

    /**
     * Objects cache element.
     */
    public class ObjectsCacheElement {

        /**
         * The object.
         */
        public Object object;
        /**
         * The database state.
         */
        public boolean inDB;
        /**
         * The edited state.
         */
        public boolean edited;

        /**
         * Constructor.
         *
         * @param object the object
         */
        public ObjectsCacheElement(Object object) {
            this(object, false, true);
        }

        /**
         * Constructor.
         *
         * @param object the object
         * @param inDB the database state
         * @param edited the edited state
         */
        public ObjectsCacheElement(
                Object object,
                boolean inDB,
                boolean edited
        ) {

            this.object = object;
            this.inDB = inDB;
            this.edited = edited;

        }
    }
}
