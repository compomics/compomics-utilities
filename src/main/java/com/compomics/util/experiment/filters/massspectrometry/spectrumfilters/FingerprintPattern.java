package com.compomics.util.experiment.filters.massspectrometry.spectrumfilters;

import com.compomics.util.experiment.filters.massspectrometry.SpectrumFilter;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.util.ArrayList;

/**
 * Filters according to an m/z - intensity fingerprint.
 *
 * @author Marc Vaudel
 */
public class FingerprintPattern extends SpectrumFilter {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 5759594763549860798L;
    /**
     * The actual filter, an and of peak filters.
     */
    private And filter;

    /**
     * Constructor.
     *
     * @param mzArray list of m/z to look for
     * @param intensityArray list of intensities corresponding to the m/z array
     * @param mzTolerance the m/z tolerance
     * @param isPpm a boolean indicating whether the m/z tolerance is in ppm
     * @param intensityTolerance the intensity relative tolerance (0.1 for 10%)
     */
    public FingerprintPattern(ArrayList<Double> mzArray, ArrayList<Double> intensityArray, double mzTolerance, boolean isPpm, double intensityTolerance) {
        filter = new And();
        for (int i = 0; i < mzArray.size(); i++) {
            filter.addFilter(new PeakFilter(mzArray.get(i), intensityArray.get(i), isPpm, mzTolerance, intensityTolerance));
        }
    }

    /**
     * Returns a boolean indicating whether the filter fingerprint was found in
     * the spectrum.
     *
     * @param spectrum the spectrum to inspect
     * @return a boolean indicating whether the filter fingerprint was found in
     * the spectrum
     */
    public boolean validateSpectrum(MSnSpectrum spectrum) {
        return filter.validateSpectrum(spectrum);
    }

    @Override
    public String getDescription() {
        return filter.getDescription();
    }
}
