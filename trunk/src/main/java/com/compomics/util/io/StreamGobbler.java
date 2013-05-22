package com.compomics.util.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Davy
 */
public class StreamGobbler extends Thread {
    private InputStream is;
    private String type;
    private BufferedWriter bw;

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
            while ((line = br.readLine()) != null)
                bw.write(type + ": "+line);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    
}
