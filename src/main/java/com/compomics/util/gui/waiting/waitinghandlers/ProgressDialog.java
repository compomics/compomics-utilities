/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 02-Aug-2009
 * Time: 14:20:25
 */
package com.compomics.util.gui.waiting.waitinghandlers;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2009/08/02 13:23:46 $
 */

/**
 * This class implements a modal dialog that is displayed during a longer-lasting task.
 *
 * @author Lennatr Martens
 * @version $Id: ProgressDialog.java,v 1.1 2009/08/02 13:23:46 lennart Exp $
 */
public class ProgressDialog extends JDialog {

    /**
     * Empty default constructor
     */
    public ProgressDialog() {
    }

    // Class specific log4j logger for ProgressDialog instances.
    Logger logger = Logger.getLogger(ProgressDialog.class);

    private final JProgressBar iProgress = new JProgressBar(JProgressBar.HORIZONTAL);

    /**
     * Creates an indeterminate ProgressDialog by default, with the specified title and message.
     * The methods 'setDeterminate()' and 'setIndeterminate()' allow the caller to
     * set the type of progressbar. A determinate progressbar can report meaningfully on the
     * progress.
     * @deprecated use ProgressDialogX instead
     *
     * @param aParent   aParent to link this dialog to.
     * @param aTitle    String with the title for this dialog.
     * @param aMessage  String with the message to display, can be 'null' for no message.
     */
    public ProgressDialog(JFrame aParent, String aTitle, String aMessage) {
        super(aParent, aTitle, true);
        constructGUI(aMessage);
        positionDialog();
    }

    /**
     * This method makes the progressbar indeterminate.
     */
    public void setIndeterminate() {
        iProgress.setIndeterminate(true);
    }

    /**
     * This method sets the progressbar to determinate mode, with the specified
     * minimum and maximum values. It will also set the current value to the minimum.
     *
     * @param aMin  int with the minimal value for the progress.
     * @param aMax  int with the maximal value for the progress.
     */
    public void setDeterminate(int aMin, int aMax) {
        iProgress.setMinimum(aMin);
        iProgress.setMaximum(aMax);
        iProgress.setIndeterminate(false);
        iProgress.setValue(aMin);
    }

    /**
     * Returns the current maximum value for the progressbar.
     * Only returns sensible information if the progressdialog is
     * in determinate mode.
     *
     * @return  int with the maximum value for the progressbar.
     */
    public int getMaximumValue() {
        return iProgress.getMaximum();
    }

    /**
     * Returns the current minimum value for the progressbar.
     * Only returns sensible information if the progressdialog is
     * in determinate mode.
     *
     * @return  int with the minimum value for the progressbar.
     */
    public int getMinimumValue() {
        return iProgress.getMinimum();
    }

    /**
     * This method signals whether the progressdialog is currently in
     * indeterminate ('true') or determinate ('false') mode.
     *
     * @return  boolean that indicates whether the progressdialog
     *                  is currently in indeterminate ('true') or
     *                  determinate ('false') mode.
     */
    public boolean isIndeterminate() {
        return iProgress.isIndeterminate();
    }

    /**
     * This method allows the caller to set the progress on the progressbar to the specified value,
     * along with the specified message. The message can be 'null' for no message. Note that
     * setting the progressbar to maximum value does not automatically make
     * the progressdialog invisible - this task is left to the user of this component!
     *
     * @param aValue    int with the value to set the progress to.
     *                  Will throw IllegalArgumentException if this value is out of bounds
     *                  (less than 'getMinimumValue()' or more than 'getMaximumValue()'. Note that
     *                  setting the progressbar to maximum value does not automatically make
     *                  the progressdialog invisible - this task is left to the user of this component!
     * @param aMessage  String with the message to set on the progressbar, or 'null' for no message.
     */
    public void setProgress(int aValue, String aMessage) {
        if(aValue < iProgress.getMinimum() || aValue > iProgress.getMaximum()) {
            throw new IllegalArgumentException("Your value (" + aValue + ") was out of bounds ("
                    + iProgress.getMinimum() + "-" + iProgress.getMaximum() + ")!");
        }
        iProgress.setValue(aValue);
        if(aMessage != null) {
            iProgress.setStringPainted(true);
            iProgress.setString(aMessage);
        } else {
            iProgress.setStringPainted(false);
        }
    }

    /**
     * This method allows the caller to set a message on the progressbar.
     * Specify a 'null' to remove any messages.
     *
     * @param aMessage  String with message, or 'null' to remove any message.
     */
    public void setMessage(String aMessage) {
        if(aMessage != null) {
            iProgress.setString(aMessage);
            iProgress.setStringPainted(true);
        } else {
            iProgress.setStringPainted(false);
        }
    }

    /**
     * This method reports on whether this instance can be controlled in a
     * multithreaded environment. While this method returns 'false', it is dangerous to
     * alter any settings on this porgressdialog, as the component is not fully drawn
     * and initialized yet!
     *
     * @return  boolean that indicates whether it is safe ('true') or not ('false') for
     *                  callers to interact with this progressdialog.
     */
    public boolean isProgressBarValid() {
        return this.iProgress.isValid();
    }

    /**
     * This method sets the progress to the specified value. This method is only useful
     * if the progressbar is in determinate mode.  Note that
     * setting the progressbar to maximum value does not automatically make
     * the progressdialog invisible - this task is left to the user of this component!
     *
     * @param aValue    int with the value to set the progress to.
     *                  Will throw IllegalArgumentException if this value is out of bounds
     *                  (less than 'getMinimumValue()' or more than 'getMaximumValue()'. Note that
     *                  setting the progressbar to maximum value does not automatically make
     *                  the progressdialog invisible - this task is left to the user of this component!
     */
    public void setProgress(int aValue) {
        this.setProgress(aValue, null);
    }

    /**
     * This method reports on the current progress of the progressdialog. This method
     * can only be relied on when the progressdialog is in determinate mode.
     *
     * @return  int with the current value of the progress.
     */
    public int getProgress() {
        return this.iProgress.getValue();
    }

    /**
     * Method to construct the interface and layout components.
     *
     * @param aMessage  String with the message to display on the progressbar, 'null' for no message.
     */
    private void constructGUI(String aMessage) {

        JPanel jpanHorizontalStrutLeft = new JPanel();
        jpanHorizontalStrutLeft.setLayout(new BoxLayout(jpanHorizontalStrutLeft, BoxLayout.X_AXIS));
        jpanHorizontalStrutLeft.add(Box.createHorizontalStrut(10));

        JPanel jpanHorizontalStrutRight = new JPanel();
        jpanHorizontalStrutRight.setLayout(new BoxLayout(jpanHorizontalStrutRight, BoxLayout.X_AXIS));
        jpanHorizontalStrutRight.add(Box.createHorizontalStrut(10));

        JPanel jpanVerticalStrutTop = new JPanel();
        jpanVerticalStrutTop.setLayout(new BoxLayout(jpanVerticalStrutTop, BoxLayout.Y_AXIS));
        jpanVerticalStrutTop.add(Box.createVerticalStrut(5));

        JPanel jpanVerticalStrutBottom = new JPanel();
        jpanVerticalStrutBottom.setLayout(new BoxLayout(jpanVerticalStrutBottom, BoxLayout.Y_AXIS));
        jpanVerticalStrutBottom.add(Box.createVerticalStrut(5));

        iProgress.setIndeterminate(true);
        setMessage(aMessage);

        JPanel jpanProgress = new JPanel(new BorderLayout());
        jpanProgress.add(jpanVerticalStrutTop, BorderLayout.NORTH);
        jpanProgress.add(jpanVerticalStrutBottom, BorderLayout.SOUTH);
        jpanProgress.add(jpanHorizontalStrutRight, BorderLayout.EAST);
        jpanProgress.add(jpanHorizontalStrutLeft, BorderLayout.WEST);
        jpanProgress.add(iProgress, BorderLayout.CENTER);

        this.getContentPane().add(jpanProgress, BorderLayout.CENTER);
    }

    /**
     * This method positions the progressdialog to the center of the parent component,
     * or roughly the center of the screen if the parent is not yet visible.
     */
    private void positionDialog() {
        // Make the size a bit wider than strictly necessary.
        this.pack();
        this.setSize(this.getSize().width + 50, this.getSize().height);
        if(super.getParent().isVisible()) {
            Point parentLoc = super.getParent().getLocationOnScreen();
            int x = (int)parentLoc.getX();
            int y = (int)parentLoc.getY();
            Dimension parentSize = super.getParent().getSize();
            Point location = new Point(x+(parentSize.width/2)-(this.getSize().width/2), y+(parentSize.height/2));
            this.setLocation(location);
        } else {
            // Invisible parent. Center on screen.
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            this.setLocation((screen.width/2)-(this.getSize().width/2), (screen.height)/2);
        }
    }
}
