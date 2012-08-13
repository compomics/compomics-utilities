package com.compomics.util.db;

import com.compomics.util.gui.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An object cache can be combined to an ObjectDB to improve its performance. A
 * single cache can be used by different databases. This ough not to be
 * serialized. The length of lists/maps in the cache shall stay independent from
 * the number of objects in cache.
 *
 * @author Marc Vaudel
 */
public class ObjectsCache {

    /**
     * Map of the databases for which this cache should be used.
     */
    private HashMap<String, ObjectsDB> databases = new HashMap<String, ObjectsDB>();
    /**
     * The cache size in number of matches.
     */
    private int cacheSize = 0;
    /**
     * boolean indicating whether the memory management should be done
     * automatically. If true, the cache size will be extended to reach 99% of
     * the available heap size. True by default.
     */
    private boolean automatedMemoryManagement = true;
    /**
     * Share of the memory to be used.
     */
    private double memoryShare = 0.99;
    /**
     * Map of the loaded matches. db -> table -> object key -> object.
     */
    private HashMap<String, HashMap<String, HashMap<String, CacheEntry>>> loadedObjectsMap = new HashMap<String, HashMap<String, HashMap<String, CacheEntry>>>();
    /**
     * Map of the loaded objects with the most used matches in the end.
     */
    private ArrayList<String> loadedObjectsKeys = new ArrayList<String>();
    /**
     * Separator used to concatenate strings.
     */
    private static final String cacheSeparator = "_ccs_";
    /**
     * The standard batch size for saving objects in databases
     */
    private int batchSize = 10000;

    /**
     * Constructor.
     */
    public ObjectsCache() {
    }

    /**
     * Returns whether the cache is in automated memory management mode.
     *
     * @return a boolean indicating whether the cache is in automated memory
     * management mode
     */
    public boolean isAutomatedMemoryManagement() {
        return automatedMemoryManagement;
    }

    /**
     * Sets whether the cache is in automated memory management mode.
     *
     * @param automatedMemoryManagement a boolean indicating whether the cache
     * is in automated memory management mode
     */
    public void setAutomatedMemoryManagement(boolean automatedMemoryManagement) {
        this.automatedMemoryManagement = automatedMemoryManagement;
    }

    /**
     * Returns the cache size in number of objects.
     *
     * @return the cache size in number of objects
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Sets the cache size in number of objects.
     *
     * @param cacheSize the cache size in number of objects
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Returns the batch size in number of objects.
     *
     * @return the batch size in number of objects
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Sets the batch size in number of objects.
     *
     * @param batchSize the batch size in number of objects
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Returns the share of heap size which can be used before emptying the
     * cache. 0.99 (default) means that objects will be removed from the cache
     * as long as more than 99% of the heap size is used.
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
    }

    /**
     * Adds a database in the list of the databases handled by the cache. If a
     * database with the same name is already present it will be silently
     * replaced.
     *
     * @param objectsDB the objects database
     */
    public void addDb(ObjectsDB objectsDB) {
        String dbName = objectsDB.getName();
        if (dbName.contains(cacheSeparator)) {
            throw new IllegalArgumentException("Database name (" + dbName + ") should not contain " + cacheSeparator);
        }
        databases.put(dbName, objectsDB);
    }

    /**
     * Removes an object from the cache mappings.
     *
     * @param dbName the name of the database
     * @param tableName the name of the table
     * @param objectKey the key of the object
     */
    public void removeObject(String dbName, String tableName, String objectKey) {
        String cacheKey = getCacheKey(dbName, tableName, objectKey);
        loadedObjectsKeys.remove(cacheKey);
        if (loadedObjectsMap.containsKey(dbName) && loadedObjectsMap.get(dbName).containsKey(tableName)) {
            loadedObjectsMap.get(dbName).get(tableName).remove(objectKey);
        }
    }

    /**
     * Returns the entry if present in the cache. Null if not.
     *
     * @param dbName the name of the database
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @return the entry of interest, null if not present in the cache
     */
    private CacheEntry getEntry(String dbName, String tableName, String objectKey) {
        if (loadedObjectsMap.containsKey(dbName) && loadedObjectsMap.get(dbName).containsKey(tableName)) {
            return loadedObjectsMap.get(dbName).get(tableName).get(objectKey);
        }
        return null;
    }

    /**
     * Returns the objects if present in the cache. Null if not.
     *
     * @param dbName the name of the database
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @return the object of interest, null if not present in the cache
     */
    public Object getObject(String dbName, String tableName, String objectKey) {
        CacheEntry entry = getEntry(dbName, tableName, objectKey);
        if (entry != null) {
            if (!memoryCheck()) {
                // if we are encountering memory issues, put the most used object at the back so that they stay in cache
                String entryKey = getCacheKey(dbName, tableName, objectKey);
                for (int i = 0; i <= Math.min(100000, loadedObjectsKeys.size() / 2); i++) {
                    if (entryKey.equals(loadedObjectsKeys.get(i))) {
                        loadedObjectsKeys.remove(i);
                        loadedObjectsKeys.add(entryKey);
                        break;
                    }
                }
            }
            return entry.getObject();
        } else {
            return null;
        }
    }

    /**
     * Sets that a match has been modified and returns true in case of success.
     *
     * @param dbName the name of the database
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @param object
     * @return returns a boolean indicating that the entry was in cache and has
     * been updated. False otherwise.
     */
    public boolean updateObject(String dbName, String tableName, String objectKey, Object object) {
        CacheEntry entry = getEntry(dbName, tableName, objectKey);
        if (entry != null) {
            entry.setModified(true);
            entry.setObject(object);
            return true;
        }
        return false;
    }

    /**
     * Adds an object to the cache. The object must not necessarily be in the
     * database. If an object is already present with the same identifiers, it
     * will be silently erased.
     *
     * @param dbName the name of the database
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @param object the object to store in the cache
     * @throws IOException
     * @throws SQLException
     */
    public void addObject(String dbName, String tableName, String objectKey, Object object) throws IOException, SQLException {
        if (dbName.contains(cacheSeparator)) {
            throw new IllegalArgumentException("Database name (" + dbName + ") should not contain " + cacheSeparator);
        } else if (tableName.contains(cacheSeparator)) {
            throw new IllegalArgumentException("Table name (" + tableName + ") should not contain " + cacheSeparator);
        } else if (objectKey.contains(cacheSeparator)) {
            throw new IllegalArgumentException("Object key (" + objectKey + ") should not contain " + cacheSeparator);
        }
        loadedObjectsKeys.add(getCacheKey(dbName, tableName, objectKey));
        if (!loadedObjectsMap.containsKey(dbName)) {
            loadedObjectsMap.put(dbName, new HashMap<String, HashMap<String, CacheEntry>>());
        }
        if (!loadedObjectsMap.get(dbName).containsKey(tableName)) {
            loadedObjectsMap.get(dbName).put(tableName, new HashMap<String, CacheEntry>());
        }
        loadedObjectsMap.get(dbName).get(tableName).put(objectKey, new CacheEntry(object, true));
        updateCache();
    }

    /**
     * Indicates whether the memory used by the application is lower than 99% of
     * the heap size.
     *
     * @return a boolean indicating whether the memory used by the application
     * is lower than 99% of the heap
     */
    public boolean memoryCheck() {
        return Runtime.getRuntime().totalMemory() < (long) (memoryShare * Runtime.getRuntime().maxMemory());
    }

    /**
     * Saves an entry in the database if modified and clears it from the cache.
     *
     * @param entryKeys the keys of the entries
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void saveObjects(ArrayList<String> entryKeys) throws IOException, SQLException {
        saveObjects(entryKeys, null, true);
    }

    /**
     * Saves an entry in the database if modified and clears it from the cache.
     *
     * @param entryKeys the keys of the entries
     * @param waitingHandler a waiting handler displaying progress to the user.
     * Can be null. Progress will be displayed as secondary.
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void saveObjects(ArrayList<String> entryKeys, WaitingHandler waitingHandler) throws IOException, SQLException {
        saveObjects(entryKeys, waitingHandler, true);
    }

    /**
     * Saves an entry in the database if modified.
     *
     * @param entryKeys the keys of the entries
     * @param waitingHandler a waiting handler displaying progress to the user.
     * Can be null. Progress will be displayed as secondary.
     * @param clearEntries a boolean indicating whether the entry shall be
     * cleared from the cache
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void saveObjects(ArrayList<String> entryKeys, WaitingHandler waitingHandler, boolean clearEntries) throws IOException, SQLException {
        if (waitingHandler != null) {
            waitingHandler.setMaxSecondaryProgressValue(2 * entryKeys.size());
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
        }
        // temporary map for batch saving
        HashMap<String, HashMap<String, HashMap<String, Object>>> toSave = new HashMap<String, HashMap<String, HashMap<String, Object>>>();
        for (String entryKey : entryKeys) {
            String[] splittedKey = getKeyComponents(entryKey);
            String dbName = splittedKey[0];
            String tableName = splittedKey[1];
            String objectKey = splittedKey[2];
            CacheEntry entry = loadedObjectsMap.get(dbName).get(tableName).get(objectKey);
            if (entry.isModified()) {
                if (!toSave.containsKey(dbName)) {
                    toSave.put(dbName, new HashMap<String, HashMap<String, Object>>());
                }
                if (!toSave.get(dbName).containsKey(tableName)) {
                    toSave.get(dbName).put(tableName, new HashMap<String, Object>());
                }
                toSave.get(dbName).get(tableName).put(objectKey, entry.getObject());
            } else if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
            }
        }
        for (String dbName : toSave.keySet()) {
            ObjectsDB objectsDB = databases.get(dbName);
            for (String tableName : toSave.get(dbName).keySet()) {
                objectsDB.insertObjects(tableName, toSave.get(dbName).get(tableName), waitingHandler);
            }
        }
        if (waitingHandler == null || !waitingHandler.isRunCanceled()) {
            if (clearEntries) {
                for (String entryKey : entryKeys) {
                    String[] splittedKey = getKeyComponents(entryKey);
                    String dbName = splittedKey[0];
                    String tableName = splittedKey[1];
                    String objectKey = splittedKey[2];
                    loadedObjectsKeys.remove(entryKey);
                    loadedObjectsMap.get(dbName).get(tableName).remove(objectKey);
                    if (loadedObjectsMap.get(dbName).get(tableName).isEmpty()) {
                        loadedObjectsMap.get(dbName).remove(tableName);
                    }
                    if (loadedObjectsMap.get(dbName).isEmpty()) {
                        loadedObjectsMap.remove(dbName);
                    }
                }
            }
        }
    }

    /**
     * Saves an entry in the database if modified and clears it from the cache.
     * Batch saving should be used instead when possible in order to limit the
     * interactions with the database. See method saveObjects
     *
     * @param entryKey the key of the entry
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void saveObject(String entryKey) throws IOException, SQLException {
        saveObject(entryKey, true);
    }

    /**
     * Saves an entry in the database if modified. Batch saving should be used
     * instead in order to limit the interactions with the database. See method
     * saveObjects
     *
     * @param entryKey the key of the entry
     * @param clearEntry a boolean indicating whether the entry shall be cleared
     * from the cache
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void saveObject(String entryKey, boolean clearEntry) throws IOException, SQLException {
        String[] splittedKey = getKeyComponents(entryKey);
        String dbName = splittedKey[0];
        String tableName = splittedKey[1];
        String objectKey = splittedKey[2];
        CacheEntry entry = loadedObjectsMap.get(dbName).get(tableName).get(objectKey);
        if (entry.isModified()) {
            try {
                ObjectsDB objectsDB = databases.get(dbName);
                if (objectsDB == null) {
                    throw new IllegalStateException("Database " + dbName + " not loaded in cache");
                }
                if (objectsDB.inDB(tableName, objectKey, false)) {
                    objectsDB.updateObject(tableName, objectKey, entry.getObject(), false);
                } else {
                    objectsDB.insertObject(tableName, objectKey, entry.getObject(), false);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error while writing match " + objectKey + " in table " + tableName + " in database" + dbName + ".");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error while writing match " + objectKey + " in table " + tableName + " in database" + dbName + ".");
            }
        }
        if (clearEntry) {
            loadedObjectsKeys.remove(entryKey);
            loadedObjectsMap.get(dbName).get(tableName).remove(objectKey);
            if (loadedObjectsMap.get(dbName).get(tableName).isEmpty()) {
                loadedObjectsMap.get(dbName).remove(tableName);
            }
            if (loadedObjectsMap.get(dbName).isEmpty()) {
                loadedObjectsMap.remove(dbName);
            }
        }
    }

    /**
     * Updates the cache according to the memory settings.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void updateCache() throws IOException, SQLException {
        while (!automatedMemoryManagement && loadedObjectsKeys.size() > cacheSize
                || automatedMemoryManagement && !memoryCheck()) {
            int toRemove = Math.min(batchSize, loadedObjectsKeys.size() / 2);
            if (toRemove <= 1) {
                saveObject(loadedObjectsKeys.get(0));
            } else {
                ArrayList<String> keysToRemove = new ArrayList<String>(loadedObjectsKeys.subList(0, toRemove));
                saveObjects(keysToRemove);
            }
            if (loadedObjectsKeys.isEmpty()) {
                break;
            }
        }
    }

    /**
     * Reduces the memory consumption by saving the given share of hits.
     *
     * @param share the share to be saved, 0.25 means that 25% of the hits will
     * be saved
     * @param waitingHandler a waiting handler on which the progress will be
     * displayed as secondary progress. can be null
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void reduceMemoryConsumption(double share, WaitingHandler waitingHandler) throws IOException, SQLException {
        int toRemove = (int) (share * loadedObjectsKeys.size());
        ArrayList<String> keysToRemove = new ArrayList<String>(loadedObjectsKeys.subList(0, toRemove));
        saveObjects(keysToRemove, waitingHandler);
    }

    /**
     * Indicates whether an object is loaded in the cache
     *
     * @param dbName the database name
     * @param tableName the table name
     * @param objectKey the object key
     * @return a boolean indicating whether an object is loaded in the cache
     */
    public boolean inCache(String dbName, String tableName, String objectKey) {
        return loadedObjectsMap.containsKey(dbName) && loadedObjectsMap.get(dbName).containsKey(tableName) && loadedObjectsMap.get(dbName).get(tableName).containsKey(objectKey);
    }

    /**
     * Saves the cache content in the database.
     *
     * @param waitingHandler a waiting handler on which the progress will be
     * @param emptyCache boolean indicating whether the cache content shall be
     * cleared while saving displayed as secondary progress. can be null
     * @throws SQLException exception thrown whenever an error occurred while
     * adding the object in the database
     * @throws IOException exception thrown whenever an error occurred while
     * writing the object
     */
    public void saveCache(WaitingHandler waitingHandler, boolean emptyCache) throws IOException, SQLException {
        if (waitingHandler != null) {
            waitingHandler.setMaxSecondaryProgressValue(loadedObjectsKeys.size() + 1);
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setSecondaryProgressValue(0);
        }

//        // individual option
//        ArrayList<String> toRemove = new ArrayList<String>(loadedObjectsKeys);
//        for (String entryKey : toRemove) {
//            saveObject(entryKey, emptyCache);
//            if (waitingHandler != null) {
//                waitingHandler.increaseSecondaryProgressValue();
//                if (waitingHandler.isRunCanceled()) {
//                    return;
//                }
//            }
//        }

        // grouped option
        for (String dbName : loadedObjectsMap.keySet()) {
            ObjectsDB objectsDB = databases.get(dbName);
            if (objectsDB == null) {
                throw new IllegalStateException("Database " + dbName + " not loaded in cache");
            }
            for (String tableName : loadedObjectsMap.get(dbName).keySet()) {
                HashMap<String, Object> objectsToStore = new HashMap<String, Object>();
                HashMap<String, CacheEntry> data = loadedObjectsMap.get(dbName).get(tableName);
                for (String objectKey : data.keySet()) {
                    CacheEntry entry = loadedObjectsMap.get(dbName).get(tableName).get(objectKey);
                    if (entry.isModified()) {
                        objectsToStore.put(objectKey, entry.getObject());
                    } else if (waitingHandler != null) {
                        waitingHandler.increaseSecondaryProgressValue();
                        if (waitingHandler.isRunCanceled()) {
                            return;
                        }
                    }
                }
                objectsDB.insertObjects(tableName, objectsToStore, waitingHandler);
            }
        }

        if (emptyCache) {
            loadedObjectsMap.clear();
            loadedObjectsKeys.clear();
        }
    }

    /**
     * Returns the cache key which will index an object based on its db name,
     * table name and object key.
     *
     * @param dbName the DB name
     * @param tableName the table name
     * @param objectKey the object key
     * @return the cache key which will index an object based on its db name,
     * table name and object key
     */
    private String getCacheKey(String dbName, String tableName, String objectKey) {
        return dbName + cacheSeparator + tableName + cacheSeparator + objectKey;
    }

    /**
     * Returns the key components in an array: 0 -> DB name 1 -> table name 2 ->
     * object key.
     *
     * @param cacheKey the key used by the cache
     * @return the components of the key
     */
    private String[] getKeyComponents(String cacheKey) {
        return cacheKey.split(cacheSeparator);
    }

    /**
     * Class representing a cache entry.
     */
    private class CacheEntry {

        /**
         * The object of this entry.
         */
        private Object object;
        /**
         * A boolean indicating whether this entry is modified when compared to
         * the version of the database. Only modified entries will be saved when
         * the cache is emptied.
         */
        private boolean modified;

        /**
         * Constructor.
         *
         * @param object the object of the entry
         * @param modified boolean indicating whether the entry is modified
         */
        public CacheEntry(Object object, boolean modified) {
            this.object = object;
            this.modified = modified;
        }

        /**
         * Indicates whether the object is modified when compared to the version
         * in the database.
         *
         * @return a boolean indicating whether the object is modified when
         * compared to the version in the database
         */
        public boolean isModified() {
            return modified;
        }

        /**
         * Sets whether the object is modified when compared to the version in
         * the database.
         *
         * @param modified a boolean indicating whether the object is modified
         * when compared to the version in the database
         */
        public void setModified(boolean modified) {
            this.modified = modified;
        }

        /**
         * Returns the object of this entry.
         *
         * @return the object contained by this entry
         */
        public Object getObject() {
            return object;
        }

        /**
         * Sets the object of this cache entry.
         *
         * @param object the object for this entry
         */
        public void setObject(Object object) {
            this.object = object;
        }
    }
}
