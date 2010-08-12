/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 29-nov-02
 * Time: 13:54:30
 */
package com.compomics.util.io;
import org.apache.log4j.Logger;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class handles an FTP connection exception.
 *
 * @author Lennart
 */
public class FtpConnectionException extends FtpProtocolException {

    // Class specific log4j logger for FtpConnectionException instances.
    Logger logger = Logger.getLogger(FtpConnectionException.class);
    
    /**
     * Creates a new FtpConnectionException from the given string.
     *
     * @param s the FTP connection exception as a string
     */
    FtpConnectionException(String s) {
        super(s);
    }
}
