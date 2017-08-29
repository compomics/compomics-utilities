/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 29-nov-02
 * Time: 13:53:50
 */
package com.compomics.util.io.ftp;
import org.apache.log4j.Logger;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class handles an FTP login exception.
 *
 * @author Lennart
 */
public class FtpLoginException extends FtpProtocolException {

    // Class specific log4j logger for FtpLoginException instances.
    Logger logger = Logger.getLogger(FtpLoginException.class);
    
    /**
     * Creates a new FtpLoginException from the given string.
     * 
     * @param s the FTP login exception as a string
     */
    FtpLoginException(String s)	{
        super(s);
    }
}
