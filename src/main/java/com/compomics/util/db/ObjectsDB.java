package com.compomics.util.db;

import com.compomics.util.Util;
import com.compomics.util.waiting.WaitingHandler;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.sql.rowset.serial.SerialBlob;

/**
 * A database which can easily be used to store objects.
 *
 * @author Marc Vaudel
 */
public class ObjectsDB implements Serializable {

    /**
     * The version UID for Serialization/Deserialization compatibility.
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
     *
     * @deprecated use longTableNames and longKeysMap instead
     */
    private ArrayList<String> longKeys = new ArrayList<String>();
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
    public static final String LONG_KEY_TABLE = "long_key_table";
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
    private boolean busy = false;
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
        if (dbName == null) {
            // backward compatibility check
            dbName = "old_idDB";
        }
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
     */
    public synchronized void addTable(String tableName) throws SQLException {
        if (debugInteractions) {
            System.out.println("Inserting table, table: " + tableName);
        }
        Statement stmt = dbConnection.createStatement();
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
     */
    public synchronized boolean hasTable(String tableName) throws SQLException {

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
     */
    public synchronized ArrayList<String> getTables() throws SQLException {

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
    public synchronized void insertObject(String tableName, String objectKey, Object object, boolean inCache) throws SQLException, IOException, InterruptedException {

        String correctedKey = correctKey(tableName, objectKey);

        if (inCache) {
            objectsCache.addObject(dbName, tableName, correctedKey, object, true);
        } else {
            if (debugInteractions) {
                System.out.println("Inserting single object, table: " + tableName + ", key: " + objectKey);
            }
            PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?)");
            ps.setString(1, correctedKey);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            try {
                oos.writeObject(object);
            } finally {
                oos.close();
                bos.close();
            }
            ps.setBytes(2, bos.toByteArray());
            ps.executeUpdate();
        }
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
     */
    public void insertObjects(String tableName, HashMap<String, Object> objects, WaitingHandler waitingHandler) throws SQLException, IOException {
        insertObjects(tableName, objects, waitingHandler, false);
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
     */
    public synchronized void insertObjects(String tableName, HashMap<String, Object> objects, WaitingHandler waitingHandler, boolean allNewObjects) throws SQLException, IOException {
        if (debugInteractions) {
            System.out.println("Preparing table insertion: " + tableName);
        }
        PreparedStatement insertStatement = dbConnection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?)");
        try {
            PreparedStatement updateStatement = dbConnection.prepareStatement("UPDATE " + tableName + " SET MATCH_BLOB=? WHERE NAME=?");
            try {
                dbConnection.setAutoCommit(false);
                ArrayList<String> tableContent = new ArrayList<String>();
                if (!allNewObjects) {
                    tableContent = tableContent(tableName);
                }
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

                            if (!allNewObjects && tableContent.contains(objectKey)) {
                                updateStatement.setString(2, correctedKey);
                                updateStatement.setBytes(1, bos.toByteArray());
                                updateStatement.addBatch();
                            } else {
                                insertStatement.setString(1, correctedKey);
                                insertStatement.setBytes(2, bos.toByteArray());
                                insertStatement.addBatch();
                            }

                            if ((++rowCounter) % objectsCache.getBatchSize() == 0) {
                                updateStatement.executeBatch();
                                insertStatement.executeBatch();
                                updateStatement.clearParameters();
                                insertStatement.clearParameters();
                                dbConnection.commit();
                                rowCounter = 0;
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

                if (waitingHandler == null || !waitingHandler.isRunCanceled()) {
                    // insert the remaining data
                    updateStatement.executeBatch();
                    insertStatement.executeBatch();
                    updateStatement.clearParameters();
                    insertStatement.clearParameters();
                    dbConnection.commit();
                }

                dbConnection.setAutoCommit(true);

                // close the statements
            } finally {
                updateStatement.close();
            }
        } finally {
            insertStatement.close();
        }
    }

    /**
     * Loads all objects from a table in the cache.
     *
     * @param tableName the table name
     * @param waitingHandler the waiting handler allowing displaying progress
     * and cancelling the process
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
    public synchronized void loadObjects(String tableName, WaitingHandler waitingHandler, boolean displayProgress) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        if (contentTableQueue == null) { // backward compatibility check
            busy = false;
            tableQueueUpdating = "";
            tableQueue = new ArrayList<String>();
            contentQueue = new HashMap<String, ArrayList<String>>();
            contentTableQueue = new ArrayList<String>();
        }

        if (!busy && (tableQueue.isEmpty() || tableQueue.indexOf(tableName) == 0)) {

            if (debugInteractions) {
                System.out.println("getting table objects, table: " + tableName);
            }
            ResultSet results;
            if (waitingHandler != null && displayProgress) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(true);

                // note that using the count statement might take a couple of seconds for a big table, but still better than an indeterminate progressbar.
                Statement rowCountStatement = dbConnection.createStatement();
                results = rowCountStatement.executeQuery("select count(*) from " + tableName);
                results.next();
                Integer numberOfRows = results.getInt(1);

                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setSecondaryProgressCounter(0);
                waitingHandler.setMaxSecondaryProgressCounter(numberOfRows);
            }

            busy = true;

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

                                ObjectInputStream in = new ObjectInputStream(bis);
                                Object object = in.readObject();
                                in.close();

                                objectsCache.addObject(dbName, tableName, key, object, false);
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
                busy = false;
            }

        } else {

            if (!tableQueue.contains(tableName)) {
                tableQueue.add(tableName);
            }

            while (busy) {
                wait(11);
            }

            loadObjects(tableName, waitingHandler, displayProgress);
        }
    }

    /**
     * Loads some objects from a table in the cache.
     *
     * @param tableName the table name
     * @param keys the keys of the objects to load
     * @param waitingHandler the waiting handler allowing displaying progress
     * and cancelling the process
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

        if (contentTableQueue == null) { // backward compatibility check
            busy = false;
            tableQueueUpdating = "";
            tableQueue = new ArrayList<String>();
            contentQueue = new HashMap<String, ArrayList<String>>();
            contentTableQueue = new ArrayList<String>();
        }

        if (!busy && (contentTableQueue.isEmpty() || contentTableQueue.indexOf(tableName) == 0)) {

            if (debugInteractions) {
                System.out.println("getting " + keys.size() + " objects, table: " + tableName);
            }

            boolean concurrentAccess = tableQueueUpdating.equals(tableName);
            ArrayList<String> queue = new ArrayList<String>();

            if (!concurrentAccess && contentQueue.get(tableName) != null) {
                queue = contentQueue.get(tableName);
                contentTableQueue.remove(tableName);
                contentQueue.remove(tableName);
            }

            if (!keys.equals(queue)) {
                for (String key : keys) {
                    if (!queue.contains(key)) {
                        queue.add(key);
                    }
                }
            }

            ArrayList<String> toLoad = new ArrayList<String>();

            for (String key : queue) {
                String correctedKey = correctKey(tableName, key);
                if (objectsCache != null && !objectsCache.inCache(dbName, tableName, correctedKey)) {
                    toLoad.add(correctedKey);
                }
            }

            if (!toLoad.isEmpty()) {

                busy = true;

                try {
                    Statement stmt = dbConnection.createStatement();
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
                                    ObjectInputStream in = new ObjectInputStream(bis);

                                    try {
                                        Object object = in.readObject();
                                        objectsCache.addObject(dbName, tableName, key, object, false);
                                    } finally {
                                        in.close();
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
                    busy = false;
                }
            }
        } else {

            tableQueueUpdating = tableName;
            if (!contentTableQueue.contains(tableName)) {
                contentTableQueue.add(tableName);
                contentQueue.put(tableName, keys);
            } else if (keys.equals(contentQueue.get(tableName))) {
                while (busy) {
                    wait(7);
                }
                loadObjects(tableName, keys, waitingHandler, displayProgress);
            } else {
                ArrayList<String> queue = contentQueue.get(tableName);
                for (String newItem : keys) {
                    if (!queue.contains(newItem)) {
                        queue.add(newItem);
                    }
                }
            }
            tableQueueUpdating = "";
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
        } else {
            return retrieveObjectSynchronized(tableName, objectKey, correctedKey, useDB, useCache);
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
    private synchronized Object retrieveObjectSynchronized(String tableName, String objectKey, String correctedKey, boolean useDB, boolean useCache) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

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

        if (dbConnection == null) {
            return object;
        }

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
                    if (useCache) {
                        objectsCache.addObject(dbName, tableName, objectKey, object, false);
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

                    return object;
                }

            } finally {
                results.close();
            }
        } finally {
            stmt.close();
        }

        return null;
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
     */
    public boolean inDB(String tableName, String objectKey, boolean cache) throws SQLException {

        String correctedKey = correctKey(tableName, objectKey);

        if (cache) {
            if (objectsCache.inCache(dbName, tableName, correctedKey)) {
                return true;
            }
        }

        return savedInDB(tableName, objectKey, correctedKey, cache);
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
     */
    private synchronized boolean savedInDB(String tableName, String objectKey, String correctedKey, boolean cache) throws SQLException {

        if (cache) {
            if (objectsCache.inCache(dbName, tableName, correctedKey)) {
                return true;
            }
        }
        if (debugInteractions) {
            System.out.println("checking db content, table: " + tableName + ", key: " + objectKey);
        }
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

        return result;
    }

    /**
     * Returns an arraylist with the content in the given table.
     *
     * @param tableName the table to get the content for
     *
     * @return an arraylist with the content in the given table
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     */
    public synchronized ArrayList<String> tableContent(String tableName) throws SQLException {

        if (debugInteractions) {
            System.out.println("checking db content, table: " + tableName);
        }

        ArrayList<String> tableContent = new ArrayList<String>();
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

        return tableContent;
    }

    /**
     * Returns a hashset with the content in the given table.
     *
     * @param tableName the table to get the content for
     *
     * @return a hashset with the content in the given table
     *
     * @throws SQLException exception thrown whenever an error occurs while
     * interacting with the database
     */
    public synchronized HashSet<String> tableContentAsSet(String tableName) throws SQLException {

        if (debugInteractions) {
            System.out.println("checking db content, table: " + tableName);
        }

        HashSet<String> tableContent = new HashSet<String>();
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
     */
    public synchronized void deleteObject(String tableName, String objectKey) throws SQLException, IOException {

        String correctedKey = correctKey(tableName, objectKey);

        // remove from the cache
        objectsCache.removeObject(dbName, tableName, correctedKey);

        // delete from database
        if (debugInteractions) {
            System.out.println("Removing object, table: " + tableName + ", key: " + objectKey);
        }
        Statement stmt = dbConnection.createStatement();
        try {
            stmt.executeUpdate("delete from " + tableName + " where NAME='" + correctedKey + "'");
        } finally {
            stmt.close();
        }
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
     */
    public void updateObject(String tableName, String objectKey, Object object) throws SQLException, IOException {
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
     */
    public void updateObject(String tableName, String objectKey, Object object, boolean cache) throws SQLException, IOException {

        String correctedKey = correctKey(tableName, objectKey);

        boolean cacheUpdated = false;

        if (cache) {
            cacheUpdated = objectsCache.updateObject(dbName, tableName, correctedKey, object);
        }

        if (!cacheUpdated) {
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
     */
    private synchronized void updateObjectInDb(String tableName, String objectKey, String correctedKey, Object object, boolean cache) throws SQLException, IOException {

        boolean cacheUpdated = false;

        if (cache) {
            cacheUpdated = objectsCache.updateObject(dbName, tableName, correctedKey, object);
        }

        if (!cacheUpdated) {
            if (debugInteractions) {
                System.out.println("Updating object, table: " + tableName + ", key: " + objectKey);
            }
            PreparedStatement ps = dbConnection.prepareStatement("update " + tableName + " set MATCH_BLOB=? where NAME='" + objectKey + "'");
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
     * Loads the long key names from the database in the cache.
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
    private void loadLongKeys() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        if (hasTable(LONG_KEY_TABLE)) {
            longTableNames = (ArrayList<String>) retrieveObject(LONG_KEY_TABLE, LONG_TABLE_NAMES, true, false);
            longKeysMap = (HashMap<String, ArrayList<String>>) retrieveObject(LONG_KEY_TABLE, LONG_KEY_PREFIX, true, false);
        }
    }

    /**
     * Saves the long keys in the database.
     *
     * @throws SQLException
     * @throws IOException
     * @throws InterruptedException
     */
    private void saveLongKeys() throws SQLException, IOException, InterruptedException {
        if (!hasTable(LONG_KEY_TABLE)) {
            addTable(LONG_KEY_TABLE);
        }

        // check if it's in the db already
        if (inDB(LONG_KEY_TABLE, LONG_TABLE_NAMES, false)) {
            updateObject(LONG_KEY_TABLE, LONG_TABLE_NAMES, longTableNames, false);
        } else {
            insertObject(LONG_KEY_TABLE, LONG_TABLE_NAMES, longTableNames, false);
        }

        // check if it's in the db already
        if (inDB(LONG_KEY_TABLE, LONG_KEY_PREFIX, false)) {
            updateObject(LONG_KEY_TABLE, LONG_KEY_PREFIX, longKeysMap, false);
        } else {
            insertObject(LONG_KEY_TABLE, LONG_KEY_PREFIX, longKeysMap, false);
        }
    }
    
    /**
     * Indicates whether the connection to the DB is active.
     * 
     * @return true if the connection to the DB is active
     */
    public boolean isConnectionActive() {
        return path != null && DerbyUtil.isActiveConnection(derbyConnectionID, path); // backward compatibility check on the path;
    }

    /**
     * Closes the db connection.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     */
    public synchronized void close() throws SQLException {

        if (dbConnection != null) {
            // try to save the long key indexes
            try {
                saveLongKeys();
            } catch (Exception e) {
                if (dbConnection != null) {
                    e.printStackTrace();
                }
            }
        }

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

        // test the connection by logging the connection in the database
        logConnection();

        // try to load the long keys indexes
        loadLongKeys();
    }

    /**
     * Verifies that the ObjectDB is up to date and makes the necessary fixes.
     */
    private void compatibilityCheck() {
        if (longTableNames == null) {
            // version older than 3.16.0
            longTableNames = new ArrayList<String>(longKeys);
            longKeysMap = new HashMap<String, ArrayList<String>>();
        }
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
        compatibilityCheck();
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

        String correctedKey = key;
        compatibilityCheck();
        if (!correctedKey.startsWith(LONG_KEY_PREFIX)) {
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
        compatibilityCheck();
        try {
            Integer index = new Integer(subKey);
            return longKeysMap.get(tableName).get(index);
        } catch (Exception e) {
            throw new IllegalArgumentException("An error occurred when getting the original key of " + correctedKey + ".");
        }
    }
}
