package com.compomics.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A stream gobbler.
 *
 * @author Davy Maddelein
 */
public class StreamGobbler implements Runnable {
    
    /**
     * If the reading is to continue or not.
     */
    private boolean continueReading = true;
    /**
     * The input stream.
     */
    private final InputStream is;
    /**
     * The string builder.
     */
    private final StringBuilder builder = new StringBuilder();

    /**
     * Constructor.
     *
     * @param is the input stream
     */
    public StreamGobbler(InputStream is) {
        this.is = is;
    }
    
    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while (continueReading) {
                if ((line = br.readLine()) != null) {
                    System.out.println(line);
                    builder.append(line);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns the messages.
     *
     * @return the messages
     */
    public String getMessages() {
        return builder.toString();
    }
    
    /**
     * Set if the reading is to continue or not.
     * 
     * @param continueReading if the reading is to continue or not
     */
    public void setContinueReading(boolean continueReading){
        this.continueReading = continueReading; 
    }
}
