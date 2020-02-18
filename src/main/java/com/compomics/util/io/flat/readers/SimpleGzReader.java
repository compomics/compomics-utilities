package com.compomics.util.io.flat.readers;

import static com.compomics.util.io.IoUtil.ENCODING;
import com.compomics.util.io.flat.SimpleFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

/**
 * Simple wrapper for a gz file reader.
 *
 * @author Marc Vaudel
 */
public class SimpleGzReader implements SimpleFileReader {

    /**
     * The buffered reader.
     */
    private final BufferedReader br;

    /**
     * Constructor.
     *
     * @param file the file to read
     */
    public SimpleGzReader(
            File file
    ) {

        try {

            InputStream fileStream = new FileInputStream(file);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, ENCODING);

            br = new BufferedReader(decoder);

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
