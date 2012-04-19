package com.compomics.util.experiment.io.identifications;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import java.io.IOException;

import java.util.HashSet;
import javax.swing.JProgressBar;

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
     * @param jProgressBar a progress bar displaying the progress (can be null)
     * @return a list of spectrum matches
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws IllegalArgumentException exception thrown whenever an error occurred while
     * parsing the file 
     * @throws Exception exception thrown whenever an error occurred while
     * working with the file
     */
    public HashSet<SpectrumMatch> getAllSpectrumMatches(JProgressBar jProgressBar) throws IOException, IllegalArgumentException, Exception;

    /**
     * Closes the file reader.
     *
     * @throws IOException
     */
    public void close() throws IOException;
}