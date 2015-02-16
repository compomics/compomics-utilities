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
    /**
     * Boolean indicating whether a new line should be printed before writing
     * feedback to the user.
     */
    private boolean needNewLine = false;

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
        if (secondaryMaxProgressCounter != 0) {
            int progress1 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
            secondaryProgressCounter++;
            int progress2 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
            printProgress(progress1, progress2);
        } else {
            secondaryProgressCounter++;
        }
    }

    @Override
    public synchronized void setSecondaryProgressCounter(int value) {
        if (secondaryMaxProgressCounter != 0) {
            int progress1 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
            secondaryProgressCounter = value;
            int progress2 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
            printProgress(progress1, progress2);
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
            printProgress(progress1, progress2);
        } else {
            secondaryProgressCounter += amount;
        }
    }

    /**
     * Print the progress to the command line
     *
     * @param progress1 previous progress value
     * @param progress2 current progress value
     */
    private synchronized void printProgress(int progress1, int progress2) {
        if (progress2 > progress1) {
            int progress = 10 * progress2;
            if (progress1 == 0) {
                if (needNewLine) {
                    System.out.append(System.getProperty("line.separator"));
                }
                System.out.print("10%");
                needNewLine = true;
            } else if (progress2 > 99) {
                System.out.print(" " + progress + "%");
                System.out.append(System.getProperty("line.separator"));
                needNewLine = false;
            } else {
                System.out.print(" " + progress + "%");
                needNewLine = true;
            }
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
        if (needNewLine) {
            System.out.append(System.getProperty("line.separator"));
            needNewLine = false;
        }
        System.out.append(tempReport);
    }

    @Override
    public synchronized void appendReportNewLineNoDate() {
        if (needNewLine) {
            System.out.append(System.getProperty("line.separator"));
            needNewLine = false;
        }
        iReport = iReport + System.getProperty("line.separator");
        System.out.append(System.getProperty("line.separator"));
    }

    @Override
    public synchronized void appendReportEndLine() {
        if (needNewLine) {
            System.out.append(System.getProperty("line.separator"));
            needNewLine = false;
        }
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
