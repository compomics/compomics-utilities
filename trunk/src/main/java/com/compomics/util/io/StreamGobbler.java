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
     * The input stream.
     */
    private InputStream is;
    /**
     * The string builder.
     */
    private StringBuilder builder = new StringBuilder();

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
            String line = "";
            if ((line = br.readLine()) != null) {
                System.out.println(line);
                builder.append(line);
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
}
