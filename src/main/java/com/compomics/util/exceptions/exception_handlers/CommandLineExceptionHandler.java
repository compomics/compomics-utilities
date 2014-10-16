package com.compomics.util.exceptions.exception_handlers;

import com.compomics.util.exceptions.ExceptionHandler;
import java.util.ArrayList;

/**
 * Exception handler for command line processes
 *
 * @author Marc
 */
public class CommandLineExceptionHandler extends ExceptionHandler {

    /**
     * List of caught exceptions.
     */
    private ArrayList<String> exceptionCaught = new ArrayList<String>();
    /**
     * Constructor
     */
    public CommandLineExceptionHandler() {
        
    }
    
    @Override
    public void notifyUser(Exception e) {
        // Do something here?
    }

}
