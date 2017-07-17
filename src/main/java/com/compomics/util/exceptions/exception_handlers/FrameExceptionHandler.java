package com.compomics.util.exceptions.exception_handlers;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.gui.JOptionEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Handles exception for a given application and displays warnings using
 * dialogs.
 *
 * @author Marc Vaudel
 */
public class FrameExceptionHandler extends ExceptionHandler {

    /**
     * The parent frame used to display feedback.
     */
    private JFrame parent = null;
    /**
     * The tool issues page, e.g.,
     * https://github.com/compomics/peptide-shaker/issues.
     */
    private String toolIssuesPage;

    /**
     * Constructor.
     *
     * @param parent the parent frame used to display feedback
     * @param toolIssuesPage the tool issues page, e.g.,
     * https://github.com/compomics/peptide-shaker/issues
     */
    public FrameExceptionHandler(JFrame parent, String toolIssuesPage) {
        this.parent = parent;
        this.toolIssuesPage = toolIssuesPage;
    }

    @Override
    public void notifyUser(Exception e) {
        if (parent != null) {
            if (getExceptionType(e).equals("Protein not found")) {
                JOptionPane.showMessageDialog(parent, JOptionEditorPane.getJOptionEditorPane(
                        e.getLocalizedMessage() + "<br>"
                        + "Please see the <a href=\"http://compomics.github.io/projects/searchgui/wiki/databasehelp.html\">Database help page</a>.<br>"
                        + "This message will appear only once."),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else if (getExceptionType(e).equals("Serialization")) {
                if (toolIssuesPage != null) {
                    JOptionPane.showMessageDialog(parent, JOptionEditorPane.getJOptionEditorPane(
                            e.getLocalizedMessage() + "<br>"
                            + "Please <a href=\"" + toolIssuesPage + "\">contact the developers</a>.<br>"
                            + "This message will appear only once."),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parent, JOptionEditorPane.getJOptionEditorPane(
                            e.getLocalizedMessage() + "<br>"
                            + "Please contact the developers.<br>"
                            + "This message will appear only once."),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {

                // @TODO: add handling of out of memory errors
                // @TODO: silently ignore some error types? i.e., just send the error to the log file
                String error = "";

                if (e.getLocalizedMessage() != null) {
                    error = e.getLocalizedMessage();
                }

                if (!error.endsWith(".")) {
                    error += ".";
                }

                if (toolIssuesPage != null) {
                    JOptionPane.showMessageDialog(parent, JOptionEditorPane.getJOptionEditorPane(
                            "An error occured: " + error + "<br>"
                            + "If the problem persists, please <a href=\"" + toolIssuesPage + "\">contact the developers</a>."),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parent, JOptionEditorPane.getJOptionEditorPane(
                            "An error occured: " + error + "<br>"
                            + "If the problem persists, please contact the developers."),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
