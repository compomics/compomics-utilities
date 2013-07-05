package com.compomics.util.gui.waiting.waitinghandlers;

import com.compomics.util.gui.waiting.WaitingHandler;
import static com.compomics.util.gui.waiting.WaitingHandler.tab;

import javax.swing.*;
import java.util.Date;

/**
 * This class is an implementation of the WaitingHandler interface to be used
 * when operating through the Command Line Interface.
 *
 * @author Kenny Helsens
 */
public class WaitingHandlerCLIImpl implements WaitingHandler {

    /**
     * A boolean indicating whether the run was canceled.
     */
    private boolean boolCanceled = false;
    /**
     * The report to append.
     */
    protected String iReport = "";

    @Override
    public void setMaxProgressValue(int maxProgressValue) {
        // not used in command line
    }

    @Override
    public void increaseProgressValue() {
        // not used in command line
    }

    @Override
    public void increaseProgressValue(int amount) {
        // not used in command line
    }

    @Override
    public void setMaxSecondaryProgressValue(int maxProgressValue) {
        // not used in command line
    }

    @Override
    public void resetSecondaryProgressBar() {
        // not used in command line
    }

    @Override
    public void increaseSecondaryProgressValue() {
        // not used in command line
    }

    @Override
    public void setSecondaryProgressValue(int value) {
        // not used in command line
    }

    @Override
    public void increaseSecondaryProgressValue(int amount) {
        // not used in command line
    }

    @Override
    public void setSecondaryProgressDialogIndeterminate(boolean intermediate) {
        // not used in command line
    }

    @Override
    public void setRunFinished() {
        // not used in command line
    }

    @Override
    public void setRunCanceled() {
        boolCanceled = true;
    }

    @Override
    public void appendReport(String report, boolean includeDate, boolean addNewLine) {

        String tempReport = report;

        if (includeDate) {
            Date date = new Date();
            tempReport = date + " " + report;
        }

        if (addNewLine) {
            tempReport = tempReport + System.getProperty("line.separator");
        }

        iReport = iReport + tempReport;
        System.out.append(tempReport);
    }

    @Override
    public void appendReportNewLineNoDate() {
        iReport = iReport + System.getProperty("line.separator");
        System.out.append(System.getProperty("line.separator"));
    }

    @Override
    public void appendReportEndLine() {
        iReport = iReport + System.getProperty("line.separator");
        System.out.append(System.getProperty("line.separator"));
    }

    @Override
    public boolean isRunCanceled() {
        return boolCanceled;
    }

    @Override
    public boolean isRunFinished() {
        // not used in command line
        return false;
    }

    @Override
    public JProgressBar getSecondaryProgressBar() {
        return null;
    }

    @Override
    public void displayMessage(String message, String title, int messageType) {
        System.out.println(tab + message);
        System.out.println(tab + title);
    }

    @Override
    public void displayHtmlMessage(JEditorPane messagePane, String title, int messageType) {
        displayMessage(messagePane.getText(), title, messageType);
    }

    @Override
    public void setWaitingText(String text) {
        System.out.println(tab + text);
    }

    @Override
    public JProgressBar getPrimaryProgressBar() {
        return null;
    }

    @Override
    public void setIndeterminate(boolean indeterminate) {
        // not used in command line
    }

    public boolean isReport() {
        return true;
    }
}
