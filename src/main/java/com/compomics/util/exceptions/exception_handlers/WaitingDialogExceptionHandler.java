package com.compomics.util.exceptions.exception_handlers;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingDialog;

/**
 * Exception handler for processes making use of a waiting dialog
 *
 * @author Marc
 */
public class WaitingDialogExceptionHandler extends ExceptionHandler {

    /**
     * The waiting handler used to notify the user.
     */
    private WaitingDialog waitingDialog;
    /**
     * The tool issues page, e.g.,
     * http://code.google.com/p/peptide-shaker/issues/list.
     */
    private String toolIssuesPage;

    /**
     * Constructor.
     * 
     * @param waitingDialog the waiting handler used to notify the user
     * @param toolIssuesPage The tool issues page to refer to
     */
    public WaitingDialogExceptionHandler(WaitingDialog waitingDialog, String toolIssuesPage) {
        this.waitingDialog = waitingDialog;
        this.toolIssuesPage = toolIssuesPage;
    }

    /**
     * Constructor.
     * 
     * @param waitingDialog the waiting handler used to notify the user
     */
    public WaitingDialogExceptionHandler(WaitingDialog waitingDialog) {
        this(waitingDialog, null);
    }

    @Override
    protected void notifyUser(Exception e) {
        waitingDialog.appendReport(e.getLocalizedMessage(), true, true);
        if (getExceptionType(e).equals("Protein not found")) {
            waitingDialog.appendReport("Please see the database help page (http://code.google.com/p/peptide-shaker/#Database_Help).", true, true);
        } else {

            if (toolIssuesPage != null) {
                waitingDialog.appendReport("Please contact the developers (" + toolIssuesPage + ").", true, true);
            } else {
                waitingDialog.appendReport("Please contact the developers.", true, true);
                waitingDialog.appendReport("Please contact the developers.<br>", true, true);
            }
        }
    }
}
