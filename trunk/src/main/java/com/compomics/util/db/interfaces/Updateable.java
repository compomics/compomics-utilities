/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-jun-02
 * Time: 16:23:17
 * To change template for new interface use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.compomics.util.db.interfaces;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */


/**
 * This interface indicates that the implementing class can be updated in permanent storage.
 *
 * @author  Lennart Martens
 */
public interface Updateable {

    /**
     * This method allows the implemented object to store its altered state in the
     * persistent store.
     *
     * @param   aConn   The Connection on which to execute SQL statements.
     *                  It should be an open connection and the implementation
     *                  should refrain from closing it, so the caller can reuse it.
     * @return  int with the count of the affected rows.
     * @exception   SQLException    When the save fails (e.g.: Connection gone stale).
     */
    public int update(Connection aConn) throws SQLException;
}
