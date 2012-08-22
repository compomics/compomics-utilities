package com.compomics.util.db;

import com.compomics.util.Util;
import com.compomics.util.gui.waiting.WaitingHandler;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

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
     * The connection, shall not be accessed outside this class. Don't be lazy,
     * implement the dedicated methods here.
     */
    private Connection dbConnection;
    /**
     * List of keys too long to create a table.
     */
    private ArrayList<String> longKeys = new ArrayList<String>();
    /**
     * The cache to be used for the objects
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
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     * @param deleteOldDatabase if true, tries to delete the old database
     * @param objectsCache
     * @throws SQLException
     */
    public ObjectsDB(String folder, String dbName, boolean deleteOldDatabase, ObjectsCache objectsCache) throws SQLException {
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
     * @param blobSize the size of the blob
     * @throws SQLException exception thrown whenever a problem occurred while
     * working with the database
     */
    public void addTable(String tableName, String blobSize) throws SQLException {
        if (tableName.length() >= 128) {
            int index = longKeys.size();
            longKeys.add(tableName);
            tableName = index + "";
        }
        if (debugInteractions) {
            System.out.println("Inserting table, table:" + tableName);
        }
        Statement stmt = dbConnection.createStatement();

        stmt.execute("CREATE table " + tableName + " ("
                + "NAME VARCHAR(3500)," // @TODO: had to increase this from as some keys can be longer. not sure how this affects the db size and speed though...
                + "MATCH_BLOB blob(" + blobSize + ")"
                + ")");

        stmt.close();
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
     * @throws SQLException exception thrown whenever an error occurred while
     * storing the object
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void insertObject(String tableName, String objectKey, Object object, boolean inCache) throws SQLException, IOException {
        if (inCache) {
            objectsCache.addObject(dbName, tableName, objectKey, object);
        } else {
            if (debugInteractions) {
                System.out.println("Inserting single object, table:" + tableName + ", key: " + objectKey);
            }
            PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?)");
            ps.setString(1, objectKey);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.close();
            ps.setBytes(2, bos.toByteArray());
            ps.executeUpdate();
        }
    }

    /**
     * Inserts a set of objects in the given table.
     *
     * @param tableName the name of the table
     * @param objects map of the objects (object key -> object)
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The progress will be displayed on the secondary progress bar.
     * @throws SQLException
     * @throws IOException
     */
    public void insertObjects(String tableName, HashMap<String, Object> objects, WaitingHandler waitingHandler) throws SQLException, IOException {
        if (debugInteractions) {
            System.out.println("Preparing table insertion:" + tableName);
        }
        PreparedStatement insertStatement = dbConnection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?)");
        PreparedStatement updateStatement = dbConnection.prepareStatement("update " + tableName + " set MATCH_BLOB=? where NAME=?");
        dbConnection.setAutoCommit(false);
        ArrayList<String> tableContent = tableContent(tableName);
        int rowCounter = 0;

        for (String objectKey : objects.keySet()) {

            if (debugContent) {
                if (debugInteractions) {
                    System.out.println("Inserting batch of objects, table:" + tableName + ", key: " + objectKey);
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
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(objects.get(objectKey));
            oos.close();

            if (tableContent.contains(objectKey)) {
                updateStatement.setString(2, objectKey);
                updateStatement.setBytes(1, bos.toByteArray());
                updateStatement.addBatch();
            } else {
                insertStatement.setString(1, objectKey);
                insertStatement.setBytes(2, bos.toByteArray());
                insertStatement.addBatch();
            }

            if ((++rowCounter) % objectsCache.getBatchSize() == 0) {
                updateStatement.executeBatch();
                insertStatement.executeBatch();
                dbConnection.commit();
                rowCounter = 0;
            }

            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }

        if (waitingHandler == null || !waitingHandler.isRunCanceled()) {
            // insert the remaining data
            updateStatement.executeBatch();
            insertStatement.executeBatch();
            dbConnection.commit();
        }
        dbConnection.setAutoCommit(true);

        // close the statements
        updateStatement.close();
        insertStatement.close();
    }
    
    /**
     * Loads all objects from a table in the cache. @TODO this deserves a progress bar
     * @param tableName the table name
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public void loadObjects(String tableName) throws SQLException, IOException, ClassNotFoundException {
        
        if (debugInteractions) {
            System.out.println("getting table objects, table:" + tableName);
        }

        Statement stmt = dbConnection.createStatement();
        ResultSet results = stmt.executeQuery("select * from " + tableName);

        while (results.next()) {
            String key = results.getString(1);
            Blob tempBlob = results.getBlob(2);
            BufferedInputStream bis = new BufferedInputStream(tempBlob.getBinaryStream());

            ObjectInputStream in = new ObjectInputStream(bis);
            Object object = in.readObject();
            in.close();

            objectsCache.addObject(dbName, tableName, key, object);
        }

        results.close();
        stmt.close();
    }

    /**
     * Retrieves an object from the desired table. The key should be unique
     * otherwise the first object will be returned. Returns null if the key is
     * not found.
     *
     * @param tableName the name of the table
     * @param objectKey the object key
     * @return the object stored in the table.
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws ClassNotFoundException exception thrown whenever the class of the
     * object is not found when deserializing it.
     */
    public Object retrieveObject(String tableName, String objectKey) throws SQLException, IOException, ClassNotFoundException {

        Object object = objectsCache.getObject(dbName, tableName, objectKey);

        if (object != null) {
            return object;
        }
        if (debugInteractions) {
            System.out.println("Retrieving object, table:" + tableName + ", key: " + objectKey);
        }

        long start = System.currentTimeMillis();

        Statement stmt = dbConnection.createStatement();
        ResultSet results = stmt.executeQuery("select MATCH_BLOB from " + tableName + " where NAME='" + objectKey + "'"); // derby

        if (results.next()) {

            Blob tempBlob = results.getBlob(1);
            BufferedInputStream bis = new BufferedInputStream(tempBlob.getBinaryStream());

            ObjectInputStream in = new ObjectInputStream(bis);
            object = in.readObject();
            in.close();
            results.close();
            stmt.close();

            objectsCache.addObject(dbName, tableName, objectKey, object);

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
                in = new ObjectInputStream(bis);
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

        results.close();
        stmt.close();
        return null;
    }

    /**
     * Indicates whether an object is loaded in the given table.
     *
     * @param tableName the table name
     * @param objectKey the object key
     * @param cache a boolean indicating whether the cache should be searched as
     * well
     * @return a boolean indicating whether an object is loaded in the given
     * table
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     */
    public boolean inDB(String tableName, String objectKey, boolean cache) throws SQLException {

        if (cache) {
            if (objectsCache.inCache(dbName, tableName, objectKey)) {
                return true;
            }
        }
        if (debugInteractions) {
            System.out.println("checking db content, table:" + tableName + ", key: " + objectKey);
        }
        Statement stmt = dbConnection.createStatement();
        ResultSet results = stmt.executeQuery("select * from " + tableName + " where NAME='" + objectKey + "'"); // derby

        boolean result = results.next();
        results.close();
        stmt.close();
        return result;
    }

    /**
     * Returns an arraylist with the content in the given table.
     *
     * @param tableName the table to get the content for
     * @return an arraylist with the content in the given tabl
     * @throws SQLException
     */
    public ArrayList<String> tableContent(String tableName) throws SQLException {

        if (debugInteractions) {
            System.out.println("checking db content, table:" + tableName);
        }

        Statement stmt = dbConnection.createStatement();
        ResultSet results = stmt.executeQuery("select * from " + tableName);
        ArrayList<String> tableContent = new ArrayList<String>();

        while (results.next()) {
            tableContent.add(results.getString(1));
        }

        results.close();
        stmt.close();
        return tableContent;
    }

    /**
     * Deletes an object from the desired table.
     *
     * @param tableName the name of the table
     * @param objectKey the object key
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     * @throws IOException exception thrown whenever an error occurred while
     * interrogating the database
     */
    public void deleteObject(String tableName, String objectKey) throws SQLException, IOException {

        // remove from the cache
        objectsCache.removeObject(dbName, tableName, objectKey);

        // delete from database
        if (debugInteractions) {
            System.out.println("Removing object, table:" + tableName + ", key: " + objectKey);
        }
        Statement stmt = dbConnection.createStatement();
        stmt.executeUpdate("delete from " + tableName + " where NAME='" + objectKey + "'");
        stmt.close();
    }

    /**
     * Updates an object in the cache or in the tables if not in cache.
     *
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @param object the object to store
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
     * @throws SQLException exception thrown whenever an error occurred while
     * storing the object
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void updateObject(String tableName, String objectKey, Object object, boolean cache) throws SQLException, IOException {

        boolean cacheUpdated = false;

        if (cache) {
            cacheUpdated = objectsCache.updateObject(dbName, tableName, objectKey, object);
        }

        if (!cacheUpdated) {
            if (debugInteractions) {
                System.out.println("Updating object, table:" + tableName + ", key: " + objectKey);
            }
            PreparedStatement ps = dbConnection.prepareStatement("update " + tableName + " set MATCH_BLOB=? where NAME='" + objectKey + "'");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.close();
            ps.setBytes(1, bos.toByteArray());
            ps.executeUpdate();
        }
    }

    /**
     * Closes the db connection.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     */
    public void close() throws SQLException {

        objectsCache = null;

        if (dbConnection != null) {
            dbConnection.close();
        }

        try {
            // we also need to shut down derby completely to release the file lock in the database folder
            DriverManager.getConnection("jdbc:derby:;shutdown=true;deregister=false");
        } catch (SQLException e) {
            if (e.getMessage().indexOf("Derby system shutdown") == -1) {
                e.printStackTrace();
            } else {
                // ignore, normal derby shut down always results in an exception thrown
            }
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
     * @param objectsCache
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection
     */
    public void establishConnection(String aDbFolder, boolean deleteOldDatabase, ObjectsCache objectsCache) throws SQLException {

        File parentFolder = new File(aDbFolder);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }
        File dbFolder = new File(aDbFolder, dbName);
        String path = dbFolder.getAbsolutePath();

        // close the old connection and delete the db folder
        if (dbFolder.exists() && deleteOldDatabase) {

            close();

            boolean deleted = Util.deleteDir(dbFolder);

            if (!deleted) {
                System.out.println("Failed to delete db folder: " + dbFolder.getPath());
            }
        }

        String url = "jdbc:derby:" + path + ";create=true";
        dbConnection = DriverManager.getConnection(url);

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

    }

    /**
     * Removes the characters forbidden in table names and puts a '_' instead.
     *
     * @param tableName the table name
     * @return the corrected table name
     */
    public String correctTableName(String tableName) {
        tableName = tableName.replace(" ", "_");
        tableName = tableName.replace("|", "_");
        tableName = tableName.replace("-", "_");
        tableName = tableName.replace("=", "_");
        tableName = tableName.replace(".", "_");

        // @TODO: seems like everything but numbers and letters ought to be replaced??? (couldn't find for derby but from another db: alphanumeric characters and the special characters $, _, and #)

        if (longKeys.contains(tableName)) {
            tableName = longKeys.indexOf(tableName) + "";
        }
        return tableName;
    }
}
