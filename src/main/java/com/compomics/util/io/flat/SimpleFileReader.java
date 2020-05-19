package com.compomics.util.io.flat;

import com.compomics.util.io.flat.readers.SimpleGzReader;
import com.compomics.util.io.flat.readers.SimpleTextReader;
import java.io.File;
import java.io.Reader;

/**
 * Interface for file readers.
 *
 * @author Marc Vaudel
 */
public interface SimpleFileReader extends AutoCloseable {

    /**
     * Returns a file reader, text or gz, according to the extension of the file.
     * 
     * @param file the file
     * 
     * @return a file reader
     */
    public static SimpleFileReader getFileReader(
            File file
    ) {
        
        if (file.getName().endsWith(".gz")) {
            
            return new SimpleGzReader(file);
            
        }
        
        return new SimpleTextReader(file);
        
    }

    /**
     * Reads a line of the file.
     *
     * @return a line of the file
     */
    public String readLine();

    /**
     * Reads characters into an array.
     *
     * @param buffer The destination buffer.
     *
     * @return The number of characters read, -1 if the end of the stream has been reached.
     */
    public int read(
            char[] buffer
    );

    /**
     * Reads characters into an array.
     *
     * @param buffer The destination buffer.
     * @param offset The offset at which to start storing characters
     * @param length The maximum number of characters to read
     *
     * @return The number of characters read, -1 if the end of the stream has been reached.
     */
    public int read(
            char[] buffer, 
            int offset, 
            int length
    );
    
    /**
     * Returns the underlying reader.
     * 
     * @return The underlying reader.
     */
    public Reader getReader();
    
    /**
     * Returns the progress reading the file in percent.
     * 
     * @return The progress reading the file in percent.
     */
    public double getProgressInPercent();

    @Override
    public void close();
}
