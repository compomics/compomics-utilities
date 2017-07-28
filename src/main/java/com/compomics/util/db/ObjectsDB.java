package com.compomics.util.db;

import com.compomics.util.IdObject;
import com.compomics.util.waiting.WaitingHandler;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import java.util.concurrent.locks.ReentrantLock;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.zoodb.internal.util.SynchronizedROCollection;

import org.zoodb.jdo.ZooJdoHelper;
import org.zoodb.tools.ZooHelper;

/**
 * A database which can easily be used to store objects.
 *
 * @author Marc Vaudel
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
    PersistenceManager pm = null;
    /**
     * HashMap to map hash IDs of entries into DB ids
     */
    private final HashMap<Long, Long> idMap = new HashMap<Long, Long>();
    /**
     * path of the database folder
     */
    private File dbFolder = null;
    /**
     * the actual db file
     */
    private File dbFile = null;
    
    private boolean connectionActive = false;
    
    private final HashMap<String, HashSet<Long>> classCounter = new HashMap<String, HashSet<Long>>();
    
    private int currentAdded = 0;
    
    private final static Object forCommit = new Object();
    
    private final static Object rWObject = new Object();
    
    private static int readWriteCounter = 0;
    
    private final static Semaphore blockCommit = new Semaphore(1);
    
    
    
    public static void increaseRWCounter() {
        synchronized(forCommit) {
            //System.out.println("inside commit");
        }
        synchronized(rWObject) {
            
            try {
                if (readWriteCounter == 0) blockCommit.acquire();
                readWriteCounter++;
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    
    public static void decreaseRWCounter() {
        synchronized(rWObject) {
            readWriteCounter--;
            if (readWriteCounter == 0) blockCommit.release();
        }
    }
    
    
    
    
    public void resetCurrentAdded(){
        currentAdded = 0;
    }
    
    public int getCurrentAdded(){
        return currentAdded;
    }
    
    
    public void commit() throws InterruptedException {
        System.out.println("start commit");
        synchronized(forCommit){
            System.out.println("commit locked");

            blockCommit.acquire();
            blockCommit.release();
            //while(readWriteCounter > 0){}

            pm.currentTransaction().commit();
            pm.currentTransaction().begin();

            System.out.println("commit unlocking");
        }
        System.out.println("end commit");
    }
    
    /**
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     *
     * @throws SQLException exception thrown whenever a problem occurred when
     * establishing the connection to the database
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while reading or writing a file
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while deserializing a file
     * @throws java.lang.InterruptedException exception thrown whenever a
     * threading error occurred while establishing the connection
     */
    public ObjectsDB(String folder, String dbName) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        this(folder, dbName, true);
    }
    
    /**
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     * @param overwrite overwriting old database
     *
     * @throws SQLException exception thrown whenever a problem occurred when
     * establishing the connection to the database
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while reading or writing a file
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while deserializing a file
     * @throws java.lang.InterruptedException exception thrown whenever a
     * threading error occurred while establishing the connection
     */
    public ObjectsDB(String folder, String dbName, boolean overwrite) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (debugInteractions){
            System.out.println(System.currentTimeMillis() + " Creating database");
        }
        
        dbFolder = new File("/" + folder);
        
        if (!dbFolder.exists()){
            if (!dbFolder.mkdirs()){
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
    
    public HashMap<Long, Long> getIdMap(){
        return idMap;
    }
    
    public File getDbFile(){
        return dbFile;
    }
    
    public File getDbFolder(){
        return dbFolder;
    }
    
    public Semaphore getDbMutex(){
        return dbMutex;
    }
    
    
    public PersistenceManager getDB(){
        return pm;
    }
    
    
    public long createLongKey(String key){
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(key));
            String md5Key = String.format("%032x", new BigInteger(1, md5.digest()));
            long longKey = 0;
            for (int i = 0; i < 32; ++i){
                longKey |= ((long)(md5Key.charAt(i) - '0')) << ((i * 11) % 63);
            }
            return longKey;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
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
     * @throws SQLException exception thrown whenever an error occurred while
     * storing the object
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred while interacting with the database
     */
    public void insertObject(String objectKey, Object object) throws SQLException, IOException, InterruptedException {
        dbMutex.acquire();
        long longKey = createLongKey(objectKey);
        
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " Inserting single object " + object.getClass().getSimpleName() + ", key: " + objectKey + "  /  " + longKey);
        }
        if (object == null){
            throw new InterruptedException("error: null insertion: " + objectKey);
        }
        
        ((IdObject)object).setId(longKey);
        ((IdObject)object).setFirstLevel(true);
        if (!idMap.containsKey(longKey)){
            idMap.put(longKey, 0l);
            if(!classCounter.containsKey(object.getClass().getSimpleName())){
                classCounter.put(object.getClass().getSimpleName(), new HashSet<Long>());
            }
            classCounter.get(object.getClass().getSimpleName()).add(longKey);
            
        }
        else {
            throw new InterruptedException("error double insertion: " + objectKey);
        }
        currentAdded += 1;
        objectsCache.addObject(longKey, object);
        dbMutex.release();
    }
    
    /**
     * Returns an iterator of all objects of a given class
     * @param className the class name
     * @return the iterator
     */
    public HashSet<Long> getClassObjects(Class className){
        return classCounter.get(className.getSimpleName());
    }
    
    /**
     * Returns an iterator of all objects of a given class
     * @param className the class name
     * @param filters filters for the class
     * @return the iterator
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred
     */
    public Iterator<?> getObjectsIterator(Class className, String filters) throws IOException, InterruptedException, SQLException{
        
        dbMutex.acquire();
        dumpToDB();
        Query q = pm.newQuery(className, filters);
        dbMutex.release();
        return ((SynchronizedROCollection<?>)q.execute()).iterator();
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
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred
     */
    public void insertObjects(HashMap<String, Object> objects, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, InterruptedException {
        
        
        dbMutex.acquire();
        HashMap<Long, Object> objectsToAdd = new HashMap<Long, Object>(objects.size());
        for (String objectKey : objects.keySet()) {
            Object object = objects.get(objectKey);
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " Inserting single object, table: " + object.getClass().getName() + ", key: " + objectKey);
            }
            
            if (object == null){
                throw new InterruptedException("error: null insertion: " + objectKey);
            }
            
            long longKey = createLongKey(objectKey);
            ((IdObject)object).setId(longKey);
            ((IdObject)object).setFirstLevel(true);
            if (!idMap.containsKey(longKey)){
                idMap.put(longKey, 0l);
                if(!classCounter.containsKey(object.getClass().getSimpleName())){
                    classCounter.put(object.getClass().getSimpleName(), new HashSet<Long>());
                }
                classCounter.get(object.getClass().getSimpleName()).add(longKey);
                objectsToAdd.put(longKey, object);
            }
            else {
                throw new InterruptedException("error double insertion: " + objectKey);
            }
        }
        currentAdded += objects.size();
        objectsCache.addObjects(objectsToAdd);
        dbMutex.release();
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
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public ArrayList<Long> loadObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        
        dbMutex.acquire();
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " loading " + keys.size() + " objects");
        }
        
        HashMap<Long, Object> allObjects = new HashMap<Long, Object>();
        ArrayList<Long> hashedKeys = new ArrayList<Long>();
        for (String objectKey : keys){
            if (waitingHandler != null && waitingHandler.isRunCanceled()) break;
            long longKey = createLongKey(objectKey);
            hashedKeys.add(longKey);
            Long zooid = idMap.get(longKey);
            if (!objectsCache.inCache(longKey) && zooid != null && zooid != 0){
                Object obj = pm.getObjectById(zooid);
                allObjects.put(longKey, obj);
            }
            
        }
        if (hashedKeys.size() != keys.size()){
            throw new InterruptedException("Array sizes in function do not match, " + keys.size() + " vs. " + hashedKeys.size());
        }
        if (waitingHandler != null && !waitingHandler.isRunCanceled()){
            objectsCache.addObjects(allObjects);
        }
        
        dbMutex.release();
        return hashedKeys;
    }
    
    

    /**
     * Loads all objects from a given class.
     *
     * @param className the class name of the objects to be retrieved
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @return returns the list of hashed keys
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public ArrayList<Long> loadObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        dbMutex.acquire();
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
        }
        
        
        HashMap<Long, Object> allObjects = new HashMap<Long, Object>();
        HashSet<Long> hashedKeys = classCounter.get(className.getSimpleName());
        for (Long longKey : hashedKeys){
            if (waitingHandler.isRunCanceled()) break;
            Long zooid = idMap.get(longKey);
            if (!objectsCache.inCache(longKey) && zooid != null && zooid != 0){
                allObjects.put(longKey, pm.getObjectById(zooid));
            }
            
        }
        if (waitingHandler != null && !waitingHandler.isRunCanceled()){
            objectsCache.addObjects(allObjects);
        }
        dbMutex.release();
        return new ArrayList<Long>(hashedKeys);
    }
    
    
    /**
     * retrieves some objects from the database or cache.
     *
     * @param longKey the keys of the object to load
     * @return the retrived objcets
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public Object retrieveObject(long longKey) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        dbMutex.acquire();
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " | retrieving one objects with key: " + longKey);
        }
        Object obj = null;
        
        if (idMap.containsKey(longKey)){                
            obj = objectsCache.getObject(longKey);
            if (obj == null){
                Long zooid = idMap.get(longKey);
                obj = pm.getObjectById(zooid);
                objectsCache.addObject(longKey, obj);
            }
        }
        dbMutex.release();
        return obj;
    }
    
    
    /**
     * retrieves some objects from the database or cache.
     *
     * @param key the keys of the object to load
     * @return the retrieved objcets
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public Object retrieveObject(String key) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
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
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public int getNumber(Class className) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        dbMutex.acquire();
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " query number of " + className.getSimpleName() + " objects");
        }
        
        HashSet counter = classCounter.get(className.getSimpleName());
        dbMutex.release();
        return (counter != null ? counter.size() : 0);
    }
    
    public void dumpToDB() throws IOException, SQLException, InterruptedException {
        dbMutex.acquire();
        objectsCache.saveCache(null, false);
        dbMutex.release();
    }
    
    
    /**
     * retrieves some objects from the database or cache.
     *
     * @param keys the keys of the objects to load
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     * @return a list of objcets
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public ArrayList<Object> retrieveObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        dbMutex.acquire();
        if (true || debugInteractions) {
            System.out.println(System.currentTimeMillis() + " retrieving " + keys.size() + " objects");
        }
        
        ArrayList<Object> retrievingObjects = new ArrayList<Object>();
        HashMap<Long, Object> allObjects = new HashMap<Long, Object>();
        for (String objectKey : keys){
            if (waitingHandler != null && waitingHandler.isRunCanceled()) break;
            long longKey = createLongKey(objectKey);
            if (idMap.containsKey(longKey)){
                Object obj = objectsCache.getObject(longKey);
                if (obj == null){

                    Long zooid = idMap.get(longKey);
                    obj = pm.getObjectById(zooid);
                    allObjects.put(longKey, obj);

                }
                retrievingObjects.add(obj);
            }
        }
        if (waitingHandler != null && !waitingHandler.isRunCanceled()){
            objectsCache.addObjects(allObjects);
        }
        dbMutex.release();
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
     * @return the list of objects
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public ArrayList<Object> retrieveObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        dbMutex.acquire();
        if (true || debugInteractions) {
            System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
        }
        
        
        HashMap<Long, Object> allObjects = new HashMap<Long, Object>();
        ArrayList<Object> retrievingObjects = new ArrayList<Object>();
        for (long longKey : classCounter.get(className.getSimpleName())){
            if (waitingHandler != null && waitingHandler.isRunCanceled()) break;
            if (idMap.containsKey(longKey)){
                Object obj = objectsCache.getObject(longKey);
                if (obj == null){

                    Long zooid = idMap.get(longKey);
                    obj = pm.getObjectById(zooid);
                    allObjects.put(longKey, obj);

                }
                retrievingObjects.add(obj);
            }
            
        }
        if (waitingHandler != null && !waitingHandler.isRunCanceled()){
            objectsCache.addObjects(allObjects);
        }
        dbMutex.release();
        return retrievingObjects;
    }
    
    
    

    /**
     * Removing an object from.
     *
     * @param keys the object key
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void removeObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        dbMutex.acquire();
        
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " removing " + keys.size() + " objects");
        }
        
        for (String key : keys){
            if (waitingHandler.isRunCanceled()) break;
            long longKey = createLongKey(key);
            String className = objectsCache.removeObject(longKey);
            Long zooid = idMap.get(longKey);
            if (zooid != null){
                if (zooid != 0){
                    Object obj = pm.getObjectById((zooid));
                    pm.deletePersistent(obj);
                    className = obj.getClass().getSimpleName();
                }
                classCounter.get(className).remove(longKey);
                idMap.remove(longKey);
            }
        }
        dbMutex.release();
    }
    
    
    

    /**
     * Removing an object from.
     *
     * @param key the object key
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void removeObject(String key) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        dbMutex.acquire();
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " removing object: " + key);
        }
        
        
        long longKey = createLongKey(key);
        String className = objectsCache.removeObject(longKey);
        Long zooid = idMap.get(longKey);
        if (zooid != null){
            if (zooid != 0){
                Object obj = pm.getObjectById((zooid));
                pm.deletePersistent(obj);
                className = obj.getClass().getSimpleName();
            }
            classCounter.get(className).remove(longKey);
            idMap.remove(longKey);
        }
        dbMutex.release();
    }
    
    
    

    /**
     * Indicates whether an object is loaded.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is loaded
     *
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    public boolean inCache(String objectKey) throws SQLException, InterruptedException {
        dbMutex.acquire();
        boolean isInCache = objectsCache.inCache(createLongKey(objectKey));
        dbMutex.release();
        return isInCache;
    }
    
    

    /**
     * Indicates whether an object is loaded.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is loaded
     *
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    public boolean inDB(String objectKey) throws SQLException, InterruptedException {
        dbMutex.acquire();

        long longKey = createLongKey(objectKey);

        if (objectsCache.inCache(longKey)) {
            dbMutex.release();
            return true;
        }
        boolean isInDB = savedInDB(objectKey);
        dbMutex.release();
        return isInDB;
    }

    /**
     * Indicates whether an object is saved.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is saved
     *
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    private boolean savedInDB(String objectKey) throws SQLException, InterruptedException {

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " Checking db content,  key: " + objectKey);
        }
        
        long longKey = createLongKey(objectKey);
        
        Query q = pm.newQuery(IdObject.class, "id == " + longKey);
        if( ((Collection<?>) q.execute()).size() > 0){
            return true;
        }
        return false;
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
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     * @throws InterruptedException exception thrown if a threading error occurs
     * @throws java.io.IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while deserializing a file
     */
    public void close() throws SQLException, InterruptedException, IOException, ClassNotFoundException {
        close(true);
    }

    /**
     * Closes the db connection.
     *
     * @param clearing clearing all database structures
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     * @throws InterruptedException exception thrown if a threading error occurs
     * @throws java.io.IOException exception thrown whenever an error occurred while
     * writing the object
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while deserializing a file
     */
    public void close(boolean clearing) throws SQLException, InterruptedException, IOException, ClassNotFoundException {
        
        dbMutex.acquire();
        //if (debugInteractions){
            System.out.println("closing database");
        //}
        
        
        objectsCache.saveCache(null, clearing);
        
        connectionActive = false;
        pm.currentTransaction().commit();
        if (pm.currentTransaction().isActive()) {
            pm.currentTransaction().rollback();
        }
        pm.close();
        pm.getPersistenceManagerFactory().close();
        if (clearing) idMap.clear();
        dbMutex.release();
    }
    
    /**
     * Establishes connection to the database.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection to the database
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while reading or writing a file
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while deserializing a file
     * @throws java.lang.InterruptedException exception thrown whenever a
     * threading error occurred while establishing the connection
     */
    public void establishConnection() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        establishConnection(true);
        
    }

    /**
     * Establishes connection to the database.
     *
     * @param loading load all objects from database
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection to the database
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while reading or writing a file
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while deserializing a file
     * @throws java.lang.InterruptedException exception thrown whenever a
     * threading error occurred while establishing the connection
     */
    public void establishConnection(boolean loading) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        dbMutex.acquire();
        if (debugInteractions){
            System.out.println(System.currentTimeMillis() + " Establishing database: " + dbFile.getAbsolutePath());
        }
        idMap.clear();
        classCounter.clear();
        
        pm = ZooJdoHelper.openOrCreateDB(dbFile.getAbsolutePath());
        pm.currentTransaction().begin();
        connectionActive = true;
        
        
        if (loading){
            Query q = pm.newQuery(IdObject.class, "firstLevel == true");
            for (Object obj : (Collection<?>) q.execute()){
                IdObject idObj = (IdObject)obj;
                long id = idObj.getId();
                long zooId = (Long)pm.getObjectId(idObj);
                idMap.put(id, zooId);     
                
                if(!classCounter.containsKey(obj.getClass().getSimpleName())){
                    classCounter.put(obj.getClass().getSimpleName(), new HashSet<Long>());
                }
                classCounter.get(obj.getClass().getSimpleName()).add(id);
            }
        }
        dbMutex.release();
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
