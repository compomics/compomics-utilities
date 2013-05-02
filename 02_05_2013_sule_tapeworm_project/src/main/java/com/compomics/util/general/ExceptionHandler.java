package com.compomics.util.general;

import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Handles exception for a given application.
 *
 * @author Marc Vaudel
 */
public class ExceptionHandler {

    /**
     * List of caught exceptions.
     */
    private ArrayList<String> exceptionCaught = new ArrayList<String>();
    /**
     * The parent frame used to display feedback.
     */
    private JFrame parent = null;

    /**
     * Constructor.
     *
     * @param parent the parent frame used to display feedback
     */
    public ExceptionHandler(JFrame parent) {
        this.parent = parent;
    }

    /**
     * Constructor without GUI.
     */
    public ExceptionHandler() {
    }

    /**
     * Method called whenever an exception is caught.
     *
     * @param e the exception caught
     */
    public void catchException(Exception e) {
        if (!exceptionCaught.contains(getExceptionType(e))) {
            e.printStackTrace();
            exceptionCaught.add(getExceptionType(e));
            if (parent != null) {
                if (getExceptionType(e).equals("Protein not found")) {
                    JOptionPane.showMessageDialog(parent,
                            e.getLocalizedMessage() + "\nPlease see the Database help page: "
                            + "http://code.google.com/p/searchgui/wiki/DatabaseHelp.\nThis message will appear only once.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else if (getExceptionType(e).equals("Serialization")) {
                    JOptionPane.showMessageDialog(parent,
                            e.getLocalizedMessage() + "\nPlease see the troubleshooting section "
                            + "in http://peptide-shaker.googlecode.com.\nThis message will appear only once.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {

                    String error = "";

                    if (e.getLocalizedMessage() != null) {
                        error = ": " + e.getLocalizedMessage();
                    }

                    JOptionPane.showMessageDialog(parent,
                            "An error occured" + error + ".\n"
                            + "Please contact the developers.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Returns the exception type.
     *
     * @param e the exception to get the type fro
     * @return the exception type as a string
     */
    public static String getExceptionType(Exception e) {
        if (e.getLocalizedMessage() == null) {
            return "null pointer";
        } else if (e.getLocalizedMessage().startsWith("Protein not found")) {
            return "Protein not found";
        } else if (e.getLocalizedMessage().startsWith("Error while loading")
                || e.getLocalizedMessage().startsWith("Error while writing")) {
            return "Serialization";
        } else {
            return e.getLocalizedMessage();
        }
    }
}
