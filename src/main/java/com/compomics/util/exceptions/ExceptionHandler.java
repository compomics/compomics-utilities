package com.compomics.util.exceptions;

import java.util.HashSet;

/**
 * Interface for the general handling of exceptions.
 *
 * @author Marc Vaudel
 */
public abstract class ExceptionHandler {

    /**
     * List of caught exceptions.
     */
    protected HashSet<String> exceptionCaught = new HashSet<>();
    /**
     * If true exceptions will be ignored.
     */
    protected boolean ignoreExceptions = false;

    /**
     * Catches an exception and informs the user.
     *
     * @param e the exception caught
     */
    public synchronized void catchException(Exception e) {
        if (!ignoreExceptions && !exceptionCaught.contains(getExceptionType(e))) {
            e.printStackTrace();
            exceptionCaught.add(getExceptionType(e));
            notifyUser(e);
        }
    }

    /**
     * Notifies the user that an exception was caught.
     *
     * @param e the exception to catch
     */
    protected abstract void notifyUser(Exception e);

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

    /**
     * Sets whether exceptions should be ignored.
     *
     * @param ignoreExceptions if true exceptions will be ignored
     */
    public void setIgnoreExceptions(boolean ignoreExceptions) {
        this.ignoreExceptions = ignoreExceptions;
    }
}
