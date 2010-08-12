/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 30-jul-2003
 * Time: 12:35:46
 */
package com.compomics.util.gui;
import org.apache.log4j.Logger;

import com.compomics.util.interfaces.Flamable;

import javax.swing.*;
import java.awt.*;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class 
 *
 * @author Lennart
 */
public abstract class FlamableJFrame extends JFrame implements Flamable {

    // Class specific log4j logger for TestFTPClient2 instances.
    Logger logger = Logger.getLogger(FlamableJFrame.class);

    /**
     * Wrapper constructor for that of the superclass.
     * @see javax.swing.JFrame
     */
    public FlamableJFrame() {
        super();
    }

    /**
     * Wrapper constructor for that of the superclass.
     * @see javax.swing.JFrame
     *
     * @param aTitle String with the title for the JFrame.
     */
    public FlamableJFrame(String aTitle) {
        super(aTitle);
    }

    /**
     * Wrapper constructor for that of the superclass.
     * @see javax.swing.JFrame
     *
     * @param aGCfg GraphicsConfiguration for the JFrame.
     */
    public FlamableJFrame(GraphicsConfiguration aGCfg) {
        super(aGCfg);
    }

    /**
     * Wrapper constructor for that of the superclass.
     * @see javax.swing.JFrame
     *
     * @param aGCfg GraphicsConfiguration for the JFrame.
     * @param aTitle String with the title for the JFrame.
     */
    public FlamableJFrame(GraphicsConfiguration aGCfg, String aTitle) {
        super(aTitle, aGCfg);
    }

    /**
     * This method will handle all errors thrown from child threads.
     *
     * @param aThrowable Throwable with the error that occurred.
     */
    public void passHotPotato(Throwable aThrowable) {
        this.passHotPotato(aThrowable, null);
    }

    /**
     * This method takes care of any unrecoverable exception or error, thrown by a child thread.
     *
     * @param aThrowable    Throwable that represents the unrecoverable error or exception.
     * @param aMessage  String with an extra message to display (can be 'null').
     */
    public void passHotPotato(Throwable aThrowable, String aMessage) {
        String[] messages = null;
        if (aMessage != null) {
            messages = new String[]{"Fatal error encountered in application!", aMessage, aThrowable.getMessage(), "\n"};
        } else {
            messages = new String[]{"Fatal error encountered in application!", aThrowable.getMessage(), "\n"};
        }
        logger.error(aThrowable.getMessage(), aThrowable);
        JFrame tempFrame = new JFrame();
        JOptionPane.showMessageDialog(tempFrame, messages, "Application unexpectedly terminated!", JOptionPane.ERROR_MESSAGE);
        tempFrame.dispose();
        // Attempt to clean up, regardless of the error.
        try {
            if (this != null) {
                this.setVisible(false);
                this.dispose();
            }
        } catch (Throwable t) {
            // Whatever.
        }
        if (isStandAlone()) {
            System.exit(1);
        }
    }

    /**
     * Returns true if frame is stand alone.
     *
     * @return true if frame is stand alone
     */
    public abstract boolean isStandAlone();
}
