/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-jun-02
 * Time: 16:21:40
 * To change template for new interface use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.compomics.util.db.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */


/**
 * This interface indicates that the implementing class can be persisted in permanent storage.
 *
 * @author  Lennart Martens
 */
public interface Persistable {
    /**
     * This method can be called to store the implemnted objects state to a persistent
     * store for the first time (equivalent to SQL 'INSERT').
     *
     * @param   aConn   The Connection on which to execute SQL statements.
     *                  It should be an open connection and the implementation
     *                  should refrain from closing it, so the caller can reuse it.
     * @return  int with the number of affected rows.
     * @exception   SQLException    When the insert fails (e.g.: connection gone stale).
     */
    public int persist(Connection aConn) throws SQLException;

    /**
	 * This method will return the automatically generated key for the insert if
	 * one was triggered, or 'null' otherwise.
	 *
	 * @return	Object[]	with the generated keys.
	 */
	public Object[] getGeneratedKeys();
}
