package com.compomics.util.gui.waiting.waitinghandlers;

import com.compomics.util.waiting.WaitingHandler;

/**
 * This class is an implementation of the WaitingHandler interface to be used
 * when a process can be canceled, but no monitoring is needed. Hence only the
 * methods related to setting and getting the status of the process are
 * implemented.
 *
 * @author Harald Barsnes
 */
public class WaitingHandlerDummy implements WaitingHandler {

    /**
     * Empty default constructor
     */
    public WaitingHandlerDummy() {
    }

    /**
     * Boolean indicating whether the process is finished.
     */
    private boolean runFinished = false;
    /**
     * Boolean indicating whether the process is canceled.
     */
    private boolean runCanceled = false;

    @Override
    public void setMaxPrimaryProgressCounter(
            int maxProgressValue
    ) {
        // not implemented
    }

    @Override
    public void increasePrimaryProgressCounter() {
        // not implemented
    }

    @Override
    public void increasePrimaryProgressCounter(
            int value
    ) {
        // not implemented
    }

    @Override
    public void setPrimaryProgressCounter(
            int value
    ) {
        // not implemented
    }

    @Override
    public void setMaxSecondaryProgressCounter(
            int maxProgressValue
    ) {
        // not implemented
    }

    @Override
    public void resetSecondaryProgressCounter() {
        // not implemented
    }

    @Override
    public void increaseSecondaryProgressCounter() {
        // not implemented
    }

    @Override
    public void setSecondaryProgressCounter(
            int value
    ) {
        // not implemented
    }

    @Override
    public void increaseSecondaryProgressCounter(
            int value
    ) {
        // not implemented
    }

    @Override
    public void setSecondaryProgressCounterIndeterminate(
            boolean indeterminate
    ) {
        // not implemented
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
    public void appendReport(
            String report, 
            boolean includeDate, 
            boolean addNewLine
    ) {
        // not implemented
    }

    @Override
    public void appendReportNewLineNoDate() {
        // not implemented
    }

    @Override
    public void appendReportEndLine() {
        // not implemented
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
    public void setWaitingText(
            String text
    ) {
        // not implemented
    }

    @Override
    public void setPrimaryProgressCounterIndeterminate(
            boolean indeterminate
    ) {
        // not implemented
    }

    @Override
    public boolean isReport() {
        return false;
    }

    @Override
    public void setSecondaryProgressText(
            String text
    ) {
        // not implemented
    }

    @Override
    public void resetPrimaryProgressCounter() {
        // not implemented
    }

    @Override
    public int getPrimaryProgressCounter() {
        return 0;
    }

    @Override
    public int getMaxPrimaryProgressCounter() {
        return 0;
    }

    @Override
    public int getSecondaryProgressCounter() {
        return 0;
    }

    @Override
    public int getMaxSecondaryProgressCounter() {
        return 0;
    }
    
    @Override
    public void setDisplayProgress(
            boolean displayProgress
    ) {
        // not implemented
    }

    @Override
    public boolean getDisplayProgress() {
        return false;
    }
}
