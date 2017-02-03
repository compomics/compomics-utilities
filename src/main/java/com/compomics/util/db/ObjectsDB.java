package com.compomics.util.db;

import com.compomics.util.Util;
import com.compomics.util.waiting.WaitingHandler;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import javax.sql.rowset.serial.SerialBlob;

/**
 * A database which can easily be used to store objects.
 *
 * @author Marc Vaudel
 */
public class ObjectsDB implements Serializable {

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
     * The connection, shall not be accessed outside this class.
     */
    private Connection dbConnection;
    /**
     * The maximal length of a table name.
     */
    public static final int TABLE_NAME_MAX_LENGTH = 128;
    /**
     * The maximal length of a varchar. Note: 32672 is the max length for a
     * varchar.
     */
    public static final int VARCHAR_MAX_LENGTH = 32672;
    /**
     * The maximum key length before using the key correction for long keys.
     */
    public static final int MAX_KEY_LENGTH = 1000;
    /**
     * List of keys too long to create a table.
     */
    private ArrayList<String> longTableNames = new ArrayList<String>();
    /**
     * Map of the keys too long to be stored in the database indexed by table
     * name.
     */
    private HashMap<String, ArrayList<String>> longKeysMap = new HashMap<String, ArrayList<String>>();
    /**
     * Tables that have already been used. Will be null for projects older than
     * 4.7.0.
     */
    private HashSet<String> usedTables = new HashSet<String>();
    /**
     * Number of tables where the content should be stored in memory.
     */
    private int tablesContentCacheSize = 4;
    /**
     * Cache for the content of the tables. Will be null for projects older than
     * 4.10.1.
     */
    private HashMap<String, HashSet<String>> tablesContentCache = new HashMap<String, HashSet<String>>(tablesContentCacheSize);
    private HashSet<String> filledTables = new HashSet<String>();
    /**
     * The table where to save the long keys. Note: needs to keep the same value
     * for backward compatibility
     */
    public static final String DB_ATTRIBUTES = "long_key_table";
    /**
     * Suffix used for long keys.
     */
    public static final String LONG_KEY_PREFIX = "long_key_";
    /**
     * Name for the long table names.
     */
    public static final String LONG_TABLE_NAMES = "long_tables";
    /**
     * The table where to save the long keys.
     */
    public static final String USED_TABLES_TABLE = "used_tables_table";
    /**
     * The name of the table to use to log connections.
     */
    public static final String CONNECTION_LOG_TABLE = "connection_log_table";
    /**
     * The cache to be used for the objects.
     */
    private ObjectsCache objectsCache;
    /**
     * The writer used to send the output to file.
     */
    private BufferedWriter debugSpeedWriter;
    /**
     * The writer used to send the output to file.
     */
    private BufferedWriter debugContentWriter;
    /**
     * The debug folder.
     */
    private File debugFolder;
    /**
     * A boolean indicating whether the database is being queried.
     */
    private boolean loading = false;
    /**
     * Mutex for the interaction with the database.
     */
    private Semaphore mutex = new Semaphore(1);
    /**
     * The name of the table currently updating in the queue.
     */
    private String tableQueueUpdating = "";
    /**
     * A queue of entire tables to load.
     */
    private ArrayList<String> tableQueue = new ArrayList<String>();
    /**
     * A queue of table components to load.
     */
    private HashMap<String, ArrayList<String>> contentQueue = new HashMap<String, ArrayList<String>>();
    /**
     * A queue of tables of content to load.
     */
    private ArrayList<String> contentTableQueue = new ArrayList<String>();
    /**
     * Debug, if true will output a table containing statistics on the speed of
     * the objects I/O.
     */
    private boolean debugSpeed = false;
    /**
     * Debug, if true will output a table containing details on the objects
     * stored.
     */
    private boolean debugContent = false;
    /**
     * Debug, if true, all interaction with the database will be logged in the
     * System.out stream.
     */
    private boolean debugInteractions = false;
    /**
     * If true, SQLite is used as the database, if false Derby is used.
     */
    private boolean useSQLite = false;
    /**
     * The identifier used to register the derby connection in the DerbyUtil
     * class.
     */
    public static final String derbyConnectionID = "objectsDB";

    /**
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     * @param deleteOldDatabase if true, tries to delete the old database
     * @param objectsCache a cache to store objects without interacting with the
     * database
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
    public ObjectsDB(String folder, String dbName, boolean deleteOldDatabase, ObjectsCache objectsCache) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        this.dbName = dbName;
        objectsCache.addDb(this);
        establishConnection(folder, deleteOldDatabase, objectsCache);
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
        objectCache.addDb(this);
    }

    /**
     * Adds the desired table in the database.
     *
     * @param tableName the name of the table
     *
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with the database
     * @throws java.lang.InterruptedException exception thrown whenever a
     * threading error occurred
     */
    public void addTable(String tableName) throws SQLException, InterruptedException {
        if (debugInteractions) {
            System.out.println("Inserting table, table: " + tableName);
        }
        Statement stmt = dbConnection.createStatement();
        mutex.acquire();
        try {
            stmt.execute("CREATE table " + tableName + " ("
                    + "NAME VARCHAR(" + VARCHAR_MAX_LENGTH + ") PRIMARY KEY,"
                    + "MATCH_BLOB blob"
                    + ")");
        } catch (SQLException e) {
            System.out.println("An error occurred while creating table " + tableName);
            throw (e);
        } finally {
            stmt.close();
        }
        mutex.release();
    }

    /**
     * Indicates whether the database contains the given table.
     *
     * @param tableName the name of the table of interest
     *
     * @return a boolean indicating whether the database contains the given
     * table
     *
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with the database
     * @throws java.lang.InterruptedException exception thrown whenever a
     * threading error occurred
     */
    public boolean hasTable(String tableName) throws SQLException, InterruptedException {

        if (tableName.startsWith("\"") && tableName.endsWith("\"")) {
            tableName = tableName.substring(1, tableName.length() - 1);
        }

        ArrayList<String> tables = getTables();
        for (String tempTable : tables) {
            if (tempTable.equalsIgnoreCase(tableName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a list of tables present in the database. Note: this includes
     * system tables.
     *
     * @return a list of tables present in the database
     *
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with the database
     * @throws java.lang.InterruptedException exception thrown whenever a
     * threading error occurred
     */
    public ArrayList<String> getTables() throws SQLException, InterruptedException {

        mutex.acquire();
        DatabaseMetaData dmd = dbConnection.getMetaData();
        ArrayList<String> result = new ArrayList<String>();
        ResultSet rs = dmd.getTables(null, null, null, null); //@TODO: not sure to which extend this is Derby dependent...

        try {
            while (rs.next()) {
                String tempDbName = (String) rs.getObject("TABLE_NAME");
                result.add(tempDbName);
            }
        } finally {
            rs.close();
        }
        mutex.release();

        return result;
    }

    /**
     * Stores an object in the desired table. When multiple objects are to be
     * inserted, use insertObjects instead.
     *
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @param object the object to store
     * @param inCache boolean indicating whether the method shall try to put the
     * object in cache or not
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * storing the object
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred while interacting with the database
     */
    public void insertObject(String tableName, String objectKey, Object object, boolean inCache) throws SQLException, IOException, InterruptedException {

        String correctedKey = correctKey(tableName, objectKey);

        if (inCache) {
            objectsCache.addObject(dbName, tableName, correctedKey, object, true, true);
        } else {
            insertObject(tableName, objectKey, correctedKey, object, inCache);
        }
    }

    /**
     * Stores an object in the desired table. When multiple objects are to be
     * inserted, use insertObjects instead.
     *
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @param correctedKey the corrected key
     * @param object the object to store
     * @param inCache boolean indicating whether the method shall try to put the
     * object in cache or not
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * storing the object
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred while interacting with the database
     */
    public void insertObject(String tableName, String objectKey, String correctedKey, Object object, boolean inCache) throws SQLException, IOException, InterruptedException {

        if (debugInteractions) {
            System.out.println("Inserting single object, table: " + tableName + ", key: " + objectKey);
        }
        if (usedTables != null && !usedTables.contains(tableName)) {
            usedTables.add(tableName);
        }
        mutex.acquire();
        PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?)");
        try {
            ps.setString(1, correctedKey);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                try {
                    oos.writeObject(object);
                } finally {
                    oos.close();
                }
            } finally {
                bos.close();
            }
            ps.setBytes(2, bos.toByteArray());
            ps.executeUpdate();
        } finally {
            ps.close();
        }

        tablesContentCache.remove(tableName);

        mutex.release();
    }

    /**
     * Inserts a set of objects in the given table.
     *
     * @param tableName the name of the table
     * @param objects map of the objects (object key &gt; object)
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The progress will be displayed on the secondary progress bar.
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred
     */
    public void insertObjects(String tableName, HashMap<String, Object> objects, WaitingHandler waitingHandler) throws SQLException, IOException, InterruptedException {
        HashMap<String, Object> objectsClone = new HashMap<String, Object>(objects);
        insertObjects(tableName, objectsClone, waitingHandler, false);
    }

    /**
     * Inserts a set of objects in the given table.
     *
     * @param tableName the name of the table
     * @param objects map of the objects (object key &gt; object)
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The progress will be displayed on the secondary progress bar.
     * @param allNewObjects boolean indicating whether all objects are new
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws InterruptedException exception thrown whenever a threading error
     * occurred
     */
    public void insertObjects(String tableName, HashMap<String, Object> objects, WaitingHandler waitingHandler, boolean allNewObjects) throws SQLException, IOException, InterruptedException {
        if (debugInteractions) {
            System.out.println("Preparing table insertion: " + tableName);
        }
        if (usedTables != null && !usedTables.contains(tableName)) {
            usedTables.add(tableName);
        }

        mutex.acquire();

        HashSet<String> tableContent = new HashSet<String>();
        if (!allNewObjects) {
            tableContent = getTableContentFromDBNoMutex(tableName);
        }

        PreparedStatement insertStatement = dbConnection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?)");
        try {
            PreparedStatement updateStatement = dbConnection.prepareStatement("UPDATE " + tableName + " SET MATCH_BLOB=? WHERE NAME=?");
            int batchCpt = 0;
            try {
                dbConnection.setAutoCommit(false);
                int rowCounter = 0;

                for (String objectKey : objects.keySet()) {

                    String correctedKey = correctKey(tableName, objectKey);

                    if (debugContent) {
                        if (debugInteractions) {
                            System.out.println("Inserting batch of objects, table: " + tableName + ", key: " + objectKey);
                        }
                        File debugObjectFile = new File(debugFolder, "debugMatch");
                        FileOutputStream fos = new FileOutputStream(debugObjectFile);
                        BufferedOutputStream debugBos = new BufferedOutputStream(fos);
                        ObjectOutputStream debugOos = new ObjectOutputStream(debugBos);
                        debugOos.writeObject(objects.get(objectKey));
                        debugOos.close();
                        debugBos.close();
                        fos.close();
                        long size = debugObjectFile.length();

                        debugContentWriter.write(tableName + "\t" + objectKey + "\t" + size + "\n");
                        debugContentWriter.flush();
                    }

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        try {
                            oos.writeObject(objects.get(objectKey));

                            if (!allNewObjects && tableContent.contains(correctedKey)) {
                                updateStatement.setString(2, correctedKey);
                                updateStatement.setBytes(1, bos.toByteArray());
                                updateStatement.addBatch();
                            } else {
                                insertStatement.setString(1, correctedKey);
                                insertStatement.setBytes(2, bos.toByteArray());
                                insertStatement.addBatch();
                            }

                            try {
                                if ((++rowCounter) % objectsCache.getBatchSize() == 0) {
                                    updateStatement.executeBatch();
                                    insertStatement.executeBatch();
                                    updateStatement.clearParameters();
                                    insertStatement.clearParameters();
                                    dbConnection.commit();
                                    rowCounter = 0;
                                    batchCpt++;
                                }
                            } catch (java.sql.BatchUpdateException e) {
                                System.out.println("Table " + tableName);
                                System.out.println("Key " + objectKey);
                                System.out.println("Table size " + tableContent.size());
                                System.out.println("batch " + batchCpt);
                                throw e;
                            }

                        } finally {
                            oos.close();
                        }
                    } finally {
                        bos.close();
                    }

                    if (waitingHandler != null) {
                        waitingHandler.increaseSecondaryProgressCounter();
                        if (waitingHandler.isRunCanceled()) {
                            break;
                        }
                    }
                }

                try {
                    if (waitingHandler == null || !waitingHandler.isRunCanceled()) {
                        // insert the remaining data
                        updateStatement.executeBatch();
                        insertStatement.executeBatch();
                        updateStatement.clearParameters();
                        insertStatement.clearParameters();
                        dbConnection.commit();
                    }
                } catch (java.sql.BatchUpdateException e) {
                    System.out.println("Table " + tableName);
                    System.out.println("Key end batch");
                    System.out.println("Table size " + tableContent.size());
                    System.out.println("batch " + batchCpt);
                    throw e;
                }

                dbConnection.setAutoCommit(true);

                // close the statements
            } finally {
                updateStatement.close();
            }
        } finally {
            insertStatement.close();
        }

        tableContent.addAll(objects.keySet());

        filledTables.add(tableName);
        mutex.release();
    }

    /**
     * Loads all objects from a table in the cache.
     *
     * @param tableName the table name
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
    public void loadObjects(String tableName, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        if (usedTables == null || usedTables.contains(tableName)) {
            if (!loading && (tableQueue.isEmpty() || tableQueue.indexOf(tableName) == 0)) {

                if (debugInteractions) {
                    System.out.println("getting table objects, table: " + tableName);
                }
                ResultSet results;
                if (waitingHandler != null && displayProgress) {
                    waitingHandler.setSecondaryProgressCounterIndeterminate(true);

                    // note that using the count statement might take a couple of seconds for a big table, but still better than an indeterminate progressbar.
                    mutex.acquire();
                    Statement rowCountStatement = dbConnection.createStatement();
                    Integer numberOfRows = null;
                    try {
                        results = rowCountStatement.executeQuery("select count(*) from " + tableName);
                        results.next();
                        numberOfRows = results.getInt(1);
                    } finally {
                        rowCountStatement.close();
                    }
                    mutex.release();

                    if (numberOfRows != null) {
                        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                        waitingHandler.setSecondaryProgressCounter(0);
                        waitingHandler.setMaxSecondaryProgressCounter(numberOfRows);
                    }
                }

                HashMap<String, Object> objectsFromDb = new HashMap<String, Object>();

                mutex.acquire();
                loading = true;

                try {
                    Statement stmt = dbConnection.createStatement();
                    try {
                        results = stmt.executeQuery("select * from " + tableName);

                        try {
                            while (results.next()) {

                                if (waitingHandler != null) {
                                    if (waitingHandler.isRunCanceled()) {
                                        break;
                                    }
                                    if (displayProgress) {
                                        waitingHandler.increaseSecondaryProgressCounter();
                                    }
                                }

                                String key = results.getString(1);

                                if (!objectsCache.inCache(dbName, tableName, key)) {

                                    Blob tempBlob;

                                    if (useSQLite) {
                                        byte[] bytes = results.getBytes(2);
                                        tempBlob = new SerialBlob(bytes);
                                    } else {
                                        tempBlob = results.getBlob(2);
                                    }

                                    BufferedInputStream bis = new BufferedInputStream(tempBlob.getBinaryStream());
                                    try {
                                        ObjectInputStream in = new ObjectInputStream(bis);
                                        try {
                                            Object object = in.readObject();
                                            objectsFromDb.put(key, object);
                                        } finally {
                                            in.close();
                                        }
                                    } finally {
                                        bis.close();
                                    }
                                }
                            }

                            tableQueue.remove(tableName);

                        } finally {
                            results.close();
                        }
                    } finally {
                        stmt.close();
                    }

                } finally {
                    loading = false;
                }
                mutex.release();

                for (String key : objectsFromDb.keySet()) {
                    Object object = objectsFromDb.get(key);
                    objectsCache.addObject(dbName, tableName, key, object, false, false);
                }
                objectsCache.updateCache();

            } else {

                if (!tableQueue.contains(tableName)) {
                    tableQueue.add(tableName);
                }

                while (loading) {
                    wait(11);
                }

                loadObjects(tableName, waitingHandler, displayProgress);
            }
        }
    }

    /**
     * Loads some objects from a table in the cache.
     *
     * @param tableName the table name
     * @param keys the keys of the objects to load
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
    public synchronized void loadObjects(String tableName, ArrayList<String> keys, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        if (usedTables == null || usedTables.contains(tableName)) {
            if (!loading && (contentTableQueue.isEmpty() || contentTableQueue.indexOf(tableName) == 0)) {

                if (debugInteractions) {
                    System.out.println("getting " + keys.size() + " objects, table: " + tableName);
                }

                boolean concurrentAccess = tableQueueUpdating.equals(tableName);
                ArrayList<String> queue = null;

                if (!concurrentAccess && contentQueue.get(tableName) != null) {
                    queue = contentQueue.get(tableName);
                    contentTableQueue.remove(tableName);
                    contentQueue.remove(tableName);
                }
                if (queue == null) {
                    queue = keys;
                } else if (keys != queue) {
                    HashSet<String> queueAsSet = new HashSet<String>(queue);
                    for (String key : keys) {
                        if (!queueAsSet.contains(key)) {
                            queue.add(key);
                        }
                    }
                }

                ArrayList<String> toLoad = new ArrayList<String>(queue.size());

                for (String key : queue) {
                    String correctedKey = correctKey(tableName, key);
                    if (objectsCache != null && !objectsCache.inCache(dbName, tableName, correctedKey)) {
                        toLoad.add(correctedKey);
                    }
                }

                if (!toLoad.isEmpty()) {

                    HashMap<String, Object> objectsFromDb = new HashMap<String, Object>(toLoad.size());

                    mutex.acquire();
                    loading = true;

                    try {
                        Statement stmt = dbConnection.createStatement();
                        //Statement stmt = dbConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY); // @TODO: test if this is faster
                        //stmt.setFetchSize(toLoad.size()); // @TODO: test if this is faster

                        try {
                            ResultSet results = stmt.executeQuery("select * from " + tableName);

                            try {
                                int found = 0;

                                while (results.next() && found < toLoad.size()) {
                                    String key = results.getString(1);
                                    if (toLoad.contains(key)) {
                                        found++;
                                        Blob tempBlob;

                                        if (useSQLite) {
                                            byte[] bytes = results.getBytes(2);
                                            tempBlob = new SerialBlob(bytes);
                                        } else {
                                            tempBlob = results.getBlob(2);
                                        }

                                        BufferedInputStream bis = new BufferedInputStream(tempBlob.getBinaryStream());
                                        try {
                                            ObjectInputStream in = new ObjectInputStream(bis);
                                            try {
                                                Object object = in.readObject();
                                                objectsFromDb.put(key, object);
                                            } finally {
                                                in.close();
                                            }
                                        } finally {
                                            bis.close();
                                        }
                                        if (waitingHandler != null && displayProgress) {
                                            waitingHandler.increaseSecondaryProgressCounter();
                                        }
                                    }
                                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                                        break;
                                    }
                                }
                            } finally {
                                results.close();
                            }
                        } finally {
                            stmt.close();
                        }

                    } finally {
                        loading = false;
                    }
                    mutex.release();

                    for (String key : objectsFromDb.keySet()) {
                        Object object = objectsFromDb.get(key);
                        objectsCache.addObject(dbName, tableName, key, object, false, false);
                    }
                    objectsCache.updateCache();
                }
            } else {

                tableQueueUpdating = tableName;
                ArrayList<String> queue = contentQueue.get(tableName);
                if (queue == null) {
                    contentTableQueue.add(tableName);
                    contentQueue.put(tableName, keys);
                } else if (keys == queue) {
                    while (loading) {
                        wait(7);
                    }
                    loadObjects(tableName, keys, waitingHandler, displayProgress);
                } else {
                    HashSet<String> queueAsSet = new HashSet<String>(queue);
                    for (String newItem : keys) {
                        if (!queueAsSet.contains(newItem)) {
                            queue.add(newItem);
                        }
                    }
                }
                tableQueueUpdating = "";
            }
        }
    }

    /**
     * Retrieves an object from the desired table. The key should be unique
     * otherwise the first object will be returned. Returns null if the key is
     * not found. The retrieved object is saved in cache.
     *
     * @param tableName the name of the table
     * @param objectKey the object key
     * @param useDB if useDB is false, null will be returned if the object is
     *
     * @return the object stored in the table
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
    public Object retrieveObject(String tableName, String objectKey, boolean useDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return retrieveObject(tableName, objectKey, useDB, true);
    }

    /**
     * Retrieves an object from the desired table. The key should be unique
     * otherwise the first object will be returned. Returns null if the key is
     * not found.
     *
     * @param tableName the name of the table
     * @param objectKey the object key
     * @param useDB if useDB is false, null will be returned if the object is
     * not in the cache
     * @param useCache if true the retrieved object will be saved in cache
     *
     * @return the object stored in the table
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
    public Object retrieveObject(String tableName, String objectKey, boolean useDB, boolean useCache) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        String correctedKey = correctKey(tableName, objectKey);

        Object object = null;

        if (objectsCache != null) {
            object = objectsCache.getObject(dbName, tableName, correctedKey);
        }

        if (!useDB || object != null) {
            return object;
        } else if (usedTables == null || usedTables.contains(tableName)) {
            return retrieveObject(tableName, objectKey, correctedKey, useDB, useCache);
        } else {
            return null;
        }
    }

    /**
     * Retrieves an object from the desired table. The key should be unique
     * otherwise the first object will be returned. Returns null if the key is
     * not found.
     *
     * @param tableName the name of the table
     * @param objectKey the object key
     * @param correctedKey the corrected object key
     * @param useDB if useDB is false, null will be returned if the object is
     * not in the cache
     * @param useCache if true the retrieved object will be saved in cache
     *
     * @return the object stored in the table.
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
    private Object retrieveObject(String tableName, String objectKey, String correctedKey, boolean useDB, boolean useCache) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        Object object = null;

        if (objectsCache != null) {
            object = objectsCache.getObject(dbName, tableName, correctedKey);
        }

        if (!useDB || object != null) {
            return object;
        }
        if (debugInteractions) {
            System.out.println("Retrieving object, table: " + tableName + ", key: " + objectKey);
        }

        if (dbConnection == null || usedTables != null && !usedTables.contains(tableName)) {
            return object;
        }

        mutex.acquire();

        long start = System.currentTimeMillis();

        Statement stmt = dbConnection.createStatement();

        try {
            ResultSet results = stmt.executeQuery("select MATCH_BLOB from " + tableName + " where NAME='" + correctedKey + "'");
            try {

                if (results.next()) {

                    Blob tempBlob;

                    if (useSQLite) {
                        byte[] bytes = results.getBytes(1);
                        tempBlob = new SerialBlob(bytes);
                    } else {
                        tempBlob = results.getBlob(1);
                    }

                    BufferedInputStream bis = new BufferedInputStream(tempBlob.getBinaryStream());
                    try {
                        ObjectInputStream in = new ObjectInputStream(bis);
                        try {
                            object = in.readObject();
                        } finally {
                            in.close();
                        }
                    } finally {
                        bis.close();
                    }

                    if (debugSpeed) {
                        long loaded = System.currentTimeMillis();

                        File debugObjectFile = new File(debugFolder, "debugMatch");
                        FileOutputStream fos = new FileOutputStream(debugObjectFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(object);
                        oos.close();
                        bos.close();
                        fos.close();

                        long written = System.currentTimeMillis();

                        FileInputStream fis = new FileInputStream(debugObjectFile);
                        bis = new BufferedInputStream(fis);
                        ObjectInputStream in = new ObjectInputStream(bis);
                        Object match = in.readObject();
                        fis.close();
                        bis.close();
                        in.close();
                        long read = System.currentTimeMillis();

                        long size = debugObjectFile.length();

                        long queryTime = loaded - start;
                        long serializationTime = written - loaded;
                        long deserializationTime = read - written;

                        debugSpeedWriter.write(tableName + "\t" + objectKey + "\t" + queryTime + "\t" + serializationTime + "\t" + deserializationTime + "\t" + size + "\n");
                    }
                }

            } finally {
                results.close();
            }
        } finally {
            stmt.close();
        }

        mutex.release();

        if (useCache) {
            objectsCache.addObject(dbName, tableName, objectKey, object, false, true);
        }

        if (object == null) {
            System.out.println("Table: " + tableName);
            System.out.println("Object: " + objectKey);
        }

        return object;
    }

    /**
     * Indicates whether an object is loaded in the given table.
     *
     * @param tableName the table name
     * @param objectKey the object key
     * @param cache a boolean indicating whether the cache should be searched as
     * well
     *
     * @return a boolean indicating whether an object is loaded in the given
     * table
     *
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    public boolean inDB(String tableName, String objectKey, boolean cache) throws SQLException, InterruptedException {

        String correctedKey = correctKey(tableName, objectKey);

        if (cache) {
            if (objectsCache.inCache(dbName, tableName, correctedKey)) {
                return true;
            }
        }

        if (usedTables != null && !usedTables.contains(tableName)) {
            return false;
        }

        return savedInDB(tableName, objectKey, correctedKey, cache);
    }

    /**
     * Indicates whether an object is saved in the given table.
     *
     * @param tableName the table name
     * @param objectKey the object key
     * @param cache a boolean indicating whether the cache should be searched as
     * well
     *
     * @return a boolean indicating whether an object is loaded in the given
     * table
     *
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    private boolean savedInDB(String tableName, String objectKey, String correctedKey, boolean cache) throws SQLException, InterruptedException {

        if (cache) {
            if (objectsCache.inCache(dbName, tableName, correctedKey)) {
                return true;
            }
        }
        if (usedTables != null && !usedTables.contains(tableName)) {
            return false;
        }
        if (debugInteractions) {
            System.out.println("checking db content, table: " + tableName + ", key: " + objectKey);
        }
        mutex.acquire();
        Statement stmt = dbConnection.createStatement();
        boolean result = false;
        try {
            ResultSet results = stmt.executeQuery("select * from " + tableName + " where NAME='" + correctedKey + "'");
            try {
                result = results.next();
            } finally {
                results.close();
            }
        } finally {
            stmt.close();
        }
        mutex.release();

        return result;
    }

    /**
     * Returns an arraylist with the content of a table.
     *
     * @param tableName the name of the table to get the content for
     *
     * @return an arraylist with the content of the table
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    public HashSet<String> getTableContent(String tableName) throws SQLException, InterruptedException {

        HashSet<String> tableContent;
        if (tablesContentCache != null) {
            tableContent = tablesContentCache.get(tableName);
            if (tableContent != null) {
                return tableContent;
            }
        }
        return getTableContentFromDB(tableName);
    }

    /**
     * Returns the content of a table from the database.
     *
     * @param tableName the name of the table to get the content for
     *
     * @return an arraylist with the content of the table
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    private HashSet<String> getTableContentFromDB(String tableName) throws SQLException, InterruptedException {
        mutex.acquire();
        HashSet<String> result = getTableContentFromDBNoMutex(tableName);
        mutex.release();
        return result;
    }

    /**
     * Returns the content of a table from the database without using the mutex.
     *
     * @param tableName the name of the table to get the content for
     *
     * @return an arraylist with the content of the table
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    private HashSet<String> getTableContentFromDBNoMutex(String tableName) throws SQLException, InterruptedException {

        HashSet<String> tableContent;
        if (tablesContentCache != null) {
            tableContent = tablesContentCache.get(tableName);
            if (tableContent != null) {
                return tableContent;
            }
        }

        if (debugInteractions) {
            System.out.println("checking db content, table: " + tableName);
        }

        tableContent = new HashSet<String>();
        Statement stmt = dbConnection.createStatement();

        try {
            ResultSet results = stmt.executeQuery("select * from " + tableName);
            try {
                while (results.next()) {
                    String key = results.getString(1);
                    if (key.startsWith(LONG_KEY_PREFIX)) {
                        key = getOriginalKey(tableName, key);
                    }
                    tableContent.add(key);
                }
            } finally {
                results.close();
            }
        } finally {
            stmt.close();
        }

        if (tablesContentCache != null) {
            if (tablesContentCache.size() == tablesContentCacheSize) {
                String keyToRemove = null;
                for (String key : tablesContentCache.keySet()) {
                    if (!key.equals(tableName)) {
                        keyToRemove = key;
                        break;
                    }
                }
                if (keyToRemove != null) {
                    tablesContentCache.remove(keyToRemove);
                }
            }
            tablesContentCache.put(tableName, tableContent);
        }

        return tableContent;
    }

    /**
     * Deletes an object from the desired table.
     *
     * @param tableName the name of the table
     * @param objectKey the object key
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void deleteObject(String tableName, String objectKey) throws SQLException, IOException, InterruptedException {

        String correctedKey = correctKey(tableName, objectKey);

        // remove from the cache
        objectsCache.removeObject(dbName, tableName, correctedKey);

        // delete from database
        mutex.acquire();
        if (debugInteractions) {
            System.out.println("Removing object, table: " + tableName + ", key: " + objectKey);
        }
        if (usedTables == null || usedTables.contains(tableName)) {
            Statement stmt = dbConnection.createStatement();
            try {
                stmt.executeUpdate("delete from " + tableName + " where NAME='" + correctedKey + "'"); // @TODO: what if the accession contains (') ..? - a single quotation mark is the escape character for a single quotation mark
            } catch (SQLSyntaxErrorException e) {
                System.out.println("SQL Exception. SQL call: " + "delete from " + tableName + " where NAME='" + correctedKey + "'");
                throw e;
            } finally {
                stmt.close();
            }
        }
        mutex.release();
    }

    /**
     * Updates an object in the cache or in the tables if not in cache.
     *
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @param object the object to store
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * storing the object
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updateObject(String tableName, String objectKey, Object object) throws SQLException, IOException, InterruptedException {
        updateObject(tableName, objectKey, object, true);
    }

    /**
     * Updates an object in the cache or in the tables if not in cache or if
     * cache is wrong.
     *
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @param object the object to store
     * @param cache a boolean indicating whether the method should look in the
     * cache
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * storing the object
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    public void updateObject(String tableName, String objectKey, Object object, boolean cache) throws SQLException, IOException, InterruptedException {

        String correctedKey = correctKey(tableName, objectKey);

        boolean cacheUpdated = false;

        if (cache) {
            cacheUpdated = objectsCache.updateObject(dbName, tableName, correctedKey, object);
        }

        if (!cacheUpdated && (usedTables == null || usedTables.contains(tableName))) {
            updateObjectInDb(tableName, objectKey, correctedKey, object, cache);
        }
    }

    /**
     * Updates an object in the cache or in the tables if not in cache or if
     * cache is wrong.
     *
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @param object the object to store
     * @param cache a boolean indicating whether the method should look in the
     * cache
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * storing the object
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     * @throws java.lang.InterruptedException if the thread is interrupted
     */
    private void updateObjectInDb(String tableName, String objectKey, String correctedKey, Object object, boolean cache) throws SQLException, IOException, InterruptedException {

        boolean cacheUpdated = false;

        if (cache) {
            cacheUpdated = objectsCache.updateObject(dbName, tableName, correctedKey, object);
        }

        if (!cacheUpdated && (usedTables == null || usedTables.contains(tableName))) {
            if (debugInteractions) {
                System.out.println("Updating object, table: " + tableName + ", key: " + objectKey);
            }
            mutex.acquire();
            PreparedStatement ps = dbConnection.prepareStatement("update " + tableName + " set MATCH_BLOB=? where NAME='" + objectKey + "'");
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    try {
                        oos.writeObject(object);
                    } finally {
                        oos.close();
                    }
                } finally {
                    bos.close();
                }
                ps.setBytes(1, bos.toByteArray());
                ps.executeUpdate();
            } finally {
                ps.close();
            }
            mutex.release();
        }
    }

    /**
     * Saves the current date to the CONNECTION_LOG_TABLE.
     *
     * @throws SQLException exception thrown if an error occurs while
     * interacting with the database.
     * @throws IOException exception thrown if an error occurs while reading or
     * writing a file.
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database.
     */
    private synchronized void logConnection() throws SQLException, IOException, InterruptedException {
        if (!hasTable(CONNECTION_LOG_TABLE)) {
            addTable(CONNECTION_LOG_TABLE);
        }
        java.util.Date date = new java.util.Date();
        String key = date + "_" + System.currentTimeMillis();
        insertObject(CONNECTION_LOG_TABLE, key, date, false);
        wait(1);
    }

    /**
     * Loads the attributes from the database.
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database.
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a file from the database.
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database.
     */
    private void loadAttributes() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (hasTable(DB_ATTRIBUTES)) {
            longTableNames = (ArrayList<String>) retrieveObject(DB_ATTRIBUTES, LONG_TABLE_NAMES, true, false);
            longKeysMap = (HashMap<String, ArrayList<String>>) retrieveObject(DB_ATTRIBUTES, LONG_KEY_PREFIX, true, false);
            usedTables = (HashSet<String>) retrieveObject(DB_ATTRIBUTES, USED_TABLES_TABLE, true, false);
        }
    }

    /**
     * Saves the attributes in the database.
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database.
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws InterruptedException exception thrown if a threading error occurs
     * while interacting with the database.
     */
    private void saveAttributes() throws SQLException, IOException, InterruptedException {

        if (!hasTable(DB_ATTRIBUTES)) {
            addTable(DB_ATTRIBUTES);
        }

        // Long table names
        if (inDB(DB_ATTRIBUTES, LONG_TABLE_NAMES, false)) {
            updateObject(DB_ATTRIBUTES, LONG_TABLE_NAMES, longTableNames, false);
        } else {
            insertObject(DB_ATTRIBUTES, LONG_TABLE_NAMES, longTableNames, false);
        }

        // Long keys
        if (inDB(DB_ATTRIBUTES, LONG_KEY_PREFIX, false)) {
            updateObject(DB_ATTRIBUTES, LONG_KEY_PREFIX, longKeysMap, false);
        } else {
            insertObject(DB_ATTRIBUTES, LONG_KEY_PREFIX, longKeysMap, false);
        }

        // used tables
        if (usedTables != null) {
            if (inDB(DB_ATTRIBUTES, USED_TABLES_TABLE, false)) {
                updateObject(DB_ATTRIBUTES, USED_TABLES_TABLE, usedTables, false);
            } else {
                insertObject(DB_ATTRIBUTES, USED_TABLES_TABLE, usedTables, false);
            }
        }
    }

    /**
     * Indicates whether the connection to the DB is active.
     *
     * @return true if the connection to the DB is active
     */
    public boolean isConnectionActive() {
        return path != null && DerbyUtil.isActiveConnection(derbyConnectionID, path);
    }

    /**
     * Closes the db connection.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    public void close() throws SQLException, InterruptedException {

        // Make sure that previous queries are done
        mutex.acquire();
        while (mutex.getQueueLength() > 0) {
            mutex.release();
            wait(5);
            mutex.acquire();
        }
        mutex.release();

        if (dbConnection != null) {
            // try to save the long key indexes
            try {
                saveAttributes();
            } catch (Exception e) {
                if (dbConnection != null) {
                    e.printStackTrace();
                }
            }
        }

        mutex.acquire();
        objectsCache = null;

        try {
            if (dbConnection != null && isConnectionActive()) {
                dbConnection.close();
                DerbyUtil.removeActiveConnection(derbyConnectionID, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (debugSpeed && debugSpeedWriter != null) {
            try {
                debugSpeedWriter.close();
                debugSpeedWriter = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (debugContent && debugContentWriter != null) {
            try {
                debugContentWriter.close();
                debugContentWriter = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dbConnection = null;

        mutex.release();
    }

    /**
     * Establishes connection to the database.
     *
     * @param aDbFolder the folder where the database is located
     * @param deleteOldDatabase if true, tries to delete the old database
     * @param objectsCache the objects cache
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
    public void establishConnection(String aDbFolder, boolean deleteOldDatabase, ObjectsCache objectsCache) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        File parentFolder = new File(aDbFolder);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }
        File dbFolder = new File(aDbFolder, dbName);
        path = dbFolder.getAbsolutePath();

        // close the old connection and delete the db folder
        if (dbFolder.exists() && deleteOldDatabase) {

            close();

            DerbyUtil.closeConnection();
            boolean deleted = Util.deleteDir(dbFolder);
            //TODO: Restore connections?

            if (!deleted) {
                System.out.println("Failed to delete db folder: " + dbFolder.getPath());
            }
        }

        mutex.acquire();

        if (useSQLite) {
            try {
                Class.forName("org.sqlite.JDBC");
                dbConnection = DriverManager.getConnection("jdbc:sqlite:" + path); // @TODO: another instance of SQLite may have already booted the database. We need to check this first?
            } catch (SQLException e) {
                // try using Derby instead
                useSQLite = false;
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        if (!useSQLite) {
            if (isConnectionActive()) {
                throw new IllegalArgumentException("Impossible to establish a Derby connection in " + path + ", connection to the folder already active.");
            }
            String url = "jdbc:derby:" + path + ";create=true";
            dbConnection = DriverManager.getConnection(url);
            DerbyUtil.addActiveConnection(derbyConnectionID, path);
        }

        // special fix for if derby breaks down and restarts in read only mode
        if (dbConnection != null) {
            dbConnection.setReadOnly(false);
        }

        this.objectsCache = objectsCache;

        // debug test speed
        if (debugSpeed) {
            try {
                debugFolder = new File(aDbFolder);
                debugSpeedWriter = new BufferedWriter(new FileWriter(new File(parentFolder, "dbSpeed.txt")));
                debugSpeedWriter.write("Table\tkey\tQuery time\tSerialization time\tDeserialization time\tsize\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // debug test content
        if (debugContent) {
            try {
                debugFolder = new File(aDbFolder);

                String tempFileName = "dbContent.txt";

                int counter = 1;

                // make sure that we don't overwrite the old files
                while (new File(parentFolder, tempFileName).exists()) {
                    tempFileName = "dbContent" + counter++ + ".txt";
                }

                debugContentWriter = new BufferedWriter(new FileWriter(new File(parentFolder, tempFileName)));
                debugContentWriter.write("Table\tkey\tsize\n");
                debugContentWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mutex.release();

        // test the connection by logging the connection in the database
        logConnection();

        // try to load the attributes
        loadAttributes();
    }

    /**
     * Surrounds the table name with quotation marks such that spaces etc are
     * allowed.
     *
     * @param tableName the table name
     *
     * @return the corrected table name
     */
    public String correctTableName(String tableName) {
        tableName = "\"" + tableName + "\"";
        if (longTableNames.contains(tableName)) {
            tableName = "\"" + longTableNames.indexOf(tableName) + "\"";
        } else if (tableName.length() >= TABLE_NAME_MAX_LENGTH) {
            int index = longTableNames.size();
            longTableNames.add(tableName);
            tableName = "\"" + index + "\"";
        }
        if (tableName.length() >= TABLE_NAME_MAX_LENGTH && !tableName.startsWith(LONG_KEY_PREFIX)) {
            throw new IllegalArgumentException("Table name " + tableName + " is too long to be stored in the database.");
        }
        return tableName;
    }

    /**
     * Indexes the long keys by a number.
     *
     * @param tableName the table name
     * @param key the key of the object to be stored
     *
     * @return the corrected table name
     */
    public String correctKey(String tableName, String key) {

        // @TODO: escape special characters:
        //String correctedKey = key.replaceAll("[^\\dA-Za-z ]", "");
        String correctedKey = key;
        if (longKeysMap != null && !correctedKey.startsWith(LONG_KEY_PREFIX)) {
            if (longKeysMap.containsKey(tableName) && longKeysMap.get(tableName).contains(key)) {
                correctedKey = LONG_KEY_PREFIX + longKeysMap.get(tableName).indexOf(key);
            } else if (key.length() >= MAX_KEY_LENGTH) { // @TODO: find the optimal value
                if (!longKeysMap.containsKey(tableName)) {
                    longKeysMap.put(tableName, new ArrayList<String>());
                }
                int index = longKeysMap.get(tableName).size();
                longKeysMap.get(tableName).add(key);
                correctedKey = LONG_KEY_PREFIX + index;
            }
        }

        if (correctedKey.length() >= MAX_KEY_LENGTH && !correctedKey.startsWith(LONG_KEY_PREFIX)) {
            throw new IllegalArgumentException("Object key " + correctedKey + " is too long to be stored in the database.");
        }

        return correctedKey;
    }

    /**
     * Returns the original key of the corrected long key.
     *
     * @param tableName the table
     * @param correctedKey the corrected key, should be prefix + number
     *
     * @return the original long key
     */
    public String getOriginalKey(String tableName, String correctedKey) {

        String subKey = correctedKey.substring(LONG_KEY_PREFIX.length());
        try {
            Integer index = new Integer(subKey);
            return longKeysMap.get(tableName).get(index);
        } catch (Exception e) {
            throw new IllegalArgumentException("An error occurred when getting the original key of " + correctedKey + ".");
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
}
