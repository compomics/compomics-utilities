package com.compomics.util.db.object;

import com.compomics.util.waiting.WaitingHandler;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.zoodb.internal.util.SynchronizedROCollection;

import org.zoodb.jdo.ZooJdoHelper;
import org.zoodb.tools.ZooHelper;

/**
 * A database which can easily be used to store objects. Note: most operations
 * with the object DB are synchronized or use semaphores. Should a thread be
 * interrupted, the exception will be sent as RunTimeException. Same if MD5 is
 * not supported. This is because our tools recover from these exceptions
 * similarly as for other unchecked exceptions. Please contact us if you need
 * another/better exception handling.
 *
 * @author Marc Vaudel
 * @author dominik.kopczynski
 */
public class ObjectsDB {

    /**
     * The version UID for serialization/deserialization compatibility.
     */
    static final long serialVersionUID = -8595805180622832745L;
    /**
     * The name of the database.
     */
    private String dbName;
    /**
     * The path to the database.
     */
    private String path;
    /**
     * The cache to be used for the objects.
     */
    private ObjectsCache objectsCache;
    /**
     * A boolean indicating whether the database is being queried.
     */
    private boolean loading = false;
    /**
     * Mutex for the interaction with the database.
     */
    private final Semaphore dbMutex = new Semaphore(1);
    /**
     * Debug, if true, all interaction with the database will be logged in the
     * System.out stream.
     */
    private static boolean debugInteractions = false;
    /**
     * OrientDB database connection
     */
    private PersistenceManager pm = null;
    /**
     * HashMap to map hash IDs of entries into DB ids
     */
    private final HashMap<Long, Long> idMap = new HashMap<>();
    /**
     * path of the database folder
     */
    private File dbFolder = null;
    /**
     * the actual db file
     */
    private File dbFile = null;

    private boolean connectionActive = false;

    private final HashMap<String, HashSet<Long>> classCounter = new HashMap<>();

    private int currentAdded = 0;

    private final static Object forCommit = new Object();

    private final static Object rWObject = new Object();

    private static int readWriteCounter = 0;

    private final static Semaphore blockCommit = new Semaphore(1);

    public static void increaseRWCounter() {
        synchronized (forCommit) {
            //System.out.println("inside commit");
        }
        synchronized (rWObject) {

            try {
                if (readWriteCounter == 0) {
                    blockCommit.acquire();
                }
                readWriteCounter++;

            } catch (InterruptedException e) {

                throw new RuntimeException(e);

            }
        }
    }

    public static void decreaseRWCounter() {
        synchronized (rWObject) {
            readWriteCounter--;
            if (readWriteCounter == 0) {
                blockCommit.release();
            }
        }
    }

    public void resetCurrentAdded() {
        currentAdded = 0;
    }

    public int getCurrentAdded() {
        return currentAdded;
    }

    public void commit() {

        try {

            System.out.println("start commit");
            synchronized (forCommit) {
                System.out.println("commit locked");

                blockCommit.acquire();
                blockCommit.release();
                //while(readWriteCounter > 0){}

                pm.currentTransaction().commit();
                pm.currentTransaction().begin();

                System.out.println("commit unlocking");
            }
            System.out.println("end commit");

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     *
     * @throws java.io.IOException thrown if the database folder could not be
     * created
     */
    public ObjectsDB(String folder, String dbName) throws IOException {
        this(folder, dbName, true);
    }

    /**
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     * @param overwrite overwriting old database
     *
     * @throws java.io.IOException thrown if the database folder could not be
     * created
     */
    public ObjectsDB(String folder, String dbName, boolean overwrite) throws IOException {
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " Creating database");
        }

        dbFolder = new File("/" + folder);

        if (!dbFolder.exists()) {
            if (!dbFolder.mkdirs()) {
                throw new IOException("cannot create database folder");
            }
        }

        dbFile = new File(dbFolder, dbName);
        if (dbFile.exists() && overwrite) {
            ZooHelper.removeDb(dbFile.getAbsolutePath());
        }

        establishConnection();
        objectsCache = new ObjectsCache(this);

    }

    public HashMap<Long, Long> getIdMap() {
        return idMap;
    }

    public File getDbFile() {
        return dbFile;
    }

    public File getDbFolder() {
        return dbFolder;
    }

    public Semaphore getDbMutex() {
        return dbMutex;
    }

    public PersistenceManager getDB() {
        return pm;
    }

    /**
     * Creates the long-based key from a string based key by taking the MD5
     * hash. Note: if MD5 is not supported, a runtime exception will be thrown.
     *
     * @param key a string based key
     *
     * @return the long-based key
     */
    public long createLongKey(String key) {

        try {

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(key));
            String md5Key = String.format("%032x", new BigInteger(1, md5.digest()));
            long longKey = 0;
            for (int i = 0; i < 32; ++i) {
                longKey |= ((long) (md5Key.charAt(i) - '0')) << ((i * 11) % 63);
            }
            return longKey;

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Returns the database name.
     *
     * @return the database name
     */
    public String getName() {
        return dbName;
    }

    /**
     * Returns the cache used by this database.
     *
     * @return the cache used by this database
     */
    public ObjectsCache getObjectsCache() {
        return objectsCache;
    }

    /**
     * Sets the object cache to be used by this database.
     *
     * @param objectCache the object cache to be used by this database
     */
    public void setObjectCache(ObjectsCache objectCache) {
        this.objectsCache = objectCache;
    }

    /**
     * Stores an object in the desired table. When multiple objects are to be
     * inserted, use insertObjects instead.
     *
     * @param objectKey the key of the object
     * @param object the object to store
     */
    public void insertObject(String objectKey, Object object) {
        try {
        dbMutex.acquire();
        long longKey = createLongKey(objectKey);

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " Inserting single object " + object.getClass().getSimpleName() + ", key: " + objectKey + "  /  " + longKey);
        }
        if (object == null) {
            throw new IllegalArgumentException("error: null insertion: " + objectKey);
        }

        ((DbObject) object).setId(longKey);
        ((DbObject) object).setFirstLevel(true);
        if (!idMap.containsKey(longKey)) {
            idMap.put(longKey, 0l);
            if (!classCounter.containsKey(object.getClass().getSimpleName())) {
                classCounter.put(object.getClass().getSimpleName(), new HashSet<>());
            }
            classCounter.get(object.getClass().getSimpleName()).add(longKey);

        } else {
            throw new IllegalArgumentException("error double insertion: " + objectKey);
        }
        currentAdded += 1;
        objectsCache.addObject(longKey, object);
        dbMutex.release();

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Returns an iterator of all objects of a given class
     *
     * @param className the class name
     * @return the iterator
     */
    public HashSet<Long> getClassObjects(Class className) {
        return classCounter.get(className.getSimpleName());
    }

    /**
     * Returns an iterator of all objects of a given class
     *
     * @param className the class name
     * @param filters filters for the class
     * @return the iterator
     */
    public Iterator<?> getObjectsIterator(Class className, String filters) {

        try {

            dbMutex.acquire();
            dumpToDB();
            Query q = pm.newQuery(className, filters);
            dbMutex.release();
            return ((SynchronizedROCollection<?>) q.execute()).iterator();

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Inserts a set of objects in the given table.
     *
     * @param objects map of the objects (object key &gt; object)
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The progress will be displayed on the secondary progress bar.
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     */
    public void insertObjects(HashMap<String, Object> objects, WaitingHandler waitingHandler, boolean displayProgress) {

        try {
            dbMutex.acquire();
            HashMap<Long, Object> objectsToAdd = new HashMap<>(objects.size());
            for (String objectKey : objects.keySet()) {
                Object object = objects.get(objectKey);
                if (debugInteractions) {
                    System.out.println(System.currentTimeMillis() + " Inserting single object, table: " + object.getClass().getName() + ", key: " + objectKey);
                }

                if (object == null) {
                    throw new IllegalArgumentException("error: null insertion: " + objectKey);
                }

                long longKey = createLongKey(objectKey);
                ((DbObject) object).setId(longKey);
                ((DbObject) object).setFirstLevel(true);
                if (!idMap.containsKey(longKey)) {
                    idMap.put(longKey, 0l);
                    if (!classCounter.containsKey(object.getClass().getSimpleName())) {
                        classCounter.put(object.getClass().getSimpleName(), new HashSet<>());
                    }
                    classCounter.get(object.getClass().getSimpleName()).add(longKey);
                    objectsToAdd.put(longKey, object);
                } else {
                    throw new IllegalArgumentException("error double insertion: " + objectKey);
                }
            }
            currentAdded += objects.size();
            objectsCache.addObjects(objectsToAdd);
            dbMutex.release();

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Loads some objects from a table in the cache.
     *
     * @param keys the keys of the objects to load
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @return returns the list of hashed keys
     */
    public ArrayList<Long> loadObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) {

        try {

            dbMutex.acquire();
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " loading " + keys.size() + " objects");
            }

            HashMap<Long, Object> allObjects = new HashMap<>();
            ArrayList<Long> hashedKeys = new ArrayList<>();
            for (String objectKey : keys) {
                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    break;
                }
                long longKey = createLongKey(objectKey);
                hashedKeys.add(longKey);
                Long zooid = idMap.get(longKey);
                if (!objectsCache.inCache(longKey) && zooid != null && zooid != 0) {
                    Object obj = pm.getObjectById(zooid);
                    allObjects.put(longKey, obj);
                }

            }
            if (hashedKeys.size() != keys.size()) {
                throw new IllegalArgumentException("Array sizes in function do not match, " + keys.size() + " vs. " + hashedKeys.size());
            }
            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
                objectsCache.addObjects(allObjects);
            }

            dbMutex.release();
            return hashedKeys;

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Loads all objects from a given class.
     *
     * @param className the class name of the objects to be retrieved
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @return returns the list of hashed keys
     */
    public ArrayList<Long> loadObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) {

        try {
            dbMutex.acquire();
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
            }

            HashMap<Long, Object> allObjects = new HashMap<>();
            HashSet<Long> hashedKeys = classCounter.get(className.getSimpleName());
            for (Long longKey : hashedKeys) {
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
                Long zooid = idMap.get(longKey);
                if (!objectsCache.inCache(longKey) && zooid != null && zooid != 0) {
                    allObjects.put(longKey, pm.getObjectById(zooid));
                }

            }
            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
                objectsCache.addObjects(allObjects);
            }
            dbMutex.release();

            return new ArrayList<>(hashedKeys);

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * retrieves some objects from the database or cache.
     *
     * @param longKey the keys of the object to load
     *
     * @return the retrieved objcets
     */
    public Object retrieveObject(long longKey) {

        try {

            dbMutex.acquire();
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " | retrieving one objects with key: " + longKey);
            }
            Object obj = null;

            if (idMap.containsKey(longKey)) {
                obj = objectsCache.getObject(longKey);
                if (obj == null) {
                    Long zooid = idMap.get(longKey);
                    obj = pm.getObjectById(zooid);
                    objectsCache.addObject(longKey, obj);
                }
            }
            dbMutex.release();
            return obj;

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * retrieves some objects from the database or cache.
     *
     * @param key the keys of the object to load
     *
     * @return the retrieved objcets
     */
    public Object retrieveObject(String key) {
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " retrieving one objects with key: " + key);
        }
        return retrieveObject(createLongKey(key));
    }

    /**
     * Returns the number of instances of a given class stored in the db
     *
     * @param className the class name of the objects to be load
     * @return the number of objects
     */
    public int getNumber(Class className) {

        try {
            dbMutex.acquire();
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " query number of " + className.getSimpleName() + " objects");
            }

            HashSet counter = classCounter.get(className.getSimpleName());
            dbMutex.release();
            return (counter != null ? counter.size() : 0);

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Dumps the cache in the database.
     */
    public void dumpToDB() {

        try {

            dbMutex.acquire();
            objectsCache.saveCache(null, false);
            dbMutex.release();

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * retrieves some objects from the database or cache.
     *
     * @param keys the keys of the objects to load
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @return a list of objcets
     */
    public ArrayList<Object> retrieveObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) {

        try {
            dbMutex.acquire();
            if (true || debugInteractions) {
                System.out.println(System.currentTimeMillis() + " retrieving " + keys.size() + " objects");
            }

            ArrayList<Object> retrievingObjects = new ArrayList<>();
            HashMap<Long, Object> allObjects = new HashMap<>();
            for (String objectKey : keys) {
                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    break;
                }
                long longKey = createLongKey(objectKey);
                if (idMap.containsKey(longKey)) {
                    Object obj = objectsCache.getObject(longKey);
                    if (obj == null) {

                        Long zooid = idMap.get(longKey);
                        obj = pm.getObjectById(zooid);
                        allObjects.put(longKey, obj);

                    }
                    retrievingObjects.add(obj);
                }
            }
            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
                objectsCache.addObjects(allObjects);
            }
            dbMutex.release();
            return retrievingObjects;

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Retrieves all objects from a given class.
     *
     * @param className the class name of the objects to be retrieved
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @return the list of objects
     */
    public ArrayList<Object> retrieveObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) {

        try {
            dbMutex.acquire();
            if (true || debugInteractions) {
                System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
            }

            HashMap<Long, Object> allObjects = new HashMap<>();
            ArrayList<Object> retrievingObjects = new ArrayList<>();
            for (long longKey : classCounter.get(className.getSimpleName())) {
                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    break;
                }
                if (idMap.containsKey(longKey)) {
                    Object obj = objectsCache.getObject(longKey);
                    if (obj == null) {

                        Long zooid = idMap.get(longKey);
                        obj = pm.getObjectById(zooid);
                        allObjects.put(longKey, obj);

                    }
                    retrievingObjects.add(obj);
                }

            }
            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
                objectsCache.addObjects(allObjects);
            }
            dbMutex.release();
            return retrievingObjects;

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Removing an object from.
     *
     * @param keys the object key
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     */
    public void removeObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) {

        try {

            dbMutex.acquire();

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " removing " + keys.size() + " objects");
            }

            for (String key : keys) {
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
                long longKey = createLongKey(key);
                String className = objectsCache.removeObject(longKey);
                Long zooid = idMap.get(longKey);
                if (zooid != null) {
                    if (zooid != 0) {
                        Object obj = pm.getObjectById((zooid));
                        pm.deletePersistent(obj);
                        className = obj.getClass().getSimpleName();
                    }
                    classCounter.get(className).remove(longKey);
                    idMap.remove(longKey);
                }
            }
            dbMutex.release();

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Removing an object from.
     *
     * @param key the object key
     */
    public void removeObject(String key) {
        try {
            dbMutex.acquire();
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " removing object: " + key);
            }

            long longKey = createLongKey(key);
            String className = objectsCache.removeObject(longKey);
            Long zooid = idMap.get(longKey);
            if (zooid != null) {
                if (zooid != 0) {
                    Object obj = pm.getObjectById((zooid));
                    pm.deletePersistent(obj);
                    className = obj.getClass().getSimpleName();
                }
                classCounter.get(className).remove(longKey);
                idMap.remove(longKey);
            }
            dbMutex.release();

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Indicates whether an object is loaded.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is loaded
     */
    public boolean inCache(String objectKey) {
        try {

            dbMutex.acquire();
            boolean isInCache = objectsCache.inCache(createLongKey(objectKey));
            dbMutex.release();
            return isInCache;

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Indicates whether an object is loaded.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is loaded
     */
    public boolean inDB(String objectKey) {

        try {
            dbMutex.acquire();

            long longKey = createLongKey(objectKey);

            if (objectsCache.inCache(longKey)) {
                dbMutex.release();
                return true;
            }
            boolean isInDB = savedInDB(objectKey);
            dbMutex.release();
            return isInDB;

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Indicates whether an object is saved.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is saved
     */
    private boolean savedInDB(String objectKey) {

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " Checking db content,  key: " + objectKey);
        }

        return idMap.containsKey(createLongKey(objectKey));
    }

    /**
     * Indicates whether the connection to the DB is active.
     *
     * @return true if the connection to the DB is active
     */
    public boolean isConnectionActive() {
        return connectionActive;
    }

    /**
     * Closes the db connection.
     */
    public void close() {
        close(true);
    }

    /**
     * Closes the db connection.
     *
     * @param clearing clearing all database structures
     */
    public void close(boolean clearing) {

        try {
            dbMutex.acquire();
            if (debugInteractions) {
                System.out.println("closing database");
            }

            objectsCache.saveCache(null, clearing);

            connectionActive = false;
            pm.currentTransaction().commit();
            if (pm.currentTransaction().isActive()) {
                pm.currentTransaction().rollback();
            }
            pm.close();
            pm.getPersistenceManagerFactory().close();
            if (clearing) {
                idMap.clear();
            }
            dbMutex.release();

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Establishes connection to the database.
     */
    public void establishConnection() {
        establishConnection(true);

    }

    /**
     * Establishes connection to the database.
     *
     * @param loading load all objects from database
     */
    public void establishConnection(boolean loading) {

        try {

            dbMutex.acquire();
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " Establishing database: " + dbFile.getAbsolutePath());
            }
            idMap.clear();
            classCounter.clear();

            pm = ZooJdoHelper.openOrCreateDB(dbFile.getAbsolutePath());
            pm.currentTransaction().begin();
            connectionActive = true;

            if (loading) {
                Query q = pm.newQuery(DbObject.class, "firstLevel == true");
                for (Object obj : (Collection<?>) q.execute()) {
                    DbObject idObj = (DbObject) obj;
                    long id = idObj.getId();
                    long zooId = (Long) pm.getObjectId(idObj);
                    idMap.put(id, zooId);

                    if (!classCounter.containsKey(obj.getClass().getSimpleName())) {
                        classCounter.put(obj.getClass().getSimpleName(), new HashSet<>());
                    }
                    classCounter.get(obj.getClass().getSimpleName()).add(id);
                }
            }
            dbMutex.release();

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Returns the path to the database.
     *
     * @return the path to the database
     */
    public String getPath() {
        return path;
    }

    /**
     * Turn the debugging of interactions on or off.
     *
     * @param debug if true, the debugging is turned on
     */
    public static void setDebugInteractions(boolean debug) {
        debugInteractions = debug;
    }
}
