package com.compomics.util.experiment.io.biology.protein.iterators;

import com.compomics.util.experiment.io.biology.protein.Header;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.Semaphore;

/**
 * Iterator for the headers of a fasta file. Errors encountered during iteration
 * are passed as runtime exceptions.
 *
 * @author Marc Vaudel
 */
public class HeaderIterator {

    /**
     * Mutex for the buffering of the fasta file.
     */
    private final Semaphore bufferingMutex = new Semaphore(1);
    /**
     * Buffered reader for the fasta file.
     */
    private final BufferedReader br;
    /**
     * The header corresponding to the last protein returned.
     */
    private final Header lastHeader = null;
    /**
     * Boolean indicating whether the end of the file has been reached.
     */
    private boolean endOfFileReached = false;

    /**
     * Constructor without sanity check.
     *
     * @param fastaFile the fasta file
     *
     * @throws FileNotFoundException exception thrown if the file could not be
     * found
     */
    public HeaderIterator(File fastaFile) throws FileNotFoundException {

        br = new BufferedReader(new FileReader(fastaFile));

    }

    /**
     * Returns the next header. Null if none.
     *
     * @return the next header
     */
    public String getNextHeader() {

        try {

            bufferingMutex.acquire();

            if (endOfFileReached) {

                return null;

            }

            String line;
            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.length() > 0) {

                    if (line.charAt(0) == '>') {

                        bufferingMutex.release();
                        
                        return line;

                    }
                }
            }

            br.close();
            endOfFileReached = true;

            bufferingMutex.release();

            return null;

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }
    
    /**
     * Closes the iterator.
     */
    public void close() {
        
        try {
            
            br.close();
            
        } catch (Exception e) {
            
            e.printStackTrace();
            
        }
    }

}
