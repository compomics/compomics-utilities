/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-dec-02
 * Time: 14:29:16
 */
package com.compomics.util.db;
import org.apache.log4j.Logger;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class wraps all exception that can occur while generating code.
 *
 * @author Lennart Martens
 */
public class GeneratorException extends Exception {

    // Class specific log4j logger for GeneratorException instances.
    Logger logger = Logger.getLogger(GeneratorException.class);

    /**
     * The nested exception.
     */
    private Exception iNested = null;

    /**
     * This constructor creates an exception with just a message.
     *
     * @param   aMsg    String with the message for the exception.
     */
    public GeneratorException(String aMsg) {
        super(aMsg);
    }

    /**
     * This constructor creates an exception with a message and a nested exception.
     *
     * @param   aMsg    String with the message for the exception.
     * @param   aNested Exception that is nested within this exception.
     */
    public GeneratorException(String aMsg, Exception aNested) {
        this(aMsg);
        this.iNested = aNested;
    }

    /**
     * This method returns a String with the description for this exception.
     *
     * @return  a string representation of this exception.
     */
    public String toString() {
        return this.getMessage();
    }

    /**
     * This method returns a String with the description for this exception.
     *
     * @return  a string representation of this exception.
     */
    public String getMessage() {
        String message = super.getMessage();

        if(this.iNested != null) {
            message += " Nested exception: " + iNested.getMessage() + ".";
        }

        return message;
    }

    /**
     * This method returns the nested exception for this exception, or
     * 'null' if none is present.
     *
     * @return  Exception   with the nested exception in this exception, or
     *                      'null' if none is present.
     */
    public Exception getNestedException() {
        return this.iNested;
    }
}
