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
import org.apache.commons.io.input.CountingInputStream;

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
     * A stream counting the bytes read.
     */
    private final CountingInputStream countingInputStream;
    /**
     * The length of the file in bytes.
     */
    private final long fileLength;

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
            countingInputStream = new CountingInputStream(fileStream);
            InputStream gzipStream = new GZIPInputStream(countingInputStream);
            Reader reader = new InputStreamReader(gzipStream, ENCODING);

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

    @Override
    public int read(
            char[] buffer
    ) {

        try {

            return br.read(buffer);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read(
            char[] buffer, 
            int offset, 
            int length
    ) {

        try {

            return br.read(
                    buffer, 
                    offset, 
                    length
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
