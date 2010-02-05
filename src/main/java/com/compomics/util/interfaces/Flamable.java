/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-jun-2003
 * Time: 7:21:23
 */
package com.compomics.util.interfaces;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This interface describes the behaviour for classes that can handle unrecoverable errors and
 * exceptions, thrown by child threads etc.
 * The default behaviour of an implementation would be to notify the user of such an event
 * and try to exit the program reasonably decent.
 *
 * @author Lennart Martens
 */
public interface Flamable {

    /**
     * This method takes care of any unrecoverable exception or error, thrown by a child thread.
     *
     * @param aThrowable    Throwable that represents the unrecoverable error or exception.
     */
    public abstract void passHotPotato(Throwable aThrowable);

    /**
     * This method takes care of any unrecoverable exception or error, thrown by a child thread.
     *
     * @param aThrowable    Throwable that represents the unrecoverable error or exception.
     * @param aMessage  String with an extra message to display.
     */
    public abstract void passHotPotato(Throwable aThrowable, String aMessage);
}
