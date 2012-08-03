/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.filters.massspectrometry.spectrumfilters;

import com.compomics.util.experiment.filters.massspectrometry.SpectrumFilter;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * filters according to an m/z - intensity fingerprint
 *
 * @author Marc
 */
public class FingerprintPattern implements SpectrumFilter, Serializable {

    /**
     * Serial number for backward compatibility
     */
    static final long serialVersionUID = 5759594763549860798L;
    /**
     * The actual filter, an and of peak filters
     */
    private And filter;

    /**
     * Constructor
     *
     * @param peaks the list of peaks constituting the pattern to look for
     * @param mzTolerance the m/z tolerance
     * @param isPpm a boolean indicating whether the m/z tolerance is in ppm
     * @param intensityTolerance the intensity relative tolerance (0.1 for 10%)
     */
    public FingerprintPattern(ArrayList<Peak> peaks, double mzTolerance, boolean isPpm, double intensityTolerance) {
        filter = new And();
        for (Peak peak : peaks) {
            filter.addFilter(new PeakFilter(peak.mz, peak.rt, isPpm, mzTolerance, intensityTolerance));
        }
    }

    /**
     * Returns a boolean indicating whether the filter fingerprint was found in
     * the spectrum
     *
     * @param spectrum the spectrum to inspect
     * @return a boolean indicating whether the filter fingerprint was found in
     * the spectrum
     */
    public boolean validateSpectrum(MSnSpectrum spectrum) {
        return filter.validateSpectrum(spectrum);
    }
}
