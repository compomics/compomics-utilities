package com.compomics.util.db;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Disable the derby log file.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class DerbyUtil {

    /**
     * Indicates whether the connection to Derby is active.
     */
    private static boolean connectionActive = false;
    /**
     * Map of the active connections.
     */
    private static final HashMap<String, ArrayList<String>> activeConnections = new HashMap<String, ArrayList<String>>();

    /**
     * Disable the derby log.
     */
    public static void disableDerbyLog() {
        System.setProperty("derby.stream.error.method", "com.compomics.util.db.DerbyUtil.disabledDerbyLog");
    }

    /**
     * Returns the disabled Derby log file. Not for direct use. Call the method
     * disableDerbyLog instead.
     *
     * @return the disabled Derby log file.
     */
    public static java.io.OutputStream disabledDerbyLog() {
        return new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
        };
    }

    /**
     * Shuts Derby down completely thus releasing the file lock in the database
     * folder.
     */
    public static void closeConnection() {

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
        connectionActive = false;
    }

    /**
     * Returns whether the connection to Derby is active.
     *
     * @return whether the connection to Derby is active
     */
    public static boolean isConnectionActive() {
        return connectionActive;
    }

    /**
     * Sets whether the connection to Derby is active.
     *
     * @param connectionActive whether the connection to Derby is active
     */
    public static void setConnectionActive(boolean connectionActive) {
        DerbyUtil.connectionActive = connectionActive;
    }

    /**
     * Registers a new active connection.
     *
     * @param id the id of this connection
     * @param path the path used for this connection
     */
    public static void addActiveConnection(String id, String path) {
        ArrayList<String> paths = activeConnections.get(id);
        if (paths == null) {
            paths = new ArrayList<String>();
            activeConnections.put(id, paths);
        }
        paths.add(path);
    }

    /**
     * Returns the paths of the active connections for the given id. Null if
     * none found.
     *
     * @param id the id of the connection
     *
     * @return the paths of the active connections
     */
    public static ArrayList<String> getActiveConnectionsPaths(String id) {
        return activeConnections.get(id);
    }

    /**
     * Indicates whether a connection is active.
     *
     * @param id the id of the connection
     * @param path the path of the connection
     *
     * @return whether a connection is active
     */
    public static boolean isActiveConnection(String id, String path) {
        ArrayList<String> paths = activeConnections.get(id);
        if (paths != null) {
            return paths.contains(path);
        }
        return false;
    }

    /**
     * Indicates whether a connection is active.
     *
     * @param path the path of the connection
     *
     * @return whether a connection is active
     */
    public static boolean isActiveConnection(String path) {
        for (ArrayList<String> paths : activeConnections.values()) {
            if (paths.contains(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a connection from the registered connections.
     *
     * @param id the id of the connection
     * @param path the path of the connection
     */
    public static void removeActiveConnection(String id, String path) {
        ArrayList<String> paths = activeConnections.get(id);
        if (paths != null) {
            paths.remove(path);
        }
    }

}
