package com.compomics.util.experiment.io.identifications;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;

import java.util.HashSet;

/**
 * This interface will retrieve spectrum matches from any identification file.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 9:44:07 AM
 */
public interface IdfileReader {

    /**
     * This methods retrieves all the identifications from an identification 
     * file as a list of spectrum matches
     *
     * @return a list of spectrum matches
     */
    public HashSet<SpectrumMatch> getAllSpectrumMatches();
}