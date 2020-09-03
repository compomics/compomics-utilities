package com.compomics.util.experiment.io.biology.protein.iterators;

import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Semaphore;

/**
 * Iterator for the headers of a FASTA file. Errors encountered during iteration
 * are passed as runtime exceptions.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class HeaderIterator {

    /**
     * Mutex for the buffering of the FASTA file.
     */
    private final Semaphore bufferingMutex = new Semaphore(1);
    /**
     * Buffered reader for the FASTA file.
     */
    private final SimpleFileReader simpleFileReader;
    /**
     * Boolean indicating whether the end of the file has been reached.
     */
    private boolean endOfFileReached = false;

    /**
     * Constructor without sanity check.
     *
     * @param fastaFile the FASTA file
     *
     * @throws FileNotFoundException exception thrown if the file could not be
     * found
     */
    public HeaderIterator(File fastaFile) throws FileNotFoundException {

        simpleFileReader = SimpleFileReader.getFileReader(fastaFile);

    }

    /**
     * Returns the next header.Null if none.
     *
     * @param waitingHandler the waiting handler
     * @return the next header
     */
    public String getNextHeader(WaitingHandler waitingHandler) {

        try {

            bufferingMutex.acquire();

            if (endOfFileReached) {

                return null;

            }

            String line;
            while ((line = simpleFileReader.readLine()) != null) {

                // progress update
                if (waitingHandler != null) {
                    double progress = simpleFileReader.getProgressInPercent();
                    waitingHandler.setSecondaryProgressCounter((int) progress);
                }

                line = line.trim();

                if (line.length() > 0) {

                    if (line.charAt(0) == '>') {

                        bufferingMutex.release();

                        return line;

                    }
                }

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {

                    return null;

                }
            }

            simpleFileReader.close();
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

            simpleFileReader.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

}
