/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 27-nov-02
 * Time: 14:29:17
 */
package com.compomics.util.general;
import org.apache.log4j.Logger;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.PrintStream;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements a default logger (for debugging purposes).
 *
 * @author Lennart Martens
 */
public class DefaultOutputLoggerImplementation implements com.compomics.util.interfaces.Logger {
	// Class specific log4j logger for DefaultOutputLoggerImplementation instances.
	org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DefaultOutputLoggerImplementation.class);

    /**
     * The output writer for normal and timed messages.
     */
    private PrintStream out = null;

    /**
     * The output writer for error messages.
     */
    private PrintStream err = null;

    /**
     * Default constructor.
     * Logs to standard out and standard err.
     */
    public DefaultOutputLoggerImplementation() {
        this.out = System.out;
        this.err = System.err;
    }

    /**
     * Constructor that takes two PrintStreams to output to. One for
     * errors, one for normal and timed messages.
     *
     * @param   aOut    PrintStream for normal and timed messages.
     * @param   aErr    PrintStream for error messages.
     */
    public DefaultOutputLoggerImplementation(PrintStream aOut, PrintStream aErr) {
        this.out = aOut;
        this.err = aErr;
    }

    /**
     * This method allows the caller to have the logger record a timestamp (implementation
     * specific format and content, although 'date - time' is a good rule of thumb) along with the
     * specified message.
     *
     * @param   aMessage    String with the message to display after the timestamp.
     */
    public void logTime(String aMessage) {
        long current = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        String output = sdf.format(new Date(current));
        logger.info(output + " : " + aMessage);
    }

    /**
     * This method allows the logging of an exceptional event.
     *
     * @param   aMessage    String with the exceptional message to log.
     */
    public void logExceptionalEvent(String aMessage) {
        logger.error(aMessage);
    }

    /**
     * This method allows the logging of a 'normal' event.
     *
     * @param   aMessage    String with a normal operation message.
     */
    public void logNormalEvent(String aMessage) {
        logger.info(aMessage);
    }
}
