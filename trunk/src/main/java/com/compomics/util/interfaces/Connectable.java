/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-jun-2003
 * Time: 13:56:17
 */
package com.compomics.util.interfaces;
import org.apache.log4j.Logger;

import java.sql.Connection;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This interface describes the behaviour for classes that can accept an
 * incoming DB connection.
 *
 * @author Lennart Martens
 */
public interface Connectable {

    /**
     * This method will be called by the class actually making the connection.
     * It will pass the connection and an identifier String for that connection
     * (typically the name of the database connected to).
     *
     * @param aConn Connection with the DB connection.
     * @param aDBName   String with an identifier for the connection, typically the
     *                  name of the DB connected to.
     */
    public void passConnection(Connection aConn, String aDBName);
}
