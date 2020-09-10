package com.compomics.util.db.object;

import static com.compomics.util.db.object.DbMutex.dbMutex;
import com.compomics.util.waiting.WaitingHandler;
import java.io.*;
import java.util.*;
import org.nustaq.serialization.FSTConfiguration;



import java.sql.*;
import java.util.Map.Entry;
import org.nustaq.serialization.FSTObjectInput;

/**
 * A database which can easily be used to store objects.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 * @author Harald Barsnes
 */
public class ObjectsDB {

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
     * Debug, if true, all interaction with the database will be logged in the
     * System.out stream.
     */
    private static boolean debugInteractions = false;
    /**
     * Database persistence manager.
     */
    private Connection connection = null;
    /**
     * Configuration for fast serialization.
     */
    public static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    /**
     * HashMap to map hash IDs of entries into DB ids.
     */
    private static boolean connectionActive = false;
    /**
     * The current number of added objects.
     */
    private int currentAdded = 0;
    /**
     * keys stored in the backend
     */
    private HashSet<Long> keysInBackend = new HashSet<>();
    /**
     * Empty default constructor.
     */
    public ObjectsDB() {
    }

    /**
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     */
    public ObjectsDB(String folder, String dbName) {

        this(folder, dbName, false);

    }

    /**
     * Constructor.
     *
     * @param path absolute path of the folder where to establish the database
     * @param dbName name of the database
     * @param overwrite overwriting old database
     */
    public ObjectsDB(String path, String dbName, boolean overwrite) {

        if (debugInteractions) {

            System.out.println(System.currentTimeMillis() + " Creating database");

        }

        this.path = path;
        this.dbName = dbName;

        File dbFolder = getDbFolder();
        if (!dbFolder.exists()) {

            if (!dbFolder.mkdirs()) {

                throw new IllegalArgumentException("Cannot create database folder!");

            }
        }
        
        File dbFile = getDbFile();
        if (dbFile.exists() && overwrite){
            dbFile.delete();
        }
        
        

        establishConnection();
        objectsCache = new ObjectsCache(this);

    }


    /**
     * Committing all changes into the database.
     */
    public void commit() {
        try {
            dbMutex.acquire();
            connection.commit();
        } 
        catch(Exception e){

        }
        finally {
            dbMutex.release();
        }

    }


    /**
     * Getter for the current number of added objects.
     *
     * @return the current number of added objects
     */
    public int getCurrentAdded() {
        return currentAdded;
    }


    /**
     * Getter for the database file.
     *
     * @return the database file
     */
    public File getDbFile() {
        return new File(path, dbName);
    }

    /**
     * Getter for the database folder.
     *
     * @return the database folder
     */
    public File getDbFolder() {
        return new File(path);
    }

    /**
     * Getter for the persistence manager.
     *
     * @return the persistence manager
     */
    public Connection getDB() {
        return connection;
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
    public void insertObject(long objectKey, Object object) {

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " Inserting single object " + object.getClass().getSimpleName() + ", key: " + objectKey);
        }

        if (object == null) {
            throw new IllegalArgumentException("error: null insertion: " + objectKey);
        }

        ((DbObject) object).setId(objectKey);
        objectsCache.addObject(objectKey, object, false, true);
    }

    
    
    
    /**
     * Returns an iterator of all objects of a given class.
     *
     * @param className the class name
     * @return the iterator
     */
    public HashSet<Long> getClassObjectIDs(Class className) {
        return getClassObjectIDs(className, null);
    }

    /**
     * Returns an iterator of all objects of a given class.
     *
     * @param className the class name
     * @param filters string with filters;
     * @return the iterator
     */
    public HashSet<Long> getClassObjectIDs(Class className, String filters) {
        HashSet<Long> cacheIDs = objectsCache.getClassInCache(className);
        HashSet<Long> classObjectIds;
        if (cacheIDs != null) classObjectIds = new HashSet<>(cacheIDs);
        else classObjectIds = new HashSet<>();
        
        
        String sqlQuery = "SELECT id FROM data WHERE class = ?";
        if (filters != null){
            sqlQuery += " AND " + filters;
        }
        sqlQuery += ";";
        
        try {
            dbMutex.acquire();
            PreparedStatement pstmt  = connection.prepareStatement(sqlQuery);
            
            // set the value
            pstmt.setString(1, className.getSimpleName());
            //
            ResultSet rs  = pstmt.executeQuery();
            
            // loop through the result set
            while (rs.next()) {
                classObjectIds.add(rs.getLong("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            dbMutex.release();
        }
        return classObjectIds;
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
    public void insertObjects(
            HashMap<Long, Object> objects, 
            WaitingHandler waitingHandler, 
            boolean displayProgress
    ) {


        for (Entry<Long, Object> entry : objects.entrySet()) {

            long objectKey = entry.getKey();
            Object object = entry.getValue();

            if (object == null) {

                throw new IllegalArgumentException("error: null insertion: " + objectKey);

            }

            if (debugInteractions) {
                System.out.println(System.currentTimeMillis() + " Inserting single object, table: " + object.getClass().getName() + ", key: " + objectKey);
            }

            ((DbObject) object).setId(objectKey);
        }

        currentAdded += objects.size();
        objectsCache.addObjects(objects, false, true);

    
    }
    
    
    /**
     * Loads objects from the database according to their unique key
     *
     * @param objectKey the keys of the objects to load
     * @return object the object loaded from the database
     */
    private Object loadFromDB(long objectKey){
        Object object = null;
        

        try {
            dbMutex.acquire();
            PreparedStatement pstmt = connection.prepareStatement("SELECT data FROM data WHERE id = ?;");
            pstmt.setLong(1, objectKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                FSTObjectInput in = new FSTObjectInput(rs.getBinaryStream("data"));
                object = in.readObject();
                
                /*
                ByteArrayInputStream bis = new ByteArrayInputStream(rs.getBinaryStream("data").readAllBytes());
                ObjectInput in = null;
                try {
                  in = new ObjectInputStream(bis);
                  object = in.readObject(); 
                } finally {
                  try {
                    if (in != null) {
                      in.close();
                    }
                  } catch (IOException ex) {
                  }
                }
                */
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            dbMutex.release();
        }
        return object;
    }
    
    

    /**
     * Loads objects from a table in the cache.
     *
     * @param keys the keys of the objects to load
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     */
    public void loadObjects(Collection<Long> keys, WaitingHandler waitingHandler, boolean displayProgress) {


        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " loading " + keys.size() + " objects");
        }
        

        HashMap<Long, Object> objectsNotInCache = new HashMap<>();
        for (long objectKey : keys) {
            

            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                return;
            }


            if (!objectsCache.inCache(objectKey)) {
                Object obj = loadFromDB(objectKey);
                if (obj != null){
                    objectsNotInCache.put(objectKey, obj);
                }
            }

        }

        objectsCache.addObjects(objectsNotInCache, true, false);
    }

    /**
     * Loads all objects from a given class.
     *
     * @param className the class name of the objects to be retrieved
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     */
    public void loadObjects(Class className, WaitingHandler waitingHandler, boolean displayProgress) {


        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " loading all " + className.getSimpleName() + " objects");
        }

        HashMap<Long, Object> objectsNotInCache = new HashMap<>();
        try {
            dbMutex.acquire();
            PreparedStatement pstmt = connection.prepareStatement("SELECT id, data FROM data WHERE class = ?;");
            pstmt.setString(1, className.getSimpleName());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    return;
                }
                FSTObjectInput in = new FSTObjectInput(rs.getBinaryStream("data"));
                long objectKey = rs.getLong("id");
                Object object = in.readObject();
                
                
                /*
                ByteArrayInputStream bis = new ByteArrayInputStream(rs.getBinaryStream("data").readAllBytes());
                ObjectInput in = null;
                Object object = null;
                try {
                  in = new ObjectInputStream(bis);
                  object = in.readObject(); 
                } finally {
                  try {
                    if (in != null) {
                      in.close();
                    }
                  } catch (IOException ex) {
                  }
                }
                */
                
                objectsNotInCache.put(objectKey, object);
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            dbMutex.release();
        }
        
        objectsCache.addObjects(objectsNotInCache, true, false);
    }

    /**
     * Retrieves an object from the database or cache.
     *
     * @param objectKey the key of the object to load
     * @return the retrieved object
     */
    public Object retrieveObject(long objectKey) {

        Object obj = null;

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " | retrieving one object with key: " + objectKey);
        }

        obj = objectsCache.getObject(objectKey);

        if (obj == null) {
            obj = loadFromDB(objectKey);
            if (obj != null){
                objectsCache.addObject(objectKey, obj, true, false);
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
        return getClassObjectIDs(className).size();
    }
    
    
    

    /**
     * Triggers a dump of all objects within the cache into the database.
     */
    public void dumpToDB() {
        dbMutex.acquire();
        objectsCache.saveCache(null, false);
        dbMutex.release();
    }

    
    
    /**
     * Retrieves some objects from the database or cache.
     *
     * @param keys the keys of the objects to load
     * @param waitingHandler the waiting handler allowing displaying progress
     * and canceling the process
     * @param displayProgress boolean indicating whether the progress of this
     * method should be displayed on the waiting handler
     *
     * @return a list of objects
     */
    public ArrayList<Object> retrieveObjects(Collection<Long> keys, WaitingHandler waitingHandler, boolean displayProgress) {

        ArrayList<Object> retrievingObjects = new ArrayList<>(keys.size());

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " retrieving " + keys.size() + " objects");
        }

        HashMap<Long, Object> objectsNotInCache = new HashMap<>();

        for (long objectKey : keys) {
            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                return retrievingObjects;
            }

            Object obj = objectsCache.getObject(objectKey);

            if (obj == null) {
                obj = loadFromDB(objectKey);
                if (obj != null){
                    objectsNotInCache.put(objectKey, obj);
                }
            }

            retrievingObjects.add(obj);
        }

        objectsCache.addObjects(objectsNotInCache, true, false);
        return retrievingObjects;
    }
    
    
    public void updateObject(long objectKey, Object object){
        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " | retrieving one object with key: " + objectKey);
        }

        objectsCache.addObject(objectKey, object, inBackend(objectKey), true);
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
        
        ArrayList<Object> retrievingObjects = new ArrayList<>();
        HashMap<Long, Object> objectsNotInCache = new HashMap<>();
        HashSet<Long> objectInCache = objectsCache.getClassInCache(className);
        objectInCache.forEach(key -> {
            retrievingObjects.add(objectsCache.getObject(key));
        });
        

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " retrieving all " + className + " objects");
        }
        
        try {
            dbMutex.acquire();
            PreparedStatement pstmt = connection.prepareStatement("SELECT id, data FROM data WHERE class = ?;");
            pstmt.setString(1, className.getSimpleName());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    return retrievingObjects;
                }
                FSTObjectInput in = new FSTObjectInput(rs.getBinaryStream("data"));
                long objectKey = rs.getLong("id");
                Object object = in.readObject();
                
                
                /*
                ByteArrayInputStream bis = new ByteArrayInputStream(rs.getBinaryStream("data").readAllBytes());
                ObjectInput in = null;
                Object object = null;
                try {
                  in = new ObjectInputStream(bis);
                  object = in.readObject(); 
                } finally {
                  try {
                    if (in != null) {
                      in.close();
                    }
                  } catch (IOException ex) {
                  }
                }
                
                
                */
                
                
                
                
                if (!objectInCache.contains(objectKey)){
                    objectsNotInCache.put(objectKey, object);
                }
                retrievingObjects.add(object);
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            dbMutex.release();
        }
        
        objectsCache.addObjects(objectsNotInCache, true, false);
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
     */
    public void removeObjects(Collection<Long> keys, WaitingHandler waitingHandler, boolean displayProgress) {

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " removing " + keys.size() + " objects");
        }

        try {
            dbMutex.acquire();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM data WHERE id = ?;");
            for (long key : keys) {

                if (waitingHandler.isRunCanceled())  break;

                objectsCache.removeObject(key);
                pstmt.setLong(1, key);
                pstmt.executeUpdate();

            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            dbMutex.release();
        }

    }

    /**
     * Removing an object from the cache and database.
     *
     * @param key the object key
     */
    public void removeObject(long key) {


        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " removing object: " + key);
        }

        
        try {
            objectsCache.removeObject(key);
            dbMutex.acquire();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM data WHERE id = ?;");
            pstmt.setLong(1, key);
            pstmt.executeUpdate();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            dbMutex.release();
        }

    }

    /**
     * Indicates whether an object is loaded in cache.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is loaded
     */
    public boolean inCache(long objectKey) {
        return objectsCache.inCache(objectKey);
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
        
        if (objectsCache.inCache(objectKey)) return true;
        return inBackend(objectKey);
    }
    
    
    /**
     * Indicates whether an object is loaded in the backend.
     *
     * @param objectKey the object key
     *
     * @return a boolean indicating whether an object is loaded
     */
    public boolean inBackend(long objectKey) {
        return keysInBackend.contains(objectKey);
        /*
        boolean result = false;
        try {
            dbMutex.acquire();
            PreparedStatement pstmt = connection.prepareStatement("SELECT count(id) cnt FROM data WHERE id = ?;");
            pstmt.setLong(1, objectKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                result = rs.getLong("cnt") == 1;
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            dbMutex.release();
        }

        return result;
        */
    }
    
    
    
    
    /**
     * Returns the keys in backend set
     * 
     *  @return the keys in backend set
     */
    public HashSet<Long> getKeysInBackend(){
        return keysInBackend;
    }
    
    

    /**
     * Indicates whether the connection to the DB is active.
     *
     * @return true if the connection to the DB is active
     */
    public static boolean isConnectionActive() {
        return connectionActive;
    }

    /**
     * Locking the db for storing.
     *
     * @param waitingHandler the waiting handler
     */
    public void lock(WaitingHandler waitingHandler) {

        dbMutex.acquire();

        if (debugInteractions) {

            System.out.println("locking database");

        }

        connectionActive = false;

        objectsCache.saveCache(waitingHandler, true);

        dbMutex.release();

    }

    /**
     * Unlocking the db after storing.
     */
    public void unlock() {

        dbMutex.acquire();

        if (debugInteractions) {
            System.out.println("unlocking database");
        }

        connectionActive = true;
        dbMutex.release();

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

            objectsCache.saveCache(null, clearing); // @TODO: verify that this is actullly needed (as this looks more like saving?)
            objectsCache.clearCache();

            connectionActive = false;
            connection.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            dbMutex.release();
        }

    }

    /**
     * Establishes connection to the database.
     *
     */
    public void establishConnection() {

        File dbFile = getDbFile();

        if (debugInteractions) {
            System.out.println(System.currentTimeMillis() + " Establishing database: " + dbFile.getAbsolutePath());
        }

        try {
            dbMutex.acquire();
            
            // Connect with the database
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            connection.setAutoCommit(false);
            
            
            boolean insertTables = true;
            PreparedStatement pst = connection.prepareStatement("SELECT * FROM sqlite_master WHERE type = 'table'");
            ResultSet rs = pst.executeQuery();
            while (rs.next()){
                if (rs.getString("name").equals("data")){
                    insertTables = false;
                    break;
                }
            }
            connection.commit();
            
            
            if (insertTables){
                String sql = "CREATE TABLE `data` (`id` INTEGER, `class` TEXT, `data` BLOB, PRIMARY KEY(id));";
                Statement stmt = connection.createStatement();
                stmt.execute(sql);
                
                sql = "CREATE INDEX `data_id_index` ON `data` (`id` ASC);";
                stmt.execute(sql);
                
                sql = "CREATE INDEX `data_class_index` ON `data` (`class` ASC);";
                stmt.execute(sql);
                connection.commit();
            }
            else {
                try {
                    PreparedStatement pstmt = connection.prepareStatement("SELECT id FROM data");
                    ResultSet rsId = pstmt.executeQuery();
                    if (rsId.next()){
                        keysInBackend.add(rsId.getLong("id"));
                    }
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            dbMutex.release();
        }

        connectionActive = true;

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
