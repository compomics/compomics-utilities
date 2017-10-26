package com.compomics.util.exceptions.exception_handlers;

import com.compomics.util.exceptions.ExceptionHandler;
import java.util.ArrayList;

/**
 * Exception handler for command line processes.
 *
 * @author Marc Vaudel
 */
public class CommandLineExceptionHandler extends ExceptionHandler {

    /**
     * List of caught exceptions.
     */
    private final ArrayList<String> exceptionCaught = new ArrayList<>();

    /**
     * Constructor.
     */
    public CommandLineExceptionHandler() {

    }

    @Override
    public void notifyUser(Exception e) {
        // Do something here?
    }
}
