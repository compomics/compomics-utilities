package com.compomics.util.io.flat.readers;

import com.compomics.util.io.flat.SimpleFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Simple wrapper for a flat file reader.
 *
 * @author Marc Vaudel
 */
public class SimpleTextReader implements SimpleFileReader {

    /**
     * The buffered reader.
     */
    private final BufferedReader br;

    /**
     * Constructor.
     *
     * @param file the file to read
     */
    public SimpleTextReader(
            File file
    ) {

        try {

            br = new BufferedReader(new FileReader(file));

        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }

    @Override
    public String readLine() {

        try {

            return br.readLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

        try {

            br.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Reader getReader() {
        
        return br;
        
    }
}
