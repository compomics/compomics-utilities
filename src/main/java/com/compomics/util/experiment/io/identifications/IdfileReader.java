package com.compomics.util.experiment.io.identifications;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.HashSet;

/**
 * This interface will retrieve spectrum matches from any identification file.
 *
 * @author Marc Vaudel
 */
public interface IdfileReader {


    /**
     * Returns the names and versions of the software used to generate the identification
     * file in a map, e.g., Mascot -> (2.2 and 2.3) and X!Tandem -> Sledgehammer (2013.09.01.1). Null if not known.
     *
     * @return the version of the software used to generate the identification
     * file, null if not known
     */
    public HashMap<String, ArrayList<String>> getSoftwareVersions();

    /**
     * Returns the extension of the file for which this IdfileReader can be
     * used.
     *
     * @return String with the extension (taken to make up the end of the
     * filename) of the file that this IdfileReader can read.
     */
    public String getExtension();
    
    /**
     * Closes the file reader.
     *
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Retrieves all the identifications from an identification file as a list
     * of spectrum matches It is very important to close the file reader after
     * creation.
     *
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The secondary progress methods will be called.
     * @return a list of spectrum matches
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while parsing the file
     * @throws Exception exception thrown whenever an error occurred while
     * working with the file
     */
    public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception;
}
