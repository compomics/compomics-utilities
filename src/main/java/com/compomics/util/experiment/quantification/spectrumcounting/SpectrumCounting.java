package com.compomics.util.experiment.quantification.spectrumcounting;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import java.util.ArrayList;

/**
 * This class evaluates the spectrum counting indices for a protein.
 *
 * @author marc
 */
public class SpectrumCounting {
        
    /**
     * The implemented reporter ion quantification methods.
     */
    public enum SpectrumCountingMethod {
        EMPAI, NSAF;
    };
}
