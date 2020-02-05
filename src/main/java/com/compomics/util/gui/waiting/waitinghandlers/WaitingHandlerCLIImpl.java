package com.compomics.util.gui.waiting.waitinghandlers;

import com.compomics.util.threading.SimpleSemaphore;
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
     * Empty default constructor
     */
    public WaitingHandlerCLIImpl() {
    }

    /**
     * Boolean indicating whether the process is finished.
     */
    private boolean runFinished = false;
    /**
     * Boolean indicating whether the process is canceled.
     */
    private boolean runCanceled = false;
    /**
     * Set if the waiting handler is to show the progress for the current
     * process or not. Useful when running subprocesses that one wants to be
     * able to cancel but do not want to show the progress for.
     */
    private boolean displayProgress = true;
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
    /**
     * The line break type.
     */
    private final String lineBreak = System.getProperty("line.separator");
    /**
     * Mutex to synchronize multiple threads on the primary error bar.
     */
    private final SimpleSemaphore primaryMutex = new SimpleSemaphore(1);
    /**
     * Mutex to synchronize multiple threads on the secondary error bar.
     */
    private final SimpleSemaphore secondaryMutex = new SimpleSemaphore(1);
    /**
     * Mutex to synchronize multiple threads writing progress.
     */
    private final SimpleSemaphore progressMutex = new SimpleSemaphore(1);
    /**
     * Mutex to synchronize multiple threads writing text.
     */
    private final SimpleSemaphore textMutex = new SimpleSemaphore(1);

    @Override
    public void setMaxPrimaryProgressCounter(int maxProgressValue) {
        if (displayProgress) {
            primaryMaxProgressCounter = maxProgressValue;
        }
    }

    @Override
    public void increasePrimaryProgressCounter() {
        if (displayProgress) {
            increasePrimaryProgressCounter(1);
        }
    }

    @Override
    public void increasePrimaryProgressCounter(int value) {
        
        if (displayProgress) {
        
            primaryMutex.acquire();
            primaryProgressCounter += value;
            primaryMutex.release();
            
        }
    }

    @Override
    public void setPrimaryProgressCounter(
            int value
    ) {
        if (displayProgress) {
            
            primaryMutex.acquire();
            primaryProgressCounter = value;
            primaryMutex.release();
            
        }
    }

    @Override
    public void setMaxSecondaryProgressCounter(
            int maxProgressValue
    ) {
        if (displayProgress) {
    
            secondaryMaxProgressCounter = maxProgressValue;
        
        }
    }

    @Override
    public void resetSecondaryProgressCounter() {
        if (displayProgress) {
            secondaryProgressCounter = 0;
        }
    }

    @Override
    public void increaseSecondaryProgressCounter() {
        
        increaseSecondaryProgressCounter(1);
        
    }

    @Override
    public void setSecondaryProgressCounter(
            int value
    ) {
        
        if (displayProgress) {
            if (secondaryMaxProgressCounter != 0) {
                
                secondaryMutex.acquire();
                int progress1 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
                secondaryProgressCounter = value;
                int progress2 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
                secondaryMutex.release();
                
                printProgress(progress1, progress2);
                
            } else {
                
                secondaryMutex.acquire();
                secondaryProgressCounter = value;
                secondaryMutex.release();
                
            }
        }
    }

    @Override
    public void increaseSecondaryProgressCounter(int value) {
        
        if (displayProgress) {
            
            if (secondaryMaxProgressCounter != 0) {
                
                secondaryMutex.acquire();
                int progress1 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
                secondaryProgressCounter += value;
                int progress2 = (int) 10.0 * secondaryProgressCounter / secondaryMaxProgressCounter;
                secondaryMutex.release();
                
                printProgress(progress1, progress2);
                
            } else {
                
                secondaryMutex.acquire();
                secondaryProgressCounter += value;
                secondaryMutex.release();
            
            }
        }
    }

    /**
     * Print the progress to the command line
     *
     * @param progress1 previous progress value
     * @param progress2 current progress value
     */
    private void printProgress(int progress1, int progress2) {
        
        if (progress2 > progress1) {
            
            progressMutex.acquire();
        
            int progress = 10 * progress2;
            if (progress1 == 0) {
                if (needNewLine) {
                    System.out.append(lineBreak);
                }
                System.out.print("10%");
                needNewLine = true;
            } else if (progress2 > 99) {
                System.out.print(" " + progress + "%");
                System.out.append(lineBreak);
                needNewLine = false;
            } else {
                System.out.print(" " + progress + "%");
                needNewLine = true;
            }
            
            progressMutex.release();
            
        }
    }

    @Override
    public void setSecondaryProgressCounterIndeterminate(
            boolean indeterminate
    ) {
    
        if (displayProgress) {
        
            secondaryProgressCounter = -1;
        
        }
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
        if (displayProgress) {
            
            String tempReport = report;

            if (includeDate) {
                Date date = new Date();
                tempReport = date + " " + report;
            }

            if (addNewLine) {
                tempReport = tempReport + lineBreak;
            }
            
            textMutex.acquire();
            iReport = iReport + tempReport;
            textMutex.release();
            
            if (needNewLine) {
            
                System.out.append(lineBreak);
                needNewLine = false;
            
            }
            
            System.out.append(tempReport);
            
        }
    }

    @Override
    public void appendReportNewLineNoDate() {
        
        if (displayProgress) {
        
            if (needNewLine) {
            
                System.out.append(lineBreak);
                needNewLine = false;
            
            }
            
            textMutex.acquire();
            iReport = iReport + lineBreak;
            textMutex.release();
            
            System.out.append(lineBreak);
        
        }
    }

    @Override
    public void appendReportEndLine() {
        
        if (displayProgress) {
        
            if (needNewLine) {
            
                System.out.append(lineBreak);
                needNewLine = false;
            
            }
            
            textMutex.acquire();
            iReport = iReport + lineBreak;
            textMutex.release();
            
            System.out.append(lineBreak);
        }
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
    
        if (displayProgress) {
        
            appendReport(text, true, true);
        
        }
    }

    @Override
    public void setPrimaryProgressCounterIndeterminate(
            boolean indeterminate
    ) {
        if (displayProgress) {
            if (indeterminate) {
                primaryProgressCounter = -1;
            }
        }
    }

    @Override
    public boolean isReport() {
        return true;
    }

    @Override
    public void setSecondaryProgressText(
            String text
    ) {
        if (displayProgress) {
            appendReport(text, true, true);
        }
    }

    @Override
    public void resetPrimaryProgressCounter() {
        if (displayProgress) {
            primaryProgressCounter = 0;
        }
    }

    @Override
    public int getPrimaryProgressCounter() {
        return primaryProgressCounter;
    }

    @Override
    public int getMaxPrimaryProgressCounter() {
        return primaryMaxProgressCounter;
    }

    @Override
    public int getSecondaryProgressCounter() {
        return secondaryProgressCounter;
    }

    @Override
    public int getMaxSecondaryProgressCounter() {
        return secondaryMaxProgressCounter;
    }

    @Override
    public void setDisplayProgress(boolean displayProgress) {
        this.displayProgress = displayProgress;
    }

    @Override
    public boolean getDisplayProgress() {
        return displayProgress;
    }
}
