/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 27-nov-02
 * Time: 13:54:35
 */
package com.compomics.util.interfaces;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This interface describes the behaviour for a class that can log messages.
 * The difference in logging between all the different methods is entirely up to the implementing class!
 * (i.e.: there will be implementations that do not make a difference!)
 *
 * @author Lennart Martens
 */
public interface Logger {

    /**
     * This method allows the caller to have the logger record a timestamp (implementation
     * specific format and content, although 'date - time' is a good rule of thumb) along with the
     * specified message.
     *
     * @param   aMessage    String with the message to display after the timestamp.
     */
    public abstract void logTime(String aMessage);

    /**
     * This method allows the logging of a 'normal' event.
     *
     * @param   aMessage    String with a normal operation message.
     */
    public abstract void logNormalEvent(String aMessage);

    /**
     * This method allows the logging of an exceptional event.
     *
     * @param   aMessage    String with the exceptional message to log.
     */
    public abstract void logExceptionalEvent(String aMessage);
}
