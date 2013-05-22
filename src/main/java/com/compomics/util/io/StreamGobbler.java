package com.compomics.util.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A stream gobbler.
 * 
 * @author Davy Maddelein
 */
public class StreamGobbler extends Thread {

    /**
     * The input stream.
     */
    private InputStream is;
    /**
     * The stream type.
     */
    private String type;
    /**
     * The buffered writer.
     */
    private BufferedWriter bw;

    /**
     * Constructor.
     * 
     * @param is the input stream
     * @param type the stream type
     * @param bw the buffered writer
     */
    public StreamGobbler(InputStream is, String type, BufferedWriter bw) {
        this.is = is;
        this.type = type;
        this.bw = bw;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                bw.write(type + ": " + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
