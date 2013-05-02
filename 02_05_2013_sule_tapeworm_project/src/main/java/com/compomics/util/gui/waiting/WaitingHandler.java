package com.compomics.util.gui.waiting;

import java.text.SimpleDateFormat;
import javax.swing.JEditorPane;
import javax.swing.JProgressBar;

/**
 * An interface for code dealing with how to handle information that is
 * displayed to the user during the loading of new projects.
 *
 * @author Harald Barsnes
 */
public interface WaitingHandler {

    /**
     * Sets whether the primary progress bar is indeterminate or not. Also turns
     * the paint progress string on or off.
     *
     * @param indeterminate a boolean indicating whether the primary progress
     * bar shall be indeterminate or not
     */
    public void setIndeterminate(boolean indeterminate);

    /**
     * Set the maximum value of the progress bar.
     *
     * @param maxProgressValue the max value
     */
    public void setMaxProgressValue(int maxProgressValue);

    /**
     * Increase the progress bar value by one "counter".
     */
    public void increaseProgressValue();

    /**
     * Increase the progress bar value by the given number.
     *
     * @param increment the increment to increase the value by
     */
    public void increaseProgressValue(int increment);

    /**
     * Set the maximum value of the secondary progress bar. And resets the value
     * to 0.
     *
     * @param maxProgressValue the max value
     */
    public void setMaxSecondaryProgressValue(int maxProgressValue);

    /**
     * Reset the secondary progress bar value to 0.
     */
    public void resetSecondaryProgressBar();

    /**
     * Increase the secondary progress bar value by one "counter".
     */
    public void increaseSecondaryProgressValue();

    /**
     * Sets the secondary progress bar to the given value.
     *
     * @param value the progress value
     */
    public void setSecondaryProgressValue(int value);

    /**
     * Increase the secondary progress bar value by the given amount.
     *
     * @param amount the amount to increase the value by
     */
    public void increaseSecondaryProgressValue(int amount);

    /**
     * Sets the secondary progress bar to indeterminate or not.
     *
     * @param indeterminate if true, set to indeterminate
     */
    public void setSecondaryProgressDialogIndeterminate(boolean indeterminate);

    /**
     * Set the analysis as finished.
     */
    public void setRunFinished();

    /**
     * Set the analysis as canceled.
     */
    public void setRunCanceled();
    /**
     * Convenience date format.
     */
    public static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
    /**
     * The tab space to add when using tab.
     */
    public static String tab = "        "; // tab could be used, but lenght is locale dependent

    /**
     * Append text to the report.
     *
     * @param report the text to append
     * @param includeDate if the date and time is to be added to the front of
     * the text
     * @param addNewLine add a new line after the text?
     */
    public void appendReport(String report, boolean includeDate, boolean addNewLine);

    /**
     * Append two tabs to the report. No new line.
     */
    public void appendReportNewLineNoDate();

    /**
     * Append a new line to the report.
     */
    public void appendReportEndLine();

    /**
     * Returns true if the run is canceled.
     *
     * @return true if the run is canceled
     */
    public boolean isRunCanceled();
    
    /**
     * Returns true if the run is finished.
     *
     * @return true if the run is finished
     */
    public boolean isRunFinished();

    /**
     * Returns the secondary progress bar for updates from external processes.
     *
     * @return the secondary progress bar, can be null
     */
    public JProgressBar getSecondaryProgressBar();

    /**
     * Returns the primary progress bar for updates from external processes.
     * Warning: shall not be used for command line processes
     *
     * @return the primary progress bar, can be null
     */
    public JProgressBar getPrimaryProgressBar();

    /**
     * Display a given message to the user separately from the main output. For
     * example a warning or error message. Usually in a separate dialog if a
     * graphical waiting handler is used. Warning: shall not be used for command
     * line processes
     *
     * @param message the message to display
     * @param title the title of the message
     * @param messageType the message type in the, e.g.,
     * JOptionPane.INFORMATION_MESSAGE
     */
    public void displayMessage(String message, String title, int messageType);

    /**
     * Display a given HTML containing message to the user separately from the
     * main output. For example a warning or error message. Usually in a
     * separate dialog if a graphical waiting handler is used. The HTML links
     * should be clickable.
     *
     * @param messagePane
     * @param title
     * @param messageType
     */
    public void displayHtmlMessage(JEditorPane messagePane, String title, int messageType);

    /**
     * Sets the text describing what is currently waited for.
     *
     * @param text a text describing what is currently waited for
     */
    public void setWaitingText(String text);
}
