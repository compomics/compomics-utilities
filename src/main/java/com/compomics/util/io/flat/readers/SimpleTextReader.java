package com.compomics.util.io.flat.readers;

import static com.compomics.util.io.IoUtil.ENCODING;
import com.compomics.util.io.flat.SimpleFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.commons.io.input.CountingInputStream;

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
     * The length of the file in bytes.
     */
    private final long fileLength;
    /**
     * A stream counting the bytes read.
     */
    private final CountingInputStream countingInputStream;

    /**
     * Constructor.
     *
     * @param file the file to read
     */
    public SimpleTextReader(
            File file
    ) {

        try {

            InputStream fileStream = new FileInputStream(file);
            countingInputStream = new CountingInputStream(fileStream);
            Reader reader = new InputStreamReader(countingInputStream, ENCODING);
            br = new BufferedReader(reader);
            
            fileLength = file.length();

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

    @Override
    public double getProgressInPercent() {
        
        return 100.0 * ((double) countingInputStream.getByteCount()) / fileLength;
        
    }
}
