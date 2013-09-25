package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.Util;
import com.compomics.util.db.ObjectsCache;
import com.compomics.util.db.ObjectsDB;
import com.compomics.util.experiment.identification.SequenceFactory;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
    public static final String dbFolderPath = System.getProperty("user.home") + "/.compomics/proteins/";
    /**
     * Boolean indicating whether the factory is in debug mode.
     */
    private boolean debug = false;
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
    private static final String lengthTable = "lengths";
    /**
     * The name of the protein length table.
     */
    private static final String parametersTable = "parameters";
    /**
     * Boolean to check whether the database has been initialized already.
     */
    private static boolean initialized = false;
    /**
     * Set to keep track of added tags (across threads).
     */
    private static final Set<String> tagsAddedToDb = new HashSet<String>();

    /**
     * Constructor.
     */
    private ProteinTreeComponentsFactory() throws IOException {
        File currentFolder = new File(dbFolderPath);
        if (!currentFolder.exists() && !currentFolder.mkdirs()) {
            throw new IOException("Impossible to create database folder " + dbFolderPath + ".");
        }
        objectsCache.setAutomatedMemoryManagement(false); // Change this to true if large objects are stored
        objectsCache.setCacheSize(15000);
    }

    /**
     * Static method returning the instance of the factory. Note: the
     * serialization folder should have been already set.
     *
     * @return the instance of the factory
     * @throws IOException
     */
    public static ProteinTreeComponentsFactory getInstance() throws IOException {
        if (instance == null) {
            instance = new ProteinTreeComponentsFactory();
        }
        return instance;
    }

    /**
     * Returning whether the factory contains the added tag. Note: this is
     * required for multithreaded saving, to prevent multiple tags from being
     * generated.
     *
     * @return whether the factory contains the added tag
     * @throws IOException
     */
    private boolean containsTag(String tag) {
        return tagsAddedToDb.contains(tag);
    }

    /**
     * Initiates the connection to the database and indicates whether the
     * corresponding folder is already created.
     *
     * @return a boolean indicating whether the database folder is already
     * created
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * attempting to connect to the database
     * @throws IOException exception thrown whenever an error occurred while
     * attempting to connect to the database
     */
    public boolean initiate() throws SQLException, IOException {
        if (!initialized) {
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
                objectsDB.addTable(lengthTable);
                objectsDB.addTable(parametersTable);
            }
            initialized = true;
            return exists;
        }
        return initialized;
    }

    /**
     * Sets the currently loaded database as corrupted and tries to delete it.
     *
     * @return true if deletion was successful
     */
    public boolean delete() {
        try {
            setCorrupted(true);
            initialized = false;
        } finally {
            try {
                close();
            } finally {
                File dbFolder = getDbFolder();
                return Util.deleteDir(dbFolder);
            }
        }
    }

    /**
     * Closes the factory, closes all connection and deletes the file.
     *
     * @throws IOException
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
     */
    public File getDbFolder() {
        return new File(dbFolderPath, getDbFolderName());
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
     */
    public void saveNode(String tag, Node node) throws SQLException, IOException {
        if (!containsTag(tag)) {
            objectsDB.insertObject(nodeTable, tag, node, false);
            tagsAddedToDb.add(tag);
        } else {
            System.out.println(tag = " : This tag has already been added"); // @TODO: this cannot be printed!!!
        }
    }

    /**
     * Adds nodes to the database.
     *
     * @param nodes map of the nodes
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * loading data in the database
     * @throws IOException exception thrown whenever an error occurred while
     * loading data in the database
     */
    public void saveNodes(HashMap<String, Object> nodes) throws SQLException, IOException {
        objectsDB.insertObjects(nodeTable, nodes, null, true);
    }

    /**
     * Retrieves the node of the given tag.
     *
     * @param tag the tag of interest
     * @return the node at this tag
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Node getNode(String tag) throws SQLException, ClassNotFoundException, IOException {
        return (Node) objectsDB.retrieveObject(nodeTable, tag, true, false);
    }

    /**
     * Returns the tags loaded in the database.
     *
     * @return the tags loaded in the database
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Set<String> getTags() throws SQLException, ClassNotFoundException, IOException {
        return new HashSet<String>(objectsDB.tableContent(nodeTable)); //@TODO: should not be done like this!
    }

    /**
     * Adds a protein length to the database.
     *
     * @param accession the protein accession
     * @param length the length
     * @throws SQLException exception thrown whenever an error occurred while
     * loading data in the database
     * @throws IOException exception thrown whenever an error occurred while
     * loading data in the database
     */
    public void saveProteinLength(String accession, int length) throws SQLException, IOException {
        objectsDB.insertObject(lengthTable, accession, length, false);
    }

    /**
     * Retrieves the length of a protein.
     *
     * @param accession the accession of the protein of interest
     * @return the length of this protein
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Integer getProteinLength(String accession) throws SQLException, ClassNotFoundException, IOException {
        return (Integer) objectsDB.retrieveObject(lengthTable, accession, true);
    }

    /**
     * Saves the initial tag size in the parameters table of the DB.
     *
     * @param size the initial tag size
     * @throws SQLException
     * @throws IOException
     */
    public void saveInitialSize(int size) throws SQLException, IOException {
        objectsDB.insertObject(parametersTable, "initialSize", size, false);
    }

    /**
     * Retrieves the initial tag size from the db.
     *
     * @return the initial tag size
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Integer getInitialSize() throws SQLException, IOException, ClassNotFoundException {
        return (Integer) objectsDB.retrieveObject(parametersTable, "initialSize", true);
    }

    /**
     * Loads all protein lengths in cache.
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws InterruptedException
     */
    public void loadProteinLenths() throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        objectsDB.loadObjects(lengthTable, null);
    }

    /**
     * Loads all tree parameters.
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws InterruptedException
     */
    public void loadParameters() throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        objectsDB.loadObjects(parametersTable, null);
    }

    /**
     * Sets whether the import was completed.
     *
     * @param completed
     * @throws SQLException
     * @throws IOException
     */
    public void setImportComplete(boolean completed) throws SQLException, IOException {
        objectsDB.insertObject(parametersTable, "importComplete", completed, false);
    }

    /**
     * Returns a boolean indicating whether the import was complete. False if
     * not set.
     *
     * @return true if the import was complete
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean importComplete() throws SQLException, IOException, ClassNotFoundException {
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
     * @param corrupted
     * @throws SQLException
     * @throws IOException
     */
    public void setCorrupted(boolean corrupted) throws SQLException, IOException {
        objectsDB.insertObject(parametersTable, "corrupted", corrupted, false);
    }

    /**
     * Returns a boolean indicating whether the database is corrupted. False if
     * not set.
     *
     * @return true if the database is corrupted
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean isCorrupted() throws SQLException, IOException, ClassNotFoundException {
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
     * @throws SQLException
     * @throws IOException
     */
    public void setVersion(String version) throws SQLException, IOException {
        objectsDB.insertObject(parametersTable, "version", version, false);
    }

    /**
     * Returns the version. Null if not set.
     *
     * @return the version
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public String getVersion() throws SQLException, IOException, ClassNotFoundException {
        return (String) objectsDB.retrieveObject(parametersTable, "version", true);
    }
}
