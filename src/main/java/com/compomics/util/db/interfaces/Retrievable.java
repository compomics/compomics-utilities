/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-jun-02
 * Time: 16:24:06
 */
package com.compomics.util.db.interfaces;

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
 * This interface indicates that the implementing class can be retrieved from permanent storage.
 *
 * @author  Lennart Martens
 */
public interface Retrievable {

    /**
     * This method allows the implemented object to retrieve its state from the persistent
     * store.
     *
     * @param   aConn   The Connection on which to execute SQL statements.
     *                  It should be an open connection and the implementation
     *                  should refrain from closing it, so the caller can reuse it.
     * @param   aHMKeys HashMap with the 'key-value' pairs necessary for the
     *                  construction of the primary key to be retrieved.
     * @exception   SQLException    When the retrieve fails (e.g.: PK not found).
     */
    public void retrieve(Connection aConn, HashMap aHMKeys) throws SQLException;
}
