package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.Util;
import com.compomics.util.db.DerbyUtil;
import com.compomics.util.db.ObjectsCache;
import com.compomics.util.db.ObjectsDB;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This factory stores and returns protein trees components from databases.
 *
 * @author Marc Vaudel
 */
public class ProteinTreeComponentsFactory {

    /**
     * Instance of the sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * Instance of the factory.
     */
    private static ProteinTreeComponentsFactory instance = null;
    /**
     * The folder containing the databases.
     */
    private static String defaultDbFolderPath = System.getProperty("user.home") + "/.compomics/proteins/";
    /**
     * The objects db used to retrieve saved nodes.
     */
    private ObjectsDB objectsDB;
    /**
     * The cache of the objectsDB.
     */
    private ObjectsCache objectsCache = new ObjectsCache();
    /**
     * The splitter in the key between database name and database version.
     */
    public static final String folderSeparator = "_cus_";
    /**
     * The name of the db to use.
     */
    public static final String dbName = "proteinTree";
    /**
     * The name of the node table.
     */
    private static final String nodeTable = "nodes";
    /**
     * The name of the protein length table.
     */
    private static final String parametersTable = "parameters";
    /**
     * List of all tags in tree.
     */
    private HashSet<String> tagsInTree = null;

    /**
     * Constructor.
     */
    private ProteinTreeComponentsFactory() throws IOException {
        objectsCache.setAutomatedMemoryManagement(false); // Change this to true if large objects are stored
        objectsCache.setCacheSize(1000);
        objectsCache.setBatchSize(100); // @TODO: why 100 and not higher?
    }

    /**
     * Static method returning the instance of the factory. Note: the
     * serialization folder should have been already set.
     *
     * @return the instance of the factory
     * @throws IOException if an IOException occurs
     */
    public static ProteinTreeComponentsFactory getInstance() throws IOException {
        if (instance == null) {
            instance = new ProteinTreeComponentsFactory();
        }
        return instance;
    }

    /**
     * Initiates the connection to the database and indicates whether the
     * corresponding folder is already created.
     *
     * @return a boolean indicating whether the database folder is already
     * created
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
    public boolean initiate() throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        File dbFolder = getDbFolder();
        boolean exists = true;

        if (!dbFolder.exists()) {
            exists = false;
            if (!dbFolder.mkdir()) {
                throw new IOException("Impossible to create database folder " + dbFolder.getAbsolutePath() + ".");
            }
        }

        objectsDB = new ObjectsDB(dbFolder.getAbsolutePath(), dbName, false, objectsCache);

        if (!exists) {
            objectsDB.addTable(nodeTable);
            objectsDB.addTable(parametersTable);
        }

        return exists;
    }

    /**
     * Sets the currently loaded database as corrupted and tries to delete it.
     *
     * @return true if deletion was successful
     *
     * @throws java.io.IOException exception thrown whenever an error occurs
     * while interacting with a file
     * @throws java.sql.SQLException exception thrown whenever an error occurs
     * while interacting with the database
     * @throws java.lang.InterruptedException exception thrown whenever a
     * threading error occurs while attempting to delete the database file
     */
    public boolean delete() throws IOException, SQLException, InterruptedException {
        boolean success;
        try {
            if (!isCorrupted()) {
                setCorrupted(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File dbFolder = getDbFolder();
        success = Util.deleteDir(dbFolder);
        return success;
    }

    /**
     * Closes the factory, closes all connection and deletes the file.
     *
     * @throws IOException if an IOException occurs
     * @throws SQLException exception thrown if closing the db failed
     */
    public void close() throws IOException, SQLException {
        if (objectsDB != null) {
            objectsDB.close();
            objectsCache = new ObjectsCache();
        }
    }

    /**
     * Returns the folder name where to store information about the protein
     * sequence database loaded in the sequence factory.
     *
     * @return the folder name where to store information about the protein
     * sequence database loaded in the sequence factory
     */
    public String getDbFolderName() {
        return sequenceFactory.getFileName() + folderSeparator + sequenceFactory.getCurrentFastaIndex().getLastModified();
    }

    /**
     * Returns the folder where the db in the sequence factory is stored.
     *
     * @return the folder where the db in the sequence factory is stored
     * @throws IOException if an IOException occurs
     */
    public File getDbFolder() throws IOException {
        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        File folder = utilitiesUserPreferences.getProteinTreeFolder();
        if (!folder.exists()) {
            boolean success = folder.mkdirs();
            if (!success) {
                throw new IOException("Unable to create database folder " + folder + ".");
            }
        }
        return new File(folder, getDbFolderName());
    }

    /**
     * Adds a node to the database.
     *
     * @param tag the tag referring to the node of interest
     * @param node the node
     * @throws SQLException exception thrown whenever an error occurred while
     * loading data in the database
     * @throws IOException exception thrown whenever an error occurred while
     * loading data in the database
     * @throws InterruptedException if an InterruptedException occurs
     */
    public void saveNode(String tag, Node node) throws SQLException, IOException, InterruptedException {
        objectsDB.insertObject(nodeTable, tag, node, false);
    }

    /**
     * Adds nodes to the database.
     *
     * @param nodes map of the nodes
     * @param waitingHandler the waiting handler
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading data in the database
     * @throws IOException exception thrown whenever an error occurred while
     * loading data in the database
     */
    public void saveNodes(HashMap<String, Object> nodes, WaitingHandler waitingHandler) throws SQLException, IOException {
        objectsDB.insertObjects(nodeTable, nodes, waitingHandler, true);
    }

    /**
     * Retrieves the node of the given tag.
     *
     * @param tag the tag of interest
     * @return the node at this tag
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public Node getNode(String tag) throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        if (tagsInTree != null && !tagsInTree.contains(tag)) {
            return null;
        }
        Node result = (Node) objectsDB.retrieveObject(nodeTable, tag, true, false);
        if (tagsInTree != null && result == null) {
            throw new IllegalArgumentException(tag + " not found in database.");
        }
        return result;
    }

    /**
     * Loads nodes in the cache.
     *
     * @param tags list of tags corresponding to the nodes to load
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void loadNodes(ArrayList<String> tags) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        objectsDB.loadObjects(nodeTable, tags, null);
    }

    /**
     * Saves the initial tag size in the parameters table of the DB.
     *
     * @param size the initial tag size
     *
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void saveInitialSize(int size) throws SQLException, IOException, InterruptedException {
        objectsDB.insertObject(parametersTable, "initialSize", size, false);
    }

    /**
     * Retrieves the initial tag size from the db.
     *
     * @return the initial tag size
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public Integer getInitialSize() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return (Integer) objectsDB.retrieveObject(parametersTable, "initialSize", true);
    }

    /**
     * Loads all tree parameters.
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void loadParameters() throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        objectsDB.loadObjects(parametersTable, null);
    }

    /**
     * Sets whether the import was completed.
     *
     * @param completed whether the import was completed
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void setImportComplete(boolean completed) throws SQLException, IOException, InterruptedException {
        objectsDB.insertObject(parametersTable, "importComplete", completed, false);
    }

    /**
     * Returns a boolean indicating whether the import was complete. False if
     * not set.
     *
     * @return true if the import was complete
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public boolean importComplete() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        Boolean result = (Boolean) objectsDB.retrieveObject(parametersTable, "importComplete", true);
        if (result == null) {
            return false;
        } else {
            return result;
        }
    }

    /**
     * Sets whether the database is corrupted.
     *
     * @param corrupted whether the database is corrupted
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void setCorrupted(boolean corrupted) throws SQLException, IOException, InterruptedException {
        objectsDB.insertObject(parametersTable, "corrupted", corrupted, false);
    }

    /**
     * Returns a boolean indicating whether the database is corrupted. False if
     * not set.
     *
     * @return true if the database is corrupted
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public boolean isCorrupted() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        Boolean result = (Boolean) objectsDB.retrieveObject(parametersTable, "corrupted", true);
        if (result == null) {
            return false;
        } else {
            return result;
        }
    }

    /**
     * Sets the version.
     *
     * @param version the version
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void setVersion(String version) throws SQLException, IOException, InterruptedException {
        objectsDB.insertObject(parametersTable, "version", version, false);
    }

    /**
     * Returns the version. Null if not set.
     *
     * @param objectsDB the objects db to look into
     * @return the version
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public static String getVersion(ObjectsDB objectsDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return (String) objectsDB.retrieveObject(parametersTable, "version", true);
    }

    /**
     * Returns the version. Null if not set.
     *
     * @return the version
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public String getVersion() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getVersion(objectsDB);
    }

    /**
     * Sets the FASTA file path.
     *
     * @param fastaFilePath the FASTA file path
     *
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void setFastaFilePath(String fastaFilePath) throws SQLException, IOException, InterruptedException {
        objectsDB.insertObject(parametersTable, "fastaFile", fastaFilePath, false);
    }

    /**
     * Returns the FASTA file path.
     *
     * @return the FASTA file path
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public String getFastaFilePath() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return getFastaFilePath(objectsDB);
    }

    /**
     * Returns the FASTA file path.
     *
     * @param objectsDB the objects DB to look into
     *
     * @return the FASTA file path
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public static String getFastaFilePath(ObjectsDB objectsDB) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return (String) objectsDB.retrieveObject(parametersTable, "fastaFile", true);
    }

    /**
     * Loads the tags implemented in the database.
     *
     * @throws SQLException if an SQLException occurs
     */
    public void loadTags() throws SQLException {
        tagsInTree = objectsDB.tableContentAsSet(nodeTable);
    }

    /**
     * Returns the default folder to use when storing the trees.
     *
     * @return the default folder to use when storing the trees
     */
    public static String getDefaultDbFolderPath() {
        return defaultDbFolderPath;
    }

    /**
     * Sets the default folder to use when storing the trees.
     *
     * @param defaultDbFolderPath the default folder to use when storing the
     * trees
     */
    public static void setDefaultDbFolderPath(String defaultDbFolderPath) {
        ProteinTreeComponentsFactory.defaultDbFolderPath = defaultDbFolderPath;
    }

    /**
     * Returns the cache used to store the nodes.
     *
     * @return the cache used to store the nodes
     */
    public ObjectsCache getCache() {
        return objectsCache;
    }

    /**
     * Deletes the outdated trees.
     *
     * @throws IOException if an IOException occurs
     */
    public static void deletOutdatedTrees() throws IOException {

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        File folder = utilitiesUserPreferences.getProteinTreeFolder();

        if (folder.exists()) {
            for (File dbFolder : folder.listFiles()) {
                if (dbFolder.isDirectory() && dbFolder.getName().contains(folderSeparator)) {

                    try {
                        ObjectsCache tempCache = new ObjectsCache();
                        ObjectsDB objectsDB = new ObjectsDB(dbFolder.getAbsolutePath(), dbName, false, tempCache);
                        boolean upToDate = true;

                        try {
                            String version = getVersion(objectsDB);

                            if (version != null && version.equals(ProteinTree.version)) {
                                String fastaFilePath = getFastaFilePath(objectsDB);
                                if (fastaFilePath != null) {
                                    File fastaFile = new File(fastaFilePath);
                                    if (!fastaFile.exists()) { //@TODO: check if the drive is available
                                        upToDate = false;
                                    }
                                } else {
                                    upToDate = false;
                                }
                            } else {
                                upToDate = false;
                            }
                        } catch (Exception e) {
                            upToDate = false;
                        }

                        objectsDB.close();

                        if (!upToDate) {
                            DerbyUtil.closeConnection();
                            Util.deleteDir(folder); //TODO: Restore connections?
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Possibly not a tree, skip
                    }
                }
            }
        }
    }
}
