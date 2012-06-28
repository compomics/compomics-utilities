package com.compomics.util.gui.waiting.waitinghandlers;

import com.compomics.util.gui.waiting.WaitingHandler;
import java.awt.Frame;
import java.awt.Image;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * A dialog for displaying information about progress.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class ProgressDialogX extends javax.swing.JDialog implements WaitingHandler {

    /**
     * If set to true, trying to close the progess bar will be ignored. Use this
     * option if the process being monitored can not be stopped. <br> Note:
     * replaced by unstoppable.
     */
    private boolean doNothingOnClose = false;
    /**
     * Set this to true of the process the progress bar is used for is not
     * possible to stop, or not possble to stop nicely. If the user still tries
     * to close the progress bar the a warning message is first shown were the
     * user has to confirm that he/she still wants to close the progress bar.
     */
    private boolean unstoppable = false;
    /**
     * Boolean indicating whether the process has been canceled.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating wheter the process has been completed.
     */
    private boolean finished = false;
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
     * Opens a new ProgressDialogX with a Frame as a parent.
     *
     * @param waitingHandlerParent
     * @param waitingIcon the dialog/frame icon to use when waiting
     * @param normalIcon the dialog/frame icon to use when done
     * @param modal
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
            waitingHandlerParent.setIconImage(waitingIcon);
        }
    }

    /**
     * Opens a new ProgressDialog with a JDialog as a parent.
     *
     * @param waitingHandlerParent
     * @param waitingIcon the dialog/frame icon to use when waiting
     * @param normalIcon the dialog/frame icon to use when done
     * @param modal
     */
    public ProgressDialogX(JDialog waitingHandlerParent, Image normalIcon, Image waitingIcon, boolean modal) {
        super(waitingHandlerParent, modal);
        initComponents();
        setLocationRelativeTo(waitingHandlerParent);

        this.waitingIcon = waitingIcon;
        this.normalIcon = normalIcon;
        this.waitingHandlerParentDialog = waitingHandlerParent;

        // change the icon to a "waiting version"
        if (waitingIcon != null) {
            waitingHandlerParent.setIconImage(waitingIcon);
        }
    }

    /**
     * Opens a new ProgressDialog.
     *
     * @param modal
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
        progressBar.setValue(value);
    }

    /**
     * Increases the progress value by 1.
     *
     * @deprecated use waiting handler method instead
     */
    public void incrementValue() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    /**
     * Increases the progress value by n.
     *
     * @deprecated use waiting handler method instead
     * @param increment the value to increment by
     */
    public void incrementValue(int increment) {
        progressBar.setValue(progressBar.getValue() + increment);
    }

    /**
     * Sets the maximum value of the progress bar.
     *
     * @deprecated use waiting handler method instead
     * @param value the maximum value
     */
    public void setMax(final int value) {
        progressBar.setMaximum(value);
    }

    /**
     * Makes the dialog indeterminate or not indeterminate. Also turns the paint
     * progress string on or off.
     *
     * @param intermidiate
     *
     * @deprecated Replaced by setIndeterminate, from utilities 3.1.17.
     * @see #setIndeterminate(boolean)
     */
    public void setIntermidiate(final boolean intermidiate) {
        progressBar.setStringPainted(!intermidiate);
        progressBar.setIndeterminate(intermidiate);
    }

    /**
     * Makes the dialog indeterminate or not indeterminate. Also turns the paint
     * progress string on or off.
     *
     * @param indeterminate
     */
    public void setIndeterminate(final boolean indeterminate) {
        progressBar.setStringPainted(!indeterminate);
        progressBar.setIndeterminate(indeterminate);
    }

    /**
     * Sets the string to display in the progrss bar. For example to show the
     * name of the file currently being converted.
     *
     * @param currentFileName
     */
    public void setString(final String currentFileName) {
        progressBar.setStringPainted(currentFileName != null);
        progressBar.setString(currentFileName);
    }

    /**
     * This method makes it impossible to close the dialog. Used when the method
     * monitored by the progres bar can not be stopped.
     *
     * @deprecated replace by setUnstoppable
     */
    public void doNothingOnClose() {
        doNothingOnClose = true;
    }

    /**
     * Set this to true of the process the progress bar is used for is not
     * possible to stop, or not possble to stop nicely. If the user still tries
     * to close the progress bar the a warning message is first shown were the
     * user has to confirm that he/she still wants to close the progress bar.
     *
     * @param unstoppable
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
        setTitle("Please Wait");
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
    public void setMaxProgressValue(int maxProgressValue) {
        progressBar.setMaximum(maxProgressValue);
    }

    @Override
    public void increaseProgressValue() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    @Override
    public void increaseProgressValue(int increment) {
        progressBar.setValue(progressBar.getValue() + increment);
    }

    /**
     * Sets the maximal value of the progress bar.
     *
     * @param maxProgressValue the maximal progress value
     */
    public void setMaxSecondaryProgressValue(int maxProgressValue) {
        setMaxProgressValue(maxProgressValue);
    }

    /**
     * resets the value of the progress bar.
     */
    public void resetSecondaryProgressBar() {
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
    }

    /**
     * Increases the progress bar.
     */
    public void increaseSecondaryProgressValue() {
        increaseProgressValue();
    }

    /**
     * Sets the value of the progress bar.
     *
     * @param value the progress value
     */
    public void setSecondaryProgressValue(int value) {
        setValue(value);
    }

    /**
     * Increases the value of the progress bar.
     *
     * @param number the increment number
     */
    public void increaseSecondaryProgressValue(int number) {
        increaseProgressValue(number);
    }

    /**
     * Makes the dialog indeterminate or not indeterminate. Also turns the paint
     * progress string on or off.
     *
     * @param indeterminate
     */
    public void setSecondaryProgressDialogIndeterminate(boolean indeterminate) {
        setIndeterminate(indeterminate);
    }

    @Override
    public void setRunFinished() {

        // change the icon back to the default version
        if (normalIcon != null) {
            if (waitingHandlerParentFrame != null) {
                waitingHandlerParentFrame.setIconImage(normalIcon);
            } else if (waitingHandlerParentDialog != null) {
                waitingHandlerParentDialog.setIconImage(normalIcon);
            }
        }

        finished = true;
        this.setVisible(true); //@TODO: why is it set visible?
        this.dispose();
    }

    @Override
    public void setRunCanceled() {
        
        if (!finished) {

            if (!doNothingOnClose && !unstoppable) {
                canceled = true;
            }

            if (!canceled && unstoppable) {

                int selection = JOptionPane.showConfirmDialog(this,
                        "Cancelling this process is not directly supported.\n"
                        + "Doing so may result in instability or errors.\n\n"
                        + "Do you still want to cancel the process?", 
                        "Cancel Process?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                if (selection == JOptionPane.YES_OPTION) {
                    canceled = true;
                }
            }
        }
    }

    @Override
    public void appendReport(String report) {
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
    public JProgressBar getSecondaryProgressBar() {
        throw new UnsupportedOperationException("This waiting handler has no secondary progress bar.");
    }

    @Override
    public void displayMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    @Override
    public void displayHtmlMessage(JEditorPane messagePane, String title, int messageType) {
        JOptionPane.showMessageDialog(this, messagePane, title, messageType);
    }

    @Override
    public void setWaitingText(String text) {
        // ignore, not implemented for this waiting handler
    }
}
