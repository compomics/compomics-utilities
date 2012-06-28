package com.compomics.util.db;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

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
     * The connection.
     */
    private Connection dbConnection;
    /**
     * List of keys too long to create a table.
     */
    private ArrayList<String> longKeys = new ArrayList<String>();
//    PreparedStatement proteinInsert, peptidesInsert, spectrumInsert,
//            proteinSelect, peptidesSelect, spectrumSelect,
//            proteinDelete, peptidesDelete, spectrumDelete,
//            proteinUpdate, peptidesUpdate, spectrumUpdate;
//    boolean spectraTableCreated = false;

    /**
     * Constructor.
     *
     * @param folder absolute path of the folder where to establish the database
     * @param dbName name of the database
     * @throws SQLException
     */
    public ObjectsDB(String folder, String dbName) throws SQLException {
        this.dbName = dbName;
        establishConnection(folder);
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
        Statement stmt = dbConnection.createStatement();
  
        stmt.execute("CREATE table " + tableName + " ("
                    + "NAME    VARCHAR(500),"
                    + "MATCH_BLOB blob(" + blobSize + ")"
                    + ")");

//        if (tableName.equalsIgnoreCase("proteins")) {
//            
//            stmt.execute("CREATE table " + tableName + " ("
//                + "NAME    VARCHAR(500),"
//                + "MATCH_BLOB blob(" + blobSize + ")"
//                + ")");
//            
//            proteinInsert = dbConnection.prepareStatement("INSERT INTO " + "proteins" + " VALUES (?, ?)");
//            proteinSelect = dbConnection.prepareStatement("select MATCH_BLOB from " + "proteins" + " where NAME=?");
//            proteinDelete = dbConnection.prepareStatement("delete from " + "proteins" + " where NAME=?");
//            proteinUpdate = dbConnection.prepareStatement("update " + "proteins" + " set MATCH_BLOB=? where NAME=?");
//
//        } else if (tableName.equalsIgnoreCase("peptides")) {
//            
//            stmt.execute("CREATE table " + tableName + " ("
//                + "NAME    VARCHAR(500),"
//                + "MATCH_BLOB blob(" + blobSize + ")"
//                + ")");
//            
//            peptidesInsert = dbConnection.prepareStatement("INSERT INTO " + "peptides" + " VALUES (?, ?)");
//            peptidesSelect = dbConnection.prepareStatement("select MATCH_BLOB from " + "peptides" + " where NAME=?");
//            peptidesDelete = dbConnection.prepareStatement("delete from " + "peptides" + " where NAME=?");
//            peptidesUpdate = dbConnection.prepareStatement("update " + "peptides" + " set MATCH_BLOB=? where NAME=?");
// 
//        } else {
//            
//            if (!spectraTableCreated) {
//            
//                spectraTableCreated = true;
//                
//                stmt.execute("CREATE table " + "spectra" + " ("
//                    + "NAME    VARCHAR(500),"
//                    + "MATCH_BLOB blob(" + blobSize + ")"
//                    + ")");
//
//                spectrumInsert = dbConnection.prepareStatement("INSERT INTO " + "spectra" + " VALUES (?, ?)");
//                spectrumSelect = dbConnection.prepareStatement("select MATCH_BLOB from " + "spectra" + " where NAME=?");
//                spectrumDelete = dbConnection.prepareStatement("delete from " + "spectra" + " where NAME=?");
//                spectrumUpdate = dbConnection.prepareStatement("update " + "spectra" + " set MATCH_BLOB=? where NAME=?");   
//            }
//        }

        // sqlite
//        stmt.execute("CREATE table \'" + tableName + "\' ("
//                + "NAME    VARCHAR(500),"
//                + "MATCH_BLOB blob"
//                + ");");

        stmt.close();
    }

    /**
     * Stores an object in the desired table.
     *
     * @param tableName the name of the table
     * @param objectKey the key of the object
     * @param object the object to store
     * @throws SQLException exception thrown whenever an error occurred while
     * storing the object
     * @throws IOException exception thrown whenever an error occurred while
     * writing in the database
     */
    public void insertObject(String tableName, String objectKey, Object object) throws SQLException, IOException {
        PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?)"); // derby
        //PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO " + "\'" + tableName + "\' VALUES (?, ?)"); // sqlite

//        PreparedStatement ps;
//
//        if (tableName.equalsIgnoreCase("proteins")) {
//            ps = proteinInsert;
//        } else if (tableName.equalsIgnoreCase("peptides")) {
//            ps = peptidesInsert;
//        } else {
//            ps = spectrumInsert;
//        }

        ps.setString(1, objectKey);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.close();
        ps.setBytes(2, bos.toByteArray());
        ps.executeUpdate();
        //ps.close();
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
        Statement stmt = dbConnection.createStatement();
        ResultSet results = stmt.executeQuery("select MATCH_BLOB from " + tableName + " where NAME='" + objectKey + "'"); // derby
        //ResultSet results = stmt.executeQuery("select MATCH_BLOB from " + "\'" + tableName + "\' where NAME='" + objectKey + "'"); // sqlite

//        PreparedStatement ps;
//
//        if (tableName.equalsIgnoreCase("proteins")) {
//            ps = proteinSelect;
//        } else if (tableName.equalsIgnoreCase("peptides")) {
//            ps = peptidesSelect;
//        } else {
//            ps = spectrumSelect;
//        }
//
//        ps.setString(1, objectKey);
//        ResultSet results = ps.executeQuery();

        if (results.next()) {

            // derby
            Blob tempBlob = results.getBlob(1);
            BufferedInputStream bis = new BufferedInputStream(tempBlob.getBinaryStream());

            // sqlite
            //BufferedInputStream bis = new BufferedInputStream(results.getBinaryStream(1));

            ObjectInputStream in = new ObjectInputStream(bis);
            Object object = in.readObject();
            in.close();
            results.close();
            stmt.close();
            //ps.close();
            return object;
        }

        results.close();
        //ps.close();
        stmt.close();
        return null;
    }

    /**
     * Indicates whether an object is loaded in the given table.
     *
     * @param tableName the table name
     * @param objectKey the object key
     * @return a boolean indicating whether an object is loaded in the given
     * table
     * @throws SQLException exception thrown whenever an exception occurred
     * while interrogating the database
     */
    public boolean inDB(String tableName, String objectKey) throws SQLException {
        Statement stmt = dbConnection.createStatement();
        ResultSet results = stmt.executeQuery("select * from " + tableName + " where NAME='" + objectKey + "'"); // derby
        //ResultSet results = stmt.executeQuery("select * from " + "\'" + tableName + "\'" + " where NAME='" + objectKey + "'"); // sqlite

//        PreparedStatement ps;
//
//        if (tableName.equalsIgnoreCase("proteins")) {
//            ps = proteinSelect;
//        } else if (tableName.equalsIgnoreCase("peptides")) {
//            ps = peptidesSelect;
//        } else {
//            ps = spectrumSelect;
//        }
//
//        ps.setString(1, objectKey);
//        ResultSet results = ps.executeQuery();

        boolean result = results.next();
        results.close();
        //ps.close();
        stmt.close();
        return result;
    }

    /**
     * Deletes an object from the desired table.
     *
     * @param tableName the name of the table
     * @param objectKey the object key
     * @throws SQLException exception thrown whenever an error occurred while
     * interrogating the database
     */
    public void deleteObject(String tableName, String objectKey) throws SQLException {
        Statement stmt = dbConnection.createStatement();
        stmt.executeUpdate("delete from " + tableName + " where NAME='" + objectKey + "'"); // derby
        //stmt.executeUpdate("delete from \'" + tableName + "\' where NAME='" + objectKey + "'"); // sqlite
        stmt.close();

//        PreparedStatement ps;
//
//        if (tableName.equalsIgnoreCase("proteins")) {
//            ps = proteinDelete;
//        } else if (tableName.equalsIgnoreCase("peptides")) {
//            ps = peptidesDelete;
//        } else {
//            ps = spectrumDelete;
//        }
//
//        ps.setString(1, objectKey);
//        ps.executeQuery();
//        ps.close();
    }

    /**
     * Stores an object in the desired table.
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
        PreparedStatement ps = dbConnection.prepareStatement("update " + tableName + " set MATCH_BLOB=? where NAME='" + objectKey + "'"); // derby
        // PreparedStatement ps = dbConnection.prepareStatement("update \'" + tableName + "\' set MATCH_BLOB=? where NAME='" + objectKey + "'"); // sqlite

//        PreparedStatement ps;
//
//        if (tableName.equalsIgnoreCase("proteins")) {
//            ps = proteinUpdate;
//        } else if (tableName.equalsIgnoreCase("peptides")) {
//            ps = peptidesUpdate;
//        } else {
//            ps = spectrumUpdate;
//        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.close();
        
        ps.setBytes(1, bos.toByteArray());
        //ps.setString(2, objectKey);
        ps.executeUpdate();
        //ps.close();
    }

    /**
     * Closes the db connection.
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * closing the database connection
     */
    public void close() throws SQLException {
        dbConnection.close();
        dbConnection = null;
    }

    /**
     * Establishes connection to the database.
     *
     * @param dbFolder the folder where the database is located
     * @throws SQLException exception thrown whenever an error occurred while
     * establishing the connection
     */
    public void establishConnection(String dbFolder) throws SQLException {
        File testFolder = new File(dbFolder, dbName);
        String path = testFolder.getAbsolutePath();

        // derby
        String url = "jdbc:derby:" + path + ";create=true";
        dbConnection = DriverManager.getConnection(url);

        // sqlite
//        try {
//            Class.forName("org.sqlite.JDBC");
//            String url = "jdbc:sqlite:" + path;
//            dbConnection = DriverManager.getConnection(url);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
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
        if (longKeys.contains(tableName)) {
            tableName = longKeys.indexOf(tableName) + "";
        }
        return tableName;
    }
}
