/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.filters.massspectrometry;

import com.compomics.util.experiment.massspectrometry.MSnSpectrum;

/**
 * This interface represent all spectrum filters which will be used to filter spectra.
 *
 * @author Marc
 */
public interface SpectrumFilter {
    
    /**
     * Indicates whether a spectrum passed the filter
     * 
     * @param spectrum the spectrum to inspect
     * @return a boolean indicating whether a spectrum passed the filter
     */
    public boolean validateSpectrum(MSnSpectrum spectrum);
    
}
