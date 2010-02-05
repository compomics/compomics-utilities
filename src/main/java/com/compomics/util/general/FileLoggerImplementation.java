/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 6-feb-03
 * Time: 8:39:31
 */
package com.compomics.util.general;

import com.compomics.util.interfaces.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements the Logger interface for logging to a file.
 *
 * @author Lennart Martens
 */
public class FileLoggerImplementation implements Logger {

    /**
     * The BufferedWriter connected to the logfile.
     */
    private BufferedWriter iOut = null;

    /**
     * SimpleDateFormat for the logger.
     */
    private SimpleDateFormat iSdf = null;

    /**
     * This constructor takes the name for the log file and sets a
     * default timestamp format: "dd/MM/yyyy - HH:mm:ss".
     *
     * @param   aFilename   String with the filename for the log file.
     *                      Note that if the file already exists, it
     *                      is silently overwritten.
     * @exception   IOException when the file could not be created or written to.
     */
    public FileLoggerImplementation(String aFilename) throws IOException {
        this(aFilename, "dd/MM/yyyy - HH:mm:ss");
    }

    /**
     * This constructor takes the name for the log file and the timestamp format.
     *
     * @param   aFilename   String with the filename for the log file.
     *                      Note that if the file already exists, it
     *                      is silently overwritten.
     * @param   aFormat String with the format for the timestamp.
     * @exception   IOException when the file could not be created or written to.
     */
    public FileLoggerImplementation(String aFilename, String aFormat) throws IOException {
        iOut = new BufferedWriter(new FileWriter(aFilename));
        iSdf = new SimpleDateFormat(aFormat);
    }



    /**
     * This method allows the caller to have the logger record a timestamp (implementation
     * specific format and content, although 'date - time' is a good rule of thumb) along with the
     * specified message.
     *
     * @param   aMessage    String with the message to display after the timestamp.
     */
    public void logTime(String aMessage) {
        try {
            iOut.write(this.getTimestamp() + " " + aMessage + "\n");
            iOut.flush();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method allows the logging of a 'normal' event.
     *
     * @param   aMessage    String with a normal operation message.
     */
    public void logNormalEvent(String aMessage) {
        try {
            iOut.write(" - " + aMessage + "\n");
            iOut.flush();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method allows the logging of an exceptional event.
     *
     * @param   aMessage    String with the exceptional message to log.
     */
    public void logExceptionalEvent(String aMessage) {
        try {
            iOut.write(" * " + this.getTimestamp() + " " + aMessage + "\n");
            iOut.flush();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Called by the garbage collector on an object when garbage collection
     * determines that there are no more references to the object.
     * A subclass overrides the <code>finalize</code> method to dispose of
     * system resources or to perform other cleanup.
     * <p>
     * The general contract of <tt>finalize</tt> is that it is invoked
     * if and when the Java<font size="-2"><sup>TM</sup></font> virtual
     * machine has determined that there is no longer any
     * means by which this object can be accessed by any thread that has
     * not yet died, except as a result of an action taken by the
     * finalization of some other object or class which is ready to be
     * finalized. The <tt>finalize</tt> method may take any action, including
     * making this object available again to other threads; the usual purpose
     * of <tt>finalize</tt>, however, is to perform cleanup actions before
     * the object is irrevocably discarded. For example, the finalize method
     * for an object that represents an input/output connection might perform
     * explicit I/O transactions to break the connection before the object is
     * permanently discarded.
     * <p>
     * The <tt>finalize</tt> method of class <tt>Object</tt> performs no
     * special action; it simply returns normally. Subclasses of
     * <tt>Object</tt> may override this definition.
     * <p>
     * The Java programming language does not guarantee which thread will
     * invoke the <tt>finalize</tt> method for any given object. It is
     * guaranteed, however, that the thread that invokes finalize will not
     * be holding any user-visible synchronization locks when finalize is
     * invoked. If an uncaught exception is thrown by the finalize method,
     * the exception is ignored and finalization of that object terminates.
     * <p>
     * After the <tt>finalize</tt> method has been invoked for an object, no
     * further action is taken until the Java virtual machine has again
     * determined that there is no longer any means by which this object can
     * be accessed by any thread that has not yet died, including possible
     * actions by other objects or classes which are ready to be finalized,
     * at which point the object may be discarded.
     * <p>
     * The <tt>finalize</tt> method is never invoked more than once by a Java
     * virtual machine for any given object.
     * <p>
     * Any exception thrown by the <code>finalize</code> method causes
     * the finalization of this object to be halted, but is otherwise
     * ignored.
     *
     * @throws Throwable the <code>Exception</code> raised by this method
     */
    protected void finalize() throws Throwable {
        this.iOut.flush();
        this.iOut.close();
        this.iOut = null;
        super.finalize();
    }

    /**
     * This method closes the stream to the output file.
     */
    public void close() {
        try {
            this.iOut.flush();
            this.iOut.close();
            this.iOut = null;
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method creates a timestamp in String format.
     *
     * @return  String  with a timestamp.
     */
    private String getTimestamp() {
        long current = System.currentTimeMillis();
        return iSdf.format(new Date(current));
    }
}
