package com.compomics.util.experiment.io.identifications;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.gui.waiting.WaitingHandler;
import java.io.IOException;

import java.util.HashSet;

/**
 * This interface will retrieve spectrum matches from any identification file.
 *
 * @author Marc Vaudel
 */
public interface IdfileReader {

    /**
     * This methods retrieves all the identifications from an identification
     * file as a list of spectrum matches It is very important to close the file
     * reader after creation.
     *
     * @param waitingHandler a waiting handler displaying the progress (can be null). The secondary progress methods will be called.
     * @return a list of spectrum matches
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws IllegalArgumentException exception thrown whenever an error occurred while
     * parsing the file 
     * @throws Exception exception thrown whenever an error occurred while
     * working with the file
     */
    public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception;

    /**
     * This method should return the extension of the file for which this IdfileReader can be used.
     *
     * @return  String with the extension (taken to make up the end of the filename) of the file that this IdfileReader can read.
     */
    public String getExtension();

    /**
     * Closes the file reader.
     *
     * @throws IOException
     */
    public void close() throws IOException;
}