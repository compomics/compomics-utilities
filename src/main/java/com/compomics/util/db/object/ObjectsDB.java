package com.compomics.util.db.object;


import com.compomics.util.waiting.WaitingHandler;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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
     * OrientDB database connection
     */
    PersistenceManager pm = null;
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
    
    private final static AtomicInteger ACCESSCOUNTER = new AtomicInteger(0);
    
    private final static AtomicBoolean COMMITBLOCKER = new AtomicBoolean(false);
    
    private final static long[] HASHVALUELIST = {
        -4785187655948605440L, 2351437082613919744L, -7620983146772158464L, -8747902906952306688L, 
        -4496485731609829376L, -4047692981156677632L, 5720028397227347968L, -4748060763626276864L,
        2063365725770047488L, -5756543758428897280L, -134698081630425088L, 867726525032218624L,
        -526450428666544128L, 4146020926348189696L, 4362296343029680128L, -672990070253072384L,
        -2559490283472277504L, 3187632952876974080L, -5716989432807307264L, -8332013824838645760L,
        4253744629365506048L, 2097316067254513664L, 8951627463544416256L, -5600031980443258880L,
        6380991404691560448L, 8903284868402118656L, -1115601857539225600L, 4631654322507227136L,
        7771989044436795392L, 7773688932940122112L, -6019734220953055232L, 3392712990065328128L,
        -8921384047543447552L, -7767842613008707584L, -1186522791511611392L, -6926112736333537280L,
        8736653739320072192L, 8867062073843642368L, 6298992568012455936L, -6831107491093487616L,
        -7084666134747267072L, -1674183307215181824L, 7180054879733344256L, -1774408477150697472L,
        -1102347028329271296L, 2837232313405440000L, 6789844965029836800L, -2021979153929187328L,
        -803643088872329216L, -6635474898121947136L, -1519775710292529152L, -7017431056149018624L,
        8399941098113230848L, 6620078501932513280L, 8402686423795523584L, 7887825026517880832L,
        6240511391300272128L, -2116326148598433792L, 3164957088731514880L, 6354445331039899648L,
        -2421944411545827328L, -6588274517877174272L, -5482092713179058176L, 1515440486213902336L,
        -3383185261582667776L, -2725557693718710272L, 2180993613288613888L, -4878984385226620928L,
        4905597879284899840L, -8937278576235966464L, -4857623260077275136L, -6678664042745585664L,
        6590419491356596224L, 3898378085667969024L, -8773012746479065088L, -4316629602317574144L,
        -578020323565103104L, 5815789437630859264L, 1330829571824838656L, 2058704620696928256L,
        5775301559630338048L, -4128281782811285504L, -6189976155577464832L, -2204893487149668352L,
        -4107985148748068864L, -2803177563490273280L, 7139083951461890048L, -6547891565468342272L,
        3512976861638146048L, 8446989268574042112L, -6262309160844883968L, -447362214463838208L,
        -4695191602764636160L, -8777129286526107648L, -2322220230279856128L, -3376371221541236736L,
        -352816524822126592L, -6489602716775188480L, -4340386299073419264L, -411238008103813120L,
        -7548606038504292352L, 3950672770391547904L, 1570846774247147520L, 2087897268844892160L,
        -6691005528687374336L, 1651506531346769920L, -9105826395118237696L, -920643688498837504L,
        6741095098680469504L, -9196666188088858624L, 4628592761082419200L, 1805632260469598208L,
        -2595685425333377024L, -2001876750192766976L, 4498796613205751808L, -3322677024598908928L,
        8658129466298726400L, 2854559136171276288L, 106897466552897536L, 5590481524594866176L,
        -4319460978758043648L, 1896969526688425984L, -2860223852340688896L, -2273634011107659776L,
        -6830438329227218944L, -1024496407927033856L, -1561168395559655424L, -1430574524350681088L};
    
    /**
     * Function for increasing the counter of processes accessing objects from the db
     */
    public static void increaseRWCounter() {
        while (COMMITBLOCKER.get()) {
            // YOU SHALL NOT PASS
            // until commit is done
        }
        ACCESSCOUNTER.incrementAndGet();
    }
    
    
    /**
     * Function for decreasing the counter of processes accessing objects from the db
     */
    public static void decreaseRWCounter() {
        ACCESSCOUNTER.decrementAndGet();
    }
    
    /**
     * Committing all changes into the database
     * @throws InterruptedException  exception thrown whenever a
     * threading error occurred while establishing the connection
     */
    public void commit() throws InterruptedException {
        COMMITBLOCKER.set(true);
        while (ACCESSCOUNTER.get() != 0){
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
     * @return the current number of added objects.
     */
    public int getCurrentAdded(){
        return currentAdded;
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
    
    /**
     * Getter for the id map mapping the hashed keys into zoo db ids.
     * @return The id map.
     */
    public HashMap<Long, Long> getIdMap(){
        return idMap;
    }
    
    /**
     * Getter for the database file.
     * @return the database file.
     */
    public File getDbFile(){
        return dbFile;
    }
    
    /**
     * Getter for the database folder.
     * @return the database folder.
     */
    public File getDbFolder(){
        return dbFolder;
    }
    
    /** 
     * Getter for the persistence manager.
     * @return the persistence manager.
     */
    public PersistenceManager getDB(){
        return pm;
    }
    
    /**
     * Creating a unique 64 bit hash key from the original key of arbitrary length.
     * The hashed key allows to search entries in the database or in dictionaries
     * in constant time.
     * 
     * @param key the original key
     * @return the hashed key
     */
    public long createLongKey(String key){
        long longKey = 0;
        for (int i = 0; i < key.length(); ++i){
            long val = HASHVALUELIST[key.charAt(i)]; // create a hash val of char
            int sft = ((i * 11) & 63); // determine a shift length of cyclic shift depending on char position in key
            val = (val << sft) | (val >>> (64 - sft)); // do the cyclic shift
            longKey ^= val; // xor it with remaining
        }        
        return longKey;
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
        synchronized(dbMutex){
            long longKey = createLongKey(objectKey);

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " Inserting single object " + object.getClass().getSimpleName() + ", key: " + objectKey + "  /  " + longKey);
            }
            if (object == null){
                throw new InterruptedException("error: null insertion: " + objectKey);
            }

            ((DbObject)object).setId(longKey);
            ((DbObject)object).setFirstLevel(true);
            if (!idMap.containsKey(longKey)){
                idMap.put(longKey, 0l);
                String simpleName = object.getClass().getSimpleName();
                if(!classCounter.containsKey(simpleName)){
                    classCounter.put(simpleName, new HashSet<>());
                }
                classCounter.get(simpleName).add(longKey);

            }
            else {
                throw new InterruptedException("error double insertion: " + objectKey);
            }
            currentAdded += 1;
            objectsCache.addObject(longKey, object);
        }
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
        Query q;
        synchronized(dbMutex){
            dumpToDB();
            q = pm.newQuery(className, filters);
        }
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
        
        synchronized(dbMutex){
            HashMap<Long, Object> objectsToAdd = new HashMap<>(objects.size());
            for (String objectKey : objects.keySet()) {
                Object object = objects.get(objectKey);
                if (debugInteractions) {
                    System.out.println(System.currentTimeMillis() + " Inserting single object, table: " + object.getClass().getName() + ", key: " + objectKey);
                }

                if (object == null){
                    throw new InterruptedException("error: null insertion: " + objectKey);
                }

                long longKey = createLongKey(objectKey);
                ((DbObject)object).setId(longKey);
                ((DbObject)object).setFirstLevel(true);
                if (!idMap.containsKey(longKey)){
                    idMap.put(longKey, 0l);
                    String simpleName = object.getClass().getSimpleName();
                    if(!classCounter.containsKey(simpleName)){
                        classCounter.put(simpleName, new HashSet<>());
                    }
                    classCounter.get(simpleName).add(longKey);
                    objectsToAdd.put(longKey, object);
                }
                else {
                    throw new InterruptedException("error double insertion: " + objectKey);
                }
            }
            currentAdded += objects.size();
            objectsCache.addObjects(objectsToAdd);
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
        
        
        ArrayList<Long> hashedKeys = new ArrayList<>();
        synchronized(dbMutex){
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " loading " + keys.size() + " objects");
            }

            HashMap<Long, Object> allObjects = new HashMap<>();
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
        
        }
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
     */
    public ArrayList<Long> loadObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) {
        
        HashSet<Long> hashedKeys = classCounter.get(className.getSimpleName());
        if (hashedKeys == null) return new ArrayList<>();
        synchronized(dbMutex){
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
            }


            HashMap<Long, Object> allObjects = new HashMap<>();
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
        }
        return new ArrayList<>(hashedKeys);
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
        
        Object obj = null;
        synchronized(dbMutex){
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " | retrieving one objects with key: " + longKey);
            }

            if (idMap.containsKey(longKey)){                
                obj = objectsCache.getObject(longKey);
                if (obj == null){
                    obj = pm.getObjectById(idMap.get(longKey));
                    objectsCache.addObject(longKey, obj);
                }
            }
        }
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
     */
    public int getNumber(Class className) {
        HashSet counter;
        synchronized(dbMutex){
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " query number of " + className.getSimpleName() + " objects");
            }

            counter = classCounter.get(className.getSimpleName());
        }
        return (counter != null ? counter.size() : 0);
    }
    
    
    /**
     * Triggers a dump of all objects within the cache into the database
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database
     */
    public void dumpToDB() throws IOException, SQLException, InterruptedException {
        synchronized(dbMutex){
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
        
        ArrayList<Object> retrievingObjects = new ArrayList<>();
        synchronized(dbMutex){
            if (true || debugInteractions) {
                System.out.println(System.currentTimeMillis() + " retrieving " + keys.size() + " objects");
            }

            HashMap<Long, Object> allObjects = new HashMap<>();
            for (String objectKey : keys){
                if (waitingHandler != null && waitingHandler.isRunCanceled()) break;
                long longKey = createLongKey(objectKey);
                if (idMap.containsKey(longKey)){
                    Object obj = objectsCache.getObject(longKey);
                    if (obj == null){
                        obj = pm.getObjectById(idMap.get(longKey));
                        allObjects.put(longKey, obj);
                    }
                    retrievingObjects.add(obj);
                }
            }
            if (waitingHandler != null && !waitingHandler.isRunCanceled()){
                objectsCache.addObjects(allObjects);
            }
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
        
        ArrayList<Object> retrievingObjects = new ArrayList<>();
        synchronized(dbMutex){
            if (true || debugInteractions) {
                System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
            }


            HashMap<Long, Object> allObjects = new HashMap<>();
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
        }
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
        
        synchronized(dbMutex){
        
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " removing " + keys.size() + " objects");
            }

            for (String key : keys){
                if (waitingHandler.isRunCanceled()) break;
                long longKey = createLongKey(key);
                Long zooid = idMap.get(longKey);
                if (zooid != null){
                    String className = objectsCache.removeObject(longKey);
                    if (zooid != 0){
                        Object obj = pm.getObjectById((zooid));
                        pm.deletePersistent(obj);
                        className = obj.getClass().getSimpleName();
                    }
                    classCounter.get(className).remove(longKey);
                    idMap.remove(longKey);
                }
            }
        }
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
        synchronized(dbMutex){
            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " removing object: " + key);
            }

            long longKey = createLongKey(key);
            Long zooid = idMap.get(longKey);
            if (zooid != null){
                String className = objectsCache.removeObject(longKey);
                if (zooid != 0){
                    Object obj = pm.getObjectById(zooid);
                    pm.deletePersistent(obj);
                    className = obj.getClass().getSimpleName();
                }
                classCounter.get(className).remove(longKey);
                idMap.remove(longKey);
            }
        }
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
        boolean isInCache;
        synchronized(dbMutex){
            isInCache = objectsCache.inCache(createLongKey(objectKey));
        }
        return isInCache;
    }
    
    

    /**
     * Indicates whether an object is loaded.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is loaded
     */
    public boolean inDB(String objectKey) {

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
        synchronized(dbMutex){
            if (debugInteractions){
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
            if (clearing) idMap.clear();
        }
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
    private void establishConnection() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
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
        
        synchronized(dbMutex){
            if (debugInteractions){
                System.out.println(System.currentTimeMillis() + " Establishing database: " + dbFile.getAbsolutePath());
            }
            idMap.clear();
            classCounter.clear();

            pm = ZooJdoHelper.openOrCreateDB(dbFile.getAbsolutePath());
            pm.currentTransaction().begin();
            connectionActive = true;


            if (loading){
                Query q = pm.newQuery(DbObject.class, "firstLevel == true");
                for (Object obj : (Collection<?>) q.execute()){
                    DbObject idObj = (DbObject)obj;
                    long id = idObj.getId();
                    long zooId = idObj.jdoZooGetOid();
                    idMap.put(id, zooId);     

                    String simpleName = obj.getClass().getSimpleName();
                    if(!classCounter.containsKey(simpleName)) classCounter.put(obj.getClass().getSimpleName(), new HashSet<>());
                    classCounter.get(simpleName).add(id);
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




//package com.compomics.util.db.object;
//
//import com.compomics.util.waiting.WaitingHandler;
//import java.io.*;
//import java.math.BigInteger;
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.concurrent.Semaphore;
//
//import javax.jdo.PersistenceManager;
//import javax.jdo.Query;
//import org.zoodb.internal.util.SynchronizedROCollection;
//
//import org.zoodb.jdo.ZooJdoHelper;
//import org.zoodb.tools.ZooHelper;
//
///**
// * A database which can easily be used to store objects. Note: most operations
// * with the object DB are synchronized or use semaphores. Should a thread be
// * interrupted, the exception will be sent as RunTimeException. Same if MD5 is
// * not supported. This is because our tools recover from these exceptions
// * similarly as for other unchecked exceptions. Please contact us if you need
// * another/better exception handling.
// *
// * @author Marc Vaudel
// * @author dominik.kopczynski
// */
//public class ObjectsDB {
//
//    /**
//     * The version UID for serialization/deserialization compatibility.
//     */
//    static final long serialVersionUID = -8595805180622832745L;
//    /**
//     * The name of the database.
//     */
//    private String dbName;
//    /**
//     * The path to the database.
//     */
//    private String path;
//    /**
//     * The cache to be used for the objects.
//     */
//    private ObjectsCache objectsCache;
//    /**
//     * A boolean indicating whether the database is being queried.
//     */
//    private boolean loading = false;
//    /**
//     * Mutex for the interaction with the database.
//     */
//    private final Semaphore dbMutex = new Semaphore(1);
//    /**
//     * Debug, if true, all interaction with the database will be logged in the
//     * System.out stream.
//     */
//    private static boolean debugInteractions = false;
//    /**
//     * OrientDB database connection
//     */
//    private PersistenceManager pm = null;
//    /**
//     * HashMap to map hash IDs of entries into DB ids
//     */
//    private final HashMap<Long, Long> idMap = new HashMap<>();
//    /**
//     * path of the database folder
//     */
//    private File dbFolder = null;
//    /**
//     * the actual db file
//     */
//    private File dbFile = null;
//
//    private boolean connectionActive = false;
//
//    private final HashMap<String, HashSet<Long>> classCounter = new HashMap<>();
//
//    private int currentAdded = 0;
//
//    private final static Object forCommit = new Object();
//
//    private final static Object rWObject = new Object();
//
//    private static int readWriteCounter = 0;
//
//    private final static Semaphore blockCommit = new Semaphore(1);
//
//    public static void increaseRWCounter() {
//        synchronized (forCommit) {
//            //System.out.println("inside commit");
//        }
//        synchronized (rWObject) {
//
//            try {
//                if (readWriteCounter == 0) {
//                    blockCommit.acquire();
//                }
//                readWriteCounter++;
//
//            } catch (InterruptedException e) {
//
//                throw new RuntimeException(e);
//
//            }
//        }
//    }
//
//    public static void decreaseRWCounter() {
//        synchronized (rWObject) {
//            readWriteCounter--;
//            if (readWriteCounter == 0) {
//                blockCommit.release();
//            }
//        }
//    }
//
//    public void resetCurrentAdded() {
//        currentAdded = 0;
//    }
//
//    public int getCurrentAdded() {
//        return currentAdded;
//    }
//
//    public void commit() {
//
//        try {
//
//            System.out.println("start commit");
//            synchronized (forCommit) {
//                System.out.println("commit locked");
//
//                blockCommit.acquire();
//                blockCommit.release();
//                //while(readWriteCounter > 0){}
//
//                pm.currentTransaction().commit();
//                pm.currentTransaction().begin();
//
//                System.out.println("commit unlocking");
//            }
//            System.out.println("end commit");
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Constructor.
//     *
//     * @param folder absolute path of the folder where to establish the database
//     * @param dbName name of the database
//     *
//     * @throws java.io.IOException thrown if the database folder could not be
//     * created
//     */
//    public ObjectsDB(String folder, String dbName) throws IOException {
//        this(folder, dbName, true);
//    }
//
//    /**
//     * Constructor.
//     *
//     * @param folder absolute path of the folder where to establish the database
//     * @param dbName name of the database
//     * @param overwrite overwriting old database
//     *
//     * @throws java.io.IOException thrown if the database folder could not be
//     * created
//     */
//    public ObjectsDB(String folder, String dbName, boolean overwrite) throws IOException {
//        if (debugInteractions) {
//            System.out.println(System.currentTimeMillis() + " Creating database");
//        }
//
//        dbFolder = new File("/" + folder);
//
//        if (!dbFolder.exists()) {
//            if (!dbFolder.mkdirs()) {
//                throw new IOException("cannot create database folder");
//            }
//        }
//
//        dbFile = new File(dbFolder, dbName);
//        if (dbFile.exists() && overwrite) {
//            ZooHelper.removeDb(dbFile.getAbsolutePath());
//        }
//
//        establishConnection();
//        objectsCache = new ObjectsCache(this);
//
//    }
//
//    public HashMap<Long, Long> getIdMap() {
//        return idMap;
//    }
//
//    public File getDbFile() {
//        return dbFile;
//    }
//
//    public File getDbFolder() {
//        return dbFolder;
//    }
//
//    public Semaphore getDbMutex() {
//        return dbMutex;
//    }
//
//    public PersistenceManager getDB() {
//        return pm;
//    }
//
//    /**
//     * Creates the long-based key from a string based key by taking the MD5
//     * hash. Note: if MD5 is not supported, a runtime exception will be thrown.
//     *
//     * @param key a string based key
//     *
//     * @return the long-based key
//     */
//    public long createLongKey(String key) {
//
//        try {
//
//            MessageDigest md5 = MessageDigest.getInstance("MD5");
//            md5.update(StandardCharsets.UTF_8.encode(key));
//            String md5Key = String.format("%032x", new BigInteger(1, md5.digest()));
//            long longKey = 0;
//            for (int i = 0; i < 32; ++i) {
//                longKey |= ((long) (md5Key.charAt(i) - '0')) << ((i * 11) % 63);
//            }
//            return longKey;
//
//        } catch (NoSuchAlgorithmException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Returns the database name.
//     *
//     * @return the database name
//     */
//    public String getName() {
//        return dbName;
//    }
//
//    /**
//     * Returns the cache used by this database.
//     *
//     * @return the cache used by this database
//     */
//    public ObjectsCache getObjectsCache() {
//        return objectsCache;
//    }
//
//    /**
//     * Sets the object cache to be used by this database.
//     *
//     * @param objectCache the object cache to be used by this database
//     */
//    public void setObjectCache(ObjectsCache objectCache) {
//        this.objectsCache = objectCache;
//    }
//
//    /**
//     * Stores an object in the desired table. When multiple objects are to be
//     * inserted, use insertObjects instead.
//     *
//     * @param objectKey the key of the object
//     * @param object the object to store
//     */
//    public void insertObject(String objectKey, Object object) {
//        try {
//        dbMutex.acquire();
//        long longKey = createLongKey(objectKey);
//
//        if (debugInteractions) {
//            System.out.println(System.currentTimeMillis() + " Inserting single object " + object.getClass().getSimpleName() + ", key: " + objectKey + "  /  " + longKey);
//        }
//        if (object == null) {
//            throw new IllegalArgumentException("error: null insertion: " + objectKey);
//        }
//
//        ((DbObject) object).setId(longKey);
//        ((DbObject) object).setFirstLevel(true);
//        if (!idMap.containsKey(longKey)) {
//            idMap.put(longKey, 0l);
//            if (!classCounter.containsKey(object.getClass().getSimpleName())) {
//                classCounter.put(object.getClass().getSimpleName(), new HashSet<>());
//            }
//            classCounter.get(object.getClass().getSimpleName()).add(longKey);
//
//        } else {
//            throw new IllegalArgumentException("error double insertion: " + objectKey);
//        }
//        currentAdded += 1;
//        objectsCache.addObject(longKey, object);
//        dbMutex.release();
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Returns an iterator of all objects of a given class
//     *
//     * @param className the class name
//     * @return the iterator
//     */
//    public HashSet<Long> getClassObjects(Class className) {
//        return classCounter.get(className.getSimpleName());
//    }
//
//    /**
//     * Returns an iterator of all objects of a given class
//     *
//     * @param className the class name
//     * @param filters filters for the class
//     * @return the iterator
//     */
//    public Iterator<?> getObjectsIterator(Class className, String filters) {
//
//        try {
//
//            dbMutex.acquire();
//            dumpToDB();
//            Query q = pm.newQuery(className, filters);
//            dbMutex.release();
//            return ((SynchronizedROCollection<?>) q.execute()).iterator();
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Inserts a set of objects in the given table.
//     *
//     * @param objects map of the objects (object key &gt; object)
//     * @param waitingHandler a waiting handler displaying the progress (can be
//     * null). The progress will be displayed on the secondary progress bar.
//     * @param displayProgress boolean indicating whether the progress of this
//     * method should be displayed on the waiting handler
//     */
//    public void insertObjects(HashMap<String, Object> objects, WaitingHandler waitingHandler, boolean displayProgress) {
//
//        try {
//            dbMutex.acquire();
//            HashMap<Long, Object> objectsToAdd = new HashMap<>(objects.size());
//            for (String objectKey : objects.keySet()) {
//                Object object = objects.get(objectKey);
//                if (debugInteractions) {
//                    System.out.println(System.currentTimeMillis() + " Inserting single object, table: " + object.getClass().getName() + ", key: " + objectKey);
//                }
//
//                if (object == null) {
//                    throw new IllegalArgumentException("error: null insertion: " + objectKey);
//                }
//
//                long longKey = createLongKey(objectKey);
//                ((DbObject) object).setId(longKey);
//                ((DbObject) object).setFirstLevel(true);
//                if (!idMap.containsKey(longKey)) {
//                    idMap.put(longKey, 0l);
//                    if (!classCounter.containsKey(object.getClass().getSimpleName())) {
//                        classCounter.put(object.getClass().getSimpleName(), new HashSet<>());
//                    }
//                    classCounter.get(object.getClass().getSimpleName()).add(longKey);
//                    objectsToAdd.put(longKey, object);
//                } else {
//                    throw new IllegalArgumentException("error double insertion: " + objectKey);
//                }
//            }
//            currentAdded += objects.size();
//            objectsCache.addObjects(objectsToAdd);
//            dbMutex.release();
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Loads some objects from a table in the cache.
//     *
//     * @param keys the keys of the objects to load
//     * @param waitingHandler the waiting handler allowing displaying progress
//     * and canceling the process
//     * @param displayProgress boolean indicating whether the progress of this
//     * method should be displayed on the waiting handler
//     * @return returns the list of hashed keys
//     */
//    public ArrayList<Long> loadObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) {
//
//        try {
//
//            dbMutex.acquire();
//            if (debugInteractions) {
//                System.out.println(System.currentTimeMillis() + " loading " + keys.size() + " objects");
//            }
//
//            HashMap<Long, Object> allObjects = new HashMap<>();
//            ArrayList<Long> hashedKeys = new ArrayList<>();
//            for (String objectKey : keys) {
//                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
//                    break;
//                }
//                long longKey = createLongKey(objectKey);
//                hashedKeys.add(longKey);
//                Long zooid = idMap.get(longKey);
//                if (!objectsCache.inCache(longKey) && zooid != null && zooid != 0) {
//                    Object obj = pm.getObjectById(zooid);
//                    allObjects.put(longKey, obj);
//                }
//
//            }
//            if (hashedKeys.size() != keys.size()) {
//                throw new IllegalArgumentException("Array sizes in function do not match, " + keys.size() + " vs. " + hashedKeys.size());
//            }
//            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
//                objectsCache.addObjects(allObjects);
//            }
//
//            dbMutex.release();
//            return hashedKeys;
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Loads all objects from a given class.
//     *
//     * @param className the class name of the objects to be retrieved
//     * @param waitingHandler the waiting handler allowing displaying progress
//     * and canceling the process
//     * @param displayProgress boolean indicating whether the progress of this
//     * method should be displayed on the waiting handler
//     *
//     * @return returns the list of hashed keys
//     */
//    public ArrayList<Long> loadObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) {
//
//        try {
//            dbMutex.acquire();
//            if (debugInteractions) {
//                System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
//            }
//
//            HashMap<Long, Object> allObjects = new HashMap<>();
//            HashSet<Long> hashedKeys = classCounter.get(className.getSimpleName());
//            for (Long longKey : hashedKeys) {
//                if (waitingHandler.isRunCanceled()) {
//                    break;
//                }
//                Long zooid = idMap.get(longKey);
//                if (!objectsCache.inCache(longKey) && zooid != null && zooid != 0) {
//                    allObjects.put(longKey, pm.getObjectById(zooid));
//                }
//
//            }
//            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
//                objectsCache.addObjects(allObjects);
//            }
//            dbMutex.release();
//
//            return new ArrayList<>(hashedKeys);
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * retrieves some objects from the database or cache.
//     *
//     * @param longKey the keys of the object to load
//     *
//     * @return the retrieved objcets
//     */
//    public Object retrieveObject(long longKey) {
//
//        try {
//
//            dbMutex.acquire();
//            if (debugInteractions) {
//                System.out.println(System.currentTimeMillis() + " | retrieving one objects with key: " + longKey);
//            }
//            Object obj = null;
//
//            if (idMap.containsKey(longKey)) {
//                obj = objectsCache.getObject(longKey);
//                if (obj == null) {
//                    Long zooid = idMap.get(longKey);
//                    obj = pm.getObjectById(zooid);
//                    objectsCache.addObject(longKey, obj);
//                }
//            }
//            dbMutex.release();
//            return obj;
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * retrieves some objects from the database or cache.
//     *
//     * @param key the keys of the object to load
//     *
//     * @return the retrieved objcets
//     */
//    public Object retrieveObject(String key) {
//        if (debugInteractions) {
//            System.out.println(System.currentTimeMillis() + " retrieving one objects with key: " + key);
//        }
//        return retrieveObject(createLongKey(key));
//    }
//
//    /**
//     * Returns the number of instances of a given class stored in the db
//     *
//     * @param className the class name of the objects to be load
//     * @return the number of objects
//     */
//    public int getNumber(Class className) {
//
//        try {
//            dbMutex.acquire();
//            if (debugInteractions) {
//                System.out.println(System.currentTimeMillis() + " query number of " + className.getSimpleName() + " objects");
//            }
//
//            HashSet counter = classCounter.get(className.getSimpleName());
//            dbMutex.release();
//            return (counter != null ? counter.size() : 0);
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Dumps the cache in the database.
//     */
//    public void dumpToDB() {
//
//        try {
//
//            dbMutex.acquire();
//            objectsCache.saveCache(null, false);
//            dbMutex.release();
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * retrieves some objects from the database or cache.
//     *
//     * @param keys the keys of the objects to load
//     * @param waitingHandler the waiting handler allowing displaying progress
//     * and canceling the process
//     * @param displayProgress boolean indicating whether the progress of this
//     * method should be displayed on the waiting handler
//     *
//     * @return a list of objcets
//     */
//    public ArrayList<Object> retrieveObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) {
//
//        try {
//            dbMutex.acquire();
//            if (true || debugInteractions) {
//                System.out.println(System.currentTimeMillis() + " retrieving " + keys.size() + " objects");
//            }
//
//            ArrayList<Object> retrievingObjects = new ArrayList<>();
//            HashMap<Long, Object> allObjects = new HashMap<>();
//            for (String objectKey : keys) {
//                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
//                    break;
//                }
//                long longKey = createLongKey(objectKey);
//                if (idMap.containsKey(longKey)) {
//                    Object obj = objectsCache.getObject(longKey);
//                    if (obj == null) {
//
//                        Long zooid = idMap.get(longKey);
//                        obj = pm.getObjectById(zooid);
//                        allObjects.put(longKey, obj);
//
//                    }
//                    retrievingObjects.add(obj);
//                }
//            }
//            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
//                objectsCache.addObjects(allObjects);
//            }
//            dbMutex.release();
//            return retrievingObjects;
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Retrieves all objects from a given class.
//     *
//     * @param className the class name of the objects to be retrieved
//     * @param waitingHandler the waiting handler allowing displaying progress
//     * and canceling the process
//     * @param displayProgress boolean indicating whether the progress of this
//     * method should be displayed on the waiting handler
//     *
//     * @return the list of objects
//     */
//    public ArrayList<Object> retrieveObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) {
//
//        try {
//            dbMutex.acquire();
//            if (true || debugInteractions) {
//                System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
//            }
//
//            HashMap<Long, Object> allObjects = new HashMap<>();
//            ArrayList<Object> retrievingObjects = new ArrayList<>();
//            for (long longKey : classCounter.get(className.getSimpleName())) {
//                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
//                    break;
//                }
//                if (idMap.containsKey(longKey)) {
//                    Object obj = objectsCache.getObject(longKey);
//                    if (obj == null) {
//
//                        Long zooid = idMap.get(longKey);
//                        obj = pm.getObjectById(zooid);
//                        allObjects.put(longKey, obj);
//
//                    }
//                    retrievingObjects.add(obj);
//                }
//
//            }
//            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
//                objectsCache.addObjects(allObjects);
//            }
//            dbMutex.release();
//            return retrievingObjects;
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Removing an object from.
//     *
//     * @param keys the object key
//     * @param waitingHandler the waiting handler allowing displaying progress
//     * and canceling the process
//     * @param displayProgress boolean indicating whether the progress of this
//     * method should be displayed on the waiting handler
//     */
//    public void removeObjects(ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) {
//
//        try {
//
//            dbMutex.acquire();
//
//            if (debugInteractions) {
//                System.out.println(System.currentTimeMillis() + " removing " + keys.size() + " objects");
//            }
//
//            for (String key : keys) {
//                if (waitingHandler.isRunCanceled()) {
//                    break;
//                }
//                long longKey = createLongKey(key);
//                String className = objectsCache.removeObject(longKey);
//                Long zooid = idMap.get(longKey);
//                if (zooid != null) {
//                    if (zooid != 0) {
//                        Object obj = pm.getObjectById((zooid));
//                        pm.deletePersistent(obj);
//                        className = obj.getClass().getSimpleName();
//                    }
//                    classCounter.get(className).remove(longKey);
//                    idMap.remove(longKey);
//                }
//            }
//            dbMutex.release();
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Removing an object from.
//     *
//     * @param key the object key
//     */
//    public void removeObject(String key) {
//        try {
//            dbMutex.acquire();
//            if (debugInteractions) {
//                System.out.println(System.currentTimeMillis() + " removing object: " + key);
//            }
//
//            long longKey = createLongKey(key);
//            String className = objectsCache.removeObject(longKey);
//            Long zooid = idMap.get(longKey);
//            if (zooid != null) {
//                if (zooid != 0) {
//                    Object obj = pm.getObjectById((zooid));
//                    pm.deletePersistent(obj);
//                    className = obj.getClass().getSimpleName();
//                }
//                classCounter.get(className).remove(longKey);
//                idMap.remove(longKey);
//            }
//            dbMutex.release();
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Indicates whether an object is loaded.
//     *
//     * @param objectKey the object key
//     *
//     * @return a boolean indicating whether an object is loaded
//     */
//    public boolean inCache(String objectKey) {
//        try {
//
//            dbMutex.acquire();
//            boolean isInCache = objectsCache.inCache(createLongKey(objectKey));
//            dbMutex.release();
//            return isInCache;
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Indicates whether an object is loaded.
//     *
//     * @param objectKey the object key
//     *
//     * @return a boolean indicating whether an object is loaded
//     */
//    public boolean inDB(String objectKey) {
//
//        try {
//            dbMutex.acquire();
//
//            long longKey = createLongKey(objectKey);
//
//            if (objectsCache.inCache(longKey)) {
//                dbMutex.release();
//                return true;
//            }
//            boolean isInDB = savedInDB(objectKey);
//            dbMutex.release();
//            return isInDB;
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Indicates whether an object is saved.
//     *
//     * @param objectKey the object key
//     *
//     * @return a boolean indicating whether an object is saved
//     */
//    private boolean savedInDB(String objectKey) {
//
//        if (debugInteractions) {
//            System.out.println(System.currentTimeMillis() + " Checking db content,  key: " + objectKey);
//        }
//
//        return idMap.containsKey(createLongKey(objectKey));
//    }
//
//    /**
//     * Indicates whether the connection to the DB is active.
//     *
//     * @return true if the connection to the DB is active
//     */
//    public boolean isConnectionActive() {
//        return connectionActive;
//    }
//
//    /**
//     * Closes the db connection.
//     */
//    public void close() {
//        close(true);
//    }
//
//    /**
//     * Closes the db connection.
//     *
//     * @param clearing clearing all database structures
//     */
//    public void close(boolean clearing) {
//
//        try {
//            dbMutex.acquire();
//            if (debugInteractions) {
//                System.out.println("closing database");
//            }
//
//            objectsCache.saveCache(null, clearing);
//
//            connectionActive = false;
//            pm.currentTransaction().commit();
//            if (pm.currentTransaction().isActive()) {
//                pm.currentTransaction().rollback();
//            }
//            pm.close();
//            pm.getPersistenceManagerFactory().close();
//            if (clearing) {
//                idMap.clear();
//            }
//            dbMutex.release();
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Establishes connection to the database.
//     */
//    public void establishConnection() {
//        establishConnection(true);
//
//    }
//
//    /**
//     * Establishes connection to the database.
//     *
//     * @param loading load all objects from database
//     */
//    public void establishConnection(boolean loading) {
//
//        try {
//
//            dbMutex.acquire();
//            if (debugInteractions) {
//                System.out.println(System.currentTimeMillis() + " Establishing database: " + dbFile.getAbsolutePath());
//            }
//            idMap.clear();
//            classCounter.clear();
//
//            pm = ZooJdoHelper.openOrCreateDB(dbFile.getAbsolutePath());
//            pm.currentTransaction().begin();
//            connectionActive = true;
//
//            if (loading) {
//                Query q = pm.newQuery(DbObject.class, "firstLevel == true");
//                for (Object obj : (Collection<?>) q.execute()) {
//                    DbObject idObj = (DbObject) obj;
//                    long id = idObj.getId();
//                    long zooId = (Long) pm.getObjectId(idObj);
//                    idMap.put(id, zooId);
//
//                    if (!classCounter.containsKey(obj.getClass().getSimpleName())) {
//                        classCounter.put(obj.getClass().getSimpleName(), new HashSet<>());
//                    }
//                    classCounter.get(obj.getClass().getSimpleName()).add(id);
//                }
//            }
//            dbMutex.release();
//
//        } catch (InterruptedException e) {
//
//            throw new RuntimeException(e);
//
//        }
//    }
//
//    /**
//     * Returns the path to the database.
//     *
//     * @return the path to the database
//     */
//    public String getPath() {
//        return path;
//    }
//
//    /**
//     * Turn the debugging of interactions on or off.
//     *
//     * @param debug if true, the debugging is turned on
//     */
//    public static void setDebugInteractions(boolean debug) {
//        debugInteractions = debug;
//    }
//}
