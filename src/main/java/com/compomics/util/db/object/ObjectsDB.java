package com.compomics.util.db.object;

import com.compomics.util.waiting.WaitingHandler;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.zoodb.internal.util.SynchronizedROCollection;
import java.util.concurrent.atomic.*;

import org.zoodb.jdo.ZooJdoHelper;
import org.zoodb.tools.ZooHelper;

/**
 * A database which can easily be used to store objects.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
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
     * Mutex for the interaction with the database.
     */
    private final Object dbMutex = new Object();
    /**
     * Debug, if true, all interaction with the database will be logged in the
     * System.out stream.
     */
    private static boolean debugInteractions = false;
    /**
     * OrientDB database connection.
     */
    PersistenceManager pm = null;
    /**
     * HashMap to map hash IDs of entries into DB ids.
     */
    private final HashMap<Long, Long> idMap = new HashMap<>();
    /**
     * Path to the database folder.
     */
    private File dbFolder = null;
    /**
     * The actual db file.
     */
    private File dbFile = null;
    /**
     * Boolean indicating if the connection is active.
     */
    private boolean connectionActive = false;
    /**
     * The class counter.
     */
    private final HashMap<String, HashSet<Long>> classCounter = new HashMap<>();
    /**
     * The current number of added objects.
     */
    private int currentAdded = 0;
    /**
     * The access counter.
     */
    private final static AtomicInteger ACCESSCOUNTER = new AtomicInteger(0);
    /**
     * The commit counter.
     */
    private final static AtomicBoolean COMMITBLOCKER = new AtomicBoolean(false);

    /**
     * Function for increasing the counter of processes accessing objects from
     * the db.
     */
    public static void increaseRWCounter() {
        while (COMMITBLOCKER.get()) {
            // YOU SHALL NOT PASS
            // until commit is done
        }
        ACCESSCOUNTER.incrementAndGet();
    }

    /**
     * Function for decreasing the counter of processes accessing objects from
     * the db.
     */
    public static void decreaseRWCounter() {
        ACCESSCOUNTER.decrementAndGet();
    }

    /**
     * Committing all changes into the database.
     */
    public void commit() {
        
        COMMITBLOCKER.set(true);
       
        while (ACCESSCOUNTER.get() != 0) {
            // YOU SHALL NOT PASS
            // while processes are potentially accessing the database
        }

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        currentAdded = 0;

        COMMITBLOCKER.set(false);
    
    }

    /**
     * Getter for the current number of added objects.
     *
     * @return the current number of added objects.
     */
    public int getCurrentAdded() {
        return currentAdded;
    }

    /**
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     */
    public ObjectsDB(String folder, String dbName) {
        
        this(folder, dbName, true);
    
    }

    /**
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     * @param overwrite overwriting old database
     */
    public ObjectsDB(String folder, String dbName, boolean overwrite) {
        
        if (debugInteractions) {
            
            System.out.println(System.currentTimeMillis() + " Creating database");

        }

        dbFolder = new File("/" + folder);

        if (!dbFolder.exists()) {

            if (!dbFolder.mkdirs()) {

                throw new IllegalArgumentException("cannot create database folder");

            }
        }

        dbFile = new File(dbFolder, dbName);

        if (dbFile.exists() && overwrite) {

            ZooHelper.removeDb(dbFile.getAbsolutePath());

        }

        establishConnection();
        objectsCache = new ObjectsCache(this);

    }

    /**
     * Getter for the id map mapping the hashed keys into zoo db ids.
     *
     * @return The id map.
     */
    public HashMap<Long, Long> getIdMap() {
        return idMap;
    }

    /**
     * Getter for the database file.
     *
     * @return the database file.
     */
    public File getDbFile() {
        return dbFile;
    }

    /**
     * Getter for the database folder.
     *
     * @return the database folder.
     */
    public File getDbFolder() {
        return dbFolder;
    }

    /**
     * Getter for the persistence manager.
     *
     * @return the persistence manager.
     */
    public PersistenceManager getDB() {
        return pm;
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
     *
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred while interacting with the database
     */
    public void insertObject(long objectKey, Object object) throws InterruptedException {

        synchronized (dbMutex) {

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " Inserting single object " + object.getClass().getSimpleName() + ", key: " + objectKey);
            }

            if (object == null) {

                throw new InterruptedException("error: null insertion: " + objectKey);

            }

            ((DbObject) object).setId(objectKey);
            ((DbObject) object).setFirstLevel(true);

            if (!idMap.containsKey(objectKey)) {

                idMap.put(objectKey, 0l);
                String simpleName = object.getClass().getSimpleName();

                if (!classCounter.containsKey(simpleName)) {

                    classCounter.put(simpleName, new HashSet<>());

                }

                classCounter.get(simpleName).add(objectKey);

            } else {

                throw new InterruptedException("error double insertion: " + objectKey);

            }

            currentAdded += 1;
            objectsCache.addObject(objectKey, object);

        }
    }

    /**
     * Returns an iterator of all objects of a given class.
     *
     * @param className the class name
     * @return the iterator
     */
    public HashSet<Long> getClassObjects(Class className) {
        return classCounter.get(className.getSimpleName());
    }

    /**
     * Returns an iterator of all objects of a given class.
     *
     * @param className the class name
     * @param filters filters for the class
     * @return the iterator
     *
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred
     */
    public Iterator<?> getObjectsIterator(Class className, String filters) throws InterruptedException {
        Query q;
        synchronized (dbMutex) {
            dumpToDB();
            q = pm.newQuery(className, filters);
        }
        return ((SynchronizedROCollection<?>) q.execute()).iterator();
    }

    /**
     * Inserts a set of objects in the given table.
     *
     * @param objects map of the objects (object key &gt; object)
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The progress will be displayed on the secondary progress bar.
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred
     */
    public void insertObjects(HashMap<Long, Object> objects, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {

        synchronized (dbMutex) {

            for (Entry<Long, Object> entry : objects.entrySet()) {

                long objectKey = entry.getKey();
                Object object = entry.getValue();

                if (object == null) {

                    throw new InterruptedException("error: null insertion: " + objectKey);

                }

                if (debugInteractions) {
                    System.out.println(System.currentTimeMillis() + " Inserting single object, table: " + object.getClass().getName() + ", key: " + objectKey);
                }

                ((DbObject) object).setId(objectKey);
                ((DbObject) object).setFirstLevel(true);

                if (!idMap.containsKey(objectKey)) {

                    idMap.put(objectKey, 0l);
                    String simpleName = object.getClass().getSimpleName();

                    if (!classCounter.containsKey(simpleName)) {

                        classCounter.put(simpleName, new HashSet<>());

                    }

                    classCounter.get(simpleName).add(objectKey);

                } else {

                    throw new InterruptedException("error double insertion: " + objectKey);

                }
            }

            currentAdded += objects.size();
            objectsCache.addObjects(objects);

        }
    }

    /**
     * Loads objects from a table in the cache.
     *
     * @param keys the keys of the objects to load
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void loadObjects(Collection<Long> keys, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {

        synchronized (dbMutex) {

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " loading " + keys.size() + " objects");
            }

            HashMap<Long, Object> allObjects = new HashMap<>(keys.size());

            for (long objectKey : keys) {

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {

                    return;

                }

                Long zooid = idMap.get(objectKey);

                if (zooid != null && zooid != 0 && !objectsCache.inCache(objectKey)) {

                    Object obj = pm.getObjectById(zooid);
                    allObjects.put(objectKey, obj);

                }

            }

            objectsCache.addObjects(allObjects);

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
     * @throws java.lang.InterruptedException exception thrown if a threading
     * error occurs while interacting with the database
     */
    public void loadObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {

        HashSet<Long> hashedKeys = classCounter.get(className.getSimpleName());

        synchronized (dbMutex) {

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
            }

            HashMap<Long, Object> allObjects = new HashMap<>(hashedKeys.size());

            for (Long longKey : hashedKeys) {

                if (waitingHandler.isRunCanceled()) {

                    return;

                }

                Long zooid = idMap.get(longKey);

                if (zooid != null && zooid != 0 && !objectsCache.inCache(longKey)) {

                    allObjects.put(longKey, pm.getObjectById(zooid));

                }
            }

            objectsCache.addObjects(allObjects);

        }
    }

    /**
     * Retrieves some objects from the database or cache.
     *
     * @param longKey the keys of the object to load
     * @return the retrieved objects
     */
    public Object retrieveObject(long longKey) {

        Object obj = null;

        synchronized (dbMutex) {

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " | retrieving one objects with key: " + longKey);
            }

            Long zooid = idMap.get(longKey);

            if (zooid != null) {

                obj = objectsCache.getObject(longKey);

                if (obj == null) {

                    obj = pm.getObjectById(zooid);
                    objectsCache.addObject(longKey, obj);

                }
            }
        }
        return obj;
    }

    /**
     * Returns the number of instances of a given class stored in the db.
     *
     * @param className the class name of the objects to be load
     *
     * @return the number of objects
     *
     */
    public int getNumber(Class className) {

        HashSet counter;

        synchronized (dbMutex) {

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " query number of " + className.getSimpleName() + " objects");
            }

            counter = classCounter.get(className.getSimpleName());
        }
        return (counter != null ? counter.size() : 0);
    }

    /**
     * Triggers a dump of all objects within the cache into the database.
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void dumpToDB() throws InterruptedException {
        synchronized (dbMutex) {
            objectsCache.saveCache(null, false);
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
     * @return a list of objects
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public ArrayList<Object> retrieveObjects(Collection<Long> keys, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {

        ArrayList<Object> retrievingObjects = new ArrayList<>(keys.size());

        synchronized (dbMutex) {

            if (true || debugInteractions) {
                System.out.println(System.currentTimeMillis() + " retrieving " + keys.size() + " objects");
            }

            HashMap<Long, Object> objectsNotInCache = new HashMap<>();

            for (Long objectKey : keys) {

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {

                    return retrievingObjects;

                }

                Long zooid = idMap.get(objectKey);

                if (zooid != null) {

                    Object obj = objectsCache.getObject(objectKey);

                    if (obj == null) {

                        obj = pm.getObjectById(zooid);
                        objectsNotInCache.put(objectKey, obj);

                    }

                    retrievingObjects.add(obj);

                }
            }

            objectsCache.addObjects(objectsNotInCache);

        }

        return retrievingObjects;

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
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public ArrayList<Object> retrieveObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {

        ArrayList<Object> retrievingObjects = new ArrayList<>();

        synchronized (dbMutex) {

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
            }

            HashMap<Long, Object> objectsNotInCache = new HashMap<>();

            for (long longKey : classCounter.get(className.getSimpleName())) {

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {

                    return retrievingObjects;

                }

                Long zooid = idMap.get(longKey);

                if (zooid != null) {

                    Object obj = objectsCache.getObject(longKey);

                    if (obj == null) {

                        obj = pm.getObjectById(zooid);
                        objectsNotInCache.put(longKey, obj);

                    }

                    retrievingObjects.add(obj);

                }
            }

            objectsCache.addObjects(objectsNotInCache);

        }

        return retrievingObjects;
    }

    /**
     * Removing an object from the cache and database.
     *
     * @param keys the object key
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void removeObjects(Collection<Long> keys, WaitingHandler waitingHandler, boolean displayProgress) throws InterruptedException {

        synchronized (dbMutex) {

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " removing " + keys.size() + " objects");
            }

            for (long key : keys) {
                
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
                
                Long zooid = idMap.get(key);
                
                if (zooid != null) {
                    
                    String className = objectsCache.removeObject(key);
                    
                    if (zooid != 0) {
                        
                        Object obj = pm.getObjectById((zooid));
                        pm.deletePersistent(obj);
                        className = obj.getClass().getSimpleName();
                        
                    }
                    
                    classCounter.get(className).remove(key);
                    idMap.remove(key);
                    
                }
            }
        }
    }

    /**
     * Removing an object from the cache and database.
     *
     * @param key the object key
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void removeObject(long key) throws InterruptedException {
        
        synchronized (dbMutex) {
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " removing object: " + key);
            }

            Long zooid = idMap.get(key);
            
            if (zooid != null) {
                
                String className = objectsCache.removeObject(key);
                
                if (zooid != 0) {
                    
                    Object obj = pm.getObjectById(zooid);
                    pm.deletePersistent(obj);
                    className = obj.getClass().getSimpleName();
                    
                }
                
                classCounter.get(className).remove(key);
                idMap.remove(key);
                
            }
        }
    }

    /**
     * Indicates whether an object is loaded in cache.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is loaded
     *
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    public boolean inCache(long objectKey) throws InterruptedException {
        
        boolean isInCache;
        
        synchronized (dbMutex) {
            
            isInCache = objectsCache.inCache(objectKey);
            
        }
        
        return isInCache;
        
    }

    /**
     * Indicates whether an object is in the cache or the database.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is loaded
     */
    public boolean inDB(long objectKey) {

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " Checking db content,  key: " + objectKey);
        }
        
        return idMap.containsKey(objectKey);
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
        
        synchronized (dbMutex) {
            
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
        }
    }

    /**
     * Establishes connection to the database.
     */
    private void establishConnection() {
        
        establishConnection(true);

    }

    /**
     * Establishes connection to the database.
     *
     * @param loading load all objects from database
     */
    public void establishConnection(boolean loading) {

        synchronized (dbMutex) {
            
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
                    long zooId = idObj.jdoZooGetOid();
                    idMap.put(id, zooId);

                    String simpleName = obj.getClass().getSimpleName();
                    HashSet<Long> classKeys = classCounter.get(simpleName);
                    
                    if (classKeys == null) {
                        
                        classKeys = new HashSet<>();
                        classCounter.put(simpleName, classKeys);
                        
                    }
                    
                    classKeys.add(id);
                    
                }
            }
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