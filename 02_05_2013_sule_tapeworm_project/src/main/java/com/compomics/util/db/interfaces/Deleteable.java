/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 15-jul-02
 * Time: 11:33:12
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
 * This interface indicates that the implementing class can be deleted from permanent storage.
 *
 * @author  Lennart Martens
 */
public interface Deleteable {

    /**
     * This method will physically delete the implemented object's data from the
     * persistent store.
     *
     * @param   aConn   The Connection on which to execute SQL statements.
     *                  It should be an open connection and the implementation
     *                  should refrain from closing it, so the caller can reuse it.
     * @return  int with the number of rows affected.
     * @exception   SQLException    When the save fails (e.g.: PK not found).
     */
    public int delete(Connection aConn) throws SQLException;
}
