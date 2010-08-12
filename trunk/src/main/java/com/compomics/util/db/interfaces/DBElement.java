/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-jun-02
 * Time: 16:26:33
 */
package com.compomics.util.db.interfaces;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This interface indicates that the implementing class can be stored, manipulated and deleted in a
 * permanent storage (typically a RDBMS).
 *
 * @author  Lennart Martens
 */
public interface DBElement {

    /**
     * This method should be called to fill the DBElement with current data
     * as present in the database.
     * The HashMap should contain the correct keys and corresponding values
     * to construct the primary key for this DBElement. The keys should be stated
     * in the implementation class as public static final variables so errors
     * in these matters can be minimized.
     *
     * @param   aConn   The Connection on which to execute SQL statements.
     *                  It should be an open connection and the implementation
     *                  should refrain from closing it, so the caller can reuse it.
     * @param   ahmKeys HashMap with the 'key-value' pairs necessary for the
     *                  construction of the primary key for the DBElement to be retrieved.
     * @exception   SQLException    When the retrieve fails (e.g.: PK does not exist).
     */
    public void load(Connection aConn, HashMap ahmKeys) throws SQLException;

    /**
     * This method can be called when changes need to be persisted.
     * This method should be able to determine the nature of the persistence
     * operation to be performed (INSERT, UPDATE or DELETE). How this is done
     * is up to the implementation, yet the caller must be assured that the correct
     * operation is executed.
     *
     * @param   aConn   The Connection on which to execute SQL statements.
     *                  It should be an open connection and the implementation
     *                  should refrain from closing it, so the caller can reuse it.
     * @exception   SQLException    When the save fails (e.g.: Connection gone stale).
     */
    public int save(Connection aConn) throws SQLException;

    /**
     * This method provides a convenient short-cut to delete the DBElement from
     * the persistent store. It is particularly interesting to use if the
     * implementation has no clear mechanism to flag a DBElement for deletion,
     * thereby preventing the 'save' method from acting as a DELETE call.
     *
     * @param   aConn   The Connection on which to execute SQL statements.
     *                  It should be an open connection and the implementation
     *                  should refrain from closing it, so the caller can reuse it.
     * @exception   SQLException    When the save fails (e.g.: PK not found).
     */
    public int remove(Connection aConn) throws SQLException;

    /**
     * This method will return a HashMap with all the properties for this DBElement.
     * The keys in this HashMap should be stated as public static final variables on
     * the implementation class.
     *
     * @return  HashMap with all the properties, associated with their corresponding keys,
     *                  as defined in the implementation class.
     */
    public HashMap getAllProperties();

    /**
     * This method signals whether the DBElement has changes that should be persisted.
     *
     * @return boolean  Is this DBElement changed as compared to the original entry,
     *                  retrieved from the DB?
     */
    public boolean isChanged();

    /**
     * This method signals whether the DBElement is marked for deletion.
     *
     * @return boolean  Is this DBElement marked for deletion from the DB?
     */
    public boolean isToBeDeleted();

    /**
     * This method signals whether the DBElement has been deleted in the persistent
     * store.
     *
     * @return boolean  Is this DBElement deleted from the DB?
     */
    public boolean isDeleted();
}
