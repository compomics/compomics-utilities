package com.compomics.util.gui.waiting.waitinghandlers;

import com.compomics.util.waiting.WaitingHandler;

import java.util.Date;

/**
 * This class is an implementation of the WaitingHandler interface to be used
 * when operating through the Command Line Interface.
 *
 * @author Kenny Helsens
 */
public class WaitingHandlerCLIImpl implements WaitingHandler {

    /**
     * Boolean indicating whether the process is finished.
     */
    private boolean runFinished = false;
    /**
     * Boolean indicating whether the process is canceled.
     */
    private boolean runCanceled = false;
    /**
     * The primary progress counter. -1 if indeterminate.
     */
    private int primaryProgressCounter = 0;
    /**
     * The secondary progress counter. -1 if indeterminate.
     */
    private int secondaryProgressCounter = 0;
    /**
     * The primary max progress counter.
     */
    private int primaryMaxProgressCounter = 0;
    /**
     * The secondary max progress counter.
     */
    private int secondaryMaxProgressCounter = 0;
    /**
     * The report to append.
     */
    protected String iReport = "";

    @Override
    public void setMaxPrimaryProgressCounter(int maxProgressValue) {
        primaryMaxProgressCounter = maxProgressValue;
    }

    @Override
    public void increasePrimaryProgressCounter() {
        primaryProgressCounter++;
    }

    @Override
    public void increasePrimaryProgressCounter(int amount) {
        primaryProgressCounter += amount;
    }

    @Override
    public void setMaxSecondaryProgressCounter(int maxProgressValue) {
        secondaryMaxProgressCounter = maxProgressValue;
    }

    @Override
    public void resetSecondaryProgressCounter() {
        secondaryProgressCounter = 0;
    }

    @Override
    public void increaseSecondaryProgressCounter() {
        secondaryProgressCounter++;
    }

    @Override
    public void setSecondaryProgressCounter(int value) {
        secondaryProgressCounter = value;
    }

    @Override
    public void increaseSecondaryProgressCounter(int amount) {
        secondaryProgressCounter += amount;
    }

    @Override
    public void setSecondaryProgressCounterIndeterminate(boolean indeterminate) {
        secondaryProgressCounter = -1;
    }

    @Override
    public void setRunFinished() {
        runFinished = true;
    }

    @Override
    public void setRunCanceled() {
        runCanceled = true;
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
        return runCanceled;
    }

    @Override
    public boolean isRunFinished() {
        return runFinished;
    }

    @Override
    public void setWaitingText(String text) {
        appendReport(text, true, true);
    }

    @Override
    public void setPrimaryProgressCounterIndeterminate(boolean indeterminate) {
        if (indeterminate) {
            primaryProgressCounter = -1;
        }
    }

    @Override
    public boolean isReport() {
        return true;
    }
    
    @Override
    public void setSecondaryProgressText(String text) {
        appendReport(text, true, true);
    }

    @Override
    public void resetPrimaryProgressCounter() {
        primaryProgressCounter = 0;
    }
    
    @Override
    public int getPrimaryProgressCounter(){
        return primaryProgressCounter;
    }

    @Override
    public int getMaxPrimaryProgressCounter(){
        return primaryMaxProgressCounter;
    }

    @Override
    public int getSecondaryProgressCounter(){
        return secondaryProgressCounter;
    }

    @Override
    public int getMaxSecondaryProgressCounter(){
        return secondaryMaxProgressCounter;
    }
}
