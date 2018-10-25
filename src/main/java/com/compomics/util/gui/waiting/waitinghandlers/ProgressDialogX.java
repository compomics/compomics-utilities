package com.compomics.util.gui.waiting.waitinghandlers;

import com.compomics.util.waiting.WaitingActionListener;
import com.compomics.util.waiting.WaitingHandler;
import java.awt.Frame;
import java.awt.Image;
import javax.swing.*;

/**
 * A dialog for displaying information about progress.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class ProgressDialogX extends javax.swing.JDialog implements WaitingHandler {

    /**
     * Empty default constructor
     */
    public ProgressDialogX() {
    }

    /**
     * If set to true, trying to close the progress bar will be ignored. Use
     * this option if the process being monitored can not be stopped. <br> Note:
     * replaced by unstoppable.
     */
    private boolean doNothingOnClose = false;
    /**
     * Set this to true of the process the progress bar is used for is not
     * possible to stop, or not possible to stop nicely. If the user still tries
     * to close the progress bar the a warning message is first shown were the
     * user has to confirm that he/she still wants to close the progress bar.
     */
    private boolean unstoppable = false;
    /**
     * Boolean indicating whether the process has been canceled.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the process has been completed.
     */
    private boolean finished = false;
    /**
     * Set if the waiting handler is to show the progress for the current
     * process or not. Useful when running subprocesses that one wants to be
     * able to cancel but do not want to show the progress for.
     */
    private boolean displayProgress = true;
    /**
     * The dialog/frame icon to use when waiting.
     */
    private Image waitingIcon;
    /**
     * The dialog/frame icon to use when done.
     */
    private Image normalIcon;
    /**
     * The waitingHandlerParent frame.
     */
    private Frame waitingHandlerParentFrame;
    /**
     * The waitingHandlerParent dialog.
     */
    private JDialog waitingHandlerParentDialog;
    /**
     * The waiting action listener.
     */
    private WaitingActionListener waitingActionListener = null;

    /**
     * Opens a new ProgressDialogX with a Frame as a parent.
     *
     * @param waitingHandlerParent the waiting handler parent
     * @param waitingIcon the frame icon to use when waiting
     * @param normalIcon the frame icon to use when done
     * @param modal if the dialog is to be modal or not
     */
    public ProgressDialogX(Frame waitingHandlerParent, Image normalIcon, Image waitingIcon, boolean modal) {
        super(waitingHandlerParent, modal);
        initComponents();
        setLocationRelativeTo(waitingHandlerParent);

        this.waitingHandlerParentFrame = waitingHandlerParent;
        this.waitingIcon = waitingIcon;
        this.normalIcon = normalIcon;

        // change the icon to a "waiting version"
        if (waitingIcon != null) {
            waitingHandlerParentFrame.setIconImage(waitingIcon);
        }
    }

    /**
     * Opens a new ProgressDialog with a JDialog as a parent.
     *
     * @param waitingHandlerParent the waiting handler parent
     * @param waitingHandlerParentFrame the dialog's parent frame (needed to set
     * the frame icons)
     * @param waitingIcon the frame icon to use when waiting
     * @param normalIcon the frame icon to use when done
     * @param modal if the dialog is to be modal or not
     */
    public ProgressDialogX(JDialog waitingHandlerParent, Frame waitingHandlerParentFrame, Image normalIcon, Image waitingIcon, boolean modal) {
        super(waitingHandlerParent, modal);
        initComponents();
        setLocationRelativeTo(waitingHandlerParent);

        this.waitingIcon = waitingIcon;
        this.normalIcon = normalIcon;
        this.waitingHandlerParentDialog = waitingHandlerParent;
        this.waitingHandlerParentFrame = waitingHandlerParentFrame;

        // change the icon to a "waiting version"
        if (waitingIcon != null) {
            waitingHandlerParentDialog.setIconImage(waitingIcon);
            waitingHandlerParentFrame.setIconImage(waitingIcon);
        }
    }

    /**
     * Opens a new ProgressDialog.
     *
     * @param modal if the dialog is to be modal or not
     */
    public ProgressDialogX(boolean modal) {
        this.setModal(true);
        initComponents();
        setLocationRelativeTo(null);
    }

    /**
     * Sets the progress bar value.
     *
     * @param value the progress bar value
     */
    public void setValue(final int value) {
        if (displayProgress) {
            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setValue(value);
                }
            });
        }
    }

    @Override
    public void setPrimaryProgressCounterIndeterminate(final boolean indeterminate) {
        if (displayProgress) {
            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setStringPainted(!indeterminate);
                    progressBar.setIndeterminate(indeterminate);
                }
            });
        }
    }

    /**
     * Sets the string to display in the progress bar. For example to show the
     * name of the file currently being converted.
     *
     * @param currentFileName the current file name
     */
    public void setString(final String currentFileName) {
        if (displayProgress) {
            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setStringPainted(currentFileName != null);
                    progressBar.setString(currentFileName);
                }
            });
        }
    }

    /**
     * Set this to true of the process the progress bar is used for is not
     * possible to stop, or not possible to stop nicely. If the user still tries
     * to close the progress bar the a warning message is first shown were the
     * user has to confirm that he/she still wants to close the progress bar.
     *
     * @param unstoppable if the current process is unstoppable
     */
    public void setUnstoppable(boolean unstoppable) {
        this.unstoppable = unstoppable;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Please Wait...");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        progressBar.setFont(progressBar.getFont().deriveFont(progressBar.getFont().getSize()-1f));
        progressBar.setStringPainted(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog (if it can be closed).
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        setRunCanceled();
    }//GEN-LAST:event_formWindowClosing
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

    /**
     * Returns the progress bar for updates from external processes.
     *
     * @return the progress bar
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void setMaxPrimaryProgressCounter(final int maxProgressValue) {
        if (displayProgress) {
            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setMaximum(maxProgressValue);
                }
            });
        }
    }

    @Override
    public void increasePrimaryProgressCounter() {
        if (displayProgress) {
            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setValue(progressBar.getValue() + 1);
                }
            });
        }
    }

    @Override
    public void increasePrimaryProgressCounter(final int increment) {
        if (displayProgress) {
            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setValue(progressBar.getValue() + increment);
                }
            });
        }
    }

    /**
     * Sets the maximal value of the progress bar.
     *
     * @param maxProgressValue the maximal progress value
     */
    public void setMaxSecondaryProgressCounter(int maxProgressValue) {
        if (displayProgress) {
            setMaxPrimaryProgressCounter(maxProgressValue);
        }
    }

    /**
     * Resets the value of the progress bar.
     */
    public void resetSecondaryProgressCounter() {
        if (displayProgress) {
            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    finished = false;
                    canceled = false;
                    progressBar.setIndeterminate(false);
                    progressBar.setStringPainted(true);
                    progressBar.setValue(0);

                    // change the icon to a "waiting version"
                    if (waitingIcon != null) {
                        waitingHandlerParentFrame.setIconImage(waitingIcon);
                    }
                }
            });
        }
    }

    /**
     * Sets the value of the progress bar.
     *
     * @param value the progress value
     */
    public void setPrimaryProgressCounter(int value) {
        if (displayProgress) {
            setValue(value);
        }
    }

    /**
     * Increases the progress bar.
     */
    public void increaseSecondaryProgressCounter() {
        if (displayProgress) {
            increasePrimaryProgressCounter();
        }
    }

    /**
     * Sets the value of the progress bar.
     *
     * @param value the progress value
     */
    public void setSecondaryProgressCounter(int value) {
        if (displayProgress) {
            setValue(value);
        }
    }

    /**
     * Increases the value of the progress bar.
     *
     * @param number the increment number
     */
    public void increaseSecondaryProgressCounter(int number) {
        if (displayProgress) {
            increasePrimaryProgressCounter(number);
        }
    }

    /**
     * Makes the dialog indeterminate or not indeterminate. Also turns the paint
     * progress string on or off.
     *
     * @param indeterminate if the progress is indeterminate
     */
    public void setSecondaryProgressCounterIndeterminate(boolean indeterminate) {
        if (displayProgress) {
            setPrimaryProgressCounterIndeterminate(indeterminate);
        }
    }

    @Override
    public void setRunFinished() {

        // change the icon back to the default version
        if (normalIcon != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (waitingHandlerParentDialog != null) {
                        waitingHandlerParentDialog.setIconImage(normalIcon);
                    }
                    waitingHandlerParentFrame.setIconImage(normalIcon);
                }
            });
        }

        finished = true;
        this.dispose();
    }

    @Override
    public void setRunCanceled() {

        if (!finished) {

            if (!canceled && !doNothingOnClose && !unstoppable) {
                canceled = true;
                if (waitingActionListener != null) {
                    waitingActionListener.cancelPressed();
                }
            }

            if (!canceled && unstoppable) {

                int selection = JOptionPane.showConfirmDialog(this,
                        "Cancelling this process is not directly supported.\n"
                        + "Doing so may result in instability or errors.\n\n"
                        + "Do you still want to cancel the process?",
                        "Cancel Process?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                if (selection == JOptionPane.YES_OPTION) {
                    canceled = true;
                    if (waitingActionListener != null) {
                        waitingActionListener.cancelPressed();
                    }
                }
            }
        }
    }

    @Override
    public void appendReport(String report, boolean includeDate, boolean addNewLine) {
        throw new UnsupportedOperationException("This waiting handler has no report.");
    }

    @Override
    public void appendReportNewLineNoDate() {
        throw new UnsupportedOperationException("This waiting handler has no report.");
    }

    @Override
    public void appendReportEndLine() {
        throw new UnsupportedOperationException("This waiting handler has no report.");
    }

    @Override
    public boolean isRunCanceled() {
        return canceled;
    }

    @Override
    public boolean isRunFinished() {
        return finished;
    }

    @Override
    public void setWaitingText(String text) {
        if (displayProgress) {
            setTitle(text);
        }
    }

    @Override
    public boolean isReport() {
        return false;
    }

    /**
     * Adds a waiting action listener.
     *
     * @param waitingActionListener the waiting action listener
     */
    public void addWaitingActionListener(WaitingActionListener waitingActionListener) {
        this.waitingActionListener = waitingActionListener;
    }

    @Override
    public void setSecondaryProgressText(String text) {
        if (displayProgress) {
            setString(text);
        }
    }

    public void resetPrimaryProgressCounter() {
        if (displayProgress) {
            resetSecondaryProgressCounter(); // has only one progress bar
        }
    }

    @Override
    public int getPrimaryProgressCounter() {
        return progressBar.getValue();
    }

    @Override
    public int getMaxPrimaryProgressCounter() {
        return progressBar.getMaximum();
    }

    @Override
    public int getSecondaryProgressCounter() {
        return progressBar.getValue();
    }

    @Override
    public int getMaxSecondaryProgressCounter() {
        return progressBar.getMaximum();
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
