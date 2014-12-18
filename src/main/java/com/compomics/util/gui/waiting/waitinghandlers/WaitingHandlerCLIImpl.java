package com.compomics.util.gui.waiting.waitinghandlers;

import com.compomics.util.waiting.WaitingHandler;

import java.util.Date;

/**
 * This class is an implementation of the WaitingHandler interface to be used
 * when operating through the Command Line Interface.
 *
 * @author Harald Barsnes
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
    public synchronized void setMaxPrimaryProgressCounter(int maxProgressValue) {
        primaryMaxProgressCounter = maxProgressValue;
    }

    @Override
    public synchronized void increasePrimaryProgressCounter() {
        primaryProgressCounter++;
    }

    @Override
    public synchronized void increasePrimaryProgressCounter(int amount) {
        primaryProgressCounter += amount;
    }

    @Override
    public void setPrimaryProgressCounter(int value) {
        primaryProgressCounter = value;
    }

    @Override
    public synchronized void setMaxSecondaryProgressCounter(int maxProgressValue) {
        secondaryMaxProgressCounter = maxProgressValue;
    }

    @Override
    public synchronized void resetSecondaryProgressCounter() {
        secondaryProgressCounter = 0;
    }

    @Override
    public synchronized void increaseSecondaryProgressCounter() {
        secondaryProgressCounter++;
    }

    @Override
    public synchronized void setSecondaryProgressCounter(int value) {
        if (secondaryMaxProgressCounter != 0) {
            int progress1 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
            secondaryProgressCounter = value;
            int progress2 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
            if (progress2 > progress1) {
                int progress = 10 * progress2;
                if (progress1 == 0) {
                    System.out.println("10%");
                } else if (progress2 == 90) {
                    System.out.println(progress + "%");
                } else if (progress2 == 100) {
                } else {
                    System.out.println(progress + "%");
                }
            }
        } else {
            secondaryProgressCounter = value;
        }
    }

    @Override
    public synchronized void increaseSecondaryProgressCounter(int amount) {
        if (secondaryMaxProgressCounter != 0) {
            int progress1 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
            secondaryProgressCounter += amount;
            int progress2 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
            if (progress2 > progress1) {
                int progress = 10 * progress2;
                if (progress1 == 0) {
                    System.out.println("10%");
                } else if (progress2 == 90) {
                    System.out.println(progress + "%");
                } else if (progress2 == 100) {
                } else {
                    System.out.println(progress + "%");
                }
            }
        } else {
            secondaryProgressCounter += amount;
        }
    }

    @Override
    public synchronized void setSecondaryProgressCounterIndeterminate(boolean indeterminate) {
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
    public synchronized void appendReport(String report, boolean includeDate, boolean addNewLine) {

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
    public synchronized void appendReportNewLineNoDate() {
        iReport = iReport + System.getProperty("line.separator");
        System.out.append(System.getProperty("line.separator"));
    }

    @Override
    public synchronized void appendReportEndLine() {
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
    public synchronized void setPrimaryProgressCounterIndeterminate(boolean indeterminate) {
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
    public synchronized void resetPrimaryProgressCounter() {
        primaryProgressCounter = 0;
    }

    @Override
    public synchronized int getPrimaryProgressCounter() {
        return primaryProgressCounter;
    }

    @Override
    public synchronized int getMaxPrimaryProgressCounter() {
        return primaryMaxProgressCounter;
    }

    @Override
    public synchronized int getSecondaryProgressCounter() {
        return secondaryProgressCounter;
    }

    @Override
    public synchronized int getMaxSecondaryProgressCounter() {
        return secondaryMaxProgressCounter;
    }
}
