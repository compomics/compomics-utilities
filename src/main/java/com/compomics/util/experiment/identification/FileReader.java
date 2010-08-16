package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;

import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 9:44:07 AM
 * This interface will retrieve spectrum matches from any identification file.
 */
public interface FileReader {

    public HashSet<SpectrumMatch> getAllSpectrumMatches();

}