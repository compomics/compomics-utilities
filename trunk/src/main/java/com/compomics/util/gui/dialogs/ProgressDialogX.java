package com.compomics.util.gui.dialogs;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * A dialog for displaying information about progress.
 *
 * @author Harald Barsnes
 */
public class ProgressDialogX extends javax.swing.JDialog {

    /**
     * The progress dialog parent.
     */
    private ProgressDialogParent progressDialogFrame;
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
     * Opens a new ProgressDialogX with a Frame as a parent.
     *
     * @param parent
     * @param progressDialogFrame
     * @param modal
     */
    public ProgressDialogX(java.awt.Frame parent, ProgressDialogParent progressDialogFrame, boolean modal) {
        super(parent, modal);
        initComponents();
        this.progressDialogFrame = progressDialogFrame;
        setLocationRelativeTo(parent);
    }

    /**
     * Opens a new ProgressDialog with a JDialog as a parent.
     *
     * @param parent
     * @param progressDialogFrame
     * @param modal
     */
    public ProgressDialogX(javax.swing.JDialog parent, ProgressDialogParent progressDialogFrame, boolean modal) {
        super(parent, modal);
        initComponents();
        this.progressDialogFrame = progressDialogFrame;
        setLocationRelativeTo(parent);
    }

    /**
     * Opens a new ProgressDialog with a ProgressDialogParent as a parent.
     *
     * @param parent
     * @param modal
     */
    public ProgressDialogX(ProgressDialogParent parent, boolean modal) {
        this.setModal(true);
        initComponents();
        this.progressDialogFrame = parent;
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
     */
    public void incrementValue() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    /**
     * Increases the progress value by n.
     *
     * @param increment the value to increment by
     */
    public void incrementValue(int increment) {
        progressBar.setValue(progressBar.getValue() + increment);
    }

    /**
     * Sets the maximum value of the progress bar.
     *
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
        if (!doNothingOnClose && !unstoppable) {
            progressDialogFrame.cancelProgress();
            this.setVisible(true);
            this.dispose();
        }

        if (unstoppable) {
            int selection = JOptionPane.showConfirmDialog(this,
                    "Cancelling this process is not directly supported.\n"
                    + "Doing so may result in instability or errors.\n\n"
                    + "Do you still want to cancel the process?", "Cancel Process?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            if (selection == JOptionPane.YES_OPTION) {
                progressDialogFrame.cancelProgress();
                this.setVisible(true);
                this.dispose();
            }
        }
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
}
