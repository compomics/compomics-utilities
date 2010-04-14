/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-dec-02
 * Time: 14:14:26
 */
package com.compomics.util.db;
import org.apache.log4j.Logger;

import com.compomics.util.general.CommandLineParser;

import java.sql.*;
import java.util.Properties;
import java.util.Vector;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class generates an Object that can be used for access to a
 * given table in a JDBC accessible RDBMS.
 *
 * @author Lennart Martens
 */
public class DBAccessorGenerator {
	// Class specific log4j logger for DBAccessorGenerator instances.
	private static Logger logger = Logger.getLogger(DBAccessorGenerator.class);

    /**
     * Defailt constructor.
     */
    public DBAccessorGenerator() {
    }

    /**
     * This method starts the generator for the specified parameters.
     *
     * @param   aDriver String with the JDBC database driver class.
     * @param   aUrl    String with the URL for the database.
     * @param   aTable  String with the tablename to generate the accessor object for.
     * @param   aPackageName    String with the packagename for the generated class. Can be empty String.
     */
    public void startGenerator(String aDriver, String aUrl, String aTable, String aPackageName) throws GeneratorException {
        this.startGenerator(aDriver, aUrl, aTable, null, null, aPackageName, false);
    }

    /**
     * This method starts the generator for the specified parameters.
     *
     * @param   aDriver String with the JDBC database driver class.
     * @param   aUrl    String with the URL for the database.
     * @param   aTable  String with the tablename to generate the accessor object for.
     * @param   aUser   String with the username for the DB connection. This can be 'null'.
     * @param   aPassword   String with the password for the specified user. This can be 'null'.
     * @param   aPackageName    String with the output packagename. This can be empty String.
     * @param   aDebug  boolean to indicate whether output should be given to stdout.
     * @exception   GeneratorException  whenever something goes wrong.
     */
    public void startGenerator(String aDriver, String aUrl, String aTable, String aUser, String aPassword, String aPackageName, boolean aDebug) throws GeneratorException {
        // Okay, first we need to get our hands on a connection.
        Connection lConn = this.getConnection(aDriver, aUrl, aUser, aPassword);

        // Okay, we've got our connection, now get the MetaData.
        DBMetaData dbmd = this.getMetaData(lConn, aTable);

        if(aDebug)logger.info("\n\n" + dbmd.toString() + "\n");
        // Close the connection.
        try {
            lConn.close();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }

        // Generate the accessor class.
        this.generateAccessor(dbmd, aPackageName, aDebug);
    }

    /**
     * The main method allows to run this tool from the command-line.
     */
    public static void main(String[] args) {
        DBAccessorGenerator dba = new DBAccessorGenerator();

        CommandLineParser clp = new CommandLineParser(args, new String[]{"user", "password"});

        // We should have the following parameters:
        //  - DBDriver
        //  - DBURL
        //  - username
        //  - password
        //  - table.
        if(clp.getParameters() == null || clp.getParameters().length < 4) {
            printUsage();
            System.exit(1);
        }
        String drivername = clp.getParameters()[0];
        String dburl = clp.getParameters()[1];
        String tablename = clp.getParameters()[2];
        String packageName = clp.getParameters()[3];

        String username = clp.getOptionParameter("user");
        String password = clp.getOptionParameter("password");

        if(drivername == null || dburl == null || tablename == null || packageName == null) {
            printUsage();
        } else {
            try {
                dba.startGenerator(drivername, dburl, tablename, username, password, packageName, true);
                logger.info("\n\nGeneration complete!\n");
            } catch(GeneratorException ge) {
                logger.error("\nGenerator encountered the following exception: \n\n" + ge.getMessage() + "\n\n");
            }
        }
    }

    /**
     * This method prints the usage of this class to stderr.
     */
    private static void printUsage() {
        logger.error("\n\nUsage:\n");
        logger.error("\tDBAccessGenerator [--user <username> --password <password>] <DBDriver> <DBURL> <tablename> <outputpackage>\n");
    }

    /**
     * This method attempts to create a Database connection.
     *
     * @param   aDriver String with the JDBC database driver class.
     * @param   aUrl    String with the URL for the database.
     * @param   aUser   String with the username for the DB connection. This can be 'null'.
     * @param   aPassword   String with the password for the specified user. This can be 'null'.
     * @exception   GeneratorException  whenever a connection could not be established.
     */
    private Connection getConnection(String aDriver, String aUrl, String aUser, String aPassword) throws GeneratorException {

        Driver d = null;
        Connection lConn = null;

        // Instantiate the Driver.
        try {
            d = (Driver)Class.forName(aDriver).newInstance();
        } catch(ClassNotFoundException cnfe) {
            throw new GeneratorException("Unable to locate driver class ('" + aDriver + "')!", cnfe);
        } catch(InstantiationException ie) {
            throw new GeneratorException("Unable to instantiate driver using default constructor ('" + aDriver + "')!", ie);
        } catch(IllegalAccessException iae) {
            throw new GeneratorException("Unable to access default constructor for driver ('" + aDriver + "')!", iae);
        }

        // Connect (with credentials, if supplied).
        Properties lProps = new Properties();

        if(aUser != null && aPassword != null) {
            lProps.put("user", aUser);
            lProps.put("password", aPassword);
        }

        try {
            lConn = d.connect(aUrl, lProps);
            if(lConn == null) {
                throw new SQLException("Connection was 'null'; perhaps USER and PASSWORD required?!");
            }
        } catch(SQLException sqle) {
            throw new GeneratorException("Unable to connect to database at URL '" + aUrl + "' with driver '" + aDriver + "'!", sqle);
        }

        return lConn;
    }

    /**
     * This method attempts to extract metadata about the table from the DB.
     *
     * @param   lConn   Connection to read from.
     * @param   aTable  String with the tablename.
     * @return  DBMetaData  with the metadata for the specified table.
     * @exception   GeneratorException  when metadata could not be read.
     */
    private DBMetaData getMetaData(Connection lConn, String aTable) throws GeneratorException {
        DBMetaData dbmd = null;

        // Get the metadata.
        DatabaseMetaData meta = null;
        try {
            meta = lConn.getMetaData();
        } catch(SQLException sqle) {
            throw new GeneratorException("Unable to read MetaData on database!", sqle);
        }

        Vector names = new Vector();
        Vector types = new Vector();
        Vector sizes = new Vector();

        // Read columnmetadata for our table.
        try {
            ResultSet rs = meta.getColumns(null, "", aTable, "%");
            boolean once = false;
            while(rs.next()) {
                names.add(rs.getString("COLUMN_NAME"));
                types.add(new Integer(rs.getInt("DATA_TYPE")));
                sizes.add(new Integer(rs.getInt("COLUMN_SIZE")));
                once = true;
            }
            rs.close();

            if(!once) {
                throw new SQLException("No columns found in table '" + aTable + "'!");
            }
        } catch(SQLException sqle) {
            throw new GeneratorException("Unable to get column metadata from database!", sqle);
        }

        // Get the primary key columns.
        Vector pkColumns = new Vector(5, 2);
        try {
            ResultSet rs = meta.getPrimaryKeys(null, "", aTable);
            boolean once = false;
            while(rs.next()) {
                pkColumns.add(rs.getString("COLUMN_NAME"));
                once = true;
            }
            if(!once) {
                throw new SQLException("No primary key columns found in table '" + aTable + "'!");
            }
            rs.close();
        } catch(SQLException sqle) {
            throw new GeneratorException("Unable to get primary key columns from database for table " + aTable + "!", sqle);
        }

        // Initialize metadata wrapper and return.
        dbmd = new DBMetaData(aTable, names, types, sizes, pkColumns);

        return dbmd;
    }

    /**
     * This method generates an accessor class code for a table
     * with the specified metadata.
     *
     * @param   aDBMD   DBMetaData with the metadata for the table to generate the
     *                  accessor for.
     * @param   aPackageName    String with the output package name.
     * @param   aDebug  boolean to indicate whether output to stdout is desired.
     * @exception   GeneratorException  when the accessor ciold not be generated.
     */
    private void generateAccessor(DBMetaData aDBMD, String aPackageName, boolean aDebug) throws GeneratorException {
        DBAccessor dba = new DBAccessor(aDBMD, aPackageName, aDebug);

        // Test output.
        String name = aDBMD.getTableName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase() + "TableAccessor";
        try {
            String outputPath = aPackageName.replace('.', '/').trim();
            if(!outputPath.endsWith("/")) {
                outputPath = outputPath + "/";
            }
            File dir = new File(outputPath);
            if(!dir.exists()) {
                if(!dir.mkdirs()) {
                    throw new GeneratorException("Unable to generate outputpath: " + outputPath + ".");
                }
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath + name + ".java"));
            bw.write(dba.toString());
            bw.flush();
            bw.close();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
